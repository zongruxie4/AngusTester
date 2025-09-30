import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { PageQuery, TESTER, appContext } from '@xcan-angus/infra';
import { createAuditOptions, createTimeOptions, createEnumTypeConfig, type QuickSearchConfig } from '@/components/quickSearch';
import { ScenarioMonitorStatus } from '@/enums/enums';
import type {
  FilterItem,
  OrderByKey,
  SearchPanelOption,
  SearchPanelParams,
  SortMenuItem,
  UseSearchPanelDataReturn
} from '../../types';

export function useSearchPanelData (projectId: string): UseSearchPanelDataReturn {
  const { t } = useI18n();

  // Search panel options configuration
  const searchPanelOptions: SearchPanelOption[] = [
    {
      valueKey: 'name',
      type: 'input',
      placeholder: t('common.placeholders.searchKeyword'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'scenarioId',
      type: 'select',
      allowClear: true,
      placeholder: t('common.placeholders.selectScenario'),
      action: `${TESTER}/scenario?projectId=${projectId}&fullTextSearch=true`,
      fieldNames: { value: 'id', label: 'name' }
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      allowClear: true,
      placeholder: t('common.placeholders.selectCreator')
    },
    {
      type: 'date-range',
      valueKey: 'createdDate',
      showTime: true,
      placeholder: [
        t('common.placeholders.selectCreatedDate.0'),
        t('common.placeholders.selectCreatedDate.1')
      ]
    }
  ];

  // Sort menu items configuration
  const sortMenuItems: SortMenuItem[] = [
    {
      name: t('scenarioMonitor.searchPanel.sortOptions.byAddTime'),
      key: 'createdDate',
      orderSort: PageQuery.OrderSort.Desc
    },
    {
      name: t('scenarioMonitor.searchPanel.sortOptions.byCreator'),
      key: 'createdByName',
      orderSort: PageQuery.OrderSort.Asc
    }
  ];

  // Data state
  const orderBy = ref<OrderByKey>();
  const orderSort = ref<PageQuery.OrderSort>();
  const searchFilters = ref<FilterItem[]>([]);
  const quickSearchFilters = ref<FilterItem[]>([]);
  const assocFilters = ref<FilterItem[]>([]);

  // Get current user ID
  const userId = computed(() => String(appContext.getUser()?.id || ''));

  // Quick search configuration
  const quickSearchConfig = computed<QuickSearchConfig>(() => ({
    title: t('quickSearch.title'),
    // Audit information options
    auditOptions: createAuditOptions([
      { key: 'createdBy', name: t('quickSearch.createdByMe'), fieldKey: 'createdBy' },
      { key: 'lastModifiedBy', name: t('quickSearch.modifiedByMe'), fieldKey: 'lastModifiedBy' }
    ], userId.value),
    enumType: createEnumTypeConfig(ScenarioMonitorStatus, 'status'),
    timeOptions: createTimeOptions([
      { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
      { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
      { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
    ], 'createdDate'),
    // External clear function
    externalClearFunction: () => {
      // This will be set by the parent component
    }
  }));

  /**
   * Get current search parameters
   * @returns Search panel parameters object
   */
  const getParams = (): SearchPanelParams => {
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
   * Handle search panel change event
   * @param data - Filter items from search panel
   */
  const searchChange = (data: FilterItem[]): void => {
    searchFilters.value = data.filter(item => item.key && !['createdDate', 'createdBy'].includes(item.key));
    assocFilters.value = data.filter(item => item.key && ['createdDate', 'createdBy'].includes(item.key));
  };

  /**
   * Handle sort change event
   * @param sortData - Sort data with orderBy and orderSort
   */
  const toSort = (sortData: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }): void => {
    orderBy.value = sortData.orderBy;
    orderSort.value = sortData.orderSort;
  };

  /**
   * Refresh search panel
   */
  const refresh = (): void => {
    // This will be implemented in the main component
  };

  return {
    searchPanelOptions,
    sortMenuItems,
    quickSearchConfig,
    orderBy,
    orderSort,
    searchFilters,
    quickSearchFilters,
    assocFilters,
    getParams,
    searchChange,
    toSort,
    refresh
  };
}
