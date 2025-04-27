<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';

import { ReportContent } from '../../PropsType';

type Props = {
  dataSource: ReportContent['content']['cases']['burnDownCharts'];
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const Chart = defineAsyncComponent(() => import('./chart.vue'));

</script>
<template>
  <Chart
    :dataSource="props.dataSource?.NUM"
    title="用例数"
    class="mb-4" />
  <Chart :dataSource="props.dataSource?.WORKLOAD" title="工作量" />
</template>
