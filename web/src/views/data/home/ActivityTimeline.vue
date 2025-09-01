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
  <div class="bg-white rounded border border-border-divider p-5">
    <Tabs size="small">
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
  font-size: 14px;
}

.ant-tabs {
  height: calc(100% - 245px);
  min-height: 375px;
}
</style>
