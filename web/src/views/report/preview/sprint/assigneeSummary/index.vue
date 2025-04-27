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
const BurnDownChart = defineAsyncComponent(() => import('@/views/report/preview/sprint/assigneeSummary/burndownChart/index.vue'));
const TaskGrouped = defineAsyncComponent(() => import('@/views/report/preview/sprint/taskSummary/taskGroup/index.vue'));

const assignees = computed(() => {
  return props.dataSource?.content?.tasks?.assignees || [];
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
      totalStatusOverview: item.statusOverview,
      totalPriorityOverview: item.priorityOverview,
      totalTypeOverview: item.typeOverview,
      fullname: map[item.assigneeId].fullname
    };
  });
});

const totalOverview = computed(() => {
  return props.dataSource?.content?.tasks?.totalOverview;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a3" class="text-4 text-theme-title font-medium">三、<em class="inline-block w-0.25"></em>经办人汇总结果</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a3.1">3.1<em class="inline-block w-3.5"></em>任务汇总结果</span>
      </h2>
      <div class="space-y-5">
        <div
          v-for="item in assigneeOverview"
          :key="item.assigneeId">
          <li>{{ item.fullname }}</li>
          <div class="flex items-center space-x-7">
            <Progress
              :percent="+item.totalOverview?.progress"
              text="进度"
              :title="item.fullname"
              class="ml-4" />
            <div class="flex-shrink-0 w-157.75 border border-solid border-border-input">
              <div class="flex border-b border-solid border-border-input">
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  任务总数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                  {{ item.totalOverview?.totalTaskNum || 0 }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  有效任务数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.validTaskNum || 0 }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  缺陷总数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.bugNum || 0 }}
                </div>
              </div>

              <div class="flex border-b border-solid border-border-input">
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  有效缺陷数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                  {{ item.totalOverview?.validBugNum || 0 }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  待处理数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.pendingNum || 0 }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  处理中数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.inProgressNum || 0 }}
                </div>
              </div>

              <div class="flex border-b border-solid border-border-input">
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  待确认数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                  {{ item.totalOverview?.confirmingNum }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  完成数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.completedNum }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  取消数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.canceledNum }}
                </div>
              </div>

              <div class="flex border-b border-solid border-border-input">
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  逾期数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                  {{ item.totalOverview?.overdueNum }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  逾期率
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.overdueRate }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  一次性通过数
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.oneTimePassedNum }}
                </div>
              </div>

              <div class="flex border-b border-solid border-border-input">
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  一次性通过率
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                  {{ item.totalOverview?.oneTimePassedRate }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  评估工作量
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.evalWorkload }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  实际工作量
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.actualWorkload }}
                </div>
              </div>

              <div class="flex">
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  完成工作量
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                  {{ item.totalOverview?.completedWorkload }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  节省工作量
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.savingWorkload }}
                </div>
                <div
                  class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                  工作量节省率
                </div>
                <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                  {{ item.totalOverview?.savingWorkloadRate }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a3.2">3.2<em class="inline-block w-3.5"></em>燃尽图</span>
      </h2>
      <div class="space-y-5">
        <div
          v-for="item in assigneeOverview"
          :key="item.assigneeId">
          <li class="mb-3">{{ item.fullname }}</li>
          <BurnDownChart :dataSource="item.burnDownCharts" />
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a3.3">3.3<em class="inline-block w-3.5"></em>分组统计</span>
      </h2>
      <div class="space-y-5">
        <div
          v-for="item in assigneeOverview"
          :key="item.assigneeId">
          <li class="mb-3">{{ item.fullname }}</li>
          <TaskGrouped :dataSource="{content: {tasks: item}}" />
        </div>
      </div>
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
