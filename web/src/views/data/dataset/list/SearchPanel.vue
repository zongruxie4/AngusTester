<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, Dropdown, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { useRouter } from 'vue-router';
import { Button, Tooltip } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

const { t } = useI18n();

// Component props
interface Props {
  /** Loading state */
  loading: boolean;
  /** Number of selected items in batch mode */
  selectedNum?: number;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

const router = useRouter();

// Component emits
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: {
    orderBy?: string;
    orderSort?: 'ASC'|'DESC';
    filters: {key: string; op: string; value: string|string[]}[];
  }): void;
  (e: 'refresh'): void;
  (e: 'toBatchDelete'): void;
  (e: 'toExecuteBatchDelete'): void;
  (e: 'toImport'): void;
  (e: 'toExport'): void;
  (e: 'toCancelBatchDelete'): void;
}>();

// Reactive state
const userInfo = ref(appContext.getUser());
const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

// Dropdown menu items
const buttonDropdownMenuItems = [
  {
    name: t('dataset.listSearchPanel.buttonDropdown.fileExtractDataset'),
    key: 'file',
    noAuth: true
  },
  {
    name: t('dataset.listSearchPanel.buttonDropdown.jdbcExtractDataset'),
    key: 'jdbc',
    noAuth: true
  }
];

// Search panel options
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('dataset.listSearchPanel.searchOptions.namePlaceholder'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: t('dataset.listSearchPanel.searchOptions.createdByPlaceholder'),
    maxlength: 100
  },
  {
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    maxlength: 100
  }
];

