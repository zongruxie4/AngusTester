import type { Ref } from 'vue';
import { ref, unref } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { scenario } from '@/api/tester';

/**
 * Composable for managing scenario trash action operations
 * @param projectId - Project identifier (can be reactive)
 * @returns Object containing action methods and loading state
 */
export function useTrashActions (projectId: string | Ref<string>) {
  const { t } = useI18n();
  const loading = ref(false);

  /**
   * Restore a single trash item
   * @param itemId - Item identifier to restore
   */
  const restoreItem = async (itemId: string): Promise<boolean> => {
    loading.value = true;

    const [error] = await scenario.backScenarioTrash(itemId);
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('scenarioTrash.messages.restoreSuccess'));
    return true;
  };

  /**
   * Permanently delete a single trash item
   * @param itemId - Item identifier to delete
   */
  const deleteItem = async (itemId: string): Promise<boolean> => {
    loading.value = true;

    const [error] = await scenario.deleteScenarioTrash(itemId);
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('scenarioTrash.messages.deleteSuccess'));
    return true;
  };

  /**
   * Restore all trash items in the project
   */
  const restoreAll = async (): Promise<boolean> => {
    const currentProjectId = unref(projectId);
    if (!currentProjectId) return false;

    loading.value = true;

    const [error] = await scenario.backAllScenarioTrash({ projectId: currentProjectId });
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('scenarioTrash.messages.restoreAllSuccess'));
    return true;
  };

  /**
   * Permanently delete all trash items in the project
   */
  const deleteAll = async (): Promise<boolean> => {
    const currentProjectId = unref(projectId);
    if (!currentProjectId) return false;

    loading.value = true;

    const [error] = await scenario.deleteAllScenarioTrash({ projectId: currentProjectId });
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('scenarioTrash.messages.deleteAllSuccess'));
    return true;
  };

  return {
    loading,
    restoreItem,
    deleteItem,
    restoreAll,
    deleteAll
  };
}
