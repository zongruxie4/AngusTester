<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import {
  Colon, DatePicker, Icon, IconRequired, IconTask, Input, Modal, notification,
  SelectUser, Spin, Tooltip
} from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox } from 'ant-design-vue';
import { duration, TESTER, utils } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { debounce } from 'throttle-debounce';
import { ai } from '@/api/gm';
import { task } from '@/api/tester';
import { TaskType } from '@/enums/enums';

import TaskPriority from '@/components/TaskPriority/index.vue';
import { TIME_FORMAT } from '@/utils/constant';
import { TaskEditState, AssocCaseProps } from '@/views/issue/issue/list/types';

import SelectEnum from '@/components/enum/SelectEnum.vue';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<AssocCaseProps>(), {
  visible: false,
  projectId: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'ok'): void;
}>();

// Constants
const MAX_TASK_COUNT = 200;

// Injected Dependencies
const aiEnabled = inject('aiEnabled', ref(false));

// Reactive State Variables
const isConfirmLoading = ref(false);
const isGenerating = ref(false);

const taskIdList = ref<string[]>([]);
const taskDataMap = ref<TaskEditState>({} as TaskEditState);

// Validation Error Sets
const taskTypeErrorSet = ref(new Set<string>());
const priorityErrorSet = ref(new Set<string>());
const nameErrorSet = ref(new Set<string>());
const assigneeIdErrorSet = ref(new Set<string>());
const deadlineDateErrorSet = ref(new Set<string>());
const nameRepeatSet = ref(new Set<string>());

// Computed Properties
const currentParentTaskId = computed(() => {
  return props.dataSource?.id;
});

const okButtonProps = computed(() => {
  return {
    disabled: isGenerating.value
  };
});

// AI Split State
const isAISplitMode = ref(false);
const aiKeywordsInput = ref('');

// AI Split Functions
/**
 * <p>Enable AI split mode</p>
 * <p>Switches to AI-powered task splitting interface</p>
 */
const enableAISplitMode = () => {
  isAISplitMode.value = true;
  aiKeywordsInput.value = t('backlog.split.aiKeywords', { taskName: props.dataSource?.name });
};

/**
 * <p>Generate sub-tasks using AI</p>
 * <p>Calls AI service to split the task into sub-tasks based on keywords</p>
 */
const generateSubTasksWithAI = async () => {
  isGenerating.value = true;
  const [error, res] = await ai.getChartResult({ type: 'SPLIT_SUB_TASK', question: aiKeywordsInput.value });
  isGenerating.value = false;
  if (error) {
    return;
  }

  const aiResponse = (res?.data || { normal: '', content: [] }) as {
    normal: string;
    content: string[];
  };

  const parentTaskInfo = props.dataSource || {};
  const subTaskContentList = aiResponse.content;
  const newTaskIdList: string[] = [];
  const newTaskDataMap = {} as TaskEditState;
  for (let i = 0, len = subTaskContentList.length; i < len; i++) {
    const taskId = utils.uuid();
    newTaskIdList.push(taskId);
    newTaskDataMap[taskId] = {
      name: subTaskContentList[i],
      assigneeId: parentTaskInfo.assigneeId,
      confirmerId: parentTaskInfo.confirmerId,
      deadlineDate: parentTaskInfo.deadlineDate,
      evalWorkload: '1',
      moduleId: parentTaskInfo.moduleId,
      parentTaskId: parentTaskInfo.id,
      priority: parentTaskInfo.priority?.value,
      projectId: parentTaskInfo.projectId,
      sprintId: parentTaskInfo.sprintId,
      taskType: TaskType.TASK,
      testType: parentTaskInfo.testType?.value
    };
  }

  taskIdList.value = newTaskIdList;
  taskDataMap.value = newTaskDataMap;
};

/**
 * <p>Cancel AI split mode</p>
 * <p>Exits AI split mode and clears AI keywords</p>
 */
