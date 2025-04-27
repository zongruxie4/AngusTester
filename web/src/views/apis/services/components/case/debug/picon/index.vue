<script setup lang="ts">
import { computed } from 'vue';
import { IconText } from '@xcan-angus/vue-ui';

interface Props {
    targetType?:'PROJECT'|'SERVICE';
}

const props = withDefaults(defineProps<Props>(), {
  targetType: 'PROJECT'
});

const className = computed(() => {
  if (props.targetType === 'PROJECT') {
    return 'bg-blue-badge-p';
  }

  return 'bg-blue-badge-s';
});

const text = computed(() => {
  return props.targetType === 'PROJECT' ? 'P' : 'S';
});
</script>
<template>
  <IconText
    :text="text"
    :class="className"
    class="h-4 w-4 text-3" />
</template>
