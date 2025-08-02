<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Input, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { EnumMessage, ReviewStatus, enumUtils } from '@xcan-angus/infra';
import { func } from '@/api/tester';

import { useI18n } from 'vue-i18n';

const { t } = useI18n();

type Params = {
  id: string;
  reviewRemark?: string;
  reviewStatus: string;
}

interface Props {
  visible: boolean;
  selectedRowKeys?: string[];

}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  selectedRowKeys: () => []
});

const emits = defineEmits<{(e: 'update'):void}>();

const formState = ref<{ reviewRemark: string; reviewStatus: string }>({ reviewRemark: '', reviewStatus: 'PASSED' });

const loading = ref(false);
const onFinish = async () => {
  const params:Params[] = props.selectedRowKeys.map(item => ({
    id: item,
    reviewRemark: formState.value.reviewRemark || undefined,
    reviewStatus: formState.value.reviewStatus
  }));
  loading.value = true;
  const [error] = await func.reviewCase(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('评审成功');
  emits('update');
};

const reviewStatusEnum = ref<EnumMessage<ReviewStatus>[]>([]);
const loadEnums = () => {
  const data = enumUtils.enumToMessages(ReviewStatus);
  reviewStatusEnum.value = data.filter(item => item.value !== ReviewStatus.PENDING) || [];
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
  '用例名表述不够简短达意',
  '用例设计和需求不一致，测试无效',
  '缺少必要的前提条件，可能导致无法正常执行',
  '用例步骤准确度和正确性不足，无法准确执行和验证',
  '用例描述不够清晰，存在二义性或模糊性',
  '用例没有覆盖所有的功能点和场景，存在遗漏',
  '用例测试边界定义不够清晰',
  '优先级安排不合理，可能影响测试的效率和效果',
  '与其他用例之间的逻辑关系不一致性或存在冲突',
  '其他'
];
const failOpt = failMessage.map((i, idx) => ({ label: i, value: idx + 1 === failMessage.length ? 'other' : idx }));

onMounted(() => {
  loadEnums();
});
</script>
<template>
  <Form
    :model="formState"
    size="small"
    layout="horizontal"
    :labelCol="{style: { width: '80px', textAlign: 'left' }}"
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
      label="评审意见">
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
        :placeholder="t('输入评审意见，最多可输入200字符')" />
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
      </div>
    </FormItem>
  </Form>
</template>
