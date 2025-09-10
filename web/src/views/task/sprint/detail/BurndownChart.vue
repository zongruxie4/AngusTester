<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import { analysis } from '@/api/tester';

const { t } = useI18n();

// Props Definition
interface Props {
  sprintId: string;
}

const props = withDefaults(defineProps<Props>(), {
  sprintId: ''
});

/**
 * Chart data fetched from API
 */
const chartData = ref();

/**
 * Currently selected chart type (NUM for task count, WORKLOAD for workload)
 */
const selectedChartType = ref('NUM');

/**
 * Reference to the chart DOM element
 */
const chartRef = ref();

/**
 * ECharts instance for the burndown chart
 */
let chartInstance: echarts.ECharts;

/**
 * Available chart type options for the radio group
 */
const chartTypeOptions = computed(() => [
  {
    value: 'NUM',
    label: t('taskSprint.burndown.taskCount')
  },
  {
    value: 'WORKLOAD',
    label: t('taskSprint.burndown.workload')
  }
]);

/**
 * ECharts configuration object for the burndown chart
 */
const chartConfig = {
  grid: {
    left: '30',
    right: '20',
    bottom: '60',
    top: 20
  },
  legend: {
    show: true,
    bottom: 0,
    type: 'plain'
  },
  tooltip: {
    show: true,
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: t('taskSprint.burndown.remaining'),
      data: [],
      type: 'line'
    },
    {
      name: t('taskSprint.burndown.expected'),
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

/**
 * Fetches burndown chart data from the API
 */
const loadChartData = async () => {
  const [error, { data }] = await analysis.getSprintBurndown(props.sprintId);
  if (error) {
    return;
  }
  chartData.value = data;
};

/**
 * Updates the chart with new data based on selected chart type
 */
const updateChartData = () => {
  if (chartData.value) {
    const currentData = chartData.value[selectedChartType.value];
    const expectedData = currentData?.expected || [];
    const remainingData = currentData?.remaining || [];

    const timeSeriesData = expectedData.map(item => item.timeSeries);
    const expectedValues = expectedData.map(item => item.value);
    const remainingValues = remainingData.map(item => item.value);

    chartConfig.xAxis.data = timeSeriesData;
    chartConfig.series[0].data = remainingValues;
    chartConfig.series[1].data = expectedValues;
  } else {
    // Clear chart data if no data available
    chartConfig.xAxis.data = [];
    chartConfig.series[0].data = [];
    chartConfig.series[1].data = [];
  }

  chartInstance.setOption(chartConfig);
};

// ===== Lifecycle Hooks =====
onMounted(async () => {
  // Load initial chart data
  await loadChartData();

  // Initialize ECharts instance
  chartInstance = echarts.init(chartRef.value);
  chartInstance.setOption(chartConfig);

  // Watch for changes in chart type or data and update chart accordingly
  watch([() => selectedChartType.value, () => chartData.value], updateChartData, { immediate: true });
});

</script>
<template>
  <RadioGroup v-model:value="selectedChartType" :options="chartTypeOptions" />
  <div ref="chartRef" class="border rounded p-2 my-3 h-60">
  </div>
</template>
