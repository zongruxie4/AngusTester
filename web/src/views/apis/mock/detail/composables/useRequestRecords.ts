import { onMounted, ref } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { mock } from '@/api/tester';

import { RequestRecordItem } from '@/views/apis/mock/detail/types';

/**
 * Composable for managing request records data and interactions.
 * <p>
 * Handles fetching, pagination, search, and selection of API request logs.
 */
export function useRequestRecords (serviceId: string) {
  const params = ref<PageQuery>({
    pageNo: 1,
    pageSize: 10,
    filters: []
  });

  const recordList = ref<RequestRecordItem[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const selectedRecord = ref<RequestRecordItem>();

  /**
   * Fetch request records list based on current parameters.
   */
  const loadRecords = async () => {
    if (!serviceId) return;

    loading.value = true;
    const [error, { data = { list: [], total: 0 } }] =
      await mock.loadMockApiLogList(serviceId, params.value);
    loading.value = false;

    if (error) return;

    recordList.value = data.list;
    total.value = Number(data.total);

    // Auto-select first item if available
    if (data.list.length > 0) {
      selectedRecord.value = data.list[0];
    }
  };

  /**
   * Debounced search handler for API summary filtering.
   */
  const handleSearch = debounce(duration.search, (value: string) => {
    params.value.pageNo = 1;
    if (value) {
      params.value.filters = [{ key: 'summary', value, op: SearchCriteria.OpEnum.MatchEnd }];
    } else {
      params.value.filters = [];
    }
    loadRecords();
  });

  /**
   * Handle pagination changes.
   */
  const handlePaginationChange = async (pageNo: number, pageSize: number) => {
    params.value.pageNo = pageNo;
    params.value.pageSize = pageSize;
    await loadRecords();
  };

  /**
   * Handle record selection from the list.
   */
  const handleRecordSelect = (record: RequestRecordItem) => {
    selectedRecord.value = record;
  };

  /**
   * Refresh the records list manually.
   */
  const refreshRecords = () => {
    loadRecords();
  };

  onMounted(() => {
    loadRecords();
  });

  return {
    params,
    recordList,
    total,
    loading,
    selectedRecord,
    loadRecords,
    handleSearch,
    handlePaginationChange,
    handleRecordSelect,
    refreshRecords
  };
}
