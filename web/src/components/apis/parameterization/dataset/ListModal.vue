<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input, Modal, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { dataset } from '@/api/tester';
import { DataSetItem } from './PropsType2';

const { t } = useI18n();

// Type definitions
type OrderByKey = 'lastModifiedDate' | 'lastModifiedByName';
type OrderSortKey = 'ASC' | 'DESC';

/**
 * Component props interface for dataset list modal
 */
type Props = {
    projectId: string;
    visible: boolean;
    selectedNames: string[];
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: false,
  selectedNames: () => []
});

// Component events
const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'ok', value: DataSetItem[]): void;
}>();

// Component state
const isDataLoaded = ref(false);
const isLoading = ref(false);
const searchKeyword = ref<string>();
const sortOrderBy = ref<OrderByKey>();
const sortOrderDirection = ref<OrderSortKey>();
const paginationConfig = ref<{ current: number; pageSize: number; total: number; showSizeChanger:boolean;}>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const tableRowSelection = ref<{
    onChange:(key: string[]) => void;
    getCheckboxProps: (data: DataSetItem) => ({ disabled: boolean; });
    selectedRowKeys: string[];
}>();
const selectedDatasetDataMap = ref<Map<string, DataSetItem>>(new Map());
const datasetTableData = ref<DataSetItem[]>([]);

/**
 * Handle search input change with debounce
 * @param event - Input change event
 */
const handleSearchInputChange = debounce(duration.search, (event: any) => {
  searchKeyword.value = event.target.value;
  paginationConfig.value.current = 1;
  loadDatasetData();
});

/**
 * Handle refresh button click
 */
const handleRefresh = () => {
  paginationConfig.value.current = 1;
  loadDatasetData();
};

/**
 * Load dataset data from API
 */
const loadDatasetData = async () => {
  const requestParams: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    name?: string;
    orderBy?: OrderByKey;
    orderSort?: OrderSortKey;
  } = {
    projectId: props.projectId,
    pageNo: paginationConfig.value.current,
    pageSize: paginationConfig.value.pageSize
  };

  if (sortOrderDirection.value) {
    requestParams.orderBy = sortOrderBy.value;
    requestParams.orderSort = sortOrderDirection.value;
  }

  if (searchKeyword.value?.length) {
    requestParams.name = searchKeyword.value;
  }

  isLoading.value = true;
  const [error, response] = await dataset.getDataSetList(requestParams);
  isDataLoaded.value = true;
  isLoading.value = false;

  if (error) {
    paginationConfig.value.total = 0;
    datasetTableData.value = [];
    return;
  }

  const responseData = response?.data || { total: 0, list: [] };
  if (responseData) {
    paginationConfig.value.total = +responseData.total;
    const datasetList = responseData.list as DataSetItem[];
    datasetTableData.value = [];

    const selectedDatasetNames = props.selectedNames;
    datasetList.every((item) => {
      const { extracted, extraction, name } = item;
      if (!extraction || !['FILE', 'http', 'JDBC'].includes(extraction.source)) {
        item.source = t('commonComp.apis.parameterizationDataset.listModal.staticValue');
      } else {
        const { source } = extraction;
        if (!extracted) {
          item.source = t('commonComp.apis.parameterizationDataset.listModal.exactValue');
          if (source === 'FILE') {
            item.source += ` ${t('commonComp.apis.parameterizationDataset.listModal.file')}`;
          } else if (source === 'HTTP') {
            item.source += ` ${t('commonComp.apis.parameterizationDataset.listModal.http')}`;
          } else if (source === 'JDBC') {
            item.source += ` ${t('commonComp.apis.parameterizationDataset.listModal.jdbc')}`;
          }
        } else {
          item.source = t('commonComp.apis.parameterizationDataset.listModal.extractedValue');
          if (source === 'FILE') {
            item.source += ` ${t('commonComp.apis.parameterizationDataset.listModal.file')}`;
          } else if (source === 'JDBC') {
            item.source += ` ${t('commonComp.apis.parameterizationDataset.listModal.jdbc')}`;
          }
        }
      }

      datasetTableData.value.push(item);

      if (tableRowSelection.value) {
        if (selectedDatasetNames.includes(name) && !tableRowSelection.value.selectedRowKeys.includes(name)) {
          tableRowSelection.value.selectedRowKeys.push(item.id);
        }
      }

      return true;
    });
  }
};

/**
 * Handle table change events (pagination, sorting)
 * @param paginationInfo - Pagination information
 * @param _filters - Table filters (unused)
 * @param sorter - Sorting information
 */
const handleTableChange = ({ current, pageSize }: { current: number; pageSize: number; }, _filters: { [key: string]: any }[], sorter: { orderBy: OrderByKey; orderSort: OrderSortKey }) => {
  paginationConfig.value.current = current;
  paginationConfig.value.pageSize = pageSize;

  sortOrderBy.value = sorter.orderBy;
  sortOrderDirection.value = sorter.orderSort;

  loadDatasetData();
};

