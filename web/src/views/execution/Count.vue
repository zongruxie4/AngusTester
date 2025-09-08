<script setup lang="ts">
import { computed, inject, onMounted, Ref, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useChartData } from './composables/useChartData';
import Charts from './PieChart.vue';
import type { ProjectInfo } from './types';

const { t } = useI18n();

// Inject project information
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const projectId = computed(() => projectInfo.value?.id);

// Use chart data composable
const {
  pieChartData,
  scriptTypeData,
  scriptTypeColors,
  statusData,
  statusColors,
  loadChartData
} = useChartData(projectId.value || '');

/**
 * Initialize chart data on component mount
 */
const initializeChart = async () => {
  await loadChartData();
};

onMounted(() => {
  initializeChart();
});

defineExpose({
  loadCount: loadChartData
});
</script>
<template>
  <div class="charts-container flex-shrink-0  h-45">
    <Charts
      key="1"
      style="width: 228px;"
      class="chart-item"
      :title="t('execution.chartInfo.scriptType')"
      type="script_type"
      :color="scriptTypeColors"
      :total="pieChartData[0]?.total"
      :data-source="scriptTypeData" />
    <Charts
      key="2"
      style="width: 274px;"
      class="chart-item"
      :title="t('execution.chartInfo.executionStatus')"
      type="status"
      :color="statusColors"
      :total="pieChartData[1]?.total"
      :data-source="statusData" />
  </div>
</template>
<style scoped>
.charts-container {
  height: 180px;
  white-space: nowrap;
}

.chart-item {
  display: inline-block;
}

.chart-item + .chart-item {
  margin-left: 0;
}

@media screen and (min-width: 1480px) {
  .chart-item + .chart-item {
    margin-left: 40px;
  }
}
</style>
