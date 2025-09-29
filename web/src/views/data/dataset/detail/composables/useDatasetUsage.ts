import { useI18n } from 'vue-i18n';
import { computed, ref, watch, onMounted } from 'vue';
import { modal, notification } from '@xcan-angus/vue-ui';
import { SourceItem } from '../../types';
import { dataSet, paramTarget } from '@/api/tester';

/**
 * Composable for managing dataset usage list logic
 * Handles loading, displaying, and managing dataset usage information
 */
export function useDatasetUsage (props: { id: string }) {
  const { t } = useI18n();

  // Constants
  const MAX_NUM = 200;

  // Reactive references for component state
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

  /**
   * Computed property for selected items count
   */
  const selectedNum = computed(() => {
    return rowSelection.value?.selectedRowKeys?.length || 0;
  });

  /**
   * Table columns configuration
   */
  const columns = [
    {
      title: t('common.resourceType'),
      dataIndex: 'targetType',
      width: '10%',
      ellipsis: true
    },
    {
      title: t('common.resourceId'),
      dataIndex: 'targetId',
      width: '20%',
      ellipsis: true
    },
    {
      title: t('common.resourceName'),
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
   * Delete a single dataset usage entry
   */
  const deleteUsage = (data: SourceItem) => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: data.targetName }),
      async onOk () {
        loading.value = true;
        const params = [props.id];
        const [error] = await paramTarget.deleteDataSet(
          data.targetId,
          data.targetType?.value,
          params,
          { dataType: true }
        );
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
   * Initiate batch delete operation
   */
  const startBatchDelete = () => {
    if (!rowSelection.value) {
      rowSelection.value = {
        onChange: handleTableSelection,
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
    const selectedNum = selectedRowKeys.length;
    if (!selectedNum) {
      rowSelection.value = undefined;
      return;
    }

    if (selectedNum > MAX_NUM) {
      notification.error(t('dataset.detail.useList.notifications.batchDeleteLimit', { max: MAX_NUM, current: selectedNum }));
      return;
    }

    modal.confirm({
      content: t('dataset.detail.useList.notifications.batchDeleteConfirm', { num: selectedNum }),
      async onOk () {
        executeBatchDelete(selectedRowKeys);
      }
    });
  };

  /**
   * Execute batch delete operation
   */
  const executeBatchDelete = async (selectedRowKeys: string[]) => {
    const ids = selectedRowKeys;
    const num = ids.length;
    const params = [props.id];
    const promises: Promise<any>[] = [];

    for (let i = 0; i < num; i++) {
      const data = dataList.value.find((item) => item.targetId === ids[i]);
      if (data) {
        const _promise = paramTarget.deleteDataSet(
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
      for (let i = 0, len = res.length; i < len; i++) {
        if (res[i][0]) {
          errorIds.push(ids[i]);
        }
      }

      const errorNum = errorIds.length;
      if (errorNum === 0) {
        notification.success(t('dataset.detail.useList.notifications.batchDeleteAllSuccess', { num }));
        pagination.value.total = pagination.value.total - num;
        rowSelection.value = undefined;
        dataList.value = dataList.value.filter((item) => !ids.includes(item.targetId));
        return;
      }

      if (errorNum === num) {
        notification.error(t('dataset.detail.useList.notifications.batchDeleteAllFail', { num }));
        return;
      }

      const successIds = ids.filter(item => !errorIds.includes(item));
      notification.warning(t('dataset.detail.useList.notifications.batchDeletePartialSuccess', { success: num - errorNum, fail: errorNum }));
      rowSelection.value!.selectedRowKeys = rowSelection.value!.selectedRowKeys.filter((item) => !successIds.includes(item));
      dataList.value = dataList.value.filter((item) => !successIds.includes(item.targetId));
    });
  };

  /**
   * Cancel batch delete operation
   */
  const cancelBatchDelete = () => {
    rowSelection.value = undefined;
  };

  /**
   * Handle table row selection changes
   */
  const handleTableSelection = (keys: string[]) => {
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

    // Filter out deselected dataset entries
    const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

    for (let i = 0, len = keys.length; i < len; i++) {
      if (!selectedRowKeys.includes(keys[i])) {
        selectedRowKeys.push(keys[i]);
      }
    }

    const selectedCount = selectedRowKeys.length;
    if (selectedCount > MAX_NUM) {
      notification.info(t('dataset.detail.useList.notifications.batchDeleteLimitInfo', { max: MAX_NUM, current: selectedCount }));
    }

    rowSelection.value.selectedRowKeys = selectedRowKeys;
  };

  /**
   * Refresh the usage data
   */
  const refresh = () => {
    if (loading.value) {
      return;
    }

    loadUsageData();
  };

  /**
   * Load usage data from API
   */
  const loadUsageData = async () => {
    loading.value = true;
    const [error, res] = await dataSet.getDataSetTarget(props.id);
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

  // Watch for changes in dataset ID and reload data
  onMounted(() => {
    watch(() => props.id, (newValue) => {
      if (!newValue) {
        return;
      }

      loadUsageData();
    }, { immediate: true });
  });

  return {
    // State
    loading,
    loaded,
    pagination,
    rowSelection,
    dataList,

    // Computed properties
    selectedNum,
    columns,

    // Methods
    deleteUsage,
    startBatchDelete,
    cancelBatchDelete,
    refresh,
    loadUsageData
  };
}
