<script setup lang="ts">
import { defineAsyncComponent, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button, PaginationProps, Tag } from 'ant-design-vue';
import { AsyncComponent, AuthorizeModal, Dropdown, Icon, notification, Table, Tooltip } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { script } from '@/api/tester';
import { useScriptTable } from './composables/useScriptTable';
import { PermissionKey, ScriptInfo } from './types';

type Props = {
  projectId: string;
  appId: string;
  userId: string;
  dataSource: ScriptInfo[];
  permissionsMap: { [key: string]: PermissionKey[] };
  pagination: PaginationProps;
  allowImportSamplesFlag: boolean;
  loaded: boolean;
  loading: boolean;
  resetSelectedIdsNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
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
  (event: 'tableChange', pagination: { current: number; pageSize: number }, sorter: { orderBy: string; orderSort: 'DESC' | 'ASC' }): void;
}>();

// Async components
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/exportModal/index.vue'));

// Router
const router = useRouter();

// Composable
const {
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
  handleActionClick,
  selectedIds
} = useScriptTable(props.permissionsMap);

// Export state
const exportIds = ref<string[]>([]);
const selectedData = ref<ScriptInfo>();
const authModalVisible = ref(false);
const exportVisible = ref(false);

/**
 * Import demo scripts
 */
const importDemo = async () => {
  emit('update:loading', true);
  const [error] = await script.importScriptDemo();
  emit('update:loading', false);
  
  if (error) {
    return;
  }

  notification.success('导入示例成功');
  emit('refresh');
};

/**
 * Handle table change events
 */
const tableChange = (pagination: { current: number; pageSize: number; }, _filters: { [key: string]: any }[], sorter: { orderBy: string; orderSort: 'DESC' | 'ASC' }) => {
  emit('tableChange', pagination, sorter);
};

/**
 * Handle auth action
 */
const toAuth = (data: ScriptInfo) => {
  selectedData.value = data;
  authModalVisible.value = true;
};

/**
 * Handle auth flag change
 */
const authFlagChange = ({ auth }: { auth: boolean }) => {
  // This would update the table data in the parent component
  emit('refresh');
};

// Watch for reset notifications
watch(() => props.resetSelectedIdsNotify, (newValue) => {
  if (!newValue) {
    return;
  }

  rowSelection.value.selectedRowKeys = [];
  selectedDataMap.value = {};
}, { immediate: true });

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
    notification.info(`最大支持批量操作 200 个脚本，当前已选中 ${selectedNum} 个脚本。`);
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

// Columns configuration
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: '12%',
    key: 'id',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    sorter: true,
    width: '15%',
    key: 'name'
  },
  {
    title: '插件',
    dataIndex: 'plugin',
    sorter: true,
    width: '6%',
    key: 'plugin',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '类型',
    dataIndex: 'type',
    sorter: true,
    width: '6%',
    key: 'type',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '来源',
    dataIndex: 'source',
    sorter: true,
    width: '6%',
    key: 'source',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '资源ID',
    dataIndex: 'sourceId',
    width: '12%',
    groupName: 'sourceId',
    hide: true,
    key: 'sourceId',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '资源名称',
    dataIndex: 'sourceName',
    width: '15%',
    groupName: 'sourceId',
    hide: false,
    key: 'sourceName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '标签',
    dataIndex: 'tags',
    key: 'tags'
  },
  {
    title: '添加人',
    dataIndex: 'createdByName',
    width: '7%',
    groupName: 'createdByName',
    key: 'createdByName',
    customRender: ({ text }: { text: string }) => text || '--',
    ellipsis: true
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedByName',
    width: '8%',
    groupName: 'createdByName',
    hide: true,
    key: 'lastModifiedByName',
    customRender: ({ text }: { text: string }) => text || '--',
    ellipsis: true
  },
  {
    title: '添加时间',
    dataIndex: 'createdDate',
    width: '10%',
    sorter: true,
    groupName: 'createdDate',
    key: 'createdDate',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '最后修改时间',
    dataIndex: 'lastModifiedDate',
    width: '10%',
    sorter: true,
    groupName: 'createdDate',
    hide: true,
    key: 'lastModifiedDate',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 138,
    key: 'action'
  }
];

// Menu items for dropdown
const menuItems = [
  {
    key: 'auth',
    icon: 'icon-quanxian1',
    name: '权限',
    permission: 'GRANT'
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: '克隆',
    permission: 'COLON'
  },
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: '删除',
    permission: 'DELETE'
  },
  {
    key: 'export',
    icon: 'icon-daochu',
    name: '导出',
    permission: 'EXPORT'
  }
];
</script>

<template>
  <template v-if="props.loaded">
    <div
      v-if="props.allowImportSamplesFlag && !props.dataSource.length"
      class="flex-1 flex flex-col items-center justify-center">
      <img class="w-27.5" src="../../../assets/images/nodata.png">
      <div class="flex items-center text-theme-sub-content text-3 leading-5">
        <span>任何脚本数据，</span>
        <Button
          type="link"
          size="small"
          class="px-0"
          @click="importDemo">
          导入示例
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
          @click="() => handleBatchExec((loading) => emit('update:loading', loading))">
          执行
        </Button>

        <Button
          :disabled="batchDeleteDisabled"
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="() => handleBatchDelete(
            (loading) => emit('update:loading', loading),
            (ids) => emit('delete', ids)
          )">
          删除
        </Button>

        <Button
          :disabled="batchExportDisabled"
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="() => handleBatchExport((ids, visible) => {
            exportIds.value = ids;
            exportVisible.value = visible;
          })">
          导出
        </Button>

        <Button
          danger
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="cancelBatchOperation">
          <span>取消批量操作</span>
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

          <div v-else-if="column.dataIndex === 'sourceId'"
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
              :disabled="!props.permissionsMap[record.id]?.includes('TEST')"
              type="text"
              size="small"
              class="flex items-center px-0 mr-2.5"
              @click="() => handleSingleExec(record, (loading) => emit('update:loading', loading))">
              <Icon icon="icon-zhihangjiaoben" class="mr-1" />
              <span>执行</span>
            </Button>

            <Button
              :disabled="!props.permissionsMap[record.id]?.includes('MODIFY')"
              type="text"
              size="small"
              class="flex items-center px-0 mr-2.5"
              @click="() => router.push(`/script/edit/${record.id}?type=edit&pageNo=${props.pagination.current}&pageSize=${props.pagination.pageSize}`)">
              <Icon icon="icon-shuxie" class="mr-1" />
              <span>编辑</span>
            </Button>

            <Dropdown
              :menuItems="menuItems"
              :admin="false"
              :permissions="props.permissionsMap[record.id]"
              @click="(key) => handleActionClick(
                key, 
                record,
                (loading) => emit('update:loading', loading),
                () => emit('refresh'),
                (ids) => emit('delete', ids),
                (ids, visible) => {
                  exportIds.value = ids;
                  exportVisible.value = visible;
                }
              )">
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
      onTips="开启权限控制后，需要手动授权后才会有相应操作权限。"
      offTips="无权限限制，账号中的所有用户都可以查看、操作，默认不开启权限控制。"
      title="脚本权限"
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
