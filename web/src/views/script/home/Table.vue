<script setup lang="ts">
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, notification, Table, Tooltip } from '@xcan-angus/vue-ui';
import { TESTER, PageQuery } from '@xcan-angus/infra';
import { script } from '@/api/tester';
import { ScriptInfo, ScriptTableProps } from '@/views/script/types';
import { ScriptPermission } from '@/enums/enums';
import { useScriptTable } from './composables/useScriptTable';

const props = withDefaults(defineProps<ScriptTableProps>(), {
  projectId: undefined,
  appId: undefined,
  userId: undefined,
  dataSource: () => [],
  permissionsMap: () => ({}),
  pagination: () => ({ current: 1, pageSize: 10, total: 0 }),
  allowImportSamplesFlag: false,
  loaded: false,
  loading: false,
  resetSelectedIdsNotify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'refresh'): void;
  (event: 'delete', value:string[]): void;
  (event: 'tableChange', pagination: { current: number; pageSize: number }, sorter: { orderBy: string; orderSort: PageQuery.OrderSort }): void;
}>();

// Async components
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/ExportScriptModal.vue'));

// I18n
const { t } = useI18n();

// Composable
const {
  columns,
  actionItems,
  selectedDataMap,
  batchExecDisabled,
  batchDeleteDisabled,
  batchExportDisabled,
  rowSelection,
  cancelBatchOperation,
  handleBatchExec,
  handleBatchDelete,
  handleBatchExport,
  handleSingleExec,
  handleToEditor,
  handleActionClick,
  selectedIds
} = useScriptTable(props.permissionsMap);

// Export state
const exportIds = ref<string[]>([]);
const selectedData = ref<ScriptInfo>();
const authModalVisible = ref<boolean>(false);
const exportVisible = ref<boolean>(false);

/**
 * Import demo scripts
 */
const importExamples = async () => {
  emit('update:loading', true);
  const [error] = await script.importScriptDemo();
  emit('update:loading', false);

  if (error) {
    return;
  }

  notification.success(t('scriptHome.table.messages.importExampleSuccess'));
  emit('refresh');
};

/**
 * Handle table change events
 */
const tableChange = (
  pagination: { current: number; pageSize: number; },
  _filters: { [key: string]: any }[],
  sorter: { orderBy: string; orderSort: PageQuery.OrderSort }) => {
  emit('tableChange', pagination, sorter);
};

/**
 * Handle auth flag change
 */
const authFlagChange = () => {
  // This would update the table data in the parent component
  emit('refresh');
};

/**
 * Handle batch execution
 */
const handleBatchExecClick = () => {
  handleBatchExec((loading) => emit('update:loading', loading));
};

/**
 * Handle batch delete
 */
const handleBatchDeleteClick = () => {
  handleBatchDelete(
    (loading) => emit('update:loading', loading),
    (ids) => emit('delete', ids)
  );
};

/**
 * Handle batch export
 */
const handleBatchExportClick = () => {
  handleBatchExport((ids: string[], visible: boolean) => {
    exportIds.value = ids;
    exportVisible.value = visible;
  });
};

/**
 * Handle single execution
 */
const handleSingleExecClick = (record: ScriptInfo) => {
  handleSingleExec(record, (loading) => emit('update:loading', loading));
};

/**
 * Handle edit click
 */
const handleEditClick = (record: ScriptInfo) => {
  handleToEditor(record, props.pagination);
};

/**
 * Handle dropdown action click
 */
const handleDropdownActionClick = (e: any, record: ScriptInfo) => {
  handleActionClick(
    e.key,
    record,
    (loading) => emit('update:loading', loading),
    () => emit('refresh'),
    (ids) => emit('delete', ids),
    (ids: string[], visible: boolean) => {
      exportIds.value = ids;
      exportVisible.value = visible;
    }
  );
};

