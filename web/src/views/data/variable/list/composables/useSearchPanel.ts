import { computed, ref } from 'vue';
import { appContext } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import type { SearchFilter } from '../types';

/**
 * Composable function for managing search panel functionality
 *
 * @returns Object containing search panel state and methods
 */
export function useSearchPanel () {
  const { t } = useI18n();

  // User context
  const userInfo = ref(appContext.getUser());

  // State management
  const selectedMenuMap = ref<{ [key: string]: boolean }>({});
  const orderBy = ref<string>();
  const orderSort = ref<'ASC' | 'DESC'>();
  const searchFilters = ref<SearchFilter[]>([]);
  const quickSearchFilters = ref<SearchFilter[]>([]);
  const assocFilters = ref<SearchFilter[]>([]);

  // Constants
  const assocKeys = ['createdBy', 'lastModifiedBy', 'createdDate'];
  const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];
  const establishedKeys = ['established=1', 'established=0'];

  /**
   * Button dropdown menu items for different extraction types
   */
  const buttonDropdownMenuItems = [
    {
      name: t('dataVariable.list.searchPanel.dropdown.fileExtract'),
      key: 'file',
      noAuth: true
    },
    {
      name: t('dataVariable.list.searchPanel.dropdown.httpExtract'),
      key: 'http',
      noAuth: true
    },
    {
      name: t('dataVariable.list.searchPanel.dropdown.jdbcExtract'),
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
      placeholder: t('dataVariable.list.searchPanel.searchOptions.namePlaceholder'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      allowClear: true,
      placeholder: t('dataVariable.list.searchPanel.searchOptions.createdByPlaceholder')
    },
    {
      valueKey: 'createdDate',
      type: 'date-range',
      allowClear: true
    }
  ];

  /**
   * Quick search menu items
   */
  const menuItems = computed(() => [
    {
      key: '',
      name: t('common.all')
    },
    {
      key: 'createdBy',
      name: t('quickSearch.createdByMe')
    },
    {
      key: 'lastModifiedBy',
      name: t('quickSearch.modifiedByMe')
    },
    {
      key: 'lastDay',
      name: t('quickSearch.last1Day')
    },
    {
      key: 'lastThreeDays',
      name: t('dataVariable.list.searchPanel.lastThreeDays')
    },
    {
      key: 'lastWeek',
      name: t('dataVariable.list.searchPanel.lastWeek')
    }
  ]);

  /**
   * Format date string based on time key
   *
   * @param key - Time key identifier
   * @returns Array of start and end date strings
   */
  const formatDateString = (key: string): [string, string] => {
    let startDate: Dayjs | undefined;
    let endDate: Dayjs | undefined;

    switch (key) {
      case 'lastDay':
        startDate = dayjs().startOf('date');
        endDate = dayjs();
        break;
      case 'lastThreeDays':
        startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
        endDate = dayjs();
        break;
      case 'lastWeek':
        startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
        endDate = dayjs();
        break;
    }

    return [
      startDate ? startDate.format(DATE_TIME_FORMAT) : '',
      endDate ? endDate.format(DATE_TIME_FORMAT) : ''
    ];
  };

  /**
   * Get current search parameters
   *
   * @returns Object containing current search configuration
   */
  const getCurrentParams = () => {
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
   * Update selected menu map based on current filters
   */
  const updateSelectedMenuMap = () => {
    if (!assocFilters.value.length) {
      // Clear all associated keys
      assocKeys.forEach(key => {
        if (key === 'createdDate') {
          timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
        } else {
          delete selectedMenuMap.value[key];
        }
      });
    } else {
      // Update selected menu map based on filters
      assocKeys.forEach(key => {
        if (key === 'createdBy' || key === 'lastModifiedBy') {
          const filterItem = assocFilters.value.find(item => item.key === key);
          if (!filterItem || filterItem.value !== String(userInfo.value?.id)) {
            delete selectedMenuMap.value[key];
          }
        }
        if (key === 'createdDate') {
          const filterItem = assocFilters.value.filter(item => item.key === key);
          const timeKey = timeKeys.find(t => selectedMenuMap.value[t]);
          if (timeKey && filterItem.length > 0) {
            const timeValue = formatDateString(timeKey);
            if (timeValue[0] !== filterItem[0].value || timeValue[1] !== filterItem[1].value) {
              delete selectedMenuMap.value[timeKey];
            }
          }
        }
      });
    }
  };

  /**
   * Update quick search filters based on selected menu items
   */
  const updateQuickSearchFilters = () => {
    const userId = userInfo.value?.id;
    const timeFilters: SearchFilter[] = [];
    const assocFiltersInQuick: any[] = [];

    const mappedFilters = Object.keys(selectedMenuMap.value).map(key => {
      if (key === '') {
        return undefined;
      } else if (timeKeys.includes(key)) {
        assocFiltersInQuick.push({ valueKey: 'createdDate', value: formatDateString(key) });
        return undefined;
      } else if (assocKeys.includes(key)) {
        if (key === 'createdBy' || key === 'lastModifiedBy') {
          assocFiltersInQuick.push({ valueKey: key, value: String(userId) });
        }
        return undefined;
      } else {
        return {
          key,
          op: 'EQUAL',
          value: String(userId) || ''
        };
      }
    }).filter((item): item is { key: string; op: string; value: string } => item !== undefined);

    quickSearchFilters.value = mappedFilters as SearchFilter[];

    quickSearchFilters.value.push(...timeFilters);

    return {
      quickSearchFilters: quickSearchFilters.value,
      assocFiltersInQuick
    };
  };

  return {
    // State
    userInfo,
    selectedMenuMap,
    orderBy,
    orderSort,
    searchFilters,
    quickSearchFilters,
    assocFilters,

    // Constants
    assocKeys,
    timeKeys,
    establishedKeys,

    // Computed
    menuItems,

    // Configuration
    buttonDropdownMenuItems,
    searchPanelOptions,

    // Methods
    formatDateString,
    getCurrentParams,
    updateSelectedMenuMap,
    updateQuickSearchFilters
  };
}
