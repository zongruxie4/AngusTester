<script lang="ts" setup>
import { computed } from 'vue';
import { Grid, TestResult } from '@xcan-angus/vue-ui';

interface Props {
  caseInfo?: {[key: string]: any}
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
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

const getOneTestPass = computed(() => {
  if (props.caseInfo?.testNum && Number(props.caseInfo.testNum) > 0) {
    return props.caseInfo?.testFailNum === '0' && props.caseInfo?.testResult?.value === 'PASSED' ? '是' : '否';
  }
  return '--';
});

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      测试信息
    </div>
    <Grid
      :columns="testInfoColumns"
      :dataSource="props.caseInfo"
      :spacing="20"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px"
      class="">
      <template #oneTestPass>
        {{ getOneTestPass }}
      </template>
      <template #testResult="{text}">
        <TestResult :value="text" />
      </template>
    </Grid>
  </div>
</template>
