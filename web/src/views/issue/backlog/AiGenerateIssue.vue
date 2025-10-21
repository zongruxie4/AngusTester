<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import {
  DatePicker, Icon, IconRequired, IconTask, Input, Modal,
  notification, SelectUser, Spin, Tooltip
} from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { duration, TESTER, utils, Priority } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { debounce } from 'throttle-debounce';
import { TaskType } from '@/enums/enums';

import { issue } from '@/api/tester';
import { ai } from '@/api/gm';

import TaskPriority from '@/components/task/TaskPriority.vue';
import SelectEnum from '@/components/form/enum/SelectEnum.vue';

import { TaskDetail } from '../types';

const { t } = useI18n();

// Type Definitions
interface Props {
  visible: boolean;
  projectId: string;
}

// Component Props & Emits
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: undefined
});

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'ok'): void;
}>();

// Constants
const MAX_TASK_COUNT = 200;

// Reactive State Variables
const isConfirmLoading = ref(false);
const isGenerating = ref(false);

const taskIdList = ref<string[]>([]);
const taskDataMap = ref<{
  [key: string]: {
    taskType: TaskDetail['taskType']['value'];
    priority: TaskDetail['priority']['value'];
    name: string;
    evalWorkload: string;
    projectId: string;
    assigneeId?: string;
    confirmerId?: string;
    deadlineDate?: string;
    sprintId?: string;
    moduleId?: string;
  }
}>({});

// Validation Error Sets
const taskTypeErrorSet = ref(new Set<string>());
const priorityErrorSet = ref(new Set<string>());
const nameErrorSet = ref(new Set<string>());
const deadlineDateErrorSet = ref(new Set<string>());
const nameRepeatSet = ref(new Set<string>());

// AI Keywords Input
const aiKeywordsInput = ref('');

// AI Task Generation Functions
/**
 * <p>Generate tasks using AI</p>
 * <p>Calls AI service to generate task content based on keywords and creates task data structure</p>
 */
const generateTasksWithAI = async () => {
  isGenerating.value = true;
  const [error, res] = await ai.getChartResult({ type: 'WRITE_BACKLOG', question: aiKeywordsInput.value });
  isGenerating.value = false;
  if (error) {
    return;
  }

  const aiResponse = (res?.data || { normal: '', content: [] }) as {
    normal: string;
    content: string[];
  };

  const taskContentList = aiResponse.content;
  const newTaskIdList: string[] = [];
  const newTaskDataMap = {};
  for (let i = 0, len = taskContentList.length; i < len; i++) {
    const taskId = utils.uuid();
    newTaskIdList.push(taskId);
    newTaskDataMap[taskId] = {
      name: taskContentList[i],
      assigneeId: undefined,
      confirmerId: undefined,
      deadlineDate: undefined,
      evalWorkload: '1',
      moduleId: undefined,
      priority: Priority.MEDIUM,
      projectId: props.projectId,
      sprintId: undefined,
      taskType: TaskType.REQUIREMENT
    };
  }

  taskIdList.value = newTaskIdList;
  taskDataMap.value = newTaskDataMap;
};

// Task Management Functions
/**
 * <p>Add a new task row</p>
 * <p>Creates a new empty task with default values</p>
 */
const addNewTask = () => {
  const newTaskId = utils.uuid();
  taskIdList.value.push(newTaskId);
  taskDataMap.value[newTaskId] = {
    assigneeId: undefined,
    confirmerId: undefined,
    deadlineDate: undefined,
    evalWorkload: '1',
    moduleId: undefined,
    priority: Priority.MEDIUM,
    projectId: props.projectId,
    sprintId: undefined,
    taskType: TaskType.REQUIREMENT,
    name: ''
  };
};

/**
 * <p>Delete a task row</p>
 * <p>Removes task from list and clears associated error states</p>
 */
const deleteTask = (taskId: string, index: number) => {
  taskIdList.value.splice(index, 1);
  delete taskDataMap.value[taskId];
  taskTypeErrorSet.value.delete(taskId);
  priorityErrorSet.value.delete(taskId);
  nameErrorSet.value.delete(taskId);
  deadlineDateErrorSet.value.delete(taskId);
};

// Modal Management Functions
/**
 * <p>Cancel modal and reset form</p>
 * <p>Closes the modal and clears all form data</p>
 */
const cancelModal = () => {
  resetForm();
  emit('update:visible', false);
};

/**
 * <p>Confirm and save tasks</p>
 * <p>Validates all tasks and creates them in the system</p>
 */
