<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import { Button } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { issue } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import {TestMenuKey} from '@/views/test/menu';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import TestResult from '@/components/TestResult/index.vue';

const SelectCaseByModuleModal = defineAsyncComponent(() => import('@/components/function/case/SelectByModuleModal.vue'));

/**
 * Props interface for AssocCase component
 */
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  taskId: undefined,
  dataSource: undefined,
  tips: ''
});

// Composables
const router = useRouter();
const { t } = useI18n();

/**
 * Event emitter for component communication
 */

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

/**
 * Loading state for submit operations
 */
const isSubmitLoading = ref(false);

/**
 * Visibility state for the case selection modal
 */
const isCaseSelectionModalVisible = ref(false);

/**
 * Closes the case selection modal
 */
const closeCaseSelectionModal = () => {
  isCaseSelectionModalVisible.value = false;
};

/**
 * Opens the case selection modal
 */
const openCaseSelectionModal = () => {
  isCaseSelectionModalVisible.value = true;
};

/**
 * Handles the association of selected test cases with the current task
 * <p>
 * Processes the selected case IDs and creates associations through the API.
 * Updates the loading state and emits success events upon completion.
 *
 * @param selectedCaseIds - Array of case IDs to associate with the task
 */
const handleCaseAssociation = async (selectedCaseIds: number[]) => {
  isCaseSelectionModalVisible.value = false;

  if (!selectedCaseIds.length) {
    closeCaseSelectionModal();
    return;
  }
  isSubmitLoading.value = true;

  const [error] = await issue.associationCase(props.taskId as number, selectedCaseIds, {
    paramsType: true
  });

  isSubmitLoading.value = false;
  if (error) {
    return;
  }

  emit('editSuccess');
};

/**
 * Handles the removal of a test case association
 * <p>
 * Shows a confirmation dialog and removes the association between
 * the specified test case and the current task.
 *
 * @param caseRecord - The test case record to disassociate
 */
const handleCaseDisassociation = (caseRecord: any) => {
  modal.confirm({
    content: t('actions.tips.confirmCancelAssoc', { name: caseRecord.name }),
    onOk () {
      return issue.cancelAssociationCase(props.taskId as number, [caseRecord.id], {
        paramsType: true
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
 * Navigates to the test case detail page
 * <p>
 * Opens the specified test case in a new tab for detailed viewing
 *
 * @param caseRecord - The test case record to open
 */
const navigateToCaseDetail = (caseRecord: any) => {
  router.push(`/test#${TestMenuKey.CASES}?id=${caseRecord.id}`);
};

/**
 * Table column definitions for the associated test cases
 * <p>
 * Defines the structure and display properties for each column
 * in the test case association table
 */
const tableColumns = [
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
    <!-- Header section with description and action button -->
    <div class="flex mb-2 items-center pr-2">
      <div class="flex-1 min-w-0 truncate px-1">
        <Hints :text="props.tips" />
      </div>
      <Button
        :disabled="(props.dataSource as any)?.refCaseInfos?.length > 19"
        :loading="isSubmitLoading"
        size="small"
        @click="openCaseSelectionModal">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('common.assocCases') }}
      </Button>
    </div>

    <!-- Test case association table -->
    <Table
      :columns="tableColumns"
      :dataSource="(props.dataSource as any) || []"
      :pagination="false"
      size="small"
      noDataSize="small"
      noDataText="">
      <template #bodyCell="{column, record}">
        <!-- Case name with navigation link -->
        <template v-if="column.dataIndex === 'name'">
          <Button
            type="link"
            size="small"
            @click="navigateToCaseDetail(record)">
            {{ record.name }}
          </Button>
        </template>

        <!-- Action buttons for case disassociation -->
        <template v-if="column.dataIndex === 'action'">
          <Button
            size="small"
            type="text"
            @click="handleCaseDisassociation(record)">
            <Icon icon="icon-qingchu" class="mr-1" />
            {{ t('actions.cancel') }}
          </Button>
        </template>

        <!-- Priority display component -->
        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record?.priority" />
        </template>

        <!-- Status display with conditional rendering -->
        <template v-if="column.dataIndex === 'status'">
          <ReviewStatus
            v-if="record?.reviewStatus?.value === FuncPlanStatus.PENDING && record.review"
            :value="record.reviewStatus" />
          <TestResult v-else :value="record.testResult" />
        </template>
      </template>
    </Table>

    <!-- Case selection modal -->
    <AsyncComponent :visible="isCaseSelectionModalVisible">
      <SelectCaseByModuleModal
        v-model:visible="isCaseSelectionModalVisible"
        :projectId="props.projectId"
        :action="`${TESTER}/task/${props.taskId}/case/notAssociated`"
        @ok="handleCaseAssociation" />
    </AsyncComponent>
  </div>
</template>
