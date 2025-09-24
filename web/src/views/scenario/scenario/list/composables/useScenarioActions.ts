import { Ref, ComputedRef } from 'vue';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { scenario } from '@/api/tester';
import { modal, notification } from '@xcan-angus/vue-ui';
import { ScenarioInfo, MenuItem } from '../types';
import { emit } from 'process';

/**
 * Composable for managing scenario actions
 * @param projectId - The project ID
 * @param addTabPane - Function to add a tab pane
 * @param deleteTabPane - Function to delete a tab pane
 * @param scenarioList - Reactive reference to scenario list
 * @param dropdownMenuItemsMap - Reactive reference to dropdown menu items map
 * @param dropdownMenuItems - Computed dropdown menu items
 * @param selectedId - Reactive reference to selected scenario ID
 */
export function useScenarioActions (
  projectId: Ref<string | undefined>,
  addTabPane: (data: any) => void,
  deleteTabPane: (data: string[]) => void,
  scenarioList: Ref<ScenarioInfo[]>,
  dropdownMenuItemsMap: Ref<{ [key: string]: MenuItem[] }>,
  dropdownMenuItems: ComputedRef<readonly MenuItem[]>,
  selectedId: Ref<string | undefined>
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

    notification.success(t('actions.tips.cloneSuccess'));
  };

  /**
   * Delete a scenario
   */
  const deleteScenario = async (name: string, id: string, emit: (e: 'delete', value: string) => void) => {
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
        emit('delete', id);
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

      const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'cancelFavourite');
      const data = dropdownMenuItems.value.find(item => item.key === 'addFavourite');
      dropdownMenuItemsMap.value[id].splice(index, 1, data!);
      notification.success(t('scenario.list.messages.cancelFavouriteSuccess'));
    } else {
      const [error] = await scenario.addScenarioFavorite(id);
      if (error) {
        return;
      }

      const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'addFavourite');
      const data = dropdownMenuItems.value.find(item => item.key === 'cancelFavourite');
      dropdownMenuItemsMap.value[id].splice(index, 1, data!);
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

      const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'cancelFollow');
      const data = dropdownMenuItems.value.find(item => item.key === 'addFollow');
      dropdownMenuItemsMap.value[id].splice(index, 1, data!);
      notification.success(t('scenario.list.messages.cancelFollowSuccess'));
    } else {
      const [error] = await scenario.addScenarioFollow(id);
      if (error) {
        return;
      }

      const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'addFollow');
      const data = dropdownMenuItems.value.find(item => item.key === 'cancelFollow');
      dropdownMenuItemsMap.value[id].splice(index, 1, data!);
      notification.success(t('scenario.list.messages.followSuccess'));
    }
  };

  /**
   * Handle scenario cloning with emit callback
   */
  const toClone = async (value: ScenarioInfo, emit: (e: 'clone', value: ScenarioInfo) => void) => {
    const [error] = await scenario.cloneScenario(value.id);
    if (error) {
      return;
    }

    emit('clone', value);
  };

  /**
   * Handle auth flag change
   */
  const authFlagChange = ({ auth: authFlag }: { auth: boolean }) => {
    const data = scenarioList.value;
    const targetId = selectedId.value;
    if (!targetId) return;

    for (let i = 0, len = data.length; i < len; i++) {
      if (data[i].id === targetId) {
        data[i].auth = authFlag;
        break;
      }
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
    toggleScenarioFollow,
    toClone,
    authFlagChange
  };
}
