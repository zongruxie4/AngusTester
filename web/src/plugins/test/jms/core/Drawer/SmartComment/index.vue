<script lang="ts" setup>
import { ref } from 'vue';
import { SmartComment } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';

export interface Props {
    id:string|undefined;
    userId:string|undefined;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  userId: undefined
});

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
