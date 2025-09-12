<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Spin } from '@xcan-angus/vue-ui';
import { task } from '@/api/tester';

import { TaskInfo } from '@/views/task/types';
import { TestInfo } from '@/views/execution/types';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
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

const ExecutionResult = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/testing/ExecResult.vue'));
const TestResult = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/testing/TestResult.vue'));

const loading = ref(false);
const testInfo = ref<TestInfo>();

const loadData = async () => {
  const { taskType, testType, targetId } = props.dataSource;
  loading.value = true;
  const [error, res] = await task.getTaskResult(taskType?.value, targetId, testType?.value);
  loading.value = false;
  if (error) {
    return;
  }

  testInfo.value = res?.data;
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="h-full pr-5 overflow-auto">
    <ExecutionResult
      class="mb-4"
      :dataSource="props.dataSource"
      :largePageLayout="props.largePageLayout" />
    <TestResult
      :dataSource="props.dataSource"
      :testInfo="testInfo"
      :largePageLayout="props.largePageLayout" />
  </Spin>
</template>
