<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Tag, TreeSelect } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, IconTask, Input, Select } from '@xcan-angus/vue-ui';
import { TESTER, EvalWorkloadMethod } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';
import { TaskType, SoftwareVersionStatus } from '@/enums/enums';
import { TaskInfo } from '../../types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import TaskStatus from '@/components/TaskStatus/index.vue';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
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

// Task type editing state
const taskTypeSelectRef = ref();
const isTaskTypeEditing = ref(false);
const taskTypeDisplayMessage = ref<string>();
const taskTypeInputValue = ref<TaskInfo['taskType']['value']>();

// Sprint editing state
const sprintSelectRef = ref();
const isSprintEditing = ref(false);
const sprintDisplayMessage = ref<string>();
const sprintInputValue = ref<string>();

// Module editing state
const moduleTreeSelectRef = ref();
const isModuleEditing = ref(false);
const moduleInputValue = ref<string>();

// Version editing state
const versionSelectRef = ref();
const isVersionEditing = ref(false);
const versionInputValue = ref<string>();

// Priority editing state
const prioritySelectRef = ref();
const isPriorityEditing = ref(false);
const priorityDisplayMessage = ref<string>();
const priorityInputValue = ref<TaskInfo['priority']['value']>();

// Tag editing state
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{id: string; name: string;}[]>([]);
const selectedTagIds = ref<string[]>([]);

// Computed Properties
const currentTaskId = computed(() => props.dataSource?.id);
const taskStatus = computed(() => props.dataSource?.status);
const taskName = computed(() => props.dataSource?.name);
const taskType = computed(() => props.dataSource?.taskType?.value);
const currentSprintId = computed(() => props.dataSource?.sprintId);
const currentModuleId = computed(() => {
  if (!props.dataSource?.moduleId || props.dataSource?.moduleId === '-1') {
    return undefined;
  }
  return props.dataSource?.moduleId;
});
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
  const [error] = await task.editTaskName(currentTaskId.value, newTaskName);
  emit('loadingChange', false);
  isTaskNameEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, name: newTaskName });
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
  const [error] = await task.editActualWorkload(currentTaskId.value, { workload: newWorkload });
  emit('loadingChange', false);
  isActualWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, actualWorkload: newWorkload });
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
  const [error] = await task.editEvalWorkloadApi(currentTaskId.value, { workload: newWorkload });
  emit('loadingChange', false);
  isEvalWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, evalWorkload: newWorkload });
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
    taskIds: [currentTaskId.value],
    targetSprintId: newSprintId
  };
  const [error] = await task.moveTask(moveTaskParams);
  emit('loadingChange', false);
  isSprintEditing.value = false;
  if (error) {
    return;
  }

  emit('refresh');
  const updatedTaskInfo = await fetchTaskDetailsById(currentTaskId.value);
  emit('change', updatedTaskInfo);
};

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
  const [error] = await task.updateTask(currentTaskId.value, updateParams);
  emit('loadingChange', false);
  isModuleEditing.value = false;
  if (error) {
    return;
  }

  emit('refresh');
  const updatedTaskInfo = await fetchTaskDetailsById(currentTaskId.value);
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
const fetchTaskDetailsById = async (id: string): Promise<Partial<TaskInfo>> => {
  emit('loadingChange', true);
  const [error, res] = await task.getTaskDetail(id);
  emit('loadingChange', false);
  if (error || !res?.data) {
    return { id };
  }

  return res.data;
};

/**
 * <p>Initialize task type editing mode</p>
 * <p>Sets the current task type value and enables editing state</p>
 */
const startTaskTypeEditing = () => {
  taskTypeInputValue.value = taskType.value;
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
 * <p>Handle task type selection change</p>
 * <p>Updates the display message for the selected task type</p>
 */
const handleTaskTypeSelectionChange = async (
  value: any,
  option: any
) => {
  if (option && option.message) {
    taskTypeDisplayMessage.value = option.message;
  }
};

/**
 * <p>Confirm task type change and update task</p>
 * <p>Validates the new task type value and calls API to update task type</p>
 */
const confirmTaskTypeChange = async () => {
  const newTaskType = taskTypeInputValue.value;
  if (!newTaskType || newTaskType === taskType.value) {
    isTaskTypeEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskTaskType(currentTaskId.value, newTaskType);
  emit('loadingChange', false);
  isTaskTypeEditing.value = false;

  // Set default bug properties when task type is changed to BUG
  if (newTaskType === 'BUG') {
    await task.updateTask(currentTaskId.value, {
      bugLevel: 'MINOR',
      missingBug: false
    });
  }

  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, taskType: { value: newTaskType, message: taskTypeDisplayMessage.value! } });
};

