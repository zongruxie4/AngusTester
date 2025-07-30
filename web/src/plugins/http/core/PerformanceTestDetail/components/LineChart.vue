<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts/core';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import dayjs from 'dayjs';

import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import elementResizeDetector from 'element-resize-detector';

interface Props {
  series: { name: string, key: string; data: number[] }[];
  xData: string[];
  brpsUnit: 'KB' | 'MB';
  bwpsUnit: 'KB' | 'MB';
  writeBytesUnit: 'KB' | 'MB';
  selectedIndex?: string;
  tabKey?: string;
}
const props = withDefaults(defineProps<Props>(), {
  series: () => [],
  xData: () => [],
  selectedIndex: '',
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  writeBytesUnit: 'KB',
  tabKey: undefined
});

const chartSeriesColorConfig = {
  0: '84,112,198',
  1: '145,204,117',
  2: '250,200,88',
  3: '238,102,102',
  4: '115,192,222',
  5: '59,162,114',
  6: '252,132,82',
  7: '154,96,180',
  8: '234,124,204',
  9: '166, 206, 255',
  10: '162, 222, 236',
  11: '45, 142, 255',
  12: '127, 145, 255',
  13: '251, 129, 255',
  14: '255, 102, 0',
  15: '255, 165, 43',
  16: '118, 196, 125',
  17: '217, 217, 217',
  18: '245, 34, 45',
  19: '255, 185, 37',
  20: '201, 119, 255',
  21: '191, 199, 255',
  22: '103, 215, 255',
  23: '127, 91, 72',
  24: '57, 129, 184'
};

echarts.use([GridComponent, LegendComponent, TooltipComponent, LineChart, CanvasRenderer, UniversalTransition]);

const erd = elementResizeDetector({ strategy: 'scroll' });

const chartsRef = ref();
let myChart: echarts.ECharts;
const legendselect = ref();
const initCharts = () => {
  if (!chartsRef.value) {
    return;
  }
  myChart = echarts.init(chartsRef.value);
  myChart.setOption(chartsOption);
  myChart.on('legendselectchanged', function (obj: any) {
    legendselect.value = obj.selected;
  });
  nextTick(() => {
    if (chartsRef.value) {
      erd.listenTo(chartsRef.value, resizeHandler);
    }
  });
};

const resizeChart = () => {
  if (chartsRef.value) {
    resizeHandler();
  }
};

const setOption = (newValue) => {
  chartsOption.xAxis[0].data = props.xData.map((str) => {
    return dayjs(str).format('MM-DD HH:mm:ss');
  });
  const names: string[] = [];
  chartsOption.series = newValue.map((item, idx) => {
    names.push(item.name);
    if (isInit) {
      if (props.tabKey === 'error') {
        chartsOption.legend.selected[item.name] = item.key === 'errors' || item.key === 'errorRate';
      } else {
        chartsOption.legend.selected[item.name] = true;
      }
    }
    return {
      ...item,
      type: 'line',
      lineStyle: {
        width: 1
      },
      barGap: '5%',
      smooth: false,
      showSymbol: props.xData?.length === 1,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },
          { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }
        ])
      },
      areaStyle: {},
      yAxisIndex: ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'].includes(item.key) && props.tabKey === 'analyze' ? 1 : 0
    };
  });

  if (legendselect.value) {
    chartsOption.legend.selected = legendselect.value;
  }
  chartsOption.legend.data = names;
  myChart?.setOption(chartsOption, true);
};

const resizeHandler = debounce(duration.resize, function () {
  myChart.resize();
});

const chartsOption = {
  grid: {
    top: 30,
    right: 60,
    bottom: 24,
    left: 40,
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    confine: true,
    formatter: (params) => {
      let res = '';
      if (params.length) {
        res = `<div style="display:flex;align-items:center;justify-content: space-between;min-width:200px;font-weight:500;">
                    <span style='color:var(--content-text-sub-content);'>${params[0].name}</span>
               </div>`;
        for (let i = 0; i < params.length; i++) {
          res += `<div style="display:flex;align-items:center;justify-content: space-between;min-width:200px;max-width:400px;font-size:12px;">
                    <div style="display:flex;align-items:center;">
                      <div style="background-color:${params[i].color};width:6px;height:6px;margin-right:8px;border-radius:50%"></div>
                      <div style='color:var(--content-text-sub-content);max-width:260px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;'>${params[i].seriesName}</div>
                    </div>
                    <span style='color:var(--content-text-content);font-weight:500;'>
                      ${['brps'].includes(props.selectedIndex) || props.series[i]?.key?.includes('brps')
              ? params[i].value + props.brpsUnit
              : ['bwps'].includes(props.selectedIndex) || props.series[i]?.key?.includes('bwps')
                ? params[i].value + props.bwpsUnit
                : ['writeBytes'].includes(props.selectedIndex) || props.series[i]?.key?.includes('writeBytes')
                  ? params[i].value + props.writeBytesUnit
                  : ['errorRate'].includes(props.selectedIndex) || props.series[i]?.key?.includes('errorRate')
                    ? params[i].value + '%'
                    : params[i].value}
                    </span>
                  </div>`;
        }
      }
      return res;
    }
  },
  legend: {
    top: 0,
    type: 'scroll',
    padding: [0, 40],
    data: props.series.map(item => item.name),
    selected: {}
  },
  xAxis: [
    {
      type: 'category',
      data: props.xData,
      boundaryGap: false,
      axisTick: {
        show: false
      }
    }
  ],
  yAxis: [
    {
      type: 'value',
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed'
        }
      },
      grid: {
        left: 'left'
      }
    },
    {
      type: 'value',
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed'
        }
      },
      grid: {
        left: 'left'
      }
    }
  ],
  series: props.series.map((item, idx) => ({
    ...item,
    type: 'line',
    lineStyle: {
      width: 1
    },
    barGap: '5%',
    smooth: false,
    showSymbol: props.xData?.length === 1,
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },
        { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }
      ])
    },
    areaStyle: {},
    yAxisIndex: ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'].includes(item.key) && props.tabKey === 'analyze' ? 1 : 0
  }))
};

onBeforeUnmount(() => {
  if (chartsRef.value) {
    erd.removeListener(chartsRef.value, resizeHandler);
  }

  myChart.off('legendselectchanged');
});

let isInit = true;
watch(() => props.series, (newValue) => {
  setOption(newValue);
  isInit = false;
}, { immediate: true });

onMounted(() => {
  initCharts();
});

defineExpose({
  resizeChart
});
</script>
<template>
  <div
    ref="chartsRef"
    class="h-70"
    style="width: 100%;"></div>
</template>
