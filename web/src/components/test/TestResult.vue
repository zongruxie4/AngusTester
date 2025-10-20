<script setup lang="ts">
import { computed } from 'vue';

type Props = {
  value: {
    value: 'PENDING' | 'PASSED' | 'NOT_PASSED' | 'BLOCKED' | 'CANCELED';
    message: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const COLOR: {
  PENDING: string;
  PASSED: string;
  NOT_PASSED: string;
  BLOCKED: string;
  CANCELED: string;
} = {
  PENDING: '#FF8100',
  PASSED: '#52c41a',
  NOT_PASSED: '#f5222d',
  BLOCKED: '#ffa52b',
  CANCELED: '#B7BBC2'
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
