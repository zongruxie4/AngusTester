<script setup lang="ts">
// Vue core imports
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Icon, Input } from '@xcan-angus/vue-ui';
import { Dropdown, Menu, MenuItem } from 'ant-design-vue';

const { t } = useI18n();

/**
 * Component props interface for editable selector
 */
interface Props {
  options: string[];
  value: string;
  [key: string]: any;
  maxLength: number;
}

// Component events
const emits = defineEmits<{(e: 'select', value: string): void}>();

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  options: () => ([]),
  value: undefined,
  maxLength: 4096
});

// Component state
const selectedInputValue = ref<string>();

/**
 * Handle option selection from dropdown
 * @param info - Menu click event info
 */
const handleOptionSelection = (info: { key: string | number }) => {
  selectedInputValue.value = String(info.key);
  setTimeout(() => {
    emits('select', selectedInputValue.value!);
  });
};

/**
 * Computed input properties excluding component-specific props
 */
const inputComponentProps = computed(() => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { value, options, ...otherProps } = props;
  return { ...otherProps };
});

// Watch for props value changes
watch(() => props.value, (newValue: string) => {
  selectedInputValue.value = newValue;
}, {
  immediate: true
});

</script>
<template>
  <Dropdown overlayClassName="border rounded" :overlayStyle="{maxHeight: '300px', overflow: 'auto'}">
    <Input
      v-model:value="selectedInputValue"
      :placeholder="t('commonComp.apis.editableSelector.placeholder')"
      v-bind="inputComponentProps"
      size="small">
      <template #suffix>
        <Icon icon="icon-xiajiantou" />
      </template>
    </Input>
    <template #overlay>
      <Menu @click="handleOptionSelection">
        <MenuItem v-for="item in props.options" :key="item">{{ item }}</MenuItem>
      </Menu>
    </template>
  </Dropdown>
</template>
