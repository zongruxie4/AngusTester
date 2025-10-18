<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { i18n } from '@xcan-angus/infra';

// Get the i18n instance for translations
const I18nInstance = i18n.getI18n();
const globalT = I18nInstance?.global?.t || ((value: string): string => value);

interface Props {
  dataSource: PerformanceEntry;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({}) as PerformanceEntry
});

const { t } = useI18n();

/**
 * Interface for timeline column data
 */
interface Column {
  /** Display name of the timeline item */
  name: string;
  /** Key used to calculate time values */
  key: string;
  /** Calculated time duration in milliseconds */
  time: number;
  /** Delay before this item starts */
  delay: number;
}

/**
 * Array of timeline columns with translated names
 */
const columns: Column[] = [
  {
    name: globalT('service.timeline.items.dnsLookup'),
    key: 'domainLookupEnd-domainLookupStart',
    time: 0,
    delay: 0
  },
  {
    name: globalT('service.timeline.items.tcpConnection'),
    key: 'connectEnd-connectStart',
    time: 0,
    delay: 0
  },
  {
    name: globalT('service.timeline.items.ssl'),
    key: 'secureConnectionEnd-secureConnectionStart',
    time: 0,
    delay: 0
  },
  {
    name: globalT('service.timeline.items.requestSent'),
    key: 'responseEnd-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: globalT('service.timeline.items.waiting'),
    key: 'responseStart-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: globalT('service.timeline.items.contentDownload'),
    key: 'responseEnd-responseStart',
    time: 0,
    delay: 0
  }
];

// Reactive reference for the total duration of the timeline
const duration = ref(0);

// Reactive state for processed timeline data
const state = reactive<{
  data: Column[];
}>({
  data: []
});

/**
 * Calculate padding left style based on delay
 * @param delay - The delay in milliseconds
 * @returns CSS style object with padding left percentage
 */
const getPaddingLeft = (delay: number): Record<string, string> => {
  return {
    paddingLeft: (delay / duration.value) * 100 + '%'
  };
};

/**
 * Calculate width style based on time duration
 * @param time - The time duration in milliseconds
 * @returns CSS style object with width percentage
 */
const getWidth = (time: number): Record<string, string> => {
  return {
    width: (time / duration.value) * 100 + '%'
  };
};

// Watch for data source changes and update timeline data
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

    // Calculate time based on key format
    if (keys?.length === 2 && keys[1]) {
      time = (newValue[keys[0]] - newValue[keys[1]]) || 0;
    } else {
      time = newValue[key] || 0;
    }

    // Calculate delay based on key type
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
