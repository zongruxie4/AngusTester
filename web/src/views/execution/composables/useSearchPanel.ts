import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { cloneDeep } from 'lodash-es';
import { duration, enumUtils, ScriptSource, ScriptType, TESTER, XCanDexie } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import dayjs, { Dayjs } from 'dayjs';
import { setting } from '@/api/gm';
import { ExecStatus } from '@/enums/enums';
import type { MenuItem } from '../types';

export type OrderByKey = 'createdDate' | 'createdByName';

export interface SearchPanelProps {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

/**
 * Composable for managing search panel functionality
 * Handles search filters, quick search, and data persistence
 */
export function useSearchPanel (props: SearchPanelProps) {
  const { t } = useI18n();

  // State management
  const editionType = ref<string>();
  const isPrivate = ref(false);
  const searchPanelRef = ref();
  const nodeQuota = ref(0);

  // Database instance
  let db: XCanDexie<{ id: string; data: any; }>;

  // Quick search state
  const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());
  let scriptTypeKeys: string[] = [];

  // Filter state
  const scriptTypeOpt = ref<{ name: string; key: string }[]>([]);
  const filters = ref<{ key: string; op: string; value: string; }[]>([]);
  const scriptSourceIdFilter = ref<{ key: 'scriptSourceId', op: 'EQUAL', value: string | undefined }>({
    key: 'scriptSourceId',
    op: 'EQUAL',
    value: undefined
  });
  const priorityFilter = ref<{
    key: 'priority',
    op: 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL',
    value: string | undefined
  }>({
    key: 'priority',
    op: 'EQUAL',
    value: undefined
  });

  /**
   * Load script type enum options
   */
  const loadScriptTypeEnum = (): void => {
    const data = enumUtils.enumToMessages(ScriptType, [ScriptType.MOCK_APIS]);
    scriptTypeOpt.value = (data || []).map(i => ({ name: i.message, key: i.value }));
    scriptTypeKeys = scriptTypeOpt.value.map(i => i.key);
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
   * Format date string for quick search
   */
  const formatDateString = (key: 'lastDay' | 'lastThreeDays' | 'lastWeek'): string[] => {
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
      startDate ? startDate.format('YYYY-MM-DD HH:mm:ss') : '',
      endDate ? endDate.format('YYYY-MM-DD HH:mm:ss') : ''
    ];
  };

