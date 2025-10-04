<script setup lang="ts">
import { computed, ref, watch, onMounted, useSlots } from 'vue';
import { Select, SelectOption } from 'ant-design-vue';
import * as infra from '@xcan-angus/infra';
import * as testerEnum from '@/enums/enums';

/**
 * Props interface for SelectEnum component
 * <p>
 * Defines all available configuration options for the enum select component
 * </p>
 */
interface Props {
  enumKey: string;
  value?: string | infra.SearchCriteria.OpEnum;
  excludes?: (value: {message:string;value:string;}) => boolean;
  lazy?: boolean;
  fieldNames?: { label: string; value: string; };
  notFoundContent?: string;
  defaultActiveFirstOption?: boolean;
  disabled?: boolean;
  disabledKeys?: string[];
  additionalOption?: {message:string;value:string;}[];
  /**
   * Format function for customizing option data structure
   * <p>
   * Can be used to disable options or modify their display properties
   * </p>
   */
  format?: (data: { label: string; value: string;id:string;_value:string;message:string; }) => { label: string; value: string;id:string;_value:string;message:string; };
  error?: boolean;
  optionStyle?: { [key: string]: string|{ [key: string]: string } };
  optionLabelProp?: 'children' | 'label';
  readonly: boolean;
  /**
   * Whether to render dropdown to parent node instead of document body
   */
  internal?: boolean;
  allowClear?: boolean;
}

// Props and Emits Configuration
const props = withDefaults(defineProps<Props>(), {
  enumKey: undefined,
  value: undefined,
  excludes: undefined,
  lazy: true,
  fieldNames: () => ({ label: 'label', value: 'value' }),
  notFoundContent: undefined,
  defaultActiveFirstOption: false,
  disabled: false,
  disabledKeys: () => [],
  additionalOption: undefined,
  format: undefined,
  error: false,
  optionStyle: undefined,
  optionLabelProp: 'children',
  readonly: false,
  internal: false
});

/**
 * Component event emissions
 * <p>
 * Defines all events that can be emitted by this component
 * </p>
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'dropdownVisibleChange', value: boolean): void;
  (e: 'update:value', value: string): void;
  (e: 'change', value: string, option?: { label: string; value: string; }): void;
  (e: 'blur', value: any): void;
}>();

// Reactive References and Computed Properties
const slots = useSlots();
const selectRef = ref();

/**
 * Current selected value
 */
const currentSelectedValue = ref<string>();

/**
 * Computed field name for option labels
 */
const labelFieldName = computed(() => props.fieldNames?.label || 'label');

/**
 * Computed field name for option values
 */
const valueFieldName = computed(() => props.fieldNames?.value || 'value');

/**
 * Available options for the select dropdown
 */
const availableOptions = ref<{value: string;label: string; disabled?: boolean}[]>([]);

/**
 * <p>
 * Map of option titles for tooltip display
 * </p>
 * <p>
 * Handles custom slot rendering by setting empty titles for custom option slots
 * </p>
 */
const optionTitleMapping = computed(() => {
  return availableOptions.value?.reduce((titleMap, currentOption) => {
    if (typeof slots.option === 'function') {
      titleMap[currentOption[labelFieldName.value]] = '';
    } else {
      titleMap[currentOption[labelFieldName.value]] = currentOption[labelFieldName.value];
    }
    return titleMap;
  }, {});
});

/**
 * Computed disabled state
 * <p>
 * Component is disabled if either disabled prop is true or readonly prop is true
 * </p>
 */
const isComponentDisabled = computed(() => {
  return props.disabled || props.readonly;
});

/**
 * Computed CSS class names
 * <p>
 * Applies error and readonly styling based on component state
 * </p>
 */
const componentClassName = computed(() => {
  let cssClass = '';

  if (props.error) {
    cssClass += 'error';
  }

  if (props.readonly) {
    if (cssClass) {
      cssClass += ' readonly-select';
    } else {
      cssClass += 'readonly-select';
    }
  }

  return cssClass;
});

/**
 * Handles dropdown visibility change events
 * <p>
 * Emits dropdownVisibleChange event to parent component
 * </p>
 * @param isOpen - Whether dropdown is open or closed
 */
const handleDropdownVisibilityChange = async (isOpen: boolean) => {
  emit('dropdownVisibleChange', isOpen);
};

/**
 * Handles value change events
 * <p>
 * Updates internal state and emits change events to parent component
 * </p>
 * @param selectedValue - The newly selected value
 */
