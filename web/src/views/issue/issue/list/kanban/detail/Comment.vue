<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { CombinedTargetType } from '@/enums/enums';
import { AssocCaseProps } from '@/views/issue/issue/list/types';

// Component Props
const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// Reactive State Variables
const smartCommentRef = ref();

/**
 * <p>Get current task ID from props</p>
 * <p>Returns the task ID for comment operations</p>
 */
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

/**
 * <p>Initialize comment component on mount</p>
 * <p>Sets up watcher to refresh comments when task ID changes</p>
 */
onMounted(() => {
  watch(() => currentTaskId.value, async (newTaskId) => {
    if (!newTaskId) {
      return;
    }

    if (typeof smartCommentRef.value?.refresh === 'function') {
      smartCommentRef.value.refresh();
    }
  }, { immediate: true });
});
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('task.comment.title') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <SmartComment
          v-if="currentTaskId"
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
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

:deep(.comment-list-title) {
  margin-top: -20px;
  margin-bottom: 14px;
}
</style>
