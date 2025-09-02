<script setup lang='ts'>
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Table } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';

import SearchPanel from '@/views/project/activity/searchPanel.vue';
import Dashboard from '@/components/dashboard/Dashboard.vue';

import { useActivityData } from './composables/useActivityData';
import { useTableColumns } from './composables/useTableColumns';
import { useDashboardConfig } from './composables/useDashboardConfig';
import type { SearchPanelChangeData } from './types';

// Internationalization
const { t } = useI18n();

// Use composables
const {
  tableData,
  loading,
  maxResource,
  pagination,
  loadActivityList,
  loadMaxResourceSetting,
  handleTableChange,
  updateSearchParams,
  refreshAllData
} = useActivityData();

const { columns, defaultTableProps } = useTableColumns();
const { dashboardConfig, dashboardConstants } = useDashboardConfig();

// UI state
const showCount = ref(true);

/**
 * Toggle statistics panel visibility
 */
const toggleCount = () => {
  showCount.value = !showCount.value;
};

/**
 * Handle search panel change events
 *
 * @param data - Search panel change data
 */
const handleSearchChange = (data: SearchPanelChangeData) => {
  updateSearchParams(data);
};

/**
 * Handle refresh events
 */
const handleRefresh = () => {
  refreshAllData();
};

/**
 * Debounced table change handler for better performance
 */
const debouncedTableChange = debounce(
  duration.search,
  (_pagination: any, _filters: any, sorter: any) => {
    handleTableChange(_pagination, _filters, sorter);
  }
);

// Initialize data on component mount
onMounted(() => {
  loadActivityList();
  loadMaxResourceSetting();
});
</script>

<template>
  <div class="p-3.5 px-5 text-3">
    <!-- Statistics dashboard panel -->
    <div v-if="showCount" class="mb-4">
      <Dashboard
        :config="dashboardConfig"
        :apiRouter="dashboardConstants.apiRouter"
        :resource="dashboardConstants.resource"
        :barTitle="dashboardConstants.barTitle"
        :dateType="dashboardConstants.dateType"
        :showChartParam="dashboardConstants.showChartParam" />
    </div>

    <!-- Search panel component -->
    <SearchPanel
      :loading="loading"
      :showCount="showCount"
      :maxResource="maxResource"
      @openCount="toggleCount"
      @change="handleSearchChange"
      @refresh="handleRefresh" />

    <!-- Activity table -->
    <Table
      v-bind="defaultTableProps"
      :loading="loading"
      :columns="columns"
      :dataSource="tableData"
      :pagination="pagination"
      :noDataText="t('common.noData')"
      @change="debouncedTableChange">
      <template #bodyCell="{ column, text, record }">
        <!-- Operator column with avatar -->
        <template v-if="column.dataIndex === 'fullName'">
          <div class="flex items-center">
            <Image
              :src="record.avatar"
              class="w-5 h-5 rounded-full flex-none mr-1"
              type="avatar" />
            {{ text }}
          </div>
        </template>

        <!-- Activity detail column with HTML content -->
        <template v-if="column.dataIndex === 'detail'">
          <div
            class="w-full truncate cursor-pointer"
            :title="text"
            v-html="text">
          </div>
        </template>
      </template>
    </Table>
  </div>
</template>
