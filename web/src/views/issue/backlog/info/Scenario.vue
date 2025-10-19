<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref } from 'vue';
import { Button, TreeSelect } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, IconTask, Input, ScriptTypeTag, Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { modules, issue } from '@/api/tester';
import { TaskDetail } from '../../types';
import { TaskDetailProps } from '@/views/issue/issue/list/types';
import { SoftwareVersionStatus } from '@/enums/enums';

import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import TaskStatus from '@/components/TaskStatus/index.vue';

// Async Components
const Description = defineAsyncComponent(() => import('@/views/issue/backlog/info/Description.vue'));

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
  (event: 'refresh'): void;
}>();

// Reactive State Variables - Task Name Management
const taskNameInputRef = ref();
const isTaskNameEditing = ref(false);
const taskNameInputValue = ref<string>();

// Reactive State Variables - Workload Management
const evalWorkloadInputRef = ref();
const isEvalWorkloadEditing = ref(false);
const evalWorkloadInputValue = ref<string>();

const actualWorkloadInputRef = ref();
const isActualWorkloadEditing = ref(false);
const actualWorkloadInputValue = ref<string>();

// Reactive State Variables - Priority Management
const prioritySelectRef = ref();
const isPriorityEditing = ref(false);
const priorityDisplayMessage = ref<string>();
const priorityInputValue = ref<TaskDetail['priority']['value']>();

// Reactive State Variables - Tag Management
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{id:string;name:string;}[]>([]);
const selectedTagIdList = ref<string[]>([]);

// Reactive State Variables - Module Management
const moduleTreeSelectRef = ref();
const isModuleEditing = ref(false);
const moduleInputValue = ref<string>();

// Reactive State Variables - Version Management
const versionSelectRef = ref();
const isVersionEditing = ref(false);
const versionInputValue = ref<string>();

// Reactive State Variables - Sprint Management
const sprintSelectRef = ref();
const isSprintEditing = ref(false);
const sprintDisplayMessage = ref<string>();
const sprintInputValue = ref<string>();

// Computed Properties
const currentSprintId = computed(() => props.dataSource?.sprintId);
const currentModuleId = computed(() => {
  if (!props.dataSource?.moduleId || props.dataSource?.moduleId === '-1') {
    return undefined;
  }
  return props.dataSource?.moduleId;
});
const currentTaskId = computed(() => props.dataSource?.id);
const taskStatus = computed(() => props.dataSource?.status);
const taskName = computed(() => props.dataSource?.name);
const taskType = computed(() => props.dataSource?.taskType?.value);
const taskPriority = computed(() => props.dataSource?.priority?.value);
const taskTags = computed(() => props.dataSource?.tags || []);
const taskTagIds = computed(() => props.dataSource?.tags?.map(item => item.id) || []);
const evalWorkload = computed(() => props.dataSource?.evalWorkload);
const actualWorkload = computed(() => props.dataSource?.actualWorkload);
const isOverdue = computed(() => props.dataSource?.overdue);
const totalProcessCount = computed(() => +(props.dataSource?.totalNum || 0));
const failedProcessCount = computed(() => +(props.dataSource?.failNum || 0));
const oneTimePassStatus = computed(() => {
  if (totalProcessCount.value <= 0) {
    return '--';
  }
  return failedProcessCount.value === 0 ? t('status.yes') : t('status.no');
});

// Module Management Functions
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
 * <p>Confirm module change</p>
 * <p>Validates the selection and calls API to update module if changed</p>
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
  const [error] = await issue.updateTask(currentTaskId.value, updateParams);
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
 * <p>Cancel module editing</p>
 * <p>Exits editing mode without saving changes</p>
 */
const cancelModuleEditing = () => {
  isModuleEditing.value = false;
};

// Data Loading Functions
const moduleTreeData = ref([]);

/**
 * <p>Load module tree data</p>
 * <p>Fetches the module tree structure for the current project</p>
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
 * <p>Load task details by ID</p>
 * <p>Fetches complete task information from the server</p>
 */
