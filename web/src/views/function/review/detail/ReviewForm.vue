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

const reviewCase = async () => {
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
  notification.success(t('caseReview.detail.reviewSuccess'));
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
  t('caseReview.detail.failReasons.caseNameNotClear'),
  t('caseReview.detail.failReasons.caseDesignInconsistent'),
  t('caseReview.detail.failReasons.missingPrerequisites'),
  t('caseReview.detail.failReasons.stepAccuracyInsufficient'),
  t('caseReview.detail.failReasons.descriptionUnclear'),
  t('caseReview.detail.failReasons.incompleteCoverage'),
  t('caseReview.detail.failReasons.boundaryUnclear'),
  t('caseReview.detail.failReasons.priorityUnreasonable'),
  t('caseReview.detail.failReasons.logicInconsistent'),
  t('caseReview.detail.failReasons.other')
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
    @finish="reviewCase">
    <FormItem
      name="reviewStatus"
      :label="t('caseReview.detail.reviewResult')"
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
      :label="t('caseReview.detail.reviewOpinion')">
      <Select
        v-show="formState.reviewStatus === 'FAILED'"
        v-model:value="failMessageValue"
        :options="failOpt"
        class="w-100"
        :placeholder="t('caseReview.detail.selectFailReason')"
        @change="changeFailMessage" />
      <Input
        v-show="formState.reviewStatus !== 'FAILED' || failMessageValue === 'other'"
        v-model:value="formState.reviewRemark"
        size="small"
        type="textarea"
        class="mt-1"
        :autoSize="{ minRows: 6, maxRows: 6}"
        :maxlength="200"
        :placeholder="t('caseReview.detail.enterReviewOpinion')" />
    </FormItem>
    <FormItem class="mt-5">
      <div class="flex justify-end">
        <Button
          :loading="loading"
          type="primary"
          size="small"
          htmlType="submit"
          class="px-3">
          {{ t('caseReview.detail.submit') }}
        </Button>
      </div>
    </FormItem>
  </Form>
</template>
