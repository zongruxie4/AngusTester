<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts/core';
import { GridComponent, LegendComponent, TitleComponent, ToolboxComponent, TooltipComponent } from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { chartSeriesColorConfig } from '@/views/report/preview/PropsType';

type Props = {
  series: { name: string; data: string[] }[];
  xAxis: { data: string[] };
  gridTop: number;
}

const props = withDefaults(defineProps<Props>(), {
  series: () => [],
  xAxis: () => ({ data: [] }),
  gridTop: 90
});

echarts.use([
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  LineChart,
  CanvasRenderer,
  UniversalTransition
]);

const chartRef = ref();
let chartInstance: echarts.ECharts;

const tooltipFormatter = (data:{[key:string]:string}[]) => {
  if (!data?.length) {
    return '';
  }

  const name = data[0].name;
  let str = '<div style="font-size:12px;line-height:20px"><div>' + name + '</div>';
  for (let i = 0, len = data.length; i < len; i++) {
    const item = data[i];
    const value = item.seriesName.endsWith('错误率') ? (item.value + '%') : item.value;
    str += '<div style="display:flex;align-items:center;justify-content:space-between">' + '<div>' + item.marker + '<span>' + item.seriesName + '</span></div>' + '<div style="margin-left:20px;">' + value + '</div>' + '</div>';
  }
  return str + '</div>';
};

const chartOption: {
  tooltip: {
    trigger: 'axis';
    formatter?: (data: { [key: string]: string }[]) => string;
  };
  legend: {
    itemHeight: number;
    itemWidth: number;
    itemGap: number;
    padding: [number, number, number, number];
    data: string[];
  };
  grid: {
    left: number;
    right: number;
    top: number;
    bottom: number;
    containLabel: true;
  };
  xAxis: {
    type: 'category';
    boundaryGap: false;
    data: string[];
  };
  yAxis: {
    type: 'value';
    splitLine: {
      show: true;
      lineStyle: {
        type: 'dashed';
      }
    }
  };
  series: {
    type: 'line';
    smooth: true;
    showSymbol: false;
    name: string;
    data: string[];
  }[];
} = {
  tooltip: {
    trigger: 'axis',
    formatter: tooltipFormatter
  },
  legend: {
    itemHeight: 10,
    itemWidth: 8,
    itemGap: 8,
    padding: [5, 10, 0, 5],
    data: []
  },
  grid: {
    left: 45,
    right: 62,
    top: props.gridTop,
    bottom: 0,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: []
  },
  yAxis: {
    type: 'value',
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed'
      }
    }
  },
  series: []
};

const initChart = () => {
  chartInstance = echarts.init(chartRef.value);
  setOption();
};

const setOption = () => {
  chartInstance.setOption(chartOption);
};

onMounted(() => {
  initChart();

  watch([() => props.series, () => props.xAxis], ([series, xAxis]) => {
    chartOption.series = series?.map((item, idx) => {
      return {
        type: 'line',
        smooth: true,
        showSymbol: false,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },
            { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }
          ])
        },
        areaStyle: {},
        ...item
      };
    });
    chartOption.xAxis.data = xAxis?.data || [];
    chartOption.legend.data = series?.map(item => item.name);

    if (props.gridTop) {
      chartOption.grid.top = props.gridTop;
    }

    setOption();
  }, { immediate: true });
});

</script>
<template>
  <div ref="chartRef" class="h-90"></div>
</template>
