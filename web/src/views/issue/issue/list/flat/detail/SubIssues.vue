<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { Button, Progress } from 'ant-design-vue';
import {
  AsyncComponent, Colon, Dropdown, Hints, Icon, IconTask,
  Input, modal, notification, Table
} from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { issue } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BugLevel, TaskType } from '@/enums/enums';
import { IssueMenuKey } from '@/views/issue/menu';
import { TaskDetail } from '@/views/issue/types';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

import TaskStatus from '@/components/task/TaskStatus.vue';
import TaskPriority from '@/components/task/TaskPriority.vue';
import SelectEnum from '@/components/form/enum/SelectEnum.vue';

const EditTaskModal = defineAsyncComponent(() => import('@/views/issue/issue/list/Edit.vue'));
const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/SelectByModuleModal.vue'));

/**
 * Props interface for SubTask component
 */
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  taskInfo: undefined,
  loading: false,
  tips: ''
});

// Composables
const { t } = useI18n();

// Event emitter for component communication

const emit = defineEmits<{
  (e: 'refreshChange'): void;
  (e: 'update:loading', value: boolean): void;
  (event: 'dataChange', value: Partial<TaskDetail>): void;
}>();

/**
 * Loading state for API operations
 */
const isOperationLoading = ref(false);

/**
 * ID of the currently selected task for editing
 */
const selectedTaskForEdit = ref<number>();

/**
 * Visibility state for the task edit modal
 */
const isTaskEditModalVisible = ref(false);

/**
 * Visibility state for the task selection modal
 */
const isTaskSelectionModalVisible = ref(false);

/**
 * Task type for the new sub-task being created
 */
const newSubTaskType = ref<TaskDetail['taskType']['value']>();

/**
 * Priority level for the new sub-task being created
 */
const newSubTaskPriority = ref<TaskDetail['priority']['value']>();

/**
 * Name for the new sub-task being created
 */
const newSubTaskName = ref<string>();

/**
 * Opens the task edit modal for creating a new sub-task
 */
const openTaskEditModal = () => {
  isTaskEditModalVisible.value = true;
};

/**
 * Handles successful completion of task edit modal
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
  const [error] = await issue.setSubTask(props.taskInfo?.id || props.id, requestParams);
  isOperationLoading.value = false;
  isTaskSelectionModalVisible.value = false;

  if (error) {
    return;
  }

  notification.success(t('issue.detail.tabs.subIssue.messages.assocSubIssueSuccess'));
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
    requestParams.escapedBug = false;
  }

  isOperationLoading.value = true;
  const [error] = await issue.addTask(requestParams);
  isOperationLoading.value = false;

  if (error) {
    return;
  }

  newSubTaskName.value = undefined;
  newSubTaskType.value = props.taskInfo?.taskType?.value;
  newSubTaskPriority.value = props.taskInfo?.priority?.value;
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
    content: t('issue.detail.tabs.subIssue.messages.confirmCancelSubIssue', { name: subTaskData.name }),
    async onOk () {
      const requestParams = {
        subTaskIds: [subTaskData.id]
      };
      emit('update:loading', true);
      const [error] = await issue.cancelSubTask(props.taskInfo?.id || props.id, requestParams);
      emit('update:loading', false);

      if (error) {
        return;
      }

      notification.success(t('issue.detail.tabs.subIssue.messages.cancelSubIssueSuccess'));
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
  const [error] = await issue.favouriteTask(taskData.id);
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
  const [error] = await issue.cancelFavouriteTask(taskData.id);
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
  const [error] = await issue.followTask(taskData.id);
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
  const [error] = await issue.cancelFollowTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFollowSuccess'));
  emit('refreshChange');
};

/**
 * Sprint ID from the parent task
 * <p>
 * Returns the sprint identifier associated with the parent task
 */
const parentSprintId = computed(() => {
  return props.taskInfo?.sprintId;
});

