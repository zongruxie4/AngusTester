<script script setup lang="ts">
import { ref, watch } from 'vue';
import { MonacoEditor } from '@xcan-angus/vue-ui';

export interface Props {
  loading: boolean;
  value: string;
  language: 'json' | 'xml' | 'text' | 'yaml';
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:loading', value:boolean):void;
}>();

const spinning = ref(false);

watch(() => spinning.value, () => {
  emit('update:loading', spinning.value);
});
</script>
<template>
  <MonacoEditor
    v-model:loading="spinning"
    :value="props.value"
    :language="props.language" />
</template>
