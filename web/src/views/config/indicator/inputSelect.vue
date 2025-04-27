<script setup lang="ts">
import { ref, watch } from 'vue';
import { Input, Select } from '@xcan-angus/vue-ui';

interface Props {
  value: [undefined|string, undefined|string];
  options: {value: string, label: string}[];
  inputProps: Record<string, any>;
  selectProps: Record<string, any>;
}

const emit = defineEmits<{(e: 'change', value: [string, string]):void}>();
const props = withDefaults(defineProps<Props>(), {
  options: () => ([]),
  value: () => ([undefined, undefined]),
  inputProps: () => ({}),
  selectProps: () => ({})
});

const inputValue = ref();
const selectValue = ref();
watch(() => props.value, newValue => {
  inputValue.value = newValue[0];
  selectValue.value = newValue[1];
}, {
  immediate: true
});

const valueChange = () => {
  emit('change', [inputValue.value, selectValue.value]);
};
</script>
<template>
  <div class="flex input-select-wrapper">
    <Input
      v-model:value="inputValue"
      class="flex-1 min-w-0 border-r-0"
      v-bind="props.inputProps"
      @change="valueChange" />
    <Select
      v-model:value="selectValue"
      :options="props.options"
      class="w-30"
      v-bind="props.selectProps"
      @change="valueChange" />
  </div>
</template>
<style scoped>
.input-select-wrapper > :deep(.ant-input-affix-wrapper) {
  @apply !rounded-r-none;
}

.input-select-wrapper :deep(.ant-select-selector) {
  @apply !rounded-l-none;
}
</style>
