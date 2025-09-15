<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts/core';
import { useI18n } from 'vue-i18n';

import { GridComponent, TooltipComponent } from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
const { t } = useI18n();

export interface Props {
  unit:string;
  xData:string[];
  yData:number[];
  maxThreads: number;
  maxThreadsTime: number;
}
const props = withDefaults(defineProps<Props>(), {
  unit: '',
  xData: () => [],
  yData: () => [],
  maxYData: 0,
  maxThreads: 0,
  maxThreadsTime: 0
});

echarts.use([GridComponent, TooltipComponent, LineChart, CanvasRenderer, UniversalTransition]);

const chartsRef = ref();
let myChart: echarts.ECharts;

const initCharts = () => {
  if (!chartsRef.value) {
    return;
  }
  myChart = echarts.init(chartsRef.value);
  myChart.setOption(chartsOption);
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

const chartsOption = {

  grid: {
    top: 20,
    right: 40,
    bottom: 60,
    left: 70
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'line',
      snap: true,
      lineStyle: {
        cap: 'round',
        color: '#999',
        width: 1,
        type: 'dashed'
      }
    },
    textStyle: {
      fontSize: 12
    },
    formatter: (params) => {
      const data = params[0];
      const color = data.color;
      const value = data.value;
      return `<div class="flex items-center text-3 text-text-centent"><div class="h-3 w-3 rounded-full mr-3.5" style="background-color:${color}"></div><span class="mr-3.5">${t('xcan_execSettingForm.executionConcurrency')}</span>${value}</div>`;
    }
  },
  xAxis: {
    name: t('xcan_execSettingForm.executionTimeUnit', { unit: props.unit }),
    nameLocation: 'middle',
    nameGap: 40,
    type: 'category',
    boundaryGap: false,
    axisLabel: {
      showMaxLabel: true // 设置为true来显示最大值
    },
    data: props.xData,
    axisTick: {
      show: false // 隐藏X轴刻度线
    }
  },
  yAxis: [
    {
      show: true,
      name: t('xcan_execSettingForm.concurrencyAxis'),
      nameLocation: 'middle',
      nameGap: 50,
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed' // 设置为虚线
        }
      },
      type: 'value'
    }
  ],
  series: [
    {
      name: t('xcan_execSettingForm.concurrencyAxis'),
      type: 'line',
      showSymbol: false,
      symbol: '',
      step: 'end',
      data: props.yData,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: 'rgba(0, 119, 255, 0.3)' // 渐变起始颜色
          }, {
            offset: 1,
            color: 'rgba(0, 119, 255, 0.10)' // 渐变结束颜色
          }]
        }
      }
    }
  ]
};

// Enable data zoom when user click bar.

watch(() => props.xData, () => {
  chartsOption.xAxis.data = props.xData;
  chartsOption.series[0].data = props.yData;
  myChart?.setOption(chartsOption, true);
}, { deep: true });

watch(() => props.unit, (newValue) => {
  chartsOption.xAxis.name = t('xcan_execSettingForm.executionTimeUnit', { unit: newValue });
  myChart?.setOption(chartsOption, true);
});

// watch(() => props.maxYData, (newValue) => {
//   if (newValue) {
//     chartsOption.yAxis[0].max = newValue;
//     myChart?.setOption(chartsOption, true);
//   }
// });

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {
    myChart.resize();
  });
});

onMounted(() => {
  initCharts();
});

</script>
<template>
  <div
    ref="chartsRef"
    class="h-50 w-full"
    style="max-width:500px">
  </div>
</template>
