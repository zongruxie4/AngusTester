import { ref } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { apis } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import type { TrashItem } from '../types';

/**
 * Composable for managing trash actions (recover, delete, batch operations)
 * @param projectId - Current project ID
 * @returns Object containing action methods and loading state
 */
export function useTrashActions (projectId: string) {
  const { t } = useI18n();
  const loading = ref(false);
  const refreshNotify = ref<string>();

  /**
   * Recover a single trash item
   * @param item - Trash item to recover
   * @returns Promise resolving to success status
   */
  const recoverItem = async (item: TrashItem): Promise<boolean> => {
    loading.value = true;

    try {
      const [error] = await apis.backTrash(item.id);

      if (error) {
        throw error;
      }

      notification.success(t('apiTrash.messages.recoverSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to recover item:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Delete a single trash item permanently
   * @param item - Trash item to delete
   * @returns Promise resolving to success status
   */
  const deleteItem = async (item: TrashItem): Promise<boolean> => {
    loading.value = true;

    try {
      const [error] = await apis.deleteTrash(item.id);

      if (error) {
        throw error;
      }

      notification.success(t('tips.deleteSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to delete item:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Recover all trash items in current project
   * @returns Promise resolving to success status
   */
  const recoverAll = async (): Promise<boolean> => {
    loading.value = true;

    try {
      const params = { projectId };
      const [error] = await apis.backAllTrash(params, { paramsType: true });

      if (error) {
        throw error;
      }

      notification.success(t('apiTrash.messages.recoverAllSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to recover all items:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Delete all trash items in current project permanently
   * @returns Promise resolving to success status
   */
  const deleteAll = async (): Promise<boolean> => {
    loading.value = true;

    try {
      const params = { projectId };
      const [error] = await apis.deleteAllTrash(params);

      if (error) {
        throw error;
      }

      notification.success(t('apiTrash.messages.deleteAllSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to delete all items:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Trigger data refresh by generating new refresh token
   */
  const triggerRefresh = () => {
    refreshNotify.value = utils.uuid();
  };

  /**
   * Manual refresh action
   */
  const refresh = () => {
    triggerRefresh();
  };

  return {
    // State
    loading,
    refreshNotify,

    // Actions
    recoverItem,
    deleteItem,
    recoverAll,
    deleteAll,
    refresh,
    triggerRefresh
  };
}
