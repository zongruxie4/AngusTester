<script setup lang="ts">
import { defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { User, AppInfo } from '@xcan-angus/infra';

type Props = {
  projectId: string;
  userInfo: User;
  appInfo: AppInfo;
  refreshNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  refreshNotify: undefined
});

const MyCreationSummary = defineAsyncComponent(() => import('@/views/data/homepage/summary/index.vue'));
const Statistics = defineAsyncComponent(() => import('./Statistics.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('./ActivityTimeline.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/data/homepage/Introduce.vue'));

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
    <div class="flex-1 min-h-full min-w-0">
      <MyCreationSummary
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId"
        class="mb-7.5" />
      <Statistics
        :notify="notify"
        :userInfo="props.userInfo"
        :projectId="props.projectId" />
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
