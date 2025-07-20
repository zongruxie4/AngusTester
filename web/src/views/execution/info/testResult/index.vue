<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { exec } from 'src/api/ctrl';

// import { NoData, Icon } from '@xcan-angus/vue-ui';

interface Props {
  execInfo: Record<string, any>; // 执行详情
  execId: string
}

const props = withDefaults(defineProps<Props>(), {
  execInfo: () => ({}),
  execId: ''
});
const ApiTestResult = defineAsyncComponent(() => import('./apiResult.vue'));
const ScenarioResult = defineAsyncComponent(() => import('./scenarioResult.vue'));

const testResult = ref({});
const loadApiTestResult = async () => {
  const { scriptSourceId, scriptType } = props.execInfo;
  const [error, { data = {} }] = await exec.getApiTestResultByType(scriptSourceId, scriptType.value);
  if (error) {
    return;
  }
  testResult.value = data;
};

const loadScenarioTestResult = async () => {
  const { scriptSourceId, scriptType } = props.execInfo;
  const [error, { data = {} }] = await exec.getScenarioTestResult(scriptSourceId, scriptType.value);
  if (error) {
    return;
  }
  testResult.value = data;
};

// const loadServiceTestResult = async () => {
//   const { scriptSourceId } = props.execInfo;
//   const [error, {data = {}}] = await exec.loadServiceTestResult(scriptSourceId);
//   if (error) {
//     return
//   }
//   testResult.value = data;
// }

onMounted(() => {
  const { scriptSource } = props.execInfo;
  if (scriptSource?.value === 'API') {
    loadApiTestResult();
    return;
  }
  if (scriptSource?.value === 'SCENARIO') {
    loadScenarioTestResult();
  }
});
</script>
<template>
  <div>
    <template v-if="props.execInfo?.scriptSource?.value === 'API'">
      <ApiTestResult :value="testResult" />
    </template>
    <template v-if="props.execInfo?.scriptSource?.value === 'SCENARIO'">
      <ScenarioResult :value="testResult" />
    </template>
  </div>
</template>
