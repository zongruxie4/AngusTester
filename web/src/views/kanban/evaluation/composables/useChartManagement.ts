import { nextTick, ref } from 'vue';
import eCharts from '@/utils/echarts';
import { ChartConfig, EvaluationData } from '../types';
import { i18n } from '@xcan-angus/infra';
import {
  createScorePieConfig,
  createCompletionPieConfig,
  createSimpleProgressPieConfig,
  createQualityRadarConfig,
  createStatisticsBarConfig
} from './useChartConfigs';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

/**
 * Chart management composable for evaluation dashboard
 *
 * @returns Object containing chart instances and management methods
 */
export function useChartManagement() {
  /** Overall score pie chart instance */
  let overallScoreChart: eCharts.ECharts | null = null;

  /** Requirement completion pie chart instance */
  let requirementCompletionChart: eCharts.ECharts | null = null;

  /** Function test coverage pie chart instance */
  let functionTestCoverageChart: eCharts.ECharts | null = null;

  /** Function test pass rate pie chart instance */
  let functionTestPassRateChart: eCharts.ECharts | null = null;

  /** Performance test pass rate pie chart instance */
  let performanceTestPassRateChart: eCharts.ECharts | null = null;

  /** Stability test pass rate pie chart instance */
  let stabilityTestPassRateChart: eCharts.ECharts | null = null;

  /** Compatibility pie chart instance */
  let compatibilityChart: eCharts.ECharts | null = null;

  /** Usability pie chart instance */
  let usabilityChart: eCharts.ECharts | null = null;

  /** Maintainability pie chart instance */
  let maintainabilityChart: eCharts.ECharts | null = null;

  /** Extensibility pie chart instance */
  let extensibilityChart: eCharts.ECharts | null = null;

  /** Quality radar chart instance */
  let qualityRadarChart: eCharts.ECharts | null = null;

  /** Statistics bar chart instance */
  let statisticsBarChart: eCharts.ECharts | null = null;

  /** Compliance score pie chart instance */
  let complianceChart: eCharts.ECharts | null = null;

  /** Availability score pie chart instance */
  let availabilityChart: eCharts.ECharts | null = null;

  let securityChart: eCharts.ECharts | null = null;

  /** Chart configuration references */
  const overallScoreConfig = ref<ChartConfig>({});
  const requirementCompletionConfig = ref<ChartConfig>({});
  const functionTestCoverageConfig = ref<ChartConfig>({});
  const functionTestPassRateConfig = ref<ChartConfig>({});
  const performanceTestPassRateConfig = ref<ChartConfig>({});
  const stabilityTestPassRateConfig = ref<ChartConfig>({});
  const compatibilityConfig = ref<ChartConfig>({});
  const usabilityConfig = ref<ChartConfig>({});
  const maintainabilityConfig = ref<ChartConfig>({});
  const extensibilityConfig = ref<ChartConfig>({});
  const qualityRadarConfig = ref<ChartConfig>({});
  const statisticsBarConfig = ref<ChartConfig>({});
  const complianceConfig = ref<ChartConfig>({});
  const availabilityConfig = ref<ChartConfig>({});
  const securityConfig = ref<ChartConfig>({});
  /**
   * Initializes all charts with their configurations
   *
   * @param chartRefs - Object containing DOM references for chart containers
   */
  const initializeCharts = (chartRefs: Record<string, HTMLElement>) => {
    // Initialize overall score pie chart
    if (chartRefs.overallScoreRef) {
      overallScoreChart = eCharts.init(chartRefs.overallScoreRef);
      overallScoreChart.setOption(overallScoreConfig.value);
    }

    // Initialize requirement completion pie chart
    if (chartRefs.requirementCompletionRef) {
      requirementCompletionChart = eCharts.init(chartRefs.requirementCompletionRef);
      requirementCompletionChart.setOption(requirementCompletionConfig.value);
    }

    // Initialize function test coverage pie chart
    if (chartRefs.functionTestCoverageRef) {
      functionTestCoverageChart = eCharts.init(chartRefs.functionTestCoverageRef);
      functionTestCoverageChart.setOption(functionTestCoverageConfig.value);
    }

    // Initialize function test pass rate pie chart
    if (chartRefs.functionTestPassRateRef) {
      functionTestPassRateChart = eCharts.init(chartRefs.functionTestPassRateRef);
      functionTestPassRateChart.setOption(functionTestPassRateConfig.value);
    }

    // Initialize performance test pass rate pie chart
    if (chartRefs.performanceTestPassRateRef) {
      performanceTestPassRateChart = eCharts.init(chartRefs.performanceTestPassRateRef);
      performanceTestPassRateChart.setOption(performanceTestPassRateConfig.value);
    }

    // Initialize stability test pass rate pie chart
    if (chartRefs.stabilityTestPassRateRef) {
      stabilityTestPassRateChart = eCharts.init(chartRefs.stabilityTestPassRateRef);
      stabilityTestPassRateChart.setOption(stabilityTestPassRateConfig.value);
    }

    // Initialize quality score pie charts
    if (chartRefs.compatibilityRef) {
      compatibilityChart = eCharts.init(chartRefs.compatibilityRef);
      compatibilityChart.setOption(compatibilityConfig.value);
    }

    if (chartRefs.usabilityRef) {
      usabilityChart = eCharts.init(chartRefs.usabilityRef);
      usabilityChart.setOption(usabilityConfig.value);
    }

    if (chartRefs.maintainabilityRef) {
      maintainabilityChart = eCharts.init(chartRefs.maintainabilityRef);
      maintainabilityChart.setOption(maintainabilityConfig.value);
    }

    if (chartRefs.extensibilityRef) {
      extensibilityChart = eCharts.init(chartRefs.extensibilityRef);
      extensibilityChart.setOption(extensibilityConfig.value);
    }

    // Initialize quality radar chart
    if (chartRefs.qualityRadarRef) {
      qualityRadarChart = eCharts.init(chartRefs.qualityRadarRef);
      qualityRadarChart.setOption(qualityRadarConfig.value);
    }

    // Initialize statistics bar chart
    if (chartRefs.statisticsBarRef) {
      statisticsBarChart = eCharts.init(chartRefs.statisticsBarRef);
      statisticsBarChart.setOption(statisticsBarConfig.value);
    }

    // Initialize compliance score pie chart
    if (chartRefs.complianceRef) {
      complianceChart = eCharts.init(chartRefs.complianceRef);
      complianceChart.setOption(complianceConfig.value);
    }
    
    // Initialize availability score pie chart
    if (chartRefs.availabilityRef) {
      availabilityChart = eCharts.init(chartRefs.availabilityRef);
      availabilityChart.setOption(availabilityConfig.value);
    }

    if (chartRefs.securityRef) {
      securityChart = eCharts.init(chartRefs.securityRef);
      securityChart.setOption(securityConfig.value);
    }
  };

  /**
   * Resizes all charts to fit their containers
   */
  const resizeAllCharts = () => {
    const charts = [
      overallScoreChart,
      requirementCompletionChart,
      functionTestCoverageChart,
      functionTestPassRateChart,
      performanceTestPassRateChart,
      stabilityTestPassRateChart,
      compatibilityChart,
      usabilityChart,
      maintainabilityChart,
      extensibilityChart,
      qualityRadarChart,
      statisticsBarChart,
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
   * Updates all charts with new evaluation data
   *
   * @param data - Evaluation data from API
   */
  const updateCharts = (data: EvaluationData) => {
    // Update overall score pie chart
    if (overallScoreChart && data.overallScore !== undefined) {
      overallScoreConfig.value = createScorePieConfig(data.overallScore, t('kanban.evaluation.chartLabels.overallScore'));
      overallScoreChart.setOption(overallScoreConfig.value, true);
    }

    // Update requirement completion pie chart
    if (requirementCompletionChart && data.PERFORMANCE_SCORE) {
      const { rate, numerator: completed, denominator: total } = data.PERFORMANCE_SCORE;
      requirementCompletionConfig.value = createCompletionPieConfig(rate, completed, total, t('kanban.evaluation.chartLabels.requirementCompletionRate'));
      requirementCompletionChart.setOption(requirementCompletionConfig.value, true);
    }

    // // Update test pass rate pie charts
    if (functionTestPassRateChart && data.FUNCTIONAL_SCORE) {
      const { rate } = data.FUNCTIONAL_SCORE;
      functionTestPassRateConfig.value = createSimpleProgressPieConfig(rate);
      functionTestPassRateChart.setOption(functionTestPassRateConfig.value, true);
    }

    if (performanceTestPassRateChart && data.PERFORMANCE_SCORE) {
      const { rate } = data.PERFORMANCE_SCORE;
      performanceTestPassRateConfig.value = createSimpleProgressPieConfig(rate);
      performanceTestPassRateChart.setOption(performanceTestPassRateConfig.value, true);
    }

    if (stabilityTestPassRateChart && data.STABILITY_SCORE) {
      const { rate } = data.STABILITY_SCORE;
      stabilityTestPassRateConfig.value = createSimpleProgressPieConfig(rate);
      stabilityTestPassRateChart.setOption(stabilityTestPassRateConfig.value, true);
    }

    // // Update quality score pie charts
    if (compatibilityChart && data.COMPATIBILITY_SCORE !== undefined) {
      compatibilityConfig.value = createScorePieConfig(+data.COMPATIBILITY_SCORE.score, t('kanban.evaluation.qualityRadar.compatibility'));
      compatibilityChart.setOption(compatibilityConfig.value, true);
    }

    if (usabilityChart && data.USABILITY_SCORE !== undefined) {
      usabilityConfig.value = createScorePieConfig(+data.USABILITY_SCORE.score, t('kanban.evaluation.qualityRadar.usability'));
      usabilityChart.setOption(usabilityConfig.value, true);
    }

    if (maintainabilityChart && data.MAINTAINABILITY_SCORE !== undefined) {
      maintainabilityConfig.value = createScorePieConfig(+data.MAINTAINABILITY_SCORE.score, t('kanban.evaluation.qualityRadar.maintainability'));
      maintainabilityChart.setOption(maintainabilityConfig.value, true);
    }

    if (extensibilityChart && data.SCALABILITY_SCORE !== undefined) {
      extensibilityConfig.value = createScorePieConfig(+data.SCALABILITY_SCORE.score, t('kanban.evaluation.qualityRadar.scalability'));
      extensibilityChart.setOption(extensibilityConfig.value, true);
    }

    
    if (complianceChart && data.COMPLIANCE_SCORE !== undefined) {
      complianceConfig.value = createScorePieConfig(+data.COMPLIANCE_SCORE.score, t('kanban.evaluation.qualityRadar.compliance'));
      complianceChart.setOption(complianceConfig.value, true);
    }

    // Update availability score pie chart
    if (availabilityChart && data.AVAILABILITY_SCORE !== undefined) {
      availabilityConfig.value = createScorePieConfig(+data.AVAILABILITY_SCORE.score, t('kanban.evaluation.qualityRadar.availability'));
      availabilityChart.setOption(availabilityConfig.value, true);
    }

    if (securityChart && data.SECURITY_SCORE !== undefined) {
      securityConfig.value = createScorePieConfig(+data.SECURITY_SCORE.score, t('kanban.evaluation.qualityRadar.security'));
      securityChart.setOption(securityConfig.value, true);
    }

      if (qualityRadarChart && data.COMPATIBILITY_SCORE !== undefined) {
      qualityRadarConfig.value = createQualityRadarConfig(
        +data.COMPATIBILITY_SCORE.score,
        +data.USABILITY_SCORE.score,
        +data.MAINTAINABILITY_SCORE.score,
        +data.SCALABILITY_SCORE.score,
        +data.SECURITY_SCORE.score,
        +data.COMPLIANCE_SCORE.score,
        +data.AVAILABILITY_SCORE.score
      );
      qualityRadarChart.setOption(qualityRadarConfig.value, true);
    }

    // // Update quality radar chart  

    // // Update statistics bar chart
    if (statisticsBarChart && data.statistics) {
      const { statistics } = data;
      statisticsBarConfig.value = createStatisticsBarConfig(
        [
          t('kanban.evaluation.statisticsBar.averageScore'),
          t('kanban.evaluation.statisticsBar.highestScore'),
          t('kanban.evaluation.statisticsBar.lowestScore')
        ],
        [
          Math.round(statistics.averageScore),
          Math.round(statistics.highestScore),
          Math.round(statistics.lowestScore)
        ]
      );
      statisticsBarChart.setOption(statisticsBarConfig.value, true);
    }
  };

  /**
   * Disposes all chart instances
   */
  const disposeAllCharts = () => {
    const charts = [
      overallScoreChart,
      requirementCompletionChart,
      functionTestCoverageChart,
      functionTestPassRateChart,
      performanceTestPassRateChart,
      stabilityTestPassRateChart,
      compatibilityChart,
      usabilityChart,
      maintainabilityChart,
      extensibilityChart,
      qualityRadarChart,
      statisticsBarChart,
      complianceChart,
      availabilityChart,
      securityChart
    ];

    charts.forEach(chart => {
      if (chart) {
        chart.dispose();
      }
    });
  };

  return {
    // Chart configurations
    overallScoreConfig,
    requirementCompletionConfig,
    functionTestCoverageConfig,
    functionTestPassRateConfig,
    performanceTestPassRateConfig,
    stabilityTestPassRateConfig,
    compatibilityConfig,
    usabilityConfig,
    maintainabilityConfig,
    extensibilityConfig,
    qualityRadarConfig,
    statisticsBarConfig,
    complianceConfig,
    availabilityConfig,

    // Methods
    initializeCharts,
    resizeAllCharts,
    updateCharts,
    disposeAllCharts
  };
}
