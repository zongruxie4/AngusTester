<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { useI18n } from 'vue-i18n';
import type { IntroduceProps } from './types';

// Component props with default values
const props = withDefaults(defineProps<IntroduceProps>(), {
  showFunc: true
});

const { t } = useI18n();

// Element resize detector for responsive layout
const erd = elementResizeDetector({ strategy: 'scroll' });
const wrapperRef = ref<HTMLElement>();

/**
 * Handle column layout changes based on container width
 * Switches between single and multi-column layout for responsive design
 */
const handleCol = (): void => {
  const clientWidth = wrapperRef.value?.clientWidth || 0;
  if (clientWidth > 800) {
    isCol2.value = false;
  } else {
    isCol2.value = true;
  }
};

// Responsive layout state
const isCol2 = ref(false);

// Setup resize detection on mount
onMounted(() => {
  if (wrapperRef.value) {
    erd.listenTo(wrapperRef.value, handleCol);
    isCol2.value = wrapperRef.value.clientWidth < 600;
  }
});

// Cleanup resize detection on unmount
onBeforeUnmount(() => {
  if (wrapperRef.value) {
    erd.removeListener(wrapperRef.value, handleCol);
  }
});
</script>
<template>
  <div ref="wrapperRef" class="space-y-4">
    <!-- 标题和描述 -->
    <div class="space-y-2">
      <div class="flex items-center space-x-2">
        <span class="text-3.5 font-semibold mb-1.5">{{ t('version.introduce.title') }}</span>
      </div>
      <div class="mb-6">
        {{ t('version.introduce.description') }}
      </div>
    </div>

    <!-- 功能特性 -->
    <div v-if="props.showFunc">
      <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-4 gap-6">
        <!-- 版本跟踪 -->
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
          <div class="flex items-start space-x-3">
            <img src="./images/1.png" class="w-8 h-8 flex-shrink-0 rounded-md">
            <div class="space-y-2">
              <h3 class="text-xs font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">
                {{ t('version.introduce.features.versionTracking.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('version.introduce.features.versionTracking.description') }}
              </p>
            </div>
          </div>
        </div>

        <!-- 版本规划 -->
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
          <div class="flex items-start space-x-3">
            <img src="./images/2.png" class="w-8 h-8 flex-shrink-0 rounded-md">
            <div class="space-y-2">
              <h3 class="text-xs font-semibold text-gray-800 group-hover:text-purple-600 transition-colors">
                {{ t('version.introduce.features.versionPlanning.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('version.introduce.features.versionPlanning.description') }}
              </p>
            </div>
          </div>
        </div>

        <!-- 版本进度 -->
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
          <div class="flex items-start space-x-3">
            <img src="./images/3.png" class="w-8 h-8 flex-shrink-0 rounded-md">
            <div class="space-y-2">
              <h3 class="text-xs font-semibold text-gray-800 group-hover:text-indigo-600 transition-colors">
                {{ t('version.introduce.features.versionProgress.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('version.introduce.features.versionProgress.description') }}
              </p>
            </div>
          </div>
        </div>

        <!-- 可验证交付物 -->
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
          <div class="flex items-start space-x-3">
            <img src="./images/4.png" class="w-8 h-8 flex-shrink-0 rounded-md">
            <div class="space-y-2">
              <h3 class="text-xs font-semibold text-gray-800 group-hover:text-orange-600 transition-colors">
                {{ t('version.introduce.features.verifiableDeliverables.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('version.introduce.features.verifiableDeliverables.description') }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
