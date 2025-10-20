<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, enumUtils, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import dayjs from 'dayjs';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { TIME_FORMAT } from '@/utils/constant';
import { TestMenuKey } from '@/views/test/menu';
import {
  QuickSearchOptions, createAuditOptions, createTimeOptions, createEnumOptions, type QuickSearchConfig
} from 'src/components/form/quickSearch';

// composables
const { t } = useI18n();

// props and emits
const props = withDefaults(defineProps<{
  loading: boolean;
  userId: string;
}>(), {
  loading: false,
  userId: ''
});

type OrderByKey = string;

const emits = defineEmits<{
    (e: 'change', value: { orderBy?: string; orderSort?: PageQuery.OrderSort; filters: SearchCriteria[]; }):void,
    (e: 'refresh'):void
  }>();

// reactive data
const userInfo = ref(appContext.getUser());
const searchPanelRef = ref();
const quickSearchOptionsRef = ref();
const planStatusOptions = ref<{name: string; key: string}[]>([]);

// search state
const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);
const assocKeys = ['ownerId'];

/**
 * Quick search configuration for plan search panel
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
      key: 'createdByMe',
      name: t('quickSearch.createdByMe'),
      fieldKey: 'createdBy'
    },
    {
      key: 'modifiedByMe',
      name: t('quickSearch.modifiedByMe'),
      fieldKey: 'lastModifiedBy'
    }
  ], String(userInfo.value?.id || '')),
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
 * Loads plan status enum options and maps them to display format
 */
const loadStatusEnum = () => {
  const data = enumUtils.enumToMessages(FuncPlanStatus);
  planStatusOptions.value = data.map(i => ({ name: i.message, key: i.value }));
};

// Configuration data
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('common.placeholders.searchKeyword'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'review',
    type: 'select',
    allowClear: true,
    options: [{ label: t('status.yes'), value: true }, { label: t('status.no'), value: false }],
    placeholder: t('testPlan.placeholders.selectReview')
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('common.placeholders.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.GreaterThanEqual,
    placeholder: t('common.placeholders.selectStartDate'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.LessThanEqual,
    placeholder: t('common.placeholders.selectDeadline'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) },
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
 * Combines all search filters and sorting parameters
 * @returns Combined search parameters object
 */
const getSearchParams = () => {
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
 * Handles search panel filter changes and updates associated filters
 * @param data - Array of search criteria from search panel
 */
const handleSearchChange = (data: SearchCriteria[], _headers?: { [key: string]: string }, key?: string) => {
  if (key === 'ownerId') {
    quickSearchOptionsRef.value.clearSelectedMap(['ownedByMe']);
    quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'ownerId');
  }
  searchFilters.value = [...(data || []).filter(f => !assocKeys.includes(f.key as string))];
  assocFilters.value = data.filter(item => assocKeys.includes(item.key as string));

  emits('change', getSearchParams());
};

/**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (selectedKeys: string[], searchCriteria: SearchCriteria[], key?: string): void => {
  // Update quick search filters
  if (key === 'ownedByMe') {
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
  searchCriteria = searchCriteria.filter(f => !assocKeys.includes(f.key as string));
  // Update quick search filters
  quickSearchFilters.value = searchCriteria;

  // Emit change event with current params
  emits('change', getSearchParams());
};

/**
 * Handles sort option changes and emits updated parameters
 * @param sortData - Sort configuration data
 */
const handleSortChange = (sortData) => {
  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  emits('change', getSearchParams());
};

/**
 * Handles refresh button click and emits refresh event
 */
const handleRefresh = () => {
  emits('refresh');
};

// lifecycle hooks
onMounted(() => {
  loadStatusEnum();
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
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/test#${TestMenuKey.PLANS}?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('testPlan.actions.addPlan') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
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
