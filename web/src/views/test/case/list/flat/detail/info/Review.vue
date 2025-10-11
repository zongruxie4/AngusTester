<script setup lang="ts">
import { computed } from 'vue';
import { Grid, ReviewStatus } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  taskId?: number;
  actionAuth?: {[key: string]: any};
  columns?: any[][];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  taskId: undefined,
  actionAuth: () => ({}),
  columns: () => []
});

const { t } = useI18n();

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
</template>
