import * as echarts from 'echarts';
import { reactive, Ref, ref } from 'vue';
import { formatBytesToUnit } from '@/utils/common';

/**
 * Chart data item interface
 * <p>
 * Represents a single data point in the chart with timestamp and values.
 * </p>
 */
export interface ChartDataItem {
  timestamp: string;
  cvsCpu?: string;
  cvsMemory?: string;
  cvsValue?: string;
}

/**
 * Table data item interface
 * <p>
 * Represents a row in the statistics table below the chart.
 * </p>
 */
export interface TableDataItem {
  name: string;
  unit: string;
  average: string;
  high: string;
  low: string;
  latest: string;
}

/**
 * Series data interface for ECharts
 * <p>
 * Represents a single line series in the chart.
 * </p>
 */
export interface SeriesData {
  data: number[];
  type: string;
  name: string;
  smooth: boolean;
  showSymbol: boolean;
  lineStyle: {
    width: number;
  };
}

/**
 * Chart option interface
 * <p>
 * Represents the complete ECharts configuration.
 * </p>
 */
export interface ChartOption {
  title: { text: string };
  tooltip: { trigger: string; textStyle: { fontSize: number } };
  grid: { top: string; left: string; right: string; bottom: string; containLabel: boolean };
  xAxis: Array<{ data: string[] }>;
  yAxis: Array<{ type: string; splitLine: { show: boolean; lineStyle: { type: string } } }>;
  series: SeriesData[];
  legend?: { type: string; data: string[]; y: string; x: string };
}

/**
 * Resource usage data interface
 * <p>
 * Represents current resource usage metrics for the node.
 * </p>
 */
export interface ResourceUsage {
  cpu: number;
  cpuPercent: number;
  cpuTotal: number;
  memory: string;
  memoryPercent: number;
  memoryTotal: string;
  swap: string;
  swapPercent: number;
  swapTotal: string;
  disk: string;
  diskPercent: number;
  diskTotal: string;
  txBytesRate: number;
  rxBytesRate: number;
  txBytes: string;
  rxBytes: string;
}

/**
 * Default ECharts configuration options
 * <p>
 * Base configuration shared across all chart types.
 * </p>
 */
export const DEFAULT_ECHARTS_OPTIONS: ChartOption = {
  title: { text: ' ' },
  tooltip: {
    trigger: 'axis',
    textStyle: { fontSize: 12 }
  },
  grid: {
    top: '3%',
    left: '3%',
    right: '3%',
    bottom: '12%',
    containLabel: true
  },
  xAxis: [{
    type: 'category',
    boundaryGap: false,
    data: [],
    splitLine: {
      show: true,
      lineStyle: { type: 'dashed' }
    }
  }],
  yAxis: [{
    type: 'value',
    splitLine: {
      show: true,
      lineStyle: { type: 'dashed' }
    }
  }],
  series: []
};

/**
 * Default legend configuration
 * <p>
 * Standard legend positioning and styling.
 * </p>
 */
export const DEFAULT_LEGEND = {
  type: 'plain' as const,
  data: [] as string[],
  y: 'bottom' as const,
  x: 'center' as const
};

/**
 * Default line series configuration
 * <p>
 * Common line chart styling and behavior.
 * </p>
 */
export const getDefaultLineConfig = (): Omit<SeriesData, 'name'> => ({
  data: [],
  type: 'line',
  smooth: true,
  showSymbol: false,
  lineStyle: { width: 1 }
});

/**
 * Create a complete series data object with name
 * <p>
 * Helper function to create a complete series with the provided name.
 * </p>
 */
export const createSeriesData = (name: string): SeriesData => ({
  ...getDefaultLineConfig(),
  name
});

/**
 * Utility class for processing chart data
 * <p>
 * Contains helper methods for data transformation and calculation.
 * </p>
 */
