import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { PageQuery, TESTER } from '@xcan-angus/infra';
import type {
  FilterItem,
  MenuItem,
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
      placeholder: t('scenarioMonitor.searchPanel.searchNamePlaceholder'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'scenarioId',
      type: 'select',
      allowClear: true,
      placeholder: t('scenarioMonitor.searchPanel.selectScenarioPlaceholder'),
      action: `${TESTER}/scenario?projectId=${projectId}&fullTextSearch=true`,
      fieldNames: { value: 'id', label: 'name' }
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      allowClear: true,
      placeholder: t('scenarioMonitor.searchPanel.selectCreatorPlaceholder')
    },
    {
      type: 'date-range',
      valueKey: 'createdDate',
      showTime: true
    }
  ];

  // Sort menu items configuration
  const sortMenuItems: SortMenuItem[] = [
    {
      name: t('scenarioMonitor.searchPanel.sortOptions.byAddTime'),
      key: 'createdDate',
      orderSort: 'DESC'
    },
    {
      name: t('scenarioMonitor.searchPanel.sortOptions.byCreator'),
      key: 'createdByName',
      orderSort: 'ASC'
    }
  ];

  // Data state
  const selectedMenuMap = ref<{[key: string]: boolean}>({});
  const orderBy = ref<OrderByKey>();
  const orderSort = ref<PageQuery.OrderSort>();
  const searchFilters = ref<FilterItem[]>([]);
  const quickSearchFilters = ref<FilterItem[]>([]);
  const assocFilters = ref<FilterItem[]>([]);

  // Menu items for quick search
  const menuItems = computed(() => [
    {
      key: '',
      name: t('scenarioMonitor.searchPanel.filterOptions.all')
    },
    {
      key: 'createdBy',
      name: t('scenarioMonitor.searchPanel.filterOptions.myCreated')
    },
    {
      key: 'lastModifiedBy',
      name: t('scenarioMonitor.searchPanel.filterOptions.myModified')
    },
    {
      key: 'PENDING',
      name: t('status.pending')
    },
    {
      key: 'SUCCESS',
      name: t('status.success')
    },
    {
      key: 'FAILURE',
      name: t('status.failed')
    },
    {
      key: 'lastDay',
      name: t('scenarioMonitor.searchPanel.timeRanges.last1Day')
    },
    {
      key: 'lastThreeDays',
      name: t('scenarioMonitor.searchPanel.timeRanges.last3Days')
    },
    {
      key: 'lastWeek',
      name: t('scenarioMonitor.searchPanel.timeRanges.last7Days')
    }
  ]);

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
    searchFilters.value = data.filter(item => !['createdDate', 'createdBy'].includes(item.key));
    assocFilters.value = data.filter(item => ['createdDate', 'createdBy'].includes(item.key));
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
   * Handle menu item click for quick search
   * @param data - Menu item data
   */
  const menuItemClick = (data: MenuItem): void => {
    // This will be implemented in the main component
    // as it requires access to other composables
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
    menuItems,
    selectedMenuMap,
    orderBy,
    orderSort,
    searchFilters,
    quickSearchFilters,
    assocFilters,
    getParams,
    searchChange,
    toSort,
    menuItemClick,
    refresh
  };
}
