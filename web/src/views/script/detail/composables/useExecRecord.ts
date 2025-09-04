import { ref } from 'vue';

import { ExecInfo } from '@/views/script/types';

/**
 * Execution record management composable
 * Handles execution record data and display
 */
export function useExecRecord () {
  const dataList = ref<ExecInfo[]>([]);

  /**
   * Handle scroll change event
   */
  const handleScrollChange = (list: ExecInfo[]) => {
    dataList.value = list;
  };

  /**
   * Status background color mapping
   */
  const BG_COLOR = {
    CREATED: 'bg-status-pending',
    PENDING: 'bg-status-orange',
    RUNNING: 'bg-status-process',
    STOPPED: 'bg-status-close',
    FAILED: 'bg-status-error',
    COMPLETED: 'bg-status-success'
  };

  return {
    // Record data
    dataList,

    // Record methods
    handleScrollChange,
    BG_COLOR
  };
}
