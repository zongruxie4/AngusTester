<script setup lang="ts">
import { inject, onMounted, onBeforeUnmount, ref, watch, nextTick, computed } from 'vue';
import { Button, Tag, Card, Divider, Statistic } from 'ant-design-vue';
import { Icon, notification, Spin, Table, Input, Select } from '@xcan-angus/vue-ui';
import { toClipboard, duration } from '@xcan-angus/infra';
import { evaluation } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { ProjectMenuKey } from '@/views/project/menu';
import { EvaluationDetail } from '../types';
import eCharts from '@/utils/echarts';
import { throttle, debounce } from 'throttle-debounce';
import { enumUtils } from '@xcan-angus/infra';
import { EvaluationPurpose, TestPurpose } from '@/enums/enums';
import { testerSetting } from '@/api/tester';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';

const { t } = useI18n();
const testPurposeOptions = enumUtils.enumToMessages(EvaluationPurpose)

// Props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const addTabPane = inject<(data: any) => void>('addTabPane', () => ({}));

// Reactive data
const loading = ref(false);
const evaluationDetail = ref<EvaluationDetail>();

/**
 * Chart references
 */
const performancePassedRateRef = ref<HTMLElement>();
const functionalPassedRateRef = ref<HTMLElement>();
const stabilityPassedRateRef = ref<HTMLElement>();
const compatibilityScoreRef = ref<HTMLElement>();
const usabilityScoreRef = ref<HTMLElement>();
const maintainabilityScoreRef = ref<HTMLElement>();
const scalabilityScoreRef = ref<HTMLElement>();
const securityScoreRef = ref<HTMLElement>();
const complianceScoreRef = ref<HTMLElement>();
const availabilityScoreRef = ref<HTMLElement>();

/**
 * Chart instances
 */
let performanceChart: any = null;
let functionalChart: any = null;
let stabilityChart: any = null;
let compatibilityChart: any = null;
let usabilityChart: any = null;
let maintainabilityChart: any = null;
let scalabilityChart: any = null;
let securityChart: any = null;
let complianceChart: any = null;
let availabilityChart: any = null;
/**
 * Get score color based on score value (0-10 scale)
 */
const getScoreColor = (score: number) => {
  if (score >= 9) return '#52c41a'; // green
  if (score >= 8) return '#1890ff'; // blue
  if (score >= 6) return '#faad14'; // orange
  return '#ff4d4f'; // red
};

/**
 * Get rate color based on rate value
 */
const getRateColor = (rate: number) => {
  if (rate >= 90) return '#52c41a'; // green
  if (rate >= 80) return '#1890ff'; // blue
  if (rate >= 60) return '#faad14'; // orange
  return '#ff4d4f'; // red
};

/**
 * Create pie chart for pass rate
 */
const createPassRatePieConfig = (rate: number, numerator: number, denominator: number, title: string) => {
  const color = getRateColor(rate);
  const remaining = denominator - numerator;
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          formatter: '{d}%',
          fontSize: 16,
          fontWeight: 'bold',
          color: '#333'
        },
        emphasis: {
          label: {
            show: false,
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: numerator,
            name: t('evaluation.detail.chartLabels.passed'),
            itemStyle: { color: color }
          },
          {
            value: remaining,
            name: t('evaluation.detail.chartLabels.notPassed'),
            itemStyle: { color: '#E4E7ED' }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: 'center',
        z: 10,
        style: {
          text: `${(+rate).toFixed(1)}%`,
          fontSize: 24,
          fontWeight: 'bold',
          fill: color,
          textAlign: 'center'
        }
      },
      {
        type: 'text',
        left: 'center',
        top: '60%',
        z: 10,
        style: {
          text: `${numerator}/${denominator}`,
          fontSize: 14,
          fill: '#666',
          textAlign: 'center'
        }
      }
    ]
  };
};

/**
 * Create progress bar chart for score (0-10 scale)
 */
const createScoreProgressBarConfig = (score: number, title: string) => {
  const color = getScoreColor(score);
  
  return {
    grid: {
      title: t('evaluation.detail.averageScore'),
      left: '20',
      right: '15%',
      top: '20%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: 10,
      min: 0,
      show: true,
      axisLine: {
        show: true,
        lineStyle: {
          color: '#d9d9d9'
        }
      },
      axisTick: {
        show: false
      },
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed',
          color: '#E8E8E8'
        }
      },
      axisLabel: {
        show: true,
        fontSize: 11,
        color: '#8c8c8c',
        formatter: '{value}'
      }
    },
    yAxis: {
      type: 'category',
      data: [t('evaluation.detail.averageScore')],
      show: true
    },
    series: [
      {
        name: title,
        type: 'bar',
        barWidth: '60%',
        data: [score],
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              {
                offset: 0,
                color: color
              },
              {
                offset: 1,
                color: color + 'DD'
              }
            ]
          },
          borderRadius: score >= 9.9 ? [0, 4, 4, 0] : [0, 0, 0, 0],
          shadowBlur: 4,
          shadowColor: color + '40'
        },
        label: {
          show: true,
          position: 'right',
          formatter: `${score.toFixed(1)}${t('evaluation.detail.points')}`,
          fontSize: 13,
          fontWeight: 'bold',
          color: color,
          offset: [8, 0]
        },
        z: 2
      }
    ]
  };
};