const confirmAndSaveTasks = async () => {
  if (taskIdList.value?.length > MAX_TASK_COUNT) {
    notification.warning(t('backlog.aiGenerate.maxTasksWarning'));
    return;
  }

  if (!validateAllTasks()) {
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

    const [error] = await issue.addTask(taskParams);
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

// Validation Functions
/**
 * <p>Clear all validation error states</p>
 * <p>Resets all error sets to empty state</p>
 */
const clearAllValidationErrors = () => {
  taskTypeErrorSet.value.clear();
  priorityErrorSet.value.clear();
  nameErrorSet.value.clear();
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
  const taskNames = Object.values(taskDataMap.value).map(item => item.name);
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
 * <p>Validate all tasks</p>
 * <p>Performs comprehensive validation on all tasks and returns validation result</p>
 */
const validateAllTasks = () => {
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

    if (taskItem.deadlineDate && dayjs(taskItem.deadlineDate).isBefore(dayjs())) {
      deadlineDateErrorSet.value.add(taskId);
    }
  }

  return !(taskTypeErrorSet.value.size +
    priorityErrorSet.value.size +
    nameErrorSet.value.size +
    deadlineDateErrorSet.value.size);
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
 * <p>Handle deadline date change</p>
 * <p>Clears deadline date validation error when user changes selection</p>
 */
const handleDeadlineDateChange = (taskId: string) => {
  deadlineDateErrorSet.value.delete(taskId);
};

// Utility Functions
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
 * <p>Clears all form data and validation errors</p>
 */
const resetForm = () => {
  aiKeywordsInput.value = '';
  taskIdList.value = [];
  taskDataMap.value = {};
  taskTypeErrorSet.value.clear();
  priorityErrorSet.value.clear();
  nameErrorSet.value.clear();
  deadlineDateErrorSet.value.clear();
  clearAllValidationErrors();
};

// Computed Properties
/**
 * <p>OK button properties</p>
 * <p>Disables OK button when AI is generating tasks</p>
 */
const okButtonProps = computed(() => {
  return {
    disabled: isGenerating.value
  };
});

// Utility Functions
/**
 * <p>Exclude specific task types from selection</p>
 * <p>Filters out API_TEST and SCENARIO_TEST from available task types</p>
 */
const excludeTaskTypes = (option: {value: string; message: string}) => {
  return [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(option.value as any);
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => props.visible, (isVisible) => {
    if (!isVisible) {
      return;
    }

    resetForm();
    addNewTask();
  }, { immediate: true, deep: true });
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :width="1200"
    :confirmLoading="isConfirmLoading"
    :okButtonProps="okButtonProps"
    :title="t('backlog.aiGenerate.title')"
    @cancel="cancelModal"
    @ok="confirmAndSaveTasks">
    <Spin
      :spinning="isGenerating||isConfirmLoading"
      :tip="isGenerating ? t('backlog.aiGenerate.generating') : ''">
      <div class="flex flex-nowrap justify-between mb-3.5 space-x-5">
        <Input
          v-model:value="aiKeywordsInput"
          :placeholder="t('backlog.aiGenerate.aiKeywordsPlaceholder')"
          trim
          allowClear
          class="flex-1"
          :maxlength="2000" />
        <Button
          :disabled="!aiKeywordsInput"
          type="primary"
          size="small"
          @click="generateTasksWithAI">
          {{ t('actions.generate') }}
        </Button>
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
          <span>{{ t('common.evalWorkload') }}</span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('common.assignee') }}</span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <span>{{ t('common.confirmer') }}</span>
        </div>

        <div class="w-42 space-x-0.5 head-item-container">
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
            :excludes="excludeTaskTypes"
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
              @change="handleTaskNameChange()" />
          </Tooltip>

          <Input
            v-model:value="taskDataMap[taskId].evalWorkload"
            class="w-20 mr-2.5"
            size="small"
            dataType="float"
            trimAll
            :min="0.1"
            :max="1000"
            :placeholder="t('common.placeholders.inputEvalWorkload')" />

          <SelectUser
            v-model:value="taskDataMap[taskId].assigneeId"
            :placeholder="t('common.assignee')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80" />

          <SelectUser
            v-model:value="taskDataMap[taskId].confirmerId"
            :placeholder="t('common.confirmer')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80" />

          <Tooltip
            :title="t('common.deadlineDate')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="deadlineDateErrorSet.has(taskId)">
            <DatePicker
              v-model:value="taskDataMap[taskId].deadlineDate"
              :error="deadlineDateErrorSet.has(taskId)"
              :showNow="false"
              :disabledDate="disablePastDates"
              :showTime="{ hideDisabledOptions: true, format: 'HH:mm:ss' }"
              :placeholder="t('common.deadlineDate')"
              type="date"
              size="small"
              showToday
              class="w-42 mr-2.5"
              @change="handleDeadlineDateChange(taskId)" />
          </Tooltip>

          <div
            class="w-5 h-5 text-3.5 flex items-center justify-center text-theme-text-hover cursor-pointer"
            @click="deleteTask(taskId, index)">
            <Icon icon="icon-qingchu" />
          </div>
        </div>
      </div>

      <Button
        :disabled="taskIdList?.length>=MAX_TASK_COUNT"
        type="link"
        size="small"
        class="flex items-center px-0 mt-1"
        @click="addNewTask">
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
