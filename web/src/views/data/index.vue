<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { utils, appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { ProjectInfo } from '@/layout/types';
import { DataMenuKey, createMenuItems } from '@/views/data/menu';

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
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const activeKey = ref<DataMenuKey>(DataMenuKey.HOME);
const menuItems = createMenuItems(t);

const homepageRefreshNotify = ref<string>('');
let homepageRefreshNotifyFlag = false;

onMounted(() => {
  watch(() => activeKey.value, (newValue) => {
    if (newValue === DataMenuKey.HOME) {
      if (homepageRefreshNotifyFlag) {
        homepageRefreshNotify.value = utils.uuid();
      }
      homepageRefreshNotifyFlag = true;
    }
  }, { immediate: true });
});
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
        v-if="activeKey === DataMenuKey.VARIABLES && projectId"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
    <template #dataset>
      <DataSet
        v-if="activeKey === DataMenuKey.DATASET && projectId"
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
    <template #datasource>
      <SourceData
        v-if="projectId"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>
  </LeftMenu>
</template>
