import { onMounted, reactive, ref } from 'vue';
import { setting } from '@/api/gm';
import { appContext } from '@xcan-angus/infra';
import type { QuotaInfo } from '../types';

/**
 * Composable for managing quota data and related operations
 */
export function useQuotaData () {
  const userInfo = ref(appContext.getUser());

  // Pagination configuration
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0
  });

  // Data and loading state
  const loading = ref(false);
  const dataSource = ref<QuotaInfo[]>([]);

  /**
   * Load quota data from the API
   */
  const loadData = async () => {
    loading.value = true;
    const { current, pageSize } = pagination;

    if (!userInfo.value?.id) {
      loading.value = false;
      return;
    }

    const [error, res] = await setting.getQuotaList({
      appCode: 'AngusTester',
      tenantId: userInfo.value.id,
      pageNo: current,
      pageSize
    });

    loading.value = false;
    if (error) {
      return;
    }

    dataSource.value = res.data.list || [];
    pagination.total = +res.data.total;
  };

  /**
   * Handle pagination changes
   * @param page - The new pagination information
   */
  const changePage = (page: { current: number; pageSize: number }) => {
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadData();
  };

  /**
   * Initialize data on component mount
   */
  const init = () => {
    onMounted(() => {
      loadData();
    });
  };

  return {
    // Reactive data
    loading,
    dataSource,
    pagination,

    // Methods
    loadData,
    changePage,
    init
  };
}
