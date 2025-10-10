<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { CombinedTargetType, TESTER } from '@xcan-angus/infra';

/**
 * Props interface for Comment component
 * <p>
 * Defines the required properties for displaying and managing
 * comments related to a specific task or entity.
 */
type Props = {
  /** Unique identifier of the target entity */
  id: number;
  /** Notification trigger for refreshing comment data */
  notify: string;
  /** User information object containing user ID */
  userInfo: { id: number; };
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  notify: undefined,
  userInfo: undefined
});

/**
 * Reference to the SmartComment component instance
 * <p>
 * Used to programmatically control the comment component
 * and trigger data refresh operations
 */
const smartCommentRef = ref();

/**
 * Component mounted lifecycle hook
 * <p>
 * Sets up a watcher to monitor notification changes and refresh
 * the comment data when notifications are received
 */
onMounted(() => {
  watch(() => props.notify, async (notificationValue) => {
    if (!notificationValue) {
      return;
    }

    if (typeof smartCommentRef.value?.refresh === 'function') {
      smartCommentRef.value.refresh();
    }
  }, { immediate: true });
});
</script>
<template>
  <!-- Smart comment component for task comments -->
  <SmartComment
    ref="smartCommentRef"
    class="h-full overflow-y-auto pr-5"
    style="box-shadow: 0;"
    avatar
    :targetType="CombinedTargetType.TASK"
    :bordered="false"
    :public0="false"
    :showPublishTitle="false"
    :userId="props.userInfo?.id"
    :targetId="props.id"
    :action="`${TESTER}/comment`" />
</template>

<style scoped>
:deep(.comment-list-title) {
  margin-top: -20px;
  margin-bottom: 14px;
}
</style>
