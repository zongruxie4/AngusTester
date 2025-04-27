<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import * as echarts from 'echarts/core';
import { TitleComponent, TitleComponentOption, TooltipComponent, TooltipComponentOption, LegendComponent, LegendComponentOption } from 'echarts/components';
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

type EChartsOption = echarts.ComposeOption<TitleComponentOption | TooltipComponentOption | LegendComponentOption | PieSeriesOption>
echarts.use([TitleComponent, TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);

const chartsRef = ref();
let myChart: echarts.ECharts;
const initCharts = () => {
  if (!chartsRef.value) {
    return;
  }
  myChart = echarts.init(chartsRef.value, undefined, { renderer: 'canvas' });
  myChart.setOption(chartsOption);
  resizeChart();
};

const resizeChart = () => {
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

const chartsOption:EChartsOption = {
  color: props.dataSource.map(item => item.color),
  title: {
    text: 'Total',
    subtext: props.dataSource.reduce((total, item) => total + item.value, 0),
    textStyle: { color: '#525A65', fontSize: '13px', fontWeight: 'normal' },
    subtextStyle: { color: '#8C8C8C', fontSize: '14px', fontWeight: 'bold' },
    left: 'center',
    top: 60
  },
  tooltip: {
    trigger: 'item',
    confine: false,
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    orient: 'vertical',
    left: 'center',
    bottom: 0,
    itemHeight: 10,
    itemWidth: 20,
    data: props.dataSource,
    formatter: function (name) {
      const item = props.dataSource.find(item => item.name === name);
      const value = item ? item.value : 0;
      return `{a|${name}} {b|${value}}`;
    },
    textStyle: {
      rich: {
        a: {
          width: 80,
          color: '#525A65',
          fontSize: 12
        },
        b: {
          color: '#8C8C8C',
          fontSize: 12,
          fontWeight: 'bold'
        }
      }
    }

  },
  series: [
    {
      top: 0,
      center: ['50%', 80],
      type: 'pie',
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
  chartsOption.series[0].data = newValue;
  myChart?.setOption(chartsOption, true);
}, { immediate: true, deep: true });

onMounted(() => {
  initCharts();
  const erd = elementResizeDetector({ strategy: 'scroll' });
  erd.listenTo(document.body, debounce(800, () => {
    myChart.resize();
  }));
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {
    myChart.resize();
  });
});

defineExpose({
  resizeChart
});
</script>
<template>
  <div ref="chartsRef" class="h-full w-full"></div>
</template>
