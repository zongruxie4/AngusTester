<script setup lang="ts">
import { computed } from 'vue';
// import { NoData } from '@xcan-angus/vue-ui';
import { ApiUtils as apiUtils } from '@xcan-angus/vue-ui';

import { ReportContent } from '../PropsType';

import deletePng from './Image/delete.png';
import shanghongPng from './Image/shanghong.png';
import shanglvPng from './Image/shanglv.png';
import xialvPng from './Image/xialv.png';
import xiahongPng from './Image/xiahong.png';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const TEST_FUNCTIONALITY = computed(() => {
  return props.dataSource?.content?.testResult?.resultDetailVoMap?.TEST_FUNCTIONALITY;
});

const TEST_PERFORMANCE = computed(() => {
  return props.dataSource?.content?.testResult?.resultDetailVoMap?.TEST_PERFORMANCE;
});

const TEST_STABILITY = computed(() => {
  return props.dataSource?.content?.testResult?.resultDetailVoMap?.TEST_STABILITY;
});

const TEST_STABILITY_NODE = computed(() => {
  const useNodeId = TEST_STABILITY.value?.usageFailedNodeId || Object.keys(TEST_STABILITY.value?.nodeUsageSummary || {})[0];
  return TEST_STABILITY.value?.nodeUsageSummary?.[useNodeId] || {};
});

const basicColumns = computed(() => [
  [
    {
      dataIndex: 'passed',
      name: '测试结果',
      customRender: (text) => {
        return text === true ? '通过' : text === false ? '未通过' : '未测试';
      }
    },
    {
      dataIndex: 'failureMessage',
      name: '测试失败原因'
    }
  ],
  [
    {
      dataIndex: 'testNum',
      name: '测试次数'
    },
    {
      dataIndex: 'testFailureNum',
      name: '失败次数'
    }
  ],
  [
    {
      dataIndex: 'scriptId',
      name: '测试脚本ID'
    },
    {
      dataIndex: 'scriptName',
      name: '测试脚本名称'
    }
  ],
  [
    {
      dataIndex: 'execId',
      name: '执行ID'
    },
    {
      dataIndex: 'execName',
      name: '执行名称'
    }
  ],
  [
    {
      dataIndex: 'execByName',
      name: '最后执行人'
    },
    {
      dataIndex: 'lastExecDate',
      name: '最后执行时间'
    }
  ]
]);

const caseSummaryColumns = computed(() => [
  [
    {
      name: '用例总数',
      dataIndex: 'totalNum'
    },
    {
      name: '通过数',
      dataIndex: 'successNum'
    }
  ],
  [
    {
      name: '未通过数',
      dataIndex: 'failNum'
    },
    {
      name: '未启用数',
      dataIndex: 'disabledNum'
    }
  ]
]);

const caseColumns = computed(() => [
  [
    {
      name: '类型',
      dataIndex: 'caseType',
      customRender: (text) => {
        return text?.message;
      }
    },
    {
      name: '是否启用',
      dataIndex: 'enabled',
      customRender: (text) => {
        return text ? '开启' : '未开启';
      }
    }
  ],
  [
    {
      name: '是否测试通过',
      dataIndex: 'passed',
      customRender: (text) => {
        return text === true ? '通过' : text === false ? '未通过' : '未测试';
      }
    },
    {
      name: '测试失败原因',
      dataIndex: 'failureMessage'
    }
  ],
  [
    {
      name: '测试次数',
      dataIndex: 'testNum'
    },
    {
      name: '测试失败次数',
      dataIndex: 'testFailureNum'
    }
  ]
]);

const indicatorPerfItem = [
  {
    label: '并发数',
    dataIndex: 'threads'
  },
  {
    label: '测试时长',
    dataIndex: 'duration'
  },
  {
    label: '增压并发数',
    dataIndex: 'rampUpThreads'
  },
  {
    label: '增压测试时长',
    dataIndex: 'rampUpInterval'
  },
  {
    label: '响应时间',
    dataIndex: 'art',
    compareOprate: '<='
  },
  {
    label: '每秒事务数',
    dataIndex: 'tps',
    compareOprate: '>='
  },
  {
    label: '错误率',
    dataIndex: 'errorRate',
    compareOprate: '<=',
    unit: '%'
  }
];