/**
 * Initialize or update a chart
 */
const initOrUpdateChart = (chartInstance: any, ref: HTMLElement | undefined, config: any) => {
  if (!ref) return null;
  
  if (chartInstance && !chartInstance.isDisposed()) {
    chartInstance.setOption(config, true);
    return chartInstance;
  } else {
    const chart = eCharts.init(ref);
    chart.setOption(config);
    return chart;
  }
};

/**
 * Initialize and update charts
 */
const initCharts = () => {
  const result = evaluationDetail.value?.result;
  if (!result || !result.metrics) return;

  const metrics = result.metrics;

  // Performance Passed Rate Chart
  if (metrics.PERFORMANCE_SCORE && performancePassedRateRef.value) {
    const data = metrics.PERFORMANCE_SCORE;
    performanceChart = initOrUpdateChart(
      performanceChart,
      performancePassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, t('evaluation.detail.chartTitles.performanceTestPassRate'))
    );
  }

  // Functional Passed Rate Chart
  if (metrics.FUNCTIONAL_SCORE && functionalPassedRateRef.value) {
    const data = metrics.FUNCTIONAL_SCORE;
    functionalChart = initOrUpdateChart(
      functionalChart,
      functionalPassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, t('evaluation.detail.chartTitles.functionalTestPassRate'))
    );
  }

  // Stability Passed Rate Chart
  if (metrics.STABILITY_SCORE && stabilityPassedRateRef.value) {
    const data = metrics.STABILITY_SCORE;
    stabilityChart = initOrUpdateChart(
      stabilityChart,
      stabilityPassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, t('evaluation.detail.chartTitles.stabilityTestPassRate'))
    );
  }

  // Compatibility Score Chart - Progress Bar
  if (metrics.COMPATIBILITY_SCORE && compatibilityScoreRef.value) {
    compatibilityChart = initOrUpdateChart(
      compatibilityChart,
      compatibilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.COMPATIBILITY_SCORE.score), enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.COMPATIBILITY_SCORE))
    );
  }

  // Usability Score Chart - Progress Bar
  if (metrics.USABILITY_SCORE && usabilityScoreRef.value) {
    usabilityChart = initOrUpdateChart(
      usabilityChart,
      usabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.USABILITY_SCORE.score), enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.USABILITY_SCORE))
    );
  }

  // Maintainability Score Chart - Progress Bar
  if (metrics.MAINTAINABILITY_SCORE && maintainabilityScoreRef.value) {
    maintainabilityChart = initOrUpdateChart(
      maintainabilityChart,
      maintainabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.MAINTAINABILITY_SCORE.score), enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.MAINTAINABILITY_SCORE))
    );
  }

  // Scalability Score Chart - Progress Bar
  if (metrics.SCALABILITY_SCORE && scalabilityScoreRef.value) {
    scalabilityChart = initOrUpdateChart(
      scalabilityChart,
      scalabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.SCALABILITY_SCORE.score), enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.SCALABILITY_SCORE))
    );
  }

  // Security Score Chart - Progress Bar
  if (metrics.SECURITY_SCORE && securityScoreRef.value) {
    securityChart = initOrUpdateChart(
      securityChart,
      securityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.SECURITY_SCORE.score), enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.SECURITY_SCORE))
    );
  }

  // Compliance Score Chart - Progress Bar
  if (metrics.COMPLIANCE_SCORE && complianceScoreRef.value) {
    complianceChart = initOrUpdateChart(
      complianceChart,
      complianceScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.COMPLIANCE_SCORE.score), enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.COMPLIANCE_SCORE))
    );
  }

  // Availability Score Chart - Progress Bar
  if (metrics.AVAILABILITY_SCORE && availabilityScoreRef.value) {
    availabilityChart = initOrUpdateChart(
      availabilityChart,
      availabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.AVAILABILITY_SCORE.score), enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.AVAILABILITY_SCORE))
    );
  }
};

/**
 * Resize all charts
 */
