<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';

import { chartSeriesColorConfig } from '@/views/report/preview/PropsType';
import { nodeCtrl } from '@/api/ctrl';
import { useI18n } from 'vue-i18n';

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
// table表格
const tableData = ref<{name: string, unit: string, average: string, high: string, low: string, latest: string}[]>([]);
const notMerge = ref(true); // 图表数据覆盖是否重新渲染
const defaultLegend = {
  type: 'plain',
  data: [],
  y: 'top',
  x: 'center'
};
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

const echartRefIops = ref();
const echartRefMb = ref();
let myEcahrtIops;
let myEcahrtMb;
const initEcahrts = () => {
  myEcahrtIops = echarts.init(echartRefIops.value);
  myEcahrtMb = echarts.init(echartRefMb.value);
  myEcahrtIops.setOption(echartsOpt);
  myEcahrtMb.setOption(echartsOpt);
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

const activeTab = ref('disk');

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

const sliderValueMemory = ref<number[]>([]); // 时间滑块的值
let sliderMaxMemory: number|undefined;
const timesMemory = ref<string[]>([]);

const sliderValueDisk = ref<number[]>([]); // 时间滑块的值
let sliderMaxDisk: number|undefined;
const timesDisk = ref<string[]>([]);

const sliderValueNetwork = ref<number[]>([]); // 时间滑块的值
let sliderMaxNetwork: number|undefined;
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
  } else if (activeTab.value === 'memory') {
    if (!memoryChartData.length) {
      return;
    }
    if (!sliderValueMemory.value.length) {
      timesMemory.value = memoryChartData.map(i => i.timestamp);
      sliderValueMemory.value = [0, timesMemory.value.length - 1];
      sliderMaxMemory = timesMemory.value.length - 1;
    } else {
      timesMemory.value = memoryChartData.map(i => i.timestamp);
      if (!props.endTime || reset) {
        if (sliderValueMemory.value[1] === sliderMaxMemory) {
          sliderValueMemory.value = [0, timesMemory.value.length - 1];
        }
      }
      sliderMaxMemory = timesMemory.value.length - 1;
    }
  } else if (activeTab.value === 'disk') {
    if (!diskChartData.length) {
      return;
    }
    if (!sliderValueDisk.value.length) {
      timesDisk.value = diskChartData.map(i => i.timestamp);
      sliderValueDisk.value = [0, timesDisk.value.length - 1];
      sliderMaxDisk = timesDisk.value.length - 1;
    } else {
      timesDisk.value = diskChartData.map(i => i.timestamp);
      if (!props.endTime || reset) {
        if (sliderValueDisk.value[1] === sliderMaxDisk) {
          sliderValueDisk.value = [0, timesDisk.value.length - 1];
        }
      }
      sliderMaxDisk = timesDisk.value.length - 1;
    }
  } else {
    if (!networkChartData.length) {
      return;
    }
    if (!sliderValueNetwork.value.length) {
      timesNetwork.value = networkChartData.map(i => i.timestamp);
      sliderValueNetwork.value = [0, timesNetwork.value.length - 1];
      sliderMaxNetwork = timesNetwork.value.length - 1;
    } else {
      timesNetwork.value = networkChartData.map(i => i.timestamp);
      if (!props.endTime || reset) {
        if (sliderValueNetwork.value[1] === sliderMaxNetwork) {
          sliderValueNetwork.value = [0, timesNetwork.value.length - 1];
        }
      }
      sliderMaxNetwork = timesNetwork.value.length - 1;
    }
  }
};

const pagination = { // 数据分页获取
  pageSize: 1000,
  pageNo: 1
};

