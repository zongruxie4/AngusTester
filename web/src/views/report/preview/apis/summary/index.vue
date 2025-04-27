<script setup lang="ts">
import { computed } from 'vue';

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

const report = computed(() => {
  return props.dataSource?.report;
});

const basicInfoSetting = computed(() => {
  return report.value?.basicInfoSetting;
});

const apis = computed(() => {
  return props.dataSource?.content?.apis;
});
</script>

<template>
  <div>
    <div class="text-theme-title font-medium text-4.5 mb-4">
      <span>摘要</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>
    <div class="border border-solid border-border-input">
      <div class="border-b border-solid border-border-input">
        <div
          class="w-24 inline-block text-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input ">
          接口状态
        </div>
        <div
          class="w-24 inline-block bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          描述
        </div>
        <div class="inline-block px-1.5 py-1.5 break-all whitespace-pre-wrap border-solid border-border-input">
          {{ report?.description }}
        </div>
      </div>

      <div class="flex">
        <div
          class="w-24 flex-shrink-0 flex items-center justify-center space-x-5 px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ apis?.status?.message }}
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
