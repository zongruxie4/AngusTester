<script setup lang="ts">
import { computed } from 'vue';
import { Colon, IconCopy } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    summary: string;
    description: string;
    value: any;
    externalValue: string;
    _id: string;
    _name: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const value = computed(() => {
  return props.value?.value;
});

const externalValue = computed(() => {
  return props.value?.externalValue;
});

const showExternalValue = computed(() => {
  if (!externalValue.value) {
    return false;
  }

  if (value.value) {
    return false;
  }

  return true;
});
</script>
<template>
  <div class="relative">
    <div v-if="showExternalValue" class="flex items-start">
      <div class="flex items-center w-15">
        <span class="text-theme-title">外部值</span>
        <Colon />
      </div>
      <a
        :href="externalValue"
        target="_blank"
        style="color:#1890ff"
        class="truncate">{{ externalValue }}</a>
    </div>
    <pre
      v-if="value"
      style="background-color: rgb(251, 251, 251);"
      class="border border-solid rounded border-theme-text-box max-h-70 overflow-y-auto py-2.5 px-3.5 mt-2.5 flex items-start whitespace-pre-wrap break-all">
        <code>{{ value }}</code>
      </pre>
    <IconCopy class="absolute right-2 top-3.5" :copyText="value" />
  </div>
</template>