class ChartDataProcessor {
  /**
   * Process CPU data and calculate statistics
   * <p>
   * Converts raw CPU data to series format and calculates table statistics.
   * </p>
   */
  static processCpuData (data: ChartDataItem[], t: (key: string) => string): {
    seriesData: SeriesData[];
    tableData: TableDataItem[];
  } {
    const dataTypes = [
      t('node.nodeDetail.chartOptions.cpu.idle'),
      t('node.nodeDetail.chartOptions.cpu.sys'),
      t('node.nodeDetail.chartOptions.cpu.user'),
      t('node.nodeDetail.chartOptions.cpu.wait'),
      t('node.nodeDetail.chartOptions.cpu.other'),
      t('node.nodeDetail.chartOptions.cpu.total')
    ];

    const seriesData = dataTypes.map(name => createSeriesData(name));

    // Process each data point
    data.forEach(item => {
      if (item.cvsCpu) {
        const cpuValues = item.cvsCpu.split(',');
        cpuValues.forEach((val, idx) => {
          if (seriesData[idx]) {
            seriesData[idx].data.push(+(+val).toFixed(2));
          }
        });
      }
    });

    // Calculate table statistics
    const tableData = seriesData.map((series, idx) => {
      const values = series.data;
      if (values.length === 0) {
        return {
          name: dataTypes[idx],
          unit: '%',
          average: '0%',
          high: '0%',
          low: '0%',
          latest: '0%'
        };
      }

      const total = values.reduce((sum, val) => sum + val, 0);
      const average = (total / values.length).toFixed(2);
      const high = Math.max(...values).toFixed(2);
      const low = Math.min(...values).toFixed(2);
      const latest = values[values.length - 1].toFixed(2);

      return {
        name: dataTypes[idx],
        unit: '%',
        average: `${average}%`,
        high: `${high}%`,
        low: `${low}%`,
        latest: `${latest}%`
      };
    });

    return { seriesData, tableData };
  }

  /**
   * Process memory data and calculate statistics
   * <p>
   * Handles both absolute values and percentages for memory metrics.
   * </p>
   */
  static processMemoryData (
    data: ChartDataItem[],
    t: (key: string) => string,
    showMemoryPercentChart: boolean
  ): {
    seriesData: SeriesData[];
    tableData: TableDataItem[];
    percentSeriesData: SeriesData[];
    percentTableData: TableDataItem[];
  } {
    const dataTypes = [
      t('node.nodeDetail.chartOptions.memory.free'),
      t('node.nodeDetail.chartOptions.memory.used'),
      t('node.nodeDetail.chartOptions.memory.freePercent'),
      t('node.nodeDetail.chartOptions.memory.usedPercent'),
      t('node.nodeDetail.chartOptions.memory.actualFree'),
      t('node.nodeDetail.chartOptions.memory.actualUsed'),
      t('node.nodeDetail.chartOptions.memory.actualFreePercent'),
      t('node.nodeDetail.chartOptions.memory.actualUsedPercent'),
      t('node.nodeDetail.chartOptions.memory.swapFree'),
      t('node.nodeDetail.chartOptions.memory.swapUsed')
    ];

    const dataTypeKeys = [
      'free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed',
      'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'
    ];

    const seriesData = dataTypes.map(name => createSeriesData(name));

    // Process each data point
    data.forEach(item => {
      if (item.cvsMemory) {
        const values = item.cvsMemory.split(',');
        values.forEach((val, idx) => {
          if (seriesData[idx]) {
            if (dataTypeKeys[idx].includes('Percent')) {
              seriesData[idx].data.push(+val);
            } else {
              const formattedValue = formatBytesToUnit(parseFloat(val), 'GB', 2);
              const numericValue = typeof formattedValue === 'string' ? parseFloat(formattedValue) : formattedValue;
              seriesData[idx].data.push(numericValue);
            }
          }
        });
      }
    });

    // Calculate table statistics
    const tableData = seriesData.map((series, idx) => {
      const values = series.data;
      if (values.length === 0) {
        return {
          name: dataTypes[idx],
          unit: dataTypeKeys[idx].includes('Percent') ? '%' : 'GB',
          average: dataTypeKeys[idx].includes('Percent') ? '0%' : '0 GB',
          high: dataTypeKeys[idx].includes('Percent') ? '0%' : '0 GB',
          low: dataTypeKeys[idx].includes('Percent') ? '0%' : '0 GB',
          latest: dataTypeKeys[idx].includes('Percent') ? '0%' : '0 GB'
        };
      }

      const total = values.reduce((sum, val) => sum + val, 0);
      const average = (total / values.length).toFixed(2);
      const high = Math.max(...values).toFixed(2);
      const low = Math.min(...values).toFixed(2);
      const latest = values[values.length - 1].toFixed(2);

      if (dataTypeKeys[idx].includes('Percent')) {
        return {
          name: dataTypes[idx],
          unit: '%',
          average: `${average}%`,
          high: `${high}%`,
          low: `${low}%`,
          latest: `${latest}%`
        };
      } else {
        return {
          name: dataTypes[idx],
          unit: 'GB',
          average: `${average} GB`,
          high: `${high} GB`,
          low: `${low} GB`,
          latest: `${latest} GB`
        };
      }
    });

    // Separate percent and absolute data
    const percentSeriesData = seriesData.slice(2, 4).concat(seriesData.slice(6, 8));
    const percentTableData = tableData.slice(2, 4).concat(tableData.slice(6, 8));

    // Filter table data based on chart type
    const filteredTableData = showMemoryPercentChart
      ? tableData.filter(item => item.unit === '%')
      : tableData.filter(item => item.unit !== '%');

    return {
      seriesData: seriesData.slice(0, 2).concat(seriesData.slice(4, 6)).concat(seriesData.slice(8)),
      tableData: filteredTableData,
      percentSeriesData,
      percentTableData
    };
  }

