<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { ResourceCount } from '../types';
import { usePieChart } from './composables/usePieChart';

const { t } = useI18n();

/**
 * Component props interface
 */
type Props = {
  /** Resource information containing script statistics */
  dataSource: ResourceCount;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

/**
 * Initialize pie chart composable with data source
 */
const {
  containerRef,
  domId,
  initialize,
  dataSource
} = usePieChart(props.dataSource);

/**
 * Initialize chart functionality when component mounts
 */
onMounted(() => {
  initialize();
  watch(() => props.dataSource, (newValue) => {
    dataSource.value = newValue;
  });
});
</script>

<template>
  <div class="relative leading-5 text-3  ml-10">
    <!-- Chart container -->
    <div
      :id="domId"
      ref="containerRef"
      class="w-80 h-34">
    </div>

    <!-- Center overlay with total count -->
    <div class="mark-container">
      <div class="text-center">{{ t('scriptHome.pieChart.title') }}</div>
      <div class="text-3.5 text-center font-semibold">{{ props.dataSource?.totalScriptNum }}</div>
    </div>
  </div>
</template>

<style scoped>
.mark-container {
  position: absolute;
  top: 50%;
  left: 30%;
  transform: translate(-50%, -50%);
}
</style>
