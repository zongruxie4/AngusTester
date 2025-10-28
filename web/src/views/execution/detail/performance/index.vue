<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, ref } from 'vue';
import dayjs from 'dayjs';
import { ScriptType, SearchCriteria } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { useExecCount } from '../composables/useExecCount';

import { exec } from 'src/api/ctrl';
import { exec as testerExec } from '@/api/tester';

// Import types and composables
import { PerformanceDetailProps, PerformanceDetailEmits } from './types';
import { usePerformanceData } from './composables/usePerformanceData';
import { usePerformancePolling } from './composables/usePerformancePolling';
import { useErrorManagement } from './composables/useErrorManagement';
import { useExceptionHandling } from './composables/useExceptionHandling';

// Component Props and Emits
const props = withDefaults(defineProps<PerformanceDetailProps>(), {
  detail: undefined,
  exception: undefined
});

/**
 * Select performance detail component by plugin type or script type.
 * <p>
 * - MOCK_DATA -> mock performance detail
 * - Http -> HTTP performance detail
 * - Otherwise -> JDBC performance detail
 */
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
const PerformanceInfo = defineAsyncComponent(() =>
  props.detail?.scriptType?.value === ScriptType.MOCK_DATA
    ? import('@/views/execution/detail/performance/mock/index.vue')
    : props.detail?.plugin === 'Http'
      ? import('./Http.vue')
      : import('./Jdbc.vue')) as any;

const emit = defineEmits<PerformanceDetailEmits>();

// Component References
const httpExecDetailRef = ref();

// Performance Data Management
const {
  apiDimensionObj,
  indexDimensionObj,
  apiNames,
  timestampData,
  newList,
  perfListParams,
  perfListTotal,
  perfListLastTimestamp,
  errCountList,
  sampleList,
  errParams,
  errTotal,
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
  saveData,
  resetData
} = usePerformanceData();

// Error Management
const {
  loadErrorCount: loadErrorCountData,
  loadSampleErrorContent: loadSampleErrorContentData,
  loadStatusCodeData: loadStatusCodeDataData
} = useErrorManagement(statusCodeData);

// Exception Handling
const { setException: setExceptionInfo } = useExceptionHandling();

// Data Loading Methods
const { convertCvsValue } = useExecCount();

/**
 * Load execution info and refresh related metrics depending on current tab
 * <p>
 * Coordinates data loading based on execution status and current tab selection
 */
