<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';

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

const DashedLine = defineAsyncComponent(() => import('./dashedLine.vue'));

</script>

<template>
  <div class="text-theme-title">
    <div class="font-medium text-4.5 mb-4">
      <span>{{ t('reportPreview.task.catalog.title') }}</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>

    <div class="space-y-3.5">
      <a href="#a1" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('common.basicInfo') }}</span>
        <DashedLine />
      </a>
      <a href="#a2" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.task.catalog.sections.personnelInfo') }}</span>
        <DashedLine />
      </a>
      <a href="#a3" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.task.catalog.sections.dateInfo') }}</span>
        <DashedLine />
      </a>
      <a href="#a4" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.4') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('common.description') }}</span>
        <DashedLine />
      </a>
      <a href="#a5" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.5') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.task.catalog.sections.subTask') }}</span>
        <DashedLine />
      </a>
      <a href="#a6" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.6') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.task.catalog.sections.assocTask') }}</span>
        <DashedLine />
      </a>
      <a href="#a7" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.7') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.task.catalog.sections.assocCase') }}</span>
        <DashedLine />
      </a>

      <a href="#a8" class="flex items-center space-x-2.5">
          <span class="h1">{{ t('reportPreview.serial.8') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('common.remark') }}</span>
          <DashedLine />
        </a>
        <a href="#a9" class="flex items-center space-x-2.5">
          <span class="h1">{{ t('reportPreview.serial.9') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.task.catalog.sections.activity') }}</span>
          <DashedLine />
        </a>
        <a href="#a10" class="flex items-center space-x-2.5">
          <span class="h1">{{ t('common.comment') }}</span>
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
