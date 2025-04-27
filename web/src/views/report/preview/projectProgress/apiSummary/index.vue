<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';

import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
  sequence: {
    big: string;
    small: number[]
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  sequence: () => ({
    big: '',
    small: []
  })
});

const Progress = defineAsyncComponent(() => import('@/views/report/preview/components/progress/index.vue'));
const ApiGrouped = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/apiSummary/group/index.vue'));

const testApis = computed(() => {
  return props.dataSource?.content?.apis?.testApis;
});

const totalApisNum = computed(() => {
  return +testApis.value?.totalApisNum || 0;
});

const enabledTestNum = computed(() => {
  return +testApis.value?.enabledTestNum || 0;
});

const unenabledTestNum = computed(() => {
  return totalApisNum.value * 3 - enabledTestNum.value;
});

const testResultCount = computed(() => {
  return props.dataSource?.content?.apis?.testResultCount;
});

const percent = computed(() => {
  return +(props.dataSource?.content?.apis?.progress?.completedRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
});
</script>

<template>
  <h1 class="text-theme-title font-medium mb-5">
    <span id="a11" class="text-4 text-theme-title font-medium">{{ props.sequence.big }}、<em class="inline-block w-0.25"></em>接口测试</span>
  </h1>

  <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
    <span id="a12">{{ props.sequence.small[0] }}<em class="inline-block w-3.5"></em>接口测试汇总结果</span>
  </h2>
  <div class="flex items-center space-x-7 mb-7">
    <Progress
      :percent="percent"
      text="进度"
      class="ml-4" />
    <div class="flex-1 border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          接口总数
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ totalApisNum }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          启用接口测试
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ enabledTestNum }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          未启用接口测试
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ unenabledTestNum }}
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          接口功能测试
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ testApis?.enabledFuncTestNum || 0 }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          接口性能测试
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ testApis?.enabledPerfTestNum || 0 }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          接口稳定性测试
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ testApis?.enabledStabilityTestNum || 0 }}
        </div>
      </div>

      <div class="flex">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          已测试接口
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ testResultCount?.testedNum }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          已通过接口
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ testResultCount?.testPassedNum }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          未通过接口
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ testResultCount?.testUnPassedNum }}
        </div>
      </div>
    </div>
  </div>

  <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
    <span id="a13">{{ props.sequence.small[1] }}<em class="inline-block w-3.5"></em>接口分组统计</span>
  </h2>
  <ApiGrouped
    :projectInfo="props.projectInfo"
    :userInfo="props.userInfo"
    :appInfo="props.appInfo"
    :dataSource="props.dataSource" />
  <div class="mb-7"></div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
