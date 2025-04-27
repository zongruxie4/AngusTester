<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  type: string,
  name: string,
  desc: string,
  icon: string,
  activeKey: string
}

const props = withDefaults(defineProps<Props>(), {
  type: '',
  name: '',
  desc: '',
  icon: '',
  activeKey: ''
});

</script>
<template>
  <div
    class="flex items-center py-2 text-3.5 leading-3.5 text-theme-title"
    :class="{ active: props.type == props.activeKey }">
    <div
      class="w-10 h-10 rounded-full bg-theme-tabs-selected text-center leading-10 text-theme-special mr-5 icon-box">
      <Icon :icon="props.icon" class="text-5" />
    </div>
    <div>
      <p>{{ props.name }}</p>
      <p class="text-3 leading-3 text-theme-sub-content mt-3.5">{{ props.desc }}</p>
    </div>
  </div>
</template>
<style scoped>
.active p {
  @apply text-text-link !important;
}

.active .icon-box {
  @apply bg-blue-1 text-white;
}
</style>
