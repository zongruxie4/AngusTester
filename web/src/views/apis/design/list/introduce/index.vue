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
    <div class="text-3.5 font-semibold mb-2.5">关于设计</div>
    <div class="mb-2">
      <div>
        用于 OpenAPI 规范的接口设计，满足接口测试和文档生成的需求。图形化界面适合不熟悉 OpenAPI规范 的用户，而熟悉 OpenAPI 规范的用户则推荐使用 Swagger Editor，能更高效地完成设计。
      </div>
    </div>
  </div>
</template>
