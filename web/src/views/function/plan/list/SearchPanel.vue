<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, enumUtils, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { LoadingProps } from '@/types/types';

// composables
const { t } = useI18n();

// props and emits
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

type OrderByKey = string;

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
    (e: 'change', value: { orderBy?: string; orderSort?: PageQuery.OrderSort; filters: SearchCriteria[]; }):void,
    (e: 'refresh'):void
  }>();

// reactive data
const userInfo = ref(appContext.getUser());
const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});
const planStatusOptions = ref<{name: string; key: string}[]>([]);

// search state
const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);
const assocKeys = ['ownerId'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

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
    placeholder: t('functionPlan.list.searchPlanName'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'review',
    type: 'select',
    allowClear: true,
    options: [{ label: t('status.yes'), value: true }, { label: t('status.no'), value: false }],
    placeholder: t('functionPlan.list.isReview')
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('functionPlan.list.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.GreaterThanEqual,
    placeholder: t('functionPlan.list.planStartTime'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.LessThanEqual,
    placeholder: t('functionPlan.list.planDeadlineTime'),
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
    name: t('functionPlan.list.sortByName'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('functionPlan.list.sortByOwner'),
    key: 'ownerId',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('functionPlan.list.sortByCreator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('functionPlan.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Asc
  }
];

// computed properties
const menuItems = computed(() => [
  {
    key: '',
    name: t('quickSearchTags.all')
  },
  {
    key: 'ownerId',
    name: t('quickSearchTags.myResponsible')
  },
  {
    key: 'createdBy',
    name: t('quickSearchTags.createdByMe')
  },
  {
    key: 'lastModifiedBy',
    name: t('quickSearchTags.modifiedByMe')
  },
  ...planStatusOptions.value,
  {
    key: 'lastDay',
    name: t('quickSearchTags.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('quickSearchTags.last3Days')
  },
  {
    key: 'lastWeek',
    name: t('quickSearchTags.last7Days')
  }
]);

/**
 * Formats date range based on time key for quick search filters
 * @param key - Time key for date range calculation
 * @returns Array of date filter criteria
 */
const formatDateString = (key: string) => {
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

  return [
    startDate
      ? { value: startDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.GreaterThanEqual, key: 'createdDate' }
      : '',
    endDate
      ? { value: endDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.LessThanEqual, key: 'createdDate' }
      : ''
  ].filter(Boolean);
};

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
const handleSearchChange = (data: SearchCriteria[]) => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key));

  if (!assocFilters.value.length) {
    assocKeys.forEach(i => delete selectedMenuMap.value[i]);
  } else {
    assocKeys.forEach(key => {
      if (key === 'ownerId') {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      }
    });
  }

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
 * Handles quick search menu item click events and updates filters accordingly
 * @param data - Menu item data containing key and name
 */
const handleMenuItemClick = (data) => {
  const key = data.key;
  const statusTypeKeys = planStatusOptions.value.map(i => i.key);
  let searchChangeFlag = false;

  if (selectedMenuMap.value[key]) {
    delete selectedMenuMap.value[key];
    if (timeKeys.includes(key) && assocKeys.includes('createdDate')) {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: undefined }
      ]);
      searchChangeFlag = true;
    } else if (assocKeys.includes(key)) {
      searchPanelRef.value.setConfigs([
        { valueKey: key, value: undefined }
      ]);
      searchChangeFlag = true;
    }
  } else {
    if (key === '') {
      selectedMenuMap.value = { '': true };
      quickSearchFilters.value = [];
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        searchChangeFlag = true;
      }
    } else {
      delete selectedMenuMap.value[''];
    }
    if (timeKeys.includes(key)) {
      timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
      selectedMenuMap.value[key] = true;
    } else if (statusTypeKeys.includes(key)) {
      statusTypeKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
      selectedMenuMap.value[key] = true;
    } else {
      selectedMenuMap.value[key] = true;
    }
  }

  const userId = userInfo.value?.id;
  let timeFilters: SearchCriteria[] = [];
  const assocFiltersInQuick = [];

  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (statusTypeKeys.includes(key)) {
      return { key: 'status', op: SearchCriteria.OpEnum.Equal, value: key };
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      timeFilters = formatDateString(key);
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (key === 'ownerId') {
        assocFiltersInQuick.push({ valueKey: key, value: userId });
      }
      return undefined;
    } else {
      return { key, op: SearchCriteria.OpEnum.Equal, value: userId };
    }
  }).filter(Boolean);

  quickSearchFilters.value.push(...timeFilters);
  if (assocFiltersInQuick.length) {
    searchPanelRef.value.setConfigs([
      ...assocFiltersInQuick
    ]);
    searchChangeFlag = true;
  }
  if (!searchChangeFlag) {
    emits('change', getSearchParams());
  }
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
    <div class="flex items-center mb-3">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 mt-1.5 rounded-full"></div>

        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ t('quickSearchTags.title') }}</span>
          <Colon />
        </div>

        <div class="flex  flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': selectedMenuMap[item.key] }"
            class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold text-3"
            @click="handleMenuItemClick(item)">
            {{ item.name }}
          </div>
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
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/function#plans?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('functionPlan.list.addPlan') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
          :menuItems="sortMenuItems"
          @click="handleSortChange">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('sort') }}</span>
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
