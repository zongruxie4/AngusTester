import { computed, ref } from 'vue';
import { exec } from '@/api/tester';
import type { ScenarioResult } from '../types';

/**
 * Composable for managing scenario test results
 */
export function useScenarioResults (scenarioId: string) {
  const dataSource = ref<ScenarioResult>();

  /**
   * Load scenario test results
   */
  const loadScenarioResult = async () => {
    const [error, { data }] = await exec.getScenarioResult(scenarioId);
    if (error) {
      return;
    }
    dataSource.value = data;
  };

  /**
   * Get execution ID for functionality tests
   */
  const funcExecId = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_FUNCTIONALITY?.execId;
  });

  /**
   * Get execution ID for performance tests
   */
  const perfExecId = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_PERFORMANCE?.execId;
  });

  /**
   * Get execution ID for customization tests
   */
  const customExecId = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_CUSTOMIZATION?.execId;
  });

  /**
   * Get execution ID for stability tests
   */
  const stabilityExecId = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_STABILITY?.execId;
  });

  /**
   * Handle result deletion and refresh
   */
  const handleResultDeletion = () => {
    loadScenarioResult();
  };

  return {
    dataSource,
    funcExecId,
    perfExecId,
    customExecId,
    stabilityExecId,
    loadScenarioResult,
    handleResultDeletion
  };
}
