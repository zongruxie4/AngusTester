<script setup lang="ts">
// ===== IMPORTS =====
import { defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';

// ===== TYPES =====
interface ComponentProps {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  refreshNotify: string;
}

// ===== PROPS =====
const props = withDefaults(defineProps<ComponentProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  refreshNotify: undefined
});

// ===== ASYNC COMPONENTS =====
// Lazy load components for better performance
const MyScenarios = defineAsyncComponent(() => import('@/views/scenario/home/MyScenarios.vue'));
const Chart = defineAsyncComponent(() => import('@/views/scenario/home/chart/index.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/scenario/home/Introduce.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('./ActivityTimeline.vue'));

// ===== REACTIVE STATE =====
// Notification state for refreshing child components
const refreshNotification = ref<string>();

// ===== METHODS =====
/**
 * Updates the refresh notification state
 * This method is provided to child components for triggering refreshes
 * @param value - The notification value to trigger refresh
 */
const updateRefreshNotification = (value: string) => {
  refreshNotification.value = value;
};

// ===== LIFECYCLE HOOKS =====
onMounted(() => {
  // Watch for external refresh notifications and propagate to child components
  watch(() => props.refreshNotify, (newNotificationValue) => {
    // Skip empty or null values
    if (newNotificationValue === undefined || newNotificationValue === null || newNotificationValue === '') {
      return;
    }

    // Update notification state to trigger child component refreshes
    refreshNotification.value = newNotificationValue;
  }, { immediate: true });
});

// ===== PROVIDE/INJECT =====
// Provide refresh notification method to child components
provide('updateRefreshNotify', updateRefreshNotification);
</script>

<template>
  <!-- Main scenario home page layout -->
  <div class="w-full h-full flex items-start px-5 py-5 leading-5 text-3 overflow-auto">
    <!-- Left content area - main scenarios and charts -->
    <div class="flex-1 min-h-full">
      <!-- User's personal scenarios (only shown when project is selected) -->
      <MyScenarios
        v-if="projectId"
        :notify="refreshNotification"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-4" />

      <!-- Project charts and analytics -->
      <Chart
        :notify="refreshNotification"
        :projectId="props.projectId" />
    </div>

    <!-- Right sidebar - introduction and activity timeline -->
    <div class="flex-shrink-0 pt-8 h-full w-right">
      <!-- Product introduction and capabilities -->
      <Introduce class="mb-5" />

      <!-- Activity timeline (only shown when project is selected) -->
      <ActivityTimeline
        v-if="projectId"
        :userInfo="props.userInfo"
        :projectId="props.projectId" />
    </div>
  </div>
</template>

<style scoped>
/* ===== RESPONSIVE SIDEBAR LAYOUT ===== */
/* Default sidebar width for large screens */
.w-right {
  width: 400px;
  margin-left: 48px;
}

/* ===== RESPONSIVE BREAKPOINTS ===== */
/* Small screens - compact sidebar */
@media screen and (max-width: 1480px) {
  .w-right {
    width: 300px;
    margin-left: 20px;
  }
}

/* Medium screens - balanced sidebar */
@media screen and (min-width: 1481px) and (max-width: 1800px) {
  .w-right {
    width: 350px;
    margin-left: 30px;
  }
}
</style>
