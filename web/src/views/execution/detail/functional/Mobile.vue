<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { Card, Table, Tag } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import * as echarts from 'echarts/core';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components';
import { BarChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { UniversalTransition } from 'echarts/features';
import elementResizeDetector from 'element-resize-detector';
import TestBasicInfo from '@/components/test/TestBasicInfo.vue';
import { FunctionalTestProps } from '@/views/execution/detail/types';

// eslint-disable-next-line import/no-absolute-path
const props = withDefaults(defineProps<FunctionalTestProps>(), {
  execInfo: undefined,
  execContent: undefined,
  exception: undefined
});


// Register ECharts components
echarts.use([
  GridComponent,
  TooltipComponent,
  LegendComponent,
  BarChart,
  CanvasRenderer,
  UniversalTransition
]);

// Execution Summary Data
const executionSummary = ref({
  total: {
    value: 350,
    trend: 20,
    trendType: 'increase' as 'increase' | 'decrease'
  },
  passed: {
    value: 336,
    passRate: 96
  },
  failed: {
    value: 12,
    trend: 5,
    trendType: 'decrease' as 'increase' | 'decrease'
  },
  skipped: {
    value: 2,
    reason: '环境依赖问题'
  }
});

// Functional Module Pass Rate Data
const modulePassRateData = ref([
  { name: '用户登录', passRate: 92, color: '#1890ff' },
  { name: '用户注册', passRate: 98, color: '#52c41a' },
  { name: '商品浏览', passRate: 95, color: '#1890ff' },
  { name: '购物车', passRate: 97, color: '#1890ff' },
  { name: '支付流程', passRate: 95, color: '#1890ff' },
  { name: '订单管理', passRate: 93, color: '#fa8c16' },
  { name: '个人中心', passRate: 98, color: '#52c41a' }
]);

// Bar chart reference and instance
const barChartRef = ref<HTMLElement>();
let barChart: echarts.ECharts | null = null;
const erd = elementResizeDetector({ strategy: 'scroll' });

// Bar chart configuration
const barChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: (params: any) => {
      const param = params[0];
      return `${param.name}<br/>${param.seriesName}: ${param.value}%`;
    }
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '15%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: modulePassRateData.value.map(item => item.name),
    axisLabel: {
      fontSize: 12,
      color: '#666',
      interval: 0,
      rotate: 0
    },
    axisLine: {
      lineStyle: {
        color: '#e8e8e8'
      }
    },
    axisTick: {
      alignWithLabel: true
    }
  },
  yAxis: {
    type: 'value',
    name: '通过率(%)',
    nameLocation: 'middle',
    nameGap: 40,
    min: 0,
    max: 100,
    interval: 10,
    axisLabel: {
      fontSize: 12,
      color: '#666',
      formatter: '{value}'
    },
    axisLine: {
      lineStyle: {
        color: '#e8e8e8'
      }
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
      name: '通过率',
      type: 'bar',
      barWidth: 30,
      data: modulePassRateData.value.map(item => ({
        value: item.passRate,
        itemStyle: {
          color: item.color,
          borderRadius: [4, 4, 0, 0]
        }
      })),
      label: {
        show: true,
        position: 'top',
        fontSize: 12,
        color: '#666',
        formatter: '{c}%'
      }
    }
  ]
}));

// Initialize bar chart
const initBarChart = () => {
  if (!barChartRef.value) {
    return;
  }
  
  if (barChart) {
    barChart.dispose();
  }
  
  barChart = echarts.init(barChartRef.value);
  barChart.setOption(barChartOption.value);
  
  // Handle resize
  erd.listenTo(barChartRef.value, () => {
    if (barChart) {
      barChart.resize();
    }
  });
};

// Cleanup
onBeforeUnmount(() => {
//   if (barChart) {
//     barChart.dispose();
//     barChart = null;
//   }
//   if (barChartRef.value) {
//     erd.uninstall(barChartRef.value);
//   }
});

onMounted(() => {
  initBarChart();
});

