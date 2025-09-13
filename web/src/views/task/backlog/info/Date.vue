<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, DatePicker, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import dayjs, { Dayjs } from 'dayjs';
import { task } from '@/api/tester';

import { TaskInfo } from '../../types';
import { TIME_FORMAT } from '@/utils/constant';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
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
    dateValidationMessage.value = t('backlog.info.date.messages.selectDeadline');
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    hasDateValidationError.value = true;
    dateValidationMessage.value = t('backlog.info.date.messages.deadlineMustBeFuture');
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
  const [error] = await task.editDeadlineDateApi(currentTaskId.value, newDeadlineDate);
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
  <div class="h-full text-3 leading-5 px-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('backlog.info.date.title') }}</div>

    <div class="space-y-2.5">
      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.addTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ createdDate }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.deadline') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isDeadlineEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ deadlineDate }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="startDeadlineDateEditing">
            <Icon icon="icon-shuxie" class="text-3.5" />
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
              class="edit-container"
              showToday
              @change="handleDeadlineDateChange"
              @blur="handleDeadlineDateBlur" />
          </Tooltip>
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.startTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ startDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.processTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ processedDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.confirmTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ confirmedDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.completeTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ completedDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.cancelTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ canceledDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.executeTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ execDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.date.lastModifiedTime') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ lastModifiedDate || '--' }}</div>
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
