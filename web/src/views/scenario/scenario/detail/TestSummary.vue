<script lang="ts" setup>
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import { Popover } from 'ant-design-vue';
import { ScriptType } from '@xcan-angus/infra';

import { useTestSummary } from './composables/useTestSummary';
import type { TestSummaryProps } from './types';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = withDefaults(defineProps<TestSummaryProps>(), {
  dataSource: undefined,
  scriptType: undefined
});

// Use test summary composable
const {
  testChartRef,
  resultSummary,
  performanceTestData,
  customTestData,
  functionalityTestData,
  stabilityTestData,
  configInfo,
  getTpsTrendIcon,
  getResponseTimeTrendIcon,
  getErrorRateTrendIcon
} = useTestSummary(computed(() => props.dataSource));

/**
 * Get status icon based on result status
 */
const getStatusIcon = (status: string) => {
  switch (status) {
    case 'FULLY_PASSED':
      return 'icon-chenggong';
    case 'PARTIALLY_PASSED':
      return 'icon-jinggao';
    case 'FULLY_FAILED':
      return 'icon-shibai';
    case 'NOT_ENABLED':
      return 'icon-weikaiqi';
    case 'UNTESTED':
      return 'icon-weiceshi';
    default:
      return 'icon-weizhi';
  }
};

/**
 * Get test status icon based on test data
 */
