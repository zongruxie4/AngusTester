<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { LoadingProps } from '@/types/types';

const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

// Type Definitions
type OrderByKey = string;

// Emits Definition
const emits = defineEmits<{
  (e: 'change', value: {
  orderBy?: string;
  orderSort?: PageQuery.OrderSort;
  filters: SearchCriteria[];
}):void,
  (e: 'refresh'):void
}>();

// Reactive Data
const currentUserInfo = ref(appContext.getUser());
const searchPanelRef = ref();
const selectedMenuItemsMap = ref<{[key: string]: boolean}>({});
const currentOrderBy = ref();
const currentOrderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const associatedFilters = ref<SearchCriteria[]>([]);

// Configuration Constants
const ASSOCIATED_KEYS = ['ownerId'];
const TIME_FILTER_KEYS = ['lastDay', 'lastThreeDays', 'lastWeek'];
const ESTABLISHED_FILTER_KEYS = ['established=1', 'established=0'];

// Search Panel Configuration
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('functionBaseline.list.searchBaselineNameDescription'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('functionBaseline.list.selectOwner'),
    maxlength: 100
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [
      t('functionBaseline.list.createdTimeGreaterEqual'),
      t('functionBaseline.list.createdTimeLessEqual')
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
    name: t('functionBaseline.list.sortByName'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('functionBaseline.list.sortByAddPerson'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('functionBaseline.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Asc
  }
];

/**
 * Get quick search menu items
 */
const quickSearchMenuItems = computed(() => [
  {
    key: '',
    name: t('functionBaseline.list.all')
  },
  {
    key: 'createdBy',
    name: t('functionBaseline.list.myCreated')
  },
  {
    key: 'lastModifiedBy',
    name: t('functionBaseline.list.myModified')
  },
  {
    key: 'established=1',
    name: t('functionBaseline.list.established')
  },
  {
    key: 'established=0',
    name: t('functionBaseline.list.notEstablished')
  },
  {
    key: 'lastDay',
    name: t('functionBaseline.list.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('functionBaseline.list.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('functionBaseline.list.lastWeek')
  }
]);

/**
 * Format date string for time-based filters
 * @param key - Time filter key
 * @returns Array of date filter objects
 */
const formatDateStringForTimeFilter = (key: string) => {
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
      ? {
          value: startDate.format(DATE_TIME_FORMAT),
          op: SearchCriteria.OpEnum.GreaterThanEqual,
          key: 'createdDate'
        }
      : '',
    endDate
      ? {
          value: endDate.format(DATE_TIME_FORMAT),
          op: SearchCriteria.OpEnum.LessThanEqual,
          key: 'createdDate'
        }
      : ''].filter(Boolean);
};

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
  searchFilters.value = data.filter(item => !ASSOCIATED_KEYS.includes(item.key));
  associatedFilters.value = data.filter(item => ASSOCIATED_KEYS.includes(item.key));

  if (!associatedFilters.value.length) {
    ASSOCIATED_KEYS.forEach(i => delete selectedMenuItemsMap.value[i]);
  } else {
    ASSOCIATED_KEYS.forEach(key => {
      if (key === 'ownerId') {
        const filterItem = associatedFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== currentUserInfo.value?.id) {
          delete selectedMenuItemsMap.value[key];
        }
      }
    });
  }

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
 * Handle quick search menu item click
 * @param data - Menu item data
 */
const handleQuickSearchMenuItemClick = (data) => {
  const key = data.key;
  let shouldTriggerSearchChange = false;

  if (selectedMenuItemsMap.value[key]) {
    delete selectedMenuItemsMap.value[key];
    if (TIME_FILTER_KEYS.includes(key) && ASSOCIATED_KEYS.includes('createdDate')) {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: undefined }
      ]);
      shouldTriggerSearchChange = true;
    } else if (ASSOCIATED_KEYS.includes(key)) {
      searchPanelRef.value.setConfigs([
        { valueKey: key, value: undefined }
      ]);
      shouldTriggerSearchChange = true;
    }
  } else {
    if (key === '') {
      selectedMenuItemsMap.value = { '': true };
      quickSearchFilters.value = [];
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        shouldTriggerSearchChange = true;
      }
    } else {
      delete selectedMenuItemsMap.value[''];
    }

    if (TIME_FILTER_KEYS.includes(key)) {
      TIME_FILTER_KEYS.forEach(timeKey => delete selectedMenuItemsMap.value[timeKey]);
      selectedMenuItemsMap.value[key] = true;
    } else if (ESTABLISHED_FILTER_KEYS.includes(key)) {
      ESTABLISHED_FILTER_KEYS.forEach(statusKey => delete selectedMenuItemsMap.value[statusKey]);
      selectedMenuItemsMap.value[key] = true;
    } else {
      selectedMenuItemsMap.value[key] = true;
    }
  }

  const userId = currentUserInfo.value?.id;
  let timeFilters: {key: string; op: string; value: string}[] = [];
  const associatedFiltersInQuick = [];

  quickSearchFilters.value = Object.keys(selectedMenuItemsMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (ESTABLISHED_FILTER_KEYS.includes(key)) {
      const value = !!(+key.split('=')[1]);
      return {
        key: 'established',
        op: SearchCriteria.OpEnum.Equal,
        value
      };
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      timeFilters = formatDateStringForTimeFilter(key);
      return undefined;
    } else if (ASSOCIATED_KEYS.includes(key)) {
      if (key === 'ownerId') {
        associatedFiltersInQuick.push({ valueKey: key, value: userId });
      }
      return undefined;
    } else {
      return {
        key,
        op: SearchCriteria.OpEnum.Equal,
        value: userId
      };
    }
  }).filter(Boolean);

  quickSearchFilters.value.push(...timeFilters);

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
 * Handle refresh button click
 */
const handleRefreshClick = () => {
  emits('refresh');
};
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ t('functionBaseline.list.quickSearch') }}</span>
        <Colon />
      </div>
      <div class="flex  flex-wrap ml-2">
        <div
          v-for="item in quickSearchMenuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuItemsMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
          @click="handleQuickSearchMenuItemClick(item)">
          {{ item.name }}
        </div>
      </div>
    </div>

    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="handleSearchPanelChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/function#baseline?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('functionBaseline.list.addBaseline') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="currentOrderBy"
          v-model:orderSort="currentOrderSort"
          :menuItems="sortMenuItems"
          @click="handleSortChange">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('functionBaseline.list.sort') }}</span>
          </div>
        </DropdownSort>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="handleRefreshClick">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
              <span class="ml-1">{{ t('functionBaseline.list.refresh') }}</span>
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
