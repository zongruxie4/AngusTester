<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import { kanban } from '@/api/tester';
import { throttle } from 'throttle-debounce';

const { t } = useI18n();

interface Props {
  projectId: string;
  sprintId: string;
  planId: string;
  creatorObjectType: 'GROUP' | 'USER' | 'DEPT';
  creatorObjectId: string;
  createdDateStart: string;
  createdDateEnd: string;
  countType: 'task' | 'useCase';
  onShow: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userId: undefined,
  sprintId: undefined,
  planId: undefined,
  creatorObjectType: undefined,
  creatorObjectId: undefined,
  createdDateStart: undefined,
  createdDateEnd: undefined,
  countType: 'task',
  onShow: false
});

const recentDate = ref('today');
const recentDateOpt = [
  {
    value: 'today',
    label: t('kanban.cto.recentDate.today')
  },
  {
    value: 'lastWeek',
    label: t('kanban.cto.recentDate.lastWeek')
  },
  {
    value: 'lastMonth',
    label: t('kanban.cto.recentDate.lastMonth')
  }
];

const params = computed(() => {
  const { countType, planId, sprintId, onShow, ...other } = props;
  if (countType === 'task') {
    return {
      ...other,
      planId: sprintId
    };
  }
  return {
    ...other,
    planId
  };
});

const memberNum = ref(0);

let progressEchart;
const progressDatas = ref({});
const progressRef = ref();
const progressEchartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 10,
      color: '#000'
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
    itemGap: 2
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
      data: [
        {
          name: t('kanban.cto.progress.incomplete'),
          value: 0,
          itemStyle: {
            color: 'rgba(217, 217, 217, 1)'
          }
        },
        {
          name: t('kanban.cto.progress.completed'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        }
      ]
    }
  ]
};

