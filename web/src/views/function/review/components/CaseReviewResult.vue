<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { ReviewStatus } from '@xcan-angus/vue-ui';

const { t } = useI18n();

interface Props {
  caseInfo?: {
    review: boolean;
    reviewRemark?: string;
    reviewStatus: {
      value: string;
      message: string;
    }
  }
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      {{ t('caseReview.comp.caseReviewResult.title') }}
    </div>

    <div class="flex">
      <label class="w-20">{{ t('caseReview.comp.caseReviewResult.reviewStatus') }}：</label>
      <div class="flex-1">
        <ReviewStatus
          v-if="props.caseInfo?.reviewStatus"
          :value="props.caseInfo?.reviewStatus" />
      </div>
    </div>

    <div class="flex">
      <label class="w-20">{{ t('caseReview.comp.caseReviewResult.reviewOpinion') }}：</label>
      <div class="flex-1">{{ props.caseInfo?.reviewRemark || t('caseReview.comp.caseReviewResult.noOpinion') }}</div>
    </div>
  </div>
</template>
