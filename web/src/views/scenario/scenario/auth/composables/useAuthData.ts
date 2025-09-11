import { computed, ref, watch } from 'vue';
import { debounce, throttle } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';
import type { AuthObjectType, AuthSearchParams, DataItem, PermissionMapItem, SearchParams } from '../types';

/**
 * Composable for managing authentication data and API calls
 */
export function useAuthData (projectId: string, authObjectId: string, type: AuthObjectType) {
  // Reactive state
  const loading = ref(true);
  const pageNo = ref(1);
  const pageSize = ref(1);
  const total = ref(0);
  const idList = ref<string[]>([]);
  const dataMap = ref<{ [key: string]: DataItem }>({});
  const permissionsMap = ref<{ [key: string]: PermissionMapItem | undefined }>({});
  const enabledLoadingMap = ref<{ [key: string]: boolean }>({});
  const searchInputValue = ref<string>();

  // Controllers for aborting requests
  let controller: AbortController;
  let authController: AbortController;

  // Updating state for preventing concurrent updates
  let updatingMap: { [key: string]: boolean } = {};

  // Computed properties
  const totalPage = computed(() => Math.ceil(total.value / pageSize.value));

  /**
   * Reset all data to initial state
   */
  const reset = () => {
    controller?.abort();
    authController?.abort();

    loading.value = false;
    pageNo.value = 1;
    total.value = 0;
    searchInputValue.value = undefined;
    idList.value = [];
    dataMap.value = {};
    permissionsMap.value = {};
    enabledLoadingMap.value = {};
    updatingMap = {};
  };

  /**
   * Get search parameters for scenario list API
   */
  const getSearchParams = (): SearchParams => {
    const params: SearchParams = {
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      projectId,
      hasPermission: 'GRANT',
      admin: true
    };

    if (searchInputValue.value) {
      params.filters = [{ key: 'name', op: 'MATCH', value: searchInputValue.value }];
    }

    return params;
  };

  /**
   * Load scenario list data
   */
  const loadScenarioData = async () => {
    if (!authObjectId) {
      loading.value = false;
      return;
    }

    controller = new AbortController();
    const axiosConfig = { signal: controller.signal };

    const params = getSearchParams();
    loading.value = true;

    const [error, { data }] = await scenario.getScenarioList(params, axiosConfig);
    if (error || !data) {
      loading.value = false;
      return;
    }

    total.value = +data.total;
    const dataList = data.list;
    const ids: string[] = [];

    for (let i = 0, len = dataList.length; i < len; i++) {
      const { id } = dataList[i];
      ids.push(id);
      dataMap.value[id] = dataList[i];
      idList.value.push(id);
    }

    if (ids.length) {
      await loadAuthData(ids);
    } else {
      loading.value = false;
    }
  };

  /**
   * Load authentication data for given scenario IDs
   */
  const loadAuthData = async (ids: string[]) => {
    const params: AuthSearchParams = {
      pageSize: 2000, // Large page size for IN query
      authObjectId,
      authObjectType: type,
      filters: [{ key: 'scenarioId', op: 'IN', value: ids }]
    };

    authController = new AbortController();
    const axiosConfig = { signal: authController.signal };

    loading.value = true;
    const [error, { data = { list: [] } }] = await scenario.getScenarioAuthList(params, axiosConfig);
    if (error || !data) {
      loading.value = false;
      return;
    }

    const list = data.list || [];
    if (list.length) {
      for (let i = 0, len = list.length; i < len; i++) {
        const { id, scenarioId, permissions, creatorFlag } = list[i];
        const currentMap = permissionsMap.value[scenarioId];

        if (currentMap) {
          const prevPermissions = currentMap.permissions;
          const curPermissions = permissions.map(item => item.value);
          if (curPermissions.length) {
            prevPermissions.push(...curPermissions);
          }

          const set = new Set(prevPermissions);
          const newPermissions = Array.from(set);

          currentMap.permissions = newPermissions;
          currentMap.creatorFlag = creatorFlag || currentMap.creatorFlag;
        } else {
          permissionsMap.value[scenarioId] = {
            id,
            creatorFlag,
            permissions: permissions.map(item => item.value)
          };
        }
      }
    }

    loading.value = false;
  };

  /**
   * Handle search input change with debounce
   */
  const handleSearchInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
    reset();
    const value = event.target.value.trim();
    searchInputValue.value = value;
    loadScenarioData();
  });

  /**
   * Handle scroll event for pagination
   */
  const handleScroll = throttle(duration.scroll, (event: Event) => {
    if (loading.value || pageNo.value >= totalPage.value) {
      return;
    }

    const target = event.target as HTMLElement;
    const LINE_HEIGHT = 44;
    if ((target.scrollTop + target.clientHeight + LINE_HEIGHT) >= target.scrollHeight) {
      pageNo.value += 1;
      loadScenarioData();
    }
  });

  // Watch for authObjectId changes
  watch(() => authObjectId, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }
    loadScenarioData();
  });

  return {
    // State
    loading,
    pageNo,
    pageSize,
    total,
    idList,
    dataMap,
    permissionsMap,
    enabledLoadingMap,
    searchInputValue,
    totalPage,

    // Methods
    reset,
    loadScenarioData,
    loadAuthData,
    handleSearchInputChange,
    handleScroll,

    // Internal state for external access
    get updatingMap () { return updatingMap; },
    set updatingMap (value) { updatingMap = value; }
  };
}
