<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { Card, Table, Tag, Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import * as echarts from 'echarts/core';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  RadarComponent
} from 'echarts/components';
import { LineChart, RadarChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { UniversalTransition } from 'echarts/features';
import elementResizeDetector from 'element-resize-detector';
import TestBasicInfo from '@/components/test/TestBasicInfo.vue';

/**
 * Props for HTTP performance detail wrapper.
 */
interface Props {
  execInfo: Record<string, any>;
  exception:{
    codeName:string;
    messageName:string;
    code:string;
    message:string;
  };
  delayInSeconds:number;
  apiDimensionObj: {[key:string]:{ [key:string]: number[]}};
  indexDimensionObj: {[key:string]:{ [key:string]: number[]}};
  apiNames:string[];
  timestampData:string[];
  isLoaded:boolean;
  brpsUnit:'KB' | 'MB';
  bwpsUnit: 'KB' | 'MB';
  errCountList: Record<string, any>[];
  sampleList: Record<string, any>[];
  statusCodeData: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  exception: undefined,
  delayInSeconds: 3000
});

// Register ECharts components
echarts.use([
  GridComponent,
  TooltipComponent,
  LegendComponent,
  RadarComponent,
  LineChart,
  RadarChart,
  CanvasRenderer,
  UniversalTransition
]);

// Props can be added later when needed for real data integration
// interface Props {
//   execId?: string;
// }

// const props = withDefaults(defineProps<Props>(), {
//   execId: undefined
// });

// Performance metrics data
const cpuUsage = ref({
  current: 25.5,
  peak: 68.2
});

const memoryUsage = ref({
  current: 152,
  growth: 7.5
});

const networkTraffic = ref({
  current: 15.2,
  requests: 156
});

const batteryConsumption = ref({
  current: 8,
  temperature: 5
});

// CPU Performance Data
const cpuPerformanceData = ref([
  {
    key: '1',
    indicator: 'CPU使用率',
    average: 25.5,
    peak: 68.2,
    sampleCount: 1000,
    status: '正常',
    statusType: 'success'
  },
  {
    key: '2',
    indicator: '用户进程CPU',
    average: 18.3,
    peak: 52.1,
    sampleCount: 1000,
    status: '正常',
    statusType: 'success'
  },
  {
    key: '3',
    indicator: '系统进程CPU',
    average: 7.2,
    peak: 16.1,
    sampleCount: 1000,
    status: '正常',
    statusType: 'success'
  }
]);

// Memory Performance Data
const memoryPerformanceData = ref([
  {
    key: '1',
    indicator: 'Java堆内存',
    startup: 65,
    average: 98,
    peak: 185,
    growthRate: 3.2,
    status: '需关注',
    statusType: 'warning'
  },
  {
    key: '2',
    indicator: '原生内存',
    startup: 20,
    average: 54,
    peak: 95,
    growthRate: 4.3,
    status: '需关注',
    statusType: 'warning'
  },
  {
    key: '3',
    indicator: '总内存',
    startup: 85,
    average: 152,
    peak: 280,
    growthRate: 7.5,
    status: '异常',
    statusType: 'error'
  }
]);

// Memory Leak Detection Data
const memoryLeakData = ref({
  detectionMethod: 'LeakCanary + MAT 分析',
  leakedObject: 'com.example.ImageCache',
  leakSize: 15,
  referenceChain: 'Activity → ImageCache → Bitmap[]',
  severity: '中等',
  severityDesc: '可能导致长时间使用后OOM'
});

// CPU Performance Table Columns
const cpuColumns = [
  {
    title: '指标',
    dataIndex: 'indicator',
    key: 'indicator',
    width: 150
  },
  {
    title: '平均值',
    dataIndex: 'average',
    key: 'average',
    width: 120
  },
  {
    title: '峰值',
    dataIndex: 'peak',
    key: 'peak',
    width: 120
  },
  {
    title: '采样次数',
    dataIndex: 'sampleCount',
    key: 'sampleCount'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  }
];

