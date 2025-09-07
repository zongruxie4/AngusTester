<script lang="ts" setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import type { ChartProps } from './types';

const { t } = useI18n();

// Component props with proper typing
const props = withDefaults(defineProps<ChartProps>(), {
  count: () => ({
    failureNum: 0,
    successNum: 0,
    successRate: '0'
  })
});

// Chart reference and instance
const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;

/**
 * Initialize chart configuration
 */
const createChartConfig = () => ({
  title: {
    text: '0%',
    left: '35%',
    top: '38%',
    padding: 2,
    itemGap: 47,
    textAlign: 'center' as const,
    textStyle: {
      fontSize: 12,
      fontWeight: 'bolder' as const
    },
    subtextStyle: {
      fontSize: 12,
      color: '#000'
    }
  },
  tooltip: {
    trigger: 'item' as const
  },
  legend: {
    top: 'middle',
    right: '10',
    orient: 'vertical' as const,
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '',
      type: 'pie' as const,
      radius: ['50%', '70%'],
      center: ['35%', '45%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
        borderColor: '#fff',
        borderWidth: 1
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: [
        {
          name: t('scenarioMonitor.chart.successCount'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        },
        {
          name: t('scenarioMonitor.chart.failureCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
});

/**
 * Update chart data based on props
 */
const updateChartData = () => {
  if (!chartInstance || !props.count) {
    return;
  }

  const { failureNum = 0, successNum = 0, successRate = '0' } = props.count;

  // Convert string values to numbers for chart display
  const successValue = typeof successNum === 'string' ? parseInt(successNum) || 0 : successNum;
  const failureValue = typeof failureNum === 'string' ? parseInt(failureNum) || 0 : failureNum;

  const option = {
    title: {
      text: `${successRate}%`
    },
    series: [{
      data: [
        {
          name: t('scenarioMonitor.chart.successCount'),
          value: successValue,
          itemStyle: { color: '#52C41A' }
        },
        {
          name: t('scenarioMonitor.chart.failureCount'),
          value: failureValue,
          itemStyle: { color: 'rgba(245, 34, 45, 1)' }
        }
      ]
    }]
  };

  chartInstance.setOption(option);
};

/**
 * Initialize chart on component mount
 */
onMounted(() => {
  if (!chartRef.value) {
    return;
  }

  chartInstance = echarts.init(chartRef.value);
  const config = createChartConfig();
  chartInstance.setOption(config);

  // Watch for count changes and update chart
  watch(() => props.count, updateChartData, { deep: true });
});

/**
 * Cleanup chart instance on component unmount
 */
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>
<template>
  <div ref="chartRef" class="h-25">
  </div>
</template>
