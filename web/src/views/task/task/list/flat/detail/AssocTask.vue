<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, Table } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { Button, Progress } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { TaskType } from '@/enums/enums';
import { task } from '@/api/tester';
import { AssocTaskProps } from '@/views/task/task/list/types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import TaskStatus from '@/components/TaskStatus/index.vue';
const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/SelectByModuleModal.vue'));

const props = withDefaults(defineProps<AssocTaskProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  taskId: undefined,
  dataSource: undefined,
  title: 'Task',
  taskType: TaskType.TASK,
  tips: ''
});

// Composables
const router = useRouter();
const { t } = useI18n();

/**
 * Event emitter for component communication
 * <p>
 * Emits events to notify parent components about loading state changes
 * and successful edit operations
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

/**
 * Loading state for submit operations
 * <p>
 * Indicates whether a task association operation is currently in progress
 */
const isSubmitLoading = ref(false);

/**
 * Visibility state for the task selection modal
 * <p>
 * Controls whether the task selection modal is currently displayed
 */
const isTaskSelectionModalVisible = ref(false);

/**
 * Filtered task data based on the specified task type
 * <p>
 * Returns only tasks that match the current task type filter
 * from the data source
 */
const filteredTaskData = computed(() => {
  return (props.dataSource || []).filter(item => item.taskType.value === props.taskType);
});

/**
 * Closes the task selection modal
 * <p>
 * Resets the modal visibility state when user cancels the operation
 */
const closeTaskSelectionModal = () => {
  isTaskSelectionModalVisible.value = false;
};

/**
 * Opens the task selection modal
 * <p>
 * Shows the modal interface for selecting tasks to associate
 */
const openTaskSelectionModal = () => {
  isTaskSelectionModalVisible.value = true;
};

/**
 * Handles the association of selected tasks with the current parent task
 * <p>
 * Processes the selected task IDs and creates associations through the API.
 * Updates the loading state and emits success events upon completion.
 *
 * @param selectedTaskIds - Array of task IDs to associate with the parent task
 */
const handleTaskAssociation = async (selectedTaskIds: string[]) => {
  isTaskSelectionModalVisible.value = false;

  if (!selectedTaskIds.length) {
    closeTaskSelectionModal();
    return;
  }
  isSubmitLoading.value = true;

  const [error] = await task.associationTask(props.taskId, selectedTaskIds, {
    paramsType: true
  });

  isSubmitLoading.value = false;
  if (error) {
    return;
  }

  emit('editSuccess');
};

/**
 * Handles the removal of a task association
 * <p>
 * Shows a confirmation dialog and removes the association between
 * the specified task and the current parent task.
 *
 * @param taskRecord - The task record to disassociate
 */
const handleTaskDisassociation = (taskRecord: any) => {
  modal.confirm({
    content: t('task.assocTask.messages.confirmCancelTask', { name: taskRecord.name }),
    onOk () {
      return task.cancelAssociationTask(props.taskId, [taskRecord.id], {
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
 * Navigates to the task detail page
 * <p>
 * Opens the specified task in a new tab for detailed viewing
 *
 * @param taskRecord - The task record to open
 */
const navigateToTaskDetail = (taskRecord: any) => {
  router.push(`/task#task?taskId=${taskRecord.id}`);
};

/**
 * Table column definitions for the associated tasks
 * <p>
 * Defines the structure and display properties for each column
 * in the task association table
 */
const tableColumns = [
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
    key: 'progress',
    dataIndex: 'progress',
    title: t('common.progress')
  },
  {
    key: 'taskType',
    dataIndex: 'taskType',
    title: t('common.taskType')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('common.priority')
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('task.assocTask.columns.evalWorkload')
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('common.status')
  },
  {
    key: 'assigneeName',
    dataIndex: 'assigneeName',
    title: t('task.assocTask.columns.assigneeName')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('task.assocTask.columns.deadlineDate')
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
    <!-- Header section with tips and action button -->
    <div class="flex mb-2 items-center pr-2">
      <div class="flex-1 ml-1 min-w-0 truncate">
        <Hints v-if="props.tips" :text="props.tips" />
      </div>
      <Button
        :disabled="props.dataSource?.length > 19"
        size="small"
        @click="openTaskSelectionModal">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('task.assocTask.actions.associateTask', { title: props.title }) }}
      </Button>
    </div>

    <!-- Task association table -->
    <Table
      :columns="tableColumns"
      :dataSource="filteredTaskData || []"
      :pagination="false"
      size="small"
      noDataSize="small"
      noDataText="">
      <template #bodyCell="{column, record}">
        <!-- Task name with navigation link -->
        <template v-if="column.dataIndex === 'name'">
          <Button
            type="link"
            size="small"
            @click="navigateToTaskDetail(record)">
            {{ record.name }}
          </Button>
        </template>

        <!-- Progress bar display -->
        <template v-if="column.dataIndex === 'progress'">
          <Progress
            :percent="+record?.progress?.completedRate"
            style="width: 80px;"
            class="mr-3.5" />
        </template>

        <!-- Action buttons for task disassociation -->
        <template v-if="column.dataIndex === 'action'">
          <Button
            size="small"
            type="text"
            @click="handleTaskDisassociation(record)">
            <Icon icon="icon-qingchu" class="mr-1" />
            {{ t('actions.cancel') }}
          </Button>
        </template>

        <!-- Task type display -->
        <template v-if="column.dataIndex === 'taskType'">
          {{ record?.taskType?.message }}
        </template>

        <!-- Priority display component -->
        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record?.priority" />
        </template>

        <!-- Status display component -->
        <template v-if="column.dataIndex === 'status'">
          <TaskStatus :value="record?.status" />
        </template>
      </template>
    </Table>

    <!-- Task selection modal -->
    <AsyncComponent :visible="isTaskSelectionModalVisible">
      <SelectTaskByModuleModal
        v-model:visible="isTaskSelectionModalVisible"
        :title="t('task.assocTask.modal.selectTask', { title: props.title })"
        :projectId="props.projectId"
        :action="`${TESTER}/task/${props.taskId}/task/notAssociated?taskType=${props.taskType}`"
        @ok="handleTaskAssociation" />
    </AsyncComponent>
  </div>
</template>
