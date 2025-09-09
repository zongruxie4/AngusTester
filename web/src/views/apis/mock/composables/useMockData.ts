import { ref, computed, inject, Ref } from 'vue';
import { SearchCriteria } from '@xcan-angus/infra';
import { MockService } from '../types';
import { mock } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import { MockServicePermission, MockServiceStatus } from '@/enums/enums';

/**
 * Composable for managing mock service data and related operations
 * Handles data fetching, state management, and CRUD operations for mock services
 */
export function useMockData () {
  // Reactive state for mock service data
  const loading = ref(false);
  const tableData = ref<MockService[]>([]);
  const total = ref(0);
  const params = ref({
    pageNo: 1,
    pageSize: 10,
    filters: [] as SearchCriteria[],
    orderBy: undefined as string | undefined,
    orderSort: undefined as 'ASC' | 'DESC' | undefined
  });

  // Inject project information
  const projectId = inject<Ref<string>>('projectId', ref(''));

  const pagination = computed(() => ({
    current: params.value.pageNo,
    pageSize: params.value.pageSize,
    total: total.value
  }));

  /**
   * Fetch mock service list from API
   */
  const fetchList = async () => {
    if (loading.value) {
      return;
    }

    if (!projectId.value) {
      return;
    }

    loading.value = true;
    const [error, { data = { list: [], total: 0 } }] = await mock.getServiceList({
      ...params.value,
      projectId: projectId.value
    });
    loading.value = false;

    if (error) {
      return;
    }

    // Process the data to add currentAuthsValue field
    tableData.value = data.list.map(item => {
      return {
        ...item,
        currentAuthsValue: item.currentAuths.map(i => i.value)
      };
    });

    total.value = +data.total;
  };

  /**
   * Handle search parameter changes
   */
  const handleSearchChange = (data: SearchCriteria[]) => {
    params.value.pageNo = 1;
    params.value.filters = data;
    fetchList();
  };

  /**
   * Handle table pagination and sorting changes
   */
  const handleTableChange = (_pagination: any, _filters: any, sorter: any) => {
    const { current, pageSize } = _pagination;
    params.value.pageNo = current;
    params.value.pageSize = pageSize;
    params.value.orderBy = sorter.orderBy;
    params.value.orderSort = sorter.orderSort;
    fetchList();
  };

  /**
   * Update table data with new service data
   */
  const updateTableData = (newData: MockService) => {
    for (let i = 0; i < tableData.value.length; i++) {
      const item = tableData.value[i];

      if (item.id === newData.id) {
        let currentAuthsValue: MockServicePermission[] = [];

        if (!newData.auth) {
          if (newData.currentAuths?.some(m => m.value === MockServicePermission.GRANT)) {
            currentAuthsValue = [MockServicePermission.EXPORT, MockServicePermission.GRANT];
          } else {
            currentAuthsValue = [MockServicePermission.EXPORT];
          }
        } else {
          currentAuthsValue = newData.currentAuths?.map(m => m.value)
            .filter(f => f !== MockServicePermission.DELETE) || [];
        }

        if (newData.status?.value === MockServiceStatus.NOT_STARTED) {
          currentAuthsValue = [...currentAuthsValue, MockServicePermission.DELETE];
        }

        tableData.value[i] = {
          ...newData,
          currentAuthsValue
        };
        break;
      }
    }
  };

  /**
   * Get current page after deletion
   */
  const getCurrentPageAfterDeletion = () => {
    params.value.pageNo = getCurrentPage(
      params.value.pageNo as number,
      params.value.pageSize as number,
      total.value
    );
  };

  return {
    // State
    loading,
    tableData,
    total,
    params,
    projectId,

    // Computed
    pagination,

    // Methods
    fetchList,
    handleSearchChange,
    handleTableChange,
    updateTableData,
    getCurrentPageAfterDeletion
  };
}
