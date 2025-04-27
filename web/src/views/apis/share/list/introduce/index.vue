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
    <div class="text-3.5 font-semibold mb-2.5">关于分享</div>
    <div class="mb-2">
      <div>
        当需要与外部合作伙伴、客户或其他团队进行接口集成，可以通过“分享”功能将接口信息传递给他们，简化沟通流程。分享包含详细接口说明信息，包括接口服务器、认证、接口地址、请求格式和响应格式等，确保所有人对接口有一致的理解。
      </div>
    </div>
  </div>
</template>
