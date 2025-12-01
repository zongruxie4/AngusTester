<script lang="ts" setup>
import { computed, ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '@/views/report/preview/PropsType';
import eCharts from '@/utils/echarts';
import { EvaluationPurpose } from '@/enums/enums';
import { enumUtils } from '@xcan-angus/infra';
import { Statistic } from 'ant-design-vue';

const { t } = useI18n();

type Props = {
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

enumUtils.enumToMessages(EvaluationPurpose);

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
 * Get rate color based on rate value (0-100 scale)
 */
const getRateColor = (rate: number) => {
  if (rate >= 90) return '#52c41a'; // green
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
            show: true,
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
            name: t('reportPreview.evaluation.detail.chartLabels.passed'),
            itemStyle: { color: color }
          },
          {
            value: remaining,
            name: t('reportPreview.evaluation.detail.chartLabels.notPassed'),
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
      left: '0%',
      right: '15%',
      top: '0%',
      bottom: '0%',
      containLabel: false
    },
    xAxis: {
      type: 'value',
      max: 10,
      min: 0,
      show: false,
      splitLine: {
        show: false
      }
    },
    yAxis: {
      type: 'category',
      data: [''],
      show: false
    },
    series: [
      {
        name: title,
        type: 'bar',
        barWidth: '100%',
        data: [score],
        itemStyle: {
          color: color,
          borderRadius: score >= 9.9 ? [4, 4, 4, 4] : [4, 0, 0, 4]
        },
        label: {
          show: true,
          position: 'right',
          formatter: `${(+score).toFixed(2)}${t('reportPreview.evaluation.detail.chartLabels.points')}`,
          fontSize: 14,
          fontWeight: 'bold',
          color: color,
          offset: [2, 0]
        },
        emphasis: {
          itemStyle: {
            color: color
          }
        }
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
    // Update existing chart
    chartInstance.setOption(config, true);
    return chartInstance;
  } else {
    // Create new chart
    const chart = eCharts.init(ref);
    chart.setOption(config);
    return chart;
  }
};

/**
 * Initialize and update charts
 */
const initCharts = () => {
  const metrics = props.dataSource?.content?.metrics;
  if (!metrics) return;

  // Performance Passed Rate Chart
  if (metrics.PERFORMANCE_SCORE && performancePassedRateRef.value) {
    const data = metrics.PERFORMANCE_SCORE;
    performanceChart = initOrUpdateChart(
      performanceChart,
      performancePassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, t('reportPreview.evaluation.detail.performanceTestPassRate.title'))
    );
  }

  // Functional Passed Rate Chart
  if (metrics.FUNCTIONAL_SCORE && functionalPassedRateRef.value) {
    const data = metrics.FUNCTIONAL_SCORE;
    functionalChart = initOrUpdateChart(
      functionalChart,
      functionalPassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, t('reportPreview.evaluation.detail.functionalTestPassRate.title'))
    );
  }

  // Stability Passed Rate Chart
  if (metrics.STABILITY_SCORE && stabilityPassedRateRef.value) {
    const data = metrics.STABILITY_SCORE;
    stabilityChart = initOrUpdateChart(
      stabilityChart,
      stabilityPassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, t('reportPreview.evaluation.detail.stabilityTestPassRate.title'))
    );
  }

  // Compatibility Score Chart - Progress Bar
  if (metrics.COMPATIBILITY_SCORE && compatibilityScoreRef.value) {
    compatibilityChart = initOrUpdateChart(
      compatibilityChart,
      compatibilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.COMPATIBILITY_SCORE.score), t('reportPreview.evaluation.detail.qualityScores.compatibility'))
    );
  }

  // Usability Score Chart - Progress Bar
  if (metrics.USABILITY_SCORE && usabilityScoreRef.value) {
    usabilityChart = initOrUpdateChart(
      usabilityChart,
      usabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.USABILITY_SCORE.score), t('reportPreview.evaluation.detail.qualityScores.usability'))
    );
  }

  // Maintainability Score Chart - Progress Bar
  if (metrics.MAINTAINABILITY_SCORE && maintainabilityScoreRef.value) {
    maintainabilityChart = initOrUpdateChart(
      maintainabilityChart,
      maintainabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.MAINTAINABILITY_SCORE.score), t('reportPreview.evaluation.detail.qualityScores.maintainability'))
    );
  }

  // Scalability Score Chart - Progress Bar
  if (metrics.SCALABILITY_SCORE && scalabilityScoreRef.value) {
    scalabilityChart = initOrUpdateChart(
      scalabilityChart,
      scalabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.SCALABILITY_SCORE.score), t('reportPreview.evaluation.detail.qualityScores.scalability'))
    );
  }

  // Security Score Chart - Progress Bar
  if (metrics.SECURITY_SCORE && securityScoreRef.value) {
    securityChart = initOrUpdateChart(
      securityChart,
      securityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.SECURITY_SCORE.score), t('reportPreview.evaluation.detail.qualityScores.security'))
    );
  }

  // Compliance Score Chart - Progress Bar
  if (metrics.COMPLIANCE_SCORE && complianceScoreRef.value) {
    complianceChart = initOrUpdateChart(
      complianceChart,
      complianceScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.COMPLIANCE_SCORE.score), t('reportPreview.evaluation.detail.qualityScores.compliance'))
    );
  }

  // Availability Score Chart - Progress Bar
  if (metrics.AVAILABILITY_SCORE && availabilityScoreRef.value) {
    availabilityChart = initOrUpdateChart(
      availabilityChart,
      availabilityScoreRef.value,
      createScoreProgressBarConfig(Number(metrics.AVAILABILITY_SCORE.score), t('reportPreview.evaluation.detail.qualityScores.availability'))
    );
  }
};

