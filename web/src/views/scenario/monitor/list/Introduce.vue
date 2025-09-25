<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

const erd = elementResizeDetector({ strategy: 'scroll' });

const { t } = useI18n();
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
  <div ref="wrapperRef" class="space-y-2 text-3.5">
    <div class="space-y-1">
      <div class="flex items-center space-x-2">
        <span class="text-3.5 font-semibold mb-1.5">{{ t('scenarioMonitor.introduce.aboutMonitor') }}</span>
      </div>
      <div class="mb-4">
        {{ t('common.description') }}
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-4 gap-6">
      <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
        <div class="flex items-start space-x-3">
          <img
            src="./images/1.png"
            class="w-12 h-12 flex-shrink-0 rounded-md"
            alt="API Delay Icon">
          <div class="space-y-1">
            <h3 class="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">
              {{ t('scenarioMonitor.introduce.scenarios.apiDelay.title') }}
            </h3>
            <p class="text-gray-600 leading-relaxed">
              {{ t('common.description') }}
            </p>
          </div>
        </div>
      </div>

      <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
        <div class="flex items-start space-x-3">
          <img
            src="./images/2.png"
            class="w-12 h-12 flex-shrink-0 rounded-md"
            alt="Health Check Icon">
          <div class="space-y-1">
            <h3 class="font-semibold text-gray-800 group-hover:text-purple-600 transition-colors">
              {{ t('scenarioMonitor.introduce.scenarios.healthCheck.title') }}
            </h3>
            <p class="text-gray-600 leading-relaxed">
              {{ t('common.description') }}
            </p>
          </div>
        </div>
      </div>

      <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
        <div class="flex items-start space-x-3">
          <img
            src="./images/3.png"
            class="w-12 h-12 flex-shrink-0 rounded-md"
            alt="API Functionality Icon">
          <div class="space-y-1">
            <h3 class="font-semibold text-gray-800 group-hover:text-indigo-600 transition-colors">
              {{ t('scenarioMonitor.introduce.scenarios.apiFunctionality.title') }}
            </h3>
            <p class="text-gray-600 leading-relaxed">
              {{ t('common.description') }}
            </p>
          </div>
        </div>
      </div>

      <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
        <div class="flex items-start space-x-3">
          <img
            src="./images/4.png"
            class="w-12 h-12 flex-shrink-0 rounded-md"
            alt="Business Process Icon">
          <div class="space-y-1">
            <h3 class="font-semibold text-gray-800 group-hover:text-orange-600 transition-colors">
              {{ t('scenarioMonitor.introduce.scenarios.businessProcess.title') }}
            </h3>
            <p class="text-gray-600 leading-relaxed">
              {{ t('common.description') }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
