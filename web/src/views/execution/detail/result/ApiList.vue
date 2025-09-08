<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Arrow, HttpMethodTag } from '@xcan-angus/vue-ui';
import { Badge } from 'ant-design-vue';

// Define component props
interface Props {
  dataSource: { apisName: string; method: string; uri: string, passed: boolean; apisId: string, open: boolean }[];
  scriptType: 'TEST_PERFORMANCE'|'TEST_STABILITY'
}

// Import result components asynchronously
const PerfResult = defineAsyncComponent(() => import('./PerfResult.vue'));
const StabilityResult = defineAsyncComponent(() => import('./StabilityResult.vue'));

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([
    { apisName: 'string', method: 'string', uri: 'string', passed: false, apisId: 'string', open: false }
  ])
});

// Get stability result data for an API
const getStabilityResult = (api) => {
  const useNodeId = api.usageFailedNodeId || Object.keys(api.nodeUsageSummary || {})[0];
  return {
    ...api.sampleSummary,
    ...(api.nodeUsageSummary?.[useNodeId] || {})
  };
};

// Track open/closed state of API details
const openMap = ref({});

// Toggle open/closed state of API details
const toggleOpen = (apisId) => {
  openMap.value[apisId] = !openMap.value[apisId];
};
</script>

<template>
  <div class="space-y-2">
    <div
      v-for="api in props.dataSource"
      :key="api.apisId">
      <!-- API summary row -->
      <div
        class="p-2 bg-bg-content flex items-center border rounded cursor-pointer"
        @click="toggleOpen(api.apisId)">
        <div class="w-100">{{ api.apisName }}（ID: {{ api.apisId }}）</div>
        <HttpMethodTag :value="api.method" />
        <div class="pl-5 flex-1">{{ api.uri }}</div>
        <div class="w-30 flex justify-between">
          <Badge :color="api.passed ? 'green' : 'red'" :text="api.passed ? t('execution.testResult.passed') : t('execution.testResult.notPassed')" />
          <Arrow v-model:open="openMap[api.apisId]" />
        </div>
      </div>

      <!-- Performance test results -->
      <PerfResult
        v-if="props.scriptType === 'TEST_PERFORMANCE'"
        v-show="openMap[api.apisId]"
        class="bg-white"
        :indicatorPerf="api.indicatorPerf"
        :result="api.sampleSummary"
        :passedInfo="api" />

      <!-- Stability test results -->
      <StabilityResult
        v-if="props.scriptType === 'TEST_STABILITY'"
        v-show="openMap[api.apisId]"
        class="bg-white"
        :indicatorStability="api.indicatorStability"
        :result="getStabilityResult(api)"
        :passedInfo="api" />
    </div>
  </div>
</template>
