<script setup lang="ts">
import { defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  refreshNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  refreshNotify: undefined
});

const MyScenarios = defineAsyncComponent(() => import('@/views/scenario/homepage/myScenarios/index.vue'));
const Chart = defineAsyncComponent(() => import('@/views/scenario/homepage/chart/index.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/scenario/homepage/introduce/index.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('./activityTimeline.vue'));

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
      <MyScenarios
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-4" />
      <Chart :notify="notify" :projectId="props.projectId" />
    </div>

    <div class="flex-shrink-0 pt-8 h-full w-right">
      <Introduce class="mb-5" />
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
