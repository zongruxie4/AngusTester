<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Icon, IconCopy, Input, Modal, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { variable } from '@/api/tester';

import { VariableItem } from '../PropsType';

type OrderByKey = 'lastModifiedDate' | 'lastModifiedByName';
type OrderSortKey = 'ASC' | 'DESC';

type Props = {
    projectId: string;
    visible: boolean;
    selectedNames: string[];
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: false,
  selectedNames: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'ok', value: VariableItem[]): void;
}>();

const loaded = ref(false);
const loading = ref(false);
const searchValue = ref<string>();
const orderBy = ref<OrderByKey>();
const orderSort = ref<OrderSortKey>();
const pagination = ref<{ current: number; pageSize: number; total: number; showSizeChanger:boolean;}>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const rowSelection = ref<{
    onChange:(key: string[]) => void;
    getCheckboxProps: (data: VariableItem) => ({ disabled: boolean; });
    selectedRowKeys: string[];
}>();
const selectedDataMap = ref<Map<string, VariableItem>>(new Map());
const tableData = ref<VariableItem[]>([]);

const visibilityIdSet = ref<Set<string>>(new Set());
const errorMessageMap = ref<Map<string, string>>(new Map());

const searchInputChange = debounce(duration.search, (event: { target: { value: string; } }) => {
  searchValue.value = event.target.value;
  pagination.value.current = 1;
  loadData();
});

const toRefresh = () => {
  visibilityIdSet.value.clear();
  errorMessageMap.value.clear();
  pagination.value.current = 1;
  loadData();
};

const loadData = async () => {
  const params: {
        projectId: string;
        pageNo: number;
        pageSize: number;
        name?: string;
        orderBy?: OrderByKey;
        orderSort?: OrderSortKey;
    } = {
      projectId: props.projectId,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize
    };

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  if (searchValue.value?.length) {
    params.name = searchValue.value;
  }

  loading.value = true;
  const [error, res] = await variable.getVariablesList(params);
  loaded.value = true;
  loading.value = false;

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

    const names = props.selectedNames;
    _list.every((item) => {
      const { extracted, extraction, name } = item;
      if (!extraction || !['FILE', 'http', 'JDBC'].includes(extraction.source)) {
        item.source = '静态值';
        if (/@\w+\w*\([^)]*\)/.test(item.value)) {
          item.previewFlag = true;
          item.source += ' (mock)';
        }
      } else {
        item.previewFlag = true;
        const { source } = extraction;
        if (!extracted) {
          item.source = '精确值';
          if (source === 'FILE') {
            item.source += ' (文件)';
          } else if (source === 'http') {
            item.source += ' (Http)';
          } else if (source === 'JDBC') {
            item.source += ' (Jdbc)';
          }
        } else {
          item.source = '提取值';
          if (source === 'FILE') {
            item.source += ' (文件)';
          } else if (source === 'http') {
            item.source += ' (Http)';
          } else if (source === 'JDBC') {
            item.source += ' (Jdbc)';
          }
        }
      }

      tableData.value.push(item);

      if (rowSelection.value) {
        if (names.includes(name) && !rowSelection.value.selectedRowKeys.includes(name)) {
          rowSelection.value.selectedRowKeys.push(item.id);
        }
      }

      if (!item.passwordValue) {
        visibilityIdSet.value.add(item.id);
      }

      return true;
    });
  }
};

const loadValue = async (data: VariableItem) => {
  const id = data.id;
  loading.value = true;
  const [error, res] = await variable.previewVariableValue({ id: data.id }, { silence: true });
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

const toHide = (data: VariableItem) => {
  visibilityIdSet.value.delete(data.id);
};

const toVisibility = (data: VariableItem) => {
  const { id, value, extracted } = data;
  visibilityIdSet.value.add(id);

  // 如果是静态变量，且包含mock函数，则调用接口查询值
  if (!extracted && !/@\w+\w*\([^)]*\)/.test(value)) {
    return;
  }

  loadValue(data);
};

