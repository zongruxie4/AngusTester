<script setup lang="ts">
import { defineAsyncComponent, inject, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import * as echarts from 'echarts';
import dayjs from 'dayjs';
import { Button, Progress, RadioButton, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import {
  Grid,
  Hints,
  Icon,
  IntervalTimestamp,
  modal,
  NoData,
  notification,
  Select,
  Spin,
  Tooltip
} from '@xcan-angus/vue-ui';
import { clipboard, TESTER } from '@xcan-angus/tools';

import { infoItem, internetInfo, nodeEchartsTabs, nodeUseProgresses } from './interface';
import { getStrokeColor, installConfigColumns } from '../interface';
import { nodeCtrl, nodeInfo } from 'src/api/ctrl';
import { node } from 'src/api/tester';

import { formatBytes, formatBytesToUnit } from '@/utils/common';

const AgentChart = defineAsyncComponent(() => import('./agentChart.vue'));
const AgentInfo = defineAsyncComponent(() => import('./agentInfo.vue'));
const Execute = defineAsyncComponent(() => import('./execute.vue'));
const Log = defineAsyncComponent(() => import('./log.vue'));
const MockService = defineAsyncComponent(() => import('./mockService.vue'));
const ExecPropulsion = defineAsyncComponent(() => import('./execPropulsion.vue'));

const defaultLegend = {
  type: 'plain',
  data: [],
  y: 'bottom',
  x: 'center'
};

const echartRef = ref();

let myEcahrt;
const route = useRoute();
const router = useRouter();
const id = ref(route.params.id);

const state = reactive<{infos: Record<string, any>, linuxOfflineInstallSteps: Record<string, any>, windowsOfflineInstallSteps: Record<string, any>}>({
  infos: {},
  linuxOfflineInstallSteps: {},
  windowsOfflineInstallSteps: {}
});

const showPassd = ref(false);

const chartServersMap = {
  cpu: `${TESTER}/node/${id.value}/metrics/cpu`,
  memory: `${TESTER}/node/${id.value}/metrics/memory`,
  disk: `${TESTER}/node/${id.value}/metrics/disk`,
  network: `${TESTER}/node/${id.value}/metrics/network`
};
const loadingChart = ref(false);

const currentSourceServer = ref();

const chartParams = ref({}); // 图表参数

let chartsData: any[] = [];

const activeTab = ref('cpu');

const diskNames = ref([]); // 所有磁盘名字 list
const activeDisk = ref(); // 选中的磁盘

const networkNames = ref([]); // 所有网络名字
const activeNetwork = ref(); // 选中的网络

const notMerge = ref(true); // 图表数据覆盖是否重新渲染

const memoryDataOptions = [{ label: '使用量', value: false }, { label: '使用率', value: true }];
const diskDataOptions = [
  // { label: '使用量', value: 'disk' }, { label: '使用率', value: 'percent' },
  { label: 'IOPS', value: 'rate' }, { label: '每秒MB数', value: 'bytesRate' }];
const networkDataOptions = [{ label: '每秒MB 数', value: 'network' }, { label: '字节数', value: 'bytes' }, { label: '错误包数', value: 'packet' }];

const showMemoryPercentChart = ref(false); // 显示内存百分比图表
const diskChartKey = ref('rate'); // 显示文件系统百分比图表
const networkChartKey = ref('network');

const timestamp = ref<string | undefined>(); // 选择时间段

const showInstallStep = ref(false); // 显示手动安装步骤

const tableData = ref<{name: string, unit: string, average: string, high: string, low: string, latest: string}[]>([]);
const sourceUse = reactive({
  cpu: 0,
  cpuPercent: 0,
  cpuTotal: 0,
  memory: '0',
  memoryPercent: 0,
  memoryTotal: '0',
  swap: '0',
  swapPercent: 0,
  swapTotal: '0',
  disk: '0',
  diskPercent: 0,
  diskTotal: '0',
  txBytesRate: 0,
  rxBytesRate: 0,
  txBytes: '0',
  rxBytes: '0'
});

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
    top: '3%',
    left: '3%',
    right: '3%',
    bottom: '12%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [],
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

const initEcahrts = () => {
  myEcahrt = echarts.init(echartRef.value);
  myEcahrt.setOption(echartsOpt);
};

const getDefaultLineConfig = () => {
  return {
    data: [] as number[],
    type: 'line',
    smooth: true,
    showSymbol: false,
    lineStyle: {
      width: 1
    }
  };
};

const loadingInfo = ref(true);
// 获取节点基本信息
const loadInfo = async () => {
  loadingInfo.value = true;
  const [error, res] = await node.getNodeDetail(id.value);
  loadingInfo.value = false;
  if (error) {
    return;
  }
  // delete res.data.online;
  const rolesName = res.data.roles.map(i => i.message).join(',');
  const rolesValues = res.data.roles.map(i => i.value);
  state.infos = {
    ...state.infos,
    ...res.data,
    sourceName: res.data.source.message,
    rolesName,
    rolesValues
  };
};

// 显示密码
const handleShowPassd = () => {
  showPassd.value = !showPassd.value;
};

const installing = ref(false); // 代理安装中
// 安装代理
const installAgen = async () => {
  installing.value = true;
  const [error] = await node.installNodeAgent({ id: id.value });
  installing.value = false;
  if (error) {
    if (typeof error.data !== 'object') {
      return;
    }
    if (error.data.linuxOfflineInstallSteps) {
      state.linuxOfflineInstallSteps = error.data.linuxOfflineInstallSteps;
      showInstallStep.value = true;
    }
    if (error.data.windowsOfflineInstallSteps) {
      state.windowsOfflineInstallSteps = error.data.windowsOfflineInstallSteps;
      showInstallStep.value = true;
    }
    return;
  }

  state.infos.geAgentInstallationCmd = true;
};

// 手动安装步骤
const getInstallStep = async () => {
  if (showInstallStep.value) {
    foldInstallAgent();
    return;
  }
  const [error, res] = await nodeInfo.geAgentInstallationCmd({ id: id.value });
  if (error) {
    return;
  }
  showInstallStep.value = true;
  if (res.data.linuxOfflineInstallSteps) {
    state.linuxOfflineInstallSteps = res.data.linuxOfflineInstallSteps;
  }
  if (res.data.windowsOfflineInstallSteps) {
    state.windowsOfflineInstallSteps = res.data.windowsOfflineInstallSteps;
  }
  state.installConfig = res.data?.installConfig || {};
};

// 收起手动安装步骤
const foldInstallAgent = () => {
  showInstallStep.value = false;
};

const turnback = () => {
  router.push({ path: '/config#node' });
};

const enableding = ref(false); // 启用、禁用中
// 启用 禁用
const enableNode = async () => {
  enableding.value = true;
  const [error] = await node.enableNode([{ enabled: !state.infos.enabled, id: id.value }]);
  enableding.value = false;
  if (error) {
    return;
  }
  state.infos.enabled = !state.infos.enabled;
};

// 资源使用 ->cpu 内存 文件系统 交换区
const loadMetrics = async () => {
  const [error, res] = await nodeCtrl.getLatestMetrics(id.value);
  if (error) {
    return;
  }
  const { cvsCpu, cvsFilesystem, cvsMemory } = res.data || {};

  // CPU cvsCpu => idle,sys,user,wait,other,total
  if (cvsCpu) {
    const cpu = +(+cvsCpu.split(',')[5]).toFixed(2);
    const cpuPercent = cpu;
    const cpuTotal = (100 - cpu).toFixed(2);
    sourceUse.cpu = cpu;
    sourceUse.cpuPercent = cpuPercent;
    sourceUse.cpuTotal = cpuTotal;
  }

  // 内存  cvsMemory => free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed
  // 取实际使用值
  if (cvsMemory) {
    const cvsMemorys = cvsMemory.split(',');
    const memoryPercent = +(+cvsMemorys[7]).toFixed(2);
    const memory = +(+cvsMemorys[5]).toFixed(2);
    const memoryTotal = +(+cvsMemorys[4] + +cvsMemorys[5]).toFixed(2);
    sourceUse.memory = formatBytes(memory, 2);
    sourceUse.memoryPercent = memoryPercent;
    sourceUse.memoryTotal = formatBytes(memoryTotal, 2);

    // 交换区
    const swapTotal = (+cvsMemorys[9] || 0) + (+cvsMemorys[8] || 0);
    const swapPercent = +((+cvsMemorys[9] || 0) / (+swapTotal || 1) * 100).toFixed(2);
    const swap = (+cvsMemorys[9] || 0);
    sourceUse.swapTotal = formatBytes(swapTotal, 2);
    sourceUse.swapPercent = swapPercent;
    sourceUse.swap = formatBytes(swap, 2);
  }

  // 文件系统 cvsFilesystem => free,used,avail,usePercent
  if (cvsFilesystem) {
    const cvsFilesystems = cvsFilesystem.split(',');
    const disk = +(+cvsFilesystems[1]).toFixed(2);
    const diskPercent = +(+cvsFilesystems[3]).toFixed(2);
    const diskTotal = +(Number(cvsFilesystems[0]) + Number(cvsFilesystems[1])).toFixed(2);
    sourceUse.disk = formatBytes(disk, 2);
    sourceUse.diskPercent = diskPercent;
    sourceUse.diskTotal = formatBytes(diskTotal, 2);
  }

  if (res.data) {
    const timestamp = res.data?.timestamp;
    const datetime = res.datetime;
    // state.infos.online = true;
    if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
      sourceUse.cpu = 0;
      sourceUse.cpuPercent = 0;
      sourceUse.cpuTotal = 0;
      sourceUse.memory = '0';
      sourceUse.memoryPercent = 0;
      sourceUse.memoryTotal = '0';
      sourceUse.disk = '0';
      sourceUse.diskPercent = 0;
      sourceUse.diskTotal = '0';
      // state.infos.online = false;
    }
  } else {
    // state.infos.online = false;
  }
};

const networkDeviceData = ref<{deviceName: string, networkUsage: {cvsValue: string, timestamp: string}}[]>([]);
const currentDeviceName = ref();
let networdkDatatime = '';

const onDeviceNameChange = (value) => {
  currentDeviceName.value = value;
  setNetworkData();
};

// 资源使用 ->网络
const loadNetwork = async () => {
  const [error, res] = await nodeCtrl.getNetworkInfoMetrics(id.value);
  if (error) {
    return;
  }
  networkDeviceData.value = res.data || [];
  networdkDatatime = res.datetime;
  if (!currentDeviceName.value || !networkDeviceData.value.find(item => item.deviceName === currentDeviceName.value)) {
    currentDeviceName.value = networkDeviceData.value[0]?.deviceName;
  }
  if (!currentDeviceName.value) {
    return;
  }
  setNetworkData();
};

const setNetworkData = () => {
  const currentNetworkUsage = networkDeviceData.value.find(item => item.deviceName === currentDeviceName.value);
  const { cvsValue } = currentNetworkUsage?.networkUsage || {};
  if (!cvsValue) {
    return;
  }
  const cvsValues = cvsValue.split(',');
  sourceUse.rxBytesRate = +(+cvsValues[1]); // 下载
  sourceUse.txBytesRate = +(+cvsValues[4]); // 上传
  sourceUse.rxBytes = formatBytes(+cvsValues[0], 2); // 总下载
  sourceUse.txBytes = formatBytes(+cvsValues[3], 2); // 总上传

  if (currentNetworkUsage?.networkUsage) {
    const timestamp = currentNetworkUsage?.networkUsage?.timestamp;
    const datetime = networdkDatatime;
    if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
      sourceUse.rxBytesRate = 0;
      sourceUse.txBytesRate = 0;
      sourceUse.rxBytes = '0';
      sourceUse.txBytes = '0';
    }
  }
};