  /**
   * Handle menu item click for quick search
   */
  const menuItemClick = (data: MenuItem, emit: any): void => {
    const key = data.key;

    // Handle deselection
    if (selectedMenuMap.value.has(key)) {
      selectedMenuMap.value.delete(key);

      if (key === 'none') {
        return;
      }

      // Clear specific filters
      if (['createdBy', 'lastModifiedBy', 'execBy'].includes(key)) {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: key, value: undefined }]);
        }
        emit('change', getData());
        return;
      }

      if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', value: undefined }]);
        }
        emit('change', getData());
        return;
      }

      if (scriptTypeKeys.includes(key)) {
        selectedMenuMap.value.delete(key);
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'scriptType', value: undefined }]);
        }
        emit('change', getData());
      }
      return;
    }

    // Handle selection
    if (key === 'none') {
      selectedMenuMap.value.clear();
      selectedMenuMap.value.set('none', { key: 'none' });

      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
      }

      scriptSourceIdFilter.value.value = undefined;

      priorityFilter.value.value = undefined;
      priorityFilter.value.op = 'EQUAL';
      emit('change', getData());
      return;
    }

    // Set specific filters
    if (['createdBy', 'lastModifiedBy', 'execBy'].includes(key)) {
      // Clear other user-related filters
      ['createdBy', 'lastModifiedBy', 'execBy'].forEach(i => selectedMenuMap.value.delete(i));
      selectedMenuMap.value.set(key, { key });
      selectedMenuMap.value.delete('none');

      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: key, value: userId.value }]);
      }
      emit('change', getData());
      return;
    }

    if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      // Clear other date filters
      ['lastDay', 'lastThreeDays', 'lastWeek'].forEach(i => selectedMenuMap.value.delete(i));
      selectedMenuMap.value.set(key, { key });
      selectedMenuMap.value.delete('none');

      const dateKey = key as 'lastDay' | 'lastThreeDays' | 'lastWeek';
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', value: formatDateString(dateKey) }]);
      }
      emit('change', getData());
      return;
    }

    if (scriptTypeKeys.includes(key)) {
      scriptTypeKeys.forEach(i => selectedMenuMap.value.delete(i));
      selectedMenuMap.value.set(key, { key });
      selectedMenuMap.value.delete('none');
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'scriptType', value: key }]);
      }
      emit('change', getData());
    }
  };

  /**
   * Handle search panel changes
   */
  const searchPanelChange = (data: { key: string; op: string; value: string }[]): void => {
    filters.value = data;

    // Clear script source filter when script source changes
    if (data.some(item => item.key === 'scriptSource')) {
      scriptSourceIdFilter.value.value = undefined;
    }
  };

  /**
   * Handle script source ID change
   */
  const scriptSourceIdChange = (value: string): void => {
    scriptSourceIdFilter.value = { key: 'scriptSourceId', op: 'EQUAL', value };
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
  const prioritySelectChange = (value: 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL'): void => {
    priorityFilter.value = {
      ...priorityFilter.value,
      op: value
    };
  };

  /**
   * Get combined filter data
   */
  const getData = (): { key: string; op: string; value: boolean | string | string[] }[] => {
    const _filters: { key: string; op: string; value: boolean | string | string[] }[] = cloneDeep(filters.value);

    if (scriptSourceIdFilter.value.value) {
      _filters.push({ ...(scriptSourceIdFilter.value as { key: string; op: string; value: string; }) });
    }

    if (priorityFilter.value.value) {
      _filters.push({ ...(priorityFilter.value as { key: string; op: string; value: string; }) });
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
          if (dateTimeKeys.includes(key)) {
            if (dateTimeMap[key]) {
              dateTimeMap[key].push(value);
            } else {
              dateTimeMap[key] = [value];
            }
          } else if (scriptTypeKeys.includes(key)) {
            scriptTypeMap[key] = value;
            valueMap[key] = value;
          } else {
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
        scriptSourceIdFilter.value = dbData.b || { key: 'scriptSourceId', op: 'EQUAL', value: undefined };
      } else {
        scriptSourceIdFilter.value = { key: 'scriptSourceId', op: 'EQUAL', value: undefined };
      }

      // Set priority filter
      if (Object.prototype.hasOwnProperty.call(dbData, 'c')) {
        priorityFilter.value = dbData.c || { key: 'priority', op: 'EQUAL', value: undefined };
      } else {
        priorityFilter.value = { key: 'priority', op: 'EQUAL', value: undefined };
      }

      const scriptValue = Object.keys(scriptTypeMap);
      if (scriptValue.length) {
        scriptValue.forEach(i => {
          selectedMenuMap.value.set(i, { key: i as MenuItem['key'] });
        });
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
    selectedMenuMap.value.clear();
    filters.value = [];
    scriptSourceIdFilter.value = { key: 'scriptSourceId', op: 'EQUAL', value: undefined };
  };

  /**
   * Handle refresh
   */
  const toRefresh = (emit: any): void => {
    emit('change', getData());
  };

  // Computed properties
  const userId = computed(() => props.userInfo?.id);

  const dbBaseKey = computed(() => {
    let key = '';
    if (userId.value) {
      key = userId.value;
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
        () => priorityFilter.value,
        () => selectedMenuMap.value
      ], () => {
        const _filters = filters.value;
        if (!(_filters.length || !!scriptSourceIdFilter.value.value)) {
          selectedMenuMap.value.clear();
          selectedMenuMap.value.set('none', { key: 'none' });
  
          emit('change', []);
        } else {
          // 删除快速查询选中的【所有】选项
          selectedMenuMap.value.delete('none');
  
          // 设置快速搜索
          const createdBy = _filters.find(item => item.key === 'createdBy')?.value;
          if (createdBy && createdBy === userId.value) {
            selectedMenuMap.value.set('createdBy', { key: 'createdBy' });
          } else {
            selectedMenuMap.value.delete('createdBy');
          }
  
          const lastModifiedBy = _filters.find(item => item.key === 'lastModifiedBy')?.value;
          if (lastModifiedBy && lastModifiedBy === userId.value) {
            selectedMenuMap.value.set('lastModifiedBy', { key: 'lastModifiedBy' });
          } else {
            selectedMenuMap.value.delete('lastModifiedBy');
          }
  
          const execBy = _filters.find(item => item.key === 'execBy')?.value;
          if (execBy && execBy === userId.value) {
            selectedMenuMap.value.set('execBy', { key: 'execBy' });
          } else {
            selectedMenuMap.value.delete('execBy');
          }
  
          const scriptType = _filters.find(item => item.key === 'scriptType')?.value;
          if (!scriptType) {
            selectedMenuMap.value.delete(scriptType as string);
          } else {
            selectedMenuMap.value.set(scriptType as string, { key: scriptType as string });
          }
  
          // if (quickDateMap.value.size > 0) {
          //   selectedMenuMap.value.delete('lastDay');
          //   selectedMenuMap.value.delete('lastThreeDays');
          //   selectedMenuMap.value.delete('lastWeek');
  
          //   const createdDateStart = _filters.find(item => item.key === 'createdDate' && item.op === 'GREATER_THAN_EQUAL')?.value;
          //   const createdDateEnd = _filters.find(item => item.key === 'createdDate' && item.op === 'LESS_THAN_EQUAL')?.value;
          //   const dateString = [createdDateStart, createdDateEnd];
          //   const entries = quickDateMap.value.entries();
          //   for (const [key, value] of entries) {
          //     if (isEqual(value, dateString)) {
          //       selectedMenuMap.value.set(key, { key });
          //     }
          //   }
  
          //   quickDateMap.value.clear();
          // }
  
          emit('change', getData());
        }
  
        // 保存到db
        if (db) {
          const dbData: {
            a?: {
              key: string;
              op: string;
              value: string;
            }[];
            b?: { key: 'scriptSourceId'; op: string; value: string | undefined; };
            c?: { key: 'priority', op: 'EQUAL'|'GREATER_THAN'|'GREATER_THAN_EQUAL'|'LESS_THAN'|'LESS_THAN_EQUAL', value: string | undefined };
          } = {};
          if (_filters.length) {
            dbData.a = cloneDeep(_filters);
          }
  
          if (scriptSourceIdFilter.value.value) {
            dbData.b = cloneDeep(scriptSourceIdFilter.value);
          }
  
          if (priorityFilter.value.value) {
            dbData.c = cloneDeep(priorityFilter.value);
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

  const menuItems = computed(() => [
    {
      key: 'none',
      name: t('execution.searchPanel.all')
    },
    {
      key: 'createdBy',
      name: t('execution.searchPanel.createdByMe')
    },
    {
      key: 'lastModifiedBy',
      name: t('execution.searchPanel.modifiedByMe')
    },
    {
      key: 'execBy',
      name: t('execution.searchPanel.executedByMe')
    },
    ...scriptTypeOpt.value,
    {
      key: 'lastDay',
      name: t('execution.searchPanel.lastDay')
    },
    {
      key: 'lastThreeDays',
      name: t('execution.searchPanel.lastThreeDays')
    },
    {
      key: 'lastWeek',
      name: t('execution.searchPanel.lastWeek')
    }
  ]);

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
      placeholder: [t('execution.searchPanel.plannedStartTimeFrom'), t('execution.searchPanel.plannedStartTimeTo')],
      showTime: true
    },
    {
      valueKey: 'actualStartDate',
      type: 'date-range',
      placeholder: [t('execution.searchPanel.actualStartTimeFrom'), t('execution.searchPanel.actualStartTimeTo')],
      showTime: true
    },
    {
      valueKey: 'endDate',
      type: 'date-range',
      placeholder: [t('execution.searchPanel.endTimeFrom'), t('execution.searchPanel.endTimeTo')],
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
      placeholder: [t('execution.searchPanel.createdTimeFrom'), t('execution.searchPanel.createdTimeTo')],
      showTime: true
    },
    {
      valueKey: 'lastModifiedBy',
      placeholder: t('execution.searchPanel.selectLastModifier'),
      type: 'select-user'
    },
    {
      valueKey: 'lastModifiedDate',
      placeholder: [t('execution.searchPanel.lastModifiedTimeFrom'), t('execution.searchPanel.lastModifiedTimeTo')],
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
    selectedMenuMap,
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
    menuItems,
    searchOptions,
    sortMenus,

    // Methods
    loadScriptTypeEnum,
    loadNodeQuota,
    formatDateString,
    menuItemClick,
    searchPanelChange,
    scriptSourceIdChange,
    priorityInputChange,
    prioritySelectChange,
    getData,
    initialize,
    resetSearchPanel,
    resetData,
    toRefresh
  };
}
