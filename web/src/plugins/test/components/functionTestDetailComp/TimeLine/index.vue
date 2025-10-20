<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { NoData } from '@xcan-angus/vue-ui';
import { TimelineConfig } from '@/plugins/test/types';

// Initialize i18n for internationalization
const { t } = useI18n();



/**
 * Timeline item interface
 * Represents a single phase in the timeline visualization
 */
interface TimelineItem {
  name: string;    // Display name of the phase
  key: string;     // Key for calculating duration (format: 'end-start')
  time: number;    // Duration of this phase in milliseconds
  delay: number;   // Delay before this phase starts in milliseconds
}

/**
 * Component props interface
 */
export interface Props {
  value: TimelineConfig;  // Timeline data from HTTP request
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

/**
 * Ref: Total request duration in milliseconds
 * Used as denominator for calculating percentage widths
 */
const totalDuration = ref(0);

/**
 * Ref: Processed timeline data for visualization
 * Array of timeline items with calculated times and delays
 */
const timelineData = ref<TimelineItem[]>();

/**
 * Calculate left padding style for timeline bar positioning
 * Positions the bar based on when the phase starts relative to total duration
 * 
 * @param delay - Delay before phase starts (in milliseconds)
 * @returns Style object with paddingLeft percentage
 */
const getPaddingLeft = (delay: number): Record<string, string> => {
  return {
    paddingLeft: (delay / totalDuration.value) * 100 + '%'
  };
};

/**
 * Calculate width style for timeline bar
 * Width represents the duration of the phase relative to total duration
 * 
 * @param time - Duration of the phase (in milliseconds)
 * @returns Style object with width percentage
 */
const getWidth = (time: number): Record<string, string> => {
  return {
    width: (time / totalDuration.value) * 100 + '%'
  };
};

/**
 * Computed: Timeline phase definitions
 * Defines all phases to be displayed in the timeline
 * Each phase has a name, key for calculation, and initial time/delay values
 * 
 * Phases in order:
 * 1. DNS Lookup: domainLookupEnd - domainLookupStart
 * 2. TCP Connection: connectEnd - connectStart
 * 3. SSL/TLS: secureConnectionEnd - secureConnectionStart
 * 4. Request Sent: responseEnd - requestStart (total from request to response)
 * 5. Waiting (TTFB): responseStart - requestStart (time to first byte)
 * 6. Content Download: responseEnd - responseStart
 * 
 * @returns Array of phase definitions
 */
const columns = computed<TimelineItem[]>(() => [
  {
    name: t('httpPlugin.functionTestDetail.http.timeline.dnsLookup'),
    key: 'domainLookupEnd-domainLookupStart',
    time: 0,
    delay: 0
  },
  {
    name: t('httpPlugin.functionTestDetail.http.timeline.tcpConnection'),
    key: 'connectEnd-connectStart',
    time: 0,
    delay: 0
  },
  {
    name: t('httpPlugin.functionTestDetail.http.timeline.ssl'),
    key: 'secureConnectionEnd-secureConnectionStart',
    time: 0,
    delay: 0
  },
  {
    name: t('httpPlugin.functionTestDetail.http.timeline.requestSent'),
    key: 'responseEnd-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('httpPlugin.functionTestDetail.http.timeline.waiting'),
    key: 'responseStart-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('httpPlugin.functionTestDetail.http.timeline.contentDownload'),
    key: 'responseEnd-responseStart',
    time: 0,
    delay: 0
  }
]);

/**
 * Lifecycle: Component mounted
 * Sets up watcher to process timeline data when props change
 * 
 * Processing logic:
 * 1. Parse timing values from props
 * 2. Calculate duration for each phase (end - start)
 * 3. Calculate delay (accumulated time before phase starts)
 * 4. Handle special cases:
 *    - "Waiting" phase starts at same time as "Request Sent"
 *    - Other phases start after previous phase completes
 */
onMounted(() => {
  watch(() => props.value, (newValue) => {
    // Validate timeline data exists
    if (!newValue || !newValue.total) {
      return;
    }

    // Set total duration for percentage calculations
    totalDuration.value = +newValue.total;
    
    // Process each timeline phase
    timelineData.value = columns.value.reduce((prevValue, currentValue) => {
      const len = prevValue.length;
      let _delay = 0;  // Delay before this phase starts
      let time = 0;    // Duration of this phase

      // Get delay from previous phase
      const { delay = 0 } = prevValue[len - 1] || {};
      const { key } = currentValue;
      
      // Parse key to calculate duration
      // Key format: "endProperty-startProperty" (e.g., "domainLookupEnd-domainLookupStart")
      const keys = key.split('-');
      if (keys?.length === 2 && keys[1]) {
        // Calculate duration: end time - start time
        const endValue = +(newValue[keys[0] as keyof TimelineConfig] || 0);
        const startValue = +(newValue[keys[1] as keyof TimelineConfig] || 0);
        time = (endValue - startValue) || 0;
      } else {
        // Direct value (fallback)
        time = +(newValue[key as keyof TimelineConfig] || 0);
      }
      
      // Calculate delay (start position on timeline)
      if (key === 'responseStart-requestStart') {
        // Special case: "Waiting" phase starts at same time as request
        // Use same delay as previous phase (doesn't wait for previous to complete)
        _delay = prevValue[len - 1]?.delay;
      } else {
        // Normal case: phase starts after previous phase completes
        // Delay = previous delay + previous phase duration
        _delay = delay + (prevValue[len - 1]?.time || 0);
      }

      // Add processed phase to result
      prevValue.push({
        ...currentValue,
        time,
        delay: _delay
      });

      return prevValue;
    }, [] as TimelineItem[]);
  }, { immediate: true });
});
</script>

<template>
  <!-- Empty state: no timeline data -->
  <template v-if="!props.value">
    <NoData size="small" class="my-13" />
  </template>
  
