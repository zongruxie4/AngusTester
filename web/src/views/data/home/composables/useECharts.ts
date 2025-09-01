import { ref, onBeforeUnmount, onMounted } from 'vue';
import * as echarts from 'echarts';
import elementResizeDetector from 'element-resize-detector';
import { ChartOption, ProjectStatistics } from '../types';

/**
 * <p>Composable for managing ECharts instances and chart operations</p>
 * <p>Handles chart initialization, data updates, and cleanup</p>
 */
export function useECharts() {
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
  const updateChartData = (projectStatistics: ProjectStatistics): void => {
    // Update variable usage chart
    if (variableChart) {
      const variableOption = variableChart.getOption() as ChartOption;
      variableOption.series[0].data[0].value = projectStatistics.variableByUse.IN_USE;
      variableOption.series[0].data[1].value = projectStatistics.variableByUse.NOT_IN_USE;
      variableChart.setOption(variableOption);
    }

    // Update dataset usage chart
    if (dataSetChart) {
      const dataSetOption = dataSetChart.getOption() as ChartOption;
      dataSetOption.series[0].data[0].value = projectStatistics.datasetByUse.IN_USE;
      dataSetOption.series[0].data[1].value = projectStatistics.datasetByUse.NOT_IN_USE;
      dataSetChart.setOption(dataSetOption);
    }

    // Update file resource type chart
    if (fileChart) {
      const fileOption = fileChart.getOption() as ChartOption;
      fileOption.series[0].data[0].value = projectStatistics.fileByResourceType.SPACE;
      fileOption.series[0].data[1].value = projectStatistics.fileByResourceType.DIRECTORY;
      fileOption.series[0].data[2].value = projectStatistics.fileByResourceType.FILE;
      fileChart.setOption(fileOption);
    }

    // Update datasource database type chart
    if (dataSourceChart) {
      const dataSourceOption = dataSourceChart.getOption() as ChartOption;
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
      dataSourceChart.setOption(dataSourceOption);
    }
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
      variableChart.setOption(variableOption);
    }
    if (dataSetChart) {
      dataSetChart.setOption(dataSetOption);
    }
    if (fileChart) {
      fileChart.setOption(fileOption);
    }
    if (dataSourceChart) {
      dataSourceChart.setOption(dataSourceOption);
    }
  };

  /**
   * <p>Clean up chart instances and resize listeners</p>
   * <p>Prevents memory leaks by properly disposing charts</p>
   */
  const cleanupCharts = (): void => {
    if (variableChart) {
      erd.removeAllListeners(variableRef.value);
      variableChart.dispose();
    }
    if (dataSetChart) {
      erd.removeAllListeners(dataSetRef.value);
      dataSetChart.dispose();
    }
    if (fileChart) {
      erd.removeAllListeners(fileRef.value);
      fileChart.dispose();
    }
    if (dataSourceChart) {
      erd.removeAllListeners(dataSourceRef.value);
      dataSourceChart.dispose();
    }
  };

  // Cleanup on component unmount
  onBeforeUnmount(() => {
    cleanupCharts();
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
    cleanupCharts
  };
}
