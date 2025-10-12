<script setup lang="ts">
import { computed, ref } from 'vue';
import { Grid, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';
import { CaseTestResult } from '@/enums/enums';
import TestResult from '@/components/TestResult/index.vue';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  actionAuth?: {[key: string]: any};
  columns?: any[][];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  actionAuth: () => ({}),
  columns: () => []
});

const { t } = useI18n();

const testExpand = ref(true);

/**
 * <p>Calculates if the test passed on first attempt.</p>
 * <p>Returns 'Yes' if test passed with no failures, 'No' otherwise, or '--' if no tests.</p>
 * @returns Test pass status string
 */
const getOneTestPass = computed(() => {
  if (props.dataSource?.testNum && Number(props.dataSource.testNum) > 0) {
    return props.dataSource?.testFailNum === 0 &&
    props.dataSource?.testResult?.value === CaseTestResult.PASSED
      ? t('status.yes')
      : t('status.no');
  }
  return '--';
});
</script>
<template>
  <Toggle
    v-model:open="testExpand"
    :title="t('common.test')"
    class="mt-3.5">
    <Grid
      :columns="columns"
      :dataSource="dataSource"
      :spacing="20"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px"
      class="pt-2 pl-5.5">
      <template #oneTestPass>
        {{ getOneTestPass }}
      </template>
      <template #testResult="{text}">
        <TestResult :value="text" />
      </template>
    </Grid>
  </Toggle>
</template>

<style scoped>
:deep(.toggle-title) {
  @apply text-3.5;
}
</style>
