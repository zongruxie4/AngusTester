<script setup lang="ts">
import { defineAsyncComponent, onMounted, provide, ref, watch, inject, Ref } from 'vue';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  refreshNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  refreshNotify: undefined
});
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTaskStatistics: true }));

const MyTasks = defineAsyncComponent(() => import('@/views/task/home/Added.vue'));
const MyCreationSummary = defineAsyncComponent(() => import('@/views/task/home/summary/CreationSummary.vue'));
const Summary = defineAsyncComponent(() => import('@/views/task/home/summary/StatusSummary.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/task/home/WorkCalendar.vue'));
const BurnDownCharts = defineAsyncComponent(() => import('@/views/task/home/BurndownChart.vue'));
const Introduction = defineAsyncComponent(() => import('@/views/task/home/Introduce.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('./ActivityTimeline.vue'));

const notify = ref<string>();

const updateRefreshNotify = (value: string) => {
  notify.value = value;
};

onMounted(() => {
  watch(() => props.refreshNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    notify.value = newValue;
  }, { immediate: true });
});

provide('updateRefreshNotify', updateRefreshNotify);
</script>

<template>
  <div class="w-full h-full flex items-start px-5 py-5 leading-5 text-3 overflow-auto">
    <div class="flex-1 min-h-full">
      <MyTasks
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-4" />
      <div class="flex space-x-5 mb-7.5">
        <BurnDownCharts
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          class="flex-1/3" />
        <WorkCalendar
          class="flex-2/3"
          :notify="notify"
          :userInfo="props.userInfo"
          :projectId="props.projectId" />
      </div>
      <MyCreationSummary
        v-if="proTypeShowMap.showTaskStatistics"
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />
      <Summary
        v-if="proTypeShowMap.showTaskStatistics"
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />
    </div>

    <div class="flex-shrink-0 pt-8 h-full w-right">
      <Introduction class="mb-5" />
      <ActivityTimeline :userInfo="props.userInfo" :projectId="props.projectId" />
    </div>
  </div>
</template>

<style scoped>
.w-right {
  width: 400px;
  margin-left: 48px;
}

@media screen and (max-width: 1480px) {
  .w-right {
    width: 300px;
    margin-left: 20px;
  }
}

@media screen and (min-width: 1481px) and (max-width: 1800px) {
  .w-right {
    width: 350px;
    margin-left: 30px;
  }
}
</style>
