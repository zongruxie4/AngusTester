<script lang="ts" setup>
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

type Props = {
  projectId: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined
});
const { t } = useI18n();
const types = ['SERVICE', 'API', 'API_CASE'];
</script>
<template>
  <Tabs size="small">
    <TabPane key="all" :tab="t('common.all')">
      <ActivityTimeline
        key="all"
        :types="types"
        :projectId="props.projectId" />
    </TabPane>

    <TabPane key="my" :tab="t('apis.activity.my')">
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
  height: calc(100% - 245px);
  min-height: 375px;
}
</style>
