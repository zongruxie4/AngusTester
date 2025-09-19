<script lang="ts" setup>
import { ref, onMounted, watch, computed, onBeforeUnmount } from 'vue';
import { RadioGroup, RadioButton, Select, Slider } from 'ant-design-vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';
import { NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { nodeCtrl } from '@/api/ctrl';

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

const chartSeriesColorConfig = {
  0: '84,112,198',
  1: '145,204,117',
  2: '250,200,88',
  3: '238,102,102',
  4: '115,192,222',
  5: '59,162,114',
  6: '252,132,82',
  7: '154,96,180',
  8: '234,124,204'
};

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
  title: {
    text: ' '
  },
  tooltip: {
    trigger: 'axis',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    top: 30,
    right: 60,
    bottom: 24,
    left: 40,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [],
    axisTick: {
      show: false
    },
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed'
      }
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

const nodeTabs = [
  {
    label: t('ftpPlugin.performanceTestDetail.nodeData.tabs.cpu'),
    value: 'cpu'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.nodeData.tabs.memory'),
    value: 'memory'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.nodeData.tabs.fileSystem'),
    value: 'disk'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.nodeData.tabs.network'),
    value: 'network'
  }
];
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

// const sliderValue = ref<number[]>([]); // 时间滑块的值
// let sliderMax: number|undefined;
// const times = ref<string[]>([]);

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

  // if (!chartsData.length) {
  //   return;
  // }
  // if (!sliderValue.value.length) {
  //   times.value = chartsData.map(i => i.timestamp);
  //   sliderValue.value = [0, times.value.length - 1];
  //   sliderMax = times.value.length - 1;
  // } else {
  //   times.value = chartsData.map(i => i.timestamp);
  //   if (!props.endTime || reset) {
  //     if (sliderValue.value[1] === sliderMax) {
  //       sliderValue.value = [0, times.value.length - 1];
  //     }
  //   }
  //   sliderMax = times.value.length - 1;
  // }
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
  const param = getChartParam();
  const [error, res] = await nodeCtrl.getCpuMetrics(currrentNodeId.value, param);
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    // chartsData = res.data?.list || [];
    cpuChartData = res.data?.list || [];
  } else {
    // chartsData = chartsData.concat(res.data?.list || []);
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
  // const showChartData = chartsData.filter((_i, idx) => {
  //   return idx >= sliderValue.value[0] && idx <= sliderValue.value[1];
  // });
  // chartsData = data;
  // 'CPU 空闲百分比', '系统空间占用 CPU 百分比', '用户空间占 CPU 百分比', '等待 IO 操作的 CPU 百分比', '其他占用 CPU 百分比', '当前占用的总 CPU 百分比'
  // const dataType = ['idle', 'sys', 'sys', 'wait', 'other', 'total'];
  const dataType = [t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.idlePercentage'), t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.systemSpacePercentage'), t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.userSpacePercentage'), t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.ioWaitPercentage'), t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.otherPercentage'), t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.totalPercentage')];
  const seriesData = dataType.map(type => {
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
  const param = getChartParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }] });
  const [error, res] = await nodeCtrl.getNetworkMetrics(currrentNodeId.value, param);
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    // chartsData = res.data?.[0]?.values?.list || [];
    networkChartData = res.data?.[0]?.values?.list || [];
  } else {
    // chartsData = chartsData.concat(res.data?.[0]?.values?.list || []);
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
  // const showChartData = chartsData.filter((_i, idx) => {
  //   return idx >= sliderValue.value[0] && idx <= sliderValue.value[1];
  // });
  // loadingChartData.value = false;
  // '接收到的总字节数', '每秒接收的 MB 数', '接收到的错误包数', '发送的总字节数', '每秒发送的 MB 数'
  const dataTypeKey = ['rxBytes', 'rxBytesRate', 'rxErrors', 'txBytes', 'txBytesRate'];
  const dataType = [t('ftpPlugin.performanceTestDetail.nodeData.networkMetrics.totalBytesReceived'), t('ftpPlugin.performanceTestDetail.nodeData.networkMetrics.mbReceivedPerSecond'), t('ftpPlugin.performanceTestDetail.nodeData.networkMetrics.errorPacketsReceived'), t('ftpPlugin.performanceTestDetail.nodeData.networkMetrics.totalBytesSent'), t('ftpPlugin.performanceTestDetail.nodeData.networkMetrics.mbSentPerSecond')];
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(idx),
      name: type
    };
  });
  showChartData.forEach(item => {
    const valus = item.cvsValue.split(',');
    valus.forEach((val, idx) => {
      if (dataTypeKey[idx].includes('BytesRate')) {
        seriesData[idx].data.push(+val);
      } else if (dataTypeKey[idx].includes('Bytes')) {
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
  const param = getChartParam();
  // // loadingChartData.value = true;
  const [error, res] = await nodeCtrl.getMemoryMetrics(currrentNodeId.value, param);
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
  // const showChartData = chartsData.filter((_i, idx) => {
  //   return idx >= sliderValue.value[0] && idx <= sliderValue.value[1];
  // });
  const showChartData = memoryChartData.filter((_i, idx) => {
    return idx >= sliderValueMemory.value[0] && idx <= sliderValueMemory.value[1];
  });
  // // '物理内存剩余量', '物理内存使用量', '实际空闲物理内存百分比', '实际使用物理内存的百分比', '实际空闲内存', '实际使用内存', '空闲内存占用的百分比', '使用内存占用的百分比', '交换区使用量', '交换区剩余量'
  const dataType = [t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.physicalMemoryFree'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.physicalMemoryUsed'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualFreePercentage'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualUsedPercentage'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualFreeMemory'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualUsedMemory'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.freeMemoryPercentage'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.usedMemoryPercentage'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.swapUsed'), t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.swapFree')];
  const dataTypeKey = ['free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed', 'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'];
  // loadingChartData.value = false;
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(idx),
      name: type
    };
  });
  showChartData.forEach(item => {
    // 'free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed', 'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'
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
    series: seriesPercentData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesPercentData
  };
  myEcahrt.setOption(memoryPercentEchartOption, notMerge.value);
};

// let diskChartOption;
// let percentChartOption;
// let diskTableData;

const diskNames = ref<{value: string, label: string}[]>([]);
const activeDisk = ref();
let diskChartData = [];
let diskTableData = [];
const diskloaded = ref(false);
// 获取图表数据 磁盘
const loadDiskEchartData = async () => {
  const param = getChartParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value }] });
  const [error, res] = await nodeCtrl.getDiskMetrics(currrentNodeId.value, param);
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
const diskChartKey = ref('rate'); // 显示文件系统百分比图表
const diskDataOptions = [{ label: t('ftpPlugin.performanceTestDetail.nodeData.diskDataOptions.iops'), value: 'rate' }, { label: t('ftpPlugin.performanceTestDetail.nodeData.diskDataOptions.mbPerSecond'), value: 'bytesRate' }];
const setDiskEchartData = () => {
  // const showChartData = chartsData.filter((_i, idx) => {
  //   return idx >= sliderValue.value[0] && idx <= sliderValue.value[1];
  // });
  const showChartData = diskChartData.filter((_i, idx) => {
    return idx >= sliderValueDisk.value[0] && idx <= sliderValueDisk.value[1];
  });
  // loadingChartData.value = false;
  // '磁盘总大小', '本地文件系统剩余大小', '本地文件系统已用大小', '本地文件系统可用大小', '本地文件系统使用率', '每秒磁盘读次数', '每秒磁盘写次数', '每秒磁盘读取 MB 数', '每秒磁盘写入 MB 数'
  const dataTypeKey = ['total', 'free', 'used', 'avail', 'usePercent', 'readsRate', 'writesRate', 'readBytesRate', 'writeBytesRate'];
  const dataType = [t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.totalSize'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.fileSystemFree'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.fileSystemUsed'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.fileSystemAvailable'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.fileSystemUsage'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.diskReadsPerSecond'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.diskWritesPerSecond'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.diskReadMBPerSecond'), t('ftpPlugin.performanceTestDetail.nodeData.diskMetrics.diskWriteMBPerSecond')];
  const seriesData = dataType.map(type => {
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
  switch (diskChartKey.value) {
    // case 'disk':
    //   tableData.value = diskTableData.filter(i => i.unit === 'GB');
    //   break;
    // case 'percent':
    //   tableData.value = diskTableData.filter(i => i.unit === '%');
    //   break;
    case 'rate':
      tableData.value = diskTableData.filter(i => i.unit === 'IO/s');
      break;
    case 'bytesRate':
      tableData.value = diskTableData.filter(i => i.unit === 'MB/s');
      break;
    default:
      tableData.value = [];
  }
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
    series: bytesRateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : bytesRateSeriesData
  };
  let showEchartOptions;
  if (diskChartKey.value === 'rate') {
    showEchartOptions = rateChartOption;
  }
  if (diskChartKey.value === 'bytesRate') {
    showEchartOptions = bytesRateChartOption;
  }
  myEcahrt.setOption(showEchartOptions, notMerge.value);
};

