<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER, CombinedTargetType } from '@xcan-angus/infra';

import { TaskInfoProps } from '@/views/task/task/list/task/types';

// Component Props
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// Reactive State Variables
const smartCommentRef = ref();

// Computed Properties
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

// Comment Management Functions
/**
 * <p>Refresh comment component</p>
 * <p>Refreshes the comment list when task changes</p>
 */
const refreshCommentComponent = async (taskId: string) => {
  if (!taskId) {
    return;
  }
  if (typeof smartCommentRef.value?.refresh === 'function') {
    smartCommentRef.value.refresh();
  }
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => currentTaskId.value, async (newTaskId) => {
    await refreshCommentComponent(newTaskId);
  }, { immediate: true });
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('backlog.comment') }}
    </div>

    <SmartComment
      ref="smartCommentRef"
      class="overflow-auto pr-5"
      style="height: calc(100% - 30px);box-shadow: 0;"
      avatar
      :targetType="CombinedTargetType.TASK"
      :bordered="false"
      :public0="false"
      :showPublishTitle="false"
      :userId="props.userInfo?.id"
      :targetId="currentTaskId"
      :action="`${TESTER}/comment`" />
  </div>
</template>

<style scoped>
:deep(.comment-list-title) {
  margin-top: -20px;
  margin-bottom: 14px;
}
</style>