const resizeCharts = throttle(500, () => {
  const charts = [
    performanceChart,
    functionalChart,
    stabilityChart,
    compatibilityChart,
    usabilityChart,
    maintainabilityChart,
    scalabilityChart,
    securityChart,
    complianceChart,
    availabilityChart
  ];
  charts.forEach(chart => {
    if (chart && !chart.isDisposed()) {
      chart.resize();
    }
  });
});

/**
 * Dispose all charts
 */
const disposeCharts = () => {
  const charts = [
    performanceChart,
    functionalChart,
    stabilityChart,
    compatibilityChart,
    usabilityChart,
    maintainabilityChart,
    scalabilityChart,
    securityChart,
    complianceChart,
    availabilityChart
  ];
  charts.forEach(chart => {
    if (chart && !chart.isDisposed()) {
      chart.dispose();
    }
  });
};

/**
 * Loads evaluation detail data from API
 */
const loadEvaluationDetail = async (evaluationId: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await evaluation.getEvaluationDetail(evaluationId);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as EvaluationDetail;
  if (!data) {
    return;
  }

  evaluationDetail.value = data;
  updateTabPaneTitle(data.name, evaluationId);
  
  // Initialize charts after data loaded
  nextTick(() => {
    initCharts();
  });
};

/**
 * Updates the tab pane title with the evaluation name
 */
const updateTabPaneTitle = (evaluationName: string, evaluationId: string) => {
  if (evaluationName && typeof updateTabPane === 'function') {
    updateTabPane({ name: evaluationName, _id: evaluationId + '-detail' });
  }
};

/**
 * Navigates to edit page
 */
const handleEdit = () => {
  if (!evaluationDetail.value) {
    return;
  }
  addTabPane({
    _id: String(evaluationDetail.value.id),
    value: 'evaluationEdit',
    noCache: true,
    data: { _id: String(evaluationDetail.value.id), id: String(evaluationDetail.value.id) }
  });
};

/**
 * Generates evaluation result
 */
const handleGenerateResult = async () => {
  if (!evaluationDetail.value) {
    return;
  }

  const id = evaluationDetail.value.id;
  loading.value = true;
  const [error] = await evaluation.generateResult(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('evaluation.actions.generateResultSuccess'));
  await loadEvaluationDetail(String(id));
};

/**
 * Copies the evaluation URL to clipboard
 */
const copyEvaluationUrl = () => {
  if (!evaluationDetail.value) {
    return;
  }
  const evaluationUrl = window.location.origin + `/project#${ProjectMenuKey.EVALUATION}?id=${evaluationDetail.value.id}`;
  toClipboard(evaluationUrl).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFailed'));
  });
};

/**
 * Refreshes the evaluation detail data
 */
const refreshEvaluationData = async () => {
  const evaluationId = props.data?.id;
  if (!evaluationId) {
    return;
  }
  await loadEvaluationDetail(String(evaluationId));
};

const showQualityScores = computed(() => {
  return evaluationDetail.value?.result?.metrics?.COMPATIBILITY_SCORE
  || evaluationDetail.value?.result?.metrics?.USABILITY_SCORE
  || evaluationDetail.value?.result?.metrics?.MAINTAINABILITY_SCORE
  || evaluationDetail.value?.result?.metrics?.SCALABILITY_SCORE
  || evaluationDetail.value?.result?.metrics?.SECURITY_SCORE
  || evaluationDetail.value?.result?.metrics?.COMPLIANCE_SCORE
  || evaluationDetail.value?.result?.metrics?.AVAILABILITY_SCORE;
});

const showPassRateScores = computed(() => {
  return evaluationDetail.value?.result?.metrics?.FUNCTIONAL_SCORE || evaluationDetail.value?.result?.metrics?.PERFORMANCE_SCORE || evaluationDetail.value?.result?.metrics?.STABILITY_SCORE;
});

const indicatorConfig = ref<any>({});
const loadIndicatorConfig = async () => {
  const [error, res] = await testerSetting.getEvaluationIndicator();
  if (error) {
    return;
  }
  indicatorConfig.value = res?.data || {};
};

const caseNameKeyword = ref('');
const caseCodeKeyword = ref('');
const testPurpose = ref<EvaluationPurpose | undefined>(undefined);
const showTableList = ref([])

const caseDetailsColumns = [
  {
    title: '编码',
    dataIndex: 'code',
    width: 100
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 100
  },
  {
    title: '测试结果',
    dataIndex: 'testResult',
    width: 100,
    customRender: ({ text }) => {
      return text?.message;
    }
  },
  {
    title: '测试得分',
    dataIndex: 'testScore',
    width: 100
  },
  {
    title: '测试备注',
    dataIndex: 'testRemark',
    width: 100
  },
  {
    title: '测试人',
    dataIndex: 'testerName',
    width: 100
  }
];

