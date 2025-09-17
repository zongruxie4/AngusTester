import { useI18n } from 'vue-i18n';
import { ChartConfig } from '../types';
import noData from '../Image/nodata.png';

/**
 * <p>
 * Creates task type chart configuration
 * </p>
 */
export const createTaskTypeConfig = (): ChartConfig => {
  const { t } = useI18n();
  return {
  title: {},
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    left: '65%',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  grid: {
    left: 0,
    right: 0,
    bottom: 0,
    top: 0
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: t('kanban.effectiveness.total'),
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        color: 'rgba(136, 185, 242, 1)',
        borderRadius: [5]
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: [
        {
          name: t('kanban.effectiveness.taskType.story'),
          value: 0,
          itemStyle: {
            color: 'rgba(136, 185, 242, 1)'
          }
        },
        {
          name: t('kanban.effectiveness.taskType.requirement'),
          value: 0,
          itemStyle: {
            color: 'rgba(201, 119, 255, 1)'
          }
        },
        {
          name: t('kanban.effectiveness.taskType.task'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: t('kanban.effectiveness.taskType.defect'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: t('kanban.effectiveness.taskType.apiTest'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('kanban.effectiveness.taskType.scenarioTest'),
          value: 0,
          itemStyle: {
            color: 'rgba(0,119,255,1)'
          }
        }
      ]
    }
  ]
  };
};

/**
 * <p>
 * Creates burn down chart configuration
 * </p>
 */
export const createBurnDownConfig = (): ChartConfig => {
  const { t } = useI18n();
  return {
  grid: {
    left: '30',
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
      name: t('kanban.effectiveness.remaining'),
      data: [],
      type: 'line',
      smooth: true
    },
    {
      name: t('kanban.effectiveness.expected'),
      data: [],
      type: 'line'
    }
  ]
  };
};

/**
 * <p>
 * Creates base bar chart configuration with no data image
 * </p>
 */
const createBaseBarConfig = (): ChartConfig => ({
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 50
  },
  toolbox: {
    show: true
  },
  xAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: []
});

/**
 * <p>
 * Creates target count chart configuration
 * </p>
 */
export const createTargetCountConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.series = [
    {
      name: t('kanban.effectiveness.total'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: t('kanban.effectiveness.completedCount'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ];
  return config;
};

/**
 * <p>
 * Creates target rate chart configuration
 * </p>
 */
export const createTargetRateConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.grid.bottom = 0;
  config.tooltip.valueFormatter = (value: number) => value + '%';
  config.series = [
    {
      name: t('kanban.effectiveness.rate'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ];
  return config;
};

/**
 * <p>
 * Creates workload chart configuration
 * </p>
 */
export const createWorkloadConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.legend = {
    show: true,
    bottom: 0
  };
  config.series = [
    {
      name: t('kanban.effectiveness.total'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: t('kanban.effectiveness.completedCount'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ];
  return config;
};

/**
 * <p>
 * Creates workload rate chart configuration
 * </p>
 */
export const createWorkloadRateConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.grid.bottom = 0;
  config.series = [
    {
      name: t('kanban.effectiveness.rate'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ];
  return config;
};

/**
 * <p>
 * Creates overdue chart configuration
 * </p>
 */
export const createOverdueConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.legend = {
    show: true,
    bottom: 0
  };
  config.series = [
    {
      name: t('kanban.effectiveness.total'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: t('kanban.effectiveness.overdueCount'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(245, 34, 45, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ];
  return config;
};

/**
 * <p>
 * Creates overdue rate chart configuration
 * </p>
 */
export const createOverdueRateConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.grid.bottom = 0;
  config.tooltip.valueFormatter = (value: number) => value + '%';
  config.series = [
    {
      name: t('kanban.effectiveness.rate'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(245, 34, 45, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ];
  return config;
};

/**
 * <p>
 * Creates one time passed test chart configuration
 * </p>
 */
export const createOneTimePassedTestConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.legend = {
    show: true,
    bottom: 0
  };
  config.series = [
    {
      name: t('kanban.effectiveness.total'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: t('kanban.effectiveness.passedCount'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ];
  return config;
};

/**
 * <p>
 * Creates one time passed test rate chart configuration
 * </p>
 */
export const createOneTimePassedTestRateConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.grid.bottom = 0;
  config.tooltip.valueFormatter = (value: number) => value + '%';
  config.series = [
    {
      name: t('kanban.effectiveness.rate'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ];
  return config;
};

/**
 * <p>
 * Creates one time unpassed test chart configuration
 * </p>
 */
export const createOneTimeUnpassedTestConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.legend = {
    show: true,
    bottom: 0
  };
  config.series = [
    {
      name: t('kanban.effectiveness.total'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: t('kanban.effectiveness.oneTimeTestPassedCount'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ];
  return config;
};

/**
 * <p>
 * Creates one time unpassed test rate chart configuration
 * </p>
 */
export const createOneTimeUnpassedTestRateConfig = (): ChartConfig => {
  const { t } = useI18n();
  const config = createBaseBarConfig();
  config.grid.bottom = 0;
  config.series = [
    {
      name: t('kanban.effectiveness.rate'),
      type: 'bar',
      itemStyle: {
        color: 'rgba(255, 165, 43, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ];
  return config;
};
