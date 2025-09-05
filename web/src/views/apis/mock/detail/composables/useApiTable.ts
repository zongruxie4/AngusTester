import { computed, onMounted, ref } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { mock as mock0 } from '@/api/tester';
import { MockApiItem } from '@/views/apis/mock/detail/types';

/**
 * Composable for managing the mock API table data and interactions.
 * <p>
 * Handles pagination, search, sorting, and data loading for the API list table.
 */
export function useApiTable (serviceId: string) {
  const tableParams = ref<PageQuery>({
    pageNo: 1,
    pageSize: 10,
    mockServiceId: serviceId,
    filters: []
  });

  const tableData = ref<MockApiItem[]>([]);
  const total = ref(0);
  const loading = ref(false);

  /**
   * Fetch API list based on current table parameters.
   */
  const loadApiList = async () => {
    if (loading.value) return;

    loading.value = true;
    const [error, { data = { list: [], total: 0 } }] = await mock0.getMockApiList(tableParams.value);
    loading.value = false;

    if (error) return;

    tableData.value = data.list;
    total.value = Number(data.total);
  };

  /**
   * Debounced search handler for API name/path filtering.
   */
  const handleSearch = debounce(duration.search, (value: string) => {
    tableParams.value.pageNo = 1;
    if (value) {
      tableParams.value.filters = [{ key: 'summary', op: SearchCriteria.OpEnum.MatchEnd, value }];
    } else {
      tableParams.value.filters = [];
    }
    loadApiList();
  });

  /**
   * Handle table pagination, filtering, and sorting changes.
   */
  const handleTableChange = (pagination: any, _filters: any, sorter: any) => {
    const { current, pageSize } = pagination;
    tableParams.value.pageNo = current;
    tableParams.value.pageSize = pageSize;
    tableParams.value.orderBy = sorter.orderBy;
    tableParams.value.orderSort = sorter.orderSort;
    loadApiList();
  };

  /**
   * Refresh table data manually.
   */
  const refreshTable = () => {
    loadApiList();
  };

  /**
   * Computed pagination config for Ant Design Table.
   */
  const paginationConfig = computed(() => ({
    current: tableParams.value.pageNo,
    pageSize: tableParams.value.pageSize,
    total: total.value
  }));

  onMounted(() => {
    loadApiList();
  });

  return {
    tableParams,
    tableData,
    total,
    loading,
    paginationConfig,
    loadApiList,
    handleSearch,
    handleTableChange,
    refreshTable
  };
}
