import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { modules } from '@/api/tester';
import { modal, notification } from '@xcan-angus/vue-ui';
import type {
  ModuleItem,
  CreateModuleParams,
  UpdateModuleParams,
  DeleteModuleParams
} from '../types';

/**
 * Composable for managing module actions and operations
 * Handles CRUD operations, movement, and modal states
 *
 * @param onDataChange - Callback function to refresh data after operations
 * @returns Object containing action methods and reactive state
 */
export function useActions (onDataChange: () => Promise<void>) {
  const { t } = useI18n();

  // Modal and editing state management
  const editId = ref<number>();
  const modalVisible = ref(false);
  const moveVisible = ref(false);
  const activeModule = ref<ModuleItem>();
  const currentParentId = ref<number>();


  /**
   * Creates a new module with the specified parameters
   * Shows success notification and refreshes data on completion
   *
   * @param params - Parameters for creating the module
   * @returns Promise resolving to the created module ID or undefined on error
   */
  const createModule = async (params: CreateModuleParams): Promise<string | undefined> => {
    try {
      const [error, response] = await modules.addModule(params);

      if (error) {
        console.error('Failed to create module:', error);
        return undefined;
      }

      // Extract the created module ID from response
      const createdId = response?.data?.[0]?.id;

      // Show success notification
      notification.success(t('actions.tips.addSuccess'));

      // Refresh the data to show the new module
      await onDataChange();

      return createdId;
    } catch (error) {
      console.error('Unexpected error during module creation:', error);
      return undefined;
    }
  };

  /**
   * Updates an existing module with new information
   * Supports updating name, parent, and sequence
   *
   * @param moduleData - Array of module updates to apply
   * @returns Promise resolving to success status
   */
  const updateModule = async (moduleData: UpdateModuleParams[]): Promise<boolean> => {
    try {
      const [error] = await modules.updateModule(moduleData);

      if (error) {
        console.error('Failed to update module:', error);
        return false;
      }

      // Show success notification
      notification.success(t('actions.tips.modifySuccess'));

      // Refresh the data to show changes
      await onDataChange();

      return true;
    } catch (error) {
      console.error('Unexpected error during module update:', error);
      return false;
    }
  };

  /**
   * Deletes a module with confirmation dialog
   * Shows confirmation before deletion and handles the delete operation
   *
   * @param moduleItem - The module to delete
   */
  const deleteModule = (moduleItem: ModuleItem): void => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: moduleItem.name }),
      async onOk () {
        try {
          const params: DeleteModuleParams = {
            ids: [moduleItem.id]
          };

          const [error] = await modules.deleteModule(params);

          if (error) {
            console.error('Failed to delete module:', error);
            return;
          }

          // Show success notification
          notification.success(t('actions.tips.deleteSuccess'));

          // Refresh the data to reflect deletion
          await onDataChange();
        } catch (error) {
          console.error('Unexpected error during module deletion:', error);
        }
      }
    });
  };

  /**
   * Moves a module up in the hierarchy or sequence
   * Handles both level changes and position changes within the same level
   *
   * @param record - Module record with position information
   */
  const moveModuleUp = async (record: ModuleItem & {
    index: number;
    ids: string[];
  }, dataList: ModuleItem[]): Promise<void> => {
    const { index, id, ids, pid } = record;
    
    let updateParams: UpdateModuleParams;

    if (index === 0) {
      let targetSequece: number = 0;
      let targetParentId: string = '-1';

      ids.reduce((pre: ModuleItem[], cur: string): ModuleItem[] => {
        if (cur === String(pid)) {
          const curIndex = pre.findIndex(f => String(f.id) === cur);
          targetSequece = Number(pre[curIndex]?.sequence ?? 0) + 1;
          targetParentId = String(pre[curIndex]?.pid ?? '-1');
          return [];
        } else {
          return pre.find(f => String(f.id) === cur)?.children || [];
        }
      }, dataList);

      // Minimal safe fallback: keep parent unchanged, reset sequence to 0
      updateParams = {
        id,
        pid: targetParentId,
        sequence: targetSequece
      };
    } else {
      let targetSequece: number = 0;

      ids.reduce((pre: ModuleItem[], cur: string): ModuleItem[] => {
        if (cur === String(id)) {
          const curIndex = pre.findIndex(f => String(f.id) === cur);
          targetSequece = Number(pre[curIndex - 1]?.sequence ?? 0) - 1;
          return [];
        } else {
          return pre.find(f => String(f.id) === cur)?.children || [];
        }
      }, dataList);
      updateParams = {
        id,
        sequence: targetSequece
      };
    }

    const success = await updateModule([updateParams]);
    if (success) {
      // success notification handled inside updateModule
    }
  };

  /**
   * Moves a module down in the sequence
   * Updates the module's sequence number to move it down in the order
   *
   * @param record - Module record with position information
   */
  const moveModuleDown = async (record: ModuleItem & {
    index: number;
    ids: string[];
  }, dataList: ModuleItem[]): Promise<void> => {

    const { id, ids } = record;
    let targetSequece: number = 0;

    ids.reduce((pre: ModuleItem[], cur: string): ModuleItem[] => {
      if (cur === String(id)) {
        const curIndex = pre.findIndex(f => String(f.id) === cur);
        targetSequece = Number(pre[curIndex + 1]?.sequence ?? 0) + 1;
        return [];
      } else {
        return pre.find(f => String(f.id) === cur)?.children || [];
      }
    }, dataList);

    const updateParams: UpdateModuleParams = {
      id: record.id,
      sequence: targetSequece
    };

    const success = await updateModule([updateParams]);
    if (success) {
      // success notification handled inside updateModule
    }
  };

  /**
   * Initiates module movement to a different parent
   * Opens the move modal for selecting new parent
   *
   * @param module - The module to move
   */
  const initiateModuleMove = (module: ModuleItem): void => {
    activeModule.value = module;
    moveVisible.value = true;
  };

  /**
   * Executes the module move to a new parent
   * Updates the module's parent ID
   *
   * @param moduleId - ID of module to move
   * @param newParentId - ID of new parent module
   */
  const executeModuleMove = async (moduleId: number, newParentId: number): Promise<boolean> => {
    const updateParams: UpdateModuleParams = {
      id: moduleId,
      pid: newParentId
    };

    return await updateModule([updateParams]);
  };

  /**
   * Starts editing mode for a module
   * Sets the edit ID to enable inline editing
   *
   * @param module - The module to edit
   */
  const startEdit = (module: ModuleItem): void => {
    editId.value = module.id;
  };

  /**
   * Cancels the current edit operation
   * Clears the edit ID to exit inline editing mode
   */
  const cancelEdit = (): void => {
    editId.value = undefined;
  };

  /**
   * Saves the edited module name
   * Updates the module with the new name
   *
   * @param moduleId - ID of the module being edited
   * @param newName - New name for the module
   */
  const saveEdit = async (moduleId: number, newName: string): Promise<void> => {
    if (!newName.trim()) {
      return;
    }

    const success = await updateModule([{
      id: moduleId,
      name: newName.trim()
    }]);

    if (success) {
      editId.value = undefined;
    }
  };

  /**
   * Opens the create module modal
   * Sets up the modal for creating a new module
   *
   * @param parentId - Optional parent ID for the new module
   */
  const openCreateModal = (parentId?: number): void => {
    currentParentId.value = parentId;
    modalVisible.value = true;
  };

  /**
   * Closes all modals and resets state
   * Clears modal visibility and active module state
   */
  const closeModals = (): void => {
    modalVisible.value = false;
    moveVisible.value = false;
    activeModule.value = undefined;
    currentParentId.value = undefined;
  };

  /**
   * Handles menu item clicks for module actions
   * Dispatches to appropriate action based on menu key
   *
   * @param menuKey - The action key from the dropdown menu
   * @param module - The module to perform the action on
   */
  const handleMenuAction = (menuKey: string, module: ModuleItem & {
    index: number;
    ids: string[];
  }, dataList: ModuleItem[]): void => {
    switch (menuKey) {
      case 'edit':
        startEdit(module);
        break;
      case 'add':
        openCreateModal(module.id);
        break;
      case 'del':
        deleteModule(module);
        break;
      case 'up':
        moveModuleUp(module, dataList);
        break;
      case 'down':
        moveModuleDown(module, dataList);
        break;
      case 'move':
        initiateModuleMove(module);
        break;
      default:
        console.warn('Unknown menu action:', menuKey);
    }
  };

  return {
    // Reactive state
    editId,
    modalVisible,
    moveVisible,
    activeModule,
    currentParentId,

    // Action methods
    createModule,
    updateModule,
    deleteModule,
    moveModuleUp,
    moveModuleDown,
    initiateModuleMove,
    executeModuleMove,
    startEdit,
    cancelEdit,
    saveEdit,
    openCreateModal,
    closeModals,
    handleMenuAction
  };
}
