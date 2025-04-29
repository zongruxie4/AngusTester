<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';

import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const Progress = defineAsyncComponent(() => import('@/views/report/preview/components/progress/index.vue'));
const BurnDownChart = defineAsyncComponent(() => import('@/views/report/preview/sprint/taskSummary/burndownChart/index.vue'));
const TaskGrouped = defineAsyncComponent(() => import('@/views/report/preview/sprint/taskSummary/taskGroup/index.vue'));

const assignees = computed(() => {
  return props.dataSource?.content?.tasks?.assignees || {};
});

const assigneeOverview = computed(() => {
  const map = assignees.value;
  return (props.dataSource?.content?.tasks?.assigneeOverview || []).map((item) => {
    item.statusOverview.progress = +(item.statusOverview.progress?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    item.statusOverview.overdueRate = +(item.statusOverview.overdueRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    item.statusOverview.oneTimePassedRate = +(item.statusOverview.oneTimePassedRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    item.statusOverview.savingWorkloadRate = +(item.statusOverview.savingWorkloadRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    return {
      ...item,
      fullName: map[item.assigneeId].fullName
    };
  });
});

const totalOverview = computed(() => {
  return props.dataSource?.content?.tasks?.totalOverview;
});

const overdueRate = computed(() => {
  return (totalOverview.value?.overdueRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0) + '%';
});

const oneTimePassedRate = computed(() => {
  return (totalOverview.value?.oneTimePassedRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0) + '%';
});

const savingWorkloadRate = computed(() => {
  return (totalOverview.value?.savingWorkloadRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0) + '%';
});

const percent = computed(() => {
  return +(totalOverview.value?.progress?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
});

const burnDownCharts = computed(() => {
  return props.dataSource?.content?.tasks?.burnDownCharts;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a3" class="text-4 text-theme-title font-medium">二、<em class="inline-block w-0.25"></em>研发任务</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2.1">2.1<em class="inline-block w-3.5"></em>任务汇总结果</span>
      </h2>
      <div class="flex items-center space-x-7">
        <Progress
          :percent="percent"
          text="进度"
          class="ml-4" />
        <div class="flex-1 border border-solid border-border-input">
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              任务总数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.totalTaskNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              有效任务数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.validTaskNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              缺陷总数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.bugNum || 0 }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              有效缺陷数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.validBugNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              待处理数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.pendingNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              处理中数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.inProgressNum || 0 }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              待确认数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.confirmingNum }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              完成数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.completedNum }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              取消数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.canceledNum }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              逾期数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.overdueNum }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              逾期率
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ overdueRate }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              一次性通过数
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.oneTimePassedNum }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              一次性通过率
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ oneTimePassedRate }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              评估工作量
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.evalWorkload }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              实际工作量
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.actualWorkload }}
            </div>
          </div>

          <div class="flex">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              完成工作量
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.completedWorkload }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              节省工作量
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.savingWorkload }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              工作量节省率
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ savingWorkloadRate }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a2.2">2.2<em class="inline-block w-3.5"></em>燃尽图</span>
      </h2>
      <BurnDownChart :dataSource="burnDownCharts" />
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2.3">2.3<em class="inline-block w-3.5"></em>分组统计</span>
      </h2>

      <TaskGrouped
        :projectInfo="props.projectInfo"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :dataSource="props.dataSource" />
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}

.meeting-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
  background-color: #fafafa;
}
</style>
