<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { Card, Tag, Statistic } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { throttle } from 'throttle-debounce';
import { kanban } from '@/api/tester';
import { useChartManagement } from './composables/useChartManagement';
import { EvaluationData } from './types';
import { EvaluationPurpose } from '@/enums/enums';
import { enumUtils } from '@xcan-angus/infra';


const { t } = useI18n();

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<{
  projectId?: string;
  onShow?: boolean;
  planId?: string;
  createdDateStart?: string;
  createdDateEnd?: string;
}>(), {
  projectId: undefined,
  onShow: false,
  planId: undefined,
  createdDateStart: undefined,
  createdDateEnd: undefined,
});

/**
 * Evaluation data
 */
const evaluationData = ref<EvaluationData>({
  PERFORMANCE_PASSED_RATE: {
    rate: 0,
    numerator: 0,
    denominator: 0,
    score: 0
  },
  FUNCTIONAL_PASSED_RATE: {
    rate: 0,
    numerator: 0,
    denominator: 0,
    score: 0
  },
  STABILITY_PASSED_RATE: {
    rate: 0,
    numerator: 0,
    denominator: 0,
    score: 0
  },
  COMPATIBILITY_SCORE: {
    score: 0
  },
  USABILITY_SCORE: {
    score: 0
  },
  MAINTAINABILITY_SCORE: {
    score: 0
  },
  SCALABILITY_SCORE: {
    score: 0
  },
  SECURITY_SCORE: {
    score: 0
  },
  COMPLIANCE_SCORE: {
    score: 0
  },
  AVAILABILITY_SCORE: {
    score: 0
  },
  
  overallScore: 0,
  scoreLevel: '',
  compatibilityScore: 0,
  usabilityScore: 0,
  maintainabilityScore: 0,
  extensibilityScore: 0,
  statistics: {
    totalEvaluations: 0,
    completedEvaluations: 0,
    averageScore: 0,
    highestScore: 0,
    lowestScore: 0
  }
});

/**
 * Chart container references
 */
const overallScoreRef = ref<HTMLElement>();
const requirementCompletionRef = ref<HTMLElement>();
const functionTestCoverageRef = ref<HTMLElement>();
const functionTestPassRateRef = ref<HTMLElement>();
const performanceTestPassRateRef = ref<HTMLElement>();
const stabilityTestPassRateRef = ref<HTMLElement>();
const compatibilityRef = ref<HTMLElement>();
const usabilityRef = ref<HTMLElement>();
const maintainabilityRef = ref<HTMLElement>();
const extensibilityRef = ref<HTMLElement>();
const qualityRadarRef = ref<HTMLElement>();
const statisticsBarRef = ref<HTMLElement>();
const complianceRef = ref<HTMLElement>();
const availabilityRef = ref<HTMLElement>();
const securityRef = ref<HTMLElement>();
/**
 * Initialize chart management
 */
const {
  initializeCharts,
  resizeAllCharts,
  updateCharts,
  disposeAllCharts
} = useChartManagement();

/**
 * Get score color based on score value
 */
const getScoreColor = (score: number) => {
  if (score >= 90) return '#52c41a';
  if (score >= 80) return '#1890ff';
  if (score >= 60) return '#faad14';
  return '#ff4d4f';
};

/**
 * Get score level text and color
 */
const getScoreLevel = (score: number) => {
  if (score >= 9) return { text: t('kanban.evaluation.overallScore.excellent'), color: 'success' };
  if (score >= 8) return { text: t('kanban.evaluation.overallScore.good'), color: 'processing' };
  if (score >= 6) return { text: t('kanban.evaluation.overallScore.pass'), color: 'warning' };
  return { text: t('kanban.evaluation.overallScore.needsImprovement'), color: 'error' };
};

/**
 * Load evaluation data from API
 */