// 图表数据 CPU
const loadCpuEchartData = async (data) => {
  // const param = getChartParam();
  // const [error, res] = await nodeCtrl.getCpuData({ id: id.value, ...param });
  // if (error) {
  //   return;
  // }
  // if (pagination.pageNo === 1) {
  //   chartsData = res.data?.list || [];
  // } else {
  //   chartsData = chartsData.concat(res.data?.list || []);
  // }
  // if (chartsData.length < res.data.total) {
  //   pagination.pageNo += 1;
  //   loadCpuEchartData();
  //   return;
  // }

  chartsData = data;
  // 'CPU 空闲百分比', '系统空间占用 CPU 百分比', '用户空间占 CPU 百分比', '等待 IO 操作的 CPU 百分比', '其他占用 CPU 百分比', '当前占用的总 CPU 百分比'
  // const dataType = ['idle', 'sys', 'sys', 'wait', 'other', 'total'];
  const dataType = ['CPU 空闲百分比(%)', '系统空间占用 CPU 百分比(%)', '用户空间占 CPU 百分比(%)', '等待 IO 操作的 CPU 百分比(%)', '其他占用 CPU 百分比(%)', '当前占用的总 CPU 百分比(%)'];
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(),
      name: type
    };
  });

  chartsData.forEach(item => {
    const cpusValues = item.cvsCpu.split(',');
    cpusValues.forEach((val, idx) => {
      seriesData[idx].data.push(+(+val).toFixed(2));
    });
  });
  if (chartsData.length) {
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
          y: 'bottom',
          x: 'center'
        }
      }
    : {};
  myEcahrt.setOption({
    ...echartsOpt,
    ...legend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [60] }] : seriesData
  }, notMerge.value);
};

