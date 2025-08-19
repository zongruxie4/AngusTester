<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

import { ExecContent, ExecInfo, ExecResult, ReportInfo } from '../PropsType';

const { t } = useI18n();

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  reportInfo: ReportInfo;
  execInfo: ExecInfo;
  execContent: ExecContent[];
  execResult: ExecResult;
  domId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  reportInfo: undefined,
  execInfo: undefined,
  execContent: undefined,
  execResult: undefined,
  domId: undefined
});

const MainTitle = defineAsyncComponent(() => import('@/views/report/preview/components/mainTitle/index.vue'));
const Summary = defineAsyncComponent(() => import('@/views/report/preview/execFunction/summary/index.vue'));
const Catalog = defineAsyncComponent(() => import('@/views/report/preview/execFunction/catalog/index.vue'));
const ExecuteDetail = defineAsyncComponent(() => import('@/views/report/preview/execFunction/detail/index.vue'));
const SprintResult = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/index.vue'));
const LogDetail = defineAsyncComponent(() => import('@/views/report/preview/execFunction/log/index.vue'));

const reportContent = computed(() => {
  return {
    report: props.reportInfo,
    content: {}
  };
});
</script>

<template>
  <MainTitle
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="reportContent"
    class="mb-7" />
  <Summary
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    class="mb-8" />
  <Catalog
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    class="mb-8" />
  <div class="text-theme-title font-medium text-4.5 mb-4">
    <span>{{ t('reportPreview.execFunction.content') }}</span>
    <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
  </div>
  <ExecuteDetail
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    class="mb-8" />
  <SprintResult
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    class="mb-8" />
  <LogDetail
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult" />
</template>
