import { computed, ref, watch } from 'vue';
import { debounce, throttle } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { script } from '@/api/tester';

/**
 * <p>
 * Composable for managing authentication data including script list, permissions,
 * and pagination logic.
 * </p>
 * <p>
 * Handles data loading, searching, scrolling pagination, and permission updates.
 * </p>
 */
export function useAuthData (projectId: string, authObjectId: string | undefined, type: 'user' | 'dept' | 'group') {
  const LINE_HEIGHT = 44;

  // Data state
  const loading = ref(true);
  const pageNo = ref(1);
  const pageSize = ref(1);
  const total = ref(0);
  const idList = ref<string[]>([]);
  const dataMap = ref<{ [key: string]: any }>({});
  const permissionsMap = ref<{ [key: string]: { id: string; creatorFlag: boolean; permissions: string[] } | undefined }>({});
  const enabledLoadingMap = ref<{ [key: string]: boolean }>({});
  const searchInputValue = ref<string>();

  // Controllers for aborting requests
  let controller: AbortController;
  let authController: AbortController;
  let updatingMap: { [key: string]: boolean } = {};

  // Computed properties
  const totalPage = computed(() => {
    return Math.ceil(total.value / pageSize.value);
  });

  /**
   * <p>
   * Debounced search input handler that resets pagination and reloads data
   * when search term changes.
   * </p>
   */
  const searchInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
    reset();
    const value = event.target.value.trim();
    searchInputValue.value = value;
    loadData();
  });

  /**
   * <p>
   * Loads authentication data for given script IDs by fetching permissions
   * from the server and updating the permissions map.
   * </p>
   */
  const loadAuths = async (ids: string[]) => {
    const params: {
      scriptId: string;
      authObjectType: 'USER' | 'DEPT' | 'GROUP';
      filters?: [{ key: 'scriptId'; op: 'IN'; value: string[]; }]
    } = {
      scriptId: authObjectId!,
      authObjectType: type.toUpperCase() as 'USER' | 'DEPT' | 'GROUP',
      filters: [{ key: 'scriptId', op: 'IN', value: ids }]
    };

    authController = new AbortController();
    const axiosConfig = {
      signal: authController.signal
    };

    loading.value = true;
    const [error, { data = { list: [] } }] = await script.getScriptAuthList(params, axiosConfig);
    if (error || !data) {
      loading.value = false;
      return;
    }

    const list = data.list || [];
    if (list.length) {
      for (let i = 0, len = list.length; i < len; i++) {
        const { id, scriptId, permissions, creatorFlag } = list[i];
        const currentMap = permissionsMap.value[scriptId];
        if (currentMap) {
          const prevPermissions = currentMap.permissions;
          const curPermissions = permissions.map(item => item.value);
          if (curPermissions.length) {
            prevPermissions.push(...curPermissions);
          }

          const set = new Set(prevPermissions);
          const newPermissions = Array.from(set);

          currentMap.permissions = newPermissions;
          currentMap.creatorFlag = creatorFlag || currentMap.permissions;
        } else {
          permissionsMap.value[scriptId] = {
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
   * <p>
   * Builds parameters for script list API call including pagination,
   * search filters, and permission requirements.
   * </p>
   */
  const getParams = () => {
    const params: {
      projectId: string,
      pageNo: number;
      pageSize: number;
      appCode: 'AngusTester',
      hasPermission: string;
      admin: boolean;
      filters?: [{ key: 'name'; op: 'MATCH'; value: string; }];
    } = {
      projectId,
      hasPermission: 'GRANT',
      admin: true,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      appCode: 'AngusTester'
    };

    if (searchInputValue.value) {
      params.filters = [{ key: 'name', op: 'MATCH', value: searchInputValue.value }];
    }

    return params;
  };

  /**
   * <p>
   * Loads script list data with pagination support and triggers
   * authentication data loading for retrieved scripts.
   * </p>
   */
  const loadData = async () => {
    if (!authObjectId) {
      loading.value = false;
      return;
    }

    controller = new AbortController();
    const axiosConfig = {
      signal: controller.signal
    };

    const params = getParams();
    loading.value = true;
    const [error, { data }] = await script.getScriptList(params, axiosConfig);
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
      loadAuths(ids);
    } else {
      loading.value = false;
    }
  };

  /**
   * <p>
   * Throttled scroll handler for implementing infinite scrolling pagination.
   * Loads more data when user scrolls near the bottom of the container.
   * </p>
   */
  const handleScroll = throttle(duration.scroll, (event: Event) => {
    if (loading.value) {
      return;
    }

    if (pageNo.value >= totalPage.value) {
      return;
    }

    const target = event.target as HTMLElement;
    if ((target.scrollTop + target.clientHeight + LINE_HEIGHT) >= target.scrollHeight) {
      pageNo.value += 1;
      loadData();
    }
  });

  /**
   * <p>
   * Debounced resize handler that recalculates page size based on container height
   * and triggers data reload if necessary.
   * </p>
   */
  const resizeHandler = debounce(duration.resize, (containerHeight: number) => {
    if (!containerHeight) {
      return;
    }

    let _pageSize = 1;
    if (containerHeight > LINE_HEIGHT) {
      const quotient = containerHeight / (LINE_HEIGHT);
      if (Number.isInteger(quotient)) {
        _pageSize = quotient + 1;
      } else {
        _pageSize = Math.ceil(quotient);
      }
    }

    if (pageSize.value < _pageSize && idList.value.length < _pageSize && total.value > idList.value.length) {
      pageSize.value = _pageSize;
      reset();
      loadData();
    }
  });

  /**
   * <p>
   * Resets all data state and aborts ongoing requests.
   * Called when search term changes or component unmounts.
   * </p>
   */
  const reset = () => {
    controller && controller.abort();
    authController && authController.abort();

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
   * <p>
   * Initializes page size based on container height and starts data loading.
   * Sets up watchers for authObjectId changes.
   * </p>
   */
  const initialize = (containerHeight: number) => {
    let _pageSize = 1;
    if (containerHeight > LINE_HEIGHT) {
      const quotient = containerHeight / (LINE_HEIGHT);
      if (Number.isInteger(quotient)) {
        _pageSize = quotient + 1;
      } else {
        _pageSize = Math.ceil(quotient);
      }
    }

    pageSize.value = _pageSize;
    loadData();
  };

  // Watch for authObjectId changes
  watch(() => authObjectId, (newValue) => {
    reset();
    if (!newValue) {
      loading.value = false;
      return;
    }
    // Ensure data is loaded when authObjectId changes
    loadData();
  }, { immediate: true });

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
    updatingMap,

    // Methods
    searchInputChange,
    loadAuths,
    getParams,
    loadData,
    handleScroll,
    resizeHandler,
    reset,
    initialize
  };
}
