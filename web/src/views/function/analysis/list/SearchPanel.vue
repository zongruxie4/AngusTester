<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { XCanDexie, SearchCriteria, PageQuery } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { MenuItem } from '@/views/function/analysis/list/types';

// Component setup
const { t } = useI18n();

// Types
type Props = {
  collapse: boolean;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  orderBy: 'createdByName' ;
  orderSort: PageQuery.OrderSort;
  groupKey: 'none' | 'lastModifiedByName';
}

const props = withDefaults(defineProps<Props>(), {
  collapse: false,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  groupKey: 'none',
  orderBy: undefined,
  orderSort: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'add'): void;
  (e: 'change', value: SearchCriteria[]): void;
  (e: 'update:orderBy', value: 'createdByName'): void;
  (e: 'update:orderSort', value: PageQuery.OrderSort): void;
  (e: 'update:groupKey', value: 'none' | 'lastModifiedByName'): void;
  (e: 'update:visible', value: boolean): void;
}>();

// DATABASE
let searchDatabase: XCanDexie<{ id: string; data: any; }>;

// REFS
const searchPanelRef = ref();

// Reactive state
const quickDateRangeMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
const selectedQuickFilterMap = ref(new Map<string, Omit<MenuItem, 'name'>>());
const searchFilters = ref<SearchCriteria[]>([]);

/**
 * Handle sort configuration changes.
 * @param sortData - Sort configuration data
 */
const handleSortChange = (sortData: { orderBy: 'createsDate' | 'createdByName' ; orderSort: PageQuery.OrderSort; }) => {
  emit('update:orderBy', sortData.orderBy);
  emit('update:orderSort', sortData.orderSort);
};

/**
 * Handle quick filter menu item click.
 * @param menuItem - Clicked menu item
 */
const handleQuickFilterClick = (menuItem: MenuItem) => {
  const filterKey = menuItem.key;

  if (selectedQuickFilterMap.value.has(filterKey)) {
    handleQuickFilterDeselection(filterKey);
  } else {
    handleQuickFilterSelection(filterKey);
  }
};

/**
 * Handle quick filter deselection.
 * @param filterKey - Filter key to deselect
 */
const handleQuickFilterDeselection = (filterKey: string) => {
  if (filterKey === 'none') {
    return;
  }

  selectedQuickFilterMap.value.delete(filterKey);

  if (filterKey === 'createdBy') {
    updateSearchPanelConfigs([{ valueKey: 'createdBy', value: undefined }]);
    return;
  }

  if (filterKey === 'lastModifiedBy') {
    updateSearchPanelConfigs([{ valueKey: 'lastModifiedBy', value: undefined }]);
    return;
  }

  if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(filterKey)) {
    quickDateRangeMap.value.clear();
    updateSearchPanelConfigs([{ valueKey: 'createdDate', value: undefined }]);
  }
};

/**
 * Handle quick filter selection.
 * @param filterKey - Filter key to select
 */
const handleQuickFilterSelection = (filterKey: string) => {
  if (filterKey === 'none') {
    selectedQuickFilterMap.value.clear();
    selectedQuickFilterMap.value.set('none', { key: 'none' });
    clearSearchPanel();
    return;
  }

  if (filterKey === 'createdBy') {
    updateSearchPanelConfigs([{ valueKey: 'createdBy', value: currentUserId.value }]);
    return;
  }

  if (filterKey === 'lastModifiedBy') {
    updateSearchPanelConfigs([{ valueKey: 'lastModifiedBy', value: currentUserId.value }]);
    return;
  }

  if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(filterKey)) {
    quickDateRangeMap.value.clear();
    const dateRange = formatDateRange(filterKey);
    quickDateRangeMap.value.set(filterKey, dateRange);
    updateSearchPanelConfigs([{ valueKey: 'createdDate', value: dateRange }]);
  }
};

/**
 * Update search panel configurations.
 * @param configs - Configuration array
 */
const updateSearchPanelConfigs = (configs: any[]) => {
  if (typeof searchPanelRef.value?.setConfigs === 'function') {
    searchPanelRef.value.setConfigs(configs);
  }
};

