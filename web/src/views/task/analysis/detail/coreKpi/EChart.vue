<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  chart0Value: {
    yData0: number[],
    yData1: number[]
  };
  chart1Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
  chart2Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
  chart3Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
  chart4Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
}
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  chart0Value: () => ({
    yData0: [0, 0, 0, 0],
    yData1: [0, 0, 0, 0]
  }),
  chart1Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart3Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart4Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  })
});

const completedRef = ref();
const completedWorkloadRef = ref();
const coreRef = ref();
const completedOverduedRef = ref();
const completedBugRef = ref();

let coreChart;
let completedEChart;
let completedWorkloadEChart;
let completedOverduedEChart;
let completedBugEChart;

// 核心指标
const coreEChartConfig = {
  title: {
    text: t('taskAnalysis.detail.coreKpi.chartTitles.coreKpi'),
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
      t('taskAnalysis.detail.coreKpi.chartLabels.taskCount'),
      t('taskAnalysis.detail.coreKpi.chartLabels.workload'),
      t('taskAnalysis.detail.coreKpi.chartLabels.overdueCount'),
      t('taskAnalysis.detail.coreKpi.chartLabels.bugCount')
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
      t('taskAnalysis.detail.coreKpi.chartLabels.completedCount'),
      t('taskAnalysis.detail.coreKpi.chartLabels.totalCount')
    ],
    top: 0
  },
  series: [
    {
      name: t('taskAnalysis.detail.coreKpi.chartLabels.completedCount'),
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    },
    {
      name: t('taskAnalysis.detail.coreKpi.chartLabels.totalCount'),
      itemStyle: {
        color: 'rgba(136, 185, 242, 0.8)',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};
// 完成任务占比
const completedEChartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('taskAnalysis.detail.coreKpi.chartTitles.completedTaskRatio'),
    // left: '25%',
    // top: '40%',
    itemGap: 40,
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
      radius: '65%',
      // radius: ['50%', '65%'],
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
          name: '',
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: '',
          value: 0,
          itemStyle: {
            color: 'rgba(45, 142, 255, 1)'
          }
        }
      ]
    }
  ]
};

// 完成工作量占比
const completedWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...completedEChartConfig,
  title: {
    ...completedEChartConfig.title,
    subtext: t('taskAnalysis.detail.coreKpi.chartTitles.completedWorkloadRatio'),
    itemGap: 40
  }
}));

// 逾期逾期数占比
const completedOverdueEChartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEChartConfig,
  title: {
    ...completedWorkloadEChartConfig.title,
    subtext: t('taskAnalysis.detail.coreKpi.chartTitles.completedOverdueRatio')
  }
}));

// 完成缺陷占比
const completedBugEChartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEChartConfig,
  title: {
    ...completedWorkloadEChartConfig.title,
    subtext: t('taskAnalysis.detail.coreKpi.chartTitles.completedBugRatio')
  }
}));

onMounted(() => {
  completedEChart = eCharts.init(completedRef.value);
  completedWorkloadEChart = eCharts.init(completedWorkloadRef.value);
  coreChart = eCharts.init(coreRef.value);
  completedOverduedEChart = eCharts.init(completedOverduedRef.value);
  completedBugEChart = eCharts.init(completedBugRef.value);

  watch([
    () => props.chart0Value,
    () => props.chart1Value,
    () => props.chart2Value,
    () => props.chart3Value,
    () => props.chart4Value
  ], () => {
    coreEChartConfig.series[0].data = props.chart0Value.yData0;
    coreEChartConfig.series[1].data = props.chart0Value.yData1;

    completedEChartConfig.series[0].data[0] = {
      ...completedEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    completedEChartConfig.series[0].data[1] = {
      ...completedEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    completedEChartConfig.title.text = props.chart1Value.title;

    completedWorkloadEChartConfig.series[0].data[0] = {
      ...completedWorkloadEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    completedWorkloadEChartConfig.series[0].data[1] = {
      ...completedWorkloadEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    completedWorkloadEChartConfig.title.text = props.chart2Value.title;

    completedOverdueEChartConfig.series[0].data[0] = {
      ...completedOverdueEChartConfig.series[0].data[0],
      ...props.chart3Value.value[0]
    };
    completedOverdueEChartConfig.series[0].data[1] = {
      ...completedOverdueEChartConfig.series[0].data[1],
      ...props.chart3Value.value[1]
    };
    completedOverdueEChartConfig.title.text = props.chart3Value.title;

    completedBugEChartConfig.series[0].data[0] = {
      ...completedBugEChartConfig.series[0].data[0],
      ...props.chart4Value.value[0]
    };
    completedBugEChartConfig.series[0].data[1] = {
      ...completedBugEChartConfig.series[0].data[1],
      ...props.chart4Value.value[1]
    };
    completedBugEChartConfig.title.text = props.chart4Value.title;

    completedEChart.setOption(completedEChartConfig);
    completedWorkloadEChart.setOption(completedWorkloadEChartConfig);
    coreChart.setOption(coreEChartConfig);
    completedOverduedEChart.setOption(completedOverdueEChartConfig);
    completedBugEChart.setOption(completedBugEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedEChart.resize();
    completedWorkloadEChart.resize();
    coreChart.resize();
    completedOverduedEChart.resize();
    completedBugEChart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="coreRef" class="h-40 w-100"></div>
    <div ref="completedRef" class="flex-1 h-30"></div>
    <div ref="completedWorkloadRef" class="flex-1 h-30"></div>
    <div ref="completedOverduedRef" class="flex-1 h-30"></div>
    <div ref="completedBugRef" class="flex-1 h-30"></div>
  </div>
</template>
