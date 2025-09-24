<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import { SummaryInfo } from '@/views/task/home/types';
import { DataSourceProps } from '@/types/types';

const { t } = useI18n();

// Props definition
const props = withDefaults(defineProps<DataSourceProps<SummaryInfo>>(), {
  dataSource: undefined
});

const ChartPie = defineAsyncComponent(() => import('./TaskPie.vue'));

// Reactive counters
const totalTaskCount = ref<string>('0');
const overdueTaskCount = ref<string>('0');
const pendingTaskCount = ref<string>('0');
const inProgressTaskCount = ref<string>('0');
const completedTaskCount = ref<string>('0');
const canceledTaskCount = ref<string>('0');
const confirmingTaskCount = ref<string>('0');

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (newValue === undefined) {
      return;
    }

    // Map props to local refs for rendering
    totalTaskCount.value = newValue.allTask;
    overdueTaskCount.value = newValue.taskByOverdue;
    pendingTaskCount.value = newValue.taskByStatus?.PENDING;
    inProgressTaskCount.value = newValue.taskByStatus?.IN_PROGRESS;
    completedTaskCount.value = newValue.taskByStatus?.COMPLETED;
    canceledTaskCount.value = newValue.taskByStatus?.CANCELED;
    confirmingTaskCount.value = newValue.taskByStatus?.CONFIRMING;
  }, { immediate: true });
});
</script>

<template>
  <div class="rounded border border-solid border-theme-text-box pt-2">
    <div class="font-semibold px-4">
      <span class="mr-2">{{ t('taskHome.summary.taskCount.totalTasks') }}</span>
      <span class="text-4">{{ totalTaskCount }}</span>
    </div>
    <div class="flex items-center pr-2">
      <div class="flex-1 flex items-center justify-center flex-wrap content-center transform-gpu translate-y-2">
        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-daiceshi" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">{{ t('status.pending') }}</div>
            <div class="text-4">{{ pendingTaskCount }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-daiqueren" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">{{ t('status.pendingConfirmation') }}</div>
            <div class="text-4">{{ confirmingTaskCount }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-yiyuqi1" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">{{ t('status.overdue') }}<span class="inline-block" style="width:1em;"></span></div>
            <div class="text-4">{{ overdueTaskCount }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-renwuceshizhong" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">{{ t('status.inProgress') }}</div>
            <div class="text-4">{{ inProgressTaskCount }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-jiaobenzhuyezhihang" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">{{ t('status.completed') }}</div>
            <div class="text-4">{{ completedTaskCount }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-yiquxiao" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">{{ t('status.cancelled') }}</div>
            <div class="text-4">{{ canceledTaskCount }}</div>
          </div>
        </div>
      </div>

      <ChartPie :dataSource="props.dataSource" class="flex-1" />
    </div>
  </div>
</template>

<style scoped>
.item-container {
  width: 33.33%;
}

@media screen and (max-width: 1800px) {
  .item-container {
    width: 50%;
  }
}
</style>
