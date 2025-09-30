<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

const { t } = useI18n();

const containerRef = ref();
const isTwoColumnLayout = ref(false);

/**
 * Element resize detector instance for responsive layout handling.
 * Uses scroll strategy for better performance when detecting container size changes.
 */
const resizeDetector = elementResizeDetector({ strategy: 'scroll' });

/**
 * Handles container resize events to adjust layout.
 * Switches between single and two-column layout based on container width.
 * Uses 800px as the breakpoint for layout switching.
 */
const handleContainerResize = () => {
  const containerWidth = containerRef.value.clientWidth;
  isTwoColumnLayout.value = containerWidth <= 800;
};

// Lifecycle Hooks
onMounted(() => {
  // Set up resize listener
  resizeDetector.listenTo(containerRef.value, handleContainerResize);

  // Set initial layout based on container width
  isTwoColumnLayout.value = containerRef.value.clientWidth <= 600;
});

onBeforeUnmount(() => {
  // Clean up resize listener
  resizeDetector.removeListener(containerRef.value, handleContainerResize);
});

</script>
<template>
  <div ref="containerRef">
    <div class="text-3.5 font-semibold mb-2.5">{{ t('taskSprint.aboutSprint') }}</div>
    <div class="text-3.5 mb-6 font-serif">{{ t('taskSprint.aboutSprintDesc') }}</div>

    <div class="space-y-6 font-serif">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.organizeWork') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.organizeWorkDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.enhanceCollaboration') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.enhanceCollaborationDesc') }}</div>
          </div>
        </div>

        <div v-show="!isTwoColumnLayout" class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveTransparency') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.improveTransparencyDesc') }}</div>
          </div>
        </div>
      </div>

      <div v-show="isTwoColumnLayout" class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveTransparency') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.improveTransparencyDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveFlexibility') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.improveFlexibilityDesc') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div v-show="!isTwoColumnLayout" class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveFlexibility') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.improveFlexibilityDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveQuality') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.improveQualityDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.enhanceSatisfaction') }}</div>
            <div class="text-3.5">{{ t('taskSprint.benefits.enhanceSatisfactionDesc') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
