<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { Card } from '@xcan-angus/vue-ui';
import * as echarts from 'echarts/core';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components';
import { LineChart, BarChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { UniversalTransition } from 'echarts/features';
import elementResizeDetector from 'element-resize-detector';
import { TitleComponent } from 'echarts/components';

// Register ECharts components
echarts.use([
  GridComponent,
  TooltipComponent,
  LegendComponent,
  LineChart,
  BarChart,
  CanvasRenderer,
  UniversalTransition,
  TitleComponent
]);

// Chart references
const cpuChartRef = ref<HTMLElement>();
const memoryChartRef = ref<HTMLElement>();
const batteryChartRef = ref<HTMLElement>();
const networkChartRef = ref<HTMLElement>();

let cpuChart: echarts.ECharts | null = null;
let memoryChart: echarts.ECharts | null = null;
let batteryChart: echarts.ECharts | null = null;
let networkChart: echarts.ECharts | null = null;

const erd = elementResizeDetector({ strategy: 'scroll' });

// Time labels (0:00 to 22:00, 2-hour intervals)
const timeLabels = ['0:00', '2:00', '4:00', '6:00', '8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'];

// CPU Usage Data
const cpuData = [15, 18, 20, 25, 30, 35, 38, 40, 35, 28, 22, 20];

// Memory Usage Data (in GB)
const memoryData = [2.2, 2.3, 2.4, 2.6, 2.8, 3.0, 3.2, 3.4, 3.5, 3.3, 3.1, 3.0];

// Battery and Temperature Data
const batteryData = [100, 95, 90, 85, 75, 65, 55, 45, 40, 38, 36, 35];
const temperatureData = [25, 26, 27, 28, 30, 32, 34, 37, 35, 32, 30, 29];

// Network Traffic Data
const downloadData = [15, 28, 10, 40, 55, 70, 45, 60, 75, 35, 25, 15];
const uploadData = [5, 8, 5, 12, 18, 20, 15, 20, 25, 10, 8, 5];

// CPU Chart Option
const cpuChartOption = computed(() => ({
  title: {
    text: 'CPU使用率',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 600,
      color: '#262626'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '15%',
    top: '20%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: timeLabels,
    axisLabel: {
      fontSize: 12,
      color: '#666'
    }
  },
  yAxis: {
    type: 'value',
    name: '使用率 (%)',
    nameLocation: 'middle',
    nameGap: 40,
    min: 0,
    max: 100,
    interval: 10,
    axisLabel: {
      fontSize: 12,
      color: '#666',
      formatter: '{value}%'
    },
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed',
        color: '#f0f0f0'
      }
    }
  },
  series: [
    {
      name: 'CPU使用率',
      type: 'line',
      data: cpuData,
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

// Memory Chart Option
const memoryChartOption = computed(() => ({
  title: {
    text: '内存使用',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 600,
      color: '#262626'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '15%',
    top: '20%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: timeLabels,
    axisLabel: {
      fontSize: 12,
      color: '#666'
    }
  },
  yAxis: {
    type: 'value',
    name: '内存 (GB)',
    nameLocation: 'middle',
    nameGap: 40,
    min: 0,
    max: 8,
    interval: 1,
    axisLabel: {
      fontSize: 12,
      color: '#666',
      formatter: '{value}'
    },
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed',
        color: '#f0f0f0'
      }
    }
  },
  series: [
    {
      name: '内存使用',
      type: 'line',
      data: memoryData,
      smooth: true,
      lineStyle: {
        color: '#52c41a',
        width: 2
      },
      itemStyle: {
        color: '#52c41a'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ]
        }
      }
    }
  ]
}));

// Battery and Temperature Chart Option
const batteryChartOption = computed(() => ({
  title: {
    text: '电池与温度',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 600,
      color: '#262626'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['电池电量', '电池温度'],
    top: 30,
    right: 20
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '15%',
    top: '20%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: timeLabels,
    axisLabel: {
      fontSize: 12,
      color: '#666'
    }
  },
  yAxis: [
    {
      type: 'value',
      name: '电池电量 (%)',
      nameLocation: 'middle',
      nameGap: 50,
      min: 0,
      max: 100,
      interval: 10,
      position: 'left',
      axisLabel: {
        fontSize: 12,
        color: '#666',
        formatter: '{value}%'
      },
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed',
          color: '#f0f0f0'
        }
      }
    },
    {
      type: 'value',
      name: '电池温度 (°C)',
      nameLocation: 'middle',
      nameGap: 50,
      min: 20,
      max: 40,
      interval: 2,
      position: 'right',
      axisLabel: {
        fontSize: 12,
        color: '#666',
        formatter: '{value}°C'
      }
    }
  ],
  series: [
    {
      name: '电池电量',
      type: 'line',
      yAxisIndex: 0,
      data: batteryData,
      smooth: true,
      lineStyle: {
        color: '#52c41a',
        width: 2
      },
      itemStyle: {
        color: '#52c41a'
      }
    },
    {
      name: '电池温度',
      type: 'line',
      yAxisIndex: 1,
      data: temperatureData,
      smooth: true,
      lineStyle: {
        color: '#ff4d4f',
        width: 2
      },
      itemStyle: {
        color: '#ff4d4f'
      }
    }
  ]
}));

