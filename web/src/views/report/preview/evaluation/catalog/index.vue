<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const DashedLine = defineAsyncComponent(() => import('./dashedLine.vue'));
</script>

<template>
  <div class="text-theme-title">
    <div class="font-medium text-4.5 mb-4">
      <span>{{ t('reportPreview.evaluation.catalog.title') }}</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>

    <div class="space-y-3.5">
      <a href="#a1" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.evaluation.title') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.1" class="flex items-center space-x-2.5">
        <span>1.1<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.evaluation.overview.title') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.2" class="flex items-center space-x-2.5">
        <span>1.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.evaluation.detail.title') }}</span>
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
