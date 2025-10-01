<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { Button, Progress } from 'ant-design-vue';
import {
  AsyncComponent, Colon, Dropdown, Hints, Icon, IconTask,
  Input, modal, notification, Table
} from '@xcan-angus/vue-ui';
import { EvalWorkloadMethod, TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BugLevel, TaskType } from '@/enums/enums';

import TaskStatus from '@/components/TaskStatus/index.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { TaskDetail } from '@/views/task/types';
import { AssocCaseProps } from '@/views/task/task/list/types';

/**
 * Props interface for SubTask component
 * <p>
 * Defines the required properties for displaying and managing
 * sub-tasks associated with a parent task.
 */
const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  taskInfo: undefined,
  loading: false
});

// Composables
const { t } = useI18n();

/**
 * Event emitter for component communication
 * <p>
 * Emits events to notify parent components about data changes,
 * loading state updates, and refresh requirements
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'refreshChange'): void;
  (e: 'update:loading', value: boolean): void;
  (event: 'dataChange', value: Partial<TaskDetail>): void;
}>();

/**
 * Lazy-loaded task edit modal component
 * <p>
 * Provides a modal interface for editing existing tasks
 */
const EditTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/Edit.vue'));

/**
 * Lazy-loaded task selection modal component
 * <p>
 * Provides a modal interface for selecting tasks to associate as sub-tasks
 */
const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/SelectByModuleModal.vue'));

/**
 * Loading state for API operations
 * <p>
 * Indicates whether a sub-task operation is currently in progress
 */
const isOperationLoading = ref(false);

/**
 * ID of the currently selected task for editing
 * <p>
 * Used to identify which task should be opened in the edit modal
 */
const selectedTaskForEdit = ref<string>();

/**
 * Visibility state for the task edit modal
 * <p>
 * Controls whether the task edit modal is currently displayed
 */
const isTaskEditModalVisible = ref(false);

/**
 * Visibility state for the task selection modal
 * <p>
 * Controls whether the task selection modal is currently displayed
 */
const isTaskSelectionModalVisible = ref(false);

/**
 * Task type for the new sub-task being created
 * <p>
 * Stores the selected task type from the form dropdown
 */
const newSubTaskType = ref<TaskDetail['taskType']['value']>();

/**
 * Priority level for the new sub-task being created
 * <p>
 * Stores the selected priority from the form dropdown
 */
const newSubTaskPriority = ref<TaskDetail['priority']['value']>();

/**
 * Name for the new sub-task being created
 * <p>
 * Stores the task name entered in the form input
 */
const newSubTaskName = ref<string>();

/**
 * Opens the task edit modal for creating a new sub-task
 * <p>
 * Shows the task edit modal to allow users to create a new sub-task
 */
const openTaskEditModal = () => {
  isTaskEditModalVisible.value = true;
};

/**
 * Handles successful completion of task edit modal
 * <p>
 * Clears the selected task ID and triggers a data refresh
 */
const handleTaskEditSuccess = () => {
  selectedTaskForEdit.value = undefined;
  emit('refreshChange');
};

/**
 * Handles the association of selected tasks as sub-tasks
 * <p>
 * Processes the selected task IDs and creates sub-task associations
 * through the API. Shows success notification and refreshes data.
 *
 * @param selectedSubTaskIds - Array of task IDs to associate as sub-tasks
 */
const handleSubTaskAssociation = async (selectedSubTaskIds: string[]) => {
  const requestParams = {
    subTaskIds: selectedSubTaskIds
  };

  isOperationLoading.value = true;
  const [error] = await task.setSubTask((props.taskInfo as any)?.id, requestParams);
  isOperationLoading.value = false;
  isTaskSelectionModalVisible.value = false;

  if (error) {
    return;
  }

  notification.success(t('task.subTask.messages.associateSubTaskSuccess'));
  emit('refreshChange');
};

/**
 * Opens the task selection modal for associating existing tasks
 * <p>
 * Shows the task selection modal to allow users to associate existing tasks as sub-tasks
 */
const openTaskSelectionModal = () => {
  isTaskSelectionModalVisible.value = true;
};

/**
 * Handles the Enter key press in the task name input
 * <p>
 * Triggers the save operation when user presses Enter in the task name field
 *
 * @param event - Input event containing the target value
 */
const handleEnterKeyPress = (event: { target: { value: string } }) => {
  const inputValue = event.target.value.trim();
  if (!inputValue) {
    return;
  }

  saveNewSubTask();
};

/**
 * Saves a new sub-task to the server
 * <p>
 * Validates the form data, creates the task through the API,
 * and resets the form on success. Handles special cases for bug tasks.
 */