let memoryEchartOption;
let memoryPercentEchartOption;
let memoryTableData;

// 图表数据 内存
const loadMemoryEchartData = async (data) => {
  // const param = getChartParam();
  // // loadingChartData.value = true;
  // const [error, res] = await nodeCtrl.getMemoryData({ id: id.value, ...param });
  // // '物理内存剩余量', '物理内存使用量', '实际空闲物理内存百分比', '实际使用物理内存的百分比', '实际空闲内存', '实际使用内存', '空闲内存占用的百分比', '使用内存占用的百分比', '交换区使用量', '交换区剩余量'
  const dataType = ['物理内存剩余量(GB)', '物理内存使用量(GB)', '实际空闲物理内存百分比(%)', '实际使用物理内存的百分比(%)', '实际空闲内存(GB)', '实际使用内存(GB)', '空闲内存占用的百分比(%)', '使用内存占用的百分比(%)', '交换区使用量(GB)', '交换区剩余量(GB)'];
  const dataTypeKey = ['free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed', 'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'];
  // if (error) {
  //   return;
  // }
  // if (pagination.pageNo === 1) {
  //   chartsData = res.data?.list || [];
  // } else {
  //   chartsData = chartsData.concat(res.data?.list || []);
  // }
  // if (chartsData.length < res.data.total) {
  //   pagination.pageNo += 1;
  //   loadMemoryEchartData();
  //   return;
  // }
  chartsData = data;
  // loadingChartData.value = false;
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(),
      name: type
    };
  });
  chartsData.forEach(item => {
    // 'free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed', 'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'
    const values = item.cvsMemory.split(',');
    values.forEach((val, idx) => {
      if (dataTypeKey[idx].includes('Percent')) {
        seriesData[idx].data.push(+val);
      } else {
        seriesData[idx].data.push(+formatBytesToUnit(val, 'GB', 2));
      }
    });
  });

  if (chartsData.length) {
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
  if (showMemoryPercentChart.value) {
    tableData.value = memoryTableData.filter(i => i.unit === '%');
  } else {
    tableData.value = memoryTableData.filter(i => i.unit !== '%');
  }

  const seriesPercentData = seriesData.splice(2, 2).concat(seriesData.splice(4, 2));
  const percentDataType = dataType.splice(2, 2).concat(dataType.splice(4, 2));
  const legend = {
    ...defaultLegend,
    data: dataType
  };
  const percentLegend = {
    ...defaultLegend,
    data: percentDataType
  };

  memoryEchartOption = {
    ...echartsOpt,
    legend: legend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesData
  };
  memoryPercentEchartOption = {
    ...echartsOpt,
    legend: percentLegend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: seriesPercentData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesPercentData
  };
  myEcahrt.setOption(showMemoryPercentChart.value ? memoryPercentEchartOption : memoryEchartOption, notMerge.value);
};

let diskChartOption;
let percentChartOption;
let rateChartOption;
let bytesRateChartOption;
let diskTableData;

// 图表数据 磁盘
const loadDiskEchartData = async (data) => {
  // loadingChartData.value = true;
  // const param = getChartParam({ filters: [{ key: 'deviceName', op: 'MATCH_END', value: activeDisk.value }] });
  // const [error, res] = await nodeCtrl.getDiskData({ id: id.value, ...param });
  // if (error) {
  //   return;
  // }
  // if (pagination.pageNo === 1) {
  //   chartsData = res.data?.[0]?.values?.list || [];
  // } else {
  //   chartsData = chartsData.concat(res.data?.[0]?.values?.list || []);
  // }
  // if (res.data?.[0]?.values?.total && chartsData.length < res.data?.[0]?.values?.total) {
  //   pagination.pageNo += 1;
  //   loadDiskEchartData();
  //   return;
  // }
  chartsData = data;
  // loadingChartData.value = false;
  // '磁盘总大小', '本地文件系统剩余大小', '本地文件系统已用大小', '本地文件系统可用大小', '本地文件系统使用率', '每秒磁盘读次数', '每秒磁盘写次数', '每秒磁盘读取 MB 数', '每秒磁盘写入 MB 数'
  const dataTypeKey = ['total', 'free', 'used', 'avail', 'usePercent', 'readsRate', 'writesRate', 'readBytesRate', 'writeBytesRate'];
  const dataType = ['磁盘总大小(GB)', '本地文件系统剩余大小(GB)', '本地文件系统已用大小(GB)', '本地文件系统可用大小(GB)', '本地文件系统使用率(%)', '每秒磁盘读次数(IO/s)', '每秒磁盘写次数(IO/s)', '每秒磁盘读取 MB 数(MB/s)', '每秒磁盘写入 MB 数(MB/s)'];
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(),
      name: type
    };
  });
  chartsData.forEach(item => {
    const diskValues = item.cvsValue.split(',');
    diskValues.forEach((val, idx) => {
      if (dataTypeKey[idx].includes('Percent') || dataTypeKey[idx].includes('BytesRate') || dataTypeKey[idx].includes('Rate')) {
        seriesData[idx].data.push(+val);
      } else {
        seriesData[idx].data.push(+formatBytesToUnit(val, 'GB', 2));
      }
    });
  });
  if (chartsData.length) {
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
    case 'disk':
      tableData.value = diskTableData.filter(i => i.unit === 'GB');
      break;
    case 'percent':
      tableData.value = diskTableData.filter(i => i.unit === '%');
      break;
    case 'rate':
      tableData.value = diskTableData.filter(i => i.unit === 'IO/s');
      break;
    case 'bytesRate':
      tableData.value = diskTableData.filter(i => i.unit === 'MB/s');
      break;
    default:
      tableData.value = [];
  }
  const percentSeriesData = seriesData.splice(4, 1);
  const percentDataType = dataType.splice(4, 1);
  const percentLegend = { ...defaultLegend, data: percentDataType };
  percentChartOption = {
    ...echartsOpt,
    legend: percentLegend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: percentSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : percentSeriesData
  };

  const rateSeriesData = seriesData.splice(4, 2);
  const rateDataType = dataType.splice(4, 2);
  const rateLegend = { ...defaultLegend, data: rateDataType };
  rateChartOption = {
    ...echartsOpt,
    legend: rateLegend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: rateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : rateSeriesData
  };

  const bytesRateSeriesData = seriesData.splice(4, 2);
  const bytesRateDataType = dataType.splice(4, 2);
  const bytesLegend = { ...defaultLegend, data: bytesRateDataType };
  bytesRateChartOption = {
    ...echartsOpt,
    legend: bytesLegend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: bytesRateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : bytesRateSeriesData
  };
  const legend = { ...defaultLegend, data: dataType };
  diskChartOption = {
    ...echartsOpt,
    legend: legend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesData
  };
  let showEchartOptions;
  if (diskChartKey.value === 'disk') {
    showEchartOptions = diskChartOption;
  }
  if (diskChartKey.value === 'percent') {
    showEchartOptions = percentChartOption;
  }
  if (diskChartKey.value === 'rate') {
    showEchartOptions = rateChartOption;
  }
  if (diskChartKey.value === 'bytesRate') {
    showEchartOptions = bytesRateChartOption;
  }
  myEcahrt.setOption(showEchartOptions, notMerge.value);
};

