
<script setup lang="ts">
import { ref, defineAsyncComponent, watch, computed } from 'vue';
import { RadioGroup, RadioButton, Slider } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { 
  allResponseTimeColumns, 
  allCvsNames, 
  allErrorsColumns, 
  allErrorRateColumns, 
  allUploadColumns, 
  oneUploadColumns, 
  throughputColumns, 
  oneThroughputColumns 
} from '../ChartConfig';

// Lazy-loaded components for better performance
const LineChart = defineAsyncComponent(() => import('../LineChart.vue'));
const InfoTable = defineAsyncComponent(() => import('../InfoTable.vue'));

/**
 * Data unit type for bandwidth metrics
 */
type BandwidthUnit = 'KB' | 'MB';

/**
 * Column definition interface for table display
 */
interface ColumnDefinition {
  label: string;      // Display label
  dataIndex: string;  // Data key for mapping
}

/**
 * Index option interface for dropdown selection
 */
interface IndexOption {
  label: string;  // Display label
  value: string;  // Option value
}

/**
 * Component props interface
 */
interface Props {
  isSingleInterface: boolean;                                      // Whether testing single API or multiple
  cvsKeys: string[];                                               // CSV metric keys
  timestampData: string[];                                         // Timestamp array for x-axis
  apiNames: string[];                                              // API names list
  brpsUnit: BandwidthUnit;                                         // Bytes received per second unit
  minBrpsUnit: BandwidthUnit;                                      // Min BRPS unit
  maxBrpsUnit: BandwidthUnit;                                      // Max BRPS unit
  meanBrpsUnit: BandwidthUnit;                                     // Mean BRPS unit
  bwpsUnit: BandwidthUnit;                                         // Bytes written per second unit
  minBwpsUnit: BandwidthUnit;                                      // Min BWPS unit
  maxBwpsUnit: BandwidthUnit;                                      // Max BWPS unit
  meanBwpsUnit: BandwidthUnit;                                     // Mean BWPS unit
  apiDimensionObj: { [key: string]: { [key: string]: number[] } }; // API dimension data
  indexDimensionObj: { [key: string]: { [key: string]: number[] } };// Index dimension data
  columns: ColumnDefinition[];                                     // Table column definitions
  indexOptions: IndexOption[];                                     // Index dropdown options
  pipelineTargetMappings: Record<string, string[] | null>;        // Pipeline to target mappings
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  isSingleInterface: false,
  cvsKeys: () => [],
  timestampData: () => [],
  apiNames: () => [],
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  indexDimensionObj: undefined,
  apiDimensionObj: undefined,
  columns: () => [],
  indexOptions: () => [],
  pipelineTargetMappings: undefined
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * View type for metric selection
 */
type ViewType = 'responseTime' | 'throughput' | 'errorNum' | 'errorRate' | 'upload';

/**
 * Series data interface for chart display
 */
interface SeriesData {
  name: string;    // Series name for legend
  key: string;     // Unique key for series
  data: number[];  // Data points array
}

/**
 * State Management
 */
// Currently active metric view (default: throughput)
const activeKey = ref<ViewType>('throughput');

// Current selected index and API
const currIndex = ref(props.cvsKeys[0]);
const currApi = ref('Total');

// Chart data
const xData = ref<string[]>([]);                        // X-axis timestamp data
const seriesData = ref<SeriesData[]>([]);               // Y-axis series data

// Time range slider state
const sliderValue = ref<[number, number]>([0, 0]);      // Current slider range
const sliderMin = ref<number>(0);                       // Minimum slider value
const sliderMax = ref<number>(0);                       // Maximum slider value
const isSliding = ref(false);                           // Whether user is actively sliding

// Table data for displaying aggregate statistics
const tableData = ref<Record<string, any>[]>([]);

/**
 * Generate table data from mappings and data source
 * Transforms raw metric data into table-ready format with hierarchical structure
 * 
 * @param mappings - Pipeline to target API mappings
 * @param keys - Metric keys to extract
 * @param dataSource - Raw data source object
 * @returns Formatted table data array
 */
const getTableData = (
  mappings: { [key: string]: string[] }, 
  keys: string[], 
  dataSource: { [key: string]: { [key: string]: number } }
): Record<string, any>[] => {
  // Handle single interface case - return single Total row
  if (props.isSingleInterface) {
    const obj: Record<string, any> = {
      name: 'Total'
    };
    
    // Extract metrics at current slider position
    for (const key in props.apiDimensionObj.Total) {
      if (keys.includes(key)) {
        obj[key] = props.apiDimensionObj.Total[key][sliderValue.value[1]];
      }
    }
    return [obj];
  }

  // Validate inputs
  if (!mappings || !Object.keys(mappings)?.length || !keys?.length) {
    return [];
  }
  
  // Build hierarchical table data with pipeline groups
  const result: any[] = [];
  for (const key in mappings) {
    const item: any = { name: key, list: [] };
    
    // Add metrics for pipeline
    keys.reduce((prev, cur) => {
      prev[cur] = isNaN(Number(dataSource?.[key]?.[cur])) ? '--' : dataSource[key][cur];
      return prev;
    }, item);
    
    // Add child APIs if exists
    if (mappings[key].length) {
      item.list = mappings[key].map((_key) => {
        const listItem: any = { name: _key };

        return keys.reduce((prev, cur) => {
          prev[cur] = isNaN(Number(dataSource?.[_key]?.[cur])) ? '--' : dataSource[_key][cur];
          return prev;
        }, listItem);
      });
    }

    result.push(item);
  }

  return result;
};

/**
 * Current metric keys to display in table based on active view
 */
const oneApiTableKey = ref(props.isSingleInterface ? ['tps'] : ['ops', 'tps']);

/**
 * Extract API dimension data at specific time index
 * Slices the time-series data to get snapshot at given index
 * 
 * @param apiDimensionObj - API dimension data object
 * @param index - Time index to extract data from
 */
const getApiDimensionObj = (
  apiDimensionObj: { [key: string]: { [key: string]: number[] } }, 
  index: number
): void => {
  const newApiDimensionObj: { [key: string]: { [key: string]: number } } = {};
  
  // Extract value at specific index for all APIs and metrics
  for (const key in apiDimensionObj) {
    newApiDimensionObj[key] = {};
    for (const prop in apiDimensionObj[key]) {
      newApiDimensionObj[key][prop] = apiDimensionObj[key][prop][index];
    }
  }
  
  // Update table with snapshot data
  tableData.value = getTableData(props.pipelineTargetMappings as any, props.cvsKeys, newApiDimensionObj);
};

/**
 * Update chart series data based on active view type
 * Determines which metrics to display and extracts data for current time range
 */
const setUpdateSeriesData = (): void => {
  // Determine which metrics to show based on active view
  if (activeKey.value === 'responseTime') {
    oneApiTableKey.value = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];
  }
  if (activeKey.value === 'throughput') {
    oneApiTableKey.value = props.isSingleInterface ? ['tps'] : ['ops', 'tps'];
  }
  if (activeKey.value === 'errorNum') {
    oneApiTableKey.value = ['errors'];
  }
  if (activeKey.value === 'errorRate') {
    oneApiTableKey.value = ['errorRate'];
  }
  if (activeKey.value === 'upload') {
    oneApiTableKey.value = ['brps', 'bwps'];
  }
  