// Menu items
const menuItems = computed(() => [
  {
    key: '',
    name: t('common.all')
  },
  {
    key: 'createdBy',
    name: t('dataset.listSearchPanel.menuItems.myCreated')
  },
  {
    key: 'lastModifiedBy',
    name: t('dataset.listSearchPanel.menuItems.myModified')
  },
  {
    key: 'lastDay',
    name: t('quickSearch.last1Day')
  },
  {
    key: 'lastThreeDays',
    name: t('dataset.listSearchPanel.menuItems.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('dataset.listSearchPanel.menuItems.lastWeek')
  }
]);

// Reactive state for search parameters
const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const quickSearchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocKeys = ['createdBy', 'createdDate'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

/**
 * Format date string for time-based filters
 * Converts relative time periods to absolute date ranges
 *
 * @param key - Time period key
 * @returns Array of start and end date strings
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

  return [startDate ? startDate.format(DATE_TIME_FORMAT) : '', endDate ? endDate.format(DATE_TIME_FORMAT) : ''];
};

/**
 * Get search parameters
 * Combines all filter types into a single parameter object
 *
 * @returns Search parameters object
 */
const getParams = () => {
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
 * Updates filters and emits change event
 *
 * @param data - Search filter data
 */
const searchChange = (data: {key: string; op: string; value: string|string[]}[]) => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key));

  if (!assocFilters.value.length) {
    assocKeys.forEach(i => {
      if (i === 'createdDate') {
        timeKeys.forEach(t => delete selectedMenuMap.value[t]);
      } else {
        delete selectedMenuMap.value[i];
      }
    });
  } else {
    assocKeys.forEach(key => {
      if (key === 'createdBy' || key === 'lastModifiedBy') {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      }
      if (key === 'createdDate') {
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
 * Handle menu item clicks
 * Updates quick search filters based on selected menu items
 *
 * @param data - Menu item data
 */
const menuItemClick = (data) => {
  const key = data.key;
  // const statusTypeKeys = planStatusTypeOpt.value.map(i => i.key);
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
      // 清空搜索面板
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
    } else {
      selectedMenuMap.value[key] = true;
    }
  }
  const userId = userInfo.value?.id;
  const timeFilters: {key: string; op: string; value: string}[] = [];
  const assocFiltersInQuick = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      assocFiltersInQuick.push({ valueKey: 'createdDate', value: formatDateString(key) });
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (key === 'createdBy') {
        assocFiltersInQuick.push({ valueKey: key, value: userId });
      }
      return undefined;
    } else {
      return {
        key,
        op: 'EQUAL',
        value: userId
      };
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
    emits('change', getParams());
  }
};

/**
 * Refresh the dataset list
 * Emits refresh event to parent component
 */
const refresh = () => {
  emits('refresh');
};

/**
 * Handle button dropdown clicks
 * Navigates to dataset creation pages based on selection
 *
 * @param param0 - Dropdown menu item
 */
const buttonDropdownClick = ({ key }: { key: 'file' | 'jdbc' }) => {
  if (key === 'file') {
    router.push('/data#dataSet?source=FILE');
    return;
  }

  if (key === 'jdbc') {
    router.push('/data#dataSet?source=JDBC');
  }
};

/**
 * Navigate to create static dataset page
 */
const toCreateStaticDataSet = () => {
  router.push('/data#dataSet?source=STATIC');
};

/**
 * Initialize batch delete mode
 * Emits toBatchDelete event to parent component
 */
const toBatchDelete = () => {
  emits('toBatchDelete');
};

/**
 * Execute batch delete operation
 * Emits toExecuteBatchDelete event to parent component
 */
const toExecuteBatchDelete = () => {
  emits('toExecuteBatchDelete');
};

/**
 * Open import modal
 * Emits toImport event to parent component
 */
const handleImport = () => {
  emits('toImport');
};

/**
 * Open export modal
 * Emits toExport event to parent component
 */
const handleExport = () => {
  emits('toExport');
};

/**
 * Cancel batch delete mode
 * Emits toCancelBatchDelete event to parent component
 */
const toCancelBatchDelete = () => {
  emits('toCancelBatchDelete');
};

onMounted(() => {
  // loadStatusEnum();
});
</script>

<template>
  <div class="mt-2.5 mb-3.5">
    <!-- Quick Search Section -->
    <div class="flex items-center mb-3">
      <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 rounded-full"></div>
      <div class="whitespace-nowrap text-3 text-text-sub-content">
        <span>{{ t('projectActivity.searchPanel.ui.quickQuery') }}</span>
        <Colon />
      </div>
      <div class="flex flex-wrap items-center ml-2 md-2">
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

    <!-- Search Panel and Action Buttons -->
    <div class="flex items-start justify-between">
      <!-- Advanced Search Panel -->
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="searchChange" />

      <!-- Action Buttons -->
      <div class="flex items-center space-x-3">
        <!-- Batch Delete Actions (when items are selected) -->
        <template v-if="props.selectedNum && typeof props.selectedNum === 'number' && props.selectedNum > 0">
          <Button
            danger
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toExecuteBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <div class="flex items-center">
              <span class="mr-0.5">{{ t('dataset.listSearchPanel.buttons.deleteSelected') }}</span>
              <span>({{ props.selectedNum }})</span>
            </div>
          </Button>

          <Button
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toCancelBatchDelete">
            <Icon icon="icon-fanhui" class="mr-1" />
            <span>{{ t('dataset.listSearchPanel.buttons.cancelDelete') }}</span>
          </Button>
        </template>

        <!-- Default Action Buttons -->
        <template v-else>
          <!-- Add Dataset Button with Dropdown -->
          <Button
            type="primary"
            size="small"
            class="flex items-center flex-shrink-0 pr-0"
            @click="toCreateStaticDataSet">
            <div class="flex items-center">
              <Icon icon="icon-jia" class="text-3.5" />
              <span class="ml-1">{{ t('dataset.listSearchPanel.buttons.addStaticDataset') }}</span>
            </div>
            <Dropdown
              :menuItems="buttonDropdownMenuItems"
              @click="buttonDropdownClick">
              <div class="w-5 h-5 flex items-center justify-center">
                <Icon icon="icon-more" />
              </div>
            </Dropdown>
          </Button>

          <!-- Batch Delete Button -->
          <Button
            type="default"
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>{{ t('dataset.listSearchPanel.buttons.batchDelete') }}</span>
          </Button>

          <!-- Refresh Button -->
          <IconRefresh
            :loading="props.loading"
            :disabled="props.loading"
            @click="refresh">
            <template #default>
              <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                <Icon icon="icon-shuaxin" class="text-3.5" />
              </div>
            </template>
          </IconRefresh>

          <!-- Import Button -->
          <Tooltip
            arrowPointAtCenter
            placement="topLeft"
            :title="t('dataVariable.list.searchPanel.tooltips.uploadVariable')">
            <Icon
              icon="icon-shangchuan"
              class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
              @click="handleImport" />
          </Tooltip>

          <!-- Export Button -->
          <Tooltip
            arrowPointAtCenter
            placement="topLeft"
            :title="t('dataVariable.list.searchPanel.tooltips.downloadVariable')">
            <Icon
              icon="icon-daochu1"
              class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
              @click="handleExport" />
          </Tooltip>
        </template>
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