let networkChartOption;
let bytesChartOption;
let packetChartOption;
let networkTableData;

// 图表数据 网络
const loadNetworkEchartData = async (data) => {
  // loadingChartData.value = true;
  // const param = getChartParam({ filters: [{ key: 'deviceName', op: 'MATCH_END', value: activeNetwork.value }] });
  // const [error, res] = await nodeCtrl.getNetworkData({ id: id.value, ...param });
  // if (error) {
  //   return;
  // }
  // if (pagination.pageNo === 1) {
  //   chartsData = res.data?.[0]?.values?.list || [];
  // } else {
  //   chartsData = chartsData.concat(res.data?.[0]?.values?.list || []);
  // }
  // if (res.data?.[0]?.values?.total && chartsData.length < res.data?.[0]?.values?.total) {
  //   pagination.pageNo += 1;
  //   loadNetworkEchartData();
  // }
  chartsData = data;
  // loadingChartData.value = false;
  // '接收到的总字节数', '每秒接收的 MB 数', '接收到的错误包数', '发送的总字节数', '每秒发送的 MB 数'
  const dataTypeKey = ['rxBytes', 'rxBytesRate', 'rxErrors', 'txBytes', 'txBytesRate'];
  const dataType = ['接收到的总字节(GB)', '每秒接收的 MB 数(MB/s)', '接收到的错误包数(packets)', '发送的总字节(GB)', '每秒发送的 MB 数(MB/s)'];
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(),
      name: type
    };
  });
  chartsData.forEach(item => {
    const valus = item.cvsValue.split(',');
    valus.forEach((val, idx) => {
      if (dataTypeKey[idx].includes('BytesRate')) {
        seriesData[idx].data.push(+val);
      } else if (dataTypeKey[idx].includes('Bytes')) {
        seriesData[idx].data.push(+formatBytesToUnit(val, 'GB', 2));
      } else {
        seriesData[idx].data.push(+val);
      }
    });
  });
  if (chartsData.length) {
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

  if (networkChartKey.value === 'network') {
    tableData.value = networkTableData.filter(i => i.unit === 'MB/s');
  } else if (networkChartKey.value === 'packet') {
    tableData.value = networkTableData.filter(i => i.unit === 'packets');
  } else {
    tableData.value = networkTableData.filter(i => i.unit === 'GB');
  }

  const networkSeriesData = seriesData.splice(1, 1).concat(seriesData.splice(3, 1));
  const networkDataTypeKey = dataType.splice(1, 1).concat(dataType.splice(3, 1));
  const networdLengend = { ...defaultLegend, data: networkDataTypeKey };
  networkChartOption = {
    ...echartsOpt,
    legend: networdLengend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: networkSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : networkSeriesData
  };

  const packetSeriesData = seriesData.splice(1, 1);
  const packetDataTypeKey = dataType.splice(1, 1);
  const packetLengend = { ...defaultLegend, data: packetDataTypeKey };
  packetChartOption = {
    ...echartsOpt,
    legend: packetLengend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: packetSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : packetSeriesData
  };
  const legend = { ...defaultLegend, data: dataType };
  bytesChartOption = {
    ...echartsOpt,
    legend: legend,
    xAxis: [
      {
        data: chartsData.map(i => i.timestamp)
      }
    ],
    series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesData
  };
  let showChartOption;
  if (networkChartKey.value === 'network') {
    showChartOption = networkChartOption;
  }
  if (networkChartKey.value === 'packet') {
    showChartOption = packetChartOption;
  }
  if (networkChartKey.value === 'bytes') {
    showChartOption = bytesChartOption;
  }
  myEcahrt.setOption(showChartOption, notMerge.value);
};

const showNoData = ref(false);
// 图表数据变更
const onChartDataChange = (data) => {
  if (!data.length) {
    showNoData.value = true;
  } else {
    showNoData.value = false;
  }
  if (activeTab.value === 'cpu') {
    loadCpuEchartData(data);
  }
  if (activeTab.value === 'memory') {
    loadMemoryEchartData(data);
  }
  if (activeTab.value === 'disk') {
    loadDiskEchartData(data);
  }
  if (activeTab.value === 'network') {
    loadNetworkEchartData(data);
  }
};

