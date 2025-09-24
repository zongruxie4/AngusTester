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
      <span id="a3" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.sprint.taskSummary.title') }}</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2.1">2.1<em class="inline-block w-3.5"></em>{{ t('reportPreview.sprint.taskSummary.summary.title') }}</span>
      </h2>
      <div class="flex items-center space-x-7">
        <Progress
          :percent="percent"
          :text="t('common.progress')"
          class="ml-4" />
        <div class="flex-1 border border-solid border-border-input">
          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.totalTasks') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.totalTaskNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.validTasks') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.validTaskNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.totalBugs') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.bugNum || 0 }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.validBugs') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.validBugNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.pendingCount') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.pendingNum || 0 }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.processingCount') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.inProgressNum || 0 }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.pendingConfirmCount') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.confirmingNum }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.completedCount') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.completedNum }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.cancelledCount') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.canceledNum }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.overdueCount') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.overdueNum }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.overdueRate') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ overdueRate }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.oneTimePassCount') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.oneTimePassedNum }}
            </div>
          </div>

          <div class="flex border-b border-solid border-border-input">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.oneTimePassRate') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ oneTimePassedRate }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.estimatedWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.evalWorkload }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.actualWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
              {{ totalOverview?.actualWorkload }}
            </div>
          </div>

          <div class="flex">
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.completedWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.completedWorkload }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.savedWorkload') }}
            </div>
            <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
              {{ totalOverview?.savingWorkload }}
            </div>
            <div
              class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ t('reportPreview.sprint.taskSummary.summary.fields.workloadSaveRate') }}
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
        <span id="a2.2">2.2<em class="inline-block w-3.5"></em>{{ t('chart.burndown.title') }}</span>
      </h2>
      <BurnDownChart :dataSource="burnDownCharts" />
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a2.3">2.3<em class="inline-block w-3.5"></em>{{ t('reportPreview.sprint.taskSummary.groupStats.title') }}</span>
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
