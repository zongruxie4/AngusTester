<script lang="ts" setup>
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

const types = ['VARIABLE'];
</script>
<template>
  <Tabs size="small">
    <TabPane key="my" tab="我的活动">
      <ActivityTimeline
        key="my"
        :types="types"
        :projectId="props.projectId"
        :userId="props.userInfo?.id"
        :showUserName="false" />
    </TabPane>
    <TabPane key="all" tab="所有活动">
      <ActivityTimeline
        key="all"
        :types="types"
        :projectId="props.projectId" />
    </TabPane>
  </Tabs>
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
