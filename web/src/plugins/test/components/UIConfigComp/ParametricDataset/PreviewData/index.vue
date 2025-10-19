<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Hints, Table, Icon, NoData, Spin, Input } from '@xcan-angus/vue-ui';
import { utils, duration, ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { dataset } from '@/api/tester';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Table row data type
 * Dynamic object with string keys for parameter values, plus an ID
 */
type TableData = {
  [key: string]: string;
} & { id: string; }

/**
 * Extraction configuration interface
 */
interface ExtractionConfig {
  defaultValue: string;
  expression: string;
  failureMessage: string;
  finalValue: string;
  matchItem: string;
  method: ExtractionMethod;
  name: string;
  source: ExtractionSource;
  value: string;
  fileType: ExtractionFileType;
  path: string;
  encoding: Encoding;
  quoteChar: string;
  escapeChar: string;
  separatorChar: string;
  rowIndex: string;
  columnIndex: string;
  select: string;
  parameterName: string;
  datasource: {
    type: string;
    username: string;
    password: string;
    jdbcUrl: string;
  };
}

/**
 * Component props interface
 */
type Props = {
  dataSource: {
    id: string;                           // Dataset unique identifier
    projectId: string;                    // Project identifier
    extracted: boolean;                   // Whether extraction is enabled
    name: string;                         // Dataset name
    extraction: ExtractionConfig;         // Extraction configuration
    parameters: {                         // Parameter definitions
      name: string;
      value: string;
    }[];
  };
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

/**
 * State Management
 */
const rowNum = ref<string>('20');       // Number of rows to preview (default: 20)

/**
 * Pagination configuration
 */
const pagination = ref<{ 
  current: number; 
  pageSize: number; 
  total: number; 
  showSizeChanger: false; 
}>({ 
  current: 1,             // Current page number
  pageSize: 10,           // Items per page
  total: 0,               // Total items count
  showSizeChanger: false  // Hide page size changer
});

const loading = ref(false);             // Loading state during API call
const loaded = ref(false);              // Whether data has been loaded at least once
const errorMessage = ref<string>();     // Error message if preview fails

/**
 * Dynamic table columns (generated from parameter names)
 */
const columns = ref<{
  title: string;
  dataIndex: string;
  ellipsis: true;
}[]>([]);

/**
 * Preview data rows
 */
const dataList = ref<TableData[]>([]);

/**
 * Handle row number input change (debounced)
 * Updates preview row count and reloads data
 * 
 * @param event - Input change event
 */
const inputChange = debounce(duration.search, (event: any) => {
  const value = event.target?.value;
  rowNum.value = value || '20';  // Default to 20 if empty
  loadData();
});

/**
 * Refresh preview data
 * Prevents double-loading if already loading
 */
const refresh = (): void => {
  if (loading.value) {
    return;
  }

  loadData();
};

/**
 * Load dataset preview data from API
 * Fetches data based on extraction configuration and row count,
 * then dynamically generates table columns and rows
 */
const loadData = async (): Promise<void> => {
  // Early return if no data source or already loading
  if (!props.dataSource || loading.value) {
    return;
  }

  // Build request parameters
  const params = {
    ...props.dataSource,
    rowNum: rowNum.value
  };

  // Fetch preview data
  loading.value = true;
  const [error, res] = await dataset.previewDataSetValue(params, { silence: true });
  loading.value = false;
  loaded.value = true;
  
  // Reset state
  columns.value = [];
  dataList.value = [];
  pagination.value.total = 0;

  // Handle error
  if (error) {
    errorMessage.value = error.message;
    return;
  }

  errorMessage.value = undefined;

  // Process response data
  const data = res?.data;
  if (data) {
    const entries = Object.entries(data);
    
    // Iterate through each parameter (column)
    entries.every(([key, value]) => {
      // Create column definition
      columns.value.push({ 
        dataIndex: key, 
        title: key, 
        ellipsis: true 
      });
      
      // Process parameter values (rows)
      if (Array.isArray(value)) {
        const newValue = value as string[];
        newValue.every((item, index) => {
          // Add value to existing row or create new row
          if (dataList.value[index]) {
            dataList.value[index][key] = item;
          } else {
            dataList.value[index] = {
              id: utils.uuid(),
              [key]: item
            };
          }

          return true;
        });
      }
      return true;
    });

    // Update total count for pagination
    pagination.value.total = dataList.value.length;
  }
};

/**
 * Reset component state to initial values
 * Clears all data, errors, and resets configuration
 */
const reset = (): void => {
  rowNum.value = '20';
  pagination.value.total = 0;
  loading.value = false;
  loaded.value = false;
  errorMessage.value = undefined;
  columns.value = [];
  dataList.value = [];
};

/**
 * Component mount lifecycle hook
 * Sets up watcher to reload data when dataSource changes
 */
onMounted(() => {
  /**
   * Watch for dataSource prop changes
   * Resets state and loads preview data when new source is provided
   */
  watch(() => props.dataSource, (newValue) => {
    reset();

    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

/**
 * Computed no-data text
 * Shows error message if exists, otherwise shows default no-data text
 */
const noDataText = computed(() => {
  return errorMessage.value ? errorMessage.value : t('common.noData');
});
</script>

<template>
  <!-- Loading spinner wrapper -->
  <Spin :spinning="loading" class="text-3 leading-5">
    <!-- Header: description hint and controls -->
    <div class="flex items-center justify-between transform-gpu -translate-y-1">
      <!-- Description hint label -->
      <Hints :text="t('common.description')" />

      <!-- Controls: row count input and refresh button -->
      <div class="flex items-center flex-nowrap space-x-2.5">
        <!-- Row count control -->
        <div class="flex items-center">
          <!-- Label -->
          <div class="flex-shrink-0 mr-2">
            {{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.previewDataModal.previewRows') }}
          </div>
          
          <!-- Row count input (1-10000, integer only) -->
          <Input
            v-model:value="rowNum"
            :placeholder="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.previewDataModal.previewRowsPlaceholder')"
            :maxlength="5"
            :min="1"
            :max="10000"
            dataType="integer"
            class="w-25"
            @change="inputChange" />
        </div>

        <!-- Refresh button -->
        <Button
          :disabled="loading"
          size="small"
          type="link"
          class="px-0 h-5 leading-5 border-0 text-theme-content text-theme-text-hover"
          @click="refresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span class="ml-1">{{ t('actions.refresh') }}</span>
        </Button>
      </div>
    </div>

    <!-- Content (shown after initial load) -->
    <template v-if="loaded">
      <!-- No data placeholder (shown when no data or error) -->
      <NoData
        v-if="!dataList.length"
        size="small"
        class="mt-5 mb-10"
        :class="{ 'no-data-error-text': !!errorMessage }"
        :text="noDataText" />

      <!-- Preview data table (dynamic columns and rows) -->
      <Table
        v-else
        :dataSource="dataList"
        :columns="columns as any"
        :pagination="pagination"
        rowKey="id"
        class="flex-1 mb-3.5"
        noDataSize="small"
        noDataText="" />
    </template>
  </Spin>
</template>

<style scoped>
.no-data-error-text>:deep(div) {
  max-height: 10em;
  margin-right: 50px;
  margin-left: 50px;
  overflow-y: auto;
  color: rgba(245, 34, 45, 100%);
  line-height: 18px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
