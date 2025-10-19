
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import * as echarts from 'echarts/core';
import { 
  TitleComponent, 
  TitleComponentOption, 
  TooltipComponent, 
  TooltipComponentOption 
} from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

/**
 * Chart data point interface
 */
interface ChartDataPoint {
  name: string;   // Data point label
  value: number;  // Data point value
}

/**
 * Component props interface
 */
interface Props {
  type?: string;                // Chart type (currently unused)
  title?: string;               // Chart title (currently unused)
  total?: number;               // Total count (currently unused)
  dataSource: ChartDataPoint[]; // Array of data points to display
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
  PieSeriesOption
>;

// Register ECharts components
echarts.use([
  TitleComponent, 
  TooltipComponent, 
  PieChart, 
  CanvasRenderer, 
  LabelLayout
]);

/**
 * Reference to the chart DOM element
 */
const chartsRef = ref<HTMLDivElement>();

/**
 * ECharts instance
 */
let myChart: echarts.ECharts;

/**
 * Initialize the ECharts instance
 * Creates a canvas-based chart and applies the configuration
 */
const initCharts = (): void => {
  if (!chartsRef.value) {
    return;
  }
  
  myChart = echarts.init(chartsRef.value, undefined, { renderer: 'canvas' });
  myChart.setOption(chartsOption);
};

/**
 * ECharts configuration options
 * Defines a donut chart with centered total count
 */
const chartsOption: EChartsOption = {
  // Title showing total count in center of donut
  title: {
    text: String(props.dataSource.reduce((total, item) => total + item.value, 0)),
    textStyle: { 
      color: '#525A65', 
      fontSize: '13px', 
      fontWeight: 'normal' 
    },
    left: 'center',
    top: 'center'
  },
  
  // Tooltip configuration
  tooltip: {
    trigger: 'item',       // Trigger on data item hover
    confine: false,        // Allow tooltip to overflow container
    textStyle: {
      fontSize: 12
    }
  },
  
  // Pie chart series configuration
  series: [
    {
      top: 0,
      center: ['50%', '50%'],
      type: 'pie',
      radius: [40, 50],          // Donut shape: inner radius 40, outer radius 50
      avoidLabelOverlap: false,  // Don't adjust label positions
      
      // Hide default labels
      label: {
        show: false,
        position: 'center',
        color: '#444C5A'
      },
      
      // Hover emphasis effect
      emphasis: {
        scale: true,             // Enable scaling on hover
        scaleSize: 3,            // Scale size in pixels
        label: {
          show: false,           // Keep label hidden on hover
          fontSize: 12,
          fontWeight: 'bold'
        }
      },
      
      // Hide label connecting lines
      labelLine: {
        show: false
      },
      
      // Chart data
      data: props.dataSource
    }
  ]
};

/**
 * Watch for data source changes
 * Updates chart data when props.dataSource changes
 */
watch(() => props.dataSource, (newValue) => {
  if (chartsOption.series?.[0]) {
    chartsOption.series[0].data = newValue;
    // Update chart with new data, merge with existing options
    myChart?.setOption(chartsOption, true);
  }
}, { 
  immediate: true  // Execute immediately on mount
});

/**
 * Component lifecycle hook
 * Initializes chart and sets up resize listener
 */
onMounted(() => {
  // Initialize the chart
  initCharts();
  
  // Set up automatic resize on container dimension changes
  const erd = elementResizeDetector({ strategy: 'scroll' });
  erd.listenTo(document.body, debounce(800, () => {
    // Debounced resize to avoid excessive redraws
    myChart.resize();
  }));
});
</script>

<template>
  <div ref="chartsRef" class="h-60 w-75"></div>
</template>
