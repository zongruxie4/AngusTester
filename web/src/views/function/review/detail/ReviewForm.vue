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
  <div class="bg-white rounded-lg p-6">
    <Form
      :model="formState"
      layout="vertical"
      @finish="reviewCase">
      <FormItem
        name="reviewStatus"
        :label="t('caseReview.detail.reviewResult')"
        class="mb-6">
        <RadioGroup
          v-model:value="formState.reviewStatus"
          class="grid grid-cols-2 gap-4">
          <Radio
            v-for="item in reviewStatusEnum"
            :key="item.value"
            :value="item.value"
            class="p-2 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition-all cursor-pointer">
            <span class="font-medium">{{ item.message }}</span>
          </Radio>
        </RadioGroup>
      </FormItem>

      <FormItem
        name="reviewRemark"
        :label="t('caseReview.detail.reviewOpinion')"
        class="mb-6">
        <div class="space-y-4">
          <Select
            v-show="formState.reviewStatus === ReviewStatus.FAILED"
            v-model:value="failMessageValue"
            :options="failMessageSelectOptions"
            class="w-full"
            :placeholder="t('caseReview.detail.selectFailReason')"
            @change="changeFailMessage" />

          <Input
            v-show="formState.reviewStatus !== ReviewStatus.FAILED || failMessageValue === 'other'"
            v-model:value="formState.reviewRemark"
            type="textarea"
            class="w-full"
            :autoSize="{ minRows: 4, maxRows: 8}"
            :maxlength="200"
            :placeholder="t('caseReview.detail.enterReviewOpinion')"
            :showCount="true" />
        </div>
      </FormItem>

      <FormItem>
        <div class="flex justify-end space-x-3">
          <Button
            size="middle"
            @click="$emit('cancel')">
            {{ t('actions.cancel') }}
          </Button>
          <Button
            :loading="loading"
            type="primary"
            size="middle"
            htmlType="submit"
            class="px-6">
            <Icon icon="icon-tijiao" class="mr-2" />
            {{ t('common.confirm') }}
          </Button>
        </div>
      </FormItem>
    </Form>
  </div>
</template>
