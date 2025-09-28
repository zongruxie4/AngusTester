import { reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { PageQuery } from '@xcan-angus/infra';

import { NodeData, NodeListResponse, PaginationConfig } from '../types';
import { node } from '@/api/tester';
import { pureParams } from '@/utils/common';

/**
 * Composable for managing node data operations
 *
 * @returns Object containing node data management functions and state
 */
export function useNodeData () {
  const { t } = useI18n();

  // Reactive state for node list
  const nodeList = reactive<{ list: NodeData[] }>({
    list: []
  });

  // Loading state
  const loading = ref(false);

  // Pagination configuration
  const pagination = reactive<PaginationConfig>({
    current: 1,
    pageSize: 5,
    showTotal: (total: number) => t('node.messages.totalCount', { total }),
    showSizeChanger: true,
    total: 0
  });

  // Search parameters
  const searchParams = reactive<PageQuery>({
    filters: []
  });

  /**
   * Loads the node list from the API
   *
   * <p>Fetches nodes based on current search parameters and pagination settings.
   * Transforms the response data to include additional display properties.</p>
   */
  const loadNodeList = async () => {
    const { pageSize, current } = pagination;
    const param = pureParams({
      ...searchParams,
      pageSize,
      pageNo: current
    });

    loading.value = true;

    try {
      const [error, res] = await node.getNodeList(param);
      if (error) {
        return;
      }

      // Transform node data for display
      const transformedList = (res.data as NodeListResponse).list.map((item: any) => {
        let standard = '';
        if (item.spec) {
          standard = item.spec.showLabel || '';
        }

        return {
          ...item,
          editable: false,
          sourceName: item.source?.message || '',
          spec: { ...item.spec },
          standard,
          monitorData: {}
        };
      });

      nodeList.list = transformedList;
      pagination.total = +(res.data as NodeListResponse).total;
    } catch (error) {
      console.error('Failed to load node list:', error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * Handles page change events
   *
   * @param page - New page number
   */
  const handlePageChange = (page: number) => {
    pagination.current = page;
    loadNodeList();
  };

  /**
   * Handles search form changes
   *
   * @param filters - New search filters
   */
  const handleSearchChange = (filters: PageQuery['filters']) => {
    searchParams.filters = filters;
    pagination.current = 1; // Reset to first page
    loadNodeList();
  };

  /**
   * Handles sorting changes
   *
   * @param sortEvent - Sort change event with orderBy and orderSort
   */
  const handleSortChange = (sortEvent: { orderBy: string; orderSort: string }) => {
    searchParams.orderBy = sortEvent.orderBy;
    searchParams.orderSort = sortEvent.orderSort;
    loadNodeList();
  };

  /**
   * Refreshes the node list
   */
  const refreshNodeList = async () => {
    await loadNodeList();
  };

  /**
   * Handles item deletion (placeholder for future implementation)
   */
  const deleteItem = (): void => {
    // TODO: Implement item deletion logic
    console.log('Delete item functionality not yet implemented');
  };

  return {
    // State
    nodeList,
    loading,
    pagination,
    searchParams,

    // Methods
    loadNodeList,
    handlePageChange,
    handleSearchChange,
    handleSortChange,
    refreshNodeList,
    deleteItem
  };
}
