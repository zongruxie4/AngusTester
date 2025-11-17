<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { TaskType } from '@/enums/enums';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

import { TaskDetail } from '@/views/issue/types';

// Component props and emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  largePageLayout: undefined,
  loading: false
});

const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
}>();

// Async component imports
const APIBasicInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Apis.vue'));
const ScenarioBasicInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Scenario.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Basic.vue'));
const WorkloadInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Workload.vue'));
const ProcessTimesInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/ProcessTimes.vue'));
const Description = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Description.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Date.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/Attachment.vue'));

// Event handlers
/**
 * <p>Handles task data change events from child components.</p>
 * <p>Forwards the change event to parent components.</p>
 * @param data - Partial task information that has been changed
 */
const handleTaskDataChange = (data: Partial<TaskDetail>) => {
  emit('change', data);
};

/**
 * <p>Handles loading state change events from child components.</p>
 * <p>Forwards the loading state change to parent components.</p>
 * @param value - New loading state value
 */
const handleLoadingStateChange = (value: boolean) => {
  emit('update:loading', value);
};

// Computed properties
/**
 * <p>Gets the current task ID from the data source.</p>
 * @returns The task ID or undefined if not available
 */
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

/**
 * <p>Gets the current task type from the data source.</p>
 * @returns The task type value or undefined if not available
 */
const currentTaskType = computed(() => {
  return props.dataSource?.taskType?.value;
});

/**
 * <p>Determines the CSS class name based on the page layout configuration.</p>
 * <p>Returns 'large-page-layout' for large pages, 'small-page-layout' for small pages.</p>
 * @returns CSS class name for the layout
 */
const layoutClassName = computed(() => {
  if (props.largePageLayout) {
    return 'large-page-layout';
  }

  if (!props.largePageLayout) {
    return 'small-page-layout';
  }
  return '';
});
</script>

<template>
  <div :class="layoutClassName" class="h-full pr-5 overflow-auto">
    <div class="flex-1 space-y-6">
      <!-- Main Task Info Section -->
      <div class="space-y-4">

        <BasicInfo

          :id="currentTaskId"
          :dataSource="props.dataSource"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :taskId="currentTaskId"
          @change="handleTaskDataChange"
          @loadingChange="handleLoadingStateChange" />
      </div>

      <!-- Metrics Section - Workload and Process Times -->
      <div class="metrics-section">
        <WorkloadInfo
          :id="currentTaskId"
          :dataSource="props.dataSource"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :taskId="currentTaskId"
          @change="handleTaskDataChange"
          @loadingChange="handleLoadingStateChange" />

        <ProcessTimesInfo
          :id="currentTaskId"
          :dataSource="props.dataSource"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :taskId="currentTaskId"
          @change="handleTaskDataChange"
          @loadingChange="handleLoadingStateChange" />
      </div>

      <!-- Additional Info Section -->
      <div class="space-y-4">
        <Description
          :id="currentTaskId"
          :dataSource="props.dataSource"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :taskId="currentTaskId"
          @change="handleTaskDataChange"
          @loadingChange="handleLoadingStateChange" />
      </div>
    </div>

    <div class="flex-shrink-0 w-75 space-y-4">
      <PersonnelInfo
        :id="currentTaskId"
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />

      <DateInfo
        :id="currentTaskId"
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />

      <AttachmentInfo
        :id="currentTaskId"
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />
    </div>
  </div>
</template>

<style scoped>
/* Main Layout */
.large-page-layout {
  display: flex;
  align-items: flex-start;
}

.large-page-layout>div+div {
  margin-left: 20px;
}

.small-page-layout>div+div {
  margin-top: 16px;
}

/* Metrics Section Styling */
.metrics-section {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: 1fr 1fr;
}

@media (max-width: 1024px) {
  .metrics-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metrics-section {
    gap: 0.5rem;
  }
}

/* Enhanced spacing and visual hierarchy */
.space-y-6 > * + * {
  margin-top: 1.5rem;
}

.space-y-4 > * + * {
  margin-top: 1rem;
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .h-full {
    padding-right: 0.75rem;
  }

  .space-y-6 > * + * {
    margin-top: 1rem;
  }
}
</style>
