<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Spin } from '@xcan-angus/vue-ui';
import { issue } from '@/api/tester';

import { TaskDetail } from '@/views/issue/types';
import { TestInfo } from '@/views/execution/types';

// Component Props
type Props = {
  projectId: number;
  userInfo: { id: number; };
  appInfo: { id: number; };
  dataSource: TaskDetail;
  largePageLayout: boolean;
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  largePageLayout: undefined,
  loading: false
});

const ExecutionResult = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/testing/ExecResult.vue'));
const TestResult = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/testing/TestResult.vue'));

/**
 * Loading state for data fetching operations
 */
const isLoadingData = ref(false);

/**
 * Stores the test information data
 */
const testResultData = ref<TestInfo>();

/**
 * Loads task result data from the API
 * <p>
 * Fetches test results based on task type, target ID, and test type
 * <p>
 * Updates loading state and stores the result data
 */
const loadTaskResultData = async () => {
  const { taskType, testType, targetId } = props.dataSource;
  isLoadingData.value = true;

  const [error, response] = await issue.getTaskResult(taskType?.value, targetId, testType?.value);
  isLoadingData.value = false;

  if (error) {
    return;
  }

  testResultData.value = response?.data;
};

// Lifecycle Hooks
onMounted(() => {
  /**
   * Watches for changes in dataSource and loads task result data
   * <p>
   * Triggers data loading when task data changes
   */
  watch(() => props.dataSource, (newTaskData) => {
    if (!newTaskData) {
      return;
    }

    loadTaskResultData();
  }, { immediate: true });
});
</script>

<template>
  <!-- Main container with loading spinner -->
  <Spin :spinning="isLoadingData" class="h-full pr-5 overflow-auto">
    <!-- Execution Result Component -->
    <ExecutionResult
      class="mb-4"
      :dataSource="props.dataSource"
      :largePageLayout="props.largePageLayout" />

    <!-- Test Result Component -->
    <TestResult
      :dataSource="props.dataSource"
      :testInfo="testResultData"
      :largePageLayout="props.largePageLayout" />
  </Spin>
</template>
