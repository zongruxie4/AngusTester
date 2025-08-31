/**
 * Node data management composable
 * <p>
 * This composable handles all node basic information operations including
 * loading node details, managing node state, and handling installation steps.
 * </p>
 */

import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { node } from '@/api/tester';
import { nodeInfo } from '@/api/ctrl';
import { ActiveKey, InstallConfig, InstallSteps, NodeInfo } from '../types';
import { i18n } from '@xcan-angus/infra';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string): string => value);

// Grid column configuration for node information display
export const infoItem = [
  [{
    label: '名称',
    dataIndex: 'name'
  },
  {
    label: '用户名',
    dataIndex: 'username'
  },
  {
    label: '来源',
    dataIndex: 'sourceName'
  },
  {
    label: '域名',
    dataIndex: 'domain'
  }],
  [{
    label: '启用状态',
    dataIndex: 'enabled'
  },
  {
    label: '内网IP地址',
    dataIndex: 'ip'
  },
  {
    label: '密码',
    dataIndex: 'password'
  },
  {
    label: '角色',
    dataIndex: 'roles'
  }
  ],
  [{
    label: '代理状态',
    dataIndex: 'installAgent'
  },
  {
    label: '公网IP地址',
    dataIndex: 'publicIp'
  },
  {
    label: '规格',
    dataIndex: 'spec'
  },
  {
    label: '端口',
    dataIndex: 'sshPort'
  }
  ],
  [{
    label: '连接状态',
    dataIndex: 'online'
  },
  {
    label: '添加人',
    dataIndex: 'createdByName'
  },
  {
    label: '添加时间',
    dataIndex: 'createdDate'
  },
  {
    label: '到期时间',
    dataIndex: 'instanceExpiredDate'
  }]
];

// Installation configuration columns for display
export const installConfigColumns = [
  [
    {
      dataIndex: 'tenantId',
      label: t('node.nodeItem.interface.installConfigColumns.tenantId')
    },
    {
      dataIndex: 'deviceId',
      label: t('node.nodeItem.interface.installConfigColumns.deviceId')
    },
    {
      dataIndex: 'serverCtrlUrlPrefix',
      label: t('node.nodeItem.interface.installConfigColumns.serverCtrlUrlPrefix')
    },
    {
      dataIndex: 'ctrlAccessToken',
      label: t('node.nodeItem.interface.installConfigColumns.ctrlAccessToken')
    }
  ]
];

/**
 * Composable for managing node data and operations
 * <p>
 * Provides reactive state management for node information, installation steps,
 * and various node operations like enable/disable, install agent, and delete.
 * </p>
 */