const indicatorPerfResultItem = computed(() => {
  const percentile = TEST_PERFORMANCE.value?.indicatorPerf?.percentile?.value;
  let percentitleKey = 'tranMax';
  switch (percentile) {
    case 'ALL':
      percentitleKey = 'tranMax';
      break;
    case 'P50':
      percentitleKey = 'tranP50';
      break;
    case 'P75':
      percentitleKey = 'tranP75';
      break;
    case 'P90':
      percentitleKey = 'tranP90';
      break;
    case 'P95':
      percentitleKey = 'tranP95';
      break;
    case 'P99':
      percentitleKey = 'tranP99';
      break;
    case 'P999':
      percentitleKey = 'tranP999';
      break;
    default:
      percentitleKey = 'tranMax';
  }
  return [
    {
      label: '并发数',
      dataIndex: 'threadPoolSize'
    },
    {
      label: '测试时长',
      dataIndex: 'duration'
    },
    {
      label: '增压并发时数',
      dataIndex: 'rampUpThreads'
    },
    {
      label: '增压并发时长',
      dataIndex: 'rampUpInterval'
    },
    {
      label: `响应时间(${percentile || '--'})`,
      dataIndex: percentitleKey || '--'
    },
    {
      label: '每秒事务数(TPS)',
      dataIndex: 'tps'
    },
    {
      label: '错误率',
      dataIndex: 'errorRate',
      unit: '%'
    }
  ];
});

const getDuration = (mseconds) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [value, unit] = apiUtils.splitDuration(TEST_PERFORMANCE.value?.indicatorPerf?.duration);
  if (!mseconds) {
    return '--';
  }
  return apiUtils.formatMillisecondToShortDuraiton(+mseconds, unit);
};

const getPerfIcon = (valueKey) => {
  const indicatorPerf = TEST_PERFORMANCE.value?.indicatorPerf || {};
  const result = TEST_PERFORMANCE.value?.sampleSummary || {};
  if (valueKey.includes('tran')) {
    if (!indicatorPerf.art) {
      return '';
    }
    if (!result[valueKey] && indicatorPerf.art) {
      // return {
      //   class: 'text-status-error',
      //   icon: 'icon-cuowu'
      // };
      return deletePng;
    }
    if (+result[valueKey] > +indicatorPerf.art) {
      // return {
      //   class: 'text-status-error',
      //   icon: 'icon-shangjiantou'
      // };
      return shanghongPng;
    }
    // return {
    //   class: 'text-status-success',
    //   icon: 'icon-xiajiantou2'
    // };
    return xialvPng;
  }
  if (valueKey === 'tps') {
    if (!indicatorPerf.tps) {
      return '';
    }
    if (!result[valueKey] && indicatorPerf.tps) {
      // return {
      //   class: 'text-status-error',
      //   icon: 'icon-cuowu'
      // };
      return deletePng;
    }
    if (+result[valueKey] < +indicatorPerf.tps) {
      // return {
      //   class: 'text-status-error',
      //   icon: 'icon-xiajiantou1'
      // };
      return xiahongPng;
    }
    // return {
    //   class: 'text-status-success',
    //   icon: 'icon-shangjiantou1'
    // };
    return shanglvPng;
  }

  if (valueKey === 'errorRate') {
    if (!indicatorPerf.errorRate) {
      return '';
    }
    if (!result[valueKey] && indicatorPerf.errorRate) {
      // return {
      //   class: 'text-status-error',
      //   icon: 'icon-cuowu'
      // };
      return deletePng;
    }
    if (+result[valueKey] > +indicatorPerf.errorRate) {
      // return {
      //   class: 'text-status-error',
      //   icon: 'icon-shangjiantou'
      // };
      return shanghongPng;
    }
    // return {
    //   class: 'text-status-success',
    //   icon: 'icon-xiajiantou2'
    // };
    return xialvPng;
  }
  return false;
};

const indicatorStabilityItem = computed(() => {
  const percentile = TEST_STABILITY.value?.indicatorStability?.percentile?.value;
  let percentitleKey = 'tranMax';
  switch (percentile) {
    case 'ALL':
      percentitleKey = 'tranMax';
      break;
    case 'P50':
      percentitleKey = 'tranP50';
      break;
    case 'P75':
      percentitleKey = 'tranP75';
      break;
    case 'P90':
      percentitleKey = 'tranP90';
      break;
    case 'P95':
      percentitleKey = 'tranP95';
      break;
    case 'P99':
      percentitleKey = 'tranP99';
      break;
    case 'P999':
      percentitleKey = 'tranP999';
      break;
    default:
      percentitleKey = '';
  }
  return [
    {
      label: '并发数',
      dataIndex: 'threads',
      resultLabel: '并发数',
      resultDataIndex: 'threadPoolSize'
    },
    {
      label: '测试时长',
      dataIndex: 'duration',
      resultLabel: '测试时长',
      resultDataIndex: 'duration'
    },
    {
      label: '响应时间',
      dataIndex: 'art',
      resultLabel: `响应时间(${percentile || '--'})`,
      resultDataIndex: percentitleKey || '--'
    },
    {
      label: '每秒事务数',
      dataIndex: 'tps',
      compareOprate: '>=',
      resultLabel: '每秒事务数(TPS)',
      resultDataIndex: 'tps'
    },
    {
      label: '错误率',
      dataIndex: 'errorRate',
      resultLabel: '错误率',
      resultDataIndex: 'errorRate'
    }
  ];
});

