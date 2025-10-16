<script lang="ts" setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';

interface Props {
  progressValues: number[];
  typeValues: number[];
}

const props = withDefaults(defineProps<Props>(), {
  progressValues: () => ([0, 0, 0, 0]),
  typeValues: () => ([0, 0, 0, 0])
});

const { t } = useI18n();

/**
 * DOM refs for ECharts containers
 */
const testProgressRef = ref<HTMLElement | null>(null);
const testTypeRef = ref<HTMLElement | null>(null);

/**
 * ECharts instances (disposed on unmount)
 */
let testProgressChart: echarts.ECharts | null = null;
let testTypeChart: echarts.ECharts | null = null;
/**
 * Bar chart: test progress stats
 */
const testProgressChartConfig = {
  title: {
    text: t('service.serviceTestDetail.chart.testApiStats'),
    bottom: 5,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '60',
    right: '20',
    bottom: '50',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [t('status.tested'), t('status.notTested'), t('status.passed'), t('status.notPassed')]
  },
  yAxis: {
    type: 'value'
  },
  tooltip: {
    show: true
  },
  series: [
    {
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barMaxWidth: '20',
      data: [] as number[],
      type: 'bar',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

/**
 * Bar chart: test type stats
 */
const testTypeChartConfig = {
  title: {
    text: t('service.serviceTestDetail.chart.testTypeStats'),
    bottom: 5,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '60',
    right: '20',
    bottom: '50',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [t('service.serviceTestDetail.chart.totalTestNum'), t('service.serviceTestDetail.chart.functionalTest'), t('service.serviceTestDetail.chart.performanceTest'), t('service.serviceTestDetail.chart.stabilityTest')]
  },
  yAxis: {
    type: 'value'
  },
  tooltip: {
    show: true
  },
  series: [
    {
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barMaxWidth: '20',
      data: [] as number[],
      type: 'bar',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

/**
 * Update chart series data and apply options
 */
const updateCharts = () => {
  testProgressChartConfig.series[0].data = props.progressValues;
  testTypeChartConfig.series[0].data = props.typeValues;
  if (testProgressChart) testProgressChart.setOption(testProgressChartConfig);
  if (testTypeChart) testTypeChart.setOption(testTypeChartConfig);
};

/**
 * Handle container resizing for responsive charts
 */
const handleResize = () => {
  if (testProgressChart) testProgressChart.resize();
  if (testTypeChart) testTypeChart.resize();
};

onMounted(() => {
  if (testProgressRef.value) {
    testProgressChart = echarts.init(testProgressRef.value);
    testProgressChart.setOption(testProgressChartConfig);
  }
  if (testTypeRef.value) {
    testTypeChart = echarts.init(testTypeRef.value);
    testTypeChart.setOption(testTypeChartConfig);
  }
  watch([() => props.progressValues, () => props.typeValues], updateCharts, { immediate: true });
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (testProgressChart) {
    testProgressChart.dispose();
    testProgressChart = null;
  }
  if (testTypeChart) {
    testTypeChart.dispose();
    testTypeChart = null;
  }
});

</script>
<template>
  <div class="flex-1 ml-4 space-x-4 min-w-100 inline-flex">
    <div ref="testProgressRef" class="h-40  flex-1"></div>
    <div ref="testTypeRef" class="h-40 flex-1"></div>
  </div>
</template>
