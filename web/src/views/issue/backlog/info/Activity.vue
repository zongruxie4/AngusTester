<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ActivityInfo as Activity, Scroll } from '@xcan-angus/vue-ui';
import { TESTER, SearchCriteria } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';

import { ActivityInfo } from '@/types/types';
import { AssocCaseProps } from '@/views/issue/issue/list/types';

// Component Props
const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// Reactive State Variables
const activityList = ref<ActivityInfo[]>([]);
const activityQueryParams = ref<{
  mainTargetId: string;
  filters: [{ key: 'targetType', value: CombinedTargetType.TASK, op: SearchCriteria.OpEnum }]
}>();

// Computed Properties
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

/**
 * <p>Handle activity data change</p>
 * <p>Updates the activity list when new data is received from the Scroll component</p>
 */
const handleActivityDataChange = (data: ActivityInfo[]) => {
  activityList.value = data;
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => currentTaskId.value, (newTaskId, oldTaskId) => {
    if (newTaskId === oldTaskId) {
      return;
    }

    activityQueryParams.value = {
      mainTargetId: currentTaskId.value,
      filters: [{ key: 'targetType', value: CombinedTargetType.TASK, op: SearchCriteria.OpEnum.Equal }]
    };
  }, { immediate: true });
});
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.activity') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <Scroll
          :action="`${TESTER}/activity`"
          :hideNoData="!!activityList.length"
          :params="activityQueryParams"
          :lineHeight="32"
          transition
          style="height:calc(100% - 30px);"
          @change="handleActivityDataChange">
          <Activity
            :dataSource="activityList"
            infoKey="description"
            class="pr-5" />
        </Scroll>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
