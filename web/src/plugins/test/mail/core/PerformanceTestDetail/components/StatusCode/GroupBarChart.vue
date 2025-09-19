<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, computed } from 'vue';
import * as echarts from 'echarts/core';

import { GridComponent, TooltipComponent, LegendComponent, DataZoomComponent } from 'echarts/components';
import { BarChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

interface Props {
  colors:string[];
  xData:string[];
  yData:{name:string, data:number[]}[];
}
const props = withDefaults(defineProps<Props>(), {
  colors: () => [],
  xData: () => [],
  yData: () => []
});

echarts.use([GridComponent, LegendComponent, TooltipComponent, BarChart, CanvasRenderer, UniversalTransition, DataZoomComponent]);

const chartsRef = ref();
let myChart: echarts.ECharts;

const labelOption = {
  show: true,
  position: 'insideBottom',
  distance: 5,
  align: 'left',
  verticalAlign: 'middle',
  rotate: 90,
  formatter: '{a}',
  fontSize: 14,
  rich: {
    name: {}
  }
};

const initCharts = () => {
  if (!chartsRef.value) {
    return;
  }
  myChart = echarts.init(chartsRef.value);
  myChart.setOption(chartsOption);
  resizeChart();
};

const resizeChart = () => {
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

const chartsOption = {
  color: props.colors,
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
  yAxis: {
    type: 'value',
    splitLine: {
      show: false,
      lineStyle: {
        type: 'dashed'
      }
    }
  },

  series: props.yData.map(item => ({
    ...item,
    type: 'bar',
    barWidth: 24,
    showBackground: true,
    barCategoryGap: 1,
    barGap: 0.1,
    label: labelOption,
    emphasis: {
      focus: 'series'
    }
  }))
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {
    myChart.resize();
  });
});

watch(() => props.yData, (newValue) => {
  chartsOption.color = props.colors;
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.legend.data = props.xData as never[];
  chartsOption.series = newValue.map(item => ({
    ...item,
    type: 'bar',
    barCategoryGap: 1,
    barWidth: 24,
    barGap: 0.1,
    label: labelOption,
    showBackground: true,
    emphasis: {
      focus: 'series'
    }
  }));
  myChart?.setOption(chartsOption, true);
}, { immediate: true, deep: true });

onMounted(() => {
  initCharts();
});

const width = computed(() => {
  if (props.xData.length) {
    const _width = (props.xData.length * (props.yData.length * 28)) + 300;
    return _width + 'px';
  }
  return '100%';
});

defineExpose({
  resizeChart
});
</script>
<template>
  <div
    ref="chartsRef"
    class="h-full min-w-full"
    :style="{width:width}"></div>
</template>
