<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const statusColorSet = {
  4: '#FF8100',
  3: '#2D8EFF',
  2: '#67D7FF',
  1: '#52C41A',
  0: '#B7BBC2'
};

const priorityColorSet = {
  HIGHEST: '#F5222D',
  HIGH: '#FF8100',
  MEDIUM: '#FFB925',
  LOW: '#52C41A',
  LOWEST: '#67D7FF'
};

const taskTypeColorSet = {
  BUG: '#F5222D',
  TASK: '#FF8100',
  STORY: '#FFB925',
  API_TEST: '#52C41A',
  REQUIREMENT: '#67D7FF',
  SCENARIO_TEST: '#67D7FF'
};

const taskStatusRef = ref();
const priorityRef = ref();
const taskTypeRef = ref();

const taskStatusOption = {
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
    right: '8%',
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
      t('status.cancelled'),
      t('status.completed'),
      t('status.pendingConfirmation'),
      t('status.inProgress'),
      t('status.pending')
    ]
  },
  series: [
    {
      type: 'bar',
      showBackground: true,
      barMaxWidth: 20,
      label: {
        show: true,
        position: 'right'
      },
      data: [
        {
          value: 0,
          itemStyle: {
            color: '#F7F8FB'
          }
        },
        {
          value: 0,
          itemStyle: {
            color: '#FF8100'
          }
        },
        {
          value: 0,
          itemStyle: {
            color: '#FFB925'
          }
        },
        {
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        },
        {
          value: 0,
          itemStyle: {
            color: '#2D8EFF'
          }
        }
      ]
    }
  ]
};

const priorityOptions = {
  title: {
    text: 0,
    subtext: t('chart.total'),
    left: '29.5%',
    top: '40%',
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
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 4
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '55%'],
      center: ['30%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 5,
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
          name: t('reportPreview.projectProgress.taskSummary.groupStats.priorities.highest'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.HIGHEST
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.priorities.high'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.HIGH
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.priorities.medium'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.MEDIUM
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.priorities.low'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.LOW
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.priorities.lowest'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.LOWEST
          }
        }
      ]
    }
  ]
};

const taskTypeOptions = {
  title: {
    text: 0,
    subtext: t('chart.total'),
    left: '29.5%',
    top: '40%',
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
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 4
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '55%'],
      center: ['30%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 5,
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
          name: t('reportPreview.projectProgress.taskSummary.groupStats.taskTypes.bug'),
          value: 0,
          itemStyle: {
            color: taskTypeColorSet.BUG
          }
        },
        {
          name: t('common.issue'),
          value: 0,
          itemStyle: {
            color: taskTypeColorSet.TASK
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.taskTypes.story'),
          value: 0,
          itemStyle: {
            color: taskTypeColorSet.STORY
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.taskTypes.requirement'),
          value: 0,
          itemStyle: {
            color: taskTypeColorSet.REQUIREMENT
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.taskTypes.apiTest'),
          value: 0,
          itemStyle: {
            color: taskTypeColorSet.API_TEST
          }
        },
        {
          name: t('reportPreview.projectProgress.taskSummary.groupStats.taskTypes.scenarioTest'),
          value: 0,
          itemStyle: {
            color: taskTypeColorSet.SCENARIO_TEST
          }
        }
      ]
    }
  ]
};

let statusEchart;
let priorityEchart;
let taskTypeEchart;

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    const totalStatusOverview = newValue?.content?.tasks?.totalStatusCount;
    if (totalStatusOverview) {
      const { CANCELED, COMPLETED, CONFIRMING, IN_PROGRESS, PENDING } = totalStatusOverview;
      taskStatusOption.series[0].data = [CANCELED, COMPLETED, CONFIRMING, IN_PROGRESS, PENDING].map((value, idx) => {
        return {
          value,
          itemStyle: {
            color: statusColorSet[idx]
          }
        };
      });
    }

    const totalPriorityOverview = newValue?.content?.tasks?.totalPriorityCount;
    if (totalPriorityOverview) {
      const {
        LOW = 0,
        HIGH = 0,
        LOWEST = 0,
        MEDIUM = 0,
        HIGHEST = 0
      } = totalPriorityOverview;
      const methodData = [
        HIGHEST,
        HIGH,
        MEDIUM,
        LOW,
        LOWEST
      ];
      priorityOptions.series[0].data.forEach((item, idx) => {
        item.value = methodData[idx];
      });
    } else {
      priorityOptions.series[0].data.forEach(item => {
        item.value = 0;
      });
    }

    const totalTypeOverview = newValue?.content?.tasks?.totalTypeCount;
    if (totalTypeOverview) {
      const {
        BUG = 0,
        TASK = 0,
        STORY = 0,
        API_TEST = 0,
        REQUIREMENT = 0,
        SCENARIO_TEST = 0
      } = totalTypeOverview;
      const taskTypeData = [
        BUG,
        TASK,
        STORY,
        REQUIREMENT,
        API_TEST,
        SCENARIO_TEST
      ];
      taskTypeOptions.series[0].data.forEach((item, idx) => {
        item.value = taskTypeData[idx];
      });
    } else {
      taskTypeOptions.series[0].data.forEach(item => {
        item.value = 0;
      });
    }

    const totalTaskNum = newValue?.content?.tasks?.totalOverview?.totalTaskNum || 0;
    priorityOptions.title.text = totalTaskNum;
    taskTypeOptions.title.text = totalTaskNum;

    statusEchart = echarts.init(taskStatusRef.value);
    priorityEchart = echarts.init(priorityRef.value);
    taskTypeEchart = echarts.init(taskTypeRef.value);
    statusEchart.setOption(taskStatusOption);
    priorityEchart.setOption(priorityOptions);
    taskTypeEchart.setOption(taskTypeOptions);
  }, {
    immediate: true
  });
});

</script>

<template>
  <h1 class="text-theme-title font-medium mb-3">
    <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.projectProgress.taskSummary.groupStats.byStatus') }}</span>
  </h1>
  <div ref="taskStatusRef" class="flex-1 h-50 w-120 mb-7">
  </div>

  <h1 class="text-theme-title font-medium mb-3">
    <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.projectProgress.taskSummary.groupStats.byPriority') }}</span>
  </h1>
  <div ref="priorityRef" class="flex-1 h-50  w-120">
  </div>

  <h1 class="text-theme-title font-medium mb-3">
    <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.projectProgress.taskSummary.groupStats.byTaskType') }}</span>
  </h1>
  <div ref="taskTypeRef" class="flex-1 h-50  w-120">
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