const getChartParam = (params = {}) => {
  const startTime = dayjs(props.startTime).format('YYYY-MM-DD HH:mm:ss');
  const [interval, unit] = splitDuration(props.reportInterval);
  const endTime = props.endTime
    ? dayjs(props.endTime).add(+interval, unit).format('YYYY-MM-DD HH:mm:ss')
    : dayjs(props.startTime).add(+interval, unit).add(1, 'day').format('YYYY-MM-DD HH:mm:ss');
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
    // return;
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
  const dataType = ['CPU 空闲百分比(%)', '系统空间占用 CPU 百分比(%)', '用户空间占 CPU 百分比(%)', '等待 IO 操作的 CPU 百分比(%)', '其他占用 CPU 百分比(%)', '当前占用的总 CPU 百分比(%)'];
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

const networkNames = ref<{value: string, label: string}[]>([]); // 网络设备选项
const activeNetwork = ref(); // 网络设备名称
let networkChartData = [];
let networkTableData = [];
const networkloaded = ref(false);
// 图表数据 网络
const loadNetworkEchartData = async () => {
  emit('loadingChange', true);
  const param = getChartParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }] });
  const [error, res] = await nodeCtrl.getNetworkMetrics(currrentNodeId.value, param);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    networkChartData = res.data?.[0]?.values?.list || [];
  } else {
    networkChartData = networkChartData.concat(res.data?.[0]?.values?.list || []);
  }
  if (res.data?.[0]?.values?.total && networkChartData.length < res.data?.[0]?.values?.total) {
    pagination.pageNo += 1;
    loadNetworkEchartData();
  }
  if (networkChartData.length > 2 && networkChartData[networkChartData.length - 1].timestamp === networkChartData[networkChartData.length - 2].timestamp) {
    networkChartData.splice(networkChartData.length - 2, 1);
  }
  networkloaded.value = true;
  setSliderConfig(true);
  setNetworkChartData();
};

// 设置网络图表数据
const setNetworkChartData = () => {
  const showChartData = networkChartData.filter((_i, idx) => {
    return idx >= sliderValueNetwork.value[0] && idx <= sliderValueNetwork.value[1];
  });
  // loadingChartData.value = false;
  // '接收到的总字节数', '每秒接收的 MB 数', '接收到的错误包数', '发送的总字节数', '每秒发送的 MB 数'
  const dataTypeKey = ['rxBytes', 'rxBytesRate', 'rxErrors', 'txBytes', 'txBytesRate'];
  const dataType = ['接收到的总字节(GB)', '每秒接收的 MB 数(MB/s)', '接收到的错误包数(packets)', '发送的总字节(GB)', '每秒发送的 MB 数(MB/s)'];
  const seriesData = dataType.map((type, idx) => {
    return {
      ...getDefaultLineConfig(idx),
      name: type
    };
  });
  showChartData.forEach(item => {
    const valus = item.cvsValue.split(',');
    valus.forEach((val, idx) => {
      if (dataTypeKey[idx].includes('Bytes')) {
        seriesData[idx].data.push(+formatBytesToUnit(+val, 'GB', 2));
      } else {
        seriesData[idx].data.push(+val);
      }
    });
  });
  if (showChartData.length) {
    networkTableData = dataType.map((name, idx) => {
      const total = seriesData[idx].data.reduce((pre, current) => {
        return pre + current;
      }, 0);
      let average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
      let high = Math.max(...(seriesData[idx].data)) + '';
      let low = Math.min(...(seriesData[idx].data)) + '';
      let latest = seriesData[idx].data[seriesData[idx].data.length - 1] + '';
      if (dataTypeKey[idx].includes('BytesRate')) {
        average += 'MB/s';
        high += 'MB/s';
        low += 'MB/s';
        latest += 'MB/s';
      } else if (dataTypeKey[idx].includes('Bytes')) {
        average += ' GB';
        high += ' GB';
        low += ' GB';
        latest += ' GB';
      } else {
        average += ' packets';
        high += ' packets';
        low += ' packets';
        latest += ' packets';
      }

      const unit = dataTypeKey[idx].includes('BytesRate') ? 'MB/s' : dataTypeKey[idx].includes('Bytes') ? 'GB' : 'packets';
      return {
        name,
        unit,
        average: average,
        high: high,
        low: low,
        latest: latest
      };
    });
  } else {
    networkTableData = [];
  }
  tableData.value = networkTableData.filter(i => i.unit === 'MB/s');

  const networkSeriesData = seriesData.slice(1, 2).concat(seriesData.slice(4, 5));
  const networkDataTypeKey = dataType.slice(1, 2).concat(dataType.slice(4, 5));
  const networdLengend = { ...defaultLegend, data: networkDataTypeKey };
  const networkChartOption = {
    ...echartsOpt,
    legend: networdLengend,
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
    series: networkSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(0), data: [50] }] : networkSeriesData
  };

  myEcahrt.setOption(networkChartOption, notMerge.value);
};

