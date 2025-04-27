<script setup lang="ts">
import { computed } from 'vue';
import { TypographyParagraph } from 'ant-design-vue';
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    type: 'boolean';
    nullable: boolean;
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
        <div class="text-theme-sub-content w-13 flex-shrink-0">
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
        <div class="text-theme-sub-content w-13 flex-shrink-0">
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