const handleValueChange = (selectedValue: any) => {
  if (selectedValue === undefined || selectedValue === null) {
    selectedValue = undefined;
  }

  const stringValue = selectedValue ? String(selectedValue) : selectedValue;
  currentSelectedValue.value = stringValue;
  const option = availableOptions.value.find(opt => opt[valueFieldName.value] === stringValue);
  emit('update:value', stringValue);
  emit('change', stringValue, option);
};

/**
 * Handles blur events
 * <p>
 * Ensures the input value matches the selected value and emits blur event
 * </p>
 * @param event - The blur event object
 */
const handleBlur = (event: any) => {
  event.target.value = currentSelectedValue.value;
  emit('blur', event);
};

/**
 * Determines the popup container for the dropdown
 * <p>
 * Returns parent node if internal prop is true, otherwise returns document body
 * </p>
 * @param node - The trigger element
 * @returns The container element for the dropdown
 */
const getDropdownContainer = (node: HTMLElement): HTMLElement => {
  if (props.internal && node) {
    return node.parentNode as HTMLElement;
  }
  return document.body;
};

/**
 * Loads enum options based on the enumKey prop
 * <p>
 * Searches for enum in both testerEnum and infra modules and converts to select options
 * </p>
 */
const loadEnumOptions = () => {
  const targetEnum = testerEnum[props.enumKey] || infra[props.enumKey];
  if (targetEnum) {
    availableOptions.value = infra.enumUtils.enumToMessages(targetEnum).map(enumItem => ({
      value: enumItem.value,
      label: enumItem.message
    }));
  }
};

/**
 * Component mounted lifecycle hook
 * <p>
 * Sets up watcher for enumKey prop changes and loads initial options
 * </p>
 */
onMounted(() => {
  watch(() => props.enumKey, (newEnumKey) => {
    if (!newEnumKey) {
      return;
    }
    loadEnumOptions();
  }, {
    immediate: true
  });
});
</script>
<template>
  <Select
    ref="selectRef"
    size="small"
    v-bind="props"
    :error="props.error"
    :value="props.value"
    :disabled="isComponentDisabled"
    :optionLabelProp="props.optionLabelProp"
    :class="componentClassName"
    :getPopupContainer="getDropdownContainer"
    @dropdownVisibleChange="handleDropdownVisibilityChange"
    @change="handleValueChange"
    @blur="handleBlur">
    <template #notFoundContent>
      <NoData
        :text="props.notFoundContent"
        size="small"
        class="my-5" />
    </template>
    <template v-if="$slots.clearIcon" #clearIcon>
      <slot name="clearIcon"></slot>
    </template>
    <template v-if="$slots.dropdownRender" #dropdownRender="record">
      <slot name="dropdownRender" v-bind="record"></slot>
    </template>
    <template v-if="$slots.maxTagPlaceholder" #maxTagPlaceholder="record">
      <slot name="maxTagPlaceholder" v-bind="record"></slot>
    </template>
    <template v-if="$slots.menuItemSelectedIcon" #menuItemSelectedIcon>
      <slot name="menuItemSelectedIcon"></slot>
    </template>
    <template v-if="$slots.placeholder" #placeholder>
      <slot name="placeholder"></slot>
    </template>
    <template v-if="$slots.removeIcon" #removeIcon>
      <slot name="removeIcon"></slot>
    </template>
    <template v-if="$slots.suffixIcon" #suffixIcon>
      <slot name="suffixIcon"></slot>
    </template>
    <template v-if="$slots.tagRender" #tagRender="record">
      <slot name="tagRender" v-bind="record"></slot>
    </template>

    <SelectOption
      v-for="option in availableOptions"
      :key="option[valueFieldName]"
      :value="option[valueFieldName]"
      :label="option[labelFieldName]"
      :disabled="option.disabled"
      :title="optionTitleMapping[option[labelFieldName]]">
      <slot name="option" v-bind="option">
        <template v-if="props.optionStyle && props.optionStyle[option[valueFieldName]]">
          <div :title="option[labelFieldName]" :style="props.optionStyle[option[valueFieldName]]">{{ option[labelFieldName] }}</div>
        </template>
        <template v-else>
          <div :title="option[labelFieldName]">{{ option[labelFieldName] }}</div>
        </template>
      </slot>
    </SelectOption>
  </Select>
</template>
<style scoped>
.readonly-select.ant-select.ant-select-disabled :deep(.ant-select-selector) {
  color: inherit;
  cursor: default;
}
</style>
