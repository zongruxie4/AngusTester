<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { utils, enumUtils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { useI18n } from 'vue-i18n';

import { TestLayer } from '@/enums/enums';
import { CaseCount } from '@/views/test/case/types';

/**
 * Component props interface for ReviewStatusChart component
 */
type Props = {
  dataSource: CaseCount;
}

/**
 * ECharts configuration option type
 */
type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

// Composables and Props
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

// Dependency Injection
const windowResizeNotify = inject('windowResizeNotify', ref<string>());

// Chart Configuration
const chartContainerRef = ref<HTMLElement>();
const chartDomId = utils.uuid('review-status-pie');

let chartInstance: echarts.ECharts;

/**
 * ECharts configuration options for the review status pie chart
 */
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
    itemGap: 5
  },
  series: [
    {
      name: '',
      type: 'pie',
      center: ['35%', '50%'],
      radius: ['65%', '90%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
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

/**
 * Renders or updates the pie chart with current data
 * <p>
 * Initializes the chart instance if it doesn't exist, then sets the chart options.
 * If the chart already exists, it updates the options to reflect new data.
 */
const renderChart = (): void => {
  if (!chartInstance) {
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
    chartInstance = echarts.init(document.getElementById(chartDomId));
    chartInstance.setOption(chartOptions);
    return;
  }

  // Update chart with new data
  chartInstance.setOption(chartOptions);
};

/**
 * Handles chart resize when window dimensions change
 */
const handleChartResize = (): void => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

/**
 * Updates chart data based on statistics information
 * <p>
 * Transforms the statistics data into chart data format and updates the chart series.
 * Each review status is mapped to a data point with its corresponding count.
 */
const updateChartData = (): void => {
  if (props.dataSource === undefined) {
    return;
  }
  // UI = 'UI',
  // API = 'API',
  // UNIT = 'UNIT',
  // INTEGRATION = 'INTEGRATION',
  // E2E = 'E2E'

  // Reset chart data
  chartOptions.series![0].data = [{
    name: enumUtils.getEnumDescription(TestLayer, TestLayer.UI),
    value: 0
  }, {
    name: enumUtils.getEnumDescription(TestLayer,TestLayer.API),
    value: 0
  }, {
    name: enumUtils.getEnumDescription(TestLayer,TestLayer.UNIT),
    value: 0
  }, {
    name: enumUtils.getEnumDescription(TestLayer,TestLayer.INTEGRATION),
    value: 0
  }, {
    name: enumUtils.getEnumDescription(TestLayer,TestLayer.E2E),
    value: 0
  }];

  renderChart();
};

// Lifecycle Hooks
onMounted(() => {
  // Watch for changes in data source
  watch(() => props.dataSource, () => {
    updateChartData();
  }, { immediate: true });

  // Watch for window resize notifications
  watch(() => windowResizeNotify.value, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }
    handleChartResize();
  }, { immediate: true });
});
</script>

<template>
  <!-- Review status chart container -->
  <div class="chart-wrapper">
    <!-- ECharts container -->
    <div
      :id="chartDomId"
      ref="chartContainerRef"
      class="chart-container"></div>
    <!-- Chart center label -->
    <div class="chart-center-label">
      <div class="label-title">{{ t('common.testLayer') }}</div>
      <div class="label-value">{{ props.dataSource?.totalCaseNum }}</div>
    </div>
  </div>
</template>

<style scoped>
.chart-wrapper {
  position: relative;
  line-height: 1.25;
  font-size: 0.75rem;
}

.chart-container {
  height: 8.5rem; /* h-34 equivalent */
}

.chart-center-label {
  position: absolute;
  top: 50%;
  left: 35%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.chart-center-label .label-title {
  text-align: center;
}

.chart-center-label .label-value {
  font-size: 0.875rem; /* text-3.5 equivalent */
  text-align: center;
  font-weight: 600; /* font-semibold equivalent */
}
</style>
