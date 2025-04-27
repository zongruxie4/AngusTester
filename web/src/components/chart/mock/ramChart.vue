<script setup lang="ts">
import { inject, onBeforeMount, onMounted, Ref, ref, watch } from 'vue';
import * as echarts from 'echarts/core';

import {
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  MarkAreaComponent,
  TitleComponent,
  TooltipComponent
} from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

import DarkTheme from '@/assets/echartsDark.json';
import GrayTheme from '@/assets/echartsGray.json';

interface Props {
  title:string;
  unit:string;
  xData:string[];
  submitData:number[];
  totalData:number[];
  usedData:number[];
}
const props = withDefaults(defineProps<Props>(), {
  title: '',
  unit: '',
  xData: () => [],
  submitData: () => [],
  totalData: () => [],
  usedData: () => []
});

echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  MarkAreaComponent,
  LineChart,
  CanvasRenderer,
  UniversalTransition
]);

const tenantInfo:Ref = inject('tenantInfo', ref());
const chartsRef = ref();
let myChart: echarts.ECharts;

const initCharts = () => {
  if (!chartsRef.value) {
    return;
  }
  echarts.registerTheme(tenantInfo.value.preference.themeCode, tenantInfo.value.preference.themeCode === 'dark' ? DarkTheme : GrayTheme);
  myChart = echarts.init(chartsRef.value, tenantInfo.value.preference.themeCode, { renderer: 'canvas' });
  myChart.setOption(chartsOption);
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

const chartsOption = {
  title: {
    text: '内存(JVM、单位GB)',
    left: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'normal'
    }
  },
  grid: {
    top: 32,
    right: 60,
    bottom: 80,
    left: 60
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      animation: false,
      label: {
        backgroundColor: '#505765'
      }
    },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    data: ['最大可用内存', '已使用内存', '已提交内存'],
    left: 'center',
    bottom: '20',
    itemStyle: {
      color: 'inherit'
    },
    textStyle: {
      color: '#333' // 设置图例项的颜色
    }
  },
  dataZoom: [
    {
      type: 'inside',
      realtime: true,
      start: 0,
      end: 100
    }
  ],
  xAxis: [
    {
      type: 'category',
      boundaryGap: false,
      axisLine: { onZero: false },
      // prettier-ignore
      data: props.xData
    }
  ],
  yAxis: [
    {
      // name: '内区大小(GB)',
      type: 'value',
      nameTextStyle: {
        padding: [5, 0, 0, 50]
      }
    }
  ],
  series: [
    {
      name: '最大可用内存',
      type: 'line',
      areaStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      lineStyle: {
        width: 1
      },
      emphasis: {
        focus: 'series'
      },
      markArea: {
        silent: true,
        itemStyle: {
          opacity: 0.3
        },
        data: [
        ]
      },
      showSymbol: false,
      // prettier-ignore
      data: props.totalData
    },
    {
      name: '已使用内存',
      type: 'line',
      areaStyle: {

      },
      lineStyle: {
        width: 1
      },
      emphasis: {
        focus: 'series'
      },
      markArea: {
        silent: true,
        itemStyle: {
          opacity: 0.3
        },
        data: [
        ]
      },
      showSymbol: false,
      // prettier-ignore
      data: props.usedData
    },
    {
      name: '已提交内存',
      type: 'line',
      areaStyle: {
        color: 'rgba(171,211,255,1)'
      },
      lineStyle: {
        width: 1
      },
      emphasis: {
        focus: 'series'
      },
      markArea: {
        silent: true,
        itemStyle: {
          opacity: 0.3
        },
        data: [
        ]
      },
      showSymbol: false,
      // prettier-ignore
      data: props.usedData
    }

  ]
};

// Enable data zoom when user click bar.

watch(() => props.xData, () => {
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.series[0].data = props.totalData;
  chartsOption.series[1].data = props.usedData;
  chartsOption.series[2].data = props.submitData;
  myChart?.setOption(chartsOption, true);
  myChart.setOption({
    touchAction: 'none'
  });
}, { deep: true });

onBeforeMount(() => {
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
    class="h-full">
  </div>
</template>
