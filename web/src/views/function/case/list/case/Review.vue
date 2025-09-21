<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Input, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { EnumMessage, ReviewStatus, enumUtils } from '@xcan-angus/infra';
import { funcCase } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseDetailChecked } from '../types';

const { t } = useI18n();

type Params = {
  id: string;
  reviewRemark: string;
  reviewStatus: string;
}

interface Props {
  visible: boolean;
  type: 'batch' | 'one',
  selectedCase?: CaseDetailChecked;
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
  notification.success(t('functionCase.reviewCase.reviewSuccess'));
  emits('update:visible', false);
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
  t('functionCase.reviewCase.failReasons.caseNameNotClear'),
  t('functionCase.reviewCase.failReasons.caseDesignInconsistent'),
  t('functionCase.reviewCase.failReasons.missingPrerequisites'),
  t('functionCase.reviewCase.failReasons.stepAccuracyInsufficient'),
  t('functionCase.reviewCase.failReasons.descriptionUnclear'),
  t('functionCase.reviewCase.failReasons.incompleteCoverage'),
  t('functionCase.reviewCase.failReasons.boundaryUnclear'),
  t('functionCase.reviewCase.failReasons.priorityUnreasonable'),
  t('functionCase.reviewCase.failReasons.logicInconsistent'),
  t('functionCase.reviewCase.failReasons.other')
];
const failOpt = failMessage.map((i, idx) => ({ label: i, value: idx + 1 === failMessage.length ? 'other' : idx }));

onMounted(() => {
  loadEnums();
});
</script>
<template>
  <Modal
    :title="t('functionCase.reviewCase.title')"
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
        :label="t('functionCase.reviewCase.reviewResult')"
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
        :label="t('functionCase.reviewCase.reviewRemark')">
        <Select
          v-show="formState.reviewStatus === ReviewStatus.FAILED"
          v-model:value="failMessageValue"
          :options="failOpt"
          class="w-100"
          :placeholder="t('functionCase.reviewCase.selectFailReason')"
          @change="changeFailMessage" />

        <Input
          v-show="formState.reviewStatus !== ReviewStatus.FAILED || failMessageValue === 'other'"
          v-model:value="formState.reviewRemark"
          size="small"
          type="textarea"
          class="mt-1"
          :autoSize="{ minRows: 6, maxRows: 6}"
          :maxlength="200"
          :placeholder="t('functionCase.reviewCase.enterReviewRemark')" />
      </FormItem>

      <FormItem class="mt-5">
        <div class="flex justify-end">
          <Button
            :loading="loading"
            type="primary"
            size="small"
            htmlType="submit"
            class="px-3">
            {{ t('actions.submit') }}
          </Button>
          <Button
            size="small"
            class="ml-5 px-3"
            @click="close">
            {{ t('actions.cancel') }}
          </Button>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
