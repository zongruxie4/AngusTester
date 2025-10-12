<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, DatePicker, Icon, Toggle, Tooltip } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import dayjs, { Dayjs } from 'dayjs';
import { issue } from '@/api/tester';

import { TaskDetail } from '@/views/issue/types';
import { TIME_FORMAT } from '@/utils/constant';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

// Component props and emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
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
  (event: 'change', value: Partial<TaskDetail>): void;
}>();

// Deadline date editing state
const deadlineDatePickerRef = ref();
const isDeadlineEditing = ref(false);
const deadlineDateInputValue = ref<string>();

// Date validation state
const isDateValidationError = ref();
const dateValidationErrorMessage = ref<string>();

// Computed properties for task date data
const currentTaskId = computed(() => props.dataSource?.id);
const taskCreatedDate = computed(() => props.dataSource?.createdDate);
const currentDeadlineDate = computed(() => props.dataSource?.deadlineDate);
const taskStartDate = computed(() => props.dataSource?.startDate);
const taskProcessedDate = computed(() => props.dataSource?.processedDate);
const taskConfirmedDate = computed(() => props.dataSource?.confirmedDate);
const taskCompletedDate = computed(() => props.dataSource?.completedDate);
const taskCanceledDate = computed(() => props.dataSource?.canceledDate);
const taskExecDate = computed(() => props.dataSource?.execDate);
const taskLastModifiedDate = computed(() => props.dataSource?.lastModifiedDate);

/**
 * <p>Initiates deadline date editing mode by setting the input value and enabling edit flag.</p>
 * <p>Focuses the date picker field after a short delay to ensure proper rendering.</p>
 */
const startDeadlineDateEditing = () => {
  deadlineDateInputValue.value = currentDeadlineDate.value;
  isDeadlineEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof deadlineDatePickerRef.value?.focus === 'function') {
        deadlineDatePickerRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Handles deadline date selection change to validate the selected date.</p>
 * <p>Validates that a date is selected and that it's not in the past.</p>
 * @param value - Selected date string value
 */
const handleDeadlineDateChange = (value: string) => {
  if (!value) {
    dateValidationErrorMessage.value = t('common.placeholders.selectDeadline');
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    isDateValidationError.value = true;
    dateValidationErrorMessage.value = t('common.placeholders.futureTimeRequired');
    return;
  }

  isDateValidationError.value = false;
  dateValidationErrorMessage.value = undefined;
};

/**
 * <p>Handles deadline date picker blur event to save changes or cancel editing.</p>
 * <p>Validates the selected date and calls API to update deadline if value has changed.</p>
 */
const handleDeadlineDateBlur = async () => {
  if (isDateValidationError.value) {
    if (typeof deadlineDatePickerRef.value?.focus === 'function') {
      deadlineDatePickerRef.value?.focus();
    }

    return;
  }

  const newValue = deadlineDateInputValue.value;
  if (!newValue || newValue === currentDeadlineDate.value) {
    isDeadlineEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.editDeadlineDateApi(currentTaskId.value, newValue);
  emit('loadingChange', false);
  if (error) {
    if (typeof deadlineDatePickerRef.value?.focus === 'function') {
      deadlineDatePickerRef.value?.focus();
    }

    return;
  }

  isDeadlineEditing.value = false;
  emit('change', { id: currentTaskId.value, deadlineDate: newValue });
};

/**
 * <p>Determines if a date should be disabled in the date picker.</p>
 * <p>Disables dates that are before today to prevent selecting past dates.</p>
 * @param current - The current date being evaluated
 * @returns true if the date should be disabled, false otherwise
 */
const isDateDisabled = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3.5">{{ t('common.date') }}</div>
    </template>

    <template #default>
      <div class="date-info-container">
        <!-- Created Date -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.createdDate') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ taskCreatedDate }}</span>
            </div>
          </div>
        </div>

        <!-- Deadline Date -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.deadlineDate') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isDeadlineEditing" class="info-value-content">
                <span :class="{ 'placeholder-text': !currentDeadlineDate }" class="info-text">
                  {{ currentDeadlineDate || '--' }}
                </span>
                <Button
                  type="link"
                  class="edit-btn"
                  @click="startDeadlineDateEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>

              <AsyncComponent :visible="isDeadlineEditing">
                <Tooltip
                  :visible="isDateValidationError"
                  :title="dateValidationErrorMessage"
                  placement="left"
                  arrowPointAtCenter>
                  <DatePicker
                    v-show="isDeadlineEditing"
                    ref="deadlineDatePickerRef"
                    v-model:value="deadlineDateInputValue"
                    :error="isDateValidationError"
                    :allowClear="false"
                    :showNow="false"
                    :disabledDate="isDateDisabled"
                    :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
                    type="date"
                    size="small"
                    class="edit-date-picker"
                    showToday
                    @change="handleDeadlineDateChange"
                    @blur="handleDeadlineDateBlur" />
                </Tooltip>
              </AsyncComponent>
            </div>
          </div>
        </div>

        <!-- Start Date -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.startDate') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !taskStartDate }" class="info-text">
                {{ taskStartDate || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Processed Date -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.processedDate') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !taskProcessedDate }" class="info-text">
                {{ taskProcessedDate || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Completion Dates -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.confirmedDate') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !taskConfirmedDate }" class="info-text">
                {{ taskConfirmedDate || '--' }}
              </span>
            </div>
          </div>
        </div>

        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.completedDate') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !taskCompletedDate }" class="info-text">
                {{ taskCompletedDate || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Other Dates -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.canceledDate') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !taskCanceledDate }" class="info-text">
                {{ taskCanceledDate || '--' }}
              </span>
            </div>
          </div>
        </div>

        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.execDate') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !taskExecDate }" class="info-text">
                {{ taskExecDate || '--' }}
              </span>
            </div>
          </div>
        </div>

        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.lastModifiedDate') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !taskLastModifiedDate }" class="info-text">
                {{ taskLastModifiedDate || '--' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
/* Main Container */
.date-info-container {
  padding: 1rem 1.375rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

/* Info Row Layout */
.info-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: stretch;
}

/* Individual Info Item */
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
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

/* Edit Date Picker */
.edit-date-picker {
  width: 100%;
  max-width: 20rem;
  font-size: 0.75rem;
}

/* Placeholder text styling */
.placeholder-text {
  color: #7c8087 !important;
  font-weight: 400 !important;
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

/* Legacy support */
.border-none {
  border: none;
}

.left-component {
  position: absolute;
  top: -4px;
  left: 87px;
  width: calc(100% - 87px);
}
</style>
