<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import StatusTag from '@/plugins/test/components/StatusTag/index.vue';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Throughput controller configuration interface
 */
interface ThroughputValue {
  target: 'THROUGHPUT';       // Target type identifier
  name: string;               // Throughput controller name
  description: string;        // Optional description
  enabled: boolean;           // Whether this controller is enabled
  beforeName: string;         // Name of step before this controller
  transactionName: string;    // Associated transaction name
  permitsPerSecond: string;   // Maximum requests per second (rate limit)
  timeoutInMs: string;        // Max wait time in milliseconds before timeout
}

/**
 * Component props interface
 */
export interface Props {
  value: ThroughputValue;  // Throughput controller configuration data
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

</script>

<template>
  <!--
    Throughput controller display container
    - Dashed border for visual distinction
    - Fixed height (42px)
    - Horizontal layout with icon, info, and status
  -->
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <!-- Throughput controller icon -->
    <Icon 
      class="flex-shrink-0 text-4 mr-3" 
      icon="icon-zusai" />
    
    <!-- Throughput controller information section -->
    <div class="flex-1 flex items-center mr-3">
      <!-- Controller name (truncated with tooltip) -->
      <div 
        :title="props.value?.name" 
        class="truncate min-w-55 max-w-100 mr-5 name">
        {{ props.value?.name }}
      </div>
      
      <!-- Permits per second (rate limit) -->
      <div class="flex items-center mr-5">
        <span class="mr-0.5">{{ t('httpPlugin.functionTestDetail.throughput.requestsPerSecond') }}</span>
        <span>{{ props.value?.permitsPerSecond }}</span>
      </div>
      
      <!-- Timeout in milliseconds (max wait time before timeout) -->
      <div class="flex items-center mr-5">
        <span class="mr-0.5">{{ t('httpPlugin.functionTestDetail.throughput.waitTimeout') }}</span>
        <span>{{ props.value?.timeoutInMs }}</span>
        <span>ms</span>
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
