<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext, utils } from '@xcan-angus/infra';
import { ProjectInfo } from '@/layout/types';

const LeftMenu = defineAsyncComponent(() => import('@/components/layout/leftMenu/index.vue'));
const Homepage = defineAsyncComponent(() => import('@/views/scenario/home/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/scenario/trash/index.vue'));
const Scenario = defineAsyncComponent(() => import('@/views/scenario/scenario/index.vue'));
const Monitor = defineAsyncComponent(() => import('@/views/scenario/monitor/index.vue'));

const { t } = useI18n();

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());

type MenuKey = 'home' | 'scenario' | 'trash' | 'monitor';
const activeKey = ref<MenuKey>();

const menuItems: {
  icon: string;
  name: string;
  key: MenuKey;
}[] = [
  { icon: 'icon-zhuye', name: t('home.title'), key: 'home' },
  { icon: 'icon-changjingguanli', name: t('scenario.title'), key: 'scenario' },
  { icon: 'icon-jiankong2', name: t('scenarioMonitor.title'), key: 'monitor' },
  { icon: 'icon-qingchu', name: t('trash.title'), key: 'trash' }
];

const homeRefreshNotify = ref<string>('');
const trashRefreshNotify = ref<string>('');
const scenarioRefreshNotify = ref<string>('');

let homeRefreshNotifyFlag = false;
let trashRefreshNotifyFlag = false;
let scenarioRefreshNotifyFlag = false;

const projectId = computed(() => {
  return projectInfo.value?.id;
});

onMounted(() => {
  watch(() => activeKey.value, (newValue) => {
    if (newValue === 'home') {
      if (homeRefreshNotifyFlag) {
        homeRefreshNotify.value = utils.uuid();
      }

      homeRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'trash') {
      if (trashRefreshNotifyFlag) {
        trashRefreshNotify.value = utils.uuid();
      }

      trashRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'scenario') {
      if (scenarioRefreshNotifyFlag) {
        scenarioRefreshNotify.value = utils.uuid();
      }

      scenarioRefreshNotifyFlag = true;
    }
  }, { immediate: true });
});
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menuItems">
    <template #home>
      <Homepage
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="homeRefreshNotify" />
    </template>
    <template #scenario>
      <Scenario
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="scenarioRefreshNotify" />
    </template>
    <template #trash>
      <Trash
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="trashRefreshNotify" />
    </template>
    <template #monitor>
      <Monitor
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
  </LeftMenu>
</template>