  /**
   * Process disk data and calculate statistics
   * <p>
   * Handles different disk metrics including capacity, IO rates, and percentages.
   * </p>
   */
  static processDiskData (
    data: ChartDataItem[],
    t: (key: string) => string,
    diskChartKey: string
  ): {
    seriesData: SeriesData[];
    tableData: TableDataItem[];
    chartOptions: Record<string, ChartOption>;
  } {
    const dataTypes = [
      t('node.nodeDetail.chartOptions.disk.total'),
      t('node.nodeDetail.chartOptions.disk.free'),
      t('node.nodeDetail.chartOptions.disk.used'),
      t('node.nodeDetail.chartOptions.disk.avail'),
      t('node.nodeDetail.chartOptions.disk.usePercent'),
      t('node.nodeDetail.chartOptions.disk.readsRate'),
      t('node.nodeDetail.chartOptions.disk.writesRate'),
      t('node.nodeDetail.chartOptions.disk.readBytesRate'),
      t('node.nodeDetail.chartOptions.disk.writeBytesRate')
    ];

    const dataTypeKeys = [
      'total', 'free', 'used', 'avail', 'usePercent', 'readsRate', 'writesRate',
      'readBytesRate', 'writeBytesRate'
    ];

    const seriesData = dataTypes.map(name => createSeriesData(name));

    // Process each data point
    data.forEach(item => {
      if (item.cvsValue) {
        const diskValues = item.cvsValue.split(',');
        diskValues.forEach((val, idx) => {
          if (seriesData[idx]) {
            if (dataTypeKeys[idx].includes('Percent') || dataTypeKeys[idx].includes('Rate')) {
              seriesData[idx].data.push(+val);
            } else {
              const formattedValue = formatBytesToUnit(parseFloat(val), 'GB', 2);
              const numericValue = typeof formattedValue === 'string' ? parseFloat(formattedValue) : formattedValue;
              seriesData[idx].data.push(numericValue);
            }
          }
        });
      }
    });

    // Calculate table statistics
    const tableData = seriesData.map((series, idx) => {
      const values = series.data;
      if (values.length === 0) {
        let unit = 'GB';
        if (dataTypeKeys[idx].includes('Percent')) unit = '%';
        else if (dataTypeKeys[idx].includes('BytesRate')) unit = 'MB/s';
        else if (dataTypeKeys[idx].includes('Rate')) unit = 'IO/s';

        return {
          name: dataTypes[idx],
          unit,
          average: `0${unit}`,
          high: `0${unit}`,
          low: `0${unit}`,
          latest: `0${unit}`
        };
      }

      const total = values.reduce((sum, val) => sum + val, 0);
      const average = (total / values.length).toFixed(2);
      const high = Math.max(...values).toFixed(2);
      const low = Math.min(...values).toFixed(2);
      const latest = values[values.length - 1].toFixed(2);

      let unit = 'GB';
      if (dataTypeKeys[idx].includes('Percent')) unit = '%';
      else if (dataTypeKeys[idx].includes('BytesRate')) unit = 'MB/s';
      else if (dataTypeKeys[idx].includes('Rate')) unit = 'IO/s';

      return {
        name: dataTypes[idx],
        unit,
        average: `${average}${unit}`,
        high: `${high}${unit}`,
        low: `${low}${unit}`,
        latest: `${latest}${unit}`
      };
    });

    // Filter table data based on chart type
    let filteredTableData: TableDataItem[] = [];
    switch (diskChartKey) {
      case 'disk':
        filteredTableData = tableData.filter(item => item.unit === 'GB');
        break;
      case 'percent':
        filteredTableData = tableData.filter(item => item.unit === '%');
        break;
      case 'rate':
        filteredTableData = tableData.filter(item => item.unit === 'IO/s');
        break;
      case 'bytesRate':
        filteredTableData = tableData.filter(item => item.unit === 'MB/s');
        break;
      default:
        filteredTableData = [];
    }

    // Create chart options for different views
    const chartOptions: Record<string, ChartOption> = {};

    // Main disk chart (capacity)
    chartOptions.disk = {
      ...DEFAULT_ECHARTS_OPTIONS,
      legend: { ...DEFAULT_LEGEND, data: dataTypes },
      xAxis: [{ data: data.map(item => item.timestamp) }],
      series: seriesData.every(series => series.data.length === 0)
        ? [createSeriesData('Default')]
        : seriesData
    };

    // Percent chart
    const percentSeriesData = seriesData.slice(4, 5);
    const percentDataTypes = dataTypes.slice(4, 5);
    chartOptions.percent = {
      ...DEFAULT_ECHARTS_OPTIONS,
      legend: { ...DEFAULT_LEGEND, data: percentDataTypes },
      xAxis: [{ data: data.map(item => item.timestamp) }],
      series: percentSeriesData.every(series => series.data.length === 0)
        ? [{ ...getDefaultLineConfig(), data: [50] }]
        : percentSeriesData
    };

    // Rate chart
    const rateSeriesData = seriesData.slice(5, 7);
    const rateDataTypes = dataTypes.slice(5, 7);
    chartOptions.rate = {
      ...DEFAULT_ECHARTS_OPTIONS,
      legend: { ...DEFAULT_LEGEND, data: rateDataTypes },
      xAxis: [{ data: data.map(item => item.timestamp) }],
      series: rateSeriesData.every(series => series.data.length === 0)
        ? [{ ...getDefaultLineConfig(), data: [50] }]
        : rateSeriesData
    };

    // Bytes rate chart
    const bytesRateSeriesData = seriesData.slice(7, 9);
    const bytesRateDataTypes = dataTypes.slice(7, 9);
    chartOptions.bytesRate = {
      ...DEFAULT_ECHARTS_OPTIONS,
      legend: { ...DEFAULT_LEGEND, data: bytesRateDataTypes },
      xAxis: [{ data: data.map(item => item.timestamp) }],
      series: bytesRateSeriesData.every(series => series.data.length === 0)
        ? [{ ...getDefaultLineConfig(), data: [50] }]
        : bytesRateSeriesData
    };

    return {
      seriesData,
      tableData: filteredTableData,
      chartOptions
    };
  }

