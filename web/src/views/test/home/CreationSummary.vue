<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { AuthObjectType } from '@xcan-angus/infra';

import { SummaryInfo } from './types';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const isLoading = ref(false);

const allPlan = ref('');
const planByLast7Days = ref('');
const planByLastMonth = ref('');

const allCase = ref('');
const caseByLast7Days = ref('');
const caseByLastMonth = ref('');

const allTag = ref('');
const tagByLast7Days = ref('');
const tagByLastMonth = ref('');

const allModule = ref('');
const moduleByLast7Days = ref('');
const moduleByLastMonth = ref('');

const reviewByLast7Days = ref('');
const allReview = ref('');
const reviewByLastMonth = ref('');

const allBaseline = ref('');
const baselineByLast7Days = ref('');
const baselineByLastMonth = ref('');

/**
 * <p>Loads creation summary statistics for the current project and user.</p>
 * <p>Fetches totals and recent period counts, then populates local state.</p>
 */
const loadSummaryData = async (): Promise<void> => {
  isLoading.value = true;
  const params = {
    projectId: props.projectId,
    creatorObjectType: AuthObjectType.USER,
    creatorObjectId: props.userInfo?.id
  };
  const [error, res] = await analysis.getFuncResourceCount(params);
  isLoading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    const data = res.data as SummaryInfo;

    allPlan.value = data.allPlan;
    planByLast7Days.value = data.planByLast7Days;
    planByLastMonth.value = data.planByLastMonth;

    allCase.value = data.allCase;
    caseByLast7Days.value = data.caseByLast7Days;
    caseByLastMonth.value = data.caseByLastMonth;

    allTag.value = data.allTag;
    tagByLast7Days.value = data.tagByLast7Days;
    tagByLastMonth.value = data.tagByLastMonth;

    allModule.value = data.allModule;
    moduleByLast7Days.value = data.moduleByLast7Days;
    moduleByLastMonth.value = data.moduleByLastMonth;

    allReview.value = data.allReview || '';
    reviewByLast7Days.value = data.reviewByLast7Days || '';
    reviewByLastMonth.value = data.reviewByLastMonth || '';

    allBaseline.value = data.allBaseline || '';
    baselineByLast7Days.value = data.baselineByLast7Days || '';
    baselineByLastMonth.value = data.baselineByLastMonth || '';
  }
};

/**
 * <p>Resets all summary values to empty strings.</p>
 * <p>Used before loading new data to avoid stale UI.</p>
 */
const resetSummaryValues = () => {
  allPlan.value = '';
  planByLast7Days.value = '';
  planByLastMonth.value = '';

  allCase.value = '';
  caseByLast7Days.value = '';
  caseByLastMonth.value = '';

  allTag.value = '';
  tagByLast7Days.value = '';
  tagByLastMonth.value = '';

  allModule.value = '';
  moduleByLast7Days.value = '';
  moduleByLastMonth.value = '';
};

onMounted(() => {
  watch(() => props.projectId, () => {
    resetSummaryValues();
    loadSummaryData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    resetSummaryValues();
    loadSummaryData();
  }, { immediate: true });
});
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('testHome.myCreationSummary.title') }}</div>
    <div class="flex flex-1 space-x-3.75 justify-start">
      <div class="p-3.5 rounded w-1/4 relative bg-img bg-yellow">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.plan') }}</span>
          <span class="text-4.5 font-semibold">{{ allPlan }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ planByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ planByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/icon-2.png" class="w-15 absolute right-0 top-0 -z-1" />
      </div>

      <div class="p-3.5 rounded w-1/4 relative bg-img bg-red">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.useCase') }}</span>
          <span class="text-4.5 font-semibold">{{ allCase }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ caseByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ caseByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/icon-3.png" class="w-15 absolute right-0 top-0 -z-1" />
      </div>

      <div class="p-3.5 rounded w-1/4 relative bg-img bg-blue">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.review') }}</span>
          <span class="text-4.5 font-semibold">{{ allReview }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ reviewByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ reviewByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/icon-1.png" class="w-15 absolute right-0 top-0 -z-1" />
      </div>

      <div class="p-3.5 rounded w-1/4 relative bg-img bg-green">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.baseline') }}</span>
          <span class="text-4.5 font-semibold">{{ allBaseline }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ baselineByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ baselineByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/icon-4.png" class="w-15 absolute right-0 top-0 -z-1" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.bg-img {
  background-repeat: no-repeat;
  background-size: 100% 100%;
}
/* Restore gradient background styles similar to previous version */
.bg-yellow {
  background: linear-gradient(to right, #f7cb71, #fef7df);
}
.bg-red {
  background: linear-gradient(to right, #ffb99f, #fff0e8);
}
.bg-blue {
  background: linear-gradient(to right, #d1e7ff, #eff6ff);
}
.bg-green {
  background: linear-gradient(to right, #87d7ee, #e2f7fd);
}
</style>
