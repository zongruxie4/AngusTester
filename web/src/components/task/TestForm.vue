<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { DatePicker, Select, SelectEnum } from '@xcan-angus/vue-ui';
import { CombinedTargetType, Priority } from '@xcan-angus/infra';

import TaskPriority from '@/components/TaskPriority/index.vue';
import { FormData } from './types';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  value: FormData;
  type: CombinedTargetType.API | CombinedTargetType.PROJECT | CombinedTargetType.SERVICE | CombinedTargetType.SCENARIO;
  users: {fullName: string; id: string}[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  users: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:value', value: FormData): void
}>();

// ===== Reactive Data and State =====
const formData = ref<FormData>({
  deadlineDate: '',
  priority: Priority.MEDIUM,
  assigneeId: undefined
});

// Validation error states for each field
const isAssigneeFieldEmpty = ref(false);
const isDeadlineFieldEmpty = ref(false);
const isPriorityFieldEmpty = ref(false);

// ===== Form Update Methods =====
/**
 * <p>
 * Updates the form data and emits the change event.
 * <p>
 * Merges the provided values with the current form data and emits the update event.
 * @param updatedValues - Object containing the fields to update
 */
const updateFormData = (updatedValues: { [key: string]: string | string[] }) => {
  emit('update:value', { ...formData.value, ...updatedValues });
};

// ===== Field Change Handlers =====
/**
 * <p>
 * Handles assignee field changes.
 * <p>
 * Clears the validation error and updates the assignee ID.
 * @param assigneeId - The selected assignee ID
 */
const handleAssigneeChange = (assigneeId: string | undefined) => {
  isAssigneeFieldEmpty.value = false;
  updateFormData({ assigneeId: assigneeId as string });
};

/**
 * <p>
 * Handles deadline date field changes.
 * <p>
 * Clears the validation error and updates the deadline date.
 * @param deadlineDate - The selected deadline date
 */
const handleDeadlineChange = (deadlineDate) => {
  isDeadlineFieldEmpty.value = false;
  updateFormData({ deadlineDate });
};

/**
 * <p>
 * Handles priority field changes.
 * <p>
 * Clears the validation error and updates the priority value.
 * @param priority - The selected priority value
 */
const handlePriorityChange = (priority: Priority) => {
  isPriorityFieldEmpty.value = false;
  updateFormData({ priority });
};

// ===== Validation Methods =====
/**
 * <p>
 * Validates all form fields.
 * <p>
 * Checks each required field and sets error states for empty fields.
 * @returns True if all validations pass, false otherwise
 */
const validateForm = () => {
  let errorCount = 0;

  if (!formData.value.assigneeId) {
    isAssigneeFieldEmpty.value = true;
    errorCount++;
  }

  if (!formData.value.deadlineDate) {
    isDeadlineFieldEmpty.value = true;
    errorCount++;
  }

  if (!formData.value.priority) {
    isPriorityFieldEmpty.value = true;
    errorCount++;
  }

  return errorCount === 0;
};

/**
 * <p>
 * Resets the form to its initial state.
 * <p>
 * Clears all validation errors and resets form data to default values.
 */
const resetForm = () => {
  isAssigneeFieldEmpty.value = false;
  isDeadlineFieldEmpty.value = false;
  isPriorityFieldEmpty.value = false;
  formData.value = props.value || {
    deadlineDate: '',
    priority: Priority.MEDIUM,
    assigneeId: undefined
  };
};

// ===== Lifecycle Hooks =====
// Watch for prop value changes and update form data
watch(() => props.value, (newValue) => {
  formData.value = newValue;
}, { deep: true, immediate: true });

// Expose validation and reset methods for parent component
defineExpose({ validate: validateForm, reset: resetForm });
</script>

<template>
  <div class="space-y-5">
    <div class="text-3 text-theme-content">
      <div class="mb-1">{{ t('commonComp.createTaskTestModal.assignee') }}</div>
      <Select
        showSearch
        :error="isAssigneeFieldEmpty"
        :value="formData.assigneeId"
        class="w-full"
        :placeholder="t('commonComp.createTaskTestModal.selectAssignee')"
        :allowClear="true"
        :options="props.users"
        :fieldNames="{ label: 'fullName', value: 'id' }"
        size="small"
        @change="(value) => handleAssigneeChange(value)" />
    </div>

    <div class="text-3 text-theme-content">
      <div class="mb-1">{{ t('commonComp.createTaskTestModal.deadlineDate') }}</div>
      <DatePicker
        :error="isDeadlineFieldEmpty"
        class="w-full"
        :value="formData.deadlineDate"
        :allowClear="true"
        size="small"
        showTime
        @change="handleDeadlineChange" />
    </div>

    <div class="text-3 text-theme-content">
      <div class="mb-1">{{ t('common.priority') }}</div>
      <SelectEnum
        class="w-full"
        enumKey="Priority"
        :error="isPriorityFieldEmpty"
        :value="formData.priority"
        :allowClear="false"
        size="small"
        @change="handlePriorityChange">
        <template #option="record">
          <TaskPriority :value="record" />
        </template>
      </SelectEnum>
    </div>
  </div>
</template>