  /**
   * Process network data and calculate statistics
   * <p>
   * Handles network metrics including bytes, rates, and packet counts.
   * </p>
   */
  static processNetworkData (
    data: ChartDataItem[],
    t: (key: string) => string,
    networkChartKey: string
  ): {
    seriesData: SeriesData[];
    tableData: TableDataItem[];
    chartOptions: Record<string, ChartOption>;
  } {
    const dataTypeKeys = ['rxBytes', 'rxBytesRate', 'rxErrors', 'txBytes', 'txBytesRate'];
    const dataTypes = [
      t('node.nodeDetail.chartOptions.network.rxBytes'),
      t('node.nodeDetail.chartOptions.network.rxBytesRate'),
      t('node.nodeDetail.chartOptions.network.rxErrors'),
      t('node.nodeDetail.chartOptions.network.txBytes'),
      t('node.nodeDetail.chartOptions.network.txBytesRate')
    ];

    const seriesData = dataTypes.map(name => createSeriesData(name));

    // Process each data point
    data.forEach(item => {
      if (item.cvsValue) {
        const values = item.cvsValue.split(',');
        values.forEach((val, idx) => {
          if (seriesData[idx]) {
            if (dataTypeKeys[idx].includes('BytesRate')) {
              seriesData[idx].data.push(+val);
            } else if (dataTypeKeys[idx].includes('Bytes')) {
              const formattedValue = formatBytesToUnit(parseFloat(val), 'GB', 2);
              const numericValue = typeof formattedValue === 'string' ? parseFloat(formattedValue) : formattedValue;
              seriesData[idx].data.push(numericValue);
            } else {
              seriesData[idx].data.push(+val);
            }
          }
        });
      }
    });

    // Calculate table statistics
    const tableData = seriesData.map((series, idx) => {
      const values = series.data;
      if (values.length === 0) {
        let unit = 'packets';
        if (dataTypeKeys[idx].includes('BytesRate')) unit = 'MB/s';
        else if (dataTypeKeys[idx].includes('Bytes')) unit = 'GB';

        return {
          name: dataTypes[idx],
          unit,
          average: `0${unit}`,
          high: `0${unit}`,
          low: `0${unit}`,
          latest: `0${unit}`
        };
      }

      const total = values.reduce((sum, val) => sum + val, 0);
      const average = (total / values.length).toFixed(2);
      const high = Math.max(...values).toFixed(2);
      const low = Math.min(...values).toFixed(2);
      const latest = values[values.length - 1].toFixed(2);

      let unit = 'packets';
      if (dataTypeKeys[idx].includes('BytesRate')) unit = 'MB/s';
      else if (dataTypeKeys[idx].includes('Bytes')) unit = 'GB';

      return {
        name: dataTypes[idx],
        unit,
        average: `${average}${unit}`,
        high: `${high}${unit}`,
        low: `${low}${unit}`,
        latest: `${latest}${unit}`
      };
    });

    // Filter table data based on chart type
    let filteredTableData: TableDataItem[] = [];
    switch (networkChartKey) {
      case 'network':
        filteredTableData = tableData.filter(item => item.unit === 'MB/s');
        break;
      case 'packet':
        filteredTableData = tableData.filter(item => item.unit === 'packets');
        break;
      case 'bytes':
        filteredTableData = tableData.filter(item => item.unit === 'GB');
        break;
      default:
        filteredTableData = [];
    }

    // Create chart options for different views
    const chartOptions: Record<string, ChartOption> = {};

    // Network rate chart (MB/s)
    const networkSeriesData = [seriesData[1], seriesData[4]];
    const networkDataTypes = [dataTypes[1], dataTypes[4]];
    chartOptions.network = {
      ...DEFAULT_ECHARTS_OPTIONS,
      legend: { ...DEFAULT_LEGEND, data: networkDataTypes },
      xAxis: [{ data: data.map(item => item.timestamp) }],
      series: networkSeriesData.every(series => series.data.length === 0)
        ? [{ ...getDefaultLineConfig(), data: [50] }]
        : networkSeriesData
    };

    // Packet chart
    const packetSeriesData = [seriesData[2]];
    const packetDataTypes = [dataTypes[2]];
    chartOptions.packet = {
      ...DEFAULT_ECHARTS_OPTIONS,
      legend: { ...DEFAULT_LEGEND, data: packetDataTypes },
      xAxis: [{ data: data.map(item => item.timestamp) }],
      series: packetSeriesData.every(series => series.data.length === 0)
        ? [{ ...getDefaultLineConfig(), data: [50] }]
        : packetSeriesData
    };

    // Bytes chart (GB)
    chartOptions.bytes = {
      ...DEFAULT_ECHARTS_OPTIONS,
      legend: { ...DEFAULT_LEGEND, data: dataTypes },
      xAxis: [{ data: data.map(item => item.timestamp) }],
      series: seriesData.every(series => series.data.length === 0)
        ? [{ ...getDefaultLineConfig(), data: [50] }]
        : seriesData
    };

    return {
      seriesData,
      tableData: filteredTableData,
      chartOptions
    };
  }
}

