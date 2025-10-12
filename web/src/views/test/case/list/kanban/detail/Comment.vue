<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { CombinedTargetType } from '@/enums/enums';

import { CaseInfoEditProps } from '@/views/test/case/list/types';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const commentRef = ref();

/**
 * Initialize comment widget watchers
 * Refreshes SmartComment when selected case changes
 */
onMounted(() => {
  watch(() => caseId.value, async (newValue) => {
    if (!newValue) {
      return;
    }

    if (typeof commentRef.value?.refresh === 'function') {
      commentRef.value.refresh();
    }
  }, { immediate: true });
});

const caseId = computed(() => {
  return props.dataSource?.id;
});
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.comment') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <SmartComment
          v-if="caseId"
          ref="commentRef"
          class="overflow-auto pr-5"
          style="height: calc(100% - 30px);box-shadow: 0;"
          avatar
          :targetType="CombinedTargetType.FUNC_CASE"
          :bordered="false"
          :public0="false"
          :showPublishTitle="false"
          :userId="props.userInfo?.id"
          :targetId="caseId"
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
