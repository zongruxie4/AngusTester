<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData: number[],

  },
  chart1Value: {
    value: {name: string, value: number[]}[],
    xData: string[]
  },
}
const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData: [0, 0, 0, 0, 0, 0]
  }),
  chart1Value: () => ({
    value: [],
    xData: []
  })
});

const unplannedTaskRef = ref();
const unplannedWorkloadRef = ref();

let unplannedTaskRefEChart;
let unplannedWorkloadRefEChart;

// 用例数
const unplannedTaskEChartConfig = {
  title: {
    text: t('testAnalysis.detail.resourceCreation.chartTitles.resourceTotal'),
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
      t('common.plan'),
      t('common.useCase'),
      t('common.review'),
      t('common.baseline'),
      t('common.analysis'),
      t('common.total')
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
    top: 0
  },
  series: [
    {
      name: '',
      itemStyle: {
        color: 'rgb(68,93,179)',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0, 0, 0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '30',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

const unplannedWorkloadEChartConfig = {
  title: {
    text: t('testAnalysis.detail.resourceCreation.chartTitles.resourceGrowth'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontStyle: 12
    }
  },
  grid: {
    left: '5%',
    right: '100',
    bottom: '60',
    top: 20
  },
  legend: {
    show: true,
    right: 0,
    type: 'plain',
    orient: 'vertical'
  },
  tooltip: {
    show: true,
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    min: function (value) {
      if (value.max < 1) {
        return 10;
      } else {
        return undefined;
      }
    }
  },
  series: [
    {
      name: 'api',
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

onMounted(() => {
  unplannedTaskRefEChart = eCharts.init(unplannedTaskRef.value);

  unplannedWorkloadRefEChart = eCharts.init(unplannedWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    unplannedTaskEChartConfig.series[0].data = props.chart0Value.yData;
    unplannedWorkloadEChartConfig.series = props.chart1Value.value.map(i => {
      return {
        ...i,
        stack: 'Total'
      };
    });
    unplannedWorkloadEChartConfig.xAxis.data = props.chart1Value.xData;

    unplannedTaskRefEChart.setOption(unplannedTaskEChartConfig);
    unplannedWorkloadRefEChart.setOption(unplannedWorkloadEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    unplannedTaskRefEChart.resize();
    unplannedWorkloadRefEChart.resize();
  }
});

</script>
<template>
  <div class="flex chart-container">
    <div ref="unplannedTaskRef" class="flex-1 h-40"></div>
    <div ref="unplannedWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
<style scoped>
.chart-container {
  padding: 20px;
}
</style>
