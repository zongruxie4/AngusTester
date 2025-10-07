<script lang="ts" setup>
import { defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { TESTER, ReviewStatus as ReviewStatusEnum } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import { funcCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { AssocCaseProps } from '@/views/test/case/types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import TestResult from '@/components/TestResult/index.vue';

const SelectCaseByModuleModal = defineAsyncComponent(() => import('@/components/function/case/SelectByModuleModal.vue'));

const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  caseId: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

const addTabPane = inject('addTabPane', (value: any) => value);

const submitLoading = ref(false);
const editRef = ref(false);

const selectCaseVisible = ref(false);

/**
 * Cancel current edit flow and close edit mode
 */
const cancelEdit = () => {
  editRef.value = false;
};

/**
 * Start edit flow by opening select-case modal
 */
const startEdit = () => {
  selectCaseVisible.value = true;
};

/**
 * Submit association of selected case IDs to the current case
 * @param refCaseIds - Selected associated case IDs
 */
const handlePut = async (refCaseIds) => {
  selectCaseVisible.value = false;
  if (!refCaseIds.length) {
    cancelEdit();
    return;
  }
  submitLoading.value = true;
  const [error] = await funcCase.putAssociationCase(props.caseId, {
    assocCaseIds: refCaseIds
  });
  submitLoading.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

/**
 * Remove association for a single linked case record
 * @param record - The associated case record to remove
 */
const handleDelTask = (record) => {
  modal.confirm({
    content: t('testCase.kanbanView.assocCase.confirmCancelAssocCase', { name: record.name }),
    onOk () {
      return funcCase.cancelAssociationCase(props.caseId, {
        assocCaseIds: [record.id]
      }).then(([error]) => {
        if (error) {
          return;
        }
        emit('editSuccess');
      });
    }
  });
};

/**
 * Open a new tab pointing to the case info view for a record
 * @param record - The case record to open
 */
const openCaseTab = (record) => {
  addTabPane({
    _id: record.id,
    name: record.caseName,
    type: 'caseInfo',
    closable: true,
    caseId: record.id
  });
};

const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('common.code')
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('common.name')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('common.priority')
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('common.evalWorkload')
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('common.status')
  },
  {
    key: 'testerName',
    dataIndex: 'testerName',
    title: t('common.tester')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('common.deadlineDate')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('common.actions')
  }
];
</script>
<template>
  <div>
    <div class="flex mb-2 items-center">
    </div>
    <div class="flex-1 truncate min-w-0 px-1">
      <Hints :text="t('testCase.kanbanView.assocCase.associateCaseTip')" />
    </div>
    <Button
      :disabled="props.dataSource?.length > 19"
      :loading="submitLoading"
      size="small"
      @click="startEdit">
      <Icon icon="icon-jia" class="mr-1" />
      {{ t('testCase.kanbanView.assocCase.associateCase') }}
    </Button>
  </div>

  <Table
    :columns="columns"
    :dataSource="props.dataSource || []"
    :pagination="false"
    size="small"
    noDataSize="small"
    :noDataText="t('common.noData')">
    <template #bodyCell="{column, record}">
      <template v-if="column.dataIndex === 'name'">
        <Button
          type="link"
          size="small"
          @click="openCaseTab(record)">
          {{ record.name }}
        </Button>
      </template>

      <template v-if="column.dataIndex === 'action'">
        <Button
          size="small"
          type="text"
          @click="handleDelTask(record)">
          <Icon icon="icon-qingchu" class="mr-1" />
          {{ t('actions.cancel') }}
        </Button>
      </template>

      <template v-if="column.dataIndex === 'priority'">
        <TaskPriority :value="record?.priority" />
      </template>

      <template v-if="column.dataIndex === 'status'">
        <ReviewStatus v-if="record?.reviewStatus?.value !== ReviewStatusEnum.PASSED && record.review" :value="record.reviewStatus" />
        <TestResult v-else :value="record.testResult" />
      </template>
    </template>
  </Table>

  <AsyncComponent :visible="selectCaseVisible">
    <SelectCaseByModuleModal
      v-model:visible="selectCaseVisible"
      :projectId="props.projectId"
      :action="`${TESTER}/func/case/${props.caseId}/case/notAssociated`"
      @ok="handlePut" />
  </AsyncComponent>
</template>
