<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  chart0Value: {
    yData: number[]
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
    yData: [0, 0, 0]
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
const oneTimePassedRef = ref();
const bugsRef = ref();
const twoTimePassedRef = ref();

let bugsChart;
let completedEChart;
let oneTimePassedEChart;
let twoTimePassedEChart;

// 完成任务
const bugsEChartConfig = {
  title: {
    text: t('taskAnalysis.detail.handlingEfficiency.chartTitles.completedTasks'),
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
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [
      t('taskAnalysis.detail.handlingEfficiency.chartLabels.completedTaskCount'),
      t('taskAnalysis.detail.handlingEfficiency.chartLabels.oneTimeCompletedCount'),
      t('taskAnalysis.detail.handlingEfficiency.chartLabels.twoTimeCompletedCount')
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
    subtext: t('taskAnalysis.detail.handlingEfficiency.chartTitles.completedTaskRatio'),
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
            color: 'rgba(136, 185, 242, 0.8)'
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

// 一次完成任务占比
const oneTimePassedEChartConfig = JSON.parse(JSON.stringify({
  ...completedEChartConfig,
  title: {
    ...completedEChartConfig.title,
    subtext: t('taskAnalysis.detail.handlingEfficiency.chartTitles.oneTimeCompletedRatio'),
    itemGap: 40
  }
}));

// 两次完成任务占比
const twoTimePassedEChartConfig = JSON.parse(JSON.stringify({
  ...oneTimePassedEChartConfig,
  title: {
    ...oneTimePassedEChartConfig.title,
    subtext: t('taskAnalysis.detail.handlingEfficiency.chartTitles.twoTimeCompletedRatio')
  }
}));

onMounted(() => {
  completedEChart = eCharts.init(completedRef.value);
  oneTimePassedEChart = eCharts.init(oneTimePassedRef.value);
  bugsChart = eCharts.init(bugsRef.value);
  twoTimePassedEChart = eCharts.init(twoTimePassedRef.value);

  watch([
    () => props.chart0Value,
    () => props.chart1Value,
    () => props.chart2Value,
    () => props.chart3Value,
    () => props.chart4Value
  ], () => {
    bugsEChartConfig.series[0].data = props.chart0Value.yData;

    completedEChartConfig.series[0].data[0] = {
      ...completedEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    completedEChartConfig.series[0].data[1] = {
      ...completedEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    completedEChartConfig.title.text = props.chart1Value.title;

    oneTimePassedEChartConfig.series[0].data[0] = {
      ...oneTimePassedEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    oneTimePassedEChartConfig.series[0].data[1] = {
      ...oneTimePassedEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    oneTimePassedEChartConfig.title.text = props.chart2Value.title;

    twoTimePassedEChartConfig.series[0].data[0] = {
      ...twoTimePassedEChartConfig.series[0].data[0],
      ...props.chart3Value.value[0]
    };
    twoTimePassedEChartConfig.series[0].data[1] = {
      ...twoTimePassedEChartConfig.series[0].data[1],
      ...props.chart3Value.value[1]
    };
    twoTimePassedEChartConfig.title.text = props.chart3Value.title;

    completedEChart.setOption(completedEChartConfig);
    oneTimePassedEChart.setOption(oneTimePassedEChartConfig);
    bugsChart.setOption(bugsEChartConfig);
    twoTimePassedEChart.setOption(twoTimePassedEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedEChart.resize();
    oneTimePassedEChart.resize();
    bugsChart.resize();
    twoTimePassedEChart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="bugsRef" class="flex-1 h-30"></div>
    <div ref="completedRef" class="flex-1 h-30"></div>
    <div ref="oneTimePassedRef" class="flex-1 h-30"></div>
    <div ref="twoTimePassedRef" class="flex-1 h-30"></div>
  </div>
</template>
