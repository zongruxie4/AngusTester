<script setup lang="ts">
import { computed, ref, defineAsyncComponent, Ref, inject, onMounted, watch } from 'vue';
import { Switch, Button } from 'ant-design-vue';
import { AsyncComponent, Tooltip, Icon, Spin, NoData, Input, modal, IconCopy, Table } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, http, utils, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { DataMenuKey } from '@/views/data/menu';

import { VariableItem } from './PropsType';
import { ProjectInfo } from '@/layout/types';

const { t } = useI18n();

type Props = {
  dataSource: VariableItem[];
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => []
});

const VariableListModal = defineAsyncComponent(() => import('./VariableListModal/index.vue'));

const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));

const updateVariableByIteration = ref(true);

const loading = ref(false);
const searched = ref(false);
const searchValue = ref<string>();
const pagination = ref<{ current: number; pageSize: number; total: number; showSizeChanger: false; }>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const rowSelection = ref<{
  onChange:(key: string[]) => void;
  getCheckboxProps: (data: VariableItem) => ({ disabled: boolean; });
  selectedRowKeys: string[];
}>();
const tableData = ref<VariableItem[]>([]);
const showTableData = ref<VariableItem[]>([]);

const modalVisible = ref(false);

const visibilityIdSet = ref<Set<string>>(new Set());
const errorMessageMap = ref<Map<string, string>>(new Map());

const selectedVariablesOk = (data: VariableItem[]) => {
  if (!data?.length) {
    return;
  }

  tableData.value.unshift(...data);
  if (searchValue.value) {
    showTableData.value = tableData.value.filter((item) => item.name.includes(searchValue.value));
    pagination.value.total = showTableData.value.length;
  } else {
    showTableData.value = tableData.value.map(item => item);
    pagination.value.total = tableData.value.length;
  }
};

const toUse = () => {
  modalVisible.value = true;
};

const searchInputChange = debounce(duration.search, (event: { target: { value: string; } }) => {
  searchValue.value = event.target.value;
  showTableData.value = tableData.value.filter((item) => item.name.includes(searchValue.value));
  pagination.value.total = showTableData.value.length;
  if (searchValue.value.length) {
    searched.value = true;
  } else {
    searched.value = false;
  }
});

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

const loadValue = async (data: VariableItem) => {
  const id = data.id;
  const params = {
    name: data.name,
    value: data.value,
    extraction: data.extraction
  };

  if (params.extraction?.method) {
    params.extraction.method = params.extraction.method?.value || params.extraction.method;
  }

  loading.value = true;
  const [error, res] = await http.post(`${TESTER}/variable/value/preview`, params, { silence: true });
  loading.value = false;
  if (error) {
    errorMessageMap.value.set(id, error.message);
    return;
  }

  errorMessageMap.value.delete(id);
  if (res?.data) {
    data.showValue = res.data;
  }
};

const toDelete = (data: VariableItem) => {
  modal.confirm({
    content: t('xcan_exec.variable.confirmUnreferenceVariable', { name: data.name }),
    async onOk () {
      const id = data.id;
      tableData.value = tableData.value.filter((item) => item.id !== id);
      showTableData.value = showTableData.value.filter(item => item.id !== id);
      pagination.value.total = showTableData.value.length;
      visibilityIdSet.value.delete(id);
      errorMessageMap.value.delete(id);

      if (rowSelection.value) {
        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => item !== id);
      }
    }
  });
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

  modal.confirm({
    content: t('xcan_exec.variable.confirmUnreferenceSelectedVariables', { num }),
    async onOk () {
      for (let i = 0, len = selectedRowKeys.length; i < len; i++) {
        const name = selectedRowKeys[i];
        visibilityIdSet.value.delete(name);
        errorMessageMap.value.delete(name);
      }

      tableData.value = tableData.value.filter((item) => !selectedRowKeys.includes(item.id));
      showTableData.value = showTableData.value.filter(item => !selectedRowKeys.includes(item.id));
      pagination.value.total = showTableData.value.length;
      rowSelection.value = undefined;
    }
  });
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

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

const toCancelBatchDelete = () => {
  rowSelection.value = undefined;
};