// 近期完成率
let recentCompleteRateEchart;
const recentDeliveryDatas = ref({});
const recentCompleteRef = ref();
const recentCompleteEchartConfig = {
  ...progressEchartConfig,
  title: {
    ...progressEchartConfig.title,
    left: '25%',
    top: '40%',
    itemGap: 40,
    subtext: t('kanban.cto.recentMetrics.completionRate')
  },
  legend: {
    top: 'middle',
    left: '55%',
    orient: 'vertical',
    itemHeight: 10,
    itemWidth: 10,
    itemGap: 2,
    textStyle: {
      fontSize: 10
    }
  },
  series: [{
    ...progressEchartConfig.series[0],
    center: ['25%', '45%'],
    radius: ['38%', '55%'],
    label: {
      show: false,
      formatter: '{c}',
      fontSize: 10
    },
    labelLine: {
      show: true,
      length: 5
    },
    data: [
      {
        name: t('kanban.cto.recentMetrics.recentCompletionCount'),
        value: 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: t('kanban.cto.recentMetrics.nonRecentCompletionCount'),
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

let recentOverdueRateChart;
const recentOverdueRef = ref();
const recentoverdueEchartConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: t('kanban.cto.recentMetrics.overdueRate')
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: t('kanban.cto.recentMetrics.recentOverdueRate'),
        value: 0,
        itemStyle: {
          color: 'rgba(245, 34, 45, 1)'
        }
      },
      {
        name: t('kanban.cto.recentMetrics.nonRecentOverdueRate'),
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 近期交付工作量
let recentCompletedWorkloadChart;
const recentCompletedWorkloadRef = ref();
const recentCompletedWorkloadEchartConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: t('kanban.cto.recentMetrics.deliveryWorkloadCompletionRate')
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: t('kanban.cto.recentMetrics.recentCompletionAmount'),
        value: 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: t('kanban.cto.recentMetrics.nonRecentCompletionAmount'),
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 近期工作量节省率
let recentSavingRateChart;
const recentSavingRateRef = ref();
const recentSavingRateConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: t('kanban.cto.recentMetrics.deliveryWorkloadSavingRate')
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: t('kanban.cto.recentMetrics.recentSavingAmount'),
        value: 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: t('kanban.cto.recentMetrics.nonRecentSavingAmount'),
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 近期时间变更
const handleRecentDateChange = () => {
  const { totalNum, completedNum, completedRate, totalOverdueNum, overdueNum, overdueRate, completedWorkload, savingWorkload, completedWorkloadRate, overdueWorkloadRate, savingWorkloadRate, totalWorkload } = recentDeliveryDatas.value[recentDate.value] || {};
  // 近期完成率
  const recentCompleteRateData = [completedNum, totalNum - completedNum];
  recentCompleteEchartConfig.title.text = completedRate + '%';
  recentCompleteRateData.forEach((value, idx) => {
    recentCompleteEchartConfig.series[0].data[idx].value = value;
  });
  recentCompleteRateEchart.setOption(recentCompleteEchartConfig);

  // 近期逾期率
  const recentOverdueRateData = [overdueNum, totalNum - overdueNum];
  recentoverdueEchartConfig.title.text = overdueRate + '%';
  recentOverdueRateData.forEach((value, idx) => {
    recentoverdueEchartConfig.series[0].data[idx].value = value;
  });
  recentOverdueRateChart.setOption(recentoverdueEchartConfig);

  // 交付工作量
  const completedWorkloadData = [completedWorkload, totalNum - completedWorkload];
  recentCompletedWorkloadEchartConfig.title.text = completedWorkloadRate + '%';
  completedWorkloadData.forEach((value, idx) => {
    recentCompletedWorkloadEchartConfig.series[0].data[idx].value = value;
  });
  recentCompletedWorkloadChart.setOption(recentCompletedWorkloadEchartConfig);

  // 节省工作量
  const recentSavingWorkloadData = [savingWorkload, totalWorkload - savingWorkload];
  recentSavingRateConfig.title.text = savingWorkloadRate + '%';
  recentSavingWorkloadData.forEach((value, idx) => {
    recentSavingRateConfig.series[0].data[idx].value = value;
  });
  recentSavingRateChart.setOption(recentSavingRateConfig);
};

// 任务积压数
const backloggedData = ref({});
let blockTaskChart;
const blockTaskRef = ref();
const blockTaskConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: t('kanban.cto.backlog.backlogTaskRatio')
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: t('kanban.cto.backlog.backlogCount'),
        value: 0,
        itemStyle: {
          color: 'rgba(255, 165, 43, 1)'
        }
      },
      {
        name: t('kanban.cto.backlog.completionCount'),
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 任务积压工作量
let blockTaskWorkloadChart;
const blockTaskWorkloadRef = ref();
const blockTaskWorkloadConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: t('kanban.cto.backlog.backlogWorkloadRatio')
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: t('kanban.cto.backlog.backlogWorkload'),
        value: 0,
        itemStyle: {
          color: 'rgba(255, 165, 43, 1)'
        }
      },
      {
        name: t('kanban.cto.backlog.completionWorkload'),
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 逾期评估
const overdueAssessmentData = ref({});
let overdueTaskChart;
const overdueTaskRef = ref();
const overdueTaskConfig = {
  ...progressEchartConfig,
  graphic: {
    type: 'text',
    left: '20%',
    bottom: 0,
    z: 0,
    font: '12px',
    silent: true,
    invisible: false,
    style: {
      text: t('kanban.cto.overdue.overdueTaskRatio')
    }
  },
  title: {
    ...progressEchartConfig.title,
    left: '35%',
    top: '40%',
    itemGap: 40
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 12,
    itemWidth: 12,
    itemGap: 2,
    textStyle: {
      fontSize: 12
    }
  },
  series: [{
    ...progressEchartConfig.series[0],
    label: {
      show: false,
      formatter: '{c}'
    },
    labelLine: {
      show: true,
      length: 5,
      length2: 5
    },
    data: [
      {
        name: '逾期数',
        value: 0,
        itemStyle: {
          color: 'rgba(245, 34, 45, 1)'
        }
      },
      {
        name: '非逾期数',
        value: 0,
        itemStyle: {
          color: 'rgba(217, 217, 217, 1)'
        }
      }
    ]
  }]
};

// 计划外工作任务数
const unplannedWorkData = ref({});
const unplanTaskNumRef = ref();
let unplanTaskNumChart;
const unplanTaskNumConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '计划外任务占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '计划外数',
        value: 0,
        itemStyle: {
          color: 'gold'
        }
      },
      {
        name: '非计划外数',
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 计划外工作量
const unplannedWorkloadRef = ref();
let unplannedWorkloadChart;
const unplannedWorkloadConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '计划外工作量占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '计划外量',
        value: 0,
        itemStyle: {
          color: 'gold'
        }
      },
      {
        name: '非计划外量',
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 故障评估
const failureAssessmentData = ref({});
let criticalFailureChart; // 致命的
const criticalFailureRef = ref();
const criticalFailureConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '致命故障占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '致命数',
        value: 0,
        itemStyle: {
          color: 'rgba(245, 34, 45, 1)'
        }
      },
      {
        name: '非致命数',
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

let majorFailureChart; // 严重的
const majorFailureRef = ref();
const majorFailureConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '严重故障占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '严重数',
        value: 0,
        itemStyle: {
          color: 'gold'
        }
      },
      {
        name: '非严重数',
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

let minorFailureChart; // 一般的
const minorFailureRef = ref();
const minorFailureConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '一般故障占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '一般数',
        value: 0,
        itemStyle: { // rgba(255, 165, 43, 1)
          color: 'rgba(255, 165, 43, 1)'
        }
      },
      {
        name: '非一般数',
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

let trivialFailureChart; // 轻微的
const trivialFailureRef = ref();
const trivialFailureConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '轻微故障占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '轻微数',
        value: 0,
        itemStyle: {
          color: 'rgba(136, 185, 242, 1)'
        }
      },
      {
        name: '非轻微数',
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }
    ]
  }]
};

// 任务task 分类
const taskTypeRef = ref();
let taskTypeEcharts;
const taskTypeEchartsConfig = {
  title: {

  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    left: '75%',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '总数',
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
          itemStyle: {
            color: 'rgba(136, 185, 242, 1)'
          }
        },
        {
          name: t('kanban.cto.taskTypes.requirement'),
          value: 0,
          itemStyle: {
            color: 'rgba(201, 119, 255, 1)'
          }
        },
        {
          name: t('kanban.cto.taskTypes.task'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: t('kanban.cto.taskTypes.defect'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: t('kanban.cto.taskTypes.apiTest'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('kanban.cto.taskTypes.scenarioTest'),
          value: 0,
          itemStyle: {
            color: 'rgba(0,119,255,1)'
          }
        }
      ]
    }
  ]
};

