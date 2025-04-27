<script setup lang="ts">
import { Checkbox } from 'ant-design-vue';

interface Props {
    options: { label: string; value: string }[];
    disabled: boolean;
    value: string[];
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  disabled: false,
  value: () => []
});

const emit = defineEmits<{(e: 'change', value: string[]) }>();

const change = (event: { target: { checked: boolean; } }, value: string) => {
  const checked = event.target.checked;
  if (checked && !props.value.includes(value)) {
    const newValue = value !== 'VIEW' && !props.value.includes('VIEW') ? props.value.concat([value, 'VIEW']) : props.value.concat([value]);
    emit('change', newValue);
    return;
  }

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
