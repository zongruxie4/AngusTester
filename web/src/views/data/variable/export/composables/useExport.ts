import { ref } from 'vue';
import { download, routerUtils } from '@xcan-angus/infra';
import type { ExportFileFormat, ExportConfig } from '../types';

/**
 * <p>Composable for managing export functionality</p>
 * <p>Handles export state, file format selection, and download operations</p>
 *
 * @returns Object containing export state and methods
 */
export function useExport () {
  /**
   * <p>Export configuration state</p>
   * <p>Manages the current export settings and loading state</p>
   */
  const exportConfig = ref<ExportConfig>({
    fileType: 'JSON',
    loading: false
  });

  /**
   * <p>Reset export configuration to default values</p>
   * <p>Clears the current export settings and resets loading state</p>
   */
  const resetExportConfig = (): void => {
    exportConfig.value = {
      fileType: 'JSON',
      loading: false
    };
  };

  /**
   * <p>Update the selected file format for export</p>
   * <p>Changes the export format between JSON and YAML</p>
   *
   * @param format - The new file format to use for export
   */
  const updateFileFormat = (format: ExportFileFormat): void => {
    exportConfig.value.fileType = format;
  };

  /**
   * <p>Set the loading state for export operations</p>
   * <p>Controls the loading indicator during export processing</p>
   *
   * @param isLoading - Whether the export operation is currently loading
   */
  const setLoading = (isLoading: boolean): void => {
    exportConfig.value.loading = isLoading;
  };

  /**
   * <p>Generate export URL with proper parameters</p>
   * <p>Constructs the download URL with project ID, format, and optional variable IDs</p>
   *
   * @param projectId - The project identifier for the export
   * @param format - The file format for export
   * @param variableIds - Optional array of specific variable IDs to export
   * @returns The complete export URL
   */
  const generateExportUrl = (
    projectId: string,
    format: ExportFileFormat,
    variableIds?: string[]
  ): string => {
    let url = routerUtils.getTesterApiUrl(`/variable/export?projectId=${projectId}&format=${format}`);
    if (variableIds && variableIds.length > 0) {
      url += `&ids=${variableIds.join(',')}`;
    }
    return url;
  };

  /**
   * <p>Execute the export operation</p>
   * <p>Downloads the exported data in the selected format</p>
   *
   * @param projectId - The project identifier for the export
   * @param variableIds - Optional array of specific variable IDs to export
   */
  const executeExport = async (
    projectId: string,
    variableIds?: string[]
  ): Promise<void> => {
    try {
      setLoading(true);

      const exportUrl = generateExportUrl(
        projectId,
        exportConfig.value.fileType,
        variableIds
      );

      await download(exportUrl);

      // Reset configuration after successful export
      resetExportConfig();
    } catch (error) {
      console.error('Export operation failed:', error);
      // Reset loading state on error
      setLoading(false);
      throw error;
    }
  };

  return {
    // State
    exportConfig,

    // Methods
    resetExportConfig,
    updateFileFormat,
    setLoading,
    generateExportUrl,
    executeExport
  };
}
