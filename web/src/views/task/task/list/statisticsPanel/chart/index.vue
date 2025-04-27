<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

import { ResourceInfo } from '../../PropsType';

type Props = {
  dataSource: ResourceInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const TaskTypeChart = defineAsyncComponent(() => import('./taskTypeChart.vue'));
const TestTypeChart = defineAsyncComponent(() => import('./testTypeChart.vue'));
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
