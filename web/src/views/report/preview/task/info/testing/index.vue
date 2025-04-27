<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';

import { ReportContent } from '../../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const Function = defineAsyncComponent(() => import('@/views/report/preview/task/info/testing/function/index.vue'));
const Stability = defineAsyncComponent(() => import('@/views/report/preview/task/info/testing/stability/index.vue'));
const Perf = defineAsyncComponent(() => import('@/views/report/preview/task/info/testing/perf/index.vue'));

const testResult = computed(() => {
  return props.dataSource?.content?.testResult;
});

const testNum = computed(() => +(testResult.value?.testNum || 0));
const testFailureNum = computed(() => +(testResult.value?.testFailureNum || 0));

const onePassText = computed(() => {
  if (testNum.value <= 0) {
    return '';
  }

  return testFailureNum.value === 0 ? '是' : '否';
});

const task = computed(() => {
  return props.dataSource?.content?.task;
});

const taskType = computed(() => {
  return task.value?.taskType?.value;
});

const testType = computed(() => {
  return task.value?.testType?.value;
});

const idText = computed(() => {
  return taskType.value === 'API_TEST' ? '接口ID' : '场景ID';
});

const nameText = computed(() => {
  return taskType.value === 'API_TEST' ? '接口名称' : '场景名称';
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a7" class="text-4 text-theme-title font-medium">七、<em class="inline-block w-0.25"></em>测试信息</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a8">7.1<em class="inline-block w-3.5"></em>测试资源</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div class="w-37 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ idText }}
          </div>
          <div class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5">
            {{ nameText }}
          </div>
        </div>

        <div class="flex">
          <div class="w-37 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ task?.targetId }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ task?.targetName }}
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a9">7.2<em class="inline-block w-3.5"></em>执行信息</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            执行结果
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ task?.execResult?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            执行ID
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ task?.execId }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            执行名称
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ task?.execName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            执行人
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ task?.execByName }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            脚本ID
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ task?.scriptId }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            脚本名称
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ task?.scriptName }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            最后执行时间
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ task?.execDate }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a10">7.3<em class="inline-block w-3.5"></em>测试结果</span>
      </h2>
      <div class="mb-4">
        <h3 class="flex items-center space-x-2.5 mb-1.5">
          <span id="a10" class="flex items-center space-x-1.5"><em
            class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em><span>基本信息</span></span>
        </h3>
        <div class="border border-solid border-border-input">
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              测试结果
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ testResult?.passed ? '通过' : '不通过' }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              一次性通过
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ onePassText }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              测试次数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ testNum }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              失败次数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ testFailureNum }}
            </div>
          </div>

          <div class="flex">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              失败原因
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ testResult?.failureMessage }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            </div>
          </div>
        </div>
      </div>

      <Function v-if="testType==='FUNCTIONAL'" :dataSource="props.dataSource" />
      <Stability v-else-if="testType==='STABILITY'" :dataSource="props.dataSource" />
      <Perf v-else-if="testType==='PERFORMANCE'" :dataSource="props.dataSource" />
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
