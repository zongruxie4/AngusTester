<script lang="ts" setup>
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  text: string,
}

const props = withDefaults(defineProps<Props>(), {
  text: undefined
});

</script>
<template>
  <div class="flex items-start px-3 border border-solid border-blue-border rounded text-3 leading-7.5 bg-blue-bg-light text-theme-content">
    <Icon icon="icon-shuoming" class="relative top-1.75 flex-shrink-0 mr-1.5 text-3.5 text-blue-badge-p" />
    <span>{{ props.text }}</span>
  </div>
</template>
