<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { NoData, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '../../../../../../types';
import { TestInfo } from './PropsType';

const ApiTestResult = defineAsyncComponent(() => import('@/views/execution/detail/result/ApiResult.vue'));
const ScenarioResult = defineAsyncComponent(() => import('@/views/execution/detail/result/ScenarioResult.vue'));

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

const { t } = useI18n();

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
      <div class="flex items-center text-3">{{ t('task.testing.testResult.title') }}</div>
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
