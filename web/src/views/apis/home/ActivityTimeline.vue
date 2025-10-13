<script lang="ts" setup>
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { CombinedTargetType } from '@/enums/enums';

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined
});

const { t } = useI18n();
const activityTypes = [
  CombinedTargetType.SERVICE,
  CombinedTargetType.API,
  CombinedTargetType.API_DESIGN,
  CombinedTargetType.API_CASE
];
</script>
<template>
  <Tabs size="small">
    <TabPane key="all" :tab="t('common.all')">
      <ActivityTimeline
        key="all"
        :types="activityTypes"
        :projectId="props.projectId" />
    </TabPane>

    <TabPane key="my" :tab="t('apis.activity.my')">
      <ActivityTimeline
        key="my"
        :types="activityTypes"
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
  height: calc(100% - 245px);
  min-height: 375px;
}
</style>
