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
const BurnDownChart = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/caseSummary/burndownChart/index.vue'));
const CaseGrouped = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/caseSummary/group/index.vue'));
const casesMap = computed(() => {
  const cases = props.dataSource?.content?.cases?.testerOverview || [];
  return cases.reduce((prev, cur) => {
    const id = cur.testerId;
    prev[id] = {
      ...cur,
      totalTestResultOverview: cur.testResultOverview,
      totalPriorityOverview: cur.priorityOverview
    };
    return prev;
  }, {});
});

const testers = computed(() => {
  const _testers = props.dataSource?.content?.cases?.testers;
  if (!_testers) {
    return [];
  }
  return Object.values(_testers);
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a3" class="text-4 text-theme-title font-medium">三、<em class="inline-block w-0.25"></em>测试人汇总结果</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a3.1">3.1<em class="inline-block w-3.5"></em>测试汇总结果</span>
      </h2>
      <div
        v-for="(item) in testers"
        :key="item.id"
        class="mb-7 last:mb-0">
        <li>{{ item.fullname }}</li>
        <!-- {{ casesMap[item.id].statusOverview }} -->
        <div class="flex items-center space-x-7">
          <Progress
            :percent="casesMap[item.id].statusOverview.progress"
            text="进度"
            class="ml-4" />
          <div class="flex-1 border border-solid border-border-input">
            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                用例总数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.totalCaseNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                有效用例数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.validCaseNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                待测试数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.pendingTestNum || 0 }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.passedTestNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                未通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.notPassedTestNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                阻塞数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.blockedTestNum || 0 }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                取消数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.canceledTestNum }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                逾期数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.overdueNum }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                逾期率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.overdueRate }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性测试通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.oneTimePassedNum }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性测试通过率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.oneTimePassedRate }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性评审通过率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.oneTimePassedReviewRate }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评估工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.evalWorkload }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                实际工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.actualWorkload }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                完成工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.completedWorkload }}
              </div>
            </div>

            <div class="flex">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                节省工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.savingWorkload }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                工作量节省率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.savingWorkloadRate }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a3.2">3.2<em class="inline-block w-3.5"></em>评审汇总结果</span>
      </h2>
      <div
        v-for="(item) in testers"
        :key="item.id"
        class="mb-7 last:mb-0">
        <li>{{ item.fullname }}</li>
        <!-- {{ casesMap[item.id].statusOverview }} -->
        <div class="flex items-center space-x-7">
          <div class="border border-solid border-border-input w-full">
            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                总评审用例数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.totalReviewCaseNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                待评审数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.pendingReviewNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评审通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.passedReviewNum || 0 }}
              </div>
            </div>
            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评审未通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.failedReviewNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                已评审数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.totalReviewedCaseNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                未评审数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.totalNotReviewCaseNum || 0 }}
              </div>
            </div>
            <div class="flex border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                总评审次数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.totalReviewTimes || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性通过评审数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ casesMap[item.id].statusOverview?.oneTimePassedReviewNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性评审通过率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ casesMap[item.id].statusOverview?.oneTimePassedReviewRate || 0 }}%
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a3.3">3.3<em class="inline-block w-3.5"></em>燃尽图</span>
      </h2>
      <div
        v-for="(item) in testers"
        :key="item.id"
        class="mb-7 last:mb-0">
        <li class="mb-3">{{ item.fullname }}</li>
        <!-- {{ casesMap[item.id].statusOverview }} -->
        <BurnDownChart :data-source="casesMap[item.id].burnDownCharts" />
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a3.4">3.4<em class="inline-block w-3.5"></em>分组统计</span>
      </h2>
      <div
        v-for="(item) in testers"
        :key="item.id"
        class="mb-7 last:mb-0">
        <li class="mb-3">{{ item.fullname }}</li>
        <!-- {{ casesMap[item.id].statusOverview }} -->
        <CaseGrouped :data-source="{content: {cases: casesMap[item.id]}}" />
      </div>
    </div>

    <!-- <div
      v-for="(item,index) in testers"
      :key="item.id"
      class="mb-7 last:mb-0">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span :id="`a${index+13}`">3.{{ index+1 }}<em class="inline-block w-3.5"></em>{{ item.fullname }}</span>
      </h2>

      <div class="space-y-5">
        <div v-for="_case in casesMap[item.id]" :key="_case.id">
          <div class="flex items-center space-x-1 mb-2  break-all whitespace-pre-wrap"><em class="block w-1 h-1 mr-1 bg-gray-500 rounded"></em><span>{{ _case.name }}</span></div>
          <div class="border border-solid border-border-input">
            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                编码
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.code }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                优先级
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.priority?.message }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                所属模块
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.moduleName }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                是否逾期
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.overdueFlag?'是':'否' }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                截止时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.deadlineDate }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                测试人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.testerName }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                测试结果
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.testResult?.message }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                测试结果备注
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.testRemark }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                测试完成时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.testResultHandleDate }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                是否评审用例
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.reviewFlag?'是':'否' }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评审状态
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.reviewStatus?.message }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评审人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.reviewerName }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评审意见
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.reviewRemark }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评审时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.reviewDate }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                工作量评估方式
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.evalWorkloadMethod?.message }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评估工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.evalWorkload }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                实际工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.actualWorkload }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                总测试次数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.testNum }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                测试失败次数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.testFailNum }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                添加人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.createdByName }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                添加时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.createdDate }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                最后修改人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _case?.lastModifiedByName }}
              </div>
            </div>

            <div class="flex">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                最后修改时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _case?.lastModifiedDate }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div> -->
  </div>
</template>
