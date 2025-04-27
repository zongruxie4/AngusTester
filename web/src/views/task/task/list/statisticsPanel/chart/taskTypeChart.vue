<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/tools';
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
  color: ['#67D7FF', '#FFB925', '#F5222D', '#2acab8', '#2D8EFF', '#52C41A'],
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
    echartOption.series?.[0].data.push({ name: '故事', value: +newValue.storyNum });
    echartOption.series?.[0].data.push({ name: '任务', value: +newValue.taskNum });
    echartOption.series?.[0].data.push({ name: '缺陷', value: +newValue.bugNum });
    echartOption.series?.[0].data.push({ name: '需求', value: +newValue.requirementNum });
    echartOption.series?.[0].data.push({ name: '接口测试', value: +newValue.apiTestNum });
    echartOption.series?.[0].data.push({ name: '场景测试', value: +newValue.scenarioTestNum });

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
      <div class="text-center">任务类型</div>
      <div class="text-3.5 text-center font-semibold">{{ props.dataSource?.totalTaskTypeNum }}</div>
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