/**
 * Resize all charts
 */
const resizeCharts = () => {
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
    if (chart) {
      chart.resize();
    }
  });
};

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
    if (chart) {
      chart.dispose();
    }
  });
};

const basicColumns = computed(() => [
   [
    {
      name: t('reportPreview.evaluation.overview.targetType'),
      dataIndex: 'targetType',
      customRender: () => {
        return props.dataSource?.report?.targetType?.message;
      }
    },
    {
      name: t('reportPreview.evaluation.overview.targetName'),
      dataIndex: 'overallScore',
      customRender: () => {
        return props.dataSource?.report?.targetName;
      }
    },
  ],
  [
    {
      name: t('reportPreview.evaluation.overview.totalCases'),
      dataIndex: 'totalCases',
      customRender: () => {
        return props.dataSource?.content?.totalCases;
      }
    },
    {
      name: t('reportPreview.evaluation.overview.overallScore'),
      dataIndex: 'overallScore',
      customRender: () => {
        return props.dataSource?.content?.overallScore;
      }
    },
  ],

]);

// Watch for data changes
watch(() => props.dataSource, () => {
  nextTick(() => {
    initCharts();
  });
}, {
  immediate: true,
  deep: true
});

onMounted(() => {
  nextTick(() => {
    initCharts();
    window.addEventListener('resize', resizeCharts);
  });
});

