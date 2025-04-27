<script setup lang="ts">
import { defineAsyncComponent, computed, inject, Ref, ref } from 'vue';

import { ReportContent } from './PropsType';
import { bigSequence, smallSequnce } from '../PropsType';

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
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

const MainTitle = defineAsyncComponent(() => import('@/views/report/preview/components/mainTitle/index.vue'));
const ProjectInfo = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/info/index.vue'));
const Summary = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/summary/index.vue'));
const Catalog = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/catalog/index.vue'));
const TaskSummary = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/taskSummary/index.vue'));
const CaseSummary = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/funcCaseSummary/index.vue'));
const ApiTestSummary = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/apiSummary/index.vue'));
const SceneTestSummary = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/scenarioTestSummary/index.vue'));

const catalogSequence = computed(() => {
  if (!proTypeShowMap.value.showTask) {
    return {
      project: 0,
      case: 1,
      api: 2,
      scenario: 3
    };
  }
  return {
    project: 0,
    task: 1,
    case: 2,
    api: 3,
    scenario: 4
  };
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
    <span>内容</span>
    <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
  </div>
  <ProjectInfo
    :sequence="{
      big: bigSequence[catalogSequence['project']],
      small: smallSequnce[catalogSequence['project']]
    }"
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-7" />
  <TaskSummary
    v-if="proTypeShowMap.showTask"
    :sequence="{
      big: bigSequence?.[catalogSequence['task']],
      small: smallSequnce?.[catalogSequence['task']]
    }"
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <CaseSummary
    :sequence="{
      big: bigSequence[catalogSequence['case']],
      small: smallSequnce[catalogSequence['case']]
    }"
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <ApiTestSummary
    :sequence="{
      big: bigSequence[catalogSequence['api']],
      small: smallSequnce[catalogSequence['api']]
    }"
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
  <SceneTestSummary
    :sequence="{
      big: bigSequence[catalogSequence['scenario']],
      small: smallSequnce[catalogSequence['scenario']]
    }"
    :userInfo="props.userInfo"
    :projectInfo="props.projectInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource"
    class="mb-8" />
</template>
