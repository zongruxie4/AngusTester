<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ActivityInfo, Scroll } from '@xcan-angus/vue-ui';
import { TESTER, SearchCriteria } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';

import { ActivityItem } from '@/types/types';
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
const activityList = ref<ActivityItem[]>([]);
const activityQueryParams = ref<{
  mainTargetId: string;
  filters: [{ key: 'targetType', value: CombinedTargetType.TASK, op: SearchCriteria.OpEnum }]
}>();

// Computed Properties
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

// Activity Management Functions
/**
 * <p>Handle activity data change</p>
 * <p>Updates the activity list when new data is received from the Scroll component</p>
 */
const handleActivityDataChange = (data: ActivityItem[]) => {
  activityList.value = data;
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => currentTaskId.value, (newTaskId, oldTaskId) => {
    if (newTaskId === oldTaskId) {
      return;
    }

    activityQueryParams.value = {
      mainTargetId: currentTaskId.value,
      filters: [{ key: 'targetType', value: CombinedTargetType.TASK, op: SearchCriteria.OpEnum.Equal }]
    };
  }, { immediate: true });
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('backlog.activity') }}
    </div>

    <Scroll
      :action="`${TESTER}/activity`"
      :hideNoData="!!activityList.length"
      :params="activityQueryParams"
      :lineHeight="32"
      transition
      style="height:calc(100% - 30px);"
      @change="handleActivityDataChange">
      <ActivityInfo
        :dataSource="activityList"
        infoKey="description"
        class="pr-5" />
    </Scroll>
  </div>
</template>
