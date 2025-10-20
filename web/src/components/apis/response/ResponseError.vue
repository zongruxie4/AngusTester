<script lang="ts" setup>
import { onMounted, nextTick } from 'vue';

/**
 * Component props interface
 */
interface Props {
  info: string;        // Detailed error description/message
  errorTitle: string;  // Main error title/heading
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  info: '',
  errorTitle: ''
});

/**
 * Emitted events interface
 */
const emit = defineEmits<{
  (e: 'rendered', value: true): void;  // Emitted when component is fully rendered
}>();

/**
 * Component mounted lifecycle hook
 * Emits rendered event after component is fully mounted
 */
onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>

<template>
  <!--
    Error Display Container
    - Centered layout with vertical and horizontal centering
    - Visual hierarchy: image -> title -> description
    - Responsive design with proper spacing
  -->
  <div class="flex-1 flex flex-col justify-center items-center">
    <!--
      Error Image
      - Visual indicator for error state
      - Provides immediate visual feedback
    -->
    <img src="response-error.png" alt="Error" />

    <!--
      Error Title
      - Main error heading
      - Prominent display with medium font weight
    -->
    <span class="font-medium text-3 leading-3">{{ props.errorTitle }}</span>

    <!--
      Error Description
      - Detailed error information
      - Subdued styling for secondary information
      - Top margin for visual separation
    -->
    <p class="text-theme-sub-content mt-3">{{ props.info }}</p>
  </div>
</template>