  <!-- Timeline visualization: waterfall chart layout -->
  <template v-else>
    <div class="h-full overflow-auto relative flex flex-nowrap whitespace-nowrap px-5 py-4">
      <!-- Left column: Phase names -->
      <div class="flex flex-col items-start text-3 leading-3 text-theme-content mr-6">
        <!-- Column header: "Time Item" -->
        <div class="mb-4 text-theme-sub-content">
          {{ t('httpPlugin.functionTestDetail.http.timeline.timeItem') }}
        </div>
        
        <!-- Phase name rows -->
        <div
          v-for="(item, index) in timelineData"
          :key="index"
          class="title-item-container"
          :class="{ 
            'pl-5': item.key === 'responseStart-requestStart' || item.key === 'responseEnd-responseStart' 
          }">
          {{ item.name }}
        </div>
        
        <!-- Total time row -->
        <div class="title-item-container">
          {{ t('httpPlugin.functionTestDetail.http.timeline.totalTime') }}
        </div>
      </div>
      
      <!-- Right column: Timeline bars with durations -->
      <div class="flex flex-col flex-1 items-start text-3 leading-3 text-theme-content pr-6">
        <!-- Column header: "Time" -->
        <div class="mb-4 text-theme-sub-content">
          {{ t('httpPlugin.functionTestDetail.http.timeline.time') }}
        </div>
        
        <!-- Timeline bar rows for each phase -->
        <div
          v-for="(item, index) in timelineData"
          :key="index"
          :style="getPaddingLeft(item.delay)"
          class="list-item-container">
          <!-- Duration text (overlays on bar) -->
          <div class="relative z-1 h-5 ml-2 leading-5">
            {{ Math.round(item.time) }}ms
          </div>
          <!-- Timeline bar (width proportional to duration) -->
          <div
            :style="getWidth(item.time)"
            class="absolute top-1.5 h-5 rounded"
            style="background-color: rgba(162, 222, 236, 100%);"></div>
        </div>
        
        <!-- Total time row (full width bar) -->
        <div class="list-item-container">
          <!-- Total duration text -->
          <div class="relative z-1 h-5 ml-2 leading-5">
            {{ Math.round(totalDuration) }}ms
          </div>
          <!-- Total timeline bar (spans entire width) -->
          <div 
            class="absolute top-1.5 w-full h-5 rounded" 
            style="background-color: rgba(166, 206, 255, 100%);"></div>
        </div>
        
        <!-- Horizontal separator line -->
        <div 
          class="absolute left-0 top-10 w-full h-0.25" 
          style="background-color: rgba(245, 245, 245, 100%);"></div>
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
