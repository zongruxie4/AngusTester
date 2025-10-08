<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Hints, Icon, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { BaseProps } from '@/types/types';

import { useVariableUsage } from './composables/useVariableUsage';

const { t } = useI18n();

const props = withDefaults(defineProps<BaseProps>(), {
  id: undefined
});

// Use the variable usage composable for list logic
const {
  columns,

  // State
  loading,
  loaded,
  pagination,
  rowSelection,
  dataList,
  selectedNum,

  // Methods
  toDelete,
  toBatchDelete,
  toCancelBatchDelete,
  refresh
} = useVariableUsage(props);
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <!-- Header with title and action buttons -->
    <div class="flex items-center justify-between mb-2">
      <Hints :text="t('variable.detail.useList.title')" />

      <!-- Action buttons when not in batch delete mode -->
      <div v-if="!rowSelection" class="flex items-center space-x-2.5">
        <Button
          v-if="dataList.length"
          :disabled="loading"
          size="small"
          type="text"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="toBatchDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span class="ml-1">{{ t('dataCommon.searchPanel.batchActions.batchDelete') }}</span>
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

      <!-- Action buttons when in batch delete mode -->
      <div v-else class="flex items-center space-x-2.5">
        <Button
          danger
          type="text"
          size="small"
          class="flex items-center px-0 h-5 leading-5 border-0"
          @click="toBatchDelete">
          <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
          <div class="flex items-center">
            <span class="mr-0.5">{{ t('dataCommon.searchPanel.batchActions.deleteSelected') }}</span>
            <span>({{ selectedNum }})</span>
          </div>
        </Button>

        <Button
          type="text"
          size="small"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="toCancelBatchDelete">
          <Icon icon="icon-fanhui" class="mr-1" />
          <span>{{ t('dataCommon.searchPanel.batchActions.cancelDelete') }}</span>
        </Button>
      </div>
    </div>

    <!-- Main content area -->
    <template v-if="loaded">
      <!-- No data state -->
      <NoData
        v-if="!dataList.length"
        size="small"
        style="min-height: 316px;" />

      <!-- Data table -->
      <Table
        v-else
        rowKey="targetId"
        class="flex-1"
        :dataSource="dataList"
        :columns="columns"
        :pagination="pagination"
        :rowSelection="rowSelection">
        <template #bodyCell="{ column, record }">
          <!-- Target type column -->
          <div v-if="column.dataIndex === 'targetType'" class="truncate">
            {{ record.targetType?.message }}
          </div>

          <!-- Action column -->
          <Button
            v-else-if="column.dataIndex === 'action'"
            :disabled="!!rowSelection?.selectedRowKeys?.length"
            type="text"
            size="small"
            class="flex items-center px-0"
            @click="toDelete(record)">
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
