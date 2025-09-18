<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, DatePicker, Icon, Tooltip } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';
import { TIME_FORMAT } from '@/utils/constant';
import { TaskInfoProps } from '@/views/task/task/list/types';

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
}>();

// Date editing state
const datePickerRef = ref();
const isDateEditing = ref(false);
const dateInputValue = ref<string>();

// Date validation state
const hasDateError = ref();
const dateErrorMessage = ref<string>();

// Computed properties
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

// Date editing methods
/**
 * Enter date editing mode and focus the date picker
 */
const enterDateEditMode = () => {
  dateInputValue.value = currentDeadlineDate.value;
  isDateEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof datePickerRef.value?.focus === 'function') {
        datePickerRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle date picker value change and validate the selected date
 * @param value - Selected date value
 */
const handleDatePickerChange = (value: string) => {
  if (!value) {
    dateErrorMessage.value = t('task.detailInfo.date.validation.selectDeadline');
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    hasDateError.value = true;
    dateErrorMessage.value = t('task.detailInfo.date.validation.futureTimeRequired');
    return;
  }

  hasDateError.value = false;
  dateErrorMessage.value = undefined;
};

/**
 * Handle date picker blur and update task deadline date
 */
const handleDatePickerBlur = async () => {
  if (hasDateError.value) {
    if (typeof datePickerRef.value?.focus === 'function') {
      datePickerRef.value?.focus();
    }
    return;
  }

  const selectedValue = dateInputValue.value;
  if (!selectedValue || selectedValue === currentDeadlineDate.value) {
    isDateEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editDeadlineDateApi(currentTaskId.value, selectedValue);
  emit('loadingChange', false);
  if (error) {
    if (typeof datePickerRef.value?.focus === 'function') {
      datePickerRef.value?.focus();
    }
    return;
  }

  isDateEditing.value = false;
  emit('change', { id: currentTaskId.value, deadlineDate: selectedValue });
};

/**
 * Disable dates before today in the date picker
 * @param current - Current date being evaluated
 * @returns Whether the date should be disabled
 */
const isDateDisabled = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};
</script>

<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('task.detailInfo.date.title') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Created Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.createTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ taskCreatedDate }}</span>
          </div>
        </div>

        <!-- Deadline Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.deadline') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isDateEditing" class="info-value-content">
              <span class="info-text">{{ currentDeadlineDate }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterDateEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isDateEditing">
              <Tooltip
                :visible="hasDateError"
                :title="dateErrorMessage"
                placement="left"
                arrowPointAtCenter>
                <DatePicker
                  v-show="isDateEditing"
                  ref="datePickerRef"
                  v-model:value="dateInputValue"
                  :error="hasDateError"
                  :showNow="false"
                  :disabledDate="isDateDisabled"
                  :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
                  type="date"
                  size="small"
                  class="edit-input"
                  showToday
                  @change="handleDatePickerChange"
                  @blur="handleDatePickerBlur" />
              </Tooltip>
            </AsyncComponent>
          </div>
        </div>

        <!-- Start Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.startTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskStartDate }">{{ taskStartDate || '--' }}</span>
          </div>
        </div>

        <!-- Processed Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.processTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskProcessedDate }">{{ taskProcessedDate || '--' }}</span>
          </div>
        </div>

        <!-- Confirmed Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.confirmTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskConfirmedDate }">{{ taskConfirmedDate || '--' }}</span>
          </div>
        </div>

        <!-- Completed Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.completeTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskCompletedDate }">{{ taskCompletedDate || '--' }}</span>
          </div>
        </div>

        <!-- Canceled Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.cancelTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskCanceledDate }">{{ taskCanceledDate || '--' }}</span>
          </div>
        </div>

        <!-- Execute Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.execTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskExecDate }">{{ taskExecDate || '--' }}</span>
          </div>
        </div>

        <!-- Last Modified Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.date.fields.lastModifyTime') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskLastModifiedDate }">{{ taskLastModifiedDate || '--' }}</span>
          </div>
        </div>
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

/* Info row styles */
.info-row { display: flex; align-items: flex-start; min-height: auto; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }

/* Label styles */
.info-label { flex-shrink: 0; width: 100px; display: flex; align-items: center; font-size: 12px; color: #686868; font-weight: 500; line-height: 1.4; }
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

/* Legacy style compatibility */
.border-none { border: none; }
.edit-container { width: 100%; transform: translateY(-5px); }
</style>
