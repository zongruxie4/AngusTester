import { ref, computed, watch } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { variable } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { VariableDetail } from '../../types';
import type { TablePagination, RowSelection } from '../types';

/**
 * Maximum number of items that can be deleted in batch
 */
const MAX_BATCH_DELETE_NUM = 200;

/**
 * Default page size for the table
 */
const DEFAULT_PAGE_SIZE = 10;

/**
 * Composable function for managing variable data
 *
 * @param projectId - Project identifier
 * @param notify - Notification message for triggering refresh
 * @returns Object containing data management functions and state
 */
export function useVariableList (props: {projectId?: string, notify?: string}) {
  const { t } = useI18n();

  // Reactive state
  const loaded = ref(false);
  const loading = ref(false);
  const searchedFlag = ref(false);
  const tableData = ref<VariableDetail[]>([]);
  const pagination = ref<TablePagination>({
    current: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    total: 0
  });

  // Search and filter state
  const searchPanelParams = ref<PageQuery>({
    orderBy: undefined,
    orderSort: undefined,
    filters: []
  });

  // Row selection state
  const rowSelection = ref<RowSelection>();

  /**
   * Load variable data from API
   *
   * <p>Fetches variables based on current pagination and search parameters</p>
   * <p>Updates table data and pagination state</p>
   */
  const loadData = async () => {
    const params = {
      projectId: props.projectId,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchPanelParams.value
    };

    loading.value = true;
    const [error, res] = await variable.getVariablesList(params);
    loaded.value = true;
    loading.value = false;

    // Update search flag based on whether filters are applied
    searchedFlag.value = (params.filters?.length ?? 0) > 0;

    if (error) {
      pagination.value.total = 0;
      tableData.value = [];
      return;
    }

    const data = res?.data || { total: 0, list: [] };
    if (data) {
      pagination.value.total = +data.total;
      const list = data.list as VariableDetail[];
      tableData.value = list;
    }
  };

  /**
   * Handle table pagination and sorting changes
   *
   * @param current - New current page
   * @param pageSize - New page size
   * @param sorter - Sorting configuration
   */
  const handleTableChange = (
    current: number,
    pageSize: number,
    sorter: { orderBy: string; orderSort: PageQuery.OrderSort }
  ) => {
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
    searchPanelParams.value.orderBy = sorter.orderBy;
    searchPanelParams.value.orderSort = sorter.orderSort;
    loadData();
  };

  /**
   * Handle search panel changes
   *
   * @param data - Search panel data with filters
   */
  const handleSearchPanelChange = (data: { filters: SearchCriteria[] }) => {
    pagination.value.current = 1;
    searchPanelParams.value.filters = data.filters;
    loadData();
  };

  /**
   * Refresh data and reset state
   */
  const refresh = () => {
    pagination.value.current = 1;
    loadData();
  };

  /**
   * Initialize row selection for batch operations
   */
  const initializeRowSelection = () => {
    if (!rowSelection.value) {
      rowSelection.value = {
        onChange: handleTableSelect,
        getCheckboxProps: () => ({ disabled: false }),
        selectedRowKeys: []
      };
    }
  };

  /**
   * Handle table row selection changes
   *
   * @param keys - Selected row keys
   */
  const handleTableSelect = (keys: string[]) => {
    if (!rowSelection.value) return;

    const currentIds = tableData.value.map(item => item.id);
    const deleteIds = currentIds.reduce((prev, cur) => {
      if (!keys.includes(cur)) {
        prev.push(cur);
      }
      return prev;
    }, [] as string[]);

    // Update selected keys based on current selection
    const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(
      item => !deleteIds.includes(item)
    );

    // Add newly selected keys
    keys.forEach(key => {
      if (!selectedRowKeys.includes(key)) {
        selectedRowKeys.push(key);
      }
    });

    // Check if selection exceeds maximum limit
    const num = selectedRowKeys.length;
    if (num > MAX_BATCH_DELETE_NUM) {
      notification.info(
        t('variable.list.messages.maxBatchDelete', {
          maxNum: MAX_BATCH_DELETE_NUM,
          num
        })
      );
    }

    rowSelection.value.selectedRowKeys = selectedRowKeys;
  };

  /**
   * Cancel batch delete operation
   */
  const cancelBatchDelete = () => {
    rowSelection.value = undefined;
  };

  /**
   * Get computed selected count
   */
  const selectedCount = computed(() =>
    rowSelection.value?.selectedRowKeys?.length || 0
  );

  /**
   * Watch for project ID changes and reload data
   */
  watch(() => props.projectId, (newValue) => {
    if (!newValue) return;

    loaded.value = false;
    pagination.value.current = 1;
    loadData();
  }, { immediate: true });

  /**
   * Watch for notification changes and refresh data
   */
  watch(() => props.notify, (newValue) => {
    if (!newValue) return;
    loadData();
  }, { immediate: true });

  return {
    // State
    loaded,
    loading,
    searchedFlag,
    tableData,
    pagination,
    searchPanelParams,
    rowSelection,
    selectedCount,

    // Methods
    loadData,
    handleTableChange,
    handleSearchPanelChange,
    refresh,
    initializeRowSelection,
    handleTableSelect,
    cancelBatchDelete
  };
}
