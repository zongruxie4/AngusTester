<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { PageQuery, SearchCriteria, XCanDexie } from '@xcan-angus/infra';
import { QuickSearchOptions, createAuditOptions, createTimeOptions, type QuickSearchConfig } from '@/components/quickSearch';
import dayjs from 'dayjs';

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
 * Current search filter criteria
 */
const currentSearchFilters = ref<SearchCriteria[]>([]);

/**
 * Quick search configuration for task analysis search panel
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
  ], String(props.userInfo?.id || '')),
  // Time options
  timeOptions: createTimeOptions([
    { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
    { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
    { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
  ], 'createdDate'),
  // External clear function
  externalClearFunction: () => {
    if (typeof searchPanelComponentRef.value?.clear === 'function') {
      searchPanelComponentRef.value.clear();
    }
  }
}));

/**
 * Handle sorting configuration changes
 * @param {object} data - Sorting data containing orderBy and orderSort
 */
const handleSortingChange = (data: { orderBy: 'createdByName' | 'lastModifiedByName' ; orderSort: PageQuery.OrderSort; }) => {
  emit('update:orderBy', data.orderBy);
  emit('update:orderSort', data.orderSort);
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
  _key?: string
) => {
  currentSearchFilters.value = data;
};

/**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]): void => {
  // Update current search filters with quick search criteria
  currentSearchFilters.value = searchCriteria;

  // Emit change event with current filters
  emit('change', buildSearchCriteria());
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

    // Process saved filters
    if (Object.prototype.hasOwnProperty.call(databaseData, 'filters')) {
      currentSearchFilters.value = databaseData.filters || [];
      const dateTimeKeys = ['createdDate'];
      const dateTimeValueMap: { [key: string]: string[] } = {};

      currentSearchFilters.value.every(({ key, value }) => {
        if (key && dateTimeKeys.includes(key)) {
          if (dateTimeValueMap[key]) {
            dateTimeValueMap[key].push(value as string);
          } else {
            dateTimeValueMap[key] = [value as string];
          }
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
  currentSearchFilters.value = [];
};

// Lifecycle Hooks
onMounted(async () => {
  // Watch for database key changes and initialize
  watch([
    () => databaseParamsKey.value,
    () => databaseCountKey.value
  ], ([key1, key2]) => {
    if (!key1 || !key2) {
      return;
    }
    initializeSearchPanel();
  }, { immediate: true });

  // Watch for search criteria changes and update UI accordingly
  watch(
    () => currentSearchFilters.value,
    () => {
      emit('change', buildSearchCriteria());
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
 * Search panel options configuration
 */
const searchPanelOptions = [
  {
    type: 'input' as const,
    valueKey: 'name',
    placeholder: t('common.placeholders.searchKeyword')
  },
  {
    type: 'select-user' as const,
    valueKey: 'createdBy',
    placeholder: t('common.placeholders.selectCreator'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    placeholder: [
      t('common.placeholders.selectCreatedDate.0'),
      t('common.placeholders.selectCreatedDate.1')
    ],
    showTime: true
  }
];

/**
 * Sort menu items configuration
 */
const sortMenuItems = [
  {
    key: 'name',
    name: t('common.name'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdByName',
    name: t('common.createdBy'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdDate',
    name: t('common.createdDate'),
    orderSort: PageQuery.OrderSort.Asc
  }
];
</script>
<template>
  <div class="text-3 leading-5">
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

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
            <span class="ml-1">{{ t('issueAnalysis.actions.addAnalysis') }}</span>
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
/* Styles are now handled by QuickSearchOptions component */

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