/**
 * Handle table row selection change
 * @param selectedKeys - Selected row keys
 */
const handleTableRowSelection = (selectedKeys: string[]) => {
  if (!tableRowSelection.value) {
    return;
  }

  const currentDatasetIds = datasetTableData.value.map(item => item.id);
  const deselectedIds = currentDatasetIds.reduce((prev, cur) => {
    if (!selectedKeys.includes(cur)) {
      prev.push(cur);
    }

    return prev;
  }, [] as string[]);

  // Remove deselected datasets
  for (let i = 0, len = deselectedIds.length; i < len; i++) {
    const id = deselectedIds[i];
    selectedDatasetDataMap.value.delete(id);
  }

  // Remove deselected dataset IDs
  const updatedSelectedRowKeys = tableRowSelection.value.selectedRowKeys.filter(item => !deselectedIds.includes(item));

  // Add newly selected datasets
  for (let i = 0, len = selectedKeys.length; i < len; i++) {
    const id = selectedKeys[i];
    if (!updatedSelectedRowKeys.includes(id)) {
      updatedSelectedRowKeys.push(id);

      const datasetData = datasetTableData.value.find(item => item.id === id);
      if (datasetData) {
        selectedDatasetDataMap.value.set(id, datasetData);
      }
    }
  }

  tableRowSelection.value.selectedRowKeys = updatedSelectedRowKeys;
};

/**
 * Handle modal cancel
 */
const handleModalCancel = () => {
  emit('update:visible', false);
};

/**
 * Handle modal confirmation
 */
const handleModalConfirmation = () => {
  const selectedDatasetList = Array.from(selectedDatasetDataMap.value.values());
  emit('ok', selectedDatasetList);

  handleModalCancel();
};

// Component initialization and watchers
onMounted(() => {
  tableRowSelection.value = {
    onChange: handleTableRowSelection,
    getCheckboxProps: ({ name }) => {
      return {
        disabled: props.selectedNames.includes(name)
      };
    },
    selectedRowKeys: []
  };

  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      isDataLoaded.value = false;
      searchKeyword.value = undefined;
      paginationConfig.value.current = 1;
      paginationConfig.value.total = 0;
      selectedDatasetDataMap.value.clear();
      datasetTableData.value = [];

      if (tableRowSelection.value) {
        tableRowSelection.value.selectedRowKeys = [];
      }

      return;
    }

    loadDatasetData();
  }, { immediate: true });
});

// Computed properties
const okButtonProps = computed(() => {
  return {
    disabled: !tableRowSelection.value?.selectedRowKeys?.length
  };
});

// Table column configuration
const tableColumns = [
  {
    key: 'name',
    title: t('commonComp.apis.parameterizationDataset.listModal.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    key: 'description',
    title: t('common.description'),
    dataIndex: 'description',
    ellipsis: true
  },
  {
    key: 'source',
    title: t('commonComp.apis.parameterizationDataset.listModal.valueSource'),
    dataIndex: 'source',
    ellipsis: true,
    width: '10%'
  },
  {
    key: 'lastModifiedByName',
    title: t('common.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    ellipsis: true,
    width: '11%',
    sort: true
  },
  {
    key: 'lastModifiedDate',
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: '15%',
    sort: true
  }
];
</script>

<template>
  <Modal
    :title="t('commonComp.apis.parameterizationDataset.listModal.title')"
    :visible="props.visible"
    :width="1100"
    :okButtonProps="okButtonProps"
    wrapClassName="table-pagination-mini"
    @cancel="handleModalCancel"
    @ok="handleModalConfirmation">
    <Spin :spinning="isLoading" class="h-full">
      <template v-if="isDataLoaded">
        <div class="flex items-center justify-between mb-3.5 space-x-5">
          <Input
            :maxlength="150"
            allowClear
            trim
            class="w-75 flex-grow-0 flex-shrink"
            :placeholder="t('common.placeholders.searchKeyword')"
            @change="handleSearchInputChange" />
          <Button
            type="default"
            size="small"
            class="flex items-center"
            @click="handleRefresh">
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuaxin" />
            <span>{{ t('actions.refresh') }}</span>
          </Button>
        </div>

        <NoData v-if="datasetTableData.length === 0" class="flex-1" />

        <Table
          v-else
          :dataSource="datasetTableData"
          :columns="tableColumns"
          :pagination="paginationConfig"
          :rowSelection="tableRowSelection"
          :noDataSize="'small'"
          :noDataText="t('common.noData')"
          rowKey="id"
          class="flex-1"
          @change="handleTableChange" />
      </template>
    </Spin>
  </Modal>
</template>

<style>
.table-pagination-mini .ant-pagination {
    margin-bottom: 0;
}
</style>
