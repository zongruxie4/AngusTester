<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

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
const props = withDefaults(defineProps<Props>(), {
  chart0Value: () => ({
    yData0: [0, 0, 0, 0],
    yData1: [0, 0, 0, 0]
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
const completedWorkloadRef = ref();
const coreRef = ref();
const completedOverdueRef = ref();
const completedBugRef = ref();

let coreChart;
let completedEchart;
let completedWorkloadEchart;
let completedOverdueEchart;
let completedBugEchart;

const coreEchartConfig = {
  title: {
    text: t('functionAnalysis.detail.coreKpi.coreIndicators'),
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
      t('functionAnalysis.detail.coreKpi.caseCount'),
      t('functionAnalysis.detail.coreKpi.workload'),
      t('functionAnalysis.detail.coreKpi.overdueCount'),
      t('functionAnalysis.detail.coreKpi.bugCount')
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
      t('functionAnalysis.detail.coreKpi.completedAmount'),
      t('functionAnalysis.detail.coreKpi.totalAmount')
    ],
    top: 0
  },
  series: [
    {
      name: t('functionAnalysis.detail.coreKpi.completedAmount'),
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
      name: t('functionAnalysis.detail.coreKpi.totalAmount'),
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
// 完成用例占比
const completedEchartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('functionAnalysis.detail.coreKpi.completedCasePercentage'),
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
const completedWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...completedEchartConfig,
  title: {
    ...completedEchartConfig.title,
    subtext: t('functionAnalysis.detail.coreKpi.completedWorkloadPercentage'),
    itemGap: 40
  }
}));

// 逾期逾期数占比
const completedOverdueEchartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEchartConfig,
  title: {
    ...completedWorkloadEchartConfig.title,
    subtext: t('functionAnalysis.detail.coreKpi.completedOverduePercentage')
  }
}));

// 完成缺陷占比
const completedBugEchartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEchartConfig,
  title: {
    ...completedWorkloadEchartConfig.title,
    subtext: t('functionAnalysis.detail.coreKpi.completedBugPercentage')
  }
}));

onMounted(() => {
  completedEchart = eCharts.init(completedRef.value);

  completedWorkloadEchart = eCharts.init(completedWorkloadRef.value);

  coreChart = eCharts.init(coreRef.value);

  completedOverdueEchart = eCharts.init(completedOverdueRef.value);

  completedBugEchart = eCharts.init(completedBugRef.value);

  watch([
    () => props.chart0Value,
    () => props.chart1Value,
    () => props.chart2Value,
    () => props.chart3Value,
    () => props.chart4Value
  ], () => {
    coreEchartConfig.series[0].data = props.chart0Value.yData0;
    coreEchartConfig.series[1].data = props.chart0Value.yData1;

    completedEchartConfig.series[0].data[0] = {
      ...completedEchartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    completedEchartConfig.series[0].data[1] = {
      ...completedEchartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    completedEchartConfig.title.text = props.chart1Value.title;

    completedWorkloadEchartConfig.series[0].data[0] = {
      ...completedWorkloadEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    completedWorkloadEchartConfig.series[0].data[1] = {
      ...completedWorkloadEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    completedWorkloadEchartConfig.title.text = props.chart2Value.title;

    completedOverdueEchartConfig.series[0].data[0] = {
      ...completedOverdueEchartConfig.series[0].data[0],
      ...props.chart3Value.value[0]
    };
    completedOverdueEchartConfig.series[0].data[1] = {
      ...completedOverdueEchartConfig.series[0].data[1],
      ...props.chart3Value.value[1]
    };
    completedOverdueEchartConfig.title.text = props.chart3Value.title;

    completedBugEchartConfig.series[0].data[0] = {
      ...completedBugEchartConfig.series[0].data[0],
      ...props.chart4Value.value[0]
    };
    completedBugEchartConfig.series[0].data[1] = {
      ...completedBugEchartConfig.series[0].data[1],
      ...props.chart4Value.value[1]
    };
    completedBugEchartConfig.title.text = props.chart4Value.title;

    completedEchart.setOption(completedEchartConfig);
    completedWorkloadEchart.setOption(completedWorkloadEchartConfig);
    coreChart.setOption(coreEchartConfig);
    completedOverdueEchart.setOption(completedOverdueEchartConfig);
    completedBugEchart.setOption(completedBugEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedEchart.resize();
    completedWorkloadEchart.resize();
    coreChart.resize();
    completedOverdueEchart.resize();
    completedBugEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="coreRef" class="h-40 w-100"></div>
    <div ref="completedRef" class="flex-1 h-30"></div>
    <div ref="completedWorkloadRef" class="flex-1 h-30"></div>
    <div ref="completedOverdueRef" class="flex-1 h-30"></div>
    <div ref="completedBugRef" class="flex-1 h-30"></div>
  </div>
</template>
