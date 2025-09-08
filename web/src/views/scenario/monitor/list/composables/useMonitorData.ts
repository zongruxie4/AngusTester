import type { Ref } from 'vue';
import { ref, watch } from 'vue';
import { scenario } from '@/api/tester';
import type { MonitorInfo, MonitorListParams, SearchPanelParams, UseMonitorDataReturn } from '../../types';

export function useMonitorData (projectId: Ref<string>, notify: Ref<string>): UseMonitorDataReturn {
  // Data state
  const loaded = ref(false);
  const loading = ref(false);
  const searchedFlag = ref(false);
  const dataList = ref<MonitorInfo[]>([]);
  const total = ref(0);
  const pageNo = ref(1);
  const pageSize = ref(16);
  const searchPanelParams = ref<SearchPanelParams>({
    orderBy: undefined,
    orderSort: undefined,
    filters: []
  });

  /**
   * Load monitor list data from API
   */
  const loadData = async (): Promise<void> => {
    loading.value = true;

    const params: MonitorListParams = {
      projectId: projectId.value,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      ...searchPanelParams.value
    };

    const [error, res] = await scenario.getMonitorList(params);
    loaded.value = true;
    loading.value = false;

    // Update search flag based on filters and sorting
    searchedFlag.value = !!(params.filters?.length || params.orderBy);

    if (error) {
      total.value = 0;
      dataList.value = [];
      return;
    }

    const data = res?.data || { total: 0, list: [] };
    if (data) {
      total.value = +data.total;
      dataList.value = data.list || [];
    }
  };

  /**
   * Refresh data by resetting pagination and reloading
   */
  const refresh = (): void => {
    pageNo.value = 1;
    loadData();
  };

  /**
   * Handle search panel changes
   * @param data - Search panel parameters
   */
  const searchChange = (data: SearchPanelParams): void => {
    pageNo.value = 1;
    searchPanelParams.value = data;
    loadData();
  };

  // Watch for project ID changes to reload data
  watch(() => projectId.value, () => {
    pageNo.value = 1;
    loadData();
  }, { immediate: true });

  // Watch for notification changes to reload data
  watch(() => notify.value, (newValue) => {
    if (!newValue) {
      return;
    }
    loadData();
  }, { immediate: false });

  return {
    // Data state
    loaded,
    loading,
    searchedFlag,
    dataList,
    total,
    pageNo,
    pageSize,
    searchPanelParams,

    // Methods
    loadData,
    refresh,
    searchChange
  };
}
