<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, nextTick, onMounted, provide, ref, Ref, watch } from 'vue';
import { XCanDexie, sessionStore, utils, appContext } from '@xcan-angus/infra';
import LeftMenu from '@/components/layout/leftMenu/index.vue';
import { useRouter } from 'vue-router';

type MenuKey = 'homepage' | 'plans' | 'cases' | 'modules' | 'tags' | 'trash';

const Homepage = defineAsyncComponent(() => import('@/views/function/homepage/index.vue'));
const Plan = defineAsyncComponent(() => import('@/views/function/plan/index.vue'));
const Case = defineAsyncComponent(() => import('@/views/function/case/index.vue'));
const Review = defineAsyncComponent(() => import('@/views/function/review/index.vue'));
const Baseline = defineAsyncComponent(() => import('@/views/function/baseline/index.vue'));
const Analysis = defineAsyncComponent(() => import('@/views/function/analysis/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/function/trash/index.vue'));

const userInfo = inject<Ref<{ id: string }>>('tenantInfo');
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = inject<Ref<{ id: string; name: string; }>>('appInfo', ref({ id: '', name: '' }));
const changeProjectInfo = inject('changeProjectInfo', () => undefined);

const activeKey = ref<MenuKey>();
const editionType = ref();

const caseRef = ref();
const router = useRouter();

const projectId = computed(() => {
  return projectInfo.value?.id;
});

// 新打开TabPane
const addTabPane = (record) => {
  router.push('/function#cases');
  if (typeof caseRef.value?.updateTabPane === 'function') {
    caseRef.value.addTabPane(record);
  } else {
    sessionStore.set('addTabPane', record);
  }
};

watch(() => caseRef.value, newValue => {
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

// 更新TabPane
const updateTabPane = (record) => {
  router.push('/function#cases');
  if (typeof caseRef.value?.updateTabPane === 'function') {
    caseRef.value.updateTabPane(record);
  }
};

const cacheParamsKey = computed(() => {
  return `${userInfo?.value?.id}${projectInfo.value.id}_case`;
});

let db: Dexie<{
  id: string;
  data: { filters: { value: string, op: string, key: string }[] };
}>;

const setCaseListPlanParam = async (record) => {
  if (!db) {
    db = new XCanDexie<{
      id: string;
      data: { filters: { value: string, op: string, key: string }[] };
    }>('parameter');
  }
  if (!caseRef.value) {
    await db.add(JSON.parse(JSON.stringify({ id: cacheParamsKey.value, data: { filters: [{ value: record.planId, op: 'EQUAL', key: 'planId' }] } })));
  } else {
    await db.add(JSON.parse(JSON.stringify({ id: cacheParamsKey.value, data: { filters: [{ value: record.planId, op: 'EQUAL', key: 'planId' }] } })));
    nextTick(() => {
      caseRef.value.setCaseListPlanParam();
    });
  }
  router.push('/function#cases');
};

const homepageRefreshNotify = ref<string>('');
const plansRefreshNotify = ref<string>('');
const casesRefreshNotify = ref<string>('');
const modulesRefreshNotify = ref<string>('');
const tagsRefreshNotify = ref<string>('');
const trashRefreshNotify = ref<string>('');

let homepageRefreshNotifyFlag = false;
let plansRefreshNotifyFlag = false;
let casesRefreshNotifyFlag = false;
let modulesRefreshNotifyFlag = false;
let tagsRefreshNotifyFlag = false;
let trashRefreshNotifyFlag = false;

onMounted(async () => {
  editionType.value = appContext.getEditionType();
  const queryString = window.location.hash;
  const searchParams = new URLSearchParams(queryString);
  const urlParams = Object.fromEntries(searchParams.entries());
  if (urlParams.projectId && urlParams.projectId !== projectInfo.value.id) {
    changeProjectInfo(urlParams.projectId);
  }

  watch(() => activeKey.value, (newValue) => {
    if (newValue === 'homepage') {
      if (homepageRefreshNotifyFlag) {
        homepageRefreshNotify.value = utils.uuid();
      }

      homepageRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'plans') {
      if (plansRefreshNotifyFlag) {
        plansRefreshNotify.value = utils.uuid();
      }

      plansRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'cases') {
      if (casesRefreshNotifyFlag) {
        casesRefreshNotify.value = utils.uuid();
      }

      casesRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'modules') {
      if (modulesRefreshNotifyFlag) {
        modulesRefreshNotify.value = utils.uuid();
      }

      modulesRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'tags') {
      if (tagsRefreshNotifyFlag) {
        tagsRefreshNotify.value = utils.uuid();
      }

      tagsRefreshNotifyFlag = true;
      return;
    }

    if (newValue === 'trash') {
      if (trashRefreshNotifyFlag) {
        trashRefreshNotify.value = utils.uuid();
      }

      trashRefreshNotifyFlag = true;
    }
  }, { immediate: true });
});

provide('addTabPane', addTabPane);
provide('updateTabPane', updateTabPane);
provide('setCaseListPlanParam', setCaseListPlanParam);

const menuItems = computed(() => {
  return [
    { name: '主页', key: 'homepage', icon: 'icon-zhuye' },
    { name: '计划', key: 'plans', icon: 'icon-jihua1' },
    { name: '用例', key: 'cases', icon: 'icon-ceshiyongli1' },
    { name: '评审', key: 'reviews', icon: 'icon-pingshen' },
    { name: '基线', key: 'baseline', icon: 'icon-jixian' },
    editionType.value !== 'COMMUNITY' && { name: '分析', icon: 'icon-fenxi', key: 'analysis' },
    { name: '回收站', key: 'trash', icon: 'icon-qingchu' }
  ].filter(Boolean);
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

    <template #plans>
      <Plan
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="plansRefreshNotify" />
    </template>

    <template #cases>
      <Case
        ref="caseRef"
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        :refreshNotify="casesRefreshNotify" />
    </template>

    <template #reviews>
      <Review
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>

    <template #baseline>
      <Baseline
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
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
