<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, nextTick, onMounted, provide, ref, Ref, watch } from 'vue';
import { XCanDexie, sessionStore, utils, appContext, SearchCriteria } from '@xcan-angus/infra';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ProjectInfo } from '@/layout/types';
import { TestMenuKey, createMenuItems } from '@/views/test/menu';

// Component Imports
const LeftMenu = defineAsyncComponent(() => import('@/components/layout/leftMenu/index.vue'));
const Homepage = defineAsyncComponent(() => import('@/views/test/home/index.vue'));
const Plan = defineAsyncComponent(() => import('@/views/test/plan/index.vue'));
const Case = defineAsyncComponent(() => import('@/views/test/case/index.vue'));
const Review = defineAsyncComponent(() => import('@/views/test/review/index.vue'));
const Baseline = defineAsyncComponent(() => import('@/views/test/baseline/index.vue'));
const Analysis = defineAsyncComponent(() => import('@/views/test/analysis/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/test/trash/index.vue'));

// Composables and Context
const { t } = useI18n();
const router = useRouter();

// Reactive State
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const currentUserInfo = ref(appContext.getUser());
const currentProjectId = computed(() => projectInfo.value?.id);
const applicationInfo = ref(appContext.getAccessApp());
const changeProjectInfo = inject('changeProjectInfo', () => undefined);

// Type Definitions
const activeMenuKey = ref<TestMenuKey>();
const currentEditionType = ref();

// Component References
const caseComponentRef = ref();

// Refresh Notification State
const homepageRefreshToken = ref<string>('');
const plansRefreshToken = ref<string>('');
const casesRefreshToken = ref<string>('');
const modulesRefreshToken = ref<string>('');
const tagsRefreshToken = ref<string>('');
const trashRefreshToken = ref<string>('');

// Track whether refresh notifications have been triggered for each section
let hasHomepageRefreshTriggered = false;
let hasPlansRefreshTriggered = false;
let hasCasesRefreshTriggered = false;
let hasModulesRefreshTriggered = false;
let hasTagsRefreshTriggered = false;
let hasTrashRefreshTriggered = false;

// Database Instance
let parameterDatabase: XCanDexie<{
  id: string;
  data: { filters: SearchCriteria[] };
}>;

/**
 * <p>Generates a unique cache key for storing case list parameters.</p>
 * <p>Combines user ID and project ID to ensure parameter isolation.</p>
 */
const caseParameterCacheKey = computed(() => {
  return `${currentUserInfo?.value?.id}${currentProjectId.value}_case`;
});

/**
 * <p>Adds a new tab pane to the case component.</p>
 * <p>Navigates to the cases section and either adds the tab directly or stores it for later processing.</p>
 */
const addTabPane = (record) => {
  router.push(`/test#${TestMenuKey.CASES}`);
  if (typeof caseComponentRef.value?.updateTabPane === 'function') {
    caseComponentRef.value.addTabPane(record);
  } else {
    sessionStore.set('addTabPane', record);
  }
};

/**
 * <p>Updates an existing tab pane in the case component.</p>
 * <p>Navigates to the cases section and updates the specified tab.</p>
 */
const updateTabPane = (record) => {
  router.push(`/test#${TestMenuKey.CASES}`);
  if (typeof caseComponentRef.value?.updateTabPane === 'function') {
    caseComponentRef.value.updateTabPane(record);
  }
};

/**
 * <p>Sets case list parameters based on plan selection.</p>
 * <p>Stores filter parameters in the database and applies them to the case list component.</p>
 */
const setCaseListPlanParam = async (record) => {
  if (!parameterDatabase) {
    parameterDatabase = new XCanDexie<{
      id: string;
      data: { filters: SearchCriteria[] };
    }>('parameter');
  }

  const filterData = {
    id: caseParameterCacheKey.value,
    data: {
      filters: [{
        value: record.planId,
        op: SearchCriteria.OpEnum.Equal,
        key: 'planId'
      }]
    }
  };

  if (!caseComponentRef.value) {
    await parameterDatabase.add(JSON.parse(JSON.stringify(filterData)));
  } else {
    await parameterDatabase.add(JSON.parse(JSON.stringify(filterData)));
    await nextTick(() => {
      caseComponentRef.value.setCaseListPlanParam();
    });
  }
  await router.push(`/test#${TestMenuKey.CASES}`);
};

/**
 * <p>Handles the refresh notification logic when switching between menu sections.</p>
 * <p>Generates unique refresh tokens to trigger child component updates.</p>
 * <p>Uses flags to ensure refresh only happens after the first visit to each section.</p>
 */
