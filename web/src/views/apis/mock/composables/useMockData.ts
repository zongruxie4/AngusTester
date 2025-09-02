import { ref, computed, inject } from 'vue';
import { MockServiceObj, Filter } from '../types';
import { mock } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';

/**
 * Composable for managing mock service data and related operations
 * Handles data fetching, state management, and CRUD operations for mock services
 */
export function useMockData () {
  // Reactive state for mock service data
  const loading = ref(false);
  const tableData = ref<MockServiceObj[]>([]);
  const total = ref(0);
  const params = ref({
    pageNo: 1,
    pageSize: 10,
    filters: [] as Filter[],
    orderBy: undefined as string | undefined,
    orderSort: undefined as 'ASC' | 'DESC' | undefined
  });

  // Project info injection
  const projectInfo = inject('projectInfo', ref({ id: '' }));

  // Computed properties
  const projectId = computed(() => projectInfo?.value?.id);
  const pagination = computed(() => ({
    current: params.value.pageNo,
    pageSize: params.value.pageSize,
    total: total.value
  }));

  /**
   * Fetch mock service list from API
   */
  const fetchMockServiceList = async () => {
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
      let currentAuthsValue: string[] = [];

      if (!item.auth) {
        if (item.currentAuths?.some(m => m.value === 'GRANT')) {
          currentAuthsValue = ['EXPORT', 'GRANT'];
        } else {
          currentAuthsValue = ['EXPORT'];
        }

        if (item.status?.value === 'NOT_STARTED') {
          currentAuthsValue = [...currentAuthsValue, 'DELETE'];
        }
      } else {
        currentAuthsValue = item.currentAuths?.map(m => m.value) || [];
        if (item.status?.value !== 'NOT_STARTED') {
          currentAuthsValue = currentAuthsValue.filter(f => f !== 'DELETE');
        }
      }

      return {
        ...item,
        currentAuthsValue
      };
    });

    total.value = +data.total;
  };

  /**
   * Handle search parameter changes
   */
  const handleSearchChange = (data: Filter[]) => {
    params.value.pageNo = 1;
    params.value.filters = data;
    fetchMockServiceList();
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
    fetchMockServiceList();
  };

  /**
   * Update table data with new service data
   */
  const updateTableData = (newData: MockServiceObj) => {
    for (let i = 0; i < tableData.value.length; i++) {
      const item = tableData.value[i];

      if (item.id === newData.id) {
        let currentAuthsValue: string[] = [];

        if (!newData.auth) {
          if (newData.currentAuths?.some(m => m.value === 'GRANT')) {
            currentAuthsValue = ['EXPORT', 'GRANT'];
          } else {
            currentAuthsValue = ['EXPORT'];
          }
        } else {
          currentAuthsValue = newData.currentAuths?.map(m => m.value).filter(f => f !== 'DELETE') || [];
        }

        if (newData.status?.value === 'NOT_STARTED') {
          currentAuthsValue = [...currentAuthsValue, 'DELETE'];
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
    fetchMockServiceList,
    handleSearchChange,
    handleTableChange,
    updateTableData,
    getCurrentPageAfterDeletion
  };
}
