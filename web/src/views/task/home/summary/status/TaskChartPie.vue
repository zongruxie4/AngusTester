<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

import { ResourceInfo } from '@/views/task/home/types';

type Props = {
  dataSource: ResourceInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

const windowResizeNotify = inject('windowResizeNotify', ref<string>());

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
    itemGap: 10
  },
  color: ['#67D7FF', '#FFB925', '#F5222D', '#2acab8', '#2D8EFF', '#52C41A'],
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
    if (width > 428) {
      echartOption.series![0].center = ['50%', '50%'];
      echartOption.series![0].radius = ['45%', '63%'];
      echartOption.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else if (width > 350 && width <= 428) {
      echartOption.series![0].center = ['45%', '50%'];
      echartOption.series![0].radius = ['45%', '63%'];
      echartOption.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else if (width > 300 && width <= 350) {
      echartOption.series![0].center = ['40%', '50%'];
      echartOption.series![0].radius = ['45%', '63%'];
      echartOption.legend = {
        top: 'middle',
        right: 0,
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 10
      };
    } else {
      echartOption.series![0].center = ['50%', '40%'];
      echartOption.series![0].radius = ['38%', '55%'];
      echartOption.legend = {
        bottom: '-7px',
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
  if (echartInstance) {
    echartInstance.setOption(echartOption);
    echartInstance.resize();
  }
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (props.dataSource === undefined) {
      return;
    }

    // 重置数据
    echartOption.series![0].data = [];

    const data = newValue.taskByType;
    echartOption.series?.[0].data.push({ name: '故事', value: +data.STORY });
    echartOption.series?.[0].data.push({ name: '任务', value: +data.TASK });
    echartOption.series?.[0].data.push({ name: '缺陷', value: +data.BUG });
    echartOption.series?.[0].data.push({ name: '需求', value: +data.REQUIREMENT });
    echartOption.series?.[0].data.push({ name: '接口测试', value: +data.API_TEST });
    echartOption.series?.[0].data.push({ name: '场景测试', value: +data.SCENARIO_TEST });

    setEchartOption();
    renderChart();
  }, { immediate: true });

  watch(() => windowResizeNotify.value, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    resizeHandler();
  }, { immediate: true });
});
</script>

<template>
  <div
    :id="domId"
    ref="containerRef"
    class="h-44"></div>
</template>
