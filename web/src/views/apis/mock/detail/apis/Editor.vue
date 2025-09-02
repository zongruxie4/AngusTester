<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useYamlEditor } from './composables/useYamlEditor';
import MonacoEditor from '@/components/monacoEditor/index.vue';

/**
 * Props interface for YAML editor component
 */
export interface Props {
  value: { [key: string]: any } | undefined;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

// Initialize YAML editor composable
const {
  content,
  loading,
  convertToYaml,
  isValid,
  getData
} = useYamlEditor();

// Watch for value changes and convert to YAML
onMounted(() => {
  watch(() => props.value, (newValue) => {
    convertToYaml(newValue);
  }, { immediate: true, deep: true });
});

// Expose methods for parent component
defineExpose({
  isValid,
  getData
});
</script>

<template>
  <MonacoEditor
    v-model:loading="loading"
    v-model:value="content"
    class="w-full h-full py-3"
    language="yaml" />
</template>
