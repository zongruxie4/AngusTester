import { ref, computed } from 'vue';
import { datasource } from '@/api/tester';
import {
  SearchParams,
  DataSourceDetail,
  EnhancedDataSourceItem,
  ConnectionTestResult
} from '../types';

/**
 * <p>Composable for managing datasource data state and operations</p>
 * <p>Handles all data fetching, state management, and data transformations</p>
 */
export function useData (projectId: string) {
  // Loading states
  const loading = ref(false);
  const isFirstLoad = ref(true);

  // Data state
  const dataList = ref<DataSourceDetail[]>([]);
  const dataMap = ref<Record<string, EnhancedDataSourceItem>>({});
  const total = ref(0);

  // Search parameters
  const params = ref<SearchParams>({
    pageNo: 1,
    pageSize: 40,
    filters: []
  });

  /**
   * <p>Fetch data source list from API</p>
   * <p>Retrieves paginated data source list with search filters</p>
   */
  const getDataSourceList = async (): Promise<void> => {
    // Clear existing data map
    dataMap.value = {};

    // Prevent concurrent requests
    if (loading.value) {
      return;
    }

    loading.value = true;

    try {
      const [error, { data = { list: [], total: 0 } }] = await datasource.getDataSourceList({
        ...params.value,
        projectId
      });
      if (error) {
        return;
      }

      // Transform and store data
      const len = data.list.length;
      if (len > 0) {
        for (let i = 0; i < len; i++) {
          const item: EnhancedDataSourceItem = {
            ...data.list[i],
            connSuccess: undefined,
            connFailureMessage: undefined,
            testLoading: false
          };
          dataMap.value[item.id] = item;
        }
      }

      dataList.value = data.list || [];
      total.value = +data.total;
      isFirstLoad.value = false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * <p>Refresh data source list</p>
   * <p>Resets pagination and fetches fresh data</p>
   */
  const refreshDataSourceList = (): void => {
    params.value.pageNo = 1;
    getDataSourceList();
  };

  /**
   * <p>Update search parameters and fetch data</p>
   * <p>Handles search filter changes and resets pagination</p>
   */
  const updateSearchParams = (filters: { key: string; value: string; op: any }[]): void => {
    params.value.pageNo = 1;
    params.value.filters = filters;
    getDataSourceList();
  };

  /**
   * <p>Update pagination parameters and fetch data</p>
   * <p>Handles page number and page size changes</p>
   */
  const updatePaginationParams = (page: number, size: number): void => {
    params.value.pageNo = page;
    params.value.pageSize = size;
    getDataSourceList();
  };

  /**
   * <p>Test data source connection</p>
   * <p>Tests the connection to a specific data source and updates UI state</p>
   */
  const testDataSourceConnection = async (record: DataSourceDetail): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;
    dataMap.value[record.id].testLoading = true;

    try {
      const [error, { data }] = await datasource.testById(record.id);
      if (error) {
        return;
      }

      // Update connection test results
      dataMap.value[record.id].connSuccess = data.connSuccess;
      dataMap.value[record.id].connFailureMessage = data.connFailureMessage;
    } finally {
      dataMap.value[record.id].testLoading = false;
      loading.value = false;
    }
  };

  /**
   * <p>Delete data source</p>
   * <p>Removes a data source and refreshes the list</p>
   */
  const deleteDataSource = async (id: string): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;

    try {
      const [error] = await datasource.deleteDataSource(id);
      if (error) {
        return;
      }

      // Refresh list after successful deletion
      refreshDataSourceList();
    } finally {
      loading.value = false;
    }
  };

  return {
    // State
    loading,
    isFirstLoad,
    dataList,
    dataMap,
    total,
    params,

    // Methods
    getDataSourceList,
    refreshDataSourceList,
    updateSearchParams,
    updatePaginationParams,
    testDataSourceConnection,
    deleteDataSource
  };
}
