import { nextTick, ref } from 'vue';
import * as eCharts from 'echarts';
import { ApiData, CaseData, ChartConfig, GrowthTrendData, ScenarioData, ScriptData, TaskData } from '../types';
import { getDateArr, getDateArrWithTime } from '@/utils/utils';

/**
 * Chart management composable for data assets dashboard
 *
 * @returns Object containing chart instances and management methods
 */
export function useChartManagement () {
  /** Growth trend chart instance */
  let growthTrendChart: eCharts.ECharts;

  /** Case bar chart instance */
  let caseBarChart: eCharts.ECharts;

  /** Case pie chart instance */
  let casePieChart: eCharts.ECharts;

  /** API bar chart instance */
  let apiBarChart: eCharts.ECharts;

  /** API pie chart instance */
  let apiPieChart: eCharts.ECharts;

  /** Task bar chart instance */
  let taskBarChart: eCharts.ECharts;

  /** Task pie chart instance */
  let taskPieChart: eCharts.ECharts;

  /** Plan pie chart instance */
  let planPieChart: eCharts.ECharts;

  /** Sprint pie chart instance */
  let sprintPieChart: eCharts.ECharts;

  /** Scenario pie chart instance */
  let scenarioPieChart: eCharts.ECharts;

  /** Script bar chart instance */
  let scriptBarChart: eCharts.ECharts;

  /** Chart configurations */
  const growthTrendConfig = ref<ChartConfig>({});
  const caseBarConfig = ref<ChartConfig>({});
  const casePieConfig = ref<ChartConfig>({});
  const apiBarConfig = ref<ChartConfig>({});
  const apiPieConfig = ref<ChartConfig>({});
  const taskBarConfig = ref<ChartConfig>({});
  const taskPieConfig = ref<ChartConfig>({});
  const planPieConfig = ref<ChartConfig>({});
  const sprintPieConfig = ref<ChartConfig>({});
  const scenarioPieConfig = ref<ChartConfig>({});
  const scriptBarConfig = ref<ChartConfig>({});

  /** Chart DOM references */
  const growthTrendRef = ref<HTMLElement>();
  const caseStatusRef = ref<HTMLElement>();
  const caseReviewRef = ref<HTMLElement>();
  const apiStatusRef = ref<HTMLElement>();
  const apiReviewRef = ref<HTMLElement>();
  const taskStatusRef = ref<HTMLElement>();
  const taskReviewRef = ref<HTMLElement>();
  const planReviewRef = ref<HTMLElement>();
  const sprintReviewRef = ref<HTMLElement>();
  const scenarioRef = ref<HTMLElement>();
  const scriptRef = ref<HTMLElement>();

  /**
   * Sets chart DOM references
   * <p>
   * Updates chart DOM references for chart initialization and management
   * </p>
   *
   * @param refs - Object containing DOM references for chart containers
   */
  const setChartRefs = (refs: Record<string, HTMLElement>) => {
    growthTrendRef.value = refs.growthTrendRef;
    caseStatusRef.value = refs.caseStatusRef;
    caseReviewRef.value = refs.caseReviewRef;
    apiStatusRef.value = refs.apiStatusRef;
    apiReviewRef.value = refs.apiReviewRef;
    taskStatusRef.value = refs.taskStatusRef;
    taskReviewRef.value = refs.taskReviewRef;
    planReviewRef.value = refs.planReviewRef;
    sprintReviewRef.value = refs.sprintReviewRef;
    scenarioRef.value = refs.scenarioRef;
    scriptRef.value = refs.scriptRef;
  };

  /**
   * Initializes all charts with their configurations
   * <p>
   * Creates ECharts instances for all chart containers
   * </p>
   */
  const initializeCharts = () => {
    if (growthTrendRef.value) {
      growthTrendChart = eCharts.init(growthTrendRef.value);
      growthTrendChart.setOption(growthTrendConfig.value);
    }

    if (caseStatusRef.value) {
      caseBarChart = eCharts.init(caseStatusRef.value);
      caseBarChart.setOption(caseBarConfig.value);
    }

    if (caseReviewRef.value) {
      casePieChart = eCharts.init(caseReviewRef.value);
      casePieChart.setOption(casePieConfig.value);
    }

    if (apiStatusRef.value) {
      apiBarChart = eCharts.init(apiStatusRef.value);
      apiBarChart.setOption(apiBarConfig.value);
    }

    if (apiReviewRef.value) {
      apiPieChart = eCharts.init(apiReviewRef.value);
      apiPieChart.setOption(apiPieConfig.value);
    }

    if (taskStatusRef.value) {
      taskBarChart = eCharts.init(taskStatusRef.value);
      taskBarChart.setOption(taskBarConfig.value);
    }

    if (taskReviewRef.value) {
      taskPieChart = eCharts.init(taskReviewRef.value);
      taskPieChart.setOption(taskPieConfig.value);
    }

    if (planReviewRef.value) {
      planPieChart = eCharts.init(planReviewRef.value);
      planPieChart.setOption(planPieConfig.value);
    }

    if (sprintReviewRef.value) {
      sprintPieChart = eCharts.init(sprintReviewRef.value);
      sprintPieChart.setOption(sprintPieConfig.value);
    }

    if (scenarioRef.value) {
      scenarioPieChart = eCharts.init(scenarioRef.value);
      scenarioPieChart.setOption(scenarioPieConfig.value);
    }

    if (scriptRef.value) {
      scriptBarChart = eCharts.init(scriptRef.value);
      scriptBarChart.setOption(scriptBarConfig.value);
    }
  };

  /**
   * Updates growth trend chart with new data
   * <p>
   * Processes growth trend data and updates chart series and axis
   * </p>
   *
   * @param data - Growth trend data from API
   * @param targetType - Target type for growth trend
   * @param chartSeriesColorConfig - Color configuration for chart series
   * @param targetDataCategory - Category mapping for data labels
   * @param createdDateStart - Start date for fallback data
   * @param createdDateEnd - End date for fallback data
   */
  const updateGrowthTrendChart = (
    data: GrowthTrendData,
    targetType: string,
    chartSeriesColorConfig: Record<number, string>,
    targetDataCategory: Record<string, string>,
    createdDateStart?: string,
    createdDateEnd?: string
  ) => {
    const keys = Object.keys(data);
    if (keys.length) {
      const xData: string[] = [];
      keys.forEach(key => {
        data[key].forEach(i => {
          if (!xData.includes(i.timeSeries)) {
            xData.push(i.timeSeries);
          }
        });
      });

      xData.sort((a, b) => {
        return a > b ? 1 : a < b ? -1 : 0;
      });

      growthTrendConfig.value.series = keys.map((key, idx) => {
        return {
          name: targetDataCategory[key],
          data: xData.map(i => {
            const target = data[key].find(item => item.timeSeries === i);
            return target ? target.value : undefined;
          }),
          itemStyle: {
            color: new eCharts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },
              { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }
            ])
          },
          type: 'line',
          smooth: true,
          connectNulls: true,
          areaStyle: {}
        };
      });

      growthTrendConfig.value.xAxis.data = xData;
    } else {
      growthTrendConfig.value.xAxis.data = [];
      growthTrendConfig.value.series = [];
    }

    // Handle empty data with fallback dates
    if (growthTrendConfig.value.xAxis.data.length === 0) {
      if (createdDateEnd && createdDateStart) {
        growthTrendConfig.value.xAxis.data = getDateArrWithTime(createdDateStart, createdDateEnd);
      } else {
        growthTrendConfig.value.xAxis.data = getDateArr();
      }

      if (growthTrendConfig.value.series.length) {
        growthTrendConfig.value.series[0].data = Array.from(
          new Array(growthTrendConfig.value.xAxis.data.length)
        ).fill(0);
      }
    }

    if (growthTrendChart) {
      growthTrendChart.setOption(growthTrendConfig.value, true);
    }
  };

  /**
   * Updates case charts with new data
   * <p>
   * Updates both case bar chart and case pie chart with case statistics
   * </p>
   *
   * @param data - Case data from API
   */
  const updateCaseCharts = (data: CaseData) => {
    // Update case bar chart
    if (data.caseByTestResult) {
      const { PENDING = 0, PASSED = 0, NOT_PASSED = 0, BLOCKED = 0, CANCELED = 0 } = data.caseByTestResult;
      const barData = [CANCELED, BLOCKED, NOT_PASSED, PASSED, PENDING];

      if (caseBarConfig.value.series[0]) {
        caseBarConfig.value.series[0].data.forEach((item, idx) => {
          item.value = barData[idx];
        });
      }
    }

    if (caseBarChart) {
      caseBarChart.setOption(caseBarConfig.value);
    }

    // Update case pie chart
    if (data.caseByReviewStatus) {
      const { PEDING = 0, PASSED = 0, FAILED = 0 } = data.caseByReviewStatus;
      const pieData = [PEDING, PASSED, FAILED];

      if (casePieConfig.value.series[0]) {
        casePieConfig.value.series[0].data.forEach((item, idx) => {
          item.value = pieData[idx];
        });
      }
    }

    if (casePieConfig.value.title) {
      casePieConfig.value.title.text = data.allCase || 0;
    }

    if (casePieChart) {
      casePieChart.setOption(casePieConfig.value);
      nextTick(() => {
        casePieChart.resize();
      });
    }

    // Update plan pie chart
    if (planPieConfig.value.title) {
      planPieConfig.value.title.text = data.allPlan || 0;
    }

    if (data.planByStatus) {
      const { PENDING, IN_PROGRESS, COMPLETED, BLOCKED } = data.planByStatus;
      const statusData = [PENDING, IN_PROGRESS, COMPLETED, BLOCKED];

      if (planPieConfig.value.series[0]) {
        planPieConfig.value.series[0].data.forEach((item, idx) => {
          item.value = statusData[idx] || 0;
        });
      }
    } else {
      if (planPieConfig.value.series[0]) {
        planPieConfig.value.series[0].data.forEach((item) => {
          item.value = 0;
        });
      }
    }

    if (planPieChart) {
      planPieChart.setOption(planPieConfig.value);
      nextTick(() => planPieChart.resize());
    }
  };

  /**
   * Updates API charts with new data
   * <p>
   * Updates both API bar chart and API pie chart with API statistics
   * </p>
   *
   * @param data - API data from API
   */
  const updateApiCharts = (data: ApiData) => {
    // Update API pie chart
    if (apiPieConfig.value.title) {
      apiPieConfig.value.title.text = data.allApis || 0;
    }

    if (data.apisByMethod) {
      const { PUT = 0, POST = 0, DELETE = 0, GET = 0, HEAD = 0, PATCH = 0, OPTIONS = 0, TRACE = 0 } = data.apisByMethod;
      const methodData = [PUT, POST, HEAD, GET, DELETE, PATCH, OPTIONS, TRACE];

      if (apiPieConfig.value.series[0]) {
        apiPieConfig.value.series[0].data.forEach((item, idx) => {
          item.value = methodData[idx] || 0;
        });
      }
    } else {
      if (apiPieConfig.value.series[0]) {
        apiPieConfig.value.series[0].data.forEach(item => {
          item.value = 0;
        });
      }
    }

    if (apiPieChart) {
      apiPieChart.setOption(apiPieConfig.value);
      nextTick(() => {
        apiPieChart.resize();
      });
    }

    // Update API bar chart
    if (data.apisByStatus) {
      const { UNKNOWN = 0, IN_DESIGN = 0, IN_DEV = 0, DEV_COMPLETED = 0, RELEASED = 0 } = data.apisByStatus;
      const statusData = [UNKNOWN, IN_DESIGN, IN_DEV, DEV_COMPLETED, RELEASED];

      if (apiBarConfig.value.series?.[0]) {
        apiBarConfig.value.series[0].data.forEach((item, idx) => {
          item.value = statusData[idx] || 0;
        });
      }
    } else {
      if (apiBarConfig.value.series?.[0]) {
        apiBarConfig.value.series[0].data.forEach(item => {
          item.value = 0;
        });
      }
    }

    if (apiBarChart) {
      apiBarChart.setOption(apiBarConfig.value);
    }
  };

  /**
   * Updates task charts with new data
   * <p>
   * Updates both task bar chart and task pie chart with task statistics
   * </p>
   *
   * @param data - Task data from API
   */
  const updateTaskCharts = (data: TaskData) => {
    // Update task pie chart
    if (taskPieConfig.value.title) {
      taskPieConfig.value.title.text = data.allTask || 0;
    }

    if (data.taskByType) {
      const { STORY = 0, TASK = 0, BUG = 0, API_TEST = 0, SCENARIO_TEST = 0, REQUIREMENT = 0 } = data.taskByType;
      const typeData = [STORY, REQUIREMENT, TASK, BUG, API_TEST, SCENARIO_TEST];

      if (taskPieConfig.value.series[0]) {
        taskPieConfig.value.series[0].data.forEach((item, idx) => {
          item.value = typeData[idx] || 0;
        });
      }
    } else {
      if (taskPieConfig.value.series[0]) {
        taskPieConfig.value.series[0].data.forEach(item => {
          item.value = 0;
        });
      }
    }

    if (taskPieChart) {
      taskPieChart.setOption(taskPieConfig.value);
      nextTick(() => {
        taskPieChart.resize();
      });
    }

    // Update task bar chart
    if (data.taskByStatus) {
      const { PENDING = 0, IN_PROGRESS = 0, CONFIRMING = 0, COMPLETED = 0, CANCELED = 0 } = data.taskByStatus;
      const statusData = [CANCELED, COMPLETED, CONFIRMING, IN_PROGRESS, PENDING];

      if (taskBarConfig.value.series[0]) {
        taskBarConfig.value.series[0].data.forEach((item, idx) => {
          item.value = statusData[idx] || 0;
        });
      }
    } else {
      if (taskBarConfig.value.series[0]) {
        taskBarConfig.value.series[0].data.forEach((item) => {
          item.value = 0;
        });
      }
    }

    if (taskBarChart) {
      taskBarChart.setOption(taskBarConfig.value);
    }

    // Update sprint pie chart
    if (sprintPieConfig.value.title) {
      sprintPieConfig.value.title.text = data.allSprint || 0;
    }

    if (data.sprintByStatus) {
      const { PENDING, IN_PROGRESS, COMPLETED, BLOCKED } = data.sprintByStatus;
      const statusData = [PENDING, IN_PROGRESS, COMPLETED, BLOCKED];

      if (sprintPieConfig.value.series[0]) {
        sprintPieConfig.value.series[0].data.forEach((item, idx) => {
          item.value = statusData[idx] || 0;
        });
      }
    } else {
      if (sprintPieConfig.value.series[0]) {
        sprintPieConfig.value.series[0].data.forEach(item => {
          item.value = 0;
        });
      }
    }

    if (sprintPieChart) {
      sprintPieChart.setOption(sprintPieConfig.value);
      nextTick(() => sprintPieChart.resize());
    }
  };

  /**
   * Updates scenario chart with new data
   * <p>
   * Updates scenario pie chart with scenario statistics
   * </p>
   *
   * @param data - Scenario data from API
   */
  const updateScenarioChart = (data: ScenarioData) => {
    if (scenarioPieConfig.value.title) {
      scenarioPieConfig.value.title.text = data.allSce || 0;
    }

    if (data.sceByScriptType) {
      const { TEST_PERFORMANCE, TEST_STABILITY, TEST_FUNCTIONALITY, TEST_CUSTOMIZATION } = data.sceByScriptType;
      const typeData = [TEST_PERFORMANCE, TEST_STABILITY, TEST_FUNCTIONALITY, TEST_CUSTOMIZATION];

      if (scenarioPieConfig.value.series[0]) {
        scenarioPieConfig.value.series[0].data.forEach((item, idx) => {
          item.value = typeData[idx] || 0;
        });
      }
    } else {
      if (scenarioPieConfig.value.series[0]) {
        scenarioPieConfig.value.series[0].data.forEach((item) => {
          item.value = 0;
        });
      }
    }

    if (scenarioPieChart) {
      scenarioPieChart.setOption(scenarioPieConfig.value);
      nextTick(() => scenarioPieChart.resize());
    }
  };

  /**
   * Updates script chart with new data
   * <p>
   * Updates script bar chart with script statistics
   * </p>
   *
   * @param data - Script data from API
   */
  const updateScriptChart = (data: ScriptData) => {
    if (data.scriptByPluginName) {
      const names = Object.keys(data.scriptByPluginName);
      const values = Object.values(data.scriptByPluginName);

      if (names.length) {
        if (scriptBarConfig.value.series[0]) {
          scriptBarConfig.value.series[0].data = values;
        }
        if (scriptBarConfig.value.xAxis) {
          scriptBarConfig.value.xAxis.data = names;
        }

        if (names.length > 5) {
          if (scriptBarConfig.value.xAxis?.axisLabel) {
            scriptBarConfig.value.xAxis.axisLabel.rotate = -45;
          }
          if (scriptBarConfig.value.grid) {
            scriptBarConfig.value.grid.bottom = '60';
          }
        } else {
          if (scriptBarConfig.value.xAxis?.axisLabel) {
            scriptBarConfig.value.xAxis.axisLabel.rotate = 0;
          }
          if (scriptBarConfig.value.grid) {
            scriptBarConfig.value.grid.bottom = '30';
          }
        }
      } else {
        if (scriptBarConfig.value.series[0]) {
          scriptBarConfig.value.series[0].data = [0, 0, 0, 0];
        }
        if (scriptBarConfig.value.xAxis) {
          scriptBarConfig.value.xAxis.data = ['Http', 'WebSocket', 'Jdbc', 'Tcp'];
        }
        if (scriptBarConfig.value.xAxis?.axisLabel) {
          scriptBarConfig.value.xAxis.axisLabel.rotate = 0;
        }
        if (scriptBarConfig.value.grid) {
          scriptBarConfig.value.grid.bottom = '30';
        }
      }
    } else {
      if (scriptBarConfig.value.series[0]) {
        scriptBarConfig.value.series[0].data = [0, 0, 0, 0];
      }
      if (scriptBarConfig.value.xAxis) {
        scriptBarConfig.value.xAxis.data = ['Http', 'WebSocket', 'Jdbc', 'Tcp'];
      }
      if (scriptBarConfig.value.xAxis?.axisLabel) {
        scriptBarConfig.value.xAxis.axisLabel.rotate = 0;
      }
      if (scriptBarConfig.value.grid) {
        scriptBarConfig.value.grid.bottom = '30';
      }
    }

    if (scriptBarChart) {
      scriptBarChart.setOption(scriptBarConfig.value);
      nextTick(() => scriptBarChart.resize());
    }
  };

  /**
   * Resizes all charts for responsive behavior
   * <p>
   * Calls resize method on all chart instances
   * </p>
   */
  const resizeAllCharts = () => {
    const charts = [
      growthTrendChart,
      caseBarChart,
      casePieChart,
      apiBarChart,
      apiPieChart,
      taskBarChart,
      taskPieChart,
      planPieChart,
      sprintPieChart,
      scenarioPieChart,
      scriptBarChart
    ];

    charts.forEach(chart => {
      if (chart) {
        chart.resize();
      }
    });
  };

  /**
   * Handles right side chart resizing for responsive behavior
   * <p>
   * Adjusts chart configurations based on container width
   * </p>
   *
   * @param containerWidth - Width of the right side container
   */
  const handleRightSideResize = (containerWidth: number) => {
    const isSmallContainer = containerWidth < 350;

    [scenarioPieConfig, planPieConfig, sprintPieConfig].forEach(config => {
      if (config.value.series[0]) {
        if (isSmallContainer) {
          if (config.value.series[0].labelLine) {
            (config.value.series[0].labelLine as any).length = 5;
          }
          if (config.value.series[0].radius) {
            config.value.series[0].radius = ['40%', '60%'];
          }
        } else {
          if (config.value.series[0].labelLine) {
            delete (config.value.series[0].labelLine as any).length;
          }
          if (config.value.series[0].radius) {
            config.value.series[0].radius = ['50%', '70%'];
          }
        }
      }
    });

    // Update charts with new configurations
    if (scenarioPieChart) {
      scenarioPieChart.setOption(scenarioPieConfig.value, true);
    }
    if (planPieChart) {
      planPieChart.setOption(planPieConfig.value, true);
    }
    if (sprintPieChart) {
      sprintPieChart.setOption(sprintPieConfig.value, true);
    }
  };

  return {
    // Chart configurations
    growthTrendConfig,
    caseBarConfig,
    casePieConfig,
    apiBarConfig,
    apiPieConfig,
    taskBarConfig,
    taskPieConfig,
    planPieConfig,
    sprintPieConfig,
    scenarioPieConfig,
    scriptBarConfig,

    // Chart DOM references
    growthTrendRef,
    caseStatusRef,
    caseReviewRef,
    apiStatusRef,
    apiReviewRef,
    taskStatusRef,
    taskReviewRef,
    planReviewRef,
    sprintReviewRef,
    scenarioRef,
    scriptRef,

    // Methods
    setChartRefs,
    initializeCharts,
    updateGrowthTrendChart,
    updateCaseCharts,
    updateApiCharts,
    updateTaskCharts,
    updateScenarioChart,
    updateScriptChart,
    resizeAllCharts,
    handleRightSideResize
  };
}
