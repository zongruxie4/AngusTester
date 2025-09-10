<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';
import { chartSeriesColorConfig } from '@/views/report/preview/PropsType';
import { nodeCtrl } from '@/api/ctrl';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

const { t } = useI18n();

interface NodeItem {
  agentPort: string; domain: string; id: string; ip: string; name: string
}

interface Props {
  execNodes: NodeItem[]; // 执行节点选项
  appNodes: NodeItem[]; // 应用节点选项
  startTime: string; // 开始时间
  endTime: string; // 结束时间
  reportInterval: string; // 采集数据间隔时间
  status: string;
  delayInSeconds: number; // 执行过程中的调接口间隔时间
  activeChart: string
}

const props = withDefaults(defineProps<Props>(), {
  execNodes: () => ([]),
  appNodes: () => ([]),
  startTime: '',
  endTime: '',
  delayInSeconds: 3000
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'loadingChange', value: boolean): void;
}>();

let myEcahrt;
const tableData = ref<{name: string, unit: string, average: string, high: string, low: string, latest: string}[]>([]);
const notMerge = ref(true); // 图表数据覆盖是否重新渲染
// echarts 图表配置
const echartsOpt = {
  tooltip: {
    trigger: 'axis',
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    itemHeight: 10,
    itemWidth: 8,
    itemGap: 8,
    padding: [5, 10, 0, 5],
    data: []
  },
  grid: {
    left: 45,
    right: 62,
    top: 60,
    bottom: 0,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [],
    axisTick: {
      show: false
    }
  },
  yAxis: {
    type: 'value',
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed'
      }
    }
  },
  series: []
};

const echartRef = ref();
const initEcahrts = () => {
  myEcahrt = echarts.init(echartRef.value);
  myEcahrt.setOption(echartsOpt);
};

const getDefaultLineConfig = (idx = 0) => {
  return {
    data: [] as number[],
    type: 'line',
    smooth: true,
    showSymbol: false,
    lineStyle: {
      width: 1
    },
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },
        { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }
      ])
    },
    areaStyle: {}
  };
};

const activeTab = ref('cpu');

const nodeType = ref('exec');
const currrentNodeId = ref();

const nodeList = computed(() => {
  if (nodeType.value === 'apply') {
    return props.appNodes;
  }
  return props.execNodes;
});

watch(() => nodeList.value, () => {
  currrentNodeId.value = nodeList.value[0].id;
});

const sliderValueCpu = ref<number[]>([]); // 时间滑块的值
let sliderMaxCpu: number|undefined;
const timesCpu = ref<string[]>([]);
const timesMemory = ref<string[]>([]);
const timesDisk = ref<string[]>([]);
const timesNetwork = ref<string[]>([]);

const times = computed(() => {
  if (activeTab.value === 'cpu') {
    return timesCpu.value;
  }
  if (activeTab.value === 'memory') {
    return timesMemory.value;
  }
  if (activeTab.value === 'disk') {
    return timesDisk.value;
  }
  if (activeTab.value === 'network') {
    return timesNetwork.value;
  }
  return [];
});

const setSliderConfig = (reset = false) => {
  if (activeTab.value === 'cpu') {
    if (!cpuChartData.length) {
      return;
    }
    if (!sliderValueCpu.value.length) {
      timesCpu.value = cpuChartData.map(i => i.timestamp);
      sliderValueCpu.value = [0, timesCpu.value.length - 1];
      sliderMaxCpu = timesCpu.value.length - 1;
    } else {
      timesCpu.value = cpuChartData.map(i => i.timestamp);
      if (!props.endTime || reset) {
        if (sliderValueCpu.value[1] === sliderMaxCpu) {
          sliderValueCpu.value = [0, timesCpu.value.length - 1];
        }
      }
      sliderMaxCpu = timesCpu.value.length - 1;
    }
  }
};

const pagination = { // 数据分页获取
  pageSize: 1000,
  pageNo: 1
};

