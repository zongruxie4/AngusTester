<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Image, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ReviewStatus } from '@xcan-angus/infra';
import { funcCase } from '@/api/tester';

const CaseInfo = defineAsyncComponent(() => import('@/views/function/case/list/flat/detail/CaseInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/case/Precondition.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/CaseSteps.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/case/Description.vue'));

interface Props {
  caseDetail: {id: string; reviewNum: number; reviewFailNum: number;};
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  caseDetail: () => ({ id: '' })
});

const reviewNum = computed(() => {
  const total = +(props.caseDetail?.reviewNum || 0);
  const failNum = +(props.caseDetail?.reviewFailNum || 0);
  const successNum = total - failNum;
  return { total, failNum, successNum };
});

const preconditionClass = ref('');
const descriptionClass = ref('');
const stepsClass = ref();

const selectRecordId = ref();
const selectRecordInfo = ref();
const changeCurrentRecord = async (record) => {
  if (selectRecordId.value === record.id) {
    return;
  }
  selectRecordId.value = record.id;
  selectRecordInfo.value = record.reviewedCase;

  if (!selectRecordInfo.value.precondition && props.caseDetail.precondition) {
    preconditionClass.value = 'bg-status-add';
  } else if (selectRecordInfo.value.precondition && !props.caseDetail.precondition) {
    preconditionClass.value = 'bg-status-del';
  } else if (selectRecordInfo.value.precondition !== props.caseDetail.precondition) {
    preconditionClass.value = 'bg-status-modify';
  } else {
    preconditionClass.value = '';
  }

  if (!selectRecordInfo.value.description && props.caseDetail.description) {
    descriptionClass.value = 'background-color: rgba(82, 196, 26, 0.1);';
  } else if (selectRecordInfo.value.description && !props.caseDetail.description) {
    descriptionClass.value = 'background-color: rgba(255, 82, 82, 0.1);';
  } else if (selectRecordInfo.value.description !== props.caseDetail.description) {
    descriptionClass.value = 'background-color: rgba(255, 102, 0, 0.1);';
  } else {
    descriptionClass.value = '';
  }

  if (!selectRecordInfo.value.steps?.length && props.caseDetail.steps?.length) {
    stepsClass.value = 'bg-status-add';
  } else if (selectRecordInfo.value.steps?.length && !props.caseDetail.steps?.length) {
    stepsClass.value = 'bg-status-del';
  } else if (!!selectRecordInfo.value.steps?.length && (selectRecordInfo.value.steps?.length !== props.caseDetail.steps?.length)) {
    stepsClass.value = 'bg-status-modify';
  } else if (!!selectRecordInfo.value.steps?.length && (selectRecordInfo.value.steps?.length === props.caseDetail.steps?.length)) {
    const isModify = selectRecordInfo.value.steps.some((item, idx) => {
      return (item.expectedResult !== props.caseDetail.steps[idx].expectedResult) || (item.step !== props.caseDetail.steps[idx].step);
    });
    if (isModify) {
      stepsClass.value = 'bg-status-modify';
    }
  } else {
    stepsClass.value = '';
  }
};

const reviewRecords = ref([]);
const loadReviewRecord = async () => {
  const [error, { data }] = await funcCase.getReviewRecord(props.caseDetail?.id);
  if (error) {
    return;
  }
  reviewRecords.value = data || [];
};

onMounted(() => {
  watch(() => props.caseDetail?.id, newValue => {
    if (newValue) {
      loadReviewRecord();
      selectRecordInfo.value = undefined;
    }
  }, {
    immediate: true
  });
});

defineExpose({
  refresh: loadReviewRecord
});
</script>
<template>
  <div class="text-3 overflow-auto h-full">
    <div class="text-title text-3 font-medium mt-2">{{ t('functionCase.detail.review.reviewCount') }}</div>

    <div class="flex w-150 space-x-15 mt-2">
      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-blue-1 text-white rounded">{{ t('functionCase.detail.review.total') }}</label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewNum.total }}
        </div>
      </div>

      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-status-success text-white rounded">{{ t('functionCase.detail.review.reviewPassed') }}</label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewNum.successNum }}
        </div>
      </div>

      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-status-error text-white rounded">{{ t('functionCase.detail.review.reviewFailed') }}</label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewNum.failNum }}
        </div>
      </div>
    </div>

    <div class="text-title text-3 font-medium mt-6">{{ t('functionCase.detail.review.reviewRecords') }}</div>
    <div class="mt-2 space-y-2">
      <div
        v-for="record in reviewRecords"
        :key="record.id"
        class="p-2 border rounded max-w-200 cursor-pointer"
        :class="{'bg-gray-9': record.id === selectRecordId}"
        @click="changeCurrentRecord(record)">
        <div class="flex items-center">
          <Image
            type="avatar"
            :src="record.avatar"
            class="w-5 mr-2" />
          <span class="font-semibold flex-1/3">
            {{ record.reviewerName }} {{ record.reviewStatus?.value === ReviewStatus.PASSED
              ? t('functionCase.detail.review.reviewPassedCase') : t('functionCase.detail.review.reviewFailedCase') }}
          </span>
          <div class=" flex-1/2">{{ record.reviewDate }}</div>
        </div>

        <div
          v-if="record.reviewRemark"
          class=" mt-3 truncate"
          :title="record.reviewRemark">
          {{ record.reviewRemark }}
        </div>
        <div v-else class="text-sub-content mt-3 truncate">{{ t('functionCase.detail.review.noRemark') }}</div>
      </div>
    </div>

    <div v-if="!reviewRecords?.length" class="h-80">
      <NoData size="small" />
    </div>

    <div v-if="!!selectRecordInfo" class="flex max-w-200 ">
      <div class="flex-1 p-2 border-r  space-y-3">
        <div class="mb-3">{{ t('functionCase.detail.review.reviewVersion') }}</div>

        <CaseInfo :caseInfo="selectRecordInfo" />

        <Precondition :caseInfo="selectRecordInfo" />

        <div class="font-semibold text-3.5">{{ t('functionCase.detail.review.testSteps') }}</div>

        <CaseStep :defaultValue="selectRecordInfo?.steps || {}" readonly />

        <Description :caseInfo="selectRecordInfo" />
      </div>

      <div class="flex-1 p-2 space-y-3">
        <div class="mb-3">{{ t('functionCase.detail.review.latestVersion') }}</div>

        <CaseInfo :caseInfo="props.caseDetail" />

        <Precondition :caseInfo="props.caseDetail" :contentClass="preconditionClass" />

        <div class="font-semibold text-3.5">{{ t('functionCase.detail.review.testSteps') }}</div>

        <div :class="stepsClass">
          <CaseStep :defaultValue="props?.caseDetail?.steps || []" readonly />
        </div>

        {{ descriptionClass }}

        <Description :caseInfo="props?.caseDetail" :contentBg="descriptionClass" />
      </div>
    </div>
  </div>
</template>
