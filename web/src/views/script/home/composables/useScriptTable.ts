import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { notification, modal } from '@xcan-angus/vue-ui';
import { script, exec } from '@/api/tester';

import { ScriptInfo } from '@/views/script/types';

/**
 * Composable for managing script table functionality
 * @param permissionsMap - Map of script permissions
 */
export function useScriptTable (permissionsMap: { [key: string]: string[] }) {
  const router = useRouter();
  const { t } = useI18n();

  // Constants
  const MAX_NUM = 200;

  // Columns configuration
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: '10%',
      key: 'id',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('scriptHome.table.columns.name'),
      dataIndex: 'name',
      ellipsis: true,
      sorter: true,
      width: '20%',
      key: 'name'
    },
    {
      title: t('scriptHome.table.columns.plugin'),
      dataIndex: 'plugin',
      sorter: true,
      width: '6%',
      key: 'plugin',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.type'),
      dataIndex: 'type',
      sorter: true,
      width: '7%',
      key: 'type',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.source'),
      dataIndex: 'source',
      sorter: true,
      width: '7%',
      key: 'source',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('scriptHome.table.columns.resourceId'),
      dataIndex: 'sourceId',
      width: '18%',
      groupName: 'source',
      hide: true,
      key: 'sourceId',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('scriptHome.table.columns.resourceName'),
      dataIndex: 'sourceName',
      width: '18%',
      groupName: 'source',
      hide: false,
      key: 'sourceName',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('scriptHome.table.columns.tags'),
      dataIndex: 'tags',
      key: 'tags',
      width: '14%'
    },
    {
      title: t('scriptHome.table.columns.creator'),
      dataIndex: 'createdByName',
      width: '8%',
      groupName: 'createdByName',
      key: 'createdByName',
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: t('scriptHome.table.columns.modifier'),
      dataIndex: 'lastModifiedByName',
      width: '8%',
      groupName: 'createdByName',
      hide: true,
      key: 'lastModifiedByName',
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: t('scriptHome.table.columns.addTime'),
      dataIndex: 'createdDate',
      width: '10%',
      sorter: true,
      groupName: 'createdDate',
      key: 'createdDate',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('scriptHome.table.columns.modifyTime'),
      dataIndex: 'lastModifiedDate',
      width: '10%',
      sorter: true,
      groupName: 'createdDate',
      hide: true,
      key: 'lastModifiedDate',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.actions'),
      dataIndex: 'action',
      width: 180,
      key: 'action'
    }
  ];

  // Menu items for dropdown
  const actionItems = [
    {
      key: 'auth',
      icon: 'icon-quanxian1',
      name: t('actions.permission'),
      permission: 'GRANT'
    },
    {
      key: 'clone',
      icon: 'icon-fuzhi',
      name: t('actions.clone'),
      permission: 'VIEW'
    },
    {
      key: 'delete',
      icon: 'icon-qingchu',
      name: t('actions.delete'),
      permission: 'DELETE'
    },
    {
      key: 'export',
      icon: 'icon-daochu',
      name: t('actions.export'),
      permission: 'EXPORT'
    }
  ];

  // Selected data tracking
  const selectedDataMap = ref<{
    [key: string]: {
      id: string;
      name: string;
    };
  }>({});

  // Batch operation disabled states
  const batchExecDisabled = ref(false);
  const batchDeleteDisabled = ref(false);
  const batchExportDisabled = ref(false);

  // Row selection configuration
  const rowSelection = ref<{
    onChange:(key: string[]) => void;
    selectedRowKeys: string[];
      }>({
        onChange: () => {},
        selectedRowKeys: []
      });

  /**
   * Cancel batch operations
   */
  const cancelBatchOperation = () => {
    rowSelection.value.selectedRowKeys = [];
    selectedDataMap.value = {};
  };

  /**
   * Handle batch execution
   */
  const handleBatchExec = async (loadingSetter: (loading: boolean) => void) => {
    const num = rowSelection.value.selectedRowKeys.length;

    if (num === 0) {
      return;
    }
    if (num > MAX_NUM) {
      notification.error(t('scriptHome.table.messages.maxBatchLimit', { maxNum: MAX_NUM, selectedNum: num }));
      return;
    }

    // Confirmation would be handled in the parent component
    loadingSetter(true);

    const ids = Object.values(selectedDataMap.value).map(item => item.id);
    const promises: Promise<any>[] = [];

    for (let i = 0, len = ids.length; i < len; i++) {
      promises.push(exec.addExecByScript({ scriptId: ids[i] }, { silence: true }));
    }

    Promise.all(promises).then((res: [Error | null, any][]) => {
      const errorIds: string[] = [];

      for (let i = 0, len = res.length; i < len; i++) {
        if (res[i][0]) {
          errorIds.push(ids[i]);
        }
      }
      const errorNum = errorIds.length;

      if (errorNum === 0) {
        notification.success(t('scriptHome.table.messages.executeSuccess', { num }));
        rowSelection.value.selectedRowKeys = [];
        selectedDataMap.value = {};
        return;
      }

      if (errorNum === num) {
        notification.error(t('scriptHome.table.messages.executeFail', { num }));
        return;
      }

      const successIds = ids.filter(item => !errorIds.includes(item));
      notification.warning(t('scriptHome.table.messages.executePartial', { successNum: num - errorNum, errorNum }));

      rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successIds.includes(item));

      for (let i = 0, len = successIds.length; i < len; i++) {
        delete selectedDataMap.value[successIds[i]];
      }
    }).finally(() => {
      loadingSetter(false);
    });
  };

  /**
   * Handle batch delete
   */
  const handleBatchDelete = async (loadingSetter: (loading: boolean) => void, deleteCallback: (ids: string[]) => void) => {
    const num = rowSelection.value.selectedRowKeys.length;
    if (!num) {
      return;
    }
    if (num > MAX_NUM) {
      notification.error(t('scriptHome.table.messages.maxDeleteLimit', { maxNum: MAX_NUM, num }));
      return;
    }
    modal.confirm({
      title: t('actions.delete'),
      content: t('actions.tips.confirmDelete', { num }),
      onOk: async () => {
        // Confirmation would be handled in the parent component
        loadingSetter(true);
        const ids = Object.values(selectedDataMap.value).map(item => item.id);
        const [error] = await script.deleteScript(ids);
        loadingSetter(false);

        if (error) {
          return;
        }
        notification.success(t('actions.tips.deleteSuccess', { num }));
        deleteCallback(ids);
        rowSelection.value.selectedRowKeys = [];
        selectedDataMap.value = {};
      }
    });
  };

  /**
   * Handle batch export
   */
  const handleBatchExport = (exportSetter: (ids: string[], visible: boolean) => void) => {
    exportSetter(rowSelection.value.selectedRowKeys, true);
  };

  /**
   * Handle single script execution
   */
  const handleSingleExec = async (data: ScriptInfo, loadingSetter: (loading: boolean) => void) => {
    modal.confirm({
      title: t('actions.execute'),
      content: t('scriptHome.table.messages.executeScriptConfirmSimple', { name: data.name }),
      onOk: async () => {
        loadingSetter(true);
        const [error] = await exec.addExecByScript({ scriptId: data.id });
        loadingSetter(false);
        if (error) {
          return;
        }
        notification.success(t('scriptHome.table.messages.addExecuteSuccess'));
      }
    });
  };

  /**
   * Handle skip to editor
   */
  const handleToEditor = async (data: ScriptInfo, pagination) => {
    router.push(`/script/edit/${data.id}?type=edit&pageNo=${pagination?.current || 1}&pageSize=${pagination?.pageSize || 10}`);
  };

  /**
   * Handle script clone
   */
  const handleClone = async (data: ScriptInfo, loadingSetter: (loading: boolean) => void, refreshCallback: () => void) => {
    loadingSetter(true);
    const [error] = await script.cloneScript(data.id);
    loadingSetter(false);
    if (error) {
      return;
    }
    notification.success(t('actions.tips.cloneSuccess'));
    refreshCallback();
  };

  /**
   * Handle script delete
   */
  const handleDelete = async (data: ScriptInfo, loadingSetter: (loading: boolean) => void, deleteCallback: (ids: string[]) => void) => {
    // Confirmation would be handled in the parent component
    modal.confirm({
      title: t('actions.delete'),
      content: t('scriptHome.table.messages.deleteScriptConfirmSimple', { name: data.name }),
      onOk: async () => {
        const id = data.id;
        loadingSetter(true);
        const [error] = await script.deleteScript([id]);
        loadingSetter(false);

        if (error) {
          return;
        }
        notification.success(t('scriptHome.table.messages.deleteScriptSuccess'));
        deleteCallback([id]);
      }
    });
  };

  /**
   * Handle script export
   */
  const handleExport = (data: ScriptInfo, exportSetter: (ids: string[], visible: boolean) => void) => {
    exportSetter([data.id], true);
  };

  /**
   * Handle action clicks
   */
  const handleActionClick = (
    key: string,
    data: ScriptInfo,
    loadingSetter: (loading: boolean) => void,
    refreshCallback: () => void,
    deleteCallback: (ids: string[]) => void,
    exportSetter: (ids: string[], visible: boolean) => void
  ) => {
    switch (key) {
      case 'exec':
        handleSingleExec(data, loadingSetter);
        break;
      case 'edit':
        handleToEditor(data);
        break;
      case 'auth':
        // Auth handling would be done in the parent component
        break;
      case 'clone':
        handleClone(data, loadingSetter, refreshCallback);
        break;
      case 'delete':
        handleDelete(data, loadingSetter, deleteCallback);
        break;
      case 'export':
        handleExport(data, exportSetter);
        break;
    }
  };

  // Watch for changes in selected data map to update batch operation states
  watch(() => selectedDataMap.value, (newValue) => {
    batchExportDisabled.value = false;
    batchExecDisabled.value = false;
    batchDeleteDisabled.value = false;

    const values = (Object.values(newValue) || []) as {
      id: string;
      name: string;
    }[];

    const _permissionsMap = permissionsMap;

    for (let i = 0, len = values.length; i < len; i++) {
      const { id } = values[i];
      const permissions = _permissionsMap[id];
      if (permissions) {
        if (!permissions.includes('TEST')) {
          batchExecDisabled.value = true;
        }

        if (!permissions.includes('DELETE')) {
          batchDeleteDisabled.value = true;
        }

        if (!permissions.includes('EXPORT')) {
          batchExportDisabled.value = true;
        }
      }
    }
  }, { immediate: true, deep: true });

  // Computed properties
  const selectedIds = ref<string[]>([]);
  watch(() => rowSelection.value.selectedRowKeys, (newVal) => {
    selectedIds.value = newVal || [];
  }, { immediate: true });

  return {
    columns,
    actionItems,

    // Reactive data
    selectedDataMap,
    batchExecDisabled,
    batchDeleteDisabled,
    batchExportDisabled,
    rowSelection,

    // Methods
    cancelBatchOperation,
    handleBatchExec,
    handleBatchDelete,
    handleBatchExport,
    handleSingleExec,
    handleToEditor,
    handleClone,
    handleDelete,
    handleExport,
    handleActionClick,

    // Computed properties
    selectedIds: selectedIds
  };
}
