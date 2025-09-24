<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { PageQuery, SearchCriteria, XCanDexie } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { MenuItem } from '@/views/task/analysis/list/types';

// Type Definitions
type Props = {
  collapse: boolean;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  orderBy: 'createdByName' | 'lastModifiedByName';
  orderSort: PageQuery.OrderSort;
  groupKey: 'none' | 'createdByName' | 'lastModifiedByName';
}

// Props and Emits
const props = withDefaults(defineProps<Props>(), {
  collapse: false,
  viewMode: undefined,
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
  (e: 'update:orderBy', value: 'createdByName' | 'lastModifiedByName'): void;
  (e: 'update:orderSort', value: PageQuery.OrderSort): void;
  (e: 'update:groupKey', value: 'none' | 'createdByName' | 'lastModifiedByName'): void;
  (e: 'update:visible', value: boolean): void;
}>();

// Composables and External Dependencies
const { t } = useI18n();

/**
 * Database instance for storing search parameters
 */
// eslint-disable-next-line no-undef
let searchParametersDatabase: XCanDexie<{ id: string; data: any; }>;

/**
 * Reference to the search panel component
 */
const searchPanelComponentRef = ref();

/**
 * Map storing quick date search converted time ranges
 */
const quickDateSearchMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());

/**
 * Map storing selected menu items for quick search
 */
const selectedQuickSearchItems = ref(new Map<string, Omit<MenuItem, 'name'>>());

/**
 * Current search filter criteria
 */
const currentSearchFilters = ref<SearchCriteria[]>([]);

/**
 * Handle sorting configuration changes
 * @param {object} data - Sorting data containing orderBy and orderSort
 */
const handleSortingChange = (data: { orderBy: 'createdByName' | 'lastModifiedByName' ; orderSort: PageQuery.OrderSort; }) => {
  emit('update:orderBy', data.orderBy);
  emit('update:orderSort', data.orderSort);
};

/**
 * Handle quick search menu item click events
 * @param {MenuItem} menuItem - The clicked menu item
 */
const handleQuickSearchMenuItemClick = (menuItem: MenuItem) => {
  const itemKey = menuItem.key;

  // Handle deselection if item is already selected
  if (selectedQuickSearchItems.value.has(itemKey)) {
    // Do nothing if "All" button is clicked again
    if (itemKey === 'none') {
      return;
    }

    // Remove the selected item
    selectedQuickSearchItems.value.delete(itemKey);

    // Clear corresponding search panel configurations
    if (itemKey === 'createdBy') {
      clearSearchPanelConfig(['createdBy']);
      return;
    }

    if (itemKey === 'lastModifiedBy') {
      clearSearchPanelConfig(['lastModifiedBy']);
      return;
    }

    if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(itemKey)) {
      quickDateSearchMap.value.clear();
      clearSearchPanelConfig(['createdDate']);
    }
    return;
  }

  // Handle "All" selection - clear all other selections
  if (itemKey === 'none') {
    selectedQuickSearchItems.value.clear();
    selectedQuickSearchItems.value.set('none', { key: 'none' });

    // Clear search panel
    if (typeof searchPanelComponentRef.value?.clear === 'function') {
      searchPanelComponentRef.value.clear();
    }
    return;
  }

  // Handle other menu item selections
  if (itemKey === 'createdBy') {
    setSearchPanelConfig([{ valueKey: 'createdBy', value: currentUserId.value }]);
    return;
  }

  if (itemKey === 'lastModifiedBy') {
    setSearchPanelConfig([{ valueKey: 'lastModifiedBy', value: currentUserId.value }]);
    return;
  }

  if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(itemKey)) {
    quickDateSearchMap.value.clear();
    quickDateSearchMap.value.set(itemKey, formatDateRange(itemKey));
    setSearchPanelConfig([{ valueKey: 'createdDate', value: quickDateSearchMap.value.get(itemKey) }]);
  }
};

/**
 * Format date range based on quick search key
 * @param {string} key - The quick search key
 * @returns {string[]} Formatted date range array
 */
const formatDateRange = (key: MenuItem['key']) => {
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

  return [startDate ? startDate.format(DATE_TIME_FORMAT) : '', endDate ? endDate.format(DATE_TIME_FORMAT) : ''];
};

/**
 * Handle create button click
 */
const handleCreateClick = () => {
  emit('add');
};

/**
 * Handle refresh button click
 */
const handleRefreshClick = () => {
  emit('change', buildSearchCriteria());
};

/**
 * Handle search panel configuration changes
 * @param {SearchCriteria[]} data - New search criteria
 * @param {object} headers - Optional headers
 * @param {string} key - Optional key that triggered the change
 */
const handleSearchPanelChange = (
  data: SearchCriteria[],
  _headers?: { [key: string]: string },
  key?: string
) => {
  currentSearchFilters.value = data;

  // Clear quick date search selections when manual date selection is made
  if (key === 'createdDate') {
    selectedQuickSearchItems.value.delete('lastDay');
    selectedQuickSearchItems.value.delete('lastThreeDays');
    selectedQuickSearchItems.value.delete('lastWeek');
  }
};

