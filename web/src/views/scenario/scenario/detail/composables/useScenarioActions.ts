import { ref, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { scenario } from '@/api/tester';
import { modal, notification } from '@xcan-angus/vue-ui';
import type { ScenarioData } from '../types';

/**
 * Composable for managing scenario actions (delete, follow, favorite, etc.)
 */
export function useScenarioActions (
  scenarioData: Ref<ScenarioData | undefined>,
  scenarioId: string,
  deleteTabPane: (data: string[]) => void
) {
  const { t } = useI18n();

  const followLoading = ref(false);
  const favouriteLoading = ref(false);
  const exportVisible = ref(false);
  const toAuthVisible = ref(false);

  /**
   * Delete scenario with confirmation
   */
  const deleteScenario = () => {
    if (!scenarioData.value?.id) return;

    const currentScenario = scenarioData.value;
    const scenarioId = currentScenario.id;
    const scenarioName = currentScenario.name;

    modal.confirm({
      centered: true,
      content: t('actions.tips.confirmDelete', { name: scenarioName }),
      async onOk () {
        const [error] = await scenario.deleteScenario(scenarioId);
        if (error) {
          return;
        }
        notification.success(t('actions.tips.deleteSuccess'));
        deleteTabPane([scenarioId, scenarioId + '-detail']);
      }
    });
  };

  /**
   * Show export script modal
   */
  const showExportScript = () => {
    exportVisible.value = true;
  };

  /**
   * Show authorization modal
   */
  const showAuthorization = () => {
    toAuthVisible.value = true;
  };

  /**
   * Handle follow/unfollow action
   */
  const handleFollow = async () => {
    if (!scenarioData.value) return;

    const currentFollowFlag = scenarioData.value.follow;
    followLoading.value = true;
    const [error] = await (currentFollowFlag
      ? scenario.deleteScenarioFollow(scenarioId)
      : scenario.addScenarioFollow(scenarioId)
    );
    followLoading.value = false;

    if (error) {
      return;
    }

    if (currentFollowFlag) {
      notification.success(t('scenario.detail.messages.cancelFollowSuccess'));
    } else {
      notification.success(t('scenario.detail.messages.followSuccess'));
    }
    scenarioData.value.follow = !currentFollowFlag;
  };

  /**
   * Handle favorite/unfavorite action
   */
  const handleFavourite = async () => {
    if (!scenarioData.value) return;

    const currentFavouriteFlag = scenarioData.value.favourite;
    favouriteLoading.value = true;
    const [error] = await (currentFavouriteFlag
      ? scenario.deleteScenarioFavorite(scenarioId)
      : scenario.addScenarioFavorite(scenarioId)
    );
    favouriteLoading.value = false;

    if (error) {
      return;
    }

    if (currentFavouriteFlag) {
      notification.success(t('scenario.detail.messages.cancelFavouriteSuccess'));
    } else {
      notification.success(t('scenario.detail.messages.favouriteSuccess'));
    }
    scenarioData.value.favourite = !currentFavouriteFlag;
  };

  return {
    followLoading,
    favouriteLoading,
    exportVisible,
    toAuthVisible,
    deleteScenario,
    showExportScript,
    showAuthorization,
    handleFollow,
    handleFavourite
  };
}
