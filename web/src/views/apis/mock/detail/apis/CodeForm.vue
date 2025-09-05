<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import YAML from 'yaml';
import { notification } from '@xcan-angus/vue-ui';

import MonacoEditor from '@/components/monacoEditor/index.vue';

export interface Props {
  value:{[key:string]:any}|undefined;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const { t } = useI18n();

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
    notification.error(t('mock.detail.apis.notifications.yamlFormatError'));
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