const handleMenuSectionChange = (newMenuKey: TestMenuKey | undefined) => {
  if (!newMenuKey) return;

  if (newMenuKey === TestMenuKey.HOME) {
    if (hasHomepageRefreshTriggered) {
      homepageRefreshToken.value = utils.uuid();
    }
    hasHomepageRefreshTriggered = true;
    return;
  }

  if (newMenuKey === TestMenuKey.PLANS) {
    if (hasPlansRefreshTriggered) {
      plansRefreshToken.value = utils.uuid();
    }
    hasPlansRefreshTriggered = true;
    return;
  }

  if (newMenuKey === TestMenuKey.CASES) {
    if (hasCasesRefreshTriggered) {
      casesRefreshToken.value = utils.uuid();
    }
    hasCasesRefreshTriggered = true;
    return;
  }

  if (newMenuKey === TestMenuKey.REVIEWS) {
    if (hasModulesRefreshTriggered) {
      modulesRefreshToken.value = utils.uuid();
    }
    hasModulesRefreshTriggered = true;
    return;
  }

  if (newMenuKey === TestMenuKey.BASELINE) {
    if (hasTagsRefreshTriggered) {
      tagsRefreshToken.value = utils.uuid();
    }
    hasTagsRefreshTriggered = true;
    return;
  }

  if (newMenuKey === TestMenuKey.TRASH) {
    if (hasTrashRefreshTriggered) {
      trashRefreshToken.value = utils.uuid();
    }
    hasTrashRefreshTriggered = true;
  }
};

/**
 * <p>Generates the menu items array based on edition type and feature availability.</p>
 * <p>Filters out menu items that should not be displayed based on configuration.</p>
 */
const menuItems = computed(() => createMenuItems(t, currentEditionType.value));

onMounted(async () => {
  // Initialize edition type from application context
  currentEditionType.value = appContext.getEditionType();

  // Handle URL parameters for project switching
  const queryString = window.location.hash;
  const searchParams = new URLSearchParams(queryString);
  const urlParams = Object.fromEntries(searchParams.entries());
  if (urlParams.projectId && urlParams.projectId !== currentProjectId.value?.toString()) {
    changeProjectInfo();
  }

  // Watch for menu key changes and handle refresh notifications
  watch(() => activeMenuKey.value, handleMenuSectionChange, { immediate: true });
});

// Watch for case component reference changes to handle pending tab operations
watch(() => caseComponentRef.value, newValue => {
  if (newValue) {
    const addTabData = sessionStore.get('addTabPane');
    if (addTabData) {
      addTabPane(addTabData);
      sessionStore.remove('addTabPane');
    }
  }
}, {
  immediate: true
});

// Provide functions to child components
provide('addTabPane', addTabPane);
provide('updateTabPane', updateTabPane);
provide('setCaseListPlanParam', setCaseListPlanParam);
</script>
<template>
  <LeftMenu
    key="test"
    v-model:activeKey="activeMenuKey"
    :menuItems="menuItems">
    <template #home>
      <Homepage
        :projectId="currentProjectId"
        :userInfo="currentUserInfo"
        :appInfo="applicationInfo"
        :refreshNotify="homepageRefreshToken" />
    </template>

    <template #plans>
      <Plan
        :projectId="currentProjectId"
        :userInfo="currentUserInfo"
        :appInfo="applicationInfo"
        :refreshNotify="plansRefreshToken" />
    </template>

    <template #cases>
      <Case
        ref="caseComponentRef"
        :projectId="currentProjectId"
        :userInfo="currentUserInfo"
        :appInfo="applicationInfo"
        :refreshNotify="casesRefreshToken" />
    </template>

    <template #reviews>
      <Review
        :projectId="currentProjectId"
        :userInfo="currentUserInfo"
        :appInfo="applicationInfo" />
    </template>

    <template #baseline>
      <Baseline
        :projectId="currentProjectId"
        :userInfo="currentUserInfo"
        :appInfo="applicationInfo" />
    </template>

    <template #analysis>
      <Analysis
        :projectId="currentProjectId"
        :userInfo="currentUserInfo"
        :appInfo="applicationInfo" />
    </template>

    <template #trash>
      <Trash
        :projectId="currentProjectId"
        :userInfo="currentUserInfo"
        :appInfo="applicationInfo"
        :refreshNotify="trashRefreshToken" />
    </template>
  </LeftMenu>
</template>
