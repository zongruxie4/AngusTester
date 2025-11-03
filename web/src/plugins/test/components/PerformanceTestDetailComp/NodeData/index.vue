
<script lang="ts" setup>
import { ref, onMounted, watch, computed, onBeforeUnmount } from 'vue';
import { RadioGroup, RadioButton, Select, Slider, Table } from 'ant-design-vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';
import { NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { nodeCtrl } from '@/api/ctrl';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Node item interface
 */
interface NodeItem {
  agentPort: string;  // Agent service port
  domain: string;     // Node domain
  id: string;         // Node unique identifier
  ip: string;         // Node IP address
  name: string;       // Node display name
}

/**
 * Table data row interface
 */
interface TableDataRow {
  name: string;    // Metric name
  unit: string;    // Unit of measurement
  average: string; // Average value
  high: string;    // Maximum value
  low: string;     // Minimum value
  latest: string;  // Latest value
}

/**
 * Component props interface
 */
interface Props {
  execNodes: NodeItem[];      // Execution node options
  appNodes: NodeItem[];       // Application node options
  startTime: string;          // Test start time
  endTime: string;            // Test end time (empty if still running)
  reportInterval: string;     // Data collection interval (e.g., "30s", "1min")
  status: string;             // Execution status (CREATED, PENDING, RUNNING, etc.)
  delayInSeconds: number;     // Polling interval in milliseconds for auto-refresh
  activeChart: string;        // Currently active chart type
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  execNodes: () => ([]),
  appNodes: () => ([]),
  startTime: '',
  endTime: '',
  delayInSeconds: 3000
});

/**
 * ECharts instance reference
 */
let myEcahrt: echarts.ECharts;

/**
 * Table data for displaying metric statistics
 */
const tableData = ref<TableDataRow[]>([]);

/**
 * Table column definitions
 */
const columns = [
  { title: t('commonPlugin.node.columns.name'), dataIndex: 'name', key: 'name' },
  { title: t('commonPlugin.node.columns.unit'), dataIndex: 'unit', key: 'unit' },
  { title: t('commonPlugin.node.columns.average'), dataIndex: 'average', key: 'average' },
  { title: t('commonPlugin.node.columns.high'), dataIndex: 'high', key: 'high' },
  { title: t('commonPlugin.node.columns.low'), dataIndex: 'low', key: 'low' },
  { title: t('commonPlugin.node.columns.latest'), dataIndex: 'latest', key: 'latest' }
];

/**
 * Whether to merge chart data or replace entirely
 * true = replace (initial load), false = merge (updates)
 */
const notMerge = ref(true);

/**
 * Default legend configuration for all charts
 */
const defaultLegend = {
  type: 'plain',
  data: [],
  y: 'top',
  x: 'center'
};

/**
 * Base ECharts configuration options
 * Applied to all metric charts with customizations per type
 */
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

/**
 * Reference to chart DOM element
 */
const echartRef = ref<HTMLDivElement>();

/**
 * Initialize ECharts instance
 * Creates the chart with base configuration
 */
const initEcahrts = (): void => {
  if (echartRef.value) {
    myEcahrt = echarts.init(echartRef.value!);
    myEcahrt.setOption(echartsOpt);
  }
};

/**
 * Get default line series configuration
 * Returns a smooth line chart config with minimal styling
 * 
 * @returns Line series configuration object
 */
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

/**
 * Tab configuration for different metric types
 */
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

/**
 * State Management
 */
// Currently active metric tab (cpu, memory, disk, network)
const activeTab = ref('cpu');

// Node type selection (exec or apply)
const nodeType = ref('exec');

// Currently selected node ID
const currrentNodeId = ref<string>();

/**
 * Get node list based on selected node type
 * Returns either execution nodes or application nodes
 */
const nodeList = computed(() => {
  if (nodeType.value === 'apply') {
    return props.appNodes;
  }
  return props.execNodes;
});

