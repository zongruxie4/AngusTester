<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';

import { TaskInfo } from '../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const commentRef = ref();

onMounted(() => {
  watch(() => taskId.value, async (newValue) => {
    if (!newValue) {
      return;
    }

    if (typeof commentRef.value?.refresh === 'function') {
      commentRef.value.refresh();
    }
  }, { immediate: true });
});

const taskId = computed(() => {
  return props.dataSource?.id;
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">评论</div>

    <SmartComment
      ref="commentRef"
      class="overflow-auto pr-5"
      style="height: calc(100% - 30px);box-shadow: 0;"
      avatar
      targetType="TASK"
      :bordered="false"
      :public0="false"
      :showPublishTitle="false"
      :userId="props.userInfo?.id"
      :targetId="taskId"
      :action="`${TESTER}/comment`" />
  </div>
</template>

<style scoped>
:deep(.comment-list-title) {
  margin-top: -20px;
  margin-bottom: 14px;
}
</style>
