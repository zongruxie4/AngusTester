<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import elementResizeDetectorMaker from 'element-resize-detector';
import { Icon } from '@xcan-angus/vue-ui';
import { Popover } from 'ant-design-vue';
import { ScriptType, EnumMessage } from '@xcan-angus/infra';
import { TestResultStatus } from '@/enums/enums';

interface Props {
  appInfo:{[key:string]:any};
  userInfo:{[key:string]:any};
  projectId:string;
  dataSource: {
    enabledTestTypes: ScriptType;
    passed: boolean;
    resultSummary: {
      resultStatus: EnumMessage<TestResultStatus>;
      testFailureNum: string;
      testNum: string;
      testSuccessRate: string;
      testSuccessNum: string;
    };
    resultDetailVoMap: {
      [key: string]: {[key: string]: any}
    }
  }
}
const { t } = useI18n();

const resizeDetector = elementResizeDetectorMaker();

let testChart;
const testChartRef = ref();

const echartConfig = {
  title: {
    text: '0%',
    left: '30%',
    bottom: '0%',
    padding: 2,
    subtext: t('service.apiExecDetail.testSummary.successRate'),
    itemGap: 35,
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 12,
      color: '#000'
    }
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '10%',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['45%', '65%'],
      // radius: '60%',
      center: ['30%', '40%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
        borderColor: '#fff',
        borderWidth: 1
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: [
        {
          name: t('status.success'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        },
        {
          name: t('status.failed'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 0.7)'
          }
        }
      ]
    }
  ]
};

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const resultSummary = computed(() => {
  return props.dataSource?.resultSummary;
});

const TEST_PERFORMANCE = computed(() => {
  return props.dataSource?.resultDetailVoMap?.TEST_PERFORMANCE;
});

const TEST_CUSTOMIZATION = computed(() => {
  return props.dataSource?.resultDetailVoMap?.TEST_CUSTOMIZATION;
});

const TEST_FUNCTIONALITY = computed(() => {
  return props.dataSource?.resultDetailVoMap?.TEST_FUNCTIONALITY;
});

const TEST_STABILITY = computed(() => {
  return props.dataSource?.resultDetailVoMap?.TEST_STABILITY;
});

/**
 * Get TPS (Transactions Per Second) indicator icon based on performance comparison
 * @param {Object} testData - Test data containing performance indicators
 * @returns {string|undefined} Icon name for TPS indicator
 */
const getTpsIndicatorIcon = (testData) => {
  const indicatorTps = testData?.indicatorPerf?.tps || testData?.indicatorStability?.tps;
  if (!indicatorTps) {
    return undefined;
  }
  const sampleTps = +testData?.sampleSummary?.tps;
  if (sampleTps > +indicatorTps) {
    return 'icon-shangjiantou1';
  }
  if (sampleTps < +indicatorTps) {
    return 'icon-xiajiantou1';
  }
  return undefined;
};

/**
 * Get transaction response time indicator icon based on performance comparison
 * @param {Object} testData - Test data containing performance indicators
 * @returns {string|undefined} Icon name for response time indicator
 */
const getResponseTimeIndicatorIcon = (testData) => {
  const indicatorArt = testData?.indicatorPerf?.art || testData?.indicatorStability?.art;
  if (!indicatorArt) {
    return undefined;
  }
  const tranP90 = +testData?.sampleSummary?.tranP90;
  if (tranP90 > +indicatorArt) {
    return 'icon-shangjiantou';
  }
  if (tranP90 < +indicatorArt) {
    return 'icon-xiajiantou2';
  }
  return undefined;
};

/**
 * Get error rate indicator icon based on performance comparison
 * @param {Object} testData - Test data containing performance indicators
 * @returns {string|undefined} Icon name for error rate indicator
 */
const getErrorRateIndicatorIcon = (testData) => {
  const indicatorErrorRate = testData?.indicatorPerf?.errorRate || testData?.indicatorStability?.errorRate;
  if (!indicatorErrorRate) {
    return undefined;
  }
  const sampleErrorRate = +testData?.sampleSummary?.errorRate;
  if (sampleErrorRate > +indicatorErrorRate) {
    return 'icon-shangjiantou';
  }
  if (sampleErrorRate < +indicatorErrorRate) {
    return 'icon-xiajiantou2';
  }
  return undefined;
};

/**
 * Configuration for test summary statistics display
 * Defines layout and styling for test result statistics
 */
const testSummaryConfig = [
  [{ label: t('status.total'), dataIndex: 'totalNum', bgColor: 'bg-blue-1' },
    { label: t('status.success'), dataIndex: 'successNum', bgColor: 'bg-status-success' }],
  [{ label: t('status.failed'), dataIndex: 'failNum', bgColor: 'bg-status-error' },
    { label: t('status.disabled'), dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }]
];

/**
 * Resize chart when container size changes
 * Ensures chart displays correctly after container resize
 */
const resizeChart = () => {
  testChart.resize();
};