// Lifecycle hooks
onMounted(async () => {
  loadIndicatorConfig();
  watch(() => props.data, async (newValue, oldValue) => {
    const evaluationId = newValue?.id;
    if (!evaluationId) {
      return;
    }

    const previousEvaluationId = oldValue?.id;
    if (evaluationId === previousEvaluationId) {
      return;
    }

    await loadEvaluationDetail(String(evaluationId));
  }, { immediate: true });

  // Watch for result changes to update charts
  watch(() => evaluationDetail.value?.result, () => {
    showTableList.value = evaluationDetail.value?.result?.caseDetails || [];
    nextTick(() => {
      initCharts();
    });
  }, { deep: true });


  watch([() => caseNameKeyword.value, () => caseCodeKeyword.value, () => testPurpose.value], debounce(duration.search, () => {
    showTableList.value = evaluationDetail.value?.result?.caseDetails || [];
    if (caseNameKeyword.value) {
      showTableList.value = (showTableList.value).filter(item => item.name.includes(caseNameKeyword.value));
    }
    if (caseCodeKeyword.value) {
      showTableList.value = (showTableList.value).filter(item => item.code.includes(caseCodeKeyword.value));
    }
    if (testPurpose.value) {
      showTableList.value = (showTableList.value).filter(item => item.testPurpose === testPurpose.value);
    }
  }));

  // Add window resize listener
  window.addEventListener('resize', resizeCharts);
});

