<script lang="ts" setup>
import { Grid } from '@xcan-angus/vue-ui';

interface Props {
  caseInfo?: {[key: string]: any}
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

const peopleInfoColumns = [
  [
    {
      label: '测试人',
      dataIndex: 'testerName'
    },
    {
      label: '开发人',
      dataIndex: 'developerName'
    },
    {
      label: '评审人',
      dataIndex: 'reviewerName'
    },
    {
      label: '添加人',
      dataIndex: 'createdByName'
    },
    {
      label: '最后修改人',
      dataIndex: 'lastModifiedByName'
    }]
];

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      人员信息
    </div>
    <Grid
      :columns="peopleInfoColumns"
      :dataSource="props.caseInfo"
      :spacing="20"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px"
      class="">
    </Grid>
  </div>
</template>
