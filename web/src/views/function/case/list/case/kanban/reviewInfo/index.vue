<script setup lang="ts">
import { computed } from 'vue';
import { Grid } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { CaseInfo } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const oneReviewPass = computed(() => {
  if (props.dataSource?.reviewNum && Number(props.dataSource.reviewNum) > 0) {
    return props.dataSource?.reviewFailNum === '0' && props.dataSource?.reviewStatus?.value === 'PASSED' ? t('status.yes') : t('status.no');
  }
  return '--';
});

const reviewInfoColumns = [
  [
    {
      label: t('functionCase.kanbanView.reviewInfoGrid.enableReview'),
      dataIndex: 'review',
      customRender: ({ text }) => text ? t('status.yes') : t('status.no')
    },
    {
      label: t('functionCase.kanbanView.reviewInfoGrid.reviewCount'),
      dataIndex: 'reviewNum'
    },
    {
      label: t('functionCase.kanbanView.reviewInfoGrid.reviewFailCount'),
      dataIndex: 'reviewFailNum'
    },
    {
      label: t('functionCase.kanbanView.reviewInfoGrid.oneTimePass'),
      dataIndex: 'oneReviewPass'
    },
    {
      label: t('functionCase.kanbanView.reviewInfoGrid.reviewOpinion'),
      dataIndex: 'reviewRemark'
    }
  ]
];
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('functionCase.kanbanView.reviewInfoGrid.title') }}</div>
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
