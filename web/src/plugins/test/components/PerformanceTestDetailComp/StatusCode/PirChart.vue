
<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import * as echarts from 'echarts/core';
import { 
  TitleComponent, 
  TitleComponentOption, 
  TooltipComponent, 
  TooltipComponentOption, 
  LegendComponent, 
  LegendComponentOption 
} from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

/**
 * Chart data point interface
 */
interface ChartDataPoint {
  name: string;    // Status code category name
  value: number;   // Count of responses
  color?: string;  // Optional custom color
}

/**
 * Component props interface
 */
interface Props {
  type?: string;                // Chart type (currently unused)
  title?: string;               // Chart title (currently unused)
  total?: number;               // Total count (currently unused)
  dataSource: ChartDataPoint[]; // Array of status code data points
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  title: '',
  type: '',
  total: 0,
  dataSource: () => []
});

/**
 * ECharts option type composition
 */
type EChartsOption = echarts.ComposeOption<
  TitleComponentOption | 
  TooltipComponentOption | 
  LegendComponentOption | 
  PieSeriesOption
>;

// Register ECharts components
echarts.use([
  TitleComponent, 
  TooltipComponent, 
  LegendComponent, 
  PieChart, 
  CanvasRenderer, 
  LabelLayout
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
 * Initialize ECharts instance
 * Creates a canvas-based chart and applies the configuration
 */
const initCharts = (): void => {
  if (!chartsRef.value) {
    return;
  }
  
  myChart = echarts.init(chartsRef.value, undefined, { renderer: 'canvas' });
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
 * Defines a donut pie chart with centered total count and custom legend
 */
const chartsOption: EChartsOption = {
  // Use custom colors from data source
  color: props.dataSource.map(item => item.color),
  
  // Title showing "Total" with count below
  title: {
    text: 'Total',
    subtext: String(props.dataSource.reduce((total, item) => total + item.value, 0)),
    textStyle: { 
      color: '#525A65', 
      fontSize: '13px', 
      fontWeight: 'normal' 
    },
    subtextStyle: { 
      color: '#8C8C8C', 
      fontSize: '14px', 
      fontWeight: 'bold' 
    },
    left: 'center',
    top: 60
  },
  
  // Tooltip configuration
  tooltip: {
    trigger: 'item',       // Trigger on data item hover
    confine: false,        // Allow tooltip to overflow container
    textStyle: {
      fontSize: 12
    }
  },
  
  // Custom legend with name and value display
  legend: {
    orient: 'vertical',    // Vertical layout
    left: 'center',
    bottom: 0,
    itemHeight: 10,
    itemWidth: 20,
    data: props.dataSource as any,
    
    // Custom formatter to show name and value
    formatter: function (name: string) {
      const item = props.dataSource.find(item => item.name === name);
      const value = item ? item.value : 0;
      return `{a|${name}} {b|${value}}`;
    },
    
    // Rich text styles for legend labels
    textStyle: {
      rich: {
        a: {
          width: 80,
          color: '#525A65',
          fontSize: 12
        },
        b: {
          color: '#8C8C8C',
          fontSize: 12,
          fontWeight: 'bold'
        }
      }
    }
  },
  
  // Pie series configuration
  series: [
    {
      top: 0,
      center: ['50%', 80],   // Center horizontally, 80px from top
      type: 'pie',
      radius: [45, 60],      // Donut shape: inner radius 45, outer radius 60
      avoidLabelOverlap: false,
      
      // Hide default labels (using legend instead)
      label: {
        show: false,
        position: 'center',
        color: '#444C5A'
      },
      
      // Hover emphasis effect
      emphasis: {
        scale: true,         // Enable scaling on hover
        scaleSize: 3,        // Scale size in pixels
        label: {
          show: false,       // Keep label hidden on hover
          fontSize: 12,
          fontWeight: 'bold'
        }
      },
      
      // Hide label connecting lines
      labelLine: {
        show: false
      },
      
      // Chart data
      data: props.dataSource as any
    }
  ]
};

/**
 * Watch for data source changes
 * Updates chart when dataSource prop changes
 */
watch(() => props.dataSource, (newValue) => {
  // Update series data with new values
  if (chartsOption.series?.[0]) {
    chartsOption.series[0].data = newValue as any;
  }
  
  // Update chart with new options (merge mode)
  myChart?.setOption(chartsOption, true);
}, { 
  immediate: true,  // Execute immediately on mount
  deep: true        // Watch nested properties
});

/**
 * Component mount hook
 * Initializes chart and sets up resize detector
 */
onMounted(() => {
  // Initialize the chart
  initCharts();
  
  // Set up element resize detector for responsive behavior
  const erd = elementResizeDetector({ strategy: 'scroll' });
  erd.listenTo(document.body, debounce(800, () => {
    // Debounced resize to avoid excessive redraws (800ms delay)
    myChart.resize();
  }));
});

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
 * Expose methods for parent component access
 */
defineExpose({
  resizeChart
});
</script>

<template>
  <div ref="chartsRef" class="h-full w-full"></div>
</template>
