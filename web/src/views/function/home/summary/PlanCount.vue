<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { SummaryInfo } from '../types';
import { DataSourceProps } from '@/types/types';

const { t } = useI18n();

const props = withDefaults(defineProps<DataSourceProps<SummaryInfo>>(), {
  dataSource: undefined
});

const total = ref<string>('0');
const pendingTotal = ref<string>('0');
const progressingTotal = ref<string>('0');
const completedTotal = ref<string>('0');
const blockedTotal = ref<string>('0');

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (newValue === undefined) {
      return;
    }

    total.value = newValue.allPlan;
    pendingTotal.value = newValue.planByStatus?.PENDING;
    progressingTotal.value = newValue.planByStatus?.IN_PROGRESS;
    completedTotal.value = newValue.planByStatus?.COMPLETED;
    blockedTotal.value = newValue.planByStatus?.BLOCKED;
  }, { immediate: true });
});
</script>

<template>
  <div class="flex flex-col rounded border border-solid border-theme-text-box pt-3.5 pb-2">
    <div class="font-semibold px-4">
      <span class="mr-2">{{ t('functionHome.summary.plan.totalPlan') }}</span>
      <span class="text-4">{{ total }}</span>
    </div>

    <div class="flex-1 flex items-center justify-center flex-wrap content-center transform-gpu translate-y-2">
      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-daiceshi" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('functionHome.summary.plan.pending') }}</div>
          <div class="text-4">{{ pendingTotal }}</div>
        </div>
      </div>

      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-renwuceshizhong" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('functionHome.summary.plan.inProgress') }}</div>
          <div class="text-4">{{ progressingTotal }}</div>
        </div>
      </div>

      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-zusaizhong" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('functionHome.summary.plan.blocked') }}</div>
          <div class="text-4">{{ blockedTotal }}</div>
        </div>
      </div>

      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-ceshitongguo" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('functionHome.summary.plan.completed') }}</div>
          <div class="text-4">{{ completedTotal }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.item-container {
  width: 40%;
}

@media screen and (max-width: 1480px) {
  .item-container {
    width: 50%;
  }
}
</style>