const loadEvaluationData = async () => {
  if (!props.projectId) {
    return;
  }

  const [error, res] = await kanban.getEvaluationData({
    projectId: props.projectId as string,
    createdDateStart: props.createdDateStart as string,
    createdDateEnd: props.createdDateEnd as string,
    planId: props.planId as string,
  });

  if (error || !res?.data) {
    return;
  }

  const scroeArr = [
    +res.data.metrics.COMPATIBILITY_SCORE.score,
    +res.data.metrics.USABILITY_SCORE.score,
    +res.data.metrics.MAINTAINABILITY_SCORE.score,
    +res.data.metrics.SCALABILITY_SCORE.score,
    +res.data.metrics.SECURITY_SCORE.score,
    +res.data.metrics.PERFORMANCE_SCORE.score,
    +res.data.metrics.FUNCTIONAL_SCORE.score,
    +res.data.metrics.STABILITY_SCORE.score,
    +res.data.metrics.COMPLIANCE_SCORE.score,
    +res.data.metrics.AVAILABILITY_SCORE.score,
  ];
  const maxScore = Math.max(...scroeArr);
  const minScore = Math.min(...scroeArr);
  const averageScore = scroeArr.reduce((acc, curr) => acc + curr, 0) / scroeArr.length;
  evaluationData.value = {
    ...res.data,
    ...res.data.metrics,
    statistics: {
      total: res.data.totalCases,
      completed: res.data.completedCases,
      highestScore: maxScore,
      lowestScore: minScore,
      averageScore: Number(averageScore).toFixed(1)
    }
  };
  
  // Update charts with new data
  nextTick(() => {
    updateCharts(evaluationData.value);
  });
};

/**
 * Handle window resize with throttling
 */
const handleWindowResize = throttle(500, () => {
  if (!props.onShow) {
    return;
  }
  resizeAllCharts();
});

/**
 * Refresh data
 */
const refresh = async () => {
  await loadEvaluationData();
};

const shouldNotify = ref(false);

/**
 * Setup lifecycle hooks
 */
onMounted(async () => {
  // Initialize charts with DOM references
  const chartRefs: Record<string, HTMLElement> = {};

  if (overallScoreRef.value) chartRefs.overallScoreRef = overallScoreRef.value;
  if (requirementCompletionRef.value) chartRefs.requirementCompletionRef = requirementCompletionRef.value;
  if (functionTestCoverageRef.value) chartRefs.functionTestCoverageRef = functionTestCoverageRef.value;
  if (functionTestPassRateRef.value) chartRefs.functionTestPassRateRef = functionTestPassRateRef.value;
  if (performanceTestPassRateRef.value) chartRefs.performanceTestPassRateRef = performanceTestPassRateRef.value;
  if (stabilityTestPassRateRef.value) chartRefs.stabilityTestPassRateRef = stabilityTestPassRateRef.value;
  if (compatibilityRef.value) chartRefs.compatibilityRef = compatibilityRef.value;
  if (usabilityRef.value) chartRefs.usabilityRef = usabilityRef.value;
  if (maintainabilityRef.value) chartRefs.maintainabilityRef = maintainabilityRef.value;
  if (extensibilityRef.value) chartRefs.extensibilityRef = extensibilityRef.value;
  if (qualityRadarRef.value) chartRefs.qualityRadarRef = qualityRadarRef.value;
  if (statisticsBarRef.value) chartRefs.statisticsBarRef = statisticsBarRef.value;
  if (complianceRef.value) chartRefs.complianceRef = complianceRef.value;
  if (availabilityRef.value) chartRefs.availabilityRef = availabilityRef.value;
  if (securityRef.value) chartRefs.securityRef = securityRef.value;
  initializeCharts(chartRefs);

  // Watch for props changes and load data
  watch([
    () => props.projectId,
    () => props.planId,
    () => props.createdDateStart,
    () => props.createdDateEnd
  ], () => {
    if (!props.onShow) {
      shouldNotify.value = true;
      return;
    }
    if (props.projectId) {
      loadEvaluationData();
    }
  }, {
    immediate: true
  });

  // Watch for onShow changes
  watch(() => props.onShow, (newValue) => {
    if (newValue && shouldNotify.value) {
      shouldNotify.value = false;
      nextTick(() => {
        resizeAllCharts();
        if (props.projectId) {
          loadEvaluationData();
        }
      });
    }
  });

  // Add window resize event listener
  window.addEventListener('resize', handleWindowResize);
});

/**
 * Cleanup on unmount
 */
onBeforeUnmount(() => {
  disposeAllCharts();
  window.removeEventListener('resize', handleWindowResize);
});

defineExpose({
  refresh,
  handleWindowResize
});
</script>

