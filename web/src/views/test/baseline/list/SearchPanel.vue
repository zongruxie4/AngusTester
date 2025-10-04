<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import { LoadingProps } from '@/types/types';
import { QuickSearchOptions, createAuditOptions, createTimeOptions, createEnumOptions, type QuickSearchConfig } from '@/components/quickSearch';

const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

// Type Definitions
type OrderByKey = string;

// Emits Definition
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: { orderBy?: string; orderSort?: PageQuery.OrderSort; filters: SearchCriteria[]; }):void,
  (e: 'refresh'):void
}>();

// Reactive Data
const currentUserInfo = ref(appContext.getUser());
const searchPanelRef = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const associatedFilters = ref<SearchCriteria[]>([]);
const currentOrderBy = ref();
const currentOrderSort = ref();

// Configuration Constants
const ASSOCIATED_KEYS = ['ownerId'];

/**
 * Quick search configuration for baseline search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Audit information options
  auditOptions: createAuditOptions([
    {
      key: 'myCreated',
      name: t('quickSearch.createdByMe'),
      fieldKey: 'createdBy'
    },
    {
      key: 'myModified',
      name: t('quickSearch.modifiedByMe'),
      fieldKey: 'lastModifiedBy'
    }
  ], String(currentUserInfo.value?.id || '')),
  // Enum status options for established status
  enumOptions: createEnumOptions([
    {
      key: 'established=1',
      name: t('testCaseBaseline.list.established'),
      fieldKey: 'established',
      fieldValue: 'true'
    },
    {
      key: 'established=0',
      name: t('testCaseBaseline.list.notEstablished'),
      fieldKey: 'established',
      fieldValue: 'false'
    }
  ]),
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

// Search Panel Configuration
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('testCaseBaseline.list.searchBaselineNameDescription'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('testCaseBaseline.list.selectOwner'),
    maxlength: 100
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [
      t('testCaseBaseline.list.createdTimeGreaterEqual'),
      t('testCaseBaseline.list.createdTimeLessEqual')
    ],
    showTime: true,
    allowClear: true
  }
];

// Sort Menu Configuration
const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: PageQuery.OrderSort;
}[] = [
  {
    name: t('testCaseBaseline.list.sortByName'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('testCaseBaseline.list.sortByAddPerson'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('testCaseBaseline.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Asc
  }
];

/**
 * Get search parameters for API calls
 * @returns Search parameters object
 */
const getSearchParameters = () => {
  return {
    filters: [
      ...quickSearchFilters.value,
      ...searchFilters.value,
      ...associatedFilters.value
    ],
    orderBy: currentOrderBy.value,
    orderSort: currentOrderSort.value
  };
};

/**
 * Handle search panel change
 * @param data - Search criteria data
 */
const handleSearchPanelChange = (data: SearchCriteria[]) => {
  // Merge search panel filters with quick search filters
  const quickSearchFields = ['createdBy', 'lastModifiedBy', 'established', 'createdDate'];
  const currentQuickSearchFilters = quickSearchFilters.value.filter(f => f.key && quickSearchFields.includes(f.key as string));
  const searchPanelFilters = data.filter(f => f.key && !quickSearchFields.includes(f.key as string));

  searchFilters.value = [...currentQuickSearchFilters, ...searchPanelFilters];
  associatedFilters.value = data.filter(item => ASSOCIATED_KEYS.includes(item.key));

  emits('change', getSearchParameters());
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
  emits('change', getSearchParameters());
};

/**
 * Handle sort change
 * @param sortData - Sort configuration data
 */
const handleSortChange = (sortData) => {
  currentOrderBy.value = sortData.orderBy;
  currentOrderSort.value = sortData.orderSort;
  emits('change', getSearchParameters());
};

/**
 * Handle refresh button click
 */
const handleRefreshClick = () => {
  emits('refresh');
};
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions as any"
        class="flex-1 mr-3.5"
        @change="handleSearchPanelChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/test#baseline?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('testCaseBaseline.list.addBaseline') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="currentOrderBy"
          v-model:orderSort="currentOrderSort"
          :menuItems="sortMenuItems"
          @click="handleSortChange">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('actions.sort') }}</span>
          </div>
        </DropdownSort>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="handleRefreshClick">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
              <span class="ml-1">{{ t('actions.refresh') }}</span>
            </div>
          </template>
        </IconRefresh>
      </div>
    </div>
  </div>
</template>
<style scoped>
/* Styles are now handled by QuickSearchOptions component */
</style>
