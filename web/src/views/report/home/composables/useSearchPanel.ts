import { computed, inject, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { PageQuery, SearchCriteria, CombinedTargetType, XCanDexie } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import type {
  MenuItem,
  MenuItemKey,
  OrderByKey,
  SearchOption,
  SearchPanelEmits,
  SearchPanelProps,
  TargetIdFilter,
  UseSearchPanelReturn
} from '../types';

/**
 * Composable for managing search panel functionality
 * Handles search filters, quick queries, and data persistence
 */
export function useSearchPanel (
  props: SearchPanelProps,
  emit: SearchPanelEmits
): UseSearchPanelReturn {
  const { t } = useI18n();

  // Injected dependencies
  const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>(
    'proTypeShowMap',
    ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true })
  );

  // Database instance
  let db: any;

  // Reactive state
  const searchPanelRef = ref();
  const quickDateMap = ref<Map<MenuItemKey, string[]>>(new Map());
  const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());
  const filters = ref<SearchCriteria[]>([]);
  const targetIdFilter = ref<TargetIdFilter>({
    key: 'targetId',
    op: 'EQUAL',
    value: undefined
  });

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
    return btoa(dbBaseKey.value + 'report');
  });

  const targetType = computed(() => {
    return filters.value.find(item => item.key === 'targetType')?.value;
  });

  // Target type computed properties
  const isProjectTargetType = computed(() => targetType.value === CombinedTargetType.PROJECT);
  const isServiceTargetType = computed(() => targetType.value === CombinedTargetType.SERVICE);
  const isAPITargetType = computed(() => targetType.value === CombinedTargetType.API);
  const isTaskTargetType = computed(() => targetType.value === CombinedTargetType.TASK);
  const isSprintTargetType = computed(() => targetType.value === CombinedTargetType.TASK_SPRINT);
  const isPlanTargetType = computed(() => targetType.value === CombinedTargetType.FUNC_PLAN);
  const isCaseTargetType = computed(() => targetType.value === CombinedTargetType.FUNC_CASE);
  const isExecutionTargetType = computed(() => targetType.value === CombinedTargetType.EXECUTION);
  const isScenarioTargetType = computed(() => targetType.value === CombinedTargetType.SCENARIO);

  // Menu items configuration
  const menuItems: MenuItem[] = [
    {
      key: 'none',
      name: t('common.all')
    },
    {
      key: 'createdBy',
      name: t('reportHome.searchPanel.menuItems.myAdded')
    },
    {
      key: 'lastModifiedBy',
      name: t('reportHome.searchPanel.menuItems.myModified')
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
  ];

  // Search options configuration
  const searchOptions = computed<SearchOption[]>(() => [
    {
      valueKey: 'name',
      placeholder: t('reportHome.searchPanel.searchOptions.namePlaceholder'),
      type: 'input',
      maxlength: 100
    },
    {
      valueKey: 'targetType',
      placeholder: t('reportHome.searchPanel.searchOptions.resourceTypePlaceholder'),
      type: 'select-enum',
      enumKey: CombinedTargetType,
      excludes: (data: { message: string; value: string }) => {
        return !([
          CombinedTargetType.PROJECT, CombinedTargetType.SERVICE, CombinedTargetType.API,
          proTypeShowMap.value.showTask && CombinedTargetType.TASK,
          proTypeShowMap.value.showSprint && CombinedTargetType.TASK_SPRINT,
          CombinedTargetType.FUNC_PLAN, CombinedTargetType.FUNC_CASE,
          CombinedTargetType.EXECUTION, CombinedTargetType.SCENARIO].filter(Boolean)).includes(data.value);
      }
    },
    {
      type: 'select',
      valueKey: 'targetId',
      placeholder: t('reportHome.searchPanel.searchOptions.resourcePlaceholder'),
      noDefaultSlot: true
    },
    {
      type: 'select-user',
      valueKey: 'createdBy',
      placeholder: t('reportHome.searchPanel.searchOptions.creatorPlaceholder')
    },
    {
      type: 'date-range',
      valueKey: 'createdDate',
      placeholder: [t('reportHome.searchPanel.searchOptions.createTimeFrom'), t('reportHome.searchPanel.searchOptions.createTimeTo')],
      showTime: true
    }
  ]);

  // Sort menus configuration
  const sortMenus = [
    {
      name: t('reportHome.searchPanel.sortMenus.byCreateTime'),
      key: 'createdDate' as OrderByKey,
      orderSort: 'DESC' as PageQuery.OrderSort
    },
    {
      name: t('reportHome.searchPanel.sortMenus.byCreator'),
      key: 'createdByName' as OrderByKey,
      orderSort: 'DESC' as PageQuery.OrderSort
    }
  ];

  /**
   * Format date string for quick date filters
   */
  const formatDateString = (key: MenuItemKey): string[] => {
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
   * Handle menu item click for quick filters
   */
  const menuItemClick = (data: MenuItem) => {
    const key = data.key;

    // Handle deselection
    if (selectedMenuMap.value.has(key)) {
      if (key === 'none') {
        return;
      }

      selectedMenuMap.value.delete(key);

      if (key === 'createdBy') {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: undefined }]);
        }
        return;
      }

      if (key === 'lastModifiedBy') {
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'lastModifiedBy', value: undefined }]);
        }
        return;
      }

      if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
        quickDateMap.value.clear();
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', value: undefined }]);
        }
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
      targetIdFilter.value.value = undefined;
      return;
    }

    if (key === 'createdBy') {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: userId.value }]);
      }
      return;
    }

    if (key === 'lastModifiedBy') {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'lastModifiedBy', value: userId.value }]);
      }
      return;
    }

    if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      quickDateMap.value.clear();
      quickDateMap.value.set(key, formatDateString(key));
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', value: quickDateMap.value.get(key) }]);
      }
    }
  };

  /**
   * Handle search panel change
   */
  const searchPanelChange = (data: SearchCriteria[], _headers?: { [key: string]: string }, key?: string) => {
    filters.value = data;

    if (key === 'targetType') {
      targetIdFilter.value.value = undefined;
    }

    if (key === 'createdDate') {
      selectedMenuMap.value.delete('lastDay');
      selectedMenuMap.value.delete('lastThreeDays');
      selectedMenuMap.value.delete('lastWeek');
    }
  };

  /**
   * Handle target ID change
   */
  const targetIdChange = (value: string) => {
    targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value };
  };

  /**
   * Get current filter data
   */
  const getData = (): SearchCriteria[] => {
    const _filters: SearchCriteria[] = cloneDeep(filters.value);
    if (targetIdFilter.value.value) {
      _filters.push({ ...(targetIdFilter.value as SearchCriteria) });
    }
    return _filters;
  };

  /**
   * Handle sort action
   */
  const toSort = (data: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }): void => {
    emit('sort', data);
  };

  /**
   * Handle refresh action
   */
  const toRefresh = () => {
    emit('change', getData());
  };

  /**
   * Handle auth action
   */
  const toAuth = () => {
    emit('openAuth');
  };

  /**
   * Initialize database and load saved data
   */
  const initialize = async () => {
    if (!db) {
      db = new XCanDexie<{ id: string; data: any }>('parameter');
    }

    const [, data2] = await db.get(dbParamsKey.value);
    const dbData = data2?.data;

    if (dbData) {
      const valueMap: { [key: string]: string | string[] } = {};

      if (Object.prototype.hasOwnProperty.call(dbData, 'a')) {
        filters.value = dbData.a || [];
        const dateTimeKeys = ['createdDate'];
        const dateTimeMap: { [key: string]: string[] } = {};

        filters.value.every(({ key, value }) => {
          if (dateTimeKeys.includes(key)) {
            if (dateTimeMap[key]) {
              dateTimeMap[key].push(value as string);
            } else {
              dateTimeMap[key] = [value as string];
            }
          } else {
            valueMap[key] = value as string;
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

      if (Object.prototype.hasOwnProperty.call(dbData, 'b')) {
        targetIdFilter.value = dbData.b || { key: 'targetId', op: 'EQUAL', value: undefined };
      } else {
        targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value: undefined };
      }

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
  const resetSearchPanel = () => {
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
  const resetData = () => {
    quickDateMap.value.clear();
    selectedMenuMap.value.clear();
    filters.value = [];
    targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value: undefined };
  };

  // Lifecycle hooks
  onMounted(async () => {
    watch(() => dbParamsKey.value, (newValue) => {
      if (!newValue) {
        return;
      }
      initialize();
    }, { immediate: true });

    watch(
      [() => filters.value, () => targetIdFilter.value, () => selectedMenuMap.value],
      () => {
        const _filters = filters.value;
        if (!(_filters.length || !!targetIdFilter.value.value)) {
          selectedMenuMap.value.clear();
          selectedMenuMap.value.set('none', { key: 'none' });
          emit('change', []);
        } else {
          selectedMenuMap.value.delete('none');

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

          if (quickDateMap.value.size > 0) {
            selectedMenuMap.value.delete('lastDay');
            selectedMenuMap.value.delete('lastThreeDays');
            selectedMenuMap.value.delete('lastWeek');

            const createdDateStart = _filters.find(item => item.key === 'createdDate' && item.op === 'GREATER_THAN_EQUAL')?.value;
            const createdDateEnd = _filters.find(item => item.key === 'createdDate' && item.op === 'LESS_THAN_EQUAL')?.value;
            const dateString = [createdDateStart, createdDateEnd];
            const entries = quickDateMap.value.entries();
            for (const [key, value] of entries) {
              if (isEqual(value, dateString)) {
                selectedMenuMap.value.set(key, { key });
              }
            }
            quickDateMap.value.clear();
          }

          emit('change', getData());
        }

        // Save to database
        if (db) {
          const dbData: {
            a?: SearchCriteria[];
            b?: TargetIdFilter;
          } = {};
          if (_filters.length) {
            dbData.a = cloneDeep(_filters);
          }
          if (targetIdFilter.value.value) {
            dbData.b = cloneDeep(targetIdFilter.value);
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
      },
      { immediate: false, deep: false }
    );
  });

  return {
    searchOptions,
    menuItems,
    sortMenus,
    selectedMenuMap,
    quickDateMap,
    filters,
    targetIdFilter,
    menuItemClick,
    searchPanelChange,
    targetIdChange,
    toSort,
    toRefresh,
    toAuth,
    getData,
    searchPanelRef,
    isProjectTargetType,
    isServiceTargetType,
    isAPITargetType,
    isTaskTargetType,
    isSprintTargetType,
    isPlanTargetType,
    isCaseTargetType,
    isExecutionTargetType,
    isScenarioTargetType
  };
}
