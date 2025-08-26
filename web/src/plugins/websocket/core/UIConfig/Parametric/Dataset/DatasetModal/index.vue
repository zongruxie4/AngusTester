<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, NoData, Table, Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { dataSet } from '@/api/tester';

import { DatasetItem } from './PropsType';

const { t } = useI18n();

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
    (e: 'ok', value: DatasetItem[]): void;
}>();

const loaded = ref(false);
const loading = ref(false);
const searchValue = ref<string>();
const orderBy = ref<OrderByKey>();
const orderSort = ref<OrderSortKey>();
const pagination = ref<{ current: number; pageSize: number; total: number; showSizeChanger:boolean;}>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const rowSelection = ref<{
    onChange:(key: string[]) => void;
    getCheckboxProps: (data: DatasetItem) => ({ disabled: boolean; });
    selectedRowKeys: string[];
}>();
const selectedDataMap = ref<Map<string, DatasetItem>>(new Map());
const tableData = ref<DatasetItem[]>([]);

const searchInputChange = debounce(duration.search, (event: { target: { value: string; } }) => {
  searchValue.value = event.target.value;
  pagination.value.current = 1;
  loadData();
});

const toRefresh = () => {
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
  const [error, res] = await dataSet.getDataSetList(params);
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
    const _list = data.list as DatasetItem[];
    tableData.value = [];

    const names = props.selectedNames;
    _list.every((item) => {
      const { extracted, extraction, name, id, createdByName } = item;
      if (!extraction || !['FILE', 'HTTP', 'JDBC'].includes(extraction.source)) {
        item.source = '静态值';
      } else {
        const { source } = extraction;
        if (!extracted) {
          item.source = '精确值';
          if (source === 'FILE') {
            item.source += ' (文件)';
          } else if (source === 'HTTP') {
            item.source += ' (Http)';
          } else if (source === 'JDBC') {
            item.source += ' (Jdbc)';
          }
        } else {
          item.source = '提取值';
          if (source === 'FILE') {
            item.source += ' (文件)';
          } else if (source === 'JDBC') {
            item.source += ' (Jdbc)';
          }
        }
      }

      item['x-id'] = id;
      item['x-createdByName'] = createdByName;

      tableData.value.push(item);

      if (rowSelection.value) {
        if (names.includes(name) && !rowSelection.value.selectedRowKeys.includes(name)) {
          rowSelection.value.selectedRowKeys.push(item.id);
        }
      }

      return true;
    });
  }
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
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.descriptionColumn'),
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.valueSource'),
    dataIndex: 'source',
    ellipsis: true,
    width: '10%'
  },
  {
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    ellipsis: true,
    width: '11%',
    sort: true
  },
  {
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: '15%',
    sort: true
  }
];
</script>

<template>
  <Modal
    :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.title')"
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
            :placeholder="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.searchPlaceholder')"
            @change="searchInputChange" />
          <Button
            type="default"
            size="small"
            class="flex items-center"
            @click="toRefresh">
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuaxin" />
            <span>{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.refresh') }}</span>
          </Button>
        </div>

        <NoData v-if="tableData.length === 0" class="flex-1 mt-10 mb-5" />

        <Table
          v-else
          :dataSource="tableData"
          :columns="columns"
          :pagination="pagination"
          :rowSelection="rowSelection"
          rowKey="id"
          class="flex-1"
          @change="tableChange" />
      </template>
    </Spin>
  </Modal>
</template>

<style>
.table-pagination-mini .ant-pagination {
    margin-bottom: 0;
}
</style>
