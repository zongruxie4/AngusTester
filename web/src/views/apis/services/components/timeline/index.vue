<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { Column, columns } from './interface';

interface Props {
  dataSource:PerformanceEntry
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({}) as PerformanceEntry
});
const { t } = useI18n();

const duration = ref(0);
const state = reactive<{
  data:Column[]
}>({
  data: []
});

const getPaddingLeft = (delay: number): Record<string, string> => {
  return {
    paddingLeft: (delay / duration.value) * 100 + '%'
  };
};

const getWidth = (time: number): Record<string, string> => {
  return {
    width: (time / duration.value) * 100 + '%'
  };
};

watch(() => props.dataSource, (newValue) => {
  if (!newValue || !newValue.duration) {
    return;
  }

  duration.value = newValue.duration;

  state.data = columns.reduce((prevValue, currentValue) => {
    const len = prevValue.length;
    let _delay = 0;
    let time = 0;

    const { delay = 0 } = prevValue[len - 1] || {};
    const { key } = currentValue;
    const keys = key.split('-');
    if (keys?.length === 2 && keys[1]) {
      time = (newValue[keys[0]] - newValue[keys[1]]) || 0;
    } else {
      time = newValue[key] || 0;
    }
    if (key === 'responseStart-requestStart') {
      _delay = prevValue[len - 1]?.delay;
    } else {
      _delay = delay + (prevValue[len - 1]?.time || 0);
    }

    prevValue.push({
      ...currentValue,
      time,
      delay: _delay
    });

    return prevValue;
  }, [] as Column[]);
}, { immediate: true });
</script>
<template>
  <div class="h-full overflow-auto relative flex flex-nowrap whitespace-nowrap px-5 py-4">
    <div class="flex flex-col items-start text-3 leading-3 text-text-content mr-6">
      <div class="mb-4 text-text-sub-content">{{ t('service.timeline.labels.timeConsuming') }}</div>
      <div
        v-for="(item,index) in state.data"
        :key="index"
        class="title-item-container"
        :class="{'pl-3': item.key=== 'responseStart-requestStart' || item.key=== 'responseEnd-responseStart'}">
        {{ t(item.name) }}
      </div>
      <div class="title-item-container">
        {{ t('service.timeline.labels.totalTime') }}
      </div>
    </div>
    <div class="flex flex-col flex-1 items-start text-3 leading-3 text-text-content pr-6">
      <div class="mb-4 text-text-sub-content">{{ t('service.timeline.labels.time') }}</div>
      <div
        v-for="(item,index) in state.data"
        :key="index"
        :style="getPaddingLeft(item.delay)"
        class="list-item-container">
        <div class="relative z-9 h-5 ml-2 leading-5">{{ Math.round(item.time) }}ms</div>
        <div :style="getWidth(item.time)" class="absolute top-1.5 h-5 rounded bg-blue-timeline"></div>
      </div>
      <div class="list-item-container">
        <div class="relative z-9 h-5 ml-2 leading-5">{{ Math.round(duration) }}ms</div>
        <div class="absolute top-1.5 w-full h-5 rounded bg-blue-light"></div>
      </div>
      <div class="absolute left-0 top-10 w-full h-0.25 bg-gray-light-a"></div>
    </div>
  </div>
</template>
<style scoped>
.title-item-container {
  @apply flex items-center h-8 text-text-content;

  flex: 0 0 auto;
}

.list-item-container {
  @apply relative flex w-full items-center h-8 text-text-content;

  flex: 0 0 auto;
}
</style>
