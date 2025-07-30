<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';

import LeftMenu from '@/components/layout/leftMenu/index.vue';

type MenuKey = 'homepage' | 'variables' | 'dataSet' | 'file' | 'dataSource';

const Homepage = defineAsyncComponent(() => import('@/views/data/homepage/index.vue'));
const Variables = defineAsyncComponent(() => import('@/views/data/variable/index.vue'));
const DataSet = defineAsyncComponent(() => import('@/views/data/dataset/index.vue'));
const FileData = defineAsyncComponent(() => import('@/views/data/file/index.vue'));
const SourceData = defineAsyncComponent(() => import('@/views/data/datasource/index.vue'));

const userInfo = inject<Ref<{ id: string }>>('tenantInfo');
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = inject<Ref<{ id: string; name: string; }>>('appInfo', ref({ id: '', name: '' }));

const activeKey = ref<MenuKey>('homepage');

const homepageRefreshNotify = ref<string>('');
let homepageRefreshNotifyFlag = false;

onMounted(() => {
  watch(() => activeKey.value, (newValue) => {
    if (newValue === 'homepage') {
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
  { name: '主页', icon: 'icon-zhuye', key: 'homepage' },
  { name: '变量', icon: 'icon-bianliang1', key: 'variables' },
  { name: '数据集', icon: 'icon-shujuji', key: 'dataSet' },
  { name: '文件', icon: 'icon-wenjian1', key: 'file' },
  { name: '数据源', icon: 'icon-shujuyuan', key: 'dataSource' }
];
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menuItems">
    <template #homepage>
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
