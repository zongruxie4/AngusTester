import { ref, watch } from 'vue';
import { nodeCtrl } from 'src/api/ctrl';
import { useI18n } from 'vue-i18n';
import type { ExecutionRecord, ExecutionColumn } from '../types';

/**
 * <p>Composable for managing execution data and operations</p>
 * <p>Handles execution data fetching and table configuration</p>
 *
 * @param nodeId - The identifier of the node
 * @returns Object containing execution data and related methods
 */
export function useExecution (nodeId: string) {
  const { t } = useI18n();

  /**
   * <p>Execution data source for table display</p>
   * <p>Contains list of execution records for the specified node</p>
   */
  const executionDataSource = ref<ExecutionRecord[]>([]);

  /**
   * <p>Loading state indicator</p>
   * <p>Tracks whether data is currently being fetched</p>
   */
  const isLoading = ref(false);

  /**
   * <p>Error state for API operations</p>
   * <p>Stores any error messages from failed API calls</p>
   */
  const error = ref<string | null>(null);

  /**
   * <p>Fetches execution data from the API</p>
   * <p>Retrieves execution information for the specified node</p>
   *
   * @returns Promise that resolves when data loading is complete
   */
  const loadExecutionData = async (): Promise<void> => {
    if (!nodeId) {
      return;
    }

    try {
      isLoading.value = true;
      error.value = null;

      const [apiError, { data }] = await nodeCtrl.getExecInfo(nodeId);

      if (apiError) {
        error.value = 'Failed to load execution data';
        return;
      }

      executionDataSource.value = data || [];
    } catch (err) {
      error.value = 'An unexpected error occurred while loading execution data';
      console.error('Error loading execution data:', err);
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * <p>Table column configuration for execution</p>
   * <p>Defines the structure and labels for each column in the execution table</p>
   */
  const tableColumns: ExecutionColumn[] = [
    {
      key: 'execId',
      dataIndex: 'id',
      title: t('common.execId'),
      width: 120
    },
    {
      key: 'execName',
      dataIndex: 'name',
      title: t('common.execName'),
      width: 200
    },
    {
      key: 'testType',
      dataIndex: 'scriptType',
      title: t('node.nodeDetail.execute.columns.testType'),
      width: 120
    },
    {
      key: 'plugin',
      dataIndex: 'plugin',
      title: t('common.plugin'),
      width: 150
    },
    {
      key: 'executor',
      dataIndex: 'execByName',
      title: t('node.nodeDetail.execute.columns.executor'),
      width: 120
    },
    {
      key: 'startTime',
      dataIndex: 'actualStartDate',
      title: t('node.nodeDetail.execute.columns.startTime'),
      width: 180
    }
  ];

  /**
   * <p>Refreshes the current execution data</p>
   * <p>Reloads data without changing any state</p>
   */
  const refreshData = (): void => {
    loadExecutionData();
  };

  // Watch for changes in nodeId to reload data
  watch(() => nodeId, async (newValue) => {
    if (newValue) {
      await loadExecutionData();
    }
  }, {
    immediate: true
  });

  return {
    // State
    executionDataSource,
    isLoading,
    error,

    // Methods
    loadExecutionData,
    refreshData,

    // Configuration
    tableColumns
  };
}
