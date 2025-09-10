import { ref } from 'vue';
import dayjs from 'dayjs';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { ErrorCountListItem, SampleErrorContent, StatusCodeData } from '../types';

/**
 * Composable for managing error-related data and operations
 * <p>
 * Handles error count loading, sample error content management, and status code data processing
 * for performance monitoring and error analysis.
 */
export function useErrorManagement () {
  // ==================== State Management ====================

  /**
   * Error count list for displaying error statistics
   */
  const errCountList = ref<ErrorCountListItem[]>([]);

  /**
   * Sample error content list for detailed error information
   */
  const sampleList = ref<SampleErrorContent[]>([]);

  /**
   * Error list pagination parameters
   */
  const errParams = ref({
    pageNo: 1,
    pageSize: 200,
    filters: [] as Array<{
      key: 'timestamp';
      op: 'GREATER_THAN_EQUAL';
      value: string;
    }>
  });

  /**
   * Total count of error records
   */
  const errTotal = ref(0);

  /**
   * Last error timestamp for incremental loading
   */
  const errTimestamp = ref('');

  /**
   * HTTP status code data organized by pipeline hierarchy
   */
  const statusCodeData = ref<StatusCodeData>({});

  // ==================== Error Count Management ====================

  /**
   * Load latest error counters and compute error rates based on last sample summary
   * <p>
   * Fetches error counter data and calculates error rates for display
   */
  const loadErrorCount = async (
    detailId: string,
    getSampleErrorCounterLatest: Function,
    newList: any[],
    emit: Function
  ) => {
    const [counterErr, counterRes] = await getSampleErrorCounterLatest(detailId);
    if (counterErr) {
      emit('update:loading', false);
      return;
    }

    if (counterRes?.data) {
      emit('update:loading', false);
    }

    const errorCounterData = counterRes.data;
    errCountList.value = getErrCountList(errorCounterData, newList[newList.length - 1]);
  };

  /**
   * Transform raw error counters to a view model with totals and per-type breakdown
   * <p>
   * Processes error counter data into a structured format for display
   */
  const getErrCountList = (counterData: Record<string, any>, lastData: any): ErrorCountListItem[] => {
    const result: ErrorCountListItem[] = [];

    for (const key in counterData) {
      const counter = counterData[key];
      let errorNum = 0;

      // Calculate total error count for this API
      for (const prop in counter) {
        const value = parseInt(counter[prop]);
        if (!isNaN(value)) {
          errorNum += value;
        }
      }

      // Find matching value in last data to calculate error rate
      const matchingValue = lastData?.values?.find((value: any) => value.name === key);
      let errorRate = '0';

      if (matchingValue && !isNaN(errorNum) && !isNaN(matchingValue.n)) {
        if (typeof matchingValue.n === 'number' && matchingValue.n !== 0) {
          errorRate = (errorNum / matchingValue.n * 100).toFixed(2) + '%';
        } else {
          errorRate = '0%';
        }
      }

      // Create detailed error breakdown
      const list: Array<{ name: string; errorNum: number | string }> = [];
      for (const prop in counter) {
        const value = parseInt(counter[prop]);
        const errorNumValue = !isNaN(value) ? value : '--';
        list.push({
          name: prop,
          errorNum: errorNumValue
        });
      }

      result.push({
        name: key,
        errorNum: !isNaN(errorNum) ? errorNum : '--',
        errorRate: errorRate,
        list: list
      });
    }

    // Sort results to put 'Total' at the end
    const totalElement = result.filter((element) => element.name === 'Total');
    const otherElements = result.filter((element) => element.name !== 'Total');
    const finalResult = [...otherElements, ...totalElement];

    return finalResult;
  };

  // ==================== Sample Error Content Management ====================

  /**
   * Load paginated sample error contents incrementally using timestamp cursor
   * <p>
   * Fetches error sample data with pagination support for large datasets
   */
  const loadSampleErrorContent = async (
    detailId: string,
    // eslint-disable-next-line @typescript-eslint/ban-types
    getSampleErrContent: Function,
    pageNo?: number,
    // eslint-disable-next-line @typescript-eslint/ban-types
    emit?: Function
  ) => {
    if (pageNo) {
      errParams.value.pageNo = pageNo;
    }

    if (errTimestamp.value) {
      errParams.value.filters = [{
        key: 'timestamp',
        op: 'GREATER_THAN_EQUAL',
        value: dayjs(errTimestamp.value).format(DATE_TIME_FORMAT)
      }];
    }

    const [error, { data = { list: [], total: 0 } }] = await getSampleErrContent(detailId, errParams.value);
    if (error) {
      emit?.('update:loading', false);
      return;
    }

    if (data?.list?.length) {
      emit?.('update:loading', false);
    }

    errTotal.value = +data.total;
    if (data.list.length) {
      errTimestamp.value = data.list[data.list.length - 1].timestamp;
      sampleList.value = [...sampleList.value, ...data.list];
    }
  };

  // ==================== Status Code Management ====================

  /**
   * Load latest HTTP status code counters (no history kept)
   * <p>
   * Fetches current status code distribution for HTTP requests
   */
  const loadStatusCodeData = async (
    detailId: string,
    // eslint-disable-next-line @typescript-eslint/ban-types
    getSampleExtensionCountMapLatest: Function,
    detail: any,
    // eslint-disable-next-line @typescript-eslint/ban-types
    emit?: Function
  ) => {
    const [error, { data }] = await getSampleExtensionCountMapLatest(detailId);
    if (error) {
      emit?.('update:loading', false);
      return;
    }

    if (data) {
      emit?.('update:loading', false);
    }

    setStatusCodeData(data, detail.pipelineTargetMappings);
  };

  /**
   * Organize status code counters by pipeline hierarchy or default to Total
   * <p>
   * Processes status code data based on pipeline configuration and single interface mode
   */
  const setStatusCodeData = (data: Record<string, any>, pipeline: Record<string, any>) => {
    if (pipeline?.isSingleInterface) {
      if (!data?.Total) {
        statusCodeData.value = {
          Total: {
            '2xx': 0,
            '3xx': 0,
            '4xx': 0,
            '5xx': 0,
            Exception: 0
          }
        };
        return;
      }
      statusCodeData.value = {
        Total: {
          '2xx': data?.total?.['2xx'] || 0,
          '3xx': data?.total?.['3xx'] || 0,
          '4xx': data?.total?.['4xx'] || 0,
          '5xx': data?.total?.['5xx'] || 0,
          Exception: data?.total?.Exception || 0
        }
      };
      return;
    }

    statusCodeData.value = {};

    const processData = (obj: Record<string, any>) => {
      for (const [key, value] of Object.entries(obj)) {
        statusCodeData.value[key] = {
          '2xx': value?.['2xx'] ? parseInt(value['2xx']) : 0,
          '3xx': value?.['3xx'] ? parseInt(value['3xx']) : 0,
          '4xx': value?.['4xx'] ? parseInt(value['4xx']) : 0,
          '5xx': value?.['5xx'] ? parseInt(value['5xx']) : 0,
          Exception: value?.Exception ? parseInt(value.Exception) : 0
        };
      }
    };

    if (!pipeline || !Object.keys(pipeline).length || !data || !Object.keys(data).length) {
      statusCodeData.value = {
        Total: {
          '2xx': 0,
          '3xx': 0,
          '4xx': 0,
          '5xx': 0,
          Exception: 0
        }
      };
      return;
    }

    if (!pipeline) {
      processData(data);
      return;
    }

    for (const key in pipeline) {
      statusCodeData.value[key] = {
        '2xx': data[key]?.['2xx'] ? parseInt(data[key]?.['2xx']) : 0,
        '3xx': data[key]?.['3xx'] ? parseInt(data[key]?.['3xx']) : 0,
        '4xx': data[key]?.['4xx'] ? parseInt(data[key]?.['4xx']) : 0,
        '5xx': data[key]?.['5xx'] ? parseInt(data[key]?.['5xx']) : 0,
        Exception: data[key]?.Exception ? parseInt(data[key]?.Exception) : 0
      };

      if (pipeline[key]?.length) {
        for (let i = 0; i < pipeline[key].length; i++) {
          const cKey = pipeline[key][i];
          statusCodeData.value[cKey] = {
            '2xx': data[cKey]?.['2xx'] ? parseInt(data[cKey]?.['2xx']) : 0,
            '3xx': data[cKey]?.['3xx'] ? parseInt(data[cKey]?.['3xx']) : 0,
            '4xx': data[cKey]?.['4xx'] ? parseInt(data[cKey]?.['4xx']) : 0,
            '5xx': data[cKey]?.['5xx'] ? parseInt(data[cKey]?.['5xx']) : 0,
            Exception: data[cKey]?.Exception ? parseInt(data[cKey]?.Exception) : 0
          };
        }
      }
    }
  };

  // ==================== Reset Methods ====================

  /**
   * Reset error-related data to initial state
   * <p>
   * Clears all error data arrays and resets pagination parameters
   */
  const resetErrorData = () => {
    errCountList.value = [];
    sampleList.value = [];
    errParams.value = { pageNo: 1, pageSize: 200, filters: [] };
    errTotal.value = 0;
    errTimestamp.value = '';
    statusCodeData.value = {};
  };

  return {
    // State
    errCountList,
    sampleList,
    errParams,
    errTotal,
    errTimestamp,
    statusCodeData,

    // Methods
    loadErrorCount,
    getErrCountList,
    loadSampleErrorContent,
    loadStatusCodeData,
    setStatusCodeData,
    resetErrorData
  };
}