  // Build series data for chart
  const yData: { name: string; key: string; data: number[] }[] = [];
  for (const item of Object.keys(props.indexDimensionObj)) {
    if (oneApiTableKey.value.includes(item)) {
      // Extract data for current time range
      const data = props.indexDimensionObj[item][currApi.value].slice(
        sliderValue.value[0], 
        sliderValue.value[1] + 1
      );
      yData.push({ 
        name: allCvsNames?.[item] || '', 
        key: item, 
        data: data 
      });
    }
  }
  seriesData.value = yData;
};

/**
 * Watch timestamp data changes
 * Updates chart and table data when new timestamp data arrives
 */
watch(() => props.timestampData, (newVal) => {
  // Reset data if no timestamps
  if (!newVal.length) {
    tableData.value = [];
    seriesData.value = [];
    xData.value = [];
    return;
  }

  // Update slider range
  sliderMax.value = newVal.length - 1;
  
  // Auto-update to latest data if not manually sliding
  if (!isSliding.value) {
    sliderValue.value[1] = sliderMax.value;
    getApiDimensionObj(props.apiDimensionObj, sliderValue.value[1]);
    xData.value = JSON.parse(JSON.stringify(newVal));
    setUpdateSeriesData();
  }
}, {
  immediate: true
});

/**
 * Handle time range slider change
 * Updates chart and table to show data for selected time range
 * 
 * @param value - New slider range [start, end]
 */
