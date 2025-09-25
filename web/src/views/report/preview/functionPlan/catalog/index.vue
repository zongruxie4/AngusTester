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

const DashedLine = defineAsyncComponent(() => import('./DashedLine.vue'));

const testers = computed(() => {
  const _testers = props.dataSource?.content?.cases?.testers;
  if (!_testers) {
    return [];
  }
  return Object.values(_testers);
});
</script>

<template>
  <div class="text-theme-title">
    <div class="font-medium text-4.5 mb-4">
      <span>{{ t('reportPreview.functionPlan.catalog.title') }}</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>

    <div class="space-y-3.5">
      <a href="#a1" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.planInfo') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.1" class="flex items-center space-x-2.5">
        <span>1.1<em class="inline-block w-4.25 font-medium"></em>{{ t('common.basicInfo') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.2" class="flex items-center space-x-2.5">
        <span>1.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.testerAndDuty') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.3" class="flex items-center space-x-2.5">
        <span>1.3<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.testTarget') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.4" class="flex items-center space-x-2.5">
        <span>1.4<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.testScope') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.5" class="flex items-center space-x-2.5">
        <span>1.5<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.acceptanceCriteria') }}</span>
        <DashedLine />
      </a>
      <a href="#a1.6" class="flex items-center space-x-2.5">
        <span>1.6<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.otherInfo') }}</span>
        <DashedLine />
      </a>

      <a href="#a2" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.caseSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a2.1" class="flex items-center space-x-2.5">
        <span>2.1<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.testSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a2.2" class="flex items-center space-x-2.5">
        <span>2.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.reviewSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a2.3" class="flex items-center space-x-2.5">
        <span>2.3<em class="inline-block w-4.25 font-medium"></em>{{ t('chart.burndown.title') }}</span>
        <DashedLine />
      </a>
      <a href="#a2.4" class="flex items-center space-x-2.5">
        <span>2.4<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.groupStats') }}</span>
        <DashedLine />
      </a>

      <a href="#a3" class="flex items-center space-x-2.5">
        <span class="h1">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.testerSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a3.1" class="flex items-center space-x-2.5">
        <span>3.1<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.testSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a3.2" class="flex items-center space-x-2.5">
        <span>3.2<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.reviewSummary') }}</span>
        <DashedLine />
      </a>
      <a href="#a3.3" class="flex items-center space-x-2.5">
        <span>3.3<em class="inline-block w-4.25 font-medium"></em>{{ t('chart.burndown.title') }}</span>
        <DashedLine />
      </a>
      <a href="#a3.4" class="flex items-center space-x-2.5">
        <span>3.4<em class="inline-block w-4.25 font-medium"></em>{{ t('reportPreview.functionPlan.catalog.sections.groupStats') }}</span>
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
