<script setup lang="ts">
import { computed, ref } from 'vue';
import { ActivityInfo as Activity, Scroll } from '@xcan-angus/vue-ui';
import { CombinedTargetType, SearchCriteria, TESTER } from '@xcan-angus/infra';
import { ActivityInfo, BaseProps } from '@/types/types';

// Props interface for Activity component
const props = withDefaults(defineProps<BaseProps>(), {
  id: undefined,
  notify: undefined
});

/**
 * List of activity items to be displayed
 */
const activityDataList = ref<ActivityInfo[]>([]);

/**
 * API parameters for fetching activity data
 * <p>
 * Constructs the query parameters including the main target ID
 * and filters to retrieve only task-related activities
 */
const apiParams = computed(() => {
  return {
    mainTargetId: props.id,
    filters: [{
      key: 'targetType',
      value: CombinedTargetType.TASK,
      op: SearchCriteria.OpEnum.Equal
    }]
  };
});

/**
 * Handles data change from the Scroll component
 * <p>
 * Updates the local activity data when new data is received from the API
 *
 * @param data - Array of activity items from the server
 */
const handleDataChange = (data: ActivityInfo[]) => {
  activityDataList.value = data;
};
</script>
<template>
  <Scroll
    :action="`${TESTER}/activity`"
    :hideNoData="!!activityDataList.length"
    :params="apiParams"
    :lineHeight="32"
    :notify="props.notify"
    transition
    class="h-full"
    @change="handleDataChange">
    <Activity
      :dataSource="activityDataList"
      infoKey="description"
      class="pr-5" />
  </Scroll>
</template>
