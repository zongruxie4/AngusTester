import { onMounted, ref } from 'vue';
import { analysis } from '@/api/tester';

import {MockServiceStats} from "@/views/apis/mock/detail/types";

/**
 * Composable for managing mock service statistics.
 * <p>
 * Handles fetching and state management for API count, request count,
 * and various response type statistics.
 */
export function useApiStats(serviceId: string) {
  const stats = ref<MockServiceStats>({
    apisNum: '0',
    requestNum: '0',
    pushbackNum: '0',
    simulateErrorNum: '0',
    successNum: '0',
    exceptionNum: '0'
  });

  const loading = ref(false);

  /**
   * Fetch latest statistics from the analysis service.
   */
  const loadStats = async () => {
    if (!serviceId) return;

    loading.value = true;
    const [error, { data }] = await analysis.getAnalysisMockService(serviceId);
    loading.value = false;

    if (error) return;

    stats.value = data;
  };

  onMounted(() => {
    loadStats();
  });

  return {
    stats,
    loading,
    loadStats
  };
}
