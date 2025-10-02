<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, enumUtils, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { TaskSprintStatus } from '@/enums/enums';
import { Button } from 'ant-design-vue';
import { LoadingProps } from '@/types/types';
import { QuickSearchOptions, createAuditOptions, createTimeOptions, createEnumOptions, type QuickSearchConfig } from '@/components/quickSearch';

const { t } = useI18n();

// Component props
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

type OrderByKey = string;

// Component events definition
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: { orderBy?: string; orderSort?: PageQuery.OrderSort; filters: SearchCriteria[]; }): void,
  (e: 'refresh'): void
}>();

// User context and component references
const currentUser = ref(appContext.getUser());
const searchPanelRef = ref();

// Sprint status options for filtering
const sprintStatusOptions = ref<{ name: string; key: string }[]>([]);

// Search and filter state
const currentOrderBy = ref();
const currentOrderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const associatedFilters = ref<SearchCriteria[]>([]);

// Keys that require special handling for associated filters
const associatedFilterKeys = ['ownerId'];

/**
 * Quick search configuration for sprint search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Audit information options
  auditOptions: createAuditOptions([
    {
      key: 'myOwned',
      name: t('quickSearch.ownedByMe'),
      fieldKey: 'ownerId'
    },
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
  ], String(currentUser.value?.id || '')),
  // Enum status options for sprint status
  enumOptions: createEnumOptions(
    sprintStatusOptions.value.map(option => ({
      key: option.key,
      name: option.name,
      fieldKey: 'status',
      fieldValue: option.key
    }))
  ),
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
 * Load sprint status enum options for filtering
 */
const loadSprintStatusOptions = () => {
  const statusData = enumUtils.enumToMessages(TaskSprintStatus);
  sprintStatusOptions.value = statusData.map(item => ({
    name: item.message,
    key: item.value
  }));
};

// Search panel configuration options
const searchPanelConfig = [
  {
    valueKey: 'name',
    type: 'input' as const,
    placeholder: t('common.placeholders.searchKeyword'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'ownerId',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('common.placeholders.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date' as const,
    valueType: 'start',
    op: SearchCriteria.OpEnum.GreaterThanEqual,
    placeholder: t('common.placeholders.selectStartDate'),
    showTime: true,
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date' as const,
    valueType: 'start',
    op: SearchCriteria.OpEnum.LessThanEqual,
    placeholder: t('common.placeholders.selectDeadline'),
    showTime: true,
    allowClear: true
  }
];

// Sorting options configuration
const sortOptions: {
  name: string;
  key: OrderByKey;
  orderSort: PageQuery.OrderSort;
}[] = [
  {
    name: t('common.createdDate'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('common.creator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('common.owner'),
    key: 'ownerId',
    orderSort: PageQuery.OrderSort.Asc
  }
];

/**
 * Get current search parameters for API call
 * @returns Combined search parameters object
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
 * Handle search panel filter changes
 * @param filterData - New filter data from search panel
 */
const handleSearchPanelChange = (filterData: SearchCriteria[]) => {
  // Merge search panel filters with quick search filters
  const quickSearchFields = ['ownerId', 'createdBy', 'lastModifiedBy', 'status', 'createdDate'];
  const currentQuickSearchFilters = quickSearchFilters.value.filter(f => f.key && quickSearchFields.includes(f.key as string));
  const searchPanelFilters = filterData.filter(f => f.key && !quickSearchFields.includes(f.key as string));

  searchFilters.value = [...currentQuickSearchFilters, ...searchPanelFilters];
  associatedFilters.value = filterData.filter(item => associatedFilterKeys.includes(item.key || ''));

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
 * Handle sorting option changes
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
const handleRefresh = () => {
  emits('refresh');
};

// Initialize component
onMounted(() => {
  loadSprintStatusOptions();
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <div class="flex items-start justify-between mb-1.5 space-x-5">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelConfig as any"
        class="flex-1 mr-3.5"
        @change="handleSearchPanelChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/issue#sprint?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('sprint.actions.addSprint') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="currentOrderBy"
          v-model:orderSort="currentOrderSort"
          :menuItems="sortOptions"
          @click="handleSortChange">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('actions.sort') }}</span>
          </div>
        </DropdownSort>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="handleRefresh">
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
