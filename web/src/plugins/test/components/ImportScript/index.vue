
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import YAML from 'yaml';

/**
 * Component props interface
 */
export interface Props {
  visible: boolean;  // Controls whether to trigger file selection dialog
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  visible: false
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;           // Update visibility state
  (e: 'ok', value: { [key: string]: any }): void;       // Emit parsed file content
}>();

/**
 * Reference to the hidden file input element
 */
const inputRef = ref<HTMLInputElement>();

/**
 * Trigger file selection dialog
 * Opens the native file picker and closes the modal after a short delay
 */
const selectFile = (): void => {
  // Verify input element has click method
  if (typeof inputRef.value?.click !== 'function') {
    return;
  }

  // Trigger file picker
  inputRef.value.click();
  
  // Close modal after 300ms to allow file dialog to open
  setTimeout(() => {
    emit('update:visible', false);
  }, 300);
};

/**
 * Handle file input change event
 * Reads and parses the selected file (JSON or YAML format)
 * Emits parsed content or shows error notification
 * 
 * @param event - File input change event
 */
const change = (event: Event): void => {
  const target = event.target as HTMLInputElement;
  const files = target?.files as FileList;
  
  // Early return if no files selected
  if (!files?.length) {
    return;
  }

  const selectedFile = files[0];
  const isJsonFile = /(.json)$/.test(selectedFile.name);

  // Create FileReader to read file content
  const fileReader = new FileReader();
  fileReader.readAsText(selectedFile);

  // Handle file read completion
  fileReader.onload = () => {
    // Reset file input value to allow selecting the same file again
    if (inputRef.value) {
      inputRef.value.value = '';
    }

    // Get file content as string
    const rawContent = fileReader.result as string;

    // Parse JSON file
    if (isJsonFile) {
      try {
        const parsedContent = JSON.parse(rawContent);
        emit('ok', parsedContent);
      } catch (error) {
        const err = error as Error;
        notification.error(err.message);
      }
      return;
    }

    // Parse YAML file
    try {
      // Remove line continuations before parsing
      const cleanedContent = rawContent.replace(/\\\n/g, '');
      const parsedContent = YAML.parse(cleanedContent);
      emit('ok', parsedContent);
    } catch (error) {
      const err = error as Error;
      notification.error(err.message);
    }
  };
};

/**
 * Component lifecycle hook
 * Sets up watcher to trigger file selection when visible prop becomes true
 */
onMounted(() => {
  // Watch for visible prop changes
  watch(() => props.visible, (newValue) => {
    // Only trigger file selection when becoming visible
    if (!newValue) {
      return;
    }

    selectFile();
  }, {
    immediate: true  // Execute immediately on mount if already visible
  });
});
</script>

<template>
  <input
    ref="inputRef"
    type="file"
    accept=".yml,.yaml,.json"
    @change="change" />
</template>
