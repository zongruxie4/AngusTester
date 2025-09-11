import { computed, nextTick, ref, watch } from 'vue';
import { debounce, throttle } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';
import { duration } from '@xcan-angus/infra';
import { report } from '@/api/tester';
import { SpaceItem } from '../types';

// 定义权限映射类型
type PermissionsMap = {
  [key: string]: {
    id: string;
    creatorFlag: boolean;
    permissions: string[]
  } | undefined
};

/**
 * Composable for managing authorization data and related operations
 * @param projectId - Project ID
 * @param authObjectId - Authorization object ID
 * @param type - Authorization object type
 * @param permissions - Available permissions
 */
export function useAuthData (
  projectId: string | undefined,
  authObjectId: string | undefined,
  type: 'user' | 'dept' | 'group',
  permissions: { value: string; label: string }[]
) {
  // Constants
  const LINE_HEIGHT = 44;

  // Reactive references
  const loading = ref(true);
  const pageNo = ref(1);
  const pageSize = ref(1);
  const total = ref(0);
  const idList = ref<string[]>([]);
  const dataMap = ref<{ [key: string]: SpaceItem }>({});
  const permissionsMap = ref<PermissionsMap>({});
  const enabledLoadingMap = ref<{ [key: string]: boolean }>({});
  const searchInputValue = ref<string>();
  const containerRef = ref<HTMLElement>();

  // Controllers for aborting requests
  let controller: AbortController;
  let authController: AbortController;
  let updatingMap: { [key: string]: boolean } = {};

  // Element resize detector
  const erd = elementResizeDetector({ strategy: 'scroll' });

  // Computed properties
  const totalPage = computed(() => Math.ceil(total.value / pageSize.value));
  const permissionValues = computed(() => permissions.map(item => item.value));

  /**
   * Reset all data and controllers
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
   * Handle search input change with debounce
   */
  const handleSearchInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
    reset();
    const value = event.target.value.trim();
    searchInputValue.value = value;
    loadData();
  });

  /**
   * Get request parameters
   */
  const getParams = () => {
    const params: {
      projectId: string | undefined,
      pageNo: number;
      pageSize: number;
      appCode:'AngusTester',
      hasPermission: string;
      admin:boolean;
      filters?: [{ key: 'name'; op: 'MATCH'; value: string; }];
    } = {
      projectId: projectId,
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
   * Load authorization data for given IDs
   * @param ids - Report IDs to load authorization data for
   */
  const loadAuths = async (ids: string[]) => {
    if (!authObjectId) return;

    const params: {
      authObjectId: string;
      authObjectType: 'user' | 'dept' | 'group';
      filters?: [{ key: 'reportId'; op: 'IN'; value: string[]; }]
    } = {
      authObjectId,
      authObjectType: type,
      filters: [{ key: 'reportId', op: 'IN', value: ids }]
    };

    authController = new AbortController();
    const axiosConfig = {
      signal: authController.signal
    };

    loading.value = true;
    const [error, { data = { list: [] } }] = await report.getReportAuth(params, axiosConfig);
    if (error || !data) {
      loading.value = false;
      return;
    }

    const list = data.list || [];
    if (list.length) {
      for (let i = 0, len = list.length; i < len; i++) {
        const { id, reportId, permissions, creatorFlag } = list[i];
        const currentMap = permissionsMap.value[reportId];
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
          permissionsMap.value[reportId] = {
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
   * Load report data
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
    const [error, { data }] = await report.getReportList(params, axiosConfig);
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
   * Handle scroll events with throttle
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
   * Handle resize events with debounce
   */
  const resizeHandler = debounce(duration.resize, () => {
    if (!containerRef.value) {
      return;
    }

    const height = containerRef.value.offsetHeight;
    let _pageSize = 1;
    if (height > LINE_HEIGHT) {
      const quotient = height / (LINE_HEIGHT);
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
   * Handle switch change for enabling/disabling reports
   * @param checked - Switch checked state
   * @param id - Report ID
   */
  const handleSwitchChange = async (checked: boolean, id: string) => {
    enabledLoadingMap.value[id] = true;
    const [error] = await report.enableReport(id, checked);
    enabledLoadingMap.value[id] = false;
    if (error) {
      return;
    }

    if (checked) {
      dataMap.value[id].auth = true;
      return;
    }

    dataMap.value[id].auth = false;
  };

  /**
   * Handle "check all" checkbox change
   * @param event - Checkbox change event
   * @param id - Report ID
   */
  const handleCheckAllChange = async (event: { target: { checked: boolean } }, id: string) => {
    if (updatingMap[id]) {
      return;
    }

    const checked = event.target.checked;
    let permissions: string[] = [];
    permissions = checked ? permissionValues.value : [];

    handleCheckChange(permissions, id);
  };

  /**
   * Handle individual checkbox changes
   * @param permissions - Selected permissions
   * @param id - Report ID
   */
  const handleCheckChange = async (permissions: string[], id: string) => {
    if (updatingMap[id]) {
      return;
    }

    // If permissionMap.value[id].id is undefined, it means not authorized, otherwise already authorized
    const authId = permissionsMap.value[id]?.id;
    if (!permissions.length) {
      updatingMap[id] = true;
      const [error] = await report.deleteReportAuth(authId as string);
      updatingMap[id] = false;
      if (error) {
        return;
      }

      permissionsMap.value[id] = undefined;
      return;
    }

    if (authId) {
      updatingMap[id] = true;
      const [error] = await report.putReportAuth(authId, { permissions });
      updatingMap[id] = false;
      if (error) {
        return;
      }

      permissionsMap.value[id]!.permissions = permissions;
      return;
    }

    // Not authorized yet
    updatingMap[id] = true;
    const params = { permissions, authObjectId: authObjectId, authObjectType: type };
    const [error, { data = { id: '' } }] = await report.addReportAuth(id, params);
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
   * Initialize component
   */
  const init = () => {
    nextTick(() => {
      if (!containerRef.value) return;

      const height = containerRef.value.offsetHeight;
      let _pageSize = 1;
      if (height > LINE_HEIGHT) {
        const quotient = height / (LINE_HEIGHT);
        if (Number.isInteger(quotient)) {
          _pageSize = quotient + 1;
        } else {
          _pageSize = Math.ceil(quotient);
        }
      }

      pageSize.value = _pageSize;
      loadData();

      erd.listenTo(containerRef.value, resizeHandler);
    });

    watch(() => authObjectId, (newValue) => {
      reset();
      if (!newValue) {
        return;
      }

      loadData();
    });
  };

  /**
   * Cleanup on component unmount
   */
  const cleanup = () => {
    erd.removeListener(containerRef.value, resizeHandler);
  };

  return {
    // Reactive data
    loading,
    idList,
    dataMap,
    permissionsMap,
    enabledLoadingMap,
    searchInputValue,
    containerRef,

    // Computed properties
    totalPage,
    permissionValues,

    // Methods
    handleSearchInputChange,
    handleScroll,
    handleSwitchChange,
    handleCheckAllChange,
    handleCheckChange,
    reset,
    init,
    cleanup
  };
}
