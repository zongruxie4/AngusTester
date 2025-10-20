<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
import { RendezvousConfig } from '@/plugins/test/types';

import StatusTag from '@/plugins/test/components/StatusTag/index.vue';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
export interface Props {
  value: RendezvousConfig;  // Rendezvous configuration data
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

</script>

<template>
  <!--
    Rendezvous point display container
    - Dashed border for visual distinction
    - Fixed height (42px)
    - Horizontal layout with icon, info, and status
  -->
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <!-- Rendezvous icon -->
    <Icon 
      class="flex-shrink-0 text-4 mr-3" 
      icon="icon-jihedian1" />
    
    <!-- Rendezvous information section -->
    <div class="flex-1 flex items-center mr-3">
      <!-- Rendezvous name (truncated with tooltip) -->
      <div 
        :title="props.value?.name" 
        class="truncate min-w-55 max-w-100 mr-5 name">
        {{ props.value?.name }}
      </div>
      
      <!-- Thread count (number of users to wait for) -->
      <div class="flex items-center mr-5">
        <span class="mr-0.5">{{ t('httpPlugin.functionTestDetail.rendezvous.userCount') }}</span>
        <span>{{ props.value?.threads }}</span>
      </div>
      
      <!-- Timeout in milliseconds (how long to wait for all threads) -->
      <div class="flex items-center">
        <span class="mr-0.5">{{ t('httpPlugin.functionTestDetail.rendezvous.waitTimeout') }}</span>
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
