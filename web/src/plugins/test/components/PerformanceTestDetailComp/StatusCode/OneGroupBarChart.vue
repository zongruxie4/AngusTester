
<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts/core';

import { 
  GridComponent, 
  TooltipComponent, 
  LegendComponent, 
  DataZoomComponent 
} from 'echarts/components';
import { BarChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

/**
 * Chart data item interface
 */
interface ChartDataItem {
  name: string;      // Status code category name
  value: number[];   // Value array (for compatibility)
  color: string;     // Bar color
}

/**
 * Component props interface
 */
interface Props {
  colors: string[];         // Colors array (currently unused)
  xData: string[];          // X-axis labels (status code categories)
  yData: ChartDataItem[];   // Bar data with values and colors
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  colors: () => [],
  xData: () => [],
  yData: () => []
});

// Register ECharts components
echarts.use([
  GridComponent, 
  LegendComponent, 
  TooltipComponent, 
  BarChart, 
  CanvasRenderer, 
  UniversalTransition, 
  DataZoomComponent
]);

/**
 * Reference to chart DOM element
 */
const chartRef = ref<HTMLDivElement>();

/**
 * ECharts instance
 */
let myChart: echarts.ECharts;

/**
 * Initialize ECharts instance
 * Creates the chart and applies initial configuration
 */
const initCharts = (): void => {
  if (!chartRef.value) {
    return;
  }
  
  myChart = echarts.init(chartRef.value);
  myChart.setOption(chartsOption);
  resizeChart();
};

/**
 * Handle window resize event
 * Listens to window resize and adjusts chart dimensions
 */
const resizeChart = (): void => {
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

/**
 * ECharts configuration options
 * Defines a single bar chart with custom colors per bar
 */
const chartsOption: any = {
  // Chart grid layout
  grid: {
    top: 20,
    right: 0,
    bottom: 0,
    left: 'left',
    containLabel: true  // Ensure labels fit within grid
  },
  
  // Tooltip configuration
  tooltip: {
    trigger: 'axis',      // Trigger on axis position
    confine: true,        // Confine tooltip within chart area
    axisPointer: {
      type: 'shadow'      // Show shadow highlighting on hover
    }
  },
  
  // Legend showing status code categories
  legend: {
    data: props.xData
  },
  
  // Enable inside zoom (scroll/pinch to zoom)
  dataZoom: {
    type: 'inside'
  },
  
  // X-axis configuration (categorical - status codes)
  xAxis: [
    {
      type: 'category',
      axisTick: { show: false },
      data: props.xData  // Status code categories
    }
  ],
  
  // Y-axis configuration (numeric - counts)
  yAxis: [
    {
      type: 'value',
      splitLine: {
        show: false,
        lineStyle: {
          type: 'dashed'
        }
      }
    }
  ],
  
  // Single bar series (data populated by watcher)
  series: [{
    data: [],
    type: 'bar',
    barWidth: 24,        // Fixed bar width in pixels
    showBackground: true, // Show background for empty bars
    barCategoryGap: 1    // Gap between categories
  }]
};

/**
 * Before unmount hook
 * Cleans up resize event listener to prevent memory leaks
 */
onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {
    myChart.resize();
  });
});

/**
 * Watch for data changes
 * Updates chart when yData changes, applying custom colors to each bar
 */
watch(() => props.yData, (newValue) => {
  // Update x-axis and legend data
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.legend.data = props.xData as never[];
  
  // Map data to bar items with custom colors
  chartsOption.series[0].data = newValue.map(item => ({
    value: item.value,
    itemStyle: {
      color: item.color  // Apply individual color to each bar
    }
  }));
  
  // Update chart with new options (merge mode)
  myChart?.setOption(chartsOption, true);
}, { 
  immediate: true,  // Execute immediately on mount
  deep: true        // Watch nested properties
});

/**
 * Component mount hook
 * Initializes the chart on mount
 */
onMounted(() => {
  initCharts();
});

/**
 * Expose methods for parent component access
 */
defineExpose({
  resizeChart
});
</script>

<template>
  <div ref="chartRef" class="h-full w-full"></div>
</template>
