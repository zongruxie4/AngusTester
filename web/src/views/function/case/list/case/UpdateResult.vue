<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem } from 'ant-design-vue';
import { EnumMessage, enumUtils } from '@xcan-angus/infra';
import { CaseTestResult } from '@/enums/enums';
import type { Rule } from 'ant-design-vue/es/form';
import { funcCase } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseListObj } from './types';

const { t } = useI18n();

type Params = {
  id: string;
  testResult: string;
  reviewRemark?: string;
  actualWorkload?: string
}

interface Props {
  visible: boolean;
  type: 'batch' | 'one',
  selectedCase?: CaseListObj;
  selectedRowKeys?: string[];
  resultPassed?: boolean;

}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'one',
  resultPassed: true,
  selectedCase: undefined,
  selectedRowKeys: () => []
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'update'): void;
  (e: 'cancel'): void;
  (e: 'addBug', description?: string): void
}>();

const formRef = ref();
const formState = ref<{ testRemark: string; testResult: string, evalWorkload?: string, actualWorkload?: string }>({ testRemark: '', testResult: 'PASSED' });

const close = () => {
  emits('update:visible', false);
  emits('cancel');
};

const loading = ref(false);
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
          id: props.selectedCase.id,
          testRemark: formState.value.testRemark || null,
          testResult: formState.value.testResult,
          evalWorkload: formState.value.evalWorkload || null,
          actualWorkload: formState.value.actualWorkload || null
        }
      ];
  loading.value = true;
  const [error] = await funcCase.putCaseResult(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('functionCase.updateCaseResultModal.testResultUpdateSuccess'));
  emits('update:visible', false);
  emits('update');
  if (addBug) {
    emits('addBug', formState.value.testRemark || '');
  }
};

const handleSubmit = () => {
  formRef.value.validate()
    .then(async () => {
      await onFinish(true);
    });
};

const testResultEnum = ref<EnumMessage<CaseTestResult>[]>([]);
const loadEnums = () => {
  const data = enumUtils.enumToMessages(CaseTestResult);
  if (!data) {
    return;
  }
  testResultEnum.value = data.filter(item => !['PENDING', 'BLOCKED', 'CANCELED'].includes(item.value)) || [];
};

onMounted(() => {
  loadEnums();
});

watch(() => props.visible, (newValue) => {
  if (!newValue || !props.selectedCase) {
    return;
  }
  formState.value.testResult = props.resultPassed ? 'PASSED' : 'NOT_PASSED';

  formState.value.evalWorkload = props.selectedCase.evalWorkload;
  formState.value.actualWorkload = props.selectedCase.actualWorkload || props.selectedCase.evalWorkload;
}, {
  immediate: true
});

const actualWorkloadChange = (value) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

const evalWorkloadChange = (value) => {
  if (!value) {
    formState.value.actualWorkload = '';
    formRef.value.clearValidate('evalWorkload');
  }
};

const validateDate = async (_rule: Rule, value: string) => {
  if (formState.value.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error(t('functionCase.updateCaseResultModal.enterEvalWorkload')));
    } else {
      return Promise.resolve();
    }
  } else {
    return Promise.resolve();
  }
};
</script>
<template>
  <Modal
    :title="props.resultPassed ? t('functionCase.updateCaseResultModal.testPassed') : t('functionCase.updateCaseResultModal.testNotPassed')"
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
        :label="props.selectedCase?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('functionCase.updateCaseResultModal.evalWorkload') : t('functionCase.updateCaseResultModal.evalWorkload')"
        name="evalWorkload"
        :rules="{validator: validateDate, trigger: 'change' }">
        <div class="flex items-center text-3">
          <Input
            v-model:value="formState.evalWorkload"
            size="small"
            dataType="float"
            :min="0.1"
            :max="1000"
            :placeholder="t('functionCase.updateCaseResultModal.minMaxRange')"
            @blur="evalWorkloadChange($event.target.value)" />
          <span
            v-if="props.selectedCase?.evalWorkloadMethod?.value !== 'STORY_POINT'"
            class="whitespace-nowrap ml-1">{{ t('functionCase.updateCaseResultModal.hour') }}</span>
        </div>
      </FormItem>
      <FormItem
        :label="props.selectedCase?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('functionCase.updateCaseResultModal.actualWorkload') : t('functionCase.updateCaseResultModal.actualWorkload')"
        name="actualWorkload">
        <div class="flex items-center text-3">
          <Input
            v-model:value="formState.actualWorkload"
            size="small"
            dataType="float"
            :min="0.1"
            :max="1000"
            :placeholder="t('functionCase.updateCaseResultModal.minMaxRange')"
            @change="actualWorkloadChange($event.target.value)" />
          <span
            v-if="props.selectedCase?.evalWorkloadMethod?.value !== 'STORY_POINT'"
            class="whitespace-nowrap ml-1">{{ t('functionCase.updateCaseResultModal.hour') }}</span>
        </div>
      </FormItem>
      <FormItem
        :label="props.resultPassed ? t('functionCase.updateCaseResultModal.testRemark') : t('functionCase.updateCaseResultModal.notPassedReason')"
        name="testRemark"
        :required="props.resultPassed === false">
        <Input
          v-model:value="formState.testRemark"
          size="small"
          type="textarea"
          :autoSize="{ minRows: 6, maxRows: 6 }"
          :maxlength="200"
          :placeholder="t('functionCase.updateCaseResultModal.testResultOrProcess')" />
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
            {{ t('functionCase.updateCaseResultModal.confirmAndSubmitBug') }}
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