/**
 * Component mounted lifecycle hook
 * Initializes ECharts with test result data and sets up resize listener
 */
onMounted(() => {
  echartConfig.series[0].data[0].value = +resultSummary.value?.testSuccessNum || 0;
  echartConfig.series[0].data[1].value = +resultSummary.value?.testFailureNum || 0;
  echartConfig.title.text = (resultSummary.value?.testSuccessRate || '--') + '%';
  testChart = echarts.init(testChartRef.value);
  testChart.setOption(echartConfig);
  resizeDetector.listenTo(testChartRef.value, resizeChart);
});

</script>
<template>
  <div class="flex space-x-2">
    <div v-show="resultSummary" class="flex-1 border rounded p-1 space-y-3 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.allTests') }}</div>

      <div class="font-semibold text-6 text-center" :class="resultSummary?.resultStatus?.value">
        {{ resultSummary?.resultStatus?.message }}
      </div>

      <div ref="testChartRef" class="flex-1"></div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-3">
      <div class="font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.functional') }}</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_FUNCTIONALITY ? '' : TEST_FUNCTIONALITY.passed ? 'PASSED': 'NOT_PASSED']">
          {{ !TEST_FUNCTIONALITY ? t('status.notTested') : TEST_FUNCTIONALITY.passed ? t('status.passed') : t('status.notPassed') }}
        </span>
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

      <div class="space-y-2 text-3 px-1">
        <li
          v-for="(line, idx) in testSummaryConfig"
          :key="idx"
          class="flex space-x-2">
          <div
            v-for="item in line"
            :key="item.dataIndex"
            class="flex flex-1 h-7 leading-7">
            <span
              v-if="item.label"
              class="flex-1 text-white px-1 rounded"
              :class="item.bgColor">{{ item.label }}</span>
            <span v-if="item.dataIndex" class="flex-1 bg-gray-light px-1 rounded-r">
              {{ TEST_FUNCTIONALITY?.targetSummary?.[item.dataIndex] || '--' }}
            </span>
          </div>
        </li>
      </div>

      <div class="text-center font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.testInterfaces') }}</div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.performance') }}</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_PERFORMANCE ? '' : TEST_PERFORMANCE.passed ? 'PASSED': 'NOT_PASSED']">
          {{ !TEST_PERFORMANCE ? t('status.notTested') : TEST_PERFORMANCE.passed ? t('status.passed') : t('status.notPassed') }}
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
            <Icon v-if="TEST_PERFORMANCE" :icon="getTpsIndicatorIcon(TEST_PERFORMANCE)" />
          </div>
          <div>{{ t('service.apiExecDetail.indicators.tps') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_PERFORMANCE?.sampleSummary?.tranP90 || '--' }}</span>
            <Icon v-if="TEST_PERFORMANCE" :icon="getResponseTimeIndicatorIcon(TEST_PERFORMANCE)" />
          </div>
          <div>{{ t('protocol.responseTime') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_PERFORMANCE?.sampleSummary?.errorRate || '--' }}%</span>
            <Icon v-if="TEST_PERFORMANCE" :icon="getErrorRateIndicatorIcon(TEST_PERFORMANCE)" />
          </div>
          <div>{{ t('service.apiExecDetail.indicators.errorRate') }}</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">
        {{ t('service.apiExecDetail.testSummary.resultIndicators') }}
      </div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.stability') }}</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_STABILITY ? '' : TEST_STABILITY.passed ? 'PASSED': 'NOT_PASSED']">
          {{ !TEST_STABILITY ? t('status.notTested') : TEST_STABILITY.passed ? t('status.passed') : t('status.notPassed') }}
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
            <Icon v-if="TEST_STABILITY" :icon="getTpsIndicatorIcon(TEST_STABILITY)" />
          </div>
          <div>{{ t('service.apiExecDetail.indicators.tps') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_STABILITY?.sampleSummary?.tranP90 || '--' }}</span>
            <Icon v-if="TEST_STABILITY" :icon="getResponseTimeIndicatorIcon(TEST_STABILITY)" />
          </div>
          <div>{{ t('protocol.responseTime') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_STABILITY?.sampleSummary?.errorRate || '--' }}%</span>
            <Icon v-if="TEST_STABILITY" :icon="getErrorRateIndicatorIcon(TEST_STABILITY)" />
          </div>
          <div>{{ t('service.apiExecDetail.indicators.errorRate') }}</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.resultIndicators') }}</div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.custom') }}</div>
      <div class="font-semibold text-6 text-center">{{ t('service.apiExecDetail.testSummary.nonStandard') }}</div>

      <div class="flex">
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.tps || '--' }}</div>
          <div>{{ t('service.apiExecDetail.indicators.tps') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.tranP90 || '--' }}</div>
          <div>{{ t('protocol.responseTime') }}</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.errorRate || '--' }}%</div>
          <div>{{ t('service.apiExecDetail.indicators.errorRate') }}</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">{{ t('service.apiExecDetail.testSummary.resultIndicators') }}</div>
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
