<script setup lang="ts">
import { computed } from 'vue';

type Props = {
  value: {
    value: 'CANCELED' | 'COMPLETED' | 'IN_PROGRESS' | 'PENDING' | 'CONFIRMING';
    message: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const COLOR: {
  CANCELED: string;
  COMPLETED: string;
  IN_PROGRESS: string;
  PENDING: string;
  CONFIRMING: string;
} = {
  CANCELED: '#B7BBC2',
  COMPLETED: '#52C41A',
  IN_PROGRESS: '#2D8EFF',
  PENDING: '#FF8100',
  CONFIRMING: '#67D7FF'
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
