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
  <div class="bg-white rounded-lg border border-gray-200 p-6">
    <div class="flex items-center mb-4">
      <Icon icon="icon-ceshixinxi" class="text-amber-500 mr-2" />
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t('caseReview.comp.testInfo.title') }}
      </h3>
    </div>

    <Grid
      :columns="testInfoColumns"
      :dataSource="props.caseInfo"
      :spacing="24"
      :marginBottom="6"
      labelSpacing="12px"
      font-size="14px"
      class="test-info-grid">
      <template #oneTestPass>
        <span
          class="px-2 py-1 text-xs font-medium rounded-full"
          :class="getOneTestPass === t('status.yes') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
          {{ getOneTestPass }}
        </span>
      </template>
      <template #testResult="{text}">
        <TestResult :value="text" />
      </template>
    </Grid>
  </div>
</template>

<style scoped>
:deep(.test-info-grid) {
  .ant-descriptions-item-label {
    @apply text-gray-600 font-medium;
  }

  .ant-descriptions-item-content {
    @apply text-gray-900;
  }
}
</style>
