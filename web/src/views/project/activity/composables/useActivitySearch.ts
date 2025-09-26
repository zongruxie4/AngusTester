import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import dayjs, { Dayjs } from 'dayjs';
import { TESTER, appContext, CombinedTargetType } from '@xcan-angus/infra';
import type { SearchFilter, QuickSearchItem } from '../types';
import { DATE_TIME_FORMAT } from '@/utils/constant';

/**
 * Composable for managing activity search functionality
 * Handles search panel configuration, quick search menus, and filter management
 *
 * @returns Object containing search configuration and management functions
 */
export function useActivitySearch () {
  const { t } = useI18n();
  const userInfo = ref(appContext.getUser());

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

  // Quick search menu items
  const menuItems = computed<QuickSearchItem[]>(() => [
    {
      key: '',
      name: t('common.all')
    },
    {
      key: 'userId',
      name: t('quickSearch.myActivity')
    },
    {
      key: 'lastDay',
      name: t('quickSearch.last1Day')
    },
    {
      key: 'lastThreeDays',
      name: t('quickSearch.last3Days')
    },
    {
      key: 'lastWeek',
      name: t('quickSearch.last7Days')
    }
  ]);

  // Search state management
  const selectedMenuMap = ref<{[key: string]: boolean}>({});
  const orderBy = ref<string>();
  const orderSort = ref<'ASC' | 'DESC'>();
  const searchFilters = ref<SearchFilter[]>([]);
  const quickSearchFilters = ref<SearchFilter[]>([]);
  const assocFilters = ref<SearchFilter[]>([]);

  // Constants for filter management
  const assocKeys = ['userId', 'optDate'];
  const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

  /**
   * Format date string for time-based quick searches
   *
   * @param key - Time key ('lastDay', 'lastThreeDays', 'lastWeek')
   * @returns Array with start and end date strings
   */
  const formatDateString = (key: string): [string, string] => {
    let startDate: Dayjs | undefined;
    let endDate: Dayjs | undefined;

    if (key === 'lastDay') {
      startDate = dayjs().startOf('date');
      endDate = dayjs();
    }

    if (key === 'lastThreeDays') {
      startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
      endDate = dayjs();
    }

    if (key === 'lastWeek') {
      startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
      endDate = dayjs();
    }

    return [
      startDate ? startDate.format(DATE_TIME_FORMAT) : '',
      endDate ? endDate.format(DATE_TIME_FORMAT) : ''
    ];
  };

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
  const handleSearchChange = (data: SearchFilter[]) => {
    searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
    assocFilters.value = data.filter(item => assocKeys.includes(item.key));

    if (!assocFilters.value.length) {
      assocKeys.forEach(i => {
        if (i === 'optDate') {
          timeKeys.forEach(t => delete selectedMenuMap.value[t]);
        } else {
          delete selectedMenuMap.value[i];
        }
      });
    } else {
      assocKeys.forEach(key => {
        if (key === 'userId') {
          const filterItem = assocFilters.value.find(i => i.key === key);
          if (!filterItem || filterItem.value !== userInfo.value?.id) {
            delete selectedMenuMap.value[key];
          }
        }
        if (key === 'optDate') {
          const filterItem = assocFilters.value.filter(i => i.key === key);
          const timeKey = timeKeys.find(t => selectedMenuMap.value[t]);
          if (timeKey) {
            const timeValue = formatDateString(timeKey);
            if (timeValue[0] !== filterItem[0]?.value || timeValue[1] !== filterItem[1]?.value) {
              delete selectedMenuMap.value[timeKey];
            }
          }
        }
      });
    }
  };

  /**
   * Handle quick search menu item clicks
   * Updates selected menu items and applies associated filters
   *
   * @param data - Menu item data
   */
  const handleMenuItemClick = (data: QuickSearchItem) => {
    const key = data.key;
    let searchChangeFlag = false;

    if (selectedMenuMap.value[key]) {
      delete selectedMenuMap.value[key];
      if (timeKeys.includes(key) && assocKeys.includes('createdDate')) {
        searchChangeFlag = true;
      } else if (assocKeys.includes(key)) {
        searchChangeFlag = true;
      }
    } else {
      if (key === '') {
        selectedMenuMap.value = { '': true };
        quickSearchFilters.value = [];
        searchChangeFlag = true;
      } else {
        delete selectedMenuMap.value[''];
      }
      if (timeKeys.includes(key)) {
        timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
        selectedMenuMap.value[key] = true;
      } else {
        selectedMenuMap.value[key] = true;
      }
    }

    const userId = userInfo.value?.id;
    const assocFiltersInQuick: Array<{valueKey: string; value: string | [string, string]}> = [];

    quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
      if (key === '') {
        return undefined;
      } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
        assocFiltersInQuick.push({ valueKey: 'optDate', value: formatDateString(key) });
        return undefined;
      } else if (assocKeys.includes(key)) {
        if (key === 'userId') {
          assocFiltersInQuick.push({ valueKey: key, value: userId || '' });
        }
        return undefined;
      } else {
        return {
          key,
          op: 'EQUAL',
          value: userId || ''
        };
      }
    }).filter((item): item is SearchFilter => Boolean(item));

    if (assocFiltersInQuick.length) {
      searchChangeFlag = true;
    }
  };

  /**
   * Clear all search filters and reset to default state
   */
  const clearAllFilters = () => {
    selectedMenuMap.value = { '': true };
    quickSearchFilters.value = [];
    searchFilters.value = [];
    assocFilters.value = [];
  };

  return {
    // Search configuration
    searchPanelOptions,
    menuItems,

    // Search state
    selectedMenuMap,
    orderBy,
    orderSort,
    searchFilters,
    quickSearchFilters,
    assocFilters,

    // Search management methods
    formatDateString,
    buildSearchParams,
    handleSearchChange,
    handleMenuItemClick,
    clearAllFilters
  };
}
