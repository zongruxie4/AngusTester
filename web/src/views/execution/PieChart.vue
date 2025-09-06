<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
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

interface Props {
  color:string[],
  type:string,
  title:string,
  total:number,
  dataSource:{name:string, value:number}[],
}
const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  title: '',
  type: '',
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
    textStyle: { color: '#444C5A', fontSize: '12px', fontWeight: 'normal' },
    left: 38,
    bottom: 0
  },
  tooltip: {
    trigger: 'item',
    confine: false,
    textStyle: {
      fontSize: 12
    }
  },
  series: [
    {
      center: [65, '50%'],
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
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
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
  if (!chartsOption.series) {
    return;
  }
  chartsOption.series[0].data = newValue;
  chartsOption.legend = props.type === 'script_type'
    ? [{
        orient: 'vertical',
        right: 10,
        top: 42,
        itemHeight: 12,
        itemWidth: 12,
        itemGap: 5,
        data: props.dataSource
      }]
    : [{
        orient: 'vertical',
        right: 80,
        top: 51,
        itemHeight: 12,
        itemWidth: 12,
        itemGap: 5,
        data: props.dataSource.slice(0, 4)
      }, {
        orient: 'vertical',
        right: 10,
        top: 51,
        itemHeight: 12,
        itemWidth: 12,
        itemGap: 5,
        data: props.dataSource.slice(4, 7)
      }];
  myChart?.setOption(chartsOption, true);
}, { deep: true, immediate: true });

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
  <div class="relative w-full h-full leading-5">
    <div ref="chartsRef" class="h-full"></div>
    <div class="mark-container text-center">
      <div class="text-3 text-center">{{ props.title }}</div>
      <div class="mt-1 text-3.5 text-theme-title">{{ props.total }}</div>
    </div>
  </div>
</template>
<style scoped>
.mark-container {
  position: absolute;
  top: 50%;
  left: 65px;
  transform: translate(-50%, -20px);
}
</style>
