import { reactive, ref } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { NodeData, NodeItemsEmits, LoadingStateMaps } from '../types';
import { node } from '@/api/tester';
import { nodeInfo } from '@/api/ctrl';

/**
 * Composable for managing node operations
 *
 * @param emits - Component emit functions
 * @returns Object containing node operation functions and state
 */
export function useNodeOperations (emits: NodeItemsEmits) {
  const { t } = useI18n();

  // Loading state maps for various operations
  const loadingStates: LoadingStateMaps = reactive({
    installingMap: {}, // Map of nodes currently installing agent
    enablingMap: {}, // Map of nodes currently enabling/disabling
    restartingMap: {} // Map of nodes currently restarting agent
  });

  // Connection test state
  const showTested = ref(false);
  const testSuccess = ref(false);
  const testFailContent = ref('');

  // Installation agent display state
  const showInstallCtrlAccessTokenMap = ref<{[key: string]: boolean}>({});

  /**
   * Gets tooltip text for online install button based on node state
   *
   * @param node - Node to get tooltip for
   * @returns Tooltip text or undefined if no tooltip needed
   */
  const getOnlineInstallTip = (node: NodeData): string | undefined => {
    if (node.geAgentInstallationCmd) {
      return t('node.nodeItem.tips.alreadyInstalled');
    }
    if (!node.free) {
      return t('node.nodeItem.tips.installPermission');
    }
    return undefined;
  };

  /**
   * Installs agent on a node
   *
   * @param state - Node to install agent on
   */
  const installAgent = async (state: NodeData) => {
    if (!state.id) {
      console.error('Cannot install agent: node ID is undefined');
      return;
    }

    if (loadingStates.installingMap[state.id]) {
      return;
    }

    loadingStates.installingMap[state.id] = true;

    try {
      const [error] = await node.installNodeAgent({ id: state.id });

      if (error) {
        // Handle offline installation steps if available
        if (typeof (error as any).data === 'object') {
          const errorData = (error as any).data;

          if (errorData.linuxOfflineInstallSteps) {
            state.linuxOfflineInstallSteps = errorData.linuxOfflineInstallSteps;
            state.showInstallAgent = true;
          }

          if (errorData.windowsOfflineInstallSteps) {
            state.windowsOfflineInstallSteps = errorData.windowsOfflineInstallSteps;
            state.showInstallAgent = true;
          }
        }
        return;
      }

      // Success - reload list and show notification
      emits('loadList');
      notification.success(t('node.nodeItem.labels.installSuccess'));
    } catch (error) {
      console.error('Failed to install agent:', error);
    } finally {
      loadingStates.installingMap[state.id] = false;
    }
  };

  /**
   * Toggles the display of manual installation agent panel
   *
   * @param state - Node to toggle panel for
   */
  const foldInstallAgent = (state: NodeData) => {
    state.showInstallAgent = !state.showInstallAgent;
  };

  /**
   * Shows manual installation steps for a node
   *
   * @param state - Node to show steps for
   */
  const showInstallStep = async (state: NodeData) => {
    if (!state.id) {
      console.error('Cannot show install steps: node ID is undefined');
      return;
    }

    if (state.showInstallAgent) {
      foldInstallAgent(state);
      return;
    }

    try {
      const [error, res] = await nodeInfo.geAgentInstallationCmd({ id: state.id });

      if (error) {
        return;
      }

      state.showInstallAgent = true;

      // Set installation steps and configuration
      if (res.data.linuxOfflineInstallSteps) {
        state.linuxOfflineInstallSteps = res.data.linuxOfflineInstallSteps;
      }

      if (res.data.windowsOfflineInstallSteps) {
        state.windowsOfflineInstallSteps = res.data.windowsOfflineInstallSteps;
      }

      state.installConfig = res.data?.installConfig || {};
    } catch (error) {
      console.error('Failed to get installation steps:', error);
    }
  };

  /**
   * Restarts agent on a node
   *
   * @param state - Node to restart agent on
   */
  const restartAgent = async (state: NodeData) => {
    if (!state.id) {
      console.error('Cannot restart agent: node ID is undefined');
      return;
    }

    loadingStates.restartingMap[state.id] = true;

    try {
      const [error] = await node.restartNodeAgent(state.id);

      if (error) {
        loadingStates.restartingMap[state.id] = false;
        return;
      }

      // Wait for restart to complete then reload
      const nodeId = state.id; // Capture the ID for the timeout callback
      setTimeout(() => {
        loadingStates.restartingMap[nodeId] = false;
        emits('loadList');
      }, 16000);
    } catch (error) {
      console.error('Failed to restart agent:', error);
      loadingStates.restartingMap[state.id] = false;
    }
  };

  /**
   * Enables or disables a node
   *
   * @param state - Node to enable/disable
   */
  const toggleNodeEnabled = async (state: NodeData) => {
    if (!state.id) {
      console.error('Cannot toggle node enabled state: node ID is undefined');
      return;
    }

    loadingStates.enablingMap[state.id] = true;

    try {
      const [error] = await node.enableNode([{
        enabled: !state.enabled,
        id: state.id
      }]);

      if (error) {
        return;
      }

      emits('loadList');
    } catch (error) {
      console.error('Failed to toggle node enabled state:', error);
    } finally {
      loadingStates.enablingMap[state.id] = false;
    }
  };

  /**
   * Tests connection to a node
   *
   * @param nodeParams - Node connection parameters
   */
  const testConnection = async (nodeParams: any) => {
    const { ip, password, sshPort, username, publicIp } = nodeParams;

    try {
      const [error] = await node.testNodeConnection({
        ip: publicIp || ip,
        password,
        sshPort: Number(sshPort),
        username
      });

      showTested.value = true;

      if (error) {
        testSuccess.value = false;
        testFailContent.value = error.message;
        return;
      }

      testSuccess.value = true;
    } catch (error) {
      console.error('Failed to test connection:', error);
      showTested.value = true;
      testSuccess.value = false;
      testFailContent.value = 'Connection test failed';
    }
  };

  /**
   * Toggles visibility of control access token
   *
   * @param id - Node ID to toggle token visibility for
   */
  const toggleShowCtrlAccessToken = (id: string) => {
    showInstallCtrlAccessTokenMap.value[id] = !showInstallCtrlAccessTokenMap.value[id];
  };

  /**
   * Resets connection test state
   */
  const resetConnectionTest = () => {
    showTested.value = false;
    testSuccess.value = false;
    testFailContent.value = '';
  };

  return {
    // State
    loadingStates,
    showTested,
    testSuccess,
    testFailContent,
    showInstallCtrlAccessTokenMap,

    // Methods
    getOnlineInstallTip,
    installAgent,
    foldInstallAgent,
    showInstallStep,
    restartAgent,
    toggleNodeEnabled,
    testConnection,
    toggleShowCtrlAccessToken,
    resetConnectionTest
  };
}
