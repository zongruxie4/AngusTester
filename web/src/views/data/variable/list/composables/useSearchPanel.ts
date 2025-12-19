import { ref, computed } from 'vue';
import { PageQuery, SearchCriteria, appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { createAuditOptions, createTimeOptions, type QuickSearchConfig } from 'src/components/form/quickSearch';

/**
 * Composable function for managing search panel functionality
 *
 * @returns Object containing search panel state and methods
 */
export function useSearchPanel (searchPanelRef?: any) {
  const { t } = useI18n();
  const userInfo = ref(appContext.getUser());

  // State management
  const selectedMenuMap = ref<{ [key: string]: boolean }>({});
  const orderBy = ref<string>();
  const orderSort = ref<PageQuery.OrderSort>();
  const SearchCriterias = ref<SearchCriteria[]>([]);
  const quickSearchCriterias = ref<SearchCriteria[]>([]);
  const assocFilters = ref<SearchCriteria[]>([]);

  // Constants
  const assocKeys = ['createdBy', 'modifiedBy', 'createdDate'];
  const establishedKeys = ['established=1', 'established=0'];

  /**
   * Button dropdown menu items for different extraction types
   */
  const buttonDropdownMenuItems = [
    {
      name: t('variable.actions.file'),
      key: 'file',
      noAuth: true
    },
    {
      name: t('variable.actions.http'),
      key: 'http',
      noAuth: true
    },
    {
      name: t('variable.actions.jdbc'),
      key: 'jdbc',
      noAuth: true
    }
  ];

  /**
   * Search panel configuration options
   */
  const searchPanelOptions = [
    {
      valueKey: 'name',
      type: 'input',
      placeholder: t('common.placeholders.searchKeyword'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      allowClear: true,
      placeholder: t('common.placeholders.selectCreator')
    },
    {
      valueKey: 'createdDate',
      type: 'date-range',
      allowClear: true,
      placeholder: [
        t('common.placeholders.selectCreatedDateRange.0'),
        t('common.placeholders.selectCreatedDateRange.1')
      ]
    }
  ];

  /**
   * Quick search configuration for data variable search panel
   * Provides predefined filter options for common searches
   */
  const quickSearchConfig = computed<QuickSearchConfig>(() => ({
    title: t('quickSearch.title'),
    // Audit information options
    auditOptions: createAuditOptions([
      {
        key: 'createdByMe',
        name: t('quickSearch.createdByMe'),
        fieldKey: 'createdBy'
      },
      {
        key: 'modifiedByMe',
        name: t('quickSearch.modifiedByMe'),
        fieldKey: 'modifiedBy'
      }
    ], String(userInfo.value?.id || '')),
    // Time options
    timeOptions: createTimeOptions([
      { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
      { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
      { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
    ], 'createdDate'),
    // External clear function
    externalClearFunction: () => {
      if (searchPanelRef && typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
      }
    }
  }));

  /**
   * Get current search parameters
   *
   * @returns Object containing current search configuration
   */
  const getCurrentParams = () => {
    return {
      filters: [
        ...quickSearchCriterias.value,
        ...SearchCriterias.value,
        ...assocFilters.value
      ],
      orderBy: orderBy.value,
      orderSort: orderSort.value
    };
  };

  /**
   * Update selected menu map based on current filters
   */
  const updateSelectedMenuMap = () => {
    // This function is kept for compatibility but simplified
    // Quick search logic is now handled by QuickSearchOptions component
  };

  /**
   * Update quick search filters based on selected menu items
   */
  const updateQuickSearchCriterias = () => {
    // This function is kept for compatibility but simplified
    // Quick search logic is now handled by QuickSearchOptions component
    return {
      quickSearchCriterias: quickSearchCriterias.value,
      assocFiltersInQuick: []
    };
  };

  return {
    // State
    selectedMenuMap,
    orderBy,
    orderSort,
    SearchCriterias,
    quickSearchCriterias,
    assocFilters,

    // Constants
    assocKeys,
    establishedKeys,

    // Configuration
    buttonDropdownMenuItems,
    searchPanelOptions,
    quickSearchConfig,

    // Methods
    getCurrentParams,
    updateSelectedMenuMap,
    updateQuickSearchCriterias
  };
}
