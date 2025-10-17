<script setup lang="ts">
import { Checkbox } from 'ant-design-vue';
import { ServicesPermission } from '@/enums/enums';

interface CheckboxOption {
  label: string;
  value: string;
}

interface Props {
  options: CheckboxOption[];
  disabled: boolean;
  value: string[];
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  disabled: false,
  value: () => []
});

const emit = defineEmits<{(e: 'change', value: string[]) }>();

/**
 * Handle checkbox change event with permission dependency logic
 * When unchecking modify permission, also uncheck release permission
 * When checking release permission, also check modify permission
 * When checking any permission except view, also check view permission
 */
const handleCheckboxChange = (event: { target: { checked: boolean; } }, selectedValue: string) => {
  const isChecked = event.target.checked;

  // Handle unchecking logic
  if (!isChecked) {
    const permissionsToExclude = [selectedValue];

    // If unchecking modify permission, also uncheck release permission
    if (selectedValue === ServicesPermission.MODIFY) {
      permissionsToExclude.push(ServicesPermission.RELEASE);
    }

    const updatedPermissions = selectedValue !== ServicesPermission.VIEW
      ? props.value.filter(permission => !permissionsToExclude.includes(permission))
      : [];

    emit('change', updatedPermissions);
    return;
  }

  // Handle checking logic
  const newPermissions = selectedValue !== ServicesPermission.VIEW && !props.value.includes(ServicesPermission.VIEW)
    ? [selectedValue, ServicesPermission.VIEW]
    : [selectedValue];

  // If checking release permission, also check modify permission
  if (selectedValue === ServicesPermission.RELEASE) {
    newPermissions.push(ServicesPermission.MODIFY);
  }

  // Merge with existing permissions and remove duplicates
  const allPermissions = new Set(props.value.concat(...newPermissions));
  emit('change', Array.from(allPermissions));
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
