<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import YAML from 'yaml';

// Component props interface
export interface Props {
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

// Component emits
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: { [key: string]: any }): void;
}>();

// Template refs
const inputRef = ref();

// ==================== File Selection Handling ====================
/**
 * Trigger file selection dialog
 * Programmatically clicks the hidden file input element
 */
const selectFile = () => {
  if (typeof inputRef.value?.click !== 'function') {
    return;
  }

  inputRef.value.click();
  setTimeout(() => {
    emit('update:visible', false);
  }, 300);
};

// ==================== File Processing ====================
/**
 * Handle file selection change event
 * Process the selected file based on its extension (JSON/YAML)
 * @param event - The file input change event
 */
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const files = target.files as FileList;

  if (!files?.length) {
    return;
  }

  const isJsonFile = /(.json)$/i.test(files[0].name);
  const selectedFile = files[0];

  // Create file reader to read file content
  const fileReader = new FileReader();
  fileReader.readAsText(selectedFile);

  // Handle file read completion
  fileReader.onload = () => {
    // Reset file input value
    inputRef.value.value = '';

    try {
      const fileContent = fileReader.result as string;
      // TODO 增加可选是否验证插件类型选项，如：场景中选择脚本必须和当前插件一致
      if (isJsonFile) {
        // Process JSON file
        const parsedContent = JSON.parse(fileContent);
        emit('ok', parsedContent);
      } else {
        // Process YAML file
        const cleanedContent = fileContent.replace(/\\\n/g, '');
        const parsedContent = YAML.parse(cleanedContent);
        emit('ok', parsedContent);
      }
    } catch (error) {
      notification.error((error as Error).message);
    }
  };
};

// ==================== Lifecycle Hooks ====================
/**
 * Watch for visibility changes to trigger file selection
 */
onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    selectFile();
  }, { immediate: true });
});
</script>
<template>
  <input
    ref="inputRef"
    type="file"
    accept=".yml,.yaml,.json"
    @change="handleFileChange" />
</template>
