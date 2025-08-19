<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const DashedLine = defineAsyncComponent(() => import('./dashedLine.vue'));
</script>

<template>
  <div class="text-theme-title">
    <div class="font-medium text-4.5 mb-4">
      <span>{{ t('reportPreview.projectProgress.catalog.title') }}</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>

    <div class="space-y-3.5">
      <a href="#a1" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.projectInfo') }}</span>
        <DashedLine />
      </a>

      <a href="#a2" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.devTask') }}</span>
        <DashedLine />
      </a>
      <a href="#a3" class="flex items-center space-x-2.5">
        <span>2.1<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.taskSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a4" class="flex items-center space-x-2.5">
        <span>2.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.taskBurndownChart') }}</span>
        <DashedLine />
      </a>
      <a href="#a5" class="flex items-center space-x-2.5">
        <span>2.3<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.taskGroupStats') }}</span>
        <DashedLine />
      </a>

      <a href="#a6" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.funcTest') }}</span>
        <DashedLine />
      </a>
      <a href="#a7" class="flex items-center space-x-2.5">
        <span>3.1<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.caseTestSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a8" class="flex items-center space-x-2.5">
        <span>3.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.caseReviewSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a9" class="flex items-center space-x-2.5">
        <span>3.3<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.caseBurndownChart') }}</span>
        <DashedLine />
      </a>
      <a href="#a10" class="flex items-center space-x-2.5">
        <span>3.4<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.caseGroupStats') }}</span>
        <DashedLine />
      </a>

      <a href="#a11" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.4') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.apiTest') }}</span>
        <DashedLine />
      </a>
      <a href="#a12" class="flex items-center space-x-2.5">
        <span>4.1<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.apiTestSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a13" class="flex items-center space-x-2.5">
        <span>4.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.apiGroupStats') }}</span>
        <DashedLine />
      </a>

      <a href="#a14" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.5') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.scenarioTest') }}</span>
        <DashedLine />
      </a>
      <a href="#a15" class="flex items-center space-x-2.5">
        <span>5.1<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.scenarioTestSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a16" class="flex items-center space-x-2.5">
        <span>5.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.projectProgress.catalog.sections.scenarioGroupStats') }}</span>
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