// Memory Performance Table Columns
const memoryColumns = [
  {
    title: '指标',
    dataIndex: 'indicator',
    key: 'indicator',
    width: 120
  },
  {
    title: '启动内存',
    dataIndex: 'startup',
    key: 'startup',
    width: 100
  },
  {
    title: '平均内存',
    dataIndex: 'average',
    key: 'average',
    width: 100
  },
  {
    title: '峰值内存',
    dataIndex: 'peak',
    key: 'peak',
    width: 100
  },
  {
    title: '内存增长率',
    dataIndex: 'growthRate',
    key: 'growthRate',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  }
];

// Chart data for 24-hour trend
const chartRef = ref<HTMLElement>();
let myChart: echarts.ECharts | null = null;
const erd = elementResizeDetector({ strategy: 'scroll' });

// Startup Performance Data
const startupPerformance = ref({
  coldStart: {
    current: 2.8,
    target: 3,
    unit: 's'
  },
  warmStart: {
    current: 0.9,
    target: 1.5,
    unit: 's'
  },
  firstScreenRender: {
    current: 1.5,
    target: 2,
    unit: 's'
  },
  fullyInteractive: {
    current: 3.2,
    target: 5,
    unit: 's'
  }
});

// Radar chart data for startup performance evaluation
const radarChartRef = ref<HTMLElement>();
let radarChart: echarts.ECharts | null = null;

// Radar chart configuration
const radarChartOption = computed(() => ({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    data: ['当前版本', '目标值'],
    itemGap: 20,
    left: '30%',
    top: '40%',
    orient: 'vertical'
  },
  radar: {
    indicator: [
      { name: '冷启动', max: 6 },
      { name: '热启动', max: 6 },
      { name: '首屏渲染', max: 6 },
      { name: '完全交互', max: 6 },
      { name: '响应时间', max: 6 },
      { name: '流畅度', max: 6 }
    ],
    center: ['50%', '50%'],
    radius: '65%',
    axisName: {
      fontSize: 12,
      color: '#666'
    },
    splitArea: {
      areaStyle: {
        color: ['rgba(250, 250, 250, 0.3)', 'rgba(200, 200, 200, 0.1)']
      }
    },
    axisLine: {
      lineStyle: {
        color: '#ddd'
      }
    },
    splitLine: {
      lineStyle: {
        color: '#ddd'
      }
    }
  },
  series: [
    {
      name: '启动性能评估',
      type: 'radar',
      data: [
        {
          value: [2.8, 0.9, 1.5, 3.2, 3.5, 3.8],
          name: '当前版本',
          areaStyle: {
            color: 'rgba(24, 144, 255, 0.2)'
          },
          lineStyle: {
            color: '#1890ff',
            width: 2
          },
          itemStyle: {
            color: '#1890ff'
          },
          label: {
            show: true,
            formatter: '{c}',
            fontSize: 11,
            color: '#333'
          }
        },
        {
          value: [3, 1.5, 2, 5, 4, 4.5],
          name: '目标值',
          areaStyle: {
            color: 'rgba(82, 196, 26, 0.2)'
          },
          lineStyle: {
            color: '#52c41a',
            width: 2
          },
          itemStyle: {
            color: '#52c41a'
          },
          label: {
            show: true,
            formatter: '{c}',
            fontSize: 11,
            color: '#333'
          }
        }
      ]
    }
  ]
}));

// Generate 24-hour data (0h to 24h, 2-hour intervals)
const timeLabels = Array.from({ length: 13 }, (_, i) => `${i * 2}h`);

// CPU usage data (starts at 20%, peaks at 6-8h around 40-45%, then stabilizes at 20-25%)
const cpuData = computed(() => {
  const data: number[] = [];
  for (let i = 0; i < 13; i++) {
    const hour = i * 2;
    let value: number;
    if (hour <= 6) {
      // Gradual increase to peak
      value = 20 + (hour / 6) * 25;
    } else if (hour <= 8) {
      // Peak period
      value = 45 - ((hour - 6) / 2) * 5;
    } else {
      // Decline and stabilize
      value = 20 + (hour - 8) / 16 * 5;
    }
    data.push(Math.round(value * 10) / 10);
  }
  return data;
});

