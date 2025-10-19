import { ref } from 'vue';
import { PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import type { OrderByKey } from '../../types';

type FilterItem = SearchCriteria;
type SearchPanelParams = PageQuery;

export function useSearchPanelAction (
  searchPanelRef: any,
  userId: string,
  searchFilters: any,
  assocFilters: any,
  quickSearchFilters: any,
  orderBy: any,
  orderSort: any,
  getParams: () => SearchPanelParams,
  emit: (value: SearchPanelParams) => void,
  emitRefresh: () => void
) {
  const { t } = useI18n();

  // Filter configuration
  const assocKeys = ['createdDate', 'createdBy'];

  // Quick search options ref
  const quickSearchOptionsRef = ref();

  /**
   * Handle search panel change event
   * @param data - Filter items from search panel
   */
  const searchChange = (data: FilterItem[], _headers?: { [key: string]: string }, key?: string): void => {
    if (key === 'createdBy') {
      const searchCriteriaKeys = quickSearchOptionsRef.value.getSearchCriteria().map(f => f.key);
      if (searchCriteriaKeys.includes('createdBy')) {
        quickSearchOptionsRef.value.clearSelectedMap('createdBy');
      }
      quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'createdBy');
    }
    if (key === 'createdDate') {
      const searchCriteriaKeys = quickSearchOptionsRef.value.getSearchCriteria().map(f => f.key);
      if (searchCriteriaKeys.includes('createdDate')) {
        quickSearchOptionsRef.value.clearSelectedMap(['createdDate', 'last1Day', 'last3Days', 'last7Days']);
      }
      quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'createdDate');
    }

    searchFilters.value = [...(data || []).filter(f => !assocKeys.includes(f.key as string))];
    assocFilters.value = data.filter(item => item.key && assocKeys.includes(item.key as string));

    emit(getParams());
  };

  /**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
  const handleQuickSearchChange = (selectedKeys: string[], searchCriteria: SearchCriteria[], key?: string): void => {
  // Update quick search filters
    if (key === 'createdBy') {
      if (selectedKeys.includes(key)) {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{
            valueKey: 'createdBy',
            value: userId
          }]);
        }
      } else {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{
            valueKey: 'createdBy',
            value: undefined
          }]);
        }
      }
      searchCriteria = searchCriteria.filter(f => f.key !== 'createdBy');
    }

    if (key && key.startsWith('last') && (key.endsWith('Day') && key.endsWith('Days'))) {
      if (selectedKeys.includes(key)) {
        const createdDataSearchCriteria = searchCriteria.filter(f => f.key === 'createdDate');
        const createdDataValue = [createdDataSearchCriteria[0].value, createdDataSearchCriteria[1].value];

        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{
            valueKey: 'createdDate',
            value: createdDataValue,
            type: 'date-range'
          }]);
        }
      } else {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{
            valueKey: 'createdDate',
            value: undefined,
            type: 'date-range'
          }]);
        }
      }
      searchCriteria = searchCriteria.filter(f => f.key !== 'createdDate');
    }
    quickSearchFilters.value = searchCriteria;

    // Emit change event with current params
    emit(getParams());
  };

  /**
   * Handle sort change event
   * @param sortData - Sort data with orderBy and orderSort
   */
  const toSort = (sortData: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }): void => {
    orderBy.value = sortData.orderBy;
    orderSort.value = sortData.orderSort;
    emit(getParams());
  };

  /**
   * Refresh search panel
   */
  const refresh = (): void => {
    emitRefresh();
  };

  return {
    searchChange,
    toSort,
    refresh,
    quickSearchOptionsRef,
    handleQuickSearchChange
  };
}