// Update rowSelection onChange handler
rowSelection.value.onChange = (keys: string[]) => {
  // Handle table select directly since we can't import the function
  const deleteIds = props.dataSource.reduce((prev, cur) => {
    const id = cur.id;
    if (!keys.includes(id)) {
      prev.push(cur.id);
      delete selectedDataMap.value[id];
    } else {
      selectedDataMap.value[id] = {
        id,
        name: cur.name
      };
    }
    return prev;
  }, [] as string[]);

  // Remove deselected scripts
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

  for (let i = 0, len = keys.length; i < len; i++) {
    if (!selectedRowKeys.includes(keys[i])) {
      selectedRowKeys.push(keys[i]);
    }
  }

  const selectedNum = selectedRowKeys.length;
  if (selectedNum > 200) {
    notification.info(t('scriptHome.table.messages.maxBatchLimit', { maxNum: 200, selectedNum }));
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

// Watch for reset notifications
watch(() => props.resetSelectedIdsNotify, (newValue) => {
  if (!newValue) {
    return;
  }

  rowSelection.value.selectedRowKeys = [];
  selectedDataMap.value = {};
}, { immediate: true });
</script>

<template>
  <template v-if="props.loaded">
    <div
      v-if="props.allowImportSamplesFlag && !props.dataSource.length"
      class="flex-1 flex flex-col items-center justify-center">
      <img class="w-27.5" src="../../../assets/images/nodata.png">
      <div class="flex items-center text-theme-sub-content text-3 leading-5">
        <span>{{ t('scriptHome.table.messages.noScriptData') }}</span>
        <Button
          type="link"
          size="small"
          class="px-0"
          @click="importExamples">
          {{ t('actions.importExamples') }}
        </Button>
      </div>
    </div>

    <div v-else>
      <div
        :visible="!!selectedIds.length"
        class="btn-group-container flex items-center transition-all duration-300 space-x-2.5">
        <Button
          :disabled="batchExecDisabled"
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="handleBatchExecClick">
          {{ t('actions.execute') }}
        </Button>

        <Button
          :disabled="batchDeleteDisabled"
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="handleBatchDeleteClick">
          {{ t('actions.delete') }}
        </Button>

        <Button
          :disabled="batchExportDisabled"
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="handleBatchExportClick">
          {{ t('actions.export') }}
        </Button>

        <Button
          danger
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="cancelBatchOperation">
          <span>{{ t('actions.cancelBatch') }}</span>
          <span class="ml-1">({{ selectedIds.length }})</span>
        </Button>
      </div>

      <Table
        storageKey="script"
        :dataSource="props.dataSource"
        :columns="columns"
        :pagination="props.pagination"
        :rowSelection="rowSelection"
        rowKey="id"
        class="flex-1"
        noDataText=""
        noDataSize="small"
        @change="tableChange">
        <template #bodyCell="{ column, text, record }">
          <RouterLink
            v-if="column.dataIndex === 'name'"
            :to="record.nameLinkUrl"
            :title="record.name"
            class="block truncate text-text-link">
            {{ record.name }}
          </RouterLink>

          <div
            v-else-if="column.dataIndex === 'plugin'"
            :title="record.sourceId"
            class="truncate">
            {{ text || '--' }}
          </div>

          <div
            v-else-if="column.dataIndex === 'type'"
            :title="record.sourceId"
            class="truncate">
            {{ record.type?.message || '--' }}
          </div>

          <div
            v-else-if="column.dataIndex === 'source'"
            :title="record.sourceId"
            class="truncate">
            {{ record.source?.message || '--' }}
          </div>

          <div v-else-if="column.dataIndex === 'tags'">
            <template v-if="text?.length">
              <Tag
                v-for="(item, index) in text"
                :key="index"
                class="mr-1 mb-1 h-6 leading-6 text-3 rounded-full text-text-content">
                <template v-if="item.length <= 15">
                  {{ item }}
                </template>

                <template v-else>
                  <Tooltip :title="item" placement="bottomLeft">
                    {{ item.slice(0, 15) }}...
                  </Tooltip>
                </template>
              </Tag>
            </template>

            <span v-else>--</span>
          </div>

          <div
            v-else-if="column.dataIndex === 'sourceId'"
            :title="record.sourceId"
            class="truncate">
            {{ record.sourceId || '--' }}
          </div>

          <template v-else-if="column.dataIndex === 'sourceName'">
            <RouterLink
              v-if="record.sourceNameLinkUrl"
              :to="record.sourceNameLinkUrl"
              :title="record.sourceName"
              target="_blank"
              class="block truncate text-text-link">
              {{ record.sourceName }}
            </RouterLink>

            <div
              v-else
              :title="record.sourceName"
              class="truncate">
              {{ record.sourceName || '--' }}
            </div>
          </template>

          <div v-else-if="column.dataIndex === 'action'" class="flex items-center">
            <Button
              :disabled="!props.permissionsMap[record.id]?.includes(ScriptPermission.TEST)"
              type="text"
              size="small"
              class="flex items-center px-0 mr-2"
              @click="() => handleSingleExecClick(record)">
              <Icon icon="icon-zhihangjiaoben" class="mr-1" />
              <span>{{ t('actions.execute') }}</span>
            </Button>

            <Button
              :disabled="!props.permissionsMap[record.id]?.includes(ScriptPermission.MODIFY)"
              type="text"
              size="small"
              class="flex items-center px-0 mr-2"
              @click="() => handleEditClick(record)">
              <Icon icon="icon-shuxie" class="mr-1" />
              <span>{{ t('actions.edit') }}</span>
            </Button>

            <Dropdown
              :menuItems="actionItems"
              :admin="false"
              :permissions="props.permissionsMap[record.id]"
              @click="(e) => handleDropdownActionClick(e, record)">
              <Button
                type="text"
                size="small"
                class="flex items-center px-0">
                <Icon icon="icon-gengduo" />
              </Button>
            </Dropdown>
          </div>
        </template>
      </Table>
    </div>
  </template>

  <AsyncComponent :visible="authModalVisible">
    <AuthorizeModal
      v-model:visible="authModalVisible"
      enumKey="ScriptPermission"
      :appId="props.appId"
      :listUrl="`${TESTER}/script/auth?scriptId=${selectedData?.id}`"
      :delUrl="`${TESTER}/script/auth`"
      :addUrl="`${TESTER}/script/${selectedData?.id}/auth`"
      :updateUrl="`${TESTER}/script/auth`"
      :enabledUrl="`${TESTER}/script/${selectedData?.id}/auth/enabled`"
      :onTips="t('scriptHome.table.tips.authOn')"
      :offTips="t('scriptHome.table.tips.authOff')"
      :title="t('scriptHome.table.tips.authTitle')"
      @change="authFlagChange" />
  </AsyncComponent>

  <AsyncComponent :visible="exportVisible">
    <ExportScriptModal
      v-model:visible="exportVisible"
      :ids="exportIds" />
  </AsyncComponent>
</template>

<style scoped>
.btn-group-container {
  height: 0;
  overflow: hidden;
  opacity: 0;
}

.btn-group-container[visible="true"] {
  height: 32px;
  opacity: 1;
}
</style>
