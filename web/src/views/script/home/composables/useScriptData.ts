import { ref } from 'vue';
import { script, analysis } from '@/api/tester';
import { ScriptInfo, ResourceInfo } from '../types';
import { utils } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';

/**
 * Composable for managing script data including list, permissions and resource counts
 * @param projectId - The ID of the current project
 * @param isAdmin - Whether the current user is an admin
 */
export function useScriptData(projectId: string, isAdmin: boolean) {
  // Script list data
  const tableData = ref<ScriptInfo[]>([]);
  const permissionsMap = ref<{ [key: string]: string[] }>({});
  const loading = ref(false);
  const loaded = ref(false);
  
  // Pagination state
  const pagination = ref<{ current: number; pageSize: number; total: number; }>({ 
    current: 1, 
    pageSize: 10, 
    total: 0 
  });
  
  // Sorting state
  const orderBy = ref<string>();
  const orderSort = ref<'DESC' | 'ASC'>();
  
  // Filter state
  const filters = ref<{ key: string; op: string; value: boolean | string | string[]; }[]>([]);
  
  // Resource count data
  const countData = ref<ResourceInfo>({
    totalScriptNum: '0',
    perfScriptNum: '0',
    functionalScriptNum: '0',
    stabilityScriptNum: '0',
    customizationScriptNum: '0',
    mockDataScriptNum: '0',
    mockApisScriptNum: '0',
    createdSourceNum: '0',
    importedSourceNum: '0',
    apisSourceNum: '0',
    caseSourceNum: '0',
    scenarioSourceNum: '0'
  });
  
  // Previous parameters for comparison
  const prevParams = ref<{ [key: string]: any }>();
  const resetSelectedIdsNotify = ref<string>();
  
  // Import samples flag
  const allowImportSamplesFlag = ref(false);

  /**
   * Get parameters for API requests
   */
  const getParams = () => {
    const params: {
      projectId: string;
      pageNo: number;
      pageSize: number;
      filters?: { key: string; op: string; value: boolean | string | string[]; }[];
      orderBy?: string;
      orderSort?: 'DESC' | 'ASC';
    } = {
      projectId: projectId,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize
    };

    if (filters.value.length) {
      params.filters = filters.value;
    }

    if (orderSort.value) {
      params.orderBy = orderBy.value;
      params.orderSort = orderSort.value;
    }

    return params;
  };

  /**
   * Load script list from API
   */
  const loadScriptList = async () => {
    loading.value = true;
    const params = getParams();
    const [error, res] = await script.getScriptList(params);
    loaded.value = true;
    loading.value = false;
    
    if (error) {
      return false;
    }

    // Check if search parameters have changed to reset selected items
    if (prevParams.value) {
      const prevParamsCopy = { ...prevParams.value };
      const paramsCopy = { ...params };
      delete prevParamsCopy.pageNo;
      delete paramsCopy.pageNo;
      if (!isEqual(prevParamsCopy, paramsCopy)) {
        resetSelectedIdsNotify.value = utils.uuid();
        permissionsMap.value = {};
      }
    }

    prevParams.value = params;

    const data = (res?.data || { total: '0', list: [], ext: {} }) as {
      total: string;
      list: ScriptInfo[];
      ext: { allowImportSamples: boolean; }
    };
    
    pagination.value.total = +data.total;
    tableData.value = data.list.map(item => {
      let sourceNameLinkUrl = '';
      const source = item.source?.value;
      const sourceId = item.sourceId;
      const sourceName = item.sourceName;
      
      if (sourceId && sourceName) {
        if (source === 'SERVICE_SMOKE') {
          sourceNameLinkUrl = `/apis#services?id=${sourceId}&name=${sourceName}&value=group`;
        } else if (source === 'SERVICE_SECURITY') {
          sourceNameLinkUrl = `/apis#services?id=${sourceId}&name=${sourceName}&value=group`;
        } else if (source === 'API') {
          sourceNameLinkUrl = `/apis#services?id=${sourceId}&name=${sourceName}&value=API`;
        } else if (source === 'SCENARIO') {
          sourceNameLinkUrl = `/scenario#scenario?id=${sourceId}&name=${sourceName}&plugin=${item.plugin}`;
        }
      }

      return {
        ...item,
        sourceNameLinkUrl,
        nameLinkUrl: `/script/detail/${item.id}?type=view&pageNo=${pagination.value.current}&pageSize=${pagination.value.pageSize}`
      };
    });

    const allowImportSamples = data.ext?.allowImportSamples;
    if (typeof data.ext?.allowImportSamples === 'boolean') {
      allowImportSamplesFlag.value = allowImportSamples;
    }

    const ids = data.list.map(item => item.id);
    await loadScriptListAuth(ids);
    
    return true;
  };

  /**
   * Load script permissions for current user
   */
  const loadScriptListAuth = async (ids: string[]) => {
    if (!ids.length) {
      return;
    }

    const [error, res] = await script.getScriptCurrentAuth(ids);
    if (error) {
      return false;
    }

    const map: {
      [key: string]: { permissions: { value: string; message: string }[]; scriptAuth: boolean; }
    } = res?.data;
    
    if (map) {
      for (const key in map) {
        const { scriptAuth, permissions } = map[key];
        let list: string[] = [];
        const values = permissions.map(item => item.value);
        
        if (isAdmin || scriptAuth === false) {
          list = ['TEST', 'VIEW', 'MODIFY', 'DELETE', 'EXPORT', 'COLON'];
          if (values.includes('GRANT')) {
            list.push('GRANT');
          }
        } else {
          list = [...values];
          if (values.includes('VIEW') || !values.includes('COLON')) {
            list.push('COLON');
          }
        }

        permissionsMap.value[key] = list;
      }
    }
  };

  /**
   * Load resource count data
   */
  const loadResourcesData = async () => {
    const params = { filters: filters.value, projectId: projectId };
    const [error, res] = await analysis.getScriptCount(params);
    if (error) {
      return false;
    }

    countData.value = res?.data || {
      totalScriptNum: '0',
      perfScriptNum: '0',
      functionalScriptNum: '0',
      stabilityScriptNum: '0',
      customizationScriptNum: '0',
      mockDataScriptNum: '0',
      mockApisScriptNum: '0',
      createdSourceNum: '0',
      importedSourceNum: '0',
      apisSourceNum: '0',
      caseSourceNum: '0',
      scenarioSourceNum: '0'
    };
    
    return true;
  };

  /**
   * Handle table change events (pagination, sorting)
   */
  const handleTableChange = (
    { current, pageSize }: { current: number; pageSize: number; }, 
    sorter: { orderBy: string; orderSort: 'DESC' | 'ASC' }
  ) => {
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;

    orderBy.value = sorter.orderBy;
    orderSort.value = sorter.orderSort;

    loadScriptList();
    loadResourcesData();
  };

  /**
   * Handle delete operations and pagination adjustments
   */
  const handleDelete = (ids: string[]) => {
    const { current, pageSize, total } = pagination.value;
    const totalPage = Math.ceil(total / pageSize);
    const remainder = total % pageSize;

    const deleteNum = ids.length;
    const deletePages = Math.floor(deleteNum / pageSize);
    const deleteRemainder = deleteNum % pageSize;

    if ((deleteRemainder === 0 || remainder === 0) || (deleteRemainder < remainder)) {
      if (current + deletePages <= totalPage) {
        loadScriptList();
        loadResourcesData();
        return;
      }

      pagination.value.current = current - (current + deletePages - totalPage) || 1;
      loadScriptList();
      loadResourcesData();
      return;
    }

    if (deleteRemainder >= remainder) {
      if (current + deletePages + 1 <= totalPage) {
        loadScriptList();
        loadResourcesData();
        return;
      }

      pagination.value.current = current - (current + deletePages - totalPage) - 1 || 1;
      loadScriptList();
      loadResourcesData();
    }
  };

  return {
    // Reactive data
    tableData,
    permissionsMap,
    loading,
    loaded,
    pagination,
    countData,
    resetSelectedIdsNotify,
    allowImportSamplesFlag,
    
    // Methods
    loadScriptList,
    loadResourcesData,
    handleTableChange,
    handleDelete,
    
    // Computed
    filters
  };
}