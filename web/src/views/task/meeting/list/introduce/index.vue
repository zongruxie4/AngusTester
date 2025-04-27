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
    <div class="text-3.5 font-semibold mb-2.5">关于会议</div>
    <div class="mb-6">
      <div>会议在敏捷框架中起到关键作用，通过定期的沟通、反馈和反思，帮助团队持续改进，提高工作效率，确保项目能够灵活应对变化。支持会议类型如下：</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">产品梳理会</div>
            <div>确认产品的长期愿景和近期目标，确保团队对产品方向的一致理解。对Backlog进行审查和梳理，确保其优先级和可行性。参与者包括产品负责人、开发团队、Scrum Master、相关利益相关者(市场、销售团队等)。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">迭代计划会议</div>
            <div>确定在即将到来的迭代(Sprint)中要完成的工作。产品负责人展示产品中Backlog，团队讨论并估算这些项目，决定在迭代中可以完成的工作量。参与者包括Scrum Master、产品负责人和开发团队。</div>
          </div>
        </div>

        <div v-show="!isCol2" class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">每日站会</div>
            <div>实时展示团队的工作进度和剩余工作量，让团队和利益相关者可以清晰了解当前迭代的状态，促进团队沟通和协调。</div>
          </div>
        </div>
      </div>
      <div v-show="isCol2" class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">每日站会</div>
            <div>实时展示团队的工作进度和剩余工作量，让团队和利益相关者可以清晰了解当前迭代的状态，促进团队沟通和协调。</div>
          </div>
        </div>
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">迭代回顾会议</div>
            <div>展示迭代期间完成的工作，收集反馈，讨论改进点和后续计划。更新待办事项列表以反映新的需求或变更。参与者包括Scrum团队和相关利益相关者(如客户、管理层)。</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div v-show="!isCol2" class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">迭代回顾会议</div>
            <div>展示迭代期间完成的工作，收集反馈，讨论改进点和后续计划。更新待办事项列表以反映新的需求或变更。参与者包括Scrum团队和相关利益相关者(如客户、管理层)。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">迭代总结(反思)会</div>
            <div>反思迭代过程，识别改进的机会。分析迭代过程中的成功之处和需要改进的地方。制定改进计划，以提升团队的工作效率和协作。参与者包括Scrum团队(开发团队、Scrum Master和产品负责人)。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">其他会议</div>
            <div>敏捷过程中团队组织的其他类型会议，如：技术评审会议、需求评审会议、发布计划会议、障碍解决会议、团队建设会议等。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