const tableChange = ({ current, pageSize }: { current: number; pageSize: number; }, _filters: { [key: string]: any }[], sorter: { orderBy: OrderByKey; orderSort: OrderSortKey }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;

  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;

  loadData();
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
  for (let i = 0, len = deleteIds.length; i < len; i++) {
    const id = deleteIds[i];
    selectedDataMap.value.delete(id);
  }

  // 删除反选的变量ID
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

  // 添加新选中的变量
  for (let i = 0, len = keys.length; i < len; i++) {
    const id = keys[i];
    if (!selectedRowKeys.includes(id)) {
      selectedRowKeys.push(id);

      const data = tableData.value.find(item => item.id === id);
      if (data) {
        selectedDataMap.value.set(id, data);
      }
    }
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

const cancel = () => {
  emit('update:visible', false);
};

const ok = () => {
  const list = Array.from(selectedDataMap.value.values());
  emit('ok', list);

  cancel();
};

onMounted(() => {
  rowSelection.value = {
    onChange: tableSelect,
    getCheckboxProps: ({ name }) => {
      return {
        disabled: props.selectedNames.includes(name)
      };
    },
    selectedRowKeys: []
  };

  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      loaded.value = false;
      searchValue.value = undefined;
      pagination.value.current = 1;
      pagination.value.total = 0;
      selectedDataMap.value.clear();
      tableData.value = [];
      visibilityIdSet.value.clear();
      errorMessageMap.value.clear();

      if (rowSelection.value) {
        rowSelection.value.selectedRowKeys = [];
      }

      return;
    }

    loadData();
  }, { immediate: true });
});

const okButtonProps = computed(() => {
  return {
    disabled: !rowSelection.value?.selectedRowKeys?.length
  };
});

const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '值',
    dataIndex: 'value',
    ellipsis: true
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
    width: '5%'
  },
  {
    title: '值来源',
    dataIndex: 'source',
    ellipsis: true,
    width: '10%'
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedByName',
    ellipsis: true,
    width: '10%',
    sort: true
  },
  {
    title: '最后修改时间',
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: '14%',
    sort: true
  }
];
</script>

<template>
  <Modal
    title="引用变量"
    :visible="props.visible"
    :width="1100"
    :okButtonProps="okButtonProps"
    wrapClassName="table-pagination-mini"
    @cancel="cancel"
    @ok="ok">
    <Spin :spinning="loading" class="h-full">
      <template v-if="loaded">
        <div class="flex items-center justify-between mb-3.5 space-x-5">
          <Input
            :maxlength="150"
            allowClear
            trim
            class="w-75 flex-grow-0 flex-shrink"
            placeholder="查询名称、描述"
            @change="searchInputChange" />
          <Button
            type="default"
            size="small"
            class="flex items-center"
            @click="toRefresh">
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuaxin" />
            <span>刷新</span>
          </Button>
        </div>

        <NoData v-if="tableData.length === 0" class="flex-1" />

        <Table
          v-else
          :dataSource="tableData"
          :columns="columns"
          :pagination="pagination"
          :rowSelection="rowSelection"
          rowKey="id"
          class="flex-1"
          @change="tableChange">
          <template #bodyCell="{ column, record }">
            <div v-if="column.dataIndex === 'name'" class="flex items-center">
              <span>{{ record.name }}</span>
              <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
            </div>

            <template v-if="column.dataIndex === 'value'">
              <div v-if="record.passwordValue" class="flex items-center">
                <template v-if="visibilityIdSet.has(record.id)">
                  <div :title="record.value" class="flex-1 truncate">{{ record.value }}</div>
                  <Icon
                    icon="icon-zhengyan"
                    class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                    @click="toHide(record)" />
                </template>
                <template v-else>
                  <div class="flex-1 truncate">******</div>
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

                <Icon
                  v-if="record.previewFlag"
                  icon="icon-zhengyan"
                  class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                  @click="toVisibility(record)" />
              </div>
            </template>

            <div v-if="column.dataIndex === 'passwordValue'" class="flex items-center">
              {{ record.passwordValue ? t('status.yes') : t('status.no') }}
            </div>
          </template>
        </Table>
      </template>
    </Spin>
  </Modal>
</template>

<style>
.table-pagination-mini .ant-pagination {
    margin-bottom: 0;
}
</style>
