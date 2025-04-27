<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts/core';

import { GridComponent, TooltipComponent, LegendComponent, DataZoomComponent } from 'echarts/components';
import { BarChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

interface Props {
  colors:string[];
  xData:string[];
  yData:{name:string, value:number[], color:string}[];
}
const props = withDefaults(defineProps<Props>(), {
  colors: () => [],
  xData: () => [],
  yData: () => []
});

echarts.use([GridComponent, LegendComponent, TooltipComponent, BarChart, CanvasRenderer, UniversalTransition, DataZoomComponent]);

const chartRef = ref();
let myChart: echarts.ECharts;

const initCharts = () => {
  if (!chartRef.value) {
    return;
  }
  myChart = echarts.init(chartRef.value);
  myChart.setOption(chartsOption);
  resizeChart();
};

const resizeChart = () => {
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

const chartsOption = {
  grid: {
    top: 20,
    right: 0,
    bottom: 0,
    left: 'left',
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    confine: true,
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: props.xData
  },
  dataZoom: {
    type: 'inside'
  },
  xAxis: [
    {

      type: 'category',
      axisTick: { show: false },
      data: props.xData
    }
  ],
  yAxis: [
    {
      type: 'value',
      splitLine: {
        show: false,
        lineStyle: {
          type: 'dashed'
        }
      }
    }
  ],
  series: [{
    data: [],
    type: 'bar',
    barWidth: 24,
    showBackground: true,
    barCategoryGap: 1
  }]
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {
    myChart.resize();
  });
});

watch(() => props.yData, (newValue) => {
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.legend.data = props.xData as never[];
  chartsOption.series[0].data = newValue.map(item => ({
    value: item.value,
    itemStyle: {
      color: item.color
    }
  }));
  myChart?.setOption(chartsOption, true);
}, { immediate: true, deep: true });

onMounted(() => {
  initCharts();
});

defineExpose({
  resizeChart
});
</script>
<template>
  <div ref="chartRef" class="h-full w-full"></div>
</template>
