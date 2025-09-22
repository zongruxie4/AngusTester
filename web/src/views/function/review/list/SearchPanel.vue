<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { EnumMessage, PageQuery, SearchCriteria, enumUtils, appContext } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { LoadingProps } from '@/types/types';

// Props and Component Setup
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
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
const selectedMenuMap = ref<{[key: string]: boolean}>({});

const planStatusOptions = ref<EnumMessage<FuncPlanStatus>[]>([]);

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
    placeholder: t('caseReview.list.searchReviewNameDescription'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('caseReview.list.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.GreaterThanEqual,
    placeholder: t('caseReview.list.reviewStartTimeGreaterEqual'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.LessThanEqual,
    placeholder: t('caseReview.list.reviewDeadlineTimeLessEqual'),
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
    name: t('caseReview.list.sortByName'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('caseReview.list.sortByOwner'),
    key: 'ownerId',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('caseReview.list.sortByAddPerson'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('caseReview.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Asc
  }
];

// Computed Properties
const quickMenuItems = computed(() => [
  {
    key: '',
    name: t('caseReview.list.all')
  },
  {
    key: 'ownerId',
    name: t('caseReview.list.myResponsible')
  },
  {
    key: 'createdBy',
    name: t('caseReview.list.myCreated')
  },
  {
    key: 'lastModifiedBy',
    name: t('caseReview.list.myModified')
  },
  ...planStatusOptions.value,
  {
    key: 'lastDay',
    name: t('caseReview.list.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('caseReview.list.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('caseReview.list.lastWeek')
  }
]);

// Search State
const currentOrderBy = ref();
const currentOrderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const associatedFilters = ref<SearchCriteria[]>([]);
const associatedKeys = ['ownerId'];
const timeRangeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

/**
 * Formats date range strings for time-based filtering
 * @param timeRangeKey - The time range key to format
 * @returns Array of search criteria for date filtering
 */
const formatDateRange = (timeRangeKey: string) => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (timeRangeKey === 'lastDay') {
    startDate = dayjs().startOf('date');
    endDate = dayjs();
  }

  if (timeRangeKey === 'lastThreeDays') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (timeRangeKey === 'lastWeek') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [
    startDate
      ? { value: startDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.GreaterThanEqual, key: 'createdDate' }
      : '',
    endDate
      ? { value: endDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.LessThanEqual, key: 'createdDate' }
      : ''].filter(Boolean);
};

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
const handleSearchChange = (searchCriteria: SearchCriteria[]) => {
  searchFilters.value = searchCriteria.filter(item => !associatedKeys.includes(item.key));
  associatedFilters.value = searchCriteria.filter(item => associatedKeys.includes(item.key));

  if (!associatedFilters.value.length) {
    associatedKeys.forEach(key => delete selectedMenuMap.value[key]);
  } else {
    associatedKeys.forEach(key => {
      if (key === 'ownerId') {
        const filterItem = associatedFilters.value.find(item => item.key === key);
        if (!filterItem || filterItem.value !== currentUser.value?.id) {
          delete selectedMenuMap.value[key];
        }
      }
    });
  }

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
 * Handles quick menu item clicks for filtering
 * @param menuItem - The clicked menu item data
 */
const handleQuickMenuItemClick = (menuItem) => {
  const itemKey = menuItem.key;
  const statusTypeKeys = planStatusOptions.value.map(option => option.key);
  let shouldTriggerSearchChange = false;

  if (selectedMenuMap.value[itemKey]) {
    delete selectedMenuMap.value[itemKey];
    if (timeRangeKeys.includes(itemKey) && associatedKeys.includes('createdDate')) {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: undefined }
      ]);
      shouldTriggerSearchChange = true;
    } else if (associatedKeys.includes(itemKey)) {
      searchPanelRef.value.setConfigs([
        { valueKey: itemKey, value: undefined }
      ]);
      shouldTriggerSearchChange = true;
    }
  } else {
    if (itemKey === '') {
      selectedMenuMap.value = { '': true };
      quickSearchFilters.value = [];
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        shouldTriggerSearchChange = true;
      }
    } else {
      delete selectedMenuMap.value[''];
    }

    if (timeRangeKeys.includes(itemKey)) {
      timeRangeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
      selectedMenuMap.value[itemKey] = true;
    } else if (statusTypeKeys.includes(itemKey)) {
      statusTypeKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
      selectedMenuMap.value[itemKey] = true;
    } else {
      selectedMenuMap.value[itemKey] = true;
    }
  }

  const currentUserId = currentUser.value?.id;
  let timeRangeFilters: {key: string; op: string; value: string}[] = [];
  const associatedFiltersInQuick = [];

  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (statusTypeKeys.includes(key)) {
      return { key: 'status', op: SearchCriteria.OpEnum.Equal, value: key };
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      timeRangeFilters = formatDateRange(key);
      return undefined;
    } else if (associatedKeys.includes(key)) {
      if (key === 'ownerId') {
        associatedFiltersInQuick.push({ valueKey: key, value: currentUserId });
      }
      return undefined;
    } else {
      return { key, op: SearchCriteria.OpEnum.Equal, value: currentUserId };
    }
  }).filter(Boolean);

  quickSearchFilters.value.push(...timeRangeFilters);

  if (associatedFiltersInQuick.length) {
    searchPanelRef.value.setConfigs([
      ...associatedFiltersInQuick
    ]);
    shouldTriggerSearchChange = true;
  }

  if (!shouldTriggerSearchChange) {
    emits('change', getSearchParameters());
  }
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
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ t('caseReview.list.quickQuery') }}</span>
        <Colon />
      </div>

      <div class="flex  flex-wrap ml-2">
        <div
          v-for="item in quickMenuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
          @click="handleQuickMenuItemClick(item)">
          {{ item.name }}
        </div>
      </div>
    </div>

    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="handleSearchChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/function#reviews?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('caseReview.list.addReview') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="currentOrderBy"
          v-model:orderSort="currentOrderSort"
          :menuItems="sortMenuItems"
          @click="handleSortChange">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('caseReview.list.sort') }}</span>
          </div>
        </DropdownSort>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="handleRefresh">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
              <span class="ml-1">{{ t('caseReview.list.refresh') }}</span>
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