/**
 * Build complete search criteria from all active filters
 * @returns {SearchCriteria[]} Complete search criteria array
 */
const buildSearchCriteria = () => {
  const searchCriteria: SearchCriteria[] = cloneDeep(currentSearchFilters.value);
  return searchCriteria;
};

/**
 * Initialize search panel with saved data from database
 */
const initializeSearchPanel = async () => {
  if (!searchParametersDatabase) {
    searchParametersDatabase = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  // Load saved search parameters from database
  const [, savedData] = await searchParametersDatabase.get(databaseParamsKey.value);
  const databaseData = savedData?.data;

  if (databaseData) {
    const searchPanelValueMap: { [key: string]: string | string[] } = {};
    const nonSearchPanelValueMap: { [key: string]: string | string[] } = {};

    // Process saved filters
    if (Object.prototype.hasOwnProperty.call(databaseData, 'filters')) {
      currentSearchFilters.value = databaseData.filters || [];
      const nonSearchPanelKeys = ['lastModifiedBy'];
      const dateTimeKeys = ['createdDate'];
      const dateTimeValueMap: { [key: string]: string[] } = {};

      currentSearchFilters.value.every(({ key, value }) => {
        if (key && dateTimeKeys.includes(key)) {
          if (dateTimeValueMap[key]) {
            dateTimeValueMap[key].push(value as string);
          } else {
            dateTimeValueMap[key] = [value as string];
          }
        } else if (key && nonSearchPanelKeys.includes(key)) {
          nonSearchPanelValueMap[key] = value;
        } else if (key) {
          searchPanelValueMap[key] = value;
        }
        return true;
      });

      // Process date time ranges
      Object.entries(dateTimeValueMap).every(([key, [date1, date2]]: [string, string[]]) => {
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
          searchPanelValueMap[key] = dateTimes;
        }
        return true;
      });
    } else {
      currentSearchFilters.value = [];
    }

    // Set up non-search panel quick filters
    const nonSearchPanelKeys = Object.keys(nonSearchPanelValueMap);
    if (nonSearchPanelKeys.length) {
      nonSearchPanelKeys.forEach(key => {
        selectedQuickSearchItems.value.set(key as any, { key: key as any });
      });
      if (!Object.keys(searchPanelValueMap).length) {
        emit('change', currentSearchFilters.value);
      }
    }

    // Configure search panel with saved values
    configureSearchPanelWithValues(searchPanelValueMap);
    return;
  }

  // No saved data, reset to defaults
  resetAllSearchStates();
  resetSearchPanelConfiguration();
};

/**
 * Configure search panel with saved values
 * @param {object} valueMap - Map of values to configure
 */
const configureSearchPanelWithValues = (valueMap: { [key: string]: string | string[] }) => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    const valueMapKeys = Object.keys(valueMap);
    if (valueMapKeys.length) {
      const configs = searchPanelOptions.map(item => ({
        valueKey: item.valueKey,
        value: valueMap[item.valueKey]
      }));
      searchPanelComponentRef.value.setConfigs(configs);
    }
  }
};

/**
 * Clear search panel configuration for specified keys
 * @param {string[]} keys - Keys to clear
 */
const clearSearchPanelConfig = (keys: string[]) => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    const configs = keys.map(key => ({ valueKey: key, value: undefined }));
    searchPanelComponentRef.value.setConfigs(configs);
  }
};

/**
 * Set search panel configuration
 * @param {object[]} configs - Configuration objects
 */
const setSearchPanelConfig = (configs: { valueKey: string; value: any }[]) => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    searchPanelComponentRef.value.setConfigs(configs);
  }
};

/**
 * Reset search panel configuration to default state
 */
const resetSearchPanelConfiguration = () => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    const configs = searchPanelOptions.map(item => ({
      valueKey: item.valueKey,
      value: undefined
    }));
    searchPanelComponentRef.value.setConfigs(configs);
  }
};

/**
 * Reset all search states to default values
 */
const resetAllSearchStates = () => {
  quickDateSearchMap.value.clear();
  selectedQuickSearchItems.value.clear();
  currentSearchFilters.value = [];
};

// Lifecycle Hooks
onMounted(async () => {
  // Watch for database key changes and initialize
  watch([
    () => databaseParamsKey.value,
    () => databaseCountKey.value
  ], ([key1, key2, key3]) => {
    if (!key1 || !key2 || !key3) {
      return;
    }
    initializeSearchPanel();
  }, { immediate: true });

  // Watch for search criteria changes and update UI accordingly
  watch(
    [
      () => currentSearchFilters.value,
      () => selectedQuickSearchItems.value
    ], () => {
      const currentFilters = currentSearchFilters.value;

      // Check if any filters are active
      const hasActiveFilters = currentFilters.length;

      if (!hasActiveFilters) {
        // No active filters, show "All" option
        selectedQuickSearchItems.value.clear();
        selectedQuickSearchItems.value.set('none', { key: 'none' });
        emit('change', buildSearchCriteria());
      } else {
        // Active filters, update quick search selections
        updateQuickSearchSelections(currentFilters);
        emit('change', buildSearchCriteria());
      }

      // Save current state to database
      saveSearchStateToDatabase();
    }, { immediate: false, deep: false });
});

