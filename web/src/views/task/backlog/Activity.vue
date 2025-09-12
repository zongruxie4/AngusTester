<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ActivityInfo, Scroll } from '@xcan-angus/vue-ui';
import { TESTER, SearchCriteria } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';

import { ActivityItem } from '@/types/types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();
const dataList = ref<ActivityItem[]>([]);
const params = ref<{
  mainTargetId:string;
  filters:[{ key: 'targetType', value: CombinedTargetType.TASK, op: SearchCriteria.OpEnum }]
}>();

const change = (data: ActivityItem[]) => {
  dataList.value = data;
};

onMounted(() => {
  watch(() => taskId.value, (newValue, oldValue) => {
    if (newValue === oldValue) {
      return;
    }

    params.value = {
      mainTargetId: taskId.value,
      filters: [{ key: 'targetType', value: CombinedTargetType.TASK, op: SearchCriteria.OpEnum.Equal }]
    };
  }, { immediate: true });
});

const taskId = computed(() => {
  return props.dataSource?.id;
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('backlog.activity') }}
    </div>

    <Scroll
      :action="`${TESTER}/activity`"
      :hideNoData="!!dataList.length"
      :params="params"
      :lineHeight="32"
      transition
      style="height:calc(100% - 30px);"
      @change="change">
      <ActivityInfo
        :dataSource="dataList"
        infoKey="description"
        class="pr-5" />
    </Scroll>
  </div>
</template>