// Appium Configuration Data
const appiumConfig = ref({
  platformName: 'Android',
  platformVersion: '13.0',
  deviceName: '测试设备',
  app: '/path/to/app.apk',
  appPackage: 'com.example.app',
  appActivity: '.MainActivity',
  autoGrantPermissions: true,
  noReset: false,
  fullReset: false,
  newCommandTimeout: 300,
  enablePerformanceLogging: true,
  autoWebview: true,
  takesScreenshot: true,
  screenshotQuality: 100
});

// Format JSON for display
const formattedAppiumConfig = computed(() => {
  return JSON.stringify(appiumConfig.value, null, 2);
});

// Test Scenario Summary Data
const testScenarioSummary = ref([
  {
    id: '1',
    title: '登录功能测试',
    status: 'failed',
    statusText: '失败',
    details: {
      failureReason: '元素未找到 (登录按钮)',
      executionDuration: 5.2,
      elementLocationTime: 1.2,
      screenshotPath: '/screenshots/login_test_fail.png'
    }
  },
  {
    id: '2',
    title: '用户注册流程',
    status: 'passed',
    statusText: '通过',
    details: {
      testSteps: 8,
      executionDuration: 7.8,
      assertionResult: '全部通过 (8/8)'
    }
  },
  {
    id: '3',
    title: '购物车操作测试',
    status: 'passed',
    statusText: '通过',
    details: {
      testSteps: 12,
      executionDuration: 10.5,
      operationResponseTime: 0.8
    }
  }
]);

// Functional Module Test Results Data
const functionalModuleResults = ref([
  {
    key: '1',
    module: '用户登录',
    totalCases: 45,
    passed: 42,
    failed: 3,
    passRate: 93.3,
    avgDuration: 4.2
  },
  {
    key: '2',
    module: '用户注册',
    totalCases: 30,
    passed: 30,
    failed: 0,
    passRate: 100,
    avgDuration: 6.8
  },
  {
    key: '3',
    module: '商品浏览',
    totalCases: 60,
    passed: 58,
    failed: 2,
    passRate: 96.7,
    avgDuration: 3.5
  },
  {
    key: '4',
    module: '购物车操作',
    totalCases: 75,
    passed: 73,
    failed: 2,
    passRate: 97.3,
    avgDuration: 8.2
  },
  {
    key: '5',
    module: '支付流程',
    totalCases: 50,
    passed: 48,
    failed: 2,
    passRate: 96,
    avgDuration: 12.5
  }
]);

// Table columns for functional module results
const moduleResultsColumns = [
  {
    title: '功能模块',
    dataIndex: 'module',
    key: 'module',
    width: 150
  },
  {
    title: '用例数',
    dataIndex: 'totalCases',
    key: 'totalCases',
    width: 100
  },
  {
    title: '通过数',
    dataIndex: 'passed',
    key: 'passed',
    width: 100
  },
  {
    title: '失败数',
    dataIndex: 'failed',
    key: 'failed',
    width: 100
  },
  {
    title: '通过率',
    dataIndex: 'passRate',
    key: 'passRate',
    width: 120
  },
  {
    title: '平均时长',
    dataIndex: 'avgDuration',
    key: 'avgDuration',
    width: 120
  }
];
</script>

