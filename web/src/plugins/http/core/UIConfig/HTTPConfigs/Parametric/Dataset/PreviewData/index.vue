<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Hints, Table, Icon, NoData, Spin, Input } from '@xcan-angus/vue-ui';
import { utils, duration, ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { dataSet } from '@/api/tester';

const { t } = useI18n();

type TableData = {
  [key: string]: string;
} & { id: string; }

type Props = {
  dataSource: {
    id: string;
    projectId: string;
    extracted: boolean;
    name: string;
    extraction: { // TODO 可以复用定义
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
    };
    parameters: {
      name: string;
      value: string;
    }[];
  };
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const rowNum = ref<string>('20');

const pagination = ref<{ current: number; pageSize: number; total: number; showSizeChanger: false; }>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });

const loading = ref(false);
const loaded = ref(false);
const errorMessage = ref<string>();
const columns = ref<{
  title: string;
  dataIndex: string;
  ellipsis: true
}[]>([]);
const dataList = ref<TableData[]>([]);

const inputChange = debounce(duration.search, (event: { target: { value: string; } }) => {
  const value = event.target.value;
  rowNum.value = value || '20';

  loadData();
});

const refresh = () => {
  if (loading.value) {
    return;
  }

  loadData();
};

const loadData = async () => {
  if (!props.dataSource || loading.value) {
    return;
  }

  const params = {
    ...props.dataSource,
    rowNum: rowNum.value
  };

  loading.value = true;
  const [error, res] = await dataSet.previewDataSetValue(params, { silence: true });
  loading.value = false;
  loaded.value = true;
  columns.value = [];
  dataList.value = [];
  pagination.value.total = 0;

  if (error) {
    errorMessage.value = error.message;
    return;
  }

  errorMessage.value = undefined;

  const data = res?.data;
  if (data) {
    const entries = Object.entries(data);
    entries.every(([key, value]) => {
      columns.value.push({ dataIndex: key, title: key, ellipsis: true });
      if (Array.isArray(value)) {
        const newValue = value as string[];
        newValue.every((item, index) => {
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

    pagination.value.total = dataList.value.length;
  }
};

const reset = () => {
  rowNum.value = '20';
  pagination.value.total = 0;
  loading.value = false;
  loaded.value = false;
  errorMessage.value = undefined;
  columns.value = [];
  dataList.value = [];
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    reset();

    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const noDataText = computed(() => {
  return errorMessage.value ? errorMessage.value : t('httPlugin.uiConfig.httpConfigs.parametric.dataset.previewDataModal.noData');
});
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center justify-between transform-gpu -translate-y-1">
      <Hints :text="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.previewDataModal.description')" />

      <div class="flex items-center flex-nowrap space-x-2.5">
        <div class="flex items-center">
          <div class="flex-shrink-0 mr-2">{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.previewDataModal.previewRows') }}</div>
          <Input
            v-model:value="rowNum"
            :placeholder="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.previewDataModal.previewRowsPlaceholder')"
            :maxlength="5"
            :min="1"
            :max="10000"
            dataType="integer"
            class="w-25"
            @change="inputChange" />
        </div>

        <Button
          :disabled="loading"
          size="small"
          type="link"
          class="px-0 h-5 leading-5 border-0 text-theme-content text-theme-text-hover"
          @click="refresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span class="ml-1">{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.previewDataModal.refresh') }}</span>
        </Button>
      </div>
    </div>

    <template v-if="loaded">
      <NoData
        v-if="!dataList.length"
        size="small"
        class="mt-5 mb-10"
        :class="{ 'no-data-error-text': !!errorMessage }"
        :text="noDataText" />

      <Table
        v-else
        :dataSource="dataList"
        :columns="columns"
        :pagination="pagination"
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
