import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { cloneDeep } from 'lodash-es';
import { duration, enumUtils, ScriptSource, ScriptType, TESTER, XCanDexie, SearchCriteria } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import dayjs from 'dayjs';
import { setting } from '@/api/gm';
import { ExecStatus } from '@/enums/enums';
import { BasicProps } from '@/types/types';
import { createAuditOptions, createTimeOptions, createEnumTypeConfig, type QuickSearchConfig } from 'src/components/form/quickSearch';

export type OrderByKey = 'createdDate' | 'createdByName';

/**
 * Composable for managing search panel functionality
 * Handles search filters, quick search, and data persistence
 */
export function useSearchPanel (props: BasicProps) {
  const { t } = useI18n();

  // State management
  const editionType = ref<string>();
  const isPrivate = ref(false);
  const searchPanelRef = ref();
  const nodeQuota = ref(0);

  // Database instance
  let db: XCanDexie<{ id: string; data: any; }>;

  // Quick search state
  let scriptTypeKeys: string[] = [];

  // Filter state
  const scriptTypeOpt = ref<SearchCriteria[]>([]);
  const filters = ref<SearchCriteria[]>([]);
  const scriptSourceIdFilter = ref<SearchCriteria>({
    key: 'scriptSourceId',
    op: SearchCriteria.OpEnum.Equal,
    value: undefined
  });
  const priorityFilter = ref<SearchCriteria>({
    key: 'priority',
    op: SearchCriteria.OpEnum.Equal,
    value: undefined
  });

  /**
   * Quick search configuration for execution search panel
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
        fieldKey: 'lastModifiedBy'
      },
      {
        key: 'executedByMe',
        name: t('quickSearch.executedByMe'),
        fieldKey: 'execBy'
      }
    ], String(userId.value || '')),
    // Enum type configuration for script types
    enumType: createEnumTypeConfig(ScriptType, 'scriptType', [ScriptType.MOCK_APIS]),
    // Time options
    timeOptions: createTimeOptions([
      { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
      { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
      { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
    ], 'createdDate'),
    // External clear function
    externalClearFunction: () => {
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
      }
      scriptSourceIdFilter.value.value = undefined;
      priorityFilter.value.value = undefined;
      priorityFilter.value.op = SearchCriteria.OpEnum.Equal;
    }
  }));

  /**
   * Load script type enum options
   */
  const loadScriptTypeEnum = (): void => {
    const data = enumUtils.enumToMessages(ScriptType, [ScriptType.MOCK_APIS]);
    scriptTypeOpt.value = (data || []).map(i => ({ name: i.message, key: i.value }));
    scriptTypeKeys = scriptTypeOpt.value.map(i => i.key).filter((key): key is string => key !== undefined);
  };

  /**
   * Load node quota information
   */
  const loadNodeQuota = async (): Promise<void> => {
    const [error, { data }] = await setting.getQuotaByName('AngusTesterNode');
    if (error) {
      return;
    }
    nodeQuota.value = +data?.quota || 0;
  };

  /**
   * Handle quick search changes
   * Processes quick search filters and updates state
   * @param selectedKeys - Array of selected option keys
   * @param searchCriteria - Array of search criteria from quick search
   */
  const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]): void => {
    // Update filters with quick search criteria
    const quickSearchFields = ['createdBy', 'lastModifiedBy', 'execBy', 'scriptType', 'createdDate'];
    const otherFilters = filters.value.filter(f => f.key && !quickSearchFields.includes(f.key as string));
    const newFilters = [...otherFilters, ...searchCriteria];

    // Update filters
    filters.value = newFilters;
  };

  /**
   * Handle search panel changes
   */
  const searchPanelChange = (data: SearchCriteria[]): void => {
    // Merge search panel filters with quick search filters
    const quickSearchFields = ['createdBy', 'lastModifiedBy', 'execBy', 'scriptType', 'createdDate'];
    const quickSearchFilters = filters.value.filter(f => f.key && quickSearchFields.includes(f.key as string));
    const searchPanelFilters = data.filter(f => f.key && !quickSearchFields.includes(f.key as string));

    filters.value = [...quickSearchFilters, ...searchPanelFilters];

    // Clear script source filter when script source changes
    if (data.some(item => item.key === 'scriptSource')) {
      scriptSourceIdFilter.value.value = undefined;
    }
  };

  /**
   * Handle script source ID change
   */
  const scriptSourceIdChange = (value: string): void => {
    scriptSourceIdFilter.value = { key: 'scriptSourceId', op: SearchCriteria.OpEnum.Equal, value };
  };

  /**
   * Handle priority input change with debounce
   */
  const priorityInputChange = debounce(duration.search, (event: { target: { value: string } }): void => {
    const value = event.target.value;
    priorityFilter.value = {
      ...priorityFilter.value,
      value
    };
  });

  /**
   * Handle priority select change
   */
  const prioritySelectChange = (value: SearchCriteria.OpEnum): void => {
    priorityFilter.value = {
      ...priorityFilter.value,
      op: value
    };
  };

  /**
   * Get combined filter data
   */
  const getFilters = (): SearchCriteria[] => {
    const _filters: SearchCriteria[] = cloneDeep(filters.value);
    if (scriptSourceIdFilter.value.value) {
      _filters.push({ ...(scriptSourceIdFilter.value) });
    }
    if (priorityFilter.value.value) {
      _filters.push({ ...(priorityFilter.value) });
    }
    return _filters;
  };

  /**
   * Initialize search panel data from database
   */
  const initialize = async (emit: any): Promise<void> => {
    initWatch(emit);
    if (!db) {
      db = new XCanDexie<{ id: string; data: any; }>('parameter');
    }

    const [, data2] = await db.get(dbParamsKey.value);
    const dbData = data2?.data;

    if (dbData) {
      const valueMap: { [key: string]: string | string[] } = {};
      const scriptTypeMap: { [key: string]: string } = {};

      if (Object.prototype.hasOwnProperty.call(dbData, 'a')) {
        filters.value = dbData.a || [];
        const dateTimeKeys = ['startDate', 'actualStartDate', 'endDate', 'createdDate', 'lastModifiedDate'];
        const dateTimeMap: { [key: string]: string[] } = {};

        filters.value.every(({ key, value }) => {
          if (key && dateTimeKeys.includes(key) && typeof value === 'string') {
            if (dateTimeMap[key]) {
              dateTimeMap[key].push(value);
            } else {
              dateTimeMap[key] = [value];
            }
          } else if (key && scriptTypeKeys.includes(key)) {
            scriptTypeMap[key] = value as string;
            valueMap[key] = value;
          } else if (key) {
            valueMap[key] = value;
          }
          return true;
        });

        Object.entries(dateTimeMap).every(([key, [date1, date2]]: [string, string[]]) => {
          const dateTimes: string[] = [];
          if (date1 && date2) {
            if (dayjs(date1).isBefore(dayjs(date2))) {
              dateTimes[0] = date1;
              dateTimes[1] = date2;
            } else {
              dateTimes[0] = date2;
              dateTimes[1] = date1;
            }
          }

          if (dateTimes.length === 2) {
            valueMap[key] = dateTimes;
          }
          return true;
        });
      } else {
        filters.value = [];
      }

      // Set script source filter
      if (Object.prototype.hasOwnProperty.call(dbData, 'b')) {
        scriptSourceIdFilter.value = dbData.b || { key: 'scriptSourceId', op: SearchCriteria.OpEnum.Equal, value: undefined };
      } else {
        scriptSourceIdFilter.value = { key: 'scriptSourceId', op: SearchCriteria.OpEnum.Equal, value: undefined };
      }

      // Set priority filter
      if (Object.prototype.hasOwnProperty.call(dbData, 'c')) {
        priorityFilter.value = dbData.c || { key: 'priority', op: SearchCriteria.OpEnum.Equal, value: undefined };
      } else {
        priorityFilter.value = { key: 'priority', op: SearchCriteria.OpEnum.Equal, value: undefined };
      }

      const scriptValue = Object.keys(scriptTypeMap);
      if (scriptValue.length) {
        if (!Object.keys(valueMap).length) {
          emit('change', filters.value);
        }
      }

      // Set search panel data
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        if (Object.keys(valueMap).length) {
          const configs = searchOptions.value.map(item => ({
            valueKey: item.valueKey,
            value: valueMap[item.valueKey]
          }));
          searchPanelRef.value.setConfigs(configs);
        }
      }
      return;
    }

    resetData();
    resetSearchPanel();
  };

  /**
   * Reset search panel data
   */
  const resetSearchPanel = (): void => {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      const configs = searchOptions.value.map(item => ({
        valueKey: item.valueKey,
        value: undefined
      }));
      searchPanelRef.value.setConfigs(configs);
    }
  };

  /**
   * Reset all data
   */
  const resetData = (): void => {
    filters.value = [];
    scriptSourceIdFilter.value = { key: 'scriptSourceId', op: SearchCriteria.OpEnum.Equal, value: undefined };
  };

  /**
   * Handle refresh
   */
  const toRefresh = (emit: any): void => {
    emit('change', getFilters());
  };

  // Computed properties
  const userId = computed(() => props.userInfo?.id);

  const dbBaseKey = computed(() => {
    let key = '';
    if (userId.value) {
      key = String(userId.value);
    }
    if (props.projectId) {
      key += props.projectId;
    }
    return key;
  });

  const dbParamsKey = computed(() => {
    return btoa(dbBaseKey.value + 'execution');
  });

  const scriptSource = computed(() => {
    return filters.value.find(item => item.key === 'scriptSource')?.value;
  });

  const isServiceTargetType = computed(() => {
    return scriptSource.value === 'SERVICE_SMOKE' || scriptSource.value === 'SERVICE_SECURITY';
  });

  const isAPITargetType = computed(() => {
    return scriptSource.value === 'API';
  });

  const isScenarioTargetType = computed(() => {
    return scriptSource.value === 'SCENARIO';
  });

  const initWatch = (emit: any): void => {
    watch(
      [
        () => filters.value,
        () => scriptSourceIdFilter.value,
        () => priorityFilter.value
      ], () => {
        const _filters = filters.value;
        if (!(_filters.length || !!scriptSourceIdFilter.value.value)) {
          emit('change', []);
        } else {
          emit('change', getFilters());
        }

        if (db) {
          const dbData: {
            filters?: SearchCriteria[];
            scriptSourceId?: SearchCriteria;
            priority?: SearchCriteria;
          } = {};
          if (_filters.length) {
            dbData.filters = cloneDeep(_filters);
          }

          if (scriptSourceIdFilter.value.value) {
            dbData.scriptSourceId = cloneDeep(scriptSourceIdFilter.value);
          }

          if (priorityFilter.value.value) {
            dbData.priority = cloneDeep(priorityFilter.value);
          }

          if (Object.keys(dbData).length) {
            db.add({
              id: dbParamsKey.value,
              data: dbData
            });
          } else {
            db.delete(dbParamsKey.value);
          }
        }
      }, { immediate: false, deep: false });
  };

  const searchOptions = computed(() => [
    {
      valueKey: 'name',
      type: 'input',
      placeholder: t('execution.searchPanel.searchNameDesc'),
      maxlength: 100
    },
    {
      valueKey: 'status',
      type: 'select-enum',
      enumKey: ExecStatus,
      placeholder: t('execution.searchPanel.selectStatus')
    },
    {
      valueKey: 'scriptId',
      type: 'select',
      action: `${TESTER}/script?projectId=${props.projectId}&fullTextSearch=true`,
      fieldNames: { label: 'name', value: 'id' },
      placeholder: t('execution.searchPanel.selectScript'),
      maxlength: 100
    },
    {
      valueKey: 'priority'
    },
    {
      valueKey: 'scriptSource',
      type: 'select-enum',
      enumKey: ScriptSource,
      placeholder: t('execution.searchPanel.selectResourceType')
    },
    {
      valueKey: 'scriptSourceId',
      type: 'select',
      placeholder: t('execution.searchPanel.selectResource'),
      noDefaultSlot: true
    },
    {
      valueKey: 'startDate',
      type: 'date-range',
      placeholder: [
        t('execution.searchPanel.plannedStartTimeFrom'),
        t('execution.searchPanel.plannedStartTimeTo')
      ],
      showTime: true
    },
    {
      valueKey: 'actualStartDate',
      type: 'date-range',
      placeholder: [
        t('execution.searchPanel.actualStartTimeFrom'),
        t('execution.searchPanel.actualStartTimeTo')
      ],
      showTime: true
    },
    {
      valueKey: 'endDate',
      type: 'date-range',
      placeholder: [
        t('execution.searchPanel.endTimeFrom'),
        t('execution.searchPanel.endTimeTo')
      ],
      showTime: true
    },
    {
      valueKey: 'execBy',
      type: 'select-user',
      placeholder: t('execution.searchPanel.selectExecutor')
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      placeholder: t('execution.searchPanel.selectCreator')
    },
    {
      valueKey: 'createdDate',
      type: 'date-range',
      placeholder: [
        t('execution.searchPanel.createdTimeFrom'),
        t('execution.searchPanel.createdTimeTo')
      ],
      showTime: true
    },
    {
      valueKey: 'lastModifiedBy',
      placeholder: t('execution.searchPanel.selectLastModifier'),
      type: 'select-user'
    },
    {
      valueKey: 'lastModifiedDate',
      placeholder: [
        t('execution.searchPanel.lastModifiedTimeFrom'),
        t('execution.searchPanel.lastModifiedTimeTo')
      ],
      type: 'date-range',
      showTime: true
    }
  ]);

  const sortMenus = [
    {
      name: t('execution.searchPanel.sortByCreatedTime'),
      key: 'createdDate',
      orderSort: 'DESC'
    },
    {
      name: t('execution.searchPanel.sortByPriority'),
      key: 'priority',
      orderSort: 'DESC'
    }
  ];

  return {
    // State
    editionType,
    isPrivate,
    searchPanelRef,
    nodeQuota,
    scriptTypeOpt,
    filters,
    scriptSourceIdFilter,
    priorityFilter,

    // Computed
    userId,
    dbParamsKey,
    scriptSource,
    isServiceTargetType,
    isAPITargetType,
    isScenarioTargetType,
    searchOptions,
    sortMenus,
    quickSearchConfig,

    // Methods
    loadScriptTypeEnum,
    loadNodeQuota,
    searchPanelChange,
    scriptSourceIdChange,
    priorityInputChange,
    prioritySelectChange,
    getData: getFilters,
    initialize,
    resetSearchPanel,
    resetData,
    toRefresh,
    handleQuickSearchChange
  };
}