export function useNodeData () {
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  // Reactive state for node information
  const state = reactive<{
    infos: Partial<NodeInfo>;
    linuxOfflineInstallSteps: Partial<InstallSteps>;
    windowsOfflineInstallSteps: Partial<InstallSteps>;
    installConfig?: InstallConfig;
  }>({
    infos: {},
    linuxOfflineInstallSteps: {},
    windowsOfflineInstallSteps: {},
    installConfig: undefined
  });

  // UI state flags
  const loadingInfo = ref(true);
  const showPassword = ref(false);
  const showInstallStep = ref(false);
  const showInstallCtrlAccessToken = ref(false);
  const installing = ref(false);
  const enabled = ref(false);
  const activeKey = ref<ActiveKey>('source');

  /**
   * Load node basic information from API
   * <p>
   * Fetches node details and processes role information for display.
   * Updates the reactive state with fetched data.
   * </p>
   */
  const loadInfo = async (nodeId: string) => {
    loadingInfo.value = true;
    try {
      const [error, res] = await node.getNodeDetail(nodeId);
      if (error) {
        return;
      }

      // Process role information
      const rolesName = res.data.roles.map((i: any) => i.message).join(',');
      const rolesValues = res.data.roles.map((i: any) => i.value);

      state.infos = {
        ...state.infos,
        ...res.data,
        sourceName: res.data.source.message,
        rolesName,
        rolesValues
      };
    } finally {
      loadingInfo.value = false;
    }
  };

  /**
   * Toggle password visibility
   * <p>
   * Switches between showing and hiding the password field.
   * </p>
   */
  const handleShowPassword = () => {
    showPassword.value = !showPassword.value;
  };

  /**
   * Install node agent
   * <p>
   * Attempts to install the agent on the node. If offline installation is required,
   * shows the manual installation steps.
   * </p>
   */
  const installAgent = async (nodeId: string) => {
    installing.value = true;
    try {
      const [error] = await node.installNodeAgent({ id: nodeId });
      if (error) {
        // Handle offline installation steps
        if (error && typeof error === 'object' && 'data' in error && error.data && typeof error.data === 'object' && 'linuxOfflineInstallSteps' in error.data) {
          state.linuxOfflineInstallSteps = error.data.linuxOfflineInstallSteps as Record<string, any>;
          showInstallStep.value = true;
        }
        if (error && typeof error === 'object' && 'data' in error && error.data && typeof error.data === 'object' && 'windowsOfflineInstallSteps' in error.data) {
          state.windowsOfflineInstallSteps = error.data.windowsOfflineInstallSteps as Record<string, any>;
          showInstallStep.value = true;
        }
        return;
      }

      state.infos.geAgentInstallationCmd = true;
    } finally {
      installing.value = false;
    }
  };

  /**
   * Get manual installation steps
   * <p>
   * Fetches manual installation instructions for offline agent installation.
   * </p>
   */
  const getInstallStep = async (nodeId: string) => {
    if (showInstallStep.value) {
      foldInstallAgent();
      return;
    }

    try {
      const [error, res] = await nodeInfo.geAgentInstallationCmd({ id: nodeId });
      if (error) {
        return;
      }

      showInstallStep.value = true;
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
   * Hide installation steps panel
   * <p>
   * Closes the manual installation steps display.
   * </p>
   */
  const foldInstallAgent = () => {
    showInstallStep.value = false;
  };

  /**
   * Navigate back to node list
   * <p>
   * Returns to the node configuration page.
   * </p>
   */
  const turnback = () => {
    router.push({ path: '/config#node' });
  };

  /**
   * Toggle node enabled/disabled state
   * <p>
   * Enables or disables the node based on current state.
   * </p>
   */
  const enableNode = async (nodeId: string) => {
    enabled.value = true;
    try {
      const [error] = await node.enableNode([{
        enabled: !state.infos.enabled,
        id: nodeId
      }]);
      if (error) {
        return;
      }
      state.infos.enabled = !state.infos.enabled;
    } finally {
      enabled.value = false;
    }
  };

  /**
   * Delete node with confirmation
   * <p>
   * Shows confirmation dialog and deletes the node if confirmed.
   * </p>
   */
  const deleteNode = (nodeId: string, nodeName: string) => {
    modal.confirm({
      content: `确定删除节点【${nodeName}】吗?`,
      onOk: async () => {
        try {
          const [error] = await node.deleteNode({ ids: [nodeId] });
          if (error) {
            return;
          }
          turnback();
        } catch (error) {
          console.error('Failed to delete node:', error);
        }
      }
    });
  };

  /**
   * Toggle control access token visibility
   * <p>
   * Switches between showing and hiding sensitive installation configuration.
   * </p>
   */
  const toggleShowCtrlAccessToken = () => {
    showInstallCtrlAccessToken.value = !showInstallCtrlAccessToken.value;
  };

  /**
   * Get online installation tip based on node state
   * <p>
   * Returns appropriate tip message for agent installation based on current status.
   * </p>
   */
  const getOnlineInstallTip = (node: any, isAdmin: boolean) => {
    if (node.infos.geAgentInstallationCmd) {
      return t('node.nodeDetail.labels.installed');
    }
    if (!isAdmin) {
      return t('node.nodeDetail.labels.installAgentTip');
    }
    return '';
  };

  /**
   * Get delete button tooltip based on node state
   * <p>
   * Returns appropriate tooltip message for delete button based on permissions and status.
   * </p>
   */
  const getDeleteTip = (node: any, isAdmin: boolean, tenantInfo: any, userInfo: any) => {
    if (node.enabled) {
      return t('node.nodeDetail.labels.disableDelete');
    }
    if (!isAdmin) {
      return t('node.nodeDetail.labels.deleteNodeTip');
    }
    if (node.source?.value !== 'OWN_NODE') {
      return t('node.nodeDetail.labels.onlyOwnNode');
    }
    return '';
  };

  return {
    // State
    state,
    loadingInfo,
    showPassword,
    showInstallStep,
    showInstallCtrlAccessToken,
    installing,
    enabled,
    activeKey,

    // Methods
    loadInfo,
    handleShowPassword,
    installAgent,
    getInstallStep,
    foldInstallAgent,
    turnback,
    enableNode,
    deleteNode,
    toggleShowCtrlAccessToken,
    getOnlineInstallTip,
    getDeleteTip
  };
}