const getChartParam = (params = {}) => {
  const startTime = dayjs(props.startTime).format(DATE_TIME_FORMAT);
  const [interval, unit] = splitDuration(props.reportInterval);
  const endTime = props.endTime
    ? dayjs(props.endTime).add(+interval, unit).format(DATE_TIME_FORMAT)
    : dayjs(props.startTime).add(+interval, unit).add(1, 'day').format(DATE_TIME_FORMAT);
  const filters = [
    { key: 'timestamp', op: 'GREATER_THAN_EQUAL', value: startTime },
    { key: 'timestamp', op: 'LESS_THAN_EQUAL', value: endTime },
    ...(params?.filters || [])
  ];
  return {
    pageNo: pagination.pageNo,
    pageSize: pagination.pageSize,
    filters
  };
};

let cpuChartData = [];
const cpuloaded = ref(false);
// 获取图表数据 CPU
const loadCpuEchartData = async () => {
  emit('loadingChange', true);
  const param = getChartParam();
  const [error, res] = await nodeCtrl.getCpuMetrics(currrentNodeId.value, param);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    cpuChartData = res.data?.list || [];
  } else {
    cpuChartData = cpuChartData.concat(res.data?.list || []);
  }
  if (cpuChartData.length < res.data.total) {
    pagination.pageNo += 1;
    loadCpuEchartData();
  }
  if (cpuChartData.length > 2 && cpuChartData[cpuChartData.length - 1].timestamp === cpuChartData[cpuChartData.length - 2].timestamp) {
    cpuChartData.splice(cpuChartData.length - 2, 1);
  }
  cpuloaded.value = true;

  setSliderConfig(true);
  setCpuChartData();
};

// 设置CPU图表数据
const setCpuChartData = () => {
  const showChartData = cpuChartData.filter((_i, idx) => {
    return idx >= sliderValueCpu.value[0] && idx <= sliderValueCpu.value[1];
  });
  // 'CPU 空闲百分比', '系统空间占用 CPU 百分比', '用户空间占 CPU 百分比', '等待 IO 操作的 CPU 百分比', '其他占用 CPU 百分比', '当前占用的总 CPU 百分比'
  const dataType = [t('reportPreview.execPerf.sampling.testDetail.nodeResource.cpuIdle'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.cpuSystem'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.cpuUser'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.cpuIo'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.cpuOther'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.cpuTotal')];
  const seriesData = dataType.map((type, idx) => {
    return {
      ...getDefaultLineConfig(idx),
      name: type
    };
  });

  showChartData.forEach(item => {
    const cpusValues = item.cvsCpu.split(',');
    cpusValues.forEach((val, idx) => {
      seriesData[idx].data.push(+(+val).toFixed(2));
    });
  });

  // table 表格数据
  if (showChartData.length) {
    tableData.value = dataType.map((name, idx) => {
      const total = seriesData[idx].data.reduce((pre, current) => {
        return pre + current;
      }, 0);
      const average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
      return {
        name,
        unit: '%',
        average: average + '%',
        high: Math.max(...(seriesData[idx].data)) + '%',
        low: Math.min(...(seriesData[idx].data)) + '%',
        latest: seriesData[idx].data[seriesData[idx].data.length - 1] + '%'
      };
    });
  } else {
    tableData.value = [];
  }

  const legend = notMerge.value
    ? {
        legend: {
          type: 'plain',
          data: dataType,
          y: 'top',
          x: 'center'
        }
      }
    : {};

  myEcahrt.setOption({
    ...echartsOpt,
    ...legend,
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        axisTick: {
          show: false
        },
        data: showChartData.map(i => i.timestamp)
      }
    ],
    series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(0), data: [60] }] : seriesData
  }, false);
};

let intervalTimer;

const intervalRefresh = () => {
  intervalTimer = setTimeout(async () => {
    notMerge.value = false;
    if (activeTab.value === 'cpu') {
      await loadCputimerData();
    }
    clearTimeout(intervalTimer);
    intervalTimer = null;
    if (!props.endTime) {
      intervalRefresh();
    }
  }, props.delayInSeconds);
};

watch(() => activeTab.value, async () => {
  if (intervalTimer) {
    clearTimeout(intervalTimer);
    intervalTimer = null;
  }
  if (activeTab.value === 'cpu') {
    if (cpuChartData.length) {
      setCpuChartData();
    } else {
      pagination.pageNo = 1;
      await loadCpuEchartData();
    }
  }

  if (!props.endTime && ['PENDING', 'RUNNING'].includes(props.status)) {
    intervalRefresh();
  }
});

