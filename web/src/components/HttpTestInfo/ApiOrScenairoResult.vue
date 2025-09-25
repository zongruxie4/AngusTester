<script lang="ts" setup>
import { watch, onMounted, ref, defineAsyncComponent, inject, Ref } from 'vue';
import { Tabs, TabPane } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
    type: 'API'| 'SCENARIO',
    testType: 'TEST_FUNCTIONALITY'|'TEST_PERFORMANCE'|'TEST_STABILITY',
    dataSource: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  type: 'API',
  testType: 'TEST_FUNCTIONALITY',
  dataSource: () => ({})
});

const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

const TestConfigInfo = defineAsyncComponent(() => import('./TestConfigInfo.vue'));
const ApiTestCase = defineAsyncComponent(() => import('./ApiTestCase.vue'));
const ScenarioTestSummary = defineAsyncComponent(() => import('./ScenarioTestSummary.vue'));
const ApiOrCaseList = defineAsyncComponent(() => import('./ApiOrCaseList.vue'));
const TaskList = defineAsyncComponent(() => import('./TaskList.vue'));
const PerfResult = defineAsyncComponent(() => import('./PerfResult.vue'));
const PerfIndicator = defineAsyncComponent(() => import('./PerfIndicator.vue'));
const StabilityResult = defineAsyncComponent(() => import('./StabilityResult.vue'));
const StabilityIndicator = defineAsyncComponent(() => import('./StabilityIndicator.vue'));

const execResults = ref();
const assocScripts = ref();
const assocTasks = ref();

const basicInfo = ref(); // 基本信息
const testCaseData = ref(); // 用例或者接口测试统计
const apiOrCaseData = ref(); // 接口测试用例
const assocScriptData = ref(); // 关联脚本
const assocTaskData = ref(); // 关联任务
const perfResultData = ref(); // 性能测试结果
const stabilityResultData = ref(); // 稳定性测试结果
const perfIndicator = ref(); // 性能测试指标
const stabilityIndicator = ref(); // 稳定性测试指标

const enabledTestMap = ref([]); // 启用的测试类型

const resetData = () => {
  execResults.value = undefined;
  assocScripts.value = undefined;
  assocTasks.value = undefined;
  basicInfo.value = undefined;
  testCaseData.value = undefined;
  apiOrCaseData.value = undefined;
  assocScriptData.value = undefined;
  assocTaskData.value = undefined;
  perfResultData.value = undefined;
  stabilityResultData.value = undefined;
  perfIndicator.value = undefined;
  stabilityIndicator.value = undefined;
  enabledTestMap.value = [];
};

const setInfoData = () => {
  basicInfo.value = execResults.value?.[props.testType] || {};
  if (!basicInfo.value) {
    // showEmpty.value = true;
    return;
  }
  // showEmpty.value = false;
  if (props.testType === 'TEST_FUNCTIONALITY' && props.type === 'API') {
    testCaseData.value = basicInfo.value.caseSummary;
    apiOrCaseData.value = basicInfo.value.caseResults;
  }
  if (props.type === 'SCENARIO') {
    testCaseData.value = basicInfo.value?.targetSummary || {};
  }
  assocScriptData.value = assocScripts.value?.[props.testType] || {};
  assocTaskData.value = assocTasks.value?.[props.testType];
  perfResultData.value = basicInfo.value.sampleSummary;
  perfIndicator.value = basicInfo.value.indicatorPerf;
  const useNodeId = basicInfo.value.usageFailedNodeId || Object.keys(basicInfo.value.nodeUsageSummary || {})[0];
  stabilityResultData.value = {
    ...basicInfo.value.sampleSummary,
    ...(basicInfo.value.nodeUsageSummary?.[useNodeId] || {})
  };
  stabilityIndicator.value = basicInfo.value.indicatorStability;
};

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    resetData();
    execResults.value = newValue.testResult?.resultDetailVoMap || {};
    enabledTestMap.value = (newValue.testResult?.enabledTestTypes || []).map(i => i.value);
    assocScripts.value = newValue.assocScripts || {};
    assocTasks.value = newValue.assocTasks || {};
    setInfoData();
  }, {
    immediate: true
  });
});

