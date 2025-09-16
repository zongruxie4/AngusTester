<script setup lang="ts">
import { computed } from 'vue';

type Props = {
  value: {
    value: 'HIGHEST' | 'HIGH' | 'MEDIUM' | 'LOW' | 'LOWEST';
    label: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const COLOR: {
  HIGHEST: string;
  HIGH: string;
  MEDIUM: string;
  LOW: string;
  LOWEST: string;
} = {
  HIGHEST: '#F5222D',
  HIGH: '#FF8100',
  MEDIUM: '#FFB925',
  LOW: '#52C41A',
  LOWEST: '#67D7FF'
};

const value = computed(() => {
  return props.value?.value;
});

const message = computed(() => {
  return props.value?.label;
});
</script>

<template>
  <div
    v-if="props.value"
    class="text-3 flex-shrink-0"
    :style="'color:' + COLOR[value]">
    {{ message }}
  </div>
</template>
