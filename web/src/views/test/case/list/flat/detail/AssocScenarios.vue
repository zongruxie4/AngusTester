<script lang="ts" setup>
import { defineAsyncComponent, ref, onMounted, watch } from 'vue';
import { AsyncComponent, Hints, Icon, modal, Table, notification } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { testCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TESTER, http } from '@xcan-angus/infra';
import { AssocScenarioProps } from '@/views/test/case/types';
import { useRouter } from 'vue-router';


const SelectScenarioByModuleModal = defineAsyncComponent(() => import('@/components/function/SelectScenarioByModuleModal.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/scenario/scenario/list/ExecTest.vue'));


interface Props extends AssocScenarioProps {
  assocScenariosCount: number;
}
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  caseId: undefined,
  assocScenariosCount: 0
});

const { t } = useI18n();
const router = useRouter();
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
  (event: 'update:assocScenariosCount', value: number): void;
}>();

const isSubmitting = ref(false);
const isEditMode = ref(false);

const isSelectCaseModalVisible = ref(false);
const isExecScenarioModalVisible = ref(false);
const selectedId = ref<string>();
const selectedScenarioIds = ref<string[]>([]);

const dataSource = ref([]);
const isLoading = ref(false);

/**
 * Cancel current edit flow and close edit mode
 */
const cancelCaseSelectionModal = () => {
  isEditMode.value = false;
};

/**
 * Start edit flow by opening select-case modal
 */
const openCaseSelectionModal = () => {
  isSelectCaseModalVisible.value = true;
};

/**
 * Submit association of selected case IDs to the current case
 * @param selectedCaseIds - Selected associated case IDs
 */
const handleAssociateDesign = async (selectedCaseIds) => {
  isSelectCaseModalVisible.value = false;
  if (!selectedCaseIds.length) {
    cancelCaseSelectionModal();
    return;
  }
  isSubmitting.value = true;
  const [error] = await testCase.putAssociationScenario(props.caseId, selectedCaseIds);
  isSubmitting.value = false;
  if (error) {
    return;
  }
  loadScenarioList();
};

/**
 * Remove association for a single linked case record
 * @param caseRecord - The associated case record to remove
 */
const handleRemoveCaseAssociation = (scenarioRecord) => {
  modal.confirm({
    content: t('actions.tips.confirmCancelAssoc', { name: scenarioRecord.name }),
    onOk () {
      return testCase.deleteAssociationScenario(props.caseId, [scenarioRecord.id]).then(([error]) => {
        if (error) {
          return;
        }
        loadScenarioList();
      });
    }
  });
};

const handleExecScenario = async (scenarioRecord) => {
  selectedId.value = scenarioRecord.id;
  isExecScenarioModalVisible.value = true;
};

const handleBatchExecScenarios = () => {
  selectedScenarioIds.value = dataSource.value.map((item: any) => item.id);
  handleExecScenarioOk([]);
};

const handleExecScenarioOk = async(params: any) => {
  Promise.all(selectedScenarioIds.value.map((id: string) => {
    return http.put(`${TESTER}/scenario/${id || ''}/exec`, params, { dataType: true });
  })).then(()=> {
    notification.success(t('actions.tips.execSuccess'));
    loadScenarioList();
  }).finally(() => {
    selectedScenarioIds.value = [];
  });
};

const tableRowSelection = ref({
  onChange: (selectedRowKeys: string[]) => {
    selectedScenarioIds.value = selectedRowKeys;
  },
  getCheckboxProps: (record: any) => {
    return {
      disabled: !!record.lastExecId
    }
  },
  hideSelectAll: true
});

const resBgColorMap = {
  CREATED: 'rgba(45, 142, 255, 1)',
  PENDING: 'rgba(45, 142, 255, 1)',
  RUNNING: 'rgba(103, 215, 255, 1)',
  STOPPED: 'rgba(245, 34, 45, 1)',
  FAILED: 'rgba(245, 34, 45, 1)',
  TIMEOUT: 'rgba(245, 34, 45, 1)',
  COMPLETED: 'rgba(82, 196, 26, 1)',
  '': 'gray'
};

