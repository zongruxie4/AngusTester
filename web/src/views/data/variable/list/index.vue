<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, IconCopy, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { variable } from '@/api/tester';

import { getCurrentPage } from '@/utils/utils';
import { VariableItem } from '../PropsType';

import SearchPanel from '@/views/data/variable/list/searchPanel/index.vue';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

const Introduce = defineAsyncComponent(() => import('@/views/data/variable/list/introduce/index.vue'));
const Import = defineAsyncComponent(() => import('@/views/data/variable/list/import/index.vue'));
const Export = defineAsyncComponent(() => import('@/views/data/variable/export/index.vue'));

type OrderByKey = 'lastModifiedDate' | 'lastModifiedByName';
type OrderSortKey = 'ASC' | 'DESC';

const router = useRouter();

const MAX_NUM = 200;
const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);

const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
const pagination = ref<{ current: number; pageSize: number; total: number; }>({ current: 1, pageSize: 10, total: 0 });
const rowSelection = ref<{
  onChange:(key: string[]) => void;
  getCheckboxProps: (data: VariableItem) => ({ disabled: boolean; });
  selectedRowKeys: string[];
}>();
const tableData = ref<VariableItem[]>([]);

const importVariableModalVisible = ref(false);
const exportVariableModalVisible = ref(false);
const exportVariableId = ref<string>();
const visibilityIdSet = ref<Set<string>>(new Set());
const errorMessageMap = ref<Map<string, string>>(new Map());

const buttonDropdownClick = ({ key }: { key: 'file' | 'http' | 'jdbc' }) => {
  if (key === 'file') {
    router.push('/data#variables?source=FILE');
    return;
  }

  if (key === 'http') {
    router.push('/data#variables?source=http');
    return;
  }

  if (key === 'jdbc') {
    router.push('/data#variables?source=JDBC');
  }
};

const toCreateStaticVariable = () => {
  router.push('/data#variables?source=STATIC');
};

const toImport = () => {
  importVariableModalVisible.value = true;
};

const importOk = () => {
  pagination.value.current = 1;
  loadData();
};

const toExport = (id?:string) => {
  exportVariableModalVisible.value = true;
  exportVariableId.value = id;
};

const toHide = (data: VariableItem) => {
  visibilityIdSet.value.delete(data.id);
};

const toVisibility = (data: VariableItem) => {
  const { id } = data;
  if (data.passwordValue) {
    visibilityIdSet.value.add(id);
    return;
  }

  if (!data.previewFlag) {
    return;
  }

  const { value, extracted } = data;
  // 如果是静态变量，且包含mock函数，则调用接口查询值
  if (!extracted && !/@\w+\w*\([^)]*\)/.test(value)) {
    return;
  }

  loadValue(data);
};

const loadValue = async (data: VariableItem) => {
  const id = data.id;
  loading.value = true;
  const [error, res] = await variable.previewValue({ id: data.id }, { silence: true });
  loading.value = false;
  if (error) {
    errorMessageMap.value.set(id, error.message);
    return;
  }

  errorMessageMap.value.delete(id);
  if (res?.data) {
    data.value = res.data;
  }
};

const tableDropdownClick = (menuItem: {
  name: string;
  key: 'export' | 'clone';
  icon: string;
}, data: VariableItem) => {
  const key = menuItem.key;
  if (key === 'export') {
    toExport(data.id);
    return;
  }

  if (key === 'clone') {
    toClone(data);
  }
};

const toEdit = (data: VariableItem) => {
  const { id } = data;
  router.push(`/data#variables?id=${id}`);
};

