<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

import { ReportContent } from '../../PropsType';

type Props = {
  dataSource: ReportContent['content']['cases']['burnDownCharts'];
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const Chart = defineAsyncComponent(() => import('./chart.vue'));
const { t } = useI18n();
</script>
<template>
  <div class="space-y-4">
    <Chart :dataSource="props.dataSource?.NUM" :title="t('reportPreview.functionPlan.caseSummary.burndownChart.caseCount')" />
    <Chart :dataSource="props.dataSource?.WORKLOAD" :title="t('reportPreview.functionPlan.caseSummary.burndownChart.workload')" />
  </div>
</template>
