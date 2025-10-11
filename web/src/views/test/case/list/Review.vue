<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Input, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { EnumMessage, ReviewStatus, enumUtils } from '@xcan-angus/infra';
import { test } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

const { t } = useI18n();

// Type definitions
type Params = {
  id: number;
  reviewRemark: string;
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
          id: props.selectedCase?.id,
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
const changeFailMessage = (value: string) => {
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
    :title="t('common.review')"
    :visible="props.visible"
    :width="800"
    :footer="null"
    @cancel="close">
    <Form
      :model="formState"
      size="small"
      layout="horizontal"
      :labelCol="{style: { width: '100px' }}"
      @finish="onFinish">
      <FormItem
        name="reviewStatus"
        :label="t('common.reviewResult')"
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
        :label="t('common.reviewOpinion')">
        <Select
          v-show="formState.reviewStatus === ReviewStatus.FAILED"
          v-model:value="failMessageValue"
          :options="failOpt"
          class="w-100"
          :placeholder="t('testCaseReview.detail.placeholders.selectNotPassedReason')"
          @change="changeFailMessage" />
        <Input
          v-show="formState.reviewStatus !== ReviewStatus.FAILED || failMessageValue === 'other'"
          v-model:value="formState.reviewRemark"
          size="small"
          type="textarea"
          class="mt-1"
          :autoSize="{ minRows: 6, maxRows: 6}"
          :maxlength="200"
          :placeholder="t('common.placeholders.inputReviewOpinion')" />
      </FormItem>

      <FormItem class="mt-5">
        <div class="flex justify-end">
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
            class="ml-5 px-3"
            @click="close">
            {{ t('actions.cancel') }}
          </Button>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
