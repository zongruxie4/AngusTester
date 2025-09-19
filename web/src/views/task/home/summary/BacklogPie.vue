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
import { DataSourceProps } from '@/types/types';

const { t } = useI18n();

// Props definition
const props = withDefaults(defineProps<DataSourceProps<SummaryInfo>>(), {
  dataSource: undefined
});

// ECharts option type
type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

// Global resize notification from parent layout
const windowResizeNotify = inject('windowResizeNotify', ref<string>());

// DOM references
const containerRef = ref<HTMLElement>();

// Summary total number displayed in title
const total = ref(0);

// Unique DOM id for ECharts instance
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
    itemGap: 10,
    formatter: function (name) {
      return name;
    }
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

// Initialize or update chart instance with latest options
const renderChart = () => {
  if (!chartInstance) {
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
    chartInstance = echarts.init(document.getElementById(domId));
    chartInstance.setOption(chartOptions);
    return;
  }

  // Redraw with updated options
  chartInstance.setOption(chartOptions);
};

// Adjust chart layout responsively according to container width
const updateChartOptions = () => {
  if (containerRef.value) {
    const width = containerRef.value.offsetWidth;
    if (width > 418) {
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
    } else if (width > 340 && width <= 418) {
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
    } else if (width > 290 && width <= 340) {
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
      chartOptions.series![0].center = ['50%', '34%'];
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

// Handle container resize and re-render chart
const handleResize = () => {
  updateChartOptions();
  if (!chartInstance) {
    return;
  }
  chartInstance.setOption(chartOptions);
  chartInstance.resize();
};

onMounted(() => {
  // Watch data changes and feed series data
  watch(() => props.dataSource, (newValue) => {
    if (props.dataSource === undefined) {
      return;
    }
    // Read total backlog; fallback to 0 when missing
    total.value = +((newValue as any).allBacklog ?? 0);

    // Reset and map series data
    chartOptions.series![0].data = [];

    const data = (newValue as any).backlogByType || {};
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.story'), value: +data.STORY });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.task'), value: +data.TASK });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.bug'), value: +data.BUG });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.requirement'), value: +data.REQUIREMENT });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.apiTest'), value: +data.API_TEST });
    chartOptions.series?.[0].data.push({ name: t('taskHome.summary.taskTypes.scenarioTest'), value: +data.SCENARIO_TEST });

    updateChartOptions();
    renderChart();
  });

  // React to global window resize notifications
  watch(() => windowResizeNotify.value, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    handleResize();
  }, { immediate: true });
});
</script>

<template>
  <div class="flex flex-col rounded border border-solid border-theme-text-box pt-2 pb-2">
    <div class="font-semibold px-4">{{ t('taskHome.summary.backlogPie.totalBacklog') }} <span class="text-4">{{ total }}</span></div>
    <div class="flex-1 flex items-center justify-center pr-2">
      <div
        :id="domId"
        ref="containerRef"
        class="w-full h-44"></div>
    </div>
  </div>
</template>
