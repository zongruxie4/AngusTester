
<script lang="ts" setup>
import { ref } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';

/**
 * Component props interface
 */
export interface Props {
  id: string;       // The unique identifier of the target scenario
  userId: string;   // The unique identifier of the current user
}

/**
 * Define props with default values
 * Note: undefined allows for proper type checking while maintaining optional behavior
 */
const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  userId: undefined
});

/**
 * Reference to the SmartComment component instance
 * Used for programmatic access to component methods if needed
 */
const smartCommentRef = ref();

</script>

<template>
  <SmartComment
    ref="smartCommentRef"
    targetType="SCENARIO"
    avatar
    size="small"
    :showPublishTitle="false"
    :bordered="false"
    :public0="false"
    :userId="props.userId"
    :targetId="props.id"
    :action="`${TESTER}/comment`" />
</template>
