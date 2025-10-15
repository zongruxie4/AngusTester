<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';
import apiUtils from '@/utils/apis/index';

import deletePng from './Image/delete.png';
import shanghongPng from './Image/shanghong.png';
import shanglvPng from './Image/shanglv.png';
import xialvPng from './Image/xialv.png';
import xiahongPng from './Image/xiahong.png';

const { t } = useI18n();

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
      name: t('common.testResult'),
      customRender: (text) => {
        return text === true ? t('status.status.passed') : text === false ? t('status.failed') : t('status.notTested');
      }
    },
    {
      dataIndex: 'failureMessage',
      name: t('common.failureReason')
    }
  ],
  [
    {
      dataIndex: 'testNum',
      name: t('reportPreview.apis.testResultDetail.fields.testCount')
    },
    {
      dataIndex: 'testFailureNum',
      name: t('reportPreview.apis.testResultDetail.fields.failureCount')
    }
  ],
  [
    {
      dataIndex: 'scriptId',
      name: t('reportPreview.apis.testResultDetail.fields.testScriptId')
    },
    {
      dataIndex: 'scriptName',
      name: t('reportPreview.apis.testResultDetail.fields.testScriptName')
    }
  ],
  [
    {
      dataIndex: 'execId',
      name: t('reportPreview.apis.testResultDetail.fields.executionId')
    },
    {
      dataIndex: 'execName',
      name: t('reportPreview.apis.testResultDetail.fields.executionName')
    }
  ],
  [
    {
      dataIndex: 'execByName',
      name: t('reportPreview.apis.testResultDetail.fields.lastExecutor')
    },
    {
      dataIndex: 'lastExecDate',
      name: t('reportPreview.apis.testResultDetail.fields.lastExecutionTime')
    }
  ]
]);

const caseSummaryColumns = computed(() => [
  [
    {
      name: t('reportPreview.apis.testResultDetail.fields.totalCases'),
      dataIndex: 'totalNum'
    },
    {
      name: t('reportPreview.apis.testResultDetail.fields.passedCount'),
      dataIndex: 'successNum'
    }
  ],
  [
    {
      name: t('reportPreview.apis.testResultDetail.fields.failedCount'),
      dataIndex: 'failNum'
    },
    {
      name: t('reportPreview.apis.testResultDetail.fields.disabledCount'),
      dataIndex: 'disabledNum'
    }
  ]
]);

const caseColumns = computed(() => [
  [
    {
      name: t('common.type'),
      dataIndex: 'caseType',
      customRender: (text) => {
        return text?.message;
      }
    },
    {
      name: t('reportPreview.apis.testResultDetail.fields.isEnabled'),
      dataIndex: 'enabled',
      customRender: (text) => {
        return text ? t('status.enabled') : t('status.disabled');
      }
    }
  ],
  [
    {
      name: t('reportPreview.apis.testResultDetail.fields.isTestPassed'),
      dataIndex: 'passed',
      customRender: (text) => {
        return text === true ? t('status.passed') : text === false ? t('status.failed') : t('status.notTested');
      }
    },
    {
      name: t('common.failureReason'),
      dataIndex: 'failureMessage'
    }
  ],
  [
    {
      name: t('reportPreview.apis.testResultDetail.fields.testCount'),
      dataIndex: 'testNum'
    },
    {
      name: t('reportPreview.apis.testResultDetail.fields.testFailureCount'),
      dataIndex: 'testFailureNum'
    }
  ]
]);

