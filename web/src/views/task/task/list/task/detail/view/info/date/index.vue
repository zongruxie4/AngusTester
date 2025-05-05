<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, DatePicker, Icon, Toggle, Tooltip } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { task } from 'src/api/tester';

import { TaskInfo } from '../../../../../../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  loading: false
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
    dateErrorMessage.value = '请选择截止时间';
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    dateError.value = true;
    dateErrorMessage.value = '截止时间必须是一个未来时间';
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
  <Toggle>
    <template #title>
      <div class="text-3">日期</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>截止时间</span>
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
                :allowClear="false"
                :showNow="false"
                :disabledDate="disabledDate"
                :showTime="{ hideDisabledOptions: true, format: 'HH:mm:ss' }"
                type="date"
                size="small"
                class="left-component"
                showToday
                @change="change"
                @blur="blur" />
            </Tooltip>
          </AsyncComponent>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>开始时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ startDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>处理时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ processedDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>确认时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ confirmedDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>完成时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ completedDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>取消时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ canceledDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>执行时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ execDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>添加时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ createdDate }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
            <span>最后修改时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ lastModifiedDate || '--' }}</div>
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
