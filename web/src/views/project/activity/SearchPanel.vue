<script lang="ts" setup>
import { ref } from 'vue';
import { Colon, IconCount, IconRefresh, SearchPanel, Hints } from '@xcan-angus/vue-ui';
import { Tooltip } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { useActivitySearch } from './composables/useActivitySearch';

const { t } = useI18n();

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
    orderSort?: 'ASC' | 'DESC';
    filters: { key: string; op: string; value: string | string[] }[]
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
  menuItems,
  selectedMenuMap,
  buildSearchParams,
  handleSearchChange,
  handleMenuItemClick,
  clearAllFilters
} = useActivitySearch();

// Template refs
const searchPanelRef = ref();

/**
 * Handle search panel change events
 *
 * @param data - Search panel data
 */
const onSearchChange = (data: { key: string; op: string; value: string | string[] }[]) => {
  handleSearchChange(data);
  emits('change', buildSearchParams());
};

/**
 * Handle quick search menu item clicks
 *
 * @param data - Menu item data
 */
const onMenuItemClick = (data: { key: string }) => {
  handleMenuItemClick(data);

  // Special handling for clearing all filters
  if (data.key === '' && typeof searchPanelRef.value?.clear === 'function') {
    searchPanelRef.value.clear();
  }

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
  <div class="mt-2.5 mb-3.5">
    <!-- Quick search menu -->
    <div class="flex items-center mb-3">
      <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 rounded-full"></div>
      <div class="whitespace-nowrap text-3 text-text-sub-content">
        <span>{{ t('activity.searchPanel.ui.quickQuery') }}</span>
        <Colon />
      </div>
      <div class="flex flex-wrap items-center ml-2">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold"
          @click="onMenuItemClick(item)">
          {{ item.name }}
        </div>
        <Hints :text="t('activity.messages.maxResourceActivityHint', { maxResource })" />
      </div>
    </div>

    <!-- Search panel and action buttons -->
    <div class="flex items-start justify-between">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="onSearchChange" />
      <!-- Max resource hint -->

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
          :title="t('common.refresh')">
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
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
