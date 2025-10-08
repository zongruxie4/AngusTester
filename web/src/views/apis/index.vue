<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, provide, reactive, Ref, ref, watch } from 'vue';
import { utils, appContext } from '@xcan-angus/infra';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ProjectInfo } from '@/layout/types';

import LeftMenu from '@/components/layout/leftMenu/index.vue';

const Homepage = defineAsyncComponent(() => import('@/views/apis/home/index.vue'));
const Services = defineAsyncComponent(() => import('@/views/apis/services/index.vue'));
const Share = defineAsyncComponent(() => import('@/views/apis/share/index.vue'));
const Server = defineAsyncComponent(() => import('@/views/apis/server/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/apis/trash/index.vue'));
const Design = defineAsyncComponent(() => import('@/views/apis/design/index.vue'));
const Mock = defineAsyncComponent(() => import('@/views/apis/mock/index.vue'));

type MenuKey = 'home' | 'services' | 'trash';

const activeKey = ref<MenuKey>();
const servicesRef = ref();
const router = useRouter();
const { t } = useI18n();

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());

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
    if (newValue === 'home') {
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

provide('addTabPane', (params) => {
  router.replace('/apis#services');
  nextTick(() => {
    servicesRef.value.addTabPane(params);
  });
});

provide('deleteTabPane', (params) => servicesRef.value && servicesRef.value.deleteTabPane(params));

provide('updateTabPane', (params) => servicesRef.value && servicesRef.value.updateTabPane(params));

provide('replaceTabPane', (params) => servicesRef.value && servicesRef.value.replaceTabPane(params));

provide('updateApiGroup', (params) => servicesRef.value && servicesRef.value.updateApiGroup(params));

const menuItems = [
  { name: t('home.title'), icon: 'icon-zhuye', key: 'home' },
  { name: t('service.title'), icon: 'icon-fuwuxinxi', key: 'services' },
  { name: 'Mock', icon: 'icon-fuwuxinxi', key: 'mock' },
  { name: t('design.title'), icon: 'icon-sheji', key: 'design' },
  { name: t('apiShare.title'), icon: 'icon-fenxiang', key: 'share' },
  { name: t('server.title'), icon: 'icon-host', key: 'server' },
  { name: t('trash.title'), icon: 'icon-qingchu', key: 'trash' }
];
</script>

<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menuItems">
    <template #home>
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
    <template #mock>
      <Mock
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
        :appInfo="appInfo" />
    </template>
  </LeftMenu>
</template>
