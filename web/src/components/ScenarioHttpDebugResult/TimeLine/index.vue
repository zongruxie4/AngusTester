<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

export interface Props {
  value: {
    fetchStart: string;
    domainLookupStart: string;
    domainLookupEnd: string;
    connectStart: string;
    connectEnd: string;
    secureConnectionStart: string;
    secureConnectionEnd: string;
    requestStart: string;
    responseStart: string;
    responseEnd: string;
    total: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const totalDuration = ref(0);
const timelineData = ref();
const getPaddingLeft = (delay: number): Record<string, string> => {
  return {
    paddingLeft: (delay / totalDuration.value) * 100 + '%'
  };
};

const getWidth = (time: number): Record<string, string> => {
  return {
    width: (time / totalDuration.value) * 100 + '%'
  };
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    if (!newValue || !newValue.total) {
      return;
    }

    totalDuration.value = +newValue.total;
    timelineData.value = columns.reduce((prevValue, currentValue) => {
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
    }, [] as {
      name: string,
      key: string,
      time: number,
      delay: number
    }[]);
  }, { immediate: true });
});

const columns: readonly {
  name: string,
  key: string,
  time: number,
  delay: number
}[] = [
  {
    name: t('commonComp.xcan_requestTimeLine.dnsLookup'),
    key: 'domainLookupEnd-domainLookupStart',
    time: 0,
    delay: 0
  },
  {
    name: t('commonComp.xcan_requestTimeLine.tcpConnection'),
    key: 'connectEnd-connectStart',
    time: 0,
    delay: 0
  },
  {
    name: t('commonComp.xcan_requestTimeLine.ssl'),
    key: 'secureConnectionEnd-secureConnectionStart',
    time: 0,
    delay: 0
  },
  {
    name: t('commonComp.xcan_requestTimeLine.requestSent'),
    key: 'responseEnd-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('commonComp.xcan_requestTimeLine.waiting'),
    key: 'responseStart-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('commonComp.xcan_requestTimeLine.contentDownload'),
    key: 'responseEnd-responseStart',
    time: 0,
    delay: 0
  }
];
</script>
<template>
  <template v-if="!props.value">
    <NoData size="small" class="my-13" />
  </template>
  <template v-else>
    <div class="h-full overflow-auto relative flex flex-nowrap whitespace-nowrap px-5 py-4">
      <div class="flex flex-col items-start text-3 leading-3 text-theme-content mr-6">
        <div class="mb-4 text-theme-sub-content">{{ t('commonComp.xcan_requestTimeLine.timeConsumingItems') }}</div>
        <div
          v-for="(item, index) in timelineData"
          :key="index"
          class="title-item-container"
          :class="{ 'pl-5': item.key === 'responseStart-requestStart' || item.key === 'responseEnd-responseStart' }">
          {{ item.name }}
        </div>
        <div class="title-item-container">
          {{ t('commonComp.xcan_requestTimeLine.totalTimeConsuming') }}
        </div>
      </div>
      <div class="flex flex-col flex-1 items-start text-3 leading-3 text-theme-content pr-6">
        <div class="mb-4 text-theme-sub-content">{{ t('commonComp.xcan_requestTimeLine.time') }}</div>
        <div
          v-for="(item, index) in timelineData"
          :key="index"
          :style="getPaddingLeft(item.delay)"
          class="list-item-container">
          <div class="relative z-1 h-5 ml-2 leading-5">{{ Math.round(item.time) }}ms</div>
          <div
            :style="getWidth(item.time)"
            class="absolute top-1.5 h-5 rounded"
            style="background-color: rgba(162, 222, 236, 100%);"></div>
        </div>
        <div class="list-item-container">
          <div class="relative z-1 h-5 ml-2 leading-5">{{ Math.round(totalDuration) }}ms</div>
          <div class="absolute top-1.5 w-full h-5 rounded" style="background-color: rgba(166, 206, 255, 100%);"></div>
        </div>
        <div class="absolute left-0 top-10 w-full h-0.25" style="background-color: rgba(245, 245, 245, 100%);"></div>
      </div>
    </div>
  </template>
</template>
<style scoped>
.title-item-container {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  height: 32px;
  color: var(--content-text-content);
}

.list-item-container {
  display: flex;
  position: relative;
  flex: 0 0 auto;
  align-items: center;
  width: 100%;
  height: 32px;
  color: var(--content-text-content);
}
</style>