// Network Traffic Chart Option
const networkChartOption = computed(() => ({
  title: {
    text: '网络流量',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 600,
      color: '#262626'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['下载 (MB)', '上传 (MB)'],
    top: 30,
    right: 20
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '15%',
    top: '20%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: timeLabels,
    axisLabel: {
      fontSize: 12,
      color: '#666'
    }
  },
  yAxis: {
    type: 'value',
    name: '流量 (MB)',
    nameLocation: 'middle',
    nameGap: 40,
    min: 0,
    max: 80,
    interval: 10,
    axisLabel: {
      fontSize: 12,
      color: '#666',
      formatter: '{value}'
    },
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed',
        color: '#f0f0f0'
      }
    }
  },
  series: [
    {
      name: '下载 (MB)',
      type: 'bar',
      data: downloadData,
      itemStyle: {
        color: '#1890ff',
        borderRadius: [4, 4, 0, 0]
      }
    },
    {
      name: '上传 (MB)',
      type: 'bar',
      data: uploadData,
      itemStyle: {
        color: '#52c41a',
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
}));

// Initialize charts
const initCharts = () => {
  // CPU Chart
  if (cpuChartRef.value) {
    cpuChart = echarts.init(cpuChartRef.value);
    cpuChart.setOption(cpuChartOption.value);
    erd.listenTo(cpuChartRef.value, () => {
      cpuChart?.resize();
    });
  }

  // Memory Chart
  if (memoryChartRef.value) {
    memoryChart = echarts.init(memoryChartRef.value);
    memoryChart.setOption(memoryChartOption.value);
    erd.listenTo(memoryChartRef.value, () => {
      memoryChart?.resize();
    });
  }

  // Battery Chart
  if (batteryChartRef.value) {
    batteryChart = echarts.init(batteryChartRef.value);
    batteryChart.setOption(batteryChartOption.value);
    erd.listenTo(batteryChartRef.value, () => {
      batteryChart?.resize();
    });
  }

  // Network Chart
  if (networkChartRef.value) {
    networkChart = echarts.init(networkChartRef.value);
    networkChart.setOption(networkChartOption.value);
    erd.listenTo(networkChartRef.value, () => {
      networkChart?.resize();
    });
  }
};

// Cleanup
onBeforeUnmount(() => {
//   if (cpuChart) {
//     cpuChart.dispose();
//     cpuChart = null;
//   }
//   if (memoryChart) {
//     memoryChart.dispose();
//     memoryChart = null;
//   }
//   if (batteryChart) {
//     batteryChart.dispose();
//     batteryChart = null;
//   }
//   if (networkChart) {
//     networkChart.dispose();
//     networkChart = null;
//   }
//   if (cpuChartRef.value) {
//     erd.uninstall(cpuChartRef.value);
//   }
//   if (memoryChartRef.value) {
//     erd.uninstall(memoryChartRef.value);
//   }
//   if (batteryChartRef.value) {
//     erd.uninstall(batteryChartRef.value);
//   }
//   if (networkChartRef.value) {
//     erd.uninstall(networkChartRef.value);
//   }
});

onMounted(() => {
  initCharts();
});
</script>

<template>
  <div class="performance-monitoring">
    <!-- Real-time Performance Monitoring -->
    <div class="monitoring-section">
      <div class="section-header">
        <div class="section-title">实时性能监控</div>
      </div>
      <div class="charts-grid">
        <!-- CPU Usage Chart -->
        <Card class="chart-card">
          <div ref="cpuChartRef" class="chart-container"></div>
        </Card>

        <!-- Memory Usage Chart -->
        <Card class="chart-card">
          <div ref="memoryChartRef" class="chart-container"></div>
        </Card>
      </div>
    </div>

    <!-- Device Status Monitoring -->
    <div class="monitoring-section">
      <div class="section-header">
        <div class="section-title">设备状态监控</div>
      </div>
      <div class="charts-grid">
        <!-- Battery and Temperature Chart -->
        <Card class="chart-card">
          <div ref="batteryChartRef" class="chart-container"></div>
        </Card>

        <!-- Network Traffic Chart -->
        <Card class="chart-card">
          <div ref="networkChartRef" class="chart-container"></div>
        </Card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.performance-monitoring {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.monitoring-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header {
  margin-bottom: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* Card Styles */
:deep(.ant-card) {
  background: #fff;
}

:deep(.ant-card-body) {
  padding: 20px;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .chart-container {
    height: 250px;
  }
}
</style>

