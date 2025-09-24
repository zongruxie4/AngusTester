import { inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { scenario } from '@/api/tester';
import type { MonitorInfo, MonitorParams } from '../../types';

/**
 * Composable for managing monitor CRUD operations
 */
export function useMonitorActions () {
  const { t } = useI18n();
  const loading = ref(false);

  const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
  const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

  /**
   * Load monitor detail data
   * @param id - Monitor ID
   * @returns Promise<MonitorInfo | null>
   */
  const loadMonitorDetail = async (id: string): Promise<MonitorInfo | null> => {
    if (loading.value) {
      return null;
    }

    loading.value = true;
    const [error, res] = await scenario.getMonitorDetail(id);
    loading.value = false;

    if (error) {
      return null;
    }

    return res?.data as MonitorInfo || null;
  };

  /**
   * Update existing monitor
   * @param params - Monitor parameters
   */
  const updateMonitor = async (params: MonitorParams): Promise<boolean> => {
    loading.value = true;
    const [error] = await scenario.updateMonitor(params);
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('actions.tips.modifySuccess'));
    return true;
  };

  /**
   * Create new monitor
   * @param params - Monitor parameters
   */
  const createMonitor = async (params: MonitorParams): Promise<boolean> => {
    loading.value = true;
    const [error] = await scenario.addMonitor(params);
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('actions.tips.addSuccess'));
    return true;
  };

  /**
   * Refresh monitor list
   */
  const refreshMonitorList = () => {
    updateTabPane({ _id: 'monitorList', notify: Date.now().toString() });
  };

  /**
   * Close current tab pane
   * @param tabId - Tab ID to close
   */
  const closeTabPane = (tabId: string) => {
    deleteTabPane([tabId]);
  };

  return {
    loading,
    loadMonitorDetail,
    updateMonitor,
    createMonitor,
    refreshMonitorList,
    closeTabPane
  };
}
