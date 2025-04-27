<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import * as echarts from 'echarts';
import elementResizeDetectorMaker from 'element-resize-detector';
import { Icon } from '@xcan-angus/vue-ui';
import { Popover } from 'ant-design-vue';

interface Props {

  appInfo:{[key:string]:any};
  userInfo:{[key:string]:any};
  projectId:string;
  dataSource: {
    enabledTestTypes: 'MOCK_APIS'|'MOCK_DATA'|'TEST_CUSTOMIZATION'|'TEST_FUNCTIONALITY'|'TEST_PERFORMANCE'|'TEST_STABILITY';
    passed: boolean;
    resultSummary: {
      resultStatus: 'FULLY_FAILED'|'FULLY_PASSED'|'NOT_ENABLED'|'PARTIALLY_PASSED'|'UNTESTED';
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
const resizeDetector = elementResizeDetectorMaker();

let testChart;
const testChartRef = ref();

const echartConfig = {
  title: {
    text: '0%',
    left: '30%',
    bottom: '0%',
    padding: 2,
    subtext: '成功率',
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
          name: '成功数',
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        },
        {
          name: '失败数',
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

const getTpsIconName = (testData) => {
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

const getTranIconName = (testData) => {
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

const getErrIconName = (testData) => {
  const indicatorErrorRate = testData?.indicatorPerf?.errorRate || testData?.indicatorStability?.errorRate;
  if (!indicatorErrorRate) {
    return undefined;
  }
  const sampleTps = +testData?.sampleSummary?.errorRate;
  if (sampleTps > +indicatorErrorRate) {
    return 'icon-shangjiantou';
  }
  if (sampleTps < +indicatorErrorRate) {
    return 'icon-xiajiantou2';
  }
  return undefined;
};

const configInfo = [
  [{ label: '总共', dataIndex: 'totalNum', bgColor: 'bg-blue-1' }, { label: '成功', dataIndex: 'successNum', bgColor: 'bg-status-success' }],
  [{ label: '失败', dataIndex: 'failNum', bgColor: 'bg-status-error' }, { label: '未启用', dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }]
];

const resize = () => {
  testChart.resize();
};

onMounted(() => {
  echartConfig.series[0].data[0].value = +resultSummary.value?.testSuccessNum || 0;
  echartConfig.series[0].data[1].value = +resultSummary.value?.testFailureNum || 0;
  echartConfig.title.text = (resultSummary.value?.testSuccessRate || '--') + '%';
  testChart = echarts.init(testChartRef.value);
  testChart.setOption(echartConfig);
  resizeDetector.listenTo(testChartRef.value, resize);
});

</script>
<template>
  <div class="flex space-x-2">
    <div v-show="resultSummary" class="flex-1 border rounded p-1 space-y-3 flex flex-col justify-between">
      <div class="font-semibold text-text-title">全部测试</div>

      <div class="font-semibold text-6 text-center" :class="resultSummary?.resultStatus?.value">
        {{ resultSummary?.resultStatus?.message }}
      </div>

      <div ref="testChartRef" class="flex-1"></div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-3">
      <div class="font-semibold text-text-title">功能</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_FUNCTIONALITY ? '' : TEST_FUNCTIONALITY.passed ? 'PASSED': 'NOT_PASSED']">{{ !TEST_FUNCTIONALITY ? '未测试' : TEST_FUNCTIONALITY.passed ? '通过' : '不通过' }}</span>
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
            <span v-if="item.dataIndex" class="flex-1 bg-gray-light px-2 rounded-r">{{ TEST_FUNCTIONALITY?.targetSummary?.[item.dataIndex] || '--' }}</span>
          </div>
        </li>
      </div>

      <div class="text-center font-semibold text-text-title">测试接口</div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">性能</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_PERFORMANCE ? '' : TEST_PERFORMANCE.passed ? 'PASSED': 'NOT_PASSED']">{{ !TEST_PERFORMANCE ? '未测试' : TEST_PERFORMANCE.passed ? '通过' : '不通过' }}</span>
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
          <div>每秒事务数(TPS)</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_PERFORMANCE?.sampleSummary?.tranP90 || '--' }}</span>
            <Icon v-if="TEST_PERFORMANCE" :icon="getTranIconName(TEST_PERFORMANCE)" />
          </div>
          <div>响应时间(P90)</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_PERFORMANCE?.sampleSummary?.errorRate || '--' }}%</span>
            <Icon v-if="TEST_PERFORMANCE" :icon="getErrIconName(TEST_PERFORMANCE)" />
          </div>
          <div>错误率</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">结果指标</div>
    </div>

    <div v-show="TEST_STABILITY" class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">稳定性</div>
      <div class="font-semibold text-6 text-center">
        <span :class="[!TEST_STABILITY ? '' : TEST_STABILITY.passed ? 'PASSED': 'NOT_PASSED']">{{ !TEST_STABILITY ? '未测试' : TEST_STABILITY.passed ? '通过' : '不通过' }}</span>
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
          <div>每秒事务数(TPS)</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_STABILITY?.sampleSummary?.tranP90 || '--' }}</span>
            <Icon v-if="TEST_STABILITY" :icon="getTranIconName(TEST_STABILITY)" />
          </div>
          <div>响应时间(P90)</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="flex items-center justify-center space-x-0.5">
            <span class="text-4">{{ TEST_STABILITY?.sampleSummary?.errorRate || '--' }}%</span>
            <Icon v-if="TEST_STABILITY" :icon="getErrIconName(TEST_STABILITY)" />
          </div>
          <div>错误率</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">结果指标</div>
    </div>

    <div class="flex-1 border rounded p-1 space-y-2 flex flex-col justify-between">
      <div class="font-semibold text-text-title">自定义</div>
      <div class="font-semibold text-6 text-center">非标准</div>

      <div class="flex">
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.tps || '--' }}</div>
          <div>每秒事务数(TPS)</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.tranP90 || '--' }}</div>
          <div>响应时间(P90)</div>
        </div>
        <div class="text-center flex-1 min-w-0">
          <div class="text-4">{{ TEST_CUSTOMIZATION?.sampleSummary?.errorRate || '--' }}%</div>
          <div>错误率</div>
        </div>
      </div>

      <div class="text-center font-semibold text-text-title">结果指标</div>
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
