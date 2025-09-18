<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref } from 'vue';
import { Button, TreeSelect } from 'ant-design-vue';
import {
  AsyncComponent, Colon, Icon, IconTask, Input, ScriptTypeTag, Select
} from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import TaskStatus from '@/components/TaskStatus/index.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/types';

// Async components
const Description = defineAsyncComponent(() => import('@/views/task/task/list/kanban/detail/Description.vue'));

// Component props and emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
  (event: 'refresh'): void;
}>();

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
const priorityInputValue = ref<TaskInfo['priority']['value']>();

// Tag editing state
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{id:string;name:string;}[]>([]);
const selectedTagIdList = ref<string[]>([]);

// Module editing state
const moduleTreeSelectRef = ref();
const isModuleEditing = ref(false);
const moduleInputValue = ref<string>();

// Version editing state
const versionSelectRef = ref();
const isVersionEditing = ref(false);
const versionInputValue = ref<string>();

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
const isOverdue = computed(() => props.dataSource?.overdue);
const totalTestCount = computed(() => +(props.dataSource?.totalNum || 0));
const failedTestCount = computed(() => +(props.dataSource?.failNum || 0));
const onePassStatusText = computed(() => {
  if (totalTestCount.value <= 0) {
    return '--';
  }
  return failedTestCount.value === 0 ? t('status.yes') : t('status.no');
});

/**
 * Enter module editing mode and focus the tree select
 */
