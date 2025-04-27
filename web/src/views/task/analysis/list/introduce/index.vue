<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import elementResizeDetector from 'element-resize-detector';

const erd = elementResizeDetector({ strategy: 'scroll' });
const wrapperRef = ref();

const handleCol = () => {
  const clientWidth = wrapperRef.value.clientWidth;
  if (clientWidth > 800) {
    isCol2.value = false;
  } else {
    isCol2.value = true;
  }
};

const isCol2 = ref(false);

onMounted(() => {
  erd.listenTo(wrapperRef.value, handleCol);
  isCol2.value = wrapperRef.value.clientWidth < 600;
});

onBeforeUnmount(() => {
  erd.removeListener(wrapperRef.value, handleCol);
});

</script>
<template>
  <div ref="wrapperRef">
    <div class="text-3.5 font-semibold mb-2.5">关于分析</div>
    <div class="mb-6">
      <div>用于提供任务实时数据分析、促进数据驱动的决策，确保信息透明，增强团队协作和问题追踪，提升项目研发管理效率。AngusTester提供默认分析模版，如燃尽图、积压任务、风险评估等，帮助用户快速查看仪表板和关键性能指标（KPI），同时用户也可以根据业务关注点自定义分析仪表板。</div>
    </div>
  </div>
</template>
