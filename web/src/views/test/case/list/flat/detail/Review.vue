<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Image, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ReviewStatus } from '@xcan-angus/infra';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';

const CaseInfo = defineAsyncComponent(() => import('@/views/test/case/list/flat/detail/CaseInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/test/review/detail/case/Precondition.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/test/case/list/CaseSteps.vue'));
const Description = defineAsyncComponent(() => import('@/views/test/review/detail/case/Description.vue'));

interface Props {
  caseDetail: CaseDetail;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  caseDetail: () => ({ id: '' } as unknown as CaseDetail)
});

const reviewStatistics = computed(() => {
  const totalReviews = +(props.caseDetail?.reviewNum || 0);
  const failedReviews = +(props.caseDetail?.reviewFailNum || 0);
  const successfulReviews = totalReviews - failedReviews;
  return { total: totalReviews, failNum: failedReviews, successNum: successfulReviews };
});

const preconditionDiffClass = ref('');
const descriptionDiffClass = ref('');
const stepsDiffClass = ref();

const selectedReviewRecordId = ref();
const selectedReviewRecordInfo = ref();

/**
 * Change current review record and compute diff highlights
 */
const handleReviewRecordSelection = async (reviewRecord) => {
  if (selectedReviewRecordId.value === reviewRecord.id) {
    return;
  }
  selectedReviewRecordId.value = reviewRecord.id;
  selectedReviewRecordInfo.value = reviewRecord.reviewedCase;

  if (!selectedReviewRecordInfo.value.precondition && props.caseDetail.precondition) {
    preconditionDiffClass.value = 'bg-status-add';
  } else if (selectedReviewRecordInfo.value.precondition && !props.caseDetail.precondition) {
    preconditionDiffClass.value = 'bg-status-del';
  } else if (selectedReviewRecordInfo.value.precondition !== props.caseDetail.precondition) {
    preconditionDiffClass.value = 'bg-status-modify';
  } else {
    preconditionDiffClass.value = '';
  }

  if (!selectedReviewRecordInfo.value.description && props.caseDetail.description) {
    descriptionDiffClass.value = 'background-color: rgba(82, 196, 26, 0.1);';
  } else if (selectedReviewRecordInfo.value.description && !props.caseDetail.description) {
    descriptionDiffClass.value = 'background-color: rgba(255, 82, 82, 0.1);';
  } else if (selectedReviewRecordInfo.value.description !== props.caseDetail.description) {
    descriptionDiffClass.value = 'background-color: rgba(255, 102, 0, 0.1);';
  } else {
    descriptionDiffClass.value = '';
  }

  if (!selectedReviewRecordInfo.value.steps?.length && props.caseDetail.steps?.length) {
    stepsDiffClass.value = 'bg-status-add';
  } else if (selectedReviewRecordInfo.value.steps?.length && !props.caseDetail.steps?.length) {
    stepsDiffClass.value = 'bg-status-del';
  } else if (!!selectedReviewRecordInfo.value.steps?.length &&
    (selectedReviewRecordInfo.value.steps?.length !== props.caseDetail.steps?.length)) {
    stepsDiffClass.value = 'bg-status-modify';
  } else if (!!selectedReviewRecordInfo.value.steps?.length &&
    (selectedReviewRecordInfo.value.steps?.length === props.caseDetail.steps?.length)) {
    const hasStepsChanged = selectedReviewRecordInfo.value.steps.some((stepItem, stepIndex) => {
      return (stepItem.expectedResult !== props.caseDetail.steps[stepIndex].expectedResult) ||
        (stepItem.step !== props.caseDetail.steps[stepIndex].step);
    });
    if (hasStepsChanged) {
      stepsDiffClass.value = 'bg-status-modify';
    }
  } else {
    stepsDiffClass.value = '';
  }
};

const reviewRecordsList = ref([]);
/**
 * Load review records of current case
 */
const loadReviewRecords = async () => {
  const [error, { data }] = await testCase.getReviewRecord(props.caseDetail?.id);
  if (error) {
    return;
  }
  reviewRecordsList.value = data || [];
};

onMounted(() => {
  watch(() => props.caseDetail?.id, caseId => {
    if (caseId) {
      loadReviewRecords();
      selectedReviewRecordInfo.value = undefined;
    }
  }, {
    immediate: true
  });
});

defineExpose({
  refresh: loadReviewRecords
});
</script>
<template>
  <div class="text-3 overflow-auto h-full">
    <div class="text-title text-3 font-medium mt-2">{{ t('common.counts.reviewCount') }}</div>

    <div class="flex w-150 space-x-15 mt-2">
      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-blue-1 text-white rounded">{{ t('chart.total') }}</label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewStatistics.total }}
        </div>
      </div>

      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-status-success text-white rounded">
          {{ t('testCase.messages.reviewPassed') }}
        </label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewStatistics.successNum }}
        </div>
      </div>

      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-status-error text-white rounded">
          {{ t('testCase.messages.reviewFailed') }}
        </label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewStatistics.failNum }}
        </div>
      </div>
    </div>

    <div class="text-title text-3 font-medium mt-6">{{ t('common.reviewRecord') }}</div>
    <div class="mt-2 space-y-2">
      <div
        v-for="record in reviewRecordsList"
        :key="record.id"
        class="p-2 border rounded max-w-200 cursor-pointer"
        :class="{'bg-gray-9': record.id === selectedReviewRecordId}"
        @click="handleReviewRecordSelection(record)">
        <div class="flex items-center">
          <Image
            type="avatar"
            :src="record.avatar"
            class="w-5 mr-2" />
          <span class="font-semibold flex-1/3">
            {{ record.reviewerName }} {{ record.reviewStatus?.value === ReviewStatus.PASSED
              ? t('testCase.messages.reviewPassedCase') : t('testCase.messages.reviewFailedCase') }}
          </span>
          <div class=" flex-1/2">{{ record.reviewDate }}</div>
        </div>

        <div
          v-if="record.reviewRemark"
          class=" mt-3 truncate"
          :title="record.reviewRemark">
          {{ record.reviewRemark }}
        </div>
        <div v-else class="text-sub-content mt-3 truncate">{{ t('common.noRemark') }}</div>
      </div>
    </div>

    <div v-if="!reviewRecordsList?.length" class="h-80">
      <NoData size="small" />
    </div>

    <div v-if="!!selectedReviewRecordInfo" class="flex max-w-200 ">
      <div class="flex-1 p-2 border-r  space-y-3">
        <div class="mb-3">{{ t('testCase.messages.reviewVersion') }}</div>

        <CaseInfo :caseInfo="selectedReviewRecordInfo" />

        <Precondition :caseInfo="selectedReviewRecordInfo" />

        <div class="font-semibold text-3.5">{{ t('common.testSteps') }}</div>

        <CaseStep :defaultValue="selectedReviewRecordInfo?.steps || {}" readonly />

        <Description :caseInfo="selectedReviewRecordInfo" />
      </div>

      <div class="flex-1 p-2 space-y-3">
        <div class="mb-3">{{ t('testCase.messages.latestVersion') }}</div>

        <CaseInfo :caseInfo="props.caseDetail" />

        <Precondition :caseInfo="props.caseDetail" :contentClass="preconditionDiffClass" />

        <div class="font-semibold text-3.5">{{ t('common.testSteps') }}</div>

        <div :class="stepsDiffClass">
          <CaseStep :defaultValue="props?.caseDetail?.steps || []" readonly />
        </div>

        {{ descriptionDiffClass }}

        <Description :caseInfo="props?.caseDetail" :contentBg="descriptionDiffClass" />
      </div>
    </div>
  </div>
</template>
