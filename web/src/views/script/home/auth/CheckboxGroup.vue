<script setup lang="ts">
import { Checkbox } from 'ant-design-vue';
import { CheckboxGroupProps } from './types';

/**
 * <p>
 * Component props with default values.
 * </p>
 */
const props = withDefaults(defineProps<CheckboxGroupProps>(), {
  options: () => [],
  disabled: false,
  value: () => []
});

/**
 * <p>
 * Component emits definition for change events.
 * </p>
 */
const emit = defineEmits<{(e: 'change', value: string[]) }>();

/**
 * <p>
 * Handles individual checkbox change events.
 * Manages permission selection logic including automatic VIEW permission handling.
 * </p>
 * <p>
 * When a permission is checked, it automatically includes VIEW permission if not VIEW itself.
 * When unchecked, removes the permission and all dependent permissions.
 * </p>
 */
const change = (event: { target: { checked: boolean; } }, value: string) => {
  const checked = event.target.checked;

  if (checked && !props.value.includes(value)) {
    // Add permission and automatically include VIEW if not VIEW itself
    const newValue = value !== 'VIEW' && !props.value.includes('VIEW')
      ? props.value.concat([value, 'VIEW'])
      : props.value.concat([value]);
    emit('change', newValue);
    return;
  }

  // Remove permission and all dependent permissions
  const temp = value !== 'VIEW' ? props.value.filter(item => item !== value) : [];
  emit('change', temp);
};
</script>

<template>
  <div class="ant-checkbox-group">
    <Checkbox
      v-for="item in props.options"
      :key="item.value"
      :disabled="props.disabled"
      :checked="props.value.includes(item.value)"
      @change="change($event, item.value)">
      {{ item.label }}
    </Checkbox>
  </div>
</template>

<style scoped>
.ant-checkbox-wrapper + .ant-checkbox-wrapper {
  margin-left: 6px;
}

.ant-checkbox-group .ant-checkbox-wrapper:first-child {
  margin-left: 6px;
}
</style>
