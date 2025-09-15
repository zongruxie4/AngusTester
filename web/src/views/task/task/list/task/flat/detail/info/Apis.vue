<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import {
  AsyncComponent, Colon, Icon, IconTask, Input, ScriptTypeTag, Select, Toggle
} from '@xcan-angus/vue-ui';
import { EvalWorkloadMethod, TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { task } from '@/api/tester';
import { SoftwareVersionStatus } from '@/enums/enums';

import TaskStatus from '@/components/TaskStatus/index.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

// Component props and emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  loading: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
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
const prioritySelectMessage = ref<string>();
const prioritySelectValue = ref<TaskInfo['priority']['value']>();

// Tag editing state
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{ id: string; name: string; }[]>([]);
const selectedTagIdList = ref<string[]>([]);

// Software version editing state
const versionSelectRef = ref();
const isVersionEditing = ref(false);
const versionSelectValue = ref<string>();

// Task name editing methods
/**
 * <p>Initiates task name editing mode by setting the input value and enabling edit flag.</p>
 * <p>Focuses the input field after a short delay to ensure proper rendering.</p>
 */
const startTaskNameEditing = () => {
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
 * <p>Handles task name input blur event to save changes or cancel editing.</p>
 * <p>Validates input value and calls API to update task name if value has changed.</p>
 * @param event - Input blur event containing the new value
 */
const handleTaskNameBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newValue = target?.value;
  if (!newValue || newValue === currentTaskName.value) {
    isTaskNameEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskName(currentTaskId.value, newValue);
  emit('loadingChange', false);
  isTaskNameEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, name: newValue });
};

/**
 * <p>Handles Enter key press on task name input to trigger blur event.</p>
 */
const handleTaskNameEnter = () => {
  if (typeof taskNameInputRef.value?.blur === 'function') {
    taskNameInputRef.value.blur();
  }
};

// Actual workload editing methods
/**
 * <p>Initiates actual workload editing mode by setting the input value and enabling edit flag.</p>
 * <p>Focuses the input field after a short delay to ensure proper rendering.</p>
 */
const startActualWorkloadEditing = () => {
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
 * <p>Handles actual workload input blur event to save changes or cancel editing.</p>
 * <p>Validates input value and calls API to update actual workload if value has changed.</p>
 * @param event - Input blur event containing the new value
 */
const handleActualWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newValue = target?.value;
  if (newValue === currentActualWorkload.value) {
    isActualWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editActualWorkload(currentTaskId.value, { workload: newValue });
  emit('loadingChange', false);
  isActualWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, actualWorkload: newValue });
};

/**
 * <p>Handles Enter key press on actual workload input to trigger blur event.</p>
 */
const handleActualWorkloadEnter = () => {
  if (typeof actualWorkloadInputRef.value?.blur === 'function') {
    actualWorkloadInputRef.value.blur();
  }
};

// Evaluation workload editing methods
/**
 * <p>Initiates evaluation workload editing mode by setting the input value and enabling edit flag.</p>
 * <p>Focuses the input field after a short delay to ensure proper rendering.</p>
 */
const startEvalWorkloadEditing = () => {
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
 * <p>Handles evaluation workload input blur event to save changes or cancel editing.</p>
 * <p>Validates input value and calls API to update evaluation workload if value has changed.</p>
 * @param event - Input blur event containing the new value
 */
const handleEvalWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newValue = target?.value;
  if (newValue === currentEvalWorkload.value) {
    isEvalWorkloadEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editEvalWorkloadApi(currentTaskId.value, { workload: newValue });
  emit('loadingChange', false);
  isEvalWorkloadEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, evalWorkload: newValue });
};

/**
 * <p>Handles Enter key press on evaluation workload input to trigger blur event.</p>
 */
const handleEvalWorkloadEnter = () => {
  if (typeof evalWorkloadInputRef.value?.blur === 'function') {
    evalWorkloadInputRef.value.blur();
  }
};

// Priority editing methods
/**
 * <p>Initiates priority editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startPriorityEditing = () => {
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
 * <p>Handles priority selection change to update the message display.</p>
 * @param value - Selected priority value
 * @param option - Selected priority option containing value and message
 */
const handlePriorityChange = async (
  value: string,
  option?: any) => {
  if (option?.message) {
    prioritySelectMessage.value = option.message;
  }
};

/**
 * <p>Handles priority select blur event to save changes or cancel editing.</p>
 * <p>Validates selected value and calls API to update task priority if value has changed.</p>
 */
const handlePriorityBlur = async () => {
  const newValue = prioritySelectValue.value;
  if (!newValue || newValue === currentPriority.value) {
    isPriorityEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskPriority(currentTaskId.value, newValue);
  emit('loadingChange', false);
  isPriorityEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, priority: { value: newValue, message: prioritySelectMessage.value! } });
};

// Tag editing methods
/**
 * <p>Initiates tag editing mode by setting the selected tag IDs and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startTagEditing = () => {
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
 * <p>Handles tag selection change to update the selected tag list.</p>
 * @param value - Selected tag IDs array
 * @param options - Array of selected tag options containing id and name
 */
const handleTagChange = async (
  value: any,
  options: any) => {
  selectedTagList.value = options;
};

/**
 * <p>Handles tag select blur event to save changes or cancel editing.</p>
 * <p>Compares selected tags with current tags and calls API to update if changed.</p>
 */
