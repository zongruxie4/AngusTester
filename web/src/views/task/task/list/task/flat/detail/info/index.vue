<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { TaskType } from '@/enums/enums';

import { TaskInfo } from '@/views/task/types';

// Component props type definition
type Props = {
  projectId: string;
  userInfo: { id: string; fullName: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
  largePageLayout: boolean;
  loading: boolean;
}

// Component props and emits
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  largePageLayout: undefined,
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Async component imports
const APIBasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Apis.vue'));
const ScenarioBasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Scenario.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Basic.vue'));
const Description = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Description.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Date.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Attachment.vue'));

// Event handlers
/**
 * <p>Handles task data change events from child components.</p>
 * <p>Forwards the change event to parent components.</p>
 * @param data - Partial task information that has been changed
 */
const handleTaskDataChange = (data: Partial<TaskInfo>) => {
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
    <div class="flex-1 space-y-4">
      <APIBasicInfo
        v-if="currentTaskType === TaskType.API_TEST"
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />

      <ScenarioBasicInfo
        v-else-if="currentTaskType === TaskType.SCENARIO_TEST"
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />

      <BasicInfo
        v-else
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />

      <Description
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />
    </div>

    <div class="flex-shrink-0 w-75 space-y-4">
      <PersonnelInfo
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />

      <DateInfo
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :appInfo="props.appInfo"
        :taskId="currentTaskId"
        @change="handleTaskDataChange"
        @loadingChange="handleLoadingStateChange" />

      <AttachmentInfo
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
</style>
