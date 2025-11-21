<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

import { CaseCount } from '@/views/test/case/types';
import { DataSourceProps } from '@/types/types';

// Props Definition
const props = withDefaults(defineProps<DataSourceProps<CaseCount>>(), {
  dataSource: undefined
});

const ReviewStatusChart = defineAsyncComponent(() => import('./ReviewStatusChart.vue'));
const TestLayerChart = defineAsyncComponent(() => import('./TestLayerChart.vue'));
</script>

<template>
  <!-- Chart container with single chart component -->
  <div class="chart-container">
    <TestLayerChart :dataSource="props.dataSource" class="chart-item" />
    <ReviewStatusChart :dataSource="props.dataSource" class="chart-item" />
  </div>
</template>

<style scoped>
.chart-container {
  @apply w-120;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.chart-item {
  width: 50%;
}
</style>