const loadEchartData = async () => {
  if (activeTab.value === 'cpu') {
    // loadCpuEchartData();
    currentSourceServer.value = chartServersMap.cpu;
    chartParams.value = {
      orderSort: 'ASC'
    };
  }
  if (activeTab.value === 'memory') {
    // loadMemoryEchartData();
    currentSourceServer.value = chartServersMap.memory;
    chartParams.value = {
      orderSort: 'ASC'
    };
  }
  if (activeTab.value === 'disk') {
    if (!diskNames.value.length) {
      // loadingChartData.value = true;
      const [error, res] = await nodeCtrl.getDiskInfoMetrics(id.value);
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
    chartParams.value = {
      orderSort: 'ASC',
      filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value }]
    };
    currentSourceServer.value = chartServersMap.disk;
    // loadDiskEchartData();
  }

  if (activeTab.value === 'network') {
    if (!networkNames.value.length) {
      // loadingChartData.value = true;
      const [error, res] = await nodeCtrl.getNetworkInfoMetrics(id.value);
      if (error) {
        return;
      }
      const _networkNames: {label: string, value: string}[] = [];
      const networkNamesMp = {};
      for (const item of (res.data || [])) {
        if (!networkNamesMp[item.deviceName]) {
          _networkNames.push({ label: item.deviceName, value: item.deviceName });
          networkNamesMp[item.deviceName] = true;
        }
      }

      networkNames.value = _networkNames;
      activeNetwork.value = networkNames.value?.[0]?.value;
    }
    chartParams.value = {
      orderSort: 'ASC',
      filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }]
    };
    currentSourceServer.value = chartServersMap.network;
    // loadNetworkEchartData();
  }
};

let timer; // 定时刷新数据
const refreshTimer = () => {
  timer && clearInterval(timer);
  timer = setInterval(() => {
    if (activeKey.value !== 'source') {
      return;
    }
    loadMetrics();
    loadNetwork();
    // if (timestamp.value && aotuRefresh.value) {
    //   resetTimestamp();
    // }
  }, 5000);
};

const proxyActiveKey = ref('proxy');
const proxyOpt = [
  {
    label: '代理信息',
    value: 'proxy'
  },
  {
    label: '日志',
    value: 'log'
  }
];

// const resetTimestamp = () => {
//   date.value = [] as string[];
//   const subTime = (timestamp.value as string).split('-');
//   const now = dayjs();
//   timestempEnd.value = now.format('YYYY-MM-DD HH:mm:ss');
//   timestempStart.value = now.subtract(+subTime[0], subTime[1] as ManipulateType).format('YYYY-MM-DD HH:mm:ss');
//   notMerge.value = false;
// };

watch(() => showMemoryPercentChart.value, () => {
  if (memoryPercentEchartOption || memoryEchartOption) {
    myEcahrt.setOption(showMemoryPercentChart.value ? memoryPercentEchartOption : memoryEchartOption, true);
    if (showMemoryPercentChart.value) {
      tableData.value = memoryTableData.filter(i => i.unit === '%');
    } else {
      tableData.value = memoryTableData.filter(i => i.unit !== '%');
    }
  }
});

watch(() => diskChartKey.value, () => {
  let showEchartOptions;

  if (diskChartKey.value === 'disk') {
    showEchartOptions = diskChartOption;
    tableData.value = diskTableData.filter(i => i.unit === 'GB');
  }
  if (diskChartKey.value === 'percent') {
    showEchartOptions = percentChartOption;
    tableData.value = diskTableData.filter(i => i.unit === '%');
  }
  if (diskChartKey.value === 'rate') {
    showEchartOptions = rateChartOption;
    tableData.value = diskTableData.filter(i => i.unit === 'IO/s');
  }
  if (diskChartKey.value === 'bytesRate') {
    showEchartOptions = bytesRateChartOption;
    tableData.value = diskTableData.filter(i => i.unit === 'MB/s');
  }
  if (showEchartOptions) {
    myEcahrt.setOption(showEchartOptions, true);
  }
});

watch(() => networkChartKey.value, () => {
  let showChartOption;
  if (networkChartKey.value === 'network') {
    showChartOption = networkChartOption;
    tableData.value = networkTableData.filter(i => i.unit === 'MB/s');
  }
  if (networkChartKey.value === 'packet') {
    showChartOption = packetChartOption;
    tableData.value = networkTableData.filter(i => i.unit === 'packets');
  }
  if (networkChartKey.value === 'bytes') {
    showChartOption = bytesChartOption;
    tableData.value = networkTableData.filter(i => i.unit === 'GB');
  }
  if (showChartOption) {
    myEcahrt.setOption(showChartOption, true);
  }
});

const isAdmin = inject('isAdmin', ref(false));
const tenantInfo = inject('tenantInfo', ref({}));
onMounted(async () => {
  id.value = route.params.id as string;
  loadInfo();
  initEcahrts();
  loadMetrics();
  loadNetwork();
  timestamp.value = '5-minute';
  currentSourceServer.value = chartServersMap.cpu;
  refreshTimer();
});

const getOnlineInstallTip = (node) => {
  if (node.infos.geAgentInstallationCmd) {
    return '已安装';
  }
  if (!isAdmin.value) {
    return '安装代理需要系统管理员或应用管理员权限';
  }
};

const delNode = () => {
  modal.confirm({
    content: `确定删除节点【${state.infos.name}】吗?`,
    onOk: async () => {
      const [error] = await node.deleteNode({ ids: [id.value] });
      if (error) {
        return;
      }
      turnback();
    }
  });
};

// 删除按钮禁用提示
const getDelTip = (node) => {
  if (node.enabled) {
    return '节点禁用后才能删除';
  }
  if (!isAdmin.value) {
    return '删除节点需要系统管理员或应用管理员权限';
  }
  if (node.source?.value !== 'OWN_NODE') {
    return '只能删除自有节点';
  }
};

onBeforeUnmount(() => {
  timer && clearInterval(timer);
});

// 监听 tab 变更
watch([() => activeTab.value, () => activeDisk.value, () => activeNetwork.value], () => {
  // pagination.pageNo = 1;
  // refreshTimer();
  notMerge.value = true;
  loadEchartData();
});

