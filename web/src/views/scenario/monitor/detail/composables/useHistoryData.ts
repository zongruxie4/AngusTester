import { ref, watch } from 'vue';
import { scenario } from '@/api/tester';
import type { HistoryRecord, HistoryExecData } from '../types';

/**
 * Composable for managing execution history data
 */
export function useHistoryData() {
  // History data state
  const historyList = ref<HistoryRecord[]>([]);
  const waitingHistory = ref<number[]>([]);
  const currentHistoryId = ref<string>();
  const currentExecId = ref<string>();
  const historyExecData = ref<HistoryExecData>();
  const loadHistoryContent = ref(false);

  /**
   * Load execution history list for a monitor
   * @param monitorId - Monitor ID
   */
  const loadHistoryList = async (monitorId: string): Promise<void> => {
    try {
      const [error, { data }] = await scenario.getMonitorHistoryList({
        monitorId,
        pageSize: 100,
        pageNo: 1
      });

      if (error) {
        console.error('Failed to load history list:', error);
        return;
      }

      historyList.value = data?.list || [];
      
      // Set default current history if none selected
      if (!currentHistoryId.value && historyList.value.length > 0) {
        currentHistoryId.value = historyList.value[0].id;
        currentExecId.value = historyList.value[0].execId;
      }

      // Generate waiting history placeholders
      waitingHistory.value = Array.from(
        new Array(100 - historyList.value.length).fill(0)
      );
    } catch (error) {
      console.error('Error loading history list:', error);
    }
  };

  /**
   * Change current selected history record
   * @param history - History record to select
   */
  const changeHistory = (history: { id: string; execId: string }): void => {
    if (loadHistoryContent.value) {
      return;
    }

    currentHistoryId.value = history.id;
    currentExecId.value = history.execId;
  };

  /**
   * Load execution data for current history record
   */
  const loadExecData = async (): Promise<void> => {
    if (!currentHistoryId.value) {
      return;
    }

    loadHistoryContent.value = true;
    
    try {
      const [error, { data }] = await scenario.getMonitorHistoryDetail(currentHistoryId.value);
      
      if (error) {
        console.error('Failed to load execution data:', error);
        return;
      }

      historyExecData.value = data;
      
      // Process sample log content
      if (historyExecData.value?.sampleLogContent) {
        historyExecData.value.sampleLogContent = 
          historyExecData.value.sampleLogContent.replaceAll('\\n', '\n');
      }
    } catch (error) {
      console.error('Error loading execution data:', error);
    } finally {
      loadHistoryContent.value = false;
    }
  };

  /**
   * Watch for current history ID changes and load execution data
   */
  const watchCurrentHistory = (): void => {
    watch(currentHistoryId, () => {
      loadExecData();
    });
  };

  /**
   * Reset history data
   */
  const resetHistoryData = (): void => {
    historyList.value = [];
    waitingHistory.value = [];
    currentHistoryId.value = undefined;
    currentExecId.value = undefined;
    historyExecData.value = undefined;
    loadHistoryContent.value = false;
  };

  return {
    // State
    historyList,
    waitingHistory,
    currentHistoryId,
    currentExecId,
    historyExecData,
    loadHistoryContent,
    
    // Methods
    loadHistoryList,
    changeHistory,
    loadExecData,
    watchCurrentHistory,
    resetHistoryData
  };
}
