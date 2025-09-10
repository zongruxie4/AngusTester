<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { ResourceInfo } from '../types';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const Plan = defineAsyncComponent(() => import('@/views/function/home/summary/plan/index.vue'));
const Case = defineAsyncComponent(() => import('@/views/function/home/summary/case/index.vue'));
const Priority = defineAsyncComponent(() => import('@/views/function/home/summary/priority/index.vue'));
const Review = defineAsyncComponent(() => import('@/views/function/home/summary/review/index.vue'));

const loading = ref(false);
const dataSource = ref<ResourceInfo>();

const loadData = async (): Promise<void> => {
  loading.value = true;
  const params = {
    projectId: props.projectId
  };
  const [error, res] = await analysis.getFuncResourceCount(params);
  loading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    dataSource.value = res.data;
  }
};

const reset = () => {
  dataSource.value = {
    allPlan: '0',
    planByLastWeek: '0',
    planByLastMonth: '0',
    allCase: '0',
    caseByLastWeek: '0',
    caseByLastMonth: '0',
    caseByOverdue: '0',
    allTag: '0',
    tagByLastWeek: '0',
    tagByLastMonth: '0',
    allModule: '0',
    moduleByLastWeek: '0',
    moduleByLastMonth: '0',
    reviewByStatus: {
      PENDING: '0',
      IN_PROGRESS: '0',
      COMPLETED: '0'
    },
    planByStatus: {
      PENDING: '0',
      IN_PROGRESS: '0',
      COMPLETED: '0',
      BLOCKED: '0'
    },
    caseByTestResult: {
      PENDING: '0',
      PASSED: '0',
      NOT_PASSED: '0',
      BLOCKED: '0',
      CANCELED: '0'
    },
    caseByReviewStatus: {
      PENDING: '0',
      PASSED: '0',
      FAILED: '0'
    },
    caseByPriority: {
      HIGHEST: '0',
      HIGH: '0',
      MEDIUM: '0',
      LOW: '0',
      LOWEST: '0'
    }
  };
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
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('functionHome.summary.title') }}</div>
    <div class="flex">
      <Plan :dataSource="dataSource" class="w-1.5/5-media mr-media" />
      <Case :dataSource="dataSource" class="w-2/5-media mr-media" />
      <!-- <priority :dataSource="dataSource" class="w-1.5/5-media" />
       -->
      <Review :dataSource="dataSource" class="w-1.5/5-media mr-media" />
    </div>
  </div>
</template>

<style scoped>
.w-1\.5\/5-media {
  width: calc(30% - 10px);
}

.w-2\/5-media {
  width: calc(40% - 10px);
}

.mr-media {
  margin-right: 15px;
}

/* @media screen and (max-width: 1480px) {
  .w-1\/4-media {
    width: calc(25% - 10px);
  }

  .w-1\/2-media {
    width: calc(50% - 10px);
  }

  .mr-media {
    margin-right: 15px;
  }
} */
</style>
