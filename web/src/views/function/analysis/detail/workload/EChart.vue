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
    title: string;
    value: {name: string, value: string|number}[];
  }
  chart2Value: {
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
  })
});

const completedWorkloadRef = ref();
const savingWorkloadRef = ref();
const workloadRef = ref();

let workloadChart;
let completedWorkloadEchart;
let savingWorkloadEchart;

const workloadEchartConfig = {
  title: {
    text: t('functionAnalysis.detail.workload.workload'),
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
      t('functionAnalysis.detail.workload.estimatedWorkload'),
      t('functionAnalysis.detail.workload.actualWorkload'),
      t('functionAnalysis.detail.workload.completedWorkload'),
      t('functionAnalysis.detail.workload.savingWorkload')
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

const completedWorkloadEchartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '38%',
    padding: 2,
    subtext: t('functionAnalysis.detail.workload.completedWorkloadPercentage'),
    // left: '25%',
    // top: '40%',
    itemGap: 47,
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
      // radius: ['50%', '70%'],
      radius: '70%',
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
          name: t('functionAnalysis.detail.workload.incomplete'),
          value: 0,
          itemStyle: {
            color: 'rgba(217, 217, 217, 1)'
          }
        },
        {
          name: t('functionAnalysis.detail.workload.completed'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        }
      ]
    }
  ]
};

const savingWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEchartConfig,
  title: {
    ...completedWorkloadEchartConfig.title,
    subtext: t('functionAnalysis.detail.workload.savingWorkloadPercentage')
  }
}));

onMounted(() => {
  completedWorkloadEchart = eCharts.init(completedWorkloadRef.value);

  savingWorkloadEchart = eCharts.init(savingWorkloadRef.value);

  workloadChart = eCharts.init(workloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value], () => {
    workloadEchartConfig.series[0].data = props.chart0Value.yData;

    completedWorkloadEchartConfig.series[0].data[0] = {
      ...completedWorkloadEchartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    completedWorkloadEchartConfig.series[0].data[1] = {
      ...completedWorkloadEchartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    completedWorkloadEchartConfig.title.text = props.chart1Value.title;

    savingWorkloadEchartConfig.series[0].data[0] = {
      ...savingWorkloadEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    savingWorkloadEchartConfig.series[0].data[1] = {
      ...savingWorkloadEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    savingWorkloadEchartConfig.title.text = props.chart2Value.title;
    completedWorkloadEchart.setOption(completedWorkloadEchartConfig);
    savingWorkloadEchart.setOption(savingWorkloadEchartConfig);
    workloadChart.setOption(workloadEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedWorkloadEchart.resize();
    savingWorkloadEchart.resize();
    workloadChart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="workloadRef" class="flex-1 h-30"></div>
    <div ref="completedWorkloadRef" class="flex-1 h-30"></div>
    <div ref="savingWorkloadRef" class="flex-1 h-30"></div>
  </div>
</template>
