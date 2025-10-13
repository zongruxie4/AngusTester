import { ref, Ref, computed } from 'vue';
import { DataSetDetail } from '../../types';
import { dataset } from '@/api/tester';
import { modal, notification } from '@xcan-angus/vue-ui';

/**
 * Batch operations composable
 * Handles batch selection, deletion and other multi-item operations
 */
export function useActions (
  tableData: Ref<DataSetDetail[]>,
  pagination: Ref<{ current: number; pageSize: number; total: number; }>,
  loadData: () => Promise<void>,
  deleteTabPane: (keys: string[]) => void,
  t: (key: string, params?: any) => string
) {
  // Maximum number of items that can be selected for batch operations
  const MAX_NUM = 200;

  // Row selection configuration for the table
  const rowSelection = ref<{
    onChange:(key: string[]) => void;
    getCheckboxProps: (data: DataSetDetail) => ({ disabled: boolean; });
    selectedRowKeys: string[];
  } | undefined>();

  /**
   * Handle table row selection change
   * Updates the selected row keys based on user selection
   *
   * @param keys - Array of selected row keys
   */
  const tableSelect = (keys: string[]) => {
    if (!rowSelection.value) {
      return;
    }

    const currentIds = tableData.value.map(item => item.id);
    const deleteIds = currentIds.reduce((prev, cur) => {
      if (!keys.includes(cur)) {
        prev.push(cur);
      }
      return prev;
    }, [] as string[]);

    // Remove deselected datasets
    const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

    for (let i = 0, len = keys.length; i < len; i++) {
      if (!selectedRowKeys.includes(keys[i])) {
        selectedRowKeys.push(keys[i]);
      }
    }

    const num = selectedRowKeys.length;
    if (num > MAX_NUM) {
      notification.info(t('dataset.list.messages.batchDeleteMaxInfo', { maxNum: MAX_NUM, num }));
    }

    rowSelection.value.selectedRowKeys = selectedRowKeys;
  };

  /**
   * Initialize batch delete mode
   * Sets up the row selection configuration for batch operations
   */
  const initBatchDelete = () => {
    if (!rowSelection.value) {
      rowSelection.value = {
        onChange: tableSelect,
        getCheckboxProps: () => {
          return {
            disabled: false
          };
        },
        selectedRowKeys: []
      };
      return;
    }

    const selectedRowKeys = rowSelection.value.selectedRowKeys;
    const num = selectedRowKeys.length;
    if (!num) {
      rowSelection.value = undefined;
    }

    if (num > MAX_NUM) {
      notification.error(t('dataset.list.messages.batchDeleteMaxError', { maxNum: MAX_NUM, num }));
    }
  };

  /**
   * Cancel batch delete mode
   * Clears the row selection configuration
   */
  const cancelBatchDelete = () => {
    rowSelection.value = undefined;
  };

  /**
   * Execute batch delete operation
   * Performs the actual deletion of selected datasets
   *
   * @param loading - Ref to the loading state
   */
  const executeBatchDelete = async (loading: Ref<boolean>) => {
    if (!rowSelection.value) return;

    const selectedRowKeys = rowSelection.value.selectedRowKeys;
    const num = selectedRowKeys.length;

    if (num > MAX_NUM) {
      notification.error(t('dataset.list.messages.batchDeleteMaxError', { maxNum: MAX_NUM, num }));
      return;
    }

    modal.confirm({
      content: t('dataset.list.messages.batchDeleteConfirm', { num }),
      async onOk () {
        const ids = selectedRowKeys;
        // Set loading state
        loading.value = true;

        const [error] = await dataset.deleteDataSet(ids);
        // Reset loading state
        loading.value = false;
        if (error) {
          return;
        }

        notification.success(t('dataset.list.messages.batchDeleteSuccess'));
        rowSelection.value = undefined;

        const { current, pageSize, total } = pagination.value;
        const totalPage = Math.ceil(total / pageSize);
        const remainder = total % pageSize;

        const deleteNum = ids.length;
        const deletePages = Math.floor(deleteNum / pageSize);
        const deleteRemainder = deleteNum % pageSize;

        if ((deleteRemainder === 0 || remainder === 0) || (deleteRemainder < remainder)) {
          if (current + deletePages <= totalPage) {
            loadData();
            return;
          }

          pagination.value.current = current - (current + deletePages - totalPage) || 1;
          loadData();
          return;
        }

        if (deleteRemainder >= remainder) {
          if (current + deletePages + 1 <= totalPage) {
            loadData();
            return;
          }

          pagination.value.current = current - (current + deletePages - totalPage) - 1 || 1;
          loadData();
        }

        // Delete tab panes for the deleted datasets
        deleteTabPane(ids);
      }
    });
  };

  /**
   * Computed property for selected items count
   * Returns the number of currently selected items
   */
  const selectedNum = computed(() => rowSelection.value?.selectedRowKeys?.length);

  return {
    // Reactive states
    rowSelection,
    selectedNum,

    // Methods
    initBatchDelete,
    cancelBatchDelete,
    executeBatchDelete,
    tableSelect
  };
}
