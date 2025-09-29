<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref } from 'vue';
import { Button, TreeSelect } from 'ant-design-vue';
import { AsyncComponent, Icon, IconTask, Input, ScriptTypeTag, Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER, EvalWorkloadMethod } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';
import { SoftwareVersionStatus } from '@/enums/enums';

import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import TaskStatus from '@/components/TaskStatus/index.vue';

import { TaskDetail } from '@/views/task/types';
import { AssocCaseProps } from '@/views/task/task/list/types';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
  (event: 'refresh'): void;
}>();

// Async Components
const Description = defineAsyncComponent(() => import('@/views/task/backlog/info/Description.vue'));

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

// Priority editing state
const prioritySelectRef = ref();
const isPriorityEditing = ref(false);
const priorityDisplayMessage = ref<string>();
const priorityInputValue = ref<TaskDetail['priority']['value']>();

// Tag editing state
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{ id: string; name: string; }[]>([]);
const selectedTagIds = ref<string[]>([]);

// Module editing state
const moduleTreeSelectRef = ref();
const isModuleEditing = ref(false);
const moduleInputValue = ref<string>();

// Version editing state
const versionSelectRef = ref();
const isVersionEditing = ref(false);
const versionInputValue = ref<string>();

// Sprint editing state
const sprintSelectRef = ref();
const isSprintEditing = ref(false);
const sprintDisplayMessage = ref<string>();
const sprintInputValue = ref<string>();

// Computed Properties
const taskId = computed(() => props.dataSource?.id);
const taskStatus = computed(() => props.dataSource?.status);
const taskName = computed(() => props.dataSource?.name);
const taskType = computed(() => props.dataSource?.taskType?.value);
const taskPriority = computed(() => props.dataSource?.priority?.value);
const taskTags = computed(() => props.dataSource?.tags || []);
const taskTagIds = computed(() => props.dataSource?.tags?.map(item => item.id) || []);
const evalWorkloadMethod = computed(() => props.dataSource?.evalWorkloadMethod?.value);
const evalWorkload = computed(() => props.dataSource?.evalWorkload);
const actualWorkload = computed(() => props.dataSource?.actualWorkload);
const isOverdue = computed(() => props.dataSource?.overdue);
const totalProcessCount = computed(() => +(props.dataSource?.totalNum || 0));
const failedProcessCount = computed(() => +(props.dataSource?.failNum || 0));

/**
 * <p>Calculate one-time pass status based on process counts</p>
 * <p>Returns '--' if no processes, 'Yes' if all passed, 'No' if any failed</p>
 */
const oneTimePassStatus = computed(() => {
  if (totalProcessCount.value <= 0) {
    return '--';
  }
  return failedProcessCount.value === 0 ? t('status.yes') : t('status.no');
});

/**
 * <p>Initialize module editing mode</p>
 * <p>Sets the current module value and enables editing state</p>
 */
