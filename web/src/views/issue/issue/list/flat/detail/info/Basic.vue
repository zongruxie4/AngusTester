<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Icon, IconTask, Input, Select, Toggle } from '@xcan-angus/vue-ui';
import { enumUtils, TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { issue } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BugLevel, SoftwareVersionStatus, TaskType } from '@/enums/enums';
import { TaskDetail } from '@/views/issue/types';
import { TaskDetailProps } from '@/views/issue/issue/list/types';
import { IssueMenuKey } from '@/views/issue/menu';

import TaskStatus from '@/components/task/TaskStatus/index.vue';
import TaskPriority from '@/components/task/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';

// Component props and emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  loading: false
});

const { t } = useI18n();

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
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
const taskTypeSelectValue = ref<TaskDetail['taskType']['value']>();

// Priority editing state
const prioritySelectRef = ref();
const isPriorityEditing = ref(false);
const prioritySelectMessage = ref<string>();
const prioritySelectValue = ref<TaskDetail['priority']['value']>();

// Tag editing state
const tagSelectRef = ref();
const isTagEditing = ref(false);
const selectedTagList = ref<{id: number; name: string;}[]>([]);
const selectedTagIdList = ref<number[]>([]);

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
const isTaskOverdue = computed(() => props.dataSource?.overdue);

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
  const [error] = await issue.editTaskName(currentTaskId.value, newValue);
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
  const [error] = await issue.editTaskTaskType(currentTaskId.value, newValue);
  emit('loadingChange', false);
  if (newValue === TaskType.BUG) {
    await issue.updateTask(currentTaskId.value, {
      bugLevel: BugLevel.MINOR,
      escapedBug: false
    });
  }
  isTaskTypeEditing.value = false;
  if (error) {
    return;
  }

  emit('change', {
    id: currentTaskId.value,
    bugLevel: { value: BugLevel.MINOR, message: enumUtils.getEnumDescription(BugLevel, BugLevel.MINOR) },
    escapedBug: false,
    taskType: { value: newValue, message: taskTypeSelectMessage.value! }
  });
};