const columns = [
  {
    key: 'name',
    dataIndex: 'name',
    title: t('common.scenarioName'),
  },
  {
    key: 'plugin',
    dataIndex: 'plugin',
    title: t('common.type'),
    width: 80
  },
  {
    key: 'lastExecName',
    dataIndex: 'lastExecName',
    title: t('common.execName'),
    width: 130
  },
  {
    key: 'scriptType',
    dataIndex: 'scriptType',
    title: t('common.scriptType'),
    width: 120,
    customRender: ({text}) => {
      return text?.message;
    }
  },
  {
    key: 'lastExecStatus',
    dataIndex: 'lastExecStatus',
    title: t('common.execStatus'),
    width: 120,
    customRender: ({text}) => {
      return text?.message || t('scenario.list.table.noExecute')
    }
  },
  {
    key: 'lastExecFailureMessage',
    dataIndex: 'lastExecFailureMessage',
    title: t('common.failureReason'),
    width: 140
  },
  {
    key: 'lastExecDate',
    dataIndex: 'lastExecDate',
    title: t('common.execDate'),
    width: 120
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('common.actions'),
    width: 110
  }
];

const loadScenarioList = async () => {
  if (isLoading.value) {
    return;
  }
  isLoading.value = true;
  const [error, response] = await testCase.getAssociationScenario(props.caseId);
  if (error) {
    return;
  }
  isLoading.value = false;
  dataSource.value = response?.data || [];
  emit('update:assocScenariosCount', dataSource.value.length);
};

const handleOpenScenarioDetail = (record: any) => {
  router.push(`/scenario#scenario?id=${record.id}&name=${record.name}&plugin=${record.plugin}&type=detail`);
};

onMounted(() => {
  watch(() => props.caseId, (newCaseId) => {
    if (newCaseId) {
      loadScenarioList();
    }
  }, { immediate: true });
});

</script>
<template>
  <div>
    <div class="flex mb-2 items-center pr-2">
      <div class="flex-1 ml-1 min-w-0 truncate">
        <Hints :text="t('testCase.messages.assocScenarioTip')" />
      </div>
      <Button
        size="small"
        type="text"
        :disabled="!selectedScenarioIds.length"
        @click="handleBatchExecScenarios">
        <Icon icon="icon-zhihang" class="mr-1" />
        {{ t('testCase.actions.batchExec') }}
      </Button>
      <Button
        :disabled="dataSource?.length > 19"
        :loading="isSubmitting"
        size="small"
        @click="openCaseSelectionModal">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('testCase.actions.assocScenarios') }}
      </Button>
    </div>

    <Table
      :columns="columns"
      :dataSource="dataSource || []"
      :pagination="false"
      :rowSelection="tableRowSelection"
      :loading="isLoading"
      rowKey="id"
      size="small"
      noDataSize="small"
      :noDataText="t('common.noData')">
      <template #bodyCell="{column, record}">
        <template v-if="column.dataIndex === 'name'">
          <Button
            type="link"
            size="small"
            @click="handleOpenScenarioDetail(record)">
            {{ record.name }}
          </Button>
        </template>

        <template v-if="column.dataIndex === 'lastExecName'">
          <RouterLink :to="`/execution/info/${record.lastExecId}`">
            {{ record.lastExecName }}
          </RouterLink>
        </template>

        <template v-if="column.dataIndex === 'execName'">
          <Button
            type="link"
            size="small">
            {{ record.execName }}
          </Button>
        </template>

        <template v-if="column.dataIndex === 'lastExecName'">
          {{ record.plugin?.message }}
        </template>

        <template v-if="column.dataIndex === 'lastExecStatus'">
          <span :style="{ color: resBgColorMap[record.lastExecStatus?.value || ''] }">
            {{ record.lastExecStatus?.message || t('scenario.list.table.noExecute') }}
          </span>
        </template>

        <template v-if="column.dataIndex === 'action'">
          <div class="flex items-center space-x-1">
            <Button
              :disabled="!!record.lastExecId"
              size="small"
              type="text"
              @click="handleExecScenario(record)">
              <Icon icon="icon-zhihang" class="mr-1" />
              执行
            </Button>
            <Button
              size="small"
              type="text"
              @click="handleRemoveCaseAssociation(record)">
              <Icon icon="icon-qingchu" class="mr-1" />
              {{ t('actions.cancel') }}
            </Button>
          </div>
          
        </template>
      </template>
    </Table>

    <AsyncComponent :visible="isSelectCaseModalVisible">
      <SelectScenarioByModuleModal
        v-model:visible="isSelectCaseModalVisible"
        :projectId="props.projectId"
        :hadSelectedIds="dataSource.map((item: any) => item.id)"
        @ok="handleAssociateDesign" />
    </AsyncComponent>
    <AsyncComponent :visible="isExecScenarioModalVisible">
      <ExecTestModal
        v-model:scenarioId="selectedId"
        v-model:scenarioIds="selectedScenarioIds"
        v-model:visible="isExecScenarioModalVisible"
        @ok="handleExecScenarioOk" />
    </AsyncComponent>
  </div>
</template>
