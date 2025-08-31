import { computed, reactive, ref, watch } from 'vue';
import { notification, modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import {
  NodeData,
  NodeFormParams,
  NodeItemsProps,
  NodeItemsEmits
} from '../types';
import { getDefaultNode } from '../../constant';
import { node } from '@/api/tester';

/**
 * Composable for managing node data operations
 *
 * @param props - Component props containing node list and configuration
 * @param emits - Component emit functions
 * @returns Object containing node data management functions and state
 */
export function useNodeData (
  props: NodeItemsProps,
  emits: NodeItemsEmits
) {
  const { t } = useI18n();

  // Reactive node list state
  const nodeList = ref<NodeData[]>([]);

  // Form parameters for adding/editing nodes
  const nodeParams = reactive<NodeFormParams>({
    name: '',
    ip: '',
    publicIp: '',
    domain: '',
    username: '',
    password: '',
    sshPort: '',
    roles: [],
    id: ''
  });

  // Form validation state
  const validated = ref(false);

  // Computed properties for form validation
  const showPortTip = computed(() => {
    const sshPort = Number(nodeParams.sshPort);
    return validated.value && !!nodeParams.sshPort && !(sshPort >= 1 && sshPort <= 65535);
  });

  const testBtnDisable = computed(() => {
    return !nodeParams.ip && !nodeParams.publicIp;
  });

  /**
   * Validates the current form data
   *
   * @returns True if form is valid, false otherwise
   */
  const validate = (): boolean => {
    const sshPort = Number(nodeParams.sshPort);
    return !!(
      nodeParams.name?.trim() &&
      nodeParams.ip?.trim() &&
      (!nodeParams.sshPort?.trim() || (sshPort >= 1 && sshPort <= 65535)) &&
      nodeParams.roles.length > 0
    );
  };

  /**
   * Adds a new editable node to the list
   *
   * <p>Creates a new default node and sets it to editable mode.
   * Ensures only one node can be edited at a time.</p>
   */
  const addNode = () => {
    // Prevent adding if first node is already being edited
    if (nodeList.value[0]?.editable && !nodeList.value[0]?.id) {
      return;
    }

    const newNode = getDefaultNode();
    nodeList.value.unshift(newNode);
    changeEditable(newNode);
  };

  /**
   * Cancels the current editing operation
   *
   * @param state - Node state to cancel editing for
   */
  const cancelEdit = (state: NodeData) => {
    if (state?.id) {
      // Cancel editing existing node
      state.editable = false;
    } else {
      // Remove newly added node
      nodeList.value.shift();
      emits('cancel');
    }

    // Reset validation state
    validated.value = false;
  };

  /**
   * Changes the editable state of a node
   *
   * @param state - Node to change editable state for
   */
  const changeEditable = (state: NodeData) => {
    // Cancel any other currently editing nodes
    if (nodeList.value.some(i => i.editable)) {
      nodeList.value.forEach(node => {
        if (node.editable) {
          cancelEdit(node);
        }
      });
    }

    // Toggle editable state and populate form
    state.editable = !state.editable;

    if (state.editable) {
      // Populate form with node data
      Object.assign(nodeParams, {
        domain: state.domain,
        ip: state.ip,
        publicIp: state.publicIp,
        id: state.id,
        name: state.name,
        username: state.username,
        password: state.password,
        sshPort: state.sshPort,
        roles: state.roles.map(i => i.value)
      });
    }
  };

  /**
   * Saves the current node data
   *
   * <p>Validates form data and either creates a new node or updates existing one.
   * Shows success notification and reloads node list on completion.</p>
   */
  const saveNode = async () => {
    validated.value = true;

    if (!validate()) {
      return;
    }

    validated.value = false;
    try {
      const response = await (!nodeParams.id
        ? node.addNode([{ ...nodeParams }])
        : node.updateNode([{ ...nodeParams }]));

      const [error] = response;
      if (error) {
        return;
      }

      // Show success message
      const messageKey = nodeParams.id
        ? 'node.nodeItem.labels.updateNodeSuccess'
        : 'node.nodeItem.labels.addNodeSuccess';
      notification.success(t(messageKey));

      // Reload node list
      emits('loadList');
    } catch (error) {
      console.error('Failed to save node:', error);
    }
  };

  /**
   * Deletes a node after confirmation
   *
   * @param state - Node to delete
   */
  const deleteNode = (state: NodeData) => {
    // Show confirmation dialog
    modal.confirm({
      content: t('node.nodeItem.confirm.deleteNode', { name: state.name }),
      onOk: async () => {
        try {
          const [error] = await node.deleteNode({ ids: state.id });
          if (error) {
            return;
          }
          emits('loadList');
        } catch (error) {
          console.error('Failed to delete node:', error);
        }
      }
    });
  };

  /**
   * Gets tooltip text for edit button based on node state
   *
   * @param node - Node to get tooltip for
   * @returns Tooltip text or undefined if no tooltip needed
   */
  const getEditTip = (node: NodeData): string | undefined => {
    if (node.source?.value !== 'OWN_NODE') {
      return t('node.nodeItem.tips.editOwnNode');
    }
    if (!props.isAdmin) {
      return t('node.nodeItem.tips.editPermission');
    }
    return undefined;
  };

  /**
   * Gets tooltip text for delete button based on node state
   *
   * @param node - Node to get tooltip for
   * @returns Tooltip text or undefined if no tooltip needed
   */
  const getDeleteTip = (node: NodeData): string | undefined => {
    if (node.enabled) {
      return t('node.nodeItem.tips.disableDelete');
    }
    if (!props.isAdmin) {
      return t('node.nodeItem.tips.deletePermission');
    }
    if (node.source?.value !== 'OWN_NODE') {
      return t('node.nodeItem.tips.deleteOwnNode');
    }
    return undefined;
  };

  // Watch for changes in props.nodeList and update local state
  watch(() => props.nodeList, (newValue) => {
    if (newValue) {
      nodeList.value = JSON.parse(JSON.stringify(newValue));
    } else {
      nodeList.value = [];
    }
  }, {
    deep: true,
    immediate: true
  });

  return {
    // State
    nodeList,
    nodeParams,
    validated,

    // Computed
    showPortTip,
    testBtnDisable,

    // Methods
    validate,
    addNode,
    cancelEdit,
    changeEditable,
    saveNode,
    deleteNode,
    getEditTip,
    getDeleteTip
  };
}