/**
 * Clear search panel.
 */
const clearSearchPanel = () => {
  if (typeof searchPanelRef.value?.clear === 'function') {
    searchPanelRef.value.clear();
  }
};

/**
 * Format date range based on quick filter key.
 * @param filterKey - Quick filter key
 * @returns Formatted date range array
 */
const formatDateRange = (filterKey: MenuItem['key']) => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (filterKey === 'lastDay') {
    startDate = dayjs().startOf('date');
    endDate = dayjs();
  }

  if (filterKey === 'lastThreeDays') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (filterKey === 'lastWeek') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [
    startDate ? startDate.format(DATE_TIME_FORMAT) : '',
    endDate ? endDate.format(DATE_TIME_FORMAT) : ''
  ];
};

/**
 * Handle create analysis button click.
 */
const handleCreateAnalysis = () => {
  emit('add');
};

/**
 * Handle refresh button click.
 */
const handleRefresh = () => {
  emit('change', getSearchFilters());
};

/**
 * Handle search panel change events.
 * @param searchData - Search criteria data
 * @param headers - Optional headers
 * @param changedKey - Changed field key
 */
const handleSearchPanelChange = (
  searchData: SearchCriteria[],
  _headers?: { [key: string]: string },
  changedKey?: string
) => {
  searchFilters.value = searchData;

  if (changedKey === 'createdDate') {
    selectedQuickFilterMap.value.delete('lastDay');
    selectedQuickFilterMap.value.delete('lastThreeDays');
    selectedQuickFilterMap.value.delete('lastWeek');
  }
};

/**
 * Get current search filters.
 * @returns Cloned search filters array
 */
const getSearchFilters = () => {
  const clonedFilters: SearchCriteria[] = cloneDeep(searchFilters.value);
  return clonedFilters;
};

/**
 * Initialize search panel with saved data from database.
 */
