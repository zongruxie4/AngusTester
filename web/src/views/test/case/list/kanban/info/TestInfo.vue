<script setup lang="ts">
import { computed } from 'vue';
import { Grid } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { CaseInfoEditProps } from '@/views/test/case/list/types';

import TestResult from '@/components/TestResult/index.vue';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const oneTestPass = computed(() => {
  if (props.dataSource?.testNum && Number(props.dataSource.testNum) > 0) {
    return props.dataSource?.testFailNum === '0' && props.dataSource?.testResult?.value === 'PASSED' ? t('status.yes') : t('status.no');
  }
  return '--';
});

const testInfoColumns = [
  [
    {
      label: '测试次数',
      dataIndex: 'testNum'
    },
    {
      label: '失败次数',
      dataIndex: 'testFailNum'
    },
    {
      label: '是否一次性通过',
      dataIndex: 'oneTestPass'
    },
    {
      label: '结果备注',
      dataIndex: 'testRemark'
    }
  ]
];
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('testCase.kanbanView.testInfo') }}
    </div>

    <Grid
      :columns="testInfoColumns"
      :dataSource="props.dataSource"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px">
      <template #oneTestPass>
        {{ oneTestPass }}
      </template>
      <template #testResult="{text}">
        <TestResult :value="text" />
      </template>
    </Grid>
  </div>
</template>
