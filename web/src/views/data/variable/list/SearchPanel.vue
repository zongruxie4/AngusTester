<script setup lang="ts">
/**
 * Search Panel Component for Variable List
 *
 * <p>Component that provides search, filter, and quick action functionality</p>
 * <p>Includes quick search options, advanced search panel, and action buttons</p>
 */

import { computed, onMounted, ref } from 'vue';
import { Colon, Dropdown, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { useRouter } from 'vue-router';
import { Button, Tooltip } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

// Import types
import type { SearchPanelProps, SearchFilter, SortConfig, SearchPanelOption } from './types';

const { t } = useI18n();

// Component props
const props = withDefaults(defineProps<SearchPanelProps>(), {
  loading: false
});

const router = useRouter();

// Component emits
const emits = defineEmits<{
  (e: 'change', value: {
    orderBy?: string;
    orderSort?: 'ASC' | 'DESC';
    filters: SearchFilter[];
  }): void;
  (e: 'refresh'): void;
  (e: 'toBatchDelete'): void;
  (e: 'toImport'): void;
  (e: 'toExport'): void;
  (e: 'toCancelBatchDelete'): void;
}>();

// User context
const userInfo = ref(appContext.getUser());

// Component refs
const searchPanelRef = ref();

// State management
const selectedMenuMap = ref<{ [key: string]: boolean }>({});
const orderBy = ref<string>();
const orderSort = ref<'ASC' | 'DESC'>();
const searchFilters = ref<SearchFilter[]>([]);
const quickSearchFilters = ref<SearchFilter[]>([]);
const assocFilters = ref<SearchFilter[]>([]);

// Constants
const assocKeys = ['createdBy', 'lastModifiedBy', 'createdDate'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];
const establishedKeys = ['established=1', 'established=0'];

/**
 * Button dropdown menu items for different extraction types
 */
const buttonDropdownMenuItems = [
  {
    name: t('dataVariable.list.searchPanel.dropdown.fileExtract'),
    key: 'file',
    noAuth: true
  },
  {
    name: t('dataVariable.list.searchPanel.dropdown.httpExtract'),
    key: 'http',
    noAuth: true
  },
  {
    name: t('dataVariable.list.searchPanel.dropdown.jdbcExtract'),
    key: 'jdbc',
    noAuth: true
  }
];

/**
 * Search panel configuration options
 */
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('dataVariable.list.searchPanel.searchOptions.namePlaceholder'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: t('dataVariable.list.searchPanel.searchOptions.createdByPlaceholder')
  },
  {
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    placeholder: t('dataVariable.list.searchPanel.searchOptions.createdDatePlaceholder')
  }
];

/**
 * Sort menu configuration
 */
const sortMenuItems: {
  name: string;
  key: string;
  orderSort: 'ASC' | 'DESC';
}[] = [
  {
    name: t('dataVariable.list.searchPanel.sortOptions.byName'),
    key: 'name',
    orderSort: 'DESC'
  },
  {
    name: t('dataVariable.list.searchPanel.sortOptions.byCreator'),
    key: 'createdBy',
    orderSort: 'ASC'
  },
  {
    name: t('dataVariable.list.searchPanel.sortOptions.byCreateTime'),
    key: 'createdDate',
    orderSort: 'ASC'
  },
  {
    name: t('dataVariable.list.searchPanel.sortOptions.byLastModifier'),
    key: 'lastModifiedBy',
    orderSort: 'ASC'
  },
  {
    name: t('dataVariable.list.searchPanel.sortOptions.byLastModifyTime'),
    key: 'lastModifiedDate',
    orderSort: 'DESC'
  }
];

/**
 * Quick search menu items
 */
const menuItems = computed(() => [
  {
    key: '',
    name: t('dataVariable.list.searchPanel.all')
  },
  {
    key: 'createdBy',
    name: t('dataVariable.list.searchPanel.createdByMe')
  },
  {
    key: 'lastModifiedBy',
    name: t('dataVariable.list.searchPanel.modifiedByMe')
  },
  {
    key: 'lastDay',
    name: t('dataVariable.list.searchPanel.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('dataVariable.list.searchPanel.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('dataVariable.list.searchPanel.lastWeek')
  }
]);

/**
 * Format date string based on time key
 *
 * @param key - Time key identifier
 * @returns Array of start and end date strings
 */
const formatDateString = (key: string): [string, string] => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  switch (key) {
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
    startDate ? startDate.format('YYYY-MM-DD HH:mm:ss') : '',
    endDate ? endDate.format('YYYY-MM-DD HH:mm:ss') : ''
  ];
};

/**
 * Get current search parameters
 *
 * @returns Object containing current search configuration
 */
const getCurrentParams = () => {
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
 *
 * @param data - Search panel data with filters
 */
const handleSearchPanelChange = (data: { filters: SearchFilter[] }) => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key));

  // Update selected menu map based on filters
  updateSelectedMenuMap();

  emits('change', getCurrentParams());
};

/**
 * Update selected menu map based on current filters
 */