// Memory usage data (starts at 80MB, linear increase to 260MB)
const memoryData = computed(() => {
  const data: number[] = [];
  for (let i = 0; i < 13; i++) {
    const value = 80 + (i * 2 / 24) * 180;
    data.push(Math.round(value * 10) / 10);
  }
  return data;
});

// Chart options
const chartOption = computed(() => ({
  grid: {
    top: 20,
    right: 30,
    bottom: 40,
    left: 50
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['CPU使用率(%)', '内存占用(MB)'],
    top: 0,
    right: 20
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: timeLabels,
    axisLabel: {
      fontSize: 12
    }
  },
  yAxis: {
    type: 'value',
    name: '资源使用量',
    nameLocation: 'middle',
    nameGap: 35,
    min: 0,
    max: 300,
    interval: 50,
    axisLabel: {
      fontSize: 12
    },
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed'
      }
    }
  },
  series: [
    {
      name: 'CPU使用率(%)',
      type: 'line',
      data: cpuData.value,
      smooth: true,
      lineStyle: {
        color: '#ff4d4f',
        width: 2
      },
      itemStyle: {
        color: '#ff4d4f'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(255, 77, 79, 0.3)' },
            { offset: 1, color: 'rgba(255, 77, 79, 0.05)' }
          ]
        }
      }
    },
    {
      name: '内存占用(MB)',
      type: 'line',
      data: memoryData.value,
      smooth: true,
      lineStyle: {
        color: '#1890ff',
        width: 2
      },
      itemStyle: {
        color: '#1890ff'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
          ]
        }
      }
    }
  ]
}));

// Initialize chart
const initChart = () => {
  if (!chartRef.value) {
    return;
  }
  
  if (myChart) {
    myChart.dispose();
  }
  
  myChart = echarts.init(chartRef.value);
  myChart.setOption(chartOption.value);
  
  // Handle resize
  erd.listenTo(chartRef.value, () => {
    if (myChart) {
      myChart.resize();
    }
  });
};

// Initialize radar chart
const initRadarChart = () => {
  if (!radarChartRef.value) {
    return;
  }
  
  if (radarChart) {
    radarChart.dispose();
  }
  
  radarChart = echarts.init(radarChartRef.value);
  radarChart.setOption(radarChartOption.value);
  
  // Handle resize
  erd.listenTo(radarChartRef.value, () => {
    if (radarChart) {
      radarChart.resize();
    }
  });
};

// Cleanup
onBeforeUnmount(() => {
  // if (myChart) {
  //   myChart.dispose();
  //   myChart = null;
  // }
  // if (chartRef.value) {
  //   erd.uninstall(chartRef.value);
  // }
  // if (radarChart) {
  //   radarChart.dispose();
  //   radarChart = null;
  // }
  // if (radarChartRef.value) {
  //   erd.uninstall(radarChartRef.value);
  // }
});

onMounted(() => {
  initChart();
  initRadarChart();
});

// Check if metric meets target
const meetsTarget = (current: number, target: number) => {
  return current <= target;
};
</script>

