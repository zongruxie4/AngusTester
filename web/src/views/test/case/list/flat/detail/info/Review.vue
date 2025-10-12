<script setup lang="ts">
import { computed, ref } from 'vue';
import { Grid, ReviewStatus, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';
import { CaseActionAuth } from '@/views/test/case/types';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  actionAuth?: CaseActionAuth[];
  columns?: any[][];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  actionAuth: () => ([]),
  columns: () => []
});

const { t } = useI18n();

const reviewExpand = ref(true);

/**
 * <p>Calculates if the review passed on first attempt.</p>
 * <p>Returns 'Yes' if review passed with no failures, 'No' otherwise, or '--' if no reviews.</p>
 * @returns Review pass status string
 */
const getOneReviewPass = computed(() => {
  if (props.dataSource?.reviewNum && Number(props.dataSource.reviewNum) > 0) {
    return props.dataSource?.reviewFailNum === 0 &&
    props.dataSource?.reviewStatus?.value === ReviewStatus.PASSED
      ? t('status.yes')
      : t('status.no');
  }
  return '--';
});
</script>
<template>
  <Toggle
    v-model:open="reviewExpand"
    :title="t('common.review')"
    class="mt-3.5">
    <Grid
      :columns="columns"
      :dataSource="dataSource"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px"
      class="pt-2 pl-5.5">
      <template #oneReviewPass>
        {{ getOneReviewPass }}
      </template>
    </Grid>
  </Toggle>
</template>
<style scoped>
:deep(.toggle-title) {
  @apply text-3.5;
}
</style>
