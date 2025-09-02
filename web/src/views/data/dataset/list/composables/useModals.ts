import { ref, Ref } from 'vue';
import { DataSetItem } from '../../types';

/**
 * Modal management composable
 * Handles visibility state of various modals in the dataset list view
 */
export function useModals () {
  // Preview modal state
  const previewDataSetModalVisible = ref(false);

  // Import modal state
  const importDataSetModalVisible = ref(false);

  // Export modal state
  const exportDataSetModalVisible = ref(false);

  // Selected dataset for preview
  const selectedData = ref<Pick<DataSetItem, 'id'>>();

  // Dataset ID for export
  const exportDataSetId = ref<string>();

  /**
   * Open preview modal for a dataset
   * Sets the selected dataset and shows the preview modal
   */
  const openPreviewModal = (data: Pick<DataSetItem, 'id'>) => {
    selectedData.value = { id: data.id };
    previewDataSetModalVisible.value = true;
  };

  /**
   * Open import modal
   * Shows the import modal
   */
  const openImportModal = () => {
    importDataSetModalVisible.value = true;
  };

  /**
   * Open export modal
   * Sets the export dataset ID and shows the export modal
   */
  const openExportModal = (id?: string) => {
    exportDataSetModalVisible.value = true;
    exportDataSetId.value = id;
  };

  /**
   * Handle import success
   * Resets pagination to first page and reloads data
   */
  const handleImportSuccess = (pagination: Ref<{ current: number; pageSize: number; total: number; }>, loadData: () => Promise<void>) => {
    pagination.value.current = 1;
    loadData();
  };

  /**
   * Close all modals
   * Hides all modals and clears associated data
   */
  const closeAllModals = () => {
    previewDataSetModalVisible.value = false;
    importDataSetModalVisible.value = false;
    exportDataSetModalVisible.value = false;
    selectedData.value = undefined;
    exportDataSetId.value = undefined;
  };

  return {
    // Reactive states
    previewDataSetModalVisible,
    importDataSetModalVisible,
    exportDataSetModalVisible,
    selectedData,
    exportDataSetId,

    // Methods
    openPreviewModal,
    openImportModal,
    openExportModal,
    handleImportSuccess,
    closeAllModals
  };
}
