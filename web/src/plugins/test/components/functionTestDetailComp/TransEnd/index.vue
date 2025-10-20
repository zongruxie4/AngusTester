<script setup lang="ts">
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import { TransEndConfig } from '@/plugins/test/types';

/**
 * Component props interface
 */
export interface Props {
  value: TransEndConfig;  // Transaction end marker configuration data
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

/**
 * Computed: Display name for transaction end
 * Extracts base transaction name by removing "_end" suffix
 * and appends " End" text to clearly indicate the endpoint
 * 
 * Processing:
 * 1. Use regex to remove "_end" suffix from configuration name
 * 2. Append " End" to create clear endpoint label
 * 
 * Example: "checkout_end" -> "checkout End"
 * 
 * @returns Formatted transaction end name or empty string if no name
 */
const showName = computed(() => {
  // Remove "_end" suffix from name using regex
  // Pattern: (.+)_end$ matches any text before "_end" at the end
  const _name = props.value?.name?.replace(/(.+)_end$/, '$1');
  
  // Return formatted name with " End" suffix
  // Note: Using hardcoded "结束" (End in Chinese) for compatibility
  // This should be replaced with i18n in future updates
  return _name ? (_name + '结束') : '';
});
</script>

<template>
  <!--
    Transaction end marker display
    - Fixed height (42px)
    - Horizontal layout with icon and name
    - Rounded corners
  -->
  <div class="h-10.5 leading-5 flex items-center px-3 rounded">
    <!-- Transaction icon -->
    <Icon 
      class="flex-shrink-0 text-4 mr-3" 
      icon="icon-shiwu" />
    
    <!-- Transaction end name (truncated with tooltip) -->
    <div 
      :title="showName" 
      class="truncate min-w-55 max-w-100 name">
      {{ showName }}
    </div>
  </div>
</template>
