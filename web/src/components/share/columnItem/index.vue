<script setup lang="ts">
interface Props{
  label:string,
  className:string
}

withDefaults(defineProps<Props>(), {
  label: '',
  className: ''
});
</script>
<template>
  <div class="flex text-3 pb-2">
    <div :class="['text-left','text-text-content', className]">{{ label }}</div>
    <div class="text-text-sub-content flex-1 min-w-0">
      <slot></slot>
    </div>
  </div>
</template>
