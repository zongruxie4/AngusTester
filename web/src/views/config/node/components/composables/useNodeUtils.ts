import { ref, onMounted } from 'vue';
import { toClipboard, appContext } from '@xcan-angus/infra';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { NodeData, NodeItemsProps } from '../types';

/**
 * Composable for node utility functions
 *
 * @param props - Component props containing admin and permission information
 * @returns Object containing utility functions and state
 */
export function useNodeUtils (props: NodeItemsProps) {
  const { t } = useI18n();

  // Application context information
  const tenantInfo = ref<any>(null);
  const userInfo = ref<any>(null);
  const isPrivate = ref(false);

  /**
   * Initializes application context information
   */
  const initializeAppContext = () => {
    try {
      tenantInfo.value = appContext.getTenant();
      userInfo.value = appContext.getUser();
      isPrivate.value = appContext.isPrivateEdition();
    } catch (error) {
      console.error('Failed to initialize app context:', error);
    }
  };

  /**
   * Copies content to clipboard and shows success notification
   *
   * @param text - Text content to copy
   */
  const copyToClipboard = async (text: string) => {
    try {
      await toClipboard(text);
      notification.success(t('actions.tips.copySuccess'));
    } catch (error) {
      console.error('Failed to copy to clipboard:', error);
    }
  };

  /**
   * Checks if current user can edit a specific node
   *
   * @param node - Node to check edit permissions for
   * @returns True if user can edit the node, false otherwise
   */
  const canEditNode = (node: NodeData): boolean => {
    if (!tenantInfo.value || !userInfo.value) return false;

    // Check if node belongs to current tenant
    if (node.tenantId !== tenantInfo.value.id) return false;

    // Check if user has admin privileges or created the node
    return props.isAdmin || node.createdBy === userInfo.value.id;
  };

  /**
   * Checks if current user can delete a specific node
   *
   * @param node - Node to check delete permissions for
   * @returns True if user can delete the node, false otherwise
   */
  const canDeleteNode = (node: NodeData): boolean => {
    if (!tenantInfo.value || !userInfo.value) return false;

    // Node must be disabled to be deleted
    if (node.enabled) return false;

    // Check if node belongs to current tenant
    if (node.tenantId !== tenantInfo.value.id) return false;

    // Check if user has admin privileges or created the node
    return props.isAdmin || node.createdBy === userInfo.value.id;
  };

  /**
   * Checks if current user can enable/disable a specific node
   *
   * @param node - Node to check enable/disable permissions for
   * @returns True if user can modify the node's enabled state, false otherwise
   */
  const canToggleNodeEnabled = (node: NodeData): boolean => {
    if (!tenantInfo.value || !userInfo.value) return false;

    // Check if node belongs to current tenant
    if (node.tenantId !== tenantInfo.value.id) return false;

    // Check if user has admin privileges or created the node
    return props.isAdmin || node.createdBy === userInfo.value.id;
  };

  /**
   * Checks if current user can restart agent on a specific node
   *
   * @param node - Node to check restart permissions for
   * @returns True if user can restart the agent, false otherwise
   */
  const canRestartAgent = (node: NodeData): boolean => {
    if (!tenantInfo.value || !userInfo.value) return false;

    // Check if node belongs to current tenant
    if (node.tenantId !== tenantInfo.value.id) return false;

    // Check if user has admin privileges or created the node
    return props.isAdmin || node.createdBy === userInfo.value.id;
  };

  /**
   * Checks if current user can install agent on a specific node
   *
   * @param node - Node to check install permissions for
   * @returns True if user can install the agent, false otherwise
   */
  const canInstallAgent = (node: NodeData): boolean => {
    if (!tenantInfo.value || !userInfo.value) return false;

    // Free nodes cannot have agents installed
    if (node.free) return false;

    // Check if user has admin privileges
    return props.isAdmin;
  };

  /**
   * Gets the appropriate tooltip text for a disabled button
   *
   * @param node - Node to get tooltip for
   * @param action - Action being performed (edit, delete, enable, etc.)
   * @returns Tooltip text or undefined if no tooltip needed
   */
  const getDisabledButtonTooltip = (node: NodeData, action: string): string | undefined => {
    switch (action) {
      case 'edit':
        if (node.source?.value !== 'OWN_NODE') {
          return t('node.nodeItem.tips.editOwnNode');
        }
        if (!props.isAdmin) {
          return t('node.nodeItem.tips.editPermission');
        }
        break;

      case 'delete':
        if (node.enabled) {
          return t('node.nodeItem.tips.disableDelete');
        }
        if (!props.isAdmin) {
          return t('node.nodeItem.tips.deletePermission');
        }
        if (node.source?.value !== 'OWN_NODE') {
          return t('node.nodeItem.tips.deleteOwnNode');
        }
        break;

      case 'install':
        if (node.geAgentInstallationCmd) {
          return t('node.nodeItem.tips.alreadyInstalled');
        }
        if (!props.isAdmin) {
          return t('node.nodeItem.tips.installPermission');
        }
        break;

      case 'manualInstall':
        if (!props.isAdmin) {
          return t('node.nodeItem.tips.manualInstallPermission');
        }
        break;
    }

    return undefined;
  };

  // Initialize app context on mount
  onMounted(() => {
    initializeAppContext();
  });

  return {
    // State
    tenantInfo,
    userInfo,
    isPrivate,

    // Methods
    copyToClipboard,
    canEditNode,
    canDeleteNode,
    canToggleNodeEnabled,
    canRestartAgent,
    canInstallAgent,
    getDisabledButtonTooltip
  };
}
