import { computed, inject, ref, Ref } from 'vue';
import { debounce, throttle } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { space } from '@/api/storage';
import type { SpaceItem, AuthSetProps } from '../types';

export function useAuthSet (props: AuthSetProps) {
  const LINE_HEIGHT = 44;
  // Inject project information
  const projectId = inject<Ref<string>>('projectId', ref(''));

  // Controllers for aborting requests
  let controller: AbortController; // For terminating space tree requests
  let authController: AbortController; // For terminating permission requests

  // Reactive state
  const loading = ref(true);
  const pageNo = ref(1);
  const pageSize = ref(1);
  const total = ref(0);
  const idList = ref<string[]>([]);
  const dataMap = ref<{ [key: string]: SpaceItem }>({});
  const permissionsMap = ref<{ [key: string]: { id: string; creatorFlag: boolean; permissions: string[] } | undefined }>({});
  const enabledLoadingMap = ref<{ [key: string]: boolean }>({});
  let updatingMap: { [key: string]: boolean; } = {}; // For permission update operations

  const searchInputValue = ref<string>();

  /**
   * <p>
   * Computed property for total pages based on current page size and total count
   * </p>
   */
  const totalPage = computed(() => {
    return Math.ceil(total.value / pageSize.value);
  });

  /**
   * <p>
   * Debounced search input change handler
   * </p>
   * <p>
   * Resets the current state and reloads data when search input changes
   * </p>
   */
  const searchInputChange = debounce(duration.search, (event: Event) => {
    reset();
    const target = event.target as HTMLInputElement;
    const value = target?.value?.trim() || '';
    searchInputValue.value = value;
    loadData();
  });

  /**
   * <p>
   * Handles space authentication enable/disable toggle
   * </p>
   *
   * @param checked - Whether authentication is enabled
   * @param id - Space ID to update
   */
  const switchChange = async (checked: boolean, id: string) => {
    enabledLoadingMap.value[id] = true;
    const [error] = await space.enabledSpaceAuth(id, checked);
    enabledLoadingMap.value[id] = false;

    if (error) {
      return;
    }

    // Update local state
    if (checked) {
      dataMap.value[id].auth = true;
    } else {
      dataMap.value[id].auth = false;
    }
  };

  /**
   * <p>
   * Handles "check all" permission changes
   * </p>
   *
   * @param event - Checkbox change event
   * @param id - Space ID to update
   */
  const checkAllChange = async (event: Event, id: string) => {
    if (updatingMap[id]) {
      return;
    }

    const target = event.target as HTMLInputElement;
    const checked = target?.checked || false;
    const permissions = checked ? permissionValues.value : [];
    checkChange(permissions, id);
  };

  /**
   * <p>
   * Handles individual permission changes
   * </p>
   * <p>
   * Manages permission updates including adding new permissions, updating existing ones,
   * and removing permissions when none are selected
   * </p>
   *
   * @param permissions - Array of permission values to set
   * @param id - Space ID to update
   */
  const checkChange = async (permissions: string[], id: string) => {
    if (updatingMap[id]) {
      return;
    }

    const authId = permissionsMap.value[id]?.id;

    // If no permissions selected, delete the authorization
    if (!permissions.length) {
      updatingMap[id] = true;
      const [error] = await space.deleteSpaceAuth(authId);
      updatingMap[id] = false;

      if (error) {
        return;
      }

      permissionsMap.value[id] = undefined;
      return;
    }

    // If authorization exists, update it
    if (authId) {
      updatingMap[id] = true;
      const [error] = await space.putSpaceAuth(authId, { permissions });
      updatingMap[id] = false;

      if (error) {
        return;
      }

      permissionsMap.value[id]!.permissions = permissions;
      return;
    }

    // Create new authorization
    updatingMap[id] = true;
    const params = {
      permissions,
      authObjectId: props.authObjectId,
      authObjectType: props.type
    };
    const [error, { data = { id: '' } }] = await space.addSpaceAuth(id, params);
    updatingMap[id] = false;

    if (error) {
      return;
    }

    permissionsMap.value[id] = {
      id: data.id,
      creatorFlag: false,
      permissions: permissions
    };
  };

  /**
   * <p>
   * Loads authentication data for specified space IDs
   * </p>
   *
   * @param ids - Array of space IDs to load permissions for
   */
  const loadAuths = async (ids: string[]) => {
    const params: {
      authObjectId: string;
      authObjectType: 'user' | 'dept' | 'group';
      filters?: [{ key: 'spaceId'; op: 'IN'; value: string[]; }]
    } = {
      authObjectId: props.authObjectId!,
      authObjectType: props.type,
      filters: [{ key: 'spaceId', op: 'IN', value: ids }]
    };

    authController = new AbortController();
    const axiosConfig = {
      signal: authController.signal
    };

    loading.value = true;
    const [error, { data = { list: [] } }] = await space.getSpaceAuthList(params, axiosConfig);

    if (error || !data) {
      loading.value = false;
      return;
    }

    const list = data.list || [];
    if (list.length) {
      for (let i = 0, len = list.length; i < len; i++) {
        const { id, spaceId, permissions, creatorFlag } = list[i];
        const currentMap = permissionsMap.value[spaceId];

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
          permissionsMap.value[spaceId] = {
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
   * Builds parameters for space list API call
   * </p>
   */
  const getParams = () => {
    const params: {
      pageNo: number;
      pageSize: number;
      appCode: 'AngusTester';
      hasPermission: string;
      admin: boolean;
      filters?: [{ key: 'name'; op: 'MATCH'; value: string; }];
      projectId: string;
    } = {
      hasPermission: 'GRANT',
      admin: true,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      appCode: 'AngusTester',
      projectId: projectId.value
    };

    if (searchInputValue.value) {
      params.filters = [{ key: 'name', op: 'MATCH', value: searchInputValue.value }];
    }

    return params;
  };

  /**
   * <p>
   * Loads space data with pagination support
   * </p>
   */
  const loadData = async () => {
    if (!props.authObjectId) {
      loading.value = false;
      return;
    }

    controller = new AbortController();
    const axiosConfig = {
      signal: controller.signal
    };

    const params = getParams();
    loading.value = true;
    const [error, { data }] = await space.getSpaceList(params, axiosConfig);

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
   * Throttled scroll handler for infinite scrolling
   * </p>
   */
  const handleScroll = throttle(duration.scroll, (event: Event) => {
    if (loading.value || pageNo.value >= totalPage.value) {
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
   * Debounced resize handler for dynamic page size calculation
   * </p>
   */
  const resizeHandler = debounce(duration.resize, () => {
    // This will be handled by the lifecycle composable
  });

  /**
   * <p>
   * Resets all state and aborts ongoing requests
   * </p>
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
   * <p>
   * Computed property for permission values
   * </p>
   */
  const permissionValues = computed(() => {
    return props.permissions.map(item => item.value);
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
    searchInputChange,
    switchChange,
    checkAllChange,
    checkChange,
    loadData,
    handleScroll,
    resizeHandler,
    reset,

    // Computed
    permissionValues
  };
}
