import { Ref } from 'vue';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { scenario } from '@/api/tester';
import { modal, notification } from '@xcan-angus/vue-ui';
import { ScenarioInfo } from '../types';

/**
 * Composable for managing scenario actions
 * @param projectId - The project ID
 * @param addTabPane - Function to add a tab pane
 * @param deleteTabPane - Function to delete a tab pane
 */
export function useScenarioActions (
  projectId: Ref<string | undefined>,
  addTabPane: (data: any) => void,
  deleteTabPane: (data: string[]) => void
) {
  const { t } = useI18n();

  /**
   * Create a new HTTP scenario
   */
  const createHttpScenario = () => {
    const name = 'scenario' + new Date().getTime();
    addTabPane({ name, _id: utils.uuid(), value: 'Http', scenarioInfo: { name, projectId: projectId.value } });
  };

  /**
   * Handle dropdown button clicks for different scenario types
   */
  const buttonDropdownClick = ({ key }: { key: 'Jdbc' | 'WebSocket' | 'Ftp' | 'Ldap' | 'Mail' | 'Smtp' | 'Tcp' }) => {
    const name = 'scenario' + new Date().getTime();
    const id = utils.uuid();
    const scenarioInfo = { name, projectId: projectId.value };

    switch (key) {
      case 'Jdbc':
        addTabPane({ name, _id: id, value: 'Jdbc', scenarioInfo: scenarioInfo });
        break;
      case 'Ftp':
        addTabPane({ name, _id: id, value: 'Ftp', scenarioInfo: scenarioInfo });
        break;
      case 'Ldap':
        addTabPane({ name, _id: id, value: 'Ldap', scenarioInfo: scenarioInfo });
        break;
      case 'Mail':
        addTabPane({ name, _id: id, value: 'Mail', scenarioInfo: scenarioInfo });
        break;
      case 'Smtp':
        addTabPane({ name, _id: id, value: 'Smtp', scenarioInfo: scenarioInfo });
        break;
      case 'Tcp':
        addTabPane({ name, _id: id, value: 'Tcp', scenarioInfo: scenarioInfo });
        break;
      case 'WebSocket':
        addTabPane({ name, _id: id, value: 'WebSocket', scenarioInfo: scenarioInfo });
        break;
    }
  };

  /**
   * Add scenario authorization
   */
  const addScenarioAuthorize = () => {
    addTabPane({ name: t('scenario.list.actions.scenarioAuth'), _id: utils.uuid(), value: 'authorization' });
  };

  /**
   * Handle scenario cloning
   */
  const cloneScenario = async (value: ScenarioInfo) => {
    const [error] = await scenario.cloneScenario(value.id);
    if (error) {
      return;
    }

    notification.success(t('tips.cloneSuccess'));
  };

  /**
   * Delete a scenario
   */
  const deleteScenario = async (name: string, id: string) => {
    modal.confirm({
      centered: true,
      content: t('scenario.list.messages.deleteConfirm', { name }),
      async onOk () {
        const [error] = await scenario.deleteScenario(id);
        if (error) {
          return;
        }

        deleteTabPane([id]); // Notify tabs to delete this tab
        notification.success(t('scenario.list.messages.deleteSuccess'));
      }
    });
  };

  /**
   * Handle scenario favorite actions
   */
  const toggleScenarioFavorite = async (id: string, isFavorite: boolean) => {
    if (isFavorite) {
      const [error] = await scenario.deleteScenarioFavorite(id);
      if (error) {
        return;
      }
      notification.success(t('scenario.list.messages.cancelFavouriteSuccess'));
    } else {
      const [error] = await scenario.addScenarioFavorite(id);
      if (error) {
        return;
      }
      notification.success(t('scenario.list.messages.favouriteSuccess'));
    }
  };

  /**
   * Handle scenario follow actions
   */
  const toggleScenarioFollow = async (id: string, isFollowing: boolean) => {
    if (isFollowing) {
      const [error] = await scenario.deleteScenarioFollow(id);
      if (error) {
        return;
      }
      notification.success(t('scenario.list.messages.cancelFollowSuccess'));
    } else {
      const [error] = await scenario.addScenarioFollow(id);
      if (error) {
        return;
      }
      notification.success(t('scenario.list.messages.followSuccess'));
    }
  };

  return {
    // Methods
    createHttpScenario,
    buttonDropdownClick,
    addScenarioAuthorize,
    cloneScenario,
    deleteScenario,
    toggleScenarioFavorite,
    toggleScenarioFollow
  };
}
