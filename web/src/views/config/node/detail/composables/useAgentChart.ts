import { ref } from 'vue';
import type { AgentChartProps, CacheChartData, RamChartData, CpuChartData, ChartDataParams } from '../types';

/**
 * <p>Composable for managing agent chart data and operations</p>
 * <p>Handles chart data processing and state management for cache, RAM, and CPU charts</p>
 *
 * @param props - Component props containing agent ID
 * @returns Object containing chart data and related methods
 */
export function useAgentChart (props: AgentChartProps) {
  /**
   * <p>Cache chart data for display</p>
   * <p>Contains cache-related metrics and time series data</p>
   */
  const cacheChartData = ref<CacheChartData>({
    xData: [],
    numData: [],
    totalData: [],
    usedData: [],
    maxTotalData: 0,
    maxNumData: 0
  });

  /**
   * <p>RAM chart data for display</p>
   * <p>Contains memory-related metrics and time series data</p>
   */
  const ramChartData = ref<RamChartData>({
    xData: [],
    submitData: [],
    totalData: [],
    usedData: []
  });

  /**
   * <p>CPU chart data for display</p>
   * <p>Contains CPU-related metrics and time series data</p>
   */
  const cpuChartData = ref<CpuChartData>({
    xData: [],
    totalData: [],
    systemCpuUsed: [],
    processCpuUsed: []
  });

  /**
   * <p>Processes chart data from API response</p>
   * <p>Transforms raw data into chart-ready format for cache, RAM, and CPU charts</p>
   *
   * @param data - Raw chart data from API
   * @param params - Chart parameters including filters
   */
  const processChartData = (data: any[], params: ChartDataParams): void => {
    // Reset chart data
    resetChartData();

    if (!data.length) {
      // Handle empty data case with time range display
      const startTime = params.filters.find(item => item.key === 'timestamp' && item.op === 'GREATER_THAN_EQUAL')?.value;
      const endTime = params.filters.find(item => item.key === 'timestamp' && item.op === 'LESS_THAN_EQUAL')?.value;

      if (startTime && endTime) {
        const timeData = [startTime.replace(' ', '\n'), endTime.replace(' ', '\n')];
        cacheChartData.value.xData = timeData;
        ramChartData.value.xData = timeData;
        cpuChartData.value.xData = timeData;
      }
      return;
    }

    // Process data for each time point
    for (let i = 0; i < data.length; i++) {
      const item = data[i];
      const timestamp = item.timestamp.replace(' ', '\n');

      // Add timestamp to all charts
      cacheChartData.value.xData.push(timestamp);
      ramChartData.value.xData.push(timestamp);
      cpuChartData.value.xData.push(timestamp);

      // Process cache data
      processCacheData(item, i);

      // Process RAM data
      processRamData(item, i);

      // Process CPU data
      processCpuData(item, i);
    }
  };

  /**
   * <p>Processes cache-related data for a single time point</p>
   * <p>Extracts and formats cache metrics from JVM data</p>
   *
   * @param item - Data item for current time point
   * @param index - Index of the data item
   */
  const processCacheData = (item: any, index: number): void => {
    const values = item.cvsJvm.split(',');

    // Process cache entry count
    const numValue = +values[0];
    cacheChartData.value.numData.push(numValue);

    // Update maximum cache entry count
    if (numValue > cacheChartData.value.maxNumData) {
      cacheChartData.value.maxNumData = numValue;
    }

    // Process cache size data (convert bytes to GB)
    const usedValue = parseFloat(values[1]) / 1024 / 1024 / 1024;
    cacheChartData.value.usedData.push(+usedValue.toFixed(2));

    const totalValue = parseFloat(values[2]) / 1024 / 1024 / 1024;
    cacheChartData.value.totalData.push(+totalValue.toFixed(2));

    // Update maximum total cache size
    if (totalValue > cacheChartData.value.maxTotalData) {
      cacheChartData.value.maxTotalData = totalValue;
    }
  };

  /**
   * <p>Processes RAM-related data for a single time point</p>
   * <p>Extracts and formats memory metrics from JVM data</p>
   *
   * @param item - Data item for current time point
   * @param index - Index of the data item
   */
  const processRamData = (item: any, index: number): void => {
    const values = item.cvsJvm.split(',');

    // Process RAM metrics (convert bytes to GB)
    const ramUsedData = parseFloat(values[3]) / 1024 / 1024 / 1024;
    ramChartData.value.usedData.push(+ramUsedData.toFixed(2));

    const ramSubmitData = parseFloat(values[4]) / 1024 / 1024 / 1024;
    ramChartData.value.submitData.push(+ramSubmitData.toFixed(2));

    const ramTotalData = parseFloat(values[5]) / 1024 / 1024 / 1024;
    ramChartData.value.totalData.push(+ramTotalData.toFixed(2));
  };

  /**
   * <p>Processes CPU-related data for a single time point</p>
   * <p>Extracts and formats CPU metrics from processor data</p>
   *
   * @param item - Data item for current time point
   * @param index - Index of the data item
   */
  const processCpuData = (item: any, index: number): void => {
    const cpuValues = item.cvsProcessor.split(',');

    // Process CPU metrics
    cpuChartData.value.totalData.push(+cpuValues[0]);
    cpuChartData.value.systemCpuUsed.push(+cpuValues[1]);
    cpuChartData.value.processCpuUsed.push(+cpuValues[2]);
  };

  /**
   * <p>Resets all chart data to initial state</p>
   * <p>Clears all time series data and resets maximum values</p>
   */
  const resetChartData = (): void => {
    cacheChartData.value = {
      xData: [],
      numData: [],
      totalData: [],
      usedData: [],
      maxTotalData: 0,
      maxNumData: 0
    };

    ramChartData.value = {
      xData: [],
      submitData: [],
      totalData: [],
      usedData: []
    };

    cpuChartData.value = {
      xData: [],
      totalData: [],
      systemCpuUsed: [],
      processCpuUsed: []
    };
  };

  /**
   * <p>Gets chart data for external components</p>
   * <p>Provides access to processed chart data for rendering</p>
   *
   * @param data - Raw chart data from API
   * @param params - Chart parameters including filters
   */
  const getChartData = (data: any[], params: ChartDataParams): void => {
    processChartData(data, params);
  };

  return {
    // State
    cacheChartData,
    ramChartData,
    cpuChartData,

    // Methods
    getChartData,
    processChartData,
    resetChartData
  };
}