/**
 * Watch node list changes
 * Auto-select first node when list changes
 */
watch(() => nodeList.value, () => {
  currrentNodeId.value = nodeList.value[0]?.id;
});

/**
 * Time Range Slider State (per metric type)
 * Each metric type maintains its own slider state for independent time range selection
 */

// CPU metric slider state
const sliderValueCpu = ref<number[]>([]);          // Current slider range [start, end]
let sliderMaxCpu: number | undefined;              // Maximum slider value
const timesCpu = ref<string[]>([]);                // Timestamp array

// Memory metric slider state
const sliderValueMemory = ref<number[]>([]);       // Current slider range [start, end]
let sliderMaxMemory: number | undefined;           // Maximum slider value
const timesMemory = ref<string[]>([]);             // Timestamp array

// Disk metric slider state
const sliderValueDisk = ref<number[]>([]);         // Current slider range [start, end]
let sliderMaxDisk: number | undefined;             // Maximum slider value
const timesDisk = ref<string[]>([]);               // Timestamp array

// Network metric slider state
const sliderValueNetwork = ref<number[]>([]);      // Current slider range [start, end]
let sliderMaxNetwork: number | undefined;          // Maximum slider value
const timesNetwork = ref<string[]>([]);            // Timestamp array

/**
 * Get current active tab's timestamp array
 * Returns timestamps for the currently selected metric type
 * 
 * @returns Array of timestamp strings
 */
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

/**
 * Configure time range slider for active metric type
 * Initializes slider state or updates range when new data arrives
 * Auto-extends slider if currently at max and test is still running
 * 
 * @param reset - Whether to reset slider to full range
 */
const setSliderConfig = (reset = false): void => {
  if (activeTab.value === 'cpu') {
    if (!cpuChartData.length) {
      return;
    }
    
    // Initialize slider on first load
    if (!sliderValueCpu.value.length) {
      timesCpu.value = cpuChartData.map(i => i.timestamp);
      sliderValueCpu.value = [0, timesCpu.value.length - 1];
      sliderMaxCpu = timesCpu.value.length - 1;
    } else {
      // Update timestamps and auto-extend if at max
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
    // Network
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

/**
 * Pagination configuration for metric data fetching
 * Large page size to minimize requests
 */
const pagination = {
  pageSize: 1000,  // Fetch 1000 records per request
  pageNo: 1        // Current page number
};

/**
 * Build API request parameters for chart data
 * Constructs time range filters based on test start/end times
 * 
 * @param params - Additional parameters (e.g., device filters)
 * @returns Request parameters object with pagination and filters
 */
const getChartParam = (params: any = {}) => {
  const startTime = dayjs(props.startTime).format(DATE_TIME_FORMAT);
  const [interval, unit] = splitDuration(props.reportInterval);
  
  // Calculate end time: test end time + interval, or start + 1 day if still running
  const endTime = props.endTime
    ? dayjs(props.endTime).add(+interval, unit as any).format(DATE_TIME_FORMAT)
    : dayjs(props.startTime).add(+interval, unit as any).add(1, 'day').format(DATE_TIME_FORMAT);
  
  // Build time range filters
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

/**
 * CPU Metrics State
 */
let cpuChartData: any[] = [];           // Raw CPU metrics data from API
const cpuloaded = ref(false);           // Whether CPU data has been loaded

/**
 * Load CPU metrics data from API
 * Fetches data with pagination, recursively loads all pages
 * Removes duplicate timestamps and initializes chart
 */
const loadCpuEchartData = async (): Promise<void> => {
  const param = getChartParam();
  const [error, res] = await nodeCtrl.getCpuMetrics(currrentNodeId.value, param);
  
  if (error) {
    return;
  }
  
  // First page: replace data, subsequent pages: append
  if (pagination.pageNo === 1) {
    cpuChartData = res.data?.list || [];
  } else {
    cpuChartData = cpuChartData.concat(res.data?.list || []);
  }
  
  // Recursively fetch next page if more data exists
  if (cpuChartData.length < res.data.total) {
    pagination.pageNo += 1;
    loadCpuEchartData();
  }
  
  // Remove duplicate timestamps (if last two entries have same timestamp)
  if (cpuChartData.length > 2 && 
      cpuChartData[cpuChartData.length - 1].timestamp === cpuChartData[cpuChartData.length - 2].timestamp) {
    cpuChartData.splice(cpuChartData.length - 2, 1);
  }
  
  cpuloaded.value = true;

  // Initialize slider and render chart
  setSliderConfig(true);
  setCpuChartData();
};

/**
 * Set CPU chart data
 * Filters data by slider range, builds series for each CPU metric,
 * calculates statistics, and updates both chart and table
 */
const setCpuChartData = (): void => {
  // Filter data by current slider range
  const showChartData = cpuChartData.filter((_i, idx) => {
    return idx >= sliderValueCpu.value[0] && idx <= sliderValueCpu.value[1];
  });
  
  // CPU metric types: idle, system, user, IO wait, other, total
  const dataType = [
    t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.idlePercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.systemSpacePercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.userSpacePercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.ioWaitPercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.otherPercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.cpuMetrics.totalPercentage')
  ];
  
  // Initialize series data structure
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(),
      name: type
    };
  });

  // Parse CSV data and populate series
  showChartData.forEach(item => {
    const cpusValues = item.cvsCpu.split(',');
    cpusValues.forEach((val, idx) => {
      seriesData[idx].data.push(+(+val).toFixed(2));
    });
  });

  // Calculate statistics for table display
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

  // Add legend only on initial load (notMerge = true)
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

  // Update chart with CPU data
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
    // Show placeholder data if no series data
    series: seriesData.every(serries => !serries.data.length) 
      ? [{ ...getDefaultLineConfig(), data: [60] }] 
      : seriesData
  }, false);
};

