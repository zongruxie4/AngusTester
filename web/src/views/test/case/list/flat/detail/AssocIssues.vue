<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, Table } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { Button, Progress } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { testCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskType } from '@/enums/enums';
import { AssocTaskProps } from '@/views/test/case/types';
import { IssueMenuKey } from '@/views/issue/menu';

import TaskPriority from '@/components/TaskPriority/index.vue';
import TaskStatus from '@/components/TaskStatus/index.vue';
const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/SelectByModuleModal.vue'));

const router = useRouter();
const { t } = useI18n();

const props = withDefaults(defineProps<AssocTaskProps>(), {
  projectId: undefined,
  userInfo: undefined,
  caseId: undefined,
  dataSource: undefined,
  title: 'Task',
  taskType: TaskType.TASK,
  tip: ''
});

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

const isSubmitting = ref(false);
const isSelectTaskModalVisible = ref(false);

/**
 * Get filtered table data by current task type
 */
const filteredTaskData = computed(() => {
  return (props.dataSource || []).filter(task => task.taskType.value === props.taskType);
});

/**
 * Cancel edit flow and close task selector
 */
const cancelTaskSelectionModal = () => {
  // isEditMode.value = false;
  isSelectTaskModalVisible.value = false;
};

/**
 * Start edit flow and open task selector
 */
const openTaskSelectionModal = () => {
  // isEditMode.value = true;
  isSelectTaskModalVisible.value = true;
};

/**
 * Submit association of selected tasks to the current case
 * @param selectedTaskIds - Selected task IDs
 */
const handleAssociateTasks = async (selectedTaskIds) => {
  isSelectTaskModalVisible.value = false;
  if (!selectedTaskIds.length) {
    cancelTaskSelectionModal();
    return;
  }
  isSubmitting.value = true;
  const [error] = await testCase.putAssociationTask(props.caseId as number, selectedTaskIds);
  isSubmitting.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

/**
 * Remove association for one linked task record
 * @param taskRecord - The associated task record to remove
 */
const handleRemoveTaskAssociation = (taskRecord) => {
  modal.confirm({
    content: t('actions.tips.confirmCancelAssoc', { name: taskRecord.name }),
    onOk () {
      return testCase.cancelAssociationTask(props.caseId as number, [taskRecord.id]).then(([error]) => {
        if (error) {
          return;
        }
        emit('editSuccess');
      });
    }
  });
};

/**
 * Open a task detail page in router by ID
 * @param taskRecord - The task record to open
 */
const openTaskDetail = (taskRecord) => {
  router.push(`/issue#${IssueMenuKey.ISSUE}?taskId=${taskRecord.id}`);
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
    key: 'progress',
    dataIndex: 'progress',
    title: t('common.progress'),
    width: 140
  },
  {
    key: 'taskType',
    dataIndex: 'taskType',
    title: t('common.type'),
    width: 120
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
    customRender: ({ text }) => text || '--',
    width: 150
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('common.status'),
    width: 120
  },
  {
    key: 'assigneeName',
    dataIndex: 'assigneeName',
    title: t('common.assignee'),
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
        @click="openTaskSelectionModal">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('testCase.actions.assocIssues', {name: props.title}) }}
      </Button>
    </div>

    <Table
      :columns="columns"
      :dataSource="filteredTaskData || []"
      :pagination="false"
      size="small"
      noDataSize="small"
      :noDataText="t('common.noData')">
      <template #bodyCell="{column, record}">
        <template v-if="column.dataIndex === 'name'">
          <Button
            type="link"
            size="small"
            @click="openTaskDetail(record)">
            {{ record.name }}
          </Button>
        </template>

        <template v-if="column.dataIndex === 'progress'">
          <Progress
            :percent="+record?.progress?.completedRate"
            style="width: 80px;"
            class="mr-3.5" />
        </template>

        <template v-if="column.dataIndex === 'action'">
          <Button
            size="small"
            type="text"
            @click="handleRemoveTaskAssociation(record)">
            <Icon icon="icon-qingchu" class="mr-1" />
            {{ t('actions.cancel') }}
          </Button>
        </template>

        <template v-if="column.dataIndex === 'taskType'">
          {{ record?.taskType?.message }}
        </template>

        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record?.priority" />
        </template>

        <template v-if="column.dataIndex === 'status'">
          <TaskStatus :value="record?.status" />
        </template>
      </template>
    </Table>

    <AsyncComponent :visible="isSelectTaskModalVisible">
      <SelectTaskByModuleModal
        v-model:visible="isSelectTaskModalVisible"
        :title="t('testCase.messages.assocSelect', {name: props.title})"
        :action="`${TESTER}/func/case/${props.caseId}/task/notAssociated?taskType=${props.taskType}`"
        :projectId="props.projectId"
        @ok="handleAssociateTasks" />
    </AsyncComponent>
  </div>
</template>
