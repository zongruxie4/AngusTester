<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
  value: Record<string, any>;
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
  value: () => ({}),
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

const bugLevelRef = ref();
const validBugRef = ref();
const bugsRef = ref();
const escapedBugRef = ref();
const bugWorkloadRef = ref();

let bugsChart;
let bugLevelEchart;
let validBugEchart;
let escapedBugEchart;
let bugWorkloadEchart;

// 缺陷
const bugsEchartConfig = {
  title: {
    text: t('common.counts.bugCount'),
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
      t('functionAnalysis.detail.bugs.totalBugs'),
      t('functionAnalysis.detail.bugs.validBugs'),
      t('functionAnalysis.detail.bugs.invalidBugs'),
      t('functionAnalysis.detail.bugs.escapedBugs')
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
        color: 'rgba(245, 34, 45, 0.5)',
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
const bugLevelEchartConfig = {
  title: {
    text: '0%',
    left: '30%',
    top: '35%',
    padding: 2,
    subtext: t('common.bugLevel'),
    // left: '25%',
    // top: '40%',
    itemGap: 70,
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
      // radius: ['45%', '60%'],
      radius: '60%',
      center: ['30%', '40%'],
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
          name: t('functionAnalysis.detail.bugs.criticalCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: t('functionAnalysis.detail.bugs.majorCount'),
          value: 0,
          itemStyle: {
            color: 'gold'
          }
        },
        {
          name: t('functionAnalysis.detail.bugs.minorCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: t('functionAnalysis.detail.bugs.trivialCount'),
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
const validBugEchartConfig = JSON.parse(JSON.stringify({
  ...bugLevelEchartConfig,
  title: {
    ...bugLevelEchartConfig.title,
    subtext: t('functionAnalysis.detail.bugs.validBugPercentage'),
    itemGap: 60
  },
  series: [{
    ...bugLevelEchartConfig.series[0],
    data: [
      {
        name: t('status.notCompleted'),
        value: 0,
        itemStyle: {
          color: 'rgba(136, 185, 242, 1)'
        }
      },
      {
        name: t('status.completed'),
        value: 0,
        itemStyle: {
          color: 'rgba(245, 34, 45, 0.6)'
        }
      }
    ]
  }]
}));

// 漏测缺陷
const escapedBugEchartConfig = JSON.parse(JSON.stringify({
  ...validBugEchartConfig,
  title: {
    ...validBugEchartConfig.title,
    subtext: t('functionAnalysis.detail.bugs.escapedBugPercentage')

  }
}));

// 缺陷工作量
const bugWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...validBugEchartConfig,
  title: {
    ...validBugEchartConfig.title,
    subtext: t('functionAnalysis.detail.bugs.bugWorkloadPercentage')
  }
}));

onMounted(() => {
  bugLevelEchart = eCharts.init(bugLevelRef.value);

  validBugEchart = eCharts.init(validBugRef.value);

  bugsChart = eCharts.init(bugsRef.value);

  escapedBugEchart = eCharts.init(escapedBugRef.value);

  bugWorkloadEchart = eCharts.init(bugWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value, () => props.chart3Value, () => props.chart4Value], () => {
    bugsEchartConfig.series[0].data = props.chart0Value.yData;

    bugLevelEchartConfig.series[0].data[0] = {
      ...bugLevelEchartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    bugLevelEchartConfig.series[0].data[1] = {
      ...bugLevelEchartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    bugLevelEchartConfig.series[0].data[2] = {
      ...bugLevelEchartConfig.series[0].data[2],
      ...props.chart1Value.value[2]
    };
    bugLevelEchartConfig.series[0].data[3] = {
      ...bugLevelEchartConfig.series[0].data[3],
      ...props.chart1Value.value[3]
    };
    bugLevelEchartConfig.title.text = props.chart1Value.title;

    validBugEchartConfig.series[0].data[0] = {
      ...validBugEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    validBugEchartConfig.series[0].data[1] = {
      ...validBugEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    validBugEchartConfig.title.text = props.chart2Value.title;

    escapedBugEchartConfig.series[0].data[0] = {
      ...escapedBugEchartConfig.series[0].data[0],
      ...props.chart3Value.value[0]
    };
    escapedBugEchartConfig.series[0].data[1] = {
      ...escapedBugEchartConfig.series[0].data[1],
      ...props.chart3Value.value[1]
    };
    escapedBugEchartConfig.title.text = props.chart3Value.title;

    bugWorkloadEchartConfig.series[0].data[0] = {
      ...bugWorkloadEchartConfig.series[0].data[0],
      ...props.chart4Value.value[0]
    };
    bugWorkloadEchartConfig.series[0].data[1] = {
      ...bugWorkloadEchartConfig.series[0].data[1],
      ...props.chart4Value.value[1]
    };
    bugWorkloadEchartConfig.title.text = props.chart4Value.title;

    bugLevelEchart.setOption(bugLevelEchartConfig);
    validBugEchart.setOption(validBugEchartConfig);
    bugsChart.setOption(bugsEchartConfig);
    escapedBugEchart.setOption(escapedBugEchartConfig);
    bugWorkloadEchart.setOption(bugWorkloadEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    bugLevelEchart.resize();
    validBugEchart.resize();
    escapedBugEchart.resize();
    bugsChart.resize();
    bugWorkloadEchart.resize();
  }
});

</script>
<template>
  <div class="flex space-x-2">
    <div class="w-30 space-y-2 text-center">
      <div>
        <span class="text-8 font-semibold">{{ props.value.testCaseHitRate || 0 }}</span>%
        <div>{{ t('functionAnalysis.detail.bugs.testCaseHitRate') }}</div>
      </div>
      <div>
        <span class="text-8 font-semibold">{{ props.value.testCaseHitNum || 0 }}</span>
        <div>{{ t('functionAnalysis.detail.bugs.testCaseHitCount') }}</div>
      </div>
    </div>
    <div ref="bugsRef" class="flex-1 min-w-80 h-40"></div>
    <div ref="bugLevelRef" class="flex-1 h-40"></div>
    <div ref="validBugRef" class="flex-1 h-40"></div>
    <div ref="escapedBugRef" class="flex-1 h-40"></div>
    <div ref="bugWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
