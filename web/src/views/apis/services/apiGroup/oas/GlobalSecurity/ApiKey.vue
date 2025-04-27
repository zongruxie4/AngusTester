<script setup lang="ts">
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    in: string;
    name: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

</script>
<template>
  <div>
    <div class="flex items-center mt-3">
      <div class="flex items-center w-9">
        <span class="text-theme-title">位置</span>
        <Colon />
      </div>
      <div>{{ props.value?.in }}</div>
    </div>
    <div class="flex items-center mt-1.5">
      <div class="flex items-center w-9">
        <span class="text-theme-title">名称</span>
        <Colon />
      </div>
      <div>{{ props.value?.name }}</div>
    </div>
  </div>
</template>