// 监听时间段变化
// watch(() => timestamp.value, (newValue) => {
//   if (newValue) {
//     date.value = [] as string[];
//     const subTime = newValue.split('-');
//     const now = dayjs();
//     timestempEnd.value = now.format('YYYY-MM-DD HH:mm:ss');
//     timestempStart.value = now.subtract(+subTime[0], subTime[1] as ManipulateType).format('YYYY-MM-DD HH:mm:ss');
//   } else if (!date.value.length) {
//     timestempStart.value = '';
//     timestempEnd.value = '';
//   }
//   notMerge.value = true;
// });

// 监听时间段变化
// watch(() => date.value, newValue => {
//   if (newValue?.length) {
//     timestempStart.value = newValue[0];
//     timestempEnd.value = newValue[1];
//     notMerge.value = true;
//     timestamp.value = undefined;
//   } else if (!timestamp.value) {
//     timestamp.value = '5-minute';
//   }
// });

// watch([() => timestempStart.value, () => timestempEnd.value], () => {
//   pagination.pageNo = 1;
//   refreshTimer();
//   loadEchartData();
// });

const showInstallCtrlAccessToken = ref(false);
const toggleShowCtrlAccessToken = () => {
  showInstallCtrlAccessToken.value = !showInstallCtrlAccessToken.value;
};

const copyContent = (text) => {
  clipboard.toClipboard(text).then(() => {
    notification.success('已复制到剪贴板');
  });
};

const activeKey = ref<'source' | 'proxy'>('source');
</script>

