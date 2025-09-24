<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
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
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  })
});

const backlogRef = ref();
const backloggedTaskRef = ref();
const backloggedWorkloadRef = ref();

let backlogRefEChart;
let backloggedTaskEChart;
let backloggedWorkloadEChart;

// 积压量
const backlogEChartConfig = {
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
    data: [
      t('taskAnalysis.detail.backlogTasks.chartLabels.taskCount'),
      t('taskAnalysis.detail.backlogTasks.chartLabels.workload')
    ],
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
    data: [
      t('taskAnalysis.detail.backlogTasks.chartLabels.backlogAmount'),
      t('taskAnalysis.detail.backlogTasks.chartLabels.totalAmount')
    ],
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
        color: 'rgb(68,93,179)',
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
const backloggedTaskEChartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '40%',
    padding: 2,
    subtext: t('taskAnalysis.detail.backlogTasks.chartLabels.backlogTaskRatio'),
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
          name: t('status.notCompleted'),
          value: 0,
          itemStyle: {
            color: 'rgb(15,175,62)'
          }
        },
        {
          name: t('status.completed'),
          value: 0,
          itemStyle: {
            color: 'rgb(246,159,42)'
          }
        }
      ]
    }
  ]
};

const backloggedWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...backloggedTaskEChartConfig,
  title: {
    ...backloggedTaskEChartConfig.title,
    subtext: t('taskAnalysis.detail.backlogTasks.chartLabels.backlogWorkloadRatio')
  }
}));

onMounted(() => {
  backlogRefEChart = eCharts.init(backlogRef.value);
  backloggedTaskEChart = eCharts.init(backloggedTaskRef.value);

  backloggedWorkloadEChart = eCharts.init(backloggedWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value], () => {
    backlogEChartConfig.series[0].data = props.chart0Value.yData0;
    backlogEChartConfig.series[1].data = props.chart0Value.yData1;

    backloggedTaskEChartConfig.series[0].data[0] = {
      ...backloggedTaskEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0],
      value: Number(props.chart1Value.value[0].value)
    };
    backloggedTaskEChartConfig.series[0].data[1] = {
      ...backloggedTaskEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1],
      value: Number(props.chart1Value.value[1].value)
    };
    backloggedTaskEChartConfig.title.text = props.chart1Value.title;

    backloggedWorkloadEChartConfig.series[0].data[0] = {
      ...backloggedWorkloadEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0],
      value: Number(props.chart2Value.value[0].value)
    };
    backloggedWorkloadEChartConfig.series[0].data[1] = {
      ...backloggedWorkloadEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1],
      value: Number(props.chart2Value.value[1].value)
    };
    backloggedWorkloadEChartConfig.title.text = props.chart2Value.title;
    backloggedTaskEChart.setOption(backloggedTaskEChartConfig);
    backloggedWorkloadEChart.setOption(backloggedWorkloadEChartConfig);
    backlogRefEChart.setOption(backlogEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    backloggedTaskEChart.resize();
    backloggedWorkloadEChart.resize();
    backlogRefEChart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div class="px-3 w-100">
      <div class="flex justify-around">
        <div class="text-center flex-1">
          <div class="font-semibold ">
            <span class="text-5 text-status-error">
              {{ props.overdueAssessmentData.backloggedCompletionTime || 0 }}
            </span>
            {{ t('taskAnalysis.detail.backlogTasks.statistics.hours') }}
          </div>
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
