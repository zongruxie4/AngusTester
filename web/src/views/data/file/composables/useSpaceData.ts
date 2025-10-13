import { computed, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { modal } from '@xcan-angus/vue-ui';
import { duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';

import { space } from '@/api/storage';
import { PaginationType, SPACE_PERMISSIONS, SpaceInfoType } from '../types';

/**
 * <p>Composable for managing space data operations including CRUD operations,</p>
 * <p>pagination, search, and authorization management.</p>
 *
 * @returns Object containing reactive state and methods for space management
 */
export function useSpaceData () {
  const { t } = useI18n();
  const router = useRouter();

  const tempProjectId = ref<string>();
  const tempIsAdmin = ref<boolean>();

  // Reactive state for pagination
  const pagination = reactive<PaginationType>({
    current: 1,
    pageSize: 10,
    total: 0,
    hideOnSinglePage: true
  });

  // Reactive state for data management
  const dataList = ref<SpaceInfoType[]>([]);
  const tableLoading = ref(false);
  const keyword = ref();
  const selectedRowKey = ref<string>('');
  const selectedRow = ref<SpaceInfoType>();

  /**
   * <p>Get parameters for API requests including pagination and search filters.</p>
   *
   * @returns Object containing request parameters
   */
  const getParams = () => {
    return {
      pageSize: pagination.pageSize,
      pageNo: pagination.current,
      filters: keyword.value ? [{ value: keyword.value, op: 'MATCH', key: 'name' }] : undefined
    };
  };

  /**
   * <p>Load space data from API with current pagination and search parameters.</p>
   * <p>Handles loading state and updates the data list.</p>
   *
   * @param projectId - Current project ID
   * @param isAdmin - Whether current user is admin
   */
  const loadData = async (projectId: string, isAdmin: boolean) => {
    tempProjectId.value = projectId;
    tempIsAdmin.value = isAdmin;
    if (tableLoading.value) {
      return;
    }

    const params = getParams();
    tableLoading.value = true;

    try {
      const [error, resp] = await space.getSpaceList({
        ...params,
        appCode: 'AngusTester',
        admin: true,
        projectId
      });

      if (error) {
        return;
      }

      const { list, total } = resp.data || {};
      dataList.value = list || [];
      pagination.total = +total || 0;
      selectedRowKey.value = '';

      // Ensure all spaces have default auth field
      dataList.value.forEach(space => {
        if (!space.auth || !Array.isArray(space.auth)) {
          space.auth = [];
        }
      });

      // Set admin permissions if user is admin
      if (isAdmin) {
        dataList.value.forEach(space => {
          space.auth = SPACE_PERMISSIONS;
        });
        return;
      }

      // Load authorization data for non-admin users
      if (dataList.value?.length > 0) {
        await loadDataAuth(dataList.value);
      }
    } finally {
      tableLoading.value = false;
    }
  };

  /**
   * <p>Load authorization data for multiple spaces.</p>
   * <p>Updates the auth field for each space based on user permissions.</p>
   *
   * @param list - List of spaces to load auth for
   */
  const loadDataAuth = async (list: SpaceInfoType[]) => {
    const ids = list.map(space => space.id);
    const [error, res] = await space.getSpaceCurrentAuthList({ ids, admin: true });

    if (error) {
      return;
    }

    const authList = res.data || {};
    dataList.value.forEach(space => {
      if (authList[space.id] && authList[space.id].spaceAuth) {
        space.auth = (authList[space.id].permissions || []).map(auth => auth.value);
      } else {
        space.auth = SPACE_PERMISSIONS;
      }
    });
  };

  /**
   * <p>Handle pagination changes and reload data.</p>
   *
   * @param paginationInfo - New pagination information
   */
  const changePage = ({ pageSize, current }: { pageSize: number; current: number }) => {
    pagination.current = current;
    pagination.pageSize = pageSize;
    loadData(tempProjectId.value as string, tempIsAdmin.value as boolean);
  };

  /**
   * <p>Handle search keyword changes with debouncing.</p>
   * <p>Resets pagination to first page when searching.</p>
   */
  const handleSearch = debounce(duration.search, () => {
    pagination.current = 1;
  });

  /**
   * <p>Show delete confirmation modal and handle space deletion.</p>
   *
   * @param record - Space record to delete
   * @param projectId - Current project ID
   * @param isAdmin - Whether current user is admin
   */
  const delConfirm = (record: SpaceInfoType, projectId: string, isAdmin: boolean) => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: record.name }),
      onOk: () => {
        return new Promise<void>((resolve, reject) => {
          space.deleteSpace(record.id).then(([error]) => {
            if (error) {
              // eslint-disable-next-line prefer-promise-reject-errors
              reject();
              return;
            }
            resolve();

            // Adjust pagination if deleting last item on current page
            if (dataList.value.length === 1 && pagination.current > 1) {
              pagination.current--;
            }

            loadData(projectId, isAdmin);
          });
        });
      }
    });
  };

  /**
   * <p>Navigate to space detail page.</p>
   *
   * @param record - Space record to open
   */
  const openSpace = (record: SpaceInfoType) => {
    router.push({
      path: `/data/file/${record.id}`,
      query: { spaceName: record.name }
    });
  };

  /**
   * <p>Handle row selection in table.</p>
   *
   * @param record - Selected record
   */
  const handleRowSelect = (record: SpaceInfoType) => {
    if (record.id === selectedRowKey.value) {
      selectedRow.value = undefined;
      selectedRowKey.value = '';
    } else {
      selectedRowKey.value = record.id;
      selectedRow.value = record;
    }
  };

  /**
   * <p>Get computed name by selected row ID.</p>
   */
  const getNameById = computed(() => {
    if (!selectedRowKey.value) {
      return '';
    }
    const selectRow = dataList.value.find(row => row.id === selectedRowKey.value);
    return selectRow?.name || '';
  });

  /**
   * <p>Safe getter for authorization array to prevent runtime errors.</p>
   *
   * @param record - Space record
   * @returns Array of authorization permissions
   */
  const getSafeAuth = (record: SpaceInfoType) => {
    return Array.isArray(record?.auth) ? record.auth : [];
  };

  /**
   * <p>Watch for keyword changes and trigger search.</p>
   */
  watch(() => keyword.value, handleSearch);

  return {
    // State
    pagination,
    dataList,
    tableLoading,
    keyword,
    selectedRowKey,
    selectedRow,

    // Computed
    getNameById,

    // Methods
    getParams,
    loadData,
    loadDataAuth,
    changePage,
    delConfirm,
    openSpace,
    handleRowSelect,
    getSafeAuth
  };
}
