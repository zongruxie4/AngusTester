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
  })
});

const completedWorkloadRef = ref();
const savingWorkloadRef = ref();
const workloadRef = ref();

let workloadChart;
let completedWorkloadEChart;
let savingWorkloadEChart;

const workloadEChartConfig = {
  title: {
    text: t('taskAnalysis.detail.workload.chartTitles.workload'),
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
      t('common.evalWorkload'),
      t('common.actualWorkload'),
      t('common.completedWorkload'),
      t('common.savingWorkload')
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

const completedWorkloadEChartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('taskAnalysis.detail.workload.chartTitles.completedWorkloadRatio'),
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
      // radius: ['45%', '60%'],
      radius: '60%',
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
          name: t('status.notCompleted'),
          value: 0,
          itemStyle: {
            color: 'rgb(246,159,42)'
          }
        },
        {
          name: t('status.completed'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        }
      ]
    }
  ]
};

const savingWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEChartConfig,
  title: {
    ...completedWorkloadEChartConfig.title,
    subtext: t('taskAnalysis.detail.workload.chartTitles.savingWorkloadRatio')
  }
}));

onMounted(() => {
  completedWorkloadEChart = eCharts.init(completedWorkloadRef.value);
  savingWorkloadEChart = eCharts.init(savingWorkloadRef.value);
  workloadChart = eCharts.init(workloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value], () => {
    workloadEChartConfig.series[0].data = props.chart0Value.yData;

    completedWorkloadEChartConfig.series[0].data[0] = {
      ...completedWorkloadEChartConfig.series[0].data[0],
      name: props.chart1Value.value[0].name,
      value: Number(props.chart1Value.value[0].value)
    };
    completedWorkloadEChartConfig.series[0].data[1] = {
      ...completedWorkloadEChartConfig.series[0].data[1],
      name: props.chart1Value.value[1].name,
      value: Number(props.chart1Value.value[1].value)
    };
    completedWorkloadEChartConfig.title.text = props.chart1Value.title;

    savingWorkloadEChartConfig.series[0].data[0] = {
      ...savingWorkloadEChartConfig.series[0].data[0],
      name: props.chart2Value.value[0].name,
      value: Number(props.chart2Value.value[0].value)
    };
    savingWorkloadEChartConfig.series[0].data[1] = {
      ...savingWorkloadEChartConfig.series[0].data[1],
      name: props.chart2Value.value[1].name,
      value: Number(props.chart2Value.value[1].value)
    };
    savingWorkloadEChartConfig.title.text = props.chart2Value.title;
    completedWorkloadEChart.setOption(completedWorkloadEChartConfig);
    savingWorkloadEChart.setOption(savingWorkloadEChartConfig);
    workloadChart.setOption(workloadEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedWorkloadEChart.resize();
    savingWorkloadEChart.resize();
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