<template>
  <div class="evaluation-dashboard py-2 text-3 space-y-2">
    <!-- Row 0: Statistics Summary with Bar Chart -->
    <Card class="statistics-card" :bordered="false">
      <template #title>
        <div class="section-title flex items-center">
          <Icon icon="icon-zonglan" class="mr-2 text-blue-600 text-lg" />
          <span class="text-base font-semibold">{{ t('kanban.evaluation.statistics.title') }}</span>
        </div>
      </template>
      <div class="flex space-x-4">
        <div class="flex-1 flex justify-around items-center">
          <div class="statistic-item">
            <Statistic
              :title="t('kanban.evaluation.statistics.totalEvaluations')"
              :value="evaluationData.statistics.total"
              :suffix="t('kanban.evaluation.statistics.times')"
              class="flex flex-col-reverse"
              :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }" />
          </div>
          <div class="statistic-item">
            <Statistic
              :title="t('kanban.evaluation.statistics.averageScore')"
              :value="evaluationData.statistics.averageScore"
              class="flex flex-col-reverse"
              :precision="1"
              :suffix="t('kanban.evaluation.statistics.points')"
              :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }" />
          </div>
          <div class="statistic-item">
            <Statistic
              :title="t('kanban.evaluation.statistics.highestScore')"
              :value="evaluationData.statistics.highestScore"
              class="flex flex-col-reverse"
              :precision="1"
              :suffix="t('kanban.evaluation.statistics.points')"
              :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }" />
          </div>
          <div class="statistic-item">
            <Statistic
              :title="t('kanban.evaluation.statistics.lowestScore')"
              :value="evaluationData.statistics.lowestScore"
              class="flex flex-col-reverse"
              :precision="1"
              :suffix="t('kanban.evaluation.statistics.points')"
              :value-style="{ fontSize: '32px', fontWeight: 'bold', color: '#ff4d4f' }" />
          </div>
        </div>
        <div class="statistics-chart-container">
          <div ref="statisticsBarRef" class="chart-container"></div>
        </div>
      </div>
    </Card>

    <!-- Row 1: Overall Score and Requirement Completion -->
    <div class="flex space-x-2 ">
      <!-- Overall Score Section -->
      <Card class="flex-1 h-full" :bordered="false">
        <template #title>
          <div class="section-title">{{ t('kanban.evaluation.overallScore.title') }}</div>
        </template>
        <div class="overall-score-container">
          <div ref="overallScoreRef" style="height: 155px;" class="w-full flex-1 min-w-0"></div>
          <div class="score-info">
            <Tag :color="getScoreLevel(evaluationData.overallScore).color" class="text-base px-3 py-1 mb-2">
              {{ getScoreLevel(evaluationData.overallScore).text }}
            </Tag>
            <div class="text-sm text-slate-500">{{ t('kanban.evaluation.overallScore.overallScoreLabel') }}：{{ evaluationData.overallScore ? Number(evaluationData.overallScore).toFixed(2) : '0.0' }} {{ t('kanban.evaluation.statistics.points') }}</div>
          </div>
        </div>
      </Card>

      <Card class="flex-1 h-full quality-radar-card" :bordered="false">
        <template #title>
          <div class="section-title">{{ t('kanban.evaluation.qualityRadar.title') }}</div>
        </template>
        <div ref="qualityRadarRef" class="quality-radar-chart"></div>
      </Card>

      <Card class="flex-2 h-full" :bordered="false">
        <template #title>
          <div class="section-title">{{ t('kanban.evaluation.testPassRate.title') }}</div>
        </template>
        <div class="test-pass-rates-container"> 
          <div class="pass-rate-charts">
            <div class="pass-rate-item">
              <div ref="functionTestPassRateRef" class="chart-container-small"></div>
              <div class="pass-rate-info">
                <div class="pass-rate-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.FUNCTIONAL_SCORE) }}</div>
                  <div class="pass-rate-value" :style="{ color: getScoreColor(evaluationData.FUNCTIONAL_SCORE?.rate) }">
                  {{ evaluationData.FUNCTIONAL_SCORE?.rate }}%
                </div>
                <div class="pass-rate-detail">
                  {{ evaluationData.FUNCTIONAL_SCORE?.numerator }}/{{ evaluationData.FUNCTIONAL_SCORE?.denominator }}
                </div>
              </div>
            </div>
            <div class="pass-rate-item">
              <div ref="performanceTestPassRateRef" class="chart-container-small"></div>
              <div class="pass-rate-info">
                <div class="pass-rate-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.PERFORMANCE_SCORE) }}</div>
                <div class="pass-rate-value" :style="{ color: getScoreColor(evaluationData.PERFORMANCE_SCORE?.rate) }">
                      {{ evaluationData.PERFORMANCE_SCORE?.rate }}%
                </div>
                <div class="pass-rate-detail">
                  {{ evaluationData.PERFORMANCE_SCORE?.numerator }}/{{ evaluationData.PERFORMANCE_SCORE?.denominator }}
                </div>
              </div>
            </div>
            <div class="pass-rate-item">
              <div ref="stabilityTestPassRateRef" class="chart-container-small"></div>
              <div class="pass-rate-info">
                <div class="pass-rate-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.STABILITY_SCORE) }}</div>
                <div class="pass-rate-value" :style="{ color: getScoreColor(evaluationData.STABILITY_SCORE?.rate) }">
                  {{ evaluationData.STABILITY_SCORE?.rate }}%
                </div>
                <div class="pass-rate-detail">
                  {{ evaluationData.STABILITY_SCORE?.numerator }}/{{ evaluationData.STABILITY_SCORE?.denominator }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </Card>

    </div>

    <!-- Row 4: Quality Scores Gauges -->
    <div class="flex space-x-2 h-64">
      <Card class="flex-1 h-full quality-score-card" :bordered="false">
        <template #title>
          <div class="section-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.COMPATIBILITY_SCORE) }}</div>
        </template>
        <div ref="compatibilityRef" class="chart-container-gauge"></div>
        <div class="quality-score-tag">
          <Tag :color="getScoreLevel(evaluationData.COMPATIBILITY_SCORE?.score).color">
            {{ getScoreLevel(evaluationData.COMPATIBILITY_SCORE?.score).text }}
          </Tag>
        </div>
      </Card>

      <Card class="flex-1 h-full quality-score-card" :bordered="false">
        <template #title>
          <div class="section-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.USABILITY_SCORE) }}</div>
        </template>
        <div ref="usabilityRef" class="chart-container-gauge"></div>
        <div class="quality-score-tag">
          <Tag :color="getScoreLevel(evaluationData.USABILITY_SCORE?.score).color">
            {{ getScoreLevel(evaluationData.USABILITY_SCORE?.score).text }}
          </Tag>
        </div>
      </Card>

      <Card class="flex-1 h-full quality-score-card" :bordered="false">
        <template #title>
          <div class="section-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.MAINTAINABILITY_SCORE) }}</div>
        </template>
        <div ref="maintainabilityRef" class="chart-container-gauge"></div>
        <div class="quality-score-tag">
          <Tag :color="getScoreLevel(evaluationData.MAINTAINABILITY_SCORE?.score).color">
            {{ getScoreLevel(evaluationData.MAINTAINABILITY_SCORE?.score).text }}
          </Tag>
        </div>
      </Card>

      <Card class="flex-1 h-full quality-score-card" :bordered="false">
        <template #title>
          <div class="section-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.SCALABILITY_SCORE) }}</div>
        </template>
        <div ref="extensibilityRef" class="chart-container-gauge"></div>
        <div class="quality-score-tag">
          <Tag :color="getScoreLevel(evaluationData.SCALABILITY_SCORE?.score).color">
            {{ getScoreLevel(evaluationData.SCALABILITY_SCORE?.score).text }}
          </Tag>
        </div>
      </Card>

    </div>

    <div class="flex space-x-2 h-64">
      <!-- Availability Score -->
      <Card class="flex-1 h-full quality-score-card" :bordered="false">
        <template #title>
          <div class="section-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.AVAILABILITY_SCORE) }}</div>
        </template>
        <div ref="availabilityRef" class="chart-container-gauge"></div>
        <div class="quality-score-tag">
          <Tag :color="getScoreLevel(evaluationData.AVAILABILITY_SCORE?.score).color">
            {{ getScoreLevel(evaluationData.AVAILABILITY_SCORE?.score).text }}
          </Tag>
        </div>
      </Card>

      <!-- Security Score -->
      <Card class="flex-1 h-full quality-score-card" :bordered="false"> 
        <template #title>
          <div class="section-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.SECURITY_SCORE) }}</div>
        </template>
        <div ref="securityRef" class="chart-container-gauge"></div>
        <div class="quality-score-tag">
          <Tag :color="getScoreLevel(evaluationData.SECURITY_SCORE?.score).color">
            {{ getScoreLevel(evaluationData.SECURITY_SCORE?.score).text }}
          </Tag>
        </div>
      </Card>

      <!-- Compliance Score -->
      <Card class="flex-1 h-full quality-score-card" :bordered="false"> 
        <template #title>
          <div class="section-title">{{ enumUtils.getEnumDescription(EvaluationPurpose, EvaluationPurpose.COMPLIANCE_SCORE) }}</div>
        </template>
        <div ref="complianceRef" class="chart-container-gauge"></div>   
        <div class="quality-score-tag">
          <Tag :color="getScoreLevel(evaluationData.COMPLIANCE_SCORE?.score).color">
            {{ getScoreLevel(evaluationData.COMPLIANCE_SCORE?.score).text }}
          </Tag>
        </div>
      </Card>
      <div class="flex-1 h-full quality-score-card" :bordered="false"></div>
    </div>
  </div>
</template>
<style scoped>
/* Dashboard container */
.evaluation-dashboard {
  min-height: 100vh;
}

/* Card styling */
:deep(.ant-card) {
  border-width: 1px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  background: #fff;
}

:deep(.ant-card):hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 0px 10px;
}

