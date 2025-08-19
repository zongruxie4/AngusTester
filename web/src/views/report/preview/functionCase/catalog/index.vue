<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const DashedLine = defineAsyncComponent(() => import('./dashedLine.vue'));
</script>

<template>
  <div class="text-theme-title">
    <div class="font-medium text-4.5 mb-4">
      <span>目录</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>

    <div class="space-y-3.5">
      <a href="#a1" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25 font-medium"></em>基本信息</span>
        <DashedLine />
      </a>
      <a href="#a2" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25 font-medium"></em>人员信息</span>
        <DashedLine />
      </a>
      <a href="#a3" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25 font-medium"></em>日期信息</span>
        <DashedLine />
      </a>
      <a href="#a4" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.4') }}<em class="inline-block w-0.25 font-medium"></em>前置条件</span>
        <DashedLine />
      </a>
      <a href="#a5" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.5') }}<em class="inline-block w-0.25 font-medium"></em>测试步骤</span>
        <DashedLine />
      </a>
      <a href="#a6" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.6') }}<em class="inline-block w-0.25 font-medium"></em>描述</span>
        <DashedLine />
      </a>
      <a href="#a7" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.7') }}<em class="inline-block w-0.25 font-medium"></em>评审信息</span>
        <DashedLine />
      </a>
      <a href="#a8" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.8') }}<em class="inline-block w-0.25 font-medium"></em>测试信息</span>
        <DashedLine />
      </a>
      <a href="#a9" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.9') }}<em class="inline-block w-0.25 font-medium"></em>关联任务</span>
        <DashedLine />
      </a>
      <a href="#a10" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.10') }}<em class="inline-block w-0.25 font-medium"></em>关联用例</span>
        <DashedLine />
      </a>
      <a href="#a11" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.11') }}<em class="inline-block w-0.25 font-medium"></em>活动</span>
        <DashedLine />
      </a>
      <a href="#a12" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.12') }}<em class="inline-block w-0.25 font-medium"></em>评论</span>
        <DashedLine />
      </a>
    </div>
    <div class="pdf-page-break"></div>
  </div>
</template>

<style scoped>
a {
  transition: all 300ms linear 0ms;
}

a:hover {
  color: #40a9ff;
}

.h1 {
  font-size: 14px;
  font-weight: 500;
}
</style>
