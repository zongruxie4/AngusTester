<script setup lang="ts">
import { computed } from 'vue';
import { ExecContent, ExecInfo, ExecResult, ReportInfo } from '../../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  reportInfo: ReportInfo;
  execInfo: ExecInfo;
  execContent: ExecContent[];
  execResult: ExecResult;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  reportInfo: undefined,
  execInfo: undefined,
  execContent: undefined,
  execResult: undefined
});

const caseSummary = computed(() => {
  return props.execResult?.caseSummary;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a1" class="text-4 text-theme-title font-medium">一、<em class="inline-block w-0.25"></em>执行信息</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2">1.1<em class="inline-block w-3.5"></em>执行基本信息</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            ID
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.id }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            名称
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.name }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            测试脚本ID
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.scriptId }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            测试脚本名称
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.scriptName }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            插件
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.plugin }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            状态
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.status?.message }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            类型
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.createdByName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            优先级
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.priority }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            开始时间
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.actualStartDate }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            结束时间
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.endDate }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            节点数
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.execNodes?.length || 0 }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            报告间隔
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.reportInterval }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            忽略断言
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ typeof props.execInfo?.ignoreAssertions ==='boolean'?(props.execInfo?.ignoreAssertions?'忽略':'不忽略'):'' }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            更新测试结果
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ typeof props.execInfo?.updateTestResult ==='boolean'?(props.execInfo?.updateTestResult?'更新':'不更新'):'' }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            添加人
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.createdByName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            添加时间
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.createdDate }}
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a3">1.2<em class="inline-block w-3.5"></em>执行结果</span>
      </h2>
      <div class="flex items-start">
        <div class="flex items-center flex-shrink-0 mr-10">
          <div class="flex-shrink-0 mr-0.5">测试结果：</div>
          <div>{{ props.execResult?.passed?'通过':'未通过' }}</div>
        </div>
        <div v-if="props.execResult?.failureMessage" class="flex items-start">
          <div class="flex-shrink-0 mr-0.5">失败原因：</div>
          <div class="break-all whitespace-pre-wrap text-status-error">{{ props.execResult?.failureMessage }}{{ props.execResult?.failureMessage }}{{ props.execResult?.failureMessage }}{{ props.execResult?.failureMessage }}</div>
        </div>
      </div>

      <div v-if="caseSummary" class="mt-2.5">
        <div class="mb-1">测试用例：</div>
        <div class="flex items-center space-x-3.5">
          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-blue-1">总共</div>
            <div class="flex-1 bg-gray-light px-2 rounded-r">{{ caseSummary?.totalNum ||0 }}</div>
          </div>

          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-status-success">通过</div>
            <div class="flex-1 bg-gray-light px-2 rounded-r">{{ caseSummary?.successNum||0 }}</div>
          </div>

          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-status-error">未通过</div>
            <div class="flex-1 bg-gray-light px-2 rounded-r">{{ caseSummary?.failNum ||0 }}</div>
          </div>

          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-gray-icon">未启用</div>
            <div class="flex-1 bg-gray-light px-2 rounded-r">{{ caseSummary?.disabledNum ||0 }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
