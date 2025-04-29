<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';

import { CaseInfo } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const commentRef = ref();

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
    <div class="text-theme-title mb-2.5 font-semibold">评论</div>

    <SmartComment
      v-if="caseId"
      ref="commentRef"
      class="overflow-auto"
      style="height: calc(100% - 30px);box-shadow: 0;"
      avatar
      targetType="FUNC_CASE"
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
