
<script setup lang="ts">
import { ref, defineAsyncComponent, watch } from 'vue';
import { Slider, CheckboxGroup, Checkbox } from 'ant-design-vue';
import { Toggle } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';

import { allCvsNames } from '../ChartConfig';

// Lazy-loaded line chart component
const LineChart = defineAsyncComponent(() => import('../LineChart.vue'));

/**
 * Data unit type for bandwidth metrics
 */
type BandwidthUnit = 'KB' | 'MB';

/**
 * Column definition interface
 */
interface ColumnDefinition {
  label: string;      // Display label
  dataIndex: string;  // Data key for mapping
}

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
  key: string;     // Metric key
  data: number[];  // Data points array
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
  bwpsUnit: BandwidthUnit;                                         // Bytes written per second unit
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
  apiDimensionObj: undefined,
  columns: () => [],
  indexOptions: () => [],
  pipelineTargetMappings: undefined
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * State Management
 */

// Current selected index (metric key)
const currIndex = ref(props.cvsKeys[0]);

// Chart data
const xData = ref<string[]>([]);                 // X-axis timestamp data
const seriesData = ref<SeriesData[]>([]);        // Y-axis series data

// Time range slider state
const sliderValue = ref<[number, number]>([0, 0]);  // Current slider range [start, end]
const sliderMin = ref<number>(0);                   // Minimum slider value
const sliderMax = ref<number>(0);                   // Maximum slider value
const isSliding = ref(false);                       // Whether user is actively sliding

// Selected metrics for overlay (format: "APIName-metricKey")
const checkboxGroupValue = ref<string[]>([]);

/**
 * Handle checkbox group change
 * Updates selected metrics and refreshes chart data
 * 
 * @param values - Array of selected metric identifiers
 */
const CheckboxGroupChange = (values: any): void => {
  const stringValues = values.map((v: any) => String(v));
  checkboxGroupValue.value = stringValues;
  setCharData(stringValues);
};

/**
 * Handle time range slider change
 * Updates chart to show data for selected time range
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

  sliderValue.value = rangeValue;

  // Update x-axis with sliced time range
  xData.value = props.timestampData.slice(rangeValue[0], rangeValue[1] + 1);
  setCharData(checkboxGroupValue.value);
};

/**
 * Build chart series data from selected metrics
 * Parses metric identifiers, extracts data, and builds series array
 * 
 * @param values - Array of selected metric identifiers (format: "APIName-metricKey")
 */
const setCharData = (values: string[]): void => {
  seriesData.value = [];
  
  for (let i = 0; i < values.length; i++) {
    // Parse metric identifier: "APIName-metricKey"
    const apiName = values[i].slice(0, values[i].lastIndexOf('-'));
    const cvsKey = values[i].slice(values[i].lastIndexOf('-')).slice(1);
    
    // Extract data for selected time range or full range
    const data = isSliding.value 
      ? props.indexDimensionObj[cvsKey][apiName].slice(sliderValue.value[0], sliderValue.value[1] + 1)
      : props.indexDimensionObj[cvsKey][apiName];
    
    // Add series with formatted name
    seriesData.value.push({ 
      name: `${apiName}-${allCvsNames[cvsKey]}`, 
      key: cvsKey, 
      data 
    });
  }
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
 * Metric selection options for different categories
 */

/**
 * Throughput metric options
 * Includes queries, transactions, and bandwidth metrics
 */
const throughputOptions = [
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.queriesPerSecond'),
    value: 'ops'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.transactionsPerSecond'),
    value: 'tps'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.downloadPerSecond'),
    value: 'brps'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.uploadPerSecond'),
    value: 'bwps'
  }
];

/**
 * Thread/concurrency metric options
 */
const threadOptions = [
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.threadOptions.threadCount'),
    value: 'threadPoolSize'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.threadOptions.maxThreadCount'),
    value: 'threadMaxPoolSize'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.threadOptions.activeThreadCount'),
    value: 'threadPoolActiveSize'
  }
];

/**
 * Response time metric options
 * Includes average, min, max, and percentiles (P50, P75, P90, P95, P99, P999)
 */
const responseTimeOptions = [
  {
    label: t('chart.average'),
    value: 'tranMean'
  },
  {
    label: t('chart.min'),
    value: 'tranMin'
  },
  {
    label: t('chart.max'),
    value: 'tranMax'
  },
  {
    label: t('chart.p50'),
    value: 'tranP50'
  },
  {
    label: t('chart.p75'),
    value: 'tranP75'
  },
  {
    label: t('chart.p90'),
    value: 'tranP90'
  },
  {
    label: t('chart.p95'),
    value: 'tranP95'
  },
  {
    label: t('chart.p99'),
    value: 'tranP99'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.responseTimeOptions.p999'),
    value: 'tranP999'
  }
];

