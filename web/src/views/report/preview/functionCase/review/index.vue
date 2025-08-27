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

const caseInfo = computed(() => {
  return props.dataSource?.content?.cases;
});

const onePassText = computed(() => {
  const reviewFailNum = +caseInfo.value?.reviewFailNum;
  const reviewStatus = caseInfo.value?.reviewStatus?.value;
  if (reviewStatus === 'PASSED' && reviewFailNum === 0) {
    return t('status.yes');
  }

  return t('status.no');
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a7" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.7') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.functionCase.review.title') }}</span>
    </h1>

    <div class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.functionCase.review.fields.isReview') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ caseInfo?.review?t('status.yes'):t('status.no') }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.functionCase.review.fields.reviewCount') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ caseInfo?.reviewNum }}
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.functionCase.review.fields.reviewFailCount') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ caseInfo?.reviewFailNum }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.functionCase.review.fields.oneTimeReviewPass') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ onePassText }}
        </div>
      </div>

      <div class="flex">
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.functionCase.review.fields.reviewRemark') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ caseInfo?.reviewRemark }}
        </div>
        <div
          class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
