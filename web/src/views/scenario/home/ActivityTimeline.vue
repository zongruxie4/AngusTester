<script setup lang="ts">
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { CombinedTargetType } from '@xcan-angus/infra';

// ===== COMPOSABLES =====
const { t } = useI18n();

// ===== TYPES =====
interface ComponentProps {
  projectId: string;
  userInfo: { id: string; };
}

// ===== PROPS =====
const props = withDefaults(defineProps<ComponentProps>(), {
  projectId: undefined,
  userInfo: undefined
});

// ===== CONSTANTS =====
// Activity types to filter in timeline
const ACTIVITY_TYPES = [CombinedTargetType.SCENARIO, CombinedTargetType.SCENARIO_MONITOR];
</script>

<template>
  <div class="bg-white rounded px-5" style="height: 340px">
    <!-- Activity timeline with two tabs: personal and all activities -->
    <Tabs size="small">
      <!-- All activities tab - shows all users' activities in the project -->
      <TabPane key="total" :tab="t('scenarioHome.activityTimeline.allActivity')">
        <ActivityTimeline
          :types="ACTIVITY_TYPES"
          :projectId="props.projectId" />
      </TabPane>

      <!-- Personal activity tab - shows only current user's activities -->
      <TabPane key="my" :tab="t('scenarioHome.activityTimeline.myActivity')">
        <ActivityTimeline
          :types="ACTIVITY_TYPES"
          :userId="props.userInfo?.id"
          :projectId="props.projectId"
          :showUserName="false" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
/* Small tab font size for better visual hierarchy */
.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 14px;
}

/* Responsive height calculation with minimum height constraint */
.ant-tabs {
  height: calc(100% - 240px);
  min-height: 390px;
}
</style>
