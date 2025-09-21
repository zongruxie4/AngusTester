<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref } from 'vue';
import { Button, Tag, TreeSelect } from 'ant-design-vue';
import { AsyncComponent, Icon, IconTask, Input, Select } from '@xcan-angus/vue-ui';
import { TESTER, EvalWorkloadMethod } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskType, BugLevel, SoftwareVersionStatus } from '@/enums/enums';

import TaskStatus from '@/components/TaskStatus/index.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { TaskDetail } from '@/views/task/types';
import { AssocCaseProps } from '@/views/task/task/list/types';

// Component props and emits
const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
  (event: 'refresh'): void;
}>();

// Async components
const Description = defineAsyncComponent(() => import('@/views/task/task/list/kanban/detail/Description.vue'));

// Task name editing state
const taskNameInputRef = ref();
const isTaskNameEditing = ref(false);
const taskNameInputValue = ref<string>();

// Evaluation workload editing state
const evalWorkloadInputRef = ref();
const isEvalWorkloadEditing = ref(false);
const evalWorkloadInputValue = ref<string>();

// Actual workload editing state
const actualWorkloadInputRef = ref();
const isActualWorkloadEditing = ref(false);
const actualWorkloadInputValue = ref<string>();

// Task type editing state
const taskTypeSelectRef = ref();
const isTaskTypeEditing = ref(false);
const taskTypeSelectMessage = ref<string>();
const taskTypeSelectValue = ref<TaskDetail['taskType']['value']>();

// Sprint editing state
const sprintSelectRef = ref();
const isSprintEditing = ref(false);
const sprintSelectMessage = ref<string>();
const sprintSelectValue = ref<string>();

// Module editing state
const moduleTreeSelectRef = ref();
const isModuleEditing = ref(false);
const moduleTreeSelectValue = ref<string>();

// Priority editing state
const prioritySelectRef = ref();
const isPriorityEditing = ref(false);
const prioritySelectMessage = ref<string>();
const prioritySelectValue = ref<TaskDetail['priority']['value']>();

// Tag editing state
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{id: string; name: string;}[]>([]);
const selectedTagIdList = ref<string[]>([]);

// Version editing state
const versionSelectRef = ref();
const isVersionEditing = ref(false);
const versionSelectValue = ref<string>();

// Module tree data and management
const moduleTreeData = ref([]);

// Computed properties
const currentSprintId = computed(() => props.dataSource?.sprintId);
const currentModuleId = computed(() => {
  if (!props.dataSource?.moduleId || props.dataSource?.moduleId === '-1') {
    return undefined;
  }
  return props.dataSource?.moduleId;
});
const currentTaskId = computed(() => props.dataSource?.id);
const currentTaskStatus = computed(() => props.dataSource?.status);
const currentTaskName = computed(() => props.dataSource?.name);
const currentTaskType = computed(() => props.dataSource?.taskType?.value);
const currentPriority = computed(() => props.dataSource?.priority?.value);
const currentTags = computed(() => props.dataSource?.tags || []);
const currentTagIds = computed(() => props.dataSource?.tags?.map(item => item.id) || []);
const currentEvalWorkloadMethod = computed(() => props.dataSource?.evalWorkloadMethod?.value);
const currentEvalWorkload = computed(() => props.dataSource?.evalWorkload);
const currentActualWorkload = computed(() => props.dataSource?.actualWorkload);
const isTaskOverdue = computed(() => props.dataSource?.overdue);
const totalProcessCount = computed(() => +(props.dataSource?.totalNum || 0));
const processFailCount = computed(() => +(props.dataSource?.failNum || 0));
const onePassStatusText = computed(() => {
  if (totalProcessCount.value <= 0) {
    return '--';
  }
  return processFailCount.value === 0 ? t('status.yes') : t('status.no');
});

/**
 * Load module tree data from API
 */
const loadModuleTreeData = async () => {
  if (!props.projectId) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = data || [];
};

/**
 * Load task information by ID from API
 * @param id - Task ID
 * @returns Partial task information
 */
const loadTaskInfoById = async (id: string): Promise<Partial<TaskDetail>> => {
  emit('loadingChange', true);
  const [error, res] = await task.getTaskDetail(id);
  emit('loadingChange', false);
  if (error || !res?.data) {
    return { id };
  }

  return res.data;
};

/**
 * Enter task name editing mode and focus the input
 */