const getChartTimerParam = (params = {}) => {
  let startDate;
  if (activeTab.value === 'cpu') {
    startDate = cpuChartData[cpuChartData.length - 1].timestamp;
  }
  const startTime = dayjs(startDate).format(DATE_TIME_FORMAT);
  const nowTime = dayjs().format(DATE_TIME_FORMAT);
  const filters = [
    { key: 'timestamp', op: 'GREATER_THAN_EQUAL', value: startTime },
    { key: 'timestamp', op: 'LESS_THAN_EQUAL', value: nowTime },
    ...(params?.filters || [])
  ];
  return {
    pageNo: 1,
    pageSize: 100,
    filters
  };
};

const loadCputimerData = async () => {
  emit('loadingChange', true);
  const params = getChartTimerParam();
  const [error, res] = await nodeCtrl.getCpuMetrics(currrentNodeId.value, params);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  const data = res.data?.list || [];
  if (data.length && data[0].timestamp === cpuChartData[cpuChartData.length - 1].timestamp) {
    data.splice(0, 1);
  }
  cpuChartData = cpuChartData.concat(data);
  setSliderConfig();
  setCpuChartData();
};

onMounted(() => {
  setTimeout(() => {
    if (props.execNodes.length) {
      currrentNodeId.value = props.execNodes[0].id;
    }
    initEcahrts();
  });
});

onBeforeUnmount(() => {
  if (intervalTimer) {
    clearTimeout(intervalTimer);
    intervalTimer = null;
  }
});

watch(() => props.activeChart, newValue => {
  if (newValue !== 'node') {
    if (intervalTimer) {
      clearTimeout(intervalTimer);
      intervalTimer = null;
    }
  } else {
    if (!cpuloaded.value) {
      restart();
    } else {
      if (!props.endTime && ['PENDING', 'RUNNING'].includes(props.status)) {
        intervalRefresh();
      }
    }
  }
});

const splitDuration = (duration: string): string[] => {
  if (duration.includes('h')) {
    duration = duration.replace('h', '');
    return [duration, 'h'];
  } else if (duration.includes('ms')) {
    duration = duration.replace('ms', '');
    return [duration, 'ms'];
  } else if (duration.includes('min')) {
    duration = duration.replace('min', '');
    return [duration, 'min'];
  } else if (duration.includes('s')) {
    duration = duration.replace('s', '');
    return [duration, 's'];
  } else if (duration.includes('d')) {
    duration = duration.replace('d', '');
    return [duration, 'd'];
  }
  return [];
};

const restart = async () => {
  pagination.pageNo = 1;
  cpuloaded.value = false;
  cpuChartData = [];
  if (intervalTimer) {
    clearTimeout(intervalTimer);
    intervalTimer = null;
  }
  if (currrentNodeId.value) {
    notMerge.value = true;
    if (activeTab.value === 'cpu') {
      await loadCpuEchartData();
    }
  }
  if (!props.endTime && ['PENDING', 'RUNNING'].includes(props.status)) {
    intervalRefresh();
  }
};

// 监听Node 变更
watch(() => currrentNodeId.value, () => {
  restart();
}, {
  immediate: true
});

defineExpose({
  restart
});
</script>
<template>
      <div v-if="!props.startTime || !props.execNodes.length || props.status === 'CREATED'">{{ t('reportPreview.execPerf.sampling.testDetail.none') }}</div>
  <div v-else class="flex flex-col justify-between">
    <div class="mt-2.5">
      <div v-if="!times.length && cpuloaded">{{ t('reportPreview.execPerf.sampling.testDetail.none') }}</div>
      <div
        v-show="times.length || !cpuloaded"
        ref="echartRef"
        class="w-full h-70"></div>
    </div>
    <!-- <Table
      v-if="!!tableData.length"
      :columns="columns"
      :pagination="false"
      :dataSource="tableData"
      size="small"
      class="mt-7" /> -->
  </div>
</template>
