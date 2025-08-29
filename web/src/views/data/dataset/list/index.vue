<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, IconCopy, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { dataSet } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { getCurrentPage } from '@/utils/utils';
import { DataSetItem } from '../PropsType';

import SearchPanel from '@/views/data/dataset/list/searchPanel/index.vue';

const { t } = useI18n();

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

const Introduce = defineAsyncComponent(() => import('@/views/data/dataset/list/introduce/index.vue'));
const ImportDataSetModal = defineAsyncComponent(() => import('@/views/data/dataset/list/import/index.vue'));
const ExportDataSetModal = defineAsyncComponent(() => import('@/views/data/dataset/export/index.vue'));
const PreviewDataSetModal = defineAsyncComponent(() => import('@/views/data/dataset/list/previewData/index.vue'));

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
  getCheckboxProps: (data: DataSetItem) => ({ disabled: boolean; });
  selectedRowKeys: string[];
}>();
const tableData = ref<DataSetItem[]>([]);

const selectedData = ref<DataSetItem>();
const previewDataSetModalVisible = ref(false);
const importDataSetModalVisible = ref(false);
const exportDataSetModalVisible = ref(false);
const exportDataSetId = ref<string>();

const buttonDropdownClick = ({ key }: { key: 'file' | 'jdbc' }) => {
  if (key === 'file') {
    router.push('/data#dataSet?source=FILE');
    return;
  }

  if (key === 'jdbc') {
    router.push('/data#dataSet?source=JDBC');
  }
};

const toCreateStaticDataSet = () => {
  router.push('/data#dataSet?source=STATIC');
};

const toImport = () => {
  importDataSetModalVisible.value = true;
};

const importOk = () => {
  pagination.value.current = 1;
  loadData();
};

const toExport = (id?: string) => {
  exportDataSetModalVisible.value = true;
  exportDataSetId.value = id;
};

const tableDropdownClick = (menuItem: {
  name: string;
  key: 'preview' | 'export' | 'clone';
  icon: string;
}, data: DataSetItem) => {
  const key = menuItem.key;
  if (key === 'preview') {
    toPreview(data);
    return;
  }

  if (key === 'export') {
    toExport(data.id);
    return;
  }

  if (key === 'clone') {
    toClone(data);
  }
};

const toEdit = (data: DataSetItem) => {
  const { id } = data;
  router.push(`/data#dataSet?id=${id}`);
};

const toDelete = (data: DataSetItem) => {
  modal.confirm({
    content: t('dataset.list.messages.deleteConfirm', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await dataSet.deleteDataSet([id]);
      if (error) {
        return;
      }

      notification.success(t('dataset.list.messages.deleteSuccess'));
      pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
      loadData();

      deleteTabPane([id]);
    }
  });
};

const toPreview = async (data: DataSetItem) => {
  selectedData.value = { id: data.id };
  previewDataSetModalVisible.value = true;
};

