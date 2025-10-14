<script setup lang="ts">
import { defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { BasicProps } from '@/types/types';

// Props interface for TaskHome component
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  refreshNotify: undefined
});

// Lazy load components for better performance
const Added = defineAsyncComponent(() => import('@/views/test/home/Added.vue'));
const CreationSummary = defineAsyncComponent(() => import('@/views/test/home/CreationSummary.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/test/home/WorkCalendar.vue'));
const BurnDownCharts = defineAsyncComponent(() => import('@/views/test/home/BurndownChart.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/test/home/Introduce.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('@/views/test/home/ActivityTimeline.vue'));

// Internal notification state for component communication
const internalNotification = ref<string>();

/**
 * Updates the internal notification state.
 */
const updateRefreshNotification = (value: string) => {
  internalNotification.value = value;
};

// Provide refresh notification function to child components
provide('updateRefreshNotify', updateRefreshNotification);

onMounted(() => {
  watch(() => props.refreshNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }
    internalNotification.value = newValue;
  }, { immediate: true });
});
</script>

<template>
  <div class="w-full h-full flex items-start px-5 py-5 leading-5 text-3 overflow-auto">
    <!-- Main content area -->
    <div class="flex-1 min-h-full">
      <!-- Case management section -->
      <Added
        v-if="projectId"
        :notify="internalNotification"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />

      <!-- Case statistics sections (conditionally rendered) -->
      <CreationSummary
        v-if="projectId"
        :notify="internalNotification"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />

      <!-- <StatusSummary
        v-if="projectId"
        :notify="internalNotification"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />-->

      <!-- Charts and calendar section -->
      <div class="flex mb-7.5 space-x-5">
        <BurnDownCharts
          v-if="projectId"
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          class="flex-1/3" />
        <WorkCalendar
          v-if="projectId"
          class="flex-2/3"
          :notify="internalNotification"
          :userInfo="props.userInfo"
          :projectId="props.projectId" />
      </div>
    </div>

    <!-- Sidebar area -->
    <div class="flex-shrink-0 pt-8 h-full w-right">
      <!-- Introduction Section -->
      <Introduce class="mb-5" />

      <!-- Activity Timeline Section -->
      <ActivityTimeline
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