/**
 * ECharts Manager for handling chart operations
 * <p>
 * Manages ECharts instances, data processing, and chart updates for different
 * resource types (CPU, Memory, Disk, Network).
 * </p>
 */
export class EChartsManager {
  private myEchart: echarts.ECharts | null = null;
  private chartsData: ChartDataItem[] = [];

  // Table data for different resource types
  private memoryTableData: TableDataItem[] = [];
  private diskTableData: TableDataItem[] = [];
  private networkTableData: TableDataItem[] = [];

  // Chart options cache for different views
  private chartOptionsCache: Record<string, ChartOption> = {};

  // Reactive state
  private tableData: Ref<TableDataItem[]> = ref([]);
  private sourceUse: ResourceUsage = reactive({
    cpu: 0,
    cpuPercent: 0,
    cpuTotal: 0,
    memory: '0',
    memoryPercent: 0,
    memoryTotal: '0',
    swap: '0',
    swapPercent: 0,
    swapTotal: '0',
    disk: '0',
    diskPercent: 0,
    diskTotal: '0',
    txBytesRate: 0,
    rxBytesRate: 0,
    txBytes: '0',
    rxBytes: '0'
  });

  /**
   * Initialize ECharts instance
   * <p>
   * Creates and configures the ECharts instance with the provided DOM element.
   * </p>
   */
  initEcharts (echartRef: HTMLElement): void {
    if (echartRef) {
      this.myEchart = echarts.init(echartRef);
      this.myEchart.setOption(DEFAULT_ECHARTS_OPTIONS);
    }
  }

