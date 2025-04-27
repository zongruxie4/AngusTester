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
    <!-- {{ tip }} -->
    <div class="text-3.5 font-semibold mb-2.5">关于监控</div>
    <div class="mb-6">
      <div>
        场景监控可以帮助我们从接口维度掌握系统或者业务状态，及时发现并解决潜在问题，从而提高系统的可靠性和用户满意度，为业务的稳定运营提供有力支持。主要应用场景如下：
      </div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">API延迟监测</div>
            <div>跟踪API的响应时间，确保访问体验。例如：监控支付API的平均响应时间，确保不超过500ms。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">检查系统健康状态</div>
            <div>以快速、简单的方式来确定系统是否正常运行。 例如：验证系统或服务健康检查端点(/health)中组件的运行状态。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">API功能正确性检查</div>
            <div>定期测试API的功能正确性，确保预期行为。 例如：自动化测试用户注册API，验证账户创建流程。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">关键业务流程监控</div>
            <div>监控核心业务流程的完整性和正确性。 例如：截止到当前时间支付订单和支付金额在业务上是否一致。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
