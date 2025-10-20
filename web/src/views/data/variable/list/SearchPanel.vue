<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Dropdown, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button, Tooltip } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { QuickSearchOptions } from 'src/components/form/quickSearch';

// Import composables
import { useSearchPanel } from './composables/useSearchPanel';
import { useSearchActions } from './composables/useSearchActions';

const { t } = useI18n();

// Component props
export type LoadingProps = {
  loading: boolean;
  selectedNum: 0;
}
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false,
  selectedNum: 0
});

// Component emits

const emits = defineEmits<{
  (e: 'change', value: {
    orderBy?: string;
    orderSort?: PageQuery.OrderSort;
    filters: SearchCriteria[];
  }): void;
  (e: 'refresh'): void;
  (e: 'toBatchDelete'): void;
  (e: 'toImport'): void;
  (e: 'toExport'): void;
  (e: 'toCancelBatchDelete'): void;
}>();

// Component refs
const searchPanelRef = ref();

// Use composables
const {
  // State
  selectedMenuMap,
  SearchCriterias: searchFilters,
  quickSearchCriterias: quickSearchFilters,
  assocFilters,

  // Constants
  assocKeys,
  establishedKeys,

  // Configuration
  buttonDropdownMenuItems,
  searchPanelOptions,
  quickSearchConfig,

  // Methods
  getCurrentParams,
  updateSelectedMenuMap,
  updateQuickSearchCriterias: updateQuickSearchFilters
} = useSearchPanel(searchPanelRef);

const {
  // Navigation methods
  handleButtonDropdownClick,
  navigateToCreateStaticVariable,

  // Action methods
  handleBatchDelete,
  handleImport,
  handleExport,
  handleCancelBatchDelete,
  handleRefresh,

  // Menu and search methods
  handleSearchPanelChange: handleSearchPanelChangeAction
} = useSearchActions(
  searchPanelRef,
  selectedMenuMap,
  [],
  assocKeys,
  establishedKeys,
  updateQuickSearchFilters,
  emits
);

/**
 * Handle search panel changes
 *
 * @param data - Search panel data with filters
 */
const handleSearchPanelChangeWrapper = (data: { filters?: SearchCriteria[] }) => {
  // Ensure data has the correct structure
  const normalizedData = {
    filters: data.filters || []
  };

  handleSearchPanelChangeAction(
    normalizedData,
    assocKeys,
    searchFilters,
    assocFilters,
    updateSelectedMenuMap
  );

  // Emit change event with current params
  emits('change', getCurrentParams());
};

/**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (
  _selectedKeys: string[],
  searchCriteria: SearchCriteria[]
): void => {
  // Update quick search filters
  quickSearchFilters.value = searchCriteria;

  // Emit change event with current params
  emits('change', getCurrentParams());
};

onMounted(() => {
  // Component initialization logic can be added here
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
        width="260"
        @change="handleSearchPanelChangeWrapper" />

      <!-- Action Buttons -->
      <div class="flex items-center space-x-3">
        <!-- Batch Delete Actions (when items are selected) -->
        <template v-if="props.selectedNum && props.selectedNum > 0">
          <Button
            danger
            size="small"
            class="flex items-center flex-shrink-0"
            @click="handleBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <div class="flex items-center">
              <span class="mr-0.5">{{ t('dataCommon.searchPanel.batchActions.deleteSelected') }}</span>
              <span>({{ props.selectedNum }})</span>
            </div>
          </Button>

          <Button
            size="small"
            class="flex items-center flex-shrink-0"
            @click="handleCancelBatchDelete">
            <Icon icon="icon-fanhui" class="mr-1" />
            <span>{{ t('dataCommon.searchPanel.batchActions.cancelDelete') }}</span>
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
              <span class="ml-1">{{ t('variable.actions.static') }}</span>
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
            <span>{{ t('dataCommon.searchPanel.batchActions.batchDelete') }}</span>
          </Button>

          <!-- Refresh Button -->
          <IconRefresh
            :loading="props.loading"
            :disabled="props.loading"
            @click="handleRefresh">
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
            :title="t('variable.actions.uploadVariable')">
            <Icon
              icon="icon-shangchuan"
              class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
              @click="handleImport" />
          </Tooltip>

          <!-- Export Button -->
          <Tooltip
            arrowPointAtCenter
            placement="topLeft"
            :title="t('variable.actions.downloadVariable')">
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