:deep(.ant-card-body) {
  padding: 20px;
}

/* Section title styling */
.section-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

/* Statistics card styling */
.statistics-card {
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
}

.statistics-card :deep(.ant-card-head) {
  border-bottom-color: #f0f0f0;
}

.statistics-card .section-title {
  /* color: #fff; */
}

.statistic-item {
  flex: 1;
  text-align: center;
  padding: 0 8px;
}

.statistic-item :deep(.ant-statistic-title) {
  font-size: 13px;
  /* color: rgba(255, 255, 255, 0.8); */
  margin-bottom: 4px;
  font-weight: 500;
}

.statistic-item :deep(.ant-statistic-content) {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}

.statistic-item :deep(.ant-statistic-content-suffix) {
  font-size: 16px;
  margin-left: 4px;
  /* color: rgba(255, 255, 255, 0.9); */
}

.statistics-chart-container {
  width: 400px;
  height: 150px;
}

/* Chart containers */
.chart-container {
  width: 100%;
  height: 100%;
  min-height: 130px;
}

.chart-container-small {
  width: 100%;
  height: 150px;
}

.chart-container-gauge {
  width: 100%;
  height: 120px;
}

/* Overall score container */
.overall-score-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  position: relative;
}

.score-info {
  margin-top: 20px;
  text-align: center;
}