let memoryChartData = [];
let memoryTableData = [];
const memoryloaded = ref(false);
// 获取图表数据 内存
const loadMemoryEchartData = async () => {
  emit('loadingChange', true);
  const param = getChartParam();
  const [error, res] = await nodeCtrl.getMemoryMetrics(currrentNodeId.value, param);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    // chartsData = res.data?.list || [];
    memoryChartData = res.data?.list || [];
  } else {
    // chartsData = chartsData.concat(res.data?.list || []);
    memoryChartData = memoryChartData.concat(res.data?.list || []);
  }
  if (memoryChartData.length < res.data.total) {
    pagination.pageNo += 1;
    loadMemoryEchartData();
    // return;
  }
  if (memoryChartData.length > 2 && memoryChartData[memoryChartData.length - 1].timestamp === memoryChartData[memoryChartData.length - 2].timestamp) {
    memoryChartData.splice(memoryChartData.length - 2, 1);
  }
  memoryloaded.value = true;
  setSliderConfig(true);
  setMemoryChartData();
};

// 设置图表数据 内存
const setMemoryChartData = () => {
  const showChartData = memoryChartData.filter((_i, idx) => {
    return idx >= sliderValueMemory.value[0] && idx <= sliderValueMemory.value[1];
  });
  // // '物理内存剩余量', '物理内存使用量', '实际空闲物理内存百分比', '实际使用物理内存的百分比', '实际空闲内存', '实际使用内存', '空闲内存占用的百分比', '使用内存占用的百分比', '交换区使用量', '交换区剩余量'
  const dataType = ['物理内存剩余量(GB)', '物理内存使用量(GB)', '实际空闲物理内存百分比(%)', '实际使用物理内存的百分比(%)', '实际空闲内存(GB)', '实际使用内存(GB)', '空闲内存占用的百分比(%)', '使用内存占用的百分比(%)', '交换区使用量(GB)', '交换区剩余量(GB)'];
  const dataTypeKey = ['free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed', 'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'];
  const seriesData = dataType.map((type, idx) => {
    return {
      ...getDefaultLineConfig(idx),
      name: type
    };
  });
  showChartData.forEach(item => {
    const values = item.cvsMemory.split(',');
    values.forEach((val, idx) => {
      if (dataTypeKey[idx].includes('Percent')) {
        seriesData[idx].data.push(+val);
      } else {
        seriesData[idx].data.push(+formatBytesToUnit(+val, 'GB', 2));
      }
    });
  });
  if (showChartData.length) {
    memoryTableData = dataType.map((name, idx) => {
      const total = seriesData[idx].data.reduce((pre, current) => {
        return pre + current;
      }, 0);
      let average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
      let high = Math.max(...(seriesData[idx].data)) + '';
      let low = Math.min(...(seriesData[idx].data)) + '';
      let latest = seriesData[idx].data[seriesData[idx].data.length - 1] + '';
      if (dataTypeKey[idx].includes('Percent')) {
        average += '%';
        high += '%';
        low += '%';
        latest += '%';
      } else {
        average += ' GB';
        high += ' GB';
        low += ' GB';
        latest += ' GB';
      }

      return {
        name,
        unit: dataTypeKey[idx].includes('Percent') ? '%' : 'B',
        average: average,
        high: high,
        low: low,
        latest: latest
      };
    });
  } else {
    memoryTableData = [];
  }
  tableData.value = memoryTableData.filter(i => i.unit === '%');

  const seriesPercentData = seriesData.slice(2, 4).concat(seriesData.slice(6, 8));
  const percentDataType = dataType.slice(2, 4).concat(dataType.slice(6, 8));
  const percentLegend = {
    ...defaultLegend,
    data: percentDataType
  };

  const memoryPercentEchartOption = {
    ...echartsOpt,
    legend: percentLegend,
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: showChartData.map(i => i.timestamp)
      }
    ],
    series: seriesPercentData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(0), data: [50] }] : seriesPercentData
  };
  myEcahrt.setOption(memoryPercentEchartOption, notMerge.value);
};