<template>
  <div class="h-full overflow-auto">
    <Spin :spinning="loadingInfo" mask>
      <div class="bg-white px-10 text-3">
        <!-- 基本信息 -->
        <div class="basic">
          <div class="title flex justify-between pt-2.5 items-center">
            <span class="mr-15 font-semibold">基本信息</span>
            <div class="detai-btns-wrapper">
              <Button
                v-if="!state.infos.enabled"
                class="node-action-btn"
                :loading="enableding"
                :disabled="state.infos?.tenantId !== tenantInfo.tenantId || !(isAdmin || state.infos?.createdBy === tenantInfo.id)"
                @click="enableNode">
                <Icon icon="icon-qiyong" />启用
              </Button>
              <Button
                v-else
                class="node-action-btn"
                :loading="enableding"
                :disabled="state.infos?.tenantId !== tenantInfo.tenantId || !(isAdmin || state.infos?.createdBy === tenantInfo.id)"
                @click="enableNode">
                <Icon icon="icon-jinyong" />禁用
              </Button>
              <Tooltip v-if="state.infos?.tenantId !== tenantInfo.tenantId || !(isAdmin || state.infos?.createdBy === tenantInfo.id) || state.infos?.enabled" :title="getDelTip(state.infos)">
                <Button
                  class="node-action-btn"
                  :disabled="true">
                  <Icon icon="icon-qingchu" class="mr-1" />
                  <span>删除</span>
                </Button>
              </Tooltip>
              <Button
                v-else
                class="node-action-btn"
                @click="delNode()">
                <Icon icon="icon-qingchu" class="mr-1" />
                <span>删除</span>
              </Button>
              <Tooltip v-if="state.infos.installNodeAgent || state.infos.free || !isAdmin" :title="getOnlineInstallTip(state)">
                <Button
                  :disabled="true"
                  :loading="installing"
                  class="node-action-btn"
                  @click="installAgen">
                  <Icon icon="icon-anzhuangdaili" />
                  在线安装代理
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="state.infos.installNodeAgent || !isAdmin"
                :loading="installing"
                class="node-action-btn"
                @click="installAgen">
                <Icon icon="icon-anzhuangdaili" />在线安装代理
                <Hints
                  v-if="installing"
                  class="absolute left-5 -bottom-3"
                  text="预计需要一分钟" />
              </Button>
              <Tooltip v-if="!isAdmin || state.infos.free" :title="!isAdmin ? `安装代理需要系统管理员或应用管理员权限` : ''">
                <Button
                  :disabled="true"
                  class="node-action-btn"
                  @click="getInstallStep">
                  <Icon icon="icon-anzhuangdaili" />手动安装代理
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="!isAdmin"
                class="node-action-btn"
                @click="getInstallStep">
                <Icon icon="icon-anzhuangdaili" />手动安装代理
              </Button>
              <Button class="node-action-btn" @click="turnback">
                <Icon icon="icon-fanhui" />返回
              </Button>
            </div>
          </div>
          <!-- 手动安装步骤 -->
          <div v-show="showInstallStep" class="pt-4 mb-4 relative">
            <Tabs size="small">
              <TabPane key="linux" tab="Linux或者Mac系统自动安装步骤">
                <div class="text-3">必须以root用户执行脚本, 安装目录为脚本所在目录</div>
                <div class="text-3">
                  安装方式1：<Icon icon="icon-fuzhi" class="cursor-pointer text-3.5 text-blue-1" @click="copyContent(state.linuxOfflineInstallSteps?.onlineInstallCmd)" />
                  <p class="install-step whitespace-pre-line">
                    {{ state.linuxOfflineInstallSteps?.onlineInstallCmd }}
                  </p>
                  安装方式2：
                  <p class="install-step whitespace-pre-line">
                    1).下载自动安装脚本：<a class="cursor-pointer" :href="state.linuxOfflineInstallSteps?.installScriptUrl">{{ state.linuxOfflineInstallSteps?.installScriptName }}</a> <br />
                    2).将{{ state.linuxOfflineInstallSteps?.installScriptName }}复制到自定义的安装目录，运行脚本安装：<br />
                    {{ state.linuxOfflineInstallSteps?.runInstallCmd }}
                  </p>
                </div>
              </TabPane>
              <!--              <TabPane key="windows" tab="Windows系统自动安装步骤">-->
              <!--                <div class="text-3">-->
              <!--                  安装方式1：-->
              <!--                  <p class="install-step whitespace-pre-line">-->
              <!--                    {{ state.windowsOfflineInstallSteps?.onlineInstallCmd }}-->
              <!--                  </p>-->
              <!--                  安装方式2：-->
              <!--                  <p class="install-step whitespace-pre-line">-->
              <!--                    1).下载自动安装脚本：<a class="cursor-pointer" :href="state.windowsOfflineInstallSteps?.installScriptUrl">{{ state.windowsOfflineInstallSteps?.installScriptName }}</a> <br />-->
              <!--                    2).将{{ state.windowsOfflineInstallSteps?.installScriptName }}复制到自定义的安装目录，运行脚本安装：<br /> { state.windowsOfflineInstallSteps?.runInstallCmd }}-->
              <!--                  </p>-->
              <!--                </div>-->
              <!--              </TabPane>-->
              <TabPane key="config" tab="安装配置信息">
                <Grid
                  :dataSource="state.installConfig"
                  :columns="installConfigColumns">
                  <template #tenantId="{text}">
                    <div class="flex items-center">
                      <span>{{ text }}</span> <Button
                        type="link"
                        size="small"
                        @click="copyContent(text)">
                        <Icon icon="icon-fuzhi" class="ml-0.5" />
                      </Button>
                    </div>
                  </template>
                  <template #deviceId="{text}">
                    <div class="flex items-center">
                      <span>{{ text }}</span> <Button
                        type="link"
                        size="small"
                        @click="copyContent(text)">
                        <Icon icon="icon-fuzhi" class="ml-0.5" />
                      </Button>
                    </div>
                  </template>
                  <template #ctrlAccessToken="{text}">
                    <div class="flex items-center">
                      <span>{{ showInstallCtrlAccessToken ? text : '******' }}</span>
                      <Button
                        size="small"
                        type="link"
                        class="leading-5 h-5"
                        @click="toggleShowCtrlAccessToken">
                        <Icon class="text-4" :icon="showInstallCtrlAccessToken ? 'icon-biyan' : 'icon-zhengyan'" />
                      </Button>
                    </div>
                  </template>
                </Grid>
              </TabPane>
            </Tabs>
            <Button
              class="absolute right-0 top-0 text-3"
              type="link"
              size="small"
              @click="foldInstallAgent">
              收起
            </Button>
          </div>
          <Grid
            class="py-4 status-wrapper"
            font-size="12px"
            :dataSource="state.infos"
            :columns="infoItem">
            <template #password="{text}">
              <template v-if="showPassd">
                <span class="text-black-active align-middle">{{ text }}</span>
                <Icon
                  icon="icon-zhengyan"
                  class="text-3 cursor-pointer align-middle ml-2"
                  @click="handleShowPassd" />
              </template>
              <template v-else>
                <div class="inline-flex items-center">
                  <span class="text-black-active">******</span>
                  <!-- <Icon
                    icon="icon-biyan"
                    class="text-3 cursor-pointer ml-2"
                    @click="handleShowPassd" /> -->
                </div>
              </template>
            </template>
            <template #spec="{record}">
              <div v-if="!!record.text">
                {{ record.text?.showLabel }}
                <!-- {{ `${record.text?.cpu}/${record.text?.memory}/${record.text?.disk}/${record.text?.network || ''}` }} -->
              </div>
            </template>
            <template #roles="{text}">
              <template v-if="text?.length">
                <span
                  v-for="item in text"
                  :key="item.value"
                  class="mr-1 border px-1.5 rounded-full py-1">{{ item.message }}</span>
              </template>
              <template v-else>--</template>
            </template>
            <template #enabled="{text}">
              <span class="status flex items-center" :class="{'success': text, 'fail': !text}">{{ text ? '启用' : '禁用' }}</span>
            </template>
            <template #installAgent="{text}">
              <span class="status  flex items-center" :class="{'success': text, 'fail': !text}">{{ text ? '已安装' : '未安装' }}</span>
            </template>
            <template #online="{text}">
              <span class="status  flex items-center" :class="{'success': text, 'fail': !text}">{{ text ? '已连接' : '未连接' }}</span>
            </template>
          </Grid>
        </div>
        <!-- 资源使用 -->
        <div class="source">
          <Tabs v-model:activeKey="activeKey" size="small">
            <TabPane key="source" forceRender>
              <template #tab><span class="font-semibold">资源监控</span></template>
              <!-- <div class="title flex pt-4">
                <span class="mr-15 font-semibold">资源使用</span>
              </div> -->
              <ul class="flex pb-5 justify-between text-3">
                <li
                  v-for="item in nodeUseProgresses"
                  :key="item.valueKey"
                  class="inline-flex">
                  <div class="rounded flex border py-2.5 px-3.5">
                    <Progress
                      v-show="item.valueKey!=='network'"
                      type="circle"
                      :percent="sourceUse[item.percentValue]"
                      :width="60"
                      :strokeColor="getStrokeColor(sourceUse[item.percentValue])">
                      <template #format>
                        <span v-if="item.valueKey==='network'"></span>
                        <span v-else style="font-size: 12px; font-weight: bold;">{{ sourceUse[item.percentValue] }}%</span>
                      </template>
                    </Progress>
                    <div v-if="item.valueKey !== 'network'" class="pl-5 w-35">
                      <span class="text-3">{{ item.label }}</span>
                      <div class="leading-5">
                        <label class="inline-block w-12 text-text-content">已使用:</label>
                        <span class="text-black-active ">{{ sourceUse[item.valueKey] }}{{ item.unit }}</span>
                      </div>
                      <div class="leading-5">
                        <label class="inline-block w-12 text-text-content">{{ item.valueKey === 'cpu' ? '空闲' : '总共' }}:</label>
                        <span class="text-black-active ">{{ sourceUse[item.totalKey] }}{{ item.unit }}</span>
                      </div>
                    </div>
                    <div v-else class="pl-5 w-80">
                      <div class="flex justify-between items-center">
                        <span class="text-3 ">{{ item.label }}</span>
                        <Select
                          v-model:value="currentDeviceName"
                          :options="networkDeviceData"
                          :fieldNames="{value: 'deviceName', label: 'deviceName'}"
                          class="min-w-25 device-select"
                          @change="onDeviceNameChange" />
                      </div>
                      <div class="flex w-full flex-wrap">
                        <div
                          v-for="info in internetInfo"
                          :key="info.valueKey"
                          class="leading-5 w-1/2">
                          <label class="inline-block w-12 text-text-content">{{ info.label }}</label>
                          <span class="text-black-active ">{{ sourceUse[info.valueKey] }} {{ info.unit }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
              <div class="flex justify-between">
                <div>
                  <RadioGroup
                    v-model:value="activeTab"
                    buttonStyle="solid"
                    class="node-tab">
                    <RadioButton
                      v-for="radio in nodeEchartsTabs"
                      :key="radio.valueKey"
                      :value="radio.valueKey">
                      {{ radio.label }}
                    </RadioButton>
                  </RadioGroup>
                  <Select
                    v-show="activeTab === 'disk'"
                    v-model:value="activeDisk"
                    class="ml-2 min-w-25"
                    size="small"
                    :options="diskNames" />
                  <!-- <RadioGroup v-show="activeTab === 'disk'"
                            v-model:value="activeDisk"
                            class="ml-2"
                            :options="diskNames" /> -->
                  <Select
                    v-show="activeTab === 'network'"
                    v-model:value="activeNetwork"
                    class="ml-2 min-w-25"
                    size="small"
                    :options="networkNames" />
                  <!-- <RadioGroup v-show="activeTab === 'network'"
                            v-model:value="activeNetwork"
                            class="ml-2"
                            :options="networkNames" /> -->
                </div>
                <div class="flex items-center">
                  <!-- <span>自动刷新</span>
            <Switch
              v-model:checked="aotuRefresh"
              size="small"
              class="mx-2"></Switch>
            <Select
              v-model:value="timestamp"
              :options="timeOpt"
              class="w-40"
              size="small"
              :allowClear="false" />
            <DatePicker
              v-model:value="date"
              valueType="multiple"
              class="ml-2 py-0.5 px-1.75" /> -->
                  <template v-if="activeKey === 'source'">
                    <IntervalTimestamp
                      v-model:loading="loadingChart"
                      :action="currentSourceServer"
                      :params="chartParams"
                      @change="onChartDataChange" />
                  </template>
                </div>
              </div>
              <RadioGroup
                v-show="activeTab==='memory'"
                v-model:value="showMemoryPercentChart"
                class="mt-3 block text-center"
                size="small"
                :options="memoryDataOptions" />
              <RadioGroup
                v-show="activeTab==='disk'"
                v-model:value="diskChartKey"
                class="mt-3 block text-center"
                size="small"
                :options="diskDataOptions" />
              <RadioGroup
                v-show="activeTab === 'network'"
                v-model:value="networkChartKey"
                class="mt-3 block text-center"
                size="small"
                :options="networkDataOptions" />

              <Spin
                :spinning="loadingChart"
                mask
                :delay="0">
                <div ref="echartRef" class="w-full h-65 mt-7.5">
                </div>
                <NoData
                  v-if="showNoData"
                  class="absolute top-0 left-1/2 -translate-x-1/2" />
              </Spin>
              <!-- table -->
              <!-- <Table
                v-if="!!tableData.length"
                :columns="columns"
                :pagination="false"
                :dataSource="tableData"
                size="small"
                class="mb-20 mt-7.5">
              </Table> -->
            </TabPane>
            <TabPane key="proxy">
              <template #tab><span class="font-semibold">代理服务</span></template>
              <RadioGroup
                v-model:value="proxyActiveKey"
                :options="proxyOpt"
                size="small"
                buttonStyle="solid"
                optionType="button">
              </RadioGroup>
              <template v-if="proxyActiveKey==='proxy'">
                <AgentInfo :ip="state.infos.publicIp || state.infos.ip" :port="state.infos.port" />
                <AgentChart :id="id" />
              </template>
              <template v-if="proxyActiveKey==='log'">
                <Log
                  class="mt-4"
                  :ip="state.infos.publicIp || state.infos.ip"
                  :port="state.infos.agentPort" />
              </template>
            </TabPane>
            <template v-if="state.infos?.rolesValues?.includes('EXECUTION')">
              <TabPane key="execTask">
                <template #tab><span class="font-semibold">执行中任务</span></template>
                <Execute :id="id" />
              </TabPane>
              <TabPane key="execPropulsion">
                <template #tab><span class="font-semibold">执行器进程</span></template>
                <ExecPropulsion :nodeId="id" :tenantId="state.infos?.tenantId" />
              </TabPane>
            </template>
            <template v-if="state.infos?.rolesValues?.includes('MOCK_SERVICE')">
              <TabPane key="mock">
                <template #tab><span class="font-semibold">Mock服务实例</span></template>
                <MockService :nodeId="id" />
              </TabPane>
            </template>
          </Tabs>
        </div>
      </div>
    </Spin>
  </div>
</template>
<style scoped>
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

.device-select {
  @apply h-5;
}

:deep(.device-select) > .ant-select-selector {
  @apply !h-full;
}

:deep(.device-select) > .ant-select-selector >.ant-select-selection-item {
  @apply !leading-5;
}

.status-wrapper > label {
  @apply text-text-content mr-2;
}

.status-wrapper .status {
  @apply text-black-active  mr-10;
}

.status-wrapper .status::before {
  @apply w-1.5 h-1.5 rounded-full mr-2 relative inline-block;

  content: "";
}

.status-wrapper .success::before {
  @apply bg-status-success;
}

.status-wrapper .fail::before {
  @apply bg-status-error;
}

.node-tab .ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked) {
  @apply bg-gray-light;
}

.detai-btns-wrapper button {
  @apply border-0 shadow-none text-text-content;
}

.detai-btns-wrapper button > :deep(span) {
  @apply ml-2;
}

.node-tab.ant-radio-group .ant-radio-button-wrapper {
  @apply border-0 text-3 leading-6 h-6;
}

.node-tab.ant-radio-group .ant-radio-button-wrapper::before {
  @apply hidden;
}

.node-tab.ant-radio-group .ant-radio-button-wrapper:focus-within {
  @apply shadow-none;
}

.ant-table-wrapper :deep(.ant-table-tbody),
.ant-table-wrapper :deep(.ant-table-thead) {
  @apply text-3;
}

.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-tbody > tr > td,
.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-thead > tr > th {
  @apply bg-white border-t-0;
}

.ant-radio-group.ant-radio-group-small :deep(.ant-radio-wrapper) {
  @apply text-3;
}

.install-step {
  @apply px-3 py-1.5 my-2 leading-6;

  background-color: #f6f6f6;
}


</style>
