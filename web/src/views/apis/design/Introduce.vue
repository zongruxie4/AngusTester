<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const erd = elementResizeDetector({ strategy: 'scroll' });
const wrapperRef = ref();

const handleCol = () => {
  const clientWidth = wrapperRef.value.clientWidth;
  isCol2.value = clientWidth <= 800;
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
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('apiDesign.home.introduceTitle') }}
    </div>
    <div class="mb-2">
      {{ t('apiDesign.home.introduceContent') }}
    </div>
  </div>
</template>
