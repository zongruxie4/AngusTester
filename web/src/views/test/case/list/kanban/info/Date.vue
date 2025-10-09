<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, DatePicker, Icon, Tooltip } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';

import { TIME_FORMAT } from '@/utils/constant';
import { CaseDetail } from '@/views/test/types';
import { CaseInfoEditProps } from '@/views/test/case/list/types';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseDetail): void;
  (event: 'update:dataSource', value: CaseDetail): void;
}>();

const dateRef = ref();
const editFlag = ref(false);
const dateValue = ref<string>();

const dateError = ref();
const dateErrorMessage = ref<string>();

const caseId = computed(() => props.dataSource?.id);
const createdDate = computed(() => props.dataSource?.createdDate);
const deadlineDate = computed(() => props.dataSource?.deadlineDate);
const testResultHandleDate = computed(() => props.dataSource?.testResultHandleDate);
const reviewDate = computed(() => props.dataSource?.reviewDate);
const lastModifiedDate = computed(() => props.dataSource?.lastModifiedDate);

/*
  Enter deadline edit mode and autofocus the date picker.
*/
const openEditDeadline = () => {
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

/*
  Validate deadline value; disallow empty and past times.
*/
const validateDeadlineChange = (value:string) => {
  if (!value) {
    dateErrorMessage.value = t('common.Placeholders.selectDeadline');
    return;
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    dateError.value = true;
    dateErrorMessage.value = t('testCase.messages.deadlineMustBeFuture');
    return;
  }

  dateError.value = false;
  dateErrorMessage.value = undefined;
};

/*
  Commit deadline on blur if valid and changed; otherwise keep focus.
*/
const commitDeadlineIfValid = async () => {
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

  emitLoadingChange(true);
  const [error] = await testCase.putDeadline(caseId.value, value);
  emitLoadingChange(false);
  if (error) {
    if (typeof dateRef.value?.focus === 'function') {
      dateRef.value?.focus();
    }

    return;
  }

  editFlag.value = false;
  await refreshCaseDetail();
};

// 禁用日期组件当前时间之前的年月日
const disabledDate = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

/*
  Emit loading state to parent.
*/
const emitLoadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

/*
  Refresh case detail and sync to parent.
*/
const refreshCaseDetail = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  emitLoadingChange(true);
  const [error, res] = await testCase.getCaseDetail(id);
  emitLoadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('common.date') }}
    </div>

    <div class="space-y-2.5">
      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('common.reviewDate') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ reviewDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('common.completedDate') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ testResultHandleDate || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('common.createdDate') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ createdDate }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('common.deadlineDate') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!editFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ deadlineDate }}</div>
          <Button
            v-if="props.canEdit"
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="openEditDeadline">
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
              @change="validateDeadlineChange"
              @blur="commitDeadlineIfValid" />
          </Tooltip>
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('common.lastModifiedDate') }}</span>
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
