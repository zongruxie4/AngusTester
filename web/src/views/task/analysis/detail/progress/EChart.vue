<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  title0: string;
  title1: string;
  value0: {name: string, value: string|number}[];
  value1: {name: string, value: string|number}[];
}
const { t } = useI18n();

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
    text: '0%',
    left: '35%',
    top: '38%',
    padding: 2,
    subtext: t('taskAnalysis.detail.progress.chartTitles.taskProgress'),
    itemGap: 45,
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
          name: t('taskAnalysis.detail.progress.chartLabels.uncompleted'),
          value: 0,
          itemStyle: {
            color: 'rgba(217, 217, 217, 1)'
          }
        },
        {
          name: t('taskAnalysis.detail.progress.chartLabels.completed'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
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
    subtext: t('taskAnalysis.detail.progress.chartTitles.workloadProgress')
  }
}));

onMounted(() => {
  progressEChart = eCharts.init(progressRef.value);
  workloadProcessEChart = eCharts.init(workloadProcessRef.value);

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
    progressEChartConfig.title.text = props.title0;

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
    workloadProgressEChartConfig.title.text = props.title1;
    progressEChart.setOption(progressEChartConfig);
    workloadProcessEChart.setOption(workloadProgressEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    progressEChart.resize();
    workloadProcessEChart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="progressRef" class="flex-1 h-30"></div>
    <div ref="workloadProcessRef" class="flex-1 h-30"></div>
  </div>
</template>
