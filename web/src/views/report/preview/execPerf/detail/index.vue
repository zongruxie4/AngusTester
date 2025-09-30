<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { ExecContent, ExecInfo, ExecResult, ReportInfo } from '../../PropsType';

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

const ApiStability = defineAsyncComponent(() => import('@/views/report/preview/execPerf/detail/apiStability/index.vue'));
const ApiPerformance = defineAsyncComponent(() => import('@/views/report/preview/execPerf/detail/apiPerf/index.vue'));

const scriptType = computed(() => props.execInfo?.scriptType?.value);

const resultInfo = computed(() => {
  if (props.execResult) {
    return {
      label: t('reportPreview.execPerf.summary.fields.testPassed'),
      value: props.execResult?.passed ? t('status.passed') : t('status.failed')
    };
  }

  return {
    label: t('common.execStatus'),
    value: props.execInfo?.status?.message
  };
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.execPerf.detail.title') }}</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2">1.1<em class="inline-block w-3.5"></em>{{ t('reportPreview.execPerf.detail.basicInfo.title') }}</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <!-- <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            ID
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.id }}
          </div> -->
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.execName') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.name }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.scriptName') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.scriptName }}
          </div>
        </div>

        <!-- <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            测试脚本ID
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.scriptId }}
          </div>

        </div> -->

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
            {{ props.execInfo?.createdByName }}
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
            {{ t('reportPreview.execPerf.detail.basicInfo.fields.nodeCount') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.execNodes?.length || 0 }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.execPerf.detail.basicInfo.fields.samplingInterval') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
            {{ props.execInfo?.reportInterval }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.creator') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ props.execInfo?.createdByName }}
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
        <span id="a3">1.2<em class="inline-block w-3.5"></em>{{ t('reportPreview.execPerf.detail.execResult.title') }}</span>
      </h2>
      <div class="flex items-start">
        <div class="flex items-center flex-shrink-0 mr-10">
          <div class="flex-shrink-0 mr-0.5">{{ resultInfo?.label }}：</div>
          <div>{{ resultInfo?.value }}</div>
        </div>
        <div v-if="props.execResult?.failureMessage" class="flex items-start">
          <div class="flex-shrink-0 mr-0.5">{{ t('reportPreview.execPerf.detail.execResult.fields.failureReason') }}：</div>
          <div class="break-all whitespace-pre-wrap text-status-error">{{ props.execResult?.failureMessage }}</div>
        </div>
      </div>

      <div v-if="props.execResult" class="mt-2.5">
        <ApiStability
          v-if="scriptType==='TEST_STABILITY'"
          :execResult="props.execResult" />
        <ApiPerformance
          v-else-if="scriptType==='TEST_PERFORMANCE'"
          :execResult="props.execResult" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
