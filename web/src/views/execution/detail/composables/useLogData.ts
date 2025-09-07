import { computed, ref, watch } from 'vue';
import { getDataByProxy } from '@/api/proxy';

/**
 * Composable for managing log data in execution detail
 * Handles loading, displaying, and downloading execution logs
 *
 * @param execId - The execution ID to load logs for
 * @param loading - Loading state getter function
 * @param execNodes - Execution nodes array
 * @param lastSchedulingResult - Last scheduling result data
 * @param emit - Vue emit function for component events
 * @returns Log data management functions and reactive state
 */
export const useLogData = (
  execId: string,
  loading: any,
  execNodes: any[],
  lastSchedulingResult: any[],
  emit: any
) => {
  // Reactive state for log data
  const nodeId = ref<string>();
  const nodeIp = ref<string>();
  const nodePort = ref<string>('6807');
  const execLogContent = ref<string>();
  const execLogPath = ref<string>('');
  const execLogErr = ref<boolean>(false);
  const errorText = ref<string>();
  const showSchedulingLog = ref<string>('1');
  const showExecLog = ref<string>('1');

  /**
   * Load execution log content from agent
   * Fetches log data from the agent via proxy
   */
  const loadExecLog = async () => {
    // Show loading state
    emit('update:loading', true);

    // Fetch log data from agent via proxy
    const [error, res] = await getDataByProxy(
      `http://${nodeIp.value}:${nodePort.value}/actuator/runner/log/${execId}`,
      {},
      { timeout: 0 }
    );

    // Hide loading state
    emit('update:loading', false);

    // Handle error response
    if (error) {
      execLogErr.value = true;
      // Type assertion to access response property
      if ((error as any).response?.data) {
        errorText.value = (error as any).response.data;
      } else {
        errorText.value = undefined;
      }
      return;
    }

    // Update log data on success
    execLogErr.value = false;
    execLogPath.value = res.headers?.['xc-agent-log-path'];
    execLogContent.value = res?.data || '';
  };

  /**
   * Get scheduling log item for current node
   * Computed property that finds the scheduling log for the selected node
   */
  const schedulingLogItem = computed(() => {
    if (lastSchedulingResult.length) {
      // Find scheduling result for current node
      const foundItem = lastSchedulingResult.find(item => item.deviceId === nodeId.value);
      if (foundItem) {
        return foundItem;
      } else {
        // Fallback to item without deviceId
        const emptyItem = lastSchedulingResult.find(item => !item?.deviceId);
        if (emptyItem) {
          return emptyItem;
        }
        return undefined;
      }
    }
    return undefined;
  });

  /**
   * Handle node selection change
   * Updates node information and reloads logs when node selection changes
   *
   * @param _nodeId - Selected node ID
   * @param options - Node options object
   */
  const nodeSelectChange = (_nodeId: string, options: any) => {
    // Update node information
    nodeId.value = _nodeId;
    nodeIp.value = options?.publicIp || options.ip;

    // Reload logs for the selected node
    loadExecLog();
  };

  /**
   * Watch for execId changes to load logs
   * Automatically loads logs when execId changes
   */
  watch(() => execId, (newValue) => {
    if (newValue && execNodes?.length) {
      // Set initial node information
      nodeId.value = execNodes[0]?.id;
      nodeIp.value = execNodes[0]?.publicIp || execNodes[0]?.ip;
      nodePort.value = execNodes[0]?.agentPort || '6807';

      // Load logs if node ID is available
      if (!nodeId.value) {
        return;
      }
      loadExecLog();
    }
  }, {
    immediate: true
  });

  /**
   * Handle double click on log sections
   * Toggles visibility of log sections on double click
   *
   * @param type - Log type (scheduling or execution)
   */
  const handleDoubleClick = (type: 'scheduling' | 'exec') => {
    if (type === 'exec') {
      // Toggle execution log visibility
      showExecLog.value = showExecLog.value === '1' ? '2' : '1';
      return;
    }
    // Toggle scheduling log visibility
    showSchedulingLog.value = showSchedulingLog.value === '1' ? '2' : '1';
  };

  /**
   * Toggle scheduling log visibility
   * Opens/closes the scheduling log section
   */
  const openSchedulingLog = () => {
    showSchedulingLog.value = showSchedulingLog.value === '1' ? '2' : '1';
  };

  /**
   * Toggle execution log visibility
   * Opens/closes the execution log section
   */
  const openExecLog = () => {
    showExecLog.value = showExecLog.value === '1' ? '2' : '1';
  };

  /**
   * Download log content as file
   * Creates and downloads a text file with log content
   *
   * @param type - Log type (scheduling or execution)
   */
  const downloadLog = (type: 'scheduling' | 'exec') => {
    // Get content based on log type
    const content = type === 'exec' ? execLogContent.value : schedulingLogItem.value?.console.join('\n');
    if (!content) {
      return;
    }

    // Create blob and download link
    const blob = new Blob([content], {
      type: 'text/plain'
    });

    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = type === 'exec' ? 'runnerLog.txt' : 'schedulingLog.txt';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  /**
   * Refresh execution log content
   * Reloads execution log data
   *
   * @param event - Click event
   */
  const refreshExecLog = (event: Event) => {
    // Prevent default behavior
    event.preventDefault();
    // Reload execution log
    loadExecLog();
  };

  // Return reactive references directly, not their .value properties
  return {
    nodeId,
    nodeIp,
    nodePort,
    execLogContent,
    execLogPath,
    execLogErr,
    errorText,
    showSchedulingLog,
    showExecLog,
    schedulingLogItem,
    loadExecLog,
    nodeSelectChange,
    handleDoubleClick,
    openSchedulingLog,
    openExecLog,
    downloadLog,
    refreshExecLog
  };
};
