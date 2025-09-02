import { ref, computed } from 'vue';
import { download, TESTER } from '@xcan-angus/infra';
import type { ExportFileFormat, ExportConfig } from '../types';

/**
 * Composable for managing dataset export functionality
 * <p>
 * Handles file format selection, export URL generation, and download operations
 * </p>
 */
export function useExport (projectId: string, datasetIds: string[]) {
  /**
   * Reactive export configuration state
   * <p>
   * Manages the current export settings and loading state
   * </p>
   */
  const exportConfig = ref<ExportConfig>({
    fileType: 'JSON',
    loading: false
  });

  /**
   * Computed property for the selected file format
   * <p>
   * Provides reactive access to the current file format selection
   * </p>
   */
  const selectedFileType = computed(() => exportConfig.value.fileType);

  /**
   * Computed property for the loading state
   * <p>
   * Indicates whether an export operation is currently in progress
   * </p>
   */
  const isLoading = computed(() => exportConfig.value.loading);

  /**
   * Updates the selected file format for export
   * <p>
   * @param format - The new file format to set
   * </p>
   */
  const updateFileType = (format: ExportFileFormat): void => {
    exportConfig.value.fileType = format;
  };

  /**
   * Generates the export URL based on current configuration
   * <p>
   * Constructs the complete URL for the dataset export API endpoint
   * </p>
   * @returns The complete export URL with query parameters
   */
  const generateExportUrl = (): string => {
    let url = `${TESTER}/dataset/export?projectId=${projectId}&format=${exportConfig.value.fileType}`;

    if (datasetIds && datasetIds.length > 0) {
      url += `&ids=${datasetIds.join(',')}`;
    }

    return url;
  };

  /**
   * Executes the export operation
   * <p>
   * Initiates the download process for the selected dataset(s) in the chosen format
   * </p>
   */
  const executeExport = async (): Promise<void> => {
    try {
      exportConfig.value.loading = true;
      const exportUrl = generateExportUrl();
      await download(exportUrl);
    } catch (error) {
      console.error('Export operation failed:', error);
      throw error;
    } finally {
      exportConfig.value.loading = false;
    }
  };

  /**
   * Resets the export configuration to default values
   * <p>
   * Clears the current export settings and resets the file type to JSON
   * </p>
   */
  const resetExportConfig = (): void => {
    exportConfig.value = {
      fileType: 'JSON',
      loading: false
    };
  };

  return {
    // State
    exportConfig,
    selectedFileType,
    isLoading,

    // Actions
    updateFileType,
    executeExport,
    resetExportConfig,
    generateExportUrl
  };
}
