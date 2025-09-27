<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { GridComponent, GridComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { BarChart, BarSeriesOption } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';

import { ResourceInfo } from './types';

/**
 * <p>Component props interface for BarChart component.</p>
 * <p>Defines the data source and resize notification properties.</p>
 */
type Props = {
  dataSource: ResourceInfo;
  resizeNotify: string;
}

/**
 * <p>ECharts configuration option type.</p>
 * <p>Combines tooltip, grid, and bar series options for type safety.</p>
 */
type EChartsOption = echarts.ComposeOption<TooltipComponentOption | GridComponentOption | BarSeriesOption>;

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({
    allSce: '0',
    sceByLast7Days: '0',
    sceByLastMonth: '0',
    sceByScriptType: {
      TEST_FUNCTIONALITY: '0',
      TEST_PERFORMANCE: '0',
      TEST_STABILITY: '0',
      TEST_CUSTOMIZATION: '0',
      MOCK_DATA: '0',
      MOCK_APIS: '0'
    },
    sceByPluginName: {}
  }),
  resizeNotify: undefined
});

/**
 * <p>Unique DOM element identifier for the chart container.</p>
 * <p>Generated using UUID to ensure uniqueness across component instances.</p>
 */
const chartDomId = utils.uuid('bar');

/**
 * <p>Total number of scenarios displayed in the chart header.</p>
 * <p>Reactive value that updates when data source changes.</p>
 */
const totalScenariosCount = ref<number>(0);

/**
 * <p>ECharts instance for managing chart lifecycle and operations.</p>
 * <p>Initialized lazily when first render is called.</p>
 */
let chartInstance: echarts.ECharts;

/**
 * <p>Default chart configuration with styling and behavior settings.</p>
 * <p>Includes tooltip, grid layout, axes, and series configuration.</p>
 */
const chartConfiguration: EChartsOption = {
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '0',
    right: '0',
    top: '35px',
    bottom: '0',
    containLabel: true
  },
  xAxis: [{
    type: 'category',
    data: [],
    axisTick: { alignWithLabel: true }
  }],
  yAxis: [{ type: 'value' }],
  series: [{
    name: t('scenarioHome.chart.quantity'),
    type: 'bar',
    barWidth: '20px',
    data: [],
    itemStyle: {
      color: '#2D8EFF',
      borderRadius: [4, 4, 0, 0]
    },
    label: {
      show: true, // Display value labels on top of bars
      position: 'top' // Position labels at the top of each bar
    }
  }]
};

/**
 * <p>Initializes and renders the ECharts bar chart.</p>
 * <p>Registers required ECharts components and creates chart instance if not exists.</p>
 * <p>Updates chart with current configuration data.</p>
 */
const renderBarChart = (): void => {
  if (!chartInstance) {
    // Register required ECharts components
    echarts.use([TooltipComponent, GridComponent, BarChart, CanvasRenderer]);

    // Initialize chart instance with DOM element
    chartInstance = echarts.init(document.getElementById(chartDomId));
    chartInstance.setOption(chartConfiguration);
    return;
  }

  // Update existing chart with new data
  chartInstance.setOption(chartConfiguration);
};

/**
 * <p>Handles chart resize when container dimensions change.</p>
 * <p>Ensures chart maintains proper aspect ratio and visibility.</p>
 */
const handleChartResize = (): void => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

/**
 * <p>Processes scenario data by plugin name and updates chart configuration.</p>
 * <p>Extracts plugin names and their corresponding scenario counts.</p>
 * <p>Provides fallback data when no plugin data is available.</p>
 *
 * @param dataSource - Resource information containing scenario counts by plugin
 */
const processPluginData = (dataSource: ResourceInfo): void => {
  // Reset chart data
  chartConfiguration.xAxis![0].data = [];
  chartConfiguration.series![0].data = [];

  if (!dataSource) {
    return;
  }

  // Update total scenarios count
  totalScenariosCount.value = +dataSource.allSce;

  const pluginData = dataSource.sceByPluginName;
  if (pluginData) {
    const pluginNames = Object.keys(pluginData);

    if (pluginNames.length > 0) {
      // Process available plugin data
      pluginNames.forEach(pluginName => {
        chartConfiguration.xAxis![0].data.push(pluginName);
        chartConfiguration.series![0].data.push(pluginData[pluginName]);
      });
    } else {
      // Provide fallback data when no plugins are available
      chartConfiguration.xAxis![0].data = ['Http', 'WebSocket', 'Jdbc', 'Tcp'];
      chartConfiguration.series![0].data = [0, 0, 0, 0];
    }
  }
};

onMounted(() => {
  // Watch for data source changes and update chart accordingly
  watch(() => props.dataSource, (newDataSource) => {
    processPluginData(newDataSource);
    renderBarChart();
  }, { immediate: true });

  // Watch for resize notifications and handle chart resizing
  watch(() => props.resizeNotify, (resizeNotification) => {
    if (resizeNotification === undefined || resizeNotification === null || resizeNotification === '') {
      return;
    }

    handleChartResize();
  }, { immediate: true });
});
</script>

<template>
  <div class="rounded border border-solid border-theme-text-box px-4 py-3.5">
    <div class="font-semibold">
      <span class="mr-2">{{ t('scenarioHome.chart.totalScenarios') }}</span>
      <span class="text-4">{{ totalScenariosCount }}</span>
    </div>
    <div :id="chartDomId" class="w-full h-50"></div>
  </div>
</template>
