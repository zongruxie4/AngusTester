import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { notification } from '@xcan-angus/vue-ui';
import { script, exec } from '@/api/tester';
import { ScriptInfo } from '../types';

/**
 * Composable for managing script table functionality
 * @param permissionsMap - Map of script permissions
 */
export function useScriptTable(permissionsMap: { [key: string]: string[] }) {
  const router = useRouter();

  // Constants
  const MAX_NUM = 200;

  // Columns configuration
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: '12%',
      key: 'id',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: '名称',
      dataIndex: 'name',
      ellipsis: true,
      sorter: true,
      width: '15%',
      key: 'name'
    },
    {
      title: '插件',
      dataIndex: 'plugin',
      sorter: true,
      width: '6%',
      key: 'plugin',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: '类型',
      dataIndex: 'type',
      sorter: true,
      width: '6%',
      key: 'type',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: '来源',
      dataIndex: 'source',
      sorter: true,
      width: '6%',
      key: 'source',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: '资源ID',
      dataIndex: 'sourceId',
      width: '12%',
      groupName: 'sourceId',
      hide: true,
      key: 'sourceId',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: '资源名称',
      dataIndex: 'sourceName',
      width: '15%',
      groupName: 'sourceId',
      hide: false,
      key: 'sourceName',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags'
    },
    {
      title: '添加人',
      dataIndex: 'createdByName',
      width: '7%',
      groupName: 'createdByName',
      key: 'createdByName',
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: '最后修改人',
      dataIndex: 'lastModifiedByName',
      width: '8%',
      groupName: 'createdByName',
      hide: true,
      key: 'lastModifiedByName',
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: '添加时间',
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
      title: '最后修改时间',
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
      title: '操作',
      dataIndex: 'action',
      width: 138,
      key: 'action'
    }
  ];

  // Menu items for dropdown
  const actionItems = [
    {
      key: 'auth',
      icon: 'icon-quanxian1',
      name: '权限',
      permission: 'GRANT'
    },
    {
      key: 'clone',
      icon: 'icon-fuzhi',
      name: '克隆',
      permission: 'COLON'
    },
    {
      key: 'delete',
      icon: 'icon-qingchu',
      name: '删除',
      permission: 'DELETE'
    },
    {
      key: 'export',
      icon: 'icon-daochu',
      name: '导出',
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
      notification.error(`最大支持批量执行 ${MAX_NUM} 个脚本，当前已选中 ${num} 个脚本。`);
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
        notification.success(`选中的 ${num} 条脚本全部执行成功`);
        rowSelection.value.selectedRowKeys = [];
        selectedDataMap.value = {};
        return;
      }

      if (errorNum === num) {
        notification.error(`选中的 ${num} 条脚本全部执行失败`);
        return;
      }

      const successIds = ids.filter(item => !errorIds.includes(item));
      notification.warning(`选中的 ${num - errorNum} 条脚本执行成功，${errorNum} 条脚本执行失败`);

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
      notification.error(`最大支持批量删除 ${MAX_NUM} 个脚本，当前已选中 ${num} 个脚本。`);
      return;
    }

    // Confirmation would be handled in the parent component
    loadingSetter(true);
    const ids = Object.values(selectedDataMap.value).map(item => item.id);
    const [error] = await script.deleteScript(ids);
    loadingSetter(false);

    if (error) {
      return;
    }
    notification.success(`选中的 ${num} 条脚本删除成功`);
    deleteCallback(ids);
    rowSelection.value.selectedRowKeys = [];
    selectedDataMap.value = {};
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
    loadingSetter(true);
    const [error] = await exec.addExecByScript({ scriptId: data.id });
    loadingSetter(false);
    if (error) {
      return;
    }
    notification.success('脚本添加执行成功，请在"执行"中查看详情');
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
    notification.success('脚本克隆成功');
    refreshCallback();
  };

  /**
   * Handle script delete
   */
  const handleDelete = async (data: ScriptInfo, loadingSetter: (loading: boolean) => void, deleteCallback: (ids: string[]) => void) => {
    const name = data.name;
    const source = data.source?.value;
    const content = source === 'SCENARIO' ? `删除脚本【${name}】同时也会删除关联场景，确定是否删除？` : `确定删除【${name}】脚本吗？`;

    // Confirmation would be handled in the parent component
    const id = data.id;
    loadingSetter(true);
    const [error] = await script.deleteScript([id]);
    loadingSetter(false);

    if (error) {
      return;
    }

    notification.success('脚本删除成功');
    deleteCallback([id]);
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
        router.push(`/script/edit/${data.id}?type=edit&pageNo=${data.pageNo}&pageSize=${data.pageSize}`);
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
    handleClone,
    handleDelete,
    handleExport,
    handleActionClick,

    // Computed properties
    selectedIds: selectedIds
  };
}
