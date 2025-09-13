<script setup lang="ts">
import { computed, ref } from 'vue';
import { ActivityInfo, Scroll } from '@xcan-angus/vue-ui';
import { CombinedTargetType, SearchCriteria, TESTER } from '@xcan-angus/infra';
import { ActivityItem } from '@/types/types';

// Props interface for Activity component
type Props = {
  /** Unique identifier of the target entity */
  id: string;
  /** Notification trigger for refreshing activity data */
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  notify: undefined
});

/**
 * List of activity items to be displayed
 */
const activityDataList = ref<ActivityItem[]>([]);

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
const handleDataChange = (data: ActivityItem[]) => {
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
    <ActivityInfo
      :dataSource="activityDataList"
      infoKey="description"
      class="pr-5" />
  </Scroll>
</template>