const diskNames = ref<{value: string, label: string}[]>([]);
const activeDisk = ref();
let diskChartData = [];
let diskTableData = [];
const diskloaded = ref(false);
// 获取图表数据 磁盘
const loadDiskEchartData = async () => {
  emit('loadingChange', true);
  const param = getChartParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value }] });
  const [error, res] = await nodeCtrl.getDiskMetrics(currrentNodeId.value, param);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    // chartsData = res.data?.[0]?.values?.list || [];
    diskChartData = res.data?.[0]?.values?.list || [];
  } else {
    // chartsData = chartsData.concat(res.data?.[0]?.values?.list || []);
    diskChartData = diskChartData.concat(res.data?.[0]?.values?.list || []);
  }
  if (res.data?.[0]?.values?.total && diskChartData.length < res.data?.[0]?.values?.total) {
    pagination.pageNo += 1;
    loadDiskEchartData();
    // return;
  }
  if (diskChartData.length > 2 && diskChartData[diskChartData.length - 1].timestamp === diskChartData[diskChartData.length - 2].timestamp) {
    diskChartData.splice(diskChartData.length - 2, 1);
  }
  diskloaded.value = true;
  setSliderConfig(true);
  setDiskEchartData();
};

// 设置图表数据 磁盘
let rateChartOption;
let bytesRateChartOption;
const tableDataIops = ref([]);
const tableDataMb = ref([]);
const setDiskEchartData = () => {
  const showChartData = diskChartData.filter((_i, idx) => {
    return idx >= sliderValueDisk.value[0] && idx <= sliderValueDisk.value[1];
  });
  // '磁盘总大小', '本地文件系统剩余大小', '本地文件系统已用大小', '本地文件系统可用大小', '本地文件系统使用率', '每秒磁盘读次数', '每秒磁盘写次数', '每秒磁盘读取 MB 数', '每秒磁盘写入 MB 数'
  const dataTypeKey = ['total', 'free', 'used', 'avail', 'usePercent', 'readsRate', 'writesRate', 'readBytesRate', 'writeBytesRate'];
  const dataType = [t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskTotal'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskFree'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskUsed'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskAvail'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskUsePercent'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskReadsRate'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskWritesRate'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskReadBytesRate'),
  t('reportPreview.execPerf.sampling.testDetail.nodeResource.diskWriteBytesRate')];
  const seriesData = dataType.map((type, idx) => {
    return {
      ...getDefaultLineConfig(idx),
      name: type
    };
  });
  showChartData.forEach(item => {
    const diskValues = item.cvsValue.split(',');
    diskValues.forEach((val, idx) => {
      if (dataTypeKey[idx].includes('Percent') || dataTypeKey[idx].includes('BytesRate') || dataTypeKey[idx].includes('Rate')) {
        seriesData[idx].data.push(+val);
      } else {
        seriesData[idx].data.push(+formatBytesToUnit(+val, 'GB', 2));
      }
    });
  });
  if (showChartData.length) {
    diskTableData = dataType.map((name, idx) => {
      const total = seriesData[idx].data.reduce((pre, current) => {
        return pre + current;
      }, 0);
      let average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
      let high = Math.max(...(seriesData[idx].data)) + '';
      let low = Math.min(...(seriesData[idx].data)) + '';
      let latest = seriesData[idx].data[seriesData[idx].data.length - 1] + '';
      let unit;
      if (dataTypeKey[idx].includes('Percent')) {
        average += '%';
        high += '%';
        low += '%';
        latest += '%';
        unit = '%';
      } else if (dataTypeKey[idx].includes('BytesRate')) {
        average += ' MB/s';
        high += ' MB/s';
        low += ' MB/s';
        latest += 'MB/s';
        unit = 'MB/s';
      } else if (dataTypeKey[idx].includes('Rate')) {
        average += ' IO/s';
        high += ' IO/s';
        low += ' IO/s';
        latest += ' IO/s';
        unit = 'IO/s';
      } else {
        average += ' GB';
        high += ' GB';
        low += ' GB';
        latest += ' GB';
        unit = 'GB';
      }
      return {
        name,
        average: average,
        high: high,
        low: low,
        latest: latest,
        unit
      };
    });
  } else {
    diskTableData = [];
  }
  tableDataIops.value = diskTableData.filter(i => i.unit === 'IO/s');
  tableDataMb.value = diskTableData.filter(i => i.unit === 'MB/s');
  const rateSeriesData = seriesData.slice(5, 7);
  const rateDataType = dataType.slice(5, 7);
  const rateLegend = { ...defaultLegend, data: rateDataType };
  rateChartOption = {
    ...echartsOpt,
    legend: rateLegend,
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: showChartData.map(i => i.timestamp)
      }
    ],
    series: rateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(0), data: [50] }] : rateSeriesData
  };

  const bytesRateSeriesData = seriesData.slice(7, 9);
  const bytesRateDataType = dataType.slice(7, 9);
  const bytesLegend = { ...defaultLegend, data: bytesRateDataType };
  bytesRateChartOption = {
    ...echartsOpt,
    legend: bytesLegend,
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: showChartData.map(i => i.timestamp)
      }
    ],
    series: bytesRateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(0), data: [50] }] : bytesRateSeriesData
  };
  myEcahrtMb.setOption(bytesRateChartOption, notMerge.value);
  myEcahrtIops.setOption(rateChartOption, notMerge.value);
};

