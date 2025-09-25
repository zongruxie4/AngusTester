<script setup lang="ts">
import { computed } from 'vue';
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

const plan = computed(() => {
  return props.dataSource?.content?.plan;
});

const testerResponsibilities = computed(() => {
  const _testers = props.dataSource?.content?.cases?.testers;
  const _testerResponsibilities = plan.value?.testerResponsibilities;
  if (!_testerResponsibilities || !_testers) {
    return [];
  }

  return Object.entries(_testerResponsibilities).map(([id, value]) => {
    return {
      id,
      name: _testers[id].fullName,
      responsibility: value
    };
  });
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.functionPlan.detail.title') }}</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a1.1">1.1<em class="inline-block w-3.5"></em>{{ t('common.basicInfo') }}</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.detail.basicInfo.fields.planName') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.name }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.detail.basicInfo.fields.casePrefix') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.casePrefix }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.status') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.status?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.detail.basicInfo.fields.owner') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.ownerName }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.detail.basicInfo.fields.project') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.projectName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.planTime') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.startDate }} - {{ plan?.deadlineDate }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.creator') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.createdByName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.createdDate') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.createdDate }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.detail.basicInfo.fields.workloadEvalMethod') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.evalWorkloadMethod?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.detail.basicInfo.fields.reviewCase') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.review?t('status.yes'):t('status.no') }}
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a.2">1.2<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.detail.testerAndDuty.title') }}</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.tester') }}
          </div>
          <div
            class="flex-1 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5">
            {{ t('reportPreview.functionPlan.detail.testerAndDuty.fields.duty') }}
          </div>
        </div>

        <div
          v-for="(item,index) in testerResponsibilities"
          :key="item.id"
          :class="{'border-b':index<testerResponsibilities.length-1}"
          class="flex border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ item.responsibility }}
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.3">1.3<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.detail.testTarget.title') }}</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.testingObjectives || t('common.noData') }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.4">1.4<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.detail.testScope.title') }}</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.testingScope || t('common.noData') }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.5">1.5<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.detail.acceptanceCriteria.title') }}</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.acceptanceCriteria || t('common.noData') }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.6">1.6<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.detail.otherInfo.title') }}</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.otherInformation || t('common.noData') }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
