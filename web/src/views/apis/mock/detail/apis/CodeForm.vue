<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import YAML from 'yaml';
import { isYAML } from '@/utils/dataFormat';

import MonacoEditor from '@/components/monacoEditor/index.vue';

export interface Props {
  value:{[key:string]:any}|undefined;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const content = ref('');
const loading = ref(true);

/**
 * Convert object to YAML string with fallback to JSON
 * @param data - Object to convert
 * @returns YAML or JSON string
 */
const convertToYamlString = (data: {[key:string]:any}) => {
  try {
    return YAML.stringify(data);
  } catch (error) {
    return JSON.stringify(data, null, 2);
  }
};

/**
 * Validate if content is valid YAML
 * @returns Whether content is valid YAML
 */
const isValid = ():boolean => {
  return isYAML(content.value);
};

/**
 * Get parsed data from YAML content
 * @returns Parsed object data
 */
const getData = ():{[key:string]:any} => {
  return YAML.parse(content.value);
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    if (!newValue) {
      return;
    }
    content.value = convertToYamlString(newValue);
  }, { immediate: true, deep: true });
});

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
