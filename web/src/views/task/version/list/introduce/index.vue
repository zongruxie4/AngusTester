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
  <div ref="wrapperRef">
    <div class="text-3.5 font-semibold mb-2.5">{{ t('taskVersion.introduce.title') }}</div>
    <div class="mb-6">
      <div>{{ t('taskVersion.introduce.description') }}</div>
    </div>

    <div v-if="props.showFunc" class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskVersion.introduce.features.versionTracking.title') }}</div>
            <div>{{ t('taskVersion.introduce.features.versionTracking.description') }}</div>
          </div>
        </div>
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskVersion.introduce.features.versionPlanning.title') }}</div>
            <div>{{ t('taskVersion.introduce.features.versionPlanning.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskVersion.introduce.features.versionProgress.title') }}</div>
            <div>{{ t('taskVersion.introduce.features.versionProgress.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskVersion.introduce.features.verifiableDeliverables.title') }}</div>
            <div>{{ t('taskVersion.introduce.features.verifiableDeliverables.description') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
