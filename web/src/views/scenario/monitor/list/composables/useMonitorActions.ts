import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { modal } from '@xcan-angus/vue-ui';
import { scenario } from '@/api/tester';
import type { MonitorInfo, TabPaneInjection, UseMonitorActionsReturn } from '../../types';

export function useMonitorActions (
  addTabPane: TabPaneInjection['addTabPane'],
  deleteTabPane: TabPaneInjection['deleteTabPane'],
  refreshData: () => void
): UseMonitorActionsReturn {
  const { t } = useI18n();
  const router = useRouter();

  /**
   * Delete monitor with confirmation
   * @param data - Monitor information to delete
   */
  const toDelete = async (data: MonitorInfo): Promise<void> => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: data.name }),
      async onOk () {
        const id = data.id;
        const [error] = await scenario.deleteMonitor({
          ids: [id]
        });

        if (error) {
          return;
        }

        refreshData();
        deleteTabPane([id, id + '-case']);
      }
    });
  };

  /**
   * Open monitor edit tab
   * @param data - Monitor information to edit
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
   * Open monitor detail tab
   * @param data - Monitor information to view details
   */
  const handleDetail = (data: MonitorInfo): void => {
    addTabPane({
      value: 'monitorDetails',
      _id: data.id + '-case',
      id: data.id,
      data,
      name: data.name
    });
  };

  /**
   * Run monitor with confirmation
   * @param data - Monitor information to run
   */
  const run = async (data: MonitorInfo): Promise<void> => {
    modal.confirm({
      content: t('scenarioMonitor.list.executeConfirm', { name: data.name }),
      async onOk () {
        const id = data.id;
        const [error] = await scenario.runMonitor(id);

        if (error) {
          return;
        }

        refreshData();
      }
    });
  };

  /**
   * Navigate to scenario detail page
   * @param scenarioId - ID of the scenario to view
   */
  const getScenarioDetail = async (scenarioId: string): Promise<void> => {
    const [error, { data }] = await scenario.getScenarioDetail(scenarioId);

    if (error) {
      return;
    }

    const { id, name, plugin } = data;
    await router.push(`/scenario#scenario?id=${id}&name=${name}&plugin=${plugin}&type=detail`);
  };

  return {
    toDelete,
    editMonitor,
    handleDetail,
    run,
    getScenarioDetail
  };
}
