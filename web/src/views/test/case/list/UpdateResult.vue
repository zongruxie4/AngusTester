<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem } from 'ant-design-vue';
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
  reviewRemark?: string;
  actualWorkload?: string
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
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'update'): void;
  (e: 'cancel'): void;
  (e: 'addBug', description?: string): void
}>();

// Form state management
const formRef = ref();
const formState = ref<{ testRemark: string; testResult: string, evalWorkload?: number, actualWorkload?: number }>
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
      id: item,
      testRemark: formState.value.testRemark || null,
      testResult: formState.value.testResult,
      evalWorkload: formState.value.evalWorkload || null,
      actualWorkload: formState.value.actualWorkload || null
    }))
    : [
        {
          id: props.selectedCase?.id,
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

  formState.value.evalWorkload = props.selectedCase.evalWorkload;
  formState.value.actualWorkload = props.selectedCase.actualWorkload || props.selectedCase.evalWorkload;
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :title="props.resultPassed ? t('status.passed') : t('status.notPassed')"
    :visible="props.visible"
    :width="600"
    :footer="null"
    @cancel="close">
    <Form
      ref="formRef"
      :model="formState"
      :labelCol="{ style: { width: '86px', textAlign: 'right' } }"
      size="small"
      layout="horizontal"
      @finish="onFinish(false)">
      <FormItem
        :label="t('common.evalWorkload')"
        name="evalWorkload"
        :rules="{validator: validateDate, trigger: 'change' }">
        <div class="flex items-center text-3">
          <Input
            v-model:value="formState.evalWorkload"
            size="small"
            dataType="float"
            :min="0.1"
            :max="1000"
            :placeholder="t('common.placeholders.workloadRange')"
            @blur="evalWorkloadChange($event.target.value)" />
          <span
            v-if="props.selectedCase?.evalWorkloadMethod?.value !== EvalWorkloadMethod.STORY_POINT"
            class="whitespace-nowrap ml-1">{{ t('unit.hour') }}</span>
        </div>
      </FormItem>

      <FormItem
        :label="t('common.actualWorkload')"
        name="actualWorkload">
        <div class="flex items-center text-3">
          <Input
            v-model:value="formState.actualWorkload"
            size="small"
            dataType="float"
            :min="0.1"
            :max="1000"
            :placeholder="t('common.placeholders.workloadRange')"
            @change="actualWorkloadChange($event.target.value)" />
          <span
            v-if="props.selectedCase?.evalWorkloadMethod?.value !== EvalWorkloadMethod.STORY_POINT"
            class="whitespace-nowrap ml-1">{{ t('unit.hour') }}</span>
        </div>
      </FormItem>

      <FormItem
        :label="props.resultPassed ? t('testCase.messages.testRemark') : t('testCase.messages.notPassedReason')"
        name="testRemark"
        :required="!props.resultPassed">
        <Input
          v-model:value="formState.testRemark"
          size="small"
          type="textarea"
          :autoSize="{ minRows: 6, maxRows: 6 }"
          :maxlength="200"
          :placeholder="t('testCase.messages.testResultOrProcess')" />
      </FormItem>

      <FormItem class="mt-5">
        <div class="flex justify-end space-x-2">
          <Button
            v-if="!props.resultPassed"
            :loading="loading"
            type="primary"
            size="small"
            class="px-3"
            @click="handleSubmit">
            {{ t('testCase.actions.confirmAndSubmitBug') }}
          </Button>
          <Button
            :loading="loading"
            type="primary"
            size="small"
            htmlType="submit"
            class="px-3">
            {{ t('actions.confirm') }}
          </Button>
          <Button
            size="small"
            class="px-3"
            @click="close">
            {{ t('actions.cancel') }}
          </Button>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
