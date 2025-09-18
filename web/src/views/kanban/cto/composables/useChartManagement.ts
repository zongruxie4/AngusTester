import { nextTick, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';
import { ChartConfig } from '../types';
import {
  createApiTestConfig,
  createBacklogTaskConfig,
  createBacklogWorkloadConfig,
  createFailureAssessmentConfig,
  createLeadTimeConfig,
  createOverdueAssessmentConfig,
  createProgressChartConfig,
  createRecentCompletedWorkloadConfig,
  createRecentCompletionRateConfig,
  createRecentOverdueRateConfig,
  createRecentSavingRateConfig,
  createReviewStatusConfig,
  createTaskStatusConfig,
  createTaskTypeConfig,
  createTestStatusConfig,
  createUnplannedWorkConfig,
  createUnplannedWorkloadConfig
} from './useChartConfigs';

/**
 * Chart management composable
 *
 * @returns Object containing chart instances and management methods
 */
export function useChartManagement () {
  const { t } = useI18n();
  /** Progress chart instance */
  let progressChart: eCharts.ECharts;

  /** Recent completion rate chart instance */
  let recentCompletionRateChart: eCharts.ECharts;

  /** Recent overdue rate chart instance */
  let recentOverdueRateChart: eCharts.ECharts;

  /** Recent completed workload chart instance */
  let recentCompletedWorkloadChart: eCharts.ECharts;

  /** Recent saving rate chart instance */
  let recentSavingRateChart: eCharts.ECharts;

  /** Backlog task chart instance */
  let backlogTaskChart: eCharts.ECharts;

  /** Backlog workload chart instance */
  let backlogWorkloadChart: eCharts.ECharts;

  /** Overdue task chart instance */
  let overdueTaskChart: eCharts.ECharts;

  /** Unplanned task number chart instance */
  let unplannedTaskNumberChart: eCharts.ECharts;

  /** Unplanned workload chart instance */
  let unplannedWorkloadChart: eCharts.ECharts;

  /** Task type chart instance */
  let taskTypeChart: eCharts.ECharts;

  /** Task status chart instance */
  let taskStatusChart: eCharts.ECharts;

  /** Lead time chart instance */
  let leadTimeChart: eCharts.ECharts;

  /** Critical failure chart instance */
  let criticalFailureChart: eCharts.ECharts;

  /** Major failure chart instance */
  let majorFailureChart: eCharts.ECharts;

  /** Minor failure chart instance */
  let minorFailureChart: eCharts.ECharts;

  /** Trivial failure chart instance */
  let trivialFailureChart: eCharts.ECharts;

  /** API open test chart instance */
  let apiOpenTestChart: eCharts.ECharts;

  /** API success test chart instance */
  let apiSuccessTestChart: eCharts.ECharts;

  /** Scenario open test chart instance */
  let scenarioOpenTestChart: eCharts.ECharts;

  /** Scenario success test chart instance */
  let scenarioSuccessTestChart: eCharts.ECharts;

  /** Test status chart instance */
  let testStatusChart: eCharts.ECharts;

  /** Review status chart instance */
  let reviewStatusChart: eCharts.ECharts;

  /** Chart configuration references */
  const progressChartConfig = ref<ChartConfig>(createProgressChartConfig());
  const recentCompletionRateConfig = ref<ChartConfig>(createRecentCompletionRateConfig());
  const recentOverdueRateConfig = ref<ChartConfig>(createRecentOverdueRateConfig());
  const recentCompletedWorkloadConfig = ref<ChartConfig>(createRecentCompletedWorkloadConfig());
  const recentSavingRateConfig = ref<ChartConfig>(createRecentSavingRateConfig());
  const backlogTaskConfig = ref<ChartConfig>(createBacklogTaskConfig());
  const backlogWorkloadConfig = ref<ChartConfig>(createBacklogWorkloadConfig());
  const overdueTaskConfig = ref<ChartConfig>(createOverdueAssessmentConfig());
  const unplannedTaskNumberConfig = ref<ChartConfig>(createUnplannedWorkConfig());
  const unplannedWorkloadConfig = ref<ChartConfig>(createUnplannedWorkloadConfig());
  const taskTypeConfig = ref<ChartConfig>(createTaskTypeConfig());
  const taskStatusConfig = ref<ChartConfig>(createTaskStatusConfig());
  const leadTimeConfig = ref<ChartConfig>(createLeadTimeConfig());
  const criticalFailureConfig = ref<ChartConfig>(createFailureAssessmentConfig(t('kanban.cto.failureAssessment.criticalFailureRatio'), 'rgba(245, 34, 45, 1)'));
  const majorFailureConfig = ref<ChartConfig>(createFailureAssessmentConfig(t('kanban.cto.failureAssessment.majorFailureRatio'), 'gold'));
  const minorFailureConfig = ref<ChartConfig>(createFailureAssessmentConfig(t('kanban.cto.failureAssessment.minorFailureRatio'), 'rgba(255, 165, 43, 1)'));
  const trivialFailureConfig = ref<ChartConfig>(createFailureAssessmentConfig(t('kanban.cto.failureAssessment.trivialFailureRatio'), 'rgba(136, 185, 242, 1)'));
  const apiOpenTestConfig = ref<ChartConfig>(createApiTestConfig(t('kanban.cto.apiTest.enabledTestRatio')));
  const apiSuccessTestConfig = ref<ChartConfig>(createApiTestConfig(t('kanban.cto.apiTest.passedTestRatio')));
  const scenarioOpenTestConfig = ref<ChartConfig>(createApiTestConfig(t('kanban.cto.scenarioTest.enabledTestRatio')));
  const scenarioSuccessTestConfig = ref<ChartConfig>(createApiTestConfig(t('kanban.cto.scenarioTest.passedTestRatio')));
  const testStatusConfig = ref<ChartConfig>(createTestStatusConfig());
  const reviewStatusConfig = ref<ChartConfig>(createReviewStatusConfig());

  /**
   * Initializes all charts with their configurations
   *
   * @param chartRefs - Object containing DOM references for chart containers
   */
  const initializeCharts = (chartRefs: Record<string, HTMLElement>) => {
    // Initialize progress chart
    if (chartRefs.progressRef) {
      progressChart = eCharts.init(chartRefs.progressRef);
      progressChart.setOption(progressChartConfig.value);
    }

    // Initialize recent completion rate chart
    if (chartRefs.recentCompleteRef) {
      recentCompletionRateChart = eCharts.init(chartRefs.recentCompleteRef);
      recentCompletionRateChart.setOption(recentCompletionRateConfig.value);
    }

    // Initialize recent overdue rate chart
    if (chartRefs.recentOverdueRef) {
      recentOverdueRateChart = eCharts.init(chartRefs.recentOverdueRef);
      recentOverdueRateChart.setOption(recentOverdueRateConfig.value);
    }

    // Initialize recent completed workload chart
    if (chartRefs.recentCompletedWorkloadRef) {
      recentCompletedWorkloadChart = eCharts.init(chartRefs.recentCompletedWorkloadRef);
      recentCompletedWorkloadChart.setOption(recentCompletedWorkloadConfig.value);
    }

    // Initialize recent saving rate chart
    if (chartRefs.recentSavingRateRef) {
      recentSavingRateChart = eCharts.init(chartRefs.recentSavingRateRef);
      recentSavingRateChart.setOption(recentSavingRateConfig.value);
    }

    // Initialize backlog task chart
    if (chartRefs.blockTaskRef) {
      backlogTaskChart = eCharts.init(chartRefs.blockTaskRef);
      backlogTaskChart.setOption(backlogTaskConfig.value);
    }

    // Initialize backlog workload chart
    if (chartRefs.blockTaskWorkloadRef) {
      backlogWorkloadChart = eCharts.init(chartRefs.blockTaskWorkloadRef);
      backlogWorkloadChart.setOption(backlogWorkloadConfig.value);
    }

    // Initialize overdue task chart
    if (chartRefs.overdueTaskRef) {
      overdueTaskChart = eCharts.init(chartRefs.overdueTaskRef);
      overdueTaskChart.setOption(overdueTaskConfig.value);
    }

    // Initialize unplanned task number chart
    if (chartRefs.unplannedTaskNumRef) {
      unplannedTaskNumberChart = eCharts.init(chartRefs.unplannedTaskNumRef);
      unplannedTaskNumberChart.setOption(unplannedTaskNumberConfig.value);
    }

    // Initialize unplanned workload chart
    if (chartRefs.unplannedWorkloadRef) {
      unplannedWorkloadChart = eCharts.init(chartRefs.unplannedWorkloadRef);
      unplannedWorkloadChart.setOption(unplannedWorkloadConfig.value);
    }

    // Initialize task type chart
    if (chartRefs.taskTypeRef) {
      taskTypeChart = eCharts.init(chartRefs.taskTypeRef);
      taskTypeChart.setOption(taskTypeConfig.value);
    }

    // Initialize task status chart
    if (chartRefs.taskStatusRef) {
      taskStatusChart = eCharts.init(chartRefs.taskStatusRef);
      taskStatusChart.setOption(taskStatusConfig.value);
    }

    // Initialize lead time chart
    if (chartRefs.leadTimeRef) {
      leadTimeChart = eCharts.init(chartRefs.leadTimeRef);
      leadTimeChart.setOption(leadTimeConfig.value);
    }

    // Initialize critical failure chart
    if (chartRefs.criticalFailureRef) {
      criticalFailureChart = eCharts.init(chartRefs.criticalFailureRef);
      criticalFailureChart.setOption(criticalFailureConfig.value);
    }

    // Initialize major failure chart
    if (chartRefs.majorFailureRef) {
      majorFailureChart = eCharts.init(chartRefs.majorFailureRef);
      majorFailureChart.setOption(majorFailureConfig.value);
    }

    // Initialize minor failure chart
    if (chartRefs.minorFailureRef) {
      minorFailureChart = eCharts.init(chartRefs.minorFailureRef);
      minorFailureChart.setOption(minorFailureConfig.value);
    }

    // Initialize trivial failure chart
    if (chartRefs.trivialFailureRef) {
      trivialFailureChart = eCharts.init(chartRefs.trivialFailureRef);
      trivialFailureChart.setOption(trivialFailureConfig.value);
    }

    // Initialize API open test chart
    if (chartRefs.apiOpenTestRef) {
      apiOpenTestChart = eCharts.init(chartRefs.apiOpenTestRef);
      apiOpenTestChart.setOption(apiOpenTestConfig.value);
    }

    // Initialize API success test chart
    if (chartRefs.apiSuccessTestRef) {
      apiSuccessTestChart = eCharts.init(chartRefs.apiSuccessTestRef);
      apiSuccessTestChart.setOption(apiSuccessTestConfig.value);
    }

    // Initialize scenario open test chart
    if (chartRefs.scenarioOpenTestRef) {
      scenarioOpenTestChart = eCharts.init(chartRefs.scenarioOpenTestRef);
      scenarioOpenTestChart.setOption(scenarioOpenTestConfig.value);
    }

    // Initialize scenario success test chart
    if (chartRefs.scenarioSuccessTestRef) {
      scenarioSuccessTestChart = eCharts.init(chartRefs.scenarioSuccessTestRef);
      scenarioSuccessTestChart.setOption(scenarioSuccessTestConfig.value);
    }

    // Initialize test status chart
    if (chartRefs.testStatusRef) {
      testStatusChart = eCharts.init(chartRefs.testStatusRef);
      testStatusChart.setOption(testStatusConfig.value);
    }

    // Initialize review status chart
    if (chartRefs.reviewStatusRef) {
      reviewStatusChart = eCharts.init(chartRefs.reviewStatusRef);
      reviewStatusChart.setOption(reviewStatusConfig.value);
    }
  };

  /**
   * Resizes all charts to fit their containers
   */
  const resizeAllCharts = () => {
    const charts = [
      progressChart,
      recentCompletionRateChart,
      recentOverdueRateChart,
      recentCompletedWorkloadChart,
      recentSavingRateChart,
      backlogTaskChart,
      backlogWorkloadChart,
      overdueTaskChart,
      unplannedTaskNumberChart,
      unplannedWorkloadChart,
      taskTypeChart,
      taskStatusChart,
      leadTimeChart,
      criticalFailureChart,
      majorFailureChart,
      minorFailureChart,
      trivialFailureChart,
      apiOpenTestChart,
      apiSuccessTestChart,
      scenarioOpenTestChart,
      scenarioSuccessTestChart,
      testStatusChart,
      reviewStatusChart
    ];

    charts.forEach(chart => {
      if (chart) {
        chart.resize();
      }
    });
  };

  /**
   * Resizes charts based on count type
   *
   * @param countType - Type of items being displayed
   */
  const resizeChartsByType = (countType: 'task' | 'useCase') => {
    nextTick(() => {
      if (countType === 'task') {
        [criticalFailureChart, majorFailureChart, minorFailureChart, trivialFailureChart, taskTypeChart, taskStatusChart].forEach(chart => {
          if (chart) chart.resize();
        });
      } else {
        [scenarioOpenTestChart, scenarioSuccessTestChart, apiOpenTestChart, apiSuccessTestChart, reviewStatusChart, testStatusChart].forEach(chart => {
          if (chart) chart.resize();
        });
      }
    });
  };

  /**
   * Updates chart data and refreshes visualizations
   *
   * @param data - Data object containing all chart data
   * @param countType - Type of items being displayed
   */
  const updateCharts = (data: any, countType: 'task' | 'useCase') => {
    // Update progress chart
    if (data.totalProgressOverview && progressChart) {
      const { totalNum = 0, totalCompletedNum = 0, totalCompletedRate = 0 } = data.totalProgressOverview;
      const progressData = [totalNum - totalCompletedNum, totalCompletedNum];
      progressChartConfig.value.series[0].data.forEach((item, idx) => {
        item.value = progressData[idx];
      });
      progressChartConfig.value.title.text = totalCompletedRate + '%';
      progressChart.setOption(progressChartConfig.value);
    }

    // Update backlog charts
    if (data.backloggedCount && backlogTaskChart && backlogWorkloadChart) {
      const { backloggedNum, backloggedRate, backloggedWorkload, backloggedWorkloadRate, totalNum, totalWorkload } = data.backloggedCount;

      // Update backlog task chart
      const blockNumData = [backloggedNum, totalNum - backloggedNum];
      backlogTaskConfig.value.title.text = backloggedRate + '%';
      backlogTaskConfig.value.series[0].data.forEach((item, idx) => {
        item.value = blockNumData[idx];
      });
      backlogTaskChart.setOption(backlogTaskConfig.value);

      // Update backlog workload chart
      const blockWorkloadData = [backloggedWorkload, totalWorkload - backloggedWorkload];
      backlogWorkloadConfig.value.title.text = backloggedWorkloadRate + '%';
      backlogWorkloadConfig.value.series[0].data.forEach((item, idx) => {
        item.value = blockWorkloadData[idx];
      });
      backlogWorkloadChart.setOption(backlogWorkloadConfig.value);
    }

    // Update overdue assessment chart
    if (data.overdueAssessmentCount && overdueTaskChart) {
      const { overdueNum, overdueRate, totalNum } = data.overdueAssessmentCount;
      const overdueata = [overdueNum, totalNum - overdueNum];
      overdueTaskConfig.value.title.text = overdueRate + '%';
      overdueTaskConfig.value.series[0].data.forEach((item, idx) => {
        item.value = overdueata[idx];
      });
      overdueTaskChart.setOption(overdueTaskConfig.value);
    }

    // Update unplanned work charts
    if (data.unplannedWorkCount && unplannedTaskNumberChart && unplannedWorkloadChart) {
      const { unplannedNum, unplannedRate, unplannedWorkload, unplannedWorkloadCompletedRate, totalNum, totalWorkload } = data.unplannedWorkCount;

      // Update unplanned task number chart
      const unplannedNumData = [unplannedNum, totalNum - unplannedNum];
      unplannedTaskNumberConfig.value.title.text = unplannedRate + '%';
      unplannedTaskNumberConfig.value.series[0].data.forEach((item, idx) => {
        item.value = unplannedNumData[idx];
      });
      unplannedTaskNumberChart.setOption(unplannedTaskNumberConfig.value);

      // Update unplanned workload chart
      const unplannedWorkloadData = [unplannedWorkload, totalWorkload - unplannedWorkload];
      unplannedWorkloadConfig.value.title.text = unplannedWorkloadCompletedRate + '%';
      unplannedWorkloadConfig.value.series[0].data.forEach((item, idx) => {
        item.value = unplannedWorkloadData[idx];
      });
      unplannedWorkloadChart.setOption(unplannedWorkloadConfig.value);
    }

    // Update failure assessment charts (only for tasks)
    if (countType === 'task' && data.failureAssessmentCount) {
      const { failureLevelCount = { CRITICAL: 0, MAJOR: 0, MINOR: 0, TRIVIAL: 0 }, failureNum = 0 } = data.failureAssessmentCount;

      // Update critical failure chart
      if (criticalFailureChart) {
        const criticalData = [failureLevelCount.CRITICAL, failureNum - failureLevelCount.CRITICAL];
        criticalFailureConfig.value.title.text = ((failureLevelCount.CRITICAL / failureNum) * 100).toFixed(2) + '%';
        criticalFailureConfig.value.series[0].data.forEach((item, idx) => {
          item.value = criticalData[idx];
        });
        criticalFailureChart.setOption(criticalFailureConfig.value);
      }

      // Update major failure chart
      if (majorFailureChart) {
        const majorData = [failureLevelCount.MAJOR, failureNum - failureLevelCount.MAJOR];
        majorFailureConfig.value.title.text = ((failureLevelCount.MAJOR / failureNum) * 100).toFixed(2) + '%';
        majorFailureConfig.value.series[0].data.forEach((item, idx) => {
          item.value = majorData[idx];
        });
        majorFailureChart.setOption(majorFailureConfig.value);
      }

      // Update minor failure chart
      if (minorFailureChart) {
        const minorData = [failureLevelCount.MINOR, failureNum - failureLevelCount.MINOR];
        minorFailureConfig.value.title.text = ((failureLevelCount.MINOR / failureNum) * 100).toFixed(2) + '%';
        minorFailureConfig.value.series[0].data.forEach((item, idx) => {
          item.value = minorData[idx];
        });
        minorFailureChart.setOption(minorFailureConfig.value);
      }

      // Update trivial failure chart
      if (trivialFailureChart) {
        const trivialData = [failureLevelCount.TRIVIAL, failureNum - failureLevelCount.TRIVIAL];
        trivialFailureConfig.value.title.text = ((failureLevelCount.TRIVIAL / failureNum) * 100).toFixed(2) + '%';
        trivialFailureConfig.value.series[0].data.forEach((item, idx) => {
          item.value = trivialData[idx];
        });
        trivialFailureChart.setOption(trivialFailureConfig.value);
      }
    }

    // Update API test charts (only for use cases)
    if (countType === 'useCase' && data.apisTestCount) {
      const { enabledTestApiNum, enabledTestApiRate, passedTestApiNum, passedTestNum, totalApiNum } = data.apisTestCount;

      // Update API open test chart
      if (apiOpenTestChart) {
        const apisTestData = [enabledTestApiNum, totalApiNum - enabledTestApiNum];
        apiOpenTestConfig.value.title.text = enabledTestApiRate + '%';
        apiOpenTestConfig.value.series[0].data.forEach((item, idx) => {
          item.value = apisTestData[idx];
        });
        apiOpenTestChart.setOption(apiOpenTestConfig.value);
      }

      // Update API success test chart
      if (apiSuccessTestChart) {
        const apiSuccessData = [passedTestApiNum, totalApiNum - passedTestApiNum];
        apiSuccessTestConfig.value.title.text = ((passedTestNum / totalApiNum) * 100).toFixed(2) + '%';
        apiSuccessTestConfig.value.series[0].data.forEach((item, idx) => {
          item.value = apiSuccessData[idx];
        });
        apiSuccessTestChart.setOption(apiSuccessTestConfig.value);
      }
    }

    // Update scenario test charts (only for use cases)
    if (countType === 'useCase' && data.scenarioTestCount) {
      const { enabledTestScenarioNum, enabledTestScenarioRate, passedTestScenarioNum, totalScenarioNum } = data.scenarioTestCount;

      // Update scenario open test chart
      if (scenarioOpenTestChart) {
        const scenarioTestData = [enabledTestScenarioNum, totalScenarioNum - enabledTestScenarioNum];
        scenarioOpenTestConfig.value.title.text = enabledTestScenarioRate + '%';
        scenarioOpenTestConfig.value.series[0].data.forEach((item, idx) => {
          item.value = scenarioTestData[idx];
        });
        scenarioOpenTestChart.setOption(scenarioOpenTestConfig.value);
      }

      // Update scenario success test chart
      if (scenarioSuccessTestChart) {
        const scenarioSuccessData = [passedTestScenarioNum, totalScenarioNum - passedTestScenarioNum];
        scenarioSuccessTestConfig.value.title.text = ((passedTestScenarioNum / totalScenarioNum) * 100).toFixed(2) + '%';
        scenarioSuccessTestConfig.value.series[0].data.forEach((item, idx) => {
          item.value = scenarioSuccessData[idx];
        });
        scenarioSuccessTestChart.setOption(scenarioSuccessTestConfig.value);
      }
    }

    // Update task type chart
    if (data.totalTypeCount && taskTypeChart) {
      const { STORY = 0, REQUIREMENT = 0, TASK = 0, BUG = 0, API_TEST = 0, SCENARIO_TEST = 0 } = data.totalTypeCount;
      const typeData = [STORY, REQUIREMENT, TASK, BUG, API_TEST, SCENARIO_TEST];
      taskTypeConfig.value.series[0].data.forEach((item, idx) => {
        item.value = typeData[idx];
      });
      taskTypeChart.setOption(taskTypeConfig.value);
    }

    // Update task status chart
    if (data.totalStatusCount && taskStatusChart) {
      const { PENDING = 0, IN_PROGRESS = 0, CONFIRMING = 0, COMPLETED = 0, CANCELED = 0 } = data.totalStatusCount;
      const statusData = [PENDING, IN_PROGRESS, CONFIRMING, COMPLETED, CANCELED];
      taskStatusConfig.value.series[0].data.forEach((item, idx) => {
        item.value = statusData[idx];
      });
      taskStatusChart.setOption(taskStatusConfig.value);
    }

    // Update lead time chart
    if (data.leadTimeCount && leadTimeChart) {
      const { avgProcessingTime = 0, maxProcessingTime = 0, minProcessingTime = 0, p50ProcessingTime = 0, p75ProcessingTime = 0, p90ProcessingTime = 0, p95ProcessingTime = 0 } = data.leadTimeCount;
      const leadTimeChartData = [avgProcessingTime, minProcessingTime, maxProcessingTime, p50ProcessingTime, p75ProcessingTime, p90ProcessingTime, p95ProcessingTime];
      leadTimeConfig.value.series[0].data = leadTimeChartData;
      leadTimeChart.setOption(leadTimeConfig.value);
    }

    // Update test status chart (only for use cases)
    if (countType === 'useCase' && data.totalTestResultCount && testStatusChart) {
      const { PENDING = 0, PASSED = 0, NOT_PASSED = 0, BLOCKED = 0, CANCELED = 0 } = data.totalTestResultCount;
      const testResultData = [PENDING, PASSED, NOT_PASSED, BLOCKED, CANCELED];
      testStatusConfig.value.series[0].data.forEach((item, idx) => {
        item.value = testResultData[idx];
      });
      testStatusChart.setOption(testStatusConfig.value);
    }

    // Update review status chart (only for use cases)
    if (countType === 'useCase' && data.totalReviewStatusCount && reviewStatusChart) {
      const { PENDING = 0, PASSED = 0, FAILED = 0 } = data.totalReviewStatusCount;
      const reviewData = [PENDING, PASSED, FAILED];
      reviewStatusConfig.value.series[0].data.forEach((item, idx) => {
        item.value = reviewData[idx];
      });
      reviewStatusChart.setOption(reviewStatusConfig.value);
    }
  };

  /**
   * Updates recent date charts based on selected time period
   *
   * @param recentDate - Selected recent date period
   * @param recentDeliveryData - Recent delivery data for the period
   */
  const updateRecentDateCharts = (recentDate: string, recentDeliveryData: any) => {
    const periodData = recentDeliveryData[recentDate] || {};
    const { totalNum, completedNum, completedRate, OverdueNum, overdueRate, completedWorkload, completedWorkloadRate, savingWorkload, savingWorkloadRate, totalWorkload } = periodData;

    // Update recent completion rate chart
    if (recentCompletionRateChart) {
      const recentCompleteRateData = [completedNum, totalNum - completedNum];
      recentCompletionRateConfig.value.title.text = completedRate + '%';
      recentCompletionRateConfig.value.series[0].data.forEach((item, idx) => {
        item.value = recentCompleteRateData[idx];
      });
      recentCompletionRateChart.setOption(recentCompletionRateConfig.value);
    }

    // Update recent overdue rate chart
    if (recentOverdueRateChart) {
      const recentOverdueRateData = [OverdueNum, totalNum - OverdueNum];
      recentOverdueRateConfig.value.title.text = overdueRate + '%';
      recentOverdueRateConfig.value.series[0].data.forEach((item, idx) => {
        item.value = recentOverdueRateData[idx];
      });
      recentOverdueRateChart.setOption(recentOverdueRateConfig.value);
    }

    // Update recent completed workload chart
    if (recentCompletedWorkloadChart) {
      const completedWorkloadData = [completedWorkload, totalNum - completedWorkload];
      recentCompletedWorkloadConfig.value.title.text = completedWorkloadRate + '%';
      recentCompletedWorkloadConfig.value.series[0].data.forEach((item, idx) => {
        item.value = completedWorkloadData[idx];
      });
      recentCompletedWorkloadChart.setOption(recentCompletedWorkloadConfig.value);
    }

    // Update recent saving rate chart
    if (recentSavingRateChart) {
      const recentSavingWorkloadData = [savingWorkload, totalWorkload - savingWorkload];
      recentSavingRateConfig.value.title.text = savingWorkloadRate + '%';
      recentSavingRateConfig.value.series[0].data.forEach((item, idx) => {
        item.value = recentSavingWorkloadData[idx];
      });
      recentSavingRateChart.setOption(recentSavingRateConfig.value);
    }
  };

  return {
    // Chart configurations
    progressChartConfig,
    recentCompletionRateConfig,
    recentOverdueRateConfig,
    recentCompletedWorkloadConfig,
    recentSavingRateConfig,
    backlogTaskConfig,
    backlogWorkloadConfig,
    overdueTaskConfig,
    unplannedTaskNumberConfig,
    unplannedWorkloadConfig,
    taskTypeConfig,
    taskStatusConfig,
    leadTimeConfig,
    criticalFailureConfig,
    majorFailureConfig,
    minorFailureConfig,
    trivialFailureConfig,
    apiOpenTestConfig,
    apiSuccessTestConfig,
    scenarioOpenTestConfig,
    scenarioSuccessTestConfig,
    testStatusConfig,
    reviewStatusConfig,

    // Methods
    initializeCharts,
    resizeAllCharts,
    resizeChartsByType,
    updateCharts,
    updateRecentDateCharts
  };
}
