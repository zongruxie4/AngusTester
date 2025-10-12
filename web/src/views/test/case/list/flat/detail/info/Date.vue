<script setup lang="ts">
import { nextTick, ref } from 'vue';
import { Grid, Icon, DatePicker, notification, Toggle } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { CaseActionAuth } from '@/views/test/case/types';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  actionAuth?: CaseActionAuth[];
  columns?: any[][];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  actionAuth: () => ([]),
  columns: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

const datePickerRef = ref();
const isEditDisabledDate = ref(false);
const deadlineDate = ref(dayjs().add(1, 'day').format(DATE_TIME_FORMAT));
const loading = ref(false);
const dateExpand = ref(true);

/**
 * <p>Disable date before yesterday end-of-day.</p>
 * <p>Prevents selection of past dates for deadline.</p>
 * @param current - Current date being evaluated
 * @returns True if date should be disabled
 */
const disabledDate = current => {
  return current && current < dayjs().subtract(1, 'day').endOf('day');
};

/**
 * <p>Enter editing mode for deadline date and focus the picker.</p>
 * <p>Sets the current deadline value and focuses the date picker.</p>
 */
const openEditDeadlineDate = () => {
  deadlineDate.value = props.dataSource?.deadlineDate || dayjs().add(1, 'day').format(DATE_TIME_FORMAT);
  isEditDisabledDate.value = true;
  nextTick(() => {
    datePickerRef.value.focus();
  });
};

/**
 * <p>Persist deadline date with validation for future date.</p>
 * <p>Updates the deadline if changed and validates it's in the future.</p>
 */
const editDeadlineDate = async () => {
  if (!props.dataSource) {
    isEditDisabledDate.value = false;
    return;
  }

  if (loading.value || props.dataSource?.deadlineDate === deadlineDate.value) {
    isEditDisabledDate.value = false;
    return;
  }

  if (dayjs(deadlineDate.value).isBefore(dayjs(), 'minute')) {
    notification.warning(t('testCase.messages.deadlineMustBeFuture'));
    return;
  }

  loading.value = true;
  const [error] = await testCase.putDeadline(props.dataSource.id, deadlineDate.value);
  loading.value = false;
  isEditDisabledDate.value = false;
  if (error) {
    return;
  }
  emit('change', { deadlineDate: deadlineDate.value });
};
</script>
<template>
  <Toggle
    v-model:open="dateExpand"
    :title="t('common.date')"
    class="mt-3.5">
    <Grid
      :columns="columns"
      :dataSource="dataSource"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px"
      class="pt-2 pl-5.5">
      <template #deadlineDate="{ text }">
        <div class="flex items-center relative w-full">
          <template v-if="!isEditDisabledDate">
            {{ text }}
            <Icon
              v-if="props.actionAuth.includes('edit')"
              class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer -mt-0.5"
              icon="icon-shuxie"
              @click="openEditDeadlineDate" />
          </template>
          <template v-else>
            <DatePicker
              ref="datePickerRef"
              v-model:value="deadlineDate"
              :allowClear="false"
              :disabledDate="disabledDate"
              :showTime="{ efaultValue: dayjs('00:00:00', TIME_FORMAT) }"
              size="small"
              type="date"
              class="w-full absolute -top-1.25"
              @blur="editDeadlineDate" />
          </template>
        </div>
      </template>
    </Grid>
  </Toggle>
</template>
<style scoped>
:deep(.toggle-title) {
  @apply text-3.5;
}
</style>
