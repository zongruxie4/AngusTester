<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button, PaginationProps, Tag } from 'ant-design-vue';
import { AsyncComponent, AuthorizeModal, Dropdown, Icon, modal, notification, Table, Tooltip } from '@xcan-angus/vue-ui';
import { TESTER, http } from '@xcan-angus/tools';

import { PermissionKey, ScriptInfo } from '../../PropsType';

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

const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/exportModal/index.vue'));

const router = useRouter();

const MAX_NUM = 200;

const tableData = ref<ScriptInfo[]>([]);
const exportIds = ref<string[]>([]);
const selectedData = ref<ScriptInfo>();
const authModalVisible = ref(false);
const exportVisible = ref(false);

const selectedDataMap = ref<{
  [key: string]: {
    id: string;
    name: string;
  };
}>({});

const batchExecDisabled = ref(false);
const batchDeleteDisabled = ref(false);
const batchExportDisabled = ref(false);

const tableSelect = (keys: string[]) => {
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

  // 删除反选的脚本
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

  for (let i = 0, len = keys.length; i < len; i++) {
    if (!selectedRowKeys.includes(keys[i])) {
      selectedRowKeys.push(keys[i]);
    }
  }

  const selectedNum = selectedRowKeys.length;
  if (selectedNum > MAX_NUM) {
    notification.info(`最大支持批量操作 ${MAX_NUM} 个脚本，当前已选中 ${selectedNum} 个脚本。`);
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

const rowSelection = ref<{
  onChange:(key: string[]) => void;
  selectedRowKeys: string[];
    }>({
      onChange: tableSelect,
      selectedRowKeys: []
    });

const cancelBatchOperation = () => {
  rowSelection.value.selectedRowKeys = [];
  selectedDataMap.value = {};
};

const batchExec = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: `确定执行选中的 ${num} 条脚本吗？`,
    async onOk () {
      emit('update:loading', true);

      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const promises: Promise<any>[] = [];
      for (let i = 0, len = ids.length; i < len; i++) {
        promises.push(http.post(`${TESTER}/exec/byscript`, { scriptId: ids[i] }, { silence: true }));
      }

      Promise.all(promises).then((res: [Error | null, any][]) => {
        const errorIds: string[] = [];
        for (let i = 0, len = res.length; i < len; i++) {
          if (res[i][0]) {
            errorIds.push(ids[i]);
          }
        }

        const errorNum = errorIds.length;
        if (errorNum === 0) {
          notification.success(`选中的 ${num} 条脚本全部执行成功`);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(`选中的 ${num} 条脚本全部执行失败`);
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(`选中的 ${num - errorNum} 条脚本执行成功，${errorNum} 条脚本执行失败`);

        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successIds.includes(item));
        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      }).finally(() => {
        emit('update:loading', false);
      });
    }
  });
};

const batchDelete = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  if (!num) {
    return;
  }

  if (num > MAX_NUM) {
    notification.error(`最大支持批量删除 ${MAX_NUM} 个脚本，当前已选中 ${num} 个脚本。`);
    return;
  }

  modal.confirm({
    content: `确定删除选中的 ${num} 条脚本吗？`,
    async onOk () {
      emit('update:loading', true);
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const [error] = await http.del(`${TESTER}/script`, { ids });
      emit('update:loading', false);
      if (error) {
        return;
      }

      notification.success(`选中的 ${num} 条脚本删除成功`);
      emit('delete', ids);
      rowSelection.value.selectedRowKeys = [];
      selectedDataMap.value = {};
    }
  });
};

const batchExport = async () => {
  exportIds.value = rowSelection.value.selectedRowKeys;
  exportVisible.value = true;
};

const importDemo = async () => {
  emit('update:loading', true);
  const [error] = await http.post(`${TESTER}/script/example/import`);
  emit('update:loading', false);
  if (error) {
    return;
  }

  notification.success('导入示例成功');
  emit('refresh');
};

const tableChange = (pagination: { current: number; pageSize: number; }, _filters: { [key: string]: any }[], sorter: { orderBy: string; orderSort: 'DESC' | 'ASC' }) => {
  emit('tableChange', pagination, sorter);
};

const toCreateExec = async (data: ScriptInfo) => {
  emit('update:loading', true);
  const [error] = await http.post(`${TESTER}/exec/byscript`, { scriptId: data.id });
  emit('update:loading', false);
  if (error) {
    return;
  }

  notification.success('脚本添加执行成功，请在“执行”中查看详情');
};

const toAuth = (data: ScriptInfo) => {
  selectedData.value = data;
  authModalVisible.value = true;
};

const toClone = async (data: ScriptInfo) => {
  emit('update:loading', true);
  const [error] = await http.post(`${TESTER}/script/${data.id}/clone`);
  emit('update:loading', false);
  if (error) {
    return;
  }

  notification.success('脚本克隆成功');
  emit('refresh');
};

const toDelete = async (data: ScriptInfo) => {
  const name = data.name;
  const source = data.source.value;
  const content = source === 'SCENARIO' ? `删除脚本【${name}】同时也会删除关联场景，确定是否删除？` : `确定删除【${name}】脚本吗？`;
  modal.confirm({
    content,
    async onOk () {
      const id = data.id;
      emit('update:loading', true);
      const [error] = await http.del(`${TESTER}/script`, { ids: [id] });
      emit('update:loading', false);
      if (error) {
        return;
      }

      notification.success('脚本删除成功');
      emit('delete', [id]);
    }
  });
};