let intervalTimer;

const intervalRefresh = () => {
  intervalTimer = setTimeout(async () => {
    notMerge.value = false;
    if (activeTab.value === 'cpu') {
      await loadCputimerData();
    }
    if (activeTab.value === 'network') {
      await loadNetworkTimerData();
    }
    if (activeTab.value === 'memory') {
      await loadMemoryTimerData();
    }
    if (activeTab.value === 'disk') {
      await loadDiskTimerData();
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
  if (activeTab.value === 'network') {
    if (!networkNames.value.length) {
      emit('loadingChange', true);
      const [error, { data }] = await nodeCtrl.getNetworkInfoMetrics(currrentNodeId.value);
      emit('loadingChange', false);
      if (!error) {
        const _networkNames: {label: string, value: string}[] = [];
        const networkNamesMp = {};
        for (const item of (data || [])) {
          if (!networkNamesMp[item.deviceName]) {
            _networkNames.push({ label: item.deviceName, value: item.deviceName });
            networkNamesMp[item.deviceName] = true;
          }
        }
        networkNames.value = _networkNames;
        activeNetwork.value = _networkNames?.[0]?.value;
      }
    }
    if (networkChartData.length) {
      setNetworkChartData();
    } else {
      pagination.pageNo = 1;
      await loadNetworkEchartData();
    }
  }

  if (activeTab.value === 'memory') {
    if (memoryChartData.length) {
      setMemoryChartData();
    } else {
      pagination.pageNo = 1;
      await loadMemoryEchartData();
    }
  }

  if (activeTab.value === 'disk') {
    if (!diskNames.value.length) {
      emit('loadingChange', true);
      const [error, res] = await nodeCtrl.getDiskInfoMetrics(currrentNodeId.value);
      emit('loadingChange', false);
      if (error) {
        return;
      }
      const _diskNames:{label: string, value: string}[] = [];
      const deviceNameMp = {};
      for (const item of (res.data || [])) {
        if (!deviceNameMp[item.deviceName]) {
          _diskNames.push({ label: item.deviceName, value: item.deviceName });
          deviceNameMp[item.deviceName] = true;
        }
      }
      diskNames.value = _diskNames;
      activeDisk.value = diskNames.value?.[0]?.value;
    }

    if (diskChartData.length) {
      setDiskEchartData();
    } else {
      pagination.pageNo = 1;
      await loadDiskEchartData();
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
  if (activeTab.value === 'memory') {
    startDate = memoryChartData[memoryChartData.length - 1].timestamp;
  }
  if (activeTab.value === 'disk') {
    startDate = diskChartData[diskChartData.length - 1].timestamp;
  }
  if (activeTab.value === 'network') {
    startDate = networkChartData[networkChartData.length - 1].timestamp;
  }
  const startTime = dayjs(startDate).format('YYYY-MM-DD HH:mm:ss');
  const nowTime = dayjs().format('YYYY-MM-DD HH:mm:ss');
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
  const params = getChartTimerParam();
  emit('loadingChange', true);
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

const loadNetworkTimerData = async () => {
  emit('loadingChange', true);
  const param = getChartTimerParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }] });
  const [error, res] = await nodeCtrl.getNetworkMetrics(currrentNodeId.value, param);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  const data = res.data?.[0]?.values?.list || [];
  if (data.length && data[0].timestamp === networkChartData[networkChartData.length - 1].timestamp) {
    data.splice(0, 1);
  }
  networkChartData = networkChartData.concat(data);
  setSliderConfig();
  setNetworkChartData();
};

const loadMemoryTimerData = async () => {
  emit('loadingChange', true);
  const param = getChartTimerParam();
  const [error, res] = await nodeCtrl.getMemoryMetrics(currrentNodeId.value, param);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  const data = res.data?.list || [];
  if (data.length && data[0].timestamp === memoryChartData[memoryChartData.length - 1].timestamp) {
    data.splice(0, 1);
  }
  memoryChartData = memoryChartData.concat(data);
  setSliderConfig();
  setMemoryChartData();
};

const loadDiskTimerData = async () => {
  emit('loadingChange', true);
  const param = getChartTimerParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value }] });
  const [error, res] = await nodeCtrl.getDiskMetrics(currrentNodeId.value, param);
  emit('loadingChange', false);
  if (error) {
    return;
  }
  const data = res.data?.[0]?.values?.list || [];
  if (data.length && data[0].timestamp === diskChartData[diskChartData.length - 1].timestamp) {
    data.splice(0, 1);
  }
  diskChartData = diskChartData.concat(data);
  setSliderConfig();
  setDiskEchartData();
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
    if (!cpuloaded.value && !memoryloaded.value && !diskloaded.value && !networkloaded.value) {
      restart();
    } else {
      if (!props.endTime && ['PENDING', 'RUNNING'].includes(props.status)) {
        intervalRefresh();
      }
    }
  }
});

