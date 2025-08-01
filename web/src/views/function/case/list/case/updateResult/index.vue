<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem } from 'ant-design-vue';
import { enumUtils } from '@xcan-angus/infra';
import type { Rule } from 'ant-design-vue/es/form';
import { funcCase } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseListObj } from '../PropsType';

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
  notification.success('测试结果修改成功');
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

const testResultEnum = ref<{ value: string, description: string }[]>([]);
const loadEnums = () => {
  const data = enumUtils.enumToMessages('CaseTestResult');
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
      return Promise.reject(new Error('请输入评估工作量'));
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
    :title="props.resultPassed ? '测试通过' : '测试未通过'"
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
        :label="props.selectedCase?.evalWorkloadMethod?.value === 'STORY_POINT' ? '评估故事点' : '评估工时'"
        name="evalWorkload"
        :rules="{validator: validateDate, trigger: 'change' }">
        <div class="flex items-center text-3">
          <Input
            v-model:value="formState.evalWorkload"
            size="small"
            dataType="float"
            :min="0.1"
            :max="1000"
            :placeholder="props.selectedCase?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('最小0.1，最大1000') : t('最小0.1，最大1000')"
            @blur="evalWorkloadChange($event.target.value)" />
          <span
            v-if="props.selectedCase?.evalWorkloadMethod?.value !== 'STORY_POINT'"
            class="whitespace-nowrap ml-1">小时</span>
        </div>
      </FormItem>
      <FormItem
        :label="props.selectedCase?.evalWorkloadMethod?.value === 'STORY_POINT' ? '实际故事点' : '实际工时'"
        name="actualWorkload">
        <div class="flex items-center text-3">
          <Input
            v-model:value="formState.actualWorkload"
            size="small"
            dataType="float"
            :min="0.1"
            :max="1000"
            :placeholder="props.selectedCase?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('最小0.1，最大1000') : t('最小0.1，最大1000')"
            @change="actualWorkloadChange($event.target.value)" />
          <span
            v-if="props.selectedCase?.evalWorkloadMethod?.value !== 'STORY_POINT'"
            class="whitespace-nowrap ml-1">小时</span>
        </div>
      </FormItem>
      <FormItem
        :label="props.resultPassed ? '测试备注' : '不通过原因'"
        name="testRemark"
        :required="props.resultPassed === false">
        <Input
          v-model:value="formState.testRemark"
          size="small"
          type="textarea"
          :autoSize="{ minRows: 6, maxRows: 6 }"
          :maxlength="200"
          :placeholder="t('测试结果或过程说明，最多可输入200字符')" />
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
            {{ t('确认并提交Bug') }}
          </Button>
          <Button
            :loading="loading"
            type="primary"
            size="small"
            htmlType="submit"
            class="px-3">
            {{ t('确认') }}
          </Button>
          <Button
            size="small"
            class="px-3"
            @click="close">
            {{ t('取消') }}
          </Button>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
