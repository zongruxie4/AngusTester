<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { Card } from '@xcan-angus/vue-ui';
import * as echarts from 'echarts/core';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components';
import { LineChart, BarChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { UniversalTransition } from 'echarts/features';
import elementResizeDetector from 'element-resize-detector';

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
const networkChartRef = ref<HTMLElement>();
const sessionChartRef = ref<HTMLElement>();

let cpuChart: echarts.ECharts | null = null;
let memoryChart: echarts.ECharts | null = null;
let networkChart: echarts.ECharts | null = null;
let sessionChart: echarts.ECharts | null = null;

const erd = elementResizeDetector({ strategy: 'scroll' });

// Time labels (0:00 to 22:00, 2-hour intervals)
const timeLabels = ['0:00', '2:00', '4:00', '6:00', '8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'];

// CPU Usage Data (starts around 25%, peaks at 50% at 14:00)
const cpuData = [25, 28, 30, 32, 35, 38, 42, 50, 45, 38, 32, 30];

// Memory Usage Data (in GB, starts around 6GB, peaks at 7.5GB at 14:00)
const memoryData = [6.0, 6.2, 6.4, 6.6, 6.8, 7.0, 7.2, 7.5, 7.3, 7.1, 7.0, 7.0];

// Network Traffic Data
// Download speed (MB/s): starts around 2.5, peaks at 4.8 at 14:00
const downloadData = [2.5, 2.8, 2.6, 3.0, 3.5, 4.0, 4.5, 4.8, 4.2, 3.5, 2.8, 2.5];
// Upload speed (MB/s): starts around 0.8, peaks at 2.5 at 14:00
const uploadData = [0.8, 1.0, 1.2, 1.5, 1.8, 2.0, 2.3, 2.5, 2.2, 1.8, 1.2, 0.8];

// Session Count Data (starts low, peaks at 4.8 at 12:00 and 14:00)
const sessionData = [1.0, 1.2, 1.5, 2.0, 2.5, 3.5, 4.5, 4.8, 3.5, 2.0, 1.5, 0.8];

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
      name: 'CPU使用率 (%)',
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
  ],
  legend: {
    show: true,
    bottom: 0,
    data: ['CPU使用率 (%)']
  }
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
    max: 16,
    interval: 2,
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
      name: '内存使用 (GB)',
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
  ],
  legend: {
    show: true,
    bottom: 0,
    data: ['内存使用 (GB)']
  }
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
    name: '速度 (MB/s)',
    nameLocation: 'middle',
    nameGap: 40,
    min: 0,
    max: 5.0,
    interval: 0.5,
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
      name: '下载速度 (MB/s)',
      type: 'line',
      data: downloadData,
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
    },
    {
      name: '上传速度 (MB/s)',
      type: 'line',
      data: uploadData,
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
  ],
  legend: {
    show: true,
    bottom: 0,
    data: ['下载速度 (MB/s)', '上传速度 (MB/s)']
  }
}));

// Session Count Chart Option
const sessionChartOption = computed(() => ({
  title: {
    text: '会话数量',
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
    name: '会话数',
    nameLocation: 'middle',
    nameGap: 40,
    min: 0,
    max: 5.0,
    interval: 0.5,
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
      name: '活动会话数',
      type: 'bar',
      data: sessionData,
      itemStyle: {
        color: '#722ed1'
      },
      barWidth: '60%'
    }
  ],
  legend: {
    show: true,
    bottom: 0,
    data: ['活动会话数']
  }
}));

// Initialize charts
const initChart = () => {
  if (cpuChartRef.value && !cpuChart) {
    cpuChart = echarts.init(cpuChartRef.value);
    cpuChart.setOption(cpuChartOption.value);
    erd.listenTo(cpuChartRef.value, () => {
      cpuChart?.resize();
    });
  }

  if (memoryChartRef.value && !memoryChart) {
    memoryChart = echarts.init(memoryChartRef.value);
    memoryChart.setOption(memoryChartOption.value);
    erd.listenTo(memoryChartRef.value, () => {
      memoryChart?.resize();
    });
  }

  if (networkChartRef.value && !networkChart) {
    networkChart = echarts.init(networkChartRef.value);
    networkChart.setOption(networkChartOption.value);
    erd.listenTo(networkChartRef.value, () => {
      networkChart?.resize();
    });
  }

  if (sessionChartRef.value && !sessionChart) {
    sessionChart = echarts.init(sessionChartRef.value);
    sessionChart.setOption(sessionChartOption.value);
    erd.listenTo(sessionChartRef.value, () => {
      sessionChart?.resize();
    });
  }
};

