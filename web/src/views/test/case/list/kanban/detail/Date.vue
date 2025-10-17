<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, DatePicker, Icon, Tooltip } from '@xcan-angus/vue-ui';
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
            <div v-show="!editFlag" class="info-value-content">
              <span class="info-text">{{ deadlineDate }}</span>
              <Button
                v-if="props.canEdit"
                type="link"
                class="edit-btn"
                @click="openEditDeadline">
                <Icon icon="icon-shuxie" />
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
                  class="edit-input"
                  showToday
                  @change="validateDeadlineChange"
                  @blur="commitDeadlineIfValid" />
              </Tooltip>
            </AsyncComponent>
          </div>
        </div>

        <!-- Review Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.reviewDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !reviewDate }">{{ reviewDate || '--' }}</span>
          </div>
        </div>

        <!-- Test Result Handle Date -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.completedDate') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !testResultHandleDate }">{{ testResultHandleDate || '--' }}</span>
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
  width: 100px;
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

/* Text styles */
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
</style>