const taskTypeExcludes = (value: { message: string; value: string }) => {
  const taskTypeValue = value.value as TaskDetail['taskType']['value'];
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
  const [error] = await issue.editTaskPriority(currentTaskId.value, newValue);
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
  const [error] = await issue.editTaskTags(currentTaskId.value, { tagIds: newTagIds });
  emit('loadingChange', false);
  isTagEditing.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, tags: selectedTagList.value });
};

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
  const [error] = await issue.updateTask(currentTaskId.value, { softwareVersion: newValue || '' });
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
      <div class="text-3.5">{{ t('common.basicInfo') }}</div>
    </template>

    <template #default>
      <div class="basic-info-container">
        <!-- Primary Information Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.code') }}</span>
            </div>
            <div class="info-value">
              <div class="flex items-center">
                <span class="info-text">{{ props.dataSource?.code }}</span>
                <span
                  v-if="isTaskOverdue"
                  class="overdue-badge">
                  {{ t('status.overdue') }}
                </span>
              </div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.status') }}</span>
            </div>
            <div class="info-value">
              <TaskStatus :value="currentTaskStatus" />
            </div>
          </div>
        </div>

        <!-- Task Name and Sprint Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.name') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isTaskNameEditing" class="info-value-content">
                <span class="info-text">{{ currentTaskName }}</span>
                <Button
                  type="link"
                  class="edit-btn"
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
                  class="edit-input"
                  :placeholder="t('common.placeholders.searchKeyword')"
                  @blur="handleTaskNameBlur"
                  @pressEnter="handleTaskNameEnter" />
              </AsyncComponent>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.sprint') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ props.dataSource?.sprintName || '--' }}</span>
            </div>
          </div>
        </div>

        <!-- Module and Parent Task Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.module') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ props.dataSource?.moduleName || '--' }}</span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.parentIssue') }}</span>
            </div>
            <div class="info-value">
              <div v-if="!props.dataSource?.parentTaskId" class="info-text">
                {{ props.dataSource?.parentTaskName || '--' }}
              </div>
              <RouterLink
                v-else
                target="_self"
                :to="`/issue#${IssueMenuKey.ISSUE}?projectId=${props.projectId}&taskId=${props.dataSource?.parentTaskId}&total=1`"
                class="info-link">
                {{ props.dataSource?.parentTaskName || '--' }}
              </RouterLink>
            </div>
          </div>
        </div>

        <!-- Task Type and Software Version Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.type') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isTaskTypeEditing" class="info-value-content">
                <div class="flex items-center">
                  <IconTask :value="currentTaskType" class="flex-shrink-0" />
                  <span class="ml-1.5 task-type-text">{{ props.dataSource?.taskType?.message }}</span>
                  <template v-if="currentTaskType === TaskType.BUG">
                    <Tag
                      v-if="props.dataSource?.bugLevel"
                      color="error"
                      class="ml-2 text-3 leading-4">
                      {{ props.dataSource?.bugLevel?.message }}
                    </Tag>
                    <Tag
                      v-if="props.dataSource?.escapedBug"
                      color="error"
                      class="ml-2 text-3 leading-4">
                      {{ t('common.escapedBug') }}
                    </Tag>
                  </template>
                </div>
                <Button
                  type="link"
                  class="edit-btn"
                  @click="startTaskTypeEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
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
                  :placeholder="t('common.placeholders.selectIssueType')"
                  class="edit-select"
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
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.softwareVersion') }}</span>
            </div>
            <div class="info-value">
              <template v-if="isVersionEditing">
                <Select
                  ref="versionSelectRef"
                  v-model:value="versionSelectValue"
                  allowClear
                  :placeholder="t('common.placeholders.selectSoftwareVersion')"
                  lazy
                  class="edit-select"
                  :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                  :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
                  :fieldNames="{value:'name', label: 'name'}"
                  @blur="handleVersionBlur"
                  @change="handleVersionChange">
                </Select>
              </template>
              <template v-else>
                <div class="info-value-content">
                  <RouterLink
                    v-if="props.dataSource?.softwareVersion"
                    class="info-link"
                    :to="`/task#version?name=${props.dataSource?.softwareVersion}`">
                    {{ props.dataSource?.softwareVersion }}
                  </RouterLink>
                  <span v-else class="info-text">--</span>
                  <Button
                    type="link"
                    class="edit-btn"
                    @click="startVersionEditing">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                  </Button>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- Priority and Unplanned Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.priority') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isPriorityEditing" class="info-value-content">
                <TaskPriority :value="props.dataSource?.priority as any" />
                <Button
                  type="link"
                  class="edit-btn"
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
                  :placeholder="t('common.placeholders.selectPriority')"
                  class="edit-select"
                  @change="handlePriorityChange as any"
                  @blur="handlePriorityBlur as any">
                  <template #option="record">
                    <TaskPriority :value="record as any" />
                  </template>
                </SelectEnum>
              </AsyncComponent>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.unplanned') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">
                {{ props.dataSource?.unplanned ? t('status.yes') : t('status.no') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Tags Row -->
        <div class="info-row">
          <div class="info-item info-item-full">
            <div class="info-label">
              <span>{{ t('common.tag') }}</span>
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
                <span v-else class="info-text">--</span>
                <Button
                  type="link"
                  class="edit-btn"
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
                  :placeholder="t('common.placeholders.selectTag')"
                  mode="multiple"
                  class="edit-select"
                  :notFoundContent="t('backlog.edit.messages.contactAdminForTags')"
                  @change="handleTagChange"
                  @blur="handleTagBlur" />
              </AsyncComponent>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
/* Main Container */
.basic-info-container {
  padding: 1rem 1.375rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

/* Info Row Layout */
.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
  align-items: start;
}

.info-item-full {
  grid-column: 1 / -1;
}

/* Individual Info Item */
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.125rem;
  min-height: 2rem;
}

/* Label Styling */
.info-label {
  flex-shrink: 0;
  width: 5rem;
  display: flex;
  align-items: center;
  min-height: 1.5rem;
}

.info-label span {
  font-size: 0.75rem;
  font-weight: 400;
  color: #7c8087;
  line-height: 1.2;
  word-break: break-word;
  white-space: normal;
}

/* Value Styling */
.info-value {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  min-height: 1.5rem;
}

.info-text {
  font-size: 0.75rem;
  font-weight: 400;
  color: #374151;
  line-height: 1.4;
  word-break: break-word;
  white-space: normal;
}

.info-link {
  font-size: 0.75rem;
  font-weight: 400;
  color: #3b82f6;
  text-decoration: none;
  line-height: 1.4;
  word-break: break-word;
  white-space: normal;
  transition: color 0.2s ease;
}

.info-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* Value Content Container */
.info-value-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
}

/* Edit Button */
.edit-btn {
  padding: 0.125rem;
  height: 1.25rem;
  width: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.25rem;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.edit-btn:hover {
  background-color: #f3f4f6;
}

/* Edit Input and Select */
.edit-input,
.edit-select {
  width: 100%;
  max-width: 20rem;
  font-size: 0.75rem;
}

/* Tags Container */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.tag-item {
  padding: 0.25rem 0.5rem;
  height: 1.375rem;
  line-height: 1.375rem;
  border-radius: 0.25rem;
  border: 1px solid #dbeafe;
  background-color: #eff6ff;
  color: #6d7ebc;
  font-size: 0.75rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Overdue Badge */
.overdue-badge {
  flex-shrink: 0;
  border: 1px solid #ef4444;
  border-radius: 0.25rem;
  padding: 0.125rem 0.25rem;
  margin-left: 0.5rem;
  color: #ef4444;
  font-size: 0.75rem;
  line-height: 1;
  font-weight: 500;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .info-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }

  .info-item-full {
    grid-column: 1;
  }
}

@media (max-width: 768px) {
  .basic-info-container {
    padding: 0.75rem 1rem;
    gap: 0.25rem;
  }

  .info-row {
    gap: 0.5rem;
  }

  .info-item {
    gap: 0.125rem;
  }

  .info-label {
    width: 5rem;
  }

  .edit-input,
  .edit-select {
    max-width: 100%;
  }
}

@media (max-width: 640px) {
  .basic-info-container {
    padding: 0.5rem 0.75rem;
    gap: 0.125rem;
  }

  .info-label {
    width: 4rem;
  }

  .info-item {
    gap: 0.0625rem;
  }

  .info-label span {
    font-size: 0.6875rem;
  }

  .info-text,
  .info-link {
    font-size: 0.6875rem;
  }
}

/* Task Type Text Styling */
.task-type-text {
  font-size: 0.75rem;
  font-weight: 400;
  color: #374151;
}

/* Animation for smooth transitions */
.info-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Legacy support for old positioning */
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
</style>
