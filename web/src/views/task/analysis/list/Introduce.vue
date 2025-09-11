<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

// Composables and External Dependencies
const { t } = useI18n();

/**
 * Element resize detector instance for monitoring container size changes
 */
const elementResizeDetectorInstance = elementResizeDetector({ strategy: 'scroll' });

/**
 * Reference to the wrapper element
 */
const containerWrapperRef = ref();

/**
 * Flag indicating if the layout should use 2-column mode based on container width
 */
const shouldUseTwoColumnLayout = ref(false);

/**
 * Handle container width changes and update layout accordingly
 */
const handleContainerWidthChange = () => {
  const containerWidth = containerWrapperRef.value.clientWidth;
  shouldUseTwoColumnLayout.value = containerWidth <= 800;
};

// Lifecycle Hooks
onMounted(() => {
  // Start monitoring container size changes
  elementResizeDetectorInstance.listenTo(containerWrapperRef.value, handleContainerWidthChange);

  // Set initial layout state based on current width
  shouldUseTwoColumnLayout.value = containerWrapperRef.value.clientWidth < 600;
});

onBeforeUnmount(() => {
  // Clean up resize listener
  elementResizeDetectorInstance.removeListener(containerWrapperRef.value, handleContainerWidthChange);
});

</script>
<template>
  <div ref="containerWrapperRef">
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('taskAnalysis.aboutAnalysis') }}
    </div>
    <div class="mb-6">
      <div>{{ t('taskAnalysis.aboutAnalysisDesc') }}</div>
    </div>
  </div>
</template>
