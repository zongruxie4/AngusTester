<script lang="ts" setup>

import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { RadioGroup } from 'ant-design-vue';
import { CtoProps, RecentDateOption } from './types';
import { useCtoData } from './composables/useCtoData';
import { useChartManagement } from './composables/useChartManagement';
import { useCtoLifecycle } from './composables/useCtoLifecycle';

const { t } = useI18n();

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<CtoProps>(), {
  projectId: undefined,
  userId: undefined,
  sprintId: undefined,
  planId: undefined,
  creatorObjectType: undefined,
  creatorObjectId: undefined,
  createdDateStart: undefined,
  createdDateEnd: undefined,
  countType: 'task',
  onShow: false
});

/**
 * Recent date options for filtering metrics
 */
const recentDateOptions: RecentDateOption[] = [
  {
    value: 'today',
    label: t('kanban.cto.recentDate.today')
  },
  {
    value: 'lastWeek',
    label: t('kanban.cto.recentDate.lastWeek')
  },
  {
    value: 'lastMonth',
    label: t('kanban.cto.recentDate.lastMonth')
  }
];

/**
 * Selected recent date for filtering
 */
const selectedRecentDate = ref('today');

/**
 * Chart container references
 */
const progressRef = ref<HTMLElement>();
const recentCompleteRef = ref<HTMLElement>();
const recentOverdueRef = ref<HTMLElement>();
const recentCompletedWorkloadRef = ref<HTMLElement>();
const recentSavingRateRef = ref<HTMLElement>();
const blockTaskRef = ref<HTMLElement>();
const blockTaskWorkloadRef = ref<HTMLElement>();
const overdueTaskRef = ref<HTMLElement>();
const unplannedTaskNumRef = ref<HTMLElement>();
const unplannedWorkloadRef = ref<HTMLElement>();
const taskTypeRef = ref<HTMLElement>();
const taskStatusRef = ref<HTMLElement>();
const leadTimeRef = ref<HTMLElement>();
const criticalFailureRef = ref<HTMLElement>();
const majorFailureRef = ref<HTMLElement>();
const minorFailureRef = ref<HTMLElement>();
const trivialFailureRef = ref<HTMLElement>();
const apiOpenTestRef = ref<HTMLElement>();
const apiSuccessTestRef = ref<HTMLElement>();
const scenarioOpenTestRef = ref<HTMLElement>();
const scenarioSuccessTestRef = ref<HTMLElement>();
const testStatusRef = ref<HTMLElement>();
const reviewStatusRef = ref<HTMLElement>();

/**
 * Initialize composables
 */
const {
  memberNum,
  progressData,
  backloggedData,
  recentDeliveryData,
  overdueAssessmentData,
  unplannedWorkData,
  failureAssessmentData,
  apiTestData,
  scenarioTestData,
  totalTypeData,
  totalStatusData,
  leadTimeData,
  totalTestResultData,
  totalReviewStatusData,
  loadCtoData,
  resetData
} = useCtoData(props);

/**
 * Loads dashboard data and updates charts
 */
const loadData = async () => {
  await loadCtoData();

  // Update all charts with new data
  const chartData = {
    totalProgressOverview: progressData.value,
    backloggedCount: backloggedData.value,
    recentDeliveryCount: recentDeliveryData.value,
    overdueAssessmentCount: overdueAssessmentData.value,
    unplannedWorkCount: unplannedWorkData.value,
    failureAssessmentCount: failureAssessmentData.value,
    apisTestCount: apiTestData.value,
    scenarioTestCount: scenarioTestData.value,
    totalTypeCount: totalTypeData.value,
    totalStatusCount: totalStatusData.value,
    leadTimeCount: leadTimeData.value,
    totalTestResultCount: totalTestResultData.value,
    totalReviewStatusCount: totalReviewStatusData.value
  };

  updateCharts(chartData, props.countType);
};

const {
  initializeCharts,
  resizeAllCharts,
  resizeChartsByType,
  updateCharts,
  updateRecentDateCharts
} = useChartManagement();

const {
  shouldNotify,
  resizeNotify,
  handleWindowResize,
  refresh,
  setupLifecycle,
  cleanup
} = useCtoLifecycle(props, loadData, resizeAllCharts, resizeChartsByType);

/**
 * Handles recent date change and updates related charts
 */
const handleRecentDateChange = () => {
  if (recentDeliveryData.value && selectedRecentDate.value) {
    updateRecentDateCharts(selectedRecentDate.value, recentDeliveryData.value);
  }
};

/**
 * Initializes component on mount
 */
