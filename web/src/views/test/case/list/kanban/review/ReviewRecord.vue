<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Image, NoData } from '@xcan-angus/vue-ui';
import { ReviewStatus } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { funcCase } from '@/api/tester';

const BasicInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/review/ReviewRecordCase.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/test/review/detail/case/Precondition.vue'));
const Description = defineAsyncComponent(() => import('@/views/test/review/detail/case/Description.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/test/case/list/CaseSteps.vue'));

interface Props {
  dataSource: {id: string; reviewNum: number; reviewFailNum: number;};
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({ id: '' })
});

const reviewNum = computed(() => {
  const total = +(props.dataSource?.reviewNum || 0);
  const failNum = +(props.dataSource?.reviewFailNum || 0);
  const successNum = total - failNum;
  return {
    total,
    failNum,
    successNum
  };
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

  if (!selectRecordInfo.value.precondition && props.dataSource.precondition) {
    preconditionClass.value = 'bg-status-add';
  } else if (selectRecordInfo.value.precondition && !props.dataSource.precondition) {
    preconditionClass.value = 'bg-status-del';
  } else if (selectRecordInfo.value.precondition !== props.dataSource.precondition) {
    preconditionClass.value = 'bg-status-modify';
  } else {
    preconditionClass.value = '';
  }

  if (!selectRecordInfo.value.description && props.dataSource.description) {
    descriptionClass.value = 'background-color: rgba(82, 196, 26, 0.1);';
  } else if (selectRecordInfo.value.description && !props.dataSource.description) {
    descriptionClass.value = 'background-color: rgba(255, 82, 82, 0.1);';
  } else if (selectRecordInfo.value.description !== props.dataSource.description) {
    descriptionClass.value = 'background-color: rgba(255, 102, 0, 0.1);';
  } else {
    descriptionClass.value = '';
  }

  if (!selectRecordInfo.value.steps?.length && props.dataSource.steps?.length) {
    stepsClass.value = 'bg-status-add';
  } else if (selectRecordInfo.value.steps?.length && !props.dataSource.steps?.length) {
    stepsClass.value = 'bg-status-del';
  } else if (!!selectRecordInfo.value.steps?.length && (selectRecordInfo.value.steps?.length !== props.dataSource.steps?.length)) {
    stepsClass.value = 'bg-status-modify';
  } else if (!!selectRecordInfo.value.steps?.length && (selectRecordInfo.value.steps?.length === props.dataSource.steps?.length)) {
    const isModify = selectRecordInfo.value.steps.some((item, idx) => {
      return (item.expectedResult !== props.dataSource.steps[idx].expectedResult) || (item.step !== props.dataSource.steps[idx].step);
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
  const [error, { data }] = await funcCase.getReviewRecord(props.dataSource?.id);
  if (error) {
    return;
  }
  reviewRecords.value = data || [];
};

onMounted(() => {
  watch(() => props.dataSource?.id, newValue => {
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
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('testCase.kanbanView.reviewRecord.title') }}
    </div>

    <div class="flex flex-col space-y-2">
      <div class="flex items-center rounded overflow-hidden">
        <div class="w-20 px-2 py-1 bg-blue-1 text-white rounded">
          {{ t('chart.total') }}
        </div>
        <div class="w-20 px-2 py-1 font-medium bg-gray-light">
          {{ reviewNum.total }}
        </div>
      </div>

      <div class="flex items-center rounded overflow-hidden">
        <div class="w-20 px-2 py-1 bg-status-success text-white rounded">
          {{ t('testCase.kanbanView.reviewRecord.reviewPassed') }}
        </div>
        <div class="w-20 px-2 py-1 font-medium bg-gray-light">
          {{ reviewNum.successNum }}
        </div>
      </div>

      <div class="flex items-center rounded overflow-hidden">
        <div class="w-20 px-2 py-1 bg-status-error text-white rounded">
          {{ t('testCase.kanbanView.reviewRecord.reviewFailed') }}
        </div>
        <div class="w-20 px-2 py-1 font-medium bg-gray-light">
          {{ reviewNum.failNum }}
        </div>
      </div>
    </div>

    <div class="text-title text-3 font-medium mt-6">
      {{ t('testCase.kanbanView.reviewRecord.reviewRecords') }}
    </div>

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

          <span class="font-semibold flex-1/3">{{ record.reviewerName }}
            {{ record.reviewStatus?.value === ReviewStatus.PASSED
              ? t('testCase.kanbanView.reviewRecord.reviewPassedCase')
              : t('testCase.kanbanView.reviewRecord.reviewFailedCase') }}</span>

          <div class=" flex-1/2">{{ record.reviewDate }}</div>
        </div>

        <div
          v-if="record.reviewRemark"
          class=" mt-3 truncate"
          :title="record.reviewRemark">
          {{ record.reviewRemark }}
        </div>
        <div v-else class="text-sub-content mt-3 truncate">
          {{ t('common.noRemark') }}
        </div>
      </div>
    </div>

    <div v-if="!reviewRecords?.length" class="h-80">
      <NoData size="small" />
    </div>

    <div v-if="!!selectRecordInfo" class="flex max-w-200 ">
      <div class="flex-1 p-2 border-r  space-y-3">
        <div class="mb-3">{{ t('testCase.kanbanView.reviewRecord.reviewVersion') }}</div>

        <BasicInfo :caseInfo="selectRecordInfo" />

        <Precondition :caseInfo="selectRecordInfo" />

        <div class="font-semibold text-3.5">
          {{ t('testCase.kanbanView.reviewRecord.testSteps') }}
        </div>

        <CaseStep :defaultValue="selectRecordInfo?.steps || {}" readonly />

        <Description :caseInfo="selectRecordInfo" />
      </div>

      <div class="flex-1 p-2 space-y-3">
        <div class="mb-3">{{ t('testCase.kanbanView.reviewRecord.latestVersion') }}</div>

        <BasicInfo :caseInfo="props.dataSource" />

        <Precondition :caseInfo="props.dataSource" :contentClass="preconditionClass" />

        <div class="font-semibold text-3.5">
          {{ t('testCase.kanbanView.reviewRecord.testSteps') }}
        </div>

        <div :class="stepsClass">
          <CaseStep :defaultValue="props?.dataSource?.steps || []" readonly />
        </div>

        {{ descriptionClass }}

        <Description :caseInfo="props?.dataSource" :contentBg="descriptionClass" />
      </div>
    </div>
  </div>
</template>
