import * as echarts from 'echarts/core';
import { LegendComponent, TooltipComponent } from 'echarts/components';
import { PieChart } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

/**
 * Composable for managing ECharts library initialization and common operations
 * Provides centralized ECharts setup and utility functions
 */
export function useECharts () {
  /**
   * Initialize ECharts library with required components
   * Registers all necessary components for pie charts and other chart types
   */
  const initializeECharts = () => {
    echarts.use([
      TooltipComponent,
      LegendComponent,
      PieChart,
      CanvasRenderer,
      LabelLayout
    ]);
  };

  /**
   * Create ECharts instance with error handling
   * @param domElement - DOM element or element ID for chart container
   * @returns ECharts instance or null if initialization fails
   */
  const createChartInstance = (domElement: string | HTMLElement) => {
    try {
      return echarts.init(domElement);
    } catch (error) {
      console.error('Failed to create ECharts instance:', error);
      return null;
    }
  };

  /**
   * Safely set chart options with error handling
   * @param chartInstance - ECharts instance
   * @param options - Chart configuration options
   */
  const setChartOptions = (chartInstance: echarts.ECharts | null, options: any) => {
    if (!chartInstance) {
      console.warn('Chart instance is not available');
      return;
    }

    try {
      chartInstance.setOption(options);
    } catch (error) {
      console.error('Failed to set chart options:', error);
    }
  };

  /**
   * Safely resize chart with error handling
   * @param chartInstance - ECharts instance
   */
  const resizeChart = (chartInstance: echarts.ECharts | null) => {
    if (!chartInstance) {
      console.warn('Chart instance is not available for resize');
      return;
    }

    try {
      chartInstance.resize();
    } catch (error) {
      console.error('Failed to resize chart:', error);
    }
  };

  /**
   * Dispose chart instance and clean up resources
   * @param chartInstance - ECharts instance to dispose
   */
  const disposeChart = (chartInstance: echarts.ECharts | null) => {
    if (!chartInstance) return;

    try {
      chartInstance.dispose();
    } catch (error) {
      console.error('Failed to dispose chart:', error);
    }
  };

  return {
    initializeECharts,
    createChartInstance,
    setChartOptions,
    resizeChart,
    disposeChart
  };
}
