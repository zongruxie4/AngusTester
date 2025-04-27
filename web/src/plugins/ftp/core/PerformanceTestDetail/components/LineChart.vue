<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts/core';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/tools';
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
  chartsOption.series = newValue.map(item => {
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
  series: props.series.map(item => ({
    ...item,
    type: 'line',
    lineStyle: {
      width: 1
    },
    barGap: '5%',
    smooth: false,
    showSymbol: props.xData?.length === 1,
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