const enterTaskNameEditMode = () => {
  taskNameInputValue.value = currentTaskName.value;
  isTaskNameEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof taskNameInputRef.value?.focus === 'function') {
        taskNameInputRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle task name input blur and update task name
 * @param event - Input blur event
 */
const handleTaskNameInputBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const inputValue = target.value;
  if (!inputValue || inputValue === currentTaskName.value) {
    isTaskNameEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskName(currentTaskId.value, inputValue);
  emit('loadingChange', false);
  isTaskNameEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, name: inputValue });
};

/**
 * Handle task name input enter key press
 */
const handleTaskNameInputEnter = () => {
  if (typeof taskNameInputRef.value?.blur === 'function') {
    taskNameInputRef.value.blur();
  }
};

/**
 * Enter actual workload editing mode and focus the input
 */
const enterActualWorkloadEditMode = () => {
  actualWorkloadInputValue.value = currentActualWorkload.value;
  isActualWorkloadEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof actualWorkloadInputRef.value?.focus === 'function') {
        actualWorkloadInputRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle actual workload input blur and update task actual workload
 * @param event - Input blur event
 */
const handleActualWorkloadInputBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const inputValue = target.value;
  if (inputValue === currentActualWorkload.value) {
    isActualWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editActualWorkload(currentTaskId.value, { workload: inputValue });
  emit('loadingChange', false);
  isActualWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, actualWorkload: inputValue });
};

/**
 * Handle actual workload input enter key press
 */
const handleActualWorkloadInputEnter = () => {
  if (typeof actualWorkloadInputRef.value?.blur === 'function') {
    actualWorkloadInputRef.value.blur();
  }
};

/**
 * Enter evaluation workload editing mode and focus the input
 */
const enterEvalWorkloadEditMode = () => {
  evalWorkloadInputValue.value = currentEvalWorkload.value;
  isEvalWorkloadEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof evalWorkloadInputRef.value?.focus === 'function') {
        evalWorkloadInputRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle evaluation workload input blur and update task evaluation workload
 * @param event - Input blur event
 */
const handleEvalWorkloadInputBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const inputValue = target.value;
  if (inputValue === currentEvalWorkload.value) {
    isEvalWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editEvalWorkloadApi(currentTaskId.value, { workload: inputValue });
  emit('loadingChange', false);
  isEvalWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, evalWorkload: inputValue });
};

/**
 * Handle evaluation workload input enter key press
 */
const handleEvalWorkloadInputEnter = () => {
  if (typeof evalWorkloadInputRef.value?.blur === 'function') {
    evalWorkloadInputRef.value.blur();
  }
};

/**
 * Enter sprint editing mode and focus the select
 */
