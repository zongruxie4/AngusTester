import { ref, computed, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { ScriptType, SearchCriteria } from '@xcan-angus/infra';
import { cloneDeep } from 'lodash-es';
import { CombinedTargetType } from '@/enums/enums';
import {
  createAuditOptions, createTimeOptions, createEnumTypeConfig, type QuickSearchConfig
} from '@/components/quickSearch';

/**
 * Composable for managing search functionality in script home
 * @param userId - The ID of the current user
 */
export function useScriptSearch (userId: string) {
  const { t } = useI18n();

  // Search panel reference
  const searchPanelRef = ref();

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

  // Flag to prevent circular updates
  let isUpdating = false;

  // Quick search configuration
  const quickSearchConfig = computed<QuickSearchConfig>(() => ({
    title: t('quickSearch.title'),
    auditOptions: createAuditOptions([
      { key: 'createdBy', name: t('scriptHome.searchPanel.quickSearchOptions.myAdded'), fieldKey: 'createdBy' },
      { key: 'lastModifiedBy', name: t('scriptHome.searchPanel.quickSearchOptions.myModified'), fieldKey: 'lastModifiedBy' }
    ], userId),
    enumType: createEnumTypeConfig(ScriptType, 'type', [ScriptType.MOCK_APIS]),
    timeOptions: createTimeOptions([
      { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
      { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
      { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
    ], 'createdDate'),
    externalClearFunction: () => {
      // Clear search panel
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
      }
      // Clear serviceId and sourceId
      serviceIdFilter.value.value = undefined;
      sourceIdFilter.value.value = undefined;
    }
  }));

  /**
   * Handle quick search changes
   */
  const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]) => {
    if (isUpdating) return;

    isUpdating = true;
    nextTick(() => {
      // Merge quick search criteria with existing filters
      const quickSearchFields = ['createdBy', 'lastModifiedBy', 'type', 'createdDate'];
      const otherFilters = filters.value.filter(f => f.key && !quickSearchFields.includes(f.key));
      const newFilters = [...otherFilters, ...searchCriteria];

      // Always update filters to ensure watch triggers
      filters.value = newFilters;
      isUpdating = false;
    });
  };

  /**
   * Handle search panel change events
   */
  const handleSearchPanelChange = (
    _filters: SearchCriteria[],
    _headers?: { [key: string]: string },
    key?: string
  ) => {
    if (isUpdating) return;

    isUpdating = true;
    nextTick(() => {
      // Only update filters if they are different to avoid unnecessary re-renders
      const quickSearchFields = ['createdBy', 'lastModifiedBy', 'type', 'createdDate'];
      const quickSearchFilters = filters.value.filter(f => f.key && quickSearchFields.includes(f.key));
      const newFilters = [...quickSearchFilters, ..._filters];

      // Check if filters actually changed
      if (JSON.stringify(filters.value) !== JSON.stringify(newFilters)) {
        filters.value = newFilters;
      }

      if (key === 'source') {
        // Reset service, API, scenario
        serviceIdFilter.value.value = undefined;
        sourceIdFilter.value.value = undefined;
      }

      isUpdating = false;
    });
  };

  /**
   * Handle source ID change
   */
  const handleSourceIdChange = (value: string | undefined) => {
    sourceIdFilter.value = { key: 'sourceId', op: SearchCriteria.OpEnum.Equal, value };
  };

  /**
   * Handle service ID change
   */
  const handleServiceIdChange = (value: string | undefined) => {
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
  ] as any;

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

    // Quick search configuration
    quickSearchConfig,
    handleQuickSearchChange,

    // Methods
    handleSearchPanelChange,
    handleSourceIdChange,
    handleServiceIdChange,
    getData,
    resetData,
    resetSearchPanel,

    // Computed properties
    searchOptions,
    isAPISource,
    isScenarioSource,
    apiParams
  };
}
