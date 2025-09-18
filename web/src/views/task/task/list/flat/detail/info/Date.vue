<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, DatePicker, Icon, Toggle, Tooltip } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import dayjs, { Dayjs } from 'dayjs';
import { task } from '@/api/tester';

import { TaskInfo } from '@/views/task/types';
import { TIME_FORMAT } from '@/utils/constant';
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
    dateValidationErrorMessage.value = t('task.detailInfo.date.validation.selectDeadline');
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    isDateValidationError.value = true;
    dateValidationErrorMessage.value = t('task.detailInfo.date.validation.futureTimeRequired');
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
  const [error] = await task.editDeadlineDateApi(currentTaskId.value, newValue);
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
      <div class="text-3.5">{{ t('task.detailInfo.date.title') }}</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.deadline') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isDeadlineEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ currentDeadlineDate }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
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
                class="left-component"
                showToday
                @change="handleDeadlineDateChange"
                @blur="handleDeadlineDateBlur" />
            </Tooltip>
          </AsyncComponent>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.startTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskStartDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.processTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskProcessedDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.confirmTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskConfirmedDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.completeTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskCompletedDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.cancelTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskCanceledDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.execTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskExecDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.createTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskCreatedDate }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-25 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.date.fields.lastModifyTime') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ taskLastModifiedDate || '--' }}</div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
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
