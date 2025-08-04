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

interface Props {
  title:string;
  unit:string;
  xData:string[];
  numData:number[];
  totalData:number[];
  usedData:number[];
  maxNumData:number;
  maxTotalData:number;
}
const props = withDefaults(defineProps<Props>(), {
  title: '',
  unit: '',
  xData: () => [],
  numData: () => [],
  totalData: () => [],
  usedData: () => [],
  maxNumData: 0,
  maxTotalData: 0
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
  title: {
    text: '缓存区(JVM、单位GB)',
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
    data: ['总缓存区大小', '已使用缓存大小', '缓存区数'],
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
      // name: '缓存区大小(MB)',
      type: 'value',
      nameTextStyle: {
        padding: [5, 0, 0, 50]
      }
    },
    {
      // name: '缓存区数(个)',
      nameLocation: 'start',
      alignTicks: true,
      type: 'value',
      inverse: true,
      min: 0,
      nameTextStyle: {
        padding: [5, 30, 0, 5]
      }
    }
  ],
  series: [
    {
      name: '总缓存区大小',
      type: 'line',
      lineStyle: {
        width: 1
      },
      emphasis: {
        focus: 'series'
      },
      areaStyle: {
        color: 'rgba(82, 196, 26, 1)'
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
      name: '已使用缓存大小',
      type: 'line',
      areaStyle: {
        color: 'rgba(45,142,255,1)'
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
      name: '缓存区数',
      type: 'line',
      yAxisIndex: 1,
      areaStyle: {},
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
      data: props.numData
    }
  ]
};

// Enable data zoom when user click bar.

watch(() => props.xData, () => {
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.series[0].data = props.totalData;
  chartsOption.series[1].data = props.usedData;
  chartsOption.series[2].data = props.numData;
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
