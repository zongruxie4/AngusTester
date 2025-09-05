/**
 * Composable for managing export modal functionality
 * Handles export format selection, API selection, and export operations
 */
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { TESTER, download, routerUtils } from '@xcan-angus/infra';

import { ApiSelectionData, ExportFormatOption, ScrollProps, TreeProps } from '@/views/apis/mock/types';

/**
 * Composable for managing export modal functionality
 * @param projectId - Current project ID
 * @param mockService - Mock service object (if editing existing service)
 * @returns Export modal state and methods
 */
export function useExportModal (projectId: string, mockService: any) {
  const { t } = useI18n();

  // Export format state
  const format = ref<'json' | 'yaml'>('json');

  // Selected API IDs
  const apiIds = ref<string[] | null>([]);

  // Loading state during export
  const loading = ref(false);

  // Mock service ID
  const mockServiceId = ref<string | undefined>();

  /**
   * Handle API selection change
   * @param data - API selection data
   */
  const handleApiSelectionChange = (data: ApiSelectionData) => {
    mockServiceId.value = data.projectId;
    apiIds.value = data.checkedAll ? null : data.apiIds;
  };

  /**
   * Tree properties for service selection
   */
  const treeProps = ref<TreeProps>({
    action: `${TESTER}/mock/service?fullTextSearch=true`,
    disabled: !!mockService,
    params: {
      admin: true,
      projectId: projectId
    },
    defaultValue: mockService
      ? {
          name: mockService.name,
          id: mockService.id
        }
      : undefined
  });

  /**
   * Scroll properties for API list
   */
  const scrollProps = ref<ScrollProps>({
    action: `${TESTER}/mock/apis?mockServiceId=${mockService ? mockService.id : mockServiceId.value}`,
    params: undefined
  });

  /**
   * Export format options
   */
  const formatTypes = ref<ExportFormatOption[]>([
    { label: 'JSON', value: 'json' },
    { label: 'YAML', value: 'yaml' }
  ]);

  /**
   * Handle export operation
   * @param isVisible - Whether the modal is visible
   */
  const handleExport = (isVisible: boolean) => {
    // Reset state when modal is opened
    if (isVisible) {
      format.value = 'json';
      apiIds.value = [];
      mockServiceId.value = undefined;

      // Update tree props based on mock service
      if (mockService) {
        treeProps.value = {
          action: `${TESTER}/mock/service?fullTextSearch=true`,
          disabled: !!mockService,
          params: {
            admin: true,
            projectId: projectId
          },
          defaultValue: {
            name: mockService.name,
            id: mockService.id
          }
        };
        scrollProps.value = {
          action: `${TESTER}/mock/apis?mockServiceId=${mockService ? mockService.id : mockServiceId.value}`,
          params: undefined
        };
      } else {
        treeProps.value = {
          action: `${TESTER}/mock/service`,
          params: {
            admin: true,
            projectId: projectId
          },
          disabled: !!mockService,
          defaultValue: undefined
        };
        scrollProps.value = {
          action: `${TESTER}/mock/apis?mockServiceId=${mockService ? mockService.id : mockServiceId.value}`,
          params: undefined
        };
      }
    }
  };

  /**
   * Execute the export operation
   * @param mockServiceId - ID of the mock service to export
   * @returns Promise that resolves when export is complete
   */
  const executeExport = async (mockServiceId: string | undefined) => {
    if (loading.value) {
      return;
    }

    loading.value = true;

    try {
      // Construct export URL based on selected APIs
      const serviceId = mockService ? mockService.id : mockServiceId;
      const exportUrl = apiIds.value
        ? `mock/service/export?mockServiceId=${serviceId}&format=${format.value}&mockApiIds=${apiIds.value}&exportScope=PROJECT`
        : `/mock/service/export?mockServiceId=${serviceId}&format=${format.value}&exportScope=PROJECT`;

      // Download the export file
      const [error] = await download(routerUtils.getTesterApiUrl(exportUrl));
      if (error) {
        return;
      }

      // Show success notification
      notification.success(t('mock.exportModal.notifications.exportSuccess'));
    } finally {
      loading.value = false;
    }
  };

  return {
    format,
    apiIds,
    loading,
    mockServiceId,
    treeProps,
    scrollProps,
    formatTypes,
    handleApiSelectionChange,
    handleExport,
    executeExport
  };
}
