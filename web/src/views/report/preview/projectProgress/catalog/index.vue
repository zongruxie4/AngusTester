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
        <span class="h1">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25 font-medium"></em>项目信息</span>
        <DashedLine />
      </a>

      <a href="#a2" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25 font-medium"></em>研发任务</span>
        <DashedLine />
      </a>
      <a href="#a3" class="flex items-center space-x-2.5">
        <span>2.1<em class="inline-block w-4.25 font-medium"></em>任务汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a4" class="flex items-center space-x-2.5">
        <span>2.2<em class="inline-block w-4.25 font-medium"></em>任务燃尽图</span>
        <DashedLine />
      </a>
      <a href="#a5" class="flex items-center space-x-2.5">
        <span>2.3<em class="inline-block w-4.25 font-medium"></em>任务分组统计</span>
        <DashedLine />
      </a>

      <a href="#a6" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25 font-medium"></em>功能测试</span>
        <DashedLine />
      </a>
      <a href="#a7" class="flex items-center space-x-2.5">
        <span>3.1<em class="inline-block w-4.25 font-medium"></em>用例测试汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a8" class="flex items-center space-x-2.5">
        <span>3.2<em class="inline-block w-4.25 font-medium"></em>用例评审汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a9" class="flex items-center space-x-2.5">
        <span>3.3<em class="inline-block w-4.25 font-medium"></em>用例燃尽图</span>
        <DashedLine />
      </a>
      <a href="#a10" class="flex items-center space-x-2.5">
        <span>3.4<em class="inline-block w-4.25 font-medium"></em>用例分组统计</span>
        <DashedLine />
      </a>

      <a href="#a11" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.4') }}<em class="inline-block w-0.25 font-medium"></em>接口测试</span>
        <DashedLine />
      </a>
      <a href="#a12" class="flex items-center space-x-2.5">
        <span>4.1<em class="inline-block w-4.25 font-medium"></em>接口测试汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a13" class="flex items-center space-x-2.5">
        <span>4.2<em class="inline-block w-4.25 font-medium"></em>接口分组统计</span>
        <DashedLine />
      </a>

      <a href="#a14" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.5') }}<em class="inline-block w-0.25 font-medium"></em>场景测试</span>
        <DashedLine />
      </a>
      <a href="#a15" class="flex items-center space-x-2.5">
        <span>5.1<em class="inline-block w-4.25 font-medium"></em>场景测试汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a16" class="flex items-center space-x-2.5">
        <span>5.2<em class="inline-block w-4.25 font-medium"></em>场景分组统计</span>
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