const loadInfo = async () => {
  const [error, { data }] = await exec.getInfo(props.detail?.id);
  if (error) {
    emit('update:loading', false);
    clearTimer();
    return;
  }

  emit('loaded', data);

  if (props.detail?.scriptType?.value !== ScriptType.MOCK_DATA) {
    if (['aggregation', 'throughput', 'vu', 'responseTime', 'analyze', 'error'].includes(countTabKey.value)) {
      if (isFirstUpdatePerfList.value) {
        const _filters = [
          {
            key: 'timestamp' as const,
            op: SearchCriteria.OpEnum.GreaterThanEqual,
            value: dayjs(perfListLastTimestamp.value).format(DATE_TIME_FORMAT)
          }
        ];
        await perfLoadList(1, hasFetchedOnce ? _filters : []);
        await computedPageLoadList(perfListParams.value, perfListTotal.value, perfLoadList);
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
  setException();
  updateData();
};

/**
 * Fetch paginated performance summary list
 * <p>
 * Loads performance data with pagination support and error handling
 */
const perfLoadList = async (_pageNo?: number, filters?: SearchCriteria[]) => {
  if (_pageNo) {
    perfListParams.value.pageNo = _pageNo;
  }

  if (filters?.length) {
    perfListParams.value.filters = filters;
  }

  const [error, { data = { list: [], total: 0 } }] = await testerExec.getSampleSummaryList(props.detail?.id, perfListParams.value);
  if (error) {
    emit('update:loading', false);
    clearTimer();
    return;
  }

  if (data.list?.length) {
    emit('update:loading', false);
  }

  perfListTotal.value = +data.total;
  saveData(data.list, convertCvsValue, pipelineKeys.value);
};

/**
 * Load latest error counters and compute error rates based on last sample summary
 * <p>
 * Fetches error counter data and calculates error rates for display
 */
const loadErrorCount = async () => {
  await loadErrorCountData(props.detail?.id, testerExec.getSampleErrorCounterLatest, newList.value, emit);
};

/**
 * Load paginated sample error contents incrementally using timestamp cursor
 * <p>
 * Fetches error sample data with pagination support for large datasets
 */
const loadSampleErrorContent = async (_pageNo?: number) => {
  await loadSampleErrorContentData(props.detail?.id, testerExec.getSampleErrContent, _pageNo, emit);
};

/**
 * Load latest HTTP status code counters (no history kept)
 * <p>
 * Fetches current status code distribution for HTTP requests
 */
const loadStatusCodeData = async () => {
  await loadStatusCodeDataData(props.detail?.id, testerExec.getSampleExtensionCountMapLatest, props.detail, emit);
};

/**
 * Load remaining pages (2..N) when not in running/scheduling status
 * <p>
 * Handles pagination for complete data loading when execution is finished
 */
// eslint-disable-next-line @typescript-eslint/ban-types
const computedPageLoadList = async (params: any, total: number, func: Function) => {
  const { pageSize } = params;
  if (total < pageSize) {
    return;
  }

  const totalPage = Math.ceil(total / pageSize);
  for (let i = 2; i <= totalPage; i++) {
    await func(i);
  }
};

/**
 * Set exception information from execution detail
 * <p>
 * Processes execution detail to extract meaningful error information
 */
const setException = () => {
  if (props.detail) {
    setExceptionInfo(props.detail);
  }
};

// Performance Polling
const {
  isLoaded,
  countTabKey,
  hasFetchedOnce,
  isFirstUpdatePerfList,
  isFirstUpdatePerfErrList,
  delayInSeconds,
  updateData,
  clearTimer,
  setCountTabKey,
  initializePolling,
  cleanupPolling
} = usePerformancePolling(
  props,
  emit,
  loadInfo,
  loadErrorCount,
  loadSampleErrorContent,
  loadStatusCodeData,
  computedPageLoadList,
  perfLoadList,
  perfListParams,
  perfListTotal,
  perfListLastTimestamp,
  errParams,
  errTotal,
  perfLoadList
);

/**
 * Pipeline keys computed from detail configuration
 * <p>
 * Extracts unique pipeline target mapping keys for data processing
 */
const pipelineKeys = computed(() => {
  const data = props.detail?.pipelineTargetMappings;
  if (!data) {
    return [];
  }

  const keys = Object.keys(data);
  if (!keys.length) {
    return [];
  }
  const _pipelineKeys: string[] = [];
  for (let i = 0; i < keys.length; i++) {
    const key = keys[i];
    if (!_pipelineKeys.includes(key)) {
      _pipelineKeys.push(key);
    }

    if (data[key]?.length) {
      for (let j = 0; j < data[key].length; j++) {
        const ckey = data[key][j];
        if (!_pipelineKeys.includes(ckey)) {
          _pipelineKeys.push(ckey);
        }
      }
    }
  }

  return _pipelineKeys;
});

/**
 * Initialize polling and start monitoring execution status
 */
const initialize = () => {
  initializePolling();
};

/**
 * Cleanup polling resources
 */
const cleanup = () => {
  cleanupPolling();
};

onBeforeUnmount(() => {
  cleanup();
});

initialize();

defineExpose({
  resetData,
  restartMock: () => httpExecDetailRef.value?.restart()
});
</script>
<template>
  <PerformanceInfo
    ref="httpExecDetailRef"
    type="detail"
    :dataSource="props.detail"
    :execInfo="props.detail"
    :delayInSeconds="delayInSeconds"
    :apiDimensionObj="apiDimensionObj"
    :indexDimensionObj="indexDimensionObj"
    :apiNames="apiNames"
    :timestampData="timestampData"
    :errCountList="errCountList"
    :sampleList="sampleList"
    :statusCodeData="statusCodeData"
    :isLoaded="isLoaded"
    :brpsUnit="brpsUnit"
    :minBrpsUnit="minBrpsUnit"
    :maxBrpsUnit="maxBrpsUnit"
    :meanBrpsUnit="meanBrpsUnit"
    :bwpsUnit="bwpsUnit"
    :minBwpsUnit="minBwpsUnit"
    :maxBwpsUnit="maxBwpsUnit"
    :meanBwpsUnit="meanBwpsUnit"
    :exception="props.exception"
    :infoMaxQps="infoMaxQps"
    :infoMaxTps="infoMaxTps"
    @setCountTabKey="setCountTabKey" />
</template>
<style scoped>
.header-tabs > :deep(.ant-tabs-content-holder) {
  @apply overflow-y-auto px-5;
}

.header-tabs > :deep(.ant-tabs-nav ){
  @apply h-10 pr-5 font-medium;

  background-image: linear-gradient(to bottom, rgb(255, 255, 255) 13%, rgb(245, 251, 255) 100%);
}

.header-tabs > :deep(.ant-tabs-nav > .ant-tabs-nav-wrap) {
  @apply pl-5;
}

.node-action-btn {
  @apply rounded mr-2 text-text-content text-3 border-0  px-2 shadow-none inline-flex items-center;
}

.node-action-btn :deep(span) {
  @apply ml-1;
}

.node-action-btn[disabled],
.node-action-btn:not([disabled]),
.node-action-btn:focus {
  @apply bg-transparent !important;
}

.node-action-btn[disabled] {
  @apply opacity-50;
}

.node-action-btn:not([disabled]):hover {
  @apply text-text-link;
}

.node-action-btn::after {
  @apply hidden;
}
</style>
