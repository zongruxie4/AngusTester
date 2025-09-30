<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, watch } from 'vue';
import { AsyncComponent, Spin } from '@xcan-angus/vue-ui';
import { BasicProps } from '@/types/types';
import { ExtractionSource } from '@xcan-angus/infra';

import { useVariableDetail } from './composables/useVariableDetail';

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  visible: false,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const refreshList = inject<() => void>('refreshList', () => ({}));

const StaticVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/StaticVariable.vue'));
const FileVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/FileVariable.vue'));
const HttpVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/HttpVariable.vue'));
const JdbcVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/JdbcVariable.vue'));
const ExportVariables = defineAsyncComponent(() => import('@/views/data/variable/export/index.vue'));

// Use the variable detail composable for business logic
const {
  // State
  loading,
  loaded,
  dataSource,
  exportModalVisible,
  exportId,

  // Methods
  loadData,
  handleOk,
  handleDelete,
  handleExport,
  handleClone,
  handleCopyLink,
  handleRefresh,
  handleCancel
} = useVariableDetail(props, {
  updateTabPane,
  deleteTabPane,
  replaceTabPane,
  refreshList
});

const source = computed(() => {
  const data = props.data;
  if (!data) {
    return;
  }

  const { id, source } = data;
  if (id) {
    if (loaded.value) {
      const data = dataSource.value;
      if (data) {
        const extraction = data.extraction;
        if (!extraction) {
          return ExtractionSource.VALUE;
        }
        return extraction.source;
      }
      return undefined;
    }
    return undefined;
  }
  return source;
});

onMounted(() => {
  watch(() => props.data, (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }
    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }
    loadData(id);
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="max-w-242.5">
      <AsyncComponent :visible="source === ExtractionSource.VALUE">
        <StaticVariable
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="handleOk"
          @cancel="handleCancel"
          @delete="handleDelete"
          @export="handleExport"
          @clone="handleClone"
          @copyLink="handleCopyLink"
          @refresh="handleRefresh" />
      </AsyncComponent>

      <AsyncComponent :visible="source === ExtractionSource.FILE">
        <FileVariable
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="handleOk"
          @cancel="handleCancel"
          @delete="handleDelete"
          @export="handleExport"
          @clone="handleClone"
          @copyLink="handleCopyLink"
          @refresh="handleRefresh" />
      </AsyncComponent>

      <AsyncComponent :visible="source === ExtractionSource.HTTP">
        <HttpVariable
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="handleOk"
          @cancel="handleCancel"
          @delete="handleDelete"
          @export="handleExport"
          @clone="handleClone"
          @copyLink="handleCopyLink"
          @refresh="handleRefresh" />
      </AsyncComponent>

      <AsyncComponent :visible="source === ExtractionSource.JDBC">
        <JdbcVariable
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="handleOk"
          @cancel="handleCancel"
          @delete="handleDelete"
          @export="handleExport"
          @clone="handleClone"
          @copyLink="handleCopyLink"
          @refresh="handleRefresh" />
      </AsyncComponent>
    </div>
  </Spin>

  <AsyncComponent :visible="exportModalVisible">
    <ExportVariables
      :id="exportId"
      v-model:visible="exportModalVisible"
      :projectId="props.projectId" />
  </AsyncComponent>
</template>
