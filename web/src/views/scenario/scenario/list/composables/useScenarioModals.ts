import { ref } from 'vue';

/**
 * Composable for managing scenario modals and UI state
 */
export function useScenarioModals () {
  // Modal visibility states
  const toAuthVisible = ref(false);
  const createTestTaskVisible = ref(false);
  const deleteTaskVisible = ref(false);
  const exportVisible = ref(false);
  const execTestVisible = ref(false);
  const reopenTestTaskVisible = ref(false);
  const restartTestTaskVisible = ref(false);

  // Selected IDs for modals
  const selectedId = ref<string>();
  const selectedScriptId = ref<string>();
  const auth = ref(false);

  // Modal content
  const reopenContent = ref('');
  const restartContent = ref('');

  /**
   * Open authorization modal
   */
  const openAuthModal = (id: string, authFlag: boolean) => {
    selectedId.value = id;
    toAuthVisible.value = true;
    auth.value = authFlag;
  };

  /**
   * Open test execution modal
   */
  const openExecTestModal = (id: string, scriptId: string) => {
    selectedId.value = id;
    selectedScriptId.value = scriptId;
    execTestVisible.value = true;
  };

  /**
   * Open export modal
   */
  const openExportModal = (scriptId: string) => {
    selectedId.value = scriptId;
    exportVisible.value = true;
  };

  /**
   * Open create test task modal
   */
  const openCreateTestTaskModal = (id: string) => {
    selectedId.value = id;
    createTestTaskVisible.value = true;
  };

  /**
   * Open delete test task modal
   */
  const openDeleteTestTaskModal = (id: string) => {
    selectedId.value = id;
    deleteTaskVisible.value = true;
  };

  /**
   * Open reopen test task modal
   */
  const openReopenTestTaskModal = (id: string, content: string) => {
    selectedId.value = id;
    reopenTestTaskVisible.value = true;
    reopenContent.value = content;
  };

  /**
   * Open restart test task modal
   */
  const openRestartTestTaskModal = (id: string, content: string) => {
    selectedId.value = id;
    restartTestTaskVisible.value = true;
    restartContent.value = content;
  };

  /**
   * Close all modals
   */
  const closeAllModals = () => {
    toAuthVisible.value = false;
    createTestTaskVisible.value = false;
    deleteTaskVisible.value = false;
    exportVisible.value = false;
    execTestVisible.value = false;
    reopenTestTaskVisible.value = false;
    restartTestTaskVisible.value = false;

    selectedId.value = undefined;
    selectedScriptId.value = undefined;
    auth.value = false;
    reopenContent.value = '';
    restartContent.value = '';
  };

  return {
    // State
    toAuthVisible,
    createTestTaskVisible,
    deleteTaskVisible,
    exportVisible,
    execTestVisible,
    reopenTestTaskVisible,
    restartTestTaskVisible,
    selectedId,
    selectedScriptId,
    auth,
    reopenContent,
    restartContent,

    // Methods
    openAuthModal,
    openExecTestModal,
    openExportModal,
    openCreateTestTaskModal,
    openDeleteTestTaskModal,
    openReopenTestTaskModal,
    openRestartTestTaskModal,
    closeAllModals
  };
}
