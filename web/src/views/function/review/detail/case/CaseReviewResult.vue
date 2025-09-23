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
  <div class="bg-white rounded-lg border border-gray-200 p-6">
    <div class="flex items-center mb-4">
      <Icon icon="icon-pingshenjieguo" class="text-green-500 mr-2" />
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t('caseReview.comp.caseReviewResult.title') }}
      </h3>
    </div>

    <div class="space-y-4">
      <div class="flex items-center">
        <label class="w-24 text-gray-600 font-medium">{{ t('caseReview.comp.caseReviewResult.reviewStatus') }}：</label>
        <div class="flex-1">
          <ReviewStatus
            v-if="props.caseInfo?.reviewStatus"
            :value="props.caseInfo?.reviewStatus" />
        </div>
      </div>

      <div class="flex items-start">
        <label class="w-24 text-gray-600 font-medium mt-1">{{ t('caseReview.comp.caseReviewResult.reviewOpinion') }}：</label>
        <div class="flex-1">
          <div v-if="props.caseInfo?.reviewRemark" class="bg-gray-50 rounded-lg p-3 text-gray-700 leading-relaxed">
            {{ props.caseInfo?.reviewRemark }}
          </div>
          <div v-else class="text-gray-400 italic">
            {{ t('caseReview.comp.caseReviewResult.noOpinion') }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
