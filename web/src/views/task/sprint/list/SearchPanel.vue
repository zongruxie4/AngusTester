<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, enumUtils, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { TaskSprintStatus } from '@/enums/enums';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { LoadingProps } from '@/types/types';

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
const selectedQuickFilters = ref<{ [key: string]: boolean }>({});

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
    placeholder: t('taskSprint.searchPanel.searchSprintName'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'ownerId',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('taskSprint.searchPanel.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date' as const,
    valueType: 'start',
    op: SearchCriteria.OpEnum.GreaterThanEqual,
    placeholder: t('taskSprint.searchPanel.startDateGreaterEqual'),
    showTime: true,
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date' as const,
    valueType: 'start',
    op: SearchCriteria.OpEnum.LessThanEqual,
    placeholder: t('taskSprint.searchPanel.deadlineDateLessEqual'),
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
    name: t('taskSprint.searchPanel.sortByCreateTime'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('taskSprint.searchPanel.sortByCreator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('taskSprint.searchPanel.sortByOwner'),
    key: 'ownerId',
    orderSort: PageQuery.OrderSort.Asc
  }
];

// Quick search filter options
const quickSearchOptions = computed(() => [
  {
    key: '',
    name: t('quickSearch.all')
  },
  {
    key: 'ownerId',
    name: t('taskSprint.searchPanel.myOwned')
  },
  {
    key: 'createdBy',
    name: t('quickSearch.createdByMe')
  },
  {
    key: 'lastModifiedBy',
    name: t('quickSearch.modifiedByMe')
  },
  ...sprintStatusOptions.value,
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
]);

/**
 * Generate date range filters based on time period key
 * @param timePeriodKey - The time period key (lastDay, lastThreeDays, lastWeek)
 * @returns Array of SearchCriteria for date filtering
 */
const generateDateRangeFilters = (timePeriodKey: string) => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (timePeriodKey === 'lastDay') {
    startDate = dayjs().startOf('date');
    endDate = dayjs();
  }

  if (timePeriodKey === 'lastThreeDays') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (timePeriodKey === 'lastWeek') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [
    startDate
      ? { value: startDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.GreaterThanEqual, key: 'createdDate' }
      : null,
    endDate
      ? { value: endDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.LessThanEqual, key: 'createdDate' }
      : null
  ].filter(Boolean) as SearchCriteria[];
};

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
  // Separate regular filters from associated filters
  searchFilters.value = filterData.filter(item => !associatedFilterKeys.includes(item.key || ''));
  associatedFilters.value = filterData.filter(item => associatedFilterKeys.includes(item.key || ''));

  // Clear associated filter selections if no filters are active
  if (!associatedFilters.value.length) {
    associatedFilterKeys.forEach(key => delete selectedQuickFilters.value[key]);
  } else {
    // Update quick filter selections based on associated filters
    associatedFilterKeys.forEach(key => {
      if (key === 'ownerId') {
        const ownerFilter = associatedFilters.value.find(filter => filter.key === key);
        if (!ownerFilter || ownerFilter.value !== String(currentUser.value?.id)) {
          delete selectedQuickFilters.value[key];
        }
      }
    });
  }

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
 * Handle quick search filter item click
 * @param filterItem - The clicked filter item
 */
const handleQuickFilterClick = (filterItem) => {
  const filterKey = filterItem.key;
  const statusKeys = sprintStatusOptions.value.map(option => option.key);
  const timePeriodKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];
  let shouldTriggerSearch = false;

  // Toggle filter selection
  if (selectedQuickFilters.value[filterKey]) {
    // Remove filter
    delete selectedQuickFilters.value[filterKey];

    // Clear related search panel configs
    if (timePeriodKeys.includes(filterKey) && associatedFilterKeys.includes('createdDate')) {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: undefined }
      ]);
      shouldTriggerSearch = true;
    } else if (associatedFilterKeys.includes(filterKey)) {
      searchPanelRef.value.setConfigs([
        { valueKey: filterKey, value: undefined }
      ]);
      shouldTriggerSearch = true;
    }
  } else {
    // Add filter
    if (filterKey === '') {
      // Select "All" - clear all other filters
      selectedQuickFilters.value = { '': true };
      quickSearchFilters.value = [];
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        shouldTriggerSearch = true;
      }
    } else {
      // Remove "All" selection when selecting specific filters
      delete selectedQuickFilters.value[''];

      // Handle mutually exclusive filter groups
      if (timePeriodKeys.includes(filterKey)) {
        timePeriodKeys.forEach(timeKey => delete selectedQuickFilters.value[timeKey]);
        selectedQuickFilters.value[filterKey] = true;
      } else if (statusKeys.includes(filterKey)) {
        statusKeys.forEach(statusKey => delete selectedQuickFilters.value[statusKey]);
        selectedQuickFilters.value[filterKey] = true;
      } else {
        selectedQuickFilters.value[filterKey] = true;
      }
    }
  }

  // Generate quick search filters based on selections
  const currentUserId = currentUser.value?.id;
  let dateRangeFilters: SearchCriteria[] = [];
  const associatedFiltersInQuick: { valueKey: string; value: any }[] = [];

  quickSearchFilters.value = Object.keys(selectedQuickFilters.value).map(key => {
    if (key === '') {
      return null;
    } else if (statusKeys.includes(key)) {
      return { key: 'status', op: SearchCriteria.OpEnum.Equal, value: key };
    } else if (timePeriodKeys.includes(key)) {
      dateRangeFilters = generateDateRangeFilters(key);
      return null;
    } else if (associatedFilterKeys.includes(key)) {
      if (key === 'ownerId') {
        associatedFiltersInQuick.push({ valueKey: key, value: currentUserId });
      }
      return null;
    } else {
      return { key, op: SearchCriteria.OpEnum.Equal, value: String(currentUserId) };
    }
  }).filter(Boolean) as SearchCriteria[];

  // Add date range filters and associated filters
  quickSearchFilters.value.push(...dateRangeFilters);

  if (associatedFiltersInQuick.length) {
    searchPanelRef.value.setConfigs([
      ...associatedFiltersInQuick
    ]);
    shouldTriggerSearch = true;
  }

  if (!shouldTriggerSearch) {
    emits('change', getSearchParameters());
  }
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
    <div class="flex items-center mb-3">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 mt-1.5 rounded-full"></div>

        <div class="whitespace-nowrap text-3 mt-0.5 text-text-sub-content">
          <span>{{ t('quickSearch.title') }}</span>
          <Colon />
        </div>

        <div class="flex flex-wrap ml-2">
          <div
            v-for="item in quickSearchOptions"
            :key="item.key"
            :class="{ 'active-key': selectedQuickFilters[item.key] }"
            class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold text-3"
            @click="handleQuickFilterClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <div class="flex items-start justify-between mb-1.5 space-x-5">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelConfig"
        class="flex-1 mr-3.5"
        @change="handleSearchPanelChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/task#sprint?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('taskSprint.addSprint') }}</span>
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
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