<template>
  <TestBasicInfo :value="props.execInfo" :exception="props.exception" class="mb-4" />
  <div class="mobile-performance-overview">
    <!-- Performance Indicator Overview -->
    <div class="performance-indicators">
      <Card class="indicator-card cpu-card">
        <div class="indicator-content">
          <div class="indicator-header">
            <div class="icon-wrapper cpu-icon-wrapper">
                CPU
              <!-- <Icon icon="icon-cpu" class="indicator-icon cpu" /> -->
            </div>
            <span class="indicator-title">平均CPU使用率</span>
          </div>
          <div class="indicator-value">{{ cpuUsage.current }}%</div>
          <div class="indicator-detail">
            <span class="detail-label">峰值：</span>
            <span class="detail-value">{{ cpuUsage.peak }}%</span>
          </div>
        </div>
      </Card>

      <Card class="indicator-card memory-card">
        <div class="indicator-content">
          <div class="indicator-header">
            <div class="icon-wrapper memory-icon-wrapper">
                Memory
                
              <!-- <Icon icon="icon-neicun" class="indicator-icon memory" /> -->
            </div>
            <span class="indicator-title">平均内存占用</span>
          </div>
          <div class="indicator-value">{{ memoryUsage.current }}MB</div>
          <div class="indicator-detail">
            <span class="detail-label">增长：</span>
            <span class="detail-value">{{ memoryUsage.growth }}MB/h</span>
          </div>
        </div>
      </Card>

      <Card class="indicator-card network-card">
        <div class="indicator-content">
          <div class="indicator-header">
            <div class="icon-wrapper network-icon-wrapper">
              <!-- <Icon icon="icon-wangluo" class="indicator-icon network" /> -->
              Network
            </div>
            <span class="indicator-title">网络总流量</span>
          </div>
          <div class="indicator-value">{{ networkTraffic.current }}MB</div>
          <div class="indicator-detail">
            <span class="detail-label">请求：</span>
            <span class="detail-value">{{ networkTraffic.requests }}次</span>
          </div>
        </div>
      </Card>

      <Card class="indicator-card battery-card">
        <div class="indicator-content">
          <div class="indicator-header">
            <div class="icon-wrapper battery-icon-wrapper">
              <!-- <Icon icon="icon-dianchi" class="indicator-icon battery" /> -->
              Battery
            </div>
            <span class="indicator-title">电池消耗</span>
          </div>
          <div class="indicator-value">{{ batteryConsumption.current }}%/h</div>
          <div class="indicator-detail">
            <span class="detail-label">温度：</span>
            <span class="detail-value">+{{ batteryConsumption.temperature }}℃</span>
          </div>
        </div>
      </Card>
    </div>

    <!-- 24-hour Performance Trend -->
    <Card class="trend-chart-card">
      <template #title>
        <div class="chart-title">24小时性能趋势</div>
      </template>
      <div ref="chartRef" class="trend-chart"></div>
    </Card>

    <!-- CPU Performance Table -->
    <Card class="performance-table-card">
      <template #title>
        <div class="section-title">CPU性能</div>
      </template>
      <Table
        :columns="cpuColumns"
        :dataSource="cpuPerformanceData"
        :pagination="false"
        size="small"
        class="performance-table">
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'status'">
            <Tag :color="record.statusType === 'success' ? 'success' : record.statusType === 'warning' ? 'warning' : 'error'">
              {{ record.status }}
            </Tag>
          </template>
          <template v-else-if="column.dataIndex === 'average' || column.dataIndex === 'peak'">
            {{ text }}%
          </template>
          <template v-else>
            {{ text }}
          </template>
        </template>
      </Table>
    </Card>

    <!-- Memory Performance Table -->
    <Card class="performance-table-card">
      <template #title>
        <div class="section-title">内存性能</div>
      </template>
      <Table
        :columns="memoryColumns"
        :dataSource="memoryPerformanceData"
        :pagination="false"
        size="small"
        class="performance-table">
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'status'">
            <Tag :color="record.statusType === 'success' ? 'success' : record.statusType === 'warning' ? 'warning' : 'error'">
              {{ record.status }}
            </Tag>
          </template>
          <template v-else-if="column.dataIndex === 'startup' || column.dataIndex === 'average' || column.dataIndex === 'peak'">
            {{ text }}MB
          </template>
          <template v-else-if="column.dataIndex === 'growthRate'">
            {{ text }}MB/h
          </template>
          <template v-else>
            {{ text }}
          </template>
        </template>
      </Table>
    </Card>

    <!-- Memory Leak Detection -->
    <Card class="memory-leak-card">
      <template #title>
        <div class="section-title">内存泄漏检测</div>
      </template>
      <div class="memory-leak-content">
        <div class="leak-info-grid">
          <div class="leak-info-item">
            <span class="leak-label">检测方法：</span>
            <span class="leak-value">{{ memoryLeakData.detectionMethod }}</span>
          </div>
          <div class="leak-info-item">
            <span class="leak-label">泄漏对象：</span>
            <span class="leak-value leak-object">{{ memoryLeakData.leakedObject }}</span>
          </div>
          <div class="leak-info-item">
            <span class="leak-label">泄漏大小：</span>
            <span class="leak-value leak-size">{{ memoryLeakData.leakSize }}MB</span>
          </div>
          <div class="leak-info-item">
            <span class="leak-label">引用链：</span>
            <span class="leak-value leak-chain">{{ memoryLeakData.referenceChain }}</span>
          </div>
          <div class="leak-info-item">
            <span class="leak-label">严重程度：</span>
            <span class="leak-value leak-severity">{{ memoryLeakData.severity }}</span>
            <span class="leak-desc">({{ memoryLeakData.severityDesc }})</span>
          </div>
        </div>
        <div class="leak-action">
          <Button type="primary" danger>
            <Icon icon="icon-jinggao" class="mr-1" />
            发现泄漏
          </Button>
        </div>
      </div>
    </Card>

    <!-- Startup Performance Analysis -->
    <div class="startup-performance-section">
      <div class="text-4 font-semibold mb-4 text-title">启动性能分析</div>
      
      <!-- Startup Performance Metric Cards -->
      <div class="startup-metrics-grid">
        <Card class="startup-metric-card">
          <div class="startup-metric-content">
            <div class="startup-metric-label">冷启动时间</div>
            <div class="startup-metric-value">
              {{ startupPerformance.coldStart.current }}{{ startupPerformance.coldStart.unit }}
            </div>
            <div class="startup-metric-target">
              目标: &lt; {{ startupPerformance.coldStart.target }}{{ startupPerformance.coldStart.unit }}
            </div>
            <div 
              v-if="meetsTarget(startupPerformance.coldStart.current, startupPerformance.coldStart.target)"
              class="startup-metric-status success">
              <Icon icon="icon-duihao-copy" class="status-icon" />
              达标
            </div>
            <div 
              v-else
              class="startup-metric-status warning">
              <Icon icon="icon-jinggao" class="status-icon" />
              未达标
            </div>
          </div>
        </Card>

        <Card class="startup-metric-card">
          <div class="startup-metric-content">
            <div class="startup-metric-label">热启动时间</div>
            <div class="startup-metric-value">
              {{ startupPerformance.warmStart.current }}{{ startupPerformance.warmStart.unit }}
            </div>
            <div class="startup-metric-target">
              目标: &lt; {{ startupPerformance.warmStart.target }}{{ startupPerformance.warmStart.unit }}
            </div>
            <div 
              v-if="meetsTarget(startupPerformance.warmStart.current, startupPerformance.warmStart.target)"
              class="startup-metric-status success">
              <Icon icon="icon-duihao-copy" class="status-icon" />
              达标
            </div>
            <div 
              v-else
              class="startup-metric-status warning">
              <Icon icon="icon-jinggao" class="status-icon" />
              未达标
            </div>
          </div>
        </Card>

        <Card class="startup-metric-card">
          <div class="startup-metric-content">
            <div class="startup-metric-label">首屏渲染时间</div>
            <div class="startup-metric-value">
              {{ startupPerformance.firstScreenRender.current }}{{ startupPerformance.firstScreenRender.unit }}
            </div>
            <div class="startup-metric-target">
              目标: &lt; {{ startupPerformance.firstScreenRender.target }}{{ startupPerformance.firstScreenRender.unit }}
            </div>
            <div 
              v-if="meetsTarget(startupPerformance.firstScreenRender.current, startupPerformance.firstScreenRender.target)"
              class="startup-metric-status success">
              <Icon icon="icon-duihao-copy" class="status-icon" />
              达标
            </div>
            <div 
              v-else
              class="startup-metric-status warning">
              <Icon icon="icon-jinggao" class="status-icon" />
              未达标
            </div>
          </div>
        </Card>

        <Card class="startup-metric-card">
          <div class="startup-metric-content">
            <div class="startup-metric-label">完全交互时间</div>
            <div class="startup-metric-value">
              {{ startupPerformance.fullyInteractive.current }}{{ startupPerformance.fullyInteractive.unit }}
            </div>
            <div class="startup-metric-target">
              目标: &lt; {{ startupPerformance.fullyInteractive.target }}{{ startupPerformance.fullyInteractive.unit }}
            </div>
            <div 
              v-if="meetsTarget(startupPerformance.fullyInteractive.current, startupPerformance.fullyInteractive.target)"
              class="startup-metric-status success">
              <Icon icon="icon-duihao-copy" class="status-icon" />
              达标
            </div>
            <div 
              v-else
              class="startup-metric-status warning">
              <Icon icon="icon-jinggao" class="status-icon" />
              未达标
            </div>
          </div>
        </Card>
      </div>

      <!-- Startup Performance Radar Chart -->
      <Card class="startup-radar-card">
        <template #title>
          <div class="section-title">启动性能评估</div>
        </template>
        <div ref="radarChartRef" class="startup-radar-chart"></div>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.mobile-performance-overview {
  /* padding: 20px; */
}

