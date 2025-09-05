import { ref } from 'vue';

import {
  CacheChartData,
  ChartTimeType,
  CpuChartData,
  MetricsDataItem,
  RamChartData
} from '@/views/apis/mock/detail/types';

/**
 * Composable for managing chart data transformation and state.
 * <p>
 * Handles the conversion of raw metrics data into chart-ready format
 * for cache, RAM, and CPU visualization components.
 */
export function useChartData () {
  const cacheChartData = ref<CacheChartData>({
    xData: [],
    numData: [],
    totalData: [],
    usedData: [],
    maxTotalData: 0,
    maxNumData: 0
  });

  const ramChartData = ref<RamChartData>({
    xData: [],
    submitData: [],
    totalData: [],
    usedData: []
  });

  const cpuChartData = ref<CpuChartData>({
    xData: [],
    totalData: [],
    systemCpuUsed: [],
    processCpuUsed: []
  });

  /**
   * Initialize chart data with empty state based on time range.
   * <p>
   * Used when no metrics data is available but we need to show time axis.
   */
  const initializeEmptyChartData = (params: any, type: ChartTimeType) => {
    const startTimeFilter = params.filters.find((item: any) =>
      item.key === 'timestamp' && item.op === 'GREATER_THAN_EQUAL'
    );
    const endTimeFilter = params.filters.find((item: any) =>
      item.key === 'timestamp' && item.op === 'LESS_THAN_EQUAL'
    );

    if (!startTimeFilter || !endTimeFilter) return;

    const formatTime = (time: string) =>
      type === 'select' ? time.split(' ')[1] : time.replace(' ', '\n');

    const timeData = [
      formatTime(startTimeFilter.value as string),
      formatTime(endTimeFilter.value as string)
    ];

    cacheChartData.value = {
      xData: timeData,
      numData: [0, 0],
      totalData: [0, 0],
      usedData: [0, 0],
      maxTotalData: 0,
      maxNumData: 0
    };

    ramChartData.value = {
      xData: timeData,
      submitData: [0, 0],
      totalData: [0, 0],
      usedData: [0, 0]
    };

    cpuChartData.value = {
      xData: timeData,
      totalData: [0, 0],
      systemCpuUsed: [0, 0],
      processCpuUsed: [0, 0]
    };
  };

  /**
   * Transform raw metrics data into chart format.
   * <p>
   * Parses CSV-like data strings and converts to appropriate units (GB for memory).
   */
  const transformMetricsToChartData = (data: MetricsDataItem[], type: ChartTimeType) => {
    // Reset chart data
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

    for (const item of data) {
      const formattedTime = type === 'select'
        ? item.timestamp.split(' ')[1]
        : item.timestamp.replace(' ', '\n');

      const jvmValues = item.cvsJvm.split(',');
      const cpuValues = item.cvsProcessor.split(',');

      // Add time to all charts
      cacheChartData.value.xData.push(formattedTime);
      ramChartData.value.xData.push(formattedTime);
      cpuChartData.value.xData.push(formattedTime);

      // Process cache data
      const numValue = Number(jvmValues[0]);
      cacheChartData.value.numData.push(numValue);
      if (numValue > cacheChartData.value.maxNumData) {
        cacheChartData.value.maxNumData = numValue;
      }

      const cacheUsedGB = parseFloat(jvmValues[1]) / 1024 / 1024 / 1024;
      cacheChartData.value.usedData.push(Number(cacheUsedGB.toFixed(2)));

      const cacheTotalGB = parseFloat(jvmValues[2]) / 1024 / 1024 / 1024;
      cacheChartData.value.totalData.push(Number(cacheTotalGB.toFixed(2)));
      if (cacheTotalGB > cacheChartData.value.maxTotalData) {
        cacheChartData.value.maxTotalData = cacheTotalGB;
      }

      // Process RAM data
      const ramUsedGB = parseFloat(jvmValues[3]) / 1024 / 1024 / 1024;
      ramChartData.value.usedData.push(Number(ramUsedGB.toFixed(2)));

      const ramSubmitGB = parseFloat(jvmValues[4]) / 1024 / 1024 / 1024;
      ramChartData.value.submitData.push(Number(ramSubmitGB.toFixed(2)));

      const ramTotalGB = parseFloat(jvmValues[5]) / 1024 / 1024 / 1024;
      ramChartData.value.totalData.push(Number(ramTotalGB.toFixed(2)));

      // Process CPU data
      cpuChartData.value.totalData.push(Number(cpuValues[0]));
      cpuChartData.value.systemCpuUsed.push(Number(cpuValues[1]));
      cpuChartData.value.processCpuUsed.push(Number(cpuValues[2]));
    }
  };

  /**
   * Main handler for chart data updates from IntervalTimestamp component.
   * <p>
   * Determines whether to show empty state or process actual metrics data.
   */
  const handleChartDataChange = (data: MetricsDataItem[], params: any, type: ChartTimeType) => {
    if (!data.length) {
      initializeEmptyChartData(params, type);
    } else {
      transformMetricsToChartData(data, type);
    }
  };

  return {
    cacheChartData,
    ramChartData,
    cpuChartData,
    handleChartDataChange
  };
}
