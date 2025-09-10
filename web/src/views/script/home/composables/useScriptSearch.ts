import { ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ScriptType, enumUtils } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep } from 'lodash-es';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { MenuItem } from '@/views/script/types';

/**
 * Composable for managing search functionality in script home
 * @param projectId - The ID of the current project
 * @param userId - The ID of the current user
 */
export function useScriptSearch (projectId: string, userId: string) {
  const { t } = useI18n();

  // Search panel reference
  const searchPanelRef = ref();

  // Quick date selection map
  const quickDateMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
  const typeDataMap = ref<Map<string, string>>(new Map());
  const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());

  // Filters
  const filters = ref<{ key: string; op: string; value: string; }[]>([]);
  const serviceIdFilter = ref<{ key: 'serviceId', op: 'EQUAL', value: string | undefined }>({
    key: 'serviceId',
    op: 'EQUAL',
    value: undefined
  });
  const sourceIdFilter = ref<{ key: 'sourceId', op: 'EQUAL', value: string | undefined }>({
    key: 'sourceId',
    op: 'EQUAL',
    value: undefined
  });

  // Script type options
  const scriptTypeOpt = ref<MenuItem[]>([]);

  /**
   * Load script type enum options
   */
  const loadEnum = () => {
    const data = enumUtils.enumToMessages(ScriptType);
    scriptTypeOpt.value = data.map(i => ({ name: i.message, key: i.value }))
      .filter(i => i.key !== ScriptType.MOCK_APIS);
  };

  /**
   * Format date string for quick date selections
   */
  const formatDateString = (key: MenuItem['key']) => {
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
   * Handle menu item click for quick search options
   */
  const handleMenuItemClick = (data: MenuItem) => {
    const key = data.key;
    // Current operation is unselecting
    const typeKeys = scriptTypeOpt.value.map(i => i.key);

    if (selectedMenuMap.value.has(key)) {
      // "All" button selected, clicking again does nothing
      if (key === 'none') {
        return;
      }

      // Remove the key from selectedMenuMap when deselecting
      selectedMenuMap.value.delete(key);

      if (key === 'createdBy') {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: undefined }]);
        }
        return;
      }

      if (key === 'lastModifiedBy') {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([
            { valueKey: 'lastModifiedBy', value: undefined }
          ]);
        }
        return;
      }

      if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
        quickDateMap.value.clear();
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([
            { valueKey: 'createdDate', value: undefined }
          ]);
        }
        return;
      }

      if (typeKeys.includes(key)) {
        typeDataMap.value.clear();
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([
            { valueKey: 'type', value: undefined }
          ]);
        }
        return;
      }

      return;
    }

    // Selecting "All", clear other button selections
    if (key === 'none') {
      selectedMenuMap.value.clear();
      selectedMenuMap.value.set('none', { key: 'none' });

      // Clear search panel
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
      }

      // Clear serviceId
      serviceIdFilter.value.value = undefined;

      // Clear sourceId
      sourceIdFilter.value.value = undefined;

      return;
    }

    // Other buttons will be automatically set through watchEffect
    if (key === 'createdBy') {
      // Toggle selection: if already selected, deselect it
      if (selectedMenuMap.value.has(key)) {
        selectedMenuMap.value.delete(key);
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: undefined }]);
        }
        return;
      }

      // Clear 'all' selection when selecting other conditions
      selectedMenuMap.value.delete('none');
      selectedMenuMap.value.set(key, { key });
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: userId }]);
      }
      return;
    }

    if (key === 'lastModifiedBy') {
      // Toggle selection: if already selected, deselect it
      if (selectedMenuMap.value.has(key)) {
        selectedMenuMap.value.delete(key);
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'lastModifiedBy', value: undefined }]);
        }
        return;
      }

      // Clear 'all' selection when selecting other conditions
      selectedMenuMap.value.delete('none');
      selectedMenuMap.value.set(key, { key });
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'lastModifiedBy', value: userId }
        ]);
      }
      return;
    }

    if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      // Toggle selection: if already selected, deselect it
      if (selectedMenuMap.value.has(key)) {
        selectedMenuMap.value.delete(key);
        quickDateMap.value.clear();
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', value: undefined }]);
        }
        return;
      }

      // Clear 'all' selection when selecting other conditions
      selectedMenuMap.value.delete('none');
      // Clear other date selections (mutual exclusion within date group)
      selectedMenuMap.value.delete('lastDay');
      selectedMenuMap.value.delete('lastThreeDays');
      selectedMenuMap.value.delete('lastWeek');

      quickDateMap.value.clear();
      quickDateMap.value.set(key as 'lastDay' | 'lastThreeDays' | 'lastWeek', formatDateString(key));
      selectedMenuMap.value.set(key, { key });
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'createdDate', value: quickDateMap.value.get(key as 'lastDay' | 'lastThreeDays' | 'lastWeek') }
        ]);
      }
      return;
    }

    if (typeKeys.includes(key)) {
      // Toggle selection: if already selected, deselect it
      if (selectedMenuMap.value.has(key)) {
        selectedMenuMap.value.delete(key);
        typeDataMap.value.clear();
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'type', value: undefined }]);
        }
        return;
      }

      // Clear 'all' selection when selecting other conditions
      selectedMenuMap.value.delete('none');
      // Clear other type selections (mutual exclusion within type group)
      scriptTypeOpt.value.forEach(type => {
        selectedMenuMap.value.delete(type.key);
      });

      typeDataMap.value.clear();
      typeDataMap.value.set(key, key);
      selectedMenuMap.value.set(key, { key });
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'type', value: key }
        ]);
      }
    }
  };

  /**
   * Handle search panel change events
   */
  const handleSearchPanelChange = (
    data: { key: string; op: string; value: string }[],
    _headers?: { [key: string]: string },
    key?: string
  ) => {
    filters.value = data;

    if (key === 'source') {
      // Reset service, API, scenario
      serviceIdFilter.value.value = undefined;
      sourceIdFilter.value.value = undefined;
    }

    // Selecting add time clears quick search selected time options
    if (key === 'createdDate') {
      selectedMenuMap.value.delete('lastDay');
      selectedMenuMap.value.delete('lastThreeDays');
      selectedMenuMap.value.delete('lastWeek');
    }
  };

  /**
   * Handle source ID change
   */
  const handleSourceIdChange = (value: string) => {
    sourceIdFilter.value = { key: 'sourceId', op: 'EQUAL', value };
  };

  /**
   * Handle service ID change
   */
  const handleServiceIdChange = (value: string) => {
    serviceIdFilter.value = { key: 'serviceId', op: 'EQUAL', value };
  };

  /**
   * Get search data for API requests
   */
  const getData = () => {
    // Package data
    const _filters: { key: string; op: string; value: boolean | string | string[] }[] = cloneDeep(filters.value);
    if (serviceIdFilter.value.value) {
      _filters.push({ ...(serviceIdFilter.value as { key: string; op: string; value: string; }) });
    }

    if (sourceIdFilter.value.value) {
      _filters.push({ ...(sourceIdFilter.value as { key: string; op: string; value: string; }) });
    }

    return _filters;
  };

  /**
   * Reset search data
   */
  const resetData = () => {
    quickDateMap.value.clear();
    selectedMenuMap.value.clear();
    typeDataMap.value.clear();
    filters.value = [];
    serviceIdFilter.value = { key: 'serviceId', op: 'EQUAL', value: undefined };
    sourceIdFilter.value = { key: 'sourceId', op: 'EQUAL', value: undefined };
  };

  /**
   * Reset search panel
   */
  const resetSearchPanel = () => {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      const configs = searchOptions.map(item => {
        return {
          valueKey: item.valueKey,
          value: undefined
        };
      });

      searchPanelRef.value.setConfigs(configs);
    }
  };

  // Search options configuration
  const searchOptions = [
    {
      valueKey: 'name',
      placeholder: t('scriptHome.searchPanel.searchPlaceholder'),
      type: 'input',
      maxlength: 100
    },
    {
      valueKey: 'source',
      placeholder: t('scriptHome.searchPanel.sourcePlaceholder'),
      type: 'select-enum',
      enumKey: 'ScriptSource',
      excludes: (data: { value: string; message: string }) => {
        return ['TASK'].includes(data.value);
      }
    },
    {
      valueKey: 'tag',
      placeholder: t('scriptHome.searchPanel.tagPlaceholder'),
      type: 'input',
      op: 'EQUAL',
      maxlength: 100
    },
    {
      type: 'select',
      valueKey: 'serviceId',
      placeholder: t('scriptHome.searchPanel.servicePlaceholder'),
      noDefaultSlot: true
    },
    {
      type: 'select',
      valueKey: 'sourceId',
      noDefaultSlot: true
    },
    {
      type: 'select-user',
      valueKey: 'createdBy',
      placeholder: t('scriptHome.searchPanel.creatorPlaceholder')
    },
    {
      type: 'select-user',
      valueKey: 'lastModifiedBy',
      placeholder: t('scriptHome.searchPanel.modifierPlaceholder')
    },
    {
      type: 'date-range',
      valueKey: 'createdDate',
      placeholder: [
        t('scriptHome.searchPanel.addTimePlaceholder.0'),
        t('scriptHome.searchPanel.addTimePlaceholder.1')
      ],
      showTime: true
    },
    {
      type: 'date-range',
      valueKey: 'lastModifiedDate',
      placeholder: [
        t('scriptHome.searchPanel.modifyTimePlaceholder.0'),
        t('scriptHome.searchPanel.modifyTimePlaceholder.1')
      ],
      showTime: true
    }
  ];

  // Computed properties
  const menuItems = computed((): MenuItem[] => {
    return [
      {
        key: 'none',
        name: t('scriptHome.searchPanel.quickSearchOptions.all')
      },
      {
        key: 'createdBy',
        name: t('scriptHome.searchPanel.quickSearchOptions.myAdded')
      },
      {
        key: 'lastModifiedBy',
        name: t('scriptHome.searchPanel.quickSearchOptions.myModified')
      },
      ...scriptTypeOpt.value,
      {
        key: 'lastDay',
        name: t('scriptHome.searchPanel.quickSearchOptions.last1Day')
      },
      {
        key: 'lastThreeDays',
        name: t('scriptHome.searchPanel.quickSearchOptions.last3Days')
      },
      {
        key: 'lastWeek',
        name: t('scriptHome.searchPanel.quickSearchOptions.last7Days')
      }
    ];
  });

  const source = computed(() => {
    return filters.value.find(item => item.key === 'source')?.value;
  });

  const isAPISource = computed(() => {
    return source.value === 'API';
  });

  const isScenarioSource = computed(() => {
    return source.value === 'SCENARIO';
  });

  const apiParams = computed(() => {
    return {
      serviceId: serviceIdFilter.value.value
    };
  });

  return {
    // Reactive data
    searchPanelRef,
    filters,
    serviceIdFilter,
    sourceIdFilter,
    scriptTypeOpt,
    selectedMenuMap,

    // Methods
    loadEnum,
    handleMenuItemClick,
    handleSearchPanelChange,
    handleSourceIdChange,
    handleServiceIdChange,
    getData,
    resetData,
    resetSearchPanel,

    // Computed properties
    menuItems,
    searchOptions,
    isAPISource,
    isScenarioSource,
    apiParams
  };
}
