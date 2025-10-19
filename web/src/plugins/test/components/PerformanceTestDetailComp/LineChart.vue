/**
 * LineChart Component

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts/core';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import dayjs from 'dayjs';

import { 
  GridComponent, 
  TooltipComponent, 
  LegendComponent 
} from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import elementResizeDetector from 'element-resize-detector';

/**
 * Data unit type for bandwidth metrics
 */
type BandwidthUnit = 'KB' | 'MB';

/**
 * Series data interface
 */
interface SeriesData {
  name: string;    // Series name for legend
  key: string;     // Metric key for special handling
  data: number[];  // Data points array
}

/**
 * Component props interface
 */
interface Props {
  series: SeriesData[];         // Array of data series to display
  xData: string[];              // X-axis timestamp data
  brpsUnit: BandwidthUnit;      // Bytes received per second unit
  bwpsUnit: BandwidthUnit;      // Bytes written per second unit
  writeBytesUnit: BandwidthUnit;// Write bytes unit
  selectedIndex?: string;       // Currently selected metric index
  tabKey?: string;              // Current tab identifier
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  series: () => [],
  xData: () => [],
  selectedIndex: '',
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  writeBytesUnit: 'KB',
  tabKey: undefined
});

/**
 * Color configuration for chart series (RGB values)
 * Each index maps to a specific color for consistent series coloring
 */
const chartSeriesColorConfig: Record<number, string> = {
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

// Register ECharts components
echarts.use([
  GridComponent, 
  LegendComponent, 
  TooltipComponent, 
  LineChart, 
  CanvasRenderer, 
  UniversalTransition
]);

/**
 * Element resize detector instance
 * Monitors chart container size changes
 */
const erd = elementResizeDetector({ strategy: 'scroll' });

/**
 * Chart state
 */
const chartsRef = ref<HTMLDivElement>();                    // Reference to chart DOM element
let myChart: echarts.ECharts;                               // ECharts instance
const legendselect = ref<Record<string, boolean>>();       // Legend selection state
/**
 * Initialize ECharts instance
 * Creates chart, applies configuration, sets up event listeners and resize detector
 */
const initCharts = (): void => {
  if (!chartsRef.value) {
    return;
  }
  
  myChart = echarts.init(chartsRef.value);
  myChart.setOption(chartsOption);
  
  // Listen to legend selection changes to preserve user preferences
  myChart.on('legendselectchanged', function (obj: any) {
    legendselect.value = obj.selected;
  });
  
  // Set up resize detector
  nextTick(() => {
    if (chartsRef.value) {
      erd.listenTo(chartsRef.value, resizeHandler);
    }
  });
};

/**
 * Resize chart to fit container
 * Public method exposed to parent component
 */
const resizeChart = (): void => {
  if (chartsRef.value) {
    resizeHandler();
  }
};

/**
 * Track if this is initial load (for default legend selection)
 */
let isInit = true;

/**
 * Update chart options with new series data
 * Formats timestamps, builds series with gradients, and preserves legend selection
 * 
 * @param newValue - New series data array
 */
const setOption = (newValue: SeriesData[]): void => {
  // Format x-axis timestamps
  chartsOption.xAxis[0].data = props.xData.map((str) => {
    return dayjs(str).format('MM-DD HH:mm:ss');
  });
  
  const names: string[] = [];
  
  // Build series with styling and Y-axis assignment
  chartsOption.series = newValue.map((item, idx) => {
    names.push(item.name);
    
    // Set default legend selection on initial load
    if (isInit) {
      if (props.tabKey === 'error') {
        // For error tab: only show error count and error rate by default
        chartsOption.legend.selected[item.name] = item.key === 'errors' || item.key === 'errorRate';
      } else {
        // For other tabs: show all series by default
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
      smooth: false,                               // Disable curve smoothing
      showSymbol: props.xData?.length === 1,       // Show symbols only for single data point
      
      // Gradient fill color
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },  // Bottom: 10% opacity
          { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }     // Top: full opacity
        ])
      },
      areaStyle: {},  // Enable area fill
      
      // Use secondary Y-axis for thread metrics in analyze tab
      yAxisIndex: ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'].includes(item.key) && props.tabKey === 'analyze' ? 1 : 0
    };
  });

  // Preserve user's legend selection state
  if (legendselect.value) {
    chartsOption.legend.selected = legendselect.value;
  }
  
  chartsOption.legend.data = names;
  
  // Update chart with new options (merge mode)
  myChart?.setOption(chartsOption, true);
};

/**
 * Debounced resize handler
 * Prevents excessive resize calls during window resize
 */
const resizeHandler = debounce(duration.resize, function () {
  myChart.resize();
});

/**
 * ECharts configuration object
 * Defines chart layout, styling, and interactions
 */
