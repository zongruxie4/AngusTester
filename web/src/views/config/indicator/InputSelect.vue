<script setup lang="ts">
import { ref, watch } from 'vue';
import { Input, Select } from '@xcan-angus/vue-ui';

/**
 * Props interface for InputSelect component
 */
interface InputSelectProps {
  /**
   * Current value as a tuple of [inputValue, selectValue]
   */
  value: [undefined | string, undefined | string];

  /**
   * Options for the select dropdown
   */
  options: { value: string; label: string }[];

  /**
   * Additional props to pass to the Input component
   */
  inputProps: Record<string, any>;

  /**
   * Additional props to pass to the Select component
   */
  selectProps: Record<string, any>;
}

/**
 * Emits interface for InputSelect component
 */
interface InputSelectEmits {
  /**
   * Emit when the value changes
   * @param event - The event name
   * @param value - The new value as a tuple of [inputValue, selectValue]
   */
  (event: 'change', value: [string, string]): void;
}

// Define props with defaults
const props = withDefaults(defineProps<InputSelectProps>(), {
  options: () => [],
  value: () => [undefined, undefined],
  inputProps: () => ({}),
  selectProps: () => ({})
});

// Define emits
const emit = defineEmits<InputSelectEmits>();

// Reactive references for input and select values
const inputValue = ref<string | undefined>();
const selectValue = ref<string | undefined>();

// Watch for changes in props.value and update local refs
watch(
  () => props.value,
  (newValue) => {
    inputValue.value = newValue[0];
    selectValue.value = newValue[1];
  },
  {
    immediate: true
  }
);

/**
 * Handle value changes and emit the updated value
 */
const handleValueChange = () => {
  emit('change', [inputValue.value || '', selectValue.value || '']);
};
</script>

<template>
  <div class="flex input-select-wrapper">
    <!-- Input field for text entry -->
    <Input
      v-model:value="inputValue"
      class="flex-1 min-w-0 border-r-0"
      v-bind="props.inputProps"
      @change="handleValueChange" />

    <!-- Select dropdown for options -->
    <Select
      v-model:value="selectValue"
      :options="props.options"
      class="w-30"
      v-bind="props.selectProps"
      @change="handleValueChange" />
  </div>
</template>

<style scoped>
/* Style the input field to remove right border for seamless integration */
.input-select-wrapper > :deep(.ant-input-affix-wrapper) {
  @apply !rounded-r-none;
}

/* Style the select field to remove left border for seamless integration */
.input-select-wrapper :deep(.ant-select-selector) {
  @apply !rounded-l-none;
}
</style>
