import { inject, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';

import { ResourceCount } from '../types';
import { useChartConfig } from './useChartConfig';
import { useECharts } from './useECharts';
import { PieChartOption } from '@/views/script/types';

/**
 * Composable for managing pie chart functionality
 * Handles chart initialization, data updates, and resize events
 */
export function usePieChart (_dataSource: ResourceCount) {
  const { createPieChartOption, transformResourceToChartData } = useChartConfig();
  const { initializeECharts, createChartInstance, setChartOptions, resizeChart } = useECharts();

  const dataSource = ref(_dataSource);
  // Window resize notification from parent component
  const windowResizeNotify = inject('windowResizeNotify', ref<string>());

  // Chart container reference
  const containerRef = ref<HTMLElement>();

  // Unique DOM ID for chart container
  const domId = utils.uuid('pie');

  // ECharts instance
  let echartInstance: echarts.ECharts | null = null;

  // Chart configuration options - will be updated with data
  let chartOption: PieChartOption | undefined;

  /**
   * Initialize ECharts library and create chart instance
   */
  const initializeChart = () => {
    initializeECharts();
    const domElement = document.getElementById(domId);
    if (domElement) {
      echartInstance = createChartInstance(domElement);
      if (echartInstance) {
        // Initialize chart with empty data first
        if (!chartOption) {
          chartOption = createPieChartOption();
        }
        setChartOptions(echartInstance, chartOption);
      }
    }
  };

  /**
   * Update chart data based on resource information
   * @param resourceInfo - Resource information containing script counts
   */
  const updateChartData = (resourceInfo: ResourceCount) => {
    if (!resourceInfo) return;

    // Transform resource data to chart data
    const chartData = transformResourceToChartData(resourceInfo);

    // Create new chart option with data for legend formatter
    chartOption = createPieChartOption(chartData);
    chartOption.series![0].data = chartData;
  };

  /**
   * Render or update the chart with current data
   */
  const renderChart = () => {
    if (!echartInstance) {
      initializeChart();
      return;
    }

    // Update chart with new data
    if (chartOption) {
      setChartOptions(echartInstance, chartOption);
    }
  };

  /**
   * Handle window resize events
   */
  const handleResize = () => {
    resizeChart(echartInstance);
  };

  /**
   * Watch for data source changes and update chart
   */
  const watchDataSource = () => {
    watch(() => dataSource.value, (newValue) => {
      if (!newValue) return;

      updateChartData(newValue);
      renderChart();
    }, { immediate: true, deep: true });
  };

  /**
   * Watch for window resize notifications
   */
  const watchResizeEvents = () => {
    watch(() => windowResizeNotify.value, (newValue) => {
      if (!newValue) return;

      handleResize();
    }, { immediate: true });
  };

  /**
   * Initialize chart functionality
   */
  const initialize = () => {
    watchDataSource();
    watchResizeEvents();
  };

  return {
    containerRef,
    domId,
    chartOption,
    initialize,
    renderChart,
    handleResize,
    dataSource
  };
}
