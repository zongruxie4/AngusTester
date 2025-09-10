<script lang="ts" setup>
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined
});

const types = ['FUNC_CASE', 'FUNC_PLAN', 'FUNC_REVIEW', 'FUNC_CASE_BASELINE', 'FUNC_CASE_ANALYSIS'];
</script>
<template>
  <Tabs size="small">
    <TabPane key="my" :tab="t('functionHome.activityTimeline.myActivity')">
      <ActivityTimeline
        key="my"
        :types="types"
        :projectId="props.projectId"
        :userId="props.userInfo?.id"
        :showUserName="false" />
    </TabPane>
    <TabPane key="all" :tab="t('functionHome.activityTimeline.allActivity')">
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
  height: calc(100% - 280px);
  min-height: 375px;
}
</style>
