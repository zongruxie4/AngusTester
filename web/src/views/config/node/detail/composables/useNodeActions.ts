import { onBeforeUnmount, ref } from 'vue';
import { appContext, toClipboard, i18n } from '@xcan-angus/infra';
import { notification } from '@xcan-angus/vue-ui';

/**
 * Composable for managing node utility functions and system operations
 * <p>
 * Provides utility functions for clipboard operations, permission checks,
 * timer management, and system context information.
 * </p>
 */
export function useNodeActions () {
  const t = i18n.getI18n()?.global?.t || ((value: string) => value);

  // System context and permissions
  const isAdmin = ref(appContext.isAdmin());
  const tenantInfo = ref(appContext.getTenant());
  const userInfo = ref(appContext.getUser());

  // Timer management
  let timer: NodeJS.Timeout | null = null;

  /**
   * Copy content to clipboard
   * <p>
   * Copies the provided text to clipboard and shows success notification.
   * </p>
   */
  const copyContent = async (text: string) => {
    try {
      await toClipboard(text);
      notification.success(t('node.nodeDetail.labels.copySuccess'));
    } catch (error) {
      console.error('Failed to copy content:', error);
      notification.error(t('actions.tips.copyFailed'));
    }
  };

  /**
   * Refresh timer for metrics updates
   * <p>
   * Sets up an interval timer to refresh node metrics every 5 seconds
   * when the source tab is active.
   * </p>
   */
  const refreshTimer = (callback: () => void, activeKey: string) => {
    // Clear existing timer
    if (timer) {
      clearInterval(timer);
    }

    // Set up new timer
    timer = setInterval(() => {
      if (activeKey !== 'source') {
        return;
      }
      callback();
    }, 5000);
  };

  /**
   * Clear refresh timer
   * <p>
   * Cleans up the interval timer to prevent memory leaks.
   * </p>
   */
  const clearRefreshTimer = () => {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  };

  /**
   * Check if user can perform actions on the node
   * <p>
   * Verifies user permissions based on tenant ownership and admin status.
   * </p>
   */
  const canPerformActions = (nodeTenantId?: string, nodeCreatedBy?: string): boolean => {
    return nodeTenantId === tenantInfo.value?.id &&
           (isAdmin.value || nodeCreatedBy === userInfo.value?.id);
  };

  /**
   * Check if user can delete the node
   * <p>
   * Verifies user permissions and node status for deletion operations.
   * </p>
   */
  const canDeleteNode = (node: any): boolean => {
    if (node.enabled) {
      return false; // Cannot delete enabled nodes
    }

    if (!isAdmin.value) {
      return false; // Only admins can delete nodes
    }

    if (node.source?.value !== 'OWN_NODE') {
      return false; // Can only delete own nodes
    }

    return canPerformActions(node.tenantId, node.createdBy);
  };

  /**
   * Check if user can install agent
   * <p>
   * Verifies user permissions for agent installation operations.
   * </p>
   */
  const canInstallAgent = (): boolean => {
    return isAdmin.value;
  };

  /**
   * Cleanup function for component unmount
   * <p>
   * Ensures proper cleanup of timers and other resources.
   * </p>
   */
  const cleanup = () => {
    clearRefreshTimer();
  };

  // Auto-cleanup on unmount
  onBeforeUnmount(cleanup);

  return {
    // State
    isAdmin,
    tenantInfo,
    userInfo,

    // Methods
    copyContent,
    refreshTimer,
    clearRefreshTimer,
    canPerformActions,
    canDeleteNode,
    canInstallAgent,
    cleanup
  };
}