  /**
   * Set chart data
   * <p>
   * Updates the internal chart data storage.
   * </p>
   */
  setChartsData (data: ChartDataItem[]): void {
    this.chartsData = data;
  }

  /**
   * Get table data
   * <p>
   * Returns the current table data for display.
   * </p>
   */
  getTableData (): Ref<TableDataItem[]> {
    return this.tableData;
  }

  /**
   * Get resource usage data
   * <p>
   * Returns the current resource usage metrics.
   * </p>
   */
  getSourceUse (): ResourceUsage {
    return this.sourceUse;
  }

  /**
   * Load CPU chart data
   * <p>
   * Processes CPU data and updates the chart and table.
   * </p>
   */
  loadCpuEchartData (data: ChartDataItem[], t: (key: string) => string, notMerge = true): void {
    try {
      this.chartsData = data;
      const { seriesData, tableData } = ChartDataProcessor.processCpuData(data, t);

      this.tableData.value = tableData;

      const legend = notMerge
        ? {
            legend: {
              ...DEFAULT_LEGEND,
              data: seriesData.map(series => series.name)
            }
          }
        : {};

      const chartOption: ChartOption = {
        ...DEFAULT_ECHARTS_OPTIONS,
        ...legend,
        xAxis: [{ data: data.map(item => item.timestamp) }],
        series: seriesData.every(series => series.data.length === 0)
          ? [{ ...getDefaultLineConfig(), data: [60] }]
          : seriesData
      };

      if (this.myEchart) {
        this.myEchart.setOption(chartOption, notMerge);
      }
    } catch (error) {
      console.error('Error loading CPU chart data:', error);
    }
  }

  /**
   * Load memory chart data
   * <p>
   * Processes memory data and updates the chart and table.
   * </p>
   */
  loadMemoryEchartData (
    data: ChartDataItem[],
    t: (key: string) => string,
    showMemoryPercentChart: boolean,
    notMerge = true
  ): void {
    try {
      this.chartsData = data;
      const { seriesData, tableData, percentSeriesData, percentTableData } =
        ChartDataProcessor.processMemoryData(data, t, showMemoryPercentChart);

      this.memoryTableData = tableData;
      this.tableData.value = showMemoryPercentChart
        ? this.memoryTableData.filter(item => item.unit === '%')
        : this.memoryTableData.filter(item => item.unit !== '%');

      // Cache chart options
      this.chartOptionsCache.memory = {
        ...DEFAULT_ECHARTS_OPTIONS,
        legend: { ...DEFAULT_LEGEND, data: seriesData.map(series => series.name) },
        xAxis: [{ data: data.map(item => item.timestamp) }],
        series: seriesData.every(series => series.data.length === 0)
          ? [{ ...getDefaultLineConfig(), data: [50] }]
          : seriesData
      };

      this.chartOptionsCache.memoryPercent = {
        ...DEFAULT_ECHARTS_OPTIONS,
        legend: { ...DEFAULT_LEGEND, data: percentSeriesData.map(series => series.name) },
        xAxis: [{ data: data.map(item => item.timestamp) }],
        series: percentSeriesData.every(series => series.data.length === 0)
          ? [{ ...getDefaultLineConfig(), data: [50] }]
          : percentSeriesData
      };

      const chartOption = showMemoryPercentChart
        ? this.chartOptionsCache.memoryPercent
        : this.chartOptionsCache.memory;

      if (this.myEchart) {
        this.myEchart.setOption(chartOption, notMerge);
      }
    } catch (error) {
      console.error('Error loading memory chart data:', error);
    }
  }

