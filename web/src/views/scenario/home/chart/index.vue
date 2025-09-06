<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { analysis } from '@/api/tester';

import { ResourceInfo } from './types';

// ==================== Types and Interfaces ====================

/**
 * <p>Component props interface for ChartContainer component.</p>
 * <p>Defines the project ID and refresh notification properties.</p>
 */
type Props = {
  projectId: string;
  notify: string;
}

// ==================== Component Setup ====================

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  notify: undefined
});

// ==================== Async Components ====================

/**
 * <p>Lazy-loaded bar chart component for displaying scenario counts by plugin.</p>
 * <p>Uses defineAsyncComponent for code splitting and performance optimization.</p>
 */
const BarChartComponent = defineAsyncComponent(() => import('./BarChart.vue'));

/**
 * <p>Lazy-loaded pie chart component for displaying scenario counts by test type.</p>
 * <p>Uses defineAsyncComponent for code splitting and performance optimization.</p>
 */
const PieChartComponent = defineAsyncComponent(() => import('./PieChart.vue'));

// ==================== Reactive State ====================

/**
 * <p>Injected window resize notification for responsive chart updates.</p>
 * <p>Used to trigger chart resize when window dimensions change.</p>
 */
const windowResizeNotification = inject('windowResizeNotify', ref<string>());

/**
 * <p>Resource information data source for both charts.</p>
 * <p>Contains scenario counts by various dimensions (plugin, test type, time periods).</p>
 */
const chartDataSource = ref<ResourceInfo>();

// ==================== Data Management Methods ====================

/**
 * <p>Fetches scenario resource count data from the API.</p>
 * <p>Retrieves statistics for the specified project and updates chart data source.</p>
 * <p>Handles API errors gracefully without throwing exceptions.</p>
 */
const fetchScenarioResourceData = async (): Promise<void> => {
  const requestParams = { projectId: props.projectId };
  const [error, response] = await analysis.getScenarioResourceCount(requestParams);
  
  if (error) {
    // Log error or handle gracefully - API call failed
    return;
  }

  chartDataSource.value = response?.data as ResourceInfo;
};

/**
 * <p>Resets chart data source to default empty state.</p>
 * <p>Initializes all counters to zero for clean chart display.</p>
 * <p>Used when switching projects or refreshing data.</p>
 */
const resetChartDataSource = (): void => {
  chartDataSource.value = {
    allSce: '0',
    sceByLastWeek: '0',
    sceByLastMonth: '0',
    sceByScriptType: {
      TEST_FUNCTIONALITY: '0',
      TEST_PERFORMANCE: '0',
      TEST_STABILITY: '0',
      TEST_CUSTOMIZATION: '0',
      MOCK_DATA: '0',
      MOCK_APIS: '0'
    },
    sceByPluginName: {}
  };
};

// ==================== Lifecycle Hooks ====================

onMounted(() => {
  // Watch for project ID changes and reload data accordingly
  watch(() => props.projectId, (newProjectId) => {
    if (!newProjectId) {
      return;
    }

    resetChartDataSource();
    fetchScenarioResourceData();
  }, { immediate: true });

  // Watch for refresh notifications and reload data
  watch(() => props.notify, (refreshNotification) => {
    if (refreshNotification === undefined || refreshNotification === null || refreshNotification === '') {
      return;
    }

    fetchScenarioResourceData();
  }, { immediate: true });
});
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('scenarioHome.chart.resourceStats') }}</div>
    <div class="flex space-x-3.75">
      <BarChartComponent
        :dataSource="chartDataSource"
        :resizeNotify="windowResizeNotification"
        style="width:calc(50% - 8px)" />
      <PieChartComponent
        :dataSource="chartDataSource"
        :resizeNotify="windowResizeNotification"
        style="width:calc(50% - 8px)" />
    </div>
  </div>
</template>