onBeforeUnmount(() => {
  disposeCharts();
  window.removeEventListener('resize', resizeCharts);
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <!-- Action Buttons -->
    <div class="flex items-start flex-wrap space-y-b-2 space-x-2.5 mb-4">
      <Button
        type="default"
        size="small"
        class="p-0">
        <a
          class="flex items-center space-x-1 leading-6.5 px-1.75"
          @click="handleEdit">
          <Icon icon="icon-shuxie" class="text-3.5" />
          <span>{{ t('actions.edit') }}</span>
        </a>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleGenerateResult">
        <Icon icon="icon-shengchengshuju" class="text-3.5" />
        <span>{{ t('evaluation.actions.generateResult') }}</span>
      </Button>

      <Button
        size="small"
        class="flex items-center"
        @click="copyEvaluationUrl">
        <Icon class="mr-1 flex-shrink-0" icon="icon-fuzhi" />
        <span>{{ t('actions.copyLink') }}</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="refreshEvaluationData">
        <Icon class="mr-1 flex-shrink-0" icon="icon-shuaxin" />
        <span>{{ t('actions.refresh') }}</span>
      </Button>
    </div>

    <!-- Part 1: Evaluation Information -->
    <Card class="evaluation-info-card mb-4" :bordered="false">
      <template #title>
        <div class="card-header">
          <Icon icon="icon-xinxi" class="header-icon" />
          <span class="text-base font-semibold">{{ t('evaluation.detail.infoTitle') }}</span>
        </div>
      </template>
      <div v-if="evaluationDetail" class="info-column">
        <div class="info-row">
          <div class="info-label">
            <Icon icon="icon-mingcheng" class="label-icon" />
            <span>{{ t('common.name') }}</span>
          </div>
          <div class="info-value">
            <span class="value-text">{{ evaluationDetail.name }}</span>
          </div>
        </div>

        <div class="info-row">
          <div class="info-label">
            <Icon icon="icon-fanwei" class="label-icon" />
            <span>{{ t('evaluation.columns.scope') }}</span>
          </div>
          <div class="info-value">
            <Tag v-if="evaluationDetail.scope" class="custom-tag scope-tag">
              {{ (evaluationDetail.scope as any)?.message || (evaluationDetail.scope as any)?.value || evaluationDetail.scope }}
            </Tag>
            <span v-else class="empty-text">-</span>
          </div>
        </div>

        <div class="info-row">
          <div class="info-label">
            <Icon icon="icon-mudi" class="label-icon" />
            <span>{{ t('evaluation.columns.purposes') }}</span>
          </div>
          <div class="info-value">
            <div v-if="evaluationDetail.purposes && evaluationDetail.purposes.length > 0" class="tag-group">
              <Tag
                v-for="(purpose, index) in evaluationDetail.purposes"
                :key="index"
                class="custom-tag purpose-tag">
                {{ (purpose as any)?.message || (purpose as any)?.value || purpose }}
              </Tag>
            </div>
            <span v-else class="empty-text">-</span>
          </div>
        </div>

        <div v-if="evaluationDetail.resourceId || (evaluationDetail as any).resourceName" class="info-row">
          <div class="info-label">
            <Icon icon="icon-ziyuan" class="label-icon" />
            <span>{{ t('evaluation.columns.resourceName') }}</span>
          </div>
          <div class="info-value">
            <span class="value-text resource-id">{{ (evaluationDetail as any).resourceName || evaluationDetail.resourceName || '-' }}</span>
          </div>
        </div>

        <div class="info-row">
          <div class="info-label">
            <Icon icon="icon-kaishishijian" class="label-icon" />
            <span>{{ t('evaluation.detail.caseCreationTime') }}</span>
          </div>
          <div class="info-value">
            <span class="value-text">{{ evaluationDetail.startDate || '-' }} - {{ evaluationDetail.deadlineDate || '-' }}</span>
          </div>
        </div>

        <div class="info-row">
          <div class="info-label">
            <Icon icon="icon-chuangjianzhe" class="label-icon" />
            <span>{{ t('common.createdBy') }}</span>
          </div>
          <div class="info-value">
            <span class="value-text">{{ evaluationDetail.createdByName || '-' }}</span>
          </div>
        </div>

        <div class="info-row">
          <div class="info-label">
            <Icon icon="icon-chuangjianshijian" class="label-icon" />
            <span>{{ t('common.createdDate') }}</span>
          </div>
          <div class="info-value">
            <span class="value-text">{{ evaluationDetail.createdDate || '-' }}</span>
          </div>
        </div>

      </div>
    </Card>

    <!-- Part 2: Evaluation Results with Charts -->
    <Card v-if="evaluationDetail?.result" class="mb-4" :bordered="false">
      <template #title>
        <div class="text-base font-semibold">{{ t('evaluation.detail.resultTitle') }}</div>
      </template>

      
      <div class="evaluation-results-container">

        <div v-if="showQualityScores" class="flex items-center">
          <div class="">
            <h3 class="text-4 font-semibold mb-4 text-title">{{ t('evaluation.detail.comprehensiveResult') }}</h3>
          </div>
          <div class="quality-score-content inline-flex flex-1 justify-around space-x-4">
            <Statistic
              :title="t('evaluation.detail.comprehensiveScore')"
              :value="evaluationDetail.result.overallScore"
              suffix=""
              class="flex flex-col-reverse"
              :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
            <Statistic
              :title="t('evaluation.detail.totalCases')"
              :value="evaluationDetail.result.totalCases"
              suffix=""
              class="flex flex-col-reverse"
              :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
            <Statistic
              :title="t('evaluation.detail.totalIndicators')"
              :value="Object.keys(evaluationDetail.result.metrics || {}).length"
              suffix=""
              class="flex flex-col-reverse"
              :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
          </div>
        </div>

        <Divider />

        <template v-if="showPassRateScores">
          <!-- Test Pass Rates Section -->
          <div class="results-section mb-6">
            <h3 class="text-4 font-semibold mb-4 text-title">{{ t('evaluation.detail.mainIndicators') }}</h3>
            
            <div class="pass-rate-charts-grid">
              <!-- Functional Test Pass Rate -->
              <Card
                v-if="evaluationDetail.result.metrics?.FUNCTIONAL_SCORE"
                class="pass-rate-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.FUNCTIONAL_SCORE) }}</div>
                </template>
                <div class="pass-rate-content">
                  <div ref="functionalPassedRateRef" class="pass-rate-chart"></div>
                  <div class="pass-rate-info">
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.passRate') }}</span>
                      <span
                        class="info-value"
                        :style="{ color: getRateColor(evaluationDetail.result.metrics.FUNCTIONAL_SCORE.rate) }">
                        {{ (+evaluationDetail.result.metrics.FUNCTIONAL_SCORE.rate).toFixed(1) }}%
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.passedCases') }}</span>
                      <span class="info-value">
                        {{ evaluationDetail.result.metrics.FUNCTIONAL_SCORE.numerator }}/{{ evaluationDetail.result.metrics.FUNCTIONAL_SCORE.denominator }}
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.score') }}</span>
                      <span class="info-value">
                        {{ (+evaluationDetail.result.metrics.FUNCTIONAL_SCORE.score).toFixed(1) }} {{ t('evaluation.detail.points') }}
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.weight') }}</span>
                      <span class="info-value">
                        {{ indicatorConfig[EvaluationPurpose.FUNCTIONAL_SCORE] }} %
                      </span>
                    </div>
                  </div>
                </div>
              </Card>
  
              <!-- Performance Test Pass Rate -->
              <Card
                v-if="evaluationDetail.result.metrics?.PERFORMANCE_SCORE"
                class="pass-rate-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.PERFORMANCE_SCORE) }}</div>
                </template>
                <div class="pass-rate-content">
                  <div ref="performancePassedRateRef" class="pass-rate-chart"></div>
                  <div class="pass-rate-info">
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.passRate') }}</span>
                      <span
                        class="info-value"
                        :style="{ color: getRateColor(evaluationDetail.result.metrics.PERFORMANCE_SCORE.rate) }">
                        {{ (+evaluationDetail.result.metrics.PERFORMANCE_SCORE.rate).toFixed(1) }}%
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.passedCases') }}</span>
                      <span class="info-value">
                        {{ evaluationDetail.result.metrics.PERFORMANCE_SCORE.numerator }}/{{ evaluationDetail.result.metrics.PERFORMANCE_SCORE.denominator }}
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.score') }}</span>
                      <span class="info-value">
                        {{ (+evaluationDetail.result.metrics.PERFORMANCE_SCORE.score).toFixed(1) }} {{ t('evaluation.detail.points') }}
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.weight') }}</span>
                      <span class="info-value">
                        {{ indicatorConfig[EvaluationPurpose.PERFORMANCE_SCORE] }} %
                      </span>
                    </div>
                  </div>
                </div>
              </Card>
  
              <!-- Stability Test Pass Rate -->
              <Card
                v-if="evaluationDetail.result.metrics?.STABILITY_SCORE"
                class="pass-rate-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.STABILITY_SCORE) }}</div>
                </template>
                <div class="pass-rate-content">
                  <div ref="stabilityPassedRateRef" class="pass-rate-chart"></div>
                  <div class="pass-rate-info">
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.passRate') }}</span>
                      <span
                        class="info-value"
                        :style="{ color: getRateColor(evaluationDetail.result.metrics.STABILITY_SCORE.rate) }">
                        {{ (+evaluationDetail.result.metrics.STABILITY_SCORE.rate).toFixed(1) }}%
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.passedCases') }}</span>
                      <span class="info-value">
                        {{ evaluationDetail.result.metrics.STABILITY_SCORE.numerator }}/{{ evaluationDetail.result.metrics.STABILITY_SCORE.denominator }}
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.score') }}</span>
                      <span class="info-value">
                        {{ (+evaluationDetail.result.metrics.STABILITY_SCORE.score).toFixed(1) }} {{ t('evaluation.detail.points') }}
                      </span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('evaluation.detail.weight') }}</span>
                      <span class="info-value">
                        {{ indicatorConfig[EvaluationPurpose.STABILITY_SCORE] }} %
                      </span>
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          </div>
          <Divider />
        </template>

        <template v-if="showQualityScores">
          <!-- Quality Scores Section -->
          <div class="results-section">
            <h3 class="text-4 font-semibold mb-4 text-title">{{ t('evaluation.detail.otherIndicators') }}</h3>
            
            <div class="quality-scores-container space-y-2">
              <!-- Compatibility Score -->
              <Card
                v-if="evaluationDetail.result.metrics?.COMPATIBILITY_SCORE"
                class="quality-score-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ t('evaluation.detail.scoreTitles.compatibility') }}</div>
                </template>
                <div class="flex ">
                  <div class="quality-score-content inline-flex justify-around space-x-2 w-100">
                    <Statistic
                      :title="t('evaluation.detail.totalCases')"
                      :value="evaluationDetail.result.metrics?.COMPATIBILITY_SCORE.totalCases"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
                    <Statistic
                      :title="t('evaluation.detail.totalScore')"
                      :value="evaluationDetail.result.metrics?.COMPATIBILITY_SCORE.totalScore"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
                    <Statistic
                      :title="t('evaluation.detail.weight')"
                      :value="evaluationDetail.result.metrics?.COMPATIBILITY_SCORE.weight"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
                  </div>
                  <div ref="compatibilityScoreRef" class="quality-score-chart flex-1"></div>
                </div>
              </Card>
  
              <!-- Usability Score -->
              <Card
                v-if="evaluationDetail.result.metrics?.USABILITY_SCORE"
                class="quality-score-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ t('evaluation.detail.scoreTitles.usability') }}</div>
                </template>
                <div class="flex">
                  <div class="quality-score-content inline-flex justify-around space-x-2 w-100">
                      <Statistic
                        :title="t('evaluation.detail.totalCases')"
                        :value="evaluationDetail.result.metrics?.USABILITY_SCORE.totalCases"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
                      <Statistic
                        :title="t('evaluation.detail.totalScore')"
                        :value="evaluationDetail.result.metrics?.USABILITY_SCORE.totalScore"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
                      <Statistic
                        :title="t('evaluation.detail.weight')"
                        :value="evaluationDetail.result.metrics?.USABILITY_SCORE.weight"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
                  </div>
                  <div ref="usabilityScoreRef" class="quality-score-chart flex-1"></div>
                </div>
              </Card>
  
              <!-- Maintainability Score -->
              <Card
                v-if="evaluationDetail.result.metrics?.MAINTAINABILITY_SCORE"
                class="quality-score-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ t('evaluation.detail.scoreTitles.maintainability') }}</div>
                </template>
                <div class="flex">
                  
                  <div class="quality-score-content inline-flex justify-around space-x-2 w-100">
                      <Statistic
                        :title="t('evaluation.detail.totalCases')"
                        :value="evaluationDetail.result.metrics?.MAINTAINABILITY_SCORE.totalCases"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
                      <Statistic
                        :title="t('evaluation.detail.totalScore')"
                        :value="evaluationDetail.result.metrics?.MAINTAINABILITY_SCORE.totalScore"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
                      <Statistic
                        :title="t('evaluation.detail.weight')"
                        :value="evaluationDetail.result.metrics?.MAINTAINABILITY_SCORE.weight"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
                  </div>
                  <div ref="maintainabilityScoreRef" class="quality-score-chart flex-1"></div>
                </div>
              </Card>
  
              <!-- Scalability Score -->
              <Card
                v-if="evaluationDetail.result.metrics?.SCALABILITY_SCORE"
                class="quality-score-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ t('evaluation.detail.scoreTitles.scalability') }}</div>
                </template>
                <div class="flex">
                  
                  <div class="quality-score-content inline-flex justify-around space-x-2 w-100">
                      <Statistic
                        :title="t('evaluation.detail.totalCases')"
                        :value="evaluationDetail.result.metrics?.SCALABILITY_SCORE.totalCases"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
                      <Statistic
                        :title="t('evaluation.detail.totalScore')"
                        :value="evaluationDetail.result.metrics?.SCALABILITY_SCORE.totalScore"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
                      <Statistic
                        :title="t('evaluation.detail.weight')"
                        :value="evaluationDetail.result.metrics?.SCALABILITY_SCORE.weight"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
                  </div>
                  <div ref="scalabilityScoreRef" class="quality-score-chart flex-1"></div>
                </div>
              </Card>
  
              <!-- Security Score -->
              <Card
                v-if="evaluationDetail.result.metrics?.SECURITY_SCORE"
                class="quality-score-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ t('evaluation.detail.scoreTitles.security') }}</div>
                </template>
                <div class="flex">
                  
                  <div class="quality-score-content inline-flex justify-around space-x-2 w-100">
                      <Statistic
                        :title="t('evaluation.detail.totalCases')"
                        :value="evaluationDetail.result.metrics?.SECURITY_SCORE.totalCases"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
                      <Statistic
                        :title="t('evaluation.detail.totalScore')"
                        :value="evaluationDetail.result.metrics?.SECURITY_SCORE.totalScore"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
                      <Statistic
                        :title="t('evaluation.detail.weight')"
                        :value="evaluationDetail.result.metrics?.SECURITY_SCORE.weight"
                        suffix=""
                        class="flex flex-col-reverse"
                        :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
                  </div>
                  <div ref="securityScoreRef" class="quality-score-chart flex-1"></div>
                </div>
              </Card>

              <!-- Compliance Score -->
              <Card
                v-if="evaluationDetail.result.metrics?.COMPLIANCE_SCORE"
                class="quality-score-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ t('evaluation.detail.scoreTitles.compliance') }}</div>
                </template>
                <div class="flex">
                  <div class="quality-score-content inline-flex justify-around space-x-2 w-100">
                    <Statistic
                      :title="t('evaluation.detail.totalCases')"
                      :value="evaluationDetail.result.metrics?.COMPLIANCE_SCORE.totalCases"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
                    <Statistic
                      :title="t('evaluation.detail.totalScore')"
                      :value="evaluationDetail.result.metrics?.COMPLIANCE_SCORE.totalScore"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
                    <Statistic
                      :title="t('evaluation.detail.weight')"
                      :value="evaluationDetail.result.metrics?.COMPLIANCE_SCORE.weight"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
                  </div>
                  <div ref="complianceScoreRef" class="quality-score-chart flex-1"></div>
                </div>
              </Card> 

              <!-- Availability Score -->
              <Card
                v-if="evaluationDetail.result.metrics?.AVAILABILITY_SCORE"
                class="quality-score-card"
                :bordered="false">
                <template #title>
                  <div class="card-title">{{ t('evaluation.detail.scoreTitles.availability') }}</div>
                </template>
                <div class="flex">
                  <div class="quality-score-content inline-flex justify-around space-x-2 w-100">
                    <Statistic
                      :title="t('evaluation.detail.totalCases')"
                      :value="evaluationDetail.result.metrics?.AVAILABILITY_SCORE.totalCases"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
                    <Statistic
                      :title="t('evaluation.detail.totalScore')"
                      :value="evaluationDetail.result.metrics?.AVAILABILITY_SCORE.totalScore"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
                    <Statistic
                      :title="t('evaluation.detail.weight')"
                      :value="evaluationDetail.result.metrics?.AVAILABILITY_SCORE.weight"
                      suffix=""
                      class="flex flex-col-reverse"
                      :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
                  </div>
                  <div ref="availabilityScoreRef" class="quality-score-chart flex-1"></div>
                </div>
              </Card>
            </div>
          </div>
        </template>
      </div>
    </Card>

    <Card v-if="evaluationDetail?.result?.caseDetails" :bordered="false">
      <template #title>
        <div class="text-base font-semibold">测试用例明细</div>
      </template>
     
      <div class="flex space-x-2">
        <Input v-model:value="caseNameKeyword" class="w-50" placeholder="搜索测试用例名称" />
        <Input v-model:value="caseCodeKeyword" class="w-50" placeholder="搜索测试用例编码" />
        <SelectEnum
          v-model:value="testPurpose"
          class="w-50"
          allowClear
          placeholder="测试目的"
          enumKey="TestPurpose"/>
      </div>
      <div class="evaluation-results-container">
        <Table
          :columns="caseDetailsColumns"
          :dataSource="showTableList"
          :pagination="false"
          :scroll="{ x: '100%' }"
          :rowKey="record => record.id"
          noDataSize="small">
        </Table>
      </div>
    </Card>

    <!-- No Result Placeholder -->
    <Card v-else-if="evaluationDetail && !evaluationDetail.result" :bordered="false">
      <div class="no-result-placeholder">
        <Icon icon="icon-shuju" class="text-6xl text-gray-400 mb-4" />
        <div class="text-lg text-gray-500 mb-2">{{ t('evaluation.detail.placeholders.noResult') }}</div>
        <div class="text-sm text-gray-400">{{ t('evaluation.detail.placeholders.generateResultHint') }}</div>
      </div>
    </Card>
  </Spin>
