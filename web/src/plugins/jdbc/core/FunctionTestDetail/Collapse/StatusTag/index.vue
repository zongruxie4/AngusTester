<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  value?: 'success' | 'fail' | 'ignore';
}

const props = withDefaults(defineProps<Props>(), {
  value: 'ignore'
});

const text = computed(() => {
  if (props.value === 'fail') {
    return '未通过';
  }

  if (props.value === 'success') {
    return '通过';
  }

  return '未启用';
});

const style = computed(() => {
  if (props.value === 'fail') {
    return 'background-color:rgba(255, 129, 0 , 0.8);';
  }

  if (props.value === 'success') {
    return 'background-color:#52c41a;';
  }

  return 'background-color:rgba(217, 217, 217, 1);';
});
</script>
<template>
  <div :style="style" class="inline-block min-w-13 text-center px-2 rounded text-3 leading-5 text-white">{{ text }}</div>
</template>
