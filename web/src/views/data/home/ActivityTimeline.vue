<script lang="ts" setup>
import { TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ref, onMounted, onUnmounted } from 'vue';
import { ActivityTimelineProps, ActivityType } from './types';

const { t } = useI18n();

/**
 * <p>Component props definition</p>
 * <p>Defines the interface for component properties</p>
 */
const props = withDefaults(defineProps<ActivityTimelineProps>(), {
  projectId: undefined,
  userInfo: undefined
});

/**
 * <p>Activity types to display in timeline</p>
 * <p>Currently supports variable activities</p>
 */
const activityTypes: ActivityType[] = ['VARIABLE'];

/**
 * <p>Dynamic height calculation for activity timeline</p>
 * <p>Automatically calculates remaining screen height</p>
 */
const timelineHeight = ref('calc(100vh - 400px)');

/**
 * <p>Calculate dynamic height based on screen size and other elements</p>
 * <p>Updates height when window resizes</p>
 */
const calculateHeight = () => {
  const viewportHeight = window.innerHeight;
  const headerHeight = 64; // Estimated header height
  const padding = 40; // Page padding
  const otherElementsHeight = 330; // Estimated height of other elements (statistics cards, charts, etc.)

  const availableHeight = viewportHeight - headerHeight - padding - otherElementsHeight;
  const finalHeight = Math.max(availableHeight, 350); // Minimum height 350px

  timelineHeight.value = `${finalHeight}px`;
};

/**
 * <p>Handle window resize for responsive height</p>
 */
const handleResize = () => {
  calculateHeight();
};

// Calculate height and add listeners when component mounts
onMounted(() => {
  calculateHeight();
  window.addEventListener('resize', handleResize);
});

// Remove listeners when component unmounts
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<template>
  <div class="bg-white rounded border border-border-divider pt-2.5 pb-5 px-5" :style="{ height: timelineHeight }">
    <Tabs size="small" class="h-full">
      <!-- My Activity Tab -->
      <TabPane key="my" :tab="t('dataHome.activityTimeline.myActivity')">
        <ActivityTimeline
          key="my"
          :types="activityTypes"
          :projectId="props.projectId"
          :userId="props.userInfo?.id"
          :showUserName="false" />
      </TabPane>

      <!-- All Activity Tab -->
      <TabPane key="all" :tab="t('dataHome.activityTimeline.allActivity')">
        <ActivityTimeline
          key="all"
          :types="activityTypes"
          :projectId="props.projectId" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 14px;
}

.ant-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ant-tabs>:deep(.ant-tabs-content-holder) {
  flex: 1;
  overflow: hidden;
}

.ant-tabs>:deep(.ant-tabs-tabpane) {
  height: 100%;
  overflow: auto;
}
</style>
