<script setup lang="ts">
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    openIdConnectUrl: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

</script>
<template>
  <div class="flex items-center">
    <div class="flex-shrink-0 flex items-center w-31.75">
      <span class="text-theme-title">OpenId Connect URL</span>
      <Colon />
    </div>
    <a
      target="_blank"
      style="color:#1890ff"
      class="truncate"
      :title="props.value?.openIdConnectUrl"
      :href="props.value?.openIdConnectUrl">{{ props.value?.openIdConnectUrl }}</a>
  </div>
</template>
