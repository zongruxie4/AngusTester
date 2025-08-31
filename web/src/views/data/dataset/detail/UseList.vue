<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { useDatasetUsage } from './composables/useDatasetUsage';

const { t } = useI18n();

type Props = {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

// Use the composable for dataset usage logic
const {
  // State
  loading,
  loaded,
  pagination,
  rowSelection,
  dataList,

  // Computed properties
  selectedNum,
  columns,

  // Methods
  deleteUsage,
  startBatchDelete,
  cancelBatchDelete,
  refresh
} = useDatasetUsage(props);
</script>
<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center justify-between mb-2">
      <Hints :text="t('dataset.detail.useList.hints')" />
      <div v-if="!rowSelection" class="flex items-center space-x-2.5">
        <Button
          v-if="dataList.length"
          :disabled="loading"
          size="small"
          type="text"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="startBatchDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span class="ml-1">{{ t('dataset.detail.useList.buttons.batchDelete') }}</span>
        </Button>

        <Button
          :disabled="loading"
          size="small"
          type="text"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="refresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span class="ml-1">{{ t('actions.refresh') }}</span>
        </Button>
      </div>

      <div v-else class="flex items-center space-x-2.5">
        <Button
          danger
          type="text"
          size="small"
          class="flex items-center px-0 h-5 leading-5 border-0"
          @click="startBatchDelete">
          <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
          <div class="flex items-center">
            <span class="mr-0.5">{{ t('dataset.detail.useList.buttons.deleteSelected') }}</span>
            <span>({{ selectedNum }})</span>
          </div>
        </Button>

        <Button
          type="text"
          size="small"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="cancelBatchDelete">
          <Icon icon="icon-fanhui" class="mr-1" />
          <span>{{ t('dataset.detail.useList.buttons.cancelDelete') }}</span>
        </Button>
      </div>
    </div>

    <template v-if="loaded">
      <NoData
        v-if="!dataList.length"
        size="small"
        style="min-height: 316px;" />

      <Table
        v-else
        rowKey="targetId"
        class="flex-1"
        :dataSource="dataList"
        :columns="columns"
        :pagination="pagination"
        :rowSelection="rowSelection">
        <template #bodyCell="{ column, record }">
          <div v-if="column.dataIndex === 'targetType'" class="truncate">
            {{ record.targetType?.message }}
          </div>

          <Button
            v-else-if="column.dataIndex === 'action'"
            :disabled="!!rowSelection?.selectedRowKeys?.length"
            type="text"
            size="small"
            class="flex items-center px-0"
            @click="deleteUsage(record)">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>{{ t('actions.delete') }}</span>
          </Button>
        </template>
      </Table>
    </template>
  </Spin>
</template>
<style scoped>
:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
