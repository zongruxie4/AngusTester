<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

import { ReportContent } from './PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
  domId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  domId: undefined
});

const MainTitle = defineAsyncComponent(() => import('@/views/report/preview/components/mainTitle/index.vue'));
const Summary = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/summary/index.vue'));
const Catalog = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/catalog/index.vue'));
const PlanDetail = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/detail/index.vue'));
const CaseSummary = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/caseSummary/index.vue'));
const CaseDetail = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/caseDetail/index.vue'));
</script>

<template>
  <MainTitle
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-7" />
  <Summary
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <Catalog
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <div class="text-theme-title font-medium text-4.5 mb-4">
    <span>内容</span>
    <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
  </div>
  <PlanDetail
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <CaseSummary
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <CaseDetail
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource" />
</template>