const saveNewSubTask = async () => {
  const taskName = newSubTaskName.value?.trim();
  if (!taskName) {
    return;
  }

  const requestParams: any = {
    projectId: props.projectId,
    name: taskName,
    priority: newSubTaskPriority.value,
    taskType: newSubTaskType.value,
    parentTaskId: parentTaskId.value
  };

  if (newSubTaskType.value === TaskType.BUG) {
    requestParams.bugLevel = BugLevel.MINOR;
    requestParams.missingBug = false;
  }

  isOperationLoading.value = true;
  const [error] = await task.addTask(requestParams);
  isOperationLoading.value = false;

  if (error) {
    return;
  }

  newSubTaskName.value = undefined;
  newSubTaskType.value = (props.taskInfo as any)?.taskType?.value;
  newSubTaskPriority.value = (props.taskInfo as any)?.priority?.value;
  emit('refreshChange');
};

/**
 * Deletes a sub-task association
 * <p>
 * Shows a confirmation dialog and removes the sub-task association
 * from the parent task. Updates loading state and refreshes data on success.
 *
 * @param subTaskData - The sub-task data to remove
 */
const deleteSubTask = (subTaskData: TaskDetail['subTaskInfos'][number]) => {
  modal.confirm({
    content: t('task.subTask.messages.confirmCancelSubTask', { name: subTaskData.name }),
    async onOk () {
      const requestParams = {
        subTaskIds: [subTaskData.id]
      };
      emit('update:loading', true);
      const [error] = await task.cancelSubTask((props.taskInfo as any)?.id, requestParams);
      emit('update:loading', false);

      if (error) {
        return;
      }

      notification.success(t('task.subTask.messages.cancelSubTaskSuccess'));
      emit('refreshChange');
    }
  });
};

/**
 * Opens the task edit modal for editing a sub-task
 * <p>
 * Sets the selected task ID and shows the edit modal for the specified sub-task
 *
 * @param subTaskData - The sub-task data to edit
 */
const editSubTask = (subTaskData: TaskDetail['subTaskInfos'][number]) => {
  selectedTaskForEdit.value = subTaskData.id;
  isTaskEditModalVisible.value = true;
};

/**
 * Handles dropdown menu item clicks
 * <p>
 * Routes different menu actions to their respective handler functions
 * based on the menu item key.
 *
 * @param menuItem - The clicked menu item object
 * @param taskData - The task data associated with the menu
 */
const handleDropdownMenuClick = (menuItem: any, taskData: TaskDetail) => {
  const actionKey = menuItem.key;

  switch (actionKey) {
    case 'addFavourite':
      addToFavourites(taskData);
      break;
    case 'cancelFavourite':
      removeFromFavourites(taskData);
      break;
    case 'addFollow':
      followTask(taskData);
      break;
    case 'cancelFollow':
      unfollowTask(taskData);
      break;
  }
};

/**
 * Adds a task to favourites
 * <p>
 * Marks the specified task as a favourite and shows success notification
 *
 * @param taskData - The task to add to favourites
 */
const addToFavourites = async (taskData: TaskDetail) => {
  const [error] = await task.favouriteTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.favouriteSuccess'));
  emit('refreshChange');
};

/**
 * Removes a task from favourites
 * <p>
 * Unmarks the specified task as a favourite and shows success notification
 *
 * @param taskData - The task to remove from favourites
 */
const removeFromFavourites = async (taskData: TaskDetail) => {
  const [error] = await task.cancelFavouriteTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFavouriteSuccess'));
  emit('refreshChange');
};

/**
 * Follows a task for notifications
 * <p>
 * Sets up following for the specified task and shows success notification
 *
 * @param taskData - The task to follow
 */
const followTask = async (taskData: TaskDetail) => {
  const [error] = await task.followTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.followSuccess'));
  emit('refreshChange');
};

/**
 * Unfollows a task
 * <p>
 * Stops following the specified task and shows success notification
 *
 * @param taskData - The task to unfollow
 */
const unfollowTask = async (taskData: TaskDetail) => {
  const [error] = await task.cancelFollowTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFollowSuccess'));
  emit('refreshChange');
};

/**
 * Component mounted lifecycle hook
 * <p>
 * Initializes the form data with appropriate default values
 * based on the parent task's properties
 */
