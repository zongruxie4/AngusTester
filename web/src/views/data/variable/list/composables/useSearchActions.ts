import { useRouter } from 'vue-router';
import type { SearchFilter } from '../types';

/**
 * Composable function for managing search panel actions
 *
 * @param searchPanelRef - Reference to the search panel component
 * @param selectedMenuMap - Map of selected menu items
 * @param timeKeys - Array of time-based menu keys
 * @param assocKeys - Array of associated filter keys
 * @param formatDateString - Function to format date strings
 * @param updateQuickSearchFilters - Function to update quick search filters
 * @param emits - Component emit function
 * @returns Object containing action methods
 */
export function useSearchActions (
  searchPanelRef: any,
  selectedMenuMap: { value: { [key: string]: boolean } },
  timeKeys: string[],
  assocKeys: string[],
  establishedKeys: string[],
  formatDateString: (key: string) => [string, string],
  updateQuickSearchFilters: () => { quickSearchFilters: SearchFilter[]; assocFiltersInQuick: any[] },
  emits: any
) {
  const router = useRouter();

  /**
   * Handle button dropdown clicks for different extraction types
   *
   * @param key - Extraction type key
   */
  const handleButtonDropdownClick = ({ key }: { key: 'file' | 'http' | 'jdbc' }) => {
    switch (key) {
      case 'file':
        router.push('/data#variables?source=FILE');
        break;
      case 'http':
        router.push('/data#variables?source=HTTP');
        break;
      case 'jdbc':
        router.push('/data#variables?source=JDBC');
        break;
    }
  };

  /**
   * Navigate to create static variable page
   */
  const navigateToCreateStaticVariable = () => {
    router.push('/data#variables?source=STATIC');
  };

  /**
   * Handle batch delete action
   */
  const handleBatchDelete = () => {
    emits('toBatchDelete');
  };

  /**
   * Handle import action
   */
  const handleImport = () => {
    emits('toImport');
  };

  /**
   * Handle export action
   */
  const handleExport = () => {
    emits('toExport');
  };

  /**
   * Handle cancel batch delete action
   */
  const handleCancelBatchDelete = () => {
    emits('toCancelBatchDelete');
  };

  /**
   * Handle refresh action
   */
  const handleRefresh = () => {
    emits('refresh');
  };

  /**
   * Handle menu item clicks for quick search
   *
   * @param data - Menu item data
   * @param userInfo - Current user information
   * @param searchFilters - Current search filters
   */
  const handleMenuItemClick = (
    data: { key: string },
    _userInfo: { id: string } | undefined,
    searchFilters: SearchFilter[]
  ) => {
    const key = data.key;
    let searchChangeFlag = false;

    if (selectedMenuMap.value[key]) {
      // Remove selection
      delete selectedMenuMap.value[key];

      if (timeKeys.includes(key) && assocKeys.includes('createdDate')) {
        if (searchPanelRef.value) {
          searchPanelRef.value.setConfigs([
            { valueKey: 'createdDate', value: undefined }
          ]);
        }
        searchChangeFlag = true;
      } else if (assocKeys.includes(key)) {
        if (searchPanelRef.value) {
          searchPanelRef.value.setConfigs([
            { valueKey: key, value: undefined }
          ]);
        }
        searchChangeFlag = true;
      }
    } else {
      // Add selection
      if (key === '') {
        // Select "All" - clear other selections
        selectedMenuMap.value = { '': true };

        if (searchPanelRef.value && typeof searchPanelRef.value?.clear === 'function') {
          searchPanelRef.value.clear();
          searchChangeFlag = true;
        }
      } else {
        delete selectedMenuMap.value[''];

        if (timeKeys.includes(key)) {
          // Time-based selection
          timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
          selectedMenuMap.value[key] = true;
        } else if (establishedKeys.includes(key)) {
          // Status-based selection
          establishedKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
          selectedMenuMap.value[key] = true;
        } else {
          selectedMenuMap.value[key] = true;
        }
      }
    }

    // Update quick search filters
    const { assocFiltersInQuick } = updateQuickSearchFilters();

    if (!searchChangeFlag && searchPanelRef.value) {
      if (assocFiltersInQuick.length) {
        searchPanelRef.value.setConfigs(assocFiltersInQuick);
      }
      emits('change', { filters: searchFilters });
    }
  };

  /**
   * Handle sort changes
   *
   * @param sortData - Sort configuration data
   * @param orderBy - Reference to orderBy state
   * @param orderSort - Reference to orderSort state
   */
  const handleSortChange = (
    sortData: { orderBy: string; orderSort: 'ASC' | 'DESC' },
    orderBy: { value: string | undefined },
    orderSort: { value: 'ASC' | 'DESC' | undefined }
  ) => {
    orderBy.value = sortData.orderBy;
    orderSort.value = sortData.orderSort;
    emits('change', sortData);
  };

  /**
   * Handle search panel changes
   *
   * @param data - Search panel data with filters
   * @param assocKeys - Array of associated filter keys
   * @param searchFilters - Reference to search filters state
   * @param assocFilters - Reference to associated filters state
   * @param updateSelectedMenuMap - Function to update selected menu map
   */
  const handleSearchPanelChange = (
    data: { filters: SearchFilter[] },
    assocKeys: string[],
    searchFilters: { value: SearchFilter[] },
    assocFilters: { value: SearchFilter[] },
    updateSelectedMenuMap: () => void
  ) => {
    searchFilters.value = data.filters.filter(item => !assocKeys.includes(item.key));
    assocFilters.value = data.filters.filter(item => assocKeys.includes(item.key));

    // Update selected menu map based on filters
    updateSelectedMenuMap();

    emits('change', data);
  };

  return {
    // Navigation methods
    handleButtonDropdownClick,
    navigateToCreateStaticVariable,

    // Action methods
    handleBatchDelete,
    handleImport,
    handleExport,
    handleCancelBatchDelete,
    handleRefresh,

    // Menu and search methods
    handleMenuItemClick,
    handleSortChange,
    handleSearchPanelChange
  };
}
