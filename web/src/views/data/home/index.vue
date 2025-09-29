<script lang="ts" setup>
import { defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { BasicProps } from '@/types/types';

/**
 * <p>Component props definition</p>
 * <p>Defines the interface for component properties</p>
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  refreshNotify: undefined
});

// Async component imports for better performance
const Introduce = defineAsyncComponent(() => import('@/views/data/home/Introduce.vue'));
const Added = defineAsyncComponent(() => import('@/views/data/home/Added.vue'));
const Statistics = defineAsyncComponent(() => import('./Statistics.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('./ActivityTimeline.vue'));

// Component state
const notify = ref<string>();

/**
 * <p>Update refresh notification value</p>
 * <p>Updates the notification state and triggers component refresh</p>
 */
const updateRefreshNotify = (value: string): void => {
  notify.value = value;
};

// Watch for refresh notification changes from parent
onMounted(() => {
  watch(() => props.refreshNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    notify.value = newValue;
  }, { immediate: true });
});

// Provide update function to child components
provide('updateRefreshNotify', updateRefreshNotify);
</script>

<template>
  <div class="w-full h-full flex items-start px-5 py-5 leading-5 text-3 overflow-auto">
    <!-- Main Content Area -->
    <div class="flex-1 min-h-full min-w-0">
      <!-- My Creation Summary Section -->
      <Added
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />

      <!-- Statistics Section -->
      <Statistics
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId" />
    </div>

    <!-- Right Sidebar -->
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
