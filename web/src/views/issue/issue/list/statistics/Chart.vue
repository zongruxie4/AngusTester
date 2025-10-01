<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

import { TaskCount } from '../../types';

/**
 * Component props interface for Chart component
 */
type Props = {
  dataSource: TaskCount;
}

// Props Definition
const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

/**
 * Lazy-loaded TaskTypeChart component for displaying task type distribution
 */
const TaskTypeChart = defineAsyncComponent(() => import('./TaskTypeChart.vue'));

/**
 * Lazy-loaded TestTypeChart component for displaying test type distribution
 */
const TestTypeChart = defineAsyncComponent(() => import('./TestTypeChart.vue'));
</script>

<template>
  <!-- Chart container with two side-by-side chart components -->
  <div class="chart-container">
    <TestTypeChart :dataSource="props.dataSource" class="chart-item" />
    <TaskTypeChart :dataSource="props.dataSource" class="chart-item" />
  </div>
</template>
<style scoped>
.chart-container {
  @apply w-125;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.chart-item {
  width: calc(100% / 2);
}
</style>
