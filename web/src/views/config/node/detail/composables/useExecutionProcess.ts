import { ref, onMounted } from 'vue';
import { modal, notification } from '@xcan-angus/vue-ui';
import { node } from '@/api/tester';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import type { ProcessInfo, NodeProcessSummary, ExecutionPropulsionColumn } from '../types';

/**
 * <p>Composable for managing execution propulsion data and operations</p>
 * <p>Handles process data fetching, process management, and table configuration</p>
 *
 * @param nodeId - The identifier of the node
 * @param tenantId - The identifier of the tenant for access control
 * @returns Object containing process data and related methods
 */
export function useExecutionProcess (nodeId: string, tenantId?: string) {
  const { t } = useI18n();

  /**
   * <p>Tenant information for access control</p>
   * <p>Retrieved from application context</p>
   */
  const tenantInfo = ref(appContext.getTenant());

  /**
   * <p>Node process summary data</p>
   * <p>Contains aggregated process statistics and process list</p>
   */
  const processSummary = ref<NodeProcessSummary>({
    processCount: 0,
    threadCount: 0,
    openFiles: 0,
    bytesWritten: '0',
    bytesRead: '0',
    processes: []
  });

  /**
   * <p>Process data list for table display</p>
   * <p>Contains processed data with row spanning for better visualization</p>
   */
  const processDataList = ref<ProcessInfo[]>([]);

  /**
   * <p>Loading state indicator</p>
   * <p>Tracks whether data is currently being fetched</p>
   */
  const isLoading = ref(false);

  /**
   * <p>Fetches node process data from the API</p>
   * <p>Retrieves process information and statistics for the specified node</p>
   *
   * @returns Promise that resolves when data loading is complete
   */
  const loadNodeProcess = async (): Promise<void> => {
    if (isLoading.value || !nodeId) {
      return;
    }

    try {
      isLoading.value = true;

      const [error, { data = {} }] = await node.getRunnerProcess({
        nodeId,
        broadcast: true
      });

      if (error) {
        return;
      }

      const nodeData = data as NodeProcessSummary;
      processSummary.value = nodeData || {
        processCount: 0,
        threadCount: 0,
        openFiles: 0,
        bytesWritten: '0',
        bytesRead: '0',
        processes: []
      };

      // Process data for table display with row spanning
      processDataList.value = (nodeData?.processes || []).reduce<ProcessInfo[]>((acc, current) => {
        acc.push(current, current);
        return acc;
      }, []);
    } catch (err) {
      console.error('Error loading node process data:', err);
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * <p>Kills a specific process</p>
   * <p>Shows confirmation dialog and executes process termination</p>
   *
   * @param process - The process information to be terminated
   */
  const killProcess = (process: ProcessInfo): void => {
    modal.confirm({
      content: t('node.nodeDetail.execPropulsion.confirmKillProcess', { processId: process.processID }),
      async onOk () {
        try {
          const [error] = await node.killRunnerProcess({
            nodeId,
            broadcast: true,
            pid: process.processID
          });

          if (error) {
            return;
          }

          notification.success(t('node.nodeDetail.execPropulsion.processExitSuccess'));
          await loadNodeProcess();
        } catch (err) {
          console.error('Error killing process:', err);
        }
      }
    });
  };

  /**
   * <p>Table column configuration for execution propulsion</p>
   * <p>Defines the structure and behavior of each column with custom cell rendering</p>
   */
  const tableColumns: ExecutionPropulsionColumn[] = [
    {
      key: 'processId',
      title: t('node.nodeDetail.execPropulsion.columns.processId'),
      dataIndex: 'processID',
      customCell: (_, index: number) => ({
        rowSpan: index % 2 === 0 ? 2 : 0
      })
    },
    {
      key: 'runTime',
      title: t('node.nodeDetail.execPropulsion.columns.runTime'),
      dataIndex: 'upTime',
      customCell: (_, index: number) => ({
        colSpan: index % 2 === 0 ? 1 : 7
      }),
      customRender: ({ record, index }: { record: ProcessInfo; index: number }) => {
        if (index % 2 === 0) {
          return record.upTime;
        } else {
          return record.commandLine.replaceAll('\u0000', ' ');
        }
      }
    },
    {
      key: 'user',
      title: t('node.nodeDetail.execPropulsion.columns.user'),
      dataIndex: 'user',
      customCell: (_, index: number) => ({
        colSpan: index % 2 === 0 ? 1 : 0
      })
    },
    {
      key: 'virtualMemory',
      title: t('node.nodeDetail.execPropulsion.columns.virtualMemory'),
      dataIndex: 'virtualSize',
      customCell: (_, index: number) => ({
        colSpan: index % 2 === 0 ? 1 : 0
      })
    },
    {
      key: 'threadCount',
      title: t('node.nodeDetail.execPropulsion.columns.threadCount'),
      dataIndex: 'threadCount',
      customCell: (_, index: number) => ({
        colSpan: index % 2 === 0 ? 1 : 0
      })
    },
    {
      key: 'openFiles',
      title: t('node.nodeDetail.execPropulsion.columns.openFiles'),
      dataIndex: 'openFiles',
      customCell: (_, index: number) => ({
        colSpan: index % 2 === 0 ? 1 : 0
      })
    },
    {
      key: 'writeDisk',
      title: t('node.nodeDetail.execPropulsion.columns.writeDisk'),
      dataIndex: 'bytesWritten',
      customCell: (_, index: number) => ({
        colSpan: index % 2 === 0 ? 1 : 0
      })
    },
    {
      key: 'readDisk',
      title: t('node.nodeDetail.execPropulsion.columns.readDisk'),
      dataIndex: 'bytesRead',
      customCell: (_, index: number) => ({
        colSpan: index % 2 === 0 ? 1 : 0
      })
    },
    {
      key: 'action',
      title: t('node.nodeDetail.execPropulsion.columns.action'),
      dataIndex: 'action',
      customCell: (_, index: number) => ({
        rowSpan: index % 2 === 0 ? 2 : 0
      })
    }
  ];

  /**
   * <p>Checks if the current user has permission to kill processes</p>
   * <p>Compares tenant ID with current user's tenant</p>
   */
  const canKillProcess = (): boolean => {
    return tenantId === tenantInfo.value?.id;
  };

  // Load data when component mounts
  onMounted(() => {
    if (nodeId) {
      loadNodeProcess();
    }
  });

  return {
    // State
    processSummary,
    processDataList,
    isLoading,
    tenantInfo,

    // Methods
    loadNodeProcess,
    killProcess,
    canKillProcess,

    // Configuration
    tableColumns
  };
}
