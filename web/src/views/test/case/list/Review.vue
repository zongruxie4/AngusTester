<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Input, Modal, notification, Select, Icon } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { EnumMessage, ReviewStatus, enumUtils } from '@xcan-angus/infra';
import { test } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

const { t } = useI18n();

// Type definitions
type Params = {
  id: number;
  reviewRemark: string | null;
  reviewStatus: ReviewStatus;
}

// Component props interface
interface Props {
  visible: boolean;
  type: 'batch' | 'one',
  selectedCase?: CaseDetail;
  selectedRowKeys?: number[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'one',
  selectedCase: undefined,
  selectedRowKeys: () => []
});

// Component emits
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'update'):void}>();

// Form state management
const formState = ref<{ reviewRemark: string; reviewStatus: ReviewStatus }>({ reviewRemark: '', reviewStatus: ReviewStatus.PASSED });
const loading = ref(false);

/**
 * Close the modal
 */
const close = () => {
  emits('update:visible', false);
};

/**
 * Handle form submission
 */
const onFinish = async () => {
  const params: Params[] = props.type === 'batch'
    ? props.selectedRowKeys.map(item => ({
      id: item,
      reviewRemark: formState.value.reviewRemark || null,
      reviewStatus: formState.value.reviewStatus
    }))
    : [
        {
          id: props.selectedCase?.id!,
          reviewRemark: formState.value.reviewRemark || null,
          reviewStatus: formState.value.reviewStatus
        }
      ];
  loading.value = true;
  const [error] = await test.reviewCase(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.reviewSuccess'));
  emits('update:visible', false);
  emits('update');
};

// Enum management
const reviewStatusEnum = ref<EnumMessage<ReviewStatus>[]>([]);

/**
 * Load review status enum options
 */
const loadEnums = () => {
  reviewStatusEnum.value = enumUtils.enumToMessages(ReviewStatus, [ReviewStatus.PENDING]);
};

// Form handling functions
const failMessageValue = ref<string>();

/**
 * Handle fail message selection change
 * @param value - Selected fail message value
 */
const changeFailMessage = (value: any) => {
  if (value === 'other') {
    formState.value.reviewRemark = '';
  } else {
    formState.value.reviewRemark = failMessage[Number(value)];
  }
};

// Fail reason options
const failMessage = [
  t('testCaseReview.detail.failReasons.caseNameNotClear'),
  t('testCaseReview.detail.failReasons.caseDesignInconsistent'),
  t('testCaseReview.detail.failReasons.missingPrerequisites'),
  t('testCaseReview.detail.failReasons.stepAccuracyInsufficient'),
  t('testCaseReview.detail.failReasons.descriptionUnclear'),
  t('testCaseReview.detail.failReasons.incompleteCoverage'),
  t('testCaseReview.detail.failReasons.boundaryUnclear'),
  t('testCaseReview.detail.failReasons.priorityUnreasonable'),
  t('testCaseReview.detail.failReasons.logicInconsistent'),
  t('testCaseReview.detail.failReasons.other')
];
const failOpt = failMessage.map((i, idx) => ({ label: i, value: idx + 1 === failMessage.length ? 'other' : idx.toString() }));

// Component lifecycle
onMounted(() => {
  loadEnums();
});
</script>
<template>
  <Modal
    class="review-modal"
    :title="t('common.review')"
    :visible="props.visible"
    :width="720"
    :footer="null"
    :destroyOnClose="true"
    @cancel="close">
    <div class="review-container">
      <Form
        :model="formState"
        size="small"
        layout="vertical"
        class="review-form"
        @finish="onFinish">
        <!-- Review Result -->
        <FormItem
          name="reviewStatus"
          :label="t('common.reviewResult')"
          class="review-status-item">
          <RadioGroup
            v-model:value="formState.reviewStatus"
            class="status-radio-group">
            <Radio
              v-for="item in reviewStatusEnum"
              :key="item.value"
              :value="item.value"
              class="status-radio">
              <span class="radio-label">{{ item.message }}</span>
            </Radio>
          </RadioGroup>
        </FormItem>

        <!-- Review Opinion -->
        <FormItem
          name="reviewRemark"
          :label="t('common.reviewOpinion')"
          class="review-remark-item">
          <!-- Failure Reason Selection -->
          <div
            v-show="formState.reviewStatus === ReviewStatus.FAILED"
            class="fail-reason-section">
            <Select
              v-model:value="failMessageValue"
              :options="failOpt"
              class="fail-reason-select"
              :placeholder="t('testCaseReview.detail.placeholders.selectNotPassedReason')"
              @change="changeFailMessage">
              <template #suffixIcon>
                <Icon icon="icon-xiala" class="select-icon" />
              </template>
            </Select>
          </div>

          <!-- Custom Opinion Input -->
          <div
            v-show="formState.reviewStatus !== ReviewStatus.FAILED || failMessageValue === 'other'"
            class="custom-remark-section">
            <Input
              v-model:value="formState.reviewRemark"
              type="textarea"
              class="remark-textarea"
              :autoSize="{ minRows: 4, maxRows: 6 }"
              :maxlength="200"
              :placeholder="t('common.placeholders.inputReviewOpinion')"
              showCount />
          </div>
        </FormItem>

        <!-- Action Buttons -->
        <FormItem class="action-buttons">
          <div class="button-group">
            <Button
              :loading="loading"
              type="primary"
              size="small"
              htmlType="submit"
              class="submit-btn">
              {{ t('actions.confirm') }}
            </Button>
            <Button
              size="small"
              class="cancel-btn"
              @click="close">
              {{ t('actions.cancel') }}
            </Button>
          </div>
        </FormItem>
      </Form>
    </div>
  </Modal>
</template>

<style scoped>
/* Modal container styles */
.review-modal {
  :deep(.ant-modal-header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px 8px 0 0;
    padding: 16px 24px;
    border-bottom: none;
  }

  :deep(.ant-modal-title) {
    color: #ffffff;
    font-size: 16px;
    font-weight: 600;
    display: flex;
    align-items: center;
  }

  :deep(.ant-modal-body) {
    padding: 0;
    background: #fafbfc;
  }

  :deep(.ant-modal-close) {
    color: #ffffff;
    top: 16px;
    right: 24px;
  }

  :deep(.ant-modal-close:hover) {
    color: #f0f0f0;
  }
}

/* Review container */
.review-container {
  padding: 24px;
  background: #ffffff;
  border-radius: 0 0 8px 8px;
}

/* Review info card */
.review-info-card {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  min-width: 80px;
}

.info-value {
  font-size: 13px;
  color: #1e293b;
  font-weight: 600;
  background: #ffffff;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}

/* Form styles */
.review-form {
  :deep(.ant-form-item-label) {
    padding-bottom: 8px;
  }

  :deep(.ant-form-item-label > label) {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
  }
}

/* Review status item */
.review-status-item {
  margin-bottom: 20px;
}

.status-radio-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.status-radio {
  :deep(.ant-radio-wrapper) {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    background: #ffffff;
    transition: all 0.3s ease;
    cursor: pointer;
    margin: 0;
  }

  :deep(.ant-radio-wrapper:hover) {
    border-color: #3b82f6;
    background: #f0f9ff;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }

  :deep(.ant-radio-wrapper-checked) {
    border-color: #3b82f6;
    background: #eff6ff;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  }

  :deep(.ant-radio) {
    margin-right: 8px;
  }

  :deep(.ant-radio-inner) {
    width: 16px;
    height: 16px;
    border-color: #d1d5db;
  }

  :deep(.ant-radio-checked .ant-radio-inner) {
    background-color: #3b82f6;
    border-color: #3b82f6;
  }
}

.radio-label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

/* Review remark item */
.review-remark-item {
  margin-bottom: 24px;
}

.fail-reason-section {
  margin-bottom: 12px;
}

.fail-reason-select {
  :deep(.ant-select-selector) {
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    padding: 8px 12px;
    background: #ffffff;
    transition: all 0.3s ease;
  }

  :deep(.ant-select-selector:hover) {
    border-color: #3b82f6;
  }

  :deep(.ant-select-focused .ant-select-selector) {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }
}

.select-icon {
  color: #9ca3af;
  font-size: 12px;
}

.custom-remark-section {
  :deep(.ant-input) {
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    padding: 12px;
    background: #ffffff;
    transition: all 0.3s ease;
    font-size: 13px;
    line-height: 1.5;
  }

  :deep(.ant-input:hover) {
    border-color: #3b82f6;
  }

  :deep(.ant-input:focus) {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }

  :deep(.ant-input::placeholder) {
    color: #9ca3af;
  }
}

.remark-textarea {
  resize: none;
}

/* Action buttons */
.action-buttons {
  margin-bottom: 0;
  padding-top: 16px;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.submit-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  border-radius: 6px;
  padding: 8px 20px;
  font-weight: 600;
  font-size: 13px;
  height: 36px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.cancel-btn {
  background: #ffffff;
  border: 2px solid #e5e7eb;
  color: #6b7280;
  border-radius: 6px;
  padding: 8px 20px;
  font-weight: 500;
  font-size: 13px;
  height: 36px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  border-color: #d1d5db;
  color: #374151;
  background: #f9fafb;
  transform: translateY(-1px);
}

.btn-icon {
  font-size: 12px;
}

/* Responsive design */
@media (max-width: 768px) {
  .review-container {
    padding: 16px;
  }

  .review-info-card {
    padding: 12px;
    margin-bottom: 16px;
  }

  .status-radio-group {
    flex-direction: column;
    gap: 8px;
  }

  .button-group {
    flex-direction: column;
    gap: 8px;
  }

  .submit-btn,
  .cancel-btn {
    width: 100%;
    justify-content: center;
  }
}

/* Animation effects */
.review-container {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fail-reason-section,
.custom-remark-section {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
