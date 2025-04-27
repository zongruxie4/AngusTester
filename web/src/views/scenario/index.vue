<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';

import LeftMenu from '@/components/layout/leftMenu/index.vue';
import { utils } from '@xcan-angus/tools';

type MenuKey = 'homepage' | 'scenario' | 'trash' | 'monitor';

const Homepage = defineAsyncComponent(() => import('@/views/scenario/homepage/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/scenario/trash/index.vue'));
const Scenario = defineAsyncComponent(() => import('@/views/scenario/scenario/index.vue'));
const Monitor = defineAsyncComponent(() => import('@/views/scenario/monitor/index.vue'));

const userInfo = inject<Ref<{ id: string }>>('tenantInfo');
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = inject<Ref<{ id: string; name: string; }>>('appInfo', ref({ id: '', name: '' }));

const activeKey = ref<MenuKey>();

const menuItems: {
  icon: string;
  name: string;
  key: MenuKey;
}[] = [
  { icon: 'icon-zhuye', name: '主页', key: 'homepage' },
  { icon: 'icon-changjingguanli', name: '场景', key: 'scenario' },
  { icon: 'icon-jiankong2', name: '监控', key: 'monitor' },
  { icon: 'icon-qingchu', name: '回收站', key: 'trash' }
];

const homepageRefreshNotify = ref<string>('');
const trashRefreshNotify = ref<string>('');
const scenesRefreshNotify = ref<string>('');

let homepageRefreshNotifyFlag = false;
let trashRefreshNotifyFlag = false;
let scenesRefreshNotifyFlag = false;

const projectId = computed(() => {
  return projectInfo.value?.id;
});

onMounted(() => {
  watch(() => activeKey.value, (newValue) => {
    if (newValue === 'homepage') {
      if (homepageRefreshNotifyFlag) {
        homepageRefreshNotify.value = utils.uuid();
      }

      homepageRefreshNotifyFlag = true;
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
      if (scenesRefreshNotifyFlag) {
        scenesRefreshNotify.value = utils.uuid();
      }

      scenesRefreshNotifyFlag = true;
    }
  }, { immediate: true });
});
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menuItems">
    <template #homepage>
      <Homepage
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="homepageRefreshNotify" />
    </template>
    <template #scenario>
      <Scenario
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="scenesRefreshNotify" />
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
