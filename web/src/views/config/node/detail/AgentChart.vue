<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { IntervalTimestamp } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useAgentChart } from './composables/useAgentChart';
import type { AgentChartProps } from './types';

/**
 * <p>Async chart components for better performance</p>
 * <p>Lazy loads chart components to reduce initial bundle size</p>
 */
const CacheChart = defineAsyncComponent(() => import('@/components/chart/mock/cacheChart.vue'));
const RamChart = defineAsyncComponent(() => import('@/components/chart/mock/ramChart.vue'));
const CpuChart = defineAsyncComponent(() => import('@/components/chart/mock/cpuChart.vue'));

/**
 * <p>Component props with default values</p>
 * <p>Provides default values for optional props</p>
 */
const props = withDefaults(defineProps<AgentChartProps>(), {
  id: undefined
});

/**
 * <p>Agent chart data management</p>
 * <p>Handles chart data processing and state management for cache, RAM, and CPU charts</p>
 */
const {
  cacheChartData,
  ramChartData,
  cpuChartData,
  getChartData
} = useAgentChart(props);
</script>

<template>
  <div class="agent-chart-container">
    <!-- Chart Header -->
    <div class="chart-header">
      <h3 class="chart-title">进程监控</h3>

      <!-- Time Interval Selector -->
      <div class="time-selector">
        <IntervalTimestamp
          :action="`${TESTER}/mock/service/${props.id}/metrics`"
          @change="getChartData" />
      </div>
    </div>

    <!-- Chart Grid -->
    <div class="chart-grid">
      <!-- Cache Chart -->
      <div class="chart-item">
        <CacheChart
          :xData="cacheChartData.xData"
          :numData="cacheChartData.numData"
          :totalData="cacheChartData.totalData"
          :usedData="cacheChartData.usedData"
          :maxNumData="cacheChartData.maxNumData"
          :maxTotalData="cacheChartData.maxTotalData" />
      </div>

      <!-- RAM Chart -->
      <div class="chart-item">
        <RamChart
          :xData="ramChartData.xData"
          :submitData="ramChartData.submitData"
          :totalData="ramChartData.totalData"
          :usedData="ramChartData.usedData" />
      </div>

      <!-- CPU Chart -->
      <div class="chart-item">
        <CpuChart
          :xData="cpuChartData.xData"
          :totalData="cpuChartData.totalData"
          :systemCpuUsed="cpuChartData.systemCpuUsed"
          :processCpuUsed="cpuChartData.processCpuUsed" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.agent-chart-container {
  padding: 20px 0;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-right: 60px;
}

.chart-title {
  font-weight: 500;
  font-size: 16px;
  color: var(--text-primary);
  margin: 0;
}

.time-selector {
  display: flex;
  align-items: center;
}

.chart-grid {
  display: flex;
  height: 240px;
  gap: 8px;
  margin-top: 8px;
}

.chart-item {
  flex: 1;
  min-width: 0;
}

/* Responsive adjustments */
@media (max-width: 1200px) {
  .chart-grid {
    flex-direction: column;
    height: auto;
  }

  .chart-item {
    height: 200px;
  }
}
</style>
