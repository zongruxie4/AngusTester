<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { XCanDexie, SearchCriteria, PageQuery } from '@xcan-angus/infra';
import { QuickSearchOptions, createAuditOptions, createTimeOptions, type QuickSearchConfig } from '@/components/quickSearch';

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
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);

/**
 * Quick search configuration for analysis search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Audit information options
  auditOptions: createAuditOptions([
    {
      key: 'myAdded',
      name: t('quickSearch.createdByMe'),
      fieldKey: 'createdBy'
    },
    {
      key: 'myModified',
      name: t('quickSearch.modifiedByMe'),
      fieldKey: 'lastModifiedBy'
    }
  ], String(currentUserId.value || '')),
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
  }
}));

/**
 * Handle sort configuration changes.
 * @param sortData - Sort configuration data
 */
const handleSortChange = (sortData: { orderBy: 'createsDate' | 'createdByName' ; orderSort: PageQuery.OrderSort; }) => {
  emit('update:orderBy', sortData.orderBy as 'createdByName');
  emit('update:orderSort', sortData.orderSort);
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
  _changedKey?: string
) => {
  // Merge search panel filters with quick search filters
  const quickSearchFields = ['createdBy', 'lastModifiedBy', 'createdDate'];
  const currentQuickSearchFilters = quickSearchFilters.value.filter(f => f.key && quickSearchFields.includes(f.key as string));
  const searchPanelFilters = searchData.filter(f => f.key && !quickSearchFields.includes(f.key as string));

  searchFilters.value = [...currentQuickSearchFilters, ...searchPanelFilters];
};

/**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]): void => {
  // Update quick search filters
  quickSearchFilters.value = searchCriteria;

  // Emit change event with current params
  emit('change', getSearchFilters());
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
  if (Object.prototype.hasOwnProperty.call(databaseData, 'a')) {
    searchFilters.value = databaseData.a || [];
  } else {
    searchFilters.value = [];
  }

  emit('change', getSearchFilters());
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
  searchFilters.value = [];
  quickSearchFilters.value = [];
};

/**
 * Handle search filters change and update quick filter states.
 */
const handleSearchFiltersChange = () => {
  const currentFilters = searchFilters.value;
  emit('change', getSearchFilters());
  saveSearchDataToDatabase(currentFilters);
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

  // Watch search filters changes
  watch(
    () => searchFilters.value,
    () => {
      handleSearchFiltersChange();
    }, { immediate: false, deep: false });
});

const searchOptions = [
  {
    type: 'input',
    valueKey: 'name',
    placeholder: t('common.placeholders.searchKeyword')
  },
  {
    type: 'select-user',
    valueKey: 'createdBy',
    placeholder: t('functionAnalysis.searchPanelOptions.createdByPlaceholder'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [
      t('functionAnalysis.searchPanelOptions.createdDatePlaceholder[0]'),
      t('functionAnalysis.searchPanelOptions.createdDatePlaceholder[1]')
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
    name: t('common.createdDate'),
    orderSort: PageQuery.OrderSort.Asc
  }];
</script>
<template>
  <div class="text-3 leading-5">
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <div class="flex justify-between">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchOptions as any"
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
