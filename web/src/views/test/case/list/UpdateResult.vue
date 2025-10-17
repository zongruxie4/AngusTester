<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Input, Modal, notification, Icon } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup, Tooltip } from 'ant-design-vue';
import { EnumMessage, enumUtils, EvalWorkloadMethod } from '@xcan-angus/infra';
import { CaseTestResult } from '@/enums/enums';
import type { Rule } from 'ant-design-vue/es/form';
import { testCase } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

const { t } = useI18n();

// Type definitions
type Params = {
  id: string;
  testResult: string;
  testRemark?: string | null;
  evalWorkload?: string | null;
  actualWorkload?: string | null;
}

// Component props interface
interface Props {
  visible: boolean;
  type: 'batch' | 'one',
  selectedCase?: CaseDetail;
  selectedRowKeys?: number[];
  resultPassed?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'one',
  resultPassed: true,
  selectedCase: undefined,
  selectedRowKeys: () => []
});

// Component emits

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'update'): void;
  (e: 'cancel'): void;
  (e: 'addBug', description?: string): void
}>();

// Form state management
const formRef = ref();
const formState = ref<{ testRemark: string; testResult: string, evalWorkload?: string, actualWorkload?: string }>
({ testRemark: '', testResult: CaseTestResult.PASSED });
const loading = ref(false);

/**
 * Close the modal
 */
const close = () => {
  emits('update:visible', false);
  emits('cancel');
};
/**
 * Handle form submission
 * @param addBug - Whether to add bug after submission
 */
