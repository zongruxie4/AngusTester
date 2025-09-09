<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';

type Props = {
  id: string;
  notify: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  notify: undefined,
  userInfo: undefined
});

const commentRef = ref();

onMounted(() => {
  watch(() => props.notify, async (newValue) => {
    if (!newValue) {
      return;
    }

    if (typeof commentRef.value?.refresh === 'function') {
      commentRef.value.refresh();
    }
  }, { immediate: true });
});
</script>
<template>
  <SmartComment
    ref="commentRef"
    class="h-full overflow-y-auto pr-5"
    style="box-shadow: 0;"
    avatar
    targetType="TASK"
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