const formatBytesToUnit = (size = 0, unit = 'GB', decimal = 5):string => {
  if (size === 0) return '0';
  const unitMap = {
    B: 1, KB: 1024, MB: 1048576, GB: 1073741824, TB: 1099511627776, PB: 1125899906842624
  };
  const c = unitMap[unit] || 1073741824;
  return (size / c).toFixed(decimal);
};

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

const restart = async (nodeChange = false) => {
  pagination.pageNo = 1;
  cpuloaded.value = false;
  networkloaded.value = false;
  diskloaded.value = false;
  memoryloaded.value = false;
  cpuChartData = [];
  networkChartData = [];
  memoryChartData = [];
  diskChartData = [];
  if (intervalTimer) {
    clearTimeout(intervalTimer);
    intervalTimer = null;
  }
  if (currrentNodeId.value) {
    notMerge.value = true;
    if (activeTab.value === 'cpu') {
      await loadCpuEchartData();
    }
    if (activeTab.value === 'network') {
      if (!networkNames.value.length || nodeChange) {
        emit('loadingChange', true);
        const [error, { data }] = await nodeCtrl.getNetworkInfoMetrics(currrentNodeId.value);
        emit('loadingChange', false);
        if (!error) {
          // networkNames.value = data;
          const _networkNames: {label: string, value: string}[] = [];
          const networkNamesMp = {};
          for (const item of (data || [])) {
            if (!networkNamesMp[item.deviceName]) {
              _networkNames.push({ label: item.deviceName, value: item.deviceName });
              networkNamesMp[item.deviceName] = true;
            }
          }
          networkNames.value = _networkNames;
          activeNetwork.value = _networkNames?.[0]?.value;
        }
      }
      await loadNetworkEchartData();
    }

    if (activeTab.value === 'memory') {
      await loadMemoryEchartData();
    }

    if (activeTab.value === 'disk') {
      if (!networkNames.value.length || nodeChange) {
        emit('loadingChange', true);
        const [error, res] = await nodeCtrl.getDiskInfoMetrics(currrentNodeId.value);
        emit('loadingChange', false);
        if (error) {
          return;
        }
        const _diskNames:{label: string, value: string}[] = [];
        const deviceNameMp = {};
        for (const item of (res.data || [])) {
          if (!deviceNameMp[item.deviceName]) {
            _diskNames.push({ label: item.deviceName, value: item.deviceName });
            deviceNameMp[item.deviceName] = true;
          }
        }
        diskNames.value = _diskNames;
        activeDisk.value = diskNames.value?.[0]?.value;
      }
      await loadDiskEchartData();
    }
  }
  if (!props.endTime && ['PENDING', 'RUNNING'].includes(props.status)) {
    intervalRefresh();
  }
};

