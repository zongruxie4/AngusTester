<script setup lang="ts">
import { Checkbox } from 'ant-design-vue';
import { useCheckboxGroup } from './composables/useCheckboxGroup';
import type { CheckboxGroupProps, CheckboxGroupEmits } from './types';

/**
 * <p>
 * CheckboxGroup component for managing permission selections
 * </p>
 * <p>
 * This component renders a group of checkboxes with special logic for VIEW permission.
 * When any non-VIEW permission is selected, VIEW permission is automatically added.
 * When VIEW permission is deselected, all other permissions are removed.
 * </p>
 */

// Component props with default values
const props = withDefaults(defineProps<CheckboxGroupProps>(), {
  options: () => [],
  disabled: false,
  value: () => []
});

// Component emits
const emit = defineEmits<CheckboxGroupEmits>();

// Use the checkbox group composable for logic separation
const { isOptionSelected, handleCheckboxChange } = useCheckboxGroup(props, emit);
</script>

<template>
  <div class="checkbox-group">
    <Checkbox
      v-for="option in props.options"
      :key="option.value"
      :disabled="props.disabled"
      :checked="isOptionSelected(option.value)"
      @change="(event) => handleCheckboxChange(event, option.value)"
    >
      {{ option.label }}
    </Checkbox>
  </div>
</template>

<style scoped>
/**
 * <p>
 * Styling for the checkbox group component
 * </p>
 */

.checkbox-group {
  /* Container for the checkbox group */
}

/* Spacing between adjacent checkboxes */
.ant-checkbox-wrapper + .ant-checkbox-wrapper {
  margin-left: 6px;
}

/* Left margin for the first checkbox in the group */
.checkbox-group .ant-checkbox-wrapper:first-child {
  margin-left: 6px;
}
</style>
