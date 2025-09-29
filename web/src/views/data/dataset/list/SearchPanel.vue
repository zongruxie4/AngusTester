<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Dropdown, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { useRouter } from 'vue-router';
import { Button, Tooltip } from 'ant-design-vue';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { LoadingProps } from '@/types/types';
import { QuickSearchOptions, createAuditOptions, createTimeOptions, type QuickSearchConfig } from '@/components/quickSearch';

const { t } = useI18n();

const props = withDefaults(defineProps<LoadingProps & { selectedNum?: number }>(), {
  loading: false,
  selectedNum: 0
});

const router = useRouter();

// Component emits
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: {
    orderBy?: string;
    orderSort?: PageQuery.OrderSort;
    filters: SearchCriteria[];
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

// Dropdown menu items
const buttonDropdownMenuItems = [
  {
    name: t('dataset.searchPanel.buttonDropdown.fileExtractDataset'),
    key: 'file',
    noAuth: true
  },
  {
    name: t('dataset.searchPanel.buttonDropdown.jdbcExtractDataset'),
    key: 'jdbc',
    noAuth: true
  }
];

// Search panel options
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('common.placeholders.searchKeyword'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: t('dataset.listsearchPanelOptions.createdByPlaceholder'),
    maxlength: 100
  },
  {
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    maxlength: 100
  }
];

/**
 * Quick search configuration for dataset search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Audit information options
  auditOptions: createAuditOptions([
    {
      key: 'myCreated',
      name: t('dataset.searchPanel.menuItems.myCreated'),
      fieldKey: 'createdBy'
    },
    {
      key: 'myModified',
      name: t('dataset.searchPanel.menuItems.myModified'),
      fieldKey: 'lastModifiedBy'
    }
  ], String(userInfo.value?.id || '')),
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

// Reactive state for search parameters
const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);
const assocKeys = ['createdBy', 'lastModifiedBy', 'createdDate'];

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
const searchChange = (data: SearchCriteria[]) => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key as string));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key as string));
  emits('change', getParams());
};

/**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]): void => {
  // Update quick search filters
  quickSearchFilters.value = searchCriteria;

  // Emit change event with current params
  emits('change', getParams());
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
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <!-- Search Panel and Action Buttons -->
    <div class="flex items-start justify-between">
      <!-- Advanced Search Panel -->
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions as any"
        class="flex-1 mr-3.5"
        @change="searchChange" />

      <!-- Action Buttons -->
      <div class="flex items-center space-x-3">
        <!-- Batch Delete Actions (when items are selected) -->
        <template v-if="(props as any).selectedNum && typeof (props as any).selectedNum === 'number' && (props as any).selectedNum > 0">
          <Button
            danger
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toExecuteBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <div class="flex items-center">
              <span class="mr-0.5">{{ t('dataset.searchPanel.buttons.deleteSelected') }}</span>
              <span>({{ (props as any).selectedNum }})</span>
            </div>
          </Button>

          <Button
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toCancelBatchDelete">
            <Icon icon="icon-fanhui" class="mr-1" />
            <span>{{ t('dataset.searchPanel.buttons.cancelDelete') }}</span>
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
              <span class="ml-1">{{ t('dataset.searchPanel.buttons.addStaticDataset') }}</span>
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
            <span>{{ t('dataset.searchPanel.buttons.batchDelete') }}</span>
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
/* Styles are now handled by QuickSearchOptions component */
</style>
