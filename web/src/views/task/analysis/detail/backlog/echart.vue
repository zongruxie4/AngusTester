<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
  // title0: string;
  // title1: string;
  // value0: {name: string, value: string|number}[];
  // value1: {name: string, value: string|number}[];

  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData0: number[],
    yData1: number[]
  },

  chart1Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
  chart2Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
}
const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData0: [0, 0],
    yData1: [0, 0]
  }),
  chart1Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  })
});

const backlogRef = ref();
const backloggedTaskRef = ref();
const backloggedWorkloadRef = ref();

let backlogRefEchart;
let backloggedTaskEchart;
let backloggedWorkloadEchart;

// 积压量
const backlogEchartConfig = {
  title: {
    text: t('taskAnalysis.detail.backlogTasks.chartLabels.backlogAmount'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '40',
    right: '30',
    bottom: '50',
    top: '40'
  },
  xAxis: {
    type: 'category',
    data: [t('taskAnalysis.detail.backlogTasks.chartLabels.taskCount'), t('taskAnalysis.detail.backlogTasks.chartLabels.workload')],
    axisLabel: {
      interval: 0,
      overflow: 'break'
    }
  },
  yAxis: [{
    type: 'value'
  }],
  tooltip: {
    show: true
  },
  legend: {
    show: true,
    data: [t('taskAnalysis.detail.backlogTasks.chartLabels.backlogAmount'), t('taskAnalysis.detail.backlogTasks.chartLabels.totalAmount')],
    top: 0
  },
  series: [
    {
      name: t('taskAnalysis.detail.backlogTasks.chartLabels.backlogAmount'),
      itemStyle: {
        color: 'orange',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    },
    {
      name: t('taskAnalysis.detail.backlogTasks.chartLabels.totalAmount'),
      itemStyle: {
        color: 'rgba(136, 185, 242, 0.8)',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};
// 积压任务数
const backloggedTaskEchartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '40%',
    padding: 2,
    subtext: t('taskAnalysis.detail.backlogTasks.chartLabels.backlogTaskRatio'),
    // left: '25%',
    // top: '40%',
    itemGap: 55,
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 12,
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
      radius: '55%',
      center: ['35%', '45%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      // itemStyle: {
      //   borderRadius: 2,
      //   borderColor: '#fff',
      //   borderWidth: 1
      // },
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
          name: t('taskAnalysis.detail.backlogTasks.chartLabels.uncompleted'),
          value: 0,
          itemStyle: {
            color: 'rgba(217, 217, 217, 1)'
          }
        },
        {
          name: t('taskAnalysis.detail.backlogTasks.chartLabels.completed'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
};

// 积压任务量
const backloggedWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...backloggedTaskEchartConfig,
  title: {
    ...backloggedTaskEchartConfig.title,
    subtext: t('taskAnalysis.detail.backlogTasks.chartLabels.backlogWorkloadRatio')
  }
}));

onMounted(() => {
  backlogRefEchart = eCharts.init(backlogRef.value);
  backloggedTaskEchart = eCharts.init(backloggedTaskRef.value);

  backloggedWorkloadEchart = eCharts.init(backloggedWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value], () => {
    backlogEchartConfig.series[0].data = props.chart0Value.yData0;
    backlogEchartConfig.series[1].data = props.chart0Value.yData1;

    backloggedTaskEchartConfig.series[0].data[0] = {
      ...backloggedTaskEchartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    backloggedTaskEchartConfig.series[0].data[1] = {
      ...backloggedTaskEchartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    backloggedTaskEchartConfig.title.text = props.chart1Value.title;

    backloggedWorkloadEchartConfig.series[0].data[0] = {
      ...backloggedWorkloadEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    backloggedWorkloadEchartConfig.series[0].data[1] = {
      ...backloggedWorkloadEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    backloggedWorkloadEchartConfig.title.text = props.chart2Value.title;
    backloggedTaskEchart.setOption(backloggedTaskEchartConfig);
    backloggedWorkloadEchart.setOption(backloggedWorkloadEchartConfig);
    backlogRefEchart.setOption(backlogEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    backloggedTaskEchart.resize();
    backloggedWorkloadEchart.resize();
    backlogRefEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div class="px-3 w-100">
      <div class="flex justify-around">
        <div class="text-center flex-1">
          <div class="font-semibold "><span class="text-5 text-status-error">{{ props.overdueAssessmentData.backloggedCompletionTime || 0 }}</span>{{ t('taskAnalysis.detail.backlogTasks.statistics.hours') }}</div>
          <div>
            {{ t('taskAnalysis.detail.backlogTasks.statistics.backlogWorkloadEstimatedTime') }}
          </div>
        </div>
      </div>
      <div class="flex justify-around mt-3">
        <div class="text-center">
          <div class="font-semibold text-5">{{ props.overdueAssessmentData.dailyProcessedNum || 0 }}</div>
          <div>
            {{ t('taskAnalysis.detail.backlogTasks.statistics.averageDailyProcessedTasks') }}
          </div>
        </div>
        <div class="text-center">
          <div class="font-semibold text-5">{{ props.overdueAssessmentData.dailyProcessedWorkload || 0 }}</div>
          <div>
            {{ t('taskAnalysis.detail.backlogTasks.statistics.averageDailyProcessedWorkload') }}
          </div>
        </div>
      </div>
    </div>
    <div ref="backlogRef" class="flex-1 h-40"></div>
    <div ref="backloggedTaskRef" class="flex-1 h-40"></div>
    <div ref="backloggedWorkloadRef" class="flex-1 h-40"></div>
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
