import { i18n, enumUtils, ScriptType } from '@xcan-angus/infra';
import { TaskType, TaskStatus, TaskSprintStatus, FuncPlanStatus, CaseTestResult, ApiStatus } from '@/enums/enums';

import { ChartConfig, ChartSeriesColorConfig, MethodColorConfig, RankIconConfig, TargetDataCategory } from '../types';

import ReviewStatus from '@/components/test/ReviewStatus.vue';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string): string => value);

/**
 * Chart configuration management composable for data assets dashboard
 *
 * @returns Object containing chart configuration factory functions
 */
export function useChartConfigs () {
  /** Chart series color configuration */
  const chartSeriesColorConfig: ChartSeriesColorConfig = {
    0: '84,112,198',
    1: '145,204,117',
    2: '250,200,88',
    3: '238,102,102',
    4: '115,192,222',
    5: '59,162,114',
    6: '252,132,82',
    7: '154,96,180',
    8: '234,124,204'
  };

  /** HTTP method color configuration */
  const methodColorConfig: MethodColorConfig = {
    GET: 'rgba(45, 142, 255, 1)',
    HEAD: '#67D7FF',
    POST: 'rgba(51, 183, 130, 1)',
    PUT: 'rgba(255, 167, 38, 1)',
    PATCH: 'rgba(171, 71, 188, 1)',
    DELETE: 'rgba(255, 82, 82, 1)',
    OPTIONS: 'rgba(0, 150, 136, 1)',
    TRACE: '#7F91FF'
  };

  /** Target data category mapping */
  const targetDataCategory: TargetDataCategory = {
    TEST_CUSTOMIZATION: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_CUSTOMIZATION),
    TEST_FUNCTIONALITY: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_FUNCTIONALITY),
    TEST_PERFORMANCE: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_PERFORMANCE),
    TEST_STABILITY: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_STABILITY),
    SERVICES: t('common.service'),
    APIS: t('common.api'),
    CASES: t('common.useCase'),
    PLAN: t('common.plan'),
    SPRINT: t('common.sprint'),
    TASK_SPRINT: t('common.sprint'),
    TASK: t('common.issue'),
    MOCK_APIS: t('common.mockApis'),
    MOCK_PUSHBACK: t('kanban.dataAssets.categories.pushback'),
    MOCK_RESPONSE: t('kanban.dataAssets.categories.response'),
    MOCK_SERVICE: t('common.mockService'),
    DATA_DATASET: t('common.dataset'),
    DATA_DATASOURCE: t('common.datasource'),
    DATA_VARIABLE: t('common.variable'),
    TOTAL: t('common.total'),
    REPORT: t('common.report'),
    REPORT_RECORD: t('common.reportRecord')
  };

  /** Rank icon configuration */
  const rankIconConfig: RankIconConfig = {
    0: 'icon-diyiming',
    1: 'icon-dierming',
    2: 'icon-disanming'
  };

  /**
   * Creates growth trend chart configuration
   */
  const createGrowthTrendConfig = (): ChartConfig => ({
    grid: {
      left: '5%',
      right: '20',
      bottom: '60',
      top: 20
    },
    legend: {
      show: true,
      bottom: 0,
      type: 'plain'
    },
    tooltip: {
      show: true,
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value',
      min: function (value: any) {
        if (value.max < 1) {
          return 10;
        } else {
          return undefined;
        }
      }
    },
    series: [
      {
        name: 'api',
        data: [],
        type: 'line',
        smooth: true
      }
    ]
  });

  /**
   * Creates case bar chart configuration
   */
  const createCaseBarConfig = (): ChartConfig => ({
    title: {
      text: '',
      textStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {},
    grid: {
      top: '8%',
      left: '3%',
      right: '6%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      axisTick: { show: false },
      splitLine: { show: false },
      axisLine: { show: false },
      data: [
        enumUtils.getEnumDescription(CaseTestResult, CaseTestResult.CANCELED),
        enumUtils.getEnumDescription(CaseTestResult, CaseTestResult.BLOCKED),
        enumUtils.getEnumDescription(CaseTestResult, CaseTestResult.NOT_PASSED),
        enumUtils.getEnumDescription(CaseTestResult, CaseTestResult.PASSED),
        enumUtils.getEnumDescription(CaseTestResult, CaseTestResult.PENDING)
      ]
    },
    series: [
      {
        type: 'bar',
        showBackground: true,
        barMaxWidth: '16',
        data: [
          {
            value: 0,
            itemStyle: { color: 'rgb(246,159,42)' }
          },
          {
            value: 0,
            itemStyle: { color: 'rgba(255, 165, 43, 1)' }
          },
          {
            value: 0,
            itemStyle: { color: 'rgba(245, 34, 45, 1)' }
          },
          {
            value: 0,
            itemStyle: { color: 'rgba(45, 142, 255, 1)' }
          },
          {
            value: 0,
            itemStyle: { color: 'rgba(82, 196, 26, 1)' }
          }
        ]
      }
    ]
  });

  /**
   * Creates case pie chart configuration
   */
  const createCasePieConfig = (): ChartConfig => ({
    title: {
      text: 0,
      subtext: t('chart.total'),
      left: '29.5%',
      top: '35%',
      padding: 2,
      textAlign: 'center',
      textStyle: {
        fontSize: 14,
        width: 120,
        fontWeight: 'bolder'
      },
      subtextStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'middle',
      left: '65%',
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14
    } as any,
    series: [
      {
        name: t('chart.total'),
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['30%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{c}'
        },
        itemStyle: {
          borderRadius: [5],
          color: 'rgba(45, 142, 255, 1)'
        },
        emphasis: {
          label: {
            show: true
          }
        },
        labelLine: {
          show: true
        },
        data: [
          {
            name: enumUtils.getEnumDescription(ReviewStatus, ReviewStatus.PENDING),
            value: 0,
            itemStyle: { color: 'rgba(201, 119, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(ReviewStatus, ReviewStatus.PASSED),
            value: 0,
            itemStyle: { color: 'rgba(82, 196, 26, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(ReviewStatus, ReviewStatus.FAILED),
            value: 0,
            itemStyle: { color: 'rgba(245, 34, 45, 1)' }
          }
        ]
      }
    ]
  } as unknown as ChartConfig);

  /**
   * Creates API bar chart configuration
   */
  const createApiBarConfig = (): ChartConfig => ({
    title: {
      text: '',
      textStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {},
    grid: {
      top: '8%',
      left: '3%',
      right: '6%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      axisTick: { show: false },
      splitLine: { show: false },
      axisLine: { show: false },
      data: [
        enumUtils.getEnumDescription(ApiStatus, ApiStatus.UNKNOWN),
        enumUtils.getEnumDescription(ApiStatus, ApiStatus.IN_DESIGN),
        enumUtils.getEnumDescription(ApiStatus, ApiStatus.IN_DEV),
        enumUtils.getEnumDescription(ApiStatus, ApiStatus.DEV_COMPLETED),
        enumUtils.getEnumDescription(ApiStatus, ApiStatus.RELEASED)
      ]
    },
    series: [
      {
        type: 'bar',
        showBackground: true,
        barMaxWidth: '16',
        data: [
          {
            value: 0,
            itemStyle: { color: 'rgba(200, 202, 208, 1)' }
          },
          {
            value: 0,
            itemStyle: { color: '#52C41A' }
          },
          {
            value: 0,
            itemStyle: { color: '#FFB925' }
          },
          {
            value: 0,
            itemStyle: { color: '#FF8100' }
          },
          {
            value: 0,
            itemStyle: { color: 'rgba(45, 142, 255, 1)' }
          }
        ]
      }
    ]
  });

  /**
   * Creates API pie chart configuration
   */
  const createApiPieConfig = (): ChartConfig => ({
    title: {
      text: 0,
      subtext: 'Total',
      left: '29.5%',
      top: '35%',
      padding: 2,
      textAlign: 'center',
      textStyle: {
        fontSize: 14,
        width: 120,
        fontWeight: 'bolder'
      },
      subtextStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'middle',
      left: '65%',
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14,
      itemGap: 14
    } as any,
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['30%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{c}'
        },
        itemStyle: {
          borderRadius: [5],
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            show: true
          }
        },
        labelLine: {
          show: true
        },
        data: [
          {
            name: 'PUT',
            value: 0,
            itemStyle: { color: methodColorConfig.PUT }
          },
          {
            name: 'POST',
            value: 0,
            itemStyle: { color: methodColorConfig.POST }
          },
          {
            name: 'HEAD',
            value: 0,
            itemStyle: { color: methodColorConfig.HEAD }
          },
          {
            name: 'GET',
            value: 0,
            itemStyle: { color: methodColorConfig.GET }
          },
          {
            name: 'DELETE',
            value: 0,
            itemStyle: { color: methodColorConfig.DELETE }
          },
          {
            name: 'PATCH',
            value: 0,
            itemStyle: { color: methodColorConfig.PATCH }
          },
          {
            name: 'OPTIONS',
            value: 0,
            itemStyle: { color: methodColorConfig.OPTIONS }
          },
          {
            name: 'TRACE',
            value: 0,
            itemStyle: { color: methodColorConfig.TRACE }
          }
        ]
      }
    ]
  } as unknown as ChartConfig);

  /**
   * Creates task bar chart configuration
   */
  const createTaskBarConfig = (): ChartConfig => ({
    title: {
      text: '',
      textStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {},
    grid: {
      top: '8%',
      left: '3%',
      right: '6%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      axisTick: { show: false },
      splitLine: { show: false },
      axisLine: { show: false },
      data: [
        enumUtils.getEnumDescription(TaskStatus, TaskStatus.CANCELED),
        enumUtils.getEnumDescription(TaskStatus, TaskStatus.COMPLETED),
        enumUtils.getEnumDescription(TaskStatus, TaskStatus.CONFIRMING),
        enumUtils.getEnumDescription(TaskStatus, TaskStatus.IN_PROGRESS),
        enumUtils.getEnumDescription(TaskStatus, TaskStatus.PENDING)
      ]
    },
    series: [
      {
        type: 'bar',
        showBackground: true,
        barMaxWidth: '16',
        data: [
          {
            value: 0,
            itemStyle: { color: 'rgba(200, 202, 208, 1)' }
          },
          {
            value: 0,
            itemStyle: { color: '#52C41A' }
          },
          {
            value: 0,
            itemStyle: { color: '#FFB925' }
          },
          {
            value: 0,
            itemStyle: { color: '#FF8100' }
          },
          {
            value: 0,
            itemStyle: { color: 'rgba(45, 142, 255, 1)' }
          }
        ]
      }
    ]
  });

  /**
   * Creates task pie chart configuration
   */
  const createTaskPieConfig = (): ChartConfig => ({
    title: {
      text: 0,
      subtext: t('chart.total'),
      left: '29.5%',
      top: '35%',
      padding: 2,
      textAlign: 'center',
      textStyle: {
        fontSize: 14,
        width: 120,
        fontWeight: 'bolder'
      },
      subtextStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'middle',
      left: '65%',
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14,
      itemGap: 6
    } as any,
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['30%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{c}'
        },
        itemStyle: {
          borderRadius: [5],
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            show: true
          }
        },
        labelLine: {
          show: true
        },
        data: [
          {
            name: enumUtils.getEnumDescription(TaskType, TaskType.STORY),
            value: 0,
            itemStyle: { color: 'rgba(45, 142, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskType, TaskType.REQUIREMENT),
            value: 0,
            itemStyle: { color: 'rgba(201, 119, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskType, TaskType.TASK),
            value: 0,
            itemStyle: { color: 'rgba(255, 165, 43, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskType, TaskType.BUG),
            value: 0,
            itemStyle: { color: 'rgba(245, 34, 45, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskType, TaskType.API_TEST),
            value: 0,
            itemStyle: { color: 'rgba(82, 196, 26, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskType, TaskType.SCENARIO_TEST),
            value: 0,
            itemStyle: { color: 'rgb(77,106,204)' }
          }
        ]
      }
    ]
  } as unknown as ChartConfig);

  /**
   * Creates plan pie chart configuration
   */
  const createPlanPieConfig = (): ChartConfig => ({
    title: {
      text: 0,
      subtext: t('chart.total'),
      left: '29.5%',
      top: '35%',
      padding: 2,
      textAlign: 'center',
      textStyle: {
        fontSize: 14,
        width: 120,
        fontWeight: 'bolder'
      },
      subtextStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'middle',
      left: '65%',
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14,
      itemGap: 12
    } as any,
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['30%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{c}'
        },
        emphasis: {
          label: {
            show: true
          }
        },
        labelLine: {
          show: true
        },
        itemStyle: {
          borderRadius: [5],
          borderColor: '#fff',
          borderWidth: 1
        },
        data: [
          {
            name: enumUtils.getEnumDescription(FuncPlanStatus, FuncPlanStatus.PENDING),
            value: 0,
            itemStyle: { color: 'rgba(45, 142, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(FuncPlanStatus, FuncPlanStatus.IN_PROGRESS),
            value: 0,
            itemStyle: { color: 'rgba(103, 215, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(FuncPlanStatus, FuncPlanStatus.COMPLETED),
            value: 0,
            itemStyle: { color: 'rgba(82, 196, 26, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(FuncPlanStatus, FuncPlanStatus.BLOCKED),
            value: 0,
            itemStyle: { color: 'rgba(245, 34, 45, 1)' }
          }
        ]
      }
    ]
  } as unknown as ChartConfig);

  /**
   * Creates sprint pie chart configuration
   */
  const createSprintPieConfig = (): ChartConfig => ({
    title: {
      text: 0,
      subtext: t('chart.total'),
      left: '29.5%',
      top: '35%',
      padding: 2,
      textAlign: 'center',
      textStyle: {
        fontSize: 14,
        width: 120,
        fontWeight: 'bolder'
      },
      subtextStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'middle',
      left: '65%',
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14,
      itemGap: 12
    } as any,
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['30%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{c}'
        },
        itemStyle: {
          borderRadius: [5],
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            show: true
          }
        },
        labelLine: {
          show: true
        },
        data: [
          {
            name: enumUtils.getEnumDescription(TaskSprintStatus, TaskSprintStatus.PENDING),
            value: 0,
            itemStyle: { color: 'rgba(45, 142, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskSprintStatus, TaskSprintStatus.IN_PROGRESS),
            value: 0,
            itemStyle: { color: 'rgba(103, 215, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskSprintStatus, TaskSprintStatus.COMPLETED),
            value: 0,
            itemStyle: { color: 'rgba(82, 196, 26, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(TaskSprintStatus, TaskSprintStatus.BLOCKED),
            value: 0,
            itemStyle: { color: 'rgba(245, 34, 45, 1)' }
          }
        ]
      }
    ]
  } as unknown as ChartConfig);

  /**
   * Creates scenario pie chart configuration
   */
  const createScenarioPieConfig = (): ChartConfig => ({
    title: {
      text: 0,
      subtext: t('chart.total'),
      left: '29.5%',
      top: '35%',
      padding: 2,
      textAlign: 'center',
      textStyle: {
        fontSize: 14,
        width: 120,
        fontWeight: 'bolder'
      },
      subtextStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'middle',
      left: '65%',
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14,
      itemGap: 12
    } as any,
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['30%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{c}'
        },
        itemStyle: {
          borderRadius: [5],
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            show: true
          }
        },
        labelLine: {
          show: true
        },
        data: [
          {
            name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_PERFORMANCE),
            value: 0,
            itemStyle: { color: 'rgba(45, 142, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_STABILITY),
            value: 0,
            itemStyle: { color: 'rgba(201, 119, 255, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_FUNCTIONALITY),
            value: 0,
            itemStyle: { color: 'rgba(255, 102, 0, 1)' }
          },
          {
            name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_CUSTOMIZATION),
            value: 0,
            itemStyle: { color: 'rgba(82, 196, 26, 1)' }
          }
        ]
      }
    ]
  } as unknown as ChartConfig);

  /**
   * Creates script bar chart configuration
   */
  const createScriptBarConfig = (): ChartConfig => ({
    grid: {
      left: '60',
      right: '30',
      bottom: '60',
      top: '20'
    },
    xAxis: {
      type: 'category',
      data: ['http', 'websocket', 'jdbc', 'tcp'],
      axisLabel: {
        interval: 0,
        rotate: -45,
        overflow: 'break'
      }
    },
    yAxis: {
      type: 'value'
    },
    tooltip: {
      show: true
    },
    series: [
      {
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)',
          borderRadius: [5, 5, 0, 0]
        },
        data: [0, 0, 0, 0],
        type: 'bar',
        barMaxWidth: '20',
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  });

  /**
   * Creates report bar chart configuration
   */
  const createReportBarConfig = (): ChartConfig => ({
    grid: {
      left: '60',
      right: '20',
      bottom: '30',
      top: '20'
    },
    xAxis: {
      type: 'category',
      data: [
        t('common.api'),
        t('common.useCase'),
        t('common.project'),
        t('common.scenario'),
        t('common.issue'),
        t('common.execution')
      ],
      axisLabel: {
        interval: 0,
        rotate: -15,
        overflow: 'break'
      }
    },
    yAxis: {
      type: 'value'
    },
    tooltip: {
      show: true
    },
    series: [
      {
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)',
          borderRadius: [5, 5, 0, 0]
        },
        barMaxWidth: '20',
        data: [],
        type: 'bar',
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  });

  return {
    // Configuration objects
    chartSeriesColorConfig,
    methodColorConfig,
    targetDataCategory,
    rankIconConfig,

    // Factory functions
    createGrowthTrendConfig,
    createCaseBarConfig,
    createCasePieConfig,
    createApiBarConfig,
    createApiPieConfig,
    createTaskBarConfig,
    createTaskPieConfig,
    createPlanPieConfig,
    createSprintPieConfig,
    createScenarioPieConfig,
    createScriptBarConfig,
    createReportBarConfig
  };
}
