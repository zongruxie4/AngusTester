<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Input, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { enumUtils } from '@xcan-angus/infra';
import { funcCase } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseListObj } from '../PropsType';

const { t } = useI18n();

type Params = {
  id: string;
  reviewRemark: string;
  reviewStatus: string;
}

interface Props {
  visible: boolean;
  type: 'batch' | 'one',
  selectedCase?: CaseListObj;
  selectedRowKeys?: string[];

}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'one',
  selectedCase: undefined,
  selectedRowKeys: () => []
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'update'):void}>();

const formState = ref<{ reviewRemark: string; reviewStatus: string }>({ reviewRemark: '', reviewStatus: 'PASSED' });

const close = () => {
  emits('update:visible', false);
};

const loading = ref(false);
const onFinish = async () => {
  const params:Params[] = props.type === 'batch'
    ? props.selectedRowKeys.map(item => ({
      id: item,
      reviewRemark: formState.value.reviewRemark || null,
      reviewStatus: formState.value.reviewStatus
    }))
    : [
        {
          id: props.selectedCase.id,
          reviewRemark: formState.value.reviewRemark || null,
          reviewStatus: formState.value.reviewStatus
        }
      ];
  loading.value = true;
  const [error] = await funcCase.reviewCase(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('评审成功');
  emits('update:visible', false);
  emits('update');
};

const reviewStatusEnum = ref<{value:string, message:string}[]>([]);
const loadEnums = async () => {
  const [error, data] = await enumUtils.enumToMessages('ReviewStatus');
  if (error) {
    return;
  }
  reviewStatusEnum.value = data.filter(item => item.value !== 'PENDING') || [];
};

const failMessageValue = ref();
const changeFailMessage = (value) => {
  if (value === 'other') {
    formState.value.reviewRemark = '';
  } else {
    formState.value.reviewRemark = failMessage[value];
  }
};

const failMessage = [
  '用例名表述不够简短达意。',
  '用例设计和需求不一致，测试无效。',
  '缺少必要的前提条件，可能导致无法正常执行。',
  '用例步骤准确度和正确性不足，无法准确执行和验证。',
  '用例描述不够清晰，存在二义性或模糊性。',
  '用例没有覆盖所有的功能点和场景，存在遗漏。',
  '用例测试边界定义不够清晰。',
  '优先级安排不合理，可能影响测试的效率和效果。',
  '与其他用例之间的逻辑关系不一致性或存在冲突。',
  '其他'
];
const failOpt = failMessage.map((i, idx) => ({ label: i, value: idx + 1 === failMessage.length ? 'other' : idx }));

onMounted(() => {
  loadEnums();
});
</script>
<template>
  <Modal
    title="评审"
    :visible="props.visible"
    :width="600"
    :footer="null"
    @cancel="close">
    <Form
      :model="formState"
      size="small"
      layout="horizontal"
      :labelCol="{style: { width: '80px' }}"
      @finish="onFinish">
      <FormItem
        name="reviewStatus"
        label="评审结果"
        class="mb-1">
        <RadioGroup
          v-model:value="formState.reviewStatus"
          class="mt-0.5">
          <Radio
            v-for="item in reviewStatusEnum"
            :key="item.value"
            :value="item.value">
            {{ item.message }}
          </Radio>
        </RadioGroup>
      </FormItem>
      <FormItem
        name="reviewRemark"
        label="评审说明">
        <Select
          v-show="formState.reviewStatus === 'FAILED'"
          v-model:value="failMessageValue"
          :options="failOpt"
          class="w-100"
          placeholder="选择不通过原因"
          @change="changeFailMessage" />
        <Input
          v-show="formState.reviewStatus !== 'FAILED' || failMessageValue === 'other'"
          v-model:value="formState.reviewRemark"
          size="small"
          type="textarea"
          class="mt-1"
          :autoSize="{ minRows: 6, maxRows: 6}"
          :maxlength="200"
          :placeholder="t('输入评审说明，最多可输入200字符')" />
      </FormItem>
      <FormItem class="mt-5">
        <div class="flex justify-end">
          <Button
            :loading="loading"
            type="primary"
            size="small"
            htmlType="submit"
            class="px-3">
            提交
          </Button>
          <Button
            size="small"
            class="ml-5 px-3"
            @click="close">
            取消
          </Button>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
