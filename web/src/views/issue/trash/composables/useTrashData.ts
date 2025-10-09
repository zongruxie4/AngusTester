import { computed, ref } from 'vue';
import { appContext, PageQuery, ProjectPageQuery } from '@xcan-angus/infra';
import { issue } from '@/api/tester';
import type { TaskTrashItem, TaskTrashPagination, TaskTrashParams } from '../types';

/**
 * Composable for managing task trash data and pagination
 * @param projectId - Current project ID
 * @param userInfo - Current user information
 * @returns Object containing data, pagination, loading states and methods
 */
export function useTrashData (
  projectId: string,
  userInfo: { id: string }
) {
  // Reactive state
  const tableData = ref<TaskTrashItem[]>([]);
  const loading = ref(false);
  const loaded = ref(false);
  const orderBy = ref<string>();
  const orderSort = ref<PageQuery.OrderSort>();
  const pagination = ref<TaskTrashPagination>({
    total: 0,
    current: 1,
    pageSize: 10
  });

  // Injected dependencies
  const isAdmin = appContext.isAdmin();

  /**
   * Load task trash data with current parameters
   * @param params - Request parameters for filtering and pagination
   */
  const loadData = async (params: TaskTrashParams) => {
    loading.value = true;

    const requestParams: ProjectPageQuery = {
      projectId,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...params,
      ...(orderSort.value && {
        orderBy: orderBy.value,
        orderSort: orderSort.value
      })
    };

    try {
      const [error, res] = await issue.getTrashTaskList(requestParams);
      if (error) {
        throw error;
      }

      const data = res?.data || { list: [], total: 0 };
      const userId = userInfo?.id;

      // Process data to add permission flags
      tableData.value = data.list.map(item => ({
        ...item,
        disabled: !(isAdmin || userId === item.createdBy || userId === item.deletedBy)
      }));

      pagination.value.total = +(data.total || 0);
      loaded.value = true;
    } catch (error) {
      console.error('Failed to load task trash data:', error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * Handle table change events (pagination, sorting)
   * @param paginationInfo - Pagination information
   * @param filters - Table filters
   * @param sorter - Sorting information
   */
  const handleTableChange = (
    paginationInfo: { current?: number; pageSize?: number },
    _filters: any,
    sorter: { orderBy?: string; orderSort?: PageQuery.OrderSort }
  ) => {
    const { current = 1, pageSize = 10 } = paginationInfo;

    orderBy.value = sorter.orderBy;
    orderSort.value = sorter.orderSort;
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
  };

  /**
   * Reset pagination to first page
   */
  const resetPagination = () => {
    pagination.value.current = 1;
  };

  /**
   * Get current page after deletion (handles edge case when deleting last item on last page)
   * @param currentPage - Current page number
   * @param pageSize - Items per page
   * @param total - Total items count
   * @returns Adjusted page number
   */
  const getCurrentPage = (currentPage: number, pageSize: number, total: number): number => {
    const maxPage = Math.max(1, Math.ceil((total - 1) / pageSize));
    return Math.min(currentPage, maxPage);
  };

  /**
   * Update pagination current page (typically after deletion)
   */
  const updateCurrentPage = () => {
    pagination.value.current = getCurrentPage(
      pagination.value.current,
      pagination.value.pageSize,
      pagination.value.total
    );
  };

  // Computed properties
  const hasData = computed(() => tableData.value.length > 0);
  const isEmpty = computed(() => loaded.value && !hasData.value);

  return {
    // State
    tableData,
    loading,
    loaded,
    pagination,
    orderBy,
    orderSort,

    // Computed
    hasData,
    isEmpty,

    // Methods
    loadData,
    handleTableChange,
    resetPagination,
    updateCurrentPage
  };
}
