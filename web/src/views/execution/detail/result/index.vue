<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { exec } from 'src/api/ctrl';

// Define component props
interface Props {
  execInfo: Record<string, any>; // Execution details
  execId: string
}

const props = withDefaults(defineProps<Props>(), {
  execInfo: () => ({}),
  execId: ''
});

// Import result components asynchronously
const ApiTestResult = defineAsyncComponent(() => import('./ApiResult.vue'));
const ScenarioResult = defineAsyncComponent(() => import('./ScenarioResult.vue'));

// Test result data
const testResult = ref({});

// Load API test results
const loadApiTestResult = async () => {
  const { scriptSourceId, scriptType } = props.execInfo;
  const [error, { data = {} }] = await exec.getApiTestResultByType(scriptSourceId, scriptType.value);
  if (error) {
    return;
  }
  testResult.value = data;
};

// Load scenario test results
const loadScenarioTestResult = async () => {
  const { scriptSourceId, scriptType } = props.execInfo;
  const [error, { data = {} }] = await exec.getScenarioTestResult(scriptSourceId, scriptType.value);
  if (error) {
    return;
  }
  testResult.value = data;
};

// Load test results on component mount
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
    <!-- Display API test results -->
    <template v-if="props.execInfo?.scriptSource?.value === 'API'">
      <ApiTestResult :value="testResult" />
    </template>

    <!-- Display scenario test results -->
    <template v-if="props.execInfo?.scriptSource?.value === 'SCENARIO'">
      <ScenarioResult :value="testResult" />
    </template>
  </div>
</template>
