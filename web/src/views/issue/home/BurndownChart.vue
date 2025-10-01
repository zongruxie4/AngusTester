<script lang="ts" setup>
import { computed, inject, onBeforeUnmount, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import { getDateArr } from '@/utils/utils';
import { analysis } from '@/api/tester';

/**
 * Props interface for BurndownChart component.
 */
interface Props {
  userInfo?: {[key: string]: string}
}

// Element resize detector for chart responsiveness
const resizeDetector = elementResizeDetector({ strategy: 'scroll' });
const props = withDefaults(defineProps<Props>(), {
  userInfo: undefined
});

const { t } = useI18n();

// Inject project information from parent component
const projectId = inject<Ref<string>>('projectId', ref(''));

/**
 * Chart display options for different metrics.
 * <p>
 * Provides options to switch between task count and workload views.
 * </p>
 */
const chartDisplayOptions = computed(() => [
  {
    value: 'NUM',
    label: t('common.count')
  },
  {
    value: 'WORKLOAD',
    label: t('common.workload')
  }
]);

// Chart data and state management
const chartData = ref();
const selectedMetric = ref('NUM');
const chartElementRef = ref();
let chartInstance;
/**
 * ECharts configuration for the burndown chart.
 * <p>
 * Defines the visual configuration including grid, legend, tooltip, axes, and series.
 * </p>
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
    type: 'value',
    min: function (value) {
      if (value.max < 1) {
        return 10;
      } else {
        return undefined;
      }
    }
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
 * Loads burndown chart data from the API.
 * <p>
 * Fetches task assignee burndown data for the current project and user.
 * </p>
 */
const loadChartData = async () => {
  const [error, { data }] = await analysis.getTaskAssigneeBurndown({
    projectId: projectId.value,
    userId: props.userInfo?.id
  });
  if (error) {
    return;
  }
  chartData.value = data;
};

/**
 * Handles chart resize events.
 * <p>
 * Updates chart configuration and triggers resize when container size changes.
 * </p>
 */
const handleChartResize = () => {
  chartInstance.setOption(chartConfiguration);
  chartInstance.resize();
};

onMounted(() => {
  // Initialize ECharts instance
  chartInstance = echarts.init(chartElementRef.value);
  chartInstance.setOption(chartConfiguration);

  // Watch for project ID changes and reload data
  watch(() => projectId.value, (newValue) => {
    if (newValue) {
      loadChartData();
    }
  }, {
    immediate: true
  });

  // Watch for metric selection and data changes to update chart
  watch([() => selectedMetric.value, () => chartData.value], () => {
    if (chartData.value) {
      const currentMetricData = chartData.value[selectedMetric.value];
      const xAxisData = (currentMetricData?.expected || []).map(item => item.timeSeries);
      const expectedData = (currentMetricData?.expected || []).map(item => item.value);
      const remainingData = (currentMetricData?.remaining || []).map(item => item.value);

      chartConfiguration.xAxis.data = xAxisData;
      (chartConfiguration.series[0] as any).data = remainingData;
      (chartConfiguration.series[1] as any).data = expectedData;
    } else {
      // Clear chart data when no data is available
      chartConfiguration.xAxis.data = [];
      (chartConfiguration.series[0] as any).data = [];
      (chartConfiguration.series[1] as any).data = [];
    }

    // Set default data if no data is available
    if (chartConfiguration.xAxis.data.length === 0) {
      (chartConfiguration.xAxis as any).data = getDateArr();
      (chartConfiguration.series[0] as any).data = [0, 0, 0, 0, 0, 0, 0];
    }

    chartInstance.setOption(chartConfiguration);
  });

  // Listen for chart container resize events
  resizeDetector.listenTo(chartElementRef.value, handleChartResize);
});

onBeforeUnmount(() => {
  // Clean up resize listener when component is unmounted
  resizeDetector.removeListener(chartElementRef.value, handleChartResize);
});
</script>
<template>
  <div class="pt-1.5 flex flex-col">
    <!-- Chart header with title and metric selection -->
    <div class="text-3.5 font-semibold flex justify-between">
      {{ t('chart.burndown.myTitle') }}
      <RadioGroup v-model:value="selectedMetric" :options="chartDisplayOptions">
      </RadioGroup>
    </div>

    <!-- Chart container -->
    <div ref="chartElementRef" class="border rounded p-2 flex-1 mt-3">
    </div>
  </div>
</template>
