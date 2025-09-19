<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { AuthObjectType } from '@xcan-angus/infra';

import { SummaryInfo } from '../types';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const loading = ref(false);

const allPlan = ref('');
const planByLastWeek = ref('');
const planByLastMonth = ref('');

const allCase = ref('');
const caseByLastWeek = ref('');
const caseByLastMonth = ref('');

const allTag = ref('');
const tagByLastWeek = ref('');
const tagByLastMonth = ref('');

const allModule = ref('');
const moduleByLastWeek = ref('');
const moduleByLastMonth = ref('');

const reviewByLastWeek = ref('');
const allReview = ref('');
const reviewByLastMonth = ref('');

const allBaseline = ref('');
const baselineByLastWeek = ref('');
const baselineByLastMonth = ref('');

const loadData = async (): Promise<void> => {
  loading.value = true;
  const params = {
    projectId: props.projectId,
    creatorObjectType: AuthObjectType.USER,
    creatorObjectId: props.userInfo?.id
  };
  const [error, res] = await analysis.getFuncResourceCount(params);
  loading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    const data = res.data as SummaryInfo;

    allPlan.value = data.allPlan;
    planByLastWeek.value = data.planByLastWeek;
    planByLastMonth.value = data.planByLastMonth;

    allCase.value = data.allCase;
    caseByLastWeek.value = data.caseByLastWeek;
    caseByLastMonth.value = data.caseByLastMonth;

    allTag.value = data.allTag;
    tagByLastWeek.value = data.tagByLastWeek;
    tagByLastMonth.value = data.tagByLastMonth;

    allModule.value = data.allModule;
    moduleByLastWeek.value = data.moduleByLastWeek;
    moduleByLastMonth.value = data.moduleByLastMonth;

    allReview.value = data.allReview;
    reviewByLastWeek.value = data.reviewByLastWeek;
    reviewByLastMonth.value = data.reviewByLastMonth;

    allBaseline.value = data.allBaseline;
    baselineByLastWeek.value = data.baselineByLastWeek;
    baselineByLastMonth.value = data.baselineByLastMonth;
  }
};

onMounted(() => {
  watch(() => props.projectId, () => {
    reset();
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    reset();
    loadData();
  }, { immediate: true });
});

const reset = () => {
  allPlan.value = '';
  planByLastWeek.value = '';
  planByLastMonth.value = '';

  allCase.value = '';
  caseByLastWeek.value = '';
  caseByLastMonth.value = '';

  allTag.value = '';
  tagByLastWeek.value = '';
  tagByLastMonth.value = '';

  allModule.value = '';
  moduleByLastWeek.value = '';
  moduleByLastMonth.value = '';
};
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('functionHome.myCreationSummary.title') }}</div>
    <div class="flex flex-1 space-x-3.75 justify-start">
      <div class="p-3.5 rounded w-1/3 relative bg-img bg-yellow">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('functionHome.myCreationSummary.plan') }}</span>
          <span class="text-4.5 font-semibold">{{ allPlan }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ planByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ planByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/icon-2.png" class="w-15 absolute right-0 top-0 -z-1" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img bg-red">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('functionHome.myCreationSummary.case') }}</span>
          <span class="text-4.5 font-semibold">{{ allCase }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ caseByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ caseByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/icon-3.png" class="w-15 absolute right-0 top-0 -z-1" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img bg-blue">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('functionHome.myCreationSummary.review') }}</span>
          <span class="text-4.5 font-semibold">{{ allReview }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ reviewByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ reviewByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/icon-1.png" class="w-15 absolute right-0 top-0 -z-1" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img bg-green">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('functionHome.myCreationSummary.baseline') }}</span>
          <span class="text-4.5 font-semibold">{{ allBaseline }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ baselineByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('functionHome.myCreationSummary.last30Days') }}</span>
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