const getStabilityDuration = (mseconds) => {
  if (!mseconds) {
    return '--';
  }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [value, unit] = apiUtils.splitDuration(TEST_STABILITY.value?.indicatorStability?.duration);

  return apiUtils.formatMillisecondToShortDuraiton(+mseconds, unit);
};

const getStabilityIcon = (valueKey) => {
  const indicatorStability = TEST_STABILITY.value?.indicatorStability;
  const result = TEST_STABILITY.value?.sampleSummary;
  if (valueKey.includes('tran')) {
    if (!indicatorStability?.art) {
      return '';
    }
    if (!result[valueKey] && indicatorStability.art) {
      // return {
      //   icon: 'icon-cuowu'
      // };
      return deletePng;
    }
    if (+result[valueKey] > +indicatorStability.art) {
      // return {
      //   icon: 'icon-shangjiantou'
      // };
      return shanghongPng;
    }
    // return {
    //   icon: 'icon-xiajiantou2'
    // };
    return xialvPng;
  }
  if (valueKey === 'tps') {
    if (!indicatorStability?.tps) {
      return '';
    }
    if (!result?.[valueKey]) {
      // return {
      //   icon: 'icon-cuowu'
      // };
      return deletePng;
    }
    if (+result[valueKey] < +indicatorStability.tps) {
      // return {
      //   icon: 'icon-xiajiantou1'
      // };
      return xiahongPng;
    }
    // return {
    //   icon: 'icon-shangjiantou1'
    // };
    return shanglvPng;
  }

  if (valueKey === 'errorRate') {
    if (!indicatorStability?.errorRate) {
      return '';
    }
    if (!result?.[valueKey]) {
      // return {
      //   icon: 'icon-cuowu'
      // };
      return deletePng;
    }
    if (+result[valueKey] > +indicatorStability.errorRate) {
      // return {
      //   icon: 'icon-shangjiantou'
      // };
      return shanghongPng;
    }
    // return {
    //   icon: 'icon-xiajiantou2'
    // };
    return xialvPng;
  }
  if (['meanCpu', 'meanMemory', 'meanFilesystem', 'meanNetwork'].includes(valueKey)) {
    let key = valueKey.replace('mean', '').toLowerCase();
    if (valueKey === 'meanFilesystem') {
      key = 'disk';
    }
    if (!indicatorStability?.valueKey) {
      return '';
    }
    if (!TEST_STABILITY_NODE.value[valueKey]) {
      // return {
      //   icon: 'icon-cuowu'
      // };
      return deletePng;
    }
    if (+TEST_STABILITY_NODE.value[valueKey] > +indicatorStability[key]) {
      // return {
      //   icon: 'icon-shangjiantou'
      // };
      return shanghongPng;
    }
    if (!indicatorStability[key]) {
      return false;
    }
    // return {
    //   icon: 'icon-xiajiantou2'
    // };
    return xialvPng;
  }
  return false;
};

