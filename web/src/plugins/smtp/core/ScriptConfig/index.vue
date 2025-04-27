<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import YAML from 'yaml';
import { notification, MonacoEditor } from '@xcan-angus/vue-ui';
import '@xcan/monaco/style.css';

import { SceneConfig } from '../PropsType';

export interface Props {
  value: SceneConfig['script'];
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
  try {
    YAML.parse(content.value);
    return true;
  } catch (error) {
    notification.error('yaml内容格式错误，请检查并更正');
    return false;
  }
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
    :readOnly="true"
    class="w-full h-full py-3"
    language="yaml" />
</template>
