<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Icon } from '@xcan-angus/vue-ui';
import { ScriptType } from '@xcan-angus/infra';

// Define component props
interface Props {
  value: {[key:string]:any};
  largePageLayout: boolean;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  value: () => ({}),
  largePageLayout: true
});

// Import result components asynchronously
const PerfResult = defineAsyncComponent(() => import('./PerfResult.vue'));
const StabilityResult = defineAsyncComponent(() => import('./StabilityResult.vue'));

// Configuration for target summary display
const configInfo = [
  [{ label: t('execution.testResult.total'), dataIndex: 'totalNum' },
    { label: t('execution.testResult.success'), dataIndex: 'successNum' },
    { label: t('execution.testResult.failure'), dataIndex: 'failNum' },
    { label: t('execution.testResult.notEnabled'), dataIndex: 'disabledNum' }
  ]
];

// Computed properties for data source
const dataSource = computed(() => {
  return props.value;
});

// Computed properties for basic scenario information
const scriptSourceId = computed(() => dataSource.value?.scriptSourceId);
const scriptSourceName = computed(() => dataSource.value?.scriptSourceName);
const passed = computed(() => dataSource.value?.passed);
const failureMessage = computed(() => dataSource.value?.failureMessage);
const testNum = computed(() => +(dataSource.value?.testNum || 0));
const testFailureNum = computed(() => +(dataSource.value?.testFailureNum || 0));

// Compute one-time pass status text
const onePassText = computed(() => {
  if (testNum.value <= 0) {
    return '--';
  }

  return testFailureNum.value === 0 ? t('execution.testResult.yes') : t('execution.testResult.no');
});
</script>

<template>
  <div class="text-3 leading-5">
    <!-- Basic information section -->
    <div class="font-semibold mb-2">{{ t('execution.testResult.basicInfo') }}</div>

    <!-- Layout for smaller screens -->
    <div v-if="!props.largePageLayout" class="space-y-2.5">
      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.scenarioId') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceId }}</div>
        </div>

        <div class="relative w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.scenarioName') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceName }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.testResult') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="flex items-center">
            <template v-if="passed">
              <Icon icon="icon-duihao" class="mr-1 text-status-success" />
              <span>{{ t('execution.testResult.passed') }}</span>
            </template>
            <template v-else>
              <Icon icon="icon-chahao" class="mr-1 text-status-error" />
              <span>{{ t('execution.testResult.notPassed') }}</span>
            </template>
          </div>
        </div>

        <div class="relative w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.failureReason') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ failureMessage }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.testCount') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ testNum }}</div>
        </div>

        <div class="relative w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.failureCount') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ testFailureNum }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.oneTimePass') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ onePassText }}</div>
        </div>
      </div>
    </div>

    <!-- Layout for larger screens -->
    <div v-else-if="props.largePageLayout" class="space-y-2.5">
      <div class="flex items-start space-x-5">
        <div class="relative w-1/3 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.scenarioId') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceId }}</div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.testResult') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="flex items-center">
            <template v-if="passed">
              <Icon icon="icon-duihao" class="mr-1 text-status-success" />
              <span>{{ t('execution.testResult.passed') }}</span>
            </template>
            <template v-else>
              <Icon icon="icon-chahao" class="mr-1 text-status-error" />
              <span>{{ t('execution.testResult.notPassed') }}</span>
            </template>
          </div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.testCount') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ testNum }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/3 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.scenarioName') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceName }}</div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.failureReason') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ failureMessage }}</div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.failureCount') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ testFailureNum }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/3 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('execution.testResult.oneTimePass') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="whitespace-pre-wrap break-words break-all">{{ onePassText }}</div>
        </div>
      </div>
    </div>

    <!-- Test scenario section -->
    <div class="mt-5 mb-2 font-semibold">
      {{ t('execution.testResult.testScenario') }}
    </div>

    <div class="space-y-2">
      <li
        v-for="(line, idx) in configInfo"
        :key="idx"
        class="flex space-x-2">
        <div
          v-for="item in line"
          :key="item.dataIndex"
          class="flex h-7 leading-7  w-30">
          <span v-if="item.label" class="flex-1 text-white bg-blue-6 px-2 rounded">{{ item.label }}</span>
          <span v-if="item.dataIndex" class="flex-1 bg-gray-light px-2 rounded-r">{{
            dataSource.targetSummary?.[item.dataIndex] }}</span>
        </div>
      </li>
    </div>

    <!-- Performance test results section -->
    <template v-if="dataSource.scriptType?.value === ScriptType.TEST_PERFORMANCE">
      <div class="font-semibold mt-5 mb-2">{{ t('execution.testResult.resultInfo') }}</div>
      <PerfResult :indicatorPerf="dataSource.indicatorPerf" :result="dataSource.sampleSummary" />
    </template>

    <!-- Stability test results section -->
    <template v-if="dataSource.scriptType?.value === ScriptType.TEST_STABILITY">
      <div class="font-semibold mt-5 mb-2">{{ t('execution.testResult.resultInfo') }}</div>
      <StabilityResult :indicatorStability="dataSource.indicatorStability" :result="dataSource.sampleSummary" />
    </template>
  </div>
</template>

<style scoped>
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-1\/3 {
  width: calc((100% - 40px)/3);
}
</style>