<template>
  <div class="execution-summary">
    <TestBasicInfo :value="props.execInfo" :exception="props.exception" class="mb-4" />
    <div class="text-4 font-semibold mb-4 text-title">执行摘要</div>
    
    <!-- Execution Summary Cards -->
    <div class="summary-cards-grid">
      <!-- Total Test Cases Card -->
      <Card class="summary-card total-card">
        <div class="summary-card-content">
          <div class="summary-card-label">测试用例总数</div>
          <div class="summary-card-value">{{ executionSummary.total.value }}</div>
          <div class="summary-card-trend increase">
            <Icon icon="icon-shangsheng" class="trend-icon" />
            较上次增加 {{ executionSummary.total.trend }}
          </div>
        </div>
      </Card>

      <!-- Passed Test Cases Card -->
      <Card class="summary-card passed-card">
        <div class="summary-card-content">
          <div class="summary-card-label">通过用例</div>
          <div class="summary-card-value">{{ executionSummary.passed.value }}</div>
          <div class="summary-card-pass-rate">
            通过率 {{ executionSummary.passed.passRate }}%
          </div>
        </div>
      </Card>

      <!-- Failed Test Cases Card -->
      <Card class="summary-card failed-card">
        <div class="summary-card-content">
          <div class="summary-card-label">失败用例</div>
          <div class="summary-card-value">{{ executionSummary.failed.value }}</div>
          <div class="summary-card-trend decrease">
            <Icon icon="icon-xiajiang" class="trend-icon" />
            较上次减少 {{ executionSummary.failed.trend }}
          </div>
        </div>
      </Card>

      <!-- Skipped Test Cases Card -->
      <Card class="summary-card skipped-card">
        <div class="summary-card-content">
          <div class="summary-card-label">跳过用例</div>
          <div class="summary-card-value">{{ executionSummary.skipped.value }}</div>
          <div class="summary-card-reason">
            {{ executionSummary.skipped.reason }}
          </div>
        </div>
      </Card>
    </div>

    <!-- Functional Module Pass Rate Chart -->
    <Card class="pass-rate-chart-card">
      <template #title>
        <div class="section-title">各功能模块测试通过率</div>
      </template>
      <div ref="barChartRef" class="pass-rate-chart"></div>
    </Card>

    <!-- Appium Configuration Details -->
    <Card class="appium-config-card">
      <template #title>
        <div class="appium-config-header">
          <Icon icon="icon-shezhi" class="config-icon" />
          <span class="section-title">Appium 配置详情</span>
        </div>
      </template>
      <div class="appium-config-content">
        <pre class="appium-config-json">{{ formattedAppiumConfig }}</pre>
      </div>
    </Card>

    <!-- Test Results Details -->
    <div class="test-results-details">
      <div class="text-4 font-semibold mb-4 text-title">测试结果明细</div>
      
      <!-- Test Scenario Summary Cards -->
      <div class="scenario-cards-grid">
        <Card
          v-for="scenario in testScenarioSummary"
          :key="scenario.id"
          :class="['scenario-card', scenario.status === 'failed' ? 'scenario-failed' : 'scenario-passed']">
          <div class="scenario-card-header">
            <div class="scenario-title">{{ scenario.title }}</div>
            <Tag
              :color="scenario.status === 'failed' ? 'error' : 'success'"
              class="scenario-status-tag">
              {{ scenario.statusText }}
            </Tag>
          </div>
          <div class="scenario-card-content">
            <!-- Failed scenario details -->
            <template v-if="scenario.status === 'failed'">
              <div class="scenario-detail-item">
                <span class="detail-label">失败原因：</span>
                <span class="detail-value">{{ scenario.details.failureReason }}</span>
              </div>
              <div class="scenario-detail-item">
                <span class="detail-label">执行时长：</span>
                <span class="detail-value">{{ scenario.details.executionDuration }}秒</span>
              </div>
              <div class="scenario-detail-item">
                <span class="detail-label">元素定位时间：</span>
                <span class="detail-value">{{ scenario.details.elementLocationTime }}秒</span>
              </div>
              <div class="scenario-detail-item">
                <span class="detail-label">截图路径：</span>
                <span class="detail-value screenshot-path">{{ scenario.details.screenshotPath }}</span>
              </div>
            </template>
            <!-- Passed scenario details -->
            <template v-else>
              <div class="scenario-detail-item">
                <span class="detail-label">测试步骤数：</span>
                <span class="detail-value">{{ scenario.details.testSteps }}</span>
              </div>
              <div class="scenario-detail-item">
                <span class="detail-label">执行时长：</span>
                <span class="detail-value">{{ scenario.details.executionDuration }}秒</span>
              </div>
              <div v-if="scenario.details.assertionResult" class="scenario-detail-item">
                <span class="detail-label">断言结果：</span>
                <span class="detail-value">{{ scenario.details.assertionResult }}</span>
              </div>
              <div v-if="scenario.details.operationResponseTime" class="scenario-detail-item">
                <span class="detail-label">操作响应时间：</span>
                <span class="detail-value">平均{{ scenario.details.operationResponseTime }}秒</span>
              </div>
            </template>
          </div>
        </Card>
      </div>

      <!-- Functional Module Test Results Table -->
      <Card class="module-results-card">
        <template #title>
          <div class="section-title">功能模块测试结果</div>
        </template>
        <Table
          :columns="moduleResultsColumns"
          :dataSource="functionalModuleResults"
          :pagination="false"
          size="middle"
          class="module-results-table">
          <template #bodyCell="{ column, text }">
            <template v-if="column.dataIndex === 'failed'">
              <span :class="{ 'failed-highlight': text > 0 }">{{ text }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'passRate'">
              <span class="pass-rate-value">{{ text }}%</span>
            </template>
            <template v-else-if="column.dataIndex === 'avgDuration'">
              <span class="duration-value">{{ text }}秒</span>
            </template>
            <template v-else>
              {{ text }}
            </template>
          </template>
        </Table>
      </Card>
    </div>
  </div>
</template>

<style scoped>
/* Execution Summary Container - padding removed as per user preference */

/* Summary Cards Grid */
.summary-cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.summary-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* Card Color Themes */
.total-card {
  border-left: 4px solid #1890ff;
}

.passed-card {
  border-left: 4px solid #52c41a;
}

.failed-card {
  border-left: 4px solid #ff4d4f;
}

.skipped-card {
  border-left: 4px solid #fa8c16;
}

.summary-card-content {
  padding: 20px;
}

.summary-card-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 12px;
  font-weight: 500;
}

