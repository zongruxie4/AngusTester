<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

import { SummaryInfo } from '@/views/task/home/types';

const { t } = useI18n();

// Props definition
type Props = {
  dataSource: SummaryInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

// ECharts option type
type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

// Global window resize notification
const windowResizeNotify = inject('windowResizeNotify', ref<string>());

// DOM refs
const containerRef = ref<HTMLElement>();
const domId = utils.uuid('pie');

// ECharts runtime
let chartInstance: echarts.ECharts;
const chartOptions: EChartsOption = {
  tooltip: {
    trigger: 'item',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    top: 'middle',
    right: 0,
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 10
  },
  color: ['#67D7FF', '#FFB925', '#F5222D', '#2acab8', '#2D8EFF', '#52C41A'],
  series: [
    {
      name: '',
      type: 'pie',
      center: ['50%', '50%'],
      radius: ['60%', '80%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{c}'
      },
      labelLine: {
        show: true
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 12,
          fontWeight: 'normal'
        }
      },
      data: []
    }
  ]
};

// Initialize or update chart
const renderChart = () => {
  if (!chartInstance) {
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
    chartInstance = echarts.init(document.getElementById(domId));
    chartInstance.setOption(chartOptions);
    return;
  }

  // Redraw with latest options
  chartInstance.setOption(chartOptions);
};

// Compute responsive layout
const updateChartOptions = () => {
  if (containerRef.value) {
    const width = containerRef.value.offsetWidth;
    if (width > 428) {
      chartOptions.series![0].center = ['50%', '50%'];
      chartOptions.series![0].radius = ['45%', '63%'];
      chartOptions.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else if (width > 350 && width <= 428) {
      chartOptions.series![0].center = ['45%', '50%'];
      chartOptions.series![0].radius = ['45%', '63%'];
      chartOptions.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else if (width > 300 && width <= 350) {
      chartOptions.series![0].center = ['40%', '50%'];
      chartOptions.series![0].radius = ['45%', '63%'];
      chartOptions.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else {
      chartOptions.series![0].center = ['50%', '40%'];
      chartOptions.series![0].radius = ['38%', '55%'];
      chartOptions.legend = {
        bottom: '-7px',
        left: 'center',
        orient: 'horizontal',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 3,
        formatter: function (name) {
          return name;
        }
      };
    }
  }

  return chartOptions;
};

// Handle resize and re-render
const handleResize = () => {
  updateChartOptions();
  if (chartInstance) {
    chartInstance.setOption(chartOptions);
    chartInstance.resize();
  }
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (props.dataSource === undefined) {
      return;
    }

    // Reset pie data and map task types
    chartOptions.series![0].data = [];

    const data = newValue.taskByType;
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.story'), value: +data.STORY });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.task'), value: +data.TASK });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.bug'), value: +data.BUG });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.requirement'), value: +data.REQUIREMENT });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.apiTest'), value: +data.API_TEST });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.scenarioTest'), value: +data.SCENARIO_TEST });

    updateChartOptions();
    renderChart();
  }, { immediate: true });

  watch(() => windowResizeNotify.value, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    handleResize();
  }, { immediate: true });
});
</script>

<template>
  <div
    :id="domId"
    ref="containerRef"
    class="h-44"></div>
</template>
