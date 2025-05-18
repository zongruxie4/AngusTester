<script setup lang="ts">
import { computed } from 'vue';
import { Grid } from '@xcan-angus/vue-ui';

import { CaseInfo } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const oneReviewPass = computed(() => {
  if (props.dataSource?.reviewNum && Number(props.dataSource.reviewNum) > 0) {
    return props.dataSource?.reviewFailNum === '0' && props.dataSource?.reviewStatus?.value === 'PASSED' ? '是' : '否';
  }
  return '--';
});

const reviewInfoColumns = [
  [
    {
      label: '是否开启评审',
      dataIndex: 'review',
      customRender: ({ text }) => text ? '是' : '否'
    },
    {
      label: '评审次数',
      dataIndex: 'reviewNum'
    },
    {
      label: '评审失败次数',
      dataIndex: 'reviewFailNum'
    },
    {
      label: '是否一次性通过',
      dataIndex: 'oneReviewPass'
    },
    {
      label: '评审意见',
      dataIndex: 'reviewRemark'
    }
  ]
];
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">评审信息</div>
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
