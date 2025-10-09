<script setup lang="ts">
import { computed } from 'vue';
import { Grid } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ReviewStatus } from '@xcan-angus/infra';

import { CaseInfoEditProps } from '@/views/test/case/list/types';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const oneReviewPass = computed(() => {
  if (props.dataSource?.reviewNum && Number(props.dataSource.reviewNum) > 0) {
    return props.dataSource?.reviewFailNum === '0' && props.dataSource?.reviewStatus?.value === ReviewStatus.PASSED ? t('status.yes') : t('status.no');
  }
  return '--';
});

const reviewInfoColumns = [
  [
    {
      label: t('testCase.messages.enableReview'),
      dataIndex: 'review',
      customRender: ({ text }) => text ? t('status.yes') : t('status.no')
    },
    {
      label: t('common.counts.reviewCount'),
      dataIndex: 'reviewNum'
    },
    {
      label: t('common.counts.reviewFailCount'),
      dataIndex: 'reviewFailNum'
    },
    {
      label: t('common.counts.oneTimePassed'),
      dataIndex: 'oneReviewPass'
    },
    {
      label: t('common.reviewOpinion'),
      dataIndex: 'reviewRemark'
    }
  ]
];
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('common.reviewInfo') }}</div>
    <Grid
      :columns="reviewInfoColumns"
      :dataSource="props.dataSource"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px">
      <template #oneReviewPass>
        {{ oneReviewPass }}
      </template>
    </Grid>
  </div>
</template>
