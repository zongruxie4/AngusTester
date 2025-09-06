import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { PageQuery } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import type { 
  SceneItem, 
  ScenarioQueryParams, 
  ScenarioListParams, 
  TablePagination 
} from '../types';

/**
 * Composable for managing scenario data operations
 * Handles data loading, pagination, and state management
 */
export function useData(
  projectId: string,
  params: ScenarioQueryParams,
  notify: string,
  deletedNotify: string
) {
  const { t } = useI18n();
  
  // Reactive data state
  const tableData = ref<SceneItem[]>();
  const loading = ref(false);
  const loaded = ref(false);
  const orderBy = ref<string>();
  const orderSort = ref<PageQuery.OrderSort>();

  // Pagination configuration
  const pagination = ref<TablePagination>({
    total: 0,
    current: 1,
    pageSize: 5,
    showSizeChanger: false,
    size: 'small',
    showTotal: (total: number) => {
      const totalPage = Math.ceil(total / pagination.value.pageSize);
      return t('scenarioHome.myScenarios.table.messages.pageInfo', { 
        current: pagination.value.current, 
        total: totalPage 
      });
    }
  });

  /**
   * Build API request parameters for scenario list
   */
  const buildRequestParams = (): ScenarioListParams => {
    const { current, pageSize } = pagination.value;
    const requestParams: ScenarioListParams = {
      projectId,
      pageNo: current,
      pageSize
    };

    // Add sorting parameters if available
    if (orderSort.value) {
      requestParams.orderBy = orderBy.value;
      requestParams.orderSort = String(orderSort.value);
    }

    // Add filter parameters
    if (params.createdBy) {
      requestParams.createdBy = params.createdBy;
    }
    if (params.favouriteBy) {
      requestParams.favouriteBy = params.favouriteBy;
    }
    if (params.followBy) {
      requestParams.followBy = params.followBy;
    }

    return requestParams;
  };

  /**
   * Load scenario data from API
   */
  const loadData = async (): Promise<void> => {
    loading.value = true;
    
    try {
      const requestParams = buildRequestParams();
      const [error, res] = await scenario.getScenarioList(requestParams);
      
      if (error) {
        console.error('Failed to load scenario data:', error);
        return;
      }

      const data = res?.data;
      tableData.value = data?.list || [];
      const total = +(data?.total || 0);
      pagination.value.total = total;
      
    } catch (error) {
      console.error('Error loading scenario data:', error);
    } finally {
      loading.value = false;
      loaded.value = true;
    }
  };

  /**
   * Handle table change events (pagination, sorting, filtering)
   */
  const handleTableChange = (
    { current = 1, pageSize = 10 }: { current?: number; pageSize?: number },
    _filters: any,
    sorter: { orderBy: string; orderSort: PageQuery.OrderSort }
  ): void => {
    orderBy.value = sorter.orderBy;
    orderSort.value = sorter.orderSort;
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
    loadData();
  };

  /**
   * Refresh data when project changes
   */
  const refreshOnProjectChange = (): void => {
    watch(() => projectId, () => {
      loadData();
    }, { immediate: true });
  };

  /**
   * Refresh data when notify prop changes
   */
  const refreshOnNotifyChange = (): void => {
    watch(() => notify, (newValue) => {
      if (newValue === undefined || newValue === null || newValue === '') {
        return;
      }
      loadData();
    }, { immediate: true });
  };

  /**
   * Refresh data when deleted notify prop changes
   */
  const refreshOnDeletedNotifyChange = (): void => {
    watch(() => deletedNotify, (newValue) => {
      if (newValue === undefined || newValue === null || newValue === '') {
        return;
      }

      pagination.value.current = getCurrentPage(
        pagination.value.current, 
        pagination.value.pageSize, 
        pagination.value.total
      );
      loadData();
    }, { immediate: true });
  };

  /**
   * Initialize watchers for reactive updates
   */
  const initializeWatchers = (): void => {
    refreshOnProjectChange();
    refreshOnNotifyChange();
    refreshOnDeletedNotifyChange();
  };

  return {
    // State
    tableData,
    loading,
    loaded,
    pagination,
    
    // Methods
    loadData,
    handleTableChange,
    initializeWatchers
  };
}
