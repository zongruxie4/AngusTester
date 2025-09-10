import { ref, computed, Ref } from 'vue';
import dayjs from 'dayjs';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { ListData } from '../../composables/useExecCount';
import { allCvsKeys } from '../../ChartConfig';
import {
  ApiDimensionData,
  IndexDimensionData,
  DataRateUnit,
  PerformanceListParams,
  ErrorCountListItem,
  SampleErrorContent,
  StatusCodeData,
  ComputedAggregateValues,
  ExceptionInfo
} from '../types';

/**
 * Composable for managing performance data state and operations
 * <p>
 * Handles data fetching, processing, and state management for performance metrics
 * including API dimensions, index dimensions, error counts, and status codes.
 */
export function usePerformanceData () {
  // ==================== State Management ====================

  /**
   * API dimension data organized by API name
   * <p>
   * Each API name maps to arrays of metric values over time
   */
  const apiDimensionObj = ref<Record<string, ApiDimensionData>>({
    Total: {
      duration: [],
      errors: [],
      iterations: [],
      n: [],
      operations: [],
      transactions: [],
      readBytes: [],
      writeBytes: [],
      ops: [],
      minOps: [],
      maxOps: [],
      meanOps: [],
      tps: [],
      minTps: [],
      maxTps: [],
      meanTps: [],
      brps: [],
      minBrps: [],
      maxBrps: [],
      meanBrps: [],
      bwps: [],
      minBwps: [],
      maxBwps: [],
      meanBwps: [],
      tranMean: [],
      tranMin: [],
      tranMax: [],
      tranP50: [],
      tranP75: [],
      tranP90: [],
      tranP95: [],
      tranP99: [],
      tranP999: [],
      errorRate: [],
      threadPoolSize: [],
      threadPoolActiveSize: [],
      threadMaxPoolSize: []
    }
  });

  /**
   * Index dimension data organized by metric type
   * <p>
   * Each metric type maps to API names and their corresponding value arrays
   */
  const indexDimensionObj = ref<IndexDimensionData>({
    duration: { Total: [] },
    errors: { Total: [] },
    iterations: { Total: [] },
    n: { Total: [] },
    operations: { Total: [] },
    transactions: { Total: [] },
    readBytes: { Total: [] },
    writeBytes: { Total: [] },
    ops: { Total: [] },
    minOps: { Total: [] },
    maxOps: { Total: [] },
    meanOps: { Total: [] },
    tps: { Total: [] },
    minTps: { Total: [] },
    maxTps: { Total: [] },
    meanTps: { Total: [] },
    brps: { Total: [] },
    minBrps: { Total: [] },
    maxBrps: { Total: [] },
    meanBrps: { Total: [] },
    bwps: { Total: [] },
    minBwps: { Total: [] },
    maxBwps: { Total: [] },
    meanBwps: { Total: [] },
    tranMean: { Total: [] },
    tranMin: { Total: [] },
    tranMax: { Total: [] },
    tranP50: { Total: [] },
    tranP75: { Total: [] },
    tranP90: { Total: [] },
    tranP95: { Total: [] },
    tranP99: { Total: [] },
    tranP999: { Total: [] },
    errorRate: { Total: [] },
    threadPoolSize: { Total: [] },
    threadPoolActiveSize: { Total: [] },
    threadMaxPoolSize: { Total: [] }
  });

  // ==================== Basic Data Arrays ====================

  const apiNames = ref<string[]>([]);
  const timestampData = ref<string[]>([]);
  const newList = ref<ListData[]>([]);
  const allList = ref<ListData[]>([]);
  const perfListData = ref<ListData[]>([]);

  // ==================== Performance List Management ====================

  const perfListParams = ref<PerformanceListParams>({
    pageNo: 1,
    pageSize: 500,
    filters: []
  });
  const perfListTotal = ref(0);
  const perfListLastTimestamp = ref('');

  // ==================== Error Management ====================

  const errCountList = ref<ErrorCountListItem[]>([]);
  const sampleList = ref<SampleErrorContent[]>([]);
  const errParams = ref<PerformanceListParams>({
    pageNo: 1,
    pageSize: 200,
    filters: []
  });
  const errTotal = ref(0);
  const errTimestamp = ref('');

  // ==================== Status Code Management ====================

  const statusCodeData = ref<StatusCodeData>({});

  // ==================== Aggregate Values ====================

  const infoMaxQps = ref<string | number>('');
  const infoMaxTps = ref<string | number>('');

  // ==================== Data Rate Units ====================

  const brpsUnit = ref<DataRateUnit>('KB');
  const minBrpsUnit = ref<DataRateUnit>('KB');
  const maxBrpsUnit = ref<DataRateUnit>('KB');
  const meanBrpsUnit = ref<DataRateUnit>('KB');
  const bwpsUnit = ref<DataRateUnit>('KB');
  const minBwpsUnit = ref<DataRateUnit>('KB');
  const maxBwpsUnit = ref<DataRateUnit>('KB');
  const meanBwpsUnit = ref<DataRateUnit>('KB');

  // ==================== Exception Management ====================

  const exception = ref<ExceptionInfo | undefined>();

  // ==================== Computed Properties ====================

  /**
   * Pipeline keys computed from detail configuration
   * <p>
   * Extracts unique pipeline target mapping keys for data processing
   */
  const pipelineKeys = computed(() => {
    // This will be set by the parent component
    return [];
  });

  // ==================== Data Processing Methods ====================

  /**
   * Normalize and push incoming list data into chart-ready structures
   * <p>
   * - De-duplicate last timestamp when new batch overlaps
   * - Maintain both API-dimension and index-dimension structures
   * - Convert BRPS/BWPS units from bytes to KB/MB dynamically
   */
  // eslint-disable-next-line @typescript-eslint/ban-types
  const saveData = (list: ListData[], convertCvsValue: Function, pipelineKeys: string[]) => {
    if (!list?.length) {
      return;
    }

    const processedList = list;

    // Handle timestamp deduplication for incremental updates
    if (perfListData.value.length && perfListData.value[perfListData.value.length - 1]?.timestamp === list[0]?.timestamp) {
      perfListData.value.pop();
      timestampData.value.pop();

      // Remove last entries from all dimension objects
      for (const key in apiDimensionObj.value) {
        Object.keys(apiDimensionObj.value[key]).forEach(metricKey => {
          (apiDimensionObj.value[key] as any)[metricKey].pop();
        });
      }

      for (const key in indexDimensionObj.value) {
        const innerObj = indexDimensionObj.value[key];
        for (const innerKey in innerObj) {
          const innerArray = innerObj[innerKey];
          innerArray.pop();
        }
      }
    }

    perfListData.value.push(...processedList);
    perfListLastTimestamp.value = perfListData.value[perfListData.value.length - 1].timestamp;

    newList.value = convertCvsValue(processedList, allCvsKeys, pipelineKeys) as unknown as ListData[];
    allList.value.push(...newList.value);

    const othersValue = computedOthersValue();
    infoMaxQps.value = othersValue.maxOps;
    infoMaxTps.value = othersValue.maxTps;

    const times: string[] = [];
    for (let i = 0; i < newList.value.length; i++) {
      times.push(newList.value[i].timestamp);
      const values = newList.value[i].values;

      for (let j = 0; j < values.length; j++) {
        const value = values[j];
        processValueData(value, othersValue);
      }
    }

    timestampData.value = [...timestampData.value, ...times].map(item => {
      return dayjs(item).format(DATE_TIME_FORMAT);
    });
  };

  /**
   * Process individual value data and update dimension objects
   * <p>
   * Handles unit conversion, data normalization, and dimension updates
   */
  const processValueData = (value: any, othersValue: ComputedAggregateValues) => {
    // Set aggregate values
    value.maxOps = othersValue.maxOps;
    value.minOps = othersValue.minOps;
    value.meanOps = othersValue.meanOps;
    value.minTps = othersValue.minTps;
    value.maxTps = othersValue.maxTps;
    value.meanTps = othersValue.meanTps;
    value.minBrps = othersValue.minBrps;
    value.maxBrps = othersValue.maxBrps;
    value.meanBrps = othersValue.meanBrps;
    value.minBwps = othersValue.minBwps;
    value.maxBwps = othersValue.maxBwps;
    value.meanBwps = othersValue.meanBwps;

    const name = value.name;

    if (!apiNames.value.includes(name)) {
      apiNames.value.push(name);
    }

    // Process BRPS/BWPS unit conversions
    const brpsData = processDataRateConversion(value.brps, brpsUnit, 'brps', name);
    const minBrpsData = processDataRateConversion(value.minBrps, minBrpsUnit, 'minBrps', name);
    const maxBrpsData = processDataRateConversion(value.maxBrps, maxBrpsUnit, 'maxBrps', name);
    const meanBrpsData = processDataRateConversion(value.meanBrps, meanBrpsUnit, 'meanBrps', name);
    const bwpsData = processDataRateConversion(value.bwps, bwpsUnit, 'bwps', name);
    const minBwpsData = processDataRateConversion(value.minBwps, minBwpsUnit, 'minBwps', name);
    const maxBwpsData = processDataRateConversion(value.maxBwps, maxBwpsUnit, 'maxBwps', name);
    const meanBwpsData = processDataRateConversion(value.meanBwps, meanBwpsUnit, 'meanBwps', name);

    // Initialize API dimension if not exists
    if (!(name in apiDimensionObj.value)) {
      apiDimensionObj.value[name] = createEmptyApiDimensionData();
    }

    // Initialize index dimension if not exists
    if (!(name in indexDimensionObj.value.duration)) {
      initializeIndexDimensionForApi(name);
    }

    // Update API dimension data
    updateApiDimensionData(name, value, brpsData, minBrpsData, maxBrpsData, meanBrpsData, bwpsData, minBwpsData, maxBwpsData, meanBwpsData);

    // Update index dimension data
    updateIndexDimensionData(name, value, brpsData, minBrpsData, maxBrpsData, meanBrpsData, bwpsData, minBwpsData, maxBwpsData, meanBwpsData);
  };

  /**
   * Process data rate conversion from bytes to KB/MB
   * <p>
   * Automatically switches unit when values exceed 1000 in current unit
   */
  const processDataRateConversion = (
    value: number,
    unitRef: Ref<DataRateUnit>,
    metricKey: string,
    apiName: string
  ): number => {
    if (!value || +value <= 0) {
      return 0;
    }

    let convertedValue = 0;
    if (unitRef.value === 'KB') {
      convertedValue = +(+value / 1024).toFixed(2);
      if (convertedValue > 1000) {
        convertedValue = +(+value / 1024 / 1024).toFixed(2);
        unitRef.value = 'MB';
        // Convert existing data in index dimension
        if (indexDimensionObj.value[metricKey]?.[apiName]?.length > 0) {
          indexDimensionObj.value[metricKey][apiName] = indexDimensionObj.value[metricKey][apiName].map(data => +(data / 1024).toFixed(2));
        }
      }
    } else {
      convertedValue = +(+value / 1024 / 1024).toFixed(2);
    }

    return convertedValue;
  };

  /**
   * Create empty API dimension data structure
   */
  const createEmptyApiDimensionData = (): ApiDimensionData => ({
    duration: [],
    errors: [],
    iterations: [],
    n: [],
    operations: [],
    transactions: [],
    readBytes: [],
    writeBytes: [],
    ops: [],
    minOps: [],
    maxOps: [],
    meanOps: [],
    tps: [],
    minTps: [],
    maxTps: [],
    meanTps: [],
    brps: [],
    minBrps: [],
    maxBrps: [],
    meanBrps: [],
    bwps: [],
    minBwps: [],
    maxBwps: [],
    meanBwps: [],
    tranMean: [],
    tranMin: [],
    tranMax: [],
    tranP50: [],
    tranP75: [],
    tranP90: [],
    tranP95: [],
    tranP99: [],
    tranP999: [],
    errorRate: [],
    threadPoolSize: [],
    threadPoolActiveSize: [],
    threadMaxPoolSize: []
  });

  /**
   * Initialize index dimension data for a new API
   */
  const initializeIndexDimensionForApi = (apiName: string) => {
    const metrics = Object.keys(indexDimensionObj.value);
    metrics.forEach(metric => {
      indexDimensionObj.value[metric][apiName] = [];
    });
  };

  /**
   * Update API dimension data with new values
   */
  const updateApiDimensionData = (
    name: string,
    value: any,
    brpsData: number,
    minBrpsData: number,
    maxBrpsData: number,
    meanBrpsData: number,
    bwpsData: number,
    minBwpsData: number,
    maxBwpsData: number,
    meanBwpsData: number
  ) => {
    const apiData = apiDimensionObj.value[name];
    apiData.duration.push(+value.duration || 0);
    apiData.errors.push(+value.errors || 0);
    apiData.iterations.push(+value.iterations || 0);
    apiData.n.push(+value.n || 0);
    apiData.operations.push(+value.operations || 0);
    apiData.transactions.push(+value.transactions || 0);
    apiData.readBytes.push(+value.readBytes || 0);
    apiData.writeBytes.push(+value.writeBytes || 0);
    apiData.ops.push(+value.ops || 0);
    apiData.minOps.push(+value.minOps || 0);
    apiData.maxOps.push(+value.maxOps || 0);
    apiData.meanOps.push(+value.meanOps || 0);
    apiData.tps.push(+value.tps || 0);
    apiData.minTps.push(+value.minTps || 0);
    apiData.maxTps.push(+value.maxTps || 0);
    apiData.meanTps.push(+value.meanTps || 0);
    apiData.brps.push(brpsData);
    apiData.minBrps.push(minBrpsData);
    apiData.maxBrps.push(maxBrpsData);
    apiData.meanBrps.push(meanBrpsData);
    apiData.bwps.push(bwpsData);
    apiData.minBwps.push(minBwpsData);
    apiData.maxBwps.push(maxBwpsData);
    apiData.meanBwps.push(meanBwpsData);
    apiData.tranMean.push(+value.tranMean || 0);
    apiData.tranMin.push(+value.tranMin || 0);
    apiData.tranMax.push(+value.tranMax || 0);
    apiData.tranP50.push(+value.tranP50 || 0);
    apiData.tranP75.push(+value.tranP75 || 0);
    apiData.tranP90.push(+value.tranP90 || 0);
    apiData.tranP95.push(+value.tranP95 || 0);
    apiData.tranP99.push(+value.tranP99 || 0);
    apiData.tranP999.push(+value.tranP999 || 0);
    apiData.errorRate.push(+value.errorRate || 0);
    apiData.threadPoolSize.push(+value.threadPoolSize || 0);
    apiData.threadPoolActiveSize.push(+value.threadPoolActiveSize || 0);
    apiData.threadMaxPoolSize.push(+value.threadMaxPoolSize || 0);
  };

  /**
   * Update index dimension data with new values
   */
  const updateIndexDimensionData = (
    name: string,
    value: any,
    brpsData: number,
    minBrpsData: number,
    maxBrpsData: number,
    meanBrpsData: number,
    bwpsData: number,
    minBwpsData: number,
    maxBwpsData: number,
    meanBwpsData: number
  ) => {
    const indexData = indexDimensionObj.value;
    indexData.duration[name].push(+value.duration || 0);
    indexData.errors[name].push(+value.errors || 0);
    indexData.iterations[name].push(+value.iterations || 0);
    indexData.n[name].push(+value.n || 0);
    indexData.operations[name].push(+value.operations || 0);
    indexData.transactions[name].push(+value.transactions || 0);
    indexData.readBytes[name].push(+value.readBytes || 0);
    indexData.writeBytes[name].push(+value.writeBytes || 0);
    indexData.ops[name].push(+value.ops || 0);
    indexData.minOps[name].push(+value.minOps || 0);
    indexData.maxOps[name].push(+value.maxOps || 0);
    indexData.meanOps[name].push(+value.meanOps || 0);
    indexData.tps[name].push(+value.tps || 0);
    indexData.minTps[name].push(+value.minTps || 0);
    indexData.maxTps[name].push(+value.maxTps || 0);
    indexData.meanTps[name].push(+value.meanTps || 0);
    indexData.brps[name].push(brpsData);
    indexData.minBrps[name].push(minBrpsData);
    indexData.maxBrps[name].push(maxBrpsData);
    indexData.meanBrps[name].push(meanBrpsData);
    indexData.bwps[name].push(bwpsData);
    indexData.minBwps[name].push(minBwpsData);
    indexData.maxBwps[name].push(maxBwpsData);
    indexData.meanBwps[name].push(meanBwpsData);
    indexData.tranMean[name].push(+value.tranMean || 0);
    indexData.tranMin[name].push(+value.tranMin || 0);
    indexData.tranMax[name].push(+value.tranMax || 0);
    indexData.tranP50[name].push(+value.tranP50 || 0);
    indexData.tranP75[name].push(+value.tranP75 || 0);
    indexData.tranP90[name].push(+value.tranP90 || 0);
    indexData.tranP95[name].push(+value.tranP95 || 0);
    indexData.tranP99[name].push(+value.tranP99 || 0);
    indexData.tranP999[name].push(+value.tranP999 || 0);
    indexData.errorRate[name].push(+value.errorRate || 0);
    indexData.threadPoolSize[name].push(+value.threadPoolSize || 0);
    indexData.threadPoolActiveSize[name].push(+value.threadPoolActiveSize || 0);
    indexData.threadMaxPoolSize[name].push(+value.threadMaxPoolSize || 0);
  };

  /**
   * Compute aggregate values for Ops/Tps/Brps/Bwps across all data points of 'Total'
   * <p>
   * Calculates min, max, and mean values for performance metrics
   */
  const computedOthersValue = (): ComputedAggregateValues => {
    let maxOps = 0; let minOps = 0; let meanOps = 0;
    let maxTps = 0; let minTps = 0; let meanTps = 0;
    let maxBrps = 0; let minBrps = 0; let meanBrps = 0;
    let maxBwps = 0; let minBwps = 0; let meanBwps = 0;

    for (let i = 0; i < allList.value.length; i++) {
      const totalItem = allList.value[i].values.find(f => f.name === 'Total');
      if (!totalItem) continue;

      const ops = Number(totalItem.ops) || 0;
      const tps = Number(totalItem.tps) || 0;
      const brps = Number(totalItem.brps) || 0;
      const bwps = Number(totalItem.bwps) || 0;

      if (i === 0) {
        minOps = ops;
        minTps = tps;
        minBrps = brps;
        minBwps = bwps;
      }

      maxOps = Math.max(maxOps, ops);
      minOps = Math.min(minOps, ops);
      meanOps += ops;

      maxTps = Math.max(maxTps, tps);
      minTps = Math.min(minTps, tps);
      meanTps += tps;

      maxBrps = Math.max(maxBrps, brps);
      minBrps = Math.min(minBrps, brps);
      meanBrps += brps;

      maxBwps = Math.max(maxBwps, bwps);
      minBwps = Math.min(minBwps, bwps);
      meanBwps += bwps;
    }

    // Compute final averages
    const length = allList.value.length || 1;
    meanOps = +(meanOps / length).toFixed(2);
    meanTps = +(meanTps / length).toFixed(2);
    meanBrps = +(meanBrps / length).toFixed(2);
    meanBwps = +(meanBwps / length).toFixed(2);

    return {
      maxOps,
      minOps,
      meanOps,
      maxTps,
      minTps,
      meanTps,
      maxBrps,
      minBrps,
      meanBrps,
      maxBwps,
      minBwps,
      meanBwps
    };
  };

  /**
   * Reset all performance data to initial state
   * <p>
   * Clears all arrays and resets counters for fresh data loading
   */
  const resetData = () => {
    apiDimensionObj.value = {
      Total: createEmptyApiDimensionData()
    };

    indexDimensionObj.value = {
      duration: { Total: [] },
      errors: { Total: [] },
      iterations: { Total: [] },
      n: { Total: [] },
      operations: { Total: [] },
      transactions: { Total: [] },
      readBytes: { Total: [] },
      writeBytes: { Total: [] },
      ops: { Total: [] },
      minOps: { Total: [] },
      maxOps: { Total: [] },
      meanOps: { Total: [] },
      tps: { Total: [] },
      minTps: { Total: [] },
      maxTps: { Total: [] },
      meanTps: { Total: [] },
      brps: { Total: [] },
      minBrps: { Total: [] },
      maxBrps: { Total: [] },
      meanBrps: { Total: [] },
      bwps: { Total: [] },
      minBwps: { Total: [] },
      maxBwps: { Total: [] },
      meanBwps: { Total: [] },
      tranMean: { Total: [] },
      tranMin: { Total: [] },
      tranMax: { Total: [] },
      tranP50: { Total: [] },
      tranP75: { Total: [] },
      tranP90: { Total: [] },
      tranP95: { Total: [] },
      tranP99: { Total: [] },
      tranP999: { Total: [] },
      errorRate: { Total: [] },
      threadPoolSize: { Total: [] },
      threadPoolActiveSize: { Total: [] },
      threadMaxPoolSize: { Total: [] }
    };

    // Reset all other state
    perfListParams.value = { pageNo: 1, pageSize: 500, filters: [] };
    perfListTotal.value = 0;
    perfListLastTimestamp.value = '';
    perfListData.value = [];
    apiNames.value = [];
    timestampData.value = [];
    newList.value = [];
    allList.value = [];
    errCountList.value = [];
    brpsUnit.value = 'KB';
    bwpsUnit.value = 'KB';
    errParams.value = { pageNo: 1, pageSize: 200, filters: [] };
    errTotal.value = 0;
    errTimestamp.value = '';
    sampleList.value = [];
    statusCodeData.value = {};
    exception.value = undefined;
  };

  return {
    // State
    apiDimensionObj,
    indexDimensionObj,
    apiNames,
    timestampData,
    newList,
    allList,
    perfListData,
    perfListParams,
    perfListTotal,
    perfListLastTimestamp,
    errCountList,
    sampleList,
    errParams,
    errTotal,
    errTimestamp,
    statusCodeData,
    infoMaxQps,
    infoMaxTps,
    brpsUnit,
    minBrpsUnit,
    maxBrpsUnit,
    meanBrpsUnit,
    bwpsUnit,
    minBwpsUnit,
    maxBwpsUnit,
    meanBwpsUnit,
    exception,
    pipelineKeys,

    // Methods
    saveData,
    computedOthersValue,
    resetData
  };
}
