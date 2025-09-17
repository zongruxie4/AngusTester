<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';
import { appContext, utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import LeftMenu from '@/components/layout/leftMenu/index.vue';

type MenuKey = 'homepage' | 'sprint' | 'task' | 'backlog' | 'trash' | 'meeting' | 'analysis';

const Homepage = defineAsyncComponent(() => import('@/views/task/home/index.vue'));
const Backlog = defineAsyncComponent(() => import('@/views/task/backlog/index.vue'));
const Sprint = defineAsyncComponent(() => import('@/views/task/sprint/index.vue'));
const Task = defineAsyncComponent(() => import('@/views/task/task/index.vue'));
const Meeting = defineAsyncComponent(() => import('@/views/task/meeting/index.vue'));
const Analysis = defineAsyncComponent(() => import('@/views/task/analysis/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/task/trash/index.vue'));

const { t } = useI18n();

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = ref(appContext.getAccessApp());
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

const activeKey = ref<MenuKey>();
const editionType = ref();

const menus = computed(() => {
  return [
    {
      name: t('taskHome.name'),
      icon: 'icon-zhuye',
      key: 'homepage'
    },
    proTypeShowMap.value.showBackLog &&
    {
      name: t('taskHome.backlogName'),
      icon: 'icon-backlog',
      key: 'backlog'
    },
    proTypeShowMap.value.showSprint &&
    {
      name: t('taskSprint.name'),
      icon: 'icon-diedai',
      key: 'sprint'
    },
    {
      name: t('task.name'),
      icon: 'icon-renwu2',
      key: 'task'
    },
    proTypeShowMap.value.showMeeting &&
    {
      name: t('taskMeeting.name'),
      icon: 'icon-RT',
      key: 'meeting'
    },
    editionType.value !== 'COMMUNITY' &&
    {
      name: t('taskAnalysis.name'),
      icon: 'icon-fenxi',
      key: 'analysis'
    },
    {
      name: t('taskTrash.name'),
      icon: 'icon-qingchu',
      key: 'trash'
    }
  ].filter(Boolean);
});

const homepageRefreshNotify = ref<string>('');
const trashRefreshNotify = ref<string>('');
const sprintsRefreshNotify = ref<string>('');
const tasksRefreshNotify = ref<string>('');

let homepageRefreshNotifyFlag = false;
let trashRefreshNotifyFlag = false;
let sprintsRefreshNotifyFlag = false;
let tasksRefreshNotifyFlag = false;

const projectId = computed(() => {
  return projectInfo.value?.id;
});

onMounted(async () => {
  editionType.value = appContext.getEditionType();
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

    if (newValue === 'sprint') {
      if (sprintsRefreshNotifyFlag) {
        sprintsRefreshNotify.value = utils.uuid();
      }

      sprintsRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'task') {
      if (tasksRefreshNotifyFlag) {
        tasksRefreshNotify.value = utils.uuid();
      }

      tasksRefreshNotifyFlag = true;
    }
  }, { immediate: true });
});
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menus">
    <template #homepage>
      <Homepage
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="homepageRefreshNotify" />
    </template>
    <template #backlog>
      <Backlog
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="tasksRefreshNotify" />
    </template>
    <template #sprint>
      <Sprint
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="sprintsRefreshNotify" />
    </template>
    <template #task>
      <Task
        :projectId="projectId"
        :projectName="projectInfo?.name"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="tasksRefreshNotify" />
    </template>
    <template #meeting>
      <Meeting
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="sprintsRefreshNotify" />
    </template>
    <template #analysis>
      <Analysis
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
  </LeftMenu>
</template>
