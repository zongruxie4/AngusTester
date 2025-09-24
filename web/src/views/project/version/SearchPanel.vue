<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import type { SearchPanelProps, SearchFilters, SearchParams, MenuItem } from './types';

// Component props with default values
const props = withDefaults(defineProps<SearchPanelProps>(), {
  loading: false
});

const { t } = useI18n();

// Component emits
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: SearchParams): void;
  (e: 'refresh'): void;
  (e: 'toMerge'): void;
  (e: 'add'): void;
}>();

// User context
const userInfo = ref(appContext.getUser());

// Component references
const searchPanelRef = ref();

// State management
const selectedMenuMap = ref<{ [key: string]: boolean }>({});

/**
 * Search panel configuration options
 * Defines available search fields and their properties
 */
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input' as const,
    placeholder: t('version.searchPanel.searchOptions.namePlaceholder'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'createdBy',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('version.searchPanel.searchOptions.creatorPlaceholder')
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    showTime: true
  }
];

/**
 * Quick search menu items
 * Provides predefined filter options for common searches
 */
const menuItems = computed((): MenuItem[] => [
  {
    key: '',
    name: t('version.searchPanel.menuItems.all')
  },
  {
    key: 'NOT_RELEASED',
    name: t('version.searchPanel.menuItems.notReleased')
  },
  {
    key: 'RELEASED',
    name: t('version.searchPanel.menuItems.released')
  },
  {
    key: 'ARCHIVED',
    name: t('version.searchPanel.menuItems.archived')
  }
]);

// Search state
const orderBy = ref<string>();
const orderSort = ref<'ASC' | 'DESC'>();
const searchFilters = ref<SearchFilters[]>([]);
const quickSearchFilters = ref<SearchFilters[]>([]);
const assocFilters = ref<SearchFilters[]>([]);

// Configuration constants
const assocKeys: string[] = [];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];
const statusKeys = ['NOT_RELEASED', 'RELEASED', 'ARCHIVED'];

/**
 * Format date string for time-based filters
 * Converts time keys to date range strings
 * @param key - Time key identifier
 * @returns Array of start and end date strings
 */
const formatDateString = (key: string): [string, string] => {
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
    startDate ? startDate.format(DATE_TIME_FORMAT) : '',
    endDate ? endDate.format(DATE_TIME_FORMAT) : ''
  ];
};

/**
 * Get current search parameters
 * Combines all filter types into single parameter object
 * @returns Combined search parameters
 */
const getParams = (): SearchParams => {
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
 * Handle search panel changes
 * Processes search filters and updates state
 * @param data - Array of search filter objects
 */
const searchChange = (data: SearchFilters[]): void => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key));

  // Clear associated menu selections if no filters
  if (!assocFilters.value.length) {
    assocKeys.forEach(i => {
      if (i === 'createdDate') {
        timeKeys.forEach(t => delete selectedMenuMap.value[t]);
      } else {
        delete selectedMenuMap.value[i];
      }
    });
  } else {
    // Validate and sync menu selections with filters
    assocKeys.forEach(key => {
      if (['createdBy'].includes(key)) {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      } else if (key === 'createdDate') {
        const filterItem = assocFilters.value.filter(i => i.key === key);
        const timeKey = timeKeys.find(t => selectedMenuMap.value[t]);
        if (timeKey) {
          const timeValue = formatDateString(timeKey);
          if (timeValue[0] !== filterItem[0].value || timeValue[1] !== filterItem[1].value) {
            delete selectedMenuMap.value[timeKey];
          }
        }
      }
    });
  }

  emits('change', getParams());
};

/**
 * Handle menu item clicks for quick search
 * Manages quick filter selection and search panel state
 * @param data - Menu item data object
 */
const menuItemClick = (data: MenuItem): void => {
  const key = data.key;
  let searchChangeFlag = false;

  if (selectedMenuMap.value[key]) {
    // Deselect item
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
    // Select item
    if (key === '') {
      selectedMenuMap.value = { '': true };
      quickSearchFilters.value = [];
      // Clear search panel
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        searchChangeFlag = true;
      }
    } else {
      delete selectedMenuMap.value[''];
    }

    if (statusKeys.includes(key)) {
      // Handle status selection (mutually exclusive)
      statusKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
      selectedMenuMap.value[key] = true;
    } else {
      selectedMenuMap.value[key] = true;
    }
  }

  // Update quick search filters
  const assocFiltersInQuick: { valueKey: string; value: string | string[] }[] = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (statusKeys.includes(key)) {
      return {
        key: 'status',
        op: 'EQUAL',
        value: key
      };
    }
  }).filter(Boolean) as SearchFilters[];

  if (assocFiltersInQuick.length) {
    searchPanelRef.value.setConfigs([
      ...assocFiltersInQuick
    ]);
    searchChangeFlag = true;
  }

  if (!searchChangeFlag) {
    emits('change', getParams());
  }
};

/**
 * Handle refresh action
 * Emits refresh event to parent component
 */
const refresh = (): void => {
  emits('refresh');
};

/**
 * Handle add action
 * Emits add event to parent component
 */
const add = (): void => {
  emits('add');
};

/**
 * Handle merge action
 * Emits merge event to parent component
 */
const toMerge = (): void => {
  emits('toMerge');
};

onMounted(() => {
  // Component initialization logic if needed
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex items-center mb-3">
      <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 rounded-full"></div>
      <div class="whitespace-nowrap text-3 text-text-sub-content">
        <span>{{ t('projectActivity.searchPanel.ui.quickQuery') }}</span>
        <Colon />
      </div>
      <div class="flex flex-wrap items-center ml-2">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold"
          @click="menuItemClick(item)">
          {{ item.name }}
        </div>
      </div>
    </div>
    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="searchChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          @click="add">
          <Icon icon="icon-jia" class="text-3.5 mr-1" />
          <span>{{ t('version.searchPanel.actions.addVersion') }}</span>
        </Button>

        <Button
          size="small"
          @click="toMerge">
          <Icon icon="icon-hebingbanben1" class="text-3.5 mr-1" />
          <span>{{ t('version.searchPanel.actions.mergeVersion') }}</span>
        </Button>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="refresh">
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
