<script setup lang="ts">
import { defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { BasicProps } from '@/types/types';

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  refreshNotify: undefined
});

const notify = ref<string>();

const updateRefreshNotify = (value: string) => {
  notify.value = value;
};

const Added = defineAsyncComponent(() => import('@/views/function/home/Added.vue'));
const CreationSummary = defineAsyncComponent(() => import('@/views/function/home/summary/CreationSummary.vue'));
const Summary = defineAsyncComponent(() => import('@/views/function/home/summary/StatusSummary.vue'));
const Introduction = defineAsyncComponent(() => import('@/views/function/home/Introduce.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/function/home/WorkCalendar.vue'));
const BurnDownCharts = defineAsyncComponent(() => import('@/views/function/home/BurndownChart.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('@/views/function/home/ActivityTimeline.vue'));

const homepageRef = ref();

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
  <div ref="homepageRef" class="w-full h-full flex px-5 py-5 leading-5 text-3 overflow-auto">
    <div class="flex-1 min-h-full">
      <Added
        v-if="projectId"
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />

      <div class="flex mb-7.5 space-x-5">
        <BurnDownCharts
          v-if="projectId"
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          class="flex-1/3" />

        <WorkCalendar
          v-if="projectId"
          class="flex-2/3"
          :notify="notify"
          :userInfo="props.userInfo"
          :projectId="props.projectId" />
      </div>

      <CreationSummary
        v-if="projectId"
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />

      <Summary
        v-if="projectId"
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />
    </div>

    <div class="flex-shrink-0 pt-8 h-full w-right">
      <Introduction class="mb-5" />
      <ActivityTimeline
        v-if="projectId"
        :userInfo="props.userInfo"
        :projectId="props.projectId" />
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
