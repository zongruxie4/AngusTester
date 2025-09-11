<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  chart0Data: {
    xData: string[],
    yData: [string|number[], string|number[]]
  };
  chart1Data: {
    xData: string[],
    yData: [string|number[], string|number[]]
  };
}
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  chart0Data: () => ({
    xData: [],
    yData: [[], []]
  }),
  chart1Data: () => ({
    xData: [],
    yData: [[], []]
  })
});

const burndownRef = ref();
const workloadBurndownRef = ref();

let burndownEChart;
let workloadBurndownEChart;

const burndownEChartConfig = {
  grid: {
    left: '30',
    right: '20',
    bottom: '60',
    top: 20
  },
  legend: {
    show: true,
    bottom: 0,
    type: 'plain'
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
      name: t('taskAnalysis.detail.burndown.chartLabels.remaining'),
      data: [],
      type: 'line'
    },
    {
      name: t('taskAnalysis.detail.burndown.chartLabels.expected'),
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

const workloadProgressEChartConfig = JSON.parse(JSON.stringify({
  ...burndownEChartConfig
}));

onMounted(() => {
  burndownEChart = eCharts.init(burndownRef.value);

  workloadBurndownEChart = eCharts.init(workloadBurndownRef.value);

  watch([() => props.chart0Data, () => props.chart1Data], () => {
    burndownEChartConfig.series[0].data = props.chart0Data.yData[0];
    burndownEChartConfig.series[1].data = props.chart0Data.yData[1];
    burndownEChartConfig.xAxis.data = props.chart0Data.xData;

    workloadProgressEChartConfig.series[0].data = props.chart1Data.yData[0];
    workloadProgressEChartConfig.series[1].data = props.chart1Data.yData[1];
    workloadProgressEChartConfig.xAxis.data = props.chart1Data.xData;

    burndownEChart.setOption(burndownEChartConfig);
    workloadBurndownEChart.setOption(burndownEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    burndownEChart.resize();
    workloadBurndownEChart.resize();
  }
});
</script>
<template>
  <div class="flex">
    <div class="flex-1">
      <div ref="burndownRef" class="flex-1 h-50"></div>
      <div class="text-center font-medium  mt-1">
        {{ t('taskAnalysis.detail.burndown.chartTitles.taskBurndown') }}
      </div>
    </div>
    <div class="flex-1">
      <div ref="workloadBurndownRef" class="flex-1 h-50"></div>
      <div class="text-center font-medium mt-1">
        {{ t('taskAnalysis.detail.burndown.chartTitles.workloadBurndown') }}
      </div>
    </div>
  </div>
</template>
