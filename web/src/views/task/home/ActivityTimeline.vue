<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { CombinedTargetType } from '@xcan-angus/infra';

/**
 * Props interface for ActivityTimeline component.
 */
type Props = {
  projectId: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined
});

const { t } = useI18n();

/**
 * Resource types to query for activity timeline.
 */
const activityResourceTypes = [
  CombinedTargetType.TASK,
  CombinedTargetType.TASK_SPRINT,
  CombinedTargetType.MEETING,
  CombinedTargetType.TASK_ANALYSIS,
  CombinedTargetType.SOFTWARE_VERSION
];
</script>

<template>
  <div class="bg-white rounded px-5" style="height: 520px">
    <Tabs size="small" class="h-full">
      <!-- All Activities Tab: Shows activities for all users in the project -->
      <TabPane key="total" :tab="t('taskHome.allActivities')">
        <ActivityTimeline
          :types="activityResourceTypes as any"
          :projectId="props.projectId" />
      </TabPane>

      <!-- My Activities Tab: Shows activities for the current user only -->
      <TabPane key="my" :tab="t('taskHome.myActivities')">
        <ActivityTimeline
          :types="activityResourceTypes as any"
          :userId="props.userInfo?.id"
          :projectId="props.projectId"
          :showUserName="false" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
/* Tab styling for small size tabs */
.ant-tabs-small > :deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 14px;
}

/* Main tabs container with responsive height */
.ant-tabs {
  height: 100%;
}
</style>