const sliderChange = (value: number | [number, number]): void => {
  // Normalize value to array
  const rangeValue = Array.isArray(value) ? value : [0, value] as [number, number];
  
  // Track whether user is manually sliding
  if (rangeValue[1] < props.timestampData.length - 1) {
    isSliding.value = true;
  } else {
    isSliding.value = false;
  }

  // Update table data if end index changed
  if (rangeValue[1] !== sliderValue.value[1]) {
    getApiDimensionObj(props.apiDimensionObj, rangeValue[1]);
  }

  sliderValue.value = rangeValue;

  // Update chart with sliced time range
  xData.value = props.timestampData.slice(sliderValue.value[0], sliderValue.value[1] + 1);
  setUpdateSeriesData();
};

/**
 * Handle metric view radio button change
 * Switches between different metric views (throughput, response time, etc.)
 * 
 * @param e - Radio change event
 */
const radioGroupChange = (e: any): void => {
  activeKey.value = e.target.value;
  xData.value = props.timestampData.slice(sliderValue.value[0], sliderValue.value[1] + 1);
  setUpdateSeriesData();

  // Determine which keys to display based on view type
  let keys = oneApiTableKey.value;
  if (activeKey.value === 'upload') {
    keys = ['brps', 'minBrps', 'maxBrps', 'meanBrps', 'bwps', 'minBwps', 'maxBwps', 'meanBwps'];
  }

  if (activeKey.value === 'throughput') {
    keys = props.isSingleInterface 
      ? ['tps', 'minTps', 'maxTps', 'meanTps'] 
      : ['ops', 'minOps', 'maxOps', 'meanOps', 'tps', 'minTps', 'maxTps', 'meanTps'];
  }
  
  // Update table with new keys
  tableData.value = getTableData({}, keys, {});
};

/**
 * Format slider tooltip to show timestamp
 * 
 * @param value - Slider index value (optional)
 * @returns Formatted timestamp string or empty
 */
const sliderFormatter = (value?: number): string => {
  if (value === undefined) return '';
  return props.timestampData[value];
};

/**
 * Computed column definitions based on active view and interface type
 * Returns appropriate columns for single interface or multiple API views
 */
const columns = computed(() => {
  if (props.isSingleInterface) {
    if (activeKey.value === 'responseTime') {
      return allResponseTimeColumns;
    }
    if (activeKey.value === 'throughput') {
      return props.isSingleInterface ? oneThroughputColumns : throughputColumns;
    }
    if (activeKey.value === 'errorNum') {
      // Filter out ops columns for single interface
      return props.isSingleInterface 
        ? allErrorsColumns.filter(item => item.dataIndex !== 'ops' && item.dataIndex !== 'operations') 
        : allErrorsColumns;
    }
    if (activeKey.value === 'errorRate') {
      return allErrorRateColumns;
    }
    if (activeKey.value === 'upload') {
      return props.isSingleInterface ? oneUploadColumns : allUploadColumns;
    }
  }
  
  // Default columns filtering
  return props.isSingleInterface 
    ? props.columns.filter(item => item.dataIndex !== 'ops' && item.dataIndex !== 'operations') 
    : props.columns;
});