const enterModuleEditMode = () => {
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
 * Confirm module selection and update task module
 */
const confirmModuleSelection = async () => {
  const selectedValue = moduleInputValue.value;
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

  emit('refresh');
  const taskInfo = await loadTaskInfoById(currentTaskId.value);
  emit('change', taskInfo);
};

/**
 * Cancel module editing mode
 */
const cancelModuleEdit = () => {
  isModuleEditing.value = false;
};

// Module tree data
const moduleTreeData = ref([]);

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
const loadTaskInfoById = async (id: string): Promise<Partial<TaskInfo>> => {
  emit('loadingChange', true);
  const [error, res] = await task.getTaskDetail(id);
  emit('loadingChange', false);
  if (error || !res?.data) {
    return { id };
  }

  return res.data;
};

// Sprint editing state
const sprintSelectRef = ref();
const isSprintEditing = ref(false);
const sprintDisplayMessage = ref<string>();
const sprintInputValue = ref<string>();

/**
 * Enter sprint editing mode and focus the select
 */
const enterSprintEditMode = () => {
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
 * Handle sprint selection change and update message
 * @param _value - Selected value
 * @param option - Selected option with message and value
 */
const handleSprintSelectionChange = async (_value: any, option: any) => {
  if (option && option.message) {
    sprintDisplayMessage.value = option.message;
  }
};

/**
 * Handle sprint selection blur and move task to selected sprint
 */
const handleSprintSelectionBlur = async () => {
  const selectedValue = sprintInputValue.value;
  if (!selectedValue || selectedValue === currentSprintId.value) {
    isSprintEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const moveParams = {
    taskIds: [currentTaskId.value],
    targetSprintId: selectedValue
  };
  const [error] = await task.moveTask(moveParams);
  emit('loadingChange', false);
  isSprintEditing.value = false;
  if (error) {
    return;
  }

  emit('refresh');
  const taskInfo = await loadTaskInfoById(currentTaskId.value);
  emit('change', taskInfo);
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
  const value = (event.target as HTMLInputElement).value;
  if (!value || value === currentTaskName.value) {
    isTaskNameEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskName(currentTaskId.value, value);
  emit('loadingChange', false);
  isTaskNameEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, name: value });
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
  const value = (event.target as HTMLInputElement).value;
  if (value === currentActualWorkload.value) {
    isActualWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editActualWorkload(currentTaskId.value, { workload: value });
  emit('loadingChange', false);
  isActualWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, actualWorkload: value });
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
  const value = (event.target as HTMLInputElement).value;
  if (value === currentEvalWorkload.value) {
    isEvalWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editEvalWorkloadApi(currentTaskId.value, { workload: value });
  emit('loadingChange', false);
  isEvalWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, evalWorkload: value });
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
 * Enter priority editing mode and focus the select
 */
const enterPriorityEditMode = () => {
  priorityInputValue.value = currentPriority.value;
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
 * @param _value - Selected value
 * @param option - Selected option with message and value
 */
const handlePrioritySelectionChange = async (_value: any, option: any) => {
  if (option && option.message) {
    priorityDisplayMessage.value = option.message;
  }
};

/**
 * Handle priority selection blur and update task priority
 */
const handlePrioritySelectionBlur = async () => {
  const selectedValue = priorityInputValue.value;
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

  emit('change', { id: currentTaskId.value, priority: { value: selectedValue, message: priorityDisplayMessage.value! } });
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
 * @param _value - Selected values
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
 * Handle version selection change
 * @param value - Selected version value
 */
const handleVersionSelectionChange = (value: any) => {
  versionInputValue.value = value;
};

/**
 * Handle version selection blur and update task software version
 */
const handleVersionSelectionBlur = async () => {
  const selectedValue = versionInputValue.value;
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

  emit('change', { id: currentTaskId.value, softwareVersion: versionInputValue.value });
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
const handleTaskInfoChange = (data: Partial<TaskInfo>) => {
  emit('change', data);
};

/**
 * Initialize component and load module tree data
 */
onMounted(() => {
  loadModuleTreeData();
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div>
      <div class="text-theme-title mb-2.5 font-semibold">{{ t('task.detailInfo.scenario.title') }}</div>

      <div class="space-y-2.5">
        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.code') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.code }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.name') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isTaskNameEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ currentTaskName }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="enterTaskNameEditMode">
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
              :placeholder="t('task.detailInfo.scenario.columns.namePlaceholder')"
              @blur="handleTaskNameInputBlur"
              @pressEnter="handleTaskNameInputEnter" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.type') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <IconTask :value="currentTaskType" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ props.dataSource?.taskType?.message }}</span>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.testType') }}</span>
            <Colon class="w-1" />
          </div>

          <ScriptTypeTag :value="props.dataSource?.testType" />
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.status') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <TaskStatus :value="currentTaskStatus" />
            <span
              v-if="isOverdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu scale-90">{{ t('task.detailInfo.scenario.columns.overdue') }}</span>
            </span>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.sprint') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isSprintEditing" class="flex items-center">
            <span class="ml-1.5">{{ props.dataSource?.sprintName }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="enterSprintEditMode">
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
              :placeholder="t('task.detailInfo.scenario.columns.selectSprint')"
              class="edit-container"
              @change="handleSprintSelectionChange"
              @blur="handleSprintSelectionBlur" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.module') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isModuleEditing" class="flex items-center">
            <span class="ml-1.5">{{ props.dataSource?.moduleName }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="enterModuleEditMode">
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
                :placeholder="t('task.detailInfo.scenario.columns.selectModule')">
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
                @click="confirmModuleSelection" />
              <Icon
                icon="icon-shanchuguanbi"
                class="text-3.5 cursor-pointer text-theme-text-hover"
                @click="cancelModuleEdit" />
            </div>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.parentTask') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-if="!props.dataSource?.parentTaskId" class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.parentTaskName || '--' }}
          </div>

          <RouterLink
            v-else
            target="_self"
            :to="`/task#task?projectId=${props.projectId}&taskId=${props.dataSource?.parentTaskId}&total=1`"
            style="color:#40a9ff"
            class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.parentTaskName || '--' }}
          </RouterLink>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.scenario') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.targetName }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.priority') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isPriorityEditing" class="flex items-center">
            <TaskPriority :value="props.dataSource?.priority" />
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="enterPriorityEditMode">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isPriorityEditing">
            <SelectEnum
              v-show="isPriorityEditing"
              ref="prioritySelectRef"
              v-model:value="priorityInputValue"
              enumKey="Priority"
              :placeholder="t('task.detailInfo.scenario.columns.selectPriority')"
              class="edit-container max-w-52"
              @change="handlePrioritySelectionChange"
              @blur="handlePrioritySelectionBlur">
              <template #option="record">
                <TaskPriority :value="record as any" />
              </template>
            </SelectEnum>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.evalWorkloadMethod') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.evalWorkloadMethod?.message }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ currentEvalWorkloadMethod === 'STORY_POINT' ? t('task.detailInfo.scenario.columns.evalWorkload') : t('task.detailInfo.scenario.columns.evalWorkload') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isEvalWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ currentEvalWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="enterEvalWorkloadEditMode">
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
              :placeholder="t('task.detailInfo.scenario.columns.evalWorkloadPlaceholder')"
              @blur="handleEvalWorkloadInputBlur"
              @pressEnter="handleEvalWorkloadInputEnter" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ currentEvalWorkloadMethod === 'STORY_POINT' ? t('task.detailInfo.scenario.columns.actualWorkload') : t('task.detailInfo.scenario.columns.actualWorkload') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isActualWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ currentActualWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="enterActualWorkloadEditMode">
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
              :placeholder="t('task.detailInfo.scenario.columns.actualWorkloadPlaceholder')"
              @blur="handleActualWorkloadInputBlur"
              @pressEnter="handleActualWorkloadInputEnter" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.totalNum') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ totalTestCount }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.failNum') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ failedTestCount }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.tags') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isTagEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div v-if="currentTags.length" class="flex items-center flex-wrap transform-gpu -translate-y-0.25">
              <div
                v-for="item in currentTags"
                :key="item.id"
                class="px-2 h-5.5 leading-5 mr-2 mb-2 rounded border border-solid border-border-divider bg-gray-light text-theme-sub-content">
                {{ item.name }}
              </div>
            </div>
            <div v-else>--</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="enterTagEditMode">
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
              :placeholder="t('task.detailInfo.scenario.columns.tagsPlaceholder')"
              mode="multiple"
              class="edit-container"
              :notFoundContent="t('task.detailInfo.scenario.columns.tagsNotFound')"
              @change="handleTagSelectionChange"
              @blur="handleTagSelectionBlur" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.onePass') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ onePassStatusText }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.softwareVersion') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="flex-1 min-w-0">
            <template v-if="isVersionEditing">
              <Select
                ref="versionSelectRef"
                v-model:value="versionInputValue"
                allowClear
                :placeholder="t('task.detailInfo.scenario.columns.softwareVersionPlaceholder')"
                class="w-full"
                lazy
                :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                :params="{filters: [{value: ['NOT_RELEASED', 'RELEASED'], key: 'status', op: 'IN'}]}"
                :fieldNames="{value:'name', label: 'name'}"
                @blur="handleVersionSelectionBlur"
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
                  @click="enterVersionEditMode">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>
            </template>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.scenario.columns.unplanned') }}</span>
            <Colon class="w-1" />
          </div>
          <div>
            {{ props.dataSource?.unplanned ? t('task.detailInfo.scenario.columns.yes') : t('task.detailInfo.scenario.columns.no') }}
          </div>
        </div>
      </div>
    </div>

    <Description
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :dataSource="props.dataSource"
      @change="handleTaskInfoChange"
      @loadingChange="handleLoadingChange" />
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
