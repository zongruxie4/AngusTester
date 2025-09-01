import { ref, onBeforeUnmount, onMounted } from 'vue';
import * as echarts from 'echarts';
import elementResizeDetector from 'element-resize-detector';
import { ChartOption, ProjectCreationStatistics } from '../types';
import { useChartConfig } from './useChartConfig';

/**
 * <p>Composable for managing ECharts instances and chart operations</p>
 * <p>Handles chart initialization, data updates, and cleanup</p>
 */
export function useECharts () {
  // Chart DOM references
  const variableRef = ref<HTMLElement>();
  const dataSetRef = ref<HTMLElement>();
  const fileRef = ref<HTMLElement>();
  const dataSourceRef = ref<HTMLElement>();

  // Chart instances
  let variableChart: echarts.ECharts;
  let dataSetChart: echarts.ECharts;
  let fileChart: echarts.ECharts;
  let dataSourceChart: echarts.ECharts;

  // Element resize detector for responsive charts
  const erd = elementResizeDetector({ strategy: 'scroll' });

  // Chart configuration
  const chartConfig = useChartConfig();

  /**
   * <p>Initialize all ECharts instances</p>
   * <p>Creates chart instances and sets up resize detection</p>
   */
  const initializeCharts = (): void => {
    if (variableRef.value) {
      variableChart = echarts.init(variableRef.value);
      erd.listenTo(variableRef.value, () => {
        variableChart.resize();
      });
    }

    if (dataSetRef.value) {
      dataSetChart = echarts.init(dataSetRef.value);
      erd.listenTo(dataSetRef.value, () => {
        dataSetChart.resize();
      });
    }

    if (fileRef.value) {
      fileChart = echarts.init(fileRef.value);
      erd.listenTo(fileRef.value, () => {
        fileChart.resize();
      });
    }

    if (dataSourceRef.value) {
      dataSourceChart = echarts.init(dataSourceRef.value);
      erd.listenTo(dataSourceRef.value, () => {
        dataSourceChart.resize();
      });
    }
  };

  /**
   * <p>Update chart data with new statistics</p>
   * <p>Updates all charts with fresh data from API</p>
   */
  const updateChartData = (projectStatistics: ProjectCreationStatistics): void => {
    // Update variable usage chart
    if (variableChart) {
      const variableOption = variableChart.getOption() as unknown as ChartOption;
      variableOption.series[0].data[0].value = projectStatistics.variableByUse.IN_USE;
      variableOption.series[0].data[1].value = projectStatistics.variableByUse.NOT_IN_USE;
      variableChart.setOption(variableOption as any);
    }

    // Update dataset usage chart
    if (dataSetChart && projectStatistics.datasetByUse) {
      const dataSetOption = dataSetChart.getOption() as unknown as ChartOption;
      dataSetOption.series[0].data[0].value = projectStatistics.datasetByUse.IN_USE;
      dataSetOption.series[0].data[1].value = projectStatistics.datasetByUse.NOT_IN_USE;
      dataSetChart.setOption(dataSetOption as any);
    }

    // Update file resource type chart
    if (fileChart) {
      const fileOption = fileChart.getOption() as unknown as ChartOption;
      fileOption.series[0].data[0].value = projectStatistics.fileByResourceType.SPACE;
      fileOption.series[0].data[1].value = projectStatistics.fileByResourceType.DIRECTORY;
      fileOption.series[0].data[2].value = projectStatistics.fileByResourceType.FILE;
      fileChart.setOption(fileOption as any);
    }

    // Update datasource database type chart
    if (dataSourceChart) {
      const dataSourceOption = dataSourceChart.getOption() as unknown as ChartOption;
      const dataSourceData = [
        projectStatistics.datasourceByDb.H2,
        projectStatistics.datasourceByDb.HSQLDB,
        projectStatistics.datasourceByDb.SQLITE,
        projectStatistics.datasourceByDb.POSTGRES,
        projectStatistics.datasourceByDb.MARIADB,
        projectStatistics.datasourceByDb.MYSQL,
        projectStatistics.datasourceByDb.ORACLE,
        projectStatistics.datasourceByDb.SQLSERVER
      ];

      dataSourceOption.series[0].data.forEach((item, idx) => {
        item.value = dataSourceData[idx];
      });
      dataSourceChart.setOption(dataSourceOption as any);
    }
  };

  /**
   * <p>Handle window resize for responsive charts</p>
   * <p>Updates chart options when screen size changes</p>
   */
  const handleWindowResize = (): void => {
    // Debounce resize events
    clearTimeout((window as any).resizeTimeout);
    (window as any).resizeTimeout = setTimeout(() => {
      // Recreate chart options with responsive configuration
      const variableOption = chartConfig.createVariableChartOption();
      const dataSetOption = chartConfig.createDatasetChartOption();
      const fileOption = chartConfig.createFileChartOption();
      const dataSourceOption = chartConfig.createDatasourceChartOption();

      // Update charts with new responsive options
      if (variableChart) {
        variableChart.setOption(variableOption as any, true);
        variableChart.resize();
      }
      if (dataSetChart) {
        dataSetChart.setOption(dataSetOption as any, true);
        dataSetChart.resize();
      }
      if (fileChart) {
        fileChart.setOption(fileOption as any, true);
        fileChart.resize();
      }
      if (dataSourceChart) {
        dataSourceChart.setOption(dataSourceOption as any, true);
        dataSourceChart.resize();
      }
    }, 300); // 300ms debounce
  };

  /**
   * <p>Set chart options for all charts</p>
   * <p>Initializes charts with their base configurations</p>
   */
  const setChartOptions = (
    variableOption: ChartOption,
    dataSetOption: ChartOption,
    fileOption: ChartOption,
    dataSourceOption: ChartOption
  ): void => {
    if (variableChart) {
      variableChart.setOption(variableOption as any);
    }
    if (dataSetChart) {
      dataSetChart.setOption(dataSetOption as any);
    }
    if (fileChart) {
      fileChart.setOption(fileOption as any);
    }
    if (dataSourceChart) {
      dataSourceChart.setOption(dataSourceOption as any);
    }
  };

  /**
   * <p>Clean up chart instances and resize listeners</p>
   * <p>Prevents memory leaks by properly disposing charts</p>
   */
  const cleanupCharts = (): void => {
    if (variableChart && variableRef.value) {
      erd.removeAllListeners(variableRef.value);
      variableChart.dispose();
    }
    if (dataSetChart && dataSetRef.value) {
      erd.removeAllListeners(dataSetRef.value);
      dataSetChart.dispose();
    }
    if (fileChart && fileRef.value) {
      erd.removeAllListeners(fileRef.value);
      fileChart.dispose();
    }
    if (dataSourceChart && dataSourceRef.value) {
      erd.removeAllListeners(dataSourceRef.value);
      dataSourceChart.dispose();
    }
  };

  // Cleanup on component unmount
  onBeforeUnmount(() => {
    cleanupCharts();
    window.removeEventListener('resize', handleWindowResize);
    clearTimeout((window as any).resizeTimeout);
  });

  // Add window resize listener on mount
  onMounted(() => {
    window.addEventListener('resize', handleWindowResize);
  });

  return {
    // Chart references
    variableRef,
    dataSetRef,
    fileRef,
    dataSourceRef,

    // Methods
    initializeCharts,
    updateChartData,
    setChartOptions,
    cleanupCharts,
    handleWindowResize
  };
}
