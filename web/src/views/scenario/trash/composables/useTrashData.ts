import type { Ref } from 'vue';
import { computed, ref, unref } from 'vue';
import { scenario } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import type { TrashItem, TrashParams } from '../types';

/**
 * Composable for managing scenario trash data operations
 * @param projectId - Project identifier (can be reactive)
 * @param userInfo - Current user information
 * @returns Object containing data state and operations
 */
export function useTrashData (projectId: string | Ref<string>, userInfo: { id: string }) {
  // State
  const tableData = ref<TrashItem[]>([]);
  const loading = ref(false);
  const loaded = ref(false);
  const orderBy = ref<string>();
  const orderSort = ref<'ASC' | 'DESC'>();

  // Pagination state
  const pagination = ref<{
    total: number;
    current: number;
    pageSize: number;
  }>({
    total: 0,
    current: 1,
    pageSize: 10
  });

  /**
   * Load trash data from API
   * @param params - Additional query parameters
   */
  const loadData = async (params?: TrashParams) => {
    const currentProjectId = unref(projectId);
    if (!currentProjectId) return;

    loading.value = true;

    const requestParams: {
      projectId: string;
      pageNo: number;
      pageSize: number;
      filters?: {value: string, op: string, key: string}[],
      orderBy?: string;
      orderSort?: string;
    } = {
      projectId: currentProjectId,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize
    };

    if (params?.filters) {
      requestParams.filters = params.filters;
    }

    if (orderSort.value) {
      requestParams.orderBy = orderBy.value;
      requestParams.orderSort = orderSort.value;
    }

    const [error, res] = await scenario.getScenarioTrashList(requestParams);
    loaded.value = true;
    loading.value = false;

    if (error) {
      return;
    }

    const data = res?.data || { list: [], total: 0 };
    const userId = userInfo?.id;

    tableData.value = data.list.map(item => {
      item.disabled = true;
      // Only admin, creator, or deleter can perform actions
      if (userId === item.createdBy || userId === item.deletedBy) {
        item.disabled = false;
      }
      return item;
    });

    pagination.value.total = +(data.total || 0);
  };

  /**
   * Handle table change events (pagination, sorting)
   * @param paginationInfo - Pagination information
   * @param filters - Table filters
   * @param sorter - Sort configuration
   */
  const handleTableChange = (
    paginationInfo: { current?: number; pageSize?: number },
    _filters: any,
    sorter: { orderBy: string; orderSort: 'ASC' | 'DESC' }
  ) => {
    orderBy.value = sorter.orderBy;
    orderSort.value = sorter.orderSort;
    pagination.value.current = paginationInfo.current || 1;
    pagination.value.pageSize = paginationInfo.pageSize || 10;
  };

  /**
   * Reset pagination to first page
   */
  const resetPagination = () => {
    pagination.value.current = 1;
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
