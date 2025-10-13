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
  <div class="bg-white rounded px-5" style="height: 520px">
    <Tabs size="small" class="h-full">
      <TabPane key="total" :tab="t('home.allActivity')">
        <ActivityTimeline
          key="all"
          :types="activityTypes"
          :projectId="props.projectId" />
      </TabPane>

      <TabPane key="my" :tab="t('home.myActivity')">
        <ActivityTimeline
          key="my"
          :types="activityTypes"
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
