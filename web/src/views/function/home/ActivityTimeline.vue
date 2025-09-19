<script lang="ts" setup>
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { CombinedTargetType } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined
});

const types = [
  CombinedTargetType.FUNC_CASE,
  CombinedTargetType.FUNC_PLAN,
  CombinedTargetType.FUNC_REVIEW,
  CombinedTargetType.FUNC_CASE_BASELINE,
  CombinedTargetType.FUNC_CASE_ANALYSIS
];
</script>
<template>
  <Tabs size="small">
    <TabPane key="all" :tab="t('functionHome.activityTimeline.allActivity')">
      <ActivityTimeline
        key="all"
        :types="types"
        :projectId="props.projectId" />
    </TabPane>

    <TabPane key="my" :tab="t('functionHome.activityTimeline.myActivity')">
      <ActivityTimeline
        key="my"
        :types="types"
        :projectId="props.projectId"
        :userId="props.userInfo?.id"
        :showUserName="false" />
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