const sysItems = [
  {
    label: 'cpu使用率',
    dataIndex: 'cpu',
    meanDataIndex: 'meanCpu',
    maxDataIndex: 'maxCpu',
    unit: '%'
  },
  {
    label: '内存使用率',
    dataIndex: 'memory',
    meanDataIndex: 'meanMemory',
    maxDataIndex: 'maxMemory',
    unit: '%'
  },
  {
    label: '磁盘使用率',
    dataIndex: 'disk',
    meanDataIndex: 'meanFilesystem',
    maxDataIndex: 'maxFilesystem',
    unit: '%'
  },
  {
    label: '网络使用量',
    dataIndex: 'network',
    meanDataIndex: 'meanNetwork',
    maxDataIndex: 'maxNetwork',
    unit: 'MB'
  }
];

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a3" class="text-4 text-theme-title font-medium">三、<em class="inline-block w-0.25"></em>场景测试详细结果信息</span>
    </h1>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a3.1" class="text-3.5 text-theme-title font-medium">3.1、<em class="inline-block w-0.25"></em>功能测试结果</span>
    </h1>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>测试基本信息</span>
    </li>

    <div v-if="TEST_FUNCTIONALITY" class="mb-5">
      <div class="border-t border-l border-solid border-border-input">
        <div v-for="column in basicColumns" class="flex border-b border-solid border-border-input">
          <template v-for="item in column">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
              {{ item.customRender ? item.customRender(TEST_FUNCTIONALITY[item.dataIndex]) : TEST_FUNCTIONALITY[item.dataIndex] }}
            </div>
          </template>
        </div>
      </div>
    </div>
    <div v-else class="mb-5">
      无
    </div>
    <!-- <NoData v-else size="small" class="my-5"/> -->

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>测试接口统计</span>
    </li>

    <div v-if="TEST_FUNCTIONALITY?.caseSummary" class="mb-5">
      <div class="border-t border-l border-solid border-border-input">
        <div v-for="column in caseSummaryColumns" class="flex border-b border-solid border-border-input">
          <template v-for="item in column">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
              {{ item.customRender ? item.customRender(TEST_FUNCTIONALITY.caseSummary[item.dataIndex]) : TEST_FUNCTIONALITY.caseSummary[item.dataIndex] }}
            </div>
          </template>
        </div>
      </div>
    </div>
    <div v-else class="mb-5">
      无
    </div>
    <!-- <NoData v-else size="small" class="my-5"/> -->

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>测试用例结果明细</span>
    </li>

    <div class="mb-5">
      <template v-for="(apiCase, idx) in TEST_FUNCTIONALITY?.caseResults || []">
        <div class="border-border-input border-l border-r border-b p-1.5 bg-gray-9" :class="{'border-t': idx === 0}">{{ apiCase.caseName }}</div>
        <div v-for="column in caseColumns" class="flex border-b border-l border-solid border-border-input">
          <template v-for="item in column">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
              {{ item.customRender ? item.customRender(apiCase[item.dataIndex]) : apiCase[item.dataIndex] }}
            </div>
          </template>
        </div>
      </template>
    </div>
    <div v-if="!TEST_FUNCTIONALITY?.caseResults?.length" class="mb-5">
      无
    </div>
    <!-- <NoData v-if="!TEST_FUNCTIONALITY.caseResults?.length" size="small" class="my-5"/> -->

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a3.2" class="text-3.5 text-theme-title font-medium">3.2、<em class="inline-block w-0.25"></em>性能测试</span>
    </h1>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>测试基本信息</span>
    </li>

    <div v-if="TEST_PERFORMANCE" class="mb-5">
      <div class="border-t border-l border-solid border-border-input">
        <div v-for="column in basicColumns" class="flex border-b border-solid border-border-input">
          <template v-for="item in column">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
              {{ item.customRender ? item.customRender(TEST_PERFORMANCE[item.dataIndex]) : TEST_PERFORMANCE[item.dataIndex] }}
            </div>
          </template>
        </div>
      </div>
    </div>
    <div v-else class="mb-5">
      无
    </div>
    <!-- <NoData v-else size="small" class="my-5"/> -->

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>测试指标和结果</span>
    </li>

    <div class="flex border border-solid border-border-input bg-blue-table">
      <div class="flex-1 p-1.5 border-r border-border-input">性能指标</div>
      <div class="flex-1 p-1.5">结果</div>
    </div>
    <div
      v-for="(perf, index) in indicatorPerfItem"
      :key="perf.dataIndex"
      class="flex border-b border-l border-solid border-border-input">
      <div
        class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
        {{ perf.label }}
      </div>
      <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
        <template v-if="perf.dataIndex === 'art'">
          {{ TEST_PERFORMANCE?.indicatorPerf?.percentile?.value ? `${TEST_PERFORMANCE.indicatorPerf.percentile.value} <= ${TEST_PERFORMANCE.indicatorPerf.art}` : '--' }}
        </template>
        <template v-else>
          {{ TEST_PERFORMANCE?.indicatorPerf?.[perf.dataIndex] ? `${perf.compareOprate || ''} ${TEST_PERFORMANCE.indicatorPerf[perf.dataIndex]}${perf.unit || ''}` : '--' }}
        </template>
      </div>
      <div
        class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
        {{ indicatorPerfResultItem[index].label }}
      </div>
      <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
        {{ TEST_PERFORMANCE?.sampleSummary?.[indicatorPerfResultItem[index].dataIndex] ? `${indicatorPerfResultItem[index].dataIndex ==='duration' ? getDuration(TEST_PERFORMANCE?.sampleSummary?.[indicatorPerfResultItem[index].dataIndex]) : TEST_PERFORMANCE?.sampleSummary?.[indicatorPerfResultItem[index].dataIndex]}${indicatorPerfResultItem[index].unit || ''}` : '--' }}
        <img
          v-if="getPerfIcon(indicatorPerfResultItem[index].dataIndex)"
          :src="getPerfIcon(indicatorPerfResultItem[index].dataIndex)"
          class="w-3" />
      </div>
    </div>

    <h1 class="text-theme-title font-medium mb-3.5 mt-5">
      <span id="a3.3" class="text-3.5 text-theme-title font-medium">3.2、<em class="inline-block w-0.25"></em>稳定性测试</span>
    </h1>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>测试基本信息</span>
    </li>

    <div v-if="TEST_STABILITY" class="mb-5">
      <div class="border-t border-l border-solid border-border-input">
        <div v-for="column in basicColumns" class="flex border-b border-solid border-border-input">
          <template v-for="item in column">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
              {{ item.customRender ? item.customRender(TEST_STABILITY[item.dataIndex]) : TEST_STABILITY[item.dataIndex] }}
            </div>
          </template>
        </div>
      </div>
    </div>
    <div v-else class="mb-5">
      无
    </div>
    <!-- <NoData v-else size="small" class="my-5"/> -->
    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>测试指标和结果</span>
    </li>

    <div>
      <div class="flex border border-solid border-border-input bg-blue-table">
        <div class="flex-1 p-1.5 border-r border-border-input">稳定性指标</div>
        <div class="flex-1 p-1.5">结果</div>
      </div>
      <div v-for="stability in indicatorStabilityItem" class="flex border-l border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-b border-solid border-border-input">
          {{ stability.label }}
        </div>
        <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-b border-solid border-border-input">
          <template v-if="stability.dataIndex === 'art'">
            {{ TEST_STABILITY?.indicatorStability?.percentile?.value ? `${TEST_STABILITY.indicatorStability.percentile.value} <= ${TEST_STABILITY.indicatorStability.art || ''}` : '--' }}
          </template>
          <template v-else-if="stability.dataIndex === 'errorRate'">
            &lt;= {{ TEST_STABILITY?.indicatorStability?.[stability.dataIndex] }} %
          </template>
          <template v-else>
            {{ (stability.compareOprate || '') + (TEST_STABILITY?.indicatorStability?.[stability.dataIndex] || '') || '--' }}
          </template>
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-b border-solid border-border-input">
          {{ stability.resultLabel }}
        </div>
        <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-b border-solid border-border-input">
          <template v-if="stability.resultDataIndex === 'duration'">
            {{ getStabilityDuration(TEST_STABILITY?.sampleSummary?.[stability.resultDataIndex]) }}
          </template>
          <template v-else>
            {{ TEST_STABILITY?.sampleSummary?.[stability.resultDataIndex] || '--' }}
          </template>
          <img
            v-if="getStabilityIcon(stability.resultDataIndex)"
            :src="getStabilityIcon(stability.resultDataIndex)"
            class="w-3" />
          <!-- {{ stability.resultDataIndex }} -->
          <!-- item.dataIndex === 'duration' ? getDuration(props.samplingSummary[item.dataIndex]) : props.samplingSummary[item.dataIndex] || '--' -->
        </div>
      </div>
      <div class="flex border-l border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-b border-solid border-border-input">
          应用系统平均负载
        </div>
        <div class="flex-1">
          <div
            v-for="sys in sysItems"
            :key="sys.dataIndex"
            class="px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-b border-solid border-border-input">
            {{ sys.label }} &lt;= {{ TEST_STABILITY?.indicatorStability?.[sys.dataIndex] ? `${TEST_STABILITY?.indicatorStability?.[sys.dataIndex]}${sys.unit}` : '--' }}
          </div>
        </div>
        <div
          class="w-27 flex-shrink-0">
          <div
            v-for="sys in sysItems"
            :key="sys.dataIndex"
            class="bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-b border-solid border-border-input">
            {{ sys.label }}
          </div>
        </div>
        <div class="flex-1">
          <div
            v-for="sys in sysItems"
            :key="sys.dataIndex"
            class="px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-b border-solid border-border-input flex">
            <div class="flex-1">
              平均 {{ TEST_STABILITY_NODE?.[sys.meanDataIndex] ? `${TEST_STABILITY_NODE?.[sys.meanDataIndex]}${sys.unit}` : '--' }}
              <img
                v-if="getStabilityIcon(sys.meanDataIndex)"
                :src="getStabilityIcon(sys.meanDataIndex)"
                class="w-3" />
            </div>
            <div class="flex-1">
              最大 {{ TEST_STABILITY_NODE?.[sys.maxDataIndex] ? `${TEST_STABILITY_NODE?.[sys.maxDataIndex]}${sys.unit}` : '--' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
