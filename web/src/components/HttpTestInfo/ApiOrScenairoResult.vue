<script lang="ts" setup>
// Vue core imports
import { watch, onMounted, ref, defineAsyncComponent, inject, Ref } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Tabs, TabPane } from 'ant-design-vue';

const { t } = useI18n();

/**
 * Component props interface for API or scenario test results
 */
interface Props {
  type: 'API' | 'SCENARIO';
  testType: 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
  dataSource: Record<string, any>;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  type: 'API',
  testType: 'TEST_FUNCTIONALITY',
  dataSource: () => ({})
});

// Injected dependencies
const projectTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({
  showTask: true,
  showBackLog: true,
  showMeeting: true,
  showSprint: true,
  showTasStatistics: true
}));

// Async component definitions
const TestConfigInfo = defineAsyncComponent(() => import('./TestConfigInfo.vue'));
const ApiTestCase = defineAsyncComponent(() => import('./ApiTestCase.vue'));
const ScenarioTestSummary = defineAsyncComponent(() => import('./ScenarioTestSummary.vue'));
const ApiOrCaseList = defineAsyncComponent(() => import('./ApiOrCaseList.vue'));
const TaskList = defineAsyncComponent(() => import('./TaskList.vue'));
const PerfResult = defineAsyncComponent(() => import('./PerfResult.vue'));
const PerfIndicator = defineAsyncComponent(() => import('./PerfIndicator.vue'));
const StabilityResult = defineAsyncComponent(() => import('./StabilityResult.vue'));
const StabilityIndicator = defineAsyncComponent(() => import('./StabilityIndicator.vue'));

// Raw data from API
const executionResults = ref();
const associatedScripts = ref();
const associatedTasks = ref();

// Processed data for display
const basicTestInfo = ref(); // Basic test information
const testCaseSummaryData = ref(); // Test case or API test statistics
const apiOrCaseTestData = ref(); // API test cases
const associatedScriptData = ref(); // Associated scripts
const associatedTaskData = ref(); // Associated tasks
const performanceResultData = ref(); // Performance test results
const stabilityResultData = ref(); // Stability test results
const performanceIndicatorData = ref(); // Performance test indicators
const stabilityIndicatorData = ref(); // Stability test indicators

// Test configuration
const enabledTestTypes = ref<string[]>([]); // Enabled test types

/**
 * Reset all component data to initial state
 */
const resetAllComponentData = () => {
  executionResults.value = undefined;
  associatedScripts.value = undefined;
  associatedTasks.value = undefined;
  basicTestInfo.value = undefined;
  testCaseSummaryData.value = undefined;
  apiOrCaseTestData.value = undefined;
  associatedScriptData.value = undefined;
  associatedTaskData.value = undefined;
  performanceResultData.value = undefined;
  stabilityResultData.value = undefined;
  performanceIndicatorData.value = undefined;
  stabilityIndicatorData.value = undefined;
  enabledTestTypes.value = [];
};

/**
 * Set and process test information data based on test type and component type
 */
const processTestInformationData = () => {
  basicTestInfo.value = executionResults.value?.[props.testType] || {};
  if (!basicTestInfo.value) {
    return;
  }
  if (props.testType === 'TEST_FUNCTIONALITY' && props.type === 'API') {
    testCaseSummaryData.value = basicTestInfo.value.caseSummary;
    apiOrCaseTestData.value = basicTestInfo.value.caseResults;
  }
  if (props.type === 'SCENARIO') {
    testCaseSummaryData.value = basicTestInfo.value?.targetSummary || {};
  }
  associatedScriptData.value = associatedScripts.value?.[props.testType] || {};
  associatedTaskData.value = associatedTasks.value?.[props.testType];
  performanceResultData.value = basicTestInfo.value.sampleSummary;
  performanceIndicatorData.value = basicTestInfo.value.indicatorPerf;
  const selectedNodeId = basicTestInfo.value.usageFailedNodeId || Object.keys(basicTestInfo.value.nodeUsageSummary || {})[0];
  stabilityResultData.value = {
    ...basicTestInfo.value.sampleSummary,
    ...(basicTestInfo.value.nodeUsageSummary?.[selectedNodeId] || {})
  };
  stabilityIndicatorData.value = basicTestInfo.value.indicatorStability;
};