const toDelete = (data: VariableItem) => {
  modal.confirm({
    content: `确定删除变量【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await variable.delVariables([id]);
      if (error) {
        return;
      }

      notification.success('变量删除成功');
      pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
      loadData();

      visibilityIdSet.value.delete(id);
      errorMessageMap.value.delete(id);

      deleteTabPane([id]);
    }
  });
};

const toClone = async (data: VariableItem) => {
  loading.value = true;
  const [error] = await variable.cloneVariable([data.id]);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('变量克隆成功');
  loadData();
};

const toBatchDelete = () => {
  if (!rowSelection.value) {
    rowSelection.value = {
      onChange: tableSelect,
      getCheckboxProps: () => {
        return {
          disabled: false
        };
      },
      selectedRowKeys: []
    };

    return;
  }

  const selectedRowKeys = rowSelection.value.selectedRowKeys;
  const num = selectedRowKeys.length;
  if (!num) {
    rowSelection.value = undefined;
    return;
  }

  if (num > MAX_NUM) {
    notification.error(`最大支持批量删除 ${MAX_NUM} 个变量，当前已选中 ${num} 个变量。`);
    return;
  }

  modal.confirm({
    content: `确定删除选中的 ${num} 条变量吗？`,
    async onOk () {
      const ids = selectedRowKeys;
      loading.value = true;
      const [error] = await variable.delVariables(ids);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('选中的变量全部删除成功');
      rowSelection.value = undefined;

      for (let i = 0, len = ids.length; i < len; i++) {
        const id = ids[i];
        visibilityIdSet.value.delete(id);
        errorMessageMap.value.delete(id);
      }

      const { current, pageSize, total } = pagination.value;
      const totalPage = Math.ceil(total / pageSize);
      const remainder = total % pageSize;

      const deleteNum = ids.length;
      const deletePages = Math.floor(deleteNum / pageSize);
      const deleteRemainder = deleteNum % pageSize;

      if ((deleteRemainder === 0 || remainder === 0) || (deleteRemainder < remainder)) {
        if (current + deletePages <= totalPage) {
          loadData();
          return;
        }

        pagination.value.current = current - (current + deletePages - totalPage) || 1;
        loadData();
        return;
      }

      if (deleteRemainder >= remainder) {
        if (current + deletePages + 1 <= totalPage) {
          loadData();
          return;
        }

        pagination.value.current = current - (current + deletePages - totalPage) - 1 || 1;
        loadData();
      }
    }
  });
};

const toCancelBatchDelete = () => {
  rowSelection.value = undefined;
};

const tableSelect = (keys: string[]) => {
  if (!rowSelection.value) {
    return;
  }

  const currentIds = tableData.value.map(item => item.id);
  const deleteIds = currentIds.reduce((prev, cur) => {
    if (!keys.includes(cur)) {
      prev.push(cur);
    }

    return prev;
  }, [] as string[]);

  // 删除反选的变量
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

  for (let i = 0, len = keys.length; i < len; i++) {
    if (!selectedRowKeys.includes(keys[i])) {
      selectedRowKeys.push(keys[i]);
    }
  }

  const num = selectedRowKeys.length;
  if (num > MAX_NUM) {
    notification.info(`最大支持批量删除 ${MAX_NUM} 个变量，当前已选中 ${num} 个变量。`);
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

const tableChange = ({ current, pageSize }: { current: number; pageSize: number; }, _filters: { [key: string]: any }[], sorter: { orderBy: OrderByKey; orderSort: OrderSortKey }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;

  // orderBy.value = sorter.orderBy;
  // orderSort.value = sorter.orderSort;
  searchPanelParams.value.orderBy = sorter.orderBy;
  searchPanelParams.value.orderSort = sorter.orderSort;
  loadData();
};

const refresh = () => {
  visibilityIdSet.value.clear();
  errorMessageMap.value.clear();
  pagination.value.current = 1;
  loadData();
};

// const searchInputChange = debounce(duration.search, (event: { target: { value: string; } }) => {
//   searchValue.value = event.target.value;
//   pagination.value.current = 1;
//   loadData();
// });

const handleSearchPanelChange = (data) => {
  pagination.value.current = 1;
  searchPanelParams.value.filters = data.filters;
  loadData();
};

const loadData = async () => {
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    orderBy?: OrderByKey;
    orderSort?: OrderSortKey;
    filters?: {value: string; op: string; key: string}[]
  } = {
    projectId: props.projectId,
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize,
    ...searchPanelParams.value
  };

  // if (orderSort.value) {
  //   params.orderBy = orderBy.value;
  //   params.orderSort = orderSort.value;
  // }

  // if (searchValue.value?.length) {
  //   params.name = searchValue.value;
  // }

  loading.value = true;
  const [error, res] = await variable.loadVariablesList(params);
  loaded.value = true;
  loading.value = false;
  if (params.filters?.length) {
    searchedFlag.value = true;
  } else {
    searchedFlag.value = false;
  }

  if (error) {
    pagination.value.total = 0;
    tableData.value = [];
    return;
  }

  const data = res?.data || { total: 0, list: [] };
  if (data) {
    pagination.value.total = +data.total;
    const _list = data.list as VariableItem[];
    tableData.value = [];
    _list.every((item) => {
      // const { extracted, extraction } = item;
      // if (!extraction || !['FILE', 'http', 'JDBC'].includes(extraction.source)) {
      //   item.source = '静态值';
      //   if (/@\w+\w*\([^)]*\)/.test(item.value)) {
      //     item.previewFlag = true;
      //     item.source += ' (mock)';
      //   }
      // } else {
      //   item.previewFlag = true;
      //   const { source } = extraction;
      //   if (!extracted) {
      //     item.source = '精确值';
      //     if (source === 'FILE') {
      //       item.source += ' (文件)';
      //     } else if (source === 'http') {
      //       item.source += ' (Http)';
      //     } else if (source === 'JDBC') {
      //       item.source += ' (Jdbc)';
      //     }
      //   } else {
      //     item.source = '提取值';
      //     if (source === 'FILE') {
      //       item.source += ' (文件)';
      //     } else if (source === 'http') {
      //       item.source += ' (Http)';
      //     } else if (source === 'JDBC') {
      //       item.source += ' (Jdbc)';
      //     }
      //   }
      // }

      tableData.value.push(item);

      if (!item.passwordValue) {
        visibilityIdSet.value.add(item.id);
      }

      return true;
    });
  }
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    visibilityIdSet.value.clear();
    errorMessageMap.value.clear();
    pagination.value.current = 1;
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    visibilityIdSet.value.clear();
    errorMessageMap.value.clear();
    loadData();
  }, { immediate: true });
});

const selectedNum = computed(() => rowSelection.value?.selectedRowKeys?.length);

const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    width: '16%',
    sorter: true
  },
  {
    title: '值',
    dataIndex: 'value',
    ellipsis: true,
    width: '16%'
  },
  {
    title: '描述',
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: '密码',
    dataIndex: 'passwordValue',
    ellipsis: true,
    width: '4%'
  },
  {
    title: '值来源',
    dataIndex: 'dataSource',
    ellipsis: true,
    width: '8%',
    customRender: ({ text }) => text?.message
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedBy',
    ellipsis: true,
    width: '10%',
    sorter: true,
    groupName: 'person',
    customRender: ({ record }) => record.lastModifiedByName
  },
  {
    title: '创建人',
    dataIndex: 'createdBy',
    ellipsis: true,
    width: '10%',
    sorter: true,
    groupName: 'person',
    hide: true,
    customRender: ({ record }) => {
      return record.createdByName;
    }
  },
  {
    title: '最后修改时间',
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: '11%',
    sorter: true,
    groupName: 'date'
  },

  {
    title: '创建时间',
    dataIndex: 'createdDate',
    ellipsis: true,
    width: '11%',
    sorter: true,
    groupName: 'date',
    hide: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 140
  }
];

const buttonDropdownMenuItems = [
  {
    name: '文件提取变量',
    key: 'file',
    noAuth: true
  },
  {
    name: 'Http提取变量',
    key: 'http',
    noAuth: true
  },
  {
    name: 'Jdbc提取变量',
    key: 'jdbc',
    noAuth: true
  }
];

const tableDropdownMenuItems: {
  name: string;
  key: 'export' | 'clone';
  icon: string;
}[] = [
  {
    name: '导出',
    key: 'export',
    icon: 'icon-daochu1'
  },
  {
    name: '克隆',
    key: 'clone',
    icon: 'icon-fuzhi'
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />

    <div class="text-3.5 font-semibold mb-1">已添加的变量</div>

    <Spin
      :spinning="loading"
      style="height: calc(100% - 41px);"
      class="flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && tableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-7">
            <span>您尚未添加任何变量，立即</span>

            <Dropdown :menuItems="buttonDropdownMenuItems" @click="buttonDropdownClick">
              <Button
                type="link"
                size="small"
                class="text-3.5 py-0 px-0 mx-1"
                @click="toCreateStaticVariable">
                <span>添加静态变量</span>
              </Button>
            </Dropdown>

            <span>或</span>

            <Button
              type="link"
              size="small"
              class="text-3.5 py-0 px-0 mx-1"
              @click="toImport">
              <span>上传变量</span>
            </Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :selectedNum="selectedNum"
            @to-import="toImport"
            @to-export="toExport()"
            @to-cancel-batch-delete="toCancelBatchDelete"
            @to-batch-delete="toBatchDelete"
            @change="handleSearchPanelChange"
            @refresh="refresh" />

          <NoData v-if="tableData.length === 0" class="flex-1" />

          <Table
            v-else
            storageKey="variableList"
            :dataSource="tableData"
            :columns="columns"
            :pagination="pagination"
            :rowSelection="rowSelection"
            rowKey="id"
            class="flex-1"
            @change="tableChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'name'" class="flex items-center">
                <RouterLink
                  class="link flex-1 truncate"
                  :title="record.name"
                  :to="`/data#variables?id=${record.id}`">
                  {{ record.name }}
                </RouterLink>
                <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
              </div>

              <template v-if="column.dataIndex === 'description'">
                <span v-if="!record.description" class="text-text-sub-content">无描述~</span>
              </template>

              <template v-if="column.dataIndex === 'value'">
                <div v-if="record.passwordValue" class="flex items-center">
                  <template v-if="visibilityIdSet.has(record.id)">
                    <div :title="record.value" class="flex-1 truncate">{{ record.value }}</div>
                    <IconCopy
                      :disabled="!record.value"
                      :copyText="record.value"
                      class="ml-1.5" />
                    <Icon
                      icon="icon-zhengyan"
                      class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                      @click="toHide(record)" />
                  </template>
                  <template v-else>
                    <div class="flex-1 truncate">******</div>
                    <IconCopy
                      :disabled="!record.value"
                      :copyText="record.value"
                      class="ml-1.5" />
                    <Icon
                      icon="icon-biyan"
                      class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                      @click="toVisibility(record)" />
                  </template>
                </div>

                <div v-else class="flex items-center">
                  <div
                    v-if="errorMessageMap.has(record.id)"
                    :title="errorMessageMap.get(record.id)"
                    class="flex-1 truncate text-status-error">
                    {{ errorMessageMap.get(record.id) }}
                  </div>

                  <div
                    v-else
                    :title="record.value"
                    class="flex-1 truncate">
                    {{ record.value }}
                  </div>

                  <IconCopy
                    :disabled="!record.value"
                    :copyText="record.value"
                    class="ml-1.5" />
                  <Icon
                    icon="icon-zhengyan"
                    class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                    @click="toVisibility(record)" />
                </div>
              </template>

              <div v-if="column.dataIndex === 'passwordValue'" class="flex items-center">
                {{ record.passwordValue ? '是' : '否' }}
              </div>

              <div v-else-if="column.dataIndex === 'action'" class="flex items-center">
                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2.5"
                  @click="toEdit(record)">
                  <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
                  <span>编辑</span>
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2.5"
                  @click="toDelete(record)">
                  <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
                  <span>删除</span>
                </Button>

                <Dropdown :menuItems="tableDropdownMenuItems" @click="tableDropdownClick($event, record)">
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
        </template>
      </template>
    </Spin>

    <AsyncComponent :visible="importVariableModalVisible">
      <Import
        v-model:visible="importVariableModalVisible"
        :projectId="props.projectId"
        @ok="importOk" />
    </AsyncComponent>

    <AsyncComponent :visible="exportVariableModalVisible">
      <Export
        :id="exportVariableId"
        v-model:visible="exportVariableModalVisible"
        :projectId="props.projectId" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
