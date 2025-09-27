<script setup lang="ts">
import { Button } from 'ant-design-vue';
import { Hints, Icon, Input, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { usePreviewData } from './composables/usePreviewData';
import { PreviewDataSource } from '../types';

const { t } = useI18n();

// Define component props with explicit typing
interface Props {
  dataSource?: PreviewDataSource;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

// Use the composable for preview data logic
const {
  // State
  rowNum,
  pagination,
  loading,
  loaded,
  errorMessage,
  columns,
  dataList,

  // Computed properties
  noDataText,

  // Methods
  handleRowNumChange,
  refresh
} = usePreviewData(props);

// Convert event to string value for input change handler
const onInputChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  handleRowNumChange({ target: { value: target.value } });
};
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center justify-between mb-1 transform-gpu -translate-y-1">
      <Hints :text="t('dataset.preview.hints')" />

      <div class="flex items-center flex-nowrap space-x-2.5">
        <div class="flex items-center">
          <div class="flex-shrink-0 mr-2">{{ t('dataset.preview.previewRows') }}</div>
          <Input
            v-model:value="rowNum"
            :placeholder="t('dataset.preview.placeholder')"
            :maxlength="5"
            :min="1"
            :max="10000"
            dataType="integer"
            class="w-25"
            @change="onInputChange" />
        </div>

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

    <template v-if="loaded">
      <NoData
        v-if="!dataList.length"
        size="small"
        style="min-height: 316px;"
        :class="{ 'no-data-error-text': !!errorMessage }"
        :text="noDataText" />

      <Table
        v-else
        :dataSource="dataList"
        :columns="columns"
        :pagination="pagination"
        rowKey="id"
        class="flex-1"
        noDataSize="small"
        :noDataText="noDataText" />
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
