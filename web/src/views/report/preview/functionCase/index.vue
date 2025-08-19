<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from './PropsType';

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
const Summary = defineAsyncComponent(() => import('@/views/report/preview/functionCase/summary/index.vue'));
const Catalog = defineAsyncComponent(() => import('@/views/report/preview/functionCase/catalog/index.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/report/preview/functionCase/info/basic/index.vue'));
const PersonInfo = defineAsyncComponent(() => import('@/views/report/preview/functionCase/info/personnel/index.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/report/preview/functionCase/info/date/index.vue'));
const PreCondition = defineAsyncComponent(() => import('@/views/report/preview/functionCase/info/precondition/index.vue'));
const TestSteps = defineAsyncComponent(() => import('@/views/report/preview/functionCase/info/testSteps/index.vue'));
const Description = defineAsyncComponent(() => import('@/views/report/preview/functionCase/info/description/index.vue'));
const ReviewInfo = defineAsyncComponent(() => import('@/views/report/preview/functionCase/review/index.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/report/preview/functionCase/info/testInfo/index.vue'));
const RefTasks = defineAsyncComponent(() => import('@/views/report/preview/functionCase/assocTask/index.vue'));
const RefCases = defineAsyncComponent(() => import('@/views/report/preview/functionCase/assocCase/index.vue'));
const Activity = defineAsyncComponent(() => import('@/views/report/preview/functionCase/activity/index.vue'));
const Comments = defineAsyncComponent(() => import('@/views/report/preview/functionCase/comment/index.vue'));
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
  <BasicInfo
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <PersonInfo
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <DateInfo
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <PreCondition
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <TestSteps
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <Description
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <ReviewInfo
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <TestInfo
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <RefTasks
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <RefCases
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <Activity
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <Comments
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
</template>
