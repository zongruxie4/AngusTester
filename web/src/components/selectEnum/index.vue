<script setup lang="ts">
import { computed, ref, watch, onMounted, useSlots } from 'vue';
import { Select, SelectOption } from 'ant-design-vue';
import * as infra from '@xcan-angus/infra';
import * as testerEnum from '@/enums/enums';

interface Props {
  enumKey: string;
  value?: string;
  excludes?: (value: {message:string;value:string;}) => boolean;
  lazy?: boolean;
  fieldNames?: { label: string; value: string; };
  notFoundContent?: string;
  defaultActiveFirstOption?: boolean;
  disabled?: boolean;
  disabledKeys?: string[];
  additionalOption?: {message:string;value:string;}[];
  format?: (data: { label: string; value: string;id:string;_value:string;message:string; }) => { label: string; value: string;id:string;_value:string;message:string; };// 格式化返回的数据格式，禁用可以在这个方法中设置
  error?: boolean;
  optionStyle?: { [key: string]: string|{ [key: string]: string } };
  optionLabelProp?: 'children' | 'label';
  readonly: boolean;
  internal?: boolean;// 是否渲染到父级节点下
}

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

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'dropdownVisibleChange', value: boolean): void;
  (e: 'update:value', value: string): void;
  (e: 'change', value: string, option: { label: string; value: string; }): void;
  (e: 'blur', value: any): void;
}>();

const slots = useSlots();
const selectRef = ref();

const selectValue = ref<string>();
const fieldKey = computed(() => props.fieldNames?.label || 'label');
const fieldValue = computed(() => props.fieldNames?.value || 'value');

const dropdownVisibleChange = async (open: boolean) => {
  emit('dropdownVisibleChange', open);
};


const onChange = (value: string) => {
  selectValue.value = value;
  const option = options.value.find(item => item[fieldValue.value] === value);
  emit('update:value', value);
  emit('change', value, option);
};

const blur = (event) => {
  event.target.value = selectValue.value;
  emit('blur', event);
};

const optionTitleMap = computed(() => {
  return options.value?.reduce((prev, cur) => {
    if (typeof slots.option === 'function') {
      prev[cur[fieldKey.value]] = '';
    } else {
      prev[cur[fieldKey.value]] = cur[fieldKey.value];
    }

    return prev;
  }, {});
});

const disabled = computed(() => {
  return props.disabled || props.readonly;
});

const className = computed(() => {
  let _class = '';
  if (props.error) {
    _class += 'error';
  }

  if (props.readonly) {
    if (_class) {
      _class += ' readonly-select';
    } else {
      _class += 'readonly-select';
    }
  }

  return _class;
});

const getPopupContainer = (node: HTMLElement): HTMLElement => {
  if (props.internal && node) {
    return node.parentNode as HTMLElement;
  }

  return document.body;
};

const options = ref<{value: string;label: string; disabled?: boolean}[]>([]);
const loadOptions = () => {
  const targetEnum = testerEnum[props.enumKey] || infra[props.enumKey];
  if (targetEnum) {
    options.value = infra.enumUtils.enumToMessages(targetEnum).map(i => ({value: i.value, label: i.message}));
  }
};

onMounted(() => {
  watch(() => props.enumKey, (newValue) => {
    if (!newValue) {
      return;
    }
    loadOptions();
  }, {
    immediate: true
  });
})
</script>
<template>
  <Select
    ref="selectRef"
    size="small"
    :error="props.error"
    :value="props.value"
    :disabled="disabled"
    :optionLabelProp="props.optionLabelProp"
    :class="className"
    :getPopupContainer="getPopupContainer"
    @dropdownVisibleChange="dropdownVisibleChange"
    @change="onChange"
    @blur="blur">
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
      v-for="item in options"
      :key="item[fieldValue]"
      :value="item[fieldValue]"
      :label="item[fieldKey]"
      :disabled="item.disabled"
      :title="optionTitleMap[item[fieldKey]]">
      <slot name="option" v-bind="item">
        <template v-if="optionStyle&&optionStyle[item[fieldValue]]">
          <div :title="item[fieldKey]" :style="optionStyle[item[fieldValue]]">{{ item[fieldKey] }}</div>
        </template>
        <template v-else>
          <div :title="item[fieldKey]">{{ item[fieldKey] }}</div>
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
