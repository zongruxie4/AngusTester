<script setup lang="ts">
import { computed } from 'vue';
import { TypographyParagraph } from 'ant-design-vue';
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    type: 'integer';
    minimum: number;
    maximum: number;
    multipleOf: number;
    nullable: boolean;
    exclusiveMinimum: boolean;
    exclusiveMaximum: boolean;
    default: string;
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

const minimum = computed(() => {
  return props.value?.minimum;
});

const maximum = computed(() => {
  return props.value?.maximum;
});

const multipleOf = computed(() => {
  return props.value?.multipleOf;
});

const exclusiveMinimum = computed(() => {
  if (!props.value) {
    return undefined;
  }

  if (props.value.exclusiveMinimum === false) {
    return 'false';
  }

  if (props.value.exclusiveMinimum === true) {
    return 'true';
  }

  return props.value.exclusiveMinimum;
});

const exclusiveMaximum = computed(() => {
  if (!props.value) {
    return undefined;
  }

  if (props.value.exclusiveMaximum === false) {
    return 'false';
  }

  if (props.value.exclusiveMaximum === true) {
    return 'true';
  }

  return props.value.exclusiveMaximum;
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
        <div class="text-theme-sub-content w-25 flex-shrink-0">
          <span>最小值</span>
          <Colon />
        </div>
        <div class="break-all">{{ maximum }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-25 flex-shrink-0">
          <span>最大值</span>
          <Colon />
        </div>
        <div class="break-all">{{ minimum }}</div>
      </div>
    </div>

    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-25 flex-shrink-0 relative">
          <!-- <Icon icon="icon-tishi1" class="absolute -left-4 top-0.75 text-tips text-3.5 mr-1 cursor-pointer" /> -->
          <span>排除最小边界值<Colon /></span>
        </div>
        <div class="break-all">{{ exclusiveMinimum }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-25 flex-shrink-0 relative">
          <!-- <Icon icon="icon-tishi1" class="absolute -left-4 top-0.75 text-tips text-3.5 mr-1 cursor-pointer" /> -->
          <span>排除最大边界值<Colon /></span>
        </div>
        <div class="break-all">{{ exclusiveMaximum }}</div>
      </div>
    </div>

    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-25 flex-shrink-0">
          <span>默认值</span>
          <Colon />
        </div>
        <div class="break-all">{{ defaultValue }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-25 flex-shrink-0">
          <span>允许为null</span>
          <Colon />
        </div>
        <div class="break-all">{{ nullable }}</div>
      </div>
    </div>

    <div class="flex items-start px-5 space-x-5">
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-25 flex-shrink-0">
          <span>倍数基数</span>
          <Colon />
        </div>
        <div class="break-all">{{ multipleOf }}</div>
      </div>
      <div class="flex items-start w-1/2 flex-shrink-0">
        <div class="text-theme-sub-content w-25 flex-shrink-0">
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
