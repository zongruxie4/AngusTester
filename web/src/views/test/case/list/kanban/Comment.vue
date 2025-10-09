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
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('common.comment') }}
    </div>

    <SmartComment
      v-if="caseId"
      ref="commentRef"
      class="overflow-auto"
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
</template>

<style scoped>
:deep(.comment-list-title) {
  margin-top: -20px;
  margin-bottom: 14px;
}
</style>
