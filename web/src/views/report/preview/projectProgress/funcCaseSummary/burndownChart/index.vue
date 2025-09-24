<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../PropsType';

type Props = {
  dataSource: ReportContent['content']['cases']['burnDownCharts'];
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
    :title="t('chart.burndown.countBurndown')"
    class="mb-4" />
  <Chart :dataSource="props.dataSource?.WORKLOAD" :title="t('chart.burndown.workloadBurndown')" />
</template>
