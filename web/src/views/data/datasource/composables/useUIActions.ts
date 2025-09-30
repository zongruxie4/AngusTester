import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { modal, notification } from '@xcan-angus/vue-ui';
import { DataSourceDetail } from '../types';

/**
 * <p>Composable for managing UI actions and modal state</p>
 * <p>Handles modal visibility, edit data, and user interaction logic</p>
 */
export function useUIActions () {
  const { t } = useI18n();

  // Modal state
  const isModalVisible = ref(false);
  const editData = ref<DataSourceDetail | undefined>();

  /**
   * <p>Open add modal for creating new data source</p>
   * <p>Resets edit data and shows the modal</p>
   */
  const openAddModal = (): void => {
    editData.value = undefined;
    isModalVisible.value = true;
  };

  /**
   * <p>Open edit modal for modifying existing data source</p>
   * <p>Sets edit data and shows the modal</p>
   */
  const openEditModal = (record: DataSourceDetail): void => {
    editData.value = record;
    isModalVisible.value = true;
  };

  /**
   * <p>Close modal and reset state</p>
   * <p>Hides modal and clears edit data</p>
   */
  const closeModal = (): void => {
    isModalVisible.value = false;
    editData.value = undefined;
  };

  /**
   * <p>Show delete confirmation dialog</p>
   * <p>Displays confirmation modal before deleting data source</p>
   */
  const showDeleteConfirmation = (
    record: DataSourceDetail,
    onConfirm: () => Promise<void>
  ): void => {
    modal.confirm({
      centered: true,
      content: t('actions.tips.confirmDelete', { name: record.name }),
      async onOk () {
        await onConfirm();
      }
    });
  };

  /**
   * <p>Show success notification</p>
   * <p>Displays success message after successful operations</p>
   */
  const showSuccessNotification = (message: string): void => {
    notification.success(message);
  };

  /**
   * <p>Show error notification</p>
   * <p>Displays error message for failed operations</p>
   */
  const showErrorNotification = (message: string): void => {
    notification.error(message);
  };

  return {
    // State
    isModalVisible,
    editData,

    // Methods
    openAddModal,
    openEditModal,
    closeModal,
    showDeleteConfirmation,
    showSuccessNotification,
    showErrorNotification
  };
}
