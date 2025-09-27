<script lang="ts" setup>
import { ref, computed } from 'vue';
import { IconCount, IconRefresh, SearchPanel, Hints } from '@xcan-angus/vue-ui';
import { Tooltip } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { PageQuery, SearchCriteria, appContext } from '@xcan-angus/infra';
import { QuickSearchOptions, createAuditOptions, createTimeOptions, type QuickSearchConfig } from '@/components/quickSearch';

import { useActivitySearch } from './composables/useActivitySearch';

const { t } = useI18n();

// User context
const userInfo = ref(appContext.getUser());

// Component props
interface Props {
  loading: boolean;
  selectedNum?: number;
  showCount?: boolean;
  maxResource?: number;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  maxResource: 0
});

// Component emits
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: {
    orderBy?: string;
    orderSort?: PageQuery.OrderSort;
    filters: SearchCriteria[]
  }): void;
  (e: 'refresh'): void;
  (e: 'openCount'): void;
  (e: 'toImport'): void;
  (e: 'toExport'): void;
  (e: 'toCancelBatchDelete'): void;
}>();

// Use composables
const {
  searchPanelOptions,
  buildSearchParams,
  handleSearchChange,
  clearAllFilters
} = useActivitySearch();

/**
 * Quick search configuration for activity search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Audit information options
  auditOptions: createAuditOptions([
    {
      key: 'myActivity',
      name: t('quickSearch.myActivity'),
      fieldKey: 'userId'
    }
  ], String(userInfo.value?.id || '')),
  // Time options
  timeOptions: createTimeOptions([
    { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
    { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
    { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
  ], 'optDate'),
  // External clear function
  externalClearFunction: () => {
    if (typeof searchPanelRef.value?.clear === 'function') {
      searchPanelRef.value.clear();
    }
  }
}));

// Template refs
const searchPanelRef = ref();

/**
 * Handle search panel change events
 *
 * @param data - Search panel data
 */
const onSearchChange = (data: SearchCriteria[]) => {
  handleSearchChange(data);
  emits('change', buildSearchParams());
};

/**
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (_selectedKeys: string[], _searchCriteria: SearchCriteria[]): void => {
  // Update quick search filters in the composable
  // This will be handled by the composable's internal state
  emits('change', buildSearchParams());
};

/**
 * Handle refresh button click
 */
const handleRefresh = () => {
  emits('refresh');
};

/**
 * Handle toggle statistics panel visibility
 */
const handleOpenCount = () => {
  emits('openCount');
};

/**
 * Clear all filters and reset search
 */
const clearAll = () => {
  clearAllFilters();
  if (typeof searchPanelRef.value?.clear === 'function') {
    searchPanelRef.value.clear();
  }
  emits('change', buildSearchParams());
};

// Expose methods to parent component
defineExpose({
  setConfigs: (configs: Array<{ valueKey: string; value: any }>) => {
    if (searchPanelRef.value?.setConfigs) {
      searchPanelRef.value.setConfigs(configs);
    }
  },
  clear: clearAll
});
</script>

<template>
  <div class="mt-8 mb-3.5">
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      :descriptionSlot="t('activity.messages.maxResourceActivityHint', { maxResource })"
      @change="handleQuickSearchChange" />

    <!-- Search panel and action buttons -->
    <div class="flex items-start justify-between">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions as any"
        class="flex-1 mr-3.5"
        @change="onSearchChange" />

      <div class="flex items-center space-x-3">
        <!-- Toggle statistics panel button -->
        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="props.showCount ? t('activity.searchPanel.ui.hideStatistics') : t('activity.searchPanel.ui.viewStatistics')">
          <IconCount
            :value="props.showCount"
            class="flex-none text-4.5"
            @click="handleOpenCount" />
        </Tooltip>

        <!-- Refresh button -->
        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="t('actions.refresh')">
          <IconRefresh
            :loading="loading"
            class="text-4.5 ml-2"
            @click="handleRefresh" />
        </Tooltip>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Styles are now handled by QuickSearchOptions component */
</style>
