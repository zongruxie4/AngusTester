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

onMounted(() => {
  watch(() => props.value, (newValue) => {
    if (!newValue) {
      return;
    }
    try {
      content.value = YAML.stringify(newValue);
    } catch (error) {
      content.value = JSON.stringify(newValue, null, 2);
    }
  }, { immediate: true, deep: true });
});

const isValid = ():boolean => {
  return isYAML(content.value);
};

const getData = ():{[key:string]:any} => {
  return YAML.parse(content.value);
};

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