const toExport = async (data: ScriptInfo) => {
  exportIds.value = [data.id];
  exportVisible.value = true;
};

const actionClick = (key: string, data: ScriptInfo) => {
  switch (key) {
    case 'exec':
      toCreateExec(data);
      break;
    case 'edit':
      router.push(`/script/edit/${data.id}?type=edit&pageNo=${props.pagination.current}&pageSize=${props.pagination.pageSize}`);
      break;
    case 'auth':
      toAuth(data);
      break;
    case 'clone':
      toClone(data);
      break;
    case 'delete':
      toDelete(data);
      break;
    case 'export':
      toExport(data);
      break;
  }
};

const authFlagChange = ({ authFlag }: { authFlag: boolean }) => {
  const data = tableData.value;
  const targetId = selectedData.value?.id;
  for (let i = 0, len = data.length; i < len; i++) {
    if (data[i].id === targetId) {
      data[i].authFlag = authFlag;
      break;
    }
  }
};

onMounted(() => {
  watch(() => props.dataSource, (newValue = []) => {
    tableData.value = newValue;
  }, { immediate: true });

  watch(() => selectedDataMap.value, (newValue) => {
    batchExportDisabled.value = false;
    batchExecDisabled.value = false;
    batchDeleteDisabled.value = false;

    const values = (Object.values(newValue) || []) as {
      id: string;
      name: string;
    }[];

    const _permissionsMap = props.permissionsMap;

    for (let i = 0, len = values.length; i < len; i++) {
      const { id } = values[i];
      const permissions = _permissionsMap[id];
      if (permissions) {
        if (!permissions.includes('TEST')) {
          batchExecDisabled.value = true;
        }

        if (!permissions.includes('DELETE')) {
          batchDeleteDisabled.value = true;
        }

        if (!permissions.includes('EXPORT')) {
          batchExportDisabled.value = true;
        }
      }
    }
  }, { immediate: true, deep: true });

  watch(() => props.resetSelectedIdsNotify, (newValue) => {
    if (!newValue) {
      return;
    }

    rowSelection.value.selectedRowKeys = [];
    selectedDataMap.value = {};
  }, { immediate: true });
});

const selectedIds = computed(() => {
  return rowSelection.value.selectedRowKeys || [];
});

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: '12%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    sorter: true,
    width: '15%'
  },
  {
    title: '插件',
    dataIndex: 'plugin',
    sorter: true,
    width: '6%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '类型',
    dataIndex: 'type',
    sorter: true,
    width: '6%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '来源',
    dataIndex: 'source',
    sorter: true,
    width: '6%',
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
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '标签',
    dataIndex: 'tags'
  },
  {
    title: '添加人',
    dataIndex: 'createdByName',
    width: '7%',
    groupName: 'createdByName',
    customRender: ({ text }) => text || '--',
    ellipsis: true
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedByName',
    width: '8%',
    groupName: 'createdByName',
    hide: true,
    customRender: ({ text }) => text || '--',
    ellipsis: true
  },
  {
    title: '添加时间',
    dataIndex: 'createdDate',
    width: '10%',
    sorter: true,
    groupName: 'createdDate',
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
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 138
  }
];

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
      v-if="props.allowImportSamplesFlag && !tableData.length"
      class="flex-1 flex flex-col items-center justify-center">
      <img class="w-27.5" src="../../../../assets/images/nodata.png">
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
          @click="batchExec">
          执行
        </Button>

        <Button
          :disabled="batchDeleteDisabled"
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="batchDelete">
          删除
        </Button>

        <Button
          :disabled="batchExportDisabled"
          type="link"
          size="small"
          class="flex items-center px-0 h-5 leading-5"
          @click="batchExport">
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
        :dataSource="tableData"
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
              :disabled="!props.permissionsMap[record.id]?.includes('TEST')"
              type="text"
              size="small"
              class="flex items-center px-0 mr-2.5"
              @click="actionClick('exec', record)">
              <Icon icon="icon-zhihangjiaoben" class="mr-1" />
              <span>执行</span>
            </Button>

            <Button
              :disabled="!props.permissionsMap[record.id]?.includes('MODIFY')"
              type="text"
              size="small"
              class="flex items-center px-0 mr-2.5"
              @click="actionClick('edit', record)">
              <Icon icon="icon-shuxie" class="mr-1" />
              <span>编辑</span>
            </Button>

            <Dropdown
              :menuItems="menuItems"
              :adminFlag="false"
              :permissions="props.permissionsMap[record.id]"
              @click="actionClick($event.key, record)">
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
      :appId="appId"
      :listUrl="`${TESTER}/script/auth?scriptId=${selectedData?.id}`"
      :delUrl="`${TESTER}/script/auth`"
      :addUrl="`${TESTER}/script/${selectedData?.id}/auth`"
      :updateUrl="`${TESTER}/script/auth`"
      :enabledUrl="`${TESTER}/script/${selectedData?.id}/auth/enabled`"
      :initStatusUrl="`${TESTER}/script/${selectedData?.id}/auth/status`"
      onTips="开启权限控制后，需要手动授权后才会有相应操作权限。"
      offTips="无权限限制，账号中的所有用户都可以查看、操作，默认不开启权限控制。"
      title="脚本权限"
      @change="authFlagChange" />
  </AsyncComponent>

  <AsyncComponent :visible="exportVisible">
    <ExportScriptModal v-model:visible="exportVisible" :ids="exportIds" />
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