const fetchTaskDetailsById = async (id: string): Promise<Partial<TaskDetail>> => {
  emit('loadingChange', true);
  const [error, res] = await issue.getTaskDetail(id);
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
 * <p>Updates the display message when user selects a new sprint</p>
 */
const handleSprintSelectionChange = async (value: any, option: any) => {
  if (option && option.message) {
    sprintDisplayMessage.value = option.message;
  }
};

/**
 * <p>Handle sprint input blur event</p>
 * <p>Validates the selection and calls API to move task to new sprint if changed</p>
 */
const handleSprintBlur = async () => {
  const newSprintId = sprintInputValue.value;
  if (!newSprintId || newSprintId === currentSprintId.value) {
    isSprintEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const moveParams = {
    taskIds: [currentTaskId.value],
    targetSprintId: newSprintId
  };
  const [error] = await issue.moveTask(moveParams);
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
 * <p>Validates the input and calls API to update task name if changed</p>
 */
const handleTaskNameBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newTaskName = target.value;
  if (!newTaskName || newTaskName === taskName.value) {
    isTaskNameEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.editTaskName(currentTaskId.value, newTaskName);
  emit('loadingChange', false);
  isTaskNameEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, name: newTaskName });
};

/**
 * <p>Handle task name input enter press</p>
 * <p>Triggers blur event to save the input</p>
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
 * <p>Validates the input and calls API to update actual workload if changed</p>
 */
const handleActualWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newActualWorkload = target.value;
  if (newActualWorkload === actualWorkload.value) {
    isActualWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.editActualWorkload(currentTaskId.value, { workload: newActualWorkload });
  emit('loadingChange', false);
  isActualWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, actualWorkload: newActualWorkload });
};

/**
 * <p>Handle actual workload input enter press</p>
 * <p>Triggers blur event to save the input</p>
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
 * <p>Validates the input and calls API to update evaluation workload if changed</p>
 */
const handleEvalWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newEvalWorkload = target.value;
  if (newEvalWorkload === evalWorkload.value) {
    isEvalWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.editEvalWorkloadApi(currentTaskId.value, { workload: newEvalWorkload });
  emit('loadingChange', false);
  isEvalWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, evalWorkload: newEvalWorkload });
};

/**
 * <p>Handle evaluation workload input enter press</p>
 * <p>Triggers blur event to save the input</p>
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
 * <p>Updates the display message when user selects a new priority</p>
 */
const handlePrioritySelectionChange = async (value: any, option: any) => {
  if (option && option.message) {
    priorityDisplayMessage.value = option.message;
  }
};

/**
 * <p>Handle priority input blur event</p>
 * <p>Validates the selection and calls API to update priority if changed</p>
 */
