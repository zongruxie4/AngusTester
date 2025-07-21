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
  processCpuUsed:number[];
  totalData:number[];
  systemCpuUsed:number[];
}
const props = withDefaults(defineProps<Props>(), {
  title: '',
  unit: '',
  xData: () => [],
  processCpuUsed: () => [],
  totalData: () => [],
  systemCpuUsed: () => []
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
  myChart = echarts.init(chartsRef.value, tenantInfo.value.preference.themeCode, { renderer: 'canvas' });
  myChart.setOption(chartsOption);
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

const chartsOption = {
  title: {
    text: props.totalData.length ? `处理器 (${props.totalData[0]}核)` : '处理器',
    left: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'normal'
      // color: '#252525'
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
    },
    valueFormatter: (value) => value ? value + '%' : value
  },
  legend: {
    data: ['CPU数', '系统CPU使用率', '进程CPU使用率'],
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
      // name: 'CPU总数(核)',
      type: 'value',
      nameTextStyle: {
        padding: [5, 0, 0, 50]
      }
    }
  ],
  series: [
    {
      name: '系统CPU使用率',
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
      data: props.systemCpuUsed
    },
    {
      name: '进程CPU使用率',
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
      data: props.processCpuUsed
    }

  ]
};

// Enable data zoom when user click bar.

watch(() => props.xData, () => {
  chartsOption.title.text = props.totalData.length ? `处理器 (${props.totalData[0]}核)` : '处理器';
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.series[0].data = props.systemCpuUsed;
  chartsOption.series[1].data = props.processCpuUsed;
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
