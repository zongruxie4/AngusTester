import { i18n } from '@xcan-angus/infra';
import { ChartConfig } from '../types';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string): string => value);

/**
 * Base pie chart configuration template
 */
const createBasePieConfig = (): ChartConfig => ({
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 600,
      color: '#777c83'
    },
    subtextStyle: {
      fontSize: 12,
      fontWeight: 600,
      color: '#777c83'
    }
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '10',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2,
    textStyle: { fontSize: 12 }
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: false,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
        borderColor: '#fff',
        borderWidth: 1
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
      data: []
    }
  ]
});

/**
 * Creates progress chart configuration
 */
export const createProgressChartConfig = (): ChartConfig => {
  const config = createBasePieConfig();
  config.series[0].data = [
    {
      name: t('kanban.cto.progress.incomplete'),
      value: 0,
      itemStyle: {
        color: 'rgb(246,159,42)'
      }
    },
    {
      name: t('kanban.cto.progress.completed'),
      value: 0,
      itemStyle: {
        color: '#52C41A'
      }
    }
  ];
  return config;
};

/**
 * Creates recent completion rate chart configuration
 */
export const createRecentCompletionRateConfig = (): ChartConfig => {
  const config = createBasePieConfig();
  config.title.left = '25%';
  config.title.top = '40%';
  config.title.itemGap = 40;
  config.title.subtext = t('kanban.cto.recentDelivery.completionRate');
  config.legend.top = 'middle';
  config.legend.left = '55%';
  config.legend.itemHeight = 10;
  config.legend.itemWidth = 10;
  config.legend.textStyle = { fontSize: 12 };
  config.series[0].center = ['25%', '45%'];
  config.series[0].radius = ['38%', '55%'];
  config.series[0].label.fontSize = 10;
  config.series[0].data = [
    {
      name: t('kanban.cto.recentDelivery.recentCompletionCount'),
      value: 0,
      itemStyle: { color: '#52C41A' }
    },
    {
      name: t('kanban.cto.recentDelivery.nonRecentCompletionCount'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates recent overdue rate chart configuration
 */
export const createRecentOverdueRateConfig = (): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = t('kanban.cto.recentDelivery.overdueRate');
  config.series[0].data = [
    {
      name: t('kanban.cto.recentDelivery.overdueRate'),
      value: 0,
      itemStyle: { color: 'rgba(245, 34, 45, 1)' }
    },
    {
      name: t('kanban.cto.recentDelivery.nonOverdueRate'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates recent completed workload chart configuration
 */
export const createRecentCompletedWorkloadConfig = (): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = t('kanban.cto.recentDelivery.deliveryWorkloadCompletionRate');
  config.series[0].data = [
    {
      name: t('kanban.cto.recentDelivery.recentCompletionAmount'),
      value: 0,
      itemStyle: { color: '#52C41A' }
    },
    {
      name: t('kanban.cto.recentDelivery.nonRecentCompletionAmount'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates recent saving rate chart configuration
 */
export const createRecentSavingRateConfig = (): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = t('kanban.cto.recentDelivery.deliveryWorkloadSavingRate');
  config.series[0].data = [
    {
      name: t('kanban.cto.recentDelivery.recentSavingAmount'),
      value: 0,
      itemStyle: { color: '#52C41A' }
    },
    {
      name: t('kanban.cto.recentDelivery.nonRecentSavingAmount'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates backlog task chart configuration
 */
export const createBacklogTaskConfig = (): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = t('kanban.cto.backlog.backlogTaskRatio');
  config.series[0].data = [
    {
      name: t('kanban.cto.backlog.backlogCount'),
      value: 0,
      itemStyle: { color: 'rgba(255, 165, 43, 1)' }
    },
    {
      name: t('kanban.cto.backlog.completionCount'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates backlog workload chart configuration
 */
export const createBacklogWorkloadConfig = (): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = t('kanban.cto.backlog.backlogWorkloadRatio');
  config.series[0].data = [
    {
      name: t('kanban.cto.backlog.backlogWorkload'),
      value: 0,
      itemStyle: { color: 'rgba(255, 165, 43, 1)' }
    },
    {
      name: t('kanban.cto.backlog.completionWorkload'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates overdue assessment chart configuration
 */
export const createOverdueAssessmentConfig = (): ChartConfig => {
  const config = createBasePieConfig();
  config.graphic = {
    type: 'text',
    left: '20%',
    bottom: 0,
    z: 0,
    silent: true,
    invisible: false,
    style: {
      text: t('kanban.cto.overdue.overdueTaskRatio')
    }
  };
  config.title.left = '35%';
  config.title.top = '40%';
  config.title.itemGap = 40;
  config.legend.top = 'middle';
  config.legend.right = '0';
  config.legend.itemHeight = 12;
  config.legend.itemWidth = 12;
  config.legend.textStyle = { fontSize: 12 };
  config.series[0].labelLine.length2 = 5;
  config.series[0].data = [
    {
      name: t('kanban.cto.overdue.overdueCount'),
      value: 0,
      itemStyle: { color: 'rgba(245, 34, 45, 1)' }
    },
    {
      name: t('kanban.cto.overdue.nonOverdueCount'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates unplanned work chart configuration
 */
export const createUnplannedWorkConfig = (): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = t('kanban.cto.unplanned.unplannedTaskRatio');
  config.series[0].data = [
    {
      name: t('kanban.cto.unplanned.unplannedCount'),
      value: 0,
      itemStyle: { color: 'gold' }
    },
    {
      name: t('kanban.cto.unplanned.nonUnplannedCount'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates unplanned workload chart configuration
 */
export const createUnplannedWorkloadConfig = (): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = t('kanban.cto.unplanned.unplannedWorkloadRatio');
  config.series[0].data = [
    {
      name: t('kanban.cto.unplanned.unplannedAmount'),
      value: 0,
      itemStyle: { color: 'gold' }
    },
    {
      name: t('kanban.cto.unplanned.nonUnplannedAmount'),
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates failure assessment chart configuration
 */
export const createFailureAssessmentConfig = (titles: string[], color: string): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = titles[0];
  config.series[0].data = [
    {
      name: titles[0],
      value: 0,
      itemStyle: { color }
    },
    {
      name: titles[1],
      value: 0,
      itemStyle: { color: 'rgba(45, 142, 255, 1)' }
    }
  ];
  return config;
};

/**
 * Creates task type chart configuration
 */
export const createTaskTypeConfig = (): ChartConfig => ({
  title: {},
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    left: '75%',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2,
    textStyle: { fontSize: 12 }
  },
  series: [
    {
      name: t('kanban.cto.taskTypes.total'),
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
        borderColor: '#fff',
        borderWidth: 1
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
          name: t('kanban.cto.taskTypes.story'),
          value: 0,
          itemStyle: { color: 'rgba(136, 185, 242, 1)' }
        },
        {
          name: t('kanban.cto.taskTypes.requirement'),
          value: 0,
          itemStyle: { color: 'rgba(201, 119, 255, 1)' }
        },
        {
          name: t('kanban.cto.taskTypes.task'),
          value: 0,
          itemStyle: { color: 'rgba(45, 142, 255, 1)' }
        },
        {
          name: t('kanban.cto.taskTypes.bug'),
          value: 0,
          itemStyle: { color: 'rgba(245, 34, 45, 1)' }
        },
        {
          name: t('kanban.cto.taskTypes.apiTest'),
          value: 0,
          itemStyle: { color: 'rgb(251,164,46)' }
        },
        {
          name: t('kanban.cto.taskTypes.scenarioTest'),
          value: 0,
          itemStyle: { color: 'rgba(0,119,255,1)' }
        }
      ]
    }
  ]
});

/**
 * Creates task status chart configuration
 */
export const createTaskStatusConfig = (): ChartConfig => ({
  title: {},
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    left: '75%',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2,
    textStyle: { fontSize: 12 }
  },
  series: [
    {
      name: t('kanban.cto.taskTypes.total'),
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
        borderColor: '#fff',
        borderWidth: 1
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
          name: t('kanban.cto.taskStatus.pendingConfirmation'),
          value: 0,
          itemStyle: { color: '#7F91FF' }
        },
        {
          name: t('kanban.cto.taskStatus.inProgress'),
          value: 0,
          itemStyle: { color: '#FF8100' }
        },
        {
          name: t('kanban.cto.taskStatus.pending'),
          value: 0,
          itemStyle: { color: '#FFB925' }
        },
        {
          name: t('kanban.cto.taskStatus.completed'),
          value: 0,
          itemStyle: { color: '#52C41A' }
        },
        {
          name: t('kanban.cto.taskStatus.cancelled'),
          value: 0,
          itemStyle: { color: 'rgba(200, 202, 208, 1)' }
        }
      ]
    }
  ]
});

/**
 * Creates lead time chart configuration
 */
export const createLeadTimeConfig = (): ChartConfig => ({
  grid: {
    left: '40',
    right: '30',
    bottom: '20',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [
      t('kanban.cto.deliveryCycle.average'),
      t('kanban.cto.deliveryCycle.minimum'),
      t('kanban.cto.deliveryCycle.maximum'),
      'P50', 'P75', 'P90', 'P95'
    ],
    axisLabel: {
      interval: 0,
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
      data: [0, 0, 0, 0, 0, 0, 0],
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
 * Creates API test chart configuration
 */
export const createTestConfig = (titles: string[]): ChartConfig => {
  const config = createRecentCompletionRateConfig();
  config.title.subtext = titles[0];
  config.series[0].data = [
    {
      name: titles[0],
      value: 0,
      itemStyle: { color: '#52C41A' }
    },
    {
      name: titles[1],
      value: 0,
      itemStyle: { color: 'rgb(246,159,42)' }
    }
  ];
  return config;
};

/**
 * Creates test status chart configuration
 */
export const createTestStatusConfig = (): ChartConfig => ({
  title: {},
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2,
    textStyle: { fontSize: 12 }
  },
  series: [
    {
      name: t('kanban.cto.taskTypes.total'),
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
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
          name: t('kanban.cto.testStatus.pendingTest'),
          value: 0,
          itemStyle: { color: 'rgba(45, 142, 255, 1)' }
        },
        {
          name: t('kanban.cto.testStatus.testPassed'),
          value: 0,
          itemStyle: { color: 'rgba(82, 196, 26, 1)' }
        },
        {
          name: t('kanban.cto.testStatus.testFailed'),
          value: 0,
          itemStyle: { color: 'rgba(245, 34, 45, 1)' }
        },
        {
          name: t('kanban.cto.testStatus.blocked'),
          value: 0,
          itemStyle: { color: 'rgba(255, 165, 43, 1)' }
        },
        {
          name: t('kanban.cto.testStatus.cancelled'),
          value: 0,
          itemStyle: { color: 'rgba(45, 142, 255, 1)' }
        }
      ]
    }
  ]
});

/**
 * Creates review status chart configuration
 */
export const createReviewStatusConfig = (): ChartConfig => ({
  title: {},
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2,
    textStyle: { fontSize: 12 }
  },
  series: [
    {
      name: t('kanban.cto.taskTypes.total'),
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
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
          name: t('kanban.cto.reviewStatus.pendingReview'),
          value: 0,
          itemStyle: { color: 'rgba(201, 119, 255, 1)' }
        },
        {
          name: t('kanban.cto.reviewStatus.reviewPassed'),
          value: 0,
          itemStyle: { color: 'rgba(82, 196, 26, 1)' }
        },
        {
          name: t('kanban.cto.reviewStatus.reviewFailed'),
          value: 0,
          itemStyle: { color: 'rgba(245, 34, 45, 1)' }
        }
      ]
    }
  ]
});
