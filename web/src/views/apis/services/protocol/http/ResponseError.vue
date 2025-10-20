<script lang="ts" setup>

interface Props {
  info: string;
  errorTitle: string;
}

withDefaults(defineProps<Props>(), {});
</script>
<template>
  <div class="flex-1 flex flex-col justify-center items-center">
    <img src="response-error.png" />
    <span class="font-medium text-3 leading-3">{{ errorTitle }}</span>
    <p class="text-text-sub-content mt-3">{{ info }}</p>
  </div>
</template>
