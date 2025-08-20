<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

import { ReportContent } from '../../PropsType';

type Props = {
  dataSource: ReportContent['content']['tasks']['burnDownCharts'];
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const Chart = defineAsyncComponent(() => import('./Chart.vue'));

</script>
<template>
  <div class="space-y-4">
    <Chart :dataSource="props.dataSource?.NUM" :title="t('reportPreview.sprint.assigneeSummary.burndownChart.taskCount')" />
    <Chart :dataSource="props.dataSource?.WORKLOAD" :title="t('reportPreview.sprint.assigneeSummary.burndownChart.workload')" />
  </div>
</template>
