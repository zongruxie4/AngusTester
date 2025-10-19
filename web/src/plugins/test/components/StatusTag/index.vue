/**
 * StatusTag Component
<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Status type definition
 */
type StatusType = 'success' | 'fail' | 'ignore' | 'block';

/**
 * Component props interface
 */
interface Props {
  value?: StatusType;  // Current status value (default: 'ignore')
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: 'ignore'
});

/**
 * Computed text label based on status value
 * Returns internationalized status text
 * 
 * @returns Localized status text
 */
const text = computed(() => {
  // Failed status
  if (props.value === 'fail') {
    return t('status.failed');
  }

  // Success status
  if (props.value === 'success') {
    return t('status.success');
  }

  // Blocked status
  if (props.value === 'block') {
    return t('status.blocked');
  }

  // Default: ignored status
  return t('status.ignored');
});

/**
 * Computed inline style based on status value
 * Returns background color CSS string for each status type:
 * - fail: Orange (rgba(255, 129, 0, 0.8))
 * - success: Green (#52c41a)
 * - block/ignore: Gray (rgba(217, 217, 217, 1))
 * 
 * @returns CSS background-color style string
 */
const style = computed(() => {
  // Failed status: Orange
  if (props.value === 'fail') {
    return 'background-color:rgba(255, 129, 0, 0.8);';
  }

  // Success status: Green
  if (props.value === 'success') {
    return 'background-color:#52c41a;';
  }

  // Blocked status: Gray
  if (props.value === 'block') {
    return 'background-color:rgba(217, 217, 217, 1);';
  }

  // Default (ignored): Gray
  return 'background-color:rgba(217, 217, 217, 1);';
});
</script>

<template>
  <div 
    :style="style" 
    class="inline-block min-w-13 text-center px-2 rounded text-3 leading-5 text-white">
    {{ text }}
  </div>
</template>
