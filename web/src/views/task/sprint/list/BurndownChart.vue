<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import { analysis } from '@/api/tester';

const { t } = useI18n();

// Component Props & Emits
interface Props {
  visible: boolean;
  sprintId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  sprintId: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean): void }>();

// Reactive Data
const chartData = ref();
const selectedChartType = ref('NUM');
const chartContainerRef = ref();
let chartInstance: echarts.ECharts;

/**
 * <p>
 * Chart type options for burndown chart display.
 * <p>
 * Provides options to switch between task count and workload views.
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
 * <p>
 * ECharts configuration object for burndown chart.
 * <p>
 * Defines the visual appearance and behavior of the chart including
 * grid layout, legend, tooltip, axes, and data series.
 */
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
 * <p>
 * Closes the burndown chart modal.
 * <p>
 * Emits update event to parent component to hide the modal.
 */
const closeModal = () => {
  emits('update:visible', false);
};

/**
 * <p>
 * Loads burndown chart data from the API.
 * <p>
 * Fetches sprint burndown data based on the current sprint ID
 * and updates the chart data reactive reference.
 */
const loadBurndownData = async () => {
  const [error, { data }] = await analysis.getSprintBurndown(props.sprintId);
  if (error) {
    return;
  }
  chartData.value = data;
};

/**
 * <p>
 * Updates chart data based on selected chart type and loaded data.
 * <p>
 * Transforms the raw API data into chart-ready format by extracting
 * time series and values for both expected and remaining data series.
 */
const updateChartData = () => {
  if (chartData.value) {
    const currentDataType = chartData.value[selectedChartType.value];
    const expectedData = currentDataType?.expected || [];
    const remainingData = currentDataType?.remaining || [];

    const timeSeriesData = expectedData.map(item => item.timeSeries);
    const expectedValues = expectedData.map(item => item.value);
    const remainingValues = remainingData.map(item => item.value);

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

// Lifecycle Hooks
onMounted(() => {
  // Initialize ECharts instance
  chartInstance = echarts.init(chartContainerRef.value);
  chartInstance.setOption(chartConfiguration);

  // Watch for modal visibility changes to load data
  watch(() => props.visible, (isVisible) => {
    if (isVisible) {
      loadBurndownData();
    }
  }, {
    immediate: true
  });

  // Watch for chart type or data changes to update chart
  watch([() => selectedChartType.value, () => chartData.value], () => {
    updateChartData();
  });
});

</script>
<template>
  <Modal
    :visible="props.visible"
    :footer="null"
    :width="800"
    :title="t('taskSprint.burndown.title')"
    @cancel="closeModal">
    <div class="pt-1.5">
      <RadioGroup v-model:value="selectedChartType" :options="chartTypeOptions" />
      <div ref="chartContainerRef" class="border rounded p-2 my-3 h-60">
      </div>
    </div>
  </Modal>
</template>
