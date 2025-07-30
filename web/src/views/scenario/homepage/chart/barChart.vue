<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { GridComponent, GridComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { BarChart, BarSeriesOption } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';

import { ResourceInfo } from './PropsType';

type Props = {
  dataSource: ResourceInfo;
  resizeNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined,
  resizeNotify: undefined
});

type EChartsOption = echarts.ComposeOption<TooltipComponentOption | GridComponentOption | BarSeriesOption>;

const domId = utils.uuid('bar');
const total = ref<number>(0);

let echartInstance:echarts.ECharts;
const echartOption :EChartsOption = {
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '0',
    right: '0',
    top: '35px',
    bottom: '0',
    containLabel: true
  },
  xAxis: [{
    type: 'category',
    data: [],
    axisTick: { alignWithLabel: true }
  }],
  yAxis: [{ type: 'value' }],
  series: [{
    name: '数量',
    type: 'bar',
    barWidth: '20px',
    data: [],
    itemStyle: {
      color: '#2D8EFF',
      borderRadius: [4, 4, 0, 0]
    },
    label: {
      show: true, // 显示数值
      position: 'top' // 数值显示在柱形的顶部
    }
  }]
};

const renderChart = () => {
  if (!echartInstance) {
    echarts.use([TooltipComponent, GridComponent, BarChart, CanvasRenderer]);
    echartInstance = echarts.init(document.getElementById(domId));
    echartInstance.setOption(echartOption);
    return;
  }

  // 重新绘制图表
  echartInstance.setOption(echartOption);
};

const resizeHandler = () => {
  if (echartInstance) {
    echartInstance.resize();
  }
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    // 重置数据
    echartOption.xAxis![0].data = [];
    echartOption.series![0].data = [];

    if (props.dataSource) {
      total.value = +newValue.allSce;
      const _data = newValue.sceByPluginName;
      if (_data) {
        const keys = Object.keys(_data);
        if (keys.length) {
          keys.forEach(key => {
            echartOption.xAxis![0].data.push(key);
            echartOption.series![0].data.push(_data[key]);
          });
        } else {
          echartOption.xAxis![0].data = ['Http', 'WebSocket', 'Jdbc', 'Tcp'];
          echartOption.series![0].data = [0, 0, 0, 0];
        }
      }
    }

    renderChart();
  }, { immediate: true });

  watch(() => props.resizeNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    resizeHandler();
  }, { immediate: true });
});
</script>

<template>
  <div class="rounded border border-solid border-theme-text-box px-4 py-3.5">
    <div class="font-semibold">
      <span class="mr-2">总场景</span>
      <span class="text-4">{{ total }}</span>
    </div>
    <div :id="domId" class="w-full h-50"></div>
  </div>
</template>
