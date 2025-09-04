<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

import { ResourceInfo } from '../types';
import { usePieChart } from './composables/usePieChart';

const { t } = useI18n();

/**
 * Component props interface
 */
type Props = {
  /** Resource information containing script statistics */
  dataSource: ResourceInfo;
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
  initialize
} = usePieChart(props.dataSource);

/**
 * Initialize chart functionality when component mounts
 */
onMounted(() => {
  initialize();
});
</script>

<template>
  <div class="relative leading-5 text-3  ml-10">
    <!-- TODO 切换顶部菜单、刷新页面会出现饼图图例数据不展示问题； 修改快速查询条件不生效 -->
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