const handleTagBlur = async () => {
  const newTagIds = selectedTagIdList.value;
  if (isEqual(newTagIds, currentTagIds.value)) {
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

// Software version editing methods
/**
 * <p>Initiates software version editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startVersionEditing = () => {
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
 * <p>Handles software version selection change to update the selected value.</p>
 * @param value - Selected software version value
 * @param option - Selected option (unused)
 */
const handleVersionChange = (value: any, option?: any) => {
  versionSelectValue.value = value;
};

/**
 * <p>Handles software version select blur event to save changes or cancel editing.</p>
 * <p>Validates selected value and calls API to update software version if value has changed.</p>
 */
const handleVersionBlur = async () => {
  const newValue = versionSelectValue.value;
  if (newValue === props.dataSource?.softwareVersion) {
    isVersionEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.updateTask(currentTaskId.value, { softwareVersion: newValue || '' });
  emit('loadingChange', false);
  isVersionEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, softwareVersion: versionSelectValue.value });
};

// Computed properties for task data
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
const totalTestCount = computed(() => +(props.dataSource?.totalNum || 0));
const failedTestCount = computed(() => +(props.dataSource?.failNum || 0));

/**
 * <p>Computes the one-pass status text based on test results.</p>
 * <p>Returns '--' if no tests have been run, 'Yes' if all tests passed, 'No' if any tests failed.</p>
 */
const onePassStatusText = computed(() => {
  if (totalTestCount.value <= 0) {
    return '--';
  }

  return failedTestCount.value === 0 ? t('task.detailInfo.apis.columns.yes') : t('task.detailInfo.apis.columns.no');
});
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3">{{ t('task.detailInfo.apis.basicInfo') }}</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.code') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.code }}</div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.testType') }}</span>
              <Colon class="w-1" />
            </div>

            <ScriptTypeTag :value="props.dataSource?.testType" />
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.name') }}</span>
              <Colon class="w-1" />
            </div>

            <div v-show="!isTaskNameEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
              <div>{{ currentTaskName }}</div>
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
                class="left-component"
                :placeholder="t('task.detailInfo.apis.columns.namePlaceholder')"
                @blur="handleTaskNameBlur"
                @pressEnter="handleTaskNameEnter" />
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.status') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <TaskStatus :value="currentTaskStatus" />
              <span
                v-if="isTaskOverdue"
                class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
                style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                <span class="inline-block transform-gpu scale-90">{{ t('task.detailInfo.apis.columns.overdue') }}</span>
              </span>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.type') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <IconTask :value="currentTaskType" class="text-4 flex-shrink-0" />
              <span class="ml-1.5">{{ props.dataSource?.taskType?.message }}</span>
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.sprint') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.sprintName }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.module') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.moduleName || '--' }}
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.service') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.targetParentName }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.parentTask') }}</span>
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

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.evalWorkloadMethod') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.evalWorkloadMethod?.message }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.api') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.targetName }}</div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>
                {{ currentEvalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                  ? t('task.detailInfo.apis.columns.evalWorkload')
                  : t('task.detailInfo.apis.columns.evalWorkHours') }}
              </span>
              <Colon class="w-1" />
            </div>

            <div v-show="!isEvalWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
              <div>{{ currentEvalWorkload || '--' }}</div>
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
                :placeholder="t('task.detailInfo.apis.columns.evalWorkloadPlaceholder')"
                @blur="handleEvalWorkloadBlur"
                @pressEnter="handleEvalWorkloadEnter" />
            </AsyncComponent>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.priority') }}</span>
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
                v-model:value="prioritySelectValue"
                :allowClear="false"
                internal
                enumKey="Priority"
                placeholder="t('task.detailInfo.apis.columns.selectPriority')"
                class="left-component max-w-52"
                @change="handlePriorityChange"
                @blur="handlePriorityBlur">
                <template #option="record">
                  <TaskPriority :value="{ value: record.value as any, message: record.label }" />
                </template>
              </SelectEnum>
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.onePass') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ onePassStatusText }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>
                {{ currentEvalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                  ? t('task.detailInfo.apis.columns.actualStoryPoint')
                  : t('task.detailInfo.apis.columns.actualWorkload') }}
              </span>
              <Colon class="w-1" />
            </div>

            <div v-show="!isActualWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
              <div>{{ currentActualWorkload || '--' }}</div>
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
                :placeholder="t('task.detailInfo.apis.columns.actualWorkloadPlaceholder')"
                @blur="handleActualWorkloadBlur"
                @pressEnter="handleActualWorkloadEnter" />
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.failNum') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ failedTestCount }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.softwareVersion') }}</span>
              <Colon class="w-1" />
            </div>
            <div class="flex-1 min-w-0">
              <template v-if="isVersionEditing">
                <Select
                  ref="versionSelectRef"
                  v-model:value="versionSelectValue"
                  allowClear
                  :placeholder="t('task.detailInfo.apis.columns.softwareVersionPlaceholder')"
                  lazy
                  class="w-full max-w-60"
                  :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                  :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
                  :fieldNames="{value:'name', label: 'name'}"
                  @blur="handleVersionBlur"
                  @change="handleVersionChange">
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
          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.unplanned') }}</span>
              <Colon class="w-1" />
            </div>
            <div class="">
              {{ props.dataSource?.unplanned ? t('status.yes') : t('status.no') }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.tags') }}</span>
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
                :allowClear="false"
                :action="`${TESTER}/task/tag?projectId=${props.projectId}&fullTextSearch=true`"
                showSearch
                internal
                :placeholder="t('task.detailInfo.apis.columns.tagsPlaceholder')"
                mode="multiple"
                class="left-component"
                :notFoundContent="t('task.detailInfo.apis.columns.tagsNotFound')"
                @change="handleTagChange"
                @blur="handleTagBlur" />
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.apis.columns.totalNum') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ totalTestCount }}</div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.border-none {
  border: none;
}

.left-component {
  position: absolute;
  top: -4px;
  left: 62px;
  width: calc(100% - 62px);
}

.right-component {
  position: absolute;
  top: -4px;
  left: 98px;
  width: calc(100% - 98px);
}
</style>
