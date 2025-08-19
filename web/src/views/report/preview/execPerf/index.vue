<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n'; 
import { ExecContent, ExecInfo, ExecResult, ReportInfo, SummaryListItem } from '../PropsType';

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
const Summary = defineAsyncComponent(() => import('@/views/report/preview/execPerf/summary/index.vue'));
const Catalog = defineAsyncComponent(() => import('@/views/report/preview/execPerf/catalog/index.vue'));
const CatalogExcludeHttp = defineAsyncComponent(() => import('@/views/report/preview/execPerf/catalogExcludeHttp/index.vue'));
const ExecuteDetail = defineAsyncComponent(() => import('@/views/report/preview/execPerf/detail/index.vue'));
const SamplingSummary = defineAsyncComponent(() => import('@/views/report/preview/execPerf/samplingSummary/index.vue'));
const LogDetail = defineAsyncComponent(() => import('@/views/report/preview/execPerf/log/index.vue'));
const Indicator = defineAsyncComponent(() => import('@/views/report/preview/execPerf/sampling/index.vue'));

const summaryList = ref<SummaryListItem[]>();

const handleListData = (data:{computedValue:{[key:string]:string};list:SummaryListItem[]}) => {
  summaryList.value = data.list || [];
};

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
    v-if="props.execInfo?.plugin === 'Http'"
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    class="mb-8" />
  <CatalogExcludeHttp
    v-else
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    class="mb-8" />
  <div class="text-theme-title font-medium text-4.5 mb-4">
    <span>内容</span>
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
  <SamplingSummary
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    :summaryList="summaryList"
    class="mb-8" />
  <Indicator
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult"
    class="mb-8"
    @change="handleListData" />
  <LogDetail
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :reportInfo="props.reportInfo"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :execResult="props.execResult" />
</template>
