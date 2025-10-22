<script lang="ts" setup>
import { onMounted, ref, defineAsyncComponent, computed, inject, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Tabs, TabPane } from 'ant-design-vue';
import { exec } from '@/api/ctrl';
import { ScriptType } from '@xcan-angus/infra';

const TestSummary = defineAsyncComponent(() => import('./TestSummary.vue'));
const Task = defineAsyncComponent(() => import('./Task.vue'));
const ExecDetail = defineAsyncComponent(() => import('@/views/execution/detail/index.vue'));

interface Props {
  apisId: string;
  appInfo:{[key:string]:any};
  userInfo:{[key:string]:any};
  projectId:string;
}

const props = withDefaults(defineProps<Props>(), {
  apisId: ''
});

const { t } = useI18n();
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

const activeTab = ref('func');
const dataSource = ref();

/**
 * Load API test results from backend
 * Updates dataSource with test result data
 */
const loadApisResult = async () => {
  const [error, { data }] = await exec.getApiTestResult(props.apisId);
  if (error) {
    return;
  }
  dataSource.value = data;
};

/**
 * Get execution ID for functional test
 * @returns {string|undefined} Functional test execution ID
 */
const functionalTestExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_FUNCTIONALITY?.execId;
});

/**
 * Get execution ID for performance test
 * @returns {string|undefined} Performance test execution ID
 */
const performanceTestExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_PERFORMANCE?.execId;
});

/**
 * Get execution ID for custom test
 * @returns {string|undefined} Custom test execution ID
 */
const customTestExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_CUSTOMIZATION?.execId;
});

/**
 * Get execution ID for stability test
 * @returns {string|undefined} Stability test execution ID
 */
const stabilityTestExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_STABILITY?.execId;
});

/**
 * Handle test deletion event
 * Reloads API test results after deletion
 */
const handleTestDeletion = () => {
  loadApisResult();
};

/**
 * Component mounted lifecycle hook
 * Loads API test results if apisId is provided
 */
onMounted(() => {
  if (props.apisId) {
    loadApisResult();
  }
});
</script>
<template>
  <div class="h-full" :class="[['activity', 'comment'].includes(activeTab) ? 'flex flex-col' : 'overflow-y-auto']">
    <TestSummary
      v-if="dataSource"
      class="mb-4"
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :userInfo="props.userInfo"
      :dataSource="dataSource" />
    <Tabs
      v-model:activeKey="activeTab"
      size="small"
      type="card"
      class="flex-1 min-h-0">
      <TabPane key="func" :tab="t('commonExecTest.functionalTest')">
        <ExecDetail
          class="p-0"
          :showBackBtn="false"
          :execId="functionalTestExecId"
          :monicaEditorStyle="{height: '600px'}"
          :scriptType="ScriptType.TEST_FUNCTIONALITY"
          @del="handleTestDeletion" />
      </TabPane>
      <TabPane key="perf" :tab="t('commonExecTest.performanceTest')">
        <ExecDetail
          :showBackBtn="false"
          :execId="performanceTestExecId"
          :monicaEditorStyle="{height: '600px'}"
          :scriptType="ScriptType.TEST_PERFORMANCE"
          @del="handleTestDeletion" />
      </TabPane>
      <TabPane key="stability" :tab="t('commonExecTest.stabilityTest')">
        <ExecDetail
          :showBackBtn="false"
          :execId="stabilityTestExecId"
          :monicaEditorStyle="{height: '600px'}"
          :scriptType="ScriptType.TEST_STABILITY"
          @del="handleTestDeletion" />
      </TabPane>
      <TabPane key="custom" :tab="t('commonExecTest.customTest'')">
        <ExecDetail
          :showBackBtn="false"
          :execId="customTestExecId"
          :monicaEditorStyle="{height: '600px'}"
          :scriptType="ScriptType.TEST_CUSTOMIZATION"
          @del="handleTestDeletion" />
      </TabPane>
      <TabPane
        v-if="proTypeShowMap.showTask"
        key="task"
        :tab="t('service.apiExecDetail.tabs.testTask')">
        <Task :apisId="props.apisId" :projectId="props.projectId" />
      </TabPane>
    </Tabs>
  </div>
</template>
