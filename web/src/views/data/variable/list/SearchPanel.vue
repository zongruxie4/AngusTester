<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Colon, Dropdown, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button, Tooltip } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

// Import composables
import { useSearchPanel } from './composables/useSearchPanel';
import { useSearchActions } from './composables/useSearchActions';

// Import types
import type { SearchPanelProps, SearchFilter } from './types';

const { t } = useI18n();

// Component props
const props = withDefaults(defineProps<SearchPanelProps>(), {
  loading: false
});

// Component emits
// eslint-disable-next-line func-call-spacing
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

// Component refs
const searchPanelRef = ref();

// Use composables
const {
  // State
  userInfo,
  selectedMenuMap,
  searchFilters,
  quickSearchFilters,
  assocFilters,

  // Constants
  assocKeys,
  timeKeys,
  establishedKeys,

  // Computed
  menuItems,

  // Configuration
  buttonDropdownMenuItems,
  searchPanelOptions,

  // Methods
  formatDateString,
  getCurrentParams,
  updateSelectedMenuMap,
  updateQuickSearchFilters
} = useSearchPanel();

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
  handleMenuItemClick,
  handleSearchPanelChange: handleSearchPanelChangeAction
} = useSearchActions(
  searchPanelRef,
  selectedMenuMap,
  timeKeys,
  assocKeys,
  establishedKeys,
  formatDateString,
  updateQuickSearchFilters,
  emits
);

/**
 * Handle search panel changes
 *
 * @param data - Search panel data with filters
 */
const handleSearchPanelChangeWrapper = (data: { filters: SearchFilter[] }) => {
  handleSearchPanelChangeAction(
    data,
    assocKeys,
    searchFilters,
    assocFilters,
    updateSelectedMenuMap
  );

  // Emit change event with current params
  emits('change', getCurrentParams());
};

/**
 * Handle menu item clicks for quick search
 */
const handleMenuItemClickWrapper = (data: { key: string }) => {
  handleMenuItemClick(
    data,
    userInfo.value,
    [...quickSearchFilters.value, ...searchFilters.value, ...assocFilters.value]
  );

  // Emit change event with current params
  emits('change', getCurrentParams());
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
          @click="handleMenuItemClickWrapper(item)">
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
        @change="handleSearchPanelChangeWrapper" />

      <!-- Action Buttons -->
      <div class="flex items-center space-x-3">
        <!-- Batch Delete Actions (when items are selected) -->
        <template v-if="props.selectedNum && typeof props.selectedNum === 'number' && props.selectedNum > 0">
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
