import { ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TESTER, PageQuery, SearchCriteria, CombinedTargetType } from '@xcan-angus/infra';

/**
 * Composable for managing activity search functionality
 * Handles search panel configuration, quick search menus, and filter management
 *
 * @returns Object containing search configuration and management functions
 */
export function useActivitySearch (quickSearchOptionsRef: Ref) {
  const { t } = useI18n();

  // Search panel configuration
  const searchPanelOptions = [
    {
      valueKey: 'detail',
      type: 'input',
      placeholder: t('activity.searchPanelOptions.queryActivityDetail'),
      allowClear: true,
      trim: true,
      maxlength: 100
    },
    {
      valueKey: 'targetType',
      type: 'select-enum',
      enumKey: CombinedTargetType,
      placeholder: t('activity.searchPanelOptions.selectActivityResource'),
      allowClear: true
    },
    {
      valueKey: 'projectId',
      type: 'select',
      placeholder: t('common.placeholders.selectProject'),
      allowClear: true,
      action: `${TESTER}/project?fullTextSearch=true`,
      fieldNames: {
        value: 'id',
        label: 'name'
      }
    },
    {
      valueKey: 'userId',
      type: 'select-user',
      placeholder: t('activity.searchPanelOptions.selectOperator'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'optDate',
      type: 'date-range',
      placeholder: [
        t('activity.searchPanelOptions.activityTimeFrom'),
        t('activity.searchPanelOptions.activityTimeTo')
      ],
      allowClear: true
    }
  ];

  // Search state management
  const orderBy = ref<string>();
  const orderSort = ref<PageQuery.OrderSort>();
  const searchFilters = ref<SearchCriteria[]>([]);
  const quickSearchFilters = ref<SearchCriteria[]>([]);
  const assocFilters = ref<SearchCriteria[]>([]);

  // Constants for filter management
  const assocKeys = ['userId', 'optDate'];

  /**
   * Build search parameters from all filter sources
   *
   * @returns Object containing all search parameters
   */
  const buildSearchParams = () => {
    return {
      filters: [
        ...quickSearchFilters.value,
        ...searchFilters.value,
        ...assocFilters.value
      ],
      orderBy: orderBy.value,
      orderSort: orderSort.value
    };
  };

  /**
   * Handle search panel change events
   * Updates search filters and associated filters
   *
   * @param data - New filter data from search panel
   */
  const handleSearchChange = (data: SearchCriteria[], _headers?: { [key: string]: string }, _changedKey?: string) => {
    if (_changedKey === 'userId') {
      quickSearchOptionsRef.value.clearSelectedMap(['myActivity']);
    }
    if (_changedKey === 'optDate') {
      quickSearchOptionsRef.value.clearSelectedMap(['last1Day', 'last3Days', 'last7Days']);
    }
    searchFilters.value = data.filter(item => !assocKeys.includes(item.key as string));
    assocFilters.value = data.filter(item => assocKeys.includes(item.key as string));
  };

  /**
   * Clear all search filters and reset to default state
   */
  const clearAllFilters = () => {
    quickSearchFilters.value = [];
    searchFilters.value = [];
    assocFilters.value = [];
  };

  return {
    // Search configuration
    searchPanelOptions,

    // Search state
    orderBy,
    orderSort,
    searchFilters,
    quickSearchFilters,
    assocFilters,

    // Search management methods
    buildSearchParams,
    handleSearchChange,
    clearAllFilters
  };
}
