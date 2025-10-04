<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

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
const props = withDefaults(defineProps<Props>(), {
  chart0Value: () => ({
    chart0Value: [0, 0, 0, 0, 0]
  }),
  chart1Value: () => ({
    chart0Value: [0, 0, 0, 0]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  })
});

const failureTimeRef = ref();
const failureLevelRef = ref();
const failureRef = ref();

let failureChart;
let failureTimeEchart;
let failureLevelEchart;

// 故障数
const failureEchartConfig = {
  title: {
    text: t('testAnalysis.detail.failures.failureCount'),
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
      t('testAnalysis.detail.failures.totalFailure'),
      t('testAnalysis.detail.failures.failureCompletedCount'),
      t('testAnalysis.detail.failures.failureOverdueCount'),
      t('testAnalysis.detail.failures.oneTimeFailureCount'),
      t('testAnalysis.detail.failures.twoTimeFailureCount')
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
const failureTimeEchartConfig = JSON.parse(JSON.stringify({
  ...failureEchartConfig,
  title: {
    text: t('testAnalysis.detail.failures.failureTime'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  xAxis: {
    ...failureEchartConfig.xAxis,
    data: [
      t('testAnalysis.detail.failures.totalFailureTime'),
      t('testAnalysis.detail.failures.averageFailureTime'),
      t('testAnalysis.detail.failures.minFailureTime'),
      t('testAnalysis.detail.failures.maxFailureTime')
    ]
  },
  series: [
    {
      ...failureEchartConfig.series[0],
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
const failureLevelEchartConfig = {
  title: {
    text: '',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('testAnalysis.detail.failures.defectLevel'),
    // left: '25%',
    // top: '40%',
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
          name: t('testAnalysis.detail.failures.criticalCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: t('testAnalysis.detail.failures.majorCount'),
          value: 0,
          itemStyle: {
            color: 'gold'
          }
        },
        {
          name: t('testAnalysis.detail.failures.minorCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: t('testAnalysis.detail.failures.trivialCount'),
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
  failureTimeEchart = eCharts.init(failureTimeRef.value);

  failureLevelEchart = eCharts.init(failureLevelRef.value);

  failureChart = eCharts.init(failureRef.value);

  watch([
    () => props.chart0Value,
    () => props.chart1Value,
    () => props.chart2Value,
    () => props.chart3Value,
    () => props.chart4Value
  ], () => {
    failureEchartConfig.series[0].data = props.chart0Value.yData;
    failureTimeEchartConfig.series[0].data = props.chart1Value.yData;

    failureLevelEchartConfig.series[0].data[0] = {
      ...failureLevelEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    failureLevelEchartConfig.series[0].data[1] = {
      ...failureLevelEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };

    failureLevelEchartConfig.series[0].data[2] = {
      ...failureLevelEchartConfig.series[0].data[2],
      ...props.chart2Value.value[2]
    };
    failureLevelEchartConfig.series[0].data[3] = {
      ...failureLevelEchartConfig.series[0].data[4],
      ...props.chart2Value.value[3]
    };
    failureLevelEchartConfig.title.text = props.chart2Value.title;

    failureTimeEchart.setOption(failureTimeEchartConfig);
    failureLevelEchart.setOption(failureLevelEchartConfig);
    failureChart.setOption(failureEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    failureTimeEchart.resize();
    failureLevelEchart.resize();
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
