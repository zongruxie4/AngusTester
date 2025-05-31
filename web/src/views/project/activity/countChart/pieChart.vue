<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import * as echarts from 'echarts/core';
import {
  LegendComponent,
  LegendComponentOption,
  TitleComponent,
  TitleComponentOption,
  TooltipComponent,
  TooltipComponentOption
} from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { duration } from '@xcan-angus/tools';

interface Props {
  color:string[],
  title:string,
  total:number,
  dataSource:{name:string, value:number}[],
}
const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  title: '',
  total: 0,
  dataSource: () => []
});

type EChartsOption = echarts.ComposeOption<TitleComponentOption | TooltipComponentOption | LegendComponentOption | PieSeriesOption>
echarts.use([TitleComponent, TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);

const erd = elementResizeDetector({ strategy: 'scroll' });

const chartsRef = ref();
let myChart: echarts.ECharts;
const initCharts = () => {
  if (!chartsRef.value) {
    return;
  }
  myChart = echarts.init(chartsRef.value, undefined, { renderer: 'canvas' });
  myChart.setOption(chartsOption);
};

const chartsOption:EChartsOption = {
  color: props.color,
  title: {
    show: false,
    text: props.title,
    textStyle: { color: '#444C5A', fontSize: '12px', fontWeight: 'normal' },
    left: 34,
    bottom: 0
  },
  tooltip: {
    trigger: 'item',
    confine: false,
    textStyle: {
      fontSize: 12
    }
  },

  legend: [
    {
      orient: 'vertical',
      left: 135,
      top: '25%',
      itemHeight: 12,
      itemWidth: 12,
      data: props.dataSource.slice(0, 5)
    },
    {
      orient: 'vertical',
      left: 240,
      top: '25%',
      itemHeight: 12,
      itemWidth: 12,
      data: props.dataSource.slice(5, 10)
    },
    {
      orient: 'vertical',
      left: 340,
      top: '25%',
      itemHeight: 12,
      itemWidth: 12,
      data: props.dataSource.slice(10, 15)
    },
    {
      orient: 'vertical',
      left: 440,
      top: '25%',
      itemHeight: 12,
      itemWidth: 12,
      data: props.dataSource.slice(15, 20)
    },
    {
      orient: 'vertical',
      left: 540,
      top: '25%',
      itemHeight: 12,
      itemWidth: 12,
      data: props.dataSource.slice(20, 25)
    },
    {
      orient: 'vertical',
      left: 640,
      top: '25%',
      itemHeight: 12,
      itemWidth: 12,
      data: props.dataSource.slice(25, 30)
    }
  ],
  series: [
    {
      center: [66, '50%'],
      type: 'pie',
      selectedOffset: 5,
      left: 0,
      top: 0,
      radius: [45, 60],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center',
        color: '#444C5A'
      },
      emphasis: {
        scale: true,
        scaleSize: 3,
        label: {
          show: false,
          fontSize: 12,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: props.dataSource
    }
  ]
};

watch(() => props.dataSource, (newValue) => {
  if (chartsOption.series?.[0]) {
    chartsOption.series[0].data = newValue;
    myChart?.setOption(chartsOption, true);
  }
}, { deep: true });

const resizeHandler = debounce(duration.resize, () => {
  myChart.resize();
});

onMounted(() => {
  initCharts();
  erd.listenTo(document.body, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(document.body, resizeHandler);
});
</script>
<template>
  <div class="relative h-50">
    <div ref="chartsRef" class="h-full"></div>
    <div class="mark-container leading-5">
      <div class="text-3 text-center text-theme-title font-medium">{{ props.title }}</div>
      <div class="mt-1 text-4.5 leading-4.5 text-theme-title text-center">{{ props.total }}</div>
    </div>
  </div>
</template>
<style scoped>
.mark-container {
  position: absolute;
  top: 45%;
  left: 66px;
  transform: translate(-50%, -20px);
}
</style>