const enterSprintEditMode = () => {
  sprintSelectValue.value = currentSprintId.value;
  isSprintEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof sprintSelectRef.value?.focus === 'function') {
        sprintSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle sprint selection change and update message
 * @param value - Selected value
 * @param option - Selected option with message and value
 */
const handleSprintSelectionChange = async (_value: any, option: any) => {
  if (option && option.message) {
    sprintSelectMessage.value = option.message;
  }
};

/**
 * Handle sprint selection blur and move task to selected sprint
 */
const handleSprintSelectionBlur = async () => {
  const selectedValue = sprintSelectValue.value;
  if (!selectedValue || selectedValue === currentSprintId.value) {
    isSprintEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const moveTaskParams = {
    taskIds: [currentTaskId.value],
    targetSprintId: selectedValue
  };
  const [error] = await task.moveTask(moveTaskParams);
  emit('loadingChange', false);
  isSprintEditing.value = false;
  if (error) {
    return;
  }

  const updatedTaskInfo = await loadTaskInfoById(currentTaskId.value);
  emit('change', updatedTaskInfo);
};

/**
 * Enter module editing mode and focus the input
 */
const enterModuleEditMode = () => {
  moduleTreeSelectValue.value = currentModuleId.value;
  isModuleEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof moduleTreeSelectRef.value?.focus === 'function') {
        moduleTreeSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Confirm module selection and update task
 */
const confirmModuleSelection = async () => {
  const selectedValue = moduleTreeSelectValue.value;
  if (!selectedValue || selectedValue === currentModuleId.value) {
    isModuleEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const updateParams = {
    moduleId: selectedValue
  };
  const [error] = await task.updateTask(currentTaskId.value, updateParams);
  emit('loadingChange', false);
  isModuleEditing.value = false;
  if (error) {
    return;
  }

  const updatedTaskInfo = await loadTaskInfoById(currentTaskId.value);
  emit('change', updatedTaskInfo);
};

/**
 * Cancel module editing mode
 */
const cancelModuleEdit = () => {
  isModuleEditing.value = false;
};

/**
 * Enter task type editing mode and focus the select
 */
const enterTaskTypeEditMode = () => {
  taskTypeSelectValue.value = currentTaskType.value;
  isTaskTypeEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof taskTypeSelectRef.value?.focus === 'function') {
        taskTypeSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle task type selection change and update message
 * @param value - Selected value
 * @param option - Selected option with message and value
 */
const handleTaskTypeSelectionChange = async (_value: string, option: any) => {
  if (option && option.message) {
    taskTypeSelectMessage.value = option.message;
  }
};

/**
 * Handle task type selection blur and update task type
 */
const handleTaskTypeSelectionBlur = async () => {
  const selectedValue = taskTypeSelectValue.value;
  if (!selectedValue || selectedValue === currentTaskType.value) {
    isTaskTypeEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskTaskType(currentTaskId.value, selectedValue);
  emit('loadingChange', false);
  isTaskTypeEditing.value = false;
  if (selectedValue === TaskType.BUG) {
    await task.updateTask(currentTaskId.value, {
      bugLevel: BugLevel.MINOR,
      missingBug: false
    });
  }
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, taskType: { value: selectedValue, message: taskTypeSelectMessage.value! } });
};

/**
 * Enter priority editing mode and focus the select
 */
const enterPriorityEditMode = () => {
  prioritySelectValue.value = currentPriority.value;
  isPriorityEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof prioritySelectRef.value?.focus === 'function') {
        prioritySelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle priority selection change and update message
 * @param value - Selected value
 * @param option - Selected option with message and value
 */
const handlePrioritySelectionChange = async (_value: string, option: any) => {
  if (option && option.message) {
    prioritySelectMessage.value = option.message;
  }
};

/**
 * Handle priority selection blur and update task priority
 */
const handlePrioritySelectionBlur = async () => {
  const selectedValue = prioritySelectValue.value;
  if (!selectedValue || selectedValue === currentPriority.value) {
    isPriorityEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskPriority(currentTaskId.value, selectedValue);
  emit('loadingChange', false);
  isPriorityEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, priority: { value: selectedValue, message: prioritySelectMessage.value! } });
};

/**
 * Enter tag editing mode and focus the select
 */
const enterTagEditMode = () => {
  selectedTagIdList.value = currentTagIds.value;
  isTagEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof tagSelectRef.value?.focus === 'function') {
        tagSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle tag selection change and update selected tags
 * @param value - Selected values
 * @param options - Selected tag options
 */
const handleTagSelectionChange = async (_value: any, options: any) => {
  if (Array.isArray(options)) {
    selectedTagList.value = options;
  }
};

/**
 * Handle tag selection blur and update task tags
 */
const handleTagSelectionBlur = async () => {
  const selectedIds = selectedTagIdList.value;
  if (isEqual(selectedIds, currentTagIds.value)) {
    isTagEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskTags(currentTaskId.value, { tagIds: selectedIds });
  emit('loadingChange', false);
  isTagEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, tags: selectedTagList.value });
};

/**
 * Enter version editing mode and focus the select
 */
const enterVersionEditMode = () => {
  isVersionEditing.value = true;
  versionSelectValue.value = props.dataSource?.softwareVersion;
  nextTick(() => {
    setTimeout(() => {
      if (typeof versionSelectRef.value?.focus === 'function') {
        versionSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle version selection change
 * @param value - Selected version value
 */
const handleVersionSelectionChange = (value) => {
  versionSelectValue.value = value;
};

/**
 * Handle version selection blur and update task software version
 */
const handleVersionSelectionBlur = async () => {
  const selectedValue = versionSelectValue.value;
  if (selectedValue === props.dataSource?.softwareVersion) {
    isVersionEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.updateTask(currentTaskId.value, { softwareVersion: selectedValue || '' });
  emit('loadingChange', false);
  isVersionEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, softwareVersion: versionSelectValue.value });
};

/**
 * Handle loading state change
 * @param value - Loading state
 */
const handleLoadingChange = (value: boolean) => {
  emit('loadingChange', value);
};

/**
 * Handle task info change
 * @param data - Changed task data
 */
const handleTaskInfoChange = (data: Partial<TaskDetail>) => {
  emit('change', data);
};

/**
 * Initialize module tree data on component mount
 */
onMounted(() => {
  loadModuleTreeData();
});
</script>

<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('task.detailInfo.basic.title') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Task Code -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.code') }}</span>
          </div>
          <div class="info-value">
            {{ props.dataSource?.code }}
          </div>
        </div>

        <!-- Task Name -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.name') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isTaskNameEditing" class="info-value-content">
              <span class="info-text">{{ currentTaskName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterTaskNameEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isTaskNameEditing">
              <Input
                v-show="isTaskNameEditing"
                ref="taskNameInputRef"
                v-model:value="taskNameInputValue"
                :maxlength="200"
                trim
                class="edit-input"
                :placeholder="t('task.detailInfo.basic.columns.namePlaceholder')"
                @blur="handleTaskNameInputBlur"
                @pressEnter="handleTaskNameInputEnter" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Task Status -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.status') }}</span>
          </div>
          <div class="info-value">
            <div class="info-value-content">
              <TaskStatus :value="currentTaskStatus" />
              <span v-if="isTaskOverdue" class="overdue-badge">
                {{ t('task.detailInfo.basic.columns.overdue') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Sprint -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.sprint') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isSprintEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !props.dataSource?.sprintName }">{{ props.dataSource?.sprintName || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterSprintEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isSprintEditing">
              <Select
                v-show="isSprintEditing"
                ref="sprintSelectRef"
                v-model:value="sprintSelectValue"
                :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
                :fieldNames="{ value: 'id', label: 'name' }"
                showSearch
                :placeholder="t('task.detailInfo.basic.columns.selectSprint')"
                class="edit-input"
                @change="handleSprintSelectionChange"
                @blur="handleSprintSelectionBlur" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Module -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.module') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isModuleEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !props.dataSource?.moduleName }">{{ props.dataSource?.moduleName || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterModuleEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isModuleEditing">
              <div v-show="isModuleEditing" class="module-edit-container">
                <TreeSelect
                  ref="moduleTreeSelectRef"
                  v-model:value="moduleTreeSelectValue"
                  :treeData="moduleTreeData"
                  :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
                  :virtual="false"
                  size="small"
                  showSearch
                  allowClear
                  class="edit-input"
                  :placeholder="t('task.detailInfo.basic.columns.selectModule')">
                  <template #title="item">
                    <div class="flex items-center" :title="item.name">
                      <Icon icon="icon-mokuai" class="mr-1" />
                      <div class="truncate">{{ item.name }}</div>
                    </div>
                  </template>
                </TreeSelect>
                <div class="module-edit-actions">
                  <Icon
                    icon="icon-gouxuanzhong"
                    class="action-icon confirm-icon"
                    @click="confirmModuleSelection" />
                  <Icon
                    icon="icon-shanchuguanbi"
                    class="action-icon cancel-icon"
                    @click="cancelModuleEdit" />
                </div>
              </div>
            </AsyncComponent>
          </div>
        </div>

        <!-- Parent Task -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.parentTask') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text dash-text">{{ props.dataSource?.parentTaskName || '--' }}</span>
          </div>
        </div>

        <!-- Task Type -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.type') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isTaskTypeEditing" class="info-value-content">
              <div class="task-type-content">
                <IconTask :value="currentTaskType" class="task-type-icon" />
                <span class="info-text">{{ props.dataSource?.taskType?.message }}</span>
                <template v-if="currentTaskType === 'BUG'">
                  <Tag
                    v-if="props.dataSource?.bugLevel"
                    color="error"
                    class="bug-tag">
                    {{ props.dataSource?.bugLevel?.message }}
                  </Tag>
                  <Tag
                    v-if="props.dataSource?.missingBug"
                    color="error"
                    class="bug-tag">
                    {{ t('task.detailInfo.basic.columns.missingBug') }}
                  </Tag>
                </template>
              </div>
              <Button
                type="link"
                class="edit-btn"
                @click="enterTaskTypeEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isTaskTypeEditing">
              <SelectEnum
                v-show="isTaskTypeEditing"
                ref="taskTypeSelectRef"
                v-model:value="taskTypeSelectValue"
                enumKey="TaskType"
                :placeholder="t('task.detailInfo.basic.columns.selectTaskType')"
                class="edit-input"
                @change="handleTaskTypeSelectionChange as any"
                @blur="handleTaskTypeSelectionBlur as any">
                <template #option="record">
                  <div class="flex items-center">
                    <IconTask :value="record.value as any" class="task-type-icon" />
                    <span class="ml-2">{{ record.label }}</span>
                  </div>
                </template>
              </SelectEnum>
            </AsyncComponent>
          </div>
        </div>

        <!-- Priority -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.priority') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isPriorityEditing" class="info-value-content">
              <TaskPriority :value="props.dataSource?.priority as any" />
              <Button
                type="link"
                class="edit-btn"
                @click="enterPriorityEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isPriorityEditing">
              <SelectEnum
                v-show="isPriorityEditing"
                ref="prioritySelectRef"
                v-model:value="prioritySelectValue"
                enumKey="Priority"
                :placeholder="t('task.detailInfo.basic.columns.selectPriority')"
                class="edit-input"
                @change="handlePrioritySelectionChange as any"
                @blur="handlePrioritySelectionBlur as any">
                <template #option="record">
                  <TaskPriority :value="record as any" />
                </template>
              </SelectEnum>
            </AsyncComponent>
          </div>
        </div>

        <!-- Workload Estimation Method -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.evalWorkloadMethod') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ props.dataSource?.evalWorkloadMethod?.message || '--' }}</span>
          </div>
        </div>

        <!-- Estimated Workload -->
        <div class="info-row">
          <div class="info-label">
            <span>
              {{ t('task.detailInfo.basic.columns.evalWorkload') }}
            </span>
          </div>
          <div class="info-value">
            <div v-show="!isEvalWorkloadEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !currentEvalWorkload }">{{ currentEvalWorkload || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterEvalWorkloadEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isEvalWorkloadEditing">
              <Input
                v-show="isEvalWorkloadEditing"
                ref="evalWorkloadInputRef"
                v-model:value="evalWorkloadInputValue"
                class="edit-input"
                dataType="float"
                trimAll
                :min="0.1"
                :max="1000"
                :placeholder="t('task.detailInfo.basic.columns.evalWorkloadPlaceholder')"
                @blur="handleEvalWorkloadInputBlur"
                @pressEnter="handleEvalWorkloadInputEnter" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Actual Workload -->
        <div class="info-row">
          <div class="info-label">
            <span>
              {{ t('task.detailInfo.basic.columns.actualWorkload') }}
            </span>
          </div>
          <div class="info-value">
            <div v-show="!isActualWorkloadEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !currentActualWorkload }">{{ currentActualWorkload || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterActualWorkloadEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isActualWorkloadEditing">
              <Input
                v-show="isActualWorkloadEditing"
                ref="actualWorkloadInputRef"
                v-model:value="actualWorkloadInputValue"
                class="edit-input"
                dataType="float"
                trimAll
                :min="0.1"
                :max="1000"
                :placeholder="t('task.detailInfo.basic.columns.actualWorkloadPlaceholder')"
                @blur="handleActualWorkloadInputBlur"
                @pressEnter="handleActualWorkloadInputEnter" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Process Count -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.totalProcessCount') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ totalProcessCount }}</span>
          </div>
        </div>

        <!-- Failed Count -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.processFailCount') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ processFailCount }}</span>
          </div>
        </div>

        <!-- Tags -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.tags') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isTagEditing" class="info-value-content">
              <div v-if="currentTags.length" class="tags-container">
                <div
                  v-for="item in currentTags"
                  :key="item.id"
                  class="tag-item">
                  {{ item.name }}
                </div>
              </div>
              <span v-else class="info-text dash-text">--</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterTagEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isTagEditing">
              <Select
                v-show="isTagEditing"
                ref="tagSelectRef"
                v-model:value="selectedTagIdList"
                :fieldNames="{ label: 'name', value: 'id' }"
                :maxTagCount="5"
                :maxTagTextLength="15"
                :maxTags="5"
                :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
                showSearch
                :placeholder="t('task.detailInfo.basic.columns.tagsPlaceholder')"
                mode="multiple"
                class="edit-input"
                :notFoundContent="t('task.detailInfo.basic.columns.tagsNotFound')"
                @change="handleTagSelectionChange as any"
                @blur="handleTagSelectionBlur as any" />
            </AsyncComponent>
          </div>
        </div>

        <!-- One Time Pass -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.onePass') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': onePassStatusText === '--' }">{{ onePassStatusText }}</span>
          </div>
        </div>

        <!-- Software Version -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.softwareVersion') }}</span>
          </div>
          <div class="info-value">
            <template v-if="isVersionEditing">
              <Select
                ref="versionSelectRef"
                v-model:value="versionSelectValue"
                allowClear
                :placeholder="t('task.detailInfo.basic.columns.softwareVersionPlaceholder')"
                class="edit-input"
                lazy
                :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
                :fieldNames="{value:'name', label: 'name'}"
                @blur="handleVersionSelectionBlur"
                @change="handleVersionSelectionChange">
              </Select>
            </template>
            <template v-else>
              <div class="info-value-content">
                <RouterLink
                  v-if="props.dataSource?.softwareVersion"
                  class="version-link"
                  :to="`/task#version?name=${props.dataSource?.softwareVersion}`">
                  {{ props.dataSource?.softwareVersion }}
                </RouterLink>
                <span v-else class="info-text dash-text">--</span>
                <Button
                  type="link"
                  class="edit-btn"
                  @click="enterVersionEditMode">
                  <Icon icon="icon-shuxie" />
                </Button>
              </div>
            </template>
          </div>
        </div>

        <!-- Unplanned Task -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.basic.columns.unplanned') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ props.dataSource?.unplanned ? t('task.detailInfo.basic.columns.yes') : t('task.detailInfo.basic.columns.no') }}</span>
          </div>
        </div>
      </div>

      <!-- Description Section -->
      <div class="description-section">
        <Description
          :id="currentTaskId"
          :projectId="props.projectId"
          :appInfo="props.appInfo"
          :dataSource="props.dataSource"
          @change="handleTaskInfoChange"
          @loadingChange="handleLoadingChange" />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Description section styles */
.description-section {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

/* Info row styles */
.info-row { display: flex; align-items: flex-start; min-height: auto; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }

/* Label styles */
.info-label { flex-shrink: 0; width: 70px; display: flex; align-items: center; font-size: 12px; color: #686868; font-weight: 500; line-height: 1.4; }
.info-label span { white-space: normal; word-break: break-word; line-height: 1.4; }

/* Value area styles */
.info-value { flex: 1; min-width: 0; display: flex; align-items: flex-start; justify-content: space-between; }
.info-value-content { display: flex; align-items: center; gap: 6px; width: 100%; min-height: 20px; flex: 1; min-width: 0; }

/* Text styles */
.info-text { font-size: 12px; color: #262626; line-height: 1.4; word-break: break-word; flex: 1; min-width: 0; }
.info-text.dash-text { color: #8c8c8c; }

/* Edit button styles */
.edit-btn { flex-shrink: 0; padding: 0; height: 16px; width: 16px; display: flex; align-items: center; justify-content: center; border: none; background: none; color: #1890ff !important; cursor: pointer; transition: color 0.2s; margin-left: auto; }
.edit-btn:focus { color: #1890ff !important; background: none !important; border: none !important; box-shadow: none !important; }
.edit-btn:hover { color: #1890ff; }
.edit-btn .anticon { font-size: 12px; }

/* Edit input styles */
.edit-input { width: 100%; font-size: 12px; }

/* Task type content styles */
.task-type-content { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.task-type-icon { font-size: 14px; flex-shrink: 0; }
.bug-tag { font-size: 10px; padding: 1px 4px; height: 16px; line-height: 14px; margin-left: 4px; }

/* Tags container styles */
.tags-container { display: flex; flex-wrap: wrap; gap: 4px; align-items: center; }
.tag-item { font-size: 10px; padding: 2px 6px; background: #f5f5f5; border: 1px solid #d9d9d9; border-radius: 4px; color: #595959; line-height: 1.2; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Module edit container */
.module-edit-container { display: flex; align-items: center; gap: 6px; width: 100%; }
.module-edit-actions { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.action-icon { font-size: 12px; cursor: pointer; padding: 2px; border-radius: 2px; transition: all 0.2s; }
.confirm-icon { color: #52c41a; }
.confirm-icon:hover { background: #f6ffed; color: #389e0d; }
.cancel-icon { color: #ff4d4f; }
.cancel-icon:hover { background: #fff2f0; color: #cf1322; }

/* Version link */
.version-link { color: #1890ff; text-decoration: none; font-size: 12px; line-height: 1.4; }
.version-link:hover { color: #40a9ff; text-decoration: underline; }

/* Overdue badge */
.overdue-badge { font-size: 10px; padding: 1px 4px; background: #fff2f0; border: 1px solid #ffccc7; border-radius: 2px; color: #ff4d4f; line-height: 1.2; margin-left: 6px; }

/* Legacy style compatibility */
.w-1\/2 { width: calc((100% - 20px)/2); }
.border-none { border: none; }
.edit-container { width: 100%; transform: translateY(-5px); }
</style>
