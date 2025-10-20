<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue';
import { EnumMessage, HttpStatus, enumUtils } from '@xcan-angus/infra';
import { getStatusColor } from 'src/utils/apis';
import { useI18n } from 'vue-i18n';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
interface Props {
  status: string | number;  // HTTP status code to display
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  status: ''
});

/**
 * Emitted events interface
 */
const emit = defineEmits<{
  (e: 'rendered', value: true): void;  // Emitted when component is fully rendered
}>();

/**
 * Ref: HTTP status options loaded from enum
 * Contains array of status code definitions with messages
 */
const statusOpt = ref<EnumMessage<HttpStatus>[]>([]);

/**
 * Load HTTP status definitions from enum
 * Converts HttpStatus enum to message array for display
 * Only loads once to avoid redundant API calls
 */
const loadHttpStatus = async (): Promise<void> => {
  if (statusOpt.value.length > 0) {
    return; // Already loaded
  }
  statusOpt.value = enumUtils.enumToMessages(HttpStatus);
};

/**
 * Get formatted status text with description
 * Combines status code with human-readable message
 * 
 * @param status - HTTP status code (number or string)
 * @returns Formatted status text (e.g., "200 OK") or just the code if not found
 */
const getStatusText = (status: number | string): string => {
  if (status) {
    const target = statusOpt.value.find(i => i.value === (status + ''));
    return target ? (target.value + ' ' + target.message) : status + '';
  }
  return '';
};

/**
 * Component mounted lifecycle hook
 * Loads HTTP status definitions and emits rendered event
 */
onMounted(() => {
  loadHttpStatus();

  nextTick(() => {
    emit('rendered', true);
  });
});
</script>

<template>
  <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5">
    <span class="mr-1">{{ t('protocol.statusCode') }}:</span>
    <span :class="getStatusColor(props.status)">
      {{ getStatusText(props.status) }}
    </span>
  </div>
</template>
