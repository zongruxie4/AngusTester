<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, provide, reactive, Ref, ref, watch } from 'vue';
import { utils } from '@xcan-angus/tools';
import { useRouter } from 'vue-router';

import LeftMenu from '@/components/layout/leftMenu/index.vue';
import { nextTick } from 'process';

type MenuKey = 'homepage' | 'services' | 'trash';

const Homepage = defineAsyncComponent(() => import('@/views/apis/homepage/index.vue'));
const Services = defineAsyncComponent(() => import('@/views/apis/services/index.vue'));
const Share = defineAsyncComponent(() => import('@/views/apis/share/index.vue'));
const Server = defineAsyncComponent(() => import('@/views/apis/server/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/apis/trash/index.vue'));
const Design = defineAsyncComponent(() => import('@/views/apis/design/index.vue'));

const activeKey = ref<MenuKey>();
const servicesRef = ref();
const router = useRouter();

const userInfo = inject<Ref<{ id: string }>>('tenantInfo');
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = inject<Ref<{ id: string; name: string; }>>('appInfo', ref({ id: '', name: '' }));

const projectId = computed(() => {
  return projectInfo.value?.id;
});

// 需要更新项目列表上的项目名称
const updateProjectInfo = reactive({
  id: '',
  name: ''
});

const homepageRefreshNotify = ref<string>('');
const trashRefreshNotify = ref<string>('');
const servicesRefreshNotify = ref<string>('');

let homepageRefreshNotifyFlag = false;
let trashRefreshNotifyFlag = false;
let servicesRefreshNotifyFlag = false;

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

    if (newValue === 'services') {
      if (servicesRefreshNotifyFlag) {
        servicesRefreshNotify.value = utils.uuid();
      }

      servicesRefreshNotifyFlag = true;
    }
  }, { immediate: true });
});

provide('updateProjectInfo', updateProjectInfo);

// 添加指定的tabPane
provide('addTabPane', (params) => {
  router.replace('/apis#services');
  nextTick(() => {
    servicesRef.value.addTabPane(params);
  });
});

// 删除指定的tabPane
provide('deleteTabPane', (params) => servicesRef.value && servicesRef.value.deleteTabPane(params));

// 更新指定的tabPane
provide('updateTabPane', (params) => servicesRef.value && servicesRef.value.updateTabPane(params));

// 替换指定tabPane
provide('replaceTabPane', (params) => servicesRef.value && servicesRef.value.replaceTabPane(params));

provide('updateApiGroup', (params) => servicesRef.value && servicesRef.value.updateApiGroup(params));

const menuItems = [
  { name: '主页', icon: 'icon-zhuye', key: 'homepage' },
  { name: '服务', icon: 'icon-fuwuxinxi', key: 'services' },
  { name: '分享', icon: 'icon-fenxiang', key: 'share' },
  { name: '服务器', icon: 'icon-host', key: 'server' },
  { name: '回收站', icon: 'icon-qingchu', key: 'trash' },
  { name: '设计', icon: 'icon-design', key: 'design'}
];
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
    <template #services>
      <Services
        ref="servicesRef"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="homepageRefreshNotify" />
    </template>
    <template #share>
      <Share
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
    <template #server>
      <Server
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
    <template #trash>
      <Trash
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="trashRefreshNotify" />
    </template>
    <template #design>
      <Design
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"/>
    </template>
  </LeftMenu>
</template>
