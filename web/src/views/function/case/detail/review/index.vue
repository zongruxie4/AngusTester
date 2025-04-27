<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { http, TESTER } from '@xcan-angus/tools';
import { Image, NoData } from '@xcan-angus/vue-ui';

interface Props {
  caseDetail: {id: string; reviewNum: number; reviewFailNum: number;};
}

const props = withDefaults(defineProps<Props>(), {
  caseDetail: () => ({ id: '' })
});

const CaseInfo = defineAsyncComponent(() => import('@/views/function/case/detail/review/caseInfo/index.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/components/precondition/index.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/case/add/caseSteps.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/components/description/index.vue'));
const reviewNum = computed(() => {
  const total = +(props.caseDetail?.reviewNum || 0);
  const failNum = +(props.caseDetail?.reviewFailNum || 0);
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

// add: 'rgba(82, 196, 26, 0.4)',
// del: 'rgba(255, 82, 82, 0.4)',
// modify: 'rgba(255, 102, 0, 0.4)'

const selectRecordId = ref();
const selectRecordInfo = ref();
const changeCurrentRecord = async (record) => {
  if (selectRecordId.value === record.id) {
    return;
  }
  selectRecordId.value = record.id;
  // const [error, {data}] = await http.get(`${TESTER}/func/review/case/${record.reviewCaseId}`);
  // if (error) {
  //   return;
  // }
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
  const [error, { data }] = await http.get(`${TESTER}/func/case/${props.caseDetail?.id}/review`);
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
    <div class="text-title text-3 font-medium mt-2">评审次数</div>
    <div class="flex w-150 space-x-15 mt-2">
      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-blue-1 text-white rounded">总共</label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewNum.total }}
        </div>
      </div>
      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-status-success text-white rounded">评审通过</label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewNum.successNum }}
        </div>
      </div>
      <div class="flex-1 inline-flex bg-gray-light rounded">
        <label class="w-20 px-2 py-1 bg-status-error text-white rounded">评审未通过</label>
        <div class=" px-2 py-1  w-15 font-medium">
          {{ reviewNum.failNum }}
        </div>
      </div>
    </div>

    <div class="text-title text-3 font-medium mt-6">评审记录</div>
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
          <span class="font-semibold flex-1/3">{{ record.reviewerName }} {{ record.reviewStatus?.value === 'PASSED' ? '评审通过了用例' : '评审用例未通过' }}</span>

          <div class=" flex-1/2">{{ record.reviewDate }}</div>
        </div>

        <div
          v-if="record.reviewRemark"
          class=" mt-3 truncate"
          :title="record.reviewRemark">
          {{ record.reviewRemark }}
        </div>
        <div v-else class="text-sub-content mt-3 truncate">无说明~ </div>
      </div>
    </div>

    <div v-if="!reviewRecords?.length" class="h-80">
      <NoData size="small" />
    </div>

    <div v-if="!!selectRecordInfo" class="flex max-w-200 ">
      <div class="flex-1 p-2 border-r  space-y-3">
        <div class="mb-3">评审版本</div>
        <CaseInfo :caseInfo="selectRecordInfo" />
        <Precondition :caseInfo="selectRecordInfo" />
        <div class="font-semibold text-3.5">
          测试步骤
        </div>
        <CaseStep :defaultValue="selectRecordInfo?.steps || {}" readonly />

        <Description :caseInfo="selectRecordInfo" />
      </div>
      <div class="flex-1 p-2 space-y-3">
        <div class="mb-3">最新版本</div>
        <CaseInfo :caseInfo="props.caseDetail" />
        <Precondition :caseInfo="props.caseDetail" :contentClass="preconditionClass" />
        <div class="font-semibold text-3.5">
          测试步骤
        </div>
        <div :class="stepsClass">
          <CaseStep :defaultValue="props?.caseDetail?.steps || []" readonly />
        </div>
        {{ descriptionClass }}
        <Description :caseInfo="props?.caseDetail" :contentBg="descriptionClass" />
      </div>
    </div>
  </div>
</template>
