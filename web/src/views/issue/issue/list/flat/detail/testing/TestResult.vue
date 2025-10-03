<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { NoData, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TaskType } from '@/enums/enums';

import { TaskDetail } from '@/views/issue/types';
import { TestInfo } from '@/views/execution/types';

const ApiTestResult = defineAsyncComponent(() => import('@/views/execution/detail/result/ApiResult.vue'));
const ScenarioResult = defineAsyncComponent(() => import('@/views/execution/detail/result/ScenarioResult.vue'));

// Component Props
type Props = {
  dataSource: TaskDetail;
  testInfo: TestInfo;
  largePageLayout: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined,
  testInfo: undefined,
  largePageLayout: undefined
});

// Composables
const { t } = useI18n();

/**
 * Stores the task description content
 */
const taskDescription = ref<string>('');

// Lifecycle Hooks
onMounted(() => {
  /**
   * Watches for changes in dataSource and updates task description
   * <p>
   * Updates the description content when task data changes
   */
  watch(() => props.dataSource, (newTaskData) => {
    taskDescription.value = newTaskData?.description || '';
  }, { immediate: true });
});
</script>

<template>
  <Toggle>
    <template #title>
      <div class="flex items-center text-3">{{ t('common.testResult') }}</div>
    </template>

    <template #default>
      <!-- API Test Result Display -->
      <template v-if="props.dataSource.taskType.value === TaskType.API_TEST && !!props.testInfo">
        <ApiTestResult
          :value="props.testInfo"
          :largePageLayout="props.largePageLayout"
          class="text-3 pl-5.5 mt-3.5" />
      </template>

      <!-- Scenario Test Result Display -->
      <template v-if="props.dataSource.taskType.value === TaskType.SCENARIO_TEST && !!props.testInfo">
        <ScenarioResult
          :value="props.testInfo"
          :largePageLayout="props.largePageLayout"
          class="text-3 pl-5.5 mt-3.5" />
      </template>

      <!-- No Data State -->
      <template v-if="!props.testInfo">
        <NoData size="small" class="my-8" />
      </template>
    </template>
  </Toggle>
</template>
