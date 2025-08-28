import { ref } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import type { TaskTrashItem } from '../types';

/**
 * Composable for managing task trash actions (recover, delete, batch operations)
 * @param projectId - Current project ID
 * @returns Object containing action methods and loading state
 */
export function useTrashActions (projectId: string) {
  const { t } = useI18n();
  const loading = ref(false);
  const refreshNotify = ref<string>();

  /**
   * Recover a single task trash item
   * @param item - Task trash item to recover
   * @returns Promise resolving to success status
   */
  const recoverItem = async (item: TaskTrashItem): Promise<boolean> => {
    loading.value = true;

    try {
      const [error] = await task.backTrashTask(item.id);
      if (error) {
        throw error;
      }
      notification.success(t('taskTrash.messages.recoverSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to recover task item:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Delete a single task trash item permanently
   * @param item - Task trash item to delete
   * @returns Promise resolving to success status
   */
  const deleteItem = async (item: TaskTrashItem): Promise<boolean> => {
    loading.value = true;
    try {
      const [error] = await task.deleteTrashTask(item.id);
      if (error) {
        throw error;
      }
      notification.success(t('taskTrash.messages.deleteSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to delete task item:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Recover all task trash items in current project
   * @returns Promise resolving to success status
   */
  const recoverAll = async (): Promise<boolean> => {
    loading.value = true;
    try {
      const params = { projectId };
      const [error] = await task.backAllTrashTask(params);
      if (error) {
        throw error;
      }
      notification.success(t('taskTrash.messages.recoverAllSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to recover all task items:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Delete all task trash items in current project permanently
   * @returns Promise resolving to success status
   */
  const deleteAll = async (): Promise<boolean> => {
    loading.value = true;
    try {
      const params = { projectId };
      const [error] = await task.deleteAllTrashTask(params);
      if (error) {
        throw error;
      }
      notification.success(t('taskTrash.messages.deleteAllSuccess'));
      triggerRefresh();
      return true;
    } catch (error) {
      console.error('Failed to delete all task items:', error);
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
