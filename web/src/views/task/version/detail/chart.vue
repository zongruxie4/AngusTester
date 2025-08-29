<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import * as eCharts from 'echarts';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
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
  chart1Value: () => ({
    title: '--%',
    value: [{ value: 0 }, {}]
  }),
  chart2Value: () => ({
    title: '--%',
    value: [{ value: 0 }, {}]
  })
});

const completedRef = ref();
const completedWorkloadRef = ref();

let completedEchart;
let completedWorkloadEchart;

const completedEchartConfig = {
  title: {
    text: '0%',
    left: '35%',
    bottom: '10%',
    padding: 2,
    subtext: t('task.detail.chart.subtext'),
    // left: '25%',
    // top: '40%',
    itemGap: 50,
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
      radius: ['45%', '60%'],
      // radius: '60%',
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
          name: t('task.detail.chart.uncompleted'),
          value: 0,
          itemStyle: {
            color: 'rgba(217, 217, 217, 1)'
          }
        },
        {
          name: t('task.detail.chart.completed'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        }
      ]
    }
  ]
};

const completedWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...completedEchartConfig,
  title: {
    ...completedEchartConfig.title,
    subtext: t('task.detail.chart.workload')
  }
}));

onMounted(() => {
  completedEchart = eCharts.init(completedRef.value);
  completedWorkloadEchart = eCharts.init(completedWorkloadRef.value);

  watch([() => props.chart1Value, () => props.chart2Value], () => {
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
    completedEchart.setOption(completedEchartConfig);
    completedWorkloadEchart.setOption(completedWorkloadEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedEchart.resize();
    completedWorkloadEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="completedRef" class="flex-1 h-40"></div>
    <div ref="completedWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
