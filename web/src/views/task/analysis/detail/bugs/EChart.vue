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
    yData: [0, 0, 0, 0]
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

const bugLevelRef = ref();
const validBugRef = ref();
const bugsRef = ref();
const missingBugRef = ref();
const bugWorkloadRef = ref();

let bugsChart;
let bugLevelEChart;
let validBugEChart;
let missingBugEChart;
let bugWorkloadEChart;

// 缺陷
const bugsEChartConfig = {
  title: {
    text: t('taskAnalysis.detail.bugs.chartTitles.bugCount'),
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
    data: [t('taskAnalysis.detail.bugs.chartLabels.totalBugs'),
      t('taskAnalysis.detail.bugs.chartLabels.validBugs'),
      t('taskAnalysis.detail.bugs.chartLabels.invalidBugs'),
      t('taskAnalysis.detail.bugs.chartLabels.missingBugs')],
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
        color: 'rgba(245, 34, 45, 1)',
        borderRadius: [5, 5, 0, 0]
      },

      data: [0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};
// 缺陷等级
const bugLevelEChartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '35%',
    padding: 2,
    subtext: t('taskAnalysis.detail.bugs.chartTitles.bugLevel'),
    // left: '25%',
    // top: '40%',
    itemGap: 60,
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
          name: t('taskAnalysis.detail.bugs.pieChartLabels.criticalCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: t('taskAnalysis.detail.bugs.pieChartLabels.majorCount'),
          value: 0,
          itemStyle: {
            color: 'gold'
          }
        },
        {
          name: t('taskAnalysis.detail.bugs.pieChartLabels.minorCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: t('taskAnalysis.detail.bugs.pieChartLabels.trivialCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(136, 185, 242, 1)'
          }
        }
      ]
    }
  ]
};

// 有效缺陷
const validBugEChartConfig = JSON.parse(JSON.stringify({
  ...bugLevelEChartConfig,
  title: {
    ...bugLevelEChartConfig.title,
    subtext: t('taskAnalysis.detail.bugs.chartTitles.validBugRatio'),
    itemGap: 50
  },
  series: [{
    ...bugLevelEChartConfig.series[0],
    data: [
      {
        name: t('taskAnalysis.detail.bugs.chartLabels.uncompleted'),
        value: 0,
        itemStyle: {
          color: 'rgba(136, 185, 242, 1)'
        }
      },
      {
        name: t('taskAnalysis.detail.bugs.chartLabels.completed'),
        value: 0,
        itemStyle: {
          color: 'rgba(245, 34, 45, 0.6)'
        }
      }
    ]
  }]
}));

// 漏测缺陷
const missingBugEChartConfig = JSON.parse(JSON.stringify({
  ...validBugEChartConfig,
  title: {
    ...validBugEChartConfig.title,
    subtext: t('taskAnalysis.detail.bugs.chartTitles.missingBugRatio')
  }
}));

// 缺陷工作量
const bugWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...validBugEChartConfig,
  title: {
    ...validBugEChartConfig.title,
    subtext: t('taskAnalysis.detail.bugs.chartTitles.bugWorkloadRatio')
  }
}));

onMounted(() => {
  bugLevelEChart = eCharts.init(bugLevelRef.value);

  validBugEChart = eCharts.init(validBugRef.value);

  bugsChart = eCharts.init(bugsRef.value);

  missingBugEChart = eCharts.init(missingBugRef.value);

  bugWorkloadEChart = eCharts.init(bugWorkloadRef.value);

  watch([
    () => props.chart0Value,
    () => props.chart1Value,
    () => props.chart2Value,
    () => props.chart3Value,
    () => props.chart4Value],
  () => {
    bugsEChartConfig.series[0].data = props.chart0Value.yData;

    bugLevelEChartConfig.series[0].data[0] = {
      ...bugLevelEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    bugLevelEChartConfig.series[0].data[1] = {
      ...bugLevelEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    bugLevelEChartConfig.series[0].data[2] = {
      ...bugLevelEChartConfig.series[0].data[2],
      ...props.chart1Value.value[2]
    };
    bugLevelEChartConfig.series[0].data[3] = {
      ...bugLevelEChartConfig.series[0].data[3],
      ...props.chart1Value.value[3]
    };
    bugLevelEChartConfig.title.text = props.chart1Value.title;

    validBugEChartConfig.series[0].data[0] = {
      ...validBugEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    validBugEChartConfig.series[0].data[1] = {
      ...validBugEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    validBugEChartConfig.title.text = props.chart2Value.title;

    missingBugEChartConfig.series[0].data[0] = {
      ...missingBugEChartConfig.series[0].data[0],
      ...props.chart3Value.value[0]
    };
    missingBugEChartConfig.series[0].data[1] = {
      ...missingBugEChartConfig.series[0].data[1],
      ...props.chart3Value.value[1]
    };
    missingBugEChartConfig.title.text = props.chart3Value.title;

    bugWorkloadEChartConfig.series[0].data[0] = {
      ...bugWorkloadEChartConfig.series[0].data[0],
      ...props.chart4Value.value[0]
    };
    bugWorkloadEChartConfig.series[0].data[1] = {
      ...bugWorkloadEChartConfig.series[0].data[1],
      ...props.chart4Value.value[1]
    };
    bugWorkloadEChartConfig.title.text = props.chart4Value.title;

    bugLevelEChart.setOption(bugLevelEChartConfig);
    validBugEChart.setOption(validBugEChartConfig);
    bugsChart.setOption(bugsEChartConfig);
    missingBugEChart.setOption(missingBugEChartConfig);
    bugWorkloadEChart.setOption(bugWorkloadEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    bugLevelEChart.resize();
    validBugEChart.resize();
    missingBugEChart.resize();
    bugsChart.resize();
    bugWorkloadEChart.resize();
  }
});

</script>
<template>
  <div class="flex space-x-4">
    <div ref="bugsRef" class="flex-1 min-w-80 h-35"></div>
    <div ref="bugLevelRef" class="flex-1 h-35"></div>
    <div ref="validBugRef" class="flex-1 h-35"></div>
    <div ref="missingBugRef" class="flex-1 h-35"></div>
    <div ref="bugWorkloadRef" class="flex-1 h-35"></div>
  </div>
</template>
