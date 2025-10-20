import { computed, inject, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import dayjs from 'dayjs';
import { cloneDeep } from 'lodash-es';
import { PageQuery, SearchCriteria, CombinedTargetType, XCanDexie } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';
import { createAuditOptions, createTimeOptions, type QuickSearchConfig } from 'src/components/form/quickSearch';

import type {
  OrderByKey,
  SearchOption,
  SearchPanelEmits,
  UseSearchPanelReturn
} from '../types';

/**
 * Composable for managing search panel functionality
 * Handles search filters, quick queries, and data persistence
 */
export function useSearchPanel (
  props: BasicProps,
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
  const filters = ref<SearchCriteria[]>([]);
  const targetIdFilter = ref<SearchCriteria>({
    key: 'targetId',
    op: SearchCriteria.OpEnum.Equal,
    value: undefined
  });

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

  /**
   * Quick search configuration for report search panel
   * Provides predefined filter options for common searches
   */
  const quickSearchConfig = computed<QuickSearchConfig>(() => ({
    title: t('quickSearch.title'),
    // Audit information options
    auditOptions: createAuditOptions([
      {
        key: 'createdBy',
        name: t('quickSearch.createdByMe'),
        fieldKey: 'createdBy'
      },
      {
        key: 'lastModifiedBy',
        name: t('quickSearch.modifiedByMe'),
        fieldKey: 'lastModifiedBy'
      }
    ], String(userId.value || '')),
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
      targetIdFilter.value.value = undefined;
    }
  }));

  // Search options configuration
  const searchOptions = computed<SearchOption[]>(() => [
    {
      valueKey: 'name',
      placeholder: t('common.placeholders.searchKeyword'),
      type: 'input',
      maxlength: 100
    },
    {
      valueKey: 'targetType',
      placeholder: t('common.placeholders.selectResourceType'),
      type: 'select-enum',
      enumKey: CombinedTargetType as any,
      excludes: (data: { message: string; value: CombinedTargetType }) => {
        return !([
          CombinedTargetType.PROJECT, CombinedTargetType.SERVICE, CombinedTargetType.API,
          proTypeShowMap.value.showTask && CombinedTargetType.TASK,
          proTypeShowMap.value.showSprint && CombinedTargetType.TASK_SPRINT,
          CombinedTargetType.FUNC_PLAN, CombinedTargetType.FUNC_CASE,
          CombinedTargetType.EXECUTION, CombinedTargetType.SCENARIO]
          .filter(Boolean)).includes(data.value);
      }
    },
    {
      type: 'select',
      valueKey: 'targetId',
      placeholder: t('common.placeholders.selectResource'),
      noDefaultSlot: true
    },
    {
      type: 'select-user',
      valueKey: 'createdBy',
      placeholder: t('common.placeholders.selectCreator')
    },
    {
      type: 'date-range',
      valueKey: 'createdDate',
      placeholder: [
        t('common.placeholders.selectCreatedDateRange.0'),
        t('common.placeholders.selectCreatedDateRange.1')
      ],
      showTime: true
    }
  ]);

  // Sort menus configuration
  const sortMenus = [
    {
      name: t('reportHome.searchPanel.sortMenus.byCreateDate'),
      key: 'createdDate' as OrderByKey,
      orderSort: PageQuery.OrderSort.Desc
    },
    {
      name: t('reportHome.searchPanel.sortMenus.byCreator'),
      key: 'createdByName' as OrderByKey,
      orderSort: PageQuery.OrderSort.Desc
    }
  ];

  /**
   * Handle quick search changes
   * Processes quick search filters and updates state
   * @param selectedKeys - Array of selected option keys
   * @param searchCriteria - Array of search criteria from quick search
   */
  const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]): void => {
    // Update filters with quick search criteria
    const quickSearchFields = ['createdBy', 'lastModifiedBy', 'createdDate'];
    const otherFilters = filters.value.filter(f => f.key && !quickSearchFields.includes(f.key as string));
    const newFilters = [...otherFilters, ...searchCriteria];

    // Update filters
    filters.value = newFilters;
  };

  /**
   * Handle search panel change
   */
  const searchPanelChange = (
    data: SearchCriteria[],
    _headers?: { [key: string]: string },
    key?: string
  ) => {
    // Merge search panel filters with quick search filters
    const quickSearchFields = ['createdBy', 'lastModifiedBy', 'createdDate'];
    const quickSearchFilters = filters.value.filter(f => f.key && quickSearchFields.includes(f.key as string));
    const searchPanelFilters = data.filter(f => f.key && !quickSearchFields.includes(f.key as string));

    filters.value = [...quickSearchFilters, ...searchPanelFilters];

    if (key === 'targetType') {
      targetIdFilter.value.value = undefined;
    }
  };

  /**
   * Handle target ID change
   */
  const targetIdChange = (value: string) => {
    targetIdFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value };
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
          if (key && dateTimeKeys.includes(key)) {
            if (value) {
              if (dateTimeMap[key]) {
                dateTimeMap[key].push(value as string);
              } else {
                dateTimeMap[key] = [value as string];
              }
            }
          } else if (key) {
            if (value) {
              valueMap[key] = value as string;
            }
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
        targetIdFilter.value = dbData.b || { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };
      } else {
        targetIdFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };
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
    filters.value = [];
    targetIdFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };
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
      [() => filters.value, () => targetIdFilter.value],
      () => {
        const _filters = filters.value;
        if (!(_filters.length || !!targetIdFilter.value.value)) {
          emit('change', []);
        } else {
          emit('change', getData());
        }

        // Save to database
        if (db) {
          const dbData: {
            filters_?: SearchCriteria[];
            targetIdFilter_?: SearchCriteria;
          } = {};
          if (_filters.length) {
            dbData.filters_ = cloneDeep(_filters);
          }
          if (targetIdFilter.value.value) {
            dbData.targetIdFilter_ = cloneDeep(targetIdFilter.value);
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
    sortMenus,
    filters,
    targetIdFilter,
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
    isScenarioTargetType,
    quickSearchConfig,
    handleQuickSearchChange
  };
}
