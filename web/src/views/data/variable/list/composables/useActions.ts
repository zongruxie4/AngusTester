import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { modal, notification } from '@xcan-angus/vue-ui';
import { variable } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { getCurrentPage } from '@/utils/utils';
import type { VariableDetail } from '../../types';

/**
 * Maximum number of items that can be deleted in batch
 */
const MAX_BATCH_DELETE_NUM = 200;

/**
 * Composable function for managing variable actions
 *
 * @param _projectId - Project identifier
 * @param deleteTabPane - Function to delete tab panes
 * @param loadData - Function to reload data
 * @param pagination - Pagination state
 * @param rowSelection - Row selection state
 * @param visibilityIdSet - Set of visible variable IDs
 * @param errorMessageMap - Map of error messages
 * @returns Object containing action functions and state
 */
export function useActions (
  _projectId: string,
  deleteTabPane: (keys: string[]) => void,
  loadData: () => void,
  pagination: { current: number; pageSize: number; total: number },
  rowSelection: { selectedRowKeys: string[] } | undefined,
  visibilityIdSet: Set<string>,
  errorMessageMap: Map<string, string>
) {
  const { t } = useI18n();
  const router = useRouter();

  // Modal visibility state
  const importVariableModalVisible = ref(false);
  const exportVariableModalVisible = ref(false);
  const exportVariableId = ref<string>();

  /**
   * Navigate to create static variable page
   */
  const navigateToCreateStaticVariable = () => {
    router.push('/data#variables?source=VALUE');
  };

  /**
   * Navigate to create variable from file extraction
   */
  const navigateToFileExtraction = () => {
    router.push('/data#variables?source=FILE');
  };

  /**
   * Navigate to create variable from HTTP extraction
   */
  const navigateToHttpExtraction = () => {
    router.push('/data#variables?source=http');
  };

  /**
   * Navigate to create variable from JDBC extraction
   */
  const navigateToJdbcExtraction = () => {
    router.push('/data#variables?source=JDBC');
  };

  /**
   * Handle dropdown button clicks for different extraction types
   *
   * @param key - Extraction type key
   */
  const handleButtonDropdownClick = ({ key }: { key: 'file' | 'http' | 'jdbc' }) => {
    switch (key) {
      case 'file':
        navigateToFileExtraction();
        break;
      case 'http':
        navigateToHttpExtraction();
        break;
      case 'jdbc':
        navigateToJdbcExtraction();
        break;
    }
  };

  /**
   * Show import variable modal
   */
  const showImportModal = () => {
    importVariableModalVisible.value = true;
  };

  /**
   * Handle import success
   */
  const handleImportSuccess = () => {
    pagination.current = 1;
    loadData();
  };

  /**
   * Show export variable modal
   *
   * @param id - Variable ID to export (optional)
   */
  const showExportModal = (id?: string) => {
    exportVariableModalVisible.value = true;
    exportVariableId.value = id;
  };

  /**
   * Navigate to edit variable page
   *
   * @param data - Variable item to edit
   */
  const navigateToEdit = (data: VariableDetail) => {
    router.push(`/data#variables?id=${data.id}`);
  };

  /**
   * Delete a single variable
   *
   * @param data - Variable item to delete
   */
  const deleteVariable = (data: VariableDetail) => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: data.name }),
      async onOk () {
        const [error] = await variable.deleteVariables([data.id]);
        if (error) return;

        notification.success(t('actions.tips.deleteSuccess'));

        // Update pagination and reload data
        pagination.current = getCurrentPage(
          pagination.current,
          pagination.pageSize,
          pagination.total
        );
        loadData();

        // Clean up state
        visibilityIdSet.delete(data.id);
        errorMessageMap.delete(data.id);
        deleteTabPane([data.id]);
      }
    });
  };

  /**
   * Clone a variable
   *
   * @param data - Variable item to clone
   */
  const cloneVariable = async (data: VariableDetail) => {
    const [error] = await variable.cloneVariable([data.id]);
    if (error) return;

    notification.success(t('actions.tips.cloneSuccess'));
    loadData();
  };

  /**
   * Handle table dropdown menu clicks
   *
   * @param menuItem - Menu item that was clicked
   * @param data - Variable item data
   */
  const handleTableDropdownClick = (
    menuItem: { name: string; key: 'export' | 'clone'; icon: string },
    data: VariableDetail
  ) => {
    switch (menuItem.key) {
      case 'export':
        showExportModal(data.id);
        break;
      case 'clone':
        cloneVariable(data);
        break;
    }
  };

  /**
   * Execute batch delete operation
   */
  const executeBatchDelete = (rowSelection: { selectedRowKeys: string[] } | undefined) => {
    // Check if rowSelection is defined
    if (!rowSelection) {
      return;
    }

    const selectedRowKeys = rowSelection.selectedRowKeys;

    // Check if selectedRowKeys is defined
    if (!selectedRowKeys) {
      return;
    }

    const num = selectedRowKeys.length;

    if (!num) {
      // Reset rowSelection
      if (rowSelection) {
        rowSelection.selectedRowKeys = [];
      }
      return;
    }

    if (num > MAX_BATCH_DELETE_NUM) {
      notification.error(
        t('dataCommon.searchPanel.messages.maxBatchDeleteError', {
          maxNum: MAX_BATCH_DELETE_NUM,
          num
        })
      );
      return;
    }

    modal.confirm({
      content: t('dataCommon.searchPanel.messages.batchDeleteConfirm', { num }),
      async onOk () {
        const [error] = await variable.deleteVariables(selectedRowKeys);
        if (error) return;

        notification.success(t('dataCommon.searchPanel.messages.batchDeleteSuccess'));

        // Clean up state
        selectedRowKeys.forEach(id => {
          visibilityIdSet.delete(id);
          errorMessageMap.delete(id);
        });

        // Update pagination and reload data
        updatePaginationAfterBatchDelete(selectedRowKeys.length);
        loadData();
      }
    });
  };

  /**
   * Update pagination after batch delete operation
   *
   * @param deleteCount - Number of deleted items
   */
  const updatePaginationAfterBatchDelete = (deleteCount: number) => {
    const { current, pageSize, total } = pagination;
    const totalPage = Math.ceil(total / pageSize);
    const remainder = total % pageSize;

    const deletePages = Math.floor(deleteCount / pageSize);
    const deleteRemainder = deleteCount % pageSize;

    if ((deleteRemainder === 0 || remainder === 0) || (deleteRemainder < remainder)) {
      if (current + deletePages <= totalPage) {
        loadData();
        return;
      }
      pagination.current = current - (current + deletePages - totalPage) || 1;
      loadData();
      return;
    }

    if (deleteRemainder >= remainder) {
      if (current + deletePages + 1 <= totalPage) {
        loadData();
        return;
      }
      pagination.current = current - (current + deletePages - totalPage) - 1 || 1;
      loadData();
    }
  };

  return {
    // State
    importVariableModalVisible,
    exportVariableModalVisible,
    exportVariableId,

    // Navigation methods
    navigateToCreateStaticVariable,
    navigateToFileExtraction,
    navigateToHttpExtraction,
    navigateToJdbcExtraction,
    navigateToEdit,

    // Action methods
    handleButtonDropdownClick,
    showImportModal,
    handleImportSuccess,
    showExportModal,
    deleteVariable,
    cloneVariable,
    handleTableDropdownClick,
    executeBatchDelete
  };
}
