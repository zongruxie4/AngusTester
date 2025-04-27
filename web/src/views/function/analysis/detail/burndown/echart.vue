<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
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
      name: '剩余',
      data: [],
      type: 'line'
    },
    {
      name: '期望',
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
      <div class="text-center font-medium  mt-1">用例燃尽数</div>
    </div>
    <div class="flex-1">
      <div ref="workloadBurndownRef" class="flex-1 h-50"></div>
      <div class="text-center font-medium mt-1">工作量燃尽</div>
    </div>
  </div>
</template>
