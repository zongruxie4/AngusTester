<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
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
const Summary = defineAsyncComponent(() => import('@/views/report/preview/task/summary/index.vue'));
const Catalog = defineAsyncComponent(() => import('@/views/report/preview/task/catalog/index.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/report/preview/task/info/basic/index.vue'));
const PersonInfo = defineAsyncComponent(() => import('@/views/report/preview/task/info/personnel/index.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/report/preview/task/info/date/index.vue'));
const Description = defineAsyncComponent(() => import('@/views/report/preview/task/info/description/index.vue'));
const RefTasks = defineAsyncComponent(() => import('@/views/report/preview/task/assocTask/index.vue'));
const RefCases = defineAsyncComponent(() => import('@/views/report/preview/task/assocCase/index.vue'));
const SubTasks = defineAsyncComponent(() => import('@/views/report/preview/task/subTask/index.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/report/preview/task/info/testing/index.vue'));
const Remarks = defineAsyncComponent(() => import('@/views/report/preview/task/info/remark/index.vue'));
const Activity = defineAsyncComponent(() => import('@/views/report/preview/task/activity/index.vue'));
const Comments = defineAsyncComponent(() => import('@/views/report/preview/task/comment/index.vue'));

const taskType = computed(() => {
  return props.dataSource?.content?.task?.taskType?.value;
});
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
    <span>{{ t('reportPreview.task.content') }}</span>
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
  <Description
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <SubTasks
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
  <TestInfo
    v-if="taskType==='API_TEST'||taskType==='SCENARIO_TEST'"
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <Remarks
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
