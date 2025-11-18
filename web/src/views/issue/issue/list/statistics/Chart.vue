<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

import { TaskCount } from '../../types';
import { DataSourceProps } from '@/types/types';

// Props Definition
const props = withDefaults(defineProps<DataSourceProps<TaskCount>>(), {
  dataSource: undefined
});

const TaskTypeChart = defineAsyncComponent(() => import('./TaskTypeChart.vue'));
// const TestTypeChart = defineAsyncComponent(() => import('./TestTypeChart.vue'));
</script>

<template>
  <!-- Chart container with two side-by-side chart components -->
  <div class="chart-container">
    <TaskTypeChart :dataSource="props.dataSource" class="chart-item" />
    <!-- <TestTypeChart :dataSource="props.dataSource" class="chart-item" /> -->
  </div>
</template>
<style scoped>
.chart-container {
  @apply w-80;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.chart-item {
  width: 100%;
}
</style>
