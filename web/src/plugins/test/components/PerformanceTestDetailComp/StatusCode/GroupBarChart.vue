
<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, computed } from 'vue';
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
 * Series data interface
 */
interface SeriesData {
  name: string;    // Series name (status code category)
  data: number[];  // Data points (counts per API)
}

/**
 * Component props interface
 */
interface Props {
  colors: string[];      // Colors for each status code category
  xData: string[];       // X-axis labels (API names)
  yData: SeriesData[];   // Series data (status code categories)
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
const chartsRef = ref<HTMLDivElement>();

/**
 * ECharts instance
 */
let myChart: echarts.ECharts;

/**
 * Label configuration for bar chart
 * Displays series name vertically inside bars
 */
const labelOption = {
  show: true,
  position: 'insideBottom',  // Position at bottom inside bar
  distance: 5,                // Distance from position
  align: 'left',
  verticalAlign: 'middle',
  rotate: 90,                 // Rotate 90 degrees for vertical text
  formatter: '{a}',           // Show series name
  fontSize: 14,
  rich: {
    name: {}
  }
};

/**
 * Initialize ECharts instance
 * Creates the chart and applies initial configuration
 */
const initCharts = (): void => {
  if (!chartsRef.value) {
    return;
  }
  
  myChart = echarts.init(chartsRef.value);
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
 * Defines a grouped bar chart with custom colors and zoom support
 */
const chartsOption: any = {
  // Custom colors for each status code category
  color: props.colors,
  
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
    trigger: 'axis',      // Trigger on axis (show all series at x position)
    confine: true,        // Confine tooltip within chart area
    axisPointer: {
      type: 'shadow'      // Show shadow highlighting on hover
    }
  },
  
  // Legend showing API names
  legend: {
    data: props.xData
  },

  // Enable inside zoom (scroll/pinch to zoom)
  dataZoom: {
    type: 'inside'
  },

  // X-axis configuration (categorical)
  xAxis: [
    {
      type: 'category',
      axisTick: { show: false },
      data: props.xData  // API names
    }
  ],
  
  // Y-axis configuration (numeric)
  yAxis: {
    type: 'value',
    splitLine: {
      show: false,
      lineStyle: {
        type: 'dashed'
      }
    }
  },

  // Bar series for each status code category
  series: props.yData.map(item => ({
    ...item,
    type: 'bar',
    barWidth: 24,        // Fixed bar width in pixels
    showBackground: true, // Show background for empty bars
    barCategoryGap: 1,   // Gap between categories
    barGap: 0.1,         // Gap between bars in same category
    label: labelOption,  // Apply label configuration
    emphasis: {
      focus: 'series'    // Highlight entire series on hover
    }
  }))
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
 * Updates chart when props change, rebuilding series with new data
 */
watch(() => props.yData, (newValue) => {
  // Update colors and axis data
  chartsOption.color = props.colors;
  chartsOption.xAxis[0].data = props.xData;
  chartsOption.legend.data = props.xData as never[];
  
  // Rebuild series with new data
  chartsOption.series = newValue.map(item => ({
    ...item,
    type: 'bar',
    barCategoryGap: 1,
    barWidth: 24,
    barGap: 0.1,
    label: labelOption,
    showBackground: true,
    emphasis: {
      focus: 'series'
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
 * Computed chart width based on data size
 * Calculates width to accommodate all bars with proper spacing
 * 
 * @returns Width string (px or percentage)
 */
const width = computed(() => {
  if (props.xData.length) {
    // Calculate width: APIs * (categories * bar width) + margin
    const _width = (props.xData.length * (props.yData.length * 28)) + 300;
    return _width + 'px';
  }
  return '100%';
});

/**
 * Expose methods for parent component access
 */
defineExpose({
  resizeChart
});
</script>

<template>

  <div
    ref="chartsRef"
    class="h-full min-w-full"
    :style="{ width: width }"></div>
</template>
