<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
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
    creatorObjectType: 'USER',
    creatorObjectId: props.userInfo?.id
  };
  const [error, res] = await analysis.getFuncResourceCount(params);
  loading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    const data = res.data as ResourceInfo;

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
      <div class="rounded flex-1 relative">
        <div class="vertical-layout-top yellow-top">
          <div>{{ t('functionHome.myCreationSummary.plan') }}</div>
          <div>{{ allPlan }}</div>
        </div>
        <div class="vertical-layout-bottom yellow-bottom">
          <div>
            <div>{{ t('functionHome.myCreationSummary.last7Days') }}</div>
            <div>{{ planByLastWeek }}</div>
          </div>
          <div>
            <div>{{ t('functionHome.myCreationSummary.last30Days') }}</div>
            <div>{{ planByLastMonth }}</div>
          </div>
        </div>
        <img src="./images/icon-2.png" class="w-15 absolute right-0 top-0" />
      </div>

      <div class="rounded flex-1 relative">
        <div class="vertical-layout-top red-top">
          <div>{{ t('functionHome.myCreationSummary.case') }}</div>
          <div>{{ allCase }}</div>
        </div>
        <div class="vertical-layout-bottom red-bottom">
          <div>
            <div>{{ t('functionHome.myCreationSummary.last7Days') }}</div>
            <div>{{ caseByLastWeek }}</div>
          </div>
          <div>
            <div>{{ t('functionHome.myCreationSummary.last30Days') }}</div>
            <div>{{ caseByLastMonth }}</div>
          </div>
        </div>
        <img src="./images/icon-3.png" class="w-15 absolute right-0 top-0" />
      </div>

      <div class="rounded flex-1 relative">
        <div class="vertical-layout-top blue-top">
          <div>{{ t('functionHome.myCreationSummary.review') }}</div>
          <div>{{ allReview }}</div>
        </div>
        <div class="vertical-layout-bottom blue-bottom">
          <div>
            <div>{{ t('functionHome.myCreationSummary.last7Days') }}</div>
            <div>{{ reviewByLastWeek }}</div>
          </div>
          <div>
            <div>{{ t('functionHome.myCreationSummary.last30Days') }}</div>
            <div>{{ reviewByLastMonth }}</div>
          </div>
        </div>
        <img src="./images/icon-1.png" class="w-15 absolute right-0 top-0" />
      </div>

      <div class="rounded flex-1 relative">
        <div class="vertical-layout-top green-top">
          <div>{{ t('functionHome.myCreationSummary.baseline') }}</div>
          <div>{{ allBaseline }}</div>
        </div>
        <div class="vertical-layout-bottom green-bottom">
          <div>
            <div>{{ t('functionHome.myCreationSummary.last7Days') }}</div>
            <div>{{ baselineByLastWeek }}</div>
          </div>
          <div>
            <div>{{ t('functionHome.myCreationSummary.last30Days') }}</div>
            <div>{{ baselineByLastMonth }}</div>
          </div>
        </div>
        <img src="./images/icon-4.png" class="w-15 absolute right-0 top-0" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.vertical-layout-top {
  padding: 14px;
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}

.vertical-layout-bottom {
  padding: 14px;
  border-bottom-right-radius: 4px;
  border-bottom-left-radius: 4px;
}

.vertical-layout-top>div:first-child {
  margin-bottom: 4px
}

.vertical-layout-bottom>div:first-child {
  margin-bottom: 4px
}

.vertical-layout-bottom>div>div:first-child {
  margin-bottom: 4px
}

@media (min-width: 1400px) {
  .vertical-layout-top {
    display: flex;
    align-items: center;
  }

  .vertical-layout-bottom>div {
    display: flex;
    align-items: center;
  }

  .vertical-layout-top>div:first-child {
    margin-right: 10px;
    margin-bottom: 0;
  }

  .vertical-layout-bottom>div:first-child {
    margin-bottom: 4px
  }

  .vertical-layout-bottom>div>div:first-child {
    margin-right: 10px;
    margin-bottom: 0;
  }
}

@media (min-width: 1600px) {
  .vertical-layout-top {
    display: flex;
    align-items: center;
  }

  .vertical-layout-bottom {
    display: flex;
    align-items: center;
  }

  .vertical-layout-bottom>div {
    display: flex;
    align-items: center;
    width: 50%
  }

  .vertical-layout-top>div:first-child {
    margin-right: 10px;
    margin-bottom: 0;
  }

  .vertical-layout-bottom>div:first-child {
    margin-bottom: 0
  }

  .vertical-layout-bottom>div>div:first-child {
    margin-right: 10px;
    margin-bottom: 0;
  }
}

.yellow-top {
  background: linear-gradient(to right, #f7cb71, #fef7df);
}

.yellow-bottom {
  background-color: #fef7df;
}

.red-top {
  background: linear-gradient(to right, #ffb99f, #fff0e8);
}

.red-bottom {
  background-color: #fff0e8;
}

.blue-top {
  background: linear-gradient(to right, #d1e7ff, #eff6ff);
}

.blue-bottom {
  background-color: #eff6ff;
}

.green-top {
  background: linear-gradient(to right, #87d7ee, #e2f7fd);
}

.green-bottom {
  background-color: #e2f7fd;
}
</style>
