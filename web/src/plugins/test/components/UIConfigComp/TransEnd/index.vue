<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import { TransEndConfig } from './PropsType';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
export interface Props {
  name: string;         // Transaction name (without '_end' suffix)
  description: string;  // Transaction description
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  name: undefined
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'renderChange'): void;  // Emit when component renders
}>();

/**
 * Component mount lifecycle hook
 * Emits render change event to notify parent
 */
onMounted(() => {
  emit('renderChange');
});

/**
 * Computed display name for transaction end
 * Removes '_end' suffix if present and adds localized suffix
 * 
 * @returns Formatted transaction end name
 * @example "LoginTransaction_end" -> "LoginTransaction (End)"
 */
const showName = computed(() => {
  const _name = props.name?.replace(/(.+)_end$/, '$1');
  return _name ? (_name + t('httpPlugin.uiConfig.transEnd.suffix')) : '';
});

/**
 * Get transaction end configuration data
 * Returns configuration object without ID
 * Transaction end is always enabled and automatically named
 * 
 * @returns TransEnd configuration (excluding ID)
 */
const getData = (): Omit<TransEndConfig, 'id'> => {
  return {
    enabled: true,                   // Always enabled
    target: 'TRANS_END',             // Component type identifier
    name: props.name + '_end',       // Add '_end' suffix to transaction name
    description: props.description,
    beforeName: '',
    transactionName: ''
  };
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  getData  // Get configuration data
});
</script>

<template>
  <!--
    Transaction end marker row
    - Light blue background for visual distinction from other components
    - Fixed height (36px)
    - Rounded corners for modern appearance
    - Read-only display (no interactive elements except for visual display)
  -->
  <div class="h-9 flex items-center pl-9.5 pr-3 rounded bg-blue-light">
    <!-- Transaction icon -->
    <Icon 
      class="flex-shrink-0 text-4 mr-3" 
      icon="icon-shiwu" />
    
    <!-- Transaction end name display (truncated with tooltip) -->
    <div
      :title="showName"
      class="truncate"
      style="width: calc((100% - (280px))*2/5);">
      {{ showName }}
    </div>
  </div>
</template>
<style scoped>
.bg-blue-light {
  background-color: rgba(232, 240, 251, 100%);
}
</style>