const toClone = async (data: DataSetItem) => {
  loading.value = true;
  const [error] = await dataSet.cloneDataSet([data.id]);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataset.list.messages.cloneSuccess'));
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
    notification.error(t('dataset.list.messages.batchDeleteMaxError', { maxNum: MAX_NUM, num }));
    return;
  }

  modal.confirm({
    content: t('dataset.list.messages.batchDeleteConfirm', { num }),
    async onOk () {
      const ids = selectedRowKeys;
      loading.value = true;
      const [error] = await dataSet.deleteDataSet(ids);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('dataset.list.messages.batchDeleteSuccess'));
      rowSelection.value = undefined;

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

  // 删除反选的数据集
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

  for (let i = 0, len = keys.length; i < len; i++) {
    if (!selectedRowKeys.includes(keys[i])) {
      selectedRowKeys.push(keys[i]);
    }
  }

  const num = selectedRowKeys.length;
  if (num > MAX_NUM) {
    notification.info(t('dataset.list.messages.batchDeleteMaxInfo', { maxNum: MAX_NUM, num }));
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

const tableChange = ({ current, pageSize }: { current: number; pageSize: number; }, _filters: { [key: string]: any }[], sorter: { orderBy: OrderByKey; orderSort: OrderSortKey }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;

  searchPanelParams.value.orderBy = sorter.orderBy;
  searchPanelParams.value.orderSort = sorter.orderSort;

  loadData();
};

const refresh = () => {
  pagination.value.current = 1;
  loadData();
};

// const searchInputChange = debounce(duration.search, (event: { target: { value: string; } }) => {
//   searchValue.value = event.target.value;
//   pagination.value.current = 1;
//   loadData();
// });

const handleSearchPanelChange = (data) => {
  searchPanelParams.value.filters = data.filters;
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
    filters?: {}[]
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
  const [error, res] = await dataSet.getDataSetList(params);
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
    const _list = data.list as DataSetItem[];
    tableData.value = [];
    _list.every((item) => {
      // const { extracted, extraction } = item;
      // if (!extraction || !['FILE', 'http', 'JDBC'].includes(extraction.source)) {
      //   item.source = '静态值';
      // } else {
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
      //     } else if (source === 'JDBC') {
      //       item.source += ' (Jdbc)';
      //     }
      //   }
      // }

      tableData.value.push(item);
      return true;
    });
  }
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    pagination.value.current = 1;
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const selectedNum = computed(() => rowSelection.value?.selectedRowKeys?.length);

const buttonDropdownMenuItems = [
  {
    name: t('dataset.list.buttonDropdown.fileExtractDataset'),
    key: 'file',
    noAuth: true
  },
  {
    name: t('dataset.list.buttonDropdown.jdbcExtractDataset'),
    key: 'jdbc',
    noAuth: true
  }
];

const columns = [
  {
    title: t('dataset.list.columns.name'),
    dataIndex: 'name',
    ellipsis: true,
    width: '20%'
  },
  {
    title: t('dataset.list.columns.description'),
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: t('dataset.list.columns.valueSource'),
    dataIndex: 'dataSource',
    ellipsis: true,
    width: '10%',
    customRender: ({ text }) => text?.message
  },
  {
    title: t('dataset.list.columns.lastModifiedBy'),
    dataIndex: 'lastModifiedBy',
    ellipsis: true,
    width: '10%',
    sorter: true,
    customRender: ({ record }) => record.lastModifiedByName,
    groupName: 'person'
  },
  {
    title: t('dataset.list.columns.createdBy'),
    dataIndex: 'createdBy',
    ellipsis: true,
    width: '10%',
    sorter: true,
    customRender: ({ record }) => record.createdByName,
    groupName: 'person',
    hide: true
  },
  {
    title: t('dataset.list.columns.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: '13%',
    sorter: true,
    groupName: 'date'
  },
  {
    title: t('dataset.list.columns.createdDate'),
    dataIndex: 'createdDate',
    ellipsis: true,
    width: '13%',
    sorter: true,
    groupName: 'date',
    hide: true
  },
  {
    title: t('dataset.list.columns.action'),
    dataIndex: 'action',
    width: 140
  }
];

const tableDropdownMenuItems: {
  name: string;
  key: 'preview' | 'export' | 'clone';
  icon: string;
}[] = [
  {
    name: t('dataset.list.tableDropdown.previewData'),
    key: 'preview',
    icon: 'icon-zhengyan'
  },
  {
    name: t('dataset.list.tableDropdown.export'),
    key: 'export',
    icon: 'icon-daochu1'
  },
  {
    name: t('dataset.list.tableDropdown.clone'),
    key: 'clone',
    icon: 'icon-fuzhi'
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />

    <div class="text-3.5 font-semibold mb-1">{{ t('dataset.list.title') }}</div>

    <Spin
      :spinning="loading"
      style="height: calc(100% - 41px);"
      class="flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && tableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-7">
            <span>{{ t('dataset.list.noDataText') }}</span>

            <Dropdown :menuItems="buttonDropdownMenuItems" @click="buttonDropdownClick">
              <Button
                type="link"
                size="small"
                class="text-3.5 py-0 px-0 mx-1"
                @click="toCreateStaticDataSet">
                <span>{{ t('dataset.addStaticDataset') }}</span>
              </Button>
            </Dropdown>

            <span>{{ t('dataset.list.or') }}</span>

            <Button
              type="link"
              size="small"
              class="text-3.5 py-0 px-0 mx-1"
              @click="toImport">
              <span>{{ t('dataset.list.uploadDataset') }}</span>
            </Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :selectedNum="selectedNum"
            @change="handleSearchPanelChange"
            @to-import="toImport"
            @to-export="toExport()"
            @to-cancel-batch-delete="toCancelBatchDelete"
            @to-batch-delete="toBatchDelete"
            @refresh="refresh" />
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
                <RouterLink
                  class="link flex-1 truncate"
                  :title="record.name"
                  :to="`/data#dataSet?id=${record.id}`">
                  {{ record.name }}
                </RouterLink>
                <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
              </div>

              <template v-if="column.dataIndex === 'description'">
                <span v-if="!record.description" class="text-text-sub-content">{{ t('dataset.list.noDescription') }}</span>
              </template>

              <div v-else-if="column.dataIndex === 'action'" class="flex items-center">
                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2.5"
                  @click="toEdit(record)">
                  <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
                  <span>{{ t('actions.edit') }}</span>
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2.5"
                  @click="toDelete(record)">
                  <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
                  <span>{{ t('actions.delete') }}</span>
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

    <AsyncComponent :visible="previewDataSetModalVisible">
      <PreviewDataSetModal
        v-model:visible="previewDataSetModalVisible"
        :projectId="props.projectId"
        :dataSource="selectedData" />
    </AsyncComponent>

    <AsyncComponent :visible="importDataSetModalVisible">
      <ImportDataSetModal
        v-model:visible="importDataSetModalVisible"
        :projectId="props.projectId"
        @ok="importOk" />
    </AsyncComponent>

    <AsyncComponent :visible="exportDataSetModalVisible">
      <ExportDataSetModal
        :id="exportDataSetId"
        v-model:visible="exportDataSetModalVisible"
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