/**
 * Network Metrics State
 */
const networkNames = ref<{ value: string; label: string }[]>([]);  // Available network devices
const activeNetwork = ref<string>();                                // Currently selected network device
let networkChartData: any[] = [];                                   // Raw network metrics data
let networkTableData: any[] = [];                                   // Processed network table data
const networkloaded = ref(false);                                   // Whether network data loaded

/**
 * Load network metrics data from API
 * Fetches data for selected network device with pagination
 */
const loadNetworkEchartData = async (): Promise<void> => {
  const param = getChartParam({ 
    filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }] 
  });
  
  const [error, res] = await nodeCtrl.getNetworkMetrics(currrentNodeId.value, param);
  
  if (error) {
    return;
  }
  
  // First page: replace data, subsequent pages: append
  if (pagination.pageNo === 1) {
    networkChartData = res.data?.[0]?.values?.list || [];
  } else {
    networkChartData = networkChartData.concat(res.data?.[0]?.values?.list || []);
  }
  
  // Recursively fetch next page if more data exists
  if (res.data?.[0]?.values?.total && networkChartData.length < res.data?.[0]?.values?.total) {
    pagination.pageNo += 1;
    loadNetworkEchartData();
  }
  
  // Remove duplicate timestamps
  if (networkChartData.length > 2 && 
      networkChartData[networkChartData.length - 1].timestamp === networkChartData[networkChartData.length - 2].timestamp) {
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
      ...getDefaultLineConfig(),
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
    series: networkSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : networkSeriesData
  };

  myEcahrt.setOption(networkChartOption, notMerge.value);
};

/**
 * Memory Metrics State
 */
let memoryChartData: any[] = [];        // Raw memory metrics data
let memoryTableData: any[] = [];        // Processed memory table data
const memoryloaded = ref(false);        // Whether memory data loaded

/**
 * Load memory metrics data from API
 * Fetches data with pagination and initializes chart
 */
