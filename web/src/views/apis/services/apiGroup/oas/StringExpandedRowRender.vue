<script setup lang="ts">
import { computed } from 'vue';
import { TypographyParagraph } from 'ant-design-vue';
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    type: 'string';
    minLength: number;
    maxLength: number;
    enum: string[] | number[];
    pattern: string;
    default: string;
    nullable: boolean;
    'x-xc-example': string;
    description: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const nullable = computed(() => {
  if (!props.value) {
    return undefined;
  }

  if (props.value.nullable === false) {
    return 'false';
  }

  if (props.value.nullable === true) {
    return 'true';
  }

  return props.value.nullable;
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

const defaultValue = computed(() => {
  if (!props.value) {
    return '';
  }

  return props.value.default;
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
          <span>最小长度</span>
          <Colon />
        </div>
        <div class="break-all">{{ minLength }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-17.75 flex-shrink-0">
          <span>最大长度</span>
          <Colon />
        </div>
        <div class="break-all">{{ maxLength }}</div>
      </div>
    </div>

    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          <span>枚举</span>
          <Colon />
        </div>
        <div class="break-all">{{ enumList }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-17.75 flex-shrink-0">
          <span>正则</span>
          <Colon />
        </div>
        <div class="break-all">{{ pattern }}</div>
      </div>
    </div>

    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          <span>默认值</span>
          <Colon />
        </div>
        <div class="break-all">{{ defaultValue }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-17.75 flex-shrink-0">
          <span>允许为null</span>
          <Colon />
        </div>
        <div class="break-all">{{ nullable }}</div>
      </div>
    </div>

    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-16 flex-shrink-0">
          <span>描述</span>
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