onBeforeUnmount(() => {
  disposeCharts();
  window.removeEventListener('resize', resizeCharts);
});

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.evaluation.overview.title') }}</span>
    </h1>

    <div class="border-t border-l border-solid border-border-input mb-8">
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
                {{ item.customRender()}}
            </div>
            </template>
        </div>
    </div>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a2" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.evaluation.detail.title') }}</span>
    </h1>

    <div v-if="props.dataSource?.content?.metrics" class="metrics-charts-container">
      <!-- Functional Passed Rate -->
      <div v-if="props.dataSource?.content?.metrics.FUNCTIONAL_SCORE" class="metric-item mb-6">
        <h2 class="text-3.5 text-theme-title font-medium mb-3">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.FUNCTIONAL_SCORE) }}</h2>
        <div class="metric-chart-wrapper">
          <div ref="functionalPassedRateRef" class="metric-chart"></div>
          <div class="metric-info">
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.functionalTestPassRate.passRate') }}</span>
              <span class="info-value" :style="{ color: getRateColor(props.dataSource.content.metrics.FUNCTIONAL_SCORE.rate) }">
                {{ (+props.dataSource.content.metrics.FUNCTIONAL_SCORE.rate).toFixed(1) }}%
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.functionalTestPassRate.passCount') }}</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.FUNCTIONAL_SCORE.numerator }}/{{ props.dataSource.content.metrics.FUNCTIONAL_SCORE.denominator }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.functionalTestPassRate.score') }}</span>
              <span class="info-value">
                {{ (+props.dataSource.content.metrics.FUNCTIONAL_SCORE.score).toFixed(1) }} {{ t('reportPreview.evaluation.detail.chartLabels.points') }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.weight') }}</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.FUNCTIONAL_SCORE.weight }} %
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Performance Passed Rate -->
      <div v-if="props.dataSource?.content?.metrics.PERFORMANCE_SCORE" class="metric-item mb-6">
        <h2 class="text-3.5 text-theme-title font-medium mb-3">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.PERFORMANCE_SCORE) }}</h2>
        <div class="metric-chart-wrapper">
          <div ref="performancePassedRateRef" class="metric-chart"></div>
          <div class="metric-info">
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.performanceTestPassRate.passRate') }}</span>
              <span class="info-value" :style="{ color: getRateColor(props.dataSource.content.metrics.PERFORMANCE_SCORE.rate) }">
                {{ (+props.dataSource.content.metrics.PERFORMANCE_SCORE.rate).toFixed(1) }}%
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.performanceTestPassRate.passCount') }}</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.PERFORMANCE_SCORE.numerator }}/{{ props.dataSource.content.metrics.PERFORMANCE_SCORE.denominator }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.performanceTestPassRate.score') }}</span>
              <span class="info-value">
                {{ (+props.dataSource.content.metrics.PERFORMANCE_SCORE.score).toFixed(1) }} {{ t('reportPreview.evaluation.detail.chartLabels.points') }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.weight') }}</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.PERFORMANCE_SCORE.weight }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Stability Passed Rate -->
      <div v-if="props.dataSource?.content?.metrics.STABILITY_SCORE" class="metric-item mb-6">
        <h2 class="text-3.5 text-theme-title font-medium mb-3">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.STABILITY_SCORE) }}</h2>
        <div class="metric-chart-wrapper">
          <div ref="stabilityPassedRateRef" class="metric-chart"></div>
          <div class="metric-info">
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.stabilityTestPassRate.passRate') }}</span>
              <span class="info-value" :style="{ color: getRateColor(props.dataSource.content.metrics.STABILITY_SCORE.rate) }">
                {{ (+props.dataSource.content.metrics.STABILITY_SCORE.rate).toFixed(1) }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.stabilityTestPassRate.passCount') }}</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.STABILITY_SCORE.numerator }}/{{ props.dataSource.content.metrics.STABILITY_SCORE.denominator }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.stabilityTestPassRate.score') }}</span>
              <span class="info-value">
                {{ (+props.dataSource.content.metrics.STABILITY_SCORE.score).toFixed(1) }} {{ t('reportPreview.evaluation.detail.chartLabels.points') }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">{{ t('reportPreview.evaluation.detail.weight') }}</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.STABILITY_SCORE.weight }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quality Scores Grid with Progress Bars -->
      <div class="quality-scores-progress-container mb-6 space-y-6">
        <!-- Compatibility Score -->
        <div v-if="props.dataSource?.content?.metrics.COMPATIBILITY_SCORE" class="quality-score-progress-item">
          <div class="progress-header">
            <span class="progress-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.COMPATIBILITY_SCORE) }}</span>
          </div>
          <div class="flex items-center">
            <div class="quality-score-content inline-flex justify-around space-x-2 w-70">
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalCases')"
                :value="props.dataSource?.content?.metrics?.COMPATIBILITY_SCORE.totalCases"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalScore')"
                :value="props.dataSource?.content?.metrics?.COMPATIBILITY_SCORE.totalScore"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#722ed1' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.weight')"
                :value="props.dataSource?.content?.metrics?.COMPATIBILITY_SCORE.weight"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }" />
            </div>
            <span class="mr-1">{{ t('reportPreview.evaluation.detail.averageScore') }}</span>
            <div ref="compatibilityScoreRef" class="quality-score-progress-bar flex-1"></div>
          </div>
         
        </div>

        <!-- Usability Score -->
        <div v-if="props.dataSource?.content?.metrics.USABILITY_SCORE" class="quality-score-progress-item">
          <div class="progress-header">
            <span class="progress-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.USABILITY_SCORE) }}</span>
          </div>
          <div class="flex items-center">
            <div class="quality-score-content inline-flex justify-around space-x-2 w-70">
              <Statistic
                  :title="t('reportPreview.evaluation.detail.totalCases')"
                  :value="props.dataSource?.content?.metrics?.USABILITY_SCORE.totalCases"
                  suffix=""
                  class="flex flex-col-reverse"
                  :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }" />
                <Statistic
                  :title="t('reportPreview.evaluation.detail.totalScore')"
                  :value="props.dataSource?.content?.metrics?.USABILITY_SCORE.totalScore"
                  suffix=""
                  class="flex flex-col-reverse"
                  :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#722ed1' }" />
                <Statistic
                  :title="t('reportPreview.evaluation.detail.weight')"
                  :value="props.dataSource?.content?.metrics?.USABILITY_SCORE.weight"
                  suffix=""
                  class="flex flex-col-reverse"
                  :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }" />
              </div>
            <span class="mr-1">{{ t('reportPreview.evaluation.detail.averageScore') }}</span>
            <div ref="usabilityScoreRef" class="quality-score-progress-bar flex-1"></div>
          </div>
        </div>
        <!-- Maintainability Score -->
        <div v-if="props.dataSource?.content?.metrics.MAINTAINABILITY_SCORE" class="quality-score-progress-item">
          <div class="progress-header">
            <span class="progress-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.MAINTAINABILITY_SCORE) }}</span>
          </div>

          <div class="flex items-center">
            <div class="quality-score-content inline-flex justify-around space-x-2 w-70">
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalCases')"
                :value="props.dataSource?.content?.metrics?.MAINTAINABILITY_SCORE.totalCases"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalScore')"
                :value="props.dataSource?.content?.metrics?.MAINTAINABILITY_SCORE.totalScore"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#722ed1' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.weight')"
                :value="props.dataSource?.content?.metrics?.MAINTAINABILITY_SCORE.weight"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }" />
            </div>
            <span class="mr-1">{{ t('reportPreview.evaluation.detail.averageScore') }}</span>
            <div ref="maintainabilityScoreRef" class="quality-score-progress-bar flex-1"></div>
          </div>
        </div>

        <!-- Scalability Score -->
        <div v-if="props.dataSource?.content?.metrics.SCALABILITY_SCORE" class="quality-score-progress-item">
          <div class="progress-header">
            <span class="progress-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.SCALABILITY_SCORE) }}</span>
          </div>
          <div class="flex items-center"> 
            <div class="quality-score-content inline-flex justify-around space-x-2 w-70">
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalCases')"
                :value="props.dataSource?.content?.metrics?.SCALABILITY_SCORE.totalCases"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalScore')"
                :value="props.dataSource?.content?.metrics?.SCALABILITY_SCORE.totalScore"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#722ed1' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.weight')"
                :value="props.dataSource?.content?.metrics?.SCALABILITY_SCORE.weight"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }" />
            </div>
            <span class="mr-1">{{ t('reportPreview.evaluation.detail.averageScore') }}</span>
            <div ref="scalabilityScoreRef" class="quality-score-progress-bar flex-1"></div>
          </div>
        </div>

        <!-- Security Score -->
        <div v-if="props.dataSource?.content?.metrics.SECURITY_SCORE" class="quality-score-progress-item">
          <div class="progress-header">
            <span class="progress-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.SECURITY_SCORE) }}</span>
          </div>
          <div class="flex items-center"> 
            <div class="quality-score-content inline-flex justify-around space-x-2 w-70">
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalCases')"
                :value="props.dataSource?.content?.metrics?.SECURITY_SCORE.totalCases"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalScore')"
                :value="props.dataSource?.content?.metrics?.SECURITY_SCORE.totalScore"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#722ed1' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.weight')"
                :value="props.dataSource?.content?.metrics?.SECURITY_SCORE.weight"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }" />
            </div>
            <span class="mr-1">{{ t('reportPreview.evaluation.detail.averageScore') }}</span>
            <div ref="securityScoreRef" class="quality-score-progress-bar flex-1"></div>
          </div>
        </div>

        <!-- Compliance Score -->
        <div v-if="props.dataSource?.content?.metrics.COMPLIANCE_SCORE" class="quality-score-progress-item">
          <div class="progress-header">
            <span class="progress-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.COMPLIANCE_SCORE) }}</span>
          </div>
          <div class="flex items-center">  
            <div class="quality-score-content inline-flex justify-around space-x-2 w-70">
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalCases')"
                :value="props.dataSource?.content?.metrics?.COMPLIANCE_SCORE.totalCases"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalScore')"
                :value="props.dataSource?.content?.metrics?.COMPLIANCE_SCORE.totalScore"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#722ed1' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.weight')"
                :value="props.dataSource?.content?.metrics?.COMPLIANCE_SCORE.weight"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }" />
            </div>
            <span class="mr-1">{{ t('reportPreview.evaluation.detail.averageScore') }}</span>
            <div ref="complianceScoreRef" class="quality-score-progress-bar flex-1"></div>
          </div>
        </div>

        <!-- Availability Score -->
        <div v-if="props.dataSource?.content?.metrics.AVAILABILITY_SCORE" class="quality-score-progress-item">
          <div class="progress-header">
            <span class="progress-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.AVAILABILITY_SCORE) }}</span>
          </div>
          <div class="flex items-center"> 
            <div class="quality-score-content inline-flex justify-around space-x-2 w-70">
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalCases')"
                :value="props.dataSource?.content?.metrics?.AVAILABILITY_SCORE.totalCases"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.totalScore')"
                :value="props.dataSource?.content?.metrics?.AVAILABILITY_SCORE.totalScore"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#722ed1' }" />
              <Statistic
                :title="t('reportPreview.evaluation.detail.weight')"
                :value="props.dataSource?.content?.metrics?.AVAILABILITY_SCORE.weight"
                suffix=""
                class="flex flex-col-reverse"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }" />
            </div>
            <span class="mr-1">{{ t('reportPreview.evaluation.detail.averageScore') }}</span>
            <div ref="availabilityScoreRef" class="quality-score-progress-bar flex-1"></div>
          </div>
        </div>
      </div>
    </div>
  </div>


</template>

<style scoped>
.metrics-charts-container {
  padding: 20px 0;
}

.metric-item {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 16px;
  background: #fff;
}

.metric-chart-wrapper {
  display: flex;
  align-items: center;
  gap: 30px;
}

.metric-chart {
  width: 300px;
  height: 130px;
  flex-shrink: 0;
}

.metric-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.info-value {
  font-weight: 600;
  color: #333;
}

.quality-scores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.quality-score-item {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 16px;
  background: #fff;
  text-align: center;
}

.quality-score-chart {
  width: 100%;
  height: 130px;
}

.quality-score-info {
  margin-top: 12px;
}

.score-value {
  font-size: 20px;
  font-weight: bold;
}

.quality-scores-progress-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.quality-score-progress-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-title {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.quality-score-progress-bar {
  width: 100%;
  height: 30px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.quality-score-progress-bar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 85%;
  height: 100%;
  border-radius: 4px;
  background-color: #D9D9D9;
}

@media print {
  .metric-chart,
  .quality-score-chart {
    page-break-inside: avoid;
  }
}
</style>