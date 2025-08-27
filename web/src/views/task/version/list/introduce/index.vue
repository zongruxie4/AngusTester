<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { useI18n } from 'vue-i18n';

interface Props {
  showFunc: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showFunc: true
});

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
  <div ref="wrapperRef" class="space-y-4">
    <!-- 标题和描述 -->
    <div class="space-y-2">
      <div class="flex items-center space-x-2">
        <div class="w-1 h-4 bg-gradient-to-b from-green-500 to-green-600 rounded-full"></div>
        <span class="text-xs font-semibold text-gray-600">{{ t('taskVersion.introduce.title') }}</span>
      </div>
      <div class="text-xs text-gray-600 leading-relaxed pl-3">
        {{ t('taskVersion.introduce.description') }}
      </div>
    </div>

    <!-- 功能特性 -->
    <div v-if="props.showFunc" class="space-y-4">
      <div class="flex items-center space-x-2">
        <div class="w-1 h-4 bg-gradient-to-b from-green-500 to-green-600 rounded-full"></div>
        <span class="text-xs font-semibold text-gray-600">核心功能</span>
      </div>
      <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-4 gap-6">
        <!-- 版本跟踪 -->
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
          <div class="flex items-start space-x-3">
            <img src="./images/1.png" class="w-8 h-8 flex-shrink-0 rounded-md">
            <div class="space-y-2">
              <h3 class="text-xs font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">
                {{ t('taskVersion.introduce.features.versionTracking.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('taskVersion.introduce.features.versionTracking.description') }}
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
                {{ t('taskVersion.introduce.features.versionPlanning.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('taskVersion.introduce.features.versionPlanning.description') }}
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
                {{ t('taskVersion.introduce.features.versionProgress.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('taskVersion.introduce.features.versionProgress.description') }}
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
                {{ t('taskVersion.introduce.features.verifiableDeliverables.title') }}
              </h3>
              <p class="text-xs text-gray-600 leading-relaxed">
                {{ t('taskVersion.introduce.features.verifiableDeliverables.description') }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
