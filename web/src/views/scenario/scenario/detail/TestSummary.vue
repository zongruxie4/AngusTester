<script lang="ts" setup>
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import { Popover } from 'ant-design-vue';
import {} from '@/enums/enums';

import { useTestSummary } from './composables/useTestSummary';
import type { TestSummaryProps } from './types';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = withDefaults(defineProps<TestSummaryProps>(), {
  dataSource: undefined
});

// Use test summary composable
const {
  testChartRef,
  resultSummary,
  TEST_PERFORMANCE,
  TEST_CUSTOMIZATION,
  TEST_FUNCTIONALITY,
  TEST_STABILITY,
  configInfo,
  getTpsIconName,
  getTranIconName,
  getErrIconName
} = useTestSummary(computed(() => props.dataSource));
</script>
<template>
  <div class="flex space-x-2">
    <div v-show="resultSummary" class="flex-1 border rounded p-1 space-y-3 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('scenario.detail.testSummary.sections.allTests') }}</div>

      <div class="font-semibold text-6 text-center" :class="resultSummary?.resultStatus?.value">
        {{ resultSummary?.resultStatus?.message }}
      </div>

      <div ref="testChartRef" class="flex-1"></div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-3">
      <div class="font-semibold text-text-title">{{ t('scenario.detail.testSummary.sections.functionality') }}</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_FUNCTIONALITY ? '' : TEST_FUNCTIONALITY.passed ? 'PASSED': 'NOT_PASSED']">
          {{ !TEST_FUNCTIONALITY ? t('scenario.detail.testSummary.status.notTested')
            : TEST_FUNCTIONALITY.passed ? t('scenario.detail.testSummary.status.passed')
              : t('scenario.detail.testSummary.status.notPassed') }}</span>
        <Popover>
          <template #content>
            <div class="max-w-80">
              {{ TEST_FUNCTIONALITY?.failureMessage }}
            </div>
          </template>
          <Icon
            v-if="TEST_FUNCTIONALITY && !TEST_FUNCTIONALITY.passed"
            icon="icon-tishi1"
            class="text-status-warn text-3" />
        </Popover>
      </div>

      <div class="space-y-2 text-3 px-2">
        <li
          v-for="(line, idx) in configInfo"
          :key="idx"
          class="flex space-x-2">
          <div
            v-for="item in line"
            :key="item.dataIndex"
            class="flex flex-1 h-7 leading-7">
            <span
              v-if="item.label"
              class="flex-1 text-white px-2 rounded"
              :class="item.bgColor">{{ item.label }}</span>
            <span v-if="item.dataIndex" class="flex-1 bg-gray-light px-2 rounded-r">
              {{ TEST_FUNCTIONALITY?.targetSummary?.[item.dataIndex] || '--' }}
            </span>
          </div>
        </li>
      </div>

      <div class="text-center font-semibold text-text-title">
        {{ t('scenario.detail.testSummary.sections.testInterfaces') }}
      </div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('scenario.detail.testSummary.sections.performance') }}</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_PERFORMANCE ? '' : TEST_PERFORMANCE.passed ? 'PASSED' : 'NOT_PASSED']">
          {{ !TEST_PERFORMANCE ? t('scenario.detail.testSummary.status.notTested')
            : TEST_PERFORMANCE.passed ? t('scenario.detail.testSummary.status.passed')
              : t('scenario.detail.testSummary.status.notPassed') }}
        </span>
        <Popover>
          <template #content>
            <div class="max-w-80">
              {{ TEST_PERFORMANCE?.failureMessage }}
            </div>
          </template>
          <Icon
            v-if="TEST_PERFORMANCE && !TEST_PERFORMANCE.passed"
            icon="icon-tishi1"
            class="text-status-warn text-3" />
        </Popover>
      </div>

      <div class="flex">
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_PERFORMANCE?.sampleSummary?.tps || '--' }}</span>
            <Icon v-if="TEST_PERFORMANCE" :icon="getTpsIconName(TEST_PERFORMANCE)" />
          </div>
          <div>{{ t('scenario.detail.testSummary.metrics.tps') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_PERFORMANCE?.sampleSummary?.tranP90 || '--' }}</span>
            <Icon v-if="TEST_PERFORMANCE" :icon="getTranIconName(TEST_PERFORMANCE)" />
          </div>
          <div>{{ t('scenario.detail.testSummary.metrics.responseTime') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_PERFORMANCE?.sampleSummary?.errorRate || '--' }}%</span>
            <Icon v-if="TEST_PERFORMANCE" :icon="getErrIconName(TEST_PERFORMANCE)" />
          </div>
          <div>{{ t('scenario.detail.testSummary.metrics.errorRate') }}</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">{{ t('scenario.detail.testSummary.sections.resultMetrics') }}</div>
    </div>

    <div v-show="TEST_STABILITY" class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('scenario.detail.testSummary.sections.stability') }}</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_STABILITY ? '' : TEST_STABILITY.passed ? 'PASSED': 'NOT_PASSED']">
          {{ !TEST_STABILITY ? t('scenario.detail.testSummary.status.notTested')
            : TEST_STABILITY.passed ? t('scenario.detail.testSummary.status.passed')
              : t('scenario.detail.testSummary.status.notPassed') }}
        </span>
        <Popover>
          <template #content>
            <div class="max-w-80">
              {{ TEST_STABILITY?.failureMessage }}
            </div>
          </template>
          <Icon
            v-if="TEST_STABILITY && !TEST_STABILITY.passed"
            icon="icon-tishi1"
            class="text-status-warn text-3" />
        </Popover>
      </div>

      <div class="flex">
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_STABILITY?.sampleSummary?.tps || '--' }}</span>
            <Icon v-if="TEST_STABILITY" :icon="getTpsIconName(TEST_STABILITY)" />
          </div>
          <div>{{ t('scenario.detail.testSummary.metrics.tps') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_STABILITY?.sampleSummary?.tranP90 || '--' }}</span>
            <Icon v-if="TEST_STABILITY" :icon="getTranIconName(TEST_STABILITY)" />
          </div>
          <div>{{ t('scenario.detail.testSummary.metrics.responseTime') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_STABILITY?.sampleSummary?.errorRate || '--' }}%</span>
            <Icon v-if="TEST_STABILITY" :icon="getErrIconName(TEST_STABILITY)" />
          </div>
          <div>{{ t('scenario.detail.testSummary.metrics.errorRate') }}</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">
        {{ t('scenario.detail.testSummary.sections.resultMetrics') }}
      </div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('scenario.detail.testSummary.sections.custom') }}</div>
      <div class="font-semibold text-6 text-center">{{ t('scenario.detail.testSummary.sections.nonStandard') }}</div>

      <div class="flex">
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.tps || '--' }}</div>
          <div>{{ t('scenario.detail.testSummary.metrics.tps') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.tranP90 || '--' }}</div>
          <div>{{ t('scenario.detail.testSummary.metrics.responseTime') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.errorRate || '--' }}%</div>
          <div>{{ t('scenario.detail.testSummary.metrics.errorRate') }}</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">
        {{ t('scenario.detail.testSummary.sections.resultMetrics') }}
      </div>
    </div>
  </div>
</template>
<style scoped>
.PARTIALLY_PASSED {
  @apply text-status-warn;
}

.PASSED {
  @apply text-status-success;
}

.NOT_PASSED {
  @apply text-status-error;
}
</style>
