<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const caseSummary = computed(() => {
  return props.dataSource?.content?.testResult?.caseSummary;
});

const caseResults = computed(() => {
  return props.dataSource?.content?.testResult?.caseResults;
});

const len = computed(() => {
  let _len = 0;
  if (caseResults.value?.length) {
    _len = caseResults.value?.length - 1;
  }
  return _len;
});
</script>

<template>
  <div class="mb-4">
    <h3 class="flex items-center space-x-2.5 mb-1.5">
      <span id="a10" class="flex items-center space-x-1.5"><em
        class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em><span>{{ t('reportPreview.task.info.testing.function.caseStats.title') }}</span></span>
    </h3>
    <div class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.function.caseStats.fields.totalCases') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ caseSummary?.totalNum }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.function.caseStats.fields.passedCases') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ caseSummary?.successNum }}
        </div>
      </div>

      <div class="flex">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.function.caseStats.fields.failedCases') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ caseSummary?.failNum }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.function.caseStats.fields.disabledCases') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ caseSummary?.disabledNum }}
        </div>
      </div>
    </div>
  </div>

  <div>
    <h3 class="flex items-center space-x-2.5 mb-1.5">
      <span id="a10" class="flex items-center space-x-1.5"><em
        class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em><span>{{ t('reportPreview.task.info.testing.function.testedCases.title') }}</span></span>
    </h3>
    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-37 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.function.testedCases.fields.caseId') }}
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.function.testedCases.fields.caseName') }}
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5">
          {{ t('reportPreview.task.info.testing.function.testedCases.fields.status') }}
        </div>
      </div>

      <div
        v-for="(item,_index) in caseResults"
        :key="item.id"
        :class="{'border-b':_index<len}"
        class="flex border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.caseId }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.caseName }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap">
          {{ item.passed ? t('reportPreview.task.info.testing.function.testedCases.options.passed') : t('reportPreview.task.info.testing.function.testedCases.options.failed') }}
        </div>
      </div>
    </div>

    <div v-else class="content-text-container">{{ t('reportPreview.task.info.testing.function.testedCases.noData') }}</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
