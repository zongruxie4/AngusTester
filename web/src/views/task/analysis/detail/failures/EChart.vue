<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  chart0Value: {
    yData: number[]
  };
  chart1Value: {
    yData: number[]
  }
  chart2Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
}
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  chart0Value: () => ({
    yData: [0, 0, 0, 0, 0]
  }),
  chart1Value: () => ({
    yData: [0, 0, 0, 0]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  })
});

const failureTimeRef = ref();
const failureLevelRef = ref();
const failureRef = ref();

let failureChart;
let failureTimeEChart;
let failureLevelEChart;

// 故障数
const failureEChartConfig = {
  title: {
    text: t('taskAnalysis.detail.failures.chartTitles.failureCount'),
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
      t('taskAnalysis.detail.failures.chartLabels.totalFailures'),
      t('taskAnalysis.detail.failures.chartLabels.completedFailures'),
      t('taskAnalysis.detail.failures.chartLabels.overdueFailures'),
      t('taskAnalysis.detail.failures.chartLabels.oneTimeFailures'),
      t('taskAnalysis.detail.failures.chartLabels.twoTimeFailures')
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
        color: 'red',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};
// 故障时间（小时）
const failureTimeEChartConfig = JSON.parse(JSON.stringify({
  ...failureEChartConfig,
  title: {
    text: t('taskAnalysis.detail.failures.chartTitles.failureTime'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  xAxis: {
    ...failureEChartConfig.xAxis,
    data: [
      t('taskAnalysis.detail.failures.chartLabels.totalFailureTime'),
      t('taskAnalysis.detail.failures.chartLabels.averageFailureTime'),
      t('taskAnalysis.detail.failures.chartLabels.minFailureTime'),
      t('taskAnalysis.detail.failures.chartLabels.maxFailureTime')
    ]
  },
  series: [
    {
      ...failureEChartConfig.series[0],
      data: [0, 0, 0, 0],
      itemStyle: { // rgba(255, 165, 43, 1)
        color: 'orange',
        borderRadius: [5, 5, 0, 0]
      }
    }
  ]
}));

//
// 缺陷等级
const failureLevelEChartConfig = {
  title: {
    text: '',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('taskAnalysis.detail.failures.chartTitles.failureLevel'),
    itemGap: 65,
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
      // radius: ['50%', '65%'],
      radius: '65%',
      center: ['35%', '40%'],
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
          name: t('taskAnalysis.detail.failures.pieChartLabels.criticalCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: t('taskAnalysis.detail.failures.pieChartLabels.majorCount'),
          value: 0,
          itemStyle: {
            color: 'gold'
          }
        },
        {
          name: t('taskAnalysis.detail.failures.pieChartLabels.minorCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: t('taskAnalysis.detail.failures.pieChartLabels.trivialCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(136, 185, 242, 1)'
          }
        }
      ]
    }
  ]
};

onMounted(() => {
  failureTimeEChart = eCharts.init(failureTimeRef.value);
  failureLevelEChart = eCharts.init(failureLevelRef.value);
  failureChart = eCharts.init(failureRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value, () => props.chart3Value, () => props.chart4Value], () => {
    failureEChartConfig.series[0].data = props.chart0Value.yData;
    failureTimeEChartConfig.series[0].data = props.chart1Value.yData;

    failureLevelEChartConfig.series[0].data[0] = {
      ...failureLevelEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    failureLevelEChartConfig.series[0].data[1] = {
      ...failureLevelEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };

    failureLevelEChartConfig.series[0].data[2] = {
      ...failureLevelEChartConfig.series[0].data[2],
      ...props.chart2Value.value[2]
    };
    failureLevelEChartConfig.series[0].data[3] = {
      ...failureLevelEChartConfig.series[0].data[4],
      ...props.chart2Value.value[3]
    };
    failureLevelEChartConfig.title.text = props.chart2Value.title;

    failureTimeEChart.setOption(failureTimeEChartConfig);
    failureLevelEChart.setOption(failureLevelEChartConfig);
    failureChart.setOption(failureEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    failureTimeEChart.resize();
    failureLevelEChart.resize();
    failureChart.resize();
  }
});

</script>
<template>
  <div class="flex space-x-5">
    <div ref="failureRef" class="flex-1 h-40"></div>
    <div ref="failureTimeRef" class="flex-1 h-40"></div>
    <div ref="failureLevelRef" class="flex-1 h-40"></div>
  </div>
</template>
