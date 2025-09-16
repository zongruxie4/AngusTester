<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { TaskMeetingType } from '@/enums/enums';
import { LoadingProps } from '@/types/types';

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
const selectedQuickSearchItems = ref<{ [key: string]: boolean }>({});

const currentOrderBy = ref();
const currentOrderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const associatedFilters = ref<SearchCriteria[]>([]);

// CONSTANTS
const ASSOCIATED_FILTER_KEYS = ['createdDate', 'moderatorId', 'createdBy'];
const TIME_RANGE_KEYS = ['lastDay', 'lastThreeDays', 'lastWeek'];

// SEARCH PANEL CONFIGURATION
const searchPanelOptions = [
  {
    valueKey: 'subject',
    type: 'input' as const,
    placeholder: t('taskMeeting.placeholder.searchSubject'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'type',
    type: 'select-enum' as const,
    enumKey: TaskMeetingType,
    allowClear: true,
    placeholder: t('taskMeeting.placeholder.selectType')
  },
  {
    valueKey: 'moderatorId',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('taskMeeting.placeholder.selectModeratorSearch')
  },
  {
    valueKey: 'createdBy',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('taskMeeting.placeholder.selectCreator')
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    placeholder: [t('taskMeeting.placeholder.createTimeFrom'), t('taskMeeting.placeholder.createTimeTo')],
    showTime: true
  }
];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: PageQuery.OrderSort;
}[] = [
  {
    name: t('taskMeeting.sort.byCreateTime'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('taskMeeting.sort.byCreator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('taskMeeting.sort.bySubject'),
    key: 'subject',
    orderSort: PageQuery.OrderSort.Asc
  }
];

// COMPUTED PROPERTIES
const quickSearchMenuItems = computed(() => [
  {
    key: '',
    name: t('taskMeeting.quickSearch.all')
  },
  {
    key: 'moderatorId',
    name: t('taskMeeting.quickSearch.myModerated')
  },
  {
    key: 'createdBy',
    name: t('taskMeeting.quickSearch.myCreated')
  },
  {
    key: 'lastModifiedBy',
    name: t('taskMeeting.quickSearch.myModified')
  },
  {
    key: 'lastDay',
    name: t('taskMeeting.quickSearch.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('taskMeeting.quickSearch.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('taskMeeting.quickSearch.lastWeek')
  }
]);

/**
 * Formats date range based on time key selection
 * @param timeKey - Time range key (lastDay, lastThreeDays, lastWeek)
 * @returns Array containing start and end date strings
 */
const formatDateRange = (timeKey: string): [string, string] => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  switch (timeKey) {
    case 'lastDay':
      startDate = dayjs().startOf('date');
      endDate = dayjs();
      break;
    case 'lastThreeDays':
      startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
      endDate = dayjs();
      break;
    case 'lastWeek':
      startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
      endDate = dayjs();
      break;
  }

  return [
    startDate ? startDate.format(DATE_TIME_FORMAT) : '',
    endDate ? endDate.format(DATE_TIME_FORMAT) : ''
  ];
};

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
  searchFilters.value = filterData.filter(item => !ASSOCIATED_FILTER_KEYS.includes(item.key));
  associatedFilters.value = filterData.filter(item => ASSOCIATED_FILTER_KEYS.includes(item.key));

  // Clear quick search selections if no associated filters
  if (!associatedFilters.value.length) {
    ASSOCIATED_FILTER_KEYS.forEach(key => {
      if (key === 'createdDate') {
        TIME_RANGE_KEYS.forEach(timeKey => delete selectedQuickSearchItems.value[timeKey]);
      } else {
        delete selectedQuickSearchItems.value[key];
      }
    });
  } else {
    // Update quick search selections based on associated filters
    ASSOCIATED_FILTER_KEYS.forEach(key => {
      if (['createdBy', 'moderatorId'].includes(key)) {
        const filterItem = associatedFilters.value.find(item => item.key === key);
        if (!filterItem || filterItem.value !== currentUser.value?.id?.toString()) {
          delete selectedQuickSearchItems.value[key];
        }
      } else if (key === 'createdDate') {
        const filterItems = associatedFilters.value.filter(item => item.key === key);
        const selectedTimeKey = TIME_RANGE_KEYS.find(timeKey => selectedQuickSearchItems.value[timeKey]);
        if (selectedTimeKey) {
          const timeRange = formatDateRange(selectedTimeKey);
          if (timeRange[0] !== filterItems[0].value || timeRange[1] !== filterItems[1].value) {
            delete selectedQuickSearchItems.value[selectedTimeKey];
          }
        }
      }
    });
  }

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
 * Handles quick search menu item clicks
 * @param menuItem - Clicked menu item data
 */
const handleQuickSearchItemClick = (menuItem: { key: string; name: string }) => {
  const itemKey = menuItem.key;
  let shouldTriggerSearchChange = false;

  if (selectedQuickSearchItems.value[itemKey]) {
    // Deselect item
    delete selectedQuickSearchItems.value[itemKey];

    if (TIME_RANGE_KEYS.includes(itemKey) && ASSOCIATED_FILTER_KEYS.includes('createdDate')) {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: undefined }
      ]);
      shouldTriggerSearchChange = true;
    } else if (ASSOCIATED_FILTER_KEYS.includes(itemKey)) {
      searchPanelRef.value.setConfigs([
        { valueKey: itemKey, value: undefined }
      ]);
      shouldTriggerSearchChange = true;
    }
  } else {
    // Select item
    if (itemKey === '') {
      // Select "All" - clear all other selections
      selectedQuickSearchItems.value = { '': true };
      quickSearchFilters.value = [];
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        shouldTriggerSearchChange = true;
      }
    } else {
      // Deselect "All" when selecting specific items
      delete selectedQuickSearchItems.value[''];
    }

    if (TIME_RANGE_KEYS.includes(itemKey)) {
      // Clear other time range selections
      TIME_RANGE_KEYS.forEach(timeKey => delete selectedQuickSearchItems.value[timeKey]);
      selectedQuickSearchItems.value[itemKey] = true;
    } else {
      selectedQuickSearchItems.value[itemKey] = true;
    }
  }

  // Build quick search filters
  const userId = currentUser.value?.id?.toString();
  const associatedFiltersInQuick: { valueKey: string; value: string | string[] }[] = [];

  quickSearchFilters.value = Object.keys(selectedQuickSearchItems.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (TIME_RANGE_KEYS.includes(key)) {
      associatedFiltersInQuick.push({
        valueKey: 'createdDate',
        value: formatDateRange(key)
      });
      return undefined;
    } else if (ASSOCIATED_FILTER_KEYS.includes(key)) {
      if (['moderatorId', 'createdBy'].includes(key)) {
        associatedFiltersInQuick.push({ valueKey: key, value: userId || '' });
      }
      return undefined;
    } else {
      return {
        key,
        op: SearchCriteria.OpEnum.Equal,
        value: userId || ''
      };
    }
  }).filter(Boolean) as SearchCriteria[];

  if (associatedFiltersInQuick.length) {
    searchPanelRef.value.setConfigs([...associatedFiltersInQuick]);
    shouldTriggerSearchChange = true;
  }

  if (!shouldTriggerSearchChange) {
    emits('change', buildSearchParameters());
  }
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
    <div class="flex items-center mb-3">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 mt-1.5 rounded-full"></div>
        <div class="whitespace-nowrap text-3 mt-0.5 text-text-sub-content">
          <span>{{ t('quickSearch') }}</span>
          <Colon />
        </div>
        <div class="flex flex-wrap ml-2">
          <div
            v-for="menuItem in quickSearchMenuItems"
            :key="menuItem.key"
            :class="{ 'active-key': selectedQuickSearchItems[menuItem.key] }"
            class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold text-3"
            @click="handleQuickSearchItemClick(menuItem)">
            {{ menuItem.name }}
          </div>
        </div>
      </div>
    </div>
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
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/task#meeting?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('taskMeeting.addMeeting') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="currentOrderBy"
          v-model:orderSort="currentOrderSort"
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
