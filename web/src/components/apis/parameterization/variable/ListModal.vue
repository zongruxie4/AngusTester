<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { Icon, IconCopy, Input, Modal, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';

import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

import { variable } from '@/api/tester';

import { VariableDetail } from '@/views/data/variable/types';

const { t } = useI18n();

// Type definitions
type OrderByKey = 'lastModifiedDate' | 'lastModifiedByName';
type OrderSortKey = 'ASC' | 'DESC';

/**
 * Component props interface for variable list modal
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
    (e: 'ok', value: VariableDetail[]): void;
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
    getCheckboxProps: (data: VariableDetail) => ({ disabled: boolean; });
    selectedRowKeys: string[];
}>();
const selectedVariableDataMap = ref<Map<string, VariableDetail>>(new Map());
const variableTableData = ref<VariableDetail[]>([]);

const visibleVariableIdSet = ref<Set<string>>(new Set());
const variableErrorMessageMap = ref<Map<string, string>>(new Map());

/**
 * Handle search input change with debounce
 * @param event - Input change event
 */
const handleSearchInputChange = debounce(duration.search, (event: any) => {
  searchKeyword.value = event.target.value;
  paginationConfig.value.current = 1;
  loadVariableData();
});

/**
 * Handle refresh button click
 */
const handleRefresh = () => {
  visibleVariableIdSet.value.clear();
  variableErrorMessageMap.value.clear();
  paginationConfig.value.current = 1;
  loadVariableData();
};

/**
 * Load variable data from API
 */
const loadVariableData = async () => {
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
  const [error, response] = await variable.getVariablesList(requestParams);
  isDataLoaded.value = true;
  isLoading.value = false;

  if (error) {
    paginationConfig.value.total = 0;
    variableTableData.value = [];
    return;
  }

  const responseData = response?.data || { total: 0, list: [] };
  if (responseData) {
    paginationConfig.value.total = +responseData.total;
    const variableList = responseData.list as VariableDetail[];
    variableTableData.value = [];

    const selectedVariableNames = props.selectedNames;
    variableList.every((item) => {
      const { extracted, extraction, name } = item;
      if (!extraction || !['FILE', 'http', 'JDBC'].includes(extraction.source)) {
        item.source = t('commonComp.apis.parameterizationVariable.listModal.staticValue');
        if (/@\w+\w*\([^)]*\)/.test(item.value)) {
          item.previewFlag = true;
          item.source += ' (mock)';
        }
      } else {
        item.previewFlag = true;
        const { source } = extraction;
        if (!extracted) {
          item.source = t('commonComp.apis.parameterizationVariable.listModal.exactValue');
          if (source === 'FILE') {
            item.source += t('commonComp.apis.parameterizationVariable.listModal.file');
          } else if (source === 'HTTP') {
            item.source += t('commonComp.apis.parameterizationVariable.listModal.http');
          } else if (source === 'JDBC') {
            item.source += t('commonComp.apis.parameterizationVariable.listModal.jdbc');
          }
        } else {
          item.source = t('commonComp.apis.parameterizationVariable.listModal.extractedValue');
          if (source === 'FILE') {
            item.source += t('commonComp.apis.parameterizationVariable.listModal.file');
          } else if (source === 'HTTP') {
            item.source += t('commonComp.apis.parameterizationVariable.listModal.http');
          } else if (source === 'JDBC') {
            item.source += t('commonComp.apis.parameterizationVariable.listModal.jdbc');
          }
        }
      }

      variableTableData.value.push(item);

      if (tableRowSelection.value) {
        if (selectedVariableNames.includes(name) && !tableRowSelection.value.selectedRowKeys.includes(name)) {
          tableRowSelection.value.selectedRowKeys.push(item.id);
        }
      }

      if (!item.passwordValue) {
        visibleVariableIdSet.value.add(item.id);
      }

      return true;
    });
  }
};

/**
 * Load variable value from API
 * @param data - Variable item to load value for
 */
const loadVariableValue = async (data: VariableDetail) => {
  const variableId = data.id;
  isLoading.value = true;
  const [error, response] = await variable.previewVariableValue({ id: data.id }, { silence: true });
  isLoading.value = false;
  if (error) {
    variableErrorMessageMap.value.set(variableId, error.message);
    return;
  }

  variableErrorMessageMap.value.delete(variableId);
  if (response?.data) {
    data.value = response.data;
  }
};

/**
 * Handle hiding variable value
 * @param data - Variable item to hide
 */
const handleHideVariableValue = (data: VariableDetail) => {
  visibleVariableIdSet.value.delete(data.id);
};

