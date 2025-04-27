<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts/core';
import { DatasetComponent, GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { BarChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';

type Props = {
  statusCodeData: {
    [key: string]: {
      [key: string]: string;
    }
  }
}

const props = withDefaults(defineProps<Props>(), {
  statusCodeData: undefined
});

echarts.use([
  DatasetComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  CanvasRenderer
]);

const chartRef = ref();
let chartInstance: echarts.ECharts;
const chartOption: {
  tooltip: {
    trigger: 'axis';
    axisPointer: { type: 'shadow' };
    textStyle: {
      fontSize: 12
    }
  };
  grid: {
    left: string;
    right: string;
    top: string;
    bottom: string;
    containLabel: true;
  };
  dataset: {
    dimensions: string[];
    source: { [key: string]: string; }[]
  };
  xAxis: { type: 'category' };
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
    type: 'bar';
    barWidth: '20px';
    itemStyle: {
      borderRadius: [4, 4, 0, 0];
    };
    label: {
      show: true;
      position: 'insideBottom';
      distance: 5;
      align: 'left';
      verticalAlign: 'middle';
      rotate: 90;
      fontSize: 12;
      formatter: (params) => string;
    }
  }[];
} = {
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '0',
    right: '0',
    top: '15px',
    bottom: '0',
    containLabel: true
  },
  dataset: {
    dimensions: [],
    source: []
  },
  xAxis: { type: 'category' },
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

  watch(() => props.statusCodeData, (newValue) => {
    if (!newValue) {
      return;
    }

    const dimensions:Set<string> = new Set();
    dimensions.add('statusCode');

    const source :{
      [key:string]:string;
      statusCode:string;
    }[] = [];
    const series:{
    type: 'bar';
    barWidth: '20px';
    itemStyle: {
      borderRadius: [4, 4, 0, 0];
    };
    label: {
      show: true;
      position: 'insideBottom';
      distance: 5;
      align: 'left';
      verticalAlign: 'middle';
      rotate: 90;
      fontSize: 12;
      formatter: (params) => string;
    }
  }[] = [];
    for (const key in newValue) {
      const _keys = Object.keys(newValue[key]);
      _keys.forEach(item => {
        dimensions.add(item);
      });

      source.push({
        statusCode: key,
        ...newValue[key]
      });
    }

    const vages = Array.from(dimensions);
    for (let i = 1; i < vages.length; i++) {
      series.push({
        type: 'bar',
        barWidth: '20px',
        itemStyle: {
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'insideBottom',
          distance: 5,
          align: 'left',
          verticalAlign: 'middle',
          rotate: 90,
          fontSize: 12,
          formatter: (params) => {
            return params.seriesName + '     ' + params.value[params.seriesName]; // 显示系列名和数值
          }
        }
      });
    }

    chartOption.series = series;
    chartOption.dataset.source = source;
    chartOption.dataset.dimensions = Array.from(dimensions);
    setOption();
  }, { immediate: true });
});
</script>
<template>
  <div ref="chartRef" class="h-70"></div>
</template>
