import { ref, computed, watch, onBeforeUnmount } from 'vue';
import dayjs from 'dayjs';
import { ExecStatus } from '@/enums/enums';
import { PerformanceDetailProps, PerformanceDetailEmits } from '../types';
import { exec } from '@/api/ctrl';

/**
 * Composable for managing performance data polling and execution state
 * <p>
 * Handles polling logic, execution status monitoring, and data loading coordination
 * based on execution status and scheduling configuration.
 */
export function usePerformancePolling (
  props: PerformanceDetailProps,
  emit: PerformanceDetailEmits,
  loadInfo: () => Promise<void>,
  loadErrorCount: () => Promise<void>,
  loadSampleErrorContent: (pageNo?: number) => Promise<void>,
  loadStatusCodeData: () => Promise<void>,
  // eslint-disable-next-line @typescript-eslint/ban-types
  computedPageLoadList: (params: any, total: number, func: Function) => Promise<void>,
  perfLoadList: (pageNo?: number, filters?: any[]) => Promise<void>,
  perfListParams: any,
  perfListTotal: any,
  perfListLastTimestamp: any,
  errParams: any,
  errTotal: any,
  // eslint-disable-next-line @typescript-eslint/ban-types
  perfLoadListFunc: Function
) {
  // ==================== State Management ====================

  /**
   * Whether the first data pull has completed at least once
   */
  let hasFetchedOnce = false;

  /**
   * Whether the first full load has completed
   */
  const isLoaded = ref(false);

  /**
   * Whether it's the first fetch for the list
   */
  const isFirstLoadList = ref(true);

  /**
   * Whether it's the first incremental update for the list
   */
  const isFirstUpdatePerfList = ref(true);

  /**
   * Whether it's the first incremental update for the error list
   */
  const isFirstUpdatePerfErrList = ref(true);

  /**
   * Current active tab key for metrics display
   */
  const countTabKey = ref('aggregation');

  /**
   * Polling timer reference
   */
  let timer: NodeJS.Timeout | null = null;

  // ==================== Computed Properties ====================

  /**
   * Compute polling delay (ms) based on reportInterval with a minimum of 3 seconds
   * <p>
   * Ensures polling frequency respects configuration while maintaining minimum responsiveness
   */
  const delayInSeconds = computed(() => {
    let seconds = 3;
    if (!props.detail) {
      return seconds * 1000;
    }
    const { reportInterval } = props.detail;
    if (reportInterval) {
      seconds = parseInt(reportInterval) < seconds ? 3 : parseInt(reportInterval);
    }
    return seconds * 1000;
  });

  /**
   * Whether a future start time is configured
   * <p>
   * If true, we should not poll yet as execution is scheduled for the future
   */
  const hasStartDate = computed(() => {
    if (!props.detail) {
      return false;
    }
    const { configuration } = props.detail;
    if (!configuration) {
      return false;
    }
    const { startAtDate } = configuration;
    if (!startAtDate) {
      return false;
    }

    const givenTime = dayjs(startAtDate);
    const currentTime = dayjs();

    return !(givenTime.isBefore(currentTime) || givenTime.isSame(currentTime));
  });

  // ==================== Polling Methods ====================

  /**
   * Schedule next data update with the computed delay, or stop when execution finished
   * <p>
   * Manages polling lifecycle based on execution status and start date configuration
   */
  const updateData = () => {
    if (timer) {
      clearTimeout(timer);
    }

    timer = setTimeout(() => {
      if (props.detail && ![ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(props.detail.status?.value) && !hasStartDate.value) {
        if (timer) {
          if (hasFetchedOnce) {
            computedPageLoadList(perfListParams.value, perfListTotal.value, perfLoadListFunc);
          }
          clearTimeout(timer);
        }
        return;
      }

      hasFetchedOnce = true;
      loadInfo();
    }, delayInSeconds.value);
  };

  /**
   * Safely clear the polling timer
   * <p>
   * Prevents memory leaks and ensures clean timer cleanup
   */
  const clearTimer = () => {
    if (timer) {
      clearTimeout(timer);
      timer = null;
    }
  };

  // ==================== Tab Management ====================

  /**
   * Handle switching of metric tabs and lazy-load corresponding data when needed
   * <p>
   * Loads specific data sets based on selected tab to optimize performance
   */
  const setCountTabKey = async (value: string) => {
    if (value === 'error') {
      isLoaded.value = false;
      await loadErrorCount();
      await loadSampleErrorContent();
      isLoaded.value = true;
    }

    if (value === 'httpCode') {
      isLoaded.value = false;
      await loadStatusCodeData();
      isLoaded.value = true;
    }

    countTabKey.value = value;
  };

  // ==================== Data Loading Coordination ====================

  /**
   * Load execution info and refresh related metrics depending on current tab
   * <p>
   * Coordinates data loading based on execution status and current tab selection
   */
  const loadInfoWithTabHandling = async () => {
    const [error, { data }] = await exec.getInfo(props.detail?.id);
    if (error) {
      emit('update:loading', false);
      clearTimer();
      return;
    }

    emit('loaded', data);

    if (props.detail?.scriptType?.value !== 'MOCK_DATA') {
      if (['aggregation', 'throughput', 'vu', 'responseTime', 'analyze', 'error'].includes(countTabKey.value)) {
        if (isFirstUpdatePerfList.value) {
          const _filters = [{ key: 'timestamp', op: 'GREATER_THAN_EQUAL', value: dayjs(perfListLastTimestamp.value).format('YYYY-MM-DD HH:mm:ss') }];
          await perfLoadList(1, hasFetchedOnce ? _filters : []);
          await computedPageLoadList(perfListParams.value, perfListTotal.value, perfLoadListFunc);
          isFirstUpdatePerfList.value = false;
        } else {
          await perfLoadList(1);
        }

        if (countTabKey.value === 'error') {
          await loadErrorCount();
          if (isFirstUpdatePerfErrList.value) {
            await loadSampleErrorContent();
            await computedPageLoadList(errParams.value, errTotal.value, loadSampleErrorContent);
            isFirstUpdatePerfErrList.value = false;
          } else {
            await loadSampleErrorContent(1);
          }
        }
      }

      if (countTabKey.value === 'httpCode' && props.detail?.plugin === 'Http') {
        await loadStatusCodeData();
      }
    } else {
      emit('update:loading', false);
    }
    updateData();
  };

  // ==================== Execution Status Monitoring ====================

  /**
   * React to execution detail changes
   * <p>
   * - When running/scheduled: start polling and live updates
   * - Otherwise: load all pages once and stop
   */
  const watchExecutionDetail = () => {
    watch(() => props.detail, async (newValue) => {
      if (!newValue) {
        isLoaded.value = true;
        return;
      }

      if ([ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(newValue?.status?.value) && !hasStartDate.value) {
        // When executing or scheduling, keep updating data
        emit('update:loading', true);
        isLoaded.value = false;
        await loadInfo();
        isLoaded.value = true;
        return;
      }

      // Not executing: load once and paginate to the end
      await perfLoadList();
      emit('update:loading', false);
      isLoaded.value = true;
      computedPageLoadList(perfListParams.value, perfListTotal.value, perfLoadListFunc);
    }, {
      immediate: true
    });
  };

  // ==================== Lifecycle Management ====================

  /**
   * Initialize polling and start monitoring execution status
   */
  const initializePolling = () => {
    watchExecutionDetail();
  };

  /**
   * Cleanup polling resources
   */
  const cleanupPolling = () => {
    clearTimer();
  };

  // ==================== Lifecycle Hooks ====================

  onBeforeUnmount(() => {
    cleanupPolling();
  });

  return {
    // State
    isLoaded,
    countTabKey,
    hasFetchedOnce,
    isFirstLoadList,
    isFirstUpdatePerfList,
    isFirstUpdatePerfErrList,
    delayInSeconds,
    hasStartDate,

    // Methods
    updateData,
    clearTimer,
    setCountTabKey,
    loadInfoWithTabHandling,
    initializePolling,
    cleanupPolling
  };
}
