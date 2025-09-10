<script setup lang="ts">
import { computed, ref } from 'vue';
import { ActivityInfo, Scroll } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';

import { ActivityItem } from '@/views/task/task/list/task/flat/types';

type Props = {
  id: string;
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  notify: undefined
});

const dataList = ref<ActivityItem[]>([]);

const change = (data: ActivityItem[]) => {
  dataList.value = data;
};

const params = computed(() => {
  return {
    mainTargetId: props.id,
    filters: [{ key: 'targetType', value: 'TASK', op: 'EQUAL' }]
  };
});
</script>
<template>
  <Scroll
    :action="`${TESTER}/activity`"
    :hideNoData="!!dataList.length"
    :params="params"
    :lineHeight="32"
    :notify="props.notify"
    transition
    class="h-full"
    @change="change">
    <ActivityInfo
      :dataSource="dataList"
      infoKey="description"
      class="pr-5" />
  </Scroll>
</template>
