
<script setup lang="ts">
import { ref, defineAsyncComponent, computed, watch, nextTick } from 'vue';
import { RadioGroup, RadioButton, Slider, CheckboxGroup, Checkbox, TableColumnType } from 'ant-design-vue';
import { Select } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';

import { allCvsNames } from './ChartConfig';

// Lazy-loaded components for better performance
const LineChart = defineAsyncComponent(() => import('./LineChart.vue'));
const InfoTable = defineAsyncComponent(() => import('./InfoTable.vue'));

/**
 * Data unit type for bandwidth metrics
 */
type BandwidthUnit = 'KB' | 'MB';

/**
 * Index option interface
 */
interface IndexOption {
  label: string;  // Display label
  value: string;  // Option value
}

/**
 * Series data interface for chart
 */
interface SeriesData {
  name: string;    // Series name for legend
  data: number[];  // Data points array
  key: string;     // Unique key for series
}

/**
 * Component props interface
 */
interface Props {
  isSingleInterface: boolean;                                      // Whether testing single API or multiple
  cvsKeys: string[];                                               // CSV metric keys
  timestampData: string[];                                         // Timestamp array for x-axis
  apiNames: string[];                                              // API names list
  columns: TableColumnType[];                                      // Table column definitions
  brpsUnit: BandwidthUnit;                                         // Bytes received per second unit
  bwpsUnit: BandwidthUnit;                                         // Bytes written per second unit
  apiDimensionObj: { [key: string]: { [key: string]: number[] } }; // API dimension data
  indexDimensionObj: { [key: string]: { [key: string]: number[] } };// Index dimension data
  indexOptions: IndexOption[];                                     // Index dropdown options
  pipelineTargetMappings: Record<string, string[] | null>;        // Pipeline to target mappings
  tabKey?: string;                                                 // Current tab identifier
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  isSingleInterface: false,
  cvsKeys: () => [],
  timestampData: () => [],
  apiNames: () => [],
  columns: () => [],
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  apiDimensionObj: undefined,
  indexOptions: () => [],
  pipelineTargetMappings: undefined,
  tabKey: ''
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * View mode type
 */
type ViewMode = 'api' | 'metric' | 'overlay';

/**
 * State Management
 */

// Current active view mode (default: by API)
const activeKey = ref<ViewMode>('api');

// Current selected metric key (for metric view)
const currIndex = ref(props.cvsKeys[0]);

// Current selected API name (for API view)
const currApi = ref('Total');

// Chart data
const xData = ref<string[]>([]);                 // X-axis timestamp data
const seriesData = ref<SeriesData[]>([]);        // Y-axis series data

// Time range slider state
const sliderValue = ref<[number, number]>([0, 0]);  // Current slider range [start, end]
const sliderMin = ref<number>(0);                   // Minimum slider value
const sliderMax = ref<number>(0);                   // Maximum slider value
const isSliding = ref(false);                       // Whether user is actively sliding

/**
 * Update chart series data based on active view mode
 * Routes to appropriate data builder based on current view
 */
const setUpdateSeriesData = (): void => {
  if (props.isSingleInterface) {
    // Single interface: always show by API (all metrics)
    setApiseriesData();
  } else {
    // Multiple interfaces: route based on view mode
    if (activeKey.value === 'metric') {
      setIndexSeriesData();
    } else if (activeKey.value === 'api') {
      setApiseriesData();
    } else {
      // Overlay mode: use custom selection
      setCharData(checkboxGroupValue.value);
    }
  }
};

/**
 * Build series data for "By API" view
 * Shows all metrics for currently selected API
 * Each series represents a different metric (ops, tps, etc.)
 */
const setApiseriesData = (): void => {
  const result: SeriesData[] = [];
  
  for (let i = 0; i < props.cvsKeys.length; i++) {
    const cvskey = props.cvsKeys[i];
    const apiObjIndexList = props.indexDimensionObj[cvskey][currApi.value];
    
    result.push({
      key: cvskey,
      name: allCvsNames?.[cvskey] || '',
      data: apiObjIndexList.slice(sliderValue.value[0], sliderValue.value[1] + 1)
    });
  }
  
  seriesData.value = result;
};

/**
 * Build series data for "By Metric" view
 * Shows single metric across all APIs
 * Each series represents a different API
 */
const setIndexSeriesData = (): void => {
  const result: SeriesData[] = [];
  
  for (let i = 0; i < props.apiNames.length; i++) {
    const name = props.apiNames[i];
    const indexObjApiList = props.apiDimensionObj[name][currIndex.value];
    
    result.push({
      key: name,
      name,
      data: indexObjApiList.slice(sliderValue.value[0], sliderValue.value[1] + 1)
    });
  }
  
  seriesData.value = result;
};

/**
 * Table data for displaying statistics
 */
const tableData = ref<Record<string, any>[]>([]);

/**
 * Transform raw dimension data into table format
 * Builds hierarchical table data with pipeline groups and child APIs
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
  const result: any[] = [];
  
  // Single interface: return single Total row
  if (props.isSingleInterface) {
    if (Object.prototype.hasOwnProperty.call(dataSource, 'Total')) {
      result.push({ name: 'Total', ...dataSource.Total });
    }
    return result;
  }

  // Validate inputs
  if (!mappings || !Object.keys(mappings)?.length || !keys?.length) {
    return [];
  }
  
  // Override keys for throughput tab to show all throughput metrics
  if (props.tabKey === 'throughput') {
    keys = ['ops', 'minOps', 'maxOps', 'meanOps', 'tps', 'minTps', 'maxTps', 'meanTps'];
  }

  // Build hierarchical table data
  for (const key in mappings) {
    const item: any = { name: key, list: [] };
    
    // Add metrics for pipeline
    keys.reduce((prev, cur) => {
      prev[cur] = isNaN(Number(dataSource?.[key]?.[cur])) ? '--' : dataSource[key][cur];
      return prev;
    }, item);
    
    // Add child APIs if exist
    if (mappings[key].length) {
      item.list = mappings[key].map(_key => {
        const listItem: any = { name: _key };
        return keys.reduce((prev, cur) => {
          prev[cur] = isNaN(Number(dataSource?.[_key]?.[cur])) ? '--' : dataSource[_key][cur];
          return prev;
        }, listItem);
      });
    }
    
    result.push(item);
  }
  
  // Return empty if no data source
  if (dataSource === undefined) {
    return [];
  }
  
  return result;
};

/**
 * Extract API dimension data at specific time index
 * Slices time-series data to get snapshot at given index
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
    xData.value = cloneDeep(newVal);
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
  xData.value = props.timestampData.slice(rangeValue[0], rangeValue[1] + 1);
  setUpdateSeriesData();
};

/**
 * Selected metrics for overlay mode (format: "APIName-metricKey")
 */
const checkboxGroupValue = ref<string[]>([]);

/**
 * Handle checkbox group change in overlay mode
 * Updates selected metrics and refreshes chart data
 * 
 * @param values - Array of selected metric identifiers
 */
const CheckboxGroupChange = (values: any): void => {
  const stringValues = Array.isArray(values) ? values.map((v: any) => String(v)) : [];
  checkboxGroupValue.value = stringValues;
  setCharData(stringValues);
};

/**
 * Track if overlay mode is first loaded (for default selection)
 */
let isFirstLoadMetric = true;

/**
 * Handle view mode radio button change
 * Switches between API, Metric, and Overlay views
 * 
 * @param e - Radio change event
 */
const radioGroupChange = (e: any): void => {
  xData.value = props.timestampData.slice(sliderValue.value[0], sliderValue.value[1] + 1);

  if (e.target.value === 'metric') {
    setIndexSeriesData();
  } else if (e.target.value === 'api') {
    setApiseriesData();
  } else {
    // Overlay mode: initialize with all Total metrics on first load
    if (isFirstLoadMetric) {
      seriesData.value = [];
      for (const cvsKey of props.cvsKeys) {
        const name = `Total-${allCvsNames[cvsKey]}`;
        checkboxGroupValue.value.push(`Total-${cvsKey}`);
        const data = props.indexDimensionObj[cvsKey].Total.slice(sliderValue.value[0], sliderValue.value[1] + 1);
        seriesData.value.push({ name, data, key: name });
      }
      isFirstLoadMetric = false;
    } else {
      setCharData(checkboxGroupValue.value);
    }
  }
};

/**
 * Build chart series data from selected metrics in overlay mode
 * Parses metric identifiers and extracts data
 * 
 * @param values - Array of selected metric identifiers (format: "APIName-metricKey")
 */
const setCharData = (values: string[]): void => {
  seriesData.value = [];
  
  for (let i = 0; i < values.length; i++) {
    const [apiName, cvsKey] = values[i].split('-');
    
    // Extract data for selected time range or full range
    const data = isSliding.value 
      ? props.indexDimensionObj[cvsKey][apiName].slice(sliderValue.value[0], sliderValue.value[1] + 1)
      : props.indexDimensionObj[cvsKey][apiName];
    
    const name = `${apiName}-${allCvsNames[cvsKey]}`;
    seriesData.value.push({ name, data, key: name });
  }
};

/**
 * Handle metric selection change
 * Updates chart to show selected metric across all APIs
 */
const currIndexChange = (): void => {
  setIndexSeriesData();
};

/**
 * Handle API selection change
 * Updates chart to show all metrics for selected API
 */
const currApiChange = (): void => {
  setApiseriesData();
};

/**
 * Format slider tooltip to show timestamp
 * 
 * @param value - Slider index value
 * @returns Formatted timestamp string
 */
const sliderFormatter = (value?: number): string => {
  if (value === undefined) return '';
  return props.timestampData[value];
};

/**
 * Get API options for metric selector
 * Converts API names to option format
 */
const metricOptions = computed(() => {
  return props.apiNames.map(item => ({ label: item, value: item }));
});

/**
 * Reference to chart component
 */
const chartRef = ref();

/**
 * Resize chart to fit container
 * Called when container dimensions change
 */
const resizeChart = (): void => {
  nextTick(() => {
    if (chartRef.value) {
      chartRef.value.resizeChart();
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
  <!-- Main container for count template -->
  <div style="width: 100%; min-height: 100%;">
    <!-- Control panel: view mode selector and metric/API selector -->
    <div 
      class="flex items-center text-3 pl-10 pr-15" 
      :class="props.isSingleInterface ? 'justify-end' : 'justify-between'">
      <!-- View mode selector (only for multiple APIs) -->
      <RadioGroup
        v-if="!isSingleInterface"
        v-model:value="activeKey"
        buttonStyle="solid"
        size="small"
        class="whitespace-nowrap"
        @change="radioGroupChange">
        <RadioButton value="api">
          {{ t('ftpPlugin.performanceTestDetail.countTemplate.radioButtons.byApi') }}
        </RadioButton>
        <RadioButton value="metric">
          {{ t('ftpPlugin.performanceTestDetail.countTemplate.radioButtons.byMetric') }}
        </RadioButton>
        <RadioButton value="overlay">
          {{ t('ftpPlugin.performanceTestDetail.countTemplate.radioButtons.overlayAnalysis') }}
        </RadioButton>
      </RadioGroup>
      
      <!-- Dynamic selector based on view mode -->
      <template v-if="!isSingleInterface">
        <!-- Metric selector (shown in "By Metric" view) -->
        <Select
          v-if="activeKey === 'metric'"
          v-model:value="currIndex"
          class="w-60"
          :options="indexOptions"
          @change="currIndexChange" />
        
        <!-- API selector (shown in "By API" view) -->
        <Select
          v-else-if="activeKey === 'api'"
          v-model:value="currApi"
          class="w-60"
          :options="metricOptions"
          @change="currApiChange" />
      </template>
    </div>
    
    <!-- Line chart for data visualization -->
    <LineChart
      ref="chartRef"
      :series="seriesData"
      :xData="xData"
      :selectedIndex="currIndex"
      :tabKey="props.tabKey"
      :brpsUnit="props.brpsUnit"
      :bwpsUnit="props.bwpsUnit"
      class="mt-2" />
    
    <!-- Time range slider (shown only when multiple data points exist) -->
    <div v-if="props.timestampData.length >= 2" class="-mt-5 pl-10 pr-12">
      <Slider
        :value="sliderValue"
        :min="sliderMin"
        :max="sliderMax"
        :tipFormatter="sliderFormatter"
        range
        @change="sliderChange" />
    </div>
    
    <!-- Overlay mode: metric selection checkboxes -->
    <template v-if="!isSingleInterface && activeKey === 'overlay'">
      <div class="pl-17 text-3 mt-3.5">
        <CheckboxGroup :value="checkboxGroupValue" @change="CheckboxGroupChange">
          <!-- Render checkboxes for each API's metrics -->
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <!-- API name label (truncated) -->
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            
            <!-- Metric checkboxes (max 10 selected, min 1 selected) -->
            <div class="space-x-5">
              <Checkbox
                v-for="metric in props.indexOptions"
                :key="apiName + '-' + metric.value"
                :value="apiName + '-' + metric.value"
                :disabled="(!checkboxGroupValue.includes(apiName + '-' + metric.value) && checkboxGroupValue.length >= 10) || 
                           (checkboxGroupValue.includes(apiName + '-' + metric.value) && checkboxGroupValue.length === 1)">
                {{ metric.label }}
              </Checkbox>
            </div>
          </div>
        </CheckboxGroup>
      </div>
    </template>
    
    <!-- Statistics table showing aggregate data -->
    <InfoTable
      class="mt-8"
      :isSingleInterface="props.isSingleInterface"
      :brpsUnit="props.brpsUnit"
      :bwpsUnit="props.bwpsUnit"
      :minBrpsUnit="props.brpsUnit"
      :maxBrpsUnit="props.brpsUnit"
      :meanBrpsUnit="props.brpsUnit"
      :minBwpsUnit="props.bwpsUnit"
      :maxBwpsUnit="props.bwpsUnit"
      :meanBwpsUnit="props.bwpsUnit"
      :columns="props.columns"
      :tableData="tableData" />
  </div>
</template>

<style scoped>
/**
 * Custom styles for grouped table layout
 */
.group-table :deep(.ant-table) {
  margin: 2px -7px 2px 12px !important;
  padding-left: 13px;
}

/**
 * Last row background color in table
 */
:deep(.ant-table .last-row > td) {
  background-color: #FAFCFC;
}

/**
 * Slider styles for time range selection
 */

/* Slider rail (base track) */
:deep(.ant-slider-rail) {
  height: 10px;
  border: 1px solid var(--border-divider);
  border-radius: 1px;
  border-color: var(--border-divider);
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider:hover .ant-slider-rail) {
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

/* Slider track (selected range) */
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

/* Slider step marks */
:deep(.ant-slider-step) {
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

/* Slider handle (drag thumb) */
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
