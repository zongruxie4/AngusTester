<script setup lang="ts">
// Vue core imports
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Button } from 'ant-design-vue';
import { Hints, Icon, Input, NoData, Spin, Table } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { utils, duration, ExtractionSource, ExtractionMethod, ExtractionFileType, Encoding } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

// API imports
import { dataset } from '@/api/tester';

const { t } = useI18n();

/**
 * Table data interface for preview
 */
type TableData = {
  [key: string]: string;
} & { id: string; }

/**
 * Component props interface for preview data
 */
type Props = {
  dataSource: {
    id: string;
    projectId: string;
    extracted: boolean;
    name: string;
    extraction: {
      defaultValue: string;
      expression: string;
      failureMessage: string;
      finalValue: string;
      matchItem: string;
      method: ExtractionSource;
      name: string;
      source: ExtractionMethod;
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
    };
    parameters: {
      name: string;
      value: string;
    }[];
  };
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

// Component state
const previewRowCount = ref<string>('20');
const paginationConfig = ref<{ current: number; pageSize: number; total: number; showSizeChanger: false; }>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const isLoading = ref(false);
const isDataLoaded = ref(false);
const errorMessage = ref<string>();
const tableColumns = ref<{
  title: string;
  dataIndex: string;
  ellipsis: true
}[]>([]);
const previewDataList = ref<TableData[]>([]);

/**
 * Handle row count input change with debounce
 * @param event - Input change event
 */
const handleRowCountInputChange = debounce(duration.search, (event: any) => {
  const value = event.target.value;
  previewRowCount.value = value || '20';

  loadPreviewData();
});

/**
 * Handle refresh button click
 */
const handleRefresh = () => {
  if (isLoading.value) {
    return;
  }

  loadPreviewData();
};

/**
 * Load preview data from API
 */
const loadPreviewData = async () => {
  if (!props.dataSource || isLoading.value) {
    return;
  }

  const requestParams = {
    ...props.dataSource,
    rowNum: previewRowCount.value
  };

  isLoading.value = true;
  const [error, response] = await dataset.previewDataSetValue(requestParams, { silence: true });
  isLoading.value = false;
  isDataLoaded.value = true;
  tableColumns.value = [];
  previewDataList.value = [];
  paginationConfig.value.total = 0;

  if (error) {
    errorMessage.value = error.message;
    return;
  }

  errorMessage.value = undefined;

  const responseData = response?.data;
  if (responseData) {
    const dataEntries = Object.entries(responseData);
    dataEntries.every(([key, value]) => {
      tableColumns.value.push({ dataIndex: key, title: key, ellipsis: true });
      if (Array.isArray(value)) {
        const arrayValue = value as string[];
        arrayValue.every((item, index) => {
          if (previewDataList.value[index]) {
            previewDataList.value[index][key] = item;
          } else {
            previewDataList.value[index] = {
              id: utils.uuid(),
              [key]: item
            };
          }

          return true;
        });
      }
      return true;
    });

    paginationConfig.value.total = previewDataList.value.length;
  }
};

/**
 * Reset component state
 */
const resetComponentState = () => {
  previewRowCount.value = '20';
  paginationConfig.value.total = 0;
  isLoading.value = false;
  isDataLoaded.value = false;
  errorMessage.value = undefined;
  tableColumns.value = [];
  previewDataList.value = [];
};

// Component initialization and watchers
onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    resetComponentState();

    if (!newValue) {
      return;
    }

    loadPreviewData();
  }, { immediate: true });
});

// Computed properties
const noDataText = computed(() => {
  return errorMessage.value ? errorMessage.value : t('common.noData');
});
</script>

<template>
  <Spin :spinning="isLoading" class="text-3 leading-5">
    <div class="flex items-center justify-between transform-gpu -translate-y-1">
      <Hints :text="t('commonComp.apis.parameterizationDataset.previewData.hintText')" />

      <div class="flex items-center flex-nowrap space-x-2.5">
        <div class="flex items-center">
          <div class="flex-shrink-0 mr-2">{{ t('commonComp.apis.parameterizationDataset.previewData.rowNum') }}</div>
          <Input
            v-model:value="previewRowCount"
            placeholder="1 ~ 10000"
            :maxlength="5"
            :min="1"
            :max="10000"
            dataType="integer"
            class="w-25"
            @change="handleRowCountInputChange" />
        </div>

        <Button
          :disabled="isLoading"
          size="small"
          type="link"
          class="px-0 h-5 leading-5 border-0 text-theme-content text-theme-text-hover"
          @click="handleRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span class="ml-1">{{ t('actions.refresh') }}</span>
        </Button>
      </div>
    </div>

    <template v-if="isDataLoaded">
      <NoData
        v-if="!previewDataList.length"
        size="small"
        class="mt-5 mb-10"
        :class="{ 'no-data-error-text': !!errorMessage }"
        :text="noDataText" />

      <Table
        v-else
        :dataSource="previewDataList"
        :columns="tableColumns"
        :pagination="paginationConfig"
        :noDataSize="'small'"
        :noDataText="t('common.noData')"
        rowKey="id"
        class="flex-1 mb-3.5" />
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
