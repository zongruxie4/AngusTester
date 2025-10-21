<script setup lang="ts">
import { computed } from 'vue';
import { ReviewStatus } from '@xcan-angus/infra';

type Props = {
  value: {
    value: ReviewStatus;
    message: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const COLOR: {
  FAILED: string;
  PASSED: string;
  PENDING: string;
} = {
  FAILED: '#f5222d',
  PASSED: '#52C41A',
  PENDING: '#FF8100'
};

const value = computed(() => {
  return props.value?.value;
});

const message = computed(() => {
  return props.value?.message;
});
</script>

<template>
  <div v-if="props.value" class="flex-shrink-0 inline-flex items-center text-3">
    <em :style="'background-color:' + COLOR[value]" class="block not-italic w-1.5 h-1.5 mr-1 rounded"></em>
    <span>{{ message }}</span>
  </div>
</template>