const initializeSearchPanel = async () => {
  if (!searchDatabase) {
    searchDatabase = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  const [, savedData] = await searchDatabase.get(databaseParamsKey.value);
  const databaseData = savedData?.data;

  if (databaseData) {
    loadSavedSearchData(databaseData);
  } else {
    resetSearchData();
    resetSearchPanelConfigs();
  }
};

/**
 * Load saved search data from database.
 * @param databaseData - Data from database
 */
const loadSavedSearchData = (databaseData: any) => {
  const searchValueMap: { [key: string]: string | string[] } = {};
  const quickFilterMap: { [key: string]: string | string[] } = {};

  if (Object.prototype.hasOwnProperty.call(databaseData, 'a')) {
    searchFilters.value = databaseData.a || [];
    const quickFilterKeys = ['lastModifiedBy'];
    const dateTimeKeys = ['createdDate'];
    const dateTimeMap: { [key: string]: string[] } = {};

    searchFilters.value.every(({ key, value }) => {
      if (dateTimeKeys.includes(key)) {
        if (dateTimeMap[key]) {
          dateTimeMap[key].push(value);
        } else {
          dateTimeMap[key] = [value];
        }
      } else if (quickFilterKeys.includes(key)) {
        quickFilterMap[key] = value;
      } else {
        searchValueMap[key] = value;
      }
      return true;
    });

    // Process date time ranges
    Object.entries(dateTimeMap).every(([key, [date1, date2]]: [string, string[]]) => {
      const sortedDates: string[] = [];
      if (date1 && date2) {
        if (dayjs(date1).isBefore(dayjs(date2))) {
          sortedDates[0] = date1;
          sortedDates[1] = date2;
        } else {
          sortedDates[0] = date2;
          sortedDates[1] = date1;
        }
      }

      if (sortedDates.length === 2) {
        searchValueMap[key] = sortedDates;
      }
      return true;
    });
  } else {
    searchFilters.value = [];
  }

  // Handle quick filters
  const quickFilterKeys = Object.keys(quickFilterMap);
  if (quickFilterKeys.length) {
    quickFilterKeys.forEach(key => {
      selectedQuickFilterMap.value.set(key, { key });
    });
    if (!Object.keys(searchValueMap).length) {
      emit('change', searchFilters.value);
    }
  }

  // Set search panel configurations
  const searchValueKeys = Object.keys(searchValueMap);
  if (searchValueKeys.length) {
    const configs = searchOptions.map(item => ({
      valueKey: item.valueKey,
      value: searchValueMap[item.valueKey]
    }));
    updateSearchPanelConfigs(configs);
  }
};

/**
 * Reset search panel configurations to default.
 */
const resetSearchPanelConfigs = () => {
  const configs = searchOptions.map(item => ({
    valueKey: item.valueKey,
    value: undefined
  }));
  updateSearchPanelConfigs(configs);
};

/**
 * Reset all search data to initial state.
 */
const resetSearchData = () => {
  quickDateRangeMap.value.clear();
  selectedQuickFilterMap.value.clear();
  searchFilters.value = [];
};

/**
 * Handle search filters change and update quick filter states.
 */
const handleSearchFiltersChange = () => {
  const currentFilters = searchFilters.value;

  if (!currentFilters.length) {
    selectedQuickFilterMap.value.clear();
    selectedQuickFilterMap.value.set('none', { key: 'none' });
    emit('change', getSearchFilters());
  } else {
    selectedQuickFilterMap.value.delete('none');
    updateQuickFilterStates(currentFilters);
    emit('change', getSearchFilters());
  }

  saveSearchDataToDatabase(currentFilters);
};

/**
 * Update quick filter states based on current filters.
 * @param currentFilters - Current search filters
 */
const updateQuickFilterStates = (currentFilters: SearchCriteria[]) => {
  // Update created by filter state
  const createdByFilter = currentFilters.find(item => item.key === 'createdBy')?.value;
  if (createdByFilter && createdByFilter === currentUserId.value) {
    selectedQuickFilterMap.value.set('createdBy', { key: 'createdBy' });
  } else {
    selectedQuickFilterMap.value.delete('createdBy');
  }

  // Update last modified by filter state
  const lastModifiedByFilter = currentFilters.find(item => item.key === 'lastModifiedBy')?.value;
  if (lastModifiedByFilter && lastModifiedByFilter === currentUserId.value) {
    selectedQuickFilterMap.value.set('lastModifiedBy', { key: 'lastModifiedBy' });
  } else {
    selectedQuickFilterMap.value.delete('lastModifiedBy');
  }

  // Update date range filter states
  if (quickDateRangeMap.value.size > 0) {
    selectedQuickFilterMap.value.delete('lastDay');
    selectedQuickFilterMap.value.delete('lastThreeDays');
    selectedQuickFilterMap.value.delete('lastWeek');

    const createdDateStart = currentFilters.find(item =>
      item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.GreaterThanEqual
    )?.value;
    const createdDateEnd = currentFilters.find(item =>
      item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.LessThanEqual
    )?.value;
    const currentDateRange = [createdDateStart, createdDateEnd];

    const dateRangeEntries = quickDateRangeMap.value.entries();
    for (const [key, value] of dateRangeEntries) {
      if (isEqual(value, currentDateRange)) {
        selectedQuickFilterMap.value.set(key, { key });
      }
    }
    quickDateRangeMap.value.clear();
  }
};

/**
 * Save search data to database.
 * @param currentFilters - Current search filters
 */
const saveSearchDataToDatabase = (currentFilters: SearchCriteria[]) => {
  if (searchDatabase) {
    const databaseData: { filter?: SearchCriteria[]; } = {};
    if (currentFilters.length) {
      databaseData.filter = cloneDeep(currentFilters);
    }

    if (Object.keys(databaseData).length) {
      searchDatabase.add({
        id: databaseParamsKey.value,
        data: databaseData
      });
    } else {
      searchDatabase.delete(databaseParamsKey.value);
    }
  }
};

/**
 * Get current user ID.
 */
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

/**
 * Generate base database key.
 */
const databaseBaseKey = computed(() => {
  let key = '';
  if (currentUserId.value) {
    key = currentUserId.value;
  }

  if (props.projectId) {
    key += props.projectId;
  }

  return key;
});

/**
 * Generate database parameters key.
 */
const databaseParamsKey = computed(() => {
  return btoa(databaseBaseKey.value + 'function_analysis');
});

/**
 * Generate database count key.
 */
const databaseCountKey = computed(() => {
  return btoa(databaseBaseKey.value + 'count');
});

// Lifecycle hooks
onMounted(async () => {
  // Watch database key changes to initialize search panel
  watch([() => databaseParamsKey.value, () => databaseCountKey.value], ([paramsKey, countKey]) => {
    if (!paramsKey || !countKey) {
      return;
    }
    initializeSearchPanel();
  }, { immediate: true });

  // Watch search filters and quick filter changes
  watch(
    [
      () => searchFilters.value,
      () => selectedQuickFilterMap.value
    ], () => {
      handleSearchFiltersChange();
    }, { immediate: false, deep: false });
});

const menuItems: MenuItem[] = [
  {
    key: 'none',
    name: t('functionAnalysis.searchPanel.all')
  },
  {
    key: 'createdBy',
    name: t('functionAnalysis.searchPanel.myAdded')
  },
  {
    key: 'lastModifiedBy',
    name: t('functionAnalysis.searchPanel.myModified')
  },
  {
    key: 'lastDay',
    name: t('functionAnalysis.searchPanel.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('functionAnalysis.searchPanel.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('functionAnalysis.searchPanel.lastWeek')
  }
];

const searchOptions = [
  {
    type: 'input',
    valueKey: 'name',
    placeholder: t('functionAnalysis.searchPanel.searchOptions.namePlaceholder')
  },
  {
    type: 'select-user',
    valueKey: 'createdBy',
    placeholder: t('functionAnalysis.searchPanel.searchOptions.createdByPlaceholder'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [
      t('functionAnalysis.searchPanel.searchOptions.createdDatePlaceholder[0]'),
      t('functionAnalysis.searchPanel.searchOptions.createdDatePlaceholder[1]')
    ],
    showTime: true
  }
];

const sortMenuItems = [
  {
    key: 'name',
    name: t('functionAnalysis.searchPanel.sortOptions.name'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdByName',
    name: t('functionAnalysis.searchPanel.sortOptions.createdByName'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdDate',
    name: t('functionAnalysis.searchPanel.sortOptions.createdDate'),
    orderSort: PageQuery.OrderSort.Asc
  }];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ t('functionAnalysis.searchPanel.quickQuery') }}</span>
          <Colon />
        </div>
        <div class="flex  flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': selectedQuickFilterMap.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
            @click="handleQuickFilterClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-between">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchOptions"
        @change="handleSearchPanelChange">
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2">
        <Button
          class="flex-shrink-0 flex items-center"
          type="primary"
          size="small"
          @click="handleCreateAnalysis">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">{{ t('functionAnalysis.searchPanel.addAnalysis') }}</span>
          </div>
        </Button>

        <DropdownSort
          :orderBy="props.orderBy"
          :orderSort="props.orderSort"
          :menuItems="sortMenuItems"
          @click="handleSortChange">
          <Button
            size="small"
            class="flex items-center cursor-pointer ">
            <Icon icon="icon-biaotoupaixu" class="text-3.5" />
            <span class="ml-1">{{ t('actions.sort') }}</span>
          </Button>
        </DropdownSort>

        <Button
          class="flex-shrink-0 flex items-center"
          size="small"
          @click="handleRefresh">
          <IconRefresh class="text-4 flex-shrink-0" />
          {{ t('actions.refresh') }}
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}

.ant-tag {
  background-color: #fff;
}

.ant-tag.tag-checked {
  background-color: rgba(255, 129, 0, 60%);
  color: #fff;
}

.ant-tag.tag-checked :deep(.ant-tag-close-icon)>svg {
  color: #fff;
}

:deep(.sprint.tag-checked) {
  background-color: rgba(255, 129, 0, 60%);
}

.tag.tag-checked {
  background-color: #4ea0fd;
}
</style>