/* Requirement completion container */
.requirement-completion-container {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 100%;
}

.completion-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
}

.detail-item {
  text-align: center;
}

.detail-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.detail-label {
  font-size: 12px;
  color: #8c8c8c;
}

/* Test coverage container */
.test-coverage-container {
  display: flex;
  /* flex-direction: column; */
  align-items: center;
  justify-content: center;
  height: 100%;
}

.coverage-info {
  margin-top: 20px;
  text-align: center;
}

/* Test pass rates container */
.test-pass-rates-container {
  height: 100%;
}

.pass-rate-charts {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 100%;
}

.pass-rate-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.pass-rate-info {
  @apply items-center flex-col space-x-2;

  margin-top: 12px;
  text-align: center;
  display: flex;
  
  
}

.pass-rate-title {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.pass-rate-value {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}

.pass-rate-detail {
  font-size: 11px;
  color: #bfbfbf;
}

/* Quality radar chart */
.quality-radar-card {
  height: 330px;
}

.quality-radar-chart {
  width: 100%;
  height: 220px;
}

/* Quality score cards */
.quality-score-card {
  display: flex;
  flex-direction: column;
}

.quality-score-tag {
  text-align: center;
  margin-top: 12px;
}

/* Flex utilities */
.flex-2 {
  flex: 2;
}

/* Height utilities */
.h-72 {
  height: 280px;
}

.h-64 {
  height: 240px;
}
</style>