</script>
<template>
  <!-- 接口测试信息 -->
  <template v-if="props.type === 'API'">
    <div class="text-3 mt-3 mb-1 font-semibold">
      {{ t('common.basicInfo') }}
    </div>
    <TestConfigInfo :value="basicInfo" :enabled="enabledTestMap.includes(props.testType)" />
    <template v-if="props.type === 'API' && props.testType === 'TEST_FUNCTIONALITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testCase') }}
      </div>
      <ApiTestCase :dataSource="testCaseData" />
    </template>
    <template v-if="props.testType === 'TEST_PERFORMANCE'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testResult') }}
      </div>
      <PerfResult :indicatorPerf="perfIndicator" :result="perfResultData" />
    </template>
    <template v-if="props.testType === 'TEST_STABILITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testResult') }}
      </div>
      <StabilityResult :indicatorStability="stabilityIndicator" :result="stabilityResultData" />
    </template>
    <Tabs size="small" class="mt-4 indicator-tabs">
      <template v-if="['API'].includes(props.type) && props.testType === 'TEST_FUNCTIONALITY'">
        <TabPane key="case" :tab="`${t('xcan_httpTestInfo.testedCase')}(${apiOrCaseData?.length || '0'})`">
          <ApiOrCaseList :dataSource="apiOrCaseData" />
        </TabPane>
      </template>
      <template v-else>
        <TabPane key="indicator" :tab="t('xcan_httpTestInfo.testIndicator')">
          <PerfIndicator v-if="props.testType === 'TEST_PERFORMANCE'" :dataSource="perfIndicator" />
          <StabilityIndicator v-if="props.testType === 'TEST_STABILITY'" :dataSource="stabilityIndicator" />
        </TabPane>
      </template>
      <TabPane
        v-if="proTypeShowMap.showTask"
        key="task"
        :tab="`${t('xcan_httpTestInfo.associatedTaskCount')}(${assocTaskData?.length || '0'})`">
        <TaskList :dataSource="assocTaskData" />
      </TabPane>
    </Tabs>
  </template>
  <!-- 场景测试信息 -->
  <template v-else-if="['SCENARIO'].includes(props.type)">
    <div class="text-3 mt-3 mb-1 font-semibold">
      {{ t('common.basicInfo') }}
    </div>
    <TestConfigInfo :value="basicInfo" />
    <template v-if="props.testType === 'TEST_FUNCTIONALITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testInterface') }}
      </div>
      <ScenarioTestSummary :dataSource="testCaseData" />
    </template>
    <template v-if="props.testType === 'TEST_PERFORMANCE'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testResult') }}
      </div>
      <PerfResult :indicatorPerf="perfIndicator" :result="perfResultData" />
    </template>
    <template v-if="props.testType === 'TEST_STABILITY'">
      <div class="text-3 mt-4 mb-1 font-semibold">
        {{ t('xcan_httpTestInfo.testResult') }}
      </div>
      <StabilityResult :indicatorStability="stabilityIndicator" :result="stabilityResultData" />
    </template>
    <Tabs size="small" class="mt-4 indicator-tabs">
      <template v-if="['TEST_PERFORMANCE', 'TEST_STABILITY'].includes(props.testType)">
        <TabPane key="indicator" :tab="t('xcan_httpTestInfo.testIndicator')">
          <PerfIndicator v-if="props.testType === 'TEST_PERFORMANCE'" :dataSource="perfIndicator" />
          <StabilityIndicator v-if="props.testType === 'TEST_STABILITY'" :dataSource="stabilityIndicator" />
        </TabPane>
      </template>

      <TabPane
        v-if="proTypeShowMap.showTask"
        key="task"
        :tab="`${t('xcan_httpTestInfo.associatedTaskCount')}(${assocTaskData?.length || '0'})`">
        <TaskList :dataSource="assocTaskData" />
      </TabPane>
    </Tabs>
  </template>
</template>
