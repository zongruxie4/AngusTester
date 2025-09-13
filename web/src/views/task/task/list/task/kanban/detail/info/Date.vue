<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, DatePicker, Icon, Tooltip } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';
import { TIME_FORMAT } from '@/utils/constant';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

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
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('task.detailInfo.date.title') }}</div>

    <div class="space-y-2.5">
      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.createTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskCreatedDate }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.deadline') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isDateEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ currentDeadlineDate }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="enterDateEditMode">
            <Icon icon="icon-shuxie" class="text-3.5" />
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
              class="edit-container"
              showToday
              @change="handleDatePickerChange"
              @blur="handleDatePickerBlur" />
          </Tooltip>
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.startTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskStartDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.processTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskProcessedDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.confirmTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskConfirmedDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.completeTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskCompletedDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.cancelTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskCanceledDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.execTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskExecDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.date.fields.lastModifyTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskLastModifiedDate || '--' }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.border-none {
  border: none;
}

.edit-container {
  width: 100%;
  transform: translateY(-5px);
}
</style>
