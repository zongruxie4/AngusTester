<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';
import { appContext, utils, EditionType } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { ProjectInfo } from '@/layout/types';
import { IssueMenuKey, createMenuItems, IssueMenuVisibility } from '@/views/issue/menu';

// Component Imports
const LeftMenu = defineAsyncComponent(() => import('@/components/layout/leftMenu/index.vue'));
const Homepage = defineAsyncComponent(() => import('@/views/issue/home/index.vue'));
const Backlog = defineAsyncComponent(() => import('@/views/issue/backlog/index.vue'));
const Sprint = defineAsyncComponent(() => import('@/views/issue/sprint/index.vue'));
const Task = defineAsyncComponent(() => import('@/views/issue/issue/index.vue'));
const Meeting = defineAsyncComponent(() => import('@/views/issue/meeting/index.vue'));
const Analysis = defineAsyncComponent(() => import('@/views/issue/analysis/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/issue/trash/index.vue'));

// Composables and Context
const { t } = useI18n();

// Reactive State
const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());
const projectTypeVisibilityMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({
  showTask: true,
  showBackLog: true,
  showMeeting: true,
  showSprint: true,
  showTasStatistics: true
}));

// Type Definitions
const editionType = ref<EditionType>();

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

const activeKey = ref<IssueMenuKey>();

/**
 * <p>Generates the menu items array based on project type visibility settings and edition type.</p>
 * <p>Filters out menu items that should not be displayed based on configuration.</p>
 */
const menuItems = computed(() => {
  const visibility: IssueMenuVisibility = projectTypeVisibilityMap.value as IssueMenuVisibility;
  return createMenuItems(t, visibility, editionType.value);
});

/**
 * <p>Handles the refresh notification logic when switching between menu sections.</p>
 * <p>Generates unique refresh tokens to trigger child component updates.</p>
 * <p>Uses flags to ensure refresh only happens after the first visit to each section.</p>
 */
const handleMenuSectionChange = (newMenuKey: IssueMenuKey | undefined) => {
  if (!newMenuKey) return;

  if (newMenuKey === IssueMenuKey.HOME) {
    if (hasHomepageRefreshTriggered) {
      homepageRefreshToken.value = utils.uuid();
    }
    hasHomepageRefreshTriggered = true;
    return;
  }

  if (newMenuKey === IssueMenuKey.TRASH) {
    if (hasTrashRefreshTriggered) {
      trashRefreshToken.value = utils.uuid();
    }
    hasTrashRefreshTriggered = true;
    return;
  }

  if (newMenuKey === IssueMenuKey.SPRINT) {
    if (hasSprintsRefreshTriggered) {
      sprintsRefreshToken.value = utils.uuid();
    }
    hasSprintsRefreshTriggered = true;
    return;
  }

  if (newMenuKey === IssueMenuKey.ISSUE) {
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
  <LeftMenu
    key="issue"
    v-model:activeKey="activeKey"
    :menuItems="menuItems">
    <template #home>
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
    <template #issue>
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
