<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import { ReportContent } from '../../PropsType';

const { t } = useI18n();

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

const report = computed(() => {
  return props.dataSource?.report;
});

const basicInfoSetting = computed(() => {
  return report.value?.basicInfoSetting;
});

const task = computed(() => {
  return props.dataSource?.content?.task;
});
</script>

<template>
  <div>
    <div class="text-theme-title font-medium text-4.5 mb-4">
      <span>{{ t('reportPreview.task.summary.title') }}</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>
    <div class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-24 flex-shrink-0 flex items-center justify-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.status') }}
        </div>
        <div
          class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.description') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-solid border-border-input">
          {{ report?.description }}
        </div>
      </div>

      <div class="flex">
        <div
          class="w-24 flex-shrink-0 flex items-center justify-center space-x-5 px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ task?.status?.message }}
        </div>

        <div class="flex-1">
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.task.summary.fields.contact') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-solid border-border-input">
              {{ basicInfoSetting?.reportContacts }}
            </div>
          </div>
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.task.summary.fields.copyright') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-solid border-border-input">
              {{ basicInfoSetting?.reportCopyright }}
            </div>
          </div>

          <div class="flex">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.task.summary.fields.other') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ basicInfoSetting?.otherInformation }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ant-progress-circle :deep(.ant-progress-text) {
  font-size: 14px;
}
</style>