const chartsOption: any = {
  // Chart grid layout configuration
  grid: {
    top: 30,              // Top padding
    right: 60,            // Right padding
    bottom: 24,           // Bottom padding
    left: 40,             // Left padding
    containLabel: true    // Include axis labels in grid size calculation
  },
  
  // Tooltip configuration (hover popup)
  tooltip: {
    trigger: 'axis',      // Show tooltip for entire axis (all series at once)
    confine: true,        // Keep tooltip within chart container
    
    /**
     * Custom tooltip formatter
     * Displays timestamp header and all series values with appropriate units
     * 
     * @param params - Array of series data at hover point
     * @returns HTML string for tooltip content
     */
    formatter: (params: any) => {
      let res = '';
      if (params.length) {
        // Header: timestamp
        res = `<div style="display:flex;align-items:center;justify-content: space-between;min-width:200px;font-weight:500;">
                    <span style='color:var(--content-text-sub-content);'>${params[0].name}</span>
               </div>`;
        
        // Body: each series with color indicator, name, and value with unit
        for (let i = 0; i < params.length; i++) {
          // Determine appropriate unit based on metric type
          let valueWithUnit: string;
          if (['brps'].includes(props.selectedIndex) || props.series[i]?.key?.includes('brps')) {
            valueWithUnit = params[i].value + props.brpsUnit;
          } else if (['bwps'].includes(props.selectedIndex) || props.series[i]?.key?.includes('bwps')) {
            valueWithUnit = params[i].value + props.bwpsUnit;
          } else if (['writeBytes'].includes(props.selectedIndex) || props.series[i]?.key?.includes('writeBytes')) {
            valueWithUnit = params[i].value + props.writeBytesUnit;
          } else if (['errorRate'].includes(props.selectedIndex) || props.series[i]?.key?.includes('errorRate')) {
            valueWithUnit = params[i].value + '%';
          } else {
            valueWithUnit = params[i].value;
          }
          
          res += `<div style="display:flex;align-items:center;justify-content: space-between;min-width:200px;max-width:400px;font-size:12px;">
                    <div style="display:flex;align-items:center;">
                      <div style="background-color:${params[i].color};width:6px;height:6px;margin-right:8px;border-radius:50%"></div>
                      <div style='color:var(--content-text-sub-content);max-width:260px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;'>${params[i].seriesName}</div>
                    </div>
                    <span style='color:var(--content-text-content);font-weight:500;'>
                      ${valueWithUnit}
                    </span>
                  </div>`;
        }
      }
      return res;
    }
  },
  
  // Legend configuration (series selector)
  legend: {
    top: 0,                                            // Position at top
    type: 'scroll',                                    // Scrollable for many series
    padding: [0, 40],                                  // Horizontal padding
    data: props.series.map(item => item.name),        // Series names
    selected: {}                                       // Initial selection state (empty = all selected)
  },
  
  // X-axis configuration
  xAxis: [
    {
      type: 'category',        // Category type for timestamp labels
      data: props.xData,       // Timestamp data
      boundaryGap: false,      // Start line at first data point (not offset)
      axisTick: {
        show: false            // Hide axis tick marks
      }
    }
  ],
  
  // Y-axis configuration (dual axes)
  yAxis: [
    // Primary Y-axis (left)
    {
      type: 'value',           // Numeric value axis
      splitLine: {
        show: true,            // Show horizontal grid lines
        lineStyle: {
          type: 'dashed'       // Dashed line style
        }
      },
      grid: {
        left: 'left'
      }
    },
    // Secondary Y-axis (right, for thread pool metrics)
    {
      type: 'value',           // Numeric value axis
      splitLine: {
        show: true,            // Show horizontal grid lines
        lineStyle: {
          type: 'dashed'       // Dashed line style
        }
      },
      grid: {
        left: 'left'
      }
    }
  ],
  
  // Series configuration
  series: props.series.map((item, idx) => ({
    ...item,
    type: 'line',
    lineStyle: {
      width: 1                                       // Thin line
    },
    barGap: '5%',
    smooth: false,                                   // Disable curve smoothing
    showSymbol: props.xData?.length === 1,          // Show symbols only for single data point
    
    // Gradient fill color from full opacity at top to transparent at bottom
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },  // Bottom: 10% opacity
        { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }     // Top: full opacity
      ])
    },
    areaStyle: {},                                   // Enable area fill under line
    
    // Use secondary Y-axis for thread pool metrics in analyze tab
    yAxisIndex: ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'].includes(item.key) && props.tabKey === 'analyze' ? 1 : 0
  }))
};

/**
 * Component lifecycle hooks
 */

/**
 * Before unmount hook
 * Cleans up event listeners and resize detector to prevent memory leaks
 */
onBeforeUnmount(() => {
  if (chartsRef.value) {
    erd.removeListener(chartsRef.value, resizeHandler);
  }

  myChart.off('legendselectchanged');
});

/**
 * Watch for series data changes
 * Updates chart with new data and resets initial load flag
 */
watch(() => props.series, (newValue) => {
  setOption(newValue);
  isInit = false;  // After first update, no longer initial load
}, { immediate: true });

/**
 * Component mount hook
 * Initializes the chart after DOM is ready
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
  <div
    ref="chartsRef"
    class="h-70"
    style="width: 100%;"></div>
</template>