/**
 * Get current user ID
 */
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

/**
 * Generate base key for database operations
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
 * Generate database key for search parameters
 */
const databaseParamsKey = computed(() => {
  return btoa(databaseBaseKey.value + 'task_analysis');
});

/**
 * Generate database key for count data
 */
const databaseCountKey = computed(() => {
  return btoa(databaseBaseKey.value + 'count');
});

/**
 * Update quick search selections based on current filters
 * @param {SearchCriteria[]} filters - Current search filters
 */
const updateQuickSearchSelections = (filters: SearchCriteria[]) => {
  // Remove "All" option when filters are active
  selectedQuickSearchItems.value.delete('none');

  // Update created by selection
  const createdBy = filters.find(item => item.key === 'createdBy')?.value;
  if (createdBy && createdBy === currentUserId.value) {
    selectedQuickSearchItems.value.set('createdBy', { key: 'createdBy' });
  } else {
    selectedQuickSearchItems.value.delete('createdBy');
  }

  // Update last modified by selection
  const lastModifiedBy = filters.find(item => item.key === 'lastModifiedBy')?.value;
  if (lastModifiedBy && lastModifiedBy === currentUserId.value) {
    selectedQuickSearchItems.value.set('lastModifiedBy', { key: 'lastModifiedBy' });
  } else {
    selectedQuickSearchItems.value.delete('lastModifiedBy');
  }

  // Update date range selections
  if (quickDateSearchMap.value.size > 0) {
    selectedQuickSearchItems.value.delete('lastDay');
    selectedQuickSearchItems.value.delete('lastThreeDays');
    selectedQuickSearchItems.value.delete('lastWeek');

    const createdDateStart = filters.find(
      item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.GreaterThanEqual)?.value;
    const createdDateEnd = filters.find(
      item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.LessThanEqual)?.value;
    const dateString = [createdDateStart, createdDateEnd];

    const entries = quickDateSearchMap.value.entries();
    for (const [key, value] of entries) {
      if (isEqual(value, dateString)) {
        selectedQuickSearchItems.value.set(key, { key });
      }
    }
    quickDateSearchMap.value.clear();
  }
};

/**
 * Save current search state to database
 */
const saveSearchStateToDatabase = () => {
  if (searchParametersDatabase) {
    const databaseData: {
      filters?: SearchCriteria[];
    } = {};

    if (currentSearchFilters.value.length) {
      databaseData.filters = cloneDeep(currentSearchFilters.value);
    }

    if (Object.keys(databaseData).length) {
      searchParametersDatabase.add({
        id: databaseParamsKey.value,
        data: databaseData
      });
    } else {
      searchParametersDatabase.delete(databaseParamsKey.value);
    }
  }
};

/**
 * Quick search menu items configuration
 */
const quickSearchMenuItems: MenuItem[] = [
  {
    key: 'none',
    name: t('quickSearch.all')
  },
  {
    key: 'createdBy',
    name: t('quickSearch.addByMe')
  },
  {
    key: 'lastModifiedBy',
    name: t('quickSearch.modifiedByMe')
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

/**
 * Search panel options configuration
 */
const searchPanelOptions = [
  {
    type: 'input' as const,
    valueKey: 'name',
    placeholder: t('taskAnalysis.searchNameDesc')
  },
  {
    type: 'select-user' as const,
    valueKey: 'createdBy',
    placeholder: t('taskAnalysis.selectCreator'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    placeholder: [t('taskAnalysis.createTimeFrom'), t('taskAnalysis.createTimeTo')],
    showTime: true
  }
];

/**
 * Sort menu items configuration
 */
const sortMenuItems = [
  {
    key: 'name',
    name: t('taskAnalysis.columns.name'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdByName',
    name: t('taskAnalysis.sortByCreator'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdDate',
    name: t('taskAnalysis.sortByCreateTime'),
    orderSort: PageQuery.OrderSort.Asc
  }
];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 mt-1.5 rounded-full"></div>
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ t('quickSearch.title') }}</span>
          <Colon />
        </div>
        <div class="flex  flex-wrap ml-2">
          <div
            v-for="item in quickSearchMenuItems"
            :key="item.key"
            :class="{ 'active-key': selectedQuickSearchItems.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold text-3"
            @click="handleQuickSearchMenuItemClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-between mt-3">
      <SearchPanel
        ref="searchPanelComponentRef"
        :options="searchPanelOptions"
        @change="handleSearchPanelChange">
      </SearchPanel>

      <div class="flex-shrink-0 flex items-center space-x-2">
        <Button
          class="flex-shrink-0 flex items-center"
          type="primary"
          size="small"
          @click="handleCreateClick">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">{{ t('taskAnalysis.addAnalysis') }}</span>
          </div>
        </Button>

        <DropdownSort
          :orderBy="props.orderBy"
          :orderSort="props.orderSort"
          :menuItems="sortMenuItems"
          @click="handleSortingChange">
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
          @click="handleRefreshClick">
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