// 文件系统展示类型变更
const onDiskChartKeyChange = () => {
  let showEchartOptions;
  if (diskChartKey.value === 'rate') {
    showEchartOptions = rateChartOption;
    tableData.value = diskTableData.filter(i => i.unit === 'IO/s');
  }
  if (diskChartKey.value === 'bytesRate') {
    showEchartOptions = bytesRateChartOption;
    tableData.value = diskTableData.filter(i => i.unit === 'MB/s');
  }
  myEcahrt.setOption(showEchartOptions, notMerge.value);
};

// slider 时间轴 变化
const onSliderChaneg = () => {
  notMerge.value = false;
  if (activeTab.value === 'cpu') {
    setCpuChartData();
  }
  if (activeTab.value === 'network') {
    setNetworkChartData();
  }
  if (activeTab.value === 'memory') {
    setMemoryChartData();
  }
  if (activeTab.value === 'disk') {
    setDiskEchartData();
  }
};

// 网络设备更换
const onActiveNetwokChange = () => {
  pagination.pageNo = 1;
  loadNetworkEchartData();
};

// 网络设备更换
const onActiveDiskChange = () => {
  pagination.pageNo = 1;
  loadDiskEchartData();
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
      const [error, { data }] = await nodeCtrl.getNetworkInfoMetrics(currrentNodeId.value);
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
      const [error, res] = await nodeCtrl.getDiskInfoMetrics(currrentNodeId.value);
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
  const params = getChartTimerParam();
  const [error, res] = await nodeCtrl.getCpuMetrics(currrentNodeId.value, params);
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
  const param = getChartTimerParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }] });
  const [error, res] = await nodeCtrl.getNetworkMetrics(currrentNodeId.value, param);
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
  const param = getChartTimerParam();
  const [error, res] = await nodeCtrl.getMemoryMetrics(currrentNodeId.value, param);
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
  const param = getChartTimerParam({ filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value }] });
  const [error, res] = await nodeCtrl.getDiskMetrics(currrentNodeId.value, param);
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
  if (props.activeChart !== 'node') {
    return;
  }
  if (currrentNodeId.value) {
    notMerge.value = true;
    if (activeTab.value === 'cpu') {
      await loadCpuEchartData();
    }
    if (activeTab.value === 'network') {
      if (!networkNames.value.length || nodeChange) {
        const [error, { data }] = await nodeCtrl.getNetworkInfoMetrics(currrentNodeId.value);
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
        const [error, res] = await nodeCtrl.getDiskInfoMetrics(currrentNodeId.value);
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
  <NoData v-if="!props.startTime || !props.execNodes.length || props.status === 'CREATED'" />
  <div v-else class="flex flex-col justify-between">
    <div class="flex pl-10 items-center pr-15">
      <RadioGroup
        v-model:value="activeTab"
        buttonStyle="solid"
        size="small">
        <RadioButton
          v-for="tab in nodeTabs"
          :key="tab.value"
          :value="tab.value">
          {{ tab.label }}
        </RadioButton>
      </RadioGroup>
      <Select
        v-if="activeTab === 'network'"
        v-model:value="activeNetwork"
        class="min-w-25 ml-2"
        size="small"
        :options="networkNames"
        @change="onActiveNetwokChange" />
      <Select
        v-show="activeTab === 'disk'"
        v-model:value="activeDisk"
        class="ml-2 min-w-25"
        size="small"
        :options="diskNames"
        @change="onActiveDiskChange" />
      <RadioGroup
        v-show="activeTab ==='disk'"
        v-model:value="diskChartKey"
        class="block text-center ml-2"
        size="small"
        :options="diskDataOptions"
        @change="onDiskChartKeyChange" />
      <div class="flex items-center flex-1 justify-end">
        <RadioGroup
          v-model:value="nodeType"
          class="mr-5"
          :disabled="!props.appNodes?.length"
          :options="[{label: t('ftpPlugin.performanceTestDetail.nodeData.nodeTypes.execNode'), value: 'exec'}, {label: t('ftpPlugin.performanceTestDetail.nodeData.nodeTypes.appNode'), value: 'apply'}]" />
        <Select
          v-model:value="currrentNodeId"
          class="min-w-25"
          size="small"
          :options="nodeList"
          :fieldNames="{value: 'id', label: 'name'}" />
      </div>
    </div>
    <div class="mt-3.5">
      <template v-if="!times.length && ((activeTab === 'cpu' && cpuloaded) || (activeTab === 'memory' && memoryloaded) || (activeTab === 'disk' && diskloaded) || (activeTab === 'network' && networkloaded))">
        <NoData class="my-20" />
      </template>
      <div v-show="times.length || (activeTab === 'cpu' && !cpuloaded) || (activeTab === 'memory' && !memoryloaded) || (activeTab === 'disk' && !diskloaded) || (activeTab === 'network' && !networkloaded)" class="w-full ">
        <div ref="echartRef" class="w-full h-70"></div>
        <div class="-mt-5 pl-10 pr-12">
          <template v-if="activeTab === 'cpu'">
            <Slider
              v-model:value="sliderValueCpu"
              range
              :min="0"
              :max="timesCpu.length - 1 > 0 ? timesCpu.length - 1 : sliderValueCpu[1]"
              :tipFormatter="(value) => timesCpu.length ? cpuChartData[value]?.timestamp : ''"
              @change="onSliderChaneg">
            </Slider>
          </template>
          <template v-if="activeTab === 'memory'">
            <Slider
              v-model:value="sliderValueMemory"
              range
              :min="0"
              :max="timesMemory.length - 1 > 0 ? timesMemory.length - 1 : sliderValueMemory[1]"
              :tipFormatter="(value) => timesMemory.length ? memoryChartData[value]?.timestamp : ''"
              @change="onSliderChaneg">
            </Slider>
          </template>
          <template v-if="activeTab === 'disk'">
            <Slider
              v-model:value="sliderValueDisk"
              range
              :min="0"
              :max="timesDisk.length - 1 > 0 ? timesDisk.length - 1 : sliderValueDisk[1]"
              :tipFormatter="(value) => timesDisk.length ? diskChartData[value]?.timestamp : ''"
              @change="onSliderChaneg">
            </Slider>
          </template>
          <template v-if="activeTab === 'network'">
            <Slider
              v-model:value="sliderValueNetwork"
              range
              :min="0"
              :max="timesNetwork.length - 1 > 0 ? timesNetwork.length - 1 : sliderValueNetwork[1]"
              :tipFormatter="(value) => timesNetwork.length ? networkChartData[value]?.timestamp : ''"
              @change="onSliderChaneg">
            </Slider>
          </template>
          <!-- <Slider
            v-model:value="sliderValue"
            range
            :min="0"
            :max="times.length - 1 > 0 ? times.length - 1 : sliderValue[1]"
            :tipFormatter="(value) => times.length ? chartsData[value]?.timestamp : ''"
            @change="onSliderChaneg">
          </Slider> -->
        </div>
      </div>
    </div>
    <!-- <Table
      v-if="!!tableData.length"
      :columns="columns"
      :pagination="false"
      :dataSource="tableData"
      size="small"
      class="mb-7.5 mt-10 pl-10 pr-15">
    </Table> -->
  </div>
</template>
<style scoped>
:deep(.ant-slider-rail){
  height: 10px;
  border: 1px solid var(--border-divider);
  border-radius:1px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider:hover .ant-slider-rail){
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider-track){
  height: 10px;
  border-color:rgba(145, 213, 255, 30%);
  background-color: rgba(245, 245, 245, 100%);

  @apply border;
}

:deep(.ant-slider:hover .ant-slider-track){
  height: 10px;
  background-color: rgba(245, 245, 245, 100%);
}

:deep(.ant-slider-step){
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider-handle){
  width: 10px;
  height: 16px;
  margin-top:-3px;
  border-radius:3px;
  border-color:rgba(145, 213, 255, 100%);

  @apply border-2 ;
}

:deep(.ant-slider-handle:focus){
  border-color:rgba(145, 213, 255, 100%);
  box-shadow:none
}

:deep(.ant-slider:hover .ant-slider-handle){
  border-color: rgba(145, 213, 255, 100%);
}
</style>
