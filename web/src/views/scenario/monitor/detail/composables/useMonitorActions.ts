import { inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { modal } from '@xcan-angus/vue-ui';
import { scenario } from '@/api/tester';
import type { MonitorInfo, AddTabPaneFunction } from '../types';

/**
 * Composable for managing monitor actions (edit, run, etc.)
 */
export function useMonitorActions () {
  const { t } = useI18n();

  // Inject addTabPane function from parent component
  const addTabPane = inject<AddTabPaneFunction>('addTabPane', () => ({}));

  /**
   * Open monitor edit dialog
   * @param data - Monitor data to edit
   */
  const editMonitor = (data: MonitorInfo): void => {
    addTabPane({
      value: 'monitorEdit',
      _id: data.id,
      id: data.id,
      data,
      name: data.name
    });
  };

  /**
   * Execute monitor with confirmation
   * @param data - Monitor data to execute
   * @param onSuccess - Callback function to call after successful execution
   */
  const runMonitor = async (
    data: MonitorInfo,
    onSuccess?: () => void
  ): Promise<void> => {
    modal.confirm({
      content: t('scenarioMonitor.list.executeConfirm', { name: data.name }),
      async onOk () {
        try {
          const [error] = await scenario.runMonitor(data.id);

          if (error) {
            console.error('Failed to run monitor:', error);
            return;
          }

          // Call success callback if provided
          if (onSuccess) {
            onSuccess();
          }
        } catch (error) {
          console.error('Error running monitor:', error);
        }
      }
    });
  };

  /**
   * Get status color configuration
   */
  const getStatusColorConfig = () => ({
    SUCCESS: 'success',
    PENDING: 'processing',
    FAILURE: 'default'
  });

  return {
    // Methods
    editMonitor,
    runMonitor,
    getStatusColorConfig
  };
}
