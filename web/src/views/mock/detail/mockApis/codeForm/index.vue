<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import YAML from 'yaml';
import { notification } from '@xcan-angus/vue-ui';

import MonacoEditor from '@/components/monacoEditor/index.vue';

export interface Props {
  value:{[key:string]:any}|undefined;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

// eslint-disable-next-line func-call-spacing
// const emit = defineEmits<{
//   (e:'change', value:{[key:string]:any}):void;
// }>();

const content = ref('');
const loading = ref(true);

// const change = (value:string) => {
//   try {
//     const data = YAML.parse(value);
//     emit('change', data);
//   } catch (error) {
//     notification.error('yaml内容格式错误，请检查并更正');
//   }
// };

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
    class="w-full h-full py-3"
    language="yaml" />
</template>
