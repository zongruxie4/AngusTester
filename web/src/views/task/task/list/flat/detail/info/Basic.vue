<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, IconTask, Input, Select, Toggle } from '@xcan-angus/vue-ui';
import { enumUtils, TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BugLevel, SoftwareVersionStatus, TaskType } from '@/enums/enums';
import { TaskInfo } from '@/views/task/types';

import TaskStatus from '@/components/TaskStatus/index.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { TaskInfoProps } from '@/views/task/task/list/types';

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

// (moved to Workload.vue)

// Task type editing state
const taskTypeSelectRef = ref();
const isTaskTypeEditing = ref(false);
const taskTypeSelectMessage = ref<string>();
const taskTypeSelectValue = ref<TaskInfo['taskType']['value']>();

// Priority editing state
const prioritySelectRef = ref();
const isPriorityEditing = ref(false);
const prioritySelectMessage = ref<string>();
const prioritySelectValue = ref<TaskInfo['priority']['value']>();

// Tag editing state
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{id: string; name: string;}[]>([]);
const selectedTagIdList = ref<string[]>([]);

// Software version editing state
const versionSelectRef = ref();
const isVersionEditing = ref(false);
const versionSelectValue = ref<string>();

// Computed properties for task data
const currentTaskId = computed(() => props.dataSource?.id);
const currentTaskStatus = computed(() => props.dataSource?.status);
const currentTaskName = computed(() => props.dataSource?.name);
const currentTaskType = computed(() => props.dataSource?.taskType?.value);
const currentPriority = computed(() => props.dataSource?.priority?.value);
const currentTags = computed(() => props.dataSource?.tags || []);
const currentTagIds = computed(() => props.dataSource?.tags?.map(item => item.id) || []);
// (moved to Workload.vue)
const isTaskOverdue = computed(() => props.dataSource?.overdue);
// (moved to ProcessTimes.vue)

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

/**
 * <p>Initiates task type editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startTaskTypeEditing = () => {
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
 * <p>Handles task type selection change to update the message display.</p>
 * @param value - Selected task type value
 * @param option - Selected task type option containing value and message
 */
const handleTaskTypeChange = async (
  _value: any,
  option?: any) => {
  if (option?.label) {
    taskTypeSelectMessage.value = option.label;
  }
};

/**
 * <p>Handles task type select blur event to save changes or cancel editing.</p>
 * <p>Validates selected value and calls API to update task type if value has changed.</p>
 */