// 任务task 分类
const taskStatusRef = ref();
let taskStatusEcharts;
const taskStatusEchartsConfig = {
  title: {

  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    left: '75%',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '总数',
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
      // CANCELED, COMPLETED, CONFIRMING,  IN_PROGRESS, PENDING
      data: [
        {
          name: t('kanban.cto.taskStatus.pendingConfirmation'),
          value: 0,
          itemStyle: {
            color: '#7F91FF'
          }
        },
        {
          name: t('kanban.cto.taskStatus.inProgress'),
          value: 0,
          itemStyle: {
            color: '#FF8100'
          }
        },
        {
          name: t('kanban.cto.taskStatus.pending'),
          value: 0,
          itemStyle: {
            color: '#FFB925'
          }
        },
        {
          name: t('kanban.cto.taskStatus.completed'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        },
        {
          name: t('kanban.cto.taskStatus.cancelled'),
          value: 0,
          itemStyle: {
            color: 'rgba(200, 202, 208, 1)'
          }
        }
      ]
    }
  ]
};

// 交付周期
const leadTimeData = ref({});
const leadTimeRef = ref();
let leadTimeChart;
const leadTimeConfig = {
  grid: {
    left: '40',
    right: '30',
    bottom: '20',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [t('kanban.cto.deliveryCycle.average'), t('kanban.cto.deliveryCycle.minimum'), t('kanban.cto.deliveryCycle.maximum'), 'P50', 'P75', 'P90', 'P95'],
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
};

// 接口测试开启
const apiTestData = ref({});
let apiOpenTestChart;
const apiOpenTestRef = ref();
const apiOpenTestConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '开启测试数占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '开启数',
        value: 0,
        itemStyle: {
          color: 'rgba(136, 185, 242, 1)'
        }
      },
      {
        name: '未开启数',
        value: 0,
        itemStyle: {
          color: 'rgba(217, 217, 217, 1)'
        }
      }
    ]
  }]
};

// 接口测试通过
let apiSuccessTestChart;
const apiSuccessTestRef = ref();
const apiSuccessTestConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '通过测试占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '通过数',
        value: 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: '未通过数',
        value: 0,
        itemStyle: {
          color: 'rgba(217, 217, 217, 1)'
        }
      }
    ]
  }]
};

// 接口测试开启
const scenarioTestData = ref({});
let scenarioOpenTestChart;
const scenarioOpenTestRef = ref();
const scenarioOpenTestConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '开启测试数占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '开启数',
        value: 0,
        itemStyle: {
          color: 'rgba(136, 185, 242, 1)'
        }
      },
      {
        name: '未开启数',
        value: 0,
        itemStyle: {
          color: 'rgba(217, 217, 217, 1)'
        }
      }
    ]
  }]
};

// 接口测试通过
let scenarioSuccessTestChart;
const scenarioSuccessTestRef = ref();
const scenarioSuccessTestConfig = {
  ...recentCompleteEchartConfig,
  title: {
    ...recentCompleteEchartConfig.title,
    subtext: '通过测试占比'
  },
  series: [{
    ...recentCompleteEchartConfig.series[0],
    data: [
      {
        name: '通过数',
        value: 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: '未通过数',
        value: 0,
        itemStyle: {
          color: 'rgba(217, 217, 217, 1)'
        }
      }
    ]
  }]
};

// 测试状态
const testStatusRef = ref();
let testStatusChart;
const testStatusConfig = {
  title: {

  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '总数',
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
          name: '待测试',
          value: 0,
          itemStyle: {
            color: 'rgba(45, 142, 255, 1)'
          }
        },
        {
          name: '测试通过',
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: '测试未通过',
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: '阻塞',
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: '已取消',
          value: 0,
          itemStyle: {
            color: 'rgba(45, 142, 255, 1)'
          }
        }
      ]
    }
  ]
};

