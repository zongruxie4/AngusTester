<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

import { StatisticsInfo } from '../../types';

type Props = {
  dataSource: StatisticsInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const TaskTypeChart = defineAsyncComponent(() => import('./TaskTypeChart.vue'));
const TestTypeChart = defineAsyncComponent(() => import('./TestTypeChart.vue'));
</script>

<template>
  <div class="w-125 flex-shrink-0 flex items-center">
    <TestTypeChart :dataSource="props.dataSource" class="chart-item" />
    <TaskTypeChart :dataSource="props.dataSource" class="chart-item" />
  </div>
</template>
<style scoped>
.chart-item {
  width: calc((100% - 0px)/2);
}
</style>
