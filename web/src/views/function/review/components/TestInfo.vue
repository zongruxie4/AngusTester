<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid } from '@xcan-angus/vue-ui';

import TestResult from '@/components/TestResult/index.vue';

const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any}
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

const testInfoColumns = [
  [
    {
      label: t('caseReview.comp.testInfo.testCount'),
      dataIndex: 'testNum'
    },
    {
      label: t('caseReview.comp.testInfo.failCount'),
      dataIndex: 'testFailNum'
    },
    {
      label: t('caseReview.comp.testInfo.oneTimePass'),
      dataIndex: 'oneTestPass'
    },
    {
      label: t('caseReview.comp.testInfo.resultRemark'),
      dataIndex: 'testRemark'
    }
  ]
];

const getOneTestPass = computed(() => {
  if (props.caseInfo?.testNum && Number(props.caseInfo.testNum) > 0) {
    return props.caseInfo?.testFailNum === '0' && props.caseInfo?.testResult?.value === 'PASSED' ? t('status.yes') : t('status.no');
  }
  return '--';
});

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      {{ t('caseReview.comp.testInfo.title') }}
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
