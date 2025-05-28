<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, ref, watch } from 'vue';
import dayjs from 'dayjs';

import { ListData, useExecCount } from '../useExecCount';
import { allCvsKeys } from '../ChartConfig';

import { Exception } from '../PropsType';
import { exec } from 'src/api/ctrl';
import { exec as testerExec } from '@/api/tester';

const PerformanceInfo = defineAsyncComponent(() => props.detail?.scriptType?.value === 'MOCK_DATA' ? import('@/views/execution/info/perf/mock/index.vue') : props.detail?.plugin === 'Http' ? import('./http.vue') : import('./jdbc.vue'));

interface Props {
  detail?:Record<string, any>;
  exception?:{ codeName: string; messageName: string; code: string; message: string;};
}

const props = withDefaults(defineProps<Props>(), {
  detail: undefined,
  exception: undefined
});

const emit = defineEmits<{(e: 'loaded', data: Record<string, any>): void;(e: 'update:loading', value:boolean): void;}>();

const httpExecDetailRef = ref();

let timer:NodeJS.Timeout | null = null;

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

  if (givenTime.isBefore(currentTime) || givenTime.isSame(currentTime)) {
    return false;
  } else {
    return true;
  }
});

const counTabKey = ref('aggregation');
const setCountTabKey = async (value:string) => {
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

  counTabKey.value = value;
};

let firstOpenTimer = false;
const updateData = () => {
  if (timer) {
    clearTimeout(timer);
  }

  timer = setTimeout(() => {
    if (props.detail && !['CREATED', 'PENDING', 'RUNNING'].includes(props.detail.status?.value) && !hasStartDate.value) {
      if (timer) {
        if (firstOpenTimer) {
          computedPageLoadList(perfListParams.value, perfListTotal.value, perfLoadList);
        }
        clearTimeout(timer);
      }
      return;
    }

    firstOpenTimer = true;
    loadInfo();
  }, delayInSeconds.value);
};

// 第一次加载是否完成
const isLoaded = ref(false);
// 是否第一次请求List
let isFirstLoadList = true;
// 是否第一次更新List
let isFirstUpdatePerfList = true;
// 是否第一次更新错误List
let isFirstUpdatePerfErrList = true;
const loadInfo = async () => {
  const [error, { data }] = await exec.getInfo(props.detail?.id);
  if (error) {
    emit('update:loading', false);
    clearTimer();
    return;
  }

  emit('loaded', data);

  if (props.detail?.scriptType?.value !== 'MOCK_DATA') {
    if (['aggregation', 'throughput', 'vu', 'responseTime', 'analyze', 'error'].includes(counTabKey.value)) {
      if (isFirstUpdatePerfList) {
        const _filters = [{ key: 'timestamp', op: 'GREATER_THAN_EQUAL', value: dayjs(perfListLastTimestamp.value).format('YYYY-MM-DD HH:mm:ss') }];
        await perfLoadList(1, firstOpenTimer ? _filters : []);
        computedPageLoadList(perfListParams.value, perfListTotal.value, perfLoadList);
        isFirstUpdatePerfList = false;
      } else {
        await perfLoadList(1);
      }

      if (counTabKey.value === 'error') {
        await loadErrorCount();
        if (isFirstUpdatePerfErrList) {
          await loadSampleErrorContent();
          computedPageLoadList(errParams.value, errTotal.value, loadSampleErrorContent);
          isFirstUpdatePerfErrList = false;
        } else {
          await loadSampleErrorContent(1);
        }
      }
    }

    if (counTabKey.value === 'httpCode' && props.detail?.plugin === 'Http') {
      loadStatusCodeData();
    }
  } else {
    emit('update:loading', false);
  }
  setException();
  updateData();
};

const perfListParams = ref<{pageNo: number, pageSize: number, filters:{key:'timestamp', op:'GREATER_THAN_EQUAL', value:string}[], nodeId?:string, }>({ pageNo: 1, pageSize: 500, filters: [] });
const perfListTotal = ref(0);
const perfListLastTimestamp = ref('');

