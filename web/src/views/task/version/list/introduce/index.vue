<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import elementResizeDetector from 'element-resize-detector';

interface Props {
  showFunc: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showFunc: true
});

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
    <div class="text-3.5 font-semibold mb-2.5">关于版本</div>
    <div class="mb-6">
      <div>版本对应软件系统的版本号，它精确标识任务、用例与软件发布版本的对应关系，支持灵活规划，确保团队聚焦于当前版本的关键任务和用例。通过直观的进展视图，项目经理可迅速识别潜在问题。将版本视为交付物，有助于团队更好地组织工作、跟踪进度，确保每个版本都代表有价值且可验证的产品增量。主要作用如下：</div>
    </div>

    <div v-if="props.showFunc" class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">版本标识和跟踪</div>
            <div>准确标识每个任务或用例对应的应用软件版本。帮助团队快速了解特定功能或修复在哪个版本中实现。</div>
          </div>
        </div>
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">版本规划和目标导向</div>
            <div>提供支持灵活的版本规划，提高团队焦点，确保所有计划的功能和修复都朝着目标版本稳步推进，并且集中于当前版本的关键任务和测试用例。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">版本进展可视化</div>
            <div>提供清晰的版本进展视图，直观展示每个版本的工作量和完成进度。帮助项目经理和团队成员快速识别潜在的延迟或问题。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">可验证的交付物</div>
            <div>将发布版本视为交付物，有效管理发布版本，团队可以更好地组织工作、跟踪进度，并确保每个版本都代表了有价值且可验证的产品增量。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
