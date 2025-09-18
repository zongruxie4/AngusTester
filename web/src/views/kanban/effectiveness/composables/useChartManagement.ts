import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';
import { BurnDownDataByType, ChartConfig, RankingData, TotalTypeCount } from '../types';
import {
  createBurnDownConfig,
  createOneTimePassedTestConfig,
  createOneTimePassedTestRateConfig,
  createOneTimeUnpassedTestConfig,
  createOneTimeUnpassedTestRateConfig,
  createOverdueConfig,
  createOverdueRateConfig,
  createTargetCountConfig,
  createTargetRateConfig,
  createTaskTypeConfig,
  createWorkloadConfig,
  createWorkloadRateConfig
} from './useChartConfigs';

/**
 * Chart management composable for effectiveness dashboard
 *
 * @returns Object containing chart instances and management methods
 */
export function useChartManagement () {
  const { t } = useI18n();
  /** Task type chart instance */
  let taskTypeChart: eCharts.ECharts;

  /** Burn down chart instance */
  let burnDownChart: eCharts.ECharts;

  /** Target count chart instance */
  let targetCountChart: eCharts.ECharts;

  /** Target rate chart instance */
  let targetRateChart: eCharts.ECharts;

  /** Workload chart instance */
  let workloadChart: eCharts.ECharts;

  /** Workload rate chart instance */
  let workloadRateChart: eCharts.ECharts;

  /** Overdue chart instance */
  let overdueChart: eCharts.ECharts;

  /** Overdue rate chart instance */
  let overdueRateChart: eCharts.ECharts;

  /** One time passed test chart instance */
  let oneTimePassedTestChart: eCharts.ECharts;

  /** One time passed test rate chart instance */
  let oneTimePassedTestRateChart: eCharts.ECharts;

  /** One time unpassed test chart instance */
  let oneTimeUnpassedTestChart: eCharts.ECharts;

  /** One time unpassed test rate chart instance */
  let oneTimeUnpassedTestRateChart: eCharts.ECharts;

  /** Chart configuration references */
  const taskTypeConfig = ref<ChartConfig>(createTaskTypeConfig());
  const burnDownConfig = ref<ChartConfig>(createBurnDownConfig());
  const targetCountConfig = ref<ChartConfig>(createTargetCountConfig());
  const targetRateConfig = ref<ChartConfig>(createTargetRateConfig());
  const workloadConfig = ref<ChartConfig>(createWorkloadConfig());
  const workloadRateConfig = ref<ChartConfig>(createWorkloadRateConfig());
  const overdueConfig = ref<ChartConfig>(createOverdueConfig());
  const overdueRateConfig = ref<ChartConfig>(createOverdueRateConfig());
  const oneTimePassedTestConfig = ref<ChartConfig>(createOneTimePassedTestConfig());
  const oneTimePassedTestRateConfig = ref<ChartConfig>(createOneTimePassedTestRateConfig());
  const oneTimeUnpassedTestConfig = ref<ChartConfig>(createOneTimeUnpassedTestConfig());
  const oneTimeUnpassedTestRateConfig = ref<ChartConfig>(createOneTimeUnpassedTestRateConfig());

  /**
   * Initializes all charts with their configurations
   *
   * @param chartRefs - Object containing DOM references for chart containers
   */
  const initializeCharts = (chartRefs: Record<string, HTMLElement>) => {
    // Initialize task type chart
    if (chartRefs.taskTypeRef) {
      taskTypeChart = eCharts.init(chartRefs.taskTypeRef);
      taskTypeChart.setOption(taskTypeConfig.value);
    }

    // Initialize burn down chart
    if (chartRefs.burnDownRef) {
      burnDownChart = eCharts.init(chartRefs.burnDownRef);
      burnDownChart.setOption(burnDownConfig.value);
    }

    // Initialize target count chart
    if (chartRefs.targetCountRef) {
      targetCountChart = eCharts.init(chartRefs.targetCountRef);
      targetCountChart.setOption(targetCountConfig.value);
    }

    // Initialize target rate chart
    if (chartRefs.targetRateRef) {
      targetRateChart = eCharts.init(chartRefs.targetRateRef);
      targetRateChart.setOption(targetRateConfig.value);
    }

    // Initialize workload chart
    if (chartRefs.workloadRef) {
      workloadChart = eCharts.init(chartRefs.workloadRef);
      workloadChart.setOption(workloadConfig.value);
    }

    // Initialize workload rate chart
    if (chartRefs.workloadRateRef) {
      workloadRateChart = eCharts.init(chartRefs.workloadRateRef);
      workloadRateChart.setOption(workloadRateConfig.value);
    }

    // Initialize overdue chart
    if (chartRefs.overdueRef) {
      overdueChart = eCharts.init(chartRefs.overdueRef);
      overdueChart.setOption(overdueConfig.value);
    }

    // Initialize overdue rate chart
    if (chartRefs.overdueRateRef) {
      overdueRateChart = eCharts.init(chartRefs.overdueRateRef);
      overdueRateChart.setOption(overdueRateConfig.value);
    }

    // Initialize one time passed test chart
    if (chartRefs.oneTimePassedTestRef) {
      oneTimePassedTestChart = eCharts.init(chartRefs.oneTimePassedTestRef);
      oneTimePassedTestChart.setOption(oneTimePassedTestConfig.value);
    }

    // Initialize one time passed test rate chart
    if (chartRefs.oneTimePassedTestRateRef) {
      oneTimePassedTestRateChart = eCharts.init(chartRefs.oneTimePassedTestRateRef);
      oneTimePassedTestRateChart.setOption(oneTimePassedTestRateConfig.value);
    }

    // Initialize one time unpassed test chart
    if (chartRefs.oneTimeUnpassedTestRef) {
      oneTimeUnpassedTestChart = eCharts.init(chartRefs.oneTimeUnpassedTestRef);
      oneTimeUnpassedTestChart.setOption(oneTimeUnpassedTestConfig.value);
    }

    // Initialize one time unpassed test rate chart
    if (chartRefs.oneTimeUnpassedTestRateRef) {
      oneTimeUnpassedTestRateChart = eCharts.init(chartRefs.oneTimeUnpassedTestRateRef);
      oneTimeUnpassedTestRateChart.setOption(oneTimeUnpassedTestRateConfig.value);
    }
  };

  /**
   * Resizes all charts to fit their containers
   */
  const resizeAllCharts = () => {
    const charts = [
      taskTypeChart,
      burnDownChart,
      targetCountChart,
      targetRateChart,
      workloadChart,
      workloadRateChart,
      overdueChart,
      overdueRateChart,
      oneTimePassedTestChart,
      oneTimePassedTestRateChart,
      oneTimeUnpassedTestChart,
      oneTimeUnpassedTestRateChart
    ];

    charts.forEach(chart => {
      if (chart) {
        chart.resize();
      }
    });
  };

  /**
   * Updates task type chart with new data
   *
   * @param totalTypeCount - Total type count data
   */
  const updateTaskTypeChart = (totalTypeCount: TotalTypeCount) => {
    if (!taskTypeChart) return;

    const { STORY = 0, REQUIREMENT = 0, TASK = 0, BUG = 0, API_TEST = 0, SCENARIO_TEST = 0 } = totalTypeCount;
    const typeData = [STORY, REQUIREMENT, TASK, BUG, API_TEST, SCENARIO_TEST];

    taskTypeConfig.value.series[0].data.forEach((item, idx) => {
      item.value = typeData[idx];
    });

    taskTypeChart.setOption(taskTypeConfig.value);
  };

  /**
   * Updates burn down chart with new data
   *
   * @param burnDownData - Burn down chart data
   * @param targetType - Target type (NUM or WORKLOAD)
   */
  const updateBurnDownChart = (burnDownData: BurnDownDataByType, targetType: 'NUM' | 'WORKLOAD') => {
    if (!burnDownChart) return;

    const targetData = burnDownData[targetType];
    if (!targetData) return;

    const xData = targetData.expected.map(i => i.timeSeries);
    const expectedYData = targetData.expected.map(i => i.value);
    const remainingYData = targetData.remaining.map(i => i.value);

    burnDownConfig.value.xAxis.data = xData;
    burnDownConfig.value.series[0].data = remainingYData;
    burnDownConfig.value.series[1].data = expectedYData;

    burnDownChart.setOption(burnDownConfig.value);
  };

  /**
   * Updates ranking charts with new data
   *
   * @param rankingData - Ranking data
   * @param assignees - Assignee information
   * @param countType - Count type (task or useCase)
   */
  const updateRankingCharts = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    // Update target count chart
    updateTargetCountChart(rankingData, assignees, countType);

    // Update workload chart
    updateWorkloadChart(rankingData, assignees);

    // Update overdue chart
    updateOverdueChart(rankingData, assignees, countType);

    // Update one time passed test chart
    updateOneTimePassedTestChart(rankingData, assignees, countType);

    // Update one time unpassed test chart
    updateOneTimeUnpassedTestChart(rankingData, assignees, countType);

    // Update rate charts
    updateRateCharts(rankingData, assignees, countType);
  };

  /**
   * Updates target count chart
   */
  const updateTargetCountChart = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    if (!targetCountChart) return;

    const ranking = countType === 'task' ? rankingData.completedNumRank : rankingData.passedTestNumRank;
    const validRanking = countType === 'task' ? rankingData.validTaskNumRank : rankingData.validCaseNumRank;

    if (ranking && validRanking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const totalXData = userIds.map((userId) => validRanking.find(i => i.assigneeId === userId)?.score || 0);
      const completeXData = rankData.map(i => i.score);

      targetCountConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      targetCountConfig.value.series[0].data = totalXData;
      targetCountConfig.value.series[1].data = completeXData;

      // Update series names based on count type
      if (countType === 'task') {
        targetCountConfig.value.series[1].name = t('kanban.effectiveness.completedCount');
      } else {
        targetCountConfig.value.series[1].name = t('kanban.effectiveness.passedCount');
      }

      // Show/hide no data image
      targetCountConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      targetCountConfig.value.yAxis.data = [];
      targetCountConfig.value.series[0].data = [];
      targetCountConfig.value.series[1].data = [];
      targetCountConfig.value.graphic!.invisible = false;
    }

    targetCountChart.setOption(targetCountConfig.value);
  };

  /**
   * Updates workload chart
   */
  const updateWorkloadChart = (
    rankingData: RankingData,
    assignees: Record<string, any>
  ) => {
    if (!workloadChart) return;

    const ranking = rankingData.completedWorkloadRank;
    const actualRanking = rankingData.actualWorkloadRank;

    if (ranking && actualRanking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const totalXData = userIds.map((userId) => actualRanking.find(i => i.assigneeId === userId)?.score || 0);
      const completeXData = rankData.map(i => i.score);

      workloadConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      workloadConfig.value.series[0].data = totalXData;
      workloadConfig.value.series[1].data = completeXData;

      workloadConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      workloadConfig.value.yAxis.data = [];
      workloadConfig.value.series[0].data = [];
      workloadConfig.value.series[1].data = [];
      workloadConfig.value.graphic!.invisible = false;
    }

    workloadChart.setOption(workloadConfig.value);
  };

  /**
   * Updates overdue chart
   */
  const updateOverdueChart = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    if (!overdueChart) return;

    const ranking = rankingData.overdueNumRank;
    const validRanking = countType === 'task' ? rankingData.validTaskNumRank : rankingData.validCaseNumRank;

    if (ranking && validRanking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const totalXData = userIds.map((userId) => validRanking.find(i => i.assigneeId === userId)?.score || 0);
      const overdueXData = rankData.map(i => i.score);

      overdueConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      overdueConfig.value.series[0].data = totalXData;
      overdueConfig.value.series[1].data = overdueXData;

      overdueConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      overdueConfig.value.yAxis.data = [];
      overdueConfig.value.series[0].data = [];
      overdueConfig.value.series[1].data = [];
      overdueConfig.value.graphic!.invisible = false;
    }

    overdueChart.setOption(overdueConfig.value);
  };

  /**
   * Updates one time passed test chart
   */
  const updateOneTimePassedTestChart = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    if (!oneTimePassedTestChart) return;

    const ranking = countType === 'task' ? rankingData.oneTimePassedNumRank : rankingData.oneTimePassedTestNumRank;
    const validRanking = countType === 'task' ? rankingData.validTaskNumRank : rankingData.validCaseNumRank;

    if (ranking && validRanking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const totalXData = userIds.map((userId) => validRanking.find(i => i.assigneeId === userId)?.score || 0);
      const passedXData = rankData.map(i => i.score);

      oneTimePassedTestConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      oneTimePassedTestConfig.value.series[0].data = totalXData;
      oneTimePassedTestConfig.value.series[1].data = passedXData;

      oneTimePassedTestConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      oneTimePassedTestConfig.value.yAxis.data = [];
      oneTimePassedTestConfig.value.series[0].data = [];
      oneTimePassedTestConfig.value.series[1].data = [];
      oneTimePassedTestConfig.value.graphic!.invisible = false;
    }

    oneTimePassedTestChart.setOption(oneTimePassedTestConfig.value);
  };

  /**
   * Updates one time unpassed test chart
   */
  const updateOneTimeUnpassedTestChart = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    if (!oneTimeUnpassedTestChart) return;

    const ranking = countType === 'task' ? rankingData.validBugNumRank : rankingData.oneTimePassedReviewNumRank;
    const validRanking = countType === 'task' ? rankingData.validTaskNumRank : rankingData.validCaseNumRank;

    if (ranking && validRanking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const totalXData = userIds.map((userId) => validRanking.find(i => i.assigneeId === userId)?.score || 0);
      const unpassedXData = rankData.map(i => i.score);

      oneTimeUnpassedTestConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      oneTimeUnpassedTestConfig.value.series[0].data = totalXData;
      oneTimeUnpassedTestConfig.value.series[1].data = unpassedXData;

      // Update series names and colors based on count type
      if (countType === 'task') {
        oneTimeUnpassedTestConfig.value.series[1].name = t('kanban.effectiveness.validBugCount');
        oneTimeUnpassedTestConfig.value.series[1].itemStyle!.color = 'rgba(255, 165, 43, 1)';
      } else {
        oneTimeUnpassedTestConfig.value.series[1].name = t('kanban.effectiveness.oneTimeReviewPassedCount');
        oneTimeUnpassedTestConfig.value.series[1].itemStyle!.color = 'rgba(82, 196, 26, 1)';
      }

      oneTimeUnpassedTestConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      oneTimeUnpassedTestConfig.value.yAxis.data = [];
      oneTimeUnpassedTestConfig.value.series[0].data = [];
      oneTimeUnpassedTestConfig.value.series[1].data = [];
      oneTimeUnpassedTestConfig.value.graphic!.invisible = false;
    }

    oneTimeUnpassedTestChart.setOption(oneTimeUnpassedTestConfig.value);
  };

  /**
   * Updates all rate charts
   */
  const updateRateCharts = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    // Update target rate chart
    updateTargetRateChart(rankingData, assignees, countType);

    // Update workload rate chart
    updateWorkloadRateChart(rankingData, assignees);

    // Update overdue rate chart
    updateOverdueRateChart(rankingData, assignees);

    // Update one time passed test rate chart
    updateOneTimePassedTestRateChart(rankingData, assignees, countType);

    // Update one time unpassed test rate chart
    updateOneTimeUnpassedTestRateChart(rankingData, assignees, countType);
  };

  /**
   * Updates target rate chart
   */
  const updateTargetRateChart = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    if (!targetRateChart) return;

    const ranking = countType === 'task' ? rankingData.completedRateRank : rankingData.passedTestRateRank;

    if (ranking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const rateData = rankData.map(i => i.score);

      targetRateConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      targetRateConfig.value.series[0].data = rateData;

      targetRateConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      targetRateConfig.value.yAxis.data = [];
      targetRateConfig.value.series[0].data = [];
      targetRateConfig.value.graphic!.invisible = false;
    }

    targetRateChart.setOption(targetRateConfig.value);
  };

  /**
   * Updates workload rate chart
   */
  const updateWorkloadRateChart = (
    rankingData: RankingData,
    assignees: Record<string, any>
  ) => {
    if (!workloadRateChart) return;

    const ranking = rankingData.savingWorkloadRank;

    if (ranking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const rateData = rankData.map(i => i.score);

      workloadRateConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      workloadRateConfig.value.series[0].data = rateData;

      workloadRateConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      workloadRateConfig.value.yAxis.data = [];
      workloadRateConfig.value.series[0].data = [];
      workloadRateConfig.value.graphic!.invisible = false;
    }

    workloadRateChart.setOption(workloadRateConfig.value);
  };

  /**
   * Updates overdue rate chart
   */
  const updateOverdueRateChart = (
    rankingData: RankingData,
    assignees: Record<string, any>
  ) => {
    if (!overdueRateChart) return;

    const ranking = rankingData.overdueRateRank;

    if (ranking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const rateData = rankData.map(i => i.score);

      overdueRateConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      overdueRateConfig.value.series[0].data = rateData;

      overdueRateConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      overdueRateConfig.value.yAxis.data = [];
      overdueRateConfig.value.series[0].data = [];
      overdueRateConfig.value.graphic!.invisible = false;
    }

    overdueRateChart.setOption(overdueRateConfig.value);
  };

  /**
   * Updates one time passed test rate chart
   */
  const updateOneTimePassedTestRateChart = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    if (!oneTimePassedTestRateChart) return;

    const ranking = countType === 'task' ? rankingData.oneTimePassedRateRank : rankingData.oneTimePassedTestRateRank;

    if (ranking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const rateData = rankData.map(i => i.score);

      oneTimePassedTestRateConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      oneTimePassedTestRateConfig.value.series[0].data = rateData;

      oneTimePassedTestRateConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      oneTimePassedTestRateConfig.value.yAxis.data = [];
      oneTimePassedTestRateConfig.value.series[0].data = [];
      oneTimePassedTestRateConfig.value.graphic!.invisible = false;
    }

    oneTimePassedTestRateChart.setOption(oneTimePassedTestRateConfig.value);
  };

  /**
   * Updates one time unpassed test rate chart
   */
  const updateOneTimeUnpassedTestRateChart = (
    rankingData: RankingData,
    assignees: Record<string, any>,
    countType: 'task' | 'useCase'
  ) => {
    if (!oneTimeUnpassedTestRateChart) return;

    const ranking = countType === 'task' ? rankingData.validBugRateRank : rankingData.oneTimePassedReviewRateRank;

    if (ranking) {
      const rankData = ranking.slice(0, 10).reverse();
      const userIds = rankData.map(i => i.assigneeId);
      const yData = userIds.map(i => assignees[i]?.fullName || '');
      const rateData = rankData.map(i => i.score);

      oneTimeUnpassedTestRateConfig.value.yAxis.data = yData.map(i => ({
        value: i,
        textStyle: { width: 50, overflow: 'breakAll' }
      }));
      oneTimeUnpassedTestRateConfig.value.series[0].data = rateData;

      // Update colors based on count type
      if (countType === 'task') {
        oneTimeUnpassedTestRateConfig.value.series[0].itemStyle!.color = 'rgba(255, 165, 43, 1)';
      } else {
        oneTimeUnpassedTestRateConfig.value.series[0].itemStyle!.color = 'rgba(82, 196, 26, 1)';
      }

      oneTimeUnpassedTestRateConfig.value.graphic!.invisible = yData.length > 0;
    } else {
      oneTimeUnpassedTestRateConfig.value.yAxis.data = [];
      oneTimeUnpassedTestRateConfig.value.series[0].data = [];
      oneTimeUnpassedTestRateConfig.value.graphic!.invisible = false;
    }
    oneTimeUnpassedTestRateChart.setOption(oneTimeUnpassedTestRateConfig.value);
  };

  return {
    // Chart configurations
    taskTypeConfig,
    burnDownConfig,
    targetCountConfig,
    targetRateConfig,
    workloadConfig,
    workloadRateConfig,
    overdueConfig,
    overdueRateConfig,
    oneTimePassedTestConfig,
    oneTimePassedTestRateConfig,
    oneTimeUnpassedTestConfig,
    oneTimeUnpassedTestRateConfig,

    // Methods
    initializeCharts,
    resizeAllCharts,
    updateTaskTypeChart,
    updateBurnDownChart,
    updateRankingCharts
  };
}
