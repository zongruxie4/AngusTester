<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

import { ReportContent } from '../PropsType';

const { t } = useI18n();

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
const Summary = defineAsyncComponent(() => import('@/views/report/preview/scenario/summary/index.vue'));
const Catalog = defineAsyncComponent(() => import('@/views/report/preview/scenario/catalog/index.vue'));
const ScenarioInfo = defineAsyncComponent(() => import('@/views/report/preview/scenario/info/index.vue'));
const ScenarioTestResult = defineAsyncComponent(() => import('@/views/report/preview/scenario/testResult/index.vue'));
const ScenarioTestResultDetail = defineAsyncComponent(() => import('@/views/report/preview/scenario/testResultDetail/index.vue'));
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
    <span>{{ t('reportPreview.scenario.content') }}</span>
    <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
  </div>
  <ScenarioInfo
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <ScenarioTestResult
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <ScenarioTestResultDetail
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
</template>
