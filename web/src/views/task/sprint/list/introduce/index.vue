<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

const { t } = useI18n();
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
    <div class="text-3.5 font-semibold mb-2.5">{{ t('taskSprint.aboutSprint') }}</div>
    <div class="mb-6">
      <div>{{ t('taskSprint.aboutSprintDesc') }}</div>
      <div>{{ t('taskSprint.aboutSprintDesc2') }}</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.organizeWork') }}</div>
            <div>{{ t('taskSprint.benefits.organizeWorkDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.enhanceCollaboration') }}</div>
            <div>{{ t('taskSprint.benefits.enhanceCollaborationDesc') }}</div>
          </div>
        </div>

        <div v-show="!isCol2" class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveTransparency') }}</div>
            <div>{{ t('taskSprint.benefits.improveTransparencyDesc') }}</div>
          </div>
        </div>
      </div>
      <div v-show="isCol2" class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveTransparency') }}</div>
            <div>{{ t('taskSprint.benefits.improveTransparencyDesc') }}</div>
          </div>
        </div>
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveFlexibility') }}</div>
            <div>{{ t('taskSprint.benefits.improveFlexibilityDesc') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div v-show="!isCol2" class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveFlexibility') }}</div>
            <div>{{ t('taskSprint.benefits.improveFlexibilityDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.improveQuality') }}</div>
            <div>{{ t('taskSprint.benefits.improveQualityDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskSprint.benefits.enhanceSatisfaction') }}</div>
            <div>{{ t('taskSprint.benefits.enhanceSatisfactionDesc') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
