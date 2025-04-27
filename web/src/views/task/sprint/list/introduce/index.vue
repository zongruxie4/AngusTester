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
    <div class="text-3.5 font-semibold mb-2.5">关于迭代</div>
    <div class="mb-6">
      <div>迭代可以帮助团队更高效地规划和执行工作，提高了团队的协作、灵活性和适应性，确保项目按时交付并满足客户需求。</div>
      <div>通过持续迭代改进和频繁交付，团队可以不断提升产品质量和客户满意度。主要好处如下：</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">组织和规划工作</div>
            <div>将项目分解成较短的时间周期（通常为1-4周），设定明确的短期目标，使团队更容易专注于当前任务。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">增强团队协作</div>
            <div>通过迭代，开发、测试、设计等不同角色的团队成员可以紧密协作，共同解决问题和完成任务，促进跨职能团队的合作。</div>
          </div>
        </div>

        <div v-show="!isCol2" class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">提高透明度和沟通</div>
            <div>实时展示团队的工作进度和剩余工作量，让团队和利益相关者可以清晰了解当前迭代的状态。</div>
          </div>
        </div>
      </div>
      <div v-show="isCol2" class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">提高透明度和沟通</div>
            <div>实时展示团队的工作进度和剩余工作量，让团队和利益相关者可以清晰了解当前迭代的状态。</div>
          </div>
        </div>
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">提高灵活性和适应性</div>
            <div>每个迭代结束时，团队可以回顾和评估工作的完成情况，获取反馈，并在下一次迭代中做出调整，快速响应变化和改进。</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div v-show="!isCol2" class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">提高灵活性和适应性</div>
            <div>每个迭代结束时，团队可以回顾和评估工作的完成情况，获取反馈，并在下一次迭代中做出调整，快速响应变化和改进。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">提升产品质量</div>
            <div>在每个迭代结束时，团队可以交付一个可用的产品增量，进行测试和验证，确保产品的稳定性和质量。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">增强客户满意度</div>
            <div>过短周期的迭代，团队可以频繁地向客户交付工作成果，获取客户的及时反馈，确保产品开发符合客户需求和期望。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