const cancelAISplitMode = () => {
  isAISplitMode.value = false;
  aiKeywordsInput.value = '';
};

// Event Handlers
/**
 * <p>Handle task type change</p>
 * <p>Clears task type validation error when user changes selection</p>
 */
const handleTaskTypeChange = (taskId: string) => {
  taskTypeErrorSet.value.delete(taskId);
};

/**
 * <p>Handle priority change</p>
 * <p>Clears priority validation error when user changes selection</p>
 */
const handlePriorityChange = (taskId: string) => {
  priorityErrorSet.value.delete(taskId);
};

/**
 * <p>Handle task name change</p>
 * <p>Debounced validation of task name duplicates</p>
 */
const handleTaskNameChange = debounce(duration.search, () => {
  validateTaskNameDuplicates();
});

/**
 * <p>Handle sub-task checkbox change</p>
 * <p>Toggles whether the task is a sub-task of the parent</p>
 */
const handleSubTaskCheckboxChange = (taskId: string) => {
  if (taskDataMap.value[taskId].parentTaskId) {
    taskDataMap.value[taskId].parentTaskId = undefined;
    return;
  }
  taskDataMap.value[taskId].parentTaskId = currentParentTaskId.value;
};

/**
 * <p>Handle assignee change</p>
 * <p>Clears assignee validation error when user changes selection</p>
 */
const handleAssigneeChange = (taskId: string) => {
  assigneeIdErrorSet.value.delete(taskId);
};

/**
 * <p>Handle deadline date change</p>
 * <p>Clears deadline date validation error when user changes selection</p>
 */
const handleDeadlineDateChange = (taskId: string) => {
  deadlineDateErrorSet.value.delete(taskId);
};

// Task Management Functions
/**
 * <p>Add new sub-task</p>
 * <p>Creates a new sub-task with default values inherited from parent task</p>
 */
const addNewSubTask = () => {
  const newTaskId = utils.uuid();
  taskIdList.value.push(newTaskId);

  const parentTaskData = props.dataSource;
  taskDataMap.value[newTaskId] = {
    assigneeId: parentTaskData.assigneeId,
    confirmerId: parentTaskData.confirmerId,
    deadlineDate: parentTaskData.deadlineDate,
    evalWorkload: '1',
    moduleId: parentTaskData.moduleId,
    parentTaskId: parentTaskData.id,
    priority: parentTaskData.priority?.value,
    projectId: parentTaskData.projectId,
    sprintId: parentTaskData.sprintId,
    taskType: TaskType.TASK,
    testType: parentTaskData.testType?.value,
    name: ''
  };
};

/**
 * <p>Delete sub-task</p>
 * <p>Removes a sub-task from the list and clears associated error states</p>
 */
const deleteSubTask = (taskId: string, index: number) => {
  taskIdList.value.splice(index, 1);
  delete taskDataMap.value[taskId];
  taskTypeErrorSet.value.delete(taskId);
  priorityErrorSet.value.delete(taskId);
  nameErrorSet.value.delete(taskId);
  assigneeIdErrorSet.value.delete(taskId);
  deadlineDateErrorSet.value.delete(taskId);
};

/**
 * <p>Cancel modal and reset form</p>
 * <p>Closes the modal and clears all form data</p>
 */
const cancelModal = () => {
  resetForm();
  emit('update:visible', false);
};

/**
 * <p>Confirm and create sub-tasks</p>
 * <p>Validates all sub-tasks and creates them in the system</p>
 */
const confirmAndCreateSubTasks = async () => {
  if (taskIdList.value?.length > MAX_TASK_COUNT) {
    notification.warning(t('backlog.split.maxSplitWarning'));
    return;
  }

  if (!validateAllSubTasks()) {
    return;
  }

  const taskIds = taskIdList.value;
  const taskMap = taskDataMap.value;
  isConfirmLoading.value = true;
  let errorCount = 0;
  for (let i = 0, len = taskIds.length; i < len; i++) {
    const taskId = taskIds[i];
    const taskParams = taskMap[taskId];
    for (const key in taskParams) {
      if (key === 'moduleId') {
        if (!taskParams[key] || taskParams[key] === '-1') {
          delete taskParams[key];
        }
      } else if (!taskParams[key]) {
        delete taskParams[key];
      }
    }

    const [error] = await task.addTask(taskParams);
    if (error) {
      errorCount++;
    }
  }

  isConfirmLoading.value = false;
  if (errorCount === 0) {
    resetForm();
    emit('update:visible', false);
    emit('ok');
  }
};

