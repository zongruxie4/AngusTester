import { PageQuery, SearchCriteria } from '@xcan-angus/infra';

import type { OrderByKey } from '../../types';

type FilterItem = SearchCriteria;
type SearchPanelParams = PageQuery;

export function useSearchPanelAction (
  searchPanelRef: any,
  _selectedMenuMap: any,
  searchFilters: any,
  assocFilters: any,
  quickSearchFilters: any,
  orderBy: any,
  orderSort: any,
  getParams: () => SearchPanelParams,
  emit: (value: SearchPanelParams) => void,
  emitRefresh: () => void
) {
  // Filter configuration
  const assocKeys = ['createdDate', 'createdBy'];

  /**
   * Handle search panel change event
   * @param data - Filter items from search panel
   */
  const searchChange = (data: FilterItem[]): void => {
    // Merge search panel filters with quick search filters
    const quickSearchFields = ['createdBy', 'lastModifiedBy', 'createdDate', 'status'];
    const currentQuickSearchFilters = quickSearchFilters.value.filter(f => f.key && quickSearchFields.includes(f.key as string));
    const searchPanelFilters = data.filter(f => f.key && !quickSearchFields.includes(f.key as string));

    searchFilters.value = [...currentQuickSearchFilters, ...searchPanelFilters];
    assocFilters.value = data.filter(item => item.key && assocKeys.includes(item.key));

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
    refresh
  };
}