// 评审状态
const reviewStatusRef = ref();
let reviewStatusChart;
const reviewStatusConfig = {
  title: {

  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '总数',
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
          name: '待评审',
          value: 0,
          itemStyle: {
            color: 'rgba(201, 119, 255, 1)'
          }
        },
        {
          name: '评审通过',
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: '评审未通过',
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
};

const loadData = async () => {
  const [error, { data = {} }] = await (props.countType === 'task' ? kanban.getTaskCto({ ...params.value }) : kanban.getTestingCto({ ...params.value }));
  if (error) {
    return;
  }
  memberNum.value = data.memberNum || 0;
  // 总进度
  const totalProgressOverview = data.totalProgressOverview || {};
  if (totalProgressOverview) {
    const { totalNum = 0, totalCompletedNum = 0, totalCompletedRate = 0 } = totalProgressOverview;
    const progressData = [totalNum - totalCompletedNum, totalCompletedNum];
    progressData.forEach((value, idx) => {
      progressEchartConfig.series[0].data[idx].value = value;
    });
    progressEchartConfig.title.text = totalCompletedRate + '%';
    progressDatas.value = totalProgressOverview;
    progressEchart.setOption(progressEchartConfig);
  }

  // 积压任务、用例
  const backloggedCount = data.backloggedCount || {};
  if (backloggedCount) {
    backloggedData.value = backloggedCount;
    const { backloggedCompletionTime, backloggedNum, backloggedRate, backloggedWorkload, backloggedWorkloadRate, dailyProcessedNum, dailyProcessedWorkload, processedInDay, totalNum, totalWorkload } = backloggedCount;
    const blockNumData = [backloggedNum, totalNum - backloggedNum];
    blockTaskConfig.title.text = backloggedRate + '%';
    blockNumData.forEach((value, idx) => {
      blockTaskConfig.series[0].data[idx].value = value;
    });
    blockTaskChart.setOption(blockTaskConfig);

    const blockWorkloadData = [backloggedWorkload, totalWorkload - backloggedWorkload];
    blockTaskWorkloadConfig.title.text = backloggedWorkloadRate + '%';
    blockWorkloadData.forEach((value, idx) => {
      blockTaskWorkloadConfig.series[0].data[idx].value = value;
    });
    blockTaskWorkloadChart.setOption(blockTaskWorkloadConfig);
  }

  // 近期交付
  const recentDeliveryCount = data.recentDeliveryCount || {};
  if (recentDeliveryCount) {
    recentDeliveryDatas.value = recentDeliveryCount;
    handleRecentDateChange();
  }

  // 逾期数
  const overdueAssessmentCount = data.overdueAssessmentCount || {};
  if (overdueAssessmentCount) {
    overdueAssessmentData.value = overdueAssessmentCount;
    const { dailyProcessedWorkload = 0, overdueNum = 0, overdueRate = 0, overdueTime = 0, overdueWorkload = 0, overdueWorkloadProcessingTime = 0, overdueWorkloadRate = 0, riskLevel, totalNum = 0, totalWorkload = 0 } = overdueAssessmentCount;
    const overdueData = [overdueNum, totalNum - overdueNum];
    overdueTaskConfig.title.text = overdueRate + '%';
    overdueData.forEach((value, idx) => {
      overdueTaskConfig.series[0].data[idx].value = value;
    });
    overdueTaskChart.setOption(overdueTaskConfig);
  }

  // 计划外任务
  const unplannedWorkCount = data.unplannedWorkCount || {};
  if (unplannedWorkCount) {
    unplannedWorkData.value = unplannedWorkCount;
    const { totalNum = 0, totalWorkload = 0, unplannedCompletedNum, unplannedCompletedRate, unplannedNum = 0, unplannedRate = 0, unplannedWorkload = 0, unplannedWorkloadCompleted, unplannedWorkloadCompletedRate, unplannedWorkloadRate } = unplannedWorkCount;
    const unplannedNumData = [unplannedNum, totalNum - unplannedNum];
    unplanTaskNumConfig.title.text = unplannedRate + '%';
    unplannedNumData.forEach((value, idx) => {
      unplanTaskNumConfig.series[0].data[idx].value = value;
    });
    unplanTaskNumChart.setOption(unplanTaskNumConfig);

    const unplannedWorkloadData = [unplannedWorkload, totalWorkload - unplannedWorkload];
    unplannedWorkloadConfig.title.text = unplannedWorkloadCompletedRate + '%';
    unplannedWorkloadData.forEach((value, idx) => {
      unplannedWorkloadConfig.series[0].data[idx].value = value;
    });
    unplannedWorkloadChart.setOption(unplannedWorkloadConfig);
  }

  // 故障
  const failureAssessmentCount = data.failureAssessmentCount || {};
  if (failureAssessmentCount && props.countType === 'task') {
    failureAssessmentData.value = failureAssessmentCount;
    const { failureLevelCount = { CRITICAL: 0, MAJOR: 0, MINOR: 0, TRIVIAL: 0 }, failureNum = 0, totalNum = 0, totalWorkload = 0 } = failureAssessmentCount;
    const criticalData = [failureLevelCount.CRITICAL, failureNum - failureLevelCount.CRITICAL];
    criticalFailureConfig.title.text = ((failureLevelCount.CRITICAL / failureNum) * 100).toFixed(2) + '%';
    criticalData.forEach((value, idx) => {
      criticalFailureConfig.series[0].data[idx].value = value;
    });
    criticalFailureChart.setOption(criticalFailureConfig);

    const minorData = [failureLevelCount.MINOR, failureNum - failureLevelCount.MINOR];
    minorFailureConfig.title.text = ((failureLevelCount.MINOR / failureNum) * 100).toFixed(2) + '%';
    minorData.forEach((value, idx) => {
      minorFailureConfig.series[0].data[idx].value = value;
    });
    minorFailureChart.setOption(minorFailureConfig);

    const majorData = [failureLevelCount.MAJOR, failureNum - failureLevelCount.MAJOR];
    majorFailureConfig.title.text = ((failureLevelCount.MAJOR / failureNum) * 100).toFixed(2) + '%';
    majorData.forEach((value, idx) => {
      majorFailureConfig.series[0].data[idx].value = value;
    });
    majorFailureChart.setOption(majorFailureConfig);

    const trivialData = [failureLevelCount.TRIVIAL, failureNum - failureLevelCount.TRIVIAL];
    trivialFailureConfig.title.text = ((failureLevelCount.TRIVIAL / failureNum) * 100).toFixed(2) + '%';
    trivialData.forEach((value, idx) => {
      trivialFailureConfig.series[0].data[idx].value = value;
    });
    trivialFailureChart.setOption(trivialFailureConfig);
  }

  // 接口测试 & 场景测试
  if (props.countType === 'useCase') {
    const apisTestCount = data.apisTestCount || {};
    apiTestData.value = apisTestCount;
    const { enabledTestApiNum, enabledTestApiRate, enabledTestNum, passedTestApiNum, passedTestNum, testApiProgress, testProgress, totalApiNum } = apisTestCount;
    const apisTestData = [enabledTestApiNum, totalApiNum - enabledTestApiNum];
    apiOpenTestConfig.title.text = enabledTestApiRate + '%';
    apisTestData.forEach((value, idx) => {
      apiOpenTestConfig.series[0].data[idx].value = value;
    });
    apiOpenTestChart.setOption(apiOpenTestConfig);

    const apiSuccessData = [passedTestApiNum, totalApiNum - passedTestApiNum];
    apiSuccessTestConfig.title.text = ((passedTestNum / totalApiNum) * 100).toFixed(2) + '%';
    apiSuccessData.forEach((value, idx) => {
      apiSuccessTestConfig.series[0].data[idx].value = value;
    });
    apiSuccessTestChart.setOption(apiSuccessTestConfig);
  }
  // 场景测试
  if (props.countType === 'useCase') {
    const scenarioTestCount = data.scenarioTestCount || {};
    scenarioTestData.value = scenarioTestCount;
    const { enabledTestScenarioNum, enabledTestScenarioRate, enabledTestNum, passedTestScenarioNum, passedTestNum, testScenarioProgress, testProgress, totalScenarioNum } = scenarioTestCount;
    const scenarioTestDatas = [enabledTestScenarioNum, totalScenarioNum - enabledTestScenarioNum];
    apiOpenTestConfig.title.text = enabledTestScenarioRate + '%';
    scenarioTestDatas.forEach((value, idx) => {
      scenarioOpenTestConfig.series[0].data[idx].value = value;
    });
    scenarioOpenTestChart.setOption(scenarioOpenTestConfig);

    const scenarioSuccessData = [passedTestScenarioNum, totalScenarioNum - passedTestScenarioNum];
    scenarioSuccessTestConfig.title.text = ((passedTestScenarioNum / totalScenarioNum) * 100).toFixed(2) + '%';
    scenarioSuccessData.forEach((value, idx) => {
      scenarioSuccessTestConfig.series[0].data[idx].value = value;
    });
    scenarioSuccessTestChart.setOption(scenarioSuccessTestConfig);
  }

  // 任务类型
  const totalTypeCount = data.totalTypeCount || {};
  if (totalTypeCount) {
    const { STORY = 0, REQUIREMENT = 0, TASK = 0, BUG = 0, API_TEST = 0, SCENARIO_TEST = 0 } = totalTypeCount;
    const typeData = [STORY, REQUIREMENT, TASK, BUG, API_TEST, SCENARIO_TEST];
    taskTypeEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = typeData[idx];
    });
    taskTypeEcharts.setOption(taskTypeEchartsConfig);
  }
  // 任务状态 taskStatusEcharts.setOption(taskStatusEchartsConfig);
  const totalStatusCount = data.totalStatusCount || {};
  if (totalStatusCount) {
    const { PENDING = 0, IN_PROGRESS = 0, CONFIRMING = 0, COMPLETED = 0, CANCELED = 0 } = totalStatusCount;
    const statusData = [PENDING, IN_PROGRESS, CONFIRMING, COMPLETED, CANCELED];
    statusData.forEach((value, idx) => {
      taskStatusEchartsConfig.series[0].data[idx].value = value;
    });
    taskStatusEcharts.setOption(taskStatusEchartsConfig);
  }
  // 交付周期
  const leadTimeCount = data.leadTimeCount || {};
  if (leadTimeCount) {
    leadTimeData.value = leadTimeCount;
    const { avgProcessingTime = 0, maxProcessingTime = 0, minProcessingTime = 0, p50ProcessingTime = 0, p75ProcessingTime = 0, p90ProcessingTime = 0, p95ProcessingTime = 0, p99ProcessingTime = 0 } = leadTimeCount;
    const leadTimeChartData = [avgProcessingTime, minProcessingTime, maxProcessingTime, p50ProcessingTime, p75ProcessingTime, p90ProcessingTime, p95ProcessingTime];
    leadTimeConfig.series[0].data = leadTimeChartData;
    leadTimeChart.setOption(leadTimeConfig);
  }

  if (props.countType === 'useCase') {
    // 测试状态
    const totalTestResultCount = data.totalTestResultCount || {};
    const { PENDING = 0, PASSED = 0, NOT_PASSED = 0, BLOCKED = 0, CANCELED = 0 } = totalTestResultCount;
    const testResultData = [PENDING, PASSED, NOT_PASSED, BLOCKED, CANCELED];
    testResultData.forEach((value, idx) => {
      testStatusConfig.series[0].data[idx].value = value;
    });
    testStatusChart.setOption(testStatusConfig);
  }

  if (props.countType === 'useCase') {
    // 评审状态 reviewStatusConfig
    const totalReviewStatusCount = data.totalReviewStatusCount || {};
    const { PENDING = 0, PASSED = 0, FAILED = 0 } = totalReviewStatusCount;
    const reviewData = [PENDING, PASSED, FAILED];
    reviewData.forEach((value, idx) => {
      reviewStatusConfig.series[0].data[idx].value = value;
    });
    reviewStatusChart.setOption(reviewStatusConfig);
  }
};

