<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { PageQuery, SearchCriteria, enumUtils, appContext } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { TestMenuKey } from '@/views/test/menu';
import {
  QuickSearchOptions, createAuditOptions, createTimeOptions, createEnumOptions, type QuickSearchConfig
} from '@/components/quickSearch';

// Props and Component Setup
const props = withDefaults(defineProps<{
  loading: boolean;
  userId: string;
}>(), {
  loading: false,
  userId: ''
});

const { t } = useI18n();

type OrderByKey = string;

// Event Emitters

const emits = defineEmits<{
  (e: 'change', value: { orderBy?: string; orderSort?: PageQuery.OrderSort; filters: SearchCriteria[]; }):void,
  (e: 'refresh'):void
}>();

// State Management
const currentUser = ref(appContext.getUser());
const searchPanelRef = ref();
const quickSearchOptionsRef = ref();
const planStatusOptions = ref<{name: string; key: string}[]>([]);

// Search State
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const associatedFilters = ref<SearchCriteria[]>([]);
const currentOrderBy = ref();
const currentOrderSort = ref();
const associatedKeys = ['ownerId', 'createdDate'];

/**
 * Quick search configuration for review search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Audit information options
  auditOptions: createAuditOptions([
    {
      key: 'ownedByMe',
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
  // Enum status options for plan status
  enumOptions: createEnumOptions(
    planStatusOptions.value.map(option => ({
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
 * Loads the plan status enum options for filtering
 */
const loadStatusEnumOptions = () => {
  const enumData = enumUtils.enumToMessages(FuncPlanStatus);
  planStatusOptions.value = enumData.map(option => ({ name: option.message, key: option.value }));
};

// Configuration Constants
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('common.placeholders.searchKeyword'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('common.placeholders.selectOwner'),
    maxlength: 100
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [
      t('common.placeholders.selectCreatedDateRange.0'),
      t('common.placeholders.selectCreatedDateRange.1')
    ],
    showTime: true,
    allowClear: true
  }
];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: PageQuery.OrderSort;
}[] = [
  {
    name: t('common.name'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('common.owner'),
    key: 'ownerId',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('common.creator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('common.createdDate'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Asc
  }
];

/**
 * Gets the current search parameters
 * @returns Object containing all search parameters
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
 * Handles search criteria changes from the search panel
 * @param searchCriteria - The new search criteria
 */
const handleSearchChange = (searchCriteria: SearchCriteria[], _headers?: { [key: string]: string }, key?: string) => {
  // Merge search panel filters with quick search filters
  if (key === 'ownerId') {
    quickSearchOptionsRef.value.clearSelectedMap(['ownedByMe']);
    quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'ownerId');
  }

  if (key === 'createdDate') {
    quickSearchOptionsRef.value.clearSelectedMap(['createdDate', 'last1Day', 'last3Days', 'last7Days']);
    quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'createdDate');
  }

  searchFilters.value = [...(searchCriteria || []).filter(f => !associatedKeys.includes(f.key as string))];
  associatedFilters.value = searchCriteria.filter(item => associatedKeys.includes(item.key as string));

  emits('change', getSearchParameters());
};

/**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (selectedKeys: string[], searchCriteria: SearchCriteria[], key?: string): void => {
  // Update quick search filters
  let isAssociated = false;
  if (key === 'ownedByMe') {
    isAssociated = true;
    if (selectedKeys.includes(key)) {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{
          valueKey: 'ownerId',
          type: 'select-user',
          value: props.userId
        }]);
      }
    } else {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{
          valueKey: 'ownerId',
          type: 'select-user',
          value: undefined
        }]);
      }
    }
  }

  if (key && key.startsWith('last') && (key.endsWith('Day') || key.endsWith('Days'))) {
    isAssociated = true;
    if (selectedKeys.includes(key)) {
      const createdDataSearchCriteria = searchCriteria.filter(f => f.key === 'createdDate');
      if (createdDataSearchCriteria.length > 0) {
        const createdDataValue = [createdDataSearchCriteria[0].value, createdDataSearchCriteria[1].value];
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{
            valueKey: 'createdDate',
            value: createdDataValue,
            type: 'date-range'
          }]);
        }
      }
    } else {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{
          valueKey: 'createdDate',
          value: undefined,
          type: 'date-range'
        }]);
      }
    }
  }

  searchCriteria = searchCriteria.filter(f => !associatedKeys.includes(f.key as string));
  // Update quick search filters
  quickSearchFilters.value = searchCriteria;
  if (isAssociated) {
    return;
  }

  // Emit change event with current params
  emits('change', getSearchParameters());
};

/**
 * Handles sort order changes
 * @param sortData - The sort configuration data
 */
const handleSortChange = (sortData) => {
  currentOrderBy.value = sortData.orderBy;
  currentOrderSort.value = sortData.orderSort;
  emits('change', getSearchParameters());
};

/**
 * Handles refresh button click
 */
const handleRefresh = () => {
  emits('refresh');
};

// Lifecycle
onMounted(() => {
  loadStatusEnumOptions();
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      ref="quickSearchOptionsRef"
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions as any"
        class="flex-1 mr-3.5"
        @change="handleSearchChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/test#${TestMenuKey.REVIEWS}?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('testCaseReview.actions.addReview') }}</span>
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
