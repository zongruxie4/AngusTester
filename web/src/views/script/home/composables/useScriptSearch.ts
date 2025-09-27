import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ScriptType, enumUtils, SearchCriteria } from '@xcan-angus/infra';
import { cloneDeep } from 'lodash-es';
import { CombinedTargetType } from '@/enums/enums';
import { MenuItem } from '@/views/script/types';
import { formatDateString } from '@/utils/utils';

/**
 * Composable for managing search functionality in script home
 * @param userId - The ID of the current user
 */
export function useScriptSearch (userId: string) {
  const { t } = useI18n();

  // Search panel reference
  const searchPanelRef = ref();

  // Quick date selection map
  const quickDateMap = ref<Map<'last1Day' | 'last3Days' | 'last7Days', string[]>>(new Map());
  const typeDataMap = ref<Map<string, string>>(new Map());
  const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());

  // Filters
  const filters = ref<SearchCriteria[]>([]);
  const serviceIdFilter = ref<SearchCriteria>({
    key: 'serviceId',
    op: SearchCriteria.OpEnum.Equal,
    value: undefined
  });
  const sourceIdFilter = ref<SearchCriteria>({
    key: 'sourceId',
    op: SearchCriteria.OpEnum.Equal,
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

      if (['last1Day', 'last3Days', 'last7Days'].includes(key)) {
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

    if (['last1Day', 'last3Days', 'last7Days'].includes(key)) {
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
      selectedMenuMap.value.delete('last1Day');
      selectedMenuMap.value.delete('last3Days');
      selectedMenuMap.value.delete('last7Days');

      quickDateMap.value.clear();
      quickDateMap.value.set(key as 'last1Day' | 'last3Days' | 'last7Days', formatDateString(key));
      selectedMenuMap.value.set(key, { key });
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'createdDate', value: quickDateMap.value.get(key as 'last1Day' | 'last3Days' | 'last7Days') }
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
    _filters: SearchCriteria[],
    _headers?: { [key: string]: string },
    key?: string
  ) => {
    filters.value = _filters;

    if (key === 'source') {
      // Reset service, API, scenario
      serviceIdFilter.value.value = undefined;
      sourceIdFilter.value.value = undefined;
    }

    // Selecting add time clears quick search selected time options
    if (key === 'createdDate') {
      selectedMenuMap.value.delete('last1Day');
      selectedMenuMap.value.delete('last3Days');
      selectedMenuMap.value.delete('last7Days');
    }
  };

  /**
   * Handle source ID change
   */
  const handleSourceIdChange = (value: string) => {
    sourceIdFilter.value = { key: 'sourceId', op: SearchCriteria.OpEnum.Equal, value };
  };

  /**
   * Handle service ID change
   */
  const handleServiceIdChange = (value: string) => {
    serviceIdFilter.value = { key: 'serviceId', op: SearchCriteria.OpEnum.Equal, value };
  };

  /**
   * Get search data for API requests
   */
  const getData = () => {
    // Package data
    const _filters: SearchCriteria[] = cloneDeep(filters.value);
    if (serviceIdFilter.value.value) {
      _filters.push({ ...(serviceIdFilter.value) });
    }

    if (sourceIdFilter.value.value) {
      _filters.push({ ...(sourceIdFilter.value) });
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
    serviceIdFilter.value = { key: 'serviceId', op: SearchCriteria.OpEnum.Equal, value: undefined };
    sourceIdFilter.value = { key: 'sourceId', op: SearchCriteria.OpEnum.Equal, value: undefined };
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
      excludes: (data: { value: CombinedTargetType; message: string }) => {
        return [CombinedTargetType.TASK].includes(data.value);
      }
    },
    {
      valueKey: 'tag',
      placeholder: t('scriptHome.searchPanel.tagPlaceholder'),
      type: 'input',
      op: SearchCriteria.OpEnum.Equal,
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
      placeholder: t('common.placeholders.selectCreator')
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
        name: t('common.all')
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
        key: 'last1Day',
        name: t('quickSearch.last1Day')
      },
      {
        key: 'last3Days',
        name: t('quickSearch.last3Days')
      },
      {
        key: 'last7Days',
        name: t('quickSearch.last7Days')
      }
    ];
  });

  const source = computed(() => {
    return filters.value.find(item => item.key === 'source')?.value;
  });

  const isAPISource = computed(() => {
    return source.value === CombinedTargetType.API;
  });

  const isScenarioSource = computed(() => {
    return source.value === CombinedTargetType.SCENARIO;
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