/**
 * <p>Determine which task types should be excluded from selection</p>
 * <p>Prevents changing between API_TEST and SCENARIO_TEST types</p>
 */
const getTaskTypeExcludes = (value: { message: string; value: string }) => {
  const taskTypeValue = value.value as any;
  const currentType = taskType.value;
  if (currentTaskId.value) {
    if (currentType === TaskType.API_TEST) {
      return taskTypeValue !== TaskType.API_TEST;
    }
    if (currentType === TaskType.SCENARIO_TEST) {
      return taskTypeValue !== TaskType.SCENARIO_TEST;
    }
    return [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(taskTypeValue);
  }
  return false;
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
  const [error] = await task.editTaskPriority(currentTaskId.value, newPriority);
  emit('loadingChange', false);
  isPriorityEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, priority: { value: newPriority, message: priorityDisplayMessage.value! } });
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
  const [error] = await task.editTaskTags(currentTaskId.value, { tagIds: newTagIds });
  emit('loadingChange', false);
  isTagEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, tags: selectedTagList.value });
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
  const [error] = await task.updateTask(currentTaskId.value, { softwareVersion: newVersion || '' });
  emit('loadingChange', false);
  isVersionEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, softwareVersion: versionInputValue.value });
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
const emitTaskInfoChange = (data: Partial<TaskInfo>) => {
  emit('change', data);
};

// Lifecycle Hooks
onMounted(() => {
  loadModuleTreeData();
});
</script>

