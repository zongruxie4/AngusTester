<script lang="ts" setup>
import { ReviewStatus } from '@xcan-angus/vue-ui';

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
      评审信息
    </div>

    <div class="flex">
      <label class="w-20">评审状态：</label>
      <div class="flex-1">
        <ReviewStatus
          v-if="props.caseInfo?.reviewStatus"
          :value="props.caseInfo?.reviewStatus" />
      </div>
    </div>

    <div class="flex">
      <label class="w-20">评审意见：</label>
      <div class="flex-1">{{ props.caseInfo?.reviewRemark || '无' }}</div>
    </div>
  </div>
</template>
