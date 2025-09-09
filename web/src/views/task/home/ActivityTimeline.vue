<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';

type Props = {
  projectId: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined
});

const { t } = useI18n();

const types = ['TASK', 'TASK_SPRINT', 'MEETING', 'TASK_ANALYSIS', 'SOFTWARE_VERSION'];
</script>

<template>
  <Tabs size="small">
    <TabPane key="my" :tab="t('taskHome.myActivities')">
      <ActivityTimeline
        :types="types"
        :userId="props.userInfo?.id"
        :projectId="props.projectId"
        :showUserName="false" />
    </TabPane>
    <TabPane key="total" :tab="t('taskHome.allActivities')">
      <ActivityTimeline :types="types" :projectId="props.projectId" />
    </TabPane>
  </Tabs>
</template>

<style scoped>
.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 14px;
}

.ant-tabs {
  height: calc(100% - 265px);
  min-height: 375px;
}
</style>
