<script setup lang="ts">
// ===== IMPORTS =====
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { ActivityTimeline } from '@xcan-angus/vue-ui';

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
const ACTIVITY_TYPES = ['SCENARIO', 'SCENARIO_MONITOR'] as const;
</script>

<template>
  <!-- Activity timeline with two tabs: personal and all activities -->
  <Tabs size="small">
    <!-- Personal activity tab - shows only current user's activities -->
    <TabPane key="my" :tab="t('scenarioHome.activityTimeline.myActivity')">
      <ActivityTimeline
        :types="ACTIVITY_TYPES"
        :userId="props.userInfo?.id"
        :projectId="props.projectId"
        :showUserName="false" />
    </TabPane>

    <!-- All activities tab - shows all users' activities in the project -->
    <TabPane key="total" :tab="t('scenarioHome.activityTimeline.allActivity')">
      <ActivityTimeline
        :types="ACTIVITY_TYPES"
        :projectId="props.projectId" />
    </TabPane>
  </Tabs>
</template>

<style scoped>
/* ===== TAB STYLING ===== */
/* Small tab font size for better visual hierarchy */
.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 14px;
}

/* ===== LAYOUT STYLING ===== */
/* Responsive height calculation with minimum height constraint */
.ant-tabs {
  height: calc(100% - 240px);
  min-height: 375px;
}
</style>