const onFinish = async (addBug = false) => {
  const params: Params[] = props.type === 'batch'
    ? props.selectedRowKeys.map(item => ({
      id: item.toString(),
      testRemark: formState.value.testRemark || null,
      testResult: formState.value.testResult,
      evalWorkload: formState.value.evalWorkload || null,
      actualWorkload: formState.value.actualWorkload || null
    }))
    : [
        {
          id: props.selectedCase?.id?.toString()!,
          testRemark: formState.value.testRemark || null,
          testResult: formState.value.testResult,
          evalWorkload: formState.value.evalWorkload || null,
          actualWorkload: formState.value.actualWorkload || null
        }
      ];
  loading.value = true;
  const [error] = await testCase.putCaseResult(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.updateSuccess'));
  emits('update:visible', false);
  emits('update');
  if (addBug) {
    emits('addBug', formState.value.testRemark || '');
  }
};

/**
 * Handle form submission with bug creation
 */
const handleSubmit = () => {
  formRef.value.validate()
    .then(async () => {
      await onFinish(true);
    });
};

// Enum management
const testResultEnum = ref<EnumMessage<CaseTestResult>[]>([]);

/**
 * Load test result enum options
 */
const loadEnums = () => {
  testResultEnum.value = enumUtils.enumToMessages(CaseTestResult, [CaseTestResult.PENDING, CaseTestResult.BLOCKED, CaseTestResult.CANCELED]);
};

/**
 * Handle actual workload change
 * @param value - Actual workload value
 */
const actualWorkloadChange = (value: string) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Handle eval workload change
 * @param value - Eval workload value
 */
const evalWorkloadChange = (value: string) => {
  if (!value) {
    formState.value.actualWorkload = undefined;
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Validate eval workload field
 * @param _rule - Validation rule
 * @param value - Field value
 * @returns Validation result
 */
const validateDate = async (_rule: Rule, value: string) => {
  if (formState.value.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error(t('testCase.messages.enterEvalWorkload')));
    } else {
      return Promise.resolve();
    }
  } else {
    return Promise.resolve();
  }
};

// Component lifecycle
onMounted(() => {
  loadEnums();
});

// Watchers
watch(() => props.visible, (newValue) => {
  if (!newValue || !props.selectedCase) {
    return;
  }
  formState.value.testResult = props.resultPassed ? CaseTestResult.PASSED : CaseTestResult.NOT_PASSED;

  formState.value.evalWorkload = props.selectedCase.evalWorkload?.toString();
  formState.value.actualWorkload = (props.selectedCase.actualWorkload || props.selectedCase.evalWorkload)?.toString();
}, {
  immediate: true
});
</script>
<template>
  <Modal
    class="update-result-modal"
    :title="t('testCase.messages.updateTestResult')"
    :visible="props.visible"
    :width="720"
    :footer="null"
    :destroyOnClose="true"
    @cancel="close">
    <div class="update-result-container">
      <Form
        ref="formRef"
        :model="formState"
        size="small"
        layout="vertical"
        class="update-result-form"
        @finish="onFinish(false)">
        <!-- Test Result Selection -->
        <FormItem
          :label="t('common.testResult')"
          name="testResult"
          class="test-result-item">
          <RadioGroup
            v-model:value="formState.testResult"
            class="result-radio-group">
            <Radio
              :value="CaseTestResult.PASSED"
              class="result-radio">
              <span class="radio-label">{{ t('status.passed') }}</span>
            </Radio>
            <Radio
              :value="CaseTestResult.NOT_PASSED"
              class="result-radio">
              <span class="radio-label">{{ t('status.notPassed') }}</span>
            </Radio>
          </RadioGroup>
        </FormItem>

        <!-- Test Remark Section -->
        <FormItem
          :label="formState.testResult === CaseTestResult.PASSED ? t('testCase.messages.testRemark') : t('testCase.messages.notPassedReason')"
          name="testRemark"
          :required="formState.testResult === CaseTestResult.NOT_PASSED"
          class="custom-remark-section">
          <Input
            v-model:value="formState.testRemark"
            type="textarea"
            class="remark-textarea"
            :autoSize="{ minRows: 4, maxRows: 6 }"
            :maxlength="200"
            :placeholder="formState.testResult === CaseTestResult.PASSED ? t('testCase.messages.testResultTip') : t('testCase.messages.notPassedReasonTip')"
            showCount />
        </FormItem>

        <!-- Workload Section Title -->
        <div class="workload-section-title">{{ t('common.evalWorkloadMethod') }}</div>

        <!-- Workload Fields -->
        <div class="workload-fields">
          <!-- Eval Workload -->
          <FormItem
            name="evalWorkload"
            :rules="{validator: validateDate, trigger: 'change' }"
            class="workload-item">
            <template #label>
              <span class="flex items-center">
                {{ t('common.evalWorkload') }}
                <Tooltip
                  placement="right"
                  arrowPointAtCenter
                  :overlayStyle="{'max-width':'400px'}"
                  :title="props.selectedCase?.evalWorkloadMethod?.value === EvalWorkloadMethod.STORY_POINT
                    ? t('common.storyPointsHint') : t('common.workHoursHint')">
                  <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                </Tooltip>
              </span>
            </template>
            <div class="workload-input-group">
              <Input
                v-model:value="formState.evalWorkload"
                size="small"
                dataType="float"
                :min="0.1"
                :max="1000"
                class="workload-input"
                :placeholder="t('common.placeholders.workloadRange')"
                @blur="evalWorkloadChange($event.target.value)" />
              <span
                v-if="props.selectedCase?.evalWorkloadMethod?.value !== EvalWorkloadMethod.STORY_POINT"
                class="workload-unit">{{ t('unit.hour') }}</span>
            </div>
          </FormItem>

          <!-- Actual Workload -->
          <FormItem
            name="actualWorkload"
            class="workload-item">
            <template #label>
              <span class="flex items-center">
                {{ t('common.actualWorkload') }}
                <Tooltip
                  placement="right"
                  arrowPointAtCenter
                  :overlayStyle="{'max-width':'400px'}"
                  :title="props.selectedCase?.evalWorkloadMethod?.value === EvalWorkloadMethod.STORY_POINT
                    ? t('common.actualStoryPointsHint') : t('common.actualWorkHoursHint')">
                  <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                </Tooltip>
              </span>
            </template>
            <div class="workload-input-group">
              <Input
                v-model:value="formState.actualWorkload"
                size="small"
                dataType="float"
                :min="0.1"
                :max="1000"
                class="workload-input"
                :placeholder="t('common.placeholders.workloadRange')"
                @change="actualWorkloadChange($event.target.value)" />
              <span
                v-if="props.selectedCase?.evalWorkloadMethod?.value !== EvalWorkloadMethod.STORY_POINT"
                class="workload-unit">{{ t('unit.hour') }}</span>
            </div>
          </FormItem>
        </div>

        <!-- Action Buttons -->
        <FormItem class="action-buttons">
          <div class="button-group">
            <Button
              v-if="formState.testResult === CaseTestResult.NOT_PASSED"
              :loading="loading"
              type="primary"
              size="small"
              class="bug-btn"
              @click="handleSubmit">
              <Icon icon="icon-bug" class="btn-icon" />
              {{ t('testCase.actions.confirmAndSubmitBug') }}
            </Button>
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
.update-result-modal {
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

/* Update result container */
.update-result-container {
  padding: 24px;
  background: #ffffff;
  border-radius: 0 0 8px 8px;
}

/* Result info card */
.result-info-card {
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
  font-weight: 600;
  background: #ffffff;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}

.result-passed {
  color: #059669;
  background: #ecfdf5;
  border-color: #a7f3d0;
}

.result-failed {
  color: #dc2626;
  background: #fef2f2;
  border-color: #fecaca;
}

/* Form styles */
.update-result-form {
  :deep(.ant-form-item-label) {
    padding-bottom: 8px;
  }

  :deep(.ant-form-item-label > label) {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
  }
}

/* Test result item */
.test-result-item {
  margin-bottom: 20px;
}

.result-radio-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.result-radio {
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

/* Workload section title */
.workload-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 4px;
  padding-bottom: 8px;
}

/* Workload fields container */
.workload-fields {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.workload-item {
  margin-bottom: 16px;
}

.workload-item:last-child {
  margin-bottom: 0;
}

.workload-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.workload-input {
  flex: 1;
}

.workload-unit {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  min-width: 40px;
  text-align: center;
}

/* Tooltip icon styles */
.text-tips {
  color: #9ca3af;
  font-size: 14px;
}

.text-tips:hover {
  color: #6b7280;
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
  flex-wrap: wrap;
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

.bug-btn {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
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
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.2);
}

.bug-btn:hover {
  background: linear-gradient(135deg, #b91c1c 0%, #991b1b 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
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

/* Animation effects */
.update-result-container {
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
</style>