// 接口维度展示对应指标
const apiDimensionObj = ref<{[key:string]:{
  duration: number[],
  errors: number[],
  iterations: number[],
  n: number[],
  operations: number[],
  transactions: number[],
  readBytes: number[],
  writeBytes: number[],
  ops: number[],
  minOps: number[],
  maxOps: number[],
  meanOps: number[],
  tps: number[],
  minTps: number[],
  maxTps: number[],
  meanTps: number[],
  brps: number[],
  minBrps: number[],
  maxBrps: number[],
  meanBrps: number[],
  bwps: number[],
  minBwps: number[],
  maxBwps: number[],
  meanBwps: number[],
  tranMean: number[],
  tranMin: number[],
  tranMax: number[],
  tranP50: number[],
  tranP75: number[],
  tranP90: number[],
  tranP95: number[],
  tranP99: number[],
  tranP999: number[],
  errorRate: number[],
  threadPoolSize: number[],
  threadPoolActiveSize: number[],
  threadMaxPoolSize: number[],
}}>({
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

// 指标维度对应接口
const indexDimensionObj = ref<{
  duration: {[key:string]:number[] },
  errors: {[key:string]:number[] },
  iterations: {[key:string]:number[] },
  n: {[key:string]:number[] },
  operations: {[key:string]:number[] },
  transactions: {[key:string]:number[] },
  readBytes: {[key:string]:number[] },
  writeBytes: {[key:string]:number[] },
  ops: {[key:string]:number[] },
  minOps: {[key:string]:number[] },
  maxOps: {[key:string]:number[] },
  meanOps: {[key:string]:number[] },
  tps: {[key:string]:number[] },
  minTps: {[key:string]:number[] },
  maxTps: {[key:string]:number[] },
  meanTps: {[key:string]:number[] },
  brps: {[key:string]:number[] },
  minBrps: {[key:string]:number[] },
  maxBrps: {[key:string]:number[] },
  meanBrps: {[key:string]:number[] },
  bwps: {[key:string]:number[] },
  minBwps: {[key:string]:number[] },
  maxBwps: {[key:string]:number[] },
  meanBwps: {[key:string]:number[] },
  tranMean: {[key:string]:number[] },
  tranMin: {[key:string]:number[] },
  tranMax: {[key:string]:number[] },
  tranP50: {[key:string]:number[] },
  tranP75: {[key:string]:number[] },
  tranP90: {[key:string]:number[] },
  tranP95: {[key:string]:number[] },
  tranP99: {[key:string]:number[] },
  tranP999: {[key:string]:number[] },
  errorRate: {[key:string]:number[] },
  threadPoolSize: {[key:string]:number[] },
  threadPoolActiveSize: {[key:string]:number[] },
  threadMaxPoolSize: {[key:string]:number[] }
}>({
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

const apiNames = ref<string[]>([]);
const timestampData = ref<string[]>([]);
const newList = ref<ListData[]>([]);
const allList = ref<ListData[]>([]);
const infoMaxQps = ref<string | number>('');
const infoMaxTps = ref<string | number>('');

const errCountList = ref<Record<string, any>[]>([]);

const brpsUnit = ref<'KB' | 'MB'>('KB');
const minBrpsUnit = ref<'KB' | 'MB'>('KB');
const maxBrpsUnit = ref<'KB' | 'MB'>('KB');
const meanBrpsUnit = ref<'KB' | 'MB'>('KB');
const bwpsUnit = ref<'KB' | 'MB'>('KB');
const minBwpsUnit = ref<'KB' | 'MB'>('KB');
const maxBwpsUnit = ref<'KB' | 'MB'>('KB');
const meanBwpsUnit = ref<'KB' | 'MB'>('KB');

const perfListData = ref<ListData[]>([]);
const { convertCvsValue } = useExecCount();

const pipelineKeys = computed(() => {
  const data = props.detail?.pipelineTargetMappings;
  if (!data) {
    return [];
  }

  const keys = Object.keys(data);
  if (!keys.length) {
    return [];
  }
  const _pipelineKeys = [];
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

const perfLoadList = async (_pageNo?:number, filters?:{ key: string; op: string; value: string; }[]) => {
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
  saveData(data.list);
};

const saveData = (_list:ListData[]) => {
  if (!_list?.length) {
    return;
  }

  let list = _list;
  let lastTimestamp = '';

  if (perfListData.value.length && firstOpenTimer) {
    lastTimestamp = perfListData.value[perfListData.value.length - 1].timestamp;
    list = _list.filter(item => dayjs(item.timestamp).startOf('millisecond').isAfter(dayjs(lastTimestamp).startOf('millisecond')) || dayjs(item.timestamp).startOf('millisecond').isSame(dayjs(lastTimestamp).startOf('millisecond')));
    if (perfListData.value[perfListData.value.length - 1]?.timestamp === list[0]?.timestamp) {
      perfListData.value.pop();
      timestampData.value.pop();
      for (const key in apiDimensionObj.value) {
        apiDimensionObj.value[key].duration.pop();
        apiDimensionObj.value[key].errors.pop();
        apiDimensionObj.value[key].iterations.pop();
        apiDimensionObj.value[key].n.pop();
        apiDimensionObj.value[key].operations.pop();
        apiDimensionObj.value[key].transactions.pop();
        apiDimensionObj.value[key].readBytes.pop();
        apiDimensionObj.value[key].writeBytes.pop();
        apiDimensionObj.value[key].ops.pop();
        apiDimensionObj.value[key].minOps.pop();
        apiDimensionObj.value[key].maxOps.pop();
        apiDimensionObj.value[key].meanOps.pop();
        apiDimensionObj.value[key].tps.pop();
        apiDimensionObj.value[key].minTps.pop();
        apiDimensionObj.value[key].maxTps.pop();
        apiDimensionObj.value[key].meanTps.pop();
        apiDimensionObj.value[key].brps.pop();
        apiDimensionObj.value[key].minBrps.pop();
        apiDimensionObj.value[key].maxBrps.pop();
        apiDimensionObj.value[key].meanBrps.pop();
        apiDimensionObj.value[key].bwps.pop();
        apiDimensionObj.value[key].minBwps.pop();
        apiDimensionObj.value[key].maxBwps.pop();
        apiDimensionObj.value[key].meanBwps.pop();
        apiDimensionObj.value[key].tranMean.pop();
        apiDimensionObj.value[key].tranMin.pop();
        apiDimensionObj.value[key].tranMax.pop();
        apiDimensionObj.value[key].tranP50.pop();
        apiDimensionObj.value[key].tranP75.pop();
        apiDimensionObj.value[key].tranP90.pop();
        apiDimensionObj.value[key].tranP95.pop();
        apiDimensionObj.value[key].tranP99.pop();
        apiDimensionObj.value[key].tranP999.pop();
        apiDimensionObj.value[key].errorRate.pop();
        apiDimensionObj.value[key].threadPoolSize.pop();
        apiDimensionObj.value[key].threadPoolActiveSize.pop();
        apiDimensionObj.value[key].threadMaxPoolSize.pop();
      }

      for (const key in indexDimensionObj.value) {
        const innerObj = indexDimensionObj.value[key];
        for (const innerKey in innerObj) {
          const innerArray = innerObj[innerKey];
          innerArray.pop();
        }
      }
    }
  }

  perfListData.value.push(...list);
  perfListLastTimestamp.value = perfListData.value[perfListData.value.length - 1].timestamp;

  newList.value = convertCvsValue(list, allCvsKeys, pipelineKeys.value);

  allList.value.push(...newList.value);
  const othersValue = computedOhtersValue();
  infoMaxQps.value = othersValue.maxOps;
  infoMaxTps.value = othersValue.maxTps;
  const times:string[] = [];
  for (let i = 0; i < newList.value.length; i++) {
    times.push(newList.value[i].timestamp);
    const values = newList.value[i].values;

    for (let j = 0; j < values.length; j++) {
      const value = values[j];
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

      let brpsInKB = 0;
      if (value.brps && +value.brps > 0) {
        if (brpsUnit.value === 'KB') {
          brpsInKB = +(+value.brps / 1024).toFixed(2);
          if (brpsInKB > 1000) {
            brpsInKB = +(+value.brps / 1024 / 1024).toFixed(2);
            brpsUnit.value = 'MB';
            if (indexDimensionObj.value.brps[name]?.length > 0) {
              indexDimensionObj.value.brps[name] = indexDimensionObj.value.brps[name].map(brpsData => +(brpsData / 1024).toFixed(2));
            }
          }
        } else {
          brpsInKB = +(+value.brps / 1024 / 1024).toFixed(2);
        }
      }

      let minBrpsInKB = 0;
      if (value.minBrps && +value.minBrps > 0) {
        if (minBrpsUnit.value === 'KB') {
          minBrpsInKB = +(+value.minBrps / 1024).toFixed(2);
          if (minBrpsInKB > 1000) {
            minBrpsInKB = +(+value.minBrps / 1024 / 1024).toFixed(2);
            minBrpsUnit.value = 'MB';
            if (indexDimensionObj.value.minBrps[name]?.length > 0) {
              indexDimensionObj.value.minBrps[name] = indexDimensionObj.value.minBrps[name].map(minBrpsData => +(minBrpsData / 1024).toFixed(2));
            }
          }
        } else {
          minBrpsInKB = +(+value.minBrps / 1024 / 1024).toFixed(2);
        }
      }

      let maxBrpsInKB = 0;
      if (value.maxBrps && +value.maxBrps > 0) {
        if (maxBrpsUnit.value === 'KB') {
          maxBrpsInKB = +(+value.maxBrps / 1024).toFixed(2);
          if (maxBrpsInKB > 1000) {
            maxBrpsInKB = +(+value.maxBrps / 1024 / 1024).toFixed(2);
            maxBrpsUnit.value = 'MB';
            if (indexDimensionObj.value.maxBrps[name]?.length > 0) {
              indexDimensionObj.value.maxBrps[name] = indexDimensionObj.value.maxBrps[name].map(maxBrpsData => +(maxBrpsData / 1024).toFixed(2));
            }
          }
        } else {
          maxBrpsInKB = +(+value.maxBrps / 1024 / 1024).toFixed(2);
        }
      }

      let meanBrpsInKB = 0;
      if (value.meanBrps && +value.meanBrps > 0) {
        if (meanBrpsUnit.value === 'KB') {
          meanBrpsInKB = +(+value.meanBrps / 1024).toFixed(2);
          if (meanBrpsInKB > 1000) {
            meanBrpsInKB = +(+value.meanBrps / 1024 / 1024).toFixed(2);
            meanBrpsUnit.value = 'MB';
            if (indexDimensionObj.value.meanBrps[name]?.length > 0) {
              indexDimensionObj.value.meanBrps[name] = indexDimensionObj.value.meanBrps[name].map(meanBrpsData => +(meanBrpsData / 1024).toFixed(2));
            }
          }
        } else {
          meanBrpsInKB = +(+value.meanBrps / 1024 / 1024).toFixed(2);
        }
      }

      let bwpsInKB = 0;
      if (value.bwps && +value.bwps > 0) {
        if (bwpsUnit.value === 'KB') {
          bwpsInKB = +(+value.bwps / 1024).toFixed(2);
          if (bwpsInKB > 1000) {
            bwpsInKB = +(+value.bwps / 1024 / 1024).toFixed(2);
            bwpsUnit.value = 'MB';
            if (indexDimensionObj.value.bwps[name]?.length > 0) {
              indexDimensionObj.value.bwps[name] = indexDimensionObj.value.bwps[name].map(bwpsData => +(bwpsData / 1024).toFixed(2));
            }
          }
        } else {
          bwpsInKB = +(+value.bwps / 1024 / 1024).toFixed(2);
        }
      }

      let minBwpsInKB = 0;
      if (value.minBwps && +value.minBwps > 0) {
        if (minBwpsUnit.value === 'KB') {
          minBwpsInKB = +(+value.minBwps / 1024).toFixed(2);
          if (minBwpsInKB > 1000) {
            minBwpsInKB = +(+value.minBwps / 1024 / 1024).toFixed(2);
            minBwpsUnit.value = 'MB';
            if (indexDimensionObj.value.minBwps[name]?.length > 0) {
              indexDimensionObj.value.minBwps[name] = indexDimensionObj.value.minBwps[name].map(minBwpsData => +(minBwpsData / 1024).toFixed(2));
            }
          }
        } else {
          minBwpsInKB = +(+value.minBwps / 1024 / 1024).toFixed(2);
        }
      }

      let maxBwpsInKB = 0;
      if (value.maxBwps && +value.maxBwps > 0) {
        if (maxBwpsUnit.value === 'KB') {
          maxBwpsInKB = +(+value.maxBwps / 1024).toFixed(2);
          if (maxBwpsInKB > 1000) {
            maxBwpsInKB = +(+value.maxBwps / 1024 / 1024).toFixed(2);
            maxBwpsUnit.value = 'MB';
            if (indexDimensionObj.value.maxBwps[name]?.length > 0) {
              indexDimensionObj.value.maxBwps[name] = indexDimensionObj.value.maxBwps[name].map(maxBwpsData => +(maxBwpsData / 1024).toFixed(2));
            }
          }
        } else {
          maxBwpsInKB = +(+value.maxBwps / 1024 / 1024).toFixed(2);
        }
      }

      let meanBwpsInKB = 0;
      if (value.meanBwps && +value.meanBwps > 0) {
        if (meanBwpsUnit.value === 'KB') {
          meanBwpsInKB = +(+value.meanBwps / 1024).toFixed(2);
          if (meanBwpsInKB > 1000) {
            meanBwpsInKB = +(+value.meanBwps / 1024 / 1024).toFixed(2);
            meanBwpsUnit.value = 'MB';
            if (indexDimensionObj.value.meanBwps[name]?.length > 0) {
              indexDimensionObj.value.meanBwps[name] = indexDimensionObj.value.meanBwps[name].map(meanBwpsData => +(meanBwpsData / 1024).toFixed(2));
            }
          }
        } else {
          meanBwpsInKB = +(+value.meanBwps / 1024 / 1024).toFixed(2);
        }
      }

      if (!(name in apiDimensionObj.value)) {
        apiDimensionObj.value[name] = {
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
        };
      }

      if (!(name in indexDimensionObj.value.duration)) {
        indexDimensionObj.value.duration[name] = [];
        indexDimensionObj.value.errors[name] = [];
        indexDimensionObj.value.iterations[name] = [];
        indexDimensionObj.value.n[name] = [];
        indexDimensionObj.value.operations[name] = [];
        indexDimensionObj.value.transactions[name] = [];
        indexDimensionObj.value.readBytes[name] = [];
        indexDimensionObj.value.writeBytes[name] = [];
        indexDimensionObj.value.ops[name] = [];
        indexDimensionObj.value.minOps[name] = [];
        indexDimensionObj.value.maxOps[name] = [];
        indexDimensionObj.value.meanOps[name] = [];
        indexDimensionObj.value.tps[name] = [];
        indexDimensionObj.value.minTps[name] = [];
        indexDimensionObj.value.maxTps[name] = [];
        indexDimensionObj.value.meanTps[name] = [];
        indexDimensionObj.value.brps[name] = [];
        indexDimensionObj.value.minBrps[name] = [];
        indexDimensionObj.value.maxBrps[name] = [];
        indexDimensionObj.value.meanBrps[name] = [];
        indexDimensionObj.value.bwps[name] = [];
        indexDimensionObj.value.minBwps[name] = [];
        indexDimensionObj.value.maxBwps[name] = [];
        indexDimensionObj.value.meanBwps[name] = [];
        indexDimensionObj.value.tranMean[name] = [];
        indexDimensionObj.value.tranMin[name] = [];
        indexDimensionObj.value.tranMax[name] = [];
        indexDimensionObj.value.tranP50[name] = [];
        indexDimensionObj.value.tranP75[name] = [];
        indexDimensionObj.value.tranP90[name] = [];
        indexDimensionObj.value.tranP95[name] = [];
        indexDimensionObj.value.tranP99[name] = [];
        indexDimensionObj.value.tranP999[name] = [];
        indexDimensionObj.value.errorRate[name] = [];
        indexDimensionObj.value.threadPoolSize[name] = [];
        indexDimensionObj.value.threadPoolActiveSize[name] = [];
        indexDimensionObj.value.threadMaxPoolSize[name] = [];
      }

      apiDimensionObj.value[name].duration.push(+value.duration || 0);
      apiDimensionObj.value[name].errors.push(+value.errors || 0);
      apiDimensionObj.value[name].iterations.push(+value.iterations || 0);
      apiDimensionObj.value[name].n.push(+value.n || 0);
      apiDimensionObj.value[name].operations.push(+value.operations || 0);
      apiDimensionObj.value[name].transactions.push(+value.transactions || 0);
      apiDimensionObj.value[name].readBytes.push(+value.readBytes || 0);
      apiDimensionObj.value[name].writeBytes.push(+value.tranMewriteBytesan || 0);
      apiDimensionObj.value[name].ops.push(+value.ops || 0);
      apiDimensionObj.value[name].minOps.push(+value.minOps || 0);
      apiDimensionObj.value[name].maxOps.push(+value.maxOps || 0);
      apiDimensionObj.value[name].meanOps.push(+value.meanOps || 0);
      apiDimensionObj.value[name].tps.push(+value.tps || 0);
      apiDimensionObj.value[name].minTps.push(+value.minTps || 0);
      apiDimensionObj.value[name].maxTps.push(+value.maxTps || 0);
      apiDimensionObj.value[name].meanTps.push(+value.meanTps || 0);
      apiDimensionObj.value[name].brps.push(brpsInKB);
      apiDimensionObj.value[name].minBrps.push(minBrpsInKB);
      apiDimensionObj.value[name].maxBrps.push(maxBrpsInKB);
      apiDimensionObj.value[name].meanBrps.push(meanBrpsInKB);
      apiDimensionObj.value[name].bwps.push(bwpsInKB);
      apiDimensionObj.value[name].minBwps.push(minBwpsInKB);
      apiDimensionObj.value[name].maxBwps.push(maxBwpsInKB);
      apiDimensionObj.value[name].meanBwps.push(meanBwpsInKB);
      apiDimensionObj.value[name].tranMean.push(+value.tranMean || 0);
      apiDimensionObj.value[name].tranMin.push(+value.tranMin || 0);
      apiDimensionObj.value[name].tranMax.push(+value.tranMax || 0);
      apiDimensionObj.value[name].tranP50.push(+value.tranP50 || 0);
      apiDimensionObj.value[name].tranP75.push(+value.tranP75 || 0);
      apiDimensionObj.value[name].tranP90.push(+value.tranP90 || 0);
      apiDimensionObj.value[name].tranP95.push(+value.tranP95 || 0);
      apiDimensionObj.value[name].tranP99.push(+value.tranP99 || 0);
      apiDimensionObj.value[name].tranP999.push(+value.tranP999 || 0);
      apiDimensionObj.value[name].errorRate.push(+value.errorRate || 0);
      apiDimensionObj.value[name].threadPoolSize.push(+value.threadPoolSize || 0);
      apiDimensionObj.value[name].threadPoolActiveSize.push(+value.threadPoolActiveSize || 0);
      apiDimensionObj.value[name].threadMaxPoolSize.push(+value.threadMaxPoolSize || 0);
      indexDimensionObj.value.duration[name].push(+value.duration || 0);
      indexDimensionObj.value.errors[name].push(+value.errors || 0);
      indexDimensionObj.value.iterations[name].push(+value.iterations || 0);
      indexDimensionObj.value.n[name].push(+value.n || 0);
      indexDimensionObj.value.operations[name].push(+value.operations || 0);
      indexDimensionObj.value.transactions[name].push(+value.transactions || 0);
      indexDimensionObj.value.readBytes[name].push(+value.readBytes || 0);
      indexDimensionObj.value.writeBytes[name].push(+value.tranMewriteBytesan || 0);
      indexDimensionObj.value.ops[name].push(+value.ops || 0);
      indexDimensionObj.value.minOps[name].push(+value.minOps || 0);
      indexDimensionObj.value.maxOps[name].push(+value.maxOps || 0);
      indexDimensionObj.value.meanOps[name].push(+value.meanOps || 0);
      indexDimensionObj.value.tps[name].push(+value.tps || 0);
      indexDimensionObj.value.minTps[name].push(+value.minTps || 0);
      indexDimensionObj.value.maxTps[name].push(+value.maxTps || 0);
      indexDimensionObj.value.meanTps[name].push(+value.meanTps || 0);
      indexDimensionObj.value.brps[name].push(brpsInKB);
      indexDimensionObj.value.minBrps[name].push(minBrpsInKB);
      indexDimensionObj.value.maxBrps[name].push(maxBrpsInKB);
      indexDimensionObj.value.meanBrps[name].push(meanBrpsInKB);
      indexDimensionObj.value.bwps[name].push(bwpsInKB);
      indexDimensionObj.value.minBwps[name].push(minBwpsInKB);
      indexDimensionObj.value.maxBwps[name].push(maxBwpsInKB);
      indexDimensionObj.value.meanBwps[name].push(meanBwpsInKB);
      indexDimensionObj.value.tranMean[name].push(+value.tranMean || 0);
      indexDimensionObj.value.tranMin[name].push(+value.tranMin || 0);
      indexDimensionObj.value.tranMax[name].push(+value.tranMax || 0);
      indexDimensionObj.value.tranP50[name].push(+value.tranP50 || 0);
      indexDimensionObj.value.tranP75[name].push(+value.tranP75 || 0);
      indexDimensionObj.value.tranP90[name].push(+value.tranP90 || 0);
      indexDimensionObj.value.tranP95[name].push(+value.tranP95 || 0);
      indexDimensionObj.value.tranP99[name].push(+value.tranP99 || 0);
      indexDimensionObj.value.tranP999[name].push(+value.tranP999 || 0);
      indexDimensionObj.value.errorRate[name].push(+value.errorRate || 0);
      indexDimensionObj.value.threadPoolSize[name].push(+value.threadPoolSize || 0);
      indexDimensionObj.value.threadPoolActiveSize[name].push(+value.threadPoolActiveSize || 0);
      indexDimensionObj.value.threadMaxPoolSize[name].push(+value.threadMaxPoolSize || 0);
    }
  }
  timestampData.value = [...timestampData.value, ...times].map(item => {
    return dayjs(item).format('YYYY-MM-DD HH:mm:ss');
  });

  if (isFirstLoadList) {
    apiNames.value = apiNames.value.sort((a, b) => {
      if (a.includes('Total') && !b.includes('Total')) {
        return -1; // a在b前面
      } else if (!a.includes('Total') && !b.includes('Total')) {
        return 1; // a在b后面
      } else {
        return 0; // 保持原有顺序
      }
    });
    isFirstLoadList = false;
  }
};

// 计算其他值
const computedOhtersValue = () => {
  let maxOps = 0;
  let minOps = 0;
  let meanOps = 0;
  let maxTps = 0;
  let minTps = 0;
  let meanTps = 0;
  let maxBrps = 0;
  let minBrps = 0;
  let meanBrps = 0;
  let maxBwps = 0;
  let minBwps = 0;
  let meanBwps = 0;

  for (let i = 0; i < allList.value.length; i++) {
    const totalItem = allList.value[i].values.find(f => f.name === 'Total');

    // 获取每一项中的 ops、tps、brps 和 bwps 值
    const ops = totalItem?.ops;
    const tps = totalItem?.tps;
    const brps = totalItem?.brps;
    const bwps = totalItem?.bwps;

    // 将 ops、tps、brps 和 bwps 值转换为数字
    let numOps = Number(ops);
    let numTps = Number(tps);
    let numBrps = Number(brps);
    let numBwps = Number(bwps);

    // 如果转换失败，则将值设置为 0
    if (isNaN(numOps)) {
      numOps = 0;
    }
    if (isNaN(numTps)) {
      numTps = 0;
    }
    if (isNaN(numBrps)) {
      numBrps = 0;
    }
    if (isNaN(numBwps)) {
      numBwps = 0;
    }

    if (i === 0) {
      minOps = numOps;
      minTps = numTps;
      minBrps = numBrps;
      minBwps = numBwps;
    }

    // 更新最大值、最小值和平均值
    maxOps = Math.max(maxOps, numOps);
    minOps = Math.min(minOps, numOps);

    meanOps += numOps;
    maxTps = Math.max(maxTps, numTps);
    minTps = Math.min(minTps, numTps);
    meanTps += numTps;
    maxBrps = Math.max(maxBrps, numBrps);
    minBrps = Math.min(minBrps, numBrps);
    meanBrps += numBrps;
    maxBwps = Math.max(maxBwps, numBwps);
    minBwps = Math.min(minBwps, numBwps);
    meanBwps += numBwps;
  }

  // 计算平均值
  meanOps /= allList.value.length;
  meanTps /= allList.value.length;
  meanBrps /= allList.value.length;
  meanBwps /= allList.value.length;
  meanOps = +meanOps.toFixed(2);
  meanTps = +meanTps.toFixed(2);
  meanBrps = +meanBrps.toFixed(2);
  meanBwps = +meanBwps.toFixed(2);
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

const loadErrorCount = async () => {
  const [counterErr, counterRes] = await testerExec.getSampleErrorCounterLatest(props.detail?.id);
  if (counterErr) {
    emit('update:loading', false);
    clearTimer();
    return;
  }

  if (counterRes?.data) {
    emit('update:loading', false);
  }

  const errorCounterData = counterRes.data;
  errCountList.value = getErrCountList(errorCounterData, newList.value[newList.value.length - 1]);
};

const getErrCountList = (counterData, lastData) => {
  const result:Record<string, any>[] = [];
  for (const key in counterData) {
    const counter = counterData[key];
    let errorNum = 0;
    for (const prop in counter) {
      const value = parseInt(counter[prop]);
      if (!isNaN(value)) {
        errorNum += value;
      }
    }
    const matchingValue = lastData.values.find((value) => value.name === key);
    let errorRate = '0';
    if (matchingValue && !isNaN(errorNum) && !isNaN(matchingValue.n)) {
      if (typeof matchingValue.n === 'number' && matchingValue.n !== 0) {
        errorRate = (errorNum / matchingValue.n * 100).toFixed(2) + '%';
      } else {
        errorRate = '0%';
      }
    }
    const list:Record<string, any>[] = [];
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
  const totalElement = result.filter((element) => element.name === 'Total');
  const otherElements = result.filter((element) => element.name !== 'Total');
  const finalResult = [...otherElements, ...totalElement];
  return finalResult;
};

const sampleList = ref<Record<string, any>[]>([]);
const errParams = ref<{pageNo: number, pageSize: number, filters:{key:'timestamp', op:'GREATER_THAN_EQUAL', value:string}[], nodeId?:string, }>({ pageNo: 1, pageSize: 200, filters: [] });
const errTotal = ref(0);
const errTimestamp = ref('');
// 请求采样错误
const loadSampleErrorContent = async (_pageNo?:number) => {
  if (_pageNo) {
    errParams.value.pageNo = _pageNo;
  }

  if (errTimestamp.value) {
    errParams.value.filters = [{ key: 'timestamp', op: 'GREATER_THAN_EQUAL', value: dayjs(errTimestamp.value).format('YYYY-MM-DD HH:mm:ss') }];
  }

  const [error, { data = { list: [], total: 0 } }] = await testerExec.getSampleErrContent(props.detail?.id, errParams.value);
  if (error) {
    emit('update:loading', false);
    clearTimer();
    return;
  }

  if (data?.list?.length) {
    emit('update:loading', false);
  }

  errTotal.value = +data.total;
  if (data.list.length) {
    errTimestamp.value = data.list[data.list.length - 1].timestamp;
    sampleList.value = [...sampleList.value, ...data.list];
  }
};

const stutasCodeData = ref({});
// 请求状态码 状态码不展示历史
const loadStatusCodeData = async () => {
  const [error, { data }] = await testerExec.getSampleExtensionCountMapLatest(props.detail?.id);
  if (error) {
    emit('update:loading', false);
    clearTimer();
    return;
  }

  if (data) {
    emit('update:loading', false);
  }

  setStutasCodeData(data, props.detail.pipelineTargetMappings);
};

const setStutasCodeData = (data: Record<string, any>, pipeline: Record<string, any>) => {
  if (props.detail?.isSingleInterface) {
    if (!data?.Total) {
      stutasCodeData.value = {
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
    stutasCodeData.value = {
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

  stutasCodeData.value = {};

  const processData = (obj) => {
    for (const [key, value] of Object.entries(obj)) {
      stutasCodeData.value[key] = {
        '2xx': value?.['2xx'] ? parseInt(value['2xx']) : 0,
        '3xx': value?.['3xx'] ? parseInt(value['3xx']) : 0,
        '4xx': value?.['4xx'] ? parseInt(value['4xx']) : 0,
        '5xx': value?.['5xx'] ? parseInt(value['5xx']) : 0,
        Exception: value?.Exception ? parseInt(value.Exception) : 0
      };
    }
  };

  if (!pipeline || !Object.keys(pipeline).length || !data || !Object.keys(data).length) {
    stutasCodeData.value = {
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
    stutasCodeData.value[key] = {
      '2xx': data[key]?.['2xx'] ? parseInt(data[key]?.['2xx']) : 0,
      '3xx': data[key]?.['3xx'] ? parseInt(data[key]?.['3xx']) : 0,
      '4xx': data[key]?.['4xx'] ? parseInt(data[key]?.['4xx']) : 0,
      '5xx': data[key]?.['5xx'] ? parseInt(data[key]?.['5xx']) : 0,
      Exception: data[key]?.Exception ? parseInt(data[key]?.Exception) : 0
    };

    if (pipeline[key]?.length) {
      for (let i = 0; i < pipeline[key].length; i++) {
        const cKey = pipeline[key][i];
        stutasCodeData.value[cKey] = {
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

watch(() => props.detail, async (newValue) => {
  if (!newValue) {
    isLoaded.value = true;
    return;
  }

  if (['CREATED', 'PENDING', 'RUNNING'].includes(newValue?.status?.value) && !hasStartDate.value) {
    // 执行和调度中 更新数据
    emit('update:loading', true);
    isLoaded.value = false;
    await loadInfo();
    isLoaded.value = true;
    if (typeof httpExecDetailRef.value?.restartNode === 'function') {
      httpExecDetailRef.value?.restartNode();
    }
    return;
  }

  // 非执行中
  await perfLoadList();
  emit('update:loading', false);
  isLoaded.value = true;
  computedPageLoadList(perfListParams.value, perfListTotal.value, perfLoadList);
}, {
  immediate: true
});

/**
 * 非执行和调度状态下，拉取第二页到最后一条的数据
 * @param params 执行函数的参数
 * @param func 需要执行的函数
 */
const computedPageLoadList = async (params, total, func) => {
  const { pageSize } = params;
  if (total < pageSize) {
    return;
  }

  const totalPage = Math.ceil(total / pageSize);
  for (let i = 2; i <= totalPage; i++) {
    await func(i);
  }
};

const resetData = () => {
  apiDimensionObj.value = {
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
  perfListParams.value.pageNo = 1;
  perfListParams.value.filters = [];
  perfListTotal.value = 0;
  perfListLastTimestamp.value = '';
  perfListData.value = [];
  apiNames.value = [];
  timestampData.value = [];
  newList.value = [];
  errCountList.value = [];
  brpsUnit.value = 'KB';
  bwpsUnit.value = 'KB';
  errParams.value.pageNo = 1;
  errParams.value.filters = [];
  errTotal.value = 0;
  errTimestamp.value = '';
  sampleList.value = [];
  stutasCodeData.value = undefined;
  firstOpenTimer = false;
  clearTimer();
};

onBeforeUnmount(() => {
  clearTimer();
});

const clearTimer = () => {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
};

const exception = ref<Exception>();

const setException = () => {
  const lastSchedulingResult = props.detail?.lastSchedulingResult;
  const meterMessage = props.detail?.meterMessage;
  if (lastSchedulingResult?.length) {
    const foundItem = lastSchedulingResult.find(f => !f.success);
    if (foundItem) {
      exception.value = { code: foundItem.exitCode, message: foundItem.message, codeName: '退出码', messageName: '失败原因' };
      return;
    }

    if (meterMessage) {
      exception.value = { code: props.detail?.meterStatus || '', message: meterMessage, codeName: '采样状态', messageName: '失败原因' };
      return;
    }

    return;
  }

  if (meterMessage) {
    exception.value = { code: props.detail?.meterStatus || '', message: meterMessage, codeName: '采样状态', messageName: '失败原因' };
    return;
  }

  exception.value = undefined;
};

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
    :stutasCodeData="stutasCodeData"
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