<template>
  <div class="h-full text-3 leading-5 px-5 overflow-auto">
    <div>
      <div class="text-theme-title mb-2.5 font-semibold">
        {{ t('backlog.basicInfo') }}
      </div>

      <div class="space-y-2.5">
        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.columns.code') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.code }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.columns.name') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isTaskNameEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ taskName }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startTaskNameEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isTaskNameEditing">
            <Input
              v-show="isTaskNameEditing"
              ref="taskNameInputRef"
              v-model:value="taskNameInputValue"
              :maxlength="200"
              trim
              class="edit-container"
              :placeholder="t('backlog.placeholder.taskName')"
              @blur="handleTaskNameBlur"
              @pressEnter="handleTaskNameEnterPress" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.taskStatus') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <TaskStatus :value="taskStatus" />
            <span
              v-if="isOverdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu scale-90">
                {{ t('backlog.overdue') }}
              </span>
            </span>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.sprint') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isSprintEditing" class="flex items-center">
            <span class="ml-1.5">{{ props.dataSource?.sprintName }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="startSprintEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
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
              :placeholder="t('backlog.info.basic.placeholders.selectOrSearchSprint')"
              class="edit-container"
              @change="handleSprintSelectionChange"
              @blur="confirmSprintChange" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.module') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isModuleEditing" class="flex items-center">
            <span class="ml-1.5">{{ props.dataSource?.moduleName }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="startModuleEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isModuleEditing">
            <div v-show="isModuleEditing" class="flex items-center w-full">
              <TreeSelect
                ref="moduleTreeSelectRef"
                v-model:value="moduleInputValue"
                :treeData="moduleTreeData"
                :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
                :virtual="false"
                size="small"
                showSearch
                allowClear
                class="flex-1"
                :placeholder="t('backlog.info.basic.placeholders.selectOrSearchModule')">
                <template #title="item">
                  <div class="flex items-center" :title="item.name">
                    <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                    <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </TreeSelect>

              <Icon
                icon="icon-gouxuanzhong"
                class="text-3.5 ml-2 mr-1.5 cursor-pointer text-theme-text-hover"
                @click="confirmModuleChange" />
              <Icon
                icon="icon-shanchuguanbi"
                class="text-3.5 cursor-pointer text-theme-text-hover"
                @click="cancelModuleEditing" />
            </div>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.parentTask') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.parentTaskName || '--' }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.type') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isTaskTypeEditing" class="flex items-center">
            <IconTask :value="taskType" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ props.dataSource?.taskType?.message }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="startTaskTypeEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>

            <template v-if="taskType === TaskType.BUG">
              <Tag
                v-if="props.dataSource?.bugLevel"
                color="error"
                class="ml-2 text-3 leading-4">
                {{ props.dataSource?.bugLevel?.message }}
              </Tag>
              <Tag
                v-if="props.dataSource?.missingBug"
                color="error"
                class="ml-2 text-3 leading-4">
                {{ t('backlog.info.basic.missingBug') }}
              </Tag>
            </template>
          </div>

          <AsyncComponent :visible="isTaskTypeEditing">
            <SelectEnum
              v-show="isTaskTypeEditing"
              ref="taskTypeSelectRef"
              v-model:value="taskTypeInputValue"
              :excludes="getTaskTypeExcludes"
              enumKey="TaskType"
              :placeholder="t('backlog.info.basic.placeholders.selectTaskType')"
              class="edit-container"
              @change="handleTaskTypeSelectionChange"
              @blur="confirmTaskTypeChange">
              <template #option="record">
                <div class="flex items-center">
                  <IconTask :value="record.value as any" class="text-4 flex-shrink-0" />
                  <span class="ml-2">{{ record.label }}</span>
                </div>
              </template>
            </SelectEnum>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.priority') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isPriorityEditing" class="flex items-center">
            <TaskPriority :value="props.dataSource?.priority" />
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="startPriorityEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isPriorityEditing">
            <SelectEnum
              v-show="isPriorityEditing"
              ref="prioritySelectRef"
              v-model:value="priorityInputValue"
              enumKey="Priority"
              :placeholder="t('backlog.info.basic.placeholders.selectPriority')"
              class="edit-container"
              @change="handlePrioritySelectionChange"
              @blur="confirmPriorityChange">
              <template #option="record">
                <TaskPriority :value="{ value: record.value as any, message: record.label }" />
              </template>
            </SelectEnum>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.evalWorkloadMethod') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.evalWorkloadMethod?.message }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>
              {{
                evalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                  ? t('backlog.info.basic.evalStoryPoint') : t('backlog.info.basic.evalWorkload')
              }}
            </span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isEvalWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ evalWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startEvalWorkloadEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isEvalWorkloadEditing">
            <Input
              v-show="isEvalWorkloadEditing"
              ref="evalWorkloadInputRef"
              v-model:value="evalWorkloadInputValue"
              class="edit-container"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('backlog.info.basic.placeholders.workloadRange')"
              @blur="handleEvalWorkloadBlur"
              @pressEnter="handleEvalWorkloadEnterPress" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>
              {{
                evalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                  ? t('backlog.info.basic.evalStoryPoint') : t('backlog.info.basic.evalWorkload')
              }}
            </span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isActualWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ actualWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startActualWorkloadEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isActualWorkloadEditing">
            <Input
              v-show="isActualWorkloadEditing"
              ref="actualWorkloadInputRef"
              v-model:value="actualWorkloadInputValue"
              class="edit-container"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('backlog.info.basic.placeholders.workloadRange')"
              @blur="handleActualWorkloadBlur"
              @pressEnter="handleActualWorkloadEnterPress" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.processCount') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ totalProcessCount }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.processFailCount') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ failedProcessCount }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.tags') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isTagEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div v-if="taskTags.length" class="flex items-center flex-wrap transform-gpu -translate-y-0.25">
              <div
                v-for="item in taskTags"
                :key="item.id"
                class="px-2 h-5 leading-4.5 mr-1 mb-1 rounded border border-solid border-border-divider bg-gray-light text-theme-sub-content">
                {{ item.name }}
              </div>
            </div>
            <div v-else>--</div>

            <Button
              type="link"
              class="flex-shrink-0 ml-1 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startTagEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
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
              :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
              showSearch
              :placeholder="t('backlog.info.basic.placeholders.maxTags')"
              mode="multiple"
              class="edit-container"
              :notFoundContent="t('backlog.info.basic.messages.contactAdminForTags')"
              @change="handleTagSelectionChange"
              @blur="confirmTagChanges" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.oneTimePass') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ oneTimePassStatus }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.softwareVersion') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="flex-1">
            <template v-if="isVersionEditing">
              <Select
                ref="versionSelectRef"
                v-model:value="versionInputValue"
                allowClear
                :placeholder="t('backlog.info.basic.placeholders.selectSoftwareVersion')"
                class="w-full"
                lazy
                :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
                :fieldNames="{value:'name', label: 'name'}"
                @blur="confirmVersionChange"
                @change="handleVersionSelectionChange">
              </Select>
            </template>
            <template v-else>
              <div class="flex space-x-1">
                <RouterLink
                  v-if="props.dataSource?.softwareVersion"
                  class="text-theme-special"
                  :to="`/task#version?name=${props.dataSource?.softwareVersion}`">
                  {{ props.dataSource?.softwareVersion }}
                </RouterLink>
                <template v-else>
                  --
                </template>

                <Button
                  type="link"
                  class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
                  @click="startVersionEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>
            </template>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('backlog.info.basic.unplannedTask') }}</span>
            <Colon class="w-1" />
          </div>
          <div>
            {{ props.dataSource?.unplanned ? t('status.yes') : t('status.no') }}
          </div>
        </div>
      </div>
    </div>

    <Description
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :dataSource="props.dataSource"
      @change="emitTaskInfoChange"
      @loadingChange="emitLoadingStateChange" />
  </div>
</template>

<style scoped>
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