const reset = () => {
  loading.value = false;
  searched.value = false;
  searchValue.value = undefined;
  pagination.value.current = 1;
  pagination.value.total = 0;
  rowSelection.value = undefined;
  tableData.value = [];
  showTableData.value = [];
  modalVisible.value = false;
  visibilityIdSet.value.clear();
  errorMessageMap.value.clear();
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }

    tableData.value = newValue.map(item => {
      const { extraction } = item;

      let extracted = false;
      if (extraction) {
        const method = extraction.method?.value || extraction.method;
        if (['JSON_PATH', 'REGEX', 'X_PATH'].includes(method)) {
          extracted = true;
        }
      }

      if (!extraction || !['FILE', 'HTTP', 'JDBC'].includes(extraction.source)) {
        item.source = t('xcan_exec.variable.staticValue');
        if (/@\w+\w*\([^)]*\)/.test(item.value)) {
          item.preview = true;
          item.source += ' (Mock)';
        }
      } else {
        item.preview = true;
        const { source } = extraction;
        if (!extracted) {
          item.source = t('xcan_exec.variable.exactValue');
          if (source === 'FILE') {
            item.source += ` (${t('xcan_exec.variable.file')})`;
          } else if (source === 'HTTP') {
            item.source += ' (Http)';
          } else if (source === 'JDBC') {
            item.source += ' (Jdbc)';
          }
        } else {
          item.source = t('xcan_exec.variable.extractedValue');
          if (source === 'FILE') {
            item.source += ` (${t('xcan_exec.variable.file')})`;
          } else if (source === 'HTTP') {
            item.source += ' (Http)';
          } else if (source === 'JDBC') {
            item.source += ' (Jdbc)';
          }
        }
      }

      const id = item['x-id'] || utils.uuid();

      if (!item.passwordValue) {
        visibilityIdSet.value.add(id);
      }

      return {
        ...item,
        id,
        extracted,
        createdByName: item['x-createdByName']
      };
    });
    showTableData.value = tableData.value.map(item => item);
    pagination.value.total = tableData.value.length;
  }, { immediate: true });
});

const selectedNum = computed(() => rowSelection.value?.selectedRowKeys?.length);

const selectedNames = computed(() => {
  return tableData.value?.map(item => item.name);
});

