<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { utils, appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { ProjectInfo } from '@/layout/types';

const LeftMenu = defineAsyncComponent(() => import('@/components/layout/leftMenu/index.vue'));
const Homepage = defineAsyncComponent(() => import('@/views/data/home/index.vue'));
const Variables = defineAsyncComponent(() => import('@/views/data/variable/index.vue'));
const DataSet = defineAsyncComponent(() => import('@/views/data/dataset/index.vue'));
const FileData = defineAsyncComponent(() => import('@/views/data/file/index.vue'));
const SourceData = defineAsyncComponent(() => import('@/views/data/datasource/index.vue'));

const { t } = useI18n();

const userInfo = ref(appContext.getUser());
const appInfo = ref(appContext.getAccessApp());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));

type MenuKey = 'home' | 'variables' | 'dataSet' | 'file' | 'dataSource';
const activeKey = ref<MenuKey>('home');

const homepageRefreshNotify = ref<string>('');
let homepageRefreshNotifyFlag = false;

onMounted(() => {
  watch(() => activeKey.value, (newValue) => {
    if (newValue === 'home') {
      if (homepageRefreshNotifyFlag) {
        homepageRefreshNotify.value = utils.uuid();
      }

      homepageRefreshNotifyFlag = true;
    }
  }, { immediate: true });
});

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const menuItems = [
  { name: t('home.title'), icon: 'icon-zhuye', key: 'home' },
  { name: t('variable.title'), icon: 'icon-bianliang1', key: 'variables' },
  { name: t('dataset.title'), icon: 'icon-shujuji', key: 'dataSet' },
  { name: t('file.title'), icon: 'icon-wenjian1', key: 'file' },
  { name: t('datasource.title'), icon: 'icon-shujuyuan', key: 'dataSource' }
];
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menuItems">
    <template #home>
      <Homepage
        v-if="projectId"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="homepageRefreshNotify" />
    </template>
    <template #variables>
      <Variables
        v-if="activeKey === 'variables' && projectId"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
    <template #dataSet>
      <DataSet
        v-if="activeKey === 'dataSet' && projectId"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
    <template #file>
      <FileData
        v-if="projectId"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
    <template #dataSource>
      <SourceData
        v-if="projectId"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
  </LeftMenu>
</template>