onMounted(async () => {
  // Initialize charts with DOM references
  const chartRefs: Record<string, HTMLElement> = {};

  if (progressRef.value) chartRefs.progressRef = progressRef.value;
  if (recentCompleteRef.value) chartRefs.recentCompleteRef = recentCompleteRef.value;
  if (recentOverdueRef.value) chartRefs.recentOverdueRef = recentOverdueRef.value;
  if (recentCompletedWorkloadRef.value) chartRefs.recentCompletedWorkloadRef = recentCompletedWorkloadRef.value;
  if (recentSavingRateRef.value) chartRefs.recentSavingRateRef = recentSavingRateRef.value;
  if (blockTaskRef.value) chartRefs.blockTaskRef = blockTaskRef.value;
  if (blockTaskWorkloadRef.value) chartRefs.blockTaskWorkloadRef = blockTaskWorkloadRef.value;
  if (overdueTaskRef.value) chartRefs.overdueTaskRef = overdueTaskRef.value;
  if (unplannedTaskNumRef.value) chartRefs.unplannedTaskNumRef = unplannedTaskNumRef.value;
  if (unplannedWorkloadRef.value) chartRefs.unplannedWorkloadRef = unplannedWorkloadRef.value;
  if (taskTypeRef.value) chartRefs.taskTypeRef = taskTypeRef.value;
  if (taskStatusRef.value) chartRefs.taskStatusRef = taskStatusRef.value;
  if (leadTimeRef.value) chartRefs.leadTimeRef = leadTimeRef.value;
  if (criticalFailureRef.value) chartRefs.criticalFailureRef = criticalFailureRef.value;
  if (majorFailureRef.value) chartRefs.majorFailureRef = majorFailureRef.value;
  if (minorFailureRef.value) chartRefs.minorFailureRef = minorFailureRef.value;
  if (trivialFailureRef.value) chartRefs.trivialFailureRef = trivialFailureRef.value;
  if (apiOpenTestRef.value) chartRefs.apiOpenTestRef = apiOpenTestRef.value;
  if (apiSuccessTestRef.value) chartRefs.apiSuccessTestRef = apiSuccessTestRef.value;
  if (scenarioOpenTestRef.value) chartRefs.scenarioOpenTestRef = scenarioOpenTestRef.value;
  if (scenarioSuccessTestRef.value) chartRefs.scenarioSuccessTestRef = scenarioSuccessTestRef.value;
  if (testStatusRef.value) chartRefs.testStatusRef = testStatusRef.value;
  if (reviewStatusRef.value) chartRefs.reviewStatusRef = reviewStatusRef.value;

  initializeCharts(chartRefs as Record<string, HTMLElement>);
  // Setup lifecycle management
  setupLifecycle();
});

/**
 * Cleans up component on unmount
 */
onBeforeUnmount(() => {
  cleanup();
  resetData();
});

/**
 * Exposes methods for parent component access
 */
defineExpose({
  refresh,
  handleWindowResize
});
</script>