const getTestStatusIcon = (testData: any) => {
  if (!testData) return 'icon-weizhi';
  if (testData.passed) return 'icon-chenggong';
  return 'icon-shibai';
};
</script>
<template>
  <div class="test-summary-grid">
    <!-- Overall test results card -->
    <div v-show="resultSummary" class="summary-card all-tests-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="icon-zonghe" class="title-icon" />
          {{ t('scenario.detail.testSummary.sections.allTests') }}
        </div>
      </div>

      <div class="status-display">
        <div class="status-badge" :class="resultSummary?.resultStatus?.value">
          <Icon :icon="getStatusIcon(resultSummary?.resultStatus?.value)" class="status-icon" />
          {{ resultSummary?.resultStatus?.message }}
        </div>
      </div>

      <div ref="testChartRef" class="chart-container"></div>
    </div>

    <!-- Functionality test card -->
    <div v-show="props.scriptType === ScriptType.TEST_FUNCTIONALITY" class="summary-card functionality-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="icon-gongneng" class="title-icon" />
          {{ t('scenario.detail.testSummary.sections.functionality') }}
        </div>
      </div>

      <div class="status-display">
        <div
          class="status-badge"
          :class="[!functionalityTestData ? '' : functionalityTestData.passed ? 'PASSED': 'NOT_PASSED']">
          <Icon :icon="getTestStatusIcon(functionalityTestData)" class="status-icon" />
          {{ !functionalityTestData ? t('status.notTested')
            : functionalityTestData.passed ? t('status.passed')
              : t('status.notPassed') }}
        </div>
        <Popover>
          <template #content>
            <div class="popover-content">
              {{ functionalityTestData?.failureMessage }}
            </div>
          </template>
          <Icon
            v-if="functionalityTestData && !functionalityTestData.passed"
            icon="icon-tishi1"
            class="warning-icon" />
        </Popover>
      </div>

      <div class="config-section">
        <div class="config-grid">
          <div
            v-for="(line, idx) in configInfo"
            :key="idx"
            class="config-row">
            <div
              v-for="item in line"
              :key="item.dataIndex"
              class="config-item">
              <span
                v-if="item.label"
                class="config-label"
                :class="item.bgColor">{{ item.label }}</span>
              <span v-if="item.dataIndex" class="config-value">
                {{ functionalityTestData?.targetSummary?.[item.dataIndex] || '--' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="card-footer">
        <Icon icon="icon-jiekou" class="footer-icon" />
        <span class="footer-text">{{ t('common.testCases') }}</span>
      </div>
    </div>

    <!-- Performance test card -->
    <div v-show="props.scriptType === ScriptType.TEST_PERFORMANCE" class="summary-card performance-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="icon-xingneng" class="title-icon" />
          {{ t('scenario.detail.testSummary.sections.performance') }}
        </div>
      </div>

      <div class="status-display">
        <div class="status-badge" :class="[!performanceTestData ? '' : performanceTestData.passed ? 'PASSED' : 'NOT_PASSED']">
          <Icon :icon="getTestStatusIcon(performanceTestData)" class="status-icon" />
          {{ !performanceTestData ? t('status.notTested')
            : performanceTestData.passed ? t('status.passed')
              : t('status.notPassed') }}
        </div>
        <Popover>
          <template #content>
            <div class="popover-content">
              {{ performanceTestData?.failureMessage }}
            </div>
          </template>
          <Icon
            v-if="performanceTestData && !performanceTestData.passed"
            icon="icon-tishi1"
            class="warning-icon" />
        </Popover>
      </div>

      <div class="metrics-section">
        <div class="metrics-grid">
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-shijian" class="metric-icon" />
              <span class="metric-title">{{ t('scenario.detail.testSummary.metrics.tps') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ performanceTestData?.sampleSummary?.tps || '--' }}</span>
                <Icon
                  v-if="performanceTestData"
                  :icon="getTpsTrendIcon(performanceTestData as any)"
                  class="trend-icon" />
              </div>
            </div>
          </div>
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-shijian" class="metric-icon" />
              <span class="metric-title">{{ t('protocol.responseTimeP90') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ performanceTestData?.sampleSummary?.tranP90 || '--' }}</span>
                <Icon
                  v-if="performanceTestData"
                  :icon="getResponseTimeTrendIcon(performanceTestData as any)"
                  class="trend-icon" />
              </div>
            </div>
          </div>
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-cuowu" class="metric-icon" />
              <span class="metric-title">{{ t('scenario.detail.testSummary.metrics.errorRate') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ performanceTestData?.sampleSummary?.errorRate || '--' }}%</span>
                <Icon
                  v-if="performanceTestData"
                  :icon="getErrorRateTrendIcon(performanceTestData as any)"
                  class="trend-icon" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card-footer">
        <Icon icon="icon-shuju" class="footer-icon" />
        <span class="footer-text">{{ t('scenario.detail.testSummary.sections.resultMetrics') }}</span>
      </div>
    </div>

    <!-- Stability test card -->
    <div v-show="props.scriptType === ScriptType.TEST_STABILITY" class="summary-card stability-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="icon-xingneng" class="title-icon" />
          {{ t('scenario.detail.testSummary.sections.stability') }}
        </div>
      </div>

      <div class="status-display">
        <div class="status-badge" :class="[!stabilityTestData ? '' : stabilityTestData.passed ? 'PASSED': 'NOT_PASSED']">
          <Icon :icon="getTestStatusIcon(stabilityTestData)" class="status-icon" />
          {{ !stabilityTestData ? t('status.notTested')
            : stabilityTestData.passed ? t('status.passed')
              : t('status.notPassed') }}
        </div>
        <Popover>
          <template #content>
            <div class="popover-content">
              {{ stabilityTestData?.failureMessage }}
            </div>
          </template>
          <Icon
            v-if="stabilityTestData && !stabilityTestData.passed"
            icon="icon-tishi1"
            class="warning-icon" />
        </Popover>
      </div>

      <div class="metrics-section">
        <div class="metrics-grid">
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-shijian" class="metric-icon" />
              <span class="metric-title">{{ t('scenario.detail.testSummary.metrics.tps') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ stabilityTestData?.sampleSummary?.tps || '--' }}</span>
                <Icon
                  v-if="stabilityTestData"
                  :icon="getTpsTrendIcon(stabilityTestData as any)"
                  class="trend-icon" />
              </div>
            </div>
          </div>
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-shijian" class="metric-icon" />
              <span class="metric-title">{{ t('protocol.responseTimeP90') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ stabilityTestData?.sampleSummary?.tranP90 || '--' }}</span>
                <Icon
                  v-if="stabilityTestData"
                  :icon="getResponseTimeTrendIcon(stabilityTestData as any)"
                  class="trend-icon" />
              </div>
            </div>
          </div>
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-cuowu" class="metric-icon" />
              <span class="metric-title">{{ t('scenario.detail.testSummary.metrics.errorRate') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ stabilityTestData?.sampleSummary?.errorRate || '--' }}%</span>
                <Icon
                  v-if="stabilityTestData"
                  :icon="getErrorRateTrendIcon(stabilityTestData as any)"
                  class="trend-icon" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card-footer">
        <Icon icon="icon-shuju" class="footer-icon" />
        <span class="footer-text">{{ t('scenario.detail.testSummary.sections.resultMetrics') }}</span>
      </div>
    </div>

    <!-- Custom test card -->
    <div v-show="props.scriptType === ScriptType.TEST_CUSTOMIZATION" class="summary-card custom-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="icon-xingneng" class="title-icon" />
          {{ t('scenario.detail.testSummary.sections.custom') }}
        </div>
      </div>

      <div class="status-display">
        <div class="status-badge non-standard">
          <Icon icon="icon-feibiaozhun" class="status-icon" />
          {{ t('scenario.detail.testSummary.sections.nonStandard') }}
        </div>
      </div>

      <div class="metrics-section">
        <div class="metrics-grid">
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-shijian" class="metric-icon" />
              <span class="metric-title">{{ t('scenario.detail.testSummary.metrics.tps') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ customTestData?.sampleSummary?.tps || '--' }}</span>
              </div>
            </div>
          </div>
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-shijian" class="metric-icon" />
              <span class="metric-title">{{ t('protocol.responseTimeP90') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ customTestData?.sampleSummary?.tranP90 || '--' }}</span>
              </div>
            </div>
          </div>
          <div class="metric-card">
            <div class="metric-header">
              <Icon icon="icon-cuowu" class="metric-icon" />
              <span class="metric-title">{{ t('scenario.detail.testSummary.metrics.errorRate') }}</span>
            </div>
            <div class="metric-value-container">
              <div class="metric-value">
                <span class="metric-number">{{ customTestData?.sampleSummary?.errorRate || '--' }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card-footer">
        <Icon icon="icon-shuju" class="footer-icon" />
        <span class="footer-text">{{ t('scenario.detail.testSummary.sections.resultMetrics') }}</span>
      </div>
    </div>
  </div>
</template>
<style scoped>
/* Main container grid layout */
.test-summary-grid {
  @apply grid gap-3;
  grid-template-columns: repeat(5, 1fr);
  min-width: 0;
}

/* Card base styles */
.summary-card {
  @apply bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-all duration-300;
  @apply flex flex-col min-h-[200px] overflow-hidden;
  @apply transform hover:-translate-y-0.5;
  min-width: 0;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* Card header */
.card-header {
  @apply px-3 py-2 border-b border-gray-100 bg-gradient-to-r from-gray-50 to-white;
}

.card-title {
  @apply flex items-center space-x-1 text-sm font-bold text-gray-800;
  font-size: 14px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.title-icon {
  @apply text-sm text-blue-500;
  flex-shrink: 0;
}

/* Status display area */
.status-display {
  @apply px-3 py-2 flex items-center justify-center space-x-2;
}

.status-badge {
  @apply px-2 py-1 rounded-full text-xs font-bold flex items-center space-x-1;
  @apply transition-all duration-200 shadow-sm;
  font-size: 12px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.status-badge.PASSED {
  @apply bg-green-100 text-green-800 border border-green-200;
}

.status-badge.NOT_PASSED {
  @apply bg-red-100 text-red-800 border border-red-200;
}

.status-badge.PARTIALLY_PASSED {
  @apply bg-yellow-100 text-yellow-800 border border-yellow-200;
}

.status-badge.non-standard {
  @apply bg-blue-100 text-blue-800 border border-blue-200;
}

.status-icon {
  @apply text-xs;
  flex-shrink: 0;
}

/* Warning icon */
.warning-icon {
  @apply text-yellow-500 text-sm cursor-pointer hover:text-yellow-600 transition-colors;
  flex-shrink: 0;
}

/* Chart container */
.chart-container {
  @apply flex-1 p-2 min-h-[100px];
}

/* Configuration info area */
.config-section {
  @apply px-2 py-1 flex-1;
}

.config-grid {
  @apply space-y-1;
}

.config-row {
  @apply grid grid-cols-2 gap-1;
}

.config-item {
  @apply flex rounded overflow-hidden shadow-sm;
}

.config-label {
  @apply px-2 py-1 text-white text-xs font-bold flex-1;
  font-size: 12px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.config-value {
  @apply px-2 py-1 bg-gray-100 text-gray-700 text-xs flex-1;
  font-size: 12px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* Metrics area */
.metrics-section {
  @apply px-2 py-1 flex-1;
}

.metrics-grid {
  @apply grid grid-cols-3 gap-1;
}

.metric-card {
  @apply bg-gradient-to-br from-gray-50 to-white rounded p-2 border border-gray-100;
  @apply hover:shadow-sm transition-all duration-200;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.metric-header {
  @apply flex items-center space-x-1 mb-1;
}

.metric-icon {
  @apply text-blue-500 text-xs;
  flex-shrink: 0;
}

.metric-title {
  @apply text-xs text-gray-600 font-bold;
  font-size: 12px;
  word-wrap: break-word;
  overflow-wrap: break-word;
  line-height: 1.2;
}

.metric-value-container {
  @apply flex-1;
  min-height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.metric-value {
  @apply flex items-center justify-center space-x-1;
  width: 100%;
}

.metric-number {
  @apply text-sm font-bold text-gray-800;
  font-size: 12px;
  text-align: center;
  word-wrap: break-word;
  overflow-wrap: break-word;
  line-height: 1.2;
}

.trend-icon {
  @apply text-xs text-gray-400;
  flex-shrink: 0;
}

/* Card footer */
.card-footer {
  @apply px-2 py-1 border-t border-gray-100 bg-gray-50 flex items-center space-x-1;
}

.footer-icon {
  @apply text-gray-500 text-xs;
  flex-shrink: 0;
}

.footer-text {
  @apply text-xs text-gray-600 font-bold;
  font-size: 12px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* Popover content */
.popover-content {
  @apply max-w-80 text-sm text-gray-700;
}

/* Special card styles */
.all-tests-card {
  @apply border-blue-200 bg-gradient-to-br from-blue-50 to-white;
}

.all-tests-card .card-header {
  @apply bg-gradient-to-r from-blue-50 to-white;
}

.functionality-card {
  @apply border-green-200 bg-gradient-to-br from-green-50 to-white;
}

.functionality-card .card-header {
  @apply bg-gradient-to-r from-green-50 to-white;
}

.performance-card {
  @apply border-orange-200 bg-gradient-to-br from-orange-50 to-white;
}

.performance-card .card-header {
  @apply bg-gradient-to-r from-orange-50 to-white;
}

.stability-card {
  @apply border-purple-200 bg-gradient-to-br from-purple-50 to-white;
}

.stability-card .card-header {
  @apply bg-gradient-to-r from-purple-50 to-white;
}

.custom-card {
  @apply border-gray-200 bg-gradient-to-br from-gray-50 to-white;
}

.custom-card .card-header {
  @apply bg-gradient-to-r from-gray-50 to-white;
}

/* Responsive design */
@media (max-width: 1200px) {
  .test-summary-grid {
    grid-template-columns: repeat(5, 1fr);
    @apply gap-2;
  }

  .summary-card {
    @apply min-h-[180px];
  }

  .card-title {
    font-size: 12px;
  }

  .status-badge {
    font-size: 10px;
  }

  .metric-title,
  .metric-number,
  .config-label,
  .config-value,
  .footer-text {
    font-size: 10px;
  }
}

@media (max-width: 768px) {
  .test-summary-grid {
    grid-template-columns: repeat(5, 1fr);
    @apply gap-1;
  }

  .summary-card {
    @apply min-h-[160px];
  }

  .card-header {
    @apply px-2 py-1;
  }

  .status-display {
    @apply px-2 py-1;
  }

  .config-section,
  .metrics-section {
    @apply px-1 py-1;
  }

  .card-footer {
    @apply px-1 py-1;
  }
}

/* Status color enhancement */
.PARTIALLY_PASSED {
  @apply text-yellow-700;
}

.PASSED {
  @apply text-green-700;
}

.NOT_PASSED {
  @apply text-red-700;
}

/* Micro animation effects */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.summary-card {
  animation: fadeInUp 0.6s ease-out;
}

/* Hover effect enhancement */
.summary-card:hover .card-title {
  @apply text-blue-600;
}

.summary-card:hover .title-icon {
  @apply text-blue-600 scale-110;
}

.metric-card:hover {
  @apply transform scale-105;
}

/* Status badge animation */
.status-badge:hover {
  @apply transform scale-105;
}

/* Icon animation */
.title-icon,
.status-icon,
.metric-icon,
.footer-icon {
  @apply transition-transform duration-200;
}

.summary-card:hover .title-icon {
  @apply rotate-12;
}
</style>
