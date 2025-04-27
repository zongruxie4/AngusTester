<script setup lang="ts">
import { ActivityTimeline } from '@xcan-angus/vue-ui';

export interface Props {
  projectId:string;
  id:string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  id: undefined
});

const types = ['SCENARIO'];
</script>

<template>
  <ActivityTimeline
    :id="props.id"
    :projectId="props.projectId"
    :types="types" />
</template>
