<script lang="ts" setup>
import { onMounted, nextTick } from 'vue';

interface Props {
  info: string;
  errorTitle: string;
}

withDefaults(defineProps<Props>(), {});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>
<template>
  <div class="flex-1 flex flex-col justify-center items-center">
    <img src="./response-error.png" />
    <span class="font-medium text-3 leading-3">{{ errorTitle }}</span>
    <p class="text-theme-sub-content mt-3">{{ info }}</p>
  </div>
</template>