const startModuleEditing = () => {
  moduleInputValue.value = currentModuleId.value;
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
 * <p>Confirm module change and update task</p>
 * <p>Validates the new module value and calls API to update task</p>
 */
const confirmModuleChange = async () => {
  const newModuleId = moduleInputValue.value;
  if (!newModuleId || newModuleId === currentModuleId.value) {
    isModuleEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const updateParams = {
    moduleId: newModuleId
  };
  const [error] = await task.updateTask(taskId.value, updateParams);
  emit('loadingChange', false);
  isModuleEditing.value = false;
  if (error) {
    return;
  }

  emit('refresh');
  const updatedTaskInfo = await fetchTaskDetailsById(taskId.value);
  emit('change', updatedTaskInfo);
};

/**
 * <p>Cancel module editing without saving changes</p>
 */
const cancelModuleEditing = () => {
  isModuleEditing.value = false;
};

// Data Loading Functions
const moduleTreeData = ref([]);

/**
 * <p>Fetch module tree data for the current project</p>
 * <p>Loads hierarchical module structure for selection</p>
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
 * <p>Fetch detailed task information by ID</p>
 * <p>Returns partial task info for updating component state</p>
 */
const fetchTaskDetailsById = async (id: string): Promise<Partial<TaskDetail>> => {
  emit('loadingChange', true);
  const [error, res] = await task.getTaskDetail(id);
  emit('loadingChange', false);
  if (error || !res?.data) {
    return { id };
  }
  return res.data;
};

/**
 * <p>Initialize sprint editing mode</p>
 * <p>Sets the current sprint value and enables editing state</p>
 */
const startSprintEditing = () => {
  sprintInputValue.value = currentSprintId.value;
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
 * <p>Handle sprint selection change</p>
 * <p>Updates the display message for the selected sprint</p>
 */
const handleSprintSelectionChange = async (
  value: any,
  option: any
) => {
  if (option && option.message) {
    sprintDisplayMessage.value = option.message;
  }
};

/**
 * <p>Confirm sprint change and move task to new sprint</p>
 * <p>Validates the new sprint value and calls API to move task</p>
 */
const confirmSprintChange = async () => {
  const newSprintId = sprintInputValue.value;
  if (!newSprintId || newSprintId === currentSprintId.value) {
    isSprintEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const moveTaskParams = {
    taskIds: [taskId.value],
    targetSprintId: newSprintId
  };
  const [error] = await task.moveTask(moveTaskParams);
  emit('loadingChange', false);
  isSprintEditing.value = false;
  if (error) {
    return;
  }

  emit('refresh');
  const updatedTaskInfo = await fetchTaskDetailsById(taskId.value);
  emit('change', updatedTaskInfo);
};

/**
 * <p>Initialize task name editing mode</p>
 * <p>Sets the current task name value and enables editing state</p>
 */
const startTaskNameEditing = () => {
  taskNameInputValue.value = taskName.value;
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
 * <p>Handle task name input blur event</p>
 * <p>Validates the new name value and calls API to update task name</p>
 */
const handleTaskNameBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newTaskName = target?.value;
  if (!newTaskName || newTaskName === taskName.value) {
    isTaskNameEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskName(taskId.value, newTaskName);
  emit('loadingChange', false);
  isTaskNameEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, name: newTaskName });
};

/**
 * <p>Handle Enter key press in task name input</p>
 * <p>Triggers blur event to save the task name</p>
 */
const handleTaskNameEnterPress = () => {
  if (typeof taskNameInputRef.value?.blur === 'function') {
    taskNameInputRef.value.blur();
  }
};

/**
 * <p>Initialize actual workload editing mode</p>
 * <p>Sets the current actual workload value and enables editing state</p>
 */
const startActualWorkloadEditing = () => {
  actualWorkloadInputValue.value = actualWorkload.value;
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
 * <p>Handle actual workload input blur event</p>
 * <p>Validates the new workload value and calls API to update actual workload</p>
 */
const handleActualWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newWorkload = target?.value;
  if (newWorkload === actualWorkload.value) {
    isActualWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editActualWorkload(taskId.value, { workload: newWorkload });
  emit('loadingChange', false);
  isActualWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, actualWorkload: newWorkload });
};

/**
 * <p>Handle Enter key press in actual workload input</p>
 * <p>Triggers blur event to save the actual workload</p>
 */
const handleActualWorkloadEnterPress = () => {
  if (typeof actualWorkloadInputRef.value?.blur === 'function') {
    actualWorkloadInputRef.value.blur();
  }
};

/**
 * <p>Initialize evaluation workload editing mode</p>
 * <p>Sets the current evaluation workload value and enables editing state</p>
 */
const startEvalWorkloadEditing = () => {
  evalWorkloadInputValue.value = evalWorkload.value;
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
 * <p>Handle evaluation workload input blur event</p>
 * <p>Validates the new workload value and calls API to update evaluation workload</p>
 */
const handleEvalWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newWorkload = target?.value;
  if (newWorkload === evalWorkload.value) {
    isEvalWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editEvalWorkloadApi(taskId.value, { workload: newWorkload });
  emit('loadingChange', false);
  isEvalWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, evalWorkload: newWorkload });
};

/**
 * <p>Handle Enter key press in evaluation workload input</p>
 * <p>Triggers blur event to save the evaluation workload</p>
 */
const handleEvalWorkloadEnterPress = () => {
  if (typeof evalWorkloadInputRef.value?.blur === 'function') {
    evalWorkloadInputRef.value.blur();
  }
};

/**
 * <p>Initialize priority editing mode</p>
 * <p>Sets the current priority value and enables editing state</p>
 */
const startPriorityEditing = () => {
  priorityInputValue.value = taskPriority.value;
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
 * <p>Handle priority selection change</p>
 * <p>Updates the display message for the selected priority</p>
 */
const handlePrioritySelectionChange = async (
  value: any,
  option: any
) => {
  if (option && option.message) {
    priorityDisplayMessage.value = option.message;
  }
};

/**
 * <p>Confirm priority change and update task</p>
 * <p>Validates the new priority value and calls API to update task priority</p>
 */
const confirmPriorityChange = async () => {
  const newPriority = priorityInputValue.value;
  if (!newPriority || newPriority === taskPriority.value) {
    isPriorityEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskPriority(taskId.value, newPriority);
  emit('loadingChange', false);
  isPriorityEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, priority: { value: newPriority, message: priorityDisplayMessage.value! } });
};

/**
 * <p>Initialize tag editing mode</p>
 * <p>Sets the current tag values and enables editing state</p>
 */
const startTagEditing = () => {
  selectedTagIds.value = taskTagIds.value;
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
 * <p>Handle tag selection change</p>
 * <p>Updates the selected tag list based on user selection</p>
 */
const handleTagSelectionChange = async (
  value: any,
  options: any
) => {
  if (Array.isArray(options)) {
    selectedTagList.value = options;
  }
};

/**
 * <p>Confirm tag changes and update task</p>
 * <p>Validates the new tag values and calls API to update task tags</p>
 */
const confirmTagChanges = async () => {
  const newTagIds = selectedTagIds.value;
  if (isEqual(newTagIds, taskTagIds.value)) {
    isTagEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskTags(taskId.value, { tagIds: newTagIds });
  emit('loadingChange', false);
  isTagEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, tags: selectedTagList.value });
};

/**
 * <p>Initialize version editing mode</p>
 * <p>Sets the current version value and enables editing state</p>
 */
const startVersionEditing = () => {
  isVersionEditing.value = true;
  versionInputValue.value = props.dataSource?.softwareVersion;
  nextTick(() => {
    setTimeout(() => {
      if (typeof versionSelectRef.value?.focus === 'function') {
        versionSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Handle version selection change</p>
 * <p>Updates the version input value based on user selection</p>
 */
const handleVersionSelectionChange = (value) => {
  versionInputValue.value = value;
};

/**
 * <p>Confirm version change and update task</p>
 * <p>Validates the new version value and calls API to update task version</p>
 */
const confirmVersionChange = async () => {
  const newVersion = versionInputValue.value;
  if (newVersion === props.dataSource?.softwareVersion) {
    isVersionEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.updateTask(taskId.value, { softwareVersion: newVersion || '' });
  emit('loadingChange', false);
  isVersionEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, softwareVersion: versionInputValue.value });
};

/**
 * <p>Emit loading state change event</p>
 * <p>Used for passing loading state to parent component</p>
 */
const emitLoadingStateChange = (value: boolean) => {
  emit('loadingChange', value);
};

/**
 * <p>Emit task info change event</p>
 * <p>Used for passing task data changes to parent component</p>
 */
const emitTaskInfoChange = (data: Partial<TaskDetail>) => {
  emit('change', data);
};

// Additional Computed Properties
const currentSprintId = computed(() => props.dataSource?.sprintId);
const currentModuleId = computed(() => {
  if (!props.dataSource?.moduleId || props.dataSource?.moduleId === '-1') {
    return undefined;
  }
  return props.dataSource?.moduleId;
});

// Lifecycle Hooks
onMounted(() => {
  loadModuleTreeData();
});
</script>

<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.basicInfo') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Task Code -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('backlog.info.apis.number') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ props.dataSource?.code }}</span>
          </div>
        </div>

        <!-- Task Name -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.name') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isTaskNameEditing" class="info-value-content">
              <span class="info-text">{{ taskName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startTaskNameEditing">
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
                :placeholder="t('backlog.info.apis.placeholders.taskName')"
                @blur="handleTaskNameBlur"
                @pressEnter="handleTaskNameEnterPress" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Task Type -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.type') }}</span>
          </div>
          <div class="info-value">
            <div class="info-value-content">
              <IconTask :value="taskType" class="task-type-icon" />
              <span class="info-text">{{ props.dataSource?.taskType?.message }}</span>
            </div>
          </div>
        </div>

        <!-- Test Type -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.testType') }}</span>
          </div>
          <div class="info-value">
            <ScriptTypeTag :value="props.dataSource?.testType" />
          </div>
        </div>

        <!-- Task Status -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.taskStatus') }}</span>
          </div>
          <div class="info-value">
            <div class="info-value-content">
              <TaskStatus :value="taskStatus" />
              <span
                v-if="isOverdue"
                class="overdue-badge">
                {{ t('status.overdue') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Sprint -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.sprint') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isSprintEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !props.dataSource?.sprintName }">{{ props.dataSource?.sprintName || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startSprintEditing">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isSprintEditing">
              <Select
                v-show="isSprintEditing"
                ref="sprintSelectRef"
                v-model:value="sprintInputValue"
                :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
                :fieldNames="{ value: 'id', label: 'name' }"
                showSearch
                :placeholder="t('backlog.info.apis.placeholders.selectOrSearchSprint')"
                class="edit-input"
                @change="handleSprintSelectionChange"
                @blur="confirmSprintChange" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Module -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.module') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isModuleEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !props.dataSource?.moduleName }">{{ props.dataSource?.moduleName || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startModuleEditing">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isModuleEditing">
              <div v-show="isModuleEditing" class="module-edit-container">
                <TreeSelect
                  ref="moduleTreeSelectRef"
                  v-model:value="moduleInputValue"
                  :treeData="moduleTreeData"
                  :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
                  :virtual="false"
                  size="small"
                  showSearch
                  allowClear
                  class="edit-input"
                  :placeholder="t('backlog.info.apis.placeholders.selectOrSearchModule')">
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
                    @click="confirmModuleChange" />
                  <Icon
                    icon="icon-shanchuguanbi"
                    class="action-icon cancel-icon"
                    @click="cancelModuleEditing" />
                </div>
              </div>
            </AsyncComponent>
          </div>
        </div>

        <!-- Parent Task -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.parentName') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text dash-text">{{ props.dataSource?.parentTaskName || '--' }}</span>
          </div>
        </div>

        <!-- Service -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.service') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ props.dataSource?.targetParentName }}</span>
          </div>
        </div>

        <!-- API -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.api') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ props.dataSource?.targetName }}</span>
          </div>
        </div>

        <!-- Priority -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.priority') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isPriorityEditing" class="info-value-content">
              <TaskPriority :value="props.dataSource?.priority" />
              <Button
                type="link"
                class="edit-btn"
                @click="startPriorityEditing">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isPriorityEditing">
              <SelectEnum
                v-show="isPriorityEditing"
                ref="prioritySelectRef"
                v-model:value="priorityInputValue"
                enumKey="Priority"
                :placeholder="t('backlog.info.apis.placeholders.selectPriority')"
                class="edit-input"
                @change="handlePrioritySelectionChange"
                @blur="confirmPriorityChange">
                <template #option="record">
                  <TaskPriority :value="{ value: record.value as any, message: record.label }" />
                </template>
              </SelectEnum>
            </AsyncComponent>
          </div>
        </div>

        <!-- Workload Estimation Method -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.evalWorkloadMethod') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ props.dataSource?.evalWorkloadMethod?.message || '--' }}</span>
          </div>
        </div>

        <!-- Estimated Workload -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.evalWorkload') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isEvalWorkloadEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !evalWorkload }">{{ evalWorkload || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startEvalWorkloadEditing">
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
                :placeholder="t('backlog.info.apis.placeholders.workloadRange')"
                @blur="handleEvalWorkloadBlur"
                @pressEnter="handleEvalWorkloadEnterPress" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Actual Workload -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.evalWorkload') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isActualWorkloadEditing" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !actualWorkload }">{{ actualWorkload || '--' }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startActualWorkloadEditing">
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
                :placeholder="t('backlog.info.apis.placeholders.workloadRange')"
                @blur="handleActualWorkloadBlur"
                @pressEnter="handleActualWorkloadEnterPress" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Process Count -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('backlog.info.apis.processCount') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ totalProcessCount }}</span>
          </div>
        </div>

        <!-- Failed Count -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('backlog.info.apis.processFailCount') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ failedProcessCount }}</span>
          </div>
        </div>

        <!-- Tags -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.tag') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isTagEditing" class="info-value-content">
              <div v-if="taskTags.length" class="tags-container">
                <div
                  v-for="item in taskTags"
                  :key="item.id"
                  class="tag-item">
                  {{ item.name }}
                </div>
              </div>
              <div v-else class="info-text dash-text">--</div>
              <Button
                type="link"
                class="edit-btn"
                @click="startTagEditing">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isTagEditing">
              <Select
                v-show="isTagEditing"
                ref="tagSelectRef"
                v-model:value="selectedTagIds"
                :fieldNames="{ label: 'name', value: 'id' }"
                :maxTagCount="5"
                :maxTagTextLength="15"
                :maxTags="5"
                :action="`${TESTER}/task/tag?projectId=${props.projectId}&fullTextSearch=true`"
                showSearch
                allowClear
                :placeholder="t('backlog.info.apis.placeholders.maxTags')"
                mode="multiple"
                class="edit-input"
                :notFoundContent="t('backlog.info.apis.messages.contactAdminForTags')"
                @change="handleTagSelectionChange"
                @blur="confirmTagChanges" />
            </AsyncComponent>
          </div>
        </div>

        <!-- One Time Pass -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('backlog.info.apis.oneTimePass') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': oneTimePassStatus === '--' }">{{ oneTimePassStatus }}</span>
          </div>
        </div>
      </div>

      <!-- Software Version -->
      <div class="info-row">
        <div class="info-label">
          <span>{{ t('common.softwareVersion') }}</span>
        </div>
        <div class="info-value">
          <template v-if="isVersionEditing">
            <Select
              ref="versionSelectRef"
              v-model:value="versionInputValue"
              allowClear
              :placeholder="t('backlog.info.apis.placeholders.selectSoftwareVersion')"
              lazy
              class="edit-input"
              :action="`${TESTER}/software/version?projectId=${props.projectId}`"
              :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
              :fieldNames="{value:'name', label: 'name'}"
              @blur="confirmVersionChange"
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
                @click="startVersionEditing">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
          </template>
        </div>
      </div>

      <!-- Unplanned Task -->
      <div class="info-row">
        <div class="info-label">
          <span>{{ t('common.unplanned') }}</span>
        </div>
        <div class="info-value">
          <span class="info-text">{{ props.dataSource?.unplanned ? t('status.yes') : t('status.no') }}</span>
        </div>
      </div>

      <!-- Description Section -->
      <div class="description-section">
        <Description
          :projectId="props.projectId"
          :appInfo="props.appInfo"
          :dataSource="props.dataSource"
          @change="emitTaskInfoChange"
          @loadingChange="emitLoadingStateChange" />
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
.info-row {
  display: flex;
  align-items: flex-start;
  min-height: auto;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

/* Label styles */
.info-label {
  flex-shrink: 0;
  width: 70px;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #686868;
  font-weight: 500;
  line-height: 1.4;
}

.info-label span {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

/* Value area styles */
.info-value {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.info-value-content {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 20px;
  flex: 1;
  min-width: 0;
}

.info-text {
  font-size: 12px;
  color: #262626;
  line-height: 1.4;
  word-break: break-word;
  flex: 1;
  min-width: 0;
}

.info-text.dash-text {
  color: #8c8c8c;
}

/* Edit button styles */
.edit-btn {
  flex-shrink: 0;
  padding: 0;
  height: 16px;
  width: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #1890ff !important;
  cursor: pointer;
  transition: color 0.2s;
  margin-left: auto;
}

.edit-btn:focus {
  color: #1890ff !important;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

.edit-btn:hover {
  color: #1890ff;
}

.edit-btn .anticon {
  font-size: 12px;
}

/* Edit input styles */
.edit-input {
  width: 100%;
  font-size: 12px;
}

/* Task type content styles */
.task-type-content {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.task-type-icon {
  font-size: 14px;
  flex-shrink: 0;
}

/* Tags container styles */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.tag-item {
  font-size: 10px;
  padding: 2px 6px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  color: #595959;
  line-height: 1.2;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Module edit container styles */
.module-edit-container {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}

.module-edit-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.action-icon {
  font-size: 12px;
  cursor: pointer;
  padding: 2px;
  border-radius: 2px;
  transition: all 0.2s;
}

.confirm-icon {
  color: #52c41a;
}

.confirm-icon:hover {
  background: #f6ffed;
  color: #389e0d;
}

.cancel-icon {
  color: #ff4d4f;
}

.cancel-icon:hover {
  background: #fff2f0;
  color: #cf1322;
}

/* Version link styles */
.version-link {
  color: #1890ff;
  text-decoration: none;
  font-size: 12px;
  line-height: 1.4;
}

.version-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* Overdue badge styles */
.overdue-badge {
  font-size: 10px;
  padding: 1px 4px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 2px;
  color: #ff4d4f;
  line-height: 1.2;
  margin-left: 6px;
}

/* Legacy style compatibility */
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.border-none {
  border: none;
}

.edit-container {
  width: 100%;
  transform: translateY(-5px);
}
</style>
