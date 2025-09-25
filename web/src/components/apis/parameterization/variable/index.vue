<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Popconfirm } from 'ant-design-vue';
import { AsyncComponent, Icon, IconCopy, Spin, Table } from '@xcan-angus/vue-ui';
import { paramTarget, variable } from '@/api/tester';

import { VariableItem } from './PropsType';

const { t } = useI18n();

type Props = {
  projectId: string;
  targetId: string;
  targetType: 'API' | 'SCENARIO' | 'API_CASE';
  dataSource: {
    'x-id': string;
    'x-createdByName': string;
    name: string;
    description: string;
    extracted: string;
    passwordValue: string;
    value: string;
    extraction: string;
  }[];
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  targetId: undefined,
  targetType: undefined,
  dataSource: () => []
});

const VariableListModal = defineAsyncComponent(() => import('@/components/apis/parameterization/variable/listModal/index.vue'));

const loaded = ref(false);
const loading = ref(false);
const tableData = ref<VariableItem[]>([]);
const modalVisible = ref(false);
const visibilityIdSet = ref<Set<string>>(new Set());
const errorMessageMap = ref<Map<string, string>>(new Map());

const toUse = () => {
  modalVisible.value = true;
};

const selectedVariablesOk = async (data: VariableItem[]) => {
  if (!data?.length) {
    return;
  }

  const ids = data.map((item) => item.id);
  loading.value = true;
  const [error] = await paramTarget.addVariable(props.targetId, props.targetType, ids);
  loading.value = false;
  if (error) {
    return;
  }

  tableData.value.unshift(...data);
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
  const [error, res] = await variable.previewVariableValue(params, { silence: true });
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

const toDelete = async (data: VariableItem) => {
  const id = data.id;
  loading.value = true;
  const [error] = await paramTarget.deleteVariable(props.targetId, props.targetType, [id], { dataType: true });
  loading.value = false;
  if (error) {
    return;
  }

  tableData.value = tableData.value.filter((item) => item.id !== id);
  visibilityIdSet.value.delete(id);
  errorMessageMap.value.delete(id);
};

const loadData = async () => {
  loading.value = true;
  const [error, res] = await paramTarget.getVariable(props.targetId, props.targetType);
  loading.value = false;
  loaded.value = true;
  if (error) {
    return;
  }

  const list = res?.data || [];
  if (!list.length) {
    return;
  }

  tableData.value = list.map(item => {
    const { extraction, id } = item;
    if (!extraction || !['FILE', 'http', 'JDBC'].includes(extraction.source)) {
      if (/@\w+\w*\([^)]*\)/.test(item.value)) {
        item.previewFlag = true;
      }
    } else {
      item.previewFlag = true;
    }

    if (!item.passwordValue) {
      visibilityIdSet.value.add(id);
    }

    return item;
  });
};

const reset = () => {
  loaded.value = false;
  loading.value = false;
  tableData.value = [];
  modalVisible.value = false;
  visibilityIdSet.value.clear();
  errorMessageMap.value.clear();
};

onMounted(() => {
  watch(() => props.targetId, (newValue) => {
    reset();

    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const selectedNames = computed(() => {
  return tableData.value?.map(item => item.name);
});

const columns = [
  {
    title: t('commonComp.apis.parameterizationVariable.name'),
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
    title: t('commonComp.apis.parameterizationVariable.reference'),
    dataIndex: 'linkName',
    ellipsis: true
  },
  {
    title: t('commonComp.apis.parameterizationVariable.creator'),
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
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center flex-nowrap mb-1.5">
      <div class="flex-shrink-0 w-1 h-3.5 rounded bg-blue-400 mr-1.5"></div>
      <div class="flex-shrink-0 text-theme-title mr-2.5">{{ t('commonComp.apis.parameterizationVariable.title') }}</div>
      <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 mr-1" />
      <div class="flex-shrink-0 break-all whitespace-pre-wrap">{{ t('commonComp.apis.parameterizationVariable.hints') }}</div>
    </div>
    <div class="mb-2">
      <Button
        type="link"
        size="small"
        class="flex items-center h-5 leading-5 p-0 space-x-1"
        @click="toUse">
        <Icon icon="icon-jia" class="text-3.5" />
        <span class="ml-1">{{ t('commonComp.apis.parameterizationVariable.addVariable') }}</span>
      </Button>
    </div>

    <template v-if="loaded">
      <div v-if=" tableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
        <img style="width:100px;" src="../../../../assets/images/nodata.png">
        <div class="flex items-center text-theme-sub-content text-3">
          <span>{{ t('commonComp.apis.parameterizationVariable.noDataMessage') }}</span>
        </div>
      </div>

      <Table
        v-else
        :dataSource="tableData"
        :columns="columns"
        :pagination="false"
        rowKey="id">
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
                v-if="record.previewFlag"
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
            <Popconfirm :title="t('commonComp.apis.parameterizationVariable.cancelReferenceConfirm', { name: record.name })" @confirm="toDelete(record)">
              <Button
                :title="t('commonComp.apis.parameterizationVariable.cancelReference')"
                type="text"
                size="small"
                class="flex items-center p-0 h-5 leading-5 space-x-1">
                <Icon icon="icon-qingchu" class="text-3.5" />
              </Button>
            </Popconfirm>

            <Button
              :title="t('commonComp.apis.parameterizationVariable.viewDefinition')"
              type="text"
              size="small"
              class="p-0 h-5 leading-5">
              <RouterLink
                class="flex items-center space-x-1"
                :to="`/data#variables?id=${record.id}`"
                target="_blank">
                <Icon icon="icon-zhengyan" class="text-3.5" />
              </RouterLink>
            </Button>
          </div>
        </template>
      </Table>
    </template>

    <AsyncComponent :visible="modalVisible">
      <VariableListModal
        v-model:visible="modalVisible"
        :selectedNames="selectedNames"
        :projectId="props.projectId"
        @ok="selectedVariablesOk" />
    </AsyncComponent>
  </Spin>
</template>

<style>
.ant-popover.ant-popconfirm .ant-popover-message {
  display: flex;
  align-items: start;
  font-size: 12px;
  line-height: 18px;
}

.ant-popover.ant-popconfirm .ant-popover-message>.anticon {
  position: static;
  margin-right: 6px;
  transform: translateY(2px);
  font-size: 12px;
}

.ant-popover.ant-popconfirm .ant-popover-message>.ant-popover-message-title {
  padding-left: 0;
}
</style>
