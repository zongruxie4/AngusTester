<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import * as echarts from 'echarts/core';
import { TitleComponent, TitleComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

interface Props {
  type:string,
  title:string,
  total:number,
  dataSource:{name:string, value:number}[],
}
const props = withDefaults(defineProps<Props>(), {
  title: '',
  type: '',
  total: 0,
  dataSource: () => []
});

type EChartsOption = echarts.ComposeOption<TitleComponentOption | TooltipComponentOption | PieSeriesOption>
echarts.use([TitleComponent, TooltipComponent, PieChart, CanvasRenderer, LabelLayout]);

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
  title: {
    text: props.dataSource.reduce((total, item) => total + item.value, 0),
    textStyle: { color: '#525A65', fontSize: '13px', fontWeight: 'normal' },
    left: 'center',
    top: 'center'
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
      top: 0,
      center: ['50%', '50%'],
      type: 'pie',
      radius: [40, 50],
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
}, { immediate: true });

onMounted(() => {
  initCharts();
  const erd = elementResizeDetector({ strategy: 'scroll' });
  erd.listenTo(document.body, debounce(800, () => {
    myChart.resize();
  }));
});

</script>
<template>
  <div ref="chartsRef" class="h-60 w-75"></div>
</template>
