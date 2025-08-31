import { ref, watch } from 'vue';
import { mock } from '@/api/tester';
import type { MockService, MockServiceListParams, MockServiceListResponse, TablePagination } from '../types';

/**
 * <p>Composable for managing mock service data operations</p>
 * <p>Handles data fetching, pagination, and state management for mock services</p>
 *
 * @param nodeId - The identifier of the node
 * @param projectId - The identifier of the project
 * @returns Object containing mock service data and related methods
 */
export function useMockServiceData (nodeId: string, projectId: string) {
  /**
   * <p>List of mock services retrieved from the API</p>
   * <p>Stores the current page of mock service data</p>
   */
  const mockServiceData = ref<MockService[]>([]);

  /**
   * <p>Pagination configuration for the table</p>
   * <p>Manages current page, page size, and total count</p>
   */
  const pagination = ref<TablePagination>({
    current: 1,
    pageSize: 10,
    total: 0,
    pageSizeOptions: [10]
  });

  /**
   * <p>Loading state indicator</p>
   * <p>Tracks whether data is currently being fetched</p>
   */
  const isLoading = ref(false);

  /**
   * <p>Error state for API operations</p>
   * <p>Stores any error messages from failed API calls</p>
   */
  const error = ref<string | null>(null);

  /**
   * <p>Fetches mock service data from the API</p>
   * <p>Retrieves paginated list of mock services for the specified node and project</p>
   *
   * @returns Promise that resolves when data loading is complete
   */
  const loadMockServiceData = async (): Promise<void> => {
    if (!nodeId || !projectId) {
      return;
    }

    try {
      isLoading.value = true;
      error.value = null;

      const { current, pageSize } = pagination.value;
      const params: MockServiceListParams = {
        nodeId,
        projectId,
        pageNo: current,
        pageSize
      };

      const [apiError, response] = await mock.getServiceList(params);

      if (apiError) {
        error.value = 'Failed to load mock service data';
        return;
      }

      const { data = { list: [], total: 0 } }: { data: MockServiceListResponse } = response;
      mockServiceData.value = data.list || [];
      pagination.value.total = +data.total || 0;
    } catch (err) {
      error.value = 'An unexpected error occurred while loading data';
      console.error('Error loading mock service data:', err);
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * <p>Handles pagination changes</p>
   * <p>Updates pagination state and reloads data when page or page size changes</p>
   *
   * @param page - Object containing new page information
   */
  const handlePaginationChange = (page: { current: number; pageSize: number }): void => {
    pagination.value.current = page.current;
    pagination.value.pageSize = page.pageSize;
    loadMockServiceData();
  };

  /**
   * <p>Refreshes the current data</p>
   * <p>Reloads data without changing pagination state</p>
   */
  const refreshData = (): void => {
    loadMockServiceData();
  };

  /**
   * <p>Resets pagination to first page</p>
   * <p>Useful when filters or search criteria change</p>
   */
  const resetToFirstPage = (): void => {
    pagination.value.current = 1;
    loadMockServiceData();
  };

  // Watch for changes in projectId and nodeId to reload data
  watch([() => projectId, () => nodeId], ([newProjectId, newNodeId]) => {
    if (newProjectId && newNodeId) {
      loadMockServiceData();
    }
  }, { immediate: true });

  return {
    // State
    mockServiceData,
    pagination,
    isLoading,
    error,

    // Methods
    loadMockServiceData,
    handlePaginationChange,
    refreshData,
    resetToFirstPage
  };
}