/**
 * Reference to chart component for resize operations
 */
const chartRef = ref();

/**
 * Resize chart to fit container
 * Used when container dimensions change
 */
const resizeChart = (): void => {
  if (chartRef.value) {
    chartRef.value.resizeChart();
  }
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  resizeChart
});
</script>

<template>
  <!-- Main container for aggregate performance data -->
  <div class="w-full min-h-ull">
    <!-- Single interface view with chart and time range selector -->
    <template v-if="isSingleInterface">
      <!-- Metric view selector (throughput, response time, errors, etc.) -->
      <RadioGroup
        v-model:value="activeKey"
        buttonStyle="solid"
        size="small"
        class="ml-10"
        @change="radioGroupChange">
        <RadioButton value="throughput">
          {{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byThroughput') }}
        </RadioButton>
        <RadioButton value="responseTime">
          {{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byResponseTime') }}
        </RadioButton>
        <RadioButton value="errorNum">
          {{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byErrorCount') }}
        </RadioButton>
        <RadioButton value="errorRate">
          {{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byErrorRate') }}
        </RadioButton>
        <RadioButton value="upload">
          {{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byUploadDownload') }}
        </RadioButton>
      </RadioGroup>
      
      <!-- Line chart for time-series data visualization -->
      <LineChart
        ref="chartRef"
        :series="seriesData"
        :xData="xData"
        :selectedIndex="currIndex"
        type="throughput"
        :brpsUnit="props.brpsUnit"
        :bwpsUnit="props.bwpsUnit"
        class="mt-2" />
      
      <!-- Time range slider (shown only when multiple data points exist) -->
      <div v-if="props.timestampData.length >= 2" class="-mt-5 pl-10 pr-12 mb-8">
        <Slider
          :value="sliderValue"
          :min="sliderMin"
          :max="sliderMax"
          :tipFormatter="sliderFormatter"
          range
          @change="sliderChange" />
      </div>
    </template>
    
    <!-- Data table showing aggregate statistics -->
    <InfoTable
      tabKey="aggregate"
      :isSingleInterface="props.isSingleInterface"
      :brpsUnit="props.brpsUnit"
      :bwpsUnit="props.bwpsUnit"
      :minBrpsUnit="props.minBrpsUnit"
      :maxBrpsUnit="props.maxBrpsUnit"
      :meanBrpsUnit="props.meanBrpsUnit"
      :minBwpsUnit="props.minBwpsUnit"
      :maxBwpsUnit="props.maxBwpsUnit"
      :meanBwpsUnit="props.meanBwpsUnit"
      :columns="columns"
      :tableData="tableData" />
  </div>
</template>
<style scoped>

.group-table :deep(.ant-table) {
  margin: 2px -7px 2px 12px !important;
  padding-left: 13px;
}


:deep(.ant-table .last-row > td) {
  background-color: #FAFCFC;
}


:deep(.ant-slider-rail) {
  height: 10px;
  border: 1px solid var(--border-divider);
  border-radius: 1px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider:hover .ant-slider-rail) {
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}


:deep(.ant-slider-track) {
  height: 10px;
  border-color: rgba(145, 213, 255, 30%);
  background-color: rgba(245, 245, 245, 100%);

  @apply border;
}

:deep(.ant-slider:hover .ant-slider-track) {
  height: 10px;
  background-color: rgba(245, 245, 245, 100%);
}


:deep(.ant-slider-step) {
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}


:deep(.ant-slider-handle) {
  width: 10px;
  height: 16px;
  margin-top: -3px;
  border-radius: 3px;
  border-color: rgba(145, 213, 255, 100%);

  @apply border-2;
}

:deep(.ant-slider-handle:focus) {
  border-color: rgba(145, 213, 255, 100%);
  box-shadow: none;
}

:deep(.ant-slider:hover .ant-slider-handle) {
  border-color: rgba(145, 213, 255, 100%);
}
</style>
