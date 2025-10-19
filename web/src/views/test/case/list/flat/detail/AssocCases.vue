<script lang="ts" setup>
import { defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { TESTER, ReviewStatus as ReviewStatusEnum } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import { testCase } from '@/api/tester';
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

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

const addTabPane = inject('addTabPane', (value: any) => value);

const isSubmitting = ref(false);
const isEditMode = ref(false);

const isSelectCaseModalVisible = ref(false);

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
const handleAssociateCases = async (selectedCaseIds) => {
  isSelectCaseModalVisible.value = false;
  if (!selectedCaseIds.length) {
    cancelCaseSelectionModal();
    return;
  }
  isSubmitting.value = true;
  const [error] = await testCase.putAssociationCase(props.caseId, selectedCaseIds);
  isSubmitting.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

/**
 * Remove association for a single linked case record
 * @param caseRecord - The associated case record to remove
 */
const handleRemoveCaseAssociation = (caseRecord) => {
  modal.confirm({
    content: t('actions.tips.confirmCancelAssoc', { name: caseRecord.name }),
    onOk () {
      return testCase.cancelAssociationCase(props.caseId, [caseRecord.id]).then(([error]) => {
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
 * @param caseRecord - The case record to open
 */
const openCaseDetailTab = (caseRecord) => {
  addTabPane({
    _id: caseRecord.id,
    name: caseRecord.caseName,
    type: 'caseInfo',
    closable: true,
    caseId: caseRecord.id
  });
};

const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('common.code'),
    width: 130
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('common.name')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('common.priority'),
    width: 120
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('common.evalWorkload'),
    width: 150,
    customRender: ({ text }) => text || '--'
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('common.status'),
    width: 120
  },
  {
    key: 'testerName',
    dataIndex: 'testerName',
    title: t('common.tester'),
    width: 140,
    customRender: ({ text }) => text || '--'
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('common.deadlineDate'),
    width: 150,
    customRender: ({ text }) => text || '--'
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('common.actions'),
    width: 110
  }
];
</script>
<template>
  <div>
    <div class="flex mb-2 items-center pr-2">
      <div class="flex-1 ml-1 min-w-0 truncate">
        <Hints :text="t('testCase.messages.assocCaseTip')" />
      </div>
      <Button
        :disabled="props.dataSource?.length > 19"
        :loading="isSubmitting"
        size="small"
        @click="openCaseSelectionModal">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('testCase.actions.assocCases') }}
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
            @click="openCaseDetailTab(record)">
            {{ record.name }}
          </Button>
        </template>

        <template v-if="column.dataIndex === 'action'">
          <Button
            size="small"
            type="text"
            @click="handleRemoveCaseAssociation(record)">
            <Icon icon="icon-qingchu" class="mr-1" />
            {{ t('actions.cancel') }}
          </Button>
        </template>

        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record?.priority" />
        </template>

        <template v-if="column.dataIndex === 'status'">
          <ReviewStatus
            v-if="record?.reviewStatus?.value !== ReviewStatusEnum.PASSED && record.review"
            :value="record.reviewStatus" />
          <TestResult v-else :value="record.testResult" />
        </template>
      </template>
    </Table>

    <AsyncComponent :visible="isSelectCaseModalVisible">
      <SelectCaseByModuleModal
        v-model:visible="isSelectCaseModalVisible"
        :projectId="props.projectId"
        :action="`${TESTER}/func/case/${props.caseId}/case/notAssociated`"
        @ok="handleAssociateCases" />
    </AsyncComponent>
  </div>
</template>