  /**
   * Load disk chart data
   * <p>
   * Processes disk data and updates the chart and table.
   * </p>
   */
  loadDiskEchartData (
    data: ChartDataItem[],
    t: (key: string) => string,
    diskChartKey: string,
    notMerge = true
  ): void {
    try {
      this.chartsData = data;
      const { seriesData, tableData, chartOptions } =
        ChartDataProcessor.processDiskData(data, t, diskChartKey);

      this.diskTableData = tableData;
      this.tableData.value = tableData;

      // Cache all chart options
      Object.assign(this.chartOptionsCache, chartOptions);

      const chartOption = chartOptions[diskChartKey];
      if (chartOption && this.myEchart) {
        this.myEchart.setOption(chartOption, notMerge);
      }
    } catch (error) {
      console.error('Error loading disk chart data:', error);
    }
  }

  /**
   * Load network chart data
   * <p>
   * Processes network data and updates the chart and table.
   * </p>
   */
  loadNetworkEchartData (
    data: ChartDataItem[],
    t: (key: string) => string,
    networkChartKey: string,
    notMerge = true
  ): void {
    try {
      this.chartsData = data;
      const { seriesData, tableData, chartOptions } =
        ChartDataProcessor.processNetworkData(data, t, networkChartKey);

      this.networkTableData = tableData;
      this.tableData.value = tableData;

      // Cache all chart options
      Object.assign(this.chartOptionsCache, chartOptions);

      const chartOption = chartOptions[networkChartKey];
      if (chartOption && this.myEchart) {
        this.myEchart.setOption(chartOption, notMerge);
      }
    } catch (error) {
      console.error('Error loading network chart data:', error);
    }
  }

  /**
   * Update chart display based on active tab and options
   * <p>
   * Switches between different chart views without reloading data.
   * </p>
   */
  updateChartDisplay (
    activeTab: string,
    showMemoryPercentChart: boolean,
    diskChartKey: string,
    networkChartKey: string
  ): void {
    try {
      if (activeTab === 'memory') {
        const chartOption = showMemoryPercentChart
          ? this.chartOptionsCache.memoryPercent
          : this.chartOptionsCache.memory;

        if (chartOption && this.myEchart) {
          this.myEchart.setOption(chartOption, true);
        }

        this.tableData.value = showMemoryPercentChart
          ? this.memoryTableData.filter(item => item.unit === '%')
          : this.memoryTableData.filter(item => item.unit !== '%');
      }

      if (activeTab === 'disk') {
        const chartOption = this.chartOptionsCache[diskChartKey];
        if (chartOption && this.myEchart) {
          this.myEchart.setOption(chartOption, true);
        }

        // Update table data based on chart type
        switch (diskChartKey) {
          case 'disk':
            this.tableData.value = this.diskTableData.filter(item => item.unit === 'GB');
            break;
          case 'percent':
            this.tableData.value = this.diskTableData.filter(item => item.unit === '%');
            break;
          case 'rate':
            this.tableData.value = this.diskTableData.filter(item => item.unit === 'IO/s');
            break;
          case 'bytesRate':
            this.tableData.value = this.diskTableData.filter(item => item.unit === 'MB/s');
            break;
        }
      }

      if (activeTab === 'network') {
        const chartOption = this.chartOptionsCache[networkChartKey];
        if (chartOption && this.myEchart) {
          this.myEchart.setOption(chartOption, true);
        }

        // Update table data based on chart type
        switch (networkChartKey) {
          case 'network':
            this.tableData.value = this.networkTableData.filter(item => item.unit === 'MB/s');
            break;
          case 'packet':
            this.tableData.value = this.networkTableData.filter(item => item.unit === 'packets');
            break;
          case 'bytes':
            this.tableData.value = this.networkTableData.filter(item => item.unit === 'GB');
            break;
        }
      }
    } catch (error) {
      console.error('Error updating chart display:', error);
    }
  }

  /**
   * Cleanup resources
   * <p>
   * Disposes of the ECharts instance and clears cached data.
   * </p>
   */
  cleanup (): void {
    if (this.myEchart) {
      this.myEchart.dispose();
      this.myEchart = null;
    }
    this.chartOptionsCache = {};
    this.chartsData = [];
    this.memoryTableData = [];
    this.diskTableData = [];
    this.networkTableData = [];
    this.tableData.value = [];
  }
}
