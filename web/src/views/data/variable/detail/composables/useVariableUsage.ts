import { useI18n } from 'vue-i18n';
import { computed, onMounted, ref, watch } from 'vue';
import { modal, notification } from '@xcan-angus/vue-ui';
import { paramTarget, variable } from '@/api/tester';
import { SourceItem } from '../types';

/**
 * Composable for managing variable usage list logic
 * Handles data fetching, pagination, row selection, and CRUD operations for variable usage
 */
export function useVariableUsage (props: { id: string }) {
  const { t } = useI18n();

  // Maximum number of items that can be selected for batch operations
  const MAX_NUM = 200;

  // Reactive state for component
  const loading = ref(false);
  const loaded = ref(false);
  const pagination = ref<{
    current: number;
    pageSize: number;
    total: number;
    showSizeChanger: false;
  }>({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: false
  });

  const rowSelection = ref<{
    onChange:(key: string[]) => void;
    getCheckboxProps: (data: SourceItem) => ({ disabled: boolean; });
    selectedRowKeys: string[];
  }>();

  const dataList = ref<SourceItem[]>([]);
  const selectedNum = computed(() => rowSelection.value?.selectedRowKeys?.length || 0);

  // Table columns definition
  const columns = [
    {
      title: t('dataVariable.detail.useList.columns.targetType'),
      dataIndex: 'targetType',
      width: '10%',
      ellipsis: true
    },
    {
      title: t('dataVariable.detail.useList.columns.targetId'),
      dataIndex: 'targetId',
      width: '20%',
      ellipsis: true
    },
    {
      title: t('dataVariable.detail.useList.columns.targetName'),
      dataIndex: 'targetName',
      ellipsis: true
    },
    {
      title: t('common.actions'),
      dataIndex: 'action',
      width: 70
    }
  ];

  /**
   * Delete a single variable usage entry
   * @param data - The source item to delete
   */
  const toDelete = (data: SourceItem) => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: data.targetName }),
      async onOk () {
        loading.value = true;
        const params = [props.id];
        const [error] = await paramTarget.deleteVariable(data.targetId, data.targetType?.value, params, { dataType: true });
        loading.value = false;
        if (error) {
          return;
        }

        pagination.value.total = pagination.value.total - 1;
        notification.success(t('actions.tips.deleteSuccess'));
        dataList.value = dataList.value.filter((item) => item.targetId !== data.targetId);
      }
    });
  };

  /**
   * Initiate batch delete mode or execute batch delete
   */
  const toBatchDelete = () => {
    // If not in batch delete mode, enter batch delete mode
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

    // If in batch delete mode, execute batch delete
    const selectedRowKeys = rowSelection.value.selectedRowKeys;
    const selectedNum = selectedRowKeys.length;
    if (!selectedNum) {
      rowSelection.value = undefined;
      return;
    }

    // Check if selection exceeds limit
    if (selectedNum > MAX_NUM) {
      notification.error(t('dataVariable.detail.useList.notifications.batchDeleteLimit', { max: MAX_NUM, current: selectedNum }));
      return;
    }

    // Confirm and execute batch delete
    modal.confirm({
      content: t('dataVariable.detail.useList.notifications.batchDeleteConfirm', { num: selectedNum }),
      async onOk () {
        const ids = selectedRowKeys;
        const num = ids.length;
        const params = [props.id];
        const promises: Promise<any>[] = [];

        // Create delete promises for all selected items
        for (let i = 0; i < num; i++) {
          const data = dataList.value.find((item) => item.targetId === ids[i]);
          if (data) {
            const _promise = paramTarget.deleteVariable(
              data.targetId,
              data.targetType?.value,
              params,
              { dataType: true, silence: true }
            );
            promises.push(_promise);
          }
        }

        loading.value = true;
        Promise.all(promises).then((res: [Error | null, any][]) => {
          loading.value = false;
          const errorIds: string[] = [];

          // Identify failed deletions
          for (let i = 0, len = res.length; i < len; i++) {
            if (res[i][0]) {
              errorIds.push(ids[i]);
            }
          }

          const errorNum = errorIds.length;

          // Handle different outcomes
          if (errorNum === 0) {
            // All successful
            notification.success(t('dataVariable.detail.useList.notifications.batchDeleteAllSuccess', { num }));
            pagination.value.total = pagination.value.total - num;
            rowSelection.value = undefined;
            dataList.value = dataList.value.filter((item) => !ids.includes(item.targetId));
            return;
          }

          if (errorNum === num) {
            // All failed
            notification.error(t('dataVariable.detail.useList.notifications.batchDeleteAllFail'));
            return;
          }

          // Partial success
          const successIds = ids.filter(item => !errorIds.includes(item));
          notification.warning(t('dataVariable.detail.useList.notifications.batchDeletePartialSuccess', {
            success: successIds.length,
            fail: errorNum
          }));

          rowSelection.value!.selectedRowKeys = rowSelection.value!.selectedRowKeys.filter((item) => !successIds.includes(item));
          dataList.value = dataList.value.filter((item) => !successIds.includes(item.targetId));
        });
      }
    });
  };

  /**
   * Cancel batch delete mode
   */
  const toCancelBatchDelete = () => {
    rowSelection.value = undefined;
  };

  /**
   * Handle table row selection changes
   * @param keys - Selected row keys
   */
  const tableSelect = (keys: string[]) => {
    if (!rowSelection.value) {
      return;
    }

    const currentIds = dataList.value.map(item => item.targetId);
    const deleteIds = currentIds.reduce((prev, cur) => {
      if (!keys.includes(cur)) {
        prev.push(cur);
      }
      return prev;
    }, [] as string[]);

    // Remove deselected items from selection
    const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

    // Add newly selected items
    for (let i = 0, len = keys.length; i < len; i++) {
      if (!selectedRowKeys.includes(keys[i])) {
        selectedRowKeys.push(keys[i]);
      }
    }

    // Check selection limit
    const selectedNum = selectedRowKeys.length;
    if (selectedNum > MAX_NUM) {
      notification.info(t('dataVariable.detail.useList.notifications.batchDeleteLimit', { max: MAX_NUM, current: selectedNum }));
    }

    rowSelection.value.selectedRowKeys = selectedRowKeys;
  };

  /**
   * Refresh the usage list data
   */
  const refresh = () => {
    if (loading.value) {
      return;
    }

    loadData();
  };

  /**
   * Load variable usage data from API
   */
  const loadData = async () => {
    loading.value = true;
    const [error, res] = await variable.getVariableTargetDetail(props.id);
    loading.value = false;
    loaded.value = true;

    if (error) {
      pagination.value.total = 0;
      dataList.value = [];
      if (rowSelection.value) {
        rowSelection.value = undefined;
      }
      return;
    }

    const data = res?.data || [];
    pagination.value.total = data.length;
    dataList.value = data;

    if (rowSelection.value) {
      rowSelection.value = undefined;
    }
  };

  // Load data when component is mounted
  onMounted(() => {
    watch(() => props.id, (newValue) => {
      if (!newValue) {
        return;
      }

      loadData();
    }, { immediate: true });
  });

  return {
    columns,

    // State
    loading,
    loaded,
    pagination,
    rowSelection,
    dataList,
    selectedNum,

    // Methods
    toDelete,
    toBatchDelete,
    toCancelBatchDelete,
    refresh
  };
}
