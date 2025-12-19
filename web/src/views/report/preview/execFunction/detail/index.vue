<script setup lang="ts">
import { computed } from 'vue';
import { ExecContent, ExecInfo, ExecResult, ReportInfo } from '../../PropsType';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

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
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.execFunction.detail.title') }}</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2">1.1<em class="inline-block w-3.5"></em>{{ t('reportPreview.execFunction.detail.basicInfo.title') }}</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.id') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.id }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.name') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.name }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.scriptId') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.scriptId }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.scriptName') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.scriptName }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.plugin') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.plugin }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.status') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.status?.message }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.type') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.creator }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.priority') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.priority }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.startTime') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.actualStartDate }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.endTime') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.endDate }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.execFunction.detail.basicInfo.fields.nodeCount') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.execNodes?.length || 0 }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.execFunction.detail.basicInfo.fields.reportInterval') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.reportInterval }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.execFunction.detail.basicInfo.fields.ignoreAssertions') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ typeof props.execInfo?.ignoreAssertions ==='boolean'?(props.execInfo?.ignoreAssertions ? t('status.ignored') : t('status.notIgnored')):'' }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.execFunction.detail.basicInfo.fields.updateTestResult') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ typeof props.execInfo?.updateTestResult ==='boolean'?(props.execInfo?.updateTestResult ? t('reportPreview.execFunction.detail.basicInfo.options.update') : t('reportPreview.execFunction.detail.basicInfo.options.notUpdate')):'' }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.creator') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.creator }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.createdDate') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.createdDate }}
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a3">1.2<em class="inline-block w-3.5"></em>{{ t('common.execResult') }}</span>
      </h2>
      <div class="flex items-start">
        <div class="flex items-center flex-shrink-0 mr-10">
          <div class="flex-shrink-0 mr-0.5">{{ t('common.testResult') }}：</div>
          <div>{{ props.execResult?.passed ? t('status.passed') : t('status.failed') }}</div>
        </div>
        <div v-if="props.execResult?.failureMessage" class="flex items-start">
          <div class="flex-shrink-0 mr-0.5">{{ t('common.failureReason') }}：</div>
          <div class="break-all whitespace-pre-wrap text-status-error">
            {{ props.execResult?.failureMessage }}{{ props.execResult?.failureMessage }}{{ props.execResult?.failureMessage }}{{ props.execResult?.failureMessage }}
          </div>
        </div>
      </div>

      <div v-if="caseSummary" class="mt-2.5">
        <div class="mb-1">{{ t('common.testCases') }}：</div>
        <div class="flex items-center space-x-3.5">
          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-blue-1">{{ t('chart.total') }}</div>
            <div class="flex-1 bg-gray-light px-2 rounded-r">{{ caseSummary?.totalNum ||0 }}</div>
          </div>

          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-status-success">{{ t('status.passed') }}</div>
            <div class="flex-1 bg-gray-light px-2 rounded-r">{{ caseSummary?.successNum||0 }}</div>
          </div>

          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-status-error">{{ t('status.failed') }}</div>
            <div class="flex-1 bg-gray-light px-2 rounded-r">{{ caseSummary?.failNum ||0 }}</div>
          </div>

          <div class="flex items-center leading-6 w-40">
            <div class="flex-1 text-white px-2 rounded bg-gray-icon">{{ t('status.disabled') }}</div>
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