/**
 * Handle showing variable value
 * @param data - Variable item to show
 */
const handleShowVariableValue = (data: VariableDetail) => {
  const { id, value, extracted } = data;
  visibleVariableIdSet.value.add(id);

  // If it's a static variable and contains mock function, call API to get value
  if (!extracted && !/@\w+\w*\([^)]*\)/.test(value)) {
    return;
  }

  loadVariableValue(data);
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

  loadVariableData();
};

/**
 * Handle table row selection change
 * @param selectedKeys - Selected row keys
 */
const handleTableRowSelection = (selectedKeys: string[]) => {
  if (!tableRowSelection.value) {
    return;
  }

  const currentVariableIds = variableTableData.value.map(item => item.id);
  const deselectedIds = currentVariableIds.reduce((prev, cur) => {
    if (!selectedKeys.includes(cur)) {
      prev.push(cur);
    }

    return prev;
  }, [] as string[]);

  // Remove deselected variables
  for (let i = 0, len = deselectedIds.length; i < len; i++) {
    const id = deselectedIds[i];
    selectedVariableDataMap.value.delete(id);
  }

  // Remove deselected variable IDs
  const updatedSelectedRowKeys = tableRowSelection.value.selectedRowKeys.filter(item => !deselectedIds.includes(item));

  // Add newly selected variables
  for (let i = 0, len = selectedKeys.length; i < len; i++) {
    const id = selectedKeys[i];
    if (!updatedSelectedRowKeys.includes(id)) {
      updatedSelectedRowKeys.push(id);

      const variableData = variableTableData.value.find(item => item.id === id);
      if (variableData) {
        selectedVariableDataMap.value.set(id, variableData);
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
  const selectedVariableList = Array.from(selectedVariableDataMap.value.values());
  emit('ok', selectedVariableList);

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
      selectedVariableDataMap.value.clear();
      variableTableData.value = [];
      visibleVariableIdSet.value.clear();
      variableErrorMessageMap.value.clear();

      if (tableRowSelection.value) {
        tableRowSelection.value.selectedRowKeys = [];
      }

      return;
    }

    loadVariableData();
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
    title: t('commonComp.apis.parameterizationVariable.listModal.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    key: 'value',
    title: t('common.value'),
    dataIndex: 'value',
    ellipsis: true
  },
  {
    key: 'description',
    title: t('common.description'),
    dataIndex: 'description',
    ellipsis: true
  },
  {
    key: 'passwordValue',
    title: t('common.password'),
    dataIndex: 'passwordValue',
    ellipsis: true,
    width: '5%'
  },
  {
    key: 'source',
    title: t('commonComp.apis.parameterizationVariable.listModal.reference'),
    dataIndex: 'source',
    ellipsis: true,
    width: '10%'
  },
  {
    key: 'lastModifiedByName',
    title: t('common.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    ellipsis: true,
    width: '10%',
    sort: true
  },
  {
    key: 'lastModifiedDate',
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: '14%',
    sort: true
  }
];
</script>

<template>
  <Modal
    :title="t('commonComp.apis.parameterizationVariable.listModal.title')"
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

        <NoData v-if="variableTableData.length === 0" class="flex-1" />

        <Table
          v-else
          :dataSource="variableTableData"
          :columns="tableColumns"
          :pagination="paginationConfig"
          :rowSelection="tableRowSelection"
          :noDataSize="'small'"
          :noDataText="t('common.noData')"
          rowKey="id"
          class="flex-1"
          @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <div v-if="column.dataIndex === 'name'" class="flex items-center">
              <span>{{ record.name }}</span>
              <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
            </div>

            <template v-if="column.dataIndex === 'value'">
              <div v-if="record.passwordValue" class="flex items-center">
                <template v-if="visibleVariableIdSet.has(record.id)">
                  <div :title="record.value" class="flex-1 truncate">{{ record.value }}</div>
                  <Icon
                    icon="icon-zhengyan"
                    class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                    @click="handleHideVariableValue(record)" />
                </template>
                <template v-else>
                  <div class="flex-1 truncate">******</div>
                  <Icon
                    icon="icon-biyan"
                    class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                    @click="handleShowVariableValue(record)" />
                </template>
              </div>

              <div v-else class="flex items-center">
                <div
                  v-if="variableErrorMessageMap.has(record.id)"
                  :title="variableErrorMessageMap.get(record.id)"
                  class="flex-1 truncate text-status-error">
                  {{ variableErrorMessageMap.get(record.id) }}
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
                  @click="handleShowVariableValue(record)" />
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
