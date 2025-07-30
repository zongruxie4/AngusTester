<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

import { ResourceInfo } from '../../PropsType';

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
    itemGap: 5,
    formatter: function (name) {
      const data = echartOption?.series?.[0].data;
      for (let i = 0; i < data.length; i++) {
        if (data[i].name === name) {
          name += ' ' + data[i].value;
          break;
        }
      }

      return name;
    }
  },
  color: ['rgba(82,196,26,70%)', 'rgba(45,142,255,70%)', 'rgba(255,185,37,70%)'],
  series: [
    {
      name: '',
      type: 'pie',
      center: ['35%', '50%'],
      radius: ['65%', '90%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
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

const resizeHandler = () => {
  echartInstance.resize();
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (props.dataSource === undefined) {
      return;
    }

    // 重置数据
    echartOption.series![0].data = [];
    echartOption.series?.[0].data.push({ name: '功能', value: +newValue.functionalNum });
    echartOption.series?.[0].data.push({ name: '性能', value: +newValue.perfNum });
    echartOption.series?.[0].data.push({ name: '稳定性', value: +newValue.stabilityNum });

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
  <div class="relative leading-5 text-3">
    <div
      :id="domId"
      ref="containerRef"
      class="h-34"></div>
    <div class="mark-container">
      <div class="text-center">测试类型</div>
      <div class="text-3.5 text-center font-semibold">{{ props.dataSource?.totalTestTypeNum }}</div>
    </div>
  </div>
</template>

<style scoped>
.mark-container {
  position: absolute;
  top: 50%;
  left: 35%;
  transform: translate(-50%, -50%);
}
</style>
