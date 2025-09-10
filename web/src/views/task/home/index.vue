<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, provide, ref, Ref, watch } from 'vue';

/**
 * <p>
 * Props interface for TaskHome component.
 * </p>
 * <p>
 * Defines the required properties for the main task home page.
 * </p>
 */
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

// Inject project type visibility configuration from parent
const projectTypeVisibilityMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({
  showTask: true,
  showBackLog: true,
  showMeeting: true,
  showSprint: true,
  showTaskStatistics: true
}));

// Lazy load components for better performance
const TaskManagement = defineAsyncComponent(() => import('@/views/task/home/Added.vue'));
const TaskCreationSummary = defineAsyncComponent(() => import('@/views/task/home/summary/CreationSummary.vue'));
const TaskStatusSummary = defineAsyncComponent(() => import('@/views/task/home/summary/StatusSummary.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/task/home/WorkCalendar.vue'));
const BurndownChart = defineAsyncComponent(() => import('@/views/task/home/BurndownChart.vue'));
const WelcomeIntroduction = defineAsyncComponent(() => import('@/views/task/home/Introduce.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('@/views/task/home/ActivityTimeline.vue'));

// Internal notification state for component communication
const internalNotification = ref<string>();

/**
 * <p>
 * Updates the internal notification state.
 * </p>
 * <p>
 * This function is provided to child components for triggering data refresh.
 * </p>
 */
const updateRefreshNotification = (value: string) => {
  internalNotification.value = value;
};

onMounted(() => {
  // Watch for external refresh notifications and propagate to internal state
  watch(() => props.refreshNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    internalNotification.value = newValue;
  }, { immediate: true });
});

// Provide refresh notification function to child components
provide('updateRefreshNotify', updateRefreshNotification);
</script>

<template>
  <div class="w-full h-full flex items-start px-5 py-5 leading-5 text-3 overflow-auto">
    <!-- Main content area -->
    <div class="flex-1 min-h-full">
      <!-- Task management section -->
      <TaskManagement
        :notify="internalNotification"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-4" />

      <!-- Charts and calendar section -->
      <div class="flex space-x-5 mb-7.5">
        <BurndownChart
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          class="flex-1/3" />
        <WorkCalendar
          class="flex-2/3"
          :notify="internalNotification"
          :userInfo="props.userInfo"
          :projectId="props.projectId" />
      </div>

      <!-- Task statistics sections (conditionally rendered) -->
      <TaskCreationSummary
        v-if="projectTypeVisibilityMap.showTaskStatistics"
        :notify="internalNotification"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />
      <TaskStatusSummary
        v-if="projectTypeVisibilityMap.showTaskStatistics"
        :notify="internalNotification"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />
    </div>

    <!-- Sidebar area -->
    <div class="flex-shrink-0 pt-8 h-full w-right">
      <WelcomeIntroduction class="mb-5" />
      <ActivityTimeline :userInfo="props.userInfo" :projectId="props.projectId" />
    </div>
  </div>
</template>

<style scoped>
/* Sidebar width and spacing configuration */
.w-right {
  width: 400px;
  margin-left: 48px;
}

/* Responsive design for smaller screens */
@media screen and (max-width: 1480px) {
  .w-right {
    width: 300px;
    margin-left: 20px;
  }
}

/* Responsive design for medium screens */
@media screen and (min-width: 1481px) and (max-width: 1800px) {
  .w-right {
    width: 350px;
    margin-left: 30px;
  }
}
</style>