/**
 * Module ID from the parent task
 * <p>
 * Returns the module identifier associated with the parent task
 */
const parentModuleId = computed(() => {
  return props.taskInfo?.moduleId;
});

/**
 * Parent task ID
 * <p>
 * Returns the unique identifier of the parent task
 */
const parentTaskId = computed(() => {
  return props.taskInfo?.id;
});

/**
 * Sub-task progress information
 * <p>
 * Returns progress statistics for all sub-tasks including completion counts and rates
 */
const subTaskProgressInfo = computed(() => {
  return props.taskInfo?.subTaskProgress || {
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
  return props.taskInfo?.subTaskInfos || [];
});

/**
 * Component mounted lifecycle hook
 * <p>
 * Initializes the form data with appropriate default values
 * based on the parent task's properties
 */
onMounted(() => {
  newSubTaskType.value = props.taskInfo?.taskType?.value
  newSubTaskPriority.value = props.taskInfo?.priority?.value;
});

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
    customRender: ({ text }) => text || '--',
    width: 140
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('common.deadlineDate'),
    customRender: ({ text }) => text || '--',
    width: 150
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('common.actions'),
    width: 180
  }
];
</script>

<template>
  <div class="h-full leading-5">
    <!-- Description hints -->
    <Hints :text="props.tips" class="flex-1 min-w-0 truncate ml-1" />

    <!-- Header section with progress and action buttons -->
    <div class="flex items-center justify-between mb-2.5 mt-1.5 pr-5">
      <!-- Progress indicator -->
      <div class="flex items-center h-8 px-3.5 rounded" style="background-color:#FAFAFA;">
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

      <!-- Action buttons -->
      <div class="flex items-center space-x-2.5">
        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="openTaskEditModal">
          <Icon icon="icon-jia" />
          <span>{{ t('issue.detail.tabs.subIssue.actions.addSubIssue') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="openTaskSelectionModal">
          <Icon icon="icon-guanlianziyuan" />
          <span>{{ t('issue.detail.tabs.subIssue.actions.assocSubIssue') }}</span>
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
            :to="`/issue#${IssueMenuKey.ISSUE}?projectId=${props.projectId}&taskId=${record.id}&total=1`"
            style="color:#40a9ff">
            {{ record.name || '--' }}
          </RouterLink>
        </template>

        <!-- Progress display -->
        <template v-if="column.dataIndex === 'progress'">
          <div style="width: 120px;" class="flex items-center space-x-1">
            <Progress
              :percent="+record?.progress?.completedRate"
              :showInfo="false"
              style="width: 80px; margin-right: 6px;" />
            <span>{{ `${record?.progress?.completed || 0}/${record?.progress?.total || 0}` }}</span>
          </div>
        </template>

        <!-- Task type display -->
        <template v-if="column.dataIndex === 'taskType'">
          <div style="width:70px;" class="flex items-center">
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
          <TaskStatus :value="record.status" />
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
        :placeholder="t('common.placeholders.inputName2')"
        trim
        class="w-200 mr-5"
        @pressEnter="(e: any) => handleEnterKeyPress(e)" />

      <div class="flex items-center space-x-2.5">
        <Button
          :disabled="!newSubTaskName"
          type="primary"
          size="small"
          @click="saveNewSubTask">
          {{ t('actions.add') }}
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
        :priority="props.taskInfo?.priority?.value"
        :deadlineDate="props.taskInfo?.deadlineDate"
        @ok="handleTaskEditSuccess" />
    </AsyncComponent>

    <!-- Task selection modal -->
    <AsyncComponent :visible="isTaskSelectionModalVisible">
      <SelectTaskByModuleModal
        v-model:visible="isTaskSelectionModalVisible"
        :title="t('issue.detail.tabs.subIssue.actions.assocSubIssue')"
        :projectId="props.projectId"
        :action="`${TESTER}/task/${props.taskInfo?.id || props.id}/subtask/notAssociated`"
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
