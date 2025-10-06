<script lang="ts" setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  chart0Value: {
    yData: number[]
  };
  chart1Value: {
    title: string;
    value: { name: string, value: string | number }[];
  }
  chart2Value: {
    title: string;
    value: { name: string, value: string | number }[];
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
    text: t('common.workload'),
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
      t('issueAnalysis.detail.workload.eval'),
      t('issueAnalysis.detail.workload.actual'),
      t('issueAnalysis.detail.workload.completed'),
      t('issueAnalysis.detail.workload.saving')
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
        color: 'rgb(68,93,179)',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '30',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

const completedWorkloadEChartConfig = {
  title: {
    text: t('common.counts.completedWorkloadRate'),
    left: '40%',
    bottom: '5%',
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: '600',
      color: '#595959'
    }
  },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    borderColor: 'transparent',
    textStyle: {
      color: '#fff',
      fontSize: 12
    },
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    top: 'center',
    orient: 'vertical',
    right: '30%',
    itemGap: 12,
    textStyle: {
      fontSize: 12,
      color: '#595959'
    }
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['50%', '78%'],
      center: ['40%', '38%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        position: 'center',
        formatter: function () {
          return '{a|' + (props.chart1Value?.title || '0%') + '}';
        },
        rich: {
          a: {
            fontSize: 16,
            fontWeight: 'bold',
            color: '#262626'
          }
        }
      },
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      emphasis: {
        scale: true,
        scaleSize: 5,
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      data: [
        {
          name: t('status.notCompleted'),
          value: 0,
          itemStyle: {
            color: '#ff7875'
          }
        },
        {
          name: t('status.completed'),
          value: 0,
          itemStyle: {
            color: '#52c41a'
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
    text: t('common.counts.savingWorkloadRate')
  },
  series: [
    {
      ...completedWorkloadEChartConfig.series[0],
      label: {
        ...completedWorkloadEChartConfig.series[0].label,
        formatter: function () {
          return '{a|' + (props.chart2Value?.title || '0%') + '}';
        }
      }
    }
  ]
}));

onMounted(() => {
  completedWorkloadEChart = eCharts.init(completedWorkloadRef.value);
  savingWorkloadEChart = eCharts.init(savingWorkloadRef.value);
  workloadChart = eCharts.init(workloadRef.value);

  const handleResize = () => {
    completedWorkloadEChart?.resize();
    savingWorkloadEChart?.resize();
    workloadChart?.resize();
  };

  window.addEventListener('resize', handleResize);

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });

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
    // Title is now static, rate value is shown in center

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
    // Update the center label formatter for the second chart
    savingWorkloadEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.chart2Value?.title || '0%') + '}';
    };
    // Title is now static, rate value is shown in center
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
    completedWorkloadEChart?.resize();
    savingWorkloadEChart?.resize();
    workloadChart?.resize();
  }
});

</script>
<template>
  <div class="flex chart-container">
    <div ref="workloadRef" class="flex-1 h-35"></div>
    <div ref="completedWorkloadRef" class="flex-1 h-35"></div>
    <div ref="savingWorkloadRef" class="flex-1 h-35"></div>
  </div>
</template>
<style scoped>
.chart-container {
  padding: 20px;
}
</style>
