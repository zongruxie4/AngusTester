
<script setup lang="ts">
import { ActivityTimeline } from '@xcan-angus/vue-ui';

/**
 * Component props interface
 */
export interface Props {
  projectId: string; // The unique identifier of the project
  id: string;        // The unique identifier of the scenario/entity
}

/**
 * Define props with default values
 * Note: undefined allows for proper type checking while maintaining optional behavior
 */
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  id: undefined
});

/**
 * Activity types to display in the timeline
 * Currently configured to show only SCENARIO type activities
 */
const activityTypes = ['SCENARIO'] as any;
</script>

<template>
  <!-- Activity timeline component displaying scenario-related activities -->
  <ActivityTimeline
    :id="props.id"
    :project-id="props.projectId"
    :types="activityTypes" />
</template>
