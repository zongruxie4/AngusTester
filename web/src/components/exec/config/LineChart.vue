<script setup lang="ts">
// Vue core imports
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';

// ECharts core imports
import * as echarts from 'echarts/core';

// ECharts component imports
import { GridComponent, TooltipComponent } from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

const { t } = useI18n();

/**
 * Component props interface for execution concurrency line chart
 */
export interface Props {
  unit: string;
  xData: string[];
  yData: number[];
  maxThreads: number;
  maxThreadsTime: number;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  unit: '',
  xData: () => [],
  yData: () => [],
  maxThreads: 0,
  maxThreadsTime: 0
});

// ECharts component registration
echarts.use([GridComponent, TooltipComponent, LineChart, CanvasRenderer, UniversalTransition]);

// Component state and references
const chartContainerRef = ref();
let chartInstance: echarts.ECharts;

/**
 * Initialize the execution concurrency line chart with configuration
 */
const initializeExecutionConcurrencyChart = () => {
  if (!chartContainerRef.value) {
    return;
  }
  chartInstance = echarts.init(chartContainerRef.value);
  chartInstance.setOption(chartConfiguration);
  window.addEventListener('resize', handleChartResize);
};

/**
 * Handle chart resize on window resize
 */
const handleChartResize = () => {
  chartInstance.resize();
};

// Chart configuration options
const chartConfiguration = {
  grid: {
    top: 20,
    right: 40,
    bottom: 60,
    left: 70
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'line',
      snap: true,
      lineStyle: {
        cap: 'round',
        color: '#999',
        width: 1,
        type: 'dashed'
      }
    },
    textStyle: {
      fontSize: 12
    },
    formatter: (tooltipParams: any[]) => {
      const dataPoint = tooltipParams[0];
      const dataColor = dataPoint.color;
      const dataValue = dataPoint.value;
      return `<div class="flex items-center text-3 text-text-centent"><div class="h-3 w-3 rounded-full mr-3.5" style="background-color:${dataColor}"></div><span class="mr-3.5">${t('xcan_execSettingForm.executionConcurrency')}</span>${dataValue}</div>`;
    }
  },
  xAxis: {
    name: t('xcan_execSettingForm.executionTimeUnit', { unit: props.unit }),
    nameLocation: 'middle',
    nameGap: 40,
    type: 'category',
    boundaryGap: false,
    axisLabel: {
      showMaxLabel: true // Show maximum value label
    },
    data: props.xData,
    axisTick: {
      show: false // Hide X-axis tick marks
    }
  },
  yAxis: [
    {
      show: true,
      name: t('xcan_execSettingForm.concurrencyAxis'),
      nameLocation: 'middle',
      nameGap: 50,
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed' // Set as dashed line
        }
      },
      type: 'value'
    }
  ],
  series: [
    {
      name: t('xcan_execSettingForm.concurrencyAxis'),
      type: 'line',
      showSymbol: false,
      symbol: '',
      step: 'end',
      data: props.yData,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: 'rgba(0, 119, 255, 0.3)' // Gradient start color
          }, {
            offset: 1,
            color: 'rgba(0, 119, 255, 0.10)' // Gradient end color
          }]
        }
      }
    }
  ]
};

// Component watchers
watch(() => props.xData, () => {
  chartConfiguration.xAxis.data = props.xData;
  chartConfiguration.series[0].data = props.yData;
  chartInstance?.setOption(chartConfiguration, true);
}, { deep: true });

watch(() => props.unit, (newUnitValue: string) => {
  chartConfiguration.xAxis.name = t('xcan_execSettingForm.executionTimeUnit', { unit: newUnitValue });
  chartInstance?.setOption(chartConfiguration, true);
});

// Component lifecycle hooks
onMounted(() => {
  initializeExecutionConcurrencyChart();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize);
});

</script>
<template>
  <div
    ref="chartContainerRef"
    class="h-50 w-full"
    style="max-width:500px">
  </div>
</template>
