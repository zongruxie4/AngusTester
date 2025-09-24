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
  <div class="bg-white rounded px-5" style="height: 520px">
    <Tabs size="small">
      <TabPane key="all" :tab="t('home.allActivity')">
        <ActivityTimeline
          key="all"
          :types="types"
          :projectId="props.projectId" />
      </TabPane>

      <TabPane key="my" :tab="t('home.myActivity')">
        <ActivityTimeline
          key="my"
          :types="types"
          :projectId="props.projectId"
          :userId="props.userInfo?.id"
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
