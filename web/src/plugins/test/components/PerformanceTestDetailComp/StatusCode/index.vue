
<script setup lang="ts">
import { ref, defineAsyncComponent, watch, nextTick } from 'vue';
import { NoData } from '@xcan-angus/vue-ui';

// Lazy-loaded chart components for better performance
const GroupBarChart = defineAsyncComponent(() => import('./GroupBarChart.vue'));
const OneGroupBarChart = defineAsyncComponent(() => import('./OneGroupBarChart.vue'));
const PirChart = defineAsyncComponent(() => import('./PirChart.vue'));

/**
 * Chart data item interface for single API
 */
interface ChartDataItem {
  name: string;    // Status code category (2xx, 3xx, etc.)
  value: number;   // Count of responses
  color: string;   // Display color
}

/**
 * Series data interface for grouped charts
 */
interface SeriesData {
  name: string;    // Status code category
  data: number[];  // Counts per API
}

/**
 * Component props interface
 */
interface Props {
  isSingleInterface: boolean;           // Whether testing single API or multiple
  statusCodeData: Record<string, any>;  // Status code statistics data
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  isSingleInterface: false,
  statusCodeData: undefined
});

/**
 * Chart data state
 */
const chartData = ref<{
  xData: string[];                      // API names or status codes
  yData: number[] | SeriesData[];       // Response counts
}>({
  xData: [],
  yData: []
});

/**
 * Total status code data for pie chart
 */
const totalData = ref<ChartDataItem[]>([]);

/**
 * HTTP status code categories to display
 * Includes standard ranges and exception category
 */
const codeEnums = ['2xx', '3xx', '4xx', '5xx', 'Exception'];

/**
 * Get color for status code category
 * Returns a specific color for each HTTP status code range
 * 
 * @param name - Status code category (1xx, 2xx, etc.)
 * @returns RGB color string
 */
const getColor = (name: string): string => {
  switch (name) {
    case '1xx':
      return 'rgba(103, 215, 255, 1)';   // Light blue for informational
    case '2xx':
      return 'rgba(82, 196, 26, 1)';     // Green for success
    case '3xx':
      return 'rgba(45, 142, 255, 1)';    // Blue for redirection
    case '4xx':
      return 'rgba(255, 215, 0, 1)';     // Gold for client errors
    case '5xx':
      return 'rgba(255, 99, 71, 1)';     // Tomato for server errors
    case 'Exception':
      return 'rgb(255, 0, 0, 1)';        // Red for exceptions
    default:
      return 'rgba(128, 128, 128, 1)';   // Gray for unknown
  }
};

/**
 * Transform status code data into chart format
 * Handles both single interface and multiple API scenarios
 * 
 * @param data - Raw status code data object
 * @returns Object containing xData and yData for charts
 */
const getXDataAndYData = (data: Record<string, any>) => {
  if (!data) {
    return { xData: [], yData: [] };
  }
  
  const keys = Object.keys(data);
  if (!keys.length) {
    return { xData: [], yData: [] };
  }

  // Single interface: x-axis = status codes, y-axis = counts
  if (props.isSingleInterface) {
    const xData = Object.keys(data[keys[0]]);
    const yData = Object.values(data[keys[0]])?.map(item => item ? +item : 0) || [];
    return { xData, yData };
  } else {
    // Multiple APIs: x-axis = API names, y-axis = series per status code
    const xData = Object.keys(data);
    const yData: SeriesData[] = [];
    
    // Build series for each status code category
    for (let i = 0; i < codeEnums.length; i++) {
      const name = codeEnums[i];
      const obj: SeriesData = {
        name,
        data: []
      };
      
      // Collect counts for this status code across all APIs
      for (let j = 0; j < keys.length; j++) {
        const key = keys[j];
        const _value = data[key]?.[name] || 0;
        obj.data.push(_value ? +_value : 0);
      }
      
      yData.push(obj);
    }

    return { xData, yData };
  }
};

/**
 * Watch for status code data changes
 * Transforms and updates chart data when new statistics arrive
 */
watch(() => props.statusCodeData, (newValue) => {
  // Transform data for bar charts
  chartData.value = getXDataAndYData(newValue) as any;
  
  // Build total data for pie chart
  if (newValue?.Total) {
    totalData.value = codeEnums.map((item) => {
      return {
        name: item,
        value: Number(newValue.Total?.[item]) || 0,
        color: getColor(item)
      };
    });
  }
}, {
  immediate: true  // Execute immediately on mount
});

/**
 * Chart component references
 */
const oneGroupBarChartRef = ref();  // Reference to single API bar chart
const groupBarChartRef = ref();     // Reference to multi-API grouped bar chart
const pirChartRef = ref();          // Reference to pie chart

/**
 * Resize all charts to fit container
 * Useful when container dimensions change
 */
const resizeChart = (): void => {
  nextTick(() => {
    if (oneGroupBarChartRef.value) {
      oneGroupBarChartRef.value.resizeChart();
    }
    if (groupBarChartRef.value) {
      groupBarChartRef.value.resizeChart();
    }
    if (pirChartRef.value) {
      pirChartRef.value.resizeChart();
    }
  });
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  resizeChart
});
</script>

<template>
  <!-- Main content (shown when data is available) -->
  <template v-if="chartData.xData.length">
    <div class="h-full flex flex-col justify-center">
      <div class="text-3 text-text-content w-full h-75 flex items-center justify-center">
        <!-- Single API view: Bar chart with status code distribution -->
        <template v-if="props.isSingleInterface">
          <div class="h-full w-full flex" style="max-width:1000px">
            <OneGroupBarChart
              ref="oneGroupBarChartRef"
              :xData="chartData.xData"
              :yData="totalData as any" />
          </div>
        </template>
        
        <!-- Multiple APIs view: Pie chart + Grouped bar chart -->
        <template v-else>
          <!-- Left side: Pie chart showing total distribution -->
          <div class="pb-2 h-full w-80">
            <PirChart 
              ref="pirChartRef" 
              :dataSource="totalData" />
          </div>
          
          <!-- Right side: Grouped bar chart showing per-API distribution -->
          <div class="overflow-x-auto pb-2 h-full flex-1 ml-5">
            <GroupBarChart
              ref="groupBarChartRef"
              :colors="totalData.map(item => item.color)"
              :xData="chartData.xData"
              :yData="chartData.yData as any" />
          </div>
        </template>
      </div>
    </div>
  </template>
  
  <!-- No data placeholder (shown when no status code data) -->
  <template v-else>
    <NoData class="h-full" />
  </template>
</template>
