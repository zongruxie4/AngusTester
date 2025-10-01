<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';
import { appContext, utils, EditionType } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import LeftMenu from '@/components/layout/leftMenu/index.vue';

// Type Definitions
type MenuKey = 'homepage' | 'sprint' | 'task' | 'backlog' | 'trash' | 'meeting' | 'analysis';

// Component Imports
const Homepage = defineAsyncComponent(() => import('@/views/task/home/index.vue'));
const Backlog = defineAsyncComponent(() => import('@/views/task/backlog/index.vue'));
const Sprint = defineAsyncComponent(() => import('@/views/task/sprint/index.vue'));
const Task = defineAsyncComponent(() => import('@/views/task/task/index.vue'));
const Meeting = defineAsyncComponent(() => import('@/views/task/meeting/index.vue'));
const Analysis = defineAsyncComponent(() => import('@/views/task/analysis/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/task/trash/index.vue'));

// Composables and Context
const { t } = useI18n();

// Reactive State
const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = ref(appContext.getAccessApp());
const projectTypeVisibilityMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({
  showTask: true,
  showBackLog: true,
  showMeeting: true,
  showSprint: true,
  showTasStatistics: true
}));

const activeKey = ref<MenuKey>();
const editionType = ref();

// Refresh Notification State
const homepageRefreshToken = ref<string>('');
const trashRefreshToken = ref<string>('');
const sprintsRefreshToken = ref<string>('');
const tasksRefreshToken = ref<string>('');

// Track whether refresh notifications have been triggered for each section
let hasHomepageRefreshTriggered = false;
let hasTrashRefreshTriggered = false;
let hasSprintsRefreshTriggered = false;
let hasTasksRefreshTriggered = false;

/**
 * <p>Computed property that returns the current project ID.</p>
 * <p>Used to pass project context to child components.</p>
 */
const projectId = computed(() => {
  return projectInfo.value?.id;
});

/**
 * <p>Generates the menu items array based on project type visibility settings and edition type.</p>
 * <p>Filters out menu items that should not be displayed based on configuration.</p>
 */
const menuItems = computed(() => {
  const allMenuItems = [
    {
      name: t('home.title'),
      icon: 'icon-zhuye',
      key: 'homepage'
    },
    projectTypeVisibilityMap.value.showBackLog
      ? {
          name: t('backlog.title'),
          icon: 'icon-backlog',
          key: 'backlog'
        }
      : null,
    projectTypeVisibilityMap.value.showSprint
      ? {
          name: t('taskSprint.title'),
          icon: 'icon-diedai',
          key: 'sprint'
        }
      : null,
    {
      name: t('task.title'),
      icon: 'icon-renwu2',
      key: 'task'
    },
    projectTypeVisibilityMap.value.showMeeting
      ? {
          name: t('taskMeeting.title'),
          icon: 'icon-RT',
          key: 'meeting'
        }
      : null,
    editionType.value !== EditionType.COMMUNITY
      ? {
          name: t('taskAnalysis.title'),
          icon: 'icon-fenxi',
          key: 'analysis'
        }
      : null,
    {
      name: t('trash.title'),
      icon: 'icon-qingchu',
      key: 'trash'
    }
  ];

  return allMenuItems.filter((item): item is NonNullable<typeof item> => item !== null);
});

/**
 * <p>Handles the refresh notification logic when switching between menu sections.</p>
 * <p>Generates unique refresh tokens to trigger child component updates.</p>
 * <p>Uses flags to ensure refresh only happens after the first visit to each section.</p>
 */
const handleMenuSectionChange = (newMenuKey: MenuKey | undefined) => {
  if (!newMenuKey) return;

  if (newMenuKey === 'homepage') {
    if (hasHomepageRefreshTriggered) {
      homepageRefreshToken.value = utils.uuid();
    }
    hasHomepageRefreshTriggered = true;
    return;
  }

  if (newMenuKey === 'trash') {
    if (hasTrashRefreshTriggered) {
      trashRefreshToken.value = utils.uuid();
    }
    hasTrashRefreshTriggered = true;
    return;
  }

  if (newMenuKey === 'sprint') {
    if (hasSprintsRefreshTriggered) {
      sprintsRefreshToken.value = utils.uuid();
    }
    hasSprintsRefreshTriggered = true;
    return;
  }

  if (newMenuKey === 'task') {
    if (hasTasksRefreshTriggered) {
      tasksRefreshToken.value = utils.uuid();
    }
    hasTasksRefreshTriggered = true;
  }
};

onMounted(async () => {
  // Initialize edition type from application context
  editionType.value = appContext.getEditionType();

  // Watch for menu key changes and handle refresh notifications
  watch(() => activeKey.value, handleMenuSectionChange, { immediate: true });
});
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menuItems">
    <template #homepage>
      <Homepage
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="homepageRefreshToken" />
    </template>
    <template #backlog>
      <Backlog
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="tasksRefreshToken" />
    </template>
    <template #sprint>
      <Sprint
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="sprintsRefreshToken" />
    </template>
    <template #task>
      <Task
        :projectId="projectId"
        :projectName="projectInfo?.name"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="tasksRefreshToken" />
    </template>
    <template #meeting>
      <Meeting
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="sprintsRefreshToken" />
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
        :refreshNotify="trashRefreshToken" />
    </template>
  </LeftMenu>
</template>
