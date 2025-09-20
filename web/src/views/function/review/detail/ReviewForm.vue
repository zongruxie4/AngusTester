<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { EnumMessage, ReviewStatus, enumUtils } from '@xcan-angus/infra';
import { func } from '@/api/tester';

// Composables
const { t } = useI18n();

// Type definitions
type ReviewParams = {
  id: string;
  reviewRemark?: string;
  reviewStatus: string;
}

interface Props {
  visible: boolean;
  selectedRowKeys?: string[];
}

// Props and emits
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  selectedRowKeys: () => []
});

const emits = defineEmits<{(e: 'update'):void}>();

// Reactive state
const formState = ref<{ reviewRemark: string; reviewStatus: string }>({
  reviewRemark: '',
  reviewStatus: ReviewStatus.PASSED
});

const loading = ref(false);
const reviewStatusEnum = ref<EnumMessage<ReviewStatus>[]>([]);
const failMessageValue = ref();

/**
 * <p>Submits review for selected cases.</p>
 * <p>Maps selected case IDs to review parameters and calls API.</p>
 */
const reviewCase = async () => {
  const reviewParams: ReviewParams[] = props.selectedRowKeys.map(caseId => ({
    id: caseId,
    reviewRemark: formState.value.reviewRemark || undefined,
    reviewStatus: formState.value.reviewStatus
  }));
  loading.value = true;
  const [error] = await func.reviewCase(reviewParams);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('caseReview.detail.reviewSuccess'));
  emits('update');
};

/**
 * <p>Loads review status enum options.</p>
 * <p>Filters out PENDING status as it's not selectable for review completion.</p>
 */
const loadEnums = () => {
  const data = enumUtils.enumToMessages(ReviewStatus);
  reviewStatusEnum.value = data.filter(item => item.value !== ReviewStatus.PENDING) || [];
};

/**
 * <p>Handles fail message selection changes.</p>
 * <p>Sets predefined message or clears for custom input.</p>
 */
const changeFailMessage = (selectedValue) => {
  if (selectedValue === 'other') {
    formState.value.reviewRemark = '';
  } else {
    formState.value.reviewRemark = failMessageOptions[selectedValue];
  }
};

// Fail message configuration
const failMessageOptions = [
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

const failMessageSelectOptions = failMessageOptions.map((message, index) => ({
  label: message,
  value: index + 1 === failMessageOptions.length ? 'other' : index
}));

/**
 * <p>Component mounted lifecycle hook.</p>
 * <p>Loads enum values for review status options.</p>
 */
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
        v-show="formState.reviewStatus === ReviewStatus.FAILED"
        v-model:value="failMessageValue"
        :options="failMessageSelectOptions"
        class="w-100"
        :placeholder="t('caseReview.detail.selectFailReason')"
        @change="changeFailMessage" />

      <Input
        v-show="formState.reviewStatus !== ReviewStatus.FAILED || failMessageValue === 'other'"
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
