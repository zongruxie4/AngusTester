<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, DatePicker, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import dayjs, { Dayjs } from 'dayjs';
import { issue } from '@/api/tester';

import { TaskDetail } from '../../types';
import { TIME_FORMAT } from '@/utils/constant';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
}>();

// Reactive State Variables
const deadlineDatePickerRef = ref();
const isDeadlineEditing = ref(false);
const deadlineInputValue = ref<string>();

// Computed Properties
const currentTaskId = computed(() => props.dataSource?.id);
const createdDate = computed(() => props.dataSource?.createdDate);
const deadlineDate = computed(() => props.dataSource?.deadlineDate);
const startDate = computed(() => props.dataSource?.startDate);
const processedDate = computed(() => props.dataSource?.processedDate);
const confirmedDate = computed(() => props.dataSource?.confirmedDate);
const completedDate = computed(() => props.dataSource?.completedDate);
const canceledDate = computed(() => props.dataSource?.canceledDate);
const execDate = computed(() => props.dataSource?.execDate);
const lastModifiedDate = computed(() => props.dataSource?.lastModifiedDate);

// Date validation state
const hasDateValidationError = ref();
const dateValidationMessage = ref<string>();

// Deadline Date Management Functions
/**
 * <p>Initialize deadline date editing mode</p>
 * <p>Sets the current deadline date value and enables editing state</p>
 */
const startDeadlineDateEditing = () => {
  deadlineInputValue.value = deadlineDate.value;
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
 * <p>Handle deadline date change and validation</p>
 * <p>Validates the selected date and shows appropriate error messages</p>
 */
const handleDeadlineDateChange = (value: string) => {
  if (!value) {
    dateValidationMessage.value = t('common.placeholders.selectDeadline');
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    hasDateValidationError.value = true;
    dateValidationMessage.value = t('common.placeholders.deadlineMustBeFuture');
    return;
  }

  hasDateValidationError.value = false;
  dateValidationMessage.value = undefined;
};

/**
 * <p>Handle deadline date input blur event</p>
 * <p>Validates the date and calls API to update deadline if valid</p>
 */
const handleDeadlineDateBlur = async () => {
  if (hasDateValidationError.value) {
    if (typeof deadlineDatePickerRef.value?.focus === 'function') {
      deadlineDatePickerRef.value?.focus();
    }
    return;
  }

  const newDeadlineDate = deadlineInputValue.value;
  if (!newDeadlineDate || newDeadlineDate === deadlineDate.value) {
    isDeadlineEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await issue.editDeadlineDateApi(currentTaskId.value, newDeadlineDate);
  emit('loadingChange', false);
  if (error) {
    if (typeof deadlineDatePickerRef.value?.focus === 'function') {
      deadlineDatePickerRef.value?.focus();
    }
    return;
  }

  isDeadlineEditing.value = false;
  emit('change', { id: currentTaskId.value, deadlineDate: newDeadlineDate });
};

/**
 * <p>Disable past dates in date picker</p>
 * <p>Prevents selection of dates before today</p>
 */
const disablePastDates = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};
</script>

<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.date') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Created Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.createdDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ createdDate }}</span>
          </div>
        </div>

        <!-- Deadline Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.deadlineDate') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isDeadlineEditing" class="info-value-content">
              <span class="info-text">{{ deadlineDate }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startDeadlineDateEditing">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <AsyncComponent :visible="isDeadlineEditing">
              <Tooltip
                :visible="hasDateValidationError"
                :title="dateValidationMessage"
                placement="left"
                arrowPointAtCenter>
                <DatePicker
                  v-show="isDeadlineEditing"
                  ref="deadlineDatePickerRef"
                  v-model:value="deadlineInputValue"
                  :error="hasDateValidationError"
                  :showNow="false"
                  :disabledDate="disablePastDates"
                  :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
                  type="date"
                  size="small"
                  class="edit-input"
                  showToday
                  @change="handleDeadlineDateChange"
                  @blur="handleDeadlineDateBlur" />
              </Tooltip>
            </AsyncComponent>
          </div>
        </div>

        <!-- Start Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.startDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !startDate }">{{ startDate || '--' }}</span>
          </div>
        </div>

        <!-- Processed Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.processedDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !processedDate }">{{ processedDate || '--' }}</span>
          </div>
        </div>

        <!-- Confirmed Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.confirmedDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !confirmedDate }">{{ confirmedDate || '--' }}</span>
          </div>
        </div>

        <!-- Completed Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.completedDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !completedDate }">{{ completedDate || '--' }}</span>
          </div>
        </div>

        <!-- Canceled Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.canceledDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !canceledDate }">{{ canceledDate || '--' }}</span>
          </div>
        </div>

        <!-- Execute Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.execDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !execDate }">{{ execDate || '--' }}</span>
          </div>
        </div>

        <!-- Last Modified Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.lastModifiedDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !lastModifiedDate }">{{ lastModifiedDate || '--' }}</span>
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
  width: 90px;
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

/* Legacy style compatibility */
.border-none {
  border: none;
}

.edit-container {
  width: 100%;
  transform: translateY(-5px);
}
</style>
