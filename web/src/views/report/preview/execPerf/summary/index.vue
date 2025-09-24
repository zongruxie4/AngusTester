<script setup lang="ts">
import { computed } from 'vue';
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

const basicInfoSetting = computed(() => {
  return props.reportInfo?.basicInfoSetting;
});

const resultInfo = computed(() => {
  if (props.execResult) {
    return {
      label: t('reportPreview.execPerf.summary.fields.testPassed'),
      value: props.execResult?.passed ? t('status.passed') : t('status.failed')
    };
  }

  return {
    label: t('reportPreview.execPerf.summary.fields.execStatus'),
    value: props.execInfo?.status?.message
  };
});
</script>

<template>
  <div>
    <div class="text-theme-title font-medium text-4.5 mb-4">
      <span>{{ t('reportPreview.execPerf.summary.title') }}</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>
    <div class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center justify-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ resultInfo?.label }}
        </div>
        <div
          class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.execPerf.summary.fields.description') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-solid border-border-input">
          {{ props.reportInfo?.description }}
        </div>
      </div>

      <div class="flex">
        <div
          class="w-27 flex-shrink-0 flex items-center justify-center space-x-5 px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ resultInfo?.value }}
        </div>

        <div class="flex-1">
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.execPerf.summary.fields.contact') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-solid border-border-input">
              {{ basicInfoSetting?.reportContacts }}
            </div>
          </div>
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.execPerf.summary.fields.copyright') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-solid border-border-input">
              {{ basicInfoSetting?.reportCopyright }}
            </div>
          </div>

          <div class="flex">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.execPerf.summary.fields.other') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap">
              {{ basicInfoSetting?.otherInformation }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
