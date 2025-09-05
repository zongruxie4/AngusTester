import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { modal, notification } from '@xcan-angus/vue-ui';
import { TESTER, download } from '@xcan-angus/infra';

/**
 * Composable for managing UI state and modal operations
 * Handles modal visibility, confirmations, and navigation
 */
export function useMockApiUI () {
  const router = useRouter();

  // Modal visibility states
  const apiCopyModalVisible = ref(false);
  const apiLinkModalVisible = ref(false);
  const importApiModalVisible = ref(false);

  // Modal loading states
  const apiCopyConfirmLoading = ref(false);
  const apiLinkConfirmLoading = ref(false);

  /**
   * Navigate back to mock service list
   */
  const goback = () => {
    router.push('/apis#mock');
  };

  /**
   * Show confirmation dialog for API deletion
   * @param apiName - Name of the API to delete
   * @param onConfirm - Callback function to execute on confirmation
   */
  const showDeleteConfirmation = (apiName: string, onConfirm: () => void) => {
    modal.confirm({
      centered: true,
      content: `Are you sure you want to delete "${apiName}"?`,
      onOk: onConfirm
    });
  };

  /**
   * Show confirmation dialog for instance refresh
   * @param onConfirm - Callback function to execute on confirmation
   */
  const showRefreshInstanceConfirmation = (onConfirm: () => void) => {
    modal.confirm({
      content: 'Are you sure you want to refresh the instance configuration?',
      onOk: onConfirm
    });
  };

  /**
   * Export Mock API to file
   * @param apiId - API ID to export
   * @param serviceId - Service ID for the export
   */
  const exportMockApi = (apiId: string, serviceId: string) => {
    const url = `${TESTER}/mock/service/export?mockApiIds=${apiId}&mockServiceId=${serviceId}`;
    download(url);
  };

  /**
   * Show error notification
   * @param message - Error message to display
   */
  const showErrorNotification = (message: string) => {
    notification.error(message);
  };

  /**
   * Show success notification
   * @param message - Success message to display
   */
  const showSuccessNotification = (message: string) => {
    notification.success(message);
  };

  return {
    // Modal states
    apiCopyModalVisible,
    apiLinkModalVisible,
    importApiModalVisible,
    apiCopyConfirmLoading,
    apiLinkConfirmLoading,

    // Methods
    goback,
    showDeleteConfirmation,
    showRefreshInstanceConfirmation,
    exportMockApi,
    showErrorNotification,
    showSuccessNotification
  };
}
