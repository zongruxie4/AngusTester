<script lang="ts" setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
  title0: string;
  title1: string;
  value0: {name: string, value: string|number}[];
  value1: {name: string, value: string|number}[];
}
const props = withDefaults(defineProps<Props>(), {
  value0: () => ([{ name: '', value: 0 }, { name: '', value: 0 }]),
  value1: () => ([{ name: '', value: 0 }, { name: '', value: 0 }]),
  title0: '',
  title1: ''
});

const progressRef = ref();
const workloadProcessRef = ref();

let progressEChart;
let workloadProcessEChart;

const progressEChartConfig = {
  title: {
    text: t('testAnalysis.detail.progress.chartTitles.caseProgress'),
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
          return '{a|' + (props.title0 || '0%') + '}';
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

const workloadProgressEChartConfig = JSON.parse(JSON.stringify({
  ...progressEChartConfig,
  title: {
    ...progressEChartConfig.title,
    text: t('testAnalysis.detail.progress.chartTitles.workloadProgress')
  },
  series: [
    {
      ...progressEChartConfig.series[0],
      label: {
        ...progressEChartConfig.series[0].label,
        formatter: function () {
          return '{a|' + (props.title1 || '0%') + '}';
        }
      }
    }
  ]
}));

onMounted(() => {
  progressEChart = eCharts.init(progressRef.value);

  workloadProcessEChart = eCharts.init(workloadProcessRef.value);

  const handleResize = () => {
    progressEChart?.resize();
    workloadProcessEChart?.resize();
  };

  window.addEventListener('resize', handleResize);

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });

  watch([() => props.value0, () => props.value1], () => {
    progressEChartConfig.series[0].data[0] = {
      ...progressEChartConfig.series[0].data[0],
      ...props.value0[0],
      value: Number(props.value0[0].value)
    };
    progressEChartConfig.series[0].data[1] = {
      ...progressEChartConfig.series[0].data[1],
      ...props.value0[1],
      value: Number(props.value0[1].value)
    };
    // Title is now static, rate value is shown in center

    workloadProgressEChartConfig.series[0].data[0] = {
      ...workloadProgressEChartConfig.series[0].data[0],
      ...props.value1[0],
      value: Number(props.value1[0].value)
    };
    workloadProgressEChartConfig.series[0].data[1] = {
      ...workloadProgressEChartConfig.series[0].data[1],
      ...props.value1[1],
      value: Number(props.value1[1].value)
    };
    // Update the center label formatter for the second chart
    workloadProgressEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.title1 || '0%') + '}';
    };
    // Title is now static, rate value is shown in center
    progressEChart.setOption(progressEChartConfig);
    workloadProcessEChart.setOption(workloadProgressEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    progressEChart?.resize();
    workloadProcessEChart?.resize();
  }
});
</script>
<template>
  <div class="flex chart-container">
    <div ref="progressRef" class="flex-1 h-30"></div>
    <div ref="workloadProcessRef" class="flex-1 h-30"></div>
  </div>
</template>
<style scoped>
.chart-container {
  padding: 20px;
}
</style>
