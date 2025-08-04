<script setup lang="ts">
import {  onBeforeMount, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts/core';
import { NoData } from '@xcan-angus/vue-ui';
import { GridComponent, TitleComponent, TooltipComponent } from 'echarts/components';

import { LineChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { computed } from '@vue/reactivity';

interface Props {
  title:string
  unit:string
  xData:string[]
  yData: (number | null)[];
}
const props = withDefaults(defineProps<Props>(), {
  title: '',
  unit: '',
  xData: () => [],
  yData: () => []
});

echarts.use([
  TitleComponent,
  GridComponent,
  TooltipComponent,
  LineChart,
  CanvasRenderer
]);

const chartsRef = ref();
let myChart: echarts.ECharts;

const initCharts = () => {
  if (!chartsRef.value) {
    return;
  }
  myChart = echarts.init(chartsRef.value);
  myChart.setOption(chartsOption);
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

const noData = computed(() => {
  return props.yData.every(element => element === null);
});

const chartsOption = {
  grid: {
    top: 26,
    right: 15,
    bottom: 45,
    left: 40
  },
  title: {
    text: `${props.title} ( ${props.unit} , 总数: ${props.yData.length ? props.yData.reduce((n, m) => Number(n) + Number(m)) : ''})`,
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'normal'
      // color: '#252525'
    }
  },
  tooltip: {
    axisPointer: {
      type: 'shadow'
    }
  },
  xAxis: [
    {
      type: 'category',
      data: props.xData,
      axisTick: {
        show: false
      },
      axisLine: {
        show: true
      }
    }
  ],
  yAxis: [
    {
      type: 'value',
      max: props.yData.every(element => element === null) ? 100 : 'dataMax',
      axisLine: {
        show: true
      },
      splitLine: {
        show: false
      }
    }
  ],
  max: 50,
  series: [
    {
      type: 'line',
      barWidth: 15,
      label: {
        show: props.yData.length === 1 && props.yData[0] === 0 ? false : props.yData.length,
        position: 'top'
      },
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 1, color: 'rgba(45, 142, 255, 0.2)' },
          { offset: 0, color: 'rgba(45, 142, 255, 1)' }
        ])
      },
      areaStyle: {},
      smooth: true,
      data: props.yData
    }
  ]
};

// Enable data zoom when user click bar.
watch(() => props.xData, () => {
  chartsOption.title.text = `${props.title} ( ${props.unit} , 总数: ${props.yData.length ? props.yData.reduce((n, m) => Number(n) + Number(m)) : ''})`;
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.yAxis[0].max = props.yData.every(element => element === null) ? 100 : 'dataMax';
  chartsOption.series[0].data = props.yData;
  chartsOption.series[0].label.show = props.yData.length === 1 && props.yData[0] === 0 ? false : props.yData.length;
  myChart?.setOption(chartsOption, true);
}, { deep: true });

onBeforeMount(() => {
  window.removeEventListener('resize', () => {
    myChart.resize();
  });
});

onMounted(() => {
  initCharts();
});

</script>
<template>
  <div class="relative" style="height: 200px;">
    <div ref="chartsRef" class="w-full h-full flex-shrink-0">
    </div>
    <template v-if="noData">
      <NoData class="h-30 absolute w-30  my-no-data" />
    </template>
  </div>
</template>
<style scoped>
.my-no-data {
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