/**
 * <p>Clear all validation error states</p>
 * <p>Resets all error sets to empty state</p>
 */
const clearAllValidationErrors = () => {
  taskTypeErrorSet.value.clear();
  priorityErrorSet.value.clear();
  nameErrorSet.value.clear();
  assigneeIdErrorSet.value.clear();
  deadlineDateErrorSet.value.clear();
  nameRepeatSet.value.clear();
};

/**
 * <p>Get duplicate task names</p>
 * <p>Identifies task names that appear more than once in the current task list</p>
 */
const getDuplicateTaskNames = () => {
  const uniqueNames = new Set();
  const duplicateNames = new Set();
  const taskNames = Object.values(taskDataMap.value).map(item => (item as any)?.name).filter(Boolean);
  for (let i = 0, len = taskNames.length; i < len; i++) {
    const taskName = taskNames[i];
    if (taskName) {
      if (uniqueNames.has(taskName)) {
        duplicateNames.add(taskName);
      } else {
        uniqueNames.add(taskName);
      }
    }
  }
  return duplicateNames;
};

/**
 * <p>Validate task name duplicates</p>
 * <p>Checks for duplicate task names and updates error states accordingly</p>
 */
const validateTaskNameDuplicates = () => {
  const taskIds = taskIdList.value;
  const duplicateNameSet = getDuplicateTaskNames();
  const taskMap = taskDataMap.value;
  for (let i = 0, len = taskIds.length; i < len; i++) {
    const taskId = taskIds[i];
    const taskItem = taskMap[taskId];
    if (taskItem.name && duplicateNameSet.has(taskItem.name)) {
      nameRepeatSet.value.add(taskId);
      nameErrorSet.value.add(taskId);
    } else {
      nameRepeatSet.value.delete(taskId);
      nameErrorSet.value.delete(taskId);
    }
  }
};

/**
 * <p>Validate all sub-tasks</p>
 * <p>Performs comprehensive validation on all sub-tasks and returns validation result</p>
 */
const validateAllSubTasks = () => {
  const taskIds = taskIdList.value;
  const taskMap = taskDataMap.value;
  const duplicateNameSet = getDuplicateTaskNames();
  clearAllValidationErrors();
  for (let i = 0, len = taskIds.length; i < len; i++) {
    const taskId = taskIds[i];
    const taskItem = taskMap[taskId];
    if (!taskItem.taskType) {
      taskTypeErrorSet.value.add(taskId);
    }

    if (!taskItem.priority) {
      priorityErrorSet.value.add(taskId);
    }

    if (!taskItem.name) {
      nameErrorSet.value.add(taskId);
    } else {
      if (duplicateNameSet.has(taskItem.name)) {
        nameRepeatSet.value.add(taskId);
        nameErrorSet.value.add(taskId);
      }
    }

    if (!taskItem.assigneeId) {
      assigneeIdErrorSet.value.add(taskId);
    }

    if (!taskItem.deadlineDate) {
      deadlineDateErrorSet.value.add(taskId);
    } else {
      if (dayjs(taskItem.deadlineDate).isBefore(dayjs())) {
        deadlineDateErrorSet.value.add(taskId);
      }
    }
  }

  return !(taskTypeErrorSet.value.size +
    priorityErrorSet.value.size +
    nameErrorSet.value.size +
    assigneeIdErrorSet.value.size +
    deadlineDateErrorSet.value.size);
};

