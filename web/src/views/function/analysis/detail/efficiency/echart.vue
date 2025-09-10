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
const props = withDefaults(defineProps<Props>(), {
  chart0Value: () => ({
    chart0Value: [0, 0, 0, 0]
  }),
  chart1Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  }),
  chart3Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  }),
  chart4Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  })
});

const completedRef = ref();
const oneTimePassedRef = ref();
const bugsRef = ref();
const twoTimePassedRef = ref();

let bugsChart;
let completedEchart;
let oneTimePassedEchart;
let twoTimePassedEchart;

// 完成用例
const bugsEchartConfig = {
  title: {
    text: t('functionAnalysis.detail.handlingEfficiency.completedCases'),
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
    data: [t('functionAnalysis.detail.handlingEfficiency.completedCaseCount'), t('functionAnalysis.detail.handlingEfficiency.oneTimeCompletedCount'), t('functionAnalysis.detail.handlingEfficiency.twoTimeCompletedCount')],
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
// 完成用例占比
const completedEchartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('functionAnalysis.detail.handlingEfficiency.completedCasePercentage'),
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

// 一次完成用例占比
const oneTimePassedEchartConfig = JSON.parse(JSON.stringify({
  ...completedEchartConfig,
  title: {
    ...completedEchartConfig.title,
    subtext: t('functionAnalysis.detail.handlingEfficiency.oneTimeCompletedPercentage'),
    itemGap: 40
  }
}));

// 两次完成用例占比
const twoTimePassedEchartConfig = JSON.parse(JSON.stringify({
  ...oneTimePassedEchartConfig,
  title: {
    ...oneTimePassedEchartConfig.title,
    subtext: t('functionAnalysis.detail.handlingEfficiency.twoTimeCompletedPercentage')
  }
}));

onMounted(() => {
  completedEchart = eCharts.init(completedRef.value);

  oneTimePassedEchart = eCharts.init(oneTimePassedRef.value);

  bugsChart = eCharts.init(bugsRef.value);

  twoTimePassedEchart = eCharts.init(twoTimePassedRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value, () => props.chart3Value, () => props.chart4Value], () => {
    bugsEchartConfig.series[0].data = props.chart0Value.yData;

    completedEchartConfig.series[0].data[0] = {
      ...completedEchartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    completedEchartConfig.series[0].data[1] = {
      ...completedEchartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    completedEchartConfig.title.text = props.chart1Value.title;

    oneTimePassedEchartConfig.series[0].data[0] = {
      ...oneTimePassedEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    oneTimePassedEchartConfig.series[0].data[1] = {
      ...oneTimePassedEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    oneTimePassedEchartConfig.title.text = props.chart2Value.title;

    twoTimePassedEchartConfig.series[0].data[0] = {
      ...twoTimePassedEchartConfig.series[0].data[0],
      ...props.chart3Value.value[0]
    };
    twoTimePassedEchartConfig.series[0].data[1] = {
      ...twoTimePassedEchartConfig.series[0].data[1],
      ...props.chart3Value.value[1]
    };
    twoTimePassedEchartConfig.title.text = props.chart3Value.title;

    completedEchart.setOption(completedEchartConfig);
    oneTimePassedEchart.setOption(oneTimePassedEchartConfig);
    bugsChart.setOption(bugsEchartConfig);
    twoTimePassedEchart.setOption(twoTimePassedEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedEchart.resize();
    oneTimePassedEchart.resize();
    bugsChart.resize();
    twoTimePassedEchart.resize();
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
