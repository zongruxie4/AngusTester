<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
</script>
<template>
  <div class="h-full flex flex-col">
    <div class="text-right pt-3">
      <Icon icon="icon-a-logodaimingcheng" :style="{height: '50px', width: 'auto'}" />
    </div>
    <div class="flex-1 flex flex-col">
      <RouterView />
    </div>
  </div>
</template>