const loadMemoryEchartData = async (): Promise<void> => {
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
  const dataType = [t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.physicalMemoryFree'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.physicalMemoryUsed'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualFreePercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualUsedPercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualFreeMemory'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.actualUsedMemory'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.freeMemoryPercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.usedMemoryPercentage'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.swapUsed'),
    t('ftpPlugin.performanceTestDetail.nodeData.memoryMetrics.swapFree')];
  const dataTypeKey = ['free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed', 'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'];
  // loadingChartData.value = false;
  const seriesData = dataType.map(type => {
    return {
      ...getDefaultLineConfig(),
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

/**
 * Disk Metrics State
 */
const diskNames = ref<{ value: string; label: string }[]>([]);  // Available disk devices
const activeDisk = ref<string>();                                // Currently selected disk device
let diskChartData: any[] = [];                                   // Raw disk metrics data
let diskTableData: any[] = [];                                   // Processed disk table data
const diskloaded = ref(false);                                   // Whether disk data loaded

/**
 * Load disk metrics data from API
 * Fetches data for selected disk device with pagination
 */
const loadDiskEchartData = async (): Promise<void> => {
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

/**
 * Disk chart configuration
 */
let rateChartOption: any;              // IOPS chart option
let bytesRateChartOption: any;         // MB/s chart option
const diskChartKey = ref('rate');      // Current disk chart view (rate or bytesRate)

/**
 * Disk chart view options
 */
const diskDataOptions = [
  { label: t('ftpPlugin.performanceTestDetail.nodeData.diskDataOptions.iops'), value: 'rate' },
  { label: t('ftpPlugin.performanceTestDetail.nodeData.diskDataOptions.mbPerSecond'), value: 'bytesRate' }
];

/**
 * Set disk chart data
 * Builds chart series for disk metrics and updates display
 */
const setDiskEchartData = (): void => {
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
      ...getDefaultLineConfig(),
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
    series: rateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : rateSeriesData
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

/**
 * Handle disk chart view type change
 * Switches between IOPS and MB/s views
 */
const onDiskChartKeyChange = (): void => {
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

/**
 * Handle time range slider change
 * Updates chart data for selected time range
 */
const onSliderChaneg = (): void => {
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

/**
 * Handle network device selection change
 * Reloads network metrics for new device
 */
const onActiveNetwokChange = (): void => {
  pagination.pageNo = 1;
  loadNetworkEchartData();
};

/**
 * Handle disk device selection change
 * Reloads disk metrics for new device
 */
const onActiveDiskChange = (): void => {
  pagination.pageNo = 1;
  loadDiskEchartData();
};

/**
 * Auto-refresh timer for real-time data updates
 */
let intervalTimer: NodeJS.Timeout | null;

/**
 * Set up automatic data refresh interval
 * Continues refreshing until test completes (endTime is set)
 */
const intervalRefresh = (): void => {
  intervalTimer = setTimeout(async () => {
    notMerge.value = false;
    
    // Load latest data for active metric type
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
    
    // Clear current timer
    clearTimeout(intervalTimer!);
    intervalTimer = null;
    
    // Continue refresh if test still running (no end time)
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

/**
 * Build API parameters for incremental timer refresh
 * Gets data from last known timestamp to now
 * 
 * @param params - Additional parameters (e.g., device filters)
 * @returns Request parameters for fetching new data since last timestamp
 */
const getChartTimerParam = (params: any = {}) => {
  let startDate: string;
  
  // Get last timestamp from active metric's data
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
  
  const startTime = dayjs(startDate!).format(DATE_TIME_FORMAT);
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

/**
 * Load incremental CPU data for timer refresh
 * Fetches new data since last timestamp and appends to existing data
 */
const loadCputimerData = async (): Promise<void> => {
  const params = getChartTimerParam();
  const [error, res] = await nodeCtrl.getCpuMetrics(currrentNodeId.value, params);
  
  if (error) {
    return;
  }
  
  const data = res.data?.list || [];
  
  // Remove duplicate if first item matches last existing timestamp
  if (data.length && data[0].timestamp === cpuChartData[cpuChartData.length - 1].timestamp) {
    data.splice(0, 1);
  }
  
  // Append new data
  cpuChartData = cpuChartData.concat(data);
  setSliderConfig();
  setCpuChartData();
};

/**
 * Load incremental network data for timer refresh
 */
const loadNetworkTimerData = async (): Promise<void> => {
  const param = getChartTimerParam({ 
    filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }] 
  });
  
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

/**
 * Load incremental memory data for timer refresh
 */
const loadMemoryTimerData = async (): Promise<void> => {
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

/**
 * Load incremental disk data for timer refresh
 */
const loadDiskTimerData = async (): Promise<void> => {
  const param = getChartTimerParam({ 
    filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value }] 
  });
  
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

/**
 * Lifecycle Hooks
 */

/**
 * Component mount hook
 * Initializes chart and selects first execution node
 */
onMounted(() => {
  setTimeout(() => {
    if (props.execNodes.length) {
      currrentNodeId.value = props.execNodes[0].id;
    }
    initEcahrts();
  });
});

/**
 * Before unmount hook
 * Cleans up auto-refresh timer to prevent memory leaks
 */
onBeforeUnmount(() => {
  if (intervalTimer) {
    clearTimeout(intervalTimer);
    intervalTimer = null;
  }
});

/**
 * Watch for active chart tab changes
 * Stops/starts refresh timer based on whether node chart is active
 */
watch(() => props.activeChart, newValue => {
  if (newValue !== 'node') {
    // Stop refresh when switching away from node chart
    if (intervalTimer) {
      clearTimeout(intervalTimer);
      intervalTimer = null;
    }
  } else {
    // Start loading or resume refresh when switching to node chart
    if (!cpuloaded.value && !memoryloaded.value && !diskloaded.value && !networkloaded.value) {
      restart();
    } else {
      if (!props.endTime && ['PENDING', 'RUNNING'].includes(props.status)) {
        intervalRefresh();
      }
    }
  }
});

/**
 * Utility Functions
 */

/**
 * Convert bytes to specified unit
 * 
 * @param size - Size in bytes
 * @param unit - Target unit (B, KB, MB, GB, TB, PB)
 * @param decimal - Number of decimal places
 * @returns Formatted string with specified decimal places
 */
const formatBytesToUnit = (size = 0, unit = 'GB', decimal = 5): string => {
  if (size === 0) return '0';
  
  // Unit conversion map (bytes per unit)
  const unitMap: Record<string, number> = {
    B: 1,
    KB: 1024,
    MB: 1048576,
    GB: 1073741824,
    TB: 1099511627776,
    PB: 1125899906842624
  };
  
  const divisor = unitMap[unit] || 1073741824;
  return (size / divisor).toFixed(decimal);
};

/**
 * Parse duration string into value and unit
 * Extracts numeric value and time unit from duration string
 * 
 * @param duration - Duration string (e.g., "30s", "5min", "2h")
 * @returns Array of [value, unit]
 */
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

/**
 * Restart data loading for current node and metric type
 * Clears all cached data, reloads device lists if needed,
 * fetches fresh metrics, and starts auto-refresh if test is running
 * 
 * @param nodeChange - Whether this is triggered by node selection change
 */
const restart = async (nodeChange = false): Promise<void> => {
  // Reset pagination and loading states
  pagination.pageNo = 1;
  cpuloaded.value = false;
  networkloaded.value = false;
  diskloaded.value = false;
  memoryloaded.value = false;
  
  // Clear all cached data
  cpuChartData = [];
  networkChartData = [];
  memoryChartData = [];
  diskChartData = [];
  
  // Clear refresh timer
  if (intervalTimer) {
    clearTimeout(intervalTimer);
    intervalTimer = null;
  }
  
  // Only proceed if node chart is active
  if (props.activeChart !== 'node') {
    return;
  }
  
  if (currrentNodeId.value) {
    notMerge.value = true;
    
    // Load data for active metric type
    if (activeTab.value === 'cpu') {
      await loadCpuEchartData();
    }
    
    if (activeTab.value === 'network') {
      // Load network device list if not loaded or node changed
      if (!networkNames.value.length || nodeChange) {
        const [error, { data }] = await nodeCtrl.getNetworkInfoMetrics(currrentNodeId.value);
        if (!error) {
          const _networkNames: { label: string; value: string }[] = [];
          const networkNamesMp: Record<string, boolean> = {};
          
          // Deduplicate device names
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
      // Load disk device list if not loaded or node changed
      if (!diskNames.value.length || nodeChange) {
        const [error, res] = await nodeCtrl.getDiskInfoMetrics(currrentNodeId.value);
        if (error) {
          return;
        }
        
        const _diskNames: { label: string; value: string }[] = [];
        const deviceNameMp: Record<string, boolean> = {};
        
        // Deduplicate device names
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
  
  // Start auto-refresh if test is still running
  if (!props.endTime && ['PENDING', 'RUNNING'].includes(props.status)) {
    intervalRefresh();
  }
};

/**
 * Watch for node selection changes
 * Triggers restart to load data for newly selected node
 */
watch(() => currrentNodeId.value, () => {
  restart(true);
}, {
  immediate: true
});

/**
 * Expose restart method for parent component
 */
defineExpose({
  restart
});
</script>

<template>
  <!-- No data placeholder (shown when test not started or no nodes) -->
  <NoData v-if="!props.startTime || !props.execNodes.length || props.status === 'CREATED'" />
  
  <!-- Main content (when data is available) -->
  <div v-else class="flex flex-col justify-between">
    <!-- Control panel: metric tabs, device selectors, and node selector -->
    <div class="flex pl-10 items-center pr-15">
      <!-- Metric type selector (CPU, Memory, Disk, Network) -->
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
      
      <!-- Network device selector (shown only on network tab) -->
      <Select
        v-if="activeTab === 'network'"
        v-model:value="activeNetwork"
        class="min-w-25 ml-2"
        size="small"
        :options="networkNames"
        @change="onActiveNetwokChange" />
      
      <!-- Disk device selector (shown only on disk tab) -->
      <Select
        v-show="activeTab === 'disk'"
        v-model:value="activeDisk"
        class="ml-2 min-w-25"
        size="small"
        :options="diskNames"
        @change="onActiveDiskChange" />
      
      <!-- Disk chart type selector: IOPS vs MB/s (shown only on disk tab) -->
      <RadioGroup
        v-show="activeTab === 'disk'"
        v-model:value="diskChartKey"
        class="block text-center ml-2"
        size="small"
        :options="diskDataOptions"
        @change="onDiskChartKeyChange" />
      
      <!-- Right side controls: node type and node selector -->
      <div class="flex items-center flex-1 justify-end">
        <!-- Node type selector (Execution Node vs Application Node) -->
        <RadioGroup
          v-model:value="nodeType"
          class="mr-5"
          :disabled="!props.appNodes?.length"
          :options="[
            { label: t('ftpPlugin.performanceTestDetail.nodeData.nodeTypes.execNode'), value: 'exec' },
            { label: t('ftpPlugin.performanceTestDetail.nodeData.nodeTypes.appNode'), value: 'apply' }
          ]" />
        
        <!-- Node selector dropdown -->
        <Select
          v-model:value="currrentNodeId"
          class="min-w-25"
          size="small"
          :options="nodeList"
          :fieldNames="{ value: 'id', label: 'name' }" />
      </div>
    </div>
    
    <!-- Chart and slider section -->
    <div class="mt-3.5">
      <!-- No data placeholder (shown when data loaded but empty) -->
      <template v-if="!times.length && ((activeTab === 'cpu' && cpuloaded) || (activeTab === 'memory' && memoryloaded) || (activeTab === 'disk' && diskloaded) || (activeTab === 'network' && networkloaded))">
        <NoData class="my-20" />
      </template>
      
      <!-- Chart container (shown when loading or has data) -->
      <div v-show="times.length || (activeTab === 'cpu' && !cpuloaded) || (activeTab === 'memory' && !memoryloaded) || (activeTab === 'disk' && !diskloaded) || (activeTab === 'network' && !networkloaded)" class="w-full">
        <!-- ECharts container -->
        <div ref="echartRef" class="w-full h-70"></div>
        
        <!-- Time range sliders (one for each metric type) -->
        <div class="-mt-5 pl-10 pr-12">
          <!-- CPU time range slider -->
          <template v-if="activeTab === 'cpu'">
            <Slider
              v-model:value="sliderValueCpu"
              range
              :min="0"
              :max="timesCpu.length - 1 > 0 ? timesCpu.length - 1 : sliderValueCpu[1]"
              :tipFormatter="(value) => timesCpu.length ? cpuChartData[value]?.timestamp : ''"
              @change="onSliderChaneg" />
          </template>
          
          <!-- Memory time range slider -->
          <template v-if="activeTab === 'memory'">
            <Slider
              v-model:value="sliderValueMemory"
              range
              :min="0"
              :max="timesMemory.length - 1 > 0 ? timesMemory.length - 1 : sliderValueMemory[1]"
              :tipFormatter="(value) => timesMemory.length ? memoryChartData[value]?.timestamp : ''"
              @change="onSliderChaneg" />
          </template>
          
          <!-- Disk time range slider -->
          <template v-if="activeTab === 'disk'">
            <Slider
              v-model:value="sliderValueDisk"
              range
              :min="0"
              :max="timesDisk.length - 1 > 0 ? timesDisk.length - 1 : sliderValueDisk[1]"
              :tipFormatter="(value) => timesDisk.length ? diskChartData[value]?.timestamp : ''"
              @change="onSliderChaneg" />
          </template>
          
          <!-- Network time range slider -->
          <template v-if="activeTab === 'network'">
            <Slider
              v-model:value="sliderValueNetwork"
              range
              :min="0"
              :max="timesNetwork.length - 1 > 0 ? timesNetwork.length - 1 : sliderValueNetwork[1]"
              :tipFormatter="(value) => timesNetwork.length ? networkChartData[value]?.timestamp : ''"
              @change="onSliderChaneg" />
          </template>
        </div>
      </div>
    </div>
    
    <!-- Statistics table (shown when data is available) -->
    <Table
      v-if="!!tableData.length"
      :columns="columns"
      :pagination="false"
      :dataSource="tableData"
      size="small"
      class="mb-7.5 mt-10 pl-10 pr-15" />
  </div>
</template>

<style scoped>
/**
 * Custom slider styles for time range selection
 */

/* Slider rail (base track) */
:deep(.ant-slider-rail) {
  height: 10px;
  border: 1px solid var(--border-divider);
  border-radius: 1px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider:hover .ant-slider-rail) {
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

/* Slider track (selected range) */
:deep(.ant-slider-track) {
  height: 10px;
  border-color: rgba(145, 213, 255, 30%);
  background-color: rgba(245, 245, 245, 100%);

  @apply border;
}

:deep(.ant-slider:hover .ant-slider-track) {
  height: 10px;
  background-color: rgba(245, 245, 245, 100%);
}

/* Slider step marks */
:deep(.ant-slider-step) {
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

/* Slider handle (drag thumb) */
:deep(.ant-slider-handle) {
  width: 10px;
  height: 16px;
  margin-top: -3px;
  border-radius: 3px;
  border-color: rgba(145, 213, 255, 100%);

  @apply border-2;
}

:deep(.ant-slider-handle:focus) {
  border-color: rgba(145, 213, 255, 100%);
  box-shadow: none;
}

:deep(.ant-slider:hover .ant-slider-handle) {
  border-color: rgba(145, 213, 255, 100%);
}
</style>
