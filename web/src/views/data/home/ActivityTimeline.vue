<script lang="ts" setup>
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ActivityTimelineProps, ActivityType } from './types';

const { t } = useI18n();

/**
 * <p>Component props definition</p>
 * <p>Defines the interface for component properties</p>
 */
const props = withDefaults(defineProps<ActivityTimelineProps>(), {
  projectId: undefined,
  userInfo: undefined
});

/**
 * <p>Activity types to display in timeline</p>
 * <p>Currently supports variable activities</p>
 */
const activityTypes: ActivityType[] = ['VARIABLE'];
</script>

<template>
  <div class="bg-white rounded px-5" style="height: 340px">
    <Tabs size="small" class="h-full">
      <!-- My Activity Tab -->
      <TabPane key="my" :tab="t('dataHome.activityTimeline.myActivity')">
        <ActivityTimeline
          key="my"
          :types="activityTypes"
          :projectId="props.projectId"
          :userId="props.userInfo?.id"
          :showUserName="false" />
      </TabPane>

      <!-- All Activity Tab -->
      <TabPane key="all" :tab="t('dataHome.activityTimeline.allActivity')">
        <ActivityTimeline
          key="all"
          :types="activityTypes"
          :projectId="props.projectId" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 12px;
}

.ant-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ant-tabs>:deep(.ant-tabs-content-holder) {
  flex: 1;
  overflow: hidden;
}

.ant-tabs>:deep(.ant-tabs-tabpane) {
  height: 100%;
  overflow: auto;
}
</style>
