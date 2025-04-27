<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/tools';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import elementResizeDetector from 'element-resize-detector';

import { ResourceInfo } from '../../PropsType';

type Props = {
  dataSource: ResourceInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});
const erd = elementResizeDetector({ strategy: 'scroll' });
const wrapperRef = ref();
type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

// const windowResizeNotify = inject('windowResizeNotify', ref<string>());

const containerRef = ref<HTMLElement>();

const domId = utils.uuid('pie');

let echartInstance: echarts.ECharts;
const echartOption: EChartsOption = {
  tooltip: {
    trigger: 'item',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    top: 'middle',
    right: 0,
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 10,
    formatter: function (name) {
      return name;
    }
  },
  color: ['#F5222D', '#FF8100', '#FFB925', '#52C41A', '#67D7FF'],
  series: [
    {
      name: '',
      type: 'pie',
      center: ['50%', '50%'],
      radius: ['60%', '80%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{c}'
      },
      labelLine: {
        show: true
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 12,
          fontWeight: 'normal'
        }
      },
      data: []
    }
  ]
};

const renderChart = () => {
  if (!echartInstance) {
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
    echartInstance = echarts.init(document.getElementById(domId));
    echartInstance.setOption(echartOption);
    return;
  }

  // 重新绘制图表
  echartInstance.setOption(echartOption);
};

const setEchartOption = () => {
  if (containerRef.value) {
    const width = containerRef.value.offsetWidth;
    if (width > 418) {
      echartOption.series![0].center = ['50%', '50%'];
      echartOption.series![0].radius = ['50%', '70%'];
      echartOption.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else if (width > 340 && width <= 418) {
      echartOption.series![0].center = ['45%', '50%'];
      echartOption.series![0].radius = ['50%', '70%'];
      echartOption.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else if (width > 290 && width <= 340) {
      echartOption.series![0].center = ['40%', '50%'];
      echartOption.series![0].radius = ['50%', '70%'];
      echartOption.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else {
      echartOption.series![0].center = ['50%', '45%'];
      echartOption.series![0].radius = ['38%', '55%'];
      echartOption.legend = {
        bottom: '0',
        left: 'center',
        orient: 'horizontal',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 3,
        formatter: function (name) {
          return name;
        }
      };
    }
  }

  return echartOption;
};

const resizeHandler = () => {
  setEchartOption();
  echartInstance.setOption(echartOption);
  echartInstance.resize();
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (props.dataSource === undefined) {
      return;
    }

    // 重置数据
    echartOption.series![0].data = [];

    const data = newValue.caseByPriority;
    echartOption.series?.[0].data.push({ name: '最高', value: +data.HIGHEST });
    echartOption.series?.[0].data.push({ name: '高', value: +data.HIGH });
    echartOption.series?.[0].data.push({ name: '中', value: +data.MEDIUM });
    echartOption.series?.[0].data.push({ name: '低', value: +data.LOW });
    echartOption.series?.[0].data.push({ name: '最低', value: +data.LOWEST });

    setEchartOption();
    renderChart();
  }, { immediate: true });

  erd.listenTo(wrapperRef.value, resizeHandler);

  // watch(() => windowResizeNotify.value, (newValue) => {
  //   if (newValue === undefined || newValue === null || newValue === '') {
  //     return;
  //   }

  //   resizeHandler();
  // }, { immediate: true });
});

onBeforeUnmount(() => {
  erd.removeListener(wrapperRef.value, resizeHandler);
});
</script>

<template>
  <div ref="wrapperRef" class="flex flex-col rounded border border-solid border-theme-text-box pt-3.5">
    <div class="font-semibold px-4">优先级</div>
    <div class="flex-1 flex items-center justify-center pr-2">
      <div
        :id="domId"
        ref="containerRef"
        class="w-full h-40"></div>
    </div>
  </div>
</template>
