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

const Chart = defineAsyncComponent(() => import('./chart.vue'));

</script>
<template>
  <Chart
    :dataSource="props.dataSource?.NUM"
    :title="t('reportPreview.projectProgress.taskSummary.burndownChart.taskCount')"
    class="mb-4" />
  <Chart :dataSource="props.dataSource?.WORKLOAD" :title="t('reportPreview.projectProgress.taskSummary.burndownChart.workload')" />
</template>