const indicatorPerfItem = [
  {
    label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.concurrency'),
    dataIndex: 'threads'
  },
  {
    label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.testDuration'),
    dataIndex: 'duration'
  },
  {
    label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.pressureConcurrency'),
    dataIndex: 'rampUpThreads'
  },
  {
    label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.pressureTestDuration'),
    dataIndex: 'rampUpInterval'
  },
  {
    label: t('protocol.responseTime'),
    dataIndex: 'art',
    compareOprate: '<='
  },
  {
    label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.tps'),
    dataIndex: 'tps',
    compareOprate: '>='
  },
  {
    label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.errorRate'),
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
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.concurrency'),
      dataIndex: 'threadPoolSize'
    },
    {
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.testDuration'),
      dataIndex: 'duration'
    },
    {
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.pressureConcurrency'),
      dataIndex: 'rampUpThreads'
    },
    {
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.pressureTestDuration'),
      dataIndex: 'rampUpInterval'
    },
    {
      label: `${t('protocol.responseTime')} (${percentile || '--'})`,
      dataIndex: percentitleKey || '--'
    },
    {
      label: `${t('reportPreview.apis.testResultDetail.performanceTest.indicators.tps')} (${percentile || '--'})`,
      dataIndex: 'tps'
    },
    {
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.errorRate'),
      dataIndex: 'errorRate',
      unit: '%'
    }
  ];
});

