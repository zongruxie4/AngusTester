<script lang="ts" setup>
import { onMounted, ref, defineAsyncComponent, computed, inject, Ref } from 'vue';
import { Tabs, TabPane } from 'ant-design-vue';
import { NoData } from '@xcan-angus/vue-ui';
import { exec } from '@/api/ctrl';

interface Props {
  apisId: string;
  appInfo:{[key:string]:any};
  userInfo:{[key:string]:any};
  projectId:string;
}

const props = withDefaults(defineProps<Props>(), {
  apisId: ''
});
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));
const TestSummary = defineAsyncComponent(() => import('./testSummary/index.vue'));
const Execdetail = defineAsyncComponent(() => import('@/views/execution/info/index.vue'));
const Task = defineAsyncComponent(() => import('./task/index.vue'));

const activeTab = ref('func');
const dataSource = ref();

const loadApisResult = async () => {
  const [error, { data }] = await exec.getApiTestResult(props.apisId);
  if (error) {
    return;
  }
  dataSource.value = data;
};

const funcExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_FUNCTIONALITY?.execId;
});
const perfExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_PERFORMANCE?.execId;
});
const customExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_CUSTOMIZATION?.execId;
});
const stabilityExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_STABILITY?.execId;
});

const handleDel = () => {
  loadApisResult();
};

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
      <TabPane key="func" tab="功能测试">
        <Execdetail
          class="p-0"
          :showBackBtn="false"
          :execId="funcExecId"
          :monicaEditorStyle="{height: '600px'}"
          scriptType="TEST_FUNCTIONALITY"
          @del="handleDel" />

        <!--        <NoData size="small" class="mt-25" />-->
      </TabPane>
      <TabPane key="perf" tab="性能测试">
        <Execdetail
          :showBackBtn="false"
          :execId="perfExecId"
          :monicaEditorStyle="{height: '600px'}"
          scriptType="TEST_PERFORMANCE"
          @del="handleDel" />
        <!--        <NoData-->
        <!--          v-else-->
        <!--          size="small"-->
        <!--          class="mt-25" />-->
      </TabPane>
      <TabPane key="stability" tab="稳定性测试">
        <Execdetail
          :showBackBtn="false"
          :execId="stabilityExecId"
          :monicaEditorStyle="{height: '600px'}"
          scriptType="TEST_STABILITY"
          @del="handleDel" />
        <!--        <NoData-->
        <!--          v-else-->
        <!--          size="small"-->
        <!--          class="mt-25" />-->
      </TabPane>
      <TabPane key="custom" tab="自定义测试">
        <Execdetail
          :showBackBtn="false"
          :execId="customExecId"
          :monicaEditorStyle="{height: '600px'}"
          scriptType="TEST_CUSTOMIZATION"
          @del="handleDel" />
        <!--        <NoData-->
        <!--          v-else-->
        <!--          size="small"-->
        <!--          class="mt-25" />-->
      </TabPane>
      <TabPane
        v-if="proTypeShowMap.showTask"
        key="task"
        tab="测试任务">
        <Task :apisId="props.apisId" :projectId="props.projectId" />
      </TabPane>
    </Tabs>
  </div>
</template>