.summary-card-value {
  font-size: 32px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
  line-height: 1.2;
}

.summary-card-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
}

.summary-card-trend.increase {
  color: #52c41a;
}

.summary-card-trend.decrease {
  color: #ff4d4f;
}

.trend-icon {
  font-size: 14px;
}

.summary-card-pass-rate {
  font-size: 13px;
  color: #52c41a;
  font-weight: 500;
}

.summary-card-reason {
  font-size: 13px;
  color: #8c8c8c;
}

/* Pass Rate Chart Card */
.pass-rate-chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.pass-rate-chart {
  width: 100%;
  height: 200px;
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
  .summary-cards-grid {
    grid-template-columns: 1fr;
  }
  
  .pass-rate-chart {
    height: 200px;
  }
}

@media (min-width: 1200px) {
  .summary-cards-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* Appium Configuration Card */
.appium-config-card {
  margin-top: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.appium-config-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-icon {
  font-size: 18px;
  color: #1890ff;
}

.appium-config-content {
  padding: 20px;
  background: #2f2f2f;
  border-radius: 4px;
  overflow-x: auto;
}

.appium-config-json {
  margin: 0;
  padding: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d4;
  white-space: pre;
  word-wrap: normal;
  overflow-x: auto;
}

/* JSON Syntax Highlighting (basic) - Note: deep selectors may not work with pre tag */

/* Test Results Details Section */
.test-results-details {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

/* Test Scenario Cards Grid */
.scenario-cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.scenario-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  overflow: hidden;
  position: relative;
}

.scenario-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.scenario-failed {
  background: linear-gradient(135deg, #fff1f0 0%, #ffffff 100%);
  border: 1px solid #ffccc7;
}

.scenario-passed {
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
  border: 1px solid #b7eb8f;
}

.scenario-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.scenario-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.scenario-status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.scenario-card-content {
  padding: 16px 20px;
}

.scenario-detail-item {
  display: flex;
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.scenario-detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: #8c8c8c;
  min-width: 100px;
  flex-shrink: 0;
}

.detail-value {
  color: #262626;
  flex: 1;
  word-break: break-all;
}

.screenshot-path {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 12px;
  color: #1890ff;
  background: #f0f9ff;
  padding: 2px 6px;
  border-radius: 4px;
}

/* Module Results Table Card */
.module-results-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.module-results-table {
  margin-top: 16px;
}

.module-results-table :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  border-bottom: 2px solid #f0f0f0;
}

.module-results-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #f0f0f0;
}

.module-results-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #fafafa;
}

.failed-highlight {
  color: #ff4d4f;
  font-weight: 600;
}

.pass-rate-value {
  color: #52c41a;
  font-weight: 500;
}

.duration-value {
  color: #595959;
}

.module-results-card :deep(.ant-card-body) {
  padding: 20px;
}

/* Responsive Design for Test Results */
@media (max-width: 1200px) {
  .scenario-cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .scenario-cards-grid {
    grid-template-columns: 1fr;
  }
  
  .module-results-table {
    overflow-x: auto;
  }
}
</style>

