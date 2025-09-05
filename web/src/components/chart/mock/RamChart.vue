<script setup lang="ts">
import { inject, onBeforeMount, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
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

const { t } = useI18n();
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
    text: t('commonComp.chart.ram.title'),
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
    data: [t('commonComp.chart.ram.legend.maxAvailableMemory'), t('commonComp.chart.ram.legend.usedMemory'), t('commonComp.chart.ram.legend.committedMemory')],
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
      name: t('commonComp.chart.ram.series.maxAvailableMemory'),
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
      name: t('commonComp.chart.ram.series.usedMemory'),
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
      name: t('commonComp.chart.ram.series.committedMemory'),
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
