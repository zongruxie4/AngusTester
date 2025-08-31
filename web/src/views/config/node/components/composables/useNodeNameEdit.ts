import { ref, nextTick } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { node } from '@/api/tester';

/**
 * Composable for managing node name editing operations
 *
 * @returns Object containing name editing functions and state
 */
export function useNodeNameEdit () {
  const { t } = useI18n();

  // References for name editing state
  const editNameInputRef = ref<any>();
  const editNameValue = ref<string>('');
  const editNameId = ref<string | undefined>();

  /**
   * Initiates inline editing for a node name
   *
   * @param name - Current node name
   * @param id - Node ID being edited
   */
  const editNodeName = (name: string, id: string) => {
    editNameId.value = id;
    editNameValue.value = name;

    // Focus the input field after DOM update
    nextTick(() => {
      editNameInputRef.value?.[0]?.focus();
    });
  };

  /**
   * Handles blur event on name input field
   *
   * <p>Validates the new name and saves changes to the backend.
   * Updates local state on successful save.</p>
   *
   * @param originalName - Original node name before editing
   * @param id - Node ID being edited
   */
  const handleNameBlur = async (originalName: string, id: string) => {
    const newName = editNameValue.value;

    // No changes or empty name - reset editing state
    if (newName === originalName || !newName.trim()) {
      editNameId.value = undefined;
      return;
    }

    try {
      // Save changes to backend
      const [error] = await node.updateNode([{ id, name: newName }]);

      if (error) {
        console.error('Failed to update node name:', error);
        return;
      }

      // Update local state
      editNameId.value = undefined;

      // Find and update the node in the list
      // Note: This assumes nodeList is accessible from parent component
      // The actual implementation may need to be adjusted based on how
      // the parent component manages the node list

      // Show success notification
      notification.success(t('node.nodeItem.labels.modifyNodeNameSuccess'));
    } catch (error) {
      console.error('Failed to update node name:', error);
    }
  };

  /**
   * Cancels the current name editing operation
   */
  const cancelNameEdit = () => {
    editNameId.value = undefined;
    editNameValue.value = '';
  };

  /**
   * Checks if a specific node is currently being edited
   *
   * @param nodeId - Node ID to check
   * @returns True if the node is being edited, false otherwise
   */
  const isEditingName = (nodeId: string): boolean => {
    return editNameId.value === nodeId;
  };

  return {
    // State
    editNameInputRef,
    editNameValue,
    editNameId,

    // Methods
    editNodeName,
    handleNameBlur,
    cancelNameEdit,
    isEditingName
  };
}
