<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

// Component setup
const { t } = useI18n();

// Reactive state
const isTwoColumnLayout = ref(false);
const containerRef = ref();

// Resize detection
const resizeDetector = elementResizeDetector({ strategy: 'scroll' });

/**
 * Handle container resize to determine layout columns.
 */
const handleContainerResize = () => {
  const containerWidth = containerRef.value.clientWidth;
  const singleColumnThreshold = 800;

  isTwoColumnLayout.value = containerWidth <= singleColumnThreshold;
};

// Lifecycle hooks
onMounted(() => {
  resizeDetector.listenTo(containerRef.value, handleContainerResize);

  // Set initial layout based on container width
  const initialWidthThreshold = 600;
  isTwoColumnLayout.value = containerRef.value.clientWidth < initialWidthThreshold;
});

onBeforeUnmount(() => {
  resizeDetector.removeListener(containerRef.value, handleContainerResize);
});

</script>
<template>
  <div ref="containerRef">
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('functionAnalysis.introduce.title') }}
    </div>
    <div class="mb-6 font-serif">
      <div>{{ t('functionAnalysis.introduce.description') }}</div>
    </div>
  </div>
</template>