<template>
  <div class="py-2 text-3 space-y-2">
    <!-- Row 1: Progress and Recent Delivery (3:5 ratio) -->
    <div class="flex space-x-2 h-65">
      <!-- Progress Section (3/8 width) -->
      <div class="border rounded h-full flex-3/8 p-2">
          <div class="section-title">{{ t('kanban.cto.progress.totalProgress') }}</div>
          <div class="flex justify-around mt-3">
            <div class="text-center">
            <div class="font-bold text-7 text-slate-700">{{ progressData.totalNum || 0 }}</div>
            <div class="text-sm text-slate-500 mt-1">{{ props.countType === 'task' ? t('kanban.cto.progress.totalCount' ) : t('kanban.cto.progress.totalUseCaseCount') }}</div>
          </div>
          <div class="text-center">
            <div class="font-bold text-7 text-slate-700">{{ progressData.totalWorkload || 0 }}</div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.progress.totalWorkload') }}</div>
          </div>
          <div class="text-center">
            <div class="font-bold text-7 text-slate-700">{{ memberNum || 0 }}</div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.progress.teamMember') }}</div>
          </div>
        </div>
        <div ref="progressRef" class="h-1/2 w-2/3"></div>
      </div>

      <!-- Recent Delivery Section (5/8 width) -->
      <div class="border rounded h-full flex-5/8 p-2 space-y-2">
        <div class="flex justify-between">
          <div class="section-title">{{ t('kanban.cto.recentDelivery.recentDeliveryTitle') }}</div>
          <RadioGroup
            v-model:value="selectedRecentDate"
            optionType="button"
            buttonStyle="solid"
            size="small"
            :options="recentDateOptions"
            @change="handleRecentDateChange" />
        </div>
        <div class="flex space-x-2 justify-around">
          <div class="flex-1 pl-15">
            <div><span class="font-bold text-5 text-emerald-600">{{ recentDeliveryData?.[selectedRecentDate]?.completedNum || 0 }}</span><span class="text-sm text-slate-400">/{{ recentDeliveryData?.[selectedRecentDate]?.totalNum || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.recentDelivery.deliveryTaskCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-red-500">{{ recentDeliveryData?.[selectedRecentDate]?.OverdueNum || 0 }}</span><span class="text-sm text-slate-400">/{{ recentDeliveryData?.[selectedRecentDate]?.totalNum || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.recentDelivery.deliveryTaskOverdueCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-blue-600">{{ recentDeliveryData?.[selectedRecentDate]?.completedWorkload || 0 }}</span><span class="text-sm text-slate-400">/{{ recentDeliveryData?.[selectedRecentDate]?.totalWorkload || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.recentDelivery.deliveryWorkload') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-amber-600">{{ recentDeliveryData?.[selectedRecentDate]?.savingWorkload || 0 }}</span><span class="text-sm text-slate-400">/{{ recentDeliveryData?.[selectedRecentDate]?.totalWorkload || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.recentDelivery.deliveryWorkloadSaving') }}</div>
          </div>
        </div>
        <div class="flex space-x-2 h-1/2">
          <div ref="recentCompleteRef" class="h-full flex-1"></div>
          <div ref="recentOverdueRef" class="h-full flex-1"></div>
          <div ref="recentCompletedWorkloadRef" class="h-full flex-1"></div>
          <div ref="recentSavingRateRef" class="h-full flex-1"></div>
        </div>
      </div>
    </div>

    <!-- Row 2: Overdue Assessment and Failure Assessment/API Test and Scenario Test (3:5 ratio) -->
    <div class="flex space-x-2 h-65">
      <!-- Overdue Assessment Section (3/8 width) -->
      <div class="border rounded h-full flex-3/8 p-2">
          <div class="section-title">{{ t('kanban.cto.overdue.overdueAssessment') }}</div>
          <div class="flex justify-around mt-3">
            <div class="text-center">
            <div class="font-bold text-6 text-red-600">{{ overdueAssessmentData.overdueNum || 0 }}</div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.overdue.overdueCount') }}</div>
            </div>
            <div class="text-center">
            <div class="font-bold text-6 text-red-600">{{ overdueAssessmentData.overdueWorkload || 0 }}</div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.overdue.overdueWorkload') }}</div>
            </div>
            <div class="text-center">
            <div class="font-bold text-6 text-red-600">{{ overdueAssessmentData.overdueWorkloadProcessingTime || 0 }}</div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.overdue.overdueWorkloadProcessingTime') }}</div>
          </div>
        </div>
        <div class="flex h-1/2 items-center">
          <div ref="overdueTaskRef" class="h-full w-3/5"></div>
          <div class="text-center flex-1">
            <div :class="`risk-level-${overdueAssessmentData?.riskLevel?.value}`" class="font-bold text-5">{{ overdueAssessmentData?.riskLevel?.message }}</div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.overdue.overdueRisk') }}</div>
          </div>
        </div>
      </div>

      <!-- Failure Assessment (Task) or API/Scenario Test (Use Case) Section (5/8 width) -->
      <div v-show="props.countType === 'task'" class="border rounded h-full flex-5/8 p-2 space-y-2">
        <div class="flex justify-between">
          <div class="section-title">{{ t('kanban.cto.failureAssessment.failureAssessment') }}</div>
        </div>
        <div class="flex space-x-2 justify-around">
          <div class="flex-1 pl-15">
            <div><span class="font-bold text-5 text-red-600">{{ failureAssessmentData?.failureNum || 0 }}</span><span class="text-sm text-slate-400">/{{ failureAssessmentData?.totalNum || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.failureAssessment.failureCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-orange-600">{{ failureAssessmentData?.totalWorkload || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.failureAssessment.failureWorkload') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-red-700">{{ failureAssessmentData?.failureLevelCount?.CRITICAL || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.failureAssessment.failureCompletedNum') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-orange-500">{{ failureAssessmentData?.failureLevelCount?.MAJOR || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.failureAssessment.failureOverdueNum') }}</div>
          </div>
        </div>
        <div class="flex space-x-2 h-1/2 mt-2">
          <div ref="criticalFailureRef" class="h-full flex-1"></div>
          <div ref="majorFailureRef" class="h-full flex-1"></div>
          <div ref="minorFailureRef" class="h-full flex-1"></div>
          <div ref="trivialFailureRef" class="h-full flex-1"></div>
        </div>
      </div>

      <!-- API and Scenario Test (Use Case) Section (5/8 width) -->
      <div v-show="props.countType === 'useCase'" class="h-full flex-5/8 space-x-2 flex">
        <div class="flex-1/2 border rounded p-2">
          <div class="section-title">{{ t('kanban.cto.apiTest.apiTestTitle') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-15">
              <div><span class="font-bold text-5 text-emerald-600">{{ apiTestData.enabledTestNum || 0 }}</span></div>
              <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.apiTest.enabledTestCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-bold text-5 text-emerald-600">{{ apiTestData.passedTestNum || 0 }}</span></div>
              <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.apiTest.passedTestCount') }}</div>
            </div>
          </div>
          <div class="flex space-x-2 justify-around mt-2 h-1/2">
            <div ref="apiOpenTestRef" class="flex-1 h-full"></div>
            <div ref="apiSuccessTestRef" class="flex-1 h-full"></div>
          </div>
        </div>
        <div class="flex-1/2 border rounded p-2">
          <div class="section-title">{{ t('kanban.cto.scenarioTest.scenarioTestTitle') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-15">
              <div><span class="font-bold text-5 text-blue-600">{{ scenarioTestData.enabledTestNum || 0 }}</span></div>
              <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.apiTest.enabledTestCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-bold text-5 text-blue-600">{{ scenarioTestData.passedTestNum || 0 }}</span></div>
              <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.apiTest.passedTestCount') }}</div>
            </div>
          </div>
          <div class="flex space-x-2 justify-around mt-2 h-1/2">
            <div ref="scenarioOpenTestRef" class="flex-1 h-full"></div>
            <div ref="scenarioSuccessTestRef" class="flex-1 h-full"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 3: Backlog and Delivery Cycle (3:5 ratio) -->
    <div class="flex space-x-2 h-65">
      <!-- Backlog Section (3/8 width) -->
      <div class="border rounded h-full flex-3/8 p-2 space-y-2">
        <div class="section-title">{{ props.countType === 'task' ? t('kanban.cto.backlog.backlogTaskTitle') : t('kanban.cto.backlog.backlogUseCaseTitle') }}</div>
        <div class="flex space-x-2 justify-around mt-2">
          <div class="flex-1 pl-15">
            <div><span class="font-bold text-5 text-amber-600">{{ backloggedData.backloggedNum || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ props.countType === 'task' ? t('kanban.cto.backlog.backlogTaskCount') : t('kanban.cto.backlog.backlogUseCaseCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-amber-600">{{ backloggedData.backloggedCompletionTime || 0 }}</span><span class="text-sm text-slate-400">{{ t('kanban.cto.deliveryCycle.hours') }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.backlog.estimatedTime') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-amber-600">{{ backloggedData.backloggedWorkload || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.backlog.backlogWorkload') }}</div>
          </div>
        </div>
        <div class="flex h-1/2 mt-2">
          <div ref="blockTaskRef" class="flex-1 h-full"></div>
          <div ref="blockTaskWorkloadRef" class="flex-1 h-full"></div>
        </div>
      </div>

      <!-- Delivery Cycle Section (5/8 width) -->
      <div class="flex-5/8 border rounded p-2 flex flex-col space-y-2">
        <div class="section-title">{{ t('kanban.cto.deliveryCycle.deliveryCycle') }}</div>
        <div class="flex-1 flex space-x-3 items-center">
          <div class="w-90 pl-4">
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5 text-sm text-slate-600">{{ t('kanban.cto.deliveryCycle.participantCount') }}</span>
              <span class="flex-3/5 font-bold text-4 text-slate-700">{{ leadTimeData.userNum || 0 }}</span>
            </div>
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5 text-sm text-slate-600">{{ t('kanban.cto.deliveryCycle.totalDuration') }}</span>
              <div class="flex-3/5 min-w-0">
                <span class="font-bold text-4 text-slate-700">{{ leadTimeData.totalProcessingTime || 0 }}</span><span class="text-slate-400 text-sm ml-1">{{ t('kanban.cto.deliveryCycle.hours') }}</span>
              </div>
            </div>
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5 text-sm text-slate-600">{{ t('kanban.cto.deliveryCycle.averageDuration') }}</span>
              <div class="flex-3/5 min-w-0">
                <span class="font-bold text-4 text-slate-700">{{ leadTimeData.userAvgProcessingTime || 0 }}</span><span class="text-slate-400 text-sm ml-1">{{ t('kanban.cto.deliveryCycle.hoursPerPerson') }}</span>
              </div>
            </div>
          </div>
          <div
            ref="leadTimeRef"
            class="flex-1 h-full"
            style="width: calc(100% - 240px)">
          </div>
        </div>
      </div>
    </div>

    <!-- Row 4: Unplanned Work and Task Type/Task Status or Test Status/Review Status (3:5 ratio) -->
    <div class="flex space-x-2 h-65">
      <!-- Unplanned Work Section (3/8 width) -->
      <div class="border rounded h-full flex-3/8 p-2">
        <div class="section-title">{{ t('kanban.cto.unplanned.unplannedWorkTitle') }}</div>
        <div class="flex space-x-2 justify-around mt-2">
          <div class="flex-1 pl-15">
            <div><span class="font-bold text-5 text-yellow-600">{{ unplannedWorkData.unplannedNum || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.unplanned.unplannedTaskCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-yellow-600">{{ unplannedWorkData.unplannedWorkloadCompleted || 0 }}</span><span class="text-sm text-slate-400">{{ t('kanban.cto.deliveryCycle.hours') }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.unplanned.estimatedTime') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-bold text-5 text-yellow-600">{{ unplannedWorkData.unplannedWorkload || 0 }}</span></div>
            <div class="text-sm text-slate-500 mt-1">{{ t('kanban.cto.unplanned.unplannedWorkload') }}</div>
          </div>
        </div>
        <div class="flex h-1/2 mt-2">
          <div ref="unplannedTaskNumRef" class="flex-1 h-full"></div>
          <div ref="unplannedWorkloadRef" class="flex-1 h-full"></div>
        </div>
      </div>

      <!-- Task Type/Task Status or Test Status/Review Status Section (5/8 width) -->
      <div class="h-full flex-5/8 space-x-2 flex">
        <!-- Task Type or Test Status (2.5/7 width) -->
        <div class="flex-1/2 border rounded p-2 flex flex-col space-y-2">
          <div class="section-title">{{ props.countType === 'task' ? t('kanban.cto.taskType') : t('kanban.cto.testStatusLable') }}</div>
          <div
            v-show="props.countType === 'task'"
            ref="taskTypeRef"
            class="flex-1"></div>
          <div
            v-show="props.countType === 'useCase'"
            ref="testStatusRef"
            class="flex-1"></div>
        </div>
        <!-- Task Status or Review Status (2.5/7 width) -->
        <div class="flex-1/2 border rounded p-2 flex flex-col space-y-2">
          <div class="section-title">{{ props.countType === 'task' ? t('kanban.cto.taskStatusName') : t('kanban.cto.reviewStatusLable') }}</div>
          <div
            v-show="props.countType === 'task'"
            ref="taskStatusRef"
            class="flex-1"></div>
          <div
            v-show="props.countType === 'useCase'"
            ref="reviewStatusRef"
            class="flex-1"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Risk level color scheme */
.risk-level-LOW {
  color: #f59e0b; /* amber-500 */
}

.risk-level-HIGH {
  color: #dc2626; /* red-600 */
}

.risk-level-NONE {
  color: #10b981; /* emerald-500 */
}

/* Custom flex classes for 3:5 ratio layout */
.flex-3\/8 {
  flex: 0 0 37.5%; /* 3/8 = 37.5% */
}

.flex-5\/8 {
  flex: 0 0 62.5%; /* 5/8 = 62.5% */
}

/* Enhanced typography and spacing */
.text-3\.5 {
  font-size: 0.875rem; /* 14px */
  line-height: 1.25rem; /* 20px */
}

/* Improved section headers */
.section-header {
  color: #1e293b; /* slate-800 */
  font-weight: 600;
  font-size: 0.875rem; /* text-sm */
}

/* Data value styling */
.data-value {
  font-weight: 700;
  color: #334155; /* slate-700 */
}

.data-label {
  font-size: 0.75rem; /* text-xs */
  color: #64748b; /* slate-500 */
  margin-top: 0.25rem; /* mt-1 */
}

/* Chart container improvements */
.chart-container {
  background-color: #f8fafc; /* slate-50 */
  border-radius: 0.375rem; /* rounded */
  border: 1px solid #e2e8f0; /* border-slate-200 */
}

/* Section title styling - consistent with effectiveness page chart headers */
.section-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}
</style>