/* Performance Indicators Grid */
.performance-indicators {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.indicator-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  overflow: hidden;
  position: relative;
  border: 2px solid transparent;
}

.indicator-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, transparent, currentColor, transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.indicator-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.indicator-card:hover::before {
  opacity: 1;
}

/* CPU Card */
.cpu-card {
  border-color: #ff4d4f;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
}

.cpu-card:hover {
  border-color: #ff7875;
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.2);
}

.cpu-card::before {
  color: #ff4d4f;
}

/* Memory Card */
.memory-card {
  border-color: #1890ff;
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
}

.memory-card:hover {
  border-color: #40a9ff;
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.2);
}

.memory-card::before {
  color: #1890ff;
}

/* Network Card */
.network-card {
  border-color: #52c41a;
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
}

.network-card:hover {
  border-color: #73d13d;
  box-shadow: 0 6px 16px rgba(82, 196, 26, 0.2);
}

.network-card::before {
  color: #52c41a;
}

/* Battery Card */
.battery-card {
  border-color: #fa8c16;
  background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
}

.battery-card:hover {
  border-color: #ffa940;
  box-shadow: 0 6px 16px rgba(250, 140, 22, 0.2);
}

.battery-card::before {
  color: #fa8c16;
}

.indicator-content {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.indicator-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.icon-wrapper {
  min-width: 48px;
  padding: 0 12px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s;
}

.icon-wrapper::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 14px;
  padding: 2px;
  background: linear-gradient(135deg, currentColor, transparent);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s;
}