/**
 * <p>Disable past dates in date picker</p>
 * <p>Prevents selection of dates before today</p>
 */
const disablePastDates = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

/**
 * <p>Reset form to initial state</p>
 * <p>Clears all form data and error states</p>
 */
const resetForm = () => {
  aiKeywordsInput.value = '';
  isAISplitMode.value = false;
  taskIdList.value = [];
  taskDataMap.value = {} as TaskEditState;
  taskTypeErrorSet.value.clear();
  priorityErrorSet.value.clear();
  nameErrorSet.value.clear();
  assigneeIdErrorSet.value.clear();
  deadlineDateErrorSet.value.clear();
  clearAllValidationErrors();
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => props.dataSource, (newDataSource) => {
    if (!newDataSource) {
      return;
    }

    const newTaskId = utils.uuid();
    taskIdList.value = [newTaskId];
    taskDataMap.value[newTaskId] = {
      assigneeId: newDataSource.assigneeId,
      confirmerId: newDataSource.confirmerId,
      deadlineDate: newDataSource.deadlineDate,
      evalWorkload: '1',
      moduleId: newDataSource.moduleId,
      parentTaskId: newDataSource.id,
      priority: newDataSource.priority?.value,
      projectId: newDataSource.projectId,
      sprintId: newDataSource.sprintId,
      taskType: TaskType.TASK,
      testType: newDataSource.testType?.value,
      name: ''
    };
  }, { immediate: true, deep: true });
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :width="1250"
    :confirmLoading="isConfirmLoading"
    :okButtonProps="okButtonProps"
    :title="t('backlog.actions.split')"
    @cancel="cancelModal"
    @ok="confirmAndCreateSubTasks">
    <Spin :spinning="isGenerating" :tip="t('backlog.split.splitting')">
      <div class="flex flex-nowrap justify-between mb-3.5 space-x-5">
        <template v-if="!isAISplitMode">
          <div class="flex items-start font-semibold leading-4.5 pt-1.5">
            <div class="flex-shrink-0 flex items-center mr-1.5">
              <span>{{ t('backlog.actions.split') }}</span>
              <Colon />
            </div>
            <div>{{ props.dataSource?.name }}</div>
          </div>

          <Button
            v-if="aiEnabled"
            :disabled="taskIdList?.length>=MAX_TASK_COUNT"
            type="primary"
            size="small"
            class="flex items-center space-x-1"
            ghost
            @click="enableAISplitMode">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('backlog.actions.aiSplit') }}</span>
          </Button>
        </template>

        <template v-else-if="aiEnabled">
          <Input
            v-model:value="aiKeywordsInput"
            :placeholder="t('backlog.split.aiPlaceholder', { taskName: props.dataSource?.name })"
            trim
            allowClear
            class="flex-1"
            :maxlength="2000" />

          <div class="flex items-center space-x-2.5">
            <Button
              :disabled="!aiKeywordsInput||taskIdList?.length>=MAX_TASK_COUNT"
              type="primary"
              size="small"
              @click="generateSubTasksWithAI">
              {{ t('backlog.split.aiSplit') }}
            </Button>
            <Button
              type="default"
              size="small"
              @click="cancelAISplitMode">
              {{ t('actions.cancel') }}
            </Button>
          </div>
        </template>
      </div>

      <div class="head-container flex items-center space-x-2.5 mb-1.5 px-2 rounded">
        <div class="w-27 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('common.type') }}</span>
        </div>

        <div class="w-20 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('common.priority') }}</span>
        </div>

        <div class="flex-1 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('common.name') }}</span>
        </div>

        <div class="w-20 space-x-0.5 head-item-container">
          <span>{{ t('common.subIssue') }}</span>
        </div>

        <div class="w-20 space-x-0.5 head-item-container">
          <span>
            {{ t('common.workload') }}
          </span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('common.assignee') }}</span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <span>{{ t('common.confirmer') }}</span>
        </div>

        <div class="w-42 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('common.deadlineDate') }}</span>
        </div>

        <div class="w-5 h-5"></div>
      </div>

      <div style="max-height: 320px;" class="space-y-2 overflow-auto">
        <div
          v-for="(taskId, index) in taskIdList"
          :key="taskId"
          class="action-container flex items-center px-2">
          <SelectEnum
            v-model:value="taskDataMap[taskId].taskType"
            :error="taskTypeErrorSet.has(taskId)"
            enumKey="TaskType"
            :placeholder="t('common.taskType')"
            class="w-27 mr-2.5"
            @change="handleTaskTypeChange(taskId)">
            <template #option="record">
              <div class="flex items-center">
                <IconTask :value="record.value as any" class="text-4 flex-shrink-0" />
                <span class="ml-1">{{ record.label }}</span>
              </div>
            </template>
          </SelectEnum>

          <SelectEnum
            v-model:value="taskDataMap[taskId].priority"
            :error="priorityErrorSet.has(taskId)"
            enumKey="Priority"
            :placeholder="t('common.priority')"
            class="w-20 mr-2.5"
            @change="handlePriorityChange(taskId)">
            <template #option="record">
              <TaskPriority :value="{ value: record.value as any, message: record.label }" />
            </template>
          </SelectEnum>

          <Tooltip
            :title="t('common.placeholders.nameRepeat')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="nameRepeatSet.has(taskId)">
            <Input
              v-model:value="taskDataMap[taskId].name"
              :error="nameErrorSet.has(taskId)"
              :maxlength="200"
              trim
              class="flex-1 mr-2.5"
              :placeholder="t('common.placeholders.inputName2')"
              @change="handleTaskNameChange" />
          </Tooltip>

          <div class="w-20 flex justify-center items-center mr-2.5">
            <Checkbox
              :checked="taskDataMap[taskId].parentTaskId === currentParentTaskId"
              @change="handleSubTaskCheckboxChange(taskId)" />
          </div>

          <Input
            v-model:value="taskDataMap[taskId].evalWorkload"
            class="w-20 mr-2.5"
            size="small"
            dataType="float"
            trimAll
            :min="0.1"
            :max="1000"
            :placeholder="t('common.placeholders.workloadRange')" />

          <SelectUser
            v-model:value="taskDataMap[taskId].assigneeId"
            :error="assigneeIdErrorSet.has(taskId)"
            :placeholder="t('common.assignee')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            @change="handleAssigneeChange(taskId)" />

          <SelectUser
            v-model:value="taskDataMap[taskId].confirmerId"
            :placeholder="t('common.confirmer')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80" />

          <Tooltip
            :title="t('common.placeholders.deadlineMustBeFuture')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="deadlineDateErrorSet.has(taskId)">
            <DatePicker
              v-model:value="taskDataMap[taskId].deadlineDate"
              :error="deadlineDateErrorSet.has(taskId)"
              :showNow="false"
              :disabledDate="disablePastDates"
              :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
              :placeholder="t('common.deadlineDate')"
              type="date"
              size="small"
              showToday
              class="w-42 mr-2.5"
              @change="handleDeadlineDateChange(taskId)" />
          </Tooltip>

          <div
            class="w-5 h-5 text-3.5 flex items-center justify-center text-theme-text-hover cursor-pointer"
            @click="deleteSubTask(taskId, index)">
            <Icon icon="icon-qingchu" />
          </div>
        </div>
      </div>

      <Button
        :disabled="taskIdList?.length>=MAX_TASK_COUNT"
        type="link"
        size="small"
        class="flex items-center px-0 mt-1"
        @click="addNewSubTask">
        <Icon icon="icon-jia" class="mr-1 text-3.5" />
        <span>{{ t('actions.saveAndContinue') }}</span>
      </Button>
    </Spin>
  </Modal>
</template>

<style scoped>
.head-container {
  background-color: rgb(246, 248, 251);
  line-height: 32px;
}

.head-item-container {
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.head-item-container:last-child {
  border: 0;
}
</style>