const getDuration = (mseconds) => {
  if (!mseconds) {
    return '--';
  }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [value, unit] = apiUtils.splitDuration(TEST_PERFORMANCE.value.indicatorPerf.duration);

  return apiUtils.formatMillisecondToShortDuration(+mseconds, unit);
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
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.concurrency'),
      dataIndex: 'threads',
      resultLabel: t('reportPreview.apis.testResultDetail.performanceTest.indicators.concurrency'),
      resultDataIndex: 'threadPoolSize'
    },
    {
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.testDuration'),
      dataIndex: 'duration',
      resultLabel: t('reportPreview.apis.testResultDetail.performanceTest.indicators.testDuration'),
      resultDataIndex: 'duration'
    },
    {
      label: t('protocol.responseTime'),
      dataIndex: 'art',
      resultLabel: `${t('protocol.responseTime')} (${percentile || '--'})`,
      resultDataIndex: percentitleKey || '--'
    },
    {
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.tps'),
      dataIndex: 'tps',
      compareOprate: '>=',
      resultLabel: `${t('reportPreview.apis.testResultDetail.performanceTest.indicators.tps')} (${percentile || '--'})`,
      resultDataIndex: 'tps'
    },
    {
      label: t('reportPreview.apis.testResultDetail.performanceTest.indicators.errorRate'),
      dataIndex: 'errorRate',
      resultLabel: t('reportPreview.apis.testResultDetail.performanceTest.indicators.errorRate'),
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

  return apiUtils.formatMillisecondToShortDuration(+mseconds, unit);
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
    if (!result[valueKey] && indicatorStability.tps) {
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
    if (!result[valueKey] && indicatorStability.errorRate) {
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
    if (!indicatorStability?.[key]) {
      return '';
    }
    if (!TEST_STABILITY_NODE.value[valueKey] && indicatorStability[key]) {
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
    label: t('reportPreview.apis.testResultDetail.fields.cpuUsage'),
    dataIndex: 'cpu',
    meanDataIndex: 'meanCpu',
    maxDataIndex: 'maxCpu',
    unit: '%'
  },
  {
    label: t('reportPreview.apis.testResultDetail.fields.memoryUsage'),
    dataIndex: 'memory',
    meanDataIndex: 'meanMemory',
    maxDataIndex: 'maxMemory',
    unit: '%'
  },
  {
    label: t('reportPreview.apis.testResultDetail.fields.diskUsage'),
    dataIndex: 'disk',
    meanDataIndex: 'meanFilesystem',
    maxDataIndex: 'maxFilesystem',
    unit: '%'
  },
  {
    label: t('reportPreview.apis.testResultDetail.fields.networkUsage'),
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
      <span id="a3" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.testResultDetail.title') }}</span>
    </h1>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a3.1" class="text-3.5 text-theme-title font-medium">3.1、<em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.testResultDetail.functionalTest.title') }}</span>
    </h1>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>{{ t('common.basicInfo') }}</span>
    </li>

    <div v-if="TEST_FUNCTIONALITY" class="mb-5">
      <div class="border-t border-l border-solid border-border-input">
        <div
          v-for="(column,index) in basicColumns"
          :key="index"
          class="flex border-b border-solid border-border-input">
          <template v-for="item in column" :key="item.dataIndex">
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
      {{ t('common.noData') }}
    </div>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium">
        <em class="inline-block w-0.25"></em>{{ t('common.testCases') }}
      </span>
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
      {{ t('common.noData') }}
    </div>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.testResultDetail.funcTestResult.caseResultDetail.title') }}</span>
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
      {{ t('common.noData') }}
    </div>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a3.2" class="text-3.5 text-theme-title font-medium">3.2、<em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.testResultDetail.performanceTest.title') }}</span>
    </h1>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>{{ t('common.basicInfo') }}</span>
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
      {{ t('common.noData') }}
    </div>
    <!-- <NoData v-else size="small" class="my-5"/> -->

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.testResultDetail.performanceTest.indicatorsAndResults') }}</span>
    </li>

    <div class="flex border border-solid border-border-input bg-blue-table">
      <div class="flex-1 p-1.5 border-r border-border-input">{{ t('reportPreview.apis.testResultDetail.performanceTest.performanceIndicators') }}</div>
      <div class="flex-1 p-1.5">{{ t('reportPreview.apis.testResultDetail.performanceTest.results') }}</div>
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
      <span id="a3.3" class="text-3.5 text-theme-title font-medium">3.2、<em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.testResultDetail.stabilityTest.title') }}</span>
    </h1>

    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>{{ t('common.basicInfo') }}</span>
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
      {{ t('common.noData') }}
    </div>
    <!-- <NoData v-else size="small" class="my-5"/> -->
    <li class="mb-2">
      <span class="text-3 text-theme-title font-medium"><em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.testResultDetail.stabilityTest.indicatorsAndResults') }}</span>
    </li>

    <div>
      <div class="flex border border-solid border-border-input bg-blue-table">
        <div class="flex-1 p-1.5 border-r border-border-input">{{ t('reportPreview.apis.testResultDetail.stabilityTest.stabilityIndicators') }}</div>
        <div class="flex-1 p-1.5">{{ t('reportPreview.apis.testResultDetail.stabilityTest.results') }}</div>
      </div>
      <div v-for="stability in indicatorStabilityItem" class="flex border-l border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-b border-solid border-border-input">
          {{ stability.label }}
        </div>
        <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-b border-solid border-border-input">
          <template v-if="stability.dataIndex === 'art'">
            {{ TEST_STABILITY?.indicatorStability?.percentile?.value ? `${TEST_STABILITY.indicatorStability.percentile.value} <= ${TEST_STABILITY?.indicatorStability?.art || ''}` : '--' }}
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
          {{ t('reportPreview.apis.testResultDetail.stabilityTest.systemLoad') }}
        </div>
        <div class="flex-1">
          <div
            v-for="sys in sysItems"
            :key="sys.dataIndex"
            class="px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-b border-solid border-border-input">
            {{ sys.label }} &lt;= {{ TEST_STABILITY?.indicatorStability?.[sys.dataIndex] ? `${TEST_STABILITY.indicatorStability[sys.dataIndex]}${sys.unit}` : '--' }}
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
              {{ t('chart.average') }} {{ TEST_STABILITY_NODE?.[sys.meanDataIndex] ? `${TEST_STABILITY_NODE[sys.meanDataIndex]}${sys.unit}` : '--' }}
              <img
                v-if="getStabilityIcon(sys.meanDataIndex)"
                :src="getStabilityIcon(sys.meanDataIndex)"
                class="w-3" />
            </div>
            <div class="flex-1">
              {{ t('chart.max') }} {{ TEST_STABILITY_NODE?.[sys.maxDataIndex] ? `${TEST_STABILITY_NODE?.[sys.maxDataIndex]}${sys.unit}` : '--' }}
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