.indicator-card:hover .icon-wrapper::after {
  opacity: 0.3;
}

.cpu-icon-wrapper {
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.1) 0%, rgba(255, 77, 79, 0.05) 100%);
  border: 1px solid rgba(255, 77, 79, 0.2);
}

.cpu-icon-wrapper::after {
  background: linear-gradient(135deg, #ff4d4f, rgba(255, 77, 79, 0.3));
}

.memory-icon-wrapper {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.1) 0%, rgba(24, 144, 255, 0.05) 100%);
  border: 1px solid rgba(24, 144, 255, 0.2);
}

.memory-icon-wrapper::after {
  background: linear-gradient(135deg, #1890ff, rgba(24, 144, 255, 0.3));
}

.network-icon-wrapper {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.1) 0%, rgba(82, 196, 26, 0.05) 100%);
  border: 1px solid rgba(82, 196, 26, 0.2);
}

.network-icon-wrapper::after {
  background: linear-gradient(135deg, #52c41a, rgba(82, 196, 26, 0.3));
}

.battery-icon-wrapper {
  background: linear-gradient(135deg, rgba(250, 140, 22, 0.1) 0%, rgba(250, 140, 22, 0.05) 100%);
  border: 1px solid rgba(250, 140, 22, 0.2);
}

.battery-icon-wrapper::after {
  background: linear-gradient(135deg, #fa8c16, rgba(250, 140, 22, 0.3));
}

.indicator-icon {
  font-size: 24px;
  position: relative;
  z-index: 1;
}

.indicator-icon.cpu {
  color: #ff4d4f;
}

.indicator-icon.memory {
  color: #1890ff;
}

.indicator-icon.network {
  color: #52c41a;
}

.indicator-icon.battery {
  color: #fa8c16;
}

.indicator-title {
  font-size: 13px;
  color: #8c8c8c;
  font-weight: 500;
}

.indicator-value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.indicator-detail {
  font-size: 12px;
  color: #8c8c8c;
}

.detail-label {
  margin-right: 4px;
}

.detail-value {
  color: #595959;
  font-weight: 500;
}

/* Trend Chart Card */
.trend-chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.trend-chart {
  width: 100%;
  height: 300px;
  margin-top: 16px;
}

/* Card Styles */
:deep(.ant-card) {
  background: #fff;
}

:deep(.ant-card-body) {
  padding: 0;
}

:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
  min-height: 56px;
}

:deep(.ant-card-head-title) {
  padding: 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .performance-indicators {
    grid-template-columns: 1fr;
  }
  
  .trend-chart {
    height: 300px;
  }
}

@media (min-width: 1200px) {
  .performance-indicators {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* Performance Table Cards */
.performance-table-card {
  margin-top: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.performance-table {
  margin-top: 16px;
}

.performance-table :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  border-bottom: 2px solid #f0f0f0;
}

.performance-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #f0f0f0;
}

.performance-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #fafafa;
}

/* Memory Leak Detection Card */
.memory-leak-card {
  margin-top: 24px;
  border-radius: 8px;
  border: 2px solid #ff4d4f;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.15);
  background: linear-gradient(135deg, #fff1f0 0%, #ffffff 100%);
}

.memory-leak-content {
  padding: 20px;
}

.leak-info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.leak-info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.leak-label {
  color: #8c8c8c;
  font-weight: 500;
  min-width: 80px;
  flex-shrink: 0;
}

.leak-value {
  color: #262626;
  font-weight: 500;
  flex: 1;
}

.leak-object {
  color: #1890ff;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #f0f9ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.leak-size {
  color: #ff4d4f;
  font-weight: 600;
}

.leak-chain {
  color: #595959;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #fafafa;
  padding: 2px 6px;
  border-radius: 4px;
}

.leak-severity {
  color: #fa8c16;
  font-weight: 600;
}

.leak-desc {
  color: #8c8c8c;
  font-size: 12px;
  margin-left: 4px;
}

.leak-action {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #ffe7e7;
}

/* Table Card Body Padding */
.performance-table-card :deep(.ant-card-body) {
  padding: 20px;
}

.memory-leak-card :deep(.ant-card-body) {
  padding: 0;
}

.memory-leak-card :deep(.ant-card-head) {
  border-bottom: 2px solid #ffe7e7;
  background: linear-gradient(135deg, #fff1f0 0%, #ffffff 100%);
}

/* Startup Performance Analysis Section */
.startup-performance-section {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.startup-metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.startup-metric-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.startup-metric-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.startup-metric-content {
  padding: 20px;
  position: relative;
}

.startup-metric-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 12px;
  font-weight: 500;
}

.startup-metric-value {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.startup-metric-target {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 12px;
}

.startup-metric-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 4px;
  width: fit-content;
}

.startup-metric-status.success {
  color: #52c41a;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.startup-metric-status.warning {
  color: #fa8c16;
  background: #fff7e6;
  border: 1px solid #ffd591;
}

.status-icon {
  font-size: 14px;
}

.startup-radar-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-top: 24px;
}

.startup-radar-chart {
  width: 100%;
  height: 300px;
  margin-top: 16px;
}

/* Responsive Design for Startup Performance */
@media (max-width: 768px) {
  .startup-metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .startup-radar-chart {
    height: 350px;
  }
}

@media (min-width: 1200px) {
  .startup-metrics-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>