/**
 * Error metric options
 */
const errorOptions = [
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.errorOptions.errorCount'),
    value: 'errors'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.errorOptions.errorRate'),
    value: 'errorRate'
  }
];

/**
 * Toggle section expansion states
 */
const throughputExpand = ref(true);      // Throughput section expanded
const threadExpand = ref(true);          // Thread section expanded
const responseTimeExpand = ref(true);    // Response time section expanded
const errorExpand = ref(true);           // Error section expanded

/**
 * Watch for timestamp data changes
 * Updates chart when new timestamp data arrives
 */
watch(() => props.timestampData, (newVal) => {
  // Reset data if no timestamps
  if (!newVal.length) {
    seriesData.value = [];
    xData.value = [];
    return;
  }

  // Update slider range
  sliderMax.value = newVal.length - 1;
  if (!isSliding.value) {
    sliderValue.value[1] = sliderMax.value;
  }

  // Auto-update to latest data if not manually sliding
  if (!isSliding.value) {
    xData.value = cloneDeep(newVal);
    
    // Initialize with default throughput metrics if none selected
    if (!checkboxGroupValue.value.length) {
      checkboxGroupValue.value = throughputOptions.map(item => 'Total-' + item.value);
    }
    
    setCharData(checkboxGroupValue.value);
  }
}, {
  immediate: true
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
  <!-- Main container for superimpose analysis -->
  <div class="w-full">
    <!-- Line chart for overlaying multiple metrics -->
    <LineChart
      ref="chartRef"
      :series="seriesData"
      :xData="xData"
      :selectedIndex="currIndex"
      :brpsUnit="brpsUnit"
      :bwpsUnit="bwpsUnit"
      tabKey="analyze" />
    
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
    
    <!-- Metric selection panel with collapsible sections -->
    <div class="pl-10 text-3 mt-5">
      <CheckboxGroup :value="checkboxGroupValue" @change="CheckboxGroupChange">
        <!-- Throughput metrics section -->
        <Toggle
          v-model:open="throughputExpand"
          :title="t('ftpPlugin.performanceTestDetail.superimposeAnalysis.toggleTitles.throughput')">
          <!-- Render checkboxes for each API's throughput metrics -->
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <!-- API name label (truncated) -->
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            
            <!-- Throughput metric checkboxes (ops, tps, brps, bwps) -->
            <div class="space-x-5">
              <Checkbox
                v-for="option in throughputOptions"
                :key="apiName + '-' + option.value"
                :value="apiName + '-' + option.value"
                :disabled="(!checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length >= 10) || 
                           (checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
        
        <!-- Thread/concurrency metrics section -->
        <Toggle
          v-model:open="threadExpand"
          :title="t('ftpPlugin.performanceTestDetail.superimposeAnalysis.toggleTitles.concurrency')"
          class="mt-2">
          <!-- Render checkboxes for each API's thread metrics -->
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            
            <!-- Thread metric checkboxes (pool size, max, active) -->
            <div class="space-x-5">
              <Checkbox
                v-for="option in threadOptions"
                :key="apiName + '-' + option.value"
                :value="apiName + '-' + option.value"
                :disabled="(!checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length >= 10) || 
                           (checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
        
        <!-- Response time metrics section -->
        <Toggle
          v-model:open="responseTimeExpand"
          :title="t('protocol.responseTime')"
          class="mt-2">
          <!-- Render checkboxes for each API's response time metrics -->
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            
            <!-- Response time metric checkboxes (avg, min, max, percentiles) -->
            <div class="space-x-5">
              <Checkbox
                v-for="option in responseTimeOptions"
                :key="apiName + '-' + option.value"
                :value="apiName + '-' + option.value"
                :disabled="(!checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length >= 10) || 
                           (checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
        
        <!-- Error metrics section -->
        <Toggle
          v-model:open="errorExpand"
          :title="t('ftpPlugin.performanceTestDetail.superimposeAnalysis.toggleTitles.error')"
          class="mt-2">
          <!-- Render checkboxes for each API's error metrics -->
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            
            <!-- Error metric checkboxes (error count, error rate) -->
            <div class="space-x-5">
              <Checkbox
                v-for="option in errorOptions"
                :key="apiName + '-' + option.value"
                :value="apiName + '-' + option.value"
                :disabled="(!checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length >= 10) || 
                           (checkboxGroupValue.includes(apiName + '-' + option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
      </CheckboxGroup>
    </div>
  </div>
</template>

<style scoped>
/**
 * Toggle component styles
 */

/* Toggle title text size */
:deep(.toggle-title) {
  @apply text-3;
}

/* Toggle content container padding */
:deep(.toggle-main-container) {
  @apply px-8 pt-2;
}

/**
 * Slider styles for time range selection
 */

/* Slider rail (base track) */
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
