<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

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

let burndownEchart;
let workloadBurndownEchart;

const burndownEchartConfig = {
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
      name: t('chart.burndown.remaining'),
      data: [],
      type: 'line'
    },
    {
      name: t('chart.burndown.expected'),
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

const workloadProgressEchartConfig = JSON.parse(JSON.stringify({
  ...burndownEchartConfig
}));

onMounted(() => {
  burndownEchart = eCharts.init(burndownRef.value);

  workloadBurndownEchart = eCharts.init(workloadBurndownRef.value);

  watch([() => props.chart0Data, () => props.chart1Data], () => {
    burndownEchartConfig.series[0].data = props.chart0Data.yData[0];
    burndownEchartConfig.series[1].data = props.chart0Data.yData[1];
    burndownEchartConfig.xAxis.data = props.chart0Data.xData;

    workloadProgressEchartConfig.series[0].data = props.chart1Data.yData[0];
    workloadProgressEchartConfig.series[1].data = props.chart1Data.yData[1];
    workloadProgressEchartConfig.xAxis.data = props.chart1Data.xData;

    burndownEchart.setOption(burndownEchartConfig);
    workloadBurndownEchart.setOption(burndownEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    burndownEchart.resize();
    workloadBurndownEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div class="flex-1">
      <div ref="burndownRef" class="flex-1 h-50"></div>
      <div class="text-center font-medium  mt-1">
        {{ t('functionAnalysis.detail.burndown.caseBurndownCount') }}
      </div>
    </div>
    <div class="flex-1">
      <div ref="workloadBurndownRef" class="flex-1 h-50"></div>
      <div class="text-center font-medium mt-1">
        {{ t('functionAnalysis.detail.burndown.workloadBurndown') }}
      </div>
    </div>
  </div>
</template>
