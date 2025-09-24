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

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

const statusColorSet = {
  4: '#FF8100',
  3: '#52c41a',
  2: '#f5222d',
  1: '#ffa52b',
  0: '#B7BBC2'
};

const priorityColorSet = {
  HIGHEST: '#F5222D',
  HIGH: '#FF8100',
  MEDIUM: '#FFB925',
  LOW: '#52C41A',
  LOWEST: '#67D7FF'
};

const taskStatusRef = ref();
const priorityRef = ref();

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
      t('status.blocked'),
      t('reportPreview.projectProgress.funcCaseSummary.groupStats.status.testFailed'),
      t('reportPreview.projectProgress.funcCaseSummary.groupStats.status.testPassed'),
      t('reportPreview.projectProgress.funcCaseSummary.groupStats.status.pendingTest')
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
          name: t('reportPreview.projectProgress.funcCaseSummary.groupStats.priorities.highest'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.HIGHEST
          }
        },
        {
          name: t('reportPreview.projectProgress.funcCaseSummary.groupStats.priorities.high'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.HIGH
          }
        },
        {
          name: t('reportPreview.projectProgress.funcCaseSummary.groupStats.priorities.medium'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.MEDIUM
          }
        },
        {
          name: t('reportPreview.projectProgress.funcCaseSummary.groupStats.priorities.low'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.LOW
          }
        },
        {
          name: t('reportPreview.projectProgress.funcCaseSummary.groupStats.priorities.lowest'),
          value: 0,
          itemStyle: {
            color: priorityColorSet.LOWEST
          }
        }
      ]
    }
  ]
};

let statusEchart;
let priorityEchart;

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    const totalTestResultOverview = newValue?.content?.cases?.totalTestResultCount;
    if (totalTestResultOverview) {
      const {
        PASSED,
        BLOCKED,
        PENDING,
        CANCELED,
        NOT_PASSED
      } = totalTestResultOverview;
      taskStatusOption.series[0].data = [CANCELED, BLOCKED, NOT_PASSED, PASSED, PENDING].map((value, idx) => {
        return {
          value,
          itemStyle: {
            color: statusColorSet[idx]
          }
        };
      });
    }

    const totalPriorityOverview = newValue?.content?.cases?.totalPriorityCount;
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
    const totalCaseNum = newValue?.content?.cases?.totalOverview?.totalCaseNum || 0;
    priorityOptions.title.text = totalCaseNum;

    statusEchart = echarts.init(taskStatusRef.value);
    priorityEchart = echarts.init(priorityRef.value);
    statusEchart.setOption(taskStatusOption);
    priorityEchart.setOption(priorityOptions);
  }, {
    immediate: true
  });
});

</script>

<template>
  <h1 class="text-theme-title font-medium mb-3">
    <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.projectProgress.funcCaseSummary.groupStats.byStatus') }}</span>
  </h1>
  <div ref="taskStatusRef" class="flex-1 h-50 w-120 mb-7">
  </div>

  <h1 class="text-theme-title font-medium mb-3">
    <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.projectProgress.funcCaseSummary.groupStats.byPriority') }}</span>
  </h1>
  <div ref="priorityRef" class="flex-1 h-50  w-120">
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
