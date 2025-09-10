<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
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

const { t } = useI18n();

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
    show: true,
    text: props.title,
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
  legend: [{
    orient: 'vertical',
    left: 135,
    top: 'middle',
    itemHeight: 12,
    itemWidth: 12
  }],
  series: [
    {
      center: [66, '50%'],
      type: 'pie',
      selectedOffset: 5,
      left: 0,
      top: 0,
      radius: [35, 50],
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
  <div class="relative w-full h-full">
    <div ref="chartsRef" class="h-full"></div>
    <div class="mark-container leading-5 text-center">
      <div class="text-3 text-center text-theme-title font-medium">{{ t('functionCase.statisticsPanel.total') }}</div>
      <div class="mt-1 text-4.5 leading-4.5 text-theme-title">{{ props.total }}</div>
    </div>
  </div>
</template>
<style scoped>
.mark-container {
  position: absolute;
  top: 50%;
  left: 66px;
  transform: translate(-50%, -20px);
}
</style>
