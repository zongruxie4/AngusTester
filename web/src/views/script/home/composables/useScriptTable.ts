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