<script setup lang="ts">
import { ActivityTimeline } from '@xcan-angus/vue-ui';

export interface Props {
  id:string|undefined;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});
</script>

<template>
  <ActivityTimeline :id="props.id" type="SCENARIO" />
</template>
