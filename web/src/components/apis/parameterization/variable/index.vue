<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Popconfirm } from 'ant-design-vue';
import { AsyncComponent, Icon, IconCopy, Spin, Table } from '@xcan-angus/vue-ui';
import { paramTarget, variable } from '@/api/tester';
import { VariableDetail } from '@/views/data/variable/types';

const { t } = useI18n();

/**
 * Component props interface for variable parameterization
 */
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

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  targetId: undefined,
  targetType: undefined,
  dataSource: () => []
});

// Async component imports
const VariableListModal = defineAsyncComponent(() => import('@/components/apis/parameterization/variable/ListModal.vue'));

// Component state
const isDataLoaded = ref(false);
const isLoading = ref(false);
const variableTableData = ref<VariableDetail[]>([]);
const isModalVisible = ref(false);
const visibleVariableIdSet = ref<Set<string>>(new Set());
const variableErrorMessageMap = ref<Map<string, string>>(new Map());

/**
 * Handle opening variable selection modal
 */
const handleOpenVariableModal = () => {
  isModalVisible.value = true;
};

/**
 * Handle selected variables confirmation
 * @param data - Selected variable items
 */
const handleSelectedVariablesConfirmation = async (data: VariableDetail[]) => {
  if (!data?.length) {
    return;
  }

  const variableIds = data.map((item) => item.id);
  isLoading.value = true;
  const [error] = await paramTarget.addVariable(props.targetId, props.targetType, variableIds);
  isLoading.value = false;
  if (error) {
    return;
  }

  variableTableData.value.unshift(...data);
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
 * Load variable value from API
 * @param data - Variable item to load value for
 */
const loadVariableValue = async (data: VariableDetail) => {
  const variableId = data.id;
  const requestParams = {
    name: data.name,
    value: data.value,
    extraction: data.extraction
  };

  if (requestParams.extraction?.method) {
    requestParams.extraction.method = requestParams.extraction.method?.value || (requestParams.extraction.method as any);
  }

  isLoading.value = true;
  const [error, response] = await variable.previewVariableValue(requestParams, { silence: true });
  isLoading.value = false;
  if (error) {
    variableErrorMessageMap.value.set(variableId, error.message);
    return;
  }

  variableErrorMessageMap.value.delete(variableId);
  if (response?.data) {
    data.showValue = response.data;
  }
};

/**
 * Handle variable deletion
 * @param data - Variable item to delete
 */
const handleVariableDeletion = async (data: VariableDetail) => {
  const variableId = data.id;
  isLoading.value = true;
  const [error] = await paramTarget.deleteVariable(props.targetId, props.targetType, [variableId], { dataType: true });
  isLoading.value = false;
  if (error) {
    return;
  }

  variableTableData.value = variableTableData.value.filter((item) => item.id !== variableId);
  visibleVariableIdSet.value.delete(variableId);
  variableErrorMessageMap.value.delete(variableId);
};

/**
 * Load variable data from API
 */
const loadVariableData = async () => {
  isLoading.value = true;
  const [error, response] = await paramTarget.getVariable(props.targetId, props.targetType);
  isLoading.value = false;
  isDataLoaded.value = true;
  if (error) {
    return;
  }

  const variableList = response?.data || [];
  if (!variableList.length) {
    return;
  }

  variableTableData.value = variableList.map(item => {
    const { extraction, id } = item;
    if (!extraction || !['FILE', 'http', 'JDBC'].includes(extraction.source)) {
      if (/@\w+\w*\([^)]*\)/.test(item.value)) {
        item.previewFlag = true;
      }
    } else {
      item.previewFlag = true;
    }

    if (!item.passwordValue) {
      visibleVariableIdSet.value.add(id);
    }

    return item;
  });
};

/**
 * Reset component state
 */
const resetComponentState = () => {
  isDataLoaded.value = false;
  isLoading.value = false;
  variableTableData.value = [];
  isModalVisible.value = false;
  visibleVariableIdSet.value.clear();
  variableErrorMessageMap.value.clear();
};

// Watch for target ID changes
onMounted(() => {
  watch(() => props.targetId, (newValue) => {
    resetComponentState();

    if (!newValue) {
      return;
    }

    loadVariableData();
  }, { immediate: true });
});

// Computed properties
const selectedVariableNames = computed(() => {
  return variableTableData.value?.map(item => item.name);
});

// Table column configuration
const tableColumns = [
  {
    key: 'name',
    title: t('commonComp.apis.parameterizationVariable.name'),
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
    key: 'linkName',
    title: t('commonComp.apis.parameterizationVariable.reference'),
    dataIndex: 'linkName',
    ellipsis: true
  },
  {
    key: 'createdByName',
    title: t('common.creator'),
    dataIndex: 'createdByName',
    ellipsis: true,
    width: '8%'
  },
  {
    key: 'action',
    title: t('common.actions'),
    dataIndex: 'action',
    width: 170
  }
];
</script>

<template>
  <Spin :spinning="isLoading" class="text-3 leading-5">
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
        @click="handleOpenVariableModal">
        <Icon icon="icon-jia" class="text-3.5" />
        <span class="ml-1">{{ t('commonComp.apis.parameterizationVariable.addVariable') }}</span>
      </Button>
    </div>

    <template v-if="isDataLoaded">
      <div v-if=" variableTableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
        <img style="width:100px;" src="../../../../assets/images/nodata.png">
        <div class="flex items-center text-theme-sub-content text-3">
          <span>{{ t('commonComp.apis.parameterizationVariable.noDataMessage') }}</span>
        </div>
      </div>

      <Table
        v-else
        :dataSource="variableTableData"
        :columns="tableColumns"
        :pagination="false"
        :noDataSize="'small'"
        rowKey="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'value'">
            <div v-if="record.passwordValue" class="flex items-center">
              <template v-if="visibleVariableIdSet.has(record.id)">
                <div :title="record.showValue" class="flex-1 truncate">{{ record.showValue }}</div>
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
                :title="record.showValue"
                class="flex-1 truncate">
                {{ record.showValue }}
              </div>

              <Icon
                v-if="record.previewFlag"
                icon="icon-zhengyan"
                class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                @click="handleShowVariableValue(record)" />
            </div>
          </template>

          <div v-else-if="column.dataIndex === 'linkName'" class="flex items-center">
            <span class="link flex-1 truncate" :title="record.name">{{ `{${record.name}\}` }}</span>
            <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
          </div>

          <div v-else-if="column.dataIndex === 'action'" class="flex items-center space-x-2.5">
            <Popconfirm :title="t('commonComp.apis.parameterizationVariable.cancelReferenceConfirm', { name: record.name })" @confirm="handleVariableDeletion(record)">
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
                :to="`/data#${DataMenuKey.VARIABLES}?id=${record.id}`"
                target="_blank">
                <Icon icon="icon-zhengyan" class="text-3.5" />
              </RouterLink>
            </Button>
          </div>
        </template>
      </Table>
    </template>

    <AsyncComponent :visible="isModalVisible">
      <VariableListModal
        v-model:visible="isModalVisible"
        :selectedNames="selectedVariableNames"
        :projectId="props.projectId"
        @ok="handleSelectedVariablesConfirmation" />
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
