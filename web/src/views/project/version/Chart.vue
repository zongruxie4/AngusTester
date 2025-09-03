<script lang="ts" setup>
import { defineExpose } from 'vue';
import { useChart } from './composables/useChart';
import type { ChartProps } from './types';

/**
 * Version progress chart component
 * Displays completion rate and workload progress using ECharts
 */

// Component props with default values
const props = withDefaults(defineProps<ChartProps>(), {
  chart1Value: () => ({
    title: '--%',
    value: [{ value: 0 }, { value: 0 }]
  }),
  chart2Value: () => ({
    title: '--%',
    value: [{ value: 0 }, { value: 0 }]
  })
});

// Use chart composable for chart management
const { completedRef, completedWorkloadRef, resizeCharts } = useChart(props);

// Expose resize method for parent components
defineExpose({
  resize: resizeCharts
});
</script>
<template>
  <div class="flex">
    <div ref="completedRef" class="flex-1 h-40"></div>
    <div ref="completedWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