const updateSelectedMenuMap = () => {
  if (!assocFilters.value.length) {
    // Clear all associated keys
    assocKeys.forEach(key => {
      if (key === 'createdDate') {
        timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
      } else {
        delete selectedMenuMap.value[key];
      }
    });
  } else {
    // Update selected menu map based on filters
    assocKeys.forEach(key => {
      if (key === 'createdBy' || key === 'lastModifiedBy') {
        const filterItem = assocFilters.value.find(item => item.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      }
      if (key === 'createdDate') {
        const filterItem = assocFilters.value.filter(item => item.key === key);
        const timeKey = timeKeys.find(t => selectedMenuMap.value[t]);
        if (timeKey && filterItem.length > 0) {
          const timeValue = formatDateString(timeKey);
          if (timeValue[0] !== filterItem[0].value || timeValue[1] !== filterItem[1].value) {
            delete selectedMenuMap.value[timeKey];
          }
        }
      }
    });
  }
};

/**
 * Handle sort changes
 *
 * @param sortData - Sort configuration data
 */
const handleSortChange = (sortData: SortConfig) => {
  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  emits('change', getCurrentParams());
};

/**
 * Handle menu item clicks for quick search
 *
 * @param data - Menu item data
 */
const handleMenuItemClick = (data: { key: string }) => {
  const key = data.key;
  let searchChangeFlag = false;

  if (selectedMenuMap.value[key]) {
    // Remove selection
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
    // Add selection
    if (key === '') {
      // Select "All" - clear other selections
      selectedMenuMap.value = { '': true };
      quickSearchFilters.value = [];

      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        searchChangeFlag = true;
      }
    } else {
      delete selectedMenuMap.value[''];

      if (timeKeys.includes(key)) {
        // Time-based selection
        timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
        selectedMenuMap.value[key] = true;
      } else if (establishedKeys.includes(key)) {
        // Status-based selection
        establishedKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
        selectedMenuMap.value[key] = true;
      } else {
        selectedMenuMap.value[key] = true;
      }
    }
  }

  // Update quick search filters
  updateQuickSearchFilters();

  if (!searchChangeFlag) {
    emits('change', getCurrentParams());
  }
};

/**
 * Update quick search filters based on selected menu items
 */
const updateQuickSearchFilters = () => {
  const userId = userInfo.value?.id;
  const timeFilters: SearchFilter[] = [];
  const assocFiltersInQuick: any[] = [];

  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (timeKeys.includes(key)) {
      assocFiltersInQuick.push({ valueKey: 'createdDate', value: formatDateString(key) });
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (key === 'createdBy' || key === 'lastModifiedBy') {
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
  }).filter((item): item is SearchFilter => item !== undefined);

  quickSearchFilters.value.push(...timeFilters);

  if (assocFiltersInQuick.length) {
    searchPanelRef.value.setConfigs(assocFiltersInQuick);
  }
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  emits('refresh');
};

/**
 * Handle button dropdown clicks for different extraction types
 *
 * @param key - Extraction type key
 */
const handleButtonDropdownClick = ({ key }: { key: 'file' | 'http' | 'jdbc' }) => {
  switch (key) {
    case 'file':
      router.push('/data#variables?source=FILE');
      break;
    case 'http':
      router.push('/data#variables?source=HTTP');
      break;
    case 'jdbc':
      router.push('/data#variables?source=JDBC');
      break;
  }
};

/**
 * Navigate to create static variable page
 */
const navigateToCreateStaticVariable = () => {
  router.push('/data#variables?source=STATIC');
};

/**
 * Handle batch delete action
 */
const handleBatchDelete = () => {
  emits('toBatchDelete');
};

/**
 * Handle import action
 */
const handleImport = () => {
  emits('toImport');
};

/**
 * Handle export action
 */
const handleExport = () => {
  emits('toExport');
};

/**
 * Handle cancel batch delete action
 */
const handleCancelBatchDelete = () => {
  emits('toCancelBatchDelete');
};

onMounted(() => {
  // Component initialization logic can be added here
});
</script>

<template>
  <div class="mt-2.5 mb-3.5">
    <!-- Quick Search Section -->
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ t('dataVariable.list.searchPanel.quickSearch') }}</span>
        <Colon />
      </div>

      <div class="flex flex-wrap ml-2">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
          @click="handleMenuItemClick(item)">
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
        @change="handleSearchPanelChange" />

      <!-- Action Buttons -->
      <div class="flex items-center space-x-3">
        <!-- Batch Delete Actions (when items are selected) -->
        <template v-if="typeof props.selectedNum === 'number'">
          <Button
            danger
            size="small"
            class="flex items-center flex-shrink-0"
            @click="handleBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <div class="flex items-center">
              <span class="mr-0.5">{{ t('dataVariable.list.searchPanel.buttons.deleteSelected') }}</span>
              <span>({{ props.selectedNum }})</span>
            </div>
          </Button>

          <Button
            size="small"
            class="flex items-center flex-shrink-0"
            @click="handleCancelBatchDelete">
            <Icon icon="icon-fanhui" class="mr-1" />
            <span>{{ t('dataVariable.list.searchPanel.buttons.cancelDelete') }}</span>
          </Button>
        </template>

        <!-- Default Action Buttons -->
        <template v-else>
          <!-- Add Variable Button with Dropdown -->
          <Button
            type="primary"
            size="small"
            class="flex items-center flex-shrink-0 pr-0"
            @click="navigateToCreateStaticVariable">
            <div class="flex items-center">
              <Icon icon="icon-jia" class="text-3.5" />
              <span class="ml-1">{{ t('dataVariable.list.searchPanel.buttons.addStaticVariable') }}</span>
            </div>
            <Dropdown
              :menuItems="buttonDropdownMenuItems"
              @click="handleButtonDropdownClick">
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
            @click="handleBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>{{ t('dataVariable.list.searchPanel.buttons.batchDelete') }}</span>
          </Button>

          <!-- Refresh Button -->
          <IconRefresh
            :loading="props.loading"
            :disabled="props.loading"
            @click="handleRefresh">
            <template #default>
              <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                <Icon icon="icon-shuaxin" class="text-3.5" />
                <span class="ml-1">{{ t('dataVariable.list.searchPanel.buttons.refresh') }}</span>
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
