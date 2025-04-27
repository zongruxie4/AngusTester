<script setup lang="ts">
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

const types = ['SCENARIO', 'SCENARIO_MONITOR'];
</script>

<template>
  <Tabs size="small">
    <TabPane key="my" tab="我的活动">
      <ActivityTimeline
        :types="types"
        :userId="props.userInfo?.id"
        :projectId="props.projectId"
        :showUserName="false" />
    </TabPane>
    <TabPane key="total" tab="所有活动">
      <ActivityTimeline :types="types" :projectId="props.projectId" />
    </TabPane>
  </Tabs>
</template>

<style scoped>
.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 14px;
}

.ant-tabs {
  height: calc(100% - 240px);
  min-height: 375px;
}
</style>
