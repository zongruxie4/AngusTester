<script setup lang="ts">
import { usePieChart } from './composables/usePieChart';
import type { PieChartProps } from './types';

// Define component props with proper typing
const props = withDefaults(defineProps<PieChartProps>(), {
  color: () => [],
  title: '',
  type: '',
  total: 0,
  dataSource: () => []
});

// Use pie chart composable for chart functionality
const { chartsRef } = usePieChart(props);
</script>
<template>
  <div class="relative w-full h-full leading-5">
    <div ref="chartsRef" class="h-full"></div>
    <div class="mark-container text-center">
      <div class="text-3 text-center">{{ props.title }}</div>
      <div class="mt-1 text-3.5 text-theme-title">{{ props.total }}</div>
    </div>
  </div>
</template>
<style scoped>
.mark-container {
  position: absolute;
  top: 50%;
  left: 65px;
  transform: translate(-50%, -20px);
}
</style>
