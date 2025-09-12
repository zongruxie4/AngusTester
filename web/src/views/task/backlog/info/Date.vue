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

const dateRef = ref();
const editFlag = ref(false);
const dateValue = ref<string>();

const dateError = ref();
const dateErrorMessage = ref<string>();

const toEdit = () => {
  dateValue.value = deadlineDate.value;
  editFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof dateRef.value?.focus === 'function') {
        dateRef.value?.focus();
      }
    }, 100);
  });
};

const change = (value:string) => {
  if (!value) {
    dateErrorMessage.value = t('backlog.info.date.messages.selectDeadline');
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    dateError.value = true;
    dateErrorMessage.value = t('backlog.info.date.messages.deadlineMustBeFuture');
    return;
  }

  dateError.value = false;
  dateErrorMessage.value = undefined;
};

const blur = async () => {
  if (dateError.value) {
    if (typeof dateRef.value?.focus === 'function') {
      dateRef.value?.focus();
    }

    return;
  }

  const value = dateValue.value;
  if (!value || value === deadlineDate.value) {
    editFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editDeadlineDateApi(taskId.value, value);
  emit('loadingChange', false);
  if (error) {
    if (typeof dateRef.value?.focus === 'function') {
      dateRef.value?.focus();
    }

    return;
  }

  editFlag.value = false;
  emit('change', { id: taskId.value, deadlineDate: value });
};

// 禁用日期组件当前时间之前的年月日
const disabledDate = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

const taskId = computed(() => props.dataSource?.id);
const createdDate = computed(() => props.dataSource?.createdDate);
const deadlineDate = computed(() => props.dataSource?.deadlineDate);
const startDate = computed(() => props.dataSource?.startDate);
const processedDate = computed(() => props.dataSource?.processedDate);
const confirmedDate = computed(() => props.dataSource?.confirmedDate);
const completedDate = computed(() => props.dataSource?.completedDate);
const canceledDate = computed(() => props.dataSource?.canceledDate);
const execDate = computed(() => props.dataSource?.execDate);
const lastModifiedDate = computed(() => props.dataSource?.lastModifiedDate);
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

        <div v-show="!editFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ deadlineDate }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="toEdit">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
        </div>

        <AsyncComponent :visible="editFlag">
          <Tooltip
            :visible="dateError"
            :title="dateErrorMessage"
            placement="left"
            arrowPointAtCenter>
            <DatePicker
              v-show="editFlag"
              ref="dateRef"
              v-model:value="dateValue"
              :error="dateError"
              :showNow="false"
              :disabledDate="disabledDate"
              :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
              type="date"
              size="small"
              class="edit-container"
              showToday
              @change="change"
              @blur="blur" />
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