const handlePriorityBlur = async () => {
  const newPriority = priorityInputValue.value;
  if (!newPriority || newPriority === taskPriority.value) {
    isPriorityEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.editTaskPriority(currentTaskId.value, newPriority);
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
  selectedTagIdList.value = taskTagIds.value;
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
 * <p>Updates the selected tag list when user selects new tags</p>
 */
const handleTagSelectionChange = async (value: any, options: any) => {
  if (Array.isArray(options)) {
    selectedTagList.value = options;
  }
};

/**
 * <p>Handle tag input blur event</p>
 * <p>Validates the selection and calls API to update tags if changed</p>
 */
const handleTagBlur = async () => {
  const newTagIds = selectedTagIdList.value;
  if (isEqual(newTagIds, taskTagIds.value)) {
    isTagEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.editTaskTags(currentTaskId.value, { tagIds: newTagIds });
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
 * <p>Updates the version value when user selects a new version</p>
 */
const handleVersionSelectionChange = (value: any) => {
  versionInputValue.value = value;
};

/**
 * <p>Handle version input blur event</p>
 * <p>Validates the selection and calls API to update version if changed</p>
 */
const handleVersionBlur = async () => {
  const newVersion = versionInputValue.value;
  if (newVersion === props.dataSource?.softwareVersion) {
    isVersionEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.updateTask(currentTaskId.value, { softwareVersion: newVersion || '' });
  emit('loadingChange', false);
  isVersionEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, softwareVersion: versionInputValue.value });
};

/**
 * <p>Emit loading state change event</p>
 * <p>Forwards loading state changes to parent component</p>
 */
const emitLoadingStateChange = (value: boolean) => {
  emit('loadingChange', value);
};

/**
 * <p>Emit task info change event</p>
 * <p>Forwards task info changes to parent component</p>
 */
const emitTaskInfoChange = (data: Partial<TaskDetail>) => {
  emit('change', data);
};

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
        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.code') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.code }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.scenario') }}</span>
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
              :placeholder="t('common.placeholders.inputName2')"
              @blur="handleTaskNameBlur"
              @pressEnter="handleTaskNameEnterPress" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.type') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <IconTask :value="taskType" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ props.dataSource?.taskType?.message }}</span>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.testType') }}</span>
            <Colon class="w-1" />
          </div>

          <ScriptTypeTag :value="props.dataSource?.testType" />
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.taskStatus') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <TaskStatus :value="taskStatus" />
            <span
              v-if="isOverdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu scale-90">
                {{ t('status.overdue') }}
              </span>
            </span>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.sprint') }}</span>
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
              :placeholder="t('common.placeholders.selectOrSearchSprint')"
              class="edit-container"
              @change="handleSprintSelectionChange"
              @blur="handleSprintBlur" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.module') }}</span>
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
                :placeholder="t('common.placeholders.selectOrSearchModule')">
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
            <span>{{ t('common.parentName') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.parentTaskName || '--' }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.scenario') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.targetName }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.priority') }}</span>
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
              :placeholder="t('common.placeholders.selectPriority')"
              class="edit-container max-w-52"
              @change="handlePrioritySelectionChange"
              @blur="handlePriorityBlur">
              <template #option="record">
                <TaskPriority :value="{ value: record.value as any, message: record.label }" />
              </template>
            </SelectEnum>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.evalWorkloadMethod') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.evalWorkloadMethod?.message }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.evalWorkload') }}</span>
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
              class="right-component max-w-52"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('common.placeholders.workloadRange')"
              @blur="handleEvalWorkloadBlur"
              @pressEnter="handleEvalWorkloadEnterPress" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.evalWorkload') }}</span>
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
              class="right-component max-w-52"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('common.placeholders.workloadRange')"
              @blur="handleActualWorkloadBlur"
              @pressEnter="handleActualWorkloadEnterPress" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.counts.processCount') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ totalProcessCount }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.counts.processFailCount') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ failedProcessCount }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.tag') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isTagEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div v-if="taskTags.length" class="flex items-center flex-wrap transform-gpu -translate-y-0.25">
              <div
                v-for="item in taskTags"
                :key="item.id"
                class="px-2 h-5.5 leading-5 mr-2 mb-2 rounded border border-solid border-border-divider bg-gray-light text-theme-sub-content">
                {{ item.name }}
              </div>
            </div>
            <div v-else>--</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startTagEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
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
              :action="`${TESTER}/task/tag?projectId=${props.projectId}&fullTextSearch=true`"
              allowClear
              showSearch
              :placeholder="t('common.tag')"
              mode="multiple"
              class="edit-container"
              :notFoundContent="t('backlog.edit.messages.contactAdminForTags')"
              @change="handleTagSelectionChange"
              @blur="handleTagBlur" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.counts.oneTimePassed') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ oneTimePassStatus }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('common.softwareVersion') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="truncate flex-1" :title="props.dataSource?.softwareVersion">
            <template v-if="isVersionEditing">
              <Select
                ref="versionSelectRef"
                v-model:value="versionInputValue"
                allowClear
                :placeholder="t('common.placeholders.selectSoftwareVersion')"
                lazy
                class="w-full"
                :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
                :fieldNames="{value:'name', label: 'name'}"
                @blur="handleVersionBlur"
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
            <span>{{ t('common.unplanned') }}</span>
            <Colon class="w-1" />
          </div>
          <div>
            {{ props.dataSource?.unplanned ? t('status.yes') : t('status.no') }}
          </div>
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
