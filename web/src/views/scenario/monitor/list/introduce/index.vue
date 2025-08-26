<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

const erd = elementResizeDetector({ strategy: 'scroll' });

const { t } = useI18n();
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
    <!-- {{ tip }} -->
    <div class="text-3.5 font-semibold mb-2.5">{{ t('scenarioMonitor.introduce.aboutMonitor') }}</div>
    <div class="mb-6">
      <div>
        {{ t('scenarioMonitor.introduce.description') }}
      </div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('scenarioMonitor.introduce.scenarios.apiDelay.title') }}</div>
            <div>{{ t('scenarioMonitor.introduce.scenarios.apiDelay.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('scenarioMonitor.introduce.scenarios.healthCheck.title') }}</div>
            <div>{{ t('scenarioMonitor.introduce.scenarios.healthCheck.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('scenarioMonitor.introduce.scenarios.apiFunctionality.title') }}</div>
            <div>{{ t('scenarioMonitor.introduce.scenarios.apiFunctionality.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('scenarioMonitor.introduce.scenarios.businessProcess.title') }}</div>
            <div>{{ t('scenarioMonitor.introduce.scenarios.businessProcess.description') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
