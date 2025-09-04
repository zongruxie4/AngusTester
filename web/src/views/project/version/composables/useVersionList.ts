import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { modal, notification } from '@xcan-angus/vue-ui';
import { software } from '@/api/tester';
import type {
  VersionListProps,
  VersionInfo,
  PaginationConfig,
  SearchParams,
  MenuItem,
  StatusColorConfig
} from '../types';

export function useVersionList (props: VersionListProps) {
  const { t } = useI18n();

  // Injected dependencies
  const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

  // Reactive state
  const loaded = ref(false);
  const loading = ref(false);
  const searchedFlag = ref(false);
  const editVisible = ref(false);
  const selectVersionId = ref<string>();
  const mergeVisible = ref(false);

  // Search and pagination state
  const searchPanelParams = ref<SearchParams>({
    orderBy: undefined,
    orderSort: undefined,
    filters: []
  });

  const pagination = ref<PaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0
  });

  const pageNo = ref(1);
  const dataList = ref<VersionInfo[]>([]);
  const permissionsMap = ref<Map<string, string[]>>(new Map());

  /**
   * Refresh version list data
   * Resets pagination and reloads data
   */
  const refresh = (): void => {
    pagination.value.current = 1;
    permissionsMap.value.clear();
    loadData();
  };

  /**
   * Handle search panel changes
   * Updates search parameters and reloads data
   * @param data - New search parameters
   */
  const searchChange = (data: SearchParams): void => {
    pagination.value.current = 1;
    searchPanelParams.value = data;
    loadData();
  };

  /**
   * Handle pagination changes
   * Updates pagination state and reloads data
   * @param page - New pagination configuration
   */
  const paginationChange = (page: PaginationConfig): void => {
    pagination.value.current = page.current;
    pagination.value.pageSize = page.pageSize;
    loadData();
  };

  /**
   * Load version list data from API
   * Fetches paginated version data with search filters
   */
  const loadData = async (): Promise<void> => {
    loading.value = true;

    const params = {
      projectId: props.projectId,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchPanelParams.value
    };

    const [error, res] = await software.getSoftwareVersionList({ ...params });
    loaded.value = true;
    loading.value = false;

    // Update search flag based on filters
    if (params.filters?.length || params.orderBy) {
      searchedFlag.value = true;
    } else {
      searchedFlag.value = false;
    }

    if (error) {
      pagination.value.total = 0;
      dataList.value = [];
      return;
    }

    const data = res?.data || { total: 0, list: [] };
    if (data) {
      pagination.value.total = +data.total;
      dataList.value = data.list || [];
    }
  };

  /**
   * Delete version with confirmation
   * Shows confirmation modal and deletes version
   * @param data - Version to delete
   */
  const toDelete = async (data: VersionInfo): Promise<void> => {
    modal.confirm({
      content: t('version.messages.deleteConfirm', { name: data.name }),
      async onOk () {
        const id = data.id;
        const [error] = await software.deleteSoftwareVersion([id]);
        if (error) {
          return;
        }

        notification.success(t('version.messages.deleteSuccess'));

        // Adjust pagination if needed
        if (pagination.value.current > 1 && dataList.value.length === 1) {
          pagination.value.current -= 1;
        }

        await loadData();
        deleteTabPane([id]);
      }
    });
  };

  /**
   * Open version edit modal
   * @param record - Version record to edit
   */
  const editVersion = (record: VersionInfo = {} as VersionInfo): void => {
    selectVersionId.value = record.id;
    editVisible.value = true;
  };

  /**
   * Open version merge modal
   */
  const toMerge = (): void => {
    mergeVisible.value = true;
  };

  /**
   * Change version status
   * Updates version status with confirmation
   * @param status - New status configuration
   * @param record - Version record to update
   */
  const changeStatus = async (status: MenuItem, record: VersionInfo): Promise<void> => {
    modal.confirm({
      content: t('version.messages.changeStatusConfirm', { status: status.name }),
      async onOk () {
        const [error] = await software.updateSoftwareVersionStatus(record.id, {
          status: status.key
        });
        if (error) {
          return;
        }

        notification.success(t('version.messages.editSuccess'));
        loadData();
      }
    });
  };

  /**
   * Handle merge operation completion
   * Updates list after successful merge
   * @param formId - ID of merged version
   */
  const handleMergeOk = (formId: string): void => {
    if (pagination.value.current > 1 && dataList.value.length === 1) {
      pagination.value.current -= 1;
    }
    deleteTabPane([formId + '-detail']);
    loadData();
  };

  /**
   * Status color configuration for version status tags
   */
  const statusColorConfig: StatusColorConfig = {
    ARCHIVED: 'default',
    NOT_RELEASED: 'processing',
    RELEASED: 'success'
  };

  /**
   * Generate context menu items for version actions
   * @param record - Version record
   * @returns Array of menu items
   */
  const getMenus = (record: VersionInfo): MenuItem[] => {
    return [
      record.status?.value !== 'NOT_RELEASED' && {
        key: 'NOT_RELEASED',
        name: t('version.status.notReleased'),
        icon: 'icon-baocundaoweiguidang'
      },
      record.status?.value !== 'RELEASED' && {
        key: 'RELEASED',
        name: t('version.status.released'),
        icon: 'icon-fabu'
      },
      record.status?.value !== 'ARCHIVED' && {
        key: 'ARCHIVED',
        name: t('version.status.archived'),
        icon: 'icon-weiguidang'
      }
    ].filter(Boolean) as MenuItem[];
  };

  // Watch for project changes and notifications
  onMounted(() => {
    watch(() => props.projectId, () => {
      pageNo.value = 1;
      loadData();
    }, { immediate: true });

    watch(() => props.notify, (newValue) => {
      if (!newValue) {
        return;
      }
      loadData();
    }, { immediate: false });
  });

  return {
    // State
    loaded,
    loading,
    searchedFlag,
    editVisible,
    selectVersionId,
    mergeVisible,
    pagination,
    dataList,
    statusColorConfig,

    // Methods
    refresh,
    searchChange,
    paginationChange,
    loadData,
    toDelete,
    editVersion,
    toMerge,
    changeStatus,
    handleMergeOk,
    getMenus
  };
}
