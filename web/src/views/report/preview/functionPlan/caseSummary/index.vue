<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';

const { t } = useI18n();

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

const testers = computed(() => {
  return props.dataSource?.content?.cases?.testers || {};
});

const testerOverview = computed(() => {
  const map = testers.value;
  return (props.dataSource?.content?.cases?.testerOverview || []).map((item) => {
    item.statusOverview.progress = +(item.statusOverview.progress?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    item.statusOverview.overdueRate = +(item.statusOverview.overdueRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    item.statusOverview.oneTimePassedRate = +(item.statusOverview.oneTimePassedRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    item.statusOverview.oneTimePassedReviewRate = +(item.statusOverview.oneTimePassedReviewRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    item.statusOverview.savingWorkloadRate = +(item.statusOverview.savingWorkloadRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
    return {
      ...item,
      fullName: map[item.testerId].fullName
    };
  });
});

const totalOverview = computed(() => {
  return props.dataSource?.content?.cases?.totalOverview;
});

const overdueRate = computed(() => {
  return (totalOverview.value?.overdueRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0) + '%';
});

const oneTimePassedRate = computed(() => {
  return (totalOverview.value?.oneTimePassedRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0) + '%';
});

const oneTimePassedReviewRate = computed(() => {
  return (totalOverview.value?.oneTimePassedReviewRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0) + '%';
});

const savingWorkloadRate = computed(() => {
  return (totalOverview.value?.savingWorkloadRate?.replace(/(\d+\.\d{2})\d+/, '$1') || 0) + '%';
});

const percent = computed(() => {
  return +(totalOverview.value?.progress?.replace(/(\d+\.\d{2})\d+/, '$1') || 0);
});

const burnDownCharts = computed(() => {
  return props.dataSource?.content?.cases?.burnDownCharts;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a2" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.functionPlan.caseSummary.title') }}</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2.1">2.1<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.caseSummary.testSummary.title') }}</span>
      </h2>
      <div class="flex items-center space-x-7">
        <Progress
          :percent="percent"
          text="进度"
          class="ml-4" />
        <div class="flex-1 border border-solid border-border-input">
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.totalCases') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.totalCaseNum || 0 }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.validCases') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.validCaseNum || 0 }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.pendingTest') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.pendingTestNum || 0 }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.passed') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.passedTestNum || 0 }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.failed') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.notPassedTestNum || 0 }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.blocked') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.blockedTestNum || 0 }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.cancelled') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.canceledTestNum }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.overdue') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.overdueNum }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.overdueRate') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ overdueRate }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.oneTimeTestPass') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.oneTimePassedNum }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.oneTimeTestPassRate') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ oneTimePassedRate }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                {{ t('reportPreview.functionPlan.caseSummary.fields.oneTimeReviewPassRate') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ oneTimePassedReviewRate }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.estimatedWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.evalWorkload }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.actualWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.actualWorkload }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.completedWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.completedWorkload }}
            </div>
          </div>

          <div class="flex">
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.savedWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.savingWorkload }}
            </div>
            <div
              class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.functionPlan.caseSummary.fields.workloadSaveRate') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ savingWorkloadRate }}
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

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2.2">2.2<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.caseSummary.reviewSummary.title') }}</span>
      </h2>

      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.totalReviewCases') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ totalOverview?.totalReviewCaseNum || 0 }}
          </div>
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.pendingReview') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ totalOverview?.pendingReviewNum || 0 }}
          </div>
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.reviewPassed') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ totalOverview?.passedReviewNum || 0 }}
          </div>
        </div>
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.reviewFailed') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ totalOverview?.failedReviewNum || 0 }}
          </div>
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.reviewed') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ totalOverview?.totalReviewedCaseNum || 0 }}
          </div>
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.notReviewed') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ totalOverview?.totalNotReviewCaseNum || 0 }}
          </div>
        </div>
        <div class="flex border-solid border-border-input">
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.totalReviewCount') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ totalOverview?.totalReviewTimes || 0 }}
          </div>
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.oneTimeReviewPass') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ totalOverview?.oneTimePassedReviewNum || 0 }}
          </div>
          <div
            class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.functionPlan.caseSummary.reviewSummary.fields.oneTimeReviewPassRate') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ totalOverview?.oneTimePassedReviewRate || 0 }}%
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a2.3">2.3<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.caseSummary.burndownChart.title') }}</span>
      </h2>
      <BurnDownChart :dataSource="burnDownCharts" />
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a2.4">2.4<em class="inline-block w-3.5"></em>{{ t('reportPreview.functionPlan.caseSummary.groupStats.title') }}</span>
      </h2>
      <CaseGrouped
        :dataSource="props.dataSource" />
    </div>

    <!-- <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a11">2.3<em class="inline-block w-3.5"></em>测试人员用例汇总结果</span>
      </h2>

      <div class="space-y-5">
        <div
          v-for="item in testerOverview"
          :key="item.testerId"
          class="flex items-center space-x-7">
          <progress
            :percent="+item.statusOverview?.progress"
            text="进度"
            :title="item.fullName"
            class="ml-4" />
          <div class="flex-shrink-0 w-157.75 border border-solid border-border-input">
            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                用例总数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.totalCaseNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                有效用例数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.validCaseNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                待测试数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ item.statusOverview?.pendingTestNum || 0 }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.passedTestNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                未通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.notPassedTestNum || 0 }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                阻塞数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ item.statusOverview?.blockedTestNum || 0 }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                取消数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.canceledTestNum }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                逾期数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.overdueNum }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                逾期率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ item.statusOverview?.overdueRate }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性测试通过数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.oneTimePassedNum }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性测试通过率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.oneTimePassedRate }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                一次性评审通过率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ item.statusOverview?.oneTimePassedReviewRate }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评估工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.evalWorkload }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                实际工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.actualWorkload }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                完成工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ item.statusOverview?.completedWorkload }}
              </div>
            </div>

            <div class="flex">
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                节省工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.savingWorkload }}
              </div>
              <div
                class="w-30 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                工作量节省率
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.statusOverview?.savingWorkloadRate }}
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
    </div> -->
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
