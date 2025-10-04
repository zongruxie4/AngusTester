<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { ReviewStatus } from '@xcan-angus/vue-ui';
import { CaseDetail } from '@/views/test/types';

const { t } = useI18n();

interface Props {
  caseInfo?: CaseDetail
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

</script>
<template>
  <div class="bg-white rounded-lg p-6">
    <div class="space-y-4">
      <div class="flex items-center">
        <label class="w-26 text-gray-600 font-medium">{{ t('common.reviewStatus') }}：</label>
        <div class="flex-1">
          <ReviewStatus
            v-if="props.caseInfo?.reviewStatus"
            :value="props.caseInfo?.reviewStatus" />
        </div>
      </div>

      <div class="flex items-start">
        <label class="w-26 text-gray-600 font-medium mt-1">{{ t('testCaseReview.detail.columns.reviewOpinion') }}：</label>
        <div class="flex-1">
          <div v-if="props.caseInfo?.reviewRemark" class="bg-gray-50 rounded-lg p-3 text-gray-700 leading-relaxed">
            {{ props.caseInfo?.reviewRemark }}
          </div>
          <div v-else class="text-gray-400 italic">
            {{ t('common.none') }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