onMounted(() => {
  newSubTaskType.value = [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes((props.taskInfo as any)?.taskType?.value)
    ? TaskType.TASK
    : (props.taskInfo as any)?.taskType?.value;
  newSubTaskPriority.value = (props.taskInfo as any)?.priority?.value;
});

/**
 * Sprint ID from the parent task
 * <p>
 * Returns the sprint identifier associated with the parent task
 */
const parentSprintId = computed(() => {
  return (props.taskInfo as any)?.sprintId;
});

/**
 * Module ID from the parent task
 * <p>
 * Returns the module identifier associated with the parent task
 */
const parentModuleId = computed(() => {
  return (props.taskInfo as any)?.moduleId;
});

/**
 * Parent task ID
 * <p>
 * Returns the unique identifier of the parent task
 */
const parentTaskId = computed(() => {
  return (props.taskInfo as any)?.id;
});

/**
 * Sub-task progress information
 * <p>
 * Returns progress statistics for all sub-tasks including completion counts and rates
 */
const subTaskProgressInfo = computed(() => {
  return (props.taskInfo as any)?.subTaskProgress || {
    completed: '0',
    completedRate: '0',
    total: '0'
  };
});

/**
 * List of sub-task information
 * <p>
 * Returns the array of sub-task data associated with the parent task
 */
const subTaskDataList = computed(() => {
  return (props.taskInfo as any)?.subTaskInfos || [];
});

// Dropdown menu configuration
/**
 * Dynamic menu items map for sub-task dropdown menus
 * <p>
 * Generates context-specific menu items for each sub-task based on
 * its current state (favourite/follow status)
 */
const subTaskMenuItemsMap = computed(() => {
  const menuMap = {};
  if (!subTaskDataList.value) {
    return menuMap;
  }

  for (let i = 0, len = subTaskDataList.value.length; i < len; i++) {
    const { favourite, follow, id } = subTaskDataList.value[i];
    const menuItems: any[] = [];

    // Favourite menu item
    if (favourite) {
      menuItems.push({
        name: t('actions.cancelFavourite'),
        key: 'cancelFavourite',
        icon: 'icon-quxiaoshoucang',
        disabled: false,
        hide: false
      });
    } else {
      menuItems.push({
        name: t('actions.addFavourite'),
        key: 'addFavourite',
        icon: 'icon-yishoucang',
        disabled: false,
        hide: false
      });
    }

    // Follow menu item
    if (follow) {
      menuItems.push({
        name: t('actions.cancelFollow'),
        key: 'cancelFollow',
        icon: 'icon-quxiaoguanzhu',
        disabled: false,
        hide: false
      });
    } else {
      menuItems.push({
        name: t('actions.addFollow'),
        key: 'addFollow',
        icon: 'icon-yiguanzhu',
        disabled: false,
        hide: false
      });
    }

    menuMap[id] = menuItems;
  }
  return menuMap;
});

/**
 * Table column definitions for the sub-task list
 * <p>
 * Defines the structure and display properties for each column
 * in the sub-task table, including conditional workload column titles
 */
const subTaskTableColumns = [
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
    title: t('common.priority'),
    groupName: 'task'
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('common.evalWorkload'),
    groupName: 'task',
    hide: true
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('common.status')
  },
  {
    key: 'assigneeName',
    dataIndex: 'assigneeName',
    title: t('common.assignee')
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
  <div class="h-full leading-5">
    <!-- Header section with progress and action buttons -->
    <div class="flex items-center mb-2.5 pr-5">
      <!-- Progress indicator -->
      <div class="flex items-center flex-nowrap h-8 px-3.5 rounded" style="background-color:#FAFAFA;">
        <span class="flex-shrink-0 font-semibold text-theme-title">
          {{ t('common.progress') }}
        </span>
        <Colon class="mr-1.5" />
        <span class="font-semibold text-3.5" style="color: #07F;">
          {{ subTaskProgressInfo?.completed || 0 }}
        </span>
        <span class="font-semibold text-3.5 mx-1">/</span>
        <span class="font-semibold text-3.5 mr-3.5">
          {{ subTaskProgressInfo?.total || 0 }}
        </span>
        <Progress
          :percent="+subTaskProgressInfo?.completedRate"
          style="width: 120px;"
          class="mr-3.5"
          :showInfo="false" />
        <span class="font-semibold text-3.5">
          {{ subTaskProgressInfo?.completedRate || 0 }}%
        </span>
      </div>

      <!-- Description hints -->
      <Hints :text="t('common.description')" class="flex-1 min-w-0 truncate ml-1" />

      <!-- Action buttons -->
      <div class="flex items-center space-x-2.5">
        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="openTaskEditModal">
          <Icon icon="icon-jia" />
          <span>{{ t('task.subTask.actions.addSubTask') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="openTaskSelectionModal">
          <Icon icon="icon-guanlianziyuan" />
          <span>{{ t('task.subTask.actions.associateSubTask') }}</span>
        </Button>
      </div>
    </div>

    <!-- Sub-task table -->
    <Table
      class="sub_task_table"
      :columns="subTaskTableColumns"
      :pagination="false"
      :dataSource="subTaskDataList"
      noDataSize="small"
      noDataText="">
      <template #bodyCell="{record, column}">
        <!-- Task name with navigation link -->
        <template v-if="column.dataIndex === 'name'">
          <RouterLink
            target="_self"
            :title="record.name"
            :to="`/task#task?projectId=${props.projectId}&taskId=${record.id}&total=1`"
            style="color:#40a9ff">
            {{ record.name || '--' }}
          </RouterLink>
        </template>

        <!-- Progress display -->
        <template v-if="column.dataIndex === 'progress'">
          <div style="width: 120px;" class="flex items-center space-x-1">
            <span>{{ `${record?.progress?.completed || 0} / ${record?.progress?.total || 0}` }}</span>
            <Progress
              :percent="+record?.progress?.completedRate"
              style="width: 80px;"
              class="mr-3.5" />
          </div>
        </template>

        <!-- Task type display -->
        <template v-if="column.dataIndex === 'taskType'">
          <div style="width:80px;" class="flex items-center">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <span class="ml-1">{{ record.taskType?.message }}</span>
          </div>
        </template>

        <!-- Priority display -->
        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record.priority" />
        </template>

        <!-- Status display -->
        <template v-if="column.dataIndex === 'status'">
          <TaskStatus :value="record.priority" />
        </template>

        <!-- Action buttons -->
        <template v-if="column.dataIndex === 'action'">
          <div style="width: 140px;padding: 0;" class="tbody-cell flex items-center space-x-2.5">
            <Button
              type="text"
              size="small"
              class="flex items-center px-0"
              @click="deleteSubTask(record)">
              <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
              <span>{{ t('actions.cancel') }}</span>
            </Button>

            <Button
              type="text"
              size="small"
              class="flex items-center px-0"
              @click="editSubTask(record)">
              <Icon icon="icon-shuxie" class="text-3.5 mr-1" />
              <span>{{ t('actions.edit') }}</span>
            </Button>

            <Dropdown :menuItems="subTaskMenuItemsMap[record.id]" @click="handleDropdownMenuClick($event,record)">
              <Button
                type="text"
                size="small"
                class="flex items-center px-0">
                <Icon icon="icon-gengduo" />
              </Button>
            </Dropdown>
          </div>
        </template>
      </template>
    </Table>

    <!-- New sub-task form -->
    <div class="flex items-center pt-2">
      <SelectEnum
        v-model:value="newSubTaskType"
        :excludes="({value}: any) => [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(value as any)"
        enumKey="TaskType"
        :placeholder="t('common.taskType')"
        class="w-28 mr-2">
        <template #option="record">
          <div class="flex items-center">
            <IconTask :value="record.value as any" class="text-4 flex-shrink-0" />
            <span class="ml-1">{{ (record as any).label }}</span>
          </div>
        </template>
      </SelectEnum>

      <SelectEnum
        v-model:value="newSubTaskPriority"
        internal
        enumKey="Priority"
        :placeholder="t('common.priority')"
        class="w-28 mr-2">
        <template #option="record">
          <TaskPriority :value="record as any" />
        </template>
      </SelectEnum>

      <Input
        ref="taskNameInputRef"
        v-model:value="newSubTaskName"
        :maxlength="200"
        :placeholder="t('task.subTask.form.taskName')"
        trim
        class="w-200 mr-5"
        @pressEnter="(e: any) => handleEnterKeyPress(e)" />

      <div class="flex items-center space-x-2.5">
        <Button
          :disabled="!newSubTaskName"
          type="primary"
          size="small"
          @click="saveNewSubTask">
          {{ t('common.add') }}
        </Button>
      </div>
    </div>

    <!-- Task edit modal -->
    <AsyncComponent :visible="isTaskEditModalVisible">
      <EditTaskModal
        v-model:visible="isTaskEditModalVisible"
        :sprintId="parentSprintId"
        :taskId="selectedTaskForEdit"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :moduleId="parentModuleId === '-1' ? undefined : parentModuleId"
        :parentTaskId="parentTaskId"
        @ok="handleTaskEditSuccess" />
    </AsyncComponent>

    <!-- Task selection modal -->
    <AsyncComponent :visible="isTaskSelectionModalVisible">
      <SelectTaskByModuleModal
        v-model:visible="isTaskSelectionModalVisible"
        :projectId="props.projectId"
        :action="`${TESTER}/task/${(props.taskInfo as any)?.id}/subtask/notAssociated`"
        @ok="handleSubTaskAssociation" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.head-container {
  background-color: rgb(246, 248, 251);
  line-height: 36px;
}

.head-item-container {
  display: flex;
  align-items: center;
  padding: 0 8px;
}

.head-item-container:last-child {
  border: 0;
}

.tbody-cell {
  padding: 0 8px;
}

.row-container {
  padding-top: 4px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--border-divider);
}

.row-container:hover {
 background-color: var(--content-tabs-bg-hover);
}

:deep(.sub_task_table) .ant-table-placeholder {
  @apply hidden;
}
</style>
