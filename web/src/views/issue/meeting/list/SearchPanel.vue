<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { TaskMeetingType } from '@/enums/enums';
import { LoadingProps } from '@/types/types';
import {
  QuickSearchOptions,
  createAuditOptions,
  createTimeOptions,
  type QuickSearchConfig
} from '@/components/quickSearch';

// TYPES & INTERFACES
type OrderByKey = string;

// COMPONENT PROPS & EMITS
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: PageQuery): void;
  (e: 'refresh'): void;
}>();

// COMPOSABLES
const { t } = useI18n();
const currentUser = ref(appContext.getUser());

// REACTIVE STATE
const searchPanelRef = ref();

const currentOrderBy = ref();
const currentOrderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const associatedFilters = ref<SearchCriteria[]>([]);

// CONSTANTS
const ASSOCIATED_FILTER_KEYS = ['createdDate', 'moderatorId', 'createdBy'];

/**
 * Quick search configuration for meeting search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Audit information options
  auditOptions: createAuditOptions([
    {
      key: 'presideByMe',
      name: t('quickSearch.presideByMe'),
      fieldKey: 'moderatorId'
    },
    {
      key: 'createdByMe',
      name: t('quickSearch.createdByMe'),
      fieldKey: 'createdBy'
    },
    {
      key: 'myModified',
      name: t('quickSearch.modifiedByMe'),
      fieldKey: 'lastModifiedBy'
    }
  ], String(currentUser.value?.id || '')),
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

// SEARCH PANEL CONFIGURATION
const searchPanelOptions = [
  {
    valueKey: 'subject',
    type: 'input' as const,
    placeholder: t('meeting.placeholder.searchSubject'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'type',
    type: 'select-enum' as const,
    enumKey: TaskMeetingType,
    allowClear: true,
    placeholder: t('meeting.placeholder.selectMeetingType')
  },
  {
    valueKey: 'moderatorId',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('meeting.placeholder.selectModeratorSearch')
  },
  {
    valueKey: 'createdBy',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('common.placeholders.selectCreator')
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    placeholder: [
      t('commons.placeholders.selectCreatedDate.0'),
      t('commons.placeholders.selectCreatedDate.1')
    ],
    showTime: true
  }
];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: PageQuery.OrderSort;
}[] = [
  {
    name: t('meeting.actions.sorts.byCreateDate'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('meeting.actions.sorts.byCreator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('meeting.actions.sorts.bySubject'),
    key: 'subject',
    orderSort: PageQuery.OrderSort.Asc
  }
];

/**
 * Combines all search parameters into a single object
 * @returns Combined search parameters
 */
const buildSearchParameters = (): PageQuery => {
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
 * Handles search panel filter changes
 * @param filterData - Array of filter objects from search panel
 */
const handleSearchFilterChange = (filterData: SearchCriteria[]) => {
  // Merge search panel filters with quick search filters
  const quickSearchFields = ['moderatorId', 'createdBy', 'lastModifiedBy', 'createdDate'];
  const currentQuickSearchFilters = quickSearchFilters.value.filter(f => f.key && quickSearchFields.includes(f.key as string));
  const searchPanelFilters = filterData.filter(f => f.key && !quickSearchFields.includes(f.key as string));

  searchFilters.value = [...currentQuickSearchFilters, ...searchPanelFilters];
  associatedFilters.value = filterData.filter(item => item.key && ASSOCIATED_FILTER_KEYS.includes(item.key as string));

  emits('change', buildSearchParameters());
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
  emits('change', buildSearchParameters());
};

/**
 * Handles sorting changes
 * @param sortData - Sort configuration object
 */
const handleSortChange = (sortData: { orderBy: string; orderSort: string }) => {
  currentOrderBy.value = sortData.orderBy;
  currentOrderSort.value = sortData.orderSort;
  emits('change', buildSearchParameters());
};

/**
 * Handles refresh action
 */
const handleRefresh = () => {
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
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="handleSearchFilterChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/issue#meeting?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('meeting.actions.addMeeting') }}</span>
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
