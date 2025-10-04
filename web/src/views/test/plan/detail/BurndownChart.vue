<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// Interfaces
interface Props {
  planId: string;
}

// Props
const props = withDefaults(defineProps<Props>(), {
  planId: ''
});

// Computed properties
const chartTypeOptions = computed(() => [
  {
    value: 'NUM',
    label: t('common.count')
  },
  {
    value: 'WORKLOAD',
    label: t('common.workload')
  }
]);

// Reactive data
const chartData = ref();
const selectedChartType = ref('NUM');
const chartContainerRef = ref();
let chartInstance;

// Chart configuration
const chartConfiguration = {
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
      name: t('chart.burndown.remaining'),
      data: [],
      type: 'line'
    },
    {
      name: t('chart.burndown.expected'),
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

/**
 * Loads burndown chart data from the API based on the current plan ID.
 * <p>
 * Updates the chartData reactive reference with the fetched data.
 * </p>
 */
const loadChartData = async () => {
  const [error, { data }] = await analysis.getFuncPlanBurndown(props.planId);
  if (error) {
    return;
  }
  chartData.value = data;
};

/**
 * Updates the chart with new data based on the selected chart type.
 * <p>
 * Extracts time series data and values for both expected and remaining series.
 * </p>
 */
const updateChartData = () => {
  if (chartData.value) {
    const currentData = chartData.value[selectedChartType.value];
    const timeSeriesData = (currentData?.expected || []).map(item => item.timeSeries);
    const expectedValues = (currentData?.expected || []).map(item => item.value);
    const remainingValues = (currentData?.remaining || []).map(item => item.value);

    chartConfiguration.xAxis.data = timeSeriesData;
    chartConfiguration.series[0].data = remainingValues;
    chartConfiguration.series[1].data = expectedValues;
  } else {
    chartConfiguration.xAxis.data = [];
    chartConfiguration.series[0].data = [];
    chartConfiguration.series[1].data = [];
  }
  chartInstance.setOption(chartConfiguration);
};

// Lifecycle hooks
onMounted(async () => {
  await loadChartData();

  chartInstance = echarts.init(chartContainerRef.value);
  chartInstance.setOption(chartConfiguration);

  watch([() => selectedChartType.value, () => chartData.value], () => {
    updateChartData();
  }, { immediate: true });
});

</script>
<template>
  <RadioGroup v-model:value="selectedChartType" :options="chartTypeOptions" />
  <div ref="chartContainerRef" class="border rounded p-2 my-3 h-60">
  </div>
</template>
