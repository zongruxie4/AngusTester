import { ref, computed } from 'vue';
import { MockAPIConfig, MockAPIInfo } from '../types';
import { utils } from '@xcan-angus/infra';

/**
 * Composable for managing Mock API data operations
 * Handles API list, data mapping, permissions and CRUD operations
 */
export function useMockApiData () {
  // API list management
  const apiIds = ref<string[]>([]);
  const apiDataMap = ref<{ [key: string]: MockAPIConfig }>({});
  const permissionMap = ref<Map<string, string[]>>(new Map());

  // Search and sorting
  const inputValue = ref<string>();
  const orderBy = ref<'createdDate' | 'id'>('createdDate');
  const orderSort = ref<'ASC' | 'DESC'>('DESC');

  /**
   * Get computed params for API requests
   * @param mockServiceId - Service ID to include in params
   * @returns Computed parameters object
   */
  const getParams = (mockServiceId: string) => {
    const _params: {
      mockServiceId: string;
      filters?: [{ key: 'summary'; op: 'MATCH_END', value: string }];
      orderBy?: string;
      orderSort?: 'ASC' | 'DESC';
    } = {
      mockServiceId
    };

    if (inputValue.value) {
      _params.filters = [{ key: 'summary', op: 'MATCH_END', value: inputValue.value }];
    }

    if (orderBy.value) {
      _params.orderBy = orderBy.value;
    }

    if (orderSort.value) {
      _params.orderSort = orderSort.value;
    }

    return _params;
  };

  /**
   * Add a new temporary Mock API to the list
   * @param mockServiceId - The service ID to associate with
   * @returns The newly created API ID
   */
  const addMockApi = (mockServiceId: string): string => {
    const id = utils.uuid();
    const data: MockAPIConfig = {
      id,
      summary: 'MockAPI-' + Date.now(),
      isTempFlag: true,
      mockServiceId,
      method: 'GET',
      endpoint: '',
      description: ''
    };

    apiIds.value.unshift(id);
    apiDataMap.value[id] = data;
    permissionMap.value.set(id, [MockServicePermission.DELETE]);

    return id;
  };

  /**
   * Update API list from server response
   * @param dataList - Array of API info from server
   */
  const updateApiList = (dataList: MockAPIInfo[]) => {
    apiIds.value = [];
    apiDataMap.value = {};

    for (let i = 0, len = dataList.length; i < len; i++) {
      const data = dataList[i];
      const { id, method, isTempFlag } = data;

      apiIds.value.push(id);
      apiDataMap.value[id] = {
        ...data,
        isTempFlag,
        method: method.value
      };
      permissionMap.value.set(id, ['CLONE', MockServicePermission.DELETE, MockServicePermission.EXPORT]);
    }
  };

  /**
   * Remove API from list and data maps
   * @param id - API ID to remove
   */
  const removeApi = (id: string) => {
    const index = apiIds.value.findIndex(item => item === id);
    if (index > -1) {
      apiIds.value.splice(index, 1);
    }
    delete apiDataMap.value[id];
    permissionMap.value.delete(id);
  };

  /**
   * Update search input value with debounce
   * @param value - New search value
   */
  const updateSearchValue = (value: string) => {
    inputValue.value = value.trim();
  };

  /**
   * Update sorting parameters
   * @param orderByValue - Field to sort by
   * @param orderSortValue - Sort direction
   */
  const updateSorting = (orderByValue: 'createdDate' | 'id', orderSortValue: 'ASC' | 'DESC') => {
    orderBy.value = orderByValue;
    orderSort.value = orderSortValue;
  };

  return {
    // State
    apiIds,
    apiDataMap,
    permissionMap,
    inputValue,
    orderBy,
    orderSort,

    // Methods
    getParams,
    addMockApi,
    updateApiList,
    removeApi,
    updateSearchValue,
    updateSorting
  };
}