// Cleanup charts
const cleanupCharts = () => {
  if (cpuChart && cpuChartRef.value) {
    erd.uninstall(cpuChartRef.value);
    cpuChart.dispose();
    cpuChart = null;
  }
  if (memoryChart && memoryChartRef.value) {
    erd.uninstall(memoryChartRef.value);
    memoryChart.dispose();
    memoryChart = null;
  }
  if (networkChart && networkChartRef.value) {
    erd.uninstall(networkChartRef.value);
    networkChart.dispose();
    networkChart = null;
  }
  if (sessionChart && sessionChartRef.value) {
    erd.uninstall(sessionChartRef.value);
    sessionChart.dispose();
    sessionChart = null;
  }
};

onMounted(() => {
  initChart();
});

onBeforeUnmount(() => {
  cleanupCharts();
});
</script>

<template>
  <div class="performance-monitoring">
    <!-- Real-time Performance Monitoring -->
    <div class="realtime-monitoring">
      <div class="section-title">实时性能监控</div>
      <div class="charts-grid">
        <Card class="chart-card">
          <div ref="cpuChartRef" class="chart-container"></div>
        </Card>
        <Card class="chart-card">
          <div ref="memoryChartRef" class="chart-container"></div>
        </Card>
      </div>
    </div>

    <!-- Network and Session Monitoring -->
    <div class="network-session-monitoring">
      <div class="section-title">网络与会话监控</div>
      <div class="charts-grid">
        <Card class="chart-card">
          <div ref="networkChartRef" class="chart-container"></div>
        </Card>
        <Card class="chart-card">
          <div ref="sessionChartRef" class="chart-container"></div>
        </Card>
      </div>
    </div>

    <!-- Performance Indicators -->
    <div class="performance-indicators">
      <div class="section-title">性能指标</div>
      <div class="indicators-grid">
        <!-- CPU Usage Section -->
        <Card class="indicator-card cpu-card">
          <div class="indicator-header">
            <div class="indicator-title">CPU使用率</div>
          </div>
          <div class="indicator-content">
            <div class="indicator-item">
              <span class="indicator-label">当前CPU使用率:</span>
              <span class="indicator-value">42.5%</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">峰值CPU使用率:</span>
              <span class="indicator-value">78.2%</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">平均CPU使用率:</span>
              <span class="indicator-value">38.3%</span>
            </div>
          </div>
        </Card>

        <!-- Memory Usage Section -->
        <Card class="indicator-card memory-card">
          <div class="indicator-header">
            <div class="indicator-title">内存使用</div>
          </div>
          <div class="indicator-content">
            <div class="indicator-item">
              <span class="indicator-label">当前内存占用:</span>
              <span class="indicator-value">7.8 GB / 16 GB</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">内存使用率:</span>
              <span class="indicator-value">48%</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">可用内存:</span>
              <span class="indicator-value">8.2 GB</span>
            </div>
          </div>
        </Card>

        <!-- Network Performance Section -->
        <Card class="indicator-card network-card">
          <div class="indicator-header">
            <div class="indicator-title">网络性能</div>
          </div>
          <div class="indicator-content">
            <div class="indicator-item">
              <span class="indicator-label">网络上传速度:</span>
              <span class="indicator-value">1.5 MB/s</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">网络下载速度:</span>
              <span class="indicator-value">4.8 MB/s</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">网络延迟:</span>
              <span class="indicator-value">12ms</span>
            </div>
          </div>
        </Card>

        <!-- Session and Response Metrics Section -->
        <Card class="indicator-card session-card">
          <div class="indicator-header">
            <div class="indicator-title">会话和响应指标</div>
          </div>
          <div class="indicator-content">
            <div class="indicator-item">
              <span class="indicator-label">当前会话数:</span>
              <span class="indicator-value">3</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">会话成功率:</span>
              <span class="indicator-value">92%</span>
            </div>
            <div class="indicator-item">
              <span class="indicator-label">平均响应时间:</span>
              <span class="indicator-value">245ms</span>
            </div>
          </div>
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

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
}

.realtime-monitoring,
.network-session-monitoring {
  display: flex;
  flex-direction: column;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
}

/* Performance Indicators Styles */
.performance-indicators {
  display: flex;
  flex-direction: column;
}

.indicators-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.indicator-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  border-left: 4px solid #1890ff;
  background: #fff;
}

.cpu-card {
  border-left-color: #1890ff;
}

.memory-card {
  border-left-color: #52c41a;
}

.network-card {
  border-left-color: #722ed1;
}

.session-card {
  border-left-color: #fa8c16;
}

.indicator-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.indicator-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

.indicator-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.indicator-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.indicator-label {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
}

.indicator-value {
  font-size: 14px;
  color: #262626;
  font-weight: 600;
}

/* Responsive Design for Indicators */
@media (max-width: 1400px) {
  .indicators-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .indicators-grid {
    grid-template-columns: 1fr;
  }
}
</style>

