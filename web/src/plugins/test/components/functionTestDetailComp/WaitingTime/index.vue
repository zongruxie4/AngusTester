<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import StatusTag from '@/plugins/test/components/StatusTag/index.vue';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Wait type enumeration
 */
type WaitType = 'fixed' | 'random';

/**
 * Waiting time configuration interface
 */
interface WaitingTimeValue {
  beforeName: string;           // Name of step before this wait
  transactionName: string;      // Associated transaction name
  target: 'WAITING_TIME';       // Target type identifier
  name: string;                 // Waiting time name
  description: string;          // Optional description
  enabled: boolean;             // Whether this wait is enabled
  maxWaitTimeInMs: string;      // Maximum wait time in milliseconds (or fixed time)
  minWaitTimeInMs?: string;     // Minimum wait time in milliseconds (only for random wait)
}

/**
 * Component props interface
 */
export interface Props {
  value: WaitingTimeValue;  // Waiting time configuration data
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

/**
 * Computed: Determine wait type (fixed or random)
 * 
 * Logic:
 * - Fixed: Only maxWaitTimeInMs is provided (minWaitTimeInMs is null/undefined)
 * - Random: Both minWaitTimeInMs and maxWaitTimeInMs are provided
 * 
 * @returns Wait type: 'fixed' | 'random'
 */
const waitType = computed<WaitType>(() => {
  if (!props.value) {
    return 'fixed';
  }

  const { maxWaitTimeInMs, minWaitTimeInMs } = props.value;
  
  // Fixed wait: maxWaitTimeInMs exists but minWaitTimeInMs doesn't
  if ((![null, undefined].includes(maxWaitTimeInMs as any)) && 
      ([null, undefined].includes(minWaitTimeInMs as any))) {
    return 'fixed';
  }

  // Random wait: both min and max are provided
  return 'random';
});

/**
 * Computed: Localized wait type labels
 * Maps wait type to internationalized display text
 * 
 * @returns Object with 'fixed' and 'random' labels
 */
const waitTypeMap = computed<Record<WaitType, string>>(() => ({
  fixed: t('httpPlugin.functionTestDetail.waitingTime.fixedWait'),
  random: t('httpPlugin.functionTestDetail.waitingTime.randomWait')
}));
</script>

<template>
  <!--
    Waiting time display container
    - Dashed border for visual distinction
    - Fixed height (42px)
    - Horizontal layout with icon, info, and status
  -->
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <!-- Waiting time icon -->
    <Icon 
      class="flex-shrink-0 text-4 mr-3" 
      icon="icon-dengdaishijian" />
    
    <!-- Waiting time information section -->
    <div class="flex-1 flex items-center mr-3">
      <!-- Wait name (truncated with tooltip) -->
      <div 
        :title="props.value?.name" 
        class="truncate min-w-55 max-w-100 mr-5 name">
        {{ props.value?.name }}
      </div>
      
      <!-- Wait type label (Fixed or Random) -->
      <div class="mr-5">{{ waitTypeMap[waitType] }}</div>
      
      <!-- Fixed wait: Single duration value -->
      <div v-if="waitType==='fixed'">
        <span>{{ props.value?.maxWaitTimeInMs }}</span>
        <span>ms</span>
      </div>
      
      <!-- Random wait: Min to Max duration range -->
      <div
        v-else
        class="flex items-center">
        <!-- Minimum wait time -->
        <div class="mr-1">
          <span>{{ props.value?.minWaitTimeInMs }}</span>
          <span>ms</span>
        </div>
        <!-- "to" separator -->
        <div class="mr-1">{{ t('httpPlugin.functionTestDetail.waitingTime.to') }}</div>
        <!-- Maximum wait time -->
        <div>
          <span>{{ props.value?.maxWaitTimeInMs }}</span>
          <span>ms</span>
        </div>
      </div>
    </div>
    
    <!-- Status tag: shown only when disabled -->
    <StatusTag 
      v-if="!props.value?.enabled" 
      class="mr-7" />
  </div>
</template>
<style scoped>
.embed.dashed-border {
  padding: 0 12px;
  border: none;
  border-bottom: 1px dashed  var(--border-text-box);
}

.embed .name {
  min-width: 208px;
}
</style>