const columns = [
  {
    title: t('xcan_exec.variable.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('common.value'),
    dataIndex: 'value',
    ellipsis: true
  },
  {
    title: t('common.description'),
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: t('xcan_exec.variable.reference'),
    dataIndex: 'linkName',
    ellipsis: true
  },
  {
    title: t('common.creator'),
    dataIndex: 'createdByName',
    ellipsis: true,
    width: '8%'
  },
  {
    title: t('common.actions'),
    dataIndex: 'action',
    width: 170
  }
];

const getData = (): {
  updateVariableByIteration:boolean;
  variables:{
    'x-id': string;
    'x-createdByName': string;
    name: string;
    description: string;
    passwordValue: string;
    value: string;
    extraction: VariableItem['extraction'];
  }
}[] => {
  const variables = tableData.value?.map(item => {
    const extraction = item.extraction;
    if (extraction) {
      extraction.method = extraction.method?.value || extraction.method;
      extraction.fileType = extraction.fileType?.value || extraction.fileType;
    }

    const pureData = utils.filterEmptyProps({
      extraction,
      name: item.name,
      description: item.description,
      passwordValue: item.passwordValue,
      value: item.value,
      'x-id': item.id,
      'x-createdByName': item.createdByName
    });

    return pureData;
  });

  return {
    variables,
    updateVariableByIteration: updateVariableByIteration.value
  };
};

defineExpose({
  getData
});
</script>

<template>
  <div class="text-3 leading-5">
    <div class="flex items-center mb-3.5">
      <span>{{ t('xcan_exec.variable.updateVariableBeforeEachIteration') }}</span>
      <Switch
        v-model:checked="updateVariableByIteration"
        size="small"
        class="ml-2" />
      <Tooltip :title="t('xcan_exec.variable.updateVariableTooltip')">
        <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
      </Tooltip>
    </div>

    <Spin
      :spinning="loading"
      style="height: calc(100% - 41px);"
      class="flex flex-col">
      <div v-if="!searched && tableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
        <img class="w-25" src="./images/nodata.png">
        <div class="flex items-center text-theme-sub-content leading-7">
          <span>{{ t('xcan_exec.variable.noVariablesReferenced') }}</span>

          <Button
            type="link"
            size="small"
            class="py-0 px-0 mx-1"
            @click="toUse">
            <span>{{ t('xcan_exec.variable.referenceVariable') }}</span>
          </Button>
        </div>
      </div>

      <template v-else>
        <div class="flex items-center justify-between mb-3.5 space-x-5">
          <Input
            :maxlength="150"
            allowClear
            trim
            class="w-75 flex-grow-0 flex-shrink"
            :placeholder="t('xcan_exec.variable.searchNameOrDescription')"
            @change="searchInputChange" />

          <div class="flex-shrink-0 flex items-center space-x-3">
            <template v-if="typeof selectedNum === 'number'">
              <Button
                danger
                size="small"
                class="flex items-center flex-shrink-0"
                @click="toBatchDelete">
                <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
                <div class="flex items-center">
                  <span class="mr-0.5">{{ t('xcan_exec.variable.deleteSelected') }}</span>
                  <span>({{ selectedNum }})</span>
                </div>
              </Button>

              <Button
                size="small"
                class="flex items-center flex-shrink-0"
                @click="toCancelBatchDelete">
                <Icon icon="icon-fanhui" class="mr-1" />
                <span>{{ t('xcan_exec.variable.cancelDelete') }}</span>
              </Button>
            </template>

            <template v-else>
              <Button
                type="primary"
                size="small"
                class="flex items-center flex-shrink-0"
                @click="toUse">
                <Icon icon="icon-jia" class="text-3.5" />
                <span class="ml-1">{{ t('xcan_exec.variable.referenceVariable') }}</span>
              </Button>

              <Button
                type="default"
                size="small"
                class="flex items-center flex-shrink-0"
                @click="toBatchDelete">
                <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
                <span>{{ t('xcan_exec.variable.batchDelete') }}</span>
              </Button>
            </template>
          </div>
        </div>

        <NoData
          v-if="tableData.length === 0"
          class="my-5"
          size="middle" />

        <Table
          v-else
          :dataSource="showTableData"
          :columns="columns"
          :pagination="pagination"
          :rowSelection="rowSelection"
          rowKey="id"
          class="flex-1">
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'value'">
              <div v-if="record.passwordValue" class="flex items-center">
                <template v-if="visibilityIdSet.has(record.id)">
                  <div :title="record.showValue" class="flex-1 truncate">{{ record.showValue }}</div>
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
                  :title="record.showValue"
                  class="flex-1 truncate">
                  {{ record.showValue }}
                </div>

                <Icon
                  v-if="record.preview"
                  icon="icon-zhengyan"
                  class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                  @click="toVisibility(record)" />
              </div>
            </template>

            <div v-else-if="column.dataIndex === 'linkName'" class="flex items-center">
              <span class="link flex-1 truncate" :title="record.name">{{ `{${record.name}\}` }}</span>
              <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
            </div>

            <div v-else-if="column.dataIndex === 'action'" class="flex items-center space-x-2.5">
              <Button
                type="text"
                size="small"
                class="flex items-center p-0 h-5 leading-5 space-x-1"
                @click="toDelete(record)">
                <Icon icon="icon-qingchu" class="text-3.5" />
                <span>{{ t('xcan_exec.variable.unreference') }}</span>
              </Button>

              <Button
                type="text"
                size="small"
                class="p-0 h-5 leading-5">
                <RouterLink
                  class="flex items-center space-x-1"
                  :to="`/data#${DataMenuKey.VARIABLES}?id=${record.id}`"
                  target="_blank">
                  <Icon icon="icon-zhengyan" class="text-3.5" />
                  <span>{{ t('xcan_exec.variable.viewDefinition') }}</span>
                </RouterLink>
              </Button>
            </div>
          </template>
        </Table>
      </template>
    </Spin>

    <AsyncComponent :visible="modalVisible">
      <VariableListModal
        v-model:visible="modalVisible"
        :selectedNames="selectedNames"
        :projectId="projectInfo?.id"
        @ok="selectedVariablesOk" />
    </AsyncComponent>
  </div>
</template>