</template>

<style scoped>
/* Card Styles */
:deep(.ant-card) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  overflow: hidden;
}

:deep(.ant-card):hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 8px 16px;
  background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%);
}

:deep(.ant-card-body) {
  padding: 16px;
}

/* Evaluation Info Card */
.evaluation-info-card {
  background: #fff;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
  color: #1890ff;
}

/* Info Column Layout */
.info-column {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row:hover {
  background: #fafafa;
}

.info-row:nth-child(even) {
  background: #fafbfc;
}

.info-row:nth-child(even):hover {
  background: #f5f5f5;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 140px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 500;
  color: #595959;
}

.label-icon {
  font-size: 14px;
  color: #8c8c8c;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.value-text {
  font-size: 13px;
  color: #262626;
  word-break: break-word;
}

.resource-id {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.empty-text {
  color: #bfbfbf;
  font-style: italic;
}

/* Tag Styles */
.custom-tag {
  margin: 0;
  border-radius: 4px;
  font-size: 12px;
  padding: 2px 10px;
  border: none;
  transition: all 0.2s ease;
}

.scope-tag {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #0958d9;
}

.scope-tag:hover {
  background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.purpose-tag {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #389e0d;
}

.purpose-tag:hover {
  background: linear-gradient(135deg, #d9f7be 0%, #b7eb8f 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.2);
}

/* Responsive Design */
@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-label {
    min-width: auto;
    width: 100%;
  }
}

@media (max-width: 576px) {
  :deep(.ant-card-body) {
    padding: 16px;
  }

  .info-row {
    padding: 12px;
  }
}

.evaluation-results-container {
  padding: 8px 0;
}

.results-section {
  width: 100%;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

/* Pass Rate Charts Grid */
.pass-rate-charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
}

.pass-rate-card {
  border: 1px solid #e8e8e8;
}

.pass-rate-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.pass-rate-chart {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

.pass-rate-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  font-weight: 600;
  color: #333;
}

/* Quality Scores Container */
.quality-scores-container {
  /* display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px; */
}

.quality-score-card {
  border: 1px solid #e8e8e8;
}

:deep(.quality-score-card > .ant-card-head) {
  padding: 0 16px;
}

:deep(.quality-score-card > .ant-card-body) {
  padding: 0 16px;
}

.quality-score-chart {
  width: 100%;
  height: 80px;
  min-height: 80px;
}

/* No Result Placeholder */
.no-result-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .pass-rate-charts-grid {
    grid-template-columns: 1fr;
  }

  .pass-rate-content {
    flex-direction: column;
    align-items: center;
  }

  /* .quality-scores-container {
    grid-template-columns: 1fr;
  } */
}
</style>