const shouldNotify = ref(false);
onMounted(() => {
  watch(() => props.countType, (newValue) => {
    if (newValue === 'task') {
      nextTick(() => {
        criticalFailureChart.resize();
        majorFailureChart.resize();
        minorFailureChart.resize();
        trivialFailureChart.resize();
        taskTypeEcharts.resize();
        taskStatusEcharts.resize();
      });
    } else {
      nextTick(() => {
        scenarioOpenTestChart.resize();
        scenarioSuccessTestChart.resize();
        apiOpenTestChart.resize();
        apiSuccessTestChart.resize();
        reviewStatusChart.resize();
        testStatusChart.resize();
      });
    }
  });
  watch([() => props.createdDateEnd, () => props.createdDateStart, () => props.creatorObjectId, () => props.creatorObjectType, () => props.projectId, () => props.countType, () => props.planId, () => props.sprintId], () => {
    if (!props.onShow && props.projectId) {
      shouldNotify.value = true;
      return;
    }
    if (props.countType === 'task') {
      // oneTimeUnPassedTestRateConfig.series[0].itemStyle.color = 'rgba(255, 165, 43, 1)';
      // oneTimeUnPassedTestConfig.series[1].itemStyle.color = 'rgba(255, 165, 43, 1)';
    } else {
      // oneTimeUnPassedTestRateConfig.series[0].itemStyle.color = 'rgba(82, 196, 26, 1)';
      // oneTimeUnPassedTestConfig.series[1].itemStyle.color = 'rgba(82, 196, 26, 1)';
    }
    if (props.projectId) {
      loadData();
    }
  }, {
    immediate: true
  });

  watch(() => props.onShow, newValue => {
    if (newValue && shouldNotify.value && props.projectId) {
      refresh();
      shouldNotify.value = false;
    }
    if (newValue && resizeNotify.value && props.projectId) {
      handleWindowResize();
      resizeNotify.value = false;
    }
  });

  progressEchart = eCharts.init(progressRef.value);
  progressEchart.setOption(progressEchartConfig);

  recentCompleteRateEchart = eCharts.init(recentCompleteRef.value);
  recentCompleteRateEchart.setOption(recentCompleteEchartConfig);

  recentOverdueRateChart = eCharts.init(recentOverdueRef.value);
  recentOverdueRateChart.setOption(recentoverdueEchartConfig);

  recentCompletedWorkloadChart = eCharts.init(recentCompletedWorkloadRef.value);
  recentCompletedWorkloadChart.setOption(recentCompletedWorkloadEchartConfig);

  recentSavingRateChart = eCharts.init(recentSavingRateRef.value);
  recentSavingRateChart.setOption(recentSavingRateConfig);

  blockTaskChart = eCharts.init(blockTaskRef.value);
  blockTaskChart.setOption(blockTaskConfig);

  blockTaskWorkloadChart = eCharts.init(blockTaskWorkloadRef.value);
  blockTaskWorkloadChart.setOption(blockTaskWorkloadConfig);

  overdueTaskChart = eCharts.init(overdueTaskRef.value);
  overdueTaskChart.setOption(overdueTaskConfig);

  unplanTaskNumChart = eCharts.init(unplanTaskNumRef.value);
  unplanTaskNumChart.setOption(unplanTaskNumConfig);

  unplannedWorkloadChart = eCharts.init(unplannedWorkloadRef.value);
  unplannedWorkloadChart.setOption(unplannedWorkloadConfig);

  taskTypeEcharts = eCharts.init(taskTypeRef.value);
  taskTypeEcharts.setOption(taskTypeEchartsConfig);

  taskStatusEcharts = eCharts.init(taskStatusRef.value);
  taskStatusEcharts.setOption(taskStatusEchartsConfig);

  leadTimeChart = eCharts.init(leadTimeRef.value);
  leadTimeChart.setOption(leadTimeConfig);

  criticalFailureChart = eCharts.init(criticalFailureRef.value);
  criticalFailureChart.setOption(criticalFailureConfig);

  majorFailureChart = eCharts.init(majorFailureRef.value);
  majorFailureChart.setOption(majorFailureConfig);

  minorFailureChart = eCharts.init(minorFailureRef.value);
  minorFailureChart.setOption(minorFailureConfig);

  trivialFailureChart = eCharts.init(trivialFailureRef.value);
  trivialFailureChart.setOption(trivialFailureConfig);

  apiOpenTestChart = eCharts.init(apiOpenTestRef.value);
  apiOpenTestChart.setOption(apiOpenTestConfig);

  apiSuccessTestChart = eCharts.init(apiSuccessTestRef.value);
  apiSuccessTestChart.setOption(apiSuccessTestConfig);
  // scenarioOpenTestRef
  scenarioOpenTestChart = eCharts.init(scenarioOpenTestRef.value);
  scenarioOpenTestChart.setOption(scenarioOpenTestConfig);

  scenarioSuccessTestChart = eCharts.init(scenarioSuccessTestRef.value);
  scenarioSuccessTestChart.setOption(scenarioSuccessTestConfig);

  testStatusChart = eCharts.init(testStatusRef.value);
  testStatusChart.setOption(testStatusConfig);

  reviewStatusChart = eCharts.init(reviewStatusRef.value);
  reviewStatusChart.setOption(reviewStatusConfig);
  window.addEventListener('resize', handleWindowResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
});

const resizeEcharts = () => {
  progressEchart.resize();
  recentCompleteRateEchart.resize();
  recentOverdueRateChart.resize();
  recentSavingRateChart.resize();
  blockTaskChart.resize();
  blockTaskWorkloadChart.resize();
  overdueTaskChart.resize();
  unplanTaskNumChart.resize();
  unplannedWorkloadChart.resize();
  taskTypeEcharts.resize();
  taskStatusEcharts.resize();
  leadTimeChart.resize();
  criticalFailureChart.resize();
  majorFailureChart.resize();
  minorFailureChart.resize();
  trivialFailureChart.resize();
  apiOpenTestChart.resize();
  apiSuccessTestChart.resize();
  scenarioOpenTestChart.resize();
  scenarioSuccessTestChart.resize();
  testStatusChart.resize();
  reviewStatusChart.resize();
};

const resizeNotify = ref(false);
const handleWindowResize = throttle(500, () => {
  if (!props.onShow) {
    resizeNotify.value = true;
    return;
  }
  resizeEcharts();
});

const refresh = () => {
  if (!props.projectId) {
    return;
  }
  loadData();
};

defineExpose({
  refresh,
  handleWindowResize
});
</script>
<template>
  <div class="py-2 text-3 space-y-2">
    <div class="flex space-x-2 h-65">
      <div class="rounded h-full flex-1/2 flex space-x-2">
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">总进度{{ t('kanban.cto.progress.totalProgress') }}</div>
          <div ref="progressRef" class="h-1/2 w-2/3"></div>
          <div class="flex justify-around mt-3">
            <div class="text-center">
              <div class="font-semibold text-5">{{ progressDatas.totalNum || 0 }}</div>
              <div>
                {{ props.countType === 'task' ? t('kanban.cto.progress.totalTaskCount' ) : t('kanban.cto.progress.totalUseCaseCount') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ progressDatas.totalWorkload || 0 }}</div>
              <div>
                {{ t('kanban.cto.progress.totalWorkload') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ memberNum || 0 }}</div>
              <div>
                {{ t('kanban.cto.progress.teamMember') }}
              </div>
            </div>
          </div>
        </div>
        <div class="flex-1/2 border rounded p-2 space-y-2">
          <div class="font-semibold">{{ props.countType === 'task' ? t('kanban.cto.backlog.backlogTask') : t('kanban.cto.backlog.backlogUseCase') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ backloggedData.backloggedNum || 0 }}</span></div>
              <div>{{ props.countType === 'task' ? t('kanban.cto.backlog.backlogTaskCount') : t('kanban.cto.backlog.backlogUseCaseCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ backloggedData.backloggedCompletionTime || 0 }}</span>小时{{ t('kanban.cto.deliveryCycle.hours') }}</div>
              <div>{{ t('kanban.cto.backlog.estimatedTime') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ backloggedData.backloggedWorkload || 0 }}</span></div>
              <div>{{ t('kanban.cto.backlog.backlogWorkload') }}</div>
            </div>
          </div>
          <div class="flex h-1/2 mt-2">
            <div ref="blockTaskRef" class="flex-1 h-full"></div>
            <div ref="blockTaskWorkloadRef" class="flex-1 h-full"></div>
          </div>
        </div>
      </div>
      <div class="border rounded h-full flex-1/2  p-2 space-y-2">
        <div class="flex justify-between">
          <div class="font-semibold ">{{ t('kanban.cto.recentDelivery.recentDelivery') }}</div>
          <RadioGroup
            v-model:value="recentDate"
            optionType="button"
            buttonStyle="solid"
            size="small"
            :options="recentDateOpt"
            @change="handleRecentDateChange" />
        </div>
        <div class="flex space-x-2 justify-around">
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryDatas?.[recentDate]?.completedNum || 0 }}</span>/{{ recentDeliveryDatas?.[recentDate]?.totalNum || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryTaskCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryDatas?.[recentDate]?.OverdueNum || 0 }}</span>/{{ recentDeliveryDatas?.[recentDate]?.totalNum || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryTaskOverdueCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryDatas?.[recentDate]?.completedWorkload || 0 }}</span>/{{ recentDeliveryDatas?.[recentDate]?.totalWorkload || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryWorkload') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryDatas?.[recentDate]?.savingWorkload || 0 }}</span>/{{ recentDeliveryDatas?.[recentDate]?.totalWorkload || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryWorkloadSaving') }}</div>
          </div>
        </div>
        <div class="flex space-x-2 h-1/2">
          <div ref="recentCompleteRef" class="h-full flex-1"></div>
          <div ref="recentOverdueRef" class="h-full flex-1"></div>
          <div ref="recentCompletedWorkloadRef" class="h-full flex-1"></div>
          <div ref="recentSavingRateRef" class="h-full flex-1"></div>
        </div>
      </div>
    </div>

    <div class="flex space-x-2 h-65">
      <div class="rounded h-full flex-1/2 flex space-x-2">
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.overdue.overdueAssessment') }}</div>
          <div class="flex h-1/2 items-center">
            <div ref="overdueTaskRef" class="h-full w-2/3"></div>
            <div class="text-center flex-1">
              <div :class="`risk-level-${overdueAssessmentData?.riskLevel?.value}`" class="font-semibold text-5">{{ overdueAssessmentData?.riskLevel?.message }}</div>
              <div>{{ t('kanban.cto.overdue.overdueRisk') }}</div>
            </div>
          </div>

          <div class="flex justify-around mt-3">
            <div class="text-center">
              <div class="font-semibold text-5">{{ overdueAssessmentData.overdueNum || 0 }}</div>
              <div>
                {{ t('kanban.cto.overdue.overdueCount') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ overdueAssessmentData.overdueWorkload || 0 }}</div>
              <div>
                {{ t('kanban.cto.overdue.overdueWorkload') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ overdueAssessmentData.overdueWorkloadProcessingTime || 0 }}</div>
              <div>
                {{ t('kanban.cto.overdue.overdueWorkloadProcessingTime') }}
              </div>
            </div>
          </div>
        </div>
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.unplanned.unplannedWork') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ unplannedWorkData.unplannedNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.unplanned.unplannedTaskCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ unplannedWorkData.unplannedWorkloadProcessingTime || 0 }}</span>小时</div>
              <div>{{ t('kanban.cto.unplanned.estimatedTime') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ unplannedWorkData.unplannedWorkload || 0 }}</span></div>
              <div>{{ t('kanban.cto.unplanned.unplannedWorkload') }}</div>
            </div>
          </div>
          <div class="flex h-1/2 mt-2">
            <div ref="unplanTaskNumRef" class="flex-1 h-full"></div>
            <div ref="unplannedWorkloadRef" class="flex-1 h-full"></div>
          </div>
        </div>
      </div>
      <div v-show="props.countType === 'task'" class="border rounded h-full flex-1/2  p-2 space-y-2">
        <div class="flex justify-between">
          <div class="font-semibold ">{{ t('kanban.cto.failureAssessment.failureAssessment ') }}</div>
        </div>
        <div class="flex space-x-2 justify-around">
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureNum || 0 }}</span>/{{ failureAssessmentData?.totalNum || 0 }}</div>
            <div>{{t('kanban.cto.failureAssessment.failureCount')}}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureWorkload || 0 }}</span>/{{ failureAssessmentData?.totalWorkload || 0 }}</div>
            <div>{{t('kanban.cto.failureAssessment.failureWorkload')}}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureCompletedNum || 0 }}</span>/{{ failureAssessmentData?.failureNum || 0 }}</div>
            <div>{{ t('kanban.cto.failureAssessment.failureCompletedNum') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureOverdueNum || 0 }}</span>/{{ failureAssessmentData?.failureNum || 0 }}</div>
            <div>{{ t('kanban.cto.failureAssessment.failureOverdueNum') }}</div>
          </div>
        </div>
        <div class="flex space-x-2 h-1/2 mt-2">
          <div ref="criticalFailureRef" class="h-full flex-1"></div>
          <div ref="majorFailureRef" class="h-full flex-1"></div>
          <div ref="minorFailureRef" class="h-full flex-1"></div>
          <div ref="trivialFailureRef" class="h-full flex-1"></div>
        </div>
      </div>
      <div v-show="props.countType === 'useCase'" class="h-full flex-1/2 space-x-2 flex">
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.apiTest.apiTest') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ apiTestData.enabledTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.enabledTestCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ apiTestData.passedTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.passedTestCount') }}</div>
            </div>
          </div>
          <div class="flex space-x-2 justify-around mt-2 h-1/2">
            <div ref="apiOpenTestRef" class="flex-1 h-full"></div>
            <div ref="apiSuccessTestRef" class="flex-1 h-full"></div>
          </div>
        </div>
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.scenarioTest.scenarioTest') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ scenarioTestData.enabledTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.enabledTestCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ scenarioTestData.passedTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.passedTestCount') }}</div>
            </div>
          </div>
          <div class="flex space-x-2 justify-around mt-2 h-1/2">
            <div ref="scenarioOpenTestRef" class="flex-1 h-full"></div>
            <div ref="scenarioSuccessTestRef" class="flex-1 h-full"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex space-x-2 h-50">
      <div class="rounded h-full flex-1/2 flex space-x-2">
        <div class="flex-1/2 border rounded p-2 flex flex-col space-y-2">
          <div class="font-semibold">{{ props.countType === 'task' ? t('kanban.cto.taskType') : t('kanban.cto.testStatus') }}</div>
          <div
            v-show="props.countType === 'task'"
            ref="taskTypeRef"
            class="flex-1"></div>
          <div
            v-show="props.countType === 'useCase'"
            ref="testStatusRef"
            class="flex-1"></div>
        </div>
        <div class="flex-1/2  border rounded p-2 flex flex-col space-y-2">
          <div class="font-semibold">{{ props.countType === 'task' ? t('kanban.cto.taskStatusName') : t('kanban.cto.reviewStatus') }}</div>
          <div
            v-show="props.countType === 'task'"
            ref="taskStatusRef"
            class="flex-1"></div>
          <div
            v-show="props.countType === 'useCase'"
            ref="reviewStatusRef"
            class="flex-1"></div>
        </div>
      </div>
      <div class="flex-1/2 border rounded p-2 flex flex-col space-y-2">
        <div class="font-semibold">{{ t('kanban.cto.deliveryCycle.deliveryCycle') }}</div>
        <div class="flex-1 flex space-x-3 items-center">
          <div class="w-60 pl-4">
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5">{{ t('kanban.cto.deliveryCycle.participantCount') }}</span>
              <span class="flex-3/5 font-semibold text-5">{{ leadTimeData.userNum || 0 }}</span>
            </div>
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5">{{ t('kanban.cto.deliveryCycle.totalDuration') }}</span>
              <div class="flex-3/5 min-w-0">
                                  <span class="font-semibold text-5">{{ leadTimeData.totalProcessingTime || 0 }} </span> {{ t('kanban.cto.deliveryCycle.hours') }}
              </div>
            </div>
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5">{{ t('kanban.cto.deliveryCycle.averageDuration') }}</span>
              <div class="flex-3/5 min-w-0">
                                  <span class=" font-semibold text-5">{{ leadTimeData.userAvgProcessingTime || 0 }} </span> {{ t('kanban.cto.deliveryCycle.hoursPerPerson') }}
              </div>
            </div>
          </div>
          <div
            ref="leadTimeRef"
            class="flex-1 h-full"
            style="width: calc(100% - 240px)">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.risk-level-LOW {
  color: 'gold'
}

.risk-level-HIGH {
  color: 'red'
}

.risk-level-NONE {
  color: '#52C41A'
}
</style>