// Component lifecycle hooks
onMounted(() => {
  watch(() => props.dataSource, (newDataSource: Record<string, any>) => {
    resetAllComponentData();
    executionResults.value = newDataSource.testResult?.resultDetailVoMap || {};
    enabledTestTypes.value = (newDataSource.testResult?.enabledTestTypes || []).map((item: any) => item.value);
    associatedScripts.value = newDataSource.assocScripts || {};
    associatedTasks.value = newDataSource.assocTasks || {};
    processTestInformationData();
  }, {
    immediate: true
  });
});

</script>
<template>
  <!-- API test information -->
  <template v-if="props.type === 'API'">
    <div class="text-3 mt-3 mb-1 font-semibold">
      {{ t('common.basicInfo') }}
    </div>
    <TestConfigInfo :value="basicTestInfo" :enabled="enabledTestTypes.includes(props.testType)" />
    <template v-if="props.type === 'API' && props.testType === 'TEST_FUNCTIONALITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testCase') }}
      </div>
      <ApiTestCase :dataSource="testCaseSummaryData" />
    </template>
    <template v-if="props.testType === 'TEST_PERFORMANCE'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('common.testResult') }}
      </div>
      <PerfResult :indicatorPerf="performanceIndicatorData" :result="performanceResultData" />
    </template>
    <template v-if="props.testType === 'TEST_STABILITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('common.testResult') }}
      </div>
      <StabilityResult :indicatorStability="stabilityIndicatorData" :result="stabilityResultData" />
    </template>
    <Tabs size="small" class="mt-4 indicator-tabs">
      <template v-if="['API'].includes(props.type) && props.testType === 'TEST_FUNCTIONALITY'">
        <TabPane key="case" :tab="`${t('xcan_httpTestInfo.testedCase')}(${apiOrCaseTestData?.length || '0'})`">
          <ApiOrCaseList :dataSource="apiOrCaseTestData" />
        </TabPane>
      </template>
      <template v-else>
        <TabPane key="indicator" :tab="t('xcan_httpTestInfo.testIndicator')">
          <PerfIndicator v-if="props.testType === 'TEST_PERFORMANCE'" :dataSource="performanceIndicatorData" />
          <StabilityIndicator v-if="props.testType === 'TEST_STABILITY'" :dataSource="stabilityIndicatorData" />
        </TabPane>
      </template>
      <TabPane
        v-if="projectTypeShowMap.showTask"
        key="task"
        :tab="`${t('xcan_httpTestInfo.associatedTaskCount')}(${associatedTaskData?.length || '0'})`">
        <TaskList :dataSource="associatedTaskData" />
      </TabPane>
    </Tabs>
  </template>
  <!-- Scenario test information -->
  <template v-else-if="['SCENARIO'].includes(props.type)">
    <div class="text-3 mt-3 mb-1 font-semibold">
      {{ t('common.basicInfo') }}
    </div>
    <TestConfigInfo :value="basicTestInfo" />
    <template v-if="props.testType === 'TEST_FUNCTIONALITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testInterface') }}
      </div>
      <ScenarioTestSummary :dataSource="testCaseSummaryData" />
    </template>
    <template v-if="props.testType === 'TEST_PERFORMANCE'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('common.testResult') }}
      </div>
      <PerfResult :indicatorPerf="performanceIndicatorData" :result="performanceResultData" />
    </template>
    <template v-if="props.testType === 'TEST_STABILITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('common.testResult') }}
      </div>
      <StabilityResult :indicatorStability="stabilityIndicatorData" :result="stabilityResultData" />
    </template>
    <Tabs size="small" class="mt-4 indicator-tabs">
      <template v-if="['TEST_PERFORMANCE', 'TEST_STABILITY'].includes(props.testType)">
        <TabPane key="indicator" :tab="t('xcan_httpTestInfo.testIndicator')">
          <PerfIndicator v-if="props.testType === 'TEST_PERFORMANCE'" :dataSource="performanceIndicatorData" />
          <StabilityIndicator v-if="props.testType === 'TEST_STABILITY'" :dataSource="stabilityIndicatorData" />
        </TabPane>
      </template>

      <TabPane
        v-if="projectTypeShowMap.showTask"
        key="task"
        :tab="`${t('xcan_httpTestInfo.associatedTaskCount')}(${associatedTaskData?.length || '0'})`">
        <TaskList :dataSource="associatedTaskData" />
      </TabPane>
    </Tabs>
  </template>
</template>
