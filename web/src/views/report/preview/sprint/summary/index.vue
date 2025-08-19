<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
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

const Progress = defineAsyncComponent(() => import('@/views/report/preview/components/progress/index.vue'));

const report = computed(() => {
  return props.dataSource?.report;
});

const basicInfoSetting = computed(() => {
  return report.value?.basicInfoSetting;
});

const content = computed(() => {
  return props.dataSource?.content;
});

const percent = computed(() => {
  return +(content.value?.progress?.completedRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
});
</script>

<template>
  <div>
    <div class="text-theme-title font-medium text-4.5 mb-4">
      <span>摘要</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>
    <div class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-40 flex-shrink-0 flex items-center justify-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          进度
        </div>
        <div
          class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          描述
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-solid border-border-input">
          {{ report?.description }}
        </div>
      </div>

      <div class="flex">
        <div
          class="w-40 flex-shrink-0 flex items-center justify-center space-x-5 px-1.5 py-1.5 border-r border-solid border-border-input">
          <Progress :percent="percent" />
        </div>

        <div class="flex-1">
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              联系人
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-solid border-border-input">
              {{ basicInfoSetting?.reportContacts }}
            </div>
          </div>
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              版权说明
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-solid border-border-input">
              {{ basicInfoSetting?.reportCopyright }}
            </div>
          </div>

          <div class="flex">
            <div
              class="w-24 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              其他
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
