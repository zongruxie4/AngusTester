import { ref } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { func } from '@/api/tester';

/**
 * Composable for managing trash action operations
 * @param projectId - Project identifier
 * @returns Object containing action methods and loading state
 */
export function useTrashActions (projectId: string) {
  const { t } = useI18n();
  const loading = ref(false);

  /**
   * Recover a single trash item
   * @param itemId - Item identifier to recover
   */
  const recoverItem = async (itemId: string): Promise<boolean> => {
    loading.value = true;

    const [error] = await func.backTrash(itemId);
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('actions.tips.recoverSuccess'));
    return true;
  };

  /**
   * Permanently delete a single trash item
   * @param itemId - Item identifier to delete
   */
  const deleteItem = async (itemId: string): Promise<boolean> => {
    loading.value = true;

    const [error] = await func.deleteTrash(itemId);
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('actions.tips.deleteSuccess'));
    return true;
  };

  /**
   * Recover all trash items in the project
   */
  const recoverAll = async (): Promise<boolean> => {
    if (!projectId) return false;

    loading.value = true;

    const [error] = await func.backAllTrash({ projectId });
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('actions.tips.recoverAllSuccess'));
    return true;
  };

  /**
   * Permanently delete all trash items in the project
   */
  const deleteAll = async (): Promise<boolean> => {
    if (!projectId) return false;

    loading.value = true;

    const [error] = await func.deleteAllTrash({ projectId });
    loading.value = false;

    if (error) {
      return false;
    }

    notification.success(t('actions.tips.deleteAllSuccess'));
    return true;
  };

  return {
    loading,
    recoverItem,
    deleteItem,
    recoverAll,
    deleteAll
  };
}
