<script setup lang="ts">
import {onMounted, ref, watch} from 'vue';
import { useI18n } from 'vue-i18n';
import {Icon} from '@xcan-angus/vue-ui';

import {SummaryInfo} from '@/views/task/home/types';

const { t } = useI18n();

// Props definition
type Props = {
  dataSource: SummaryInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

// Reactive counters
const totalSprintCount = ref<string>('0');
const pendingSprintCount = ref<string>('0');
const inProgressSprintCount = ref<string>('0');
const completedSprintCount = ref<string>('0');
const blockedSprintCount = ref<string>('0');

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (newValue === undefined) {
      return;
    }

    // Map props to local refs for rendering
    totalSprintCount.value = newValue.allSprint;
    pendingSprintCount.value = newValue.sprintByStatus?.PENDING;
    inProgressSprintCount.value = newValue.sprintByStatus?.IN_PROGRESS;
    completedSprintCount.value = newValue.sprintByStatus?.COMPLETED;
    blockedSprintCount.value = newValue.sprintByStatus?.BLOCKED;
  }, { immediate: true });
});
</script>

<template>
  <div class="flex flex-col rounded border border-solid border-theme-text-box pt-2 pb-2">
    <div class="font-semibold px-4">
        <span class="mr-2">{{ t('taskHome.summary.sprintCount.totalSprints') }}</span>
      <span class="text-4">{{ totalSprintCount }}</span>
    </div>

    <div class="flex-1 flex items-center justify-center flex-wrap content-center transform-gpu translate-y-2">
      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-daiceshi" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('taskHome.summary.sprintCount.pending') }}</div>
          <div class="text-4">{{ pendingSprintCount }}</div>
        </div>
      </div>

      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-renwuceshizhong" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('taskHome.summary.sprintCount.inProgress') }}</div>
          <div class="text-4">{{ inProgressSprintCount }}</div>
        </div>
      </div>

      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-zusaizhong" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('taskHome.summary.sprintCount.blocked') }}</div>
          <div class="text-4">{{ blockedSprintCount }}</div>
        </div>
      </div>

      <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
        <Icon icon="icon-jiaobenzhuyezhihang" class="text-10 flex-shrink-0" />
        <div class="whitespace-nowrap space-y-1">
          <div class="text-theme-sub-content">{{ t('taskHome.summary.sprintCount.completed') }}</div>
          <div class="text-4">{{ completedSprintCount }}</div>
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