// 监听Node 变更
watch(() => currrentNodeId.value, () => {
  restart(true);
}, {
  immediate: true
});

defineExpose({
  restart
});
</script>
<template>
      <div v-if="!props.startTime || !props.execNodes.length || props.status === 'CREATED'">{{ t('reportPreview.execPerf.sampling.testDetail.none') }}</div>
  <template v-else>
    <div class="flex flex-col justify-between">
      <div class="mt-2.5">
        <div v-if="!times.length && diskloaded">{{ t('reportPreview.execPerf.sampling.testDetail.none') }}</div>
        <div
          v-show="times.length || !diskloaded"
          ref="echartRefIops"
          class="w-full h-70"></div>
      </div>
      <!-- <Table
        v-if="!!tableDataIops.length"
        :columns="columns"
        :pagination="false"
        :dataSource="tableDataIops"
        size="small"
        class="mt-7" /> -->
    </div>

    <div class="flex flex-col justify-between">
      <div class="mt-2.5">
        <div v-if="!times.length && cpuloaded">{{ t('reportPreview.execPerf.sampling.testDetail.none') }}</div>
        <div
          v-show="times.length || !cpuloaded"
          ref="echartRefMb"
          class="w-full h-70"></div>
      </div>
      <!-- <Table
        v-if="!!tableDataMb.length"
        :columns="columns"
        :pagination="false"
        :dataSource="tableDataMb"
        size="small"
        class="mt-7" /> -->
    </div>
  </template>
</template>
