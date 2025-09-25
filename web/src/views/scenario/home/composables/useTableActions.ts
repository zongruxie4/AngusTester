import { inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { modal, notification } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';
import type { ScenarioItem } from '../types';

/**
 * Composable for managing table actions
 * Handles delete, unfavorite, and unfollow operations
 */
export function useTableActions (
  loadData: () => Promise<void>,
  emit: {
    (e: 'update:deletedNotify', value: string): void;
  }
) {
  const { t } = useI18n();
  const loading = ref(false);

  // Inject refresh notification function from parent
  const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

  /**
   * Handle scenario deletion with confirmation
   */
  const handleDelete = (data: ScenarioItem): void => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: data.name }),
      async onOk () {
        try {
          const [error] = await scenario.deleteScenario(data.id);
          if (error) {
            console.error('Failed to delete scenario:', error);
            return;
          }

          notification.success(t('actions.tips.deleteSuccess'));
          emit('update:deletedNotify', utils.uuid());

          // Trigger refresh notification if available
          if (typeof updateRefreshNotify === 'function') {
            updateRefreshNotify(utils.uuid());
          }
        } catch (error) {
          console.error('Error deleting scenario:', error);
        }
      }
    });
  };

  /**
   * Handle unfavorite action
   */
  const handleUnfavorite = async (data: ScenarioItem): Promise<void> => {
    loading.value = true;

    try {
      const [error] = await scenario.deleteScenarioFavorite(data.id);
      if (error) {
        console.error('Failed to unfavorite scenario:', error);
        return;
      }

      notification.success(t('scenarioHome.myScenarios.table.messages.unfavoriteSuccess'));
      await loadData();

      // Trigger refresh notification if available
      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    } catch (error) {
      console.error('Error unfavoriting scenario:', error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * Handle unfollow action
   */
  const handleUnfollow = async (data: ScenarioItem): Promise<void> => {
    loading.value = true;

    try {
      const [error] = await scenario.deleteScenarioFollow(data.id);
      if (error) {
        console.error('Failed to unfollow scenario:', error);
        return;
      }

      notification.success(t('scenarioHome.myScenarios.table.messages.unfollowSuccess'));
      await loadData();

      // Trigger refresh notification if available
      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    } catch (error) {
      console.error('Error unfollowing scenario:', error);
    } finally {
      loading.value = false;
    }
  };

  return {
    loading,
    handleDelete,
    handleUnfavorite,
    handleUnfollow
  };
}
