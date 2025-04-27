<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { NoData, Toggle } from '@xcan-angus/vue-ui';

import { TaskInfo } from '../../../../../../../PropsType';
import { TestInfo } from '../PropsType';

const ApiTestResult = defineAsyncComponent(() => import('@/views/execution/info/testResult/apiResult.vue'));
const ScenarioResult = defineAsyncComponent(() => import('@/views/execution/info/testResult/scenarioResult.vue'));

type Props = {
  dataSource: TaskInfo;
  testInfo: TestInfo;
  largePageLayout: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined,
  testInfo: undefined,
  largePageLayout: undefined
});

const content = ref<string>('');

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    content.value = newValue?.description || '';
  }, { immediate: true });
});

</script>

<template>
  <Toggle>
    <template #title>
      <div class="flex items-center text-3">测试结果</div>
    </template>

    <template #default>
      <template v-if="props.dataSource.taskType.value === 'API_TEST' && !!props.testInfo">
        <ApiTestResult
          :value="props.testInfo"
          :largePageLayout="props.largePageLayout"
          class="text-3 pl-5.5 mt-3.5" />
      </template>
      <template v-if="props.dataSource.taskType.value === 'SCENARIO_TEST' && !!props.testInfo">
        <ScenarioResult
          :value="props.testInfo"
          :largePageLayout="props.largePageLayout"
          class="text-3 pl-5.5 mt-3.5" />
      </template>
      <template v-if="!props.testInfo">
        <NoData size="small" class="my-8" />
      </template>
    </template>
  </Toggle>
</template>
