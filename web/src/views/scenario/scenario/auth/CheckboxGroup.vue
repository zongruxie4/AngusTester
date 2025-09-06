<script setup lang="ts">
import { Checkbox } from 'ant-design-vue';
import type { CheckboxGroupProps, CheckboxChangeEvent } from './types';

// Component props with proper typing
const props = withDefaults(defineProps<CheckboxGroupProps>(), {
  options: () => [],
  disabled: false,
  value: () => []
});

// Define emits with proper typing
const emit = defineEmits<{
  (e: 'change', value: string[]): void;
}>();

/**
 * Handle individual checkbox change
 * Automatically includes 'VIEW' permission when other permissions are selected
 */
const handleCheckboxChange = (event: CheckboxChangeEvent, value: string) => {
  const checked = event.target.checked;
  
  if (checked && !props.value.includes(value)) {
    // If selecting a non-VIEW permission and VIEW is not already included, add both
    const newValue = value !== 'VIEW' && !props.value.includes('VIEW') 
      ? props.value.concat([value, 'VIEW']) 
      : props.value.concat([value]);
    emit('change', newValue);
    return;
  }

  // If unchecking, remove the permission (and VIEW if it's not VIEW being unchecked)
  const filteredValue = value !== 'VIEW' 
    ? props.value.filter(item => item !== value) 
    : [];
  emit('change', filteredValue);
};
</script>
<template>
  <div class="ant-checkbox-group">
    <Checkbox
      v-for="item in props.options"
      :key="item.value"
      :disabled="props.disabled"
      :checked="props.value.includes(item.value)"
      @change="handleCheckboxChange($event, item.value)">
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
