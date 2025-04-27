<script setup lang="ts">
import { computed } from 'vue';
import { TypographyParagraph } from 'ant-design-vue';
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    type: 'string' | 'number' | 'integer' | 'boolean' | 'array' | 'object';
    minimum: number;
    maximum: number;
    minLength: number;
    maxLength: number;
    enum: string[] | number[];
    pattern: string;
    default: string;
    items: {
      type: 'string' | 'number' | 'integer' | 'boolean' | 'array' | 'object';
    };
    'x-xc-example': string;
    description: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const minimum = computed(() => {
  return props.value?.minimum;
});

const maximum = computed(() => {
  return props.value?.maximum;
});

const minLength = computed(() => {
  return props.value?.minLength;
});

const maxLength = computed(() => {
  return props.value?.maxLength;
});

const enumList = computed(() => {
  if (props.value?.enum?.length) {
    return JSON.stringify(props.value?.enum);
  }

  return '';
});

const pattern = computed(() => {
  return props.value?.pattern;
});

const example = computed(() => {
  if (!props.value) {
    return '';
  }

  return JSON.stringify(props.value['x-xc-example']);
});

const defaultValue = computed(() => {
  if (!props.value) {
    return '';
  }

  return JSON.stringify(props.value.default);
});

const description = computed(() => {
  return props.value?.description;
});
</script>

<template>
  <div class="py-4 space-y-2.5 leading-5">
    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          默认值
          <Colon />
        </div>
        <div class="break-all">{{ defaultValue }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          示例值
          <Colon />
        </div>
        <div class="break-all">{{ example }}</div>
      </div>
    </div>
    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          最小值
          <Colon />
        </div>
        <div class="break-all">{{ maximum }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          最大值
          <Colon />
        </div>
        <div class="break-all">{{ minimum }}</div>
      </div>
    </div>
    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          最小长度
          <Colon />
        </div>
        <div class="break-all">{{ maxLength }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          最大长度
          <Colon />
        </div>
        <div class="break-all">{{ minLength }}</div>
      </div>
    </div>
    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          枚举
          <Colon />
        </div>
        <div class="break-all">{{ enumList }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          正则
          <Colon />
        </div>
        <div class="break-all">{{ pattern }}</div>
      </div>
    </div>
    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          描述
          <Colon />
        </div>
        <TypographyParagraph
          class="break-all"
          :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }"
          :content="description" />
      </div>
    </div>
  </div>
</template>