const handleTaskTypeBlur = async () => {
  const newValue = taskTypeSelectValue.value;
  if (!newValue || newValue === currentTaskType.value) {
    isTaskTypeEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskTaskType(currentTaskId.value, newValue);
  emit('loadingChange', false);
  if (newValue === TaskType.BUG) {
    await task.updateTask(currentTaskId.value, {
      bugLevel: BugLevel.MINOR,
      missingBug: false
    });
  }
  isTaskTypeEditing.value = false;
  if (error) {
    return;
  }

  emit('change', {
    id: currentTaskId.value,
    bugLevel: { value: BugLevel.MINOR, message: enumUtils.getEnumDescription(BugLevel, BugLevel.MINOR) },
    missingBug: false,
    taskType: { value: newValue, message: taskTypeSelectMessage.value! }
  });
};

const taskTypeExcludes = (value: { message: string; value: string }) => {
  const taskTypeValue = value.value as TaskInfo['taskType']['value'];
  const type = currentTaskType.value;
  if (currentTaskId.value) {
    if (type === TaskType.API_TEST) {
      return taskTypeValue !== TaskType.API_TEST;
    }
    if (type === TaskType.SCENARIO_TEST) {
      return taskTypeValue !== TaskType.SCENARIO_TEST;
    }
    return [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(taskTypeValue);
  }
  return false;
};

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
  _value: any,
  option?: any) => {
  if (option?.label) {
    prioritySelectMessage.value = option.label;
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

  emit('change', {
    id: currentTaskId.value,
    priority: { value: newValue, message: prioritySelectMessage.value! }
  });
};

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
  _value: any,
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
const handleVersionChange = (value: any, _option?: any) => {
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
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3.5">{{ t('task.detailInfo.basic.title') }}</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.code') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.code }}</div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.status') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <TaskStatus :value="currentTaskStatus" />
              <span
                v-if="isTaskOverdue"
                class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
                style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                <span class="inline-block transform-gpu scale-90">{{ t('task.detailInfo.basic.columns.overdue') }}</span>
              </span>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.name') }}</span>
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
                :placeholder="t('task.detailInfo.basic.columns.namePlaceholder')"
                @blur="handleTaskNameBlur"
                @pressEnter="handleTaskNameEnter" />
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.sprint') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.sprintName }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.module') }}</span>
            </div>

            <div class="font-medium whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.moduleName || '--' }}
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.parentTask') }}</span>
            </div>

            <div v-if="!props.dataSource?.parentTaskId" class="font-medium whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.parentTaskName || '--' }}
            </div>

            <RouterLink
              v-else
              target="_self"
              :to="`/task#task?projectId=${props.projectId}&taskId=${props.dataSource?.parentTaskId}&total=1`"
              style="color:#40a9ff"
              class="font-medium whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.parentTaskName || '--' }}
            </RouterLink>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.type') }}</span>
              <Colon class="w-1" />
            </div>

            <div v-show="!isTaskTypeEditing" class="flex items-center">
              <IconTask :value="currentTaskType" class="text-4 flex-shrink-0" />
              <span class="ml-1.5">{{ props.dataSource?.taskType?.message }}</span>
              <Button
                type="link"
                class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
                @click="startTaskTypeEditing">
                <Icon icon="icon-shuxie" class="text-3.5" />
              </Button>
              <template v-if="currentTaskType === TaskType.BUG">
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
                  {{ t('task.detailInfo.basic.columns.missingBug') }}
                </Tag>
              </template>
            </div>

            <AsyncComponent :visible="isTaskTypeEditing">
              <SelectEnum
                v-show="isTaskTypeEditing"
                ref="taskTypeSelectRef"
                v-model:value="taskTypeSelectValue"
                :allowClear="false"
                :excludes="taskTypeExcludes"
                internal
                enumKey="TaskType"
                :placeholder="t('task.detailInfo.basic.columns.selectTaskType')"
                class="left-component max-w-52"
                @change="handleTaskTypeChange"
                @blur="handleTaskTypeBlur">
                <template #option="record">
                  <div class="flex items-center">
                    <IconTask :value="record.value as any" class="text-4 flex-shrink-0" />
                    <span class="ml-2">{{ record.label }}</span>
                  </div>
                </template>
              </SelectEnum>
            </AsyncComponent>
          </div>

          <div class="flex items-start space-x-5">
            <div class="relative w-1/2 flex items-start">
              <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>{{ t('task.detailInfo.basic.columns.softwareVersion') }}</span>
                <Colon class="w-1" />
              </div>
              <div class="flex-1 min-w-0">
                <template v-if="isVersionEditing">
                  <Select
                    ref="versionSelectRef"
                    v-model:value="versionSelectValue"
                    allowClear
                    :placeholder="t('task.detailInfo.basic.columns.softwareVersionPlaceholder')"
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
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.priority') }}</span>
              <Colon class="w-1" />
            </div>

            <div v-show="!isPriorityEditing" class="flex items-center">
              <TaskPriority :value="props.dataSource?.priority as any" />
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
                :placeholder="t('task.detailInfo.basic.columns.selectPriority')"
                class="left-component max-w-52"
                @change="handlePriorityChange as any"
                @blur="handlePriorityBlur as any">
                <template #option="record">
                  <TaskPriority :value="record as any" />
                </template>
              </SelectEnum>
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.unplanned') }}</span>
              <Colon class="w-1" />
            </div>
            <div class="">
              {{ props.dataSource?.unplanned ? t('task.detailInfo.basic.columns.yes') : t('task.detailInfo.basic.columns.no') }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.detailInfo.basic.columns.tags') }}</span>
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
                :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
                showSearch
                internal
                :placeholder="t('task.detailInfo.basic.columns.tagsPlaceholder')"
                mode="multiple"
                class="left-component"
                :notFoundContent="t('task.detailInfo.basic.columns.tagsNotFound')"
                @change="handleTagChange"
                @blur="handleTagBlur" />
            </AsyncComponent>
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
  left: 50px;
  width: calc(100% - 50px);
}

.right-component {
  position: absolute;
  top: -4px;
  left: 98px;
  width: calc(100% - 98px);
}

/* Allow long label text to wrap and align to top */
.w-18\.5 { align-items: flex-start !important; }
.w-24\.5 { align-items: flex-start !important; }
.w-18\.5 span, .w-24\.5 span { white-space: normal; word-break: break-word; line-height: 1.2; }
</style>
