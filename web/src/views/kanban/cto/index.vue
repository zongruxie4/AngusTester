<script lang="ts" setup>

import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';
import { RadioGroup } from 'ant-design-vue';
import { CtoProps, RecentDateOption } from './types';
import { useCtoData } from './composables/useCtoData';
import { useChartManagement } from './composables/useChartManagement';
import { useCtoLifecycle } from './composables/useCtoLifecycle';

const { t } = useI18n();

/**
 * <p>
 * Component props with default values
 * </p>
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
 * <p>
 * Recent date options for filtering metrics
 * </p>
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
 * <p>
 * Selected recent date for filtering
 * </p>
 */
const selectedRecentDate = ref('today');

/**
 * <p>
 * Chart container references
 * </p>
 */
const progressRef = ref<HTMLElement>();
const recentCompleteRef = ref<HTMLElement>();
const recentOverdueRef = ref<HTMLElement>();
const recentCompletedWorkloadRef = ref<HTMLElement>();
const recentSavingRateRef = ref<HTMLElement>();
const blockTaskRef = ref<HTMLElement>();
const blockTaskWorkloadRef = ref<HTMLElement>();
const overdueTaskRef = ref<HTMLElement>();
const unplanTaskNumRef = ref<HTMLElement>();
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
 * <p>
 * Initialize composables
 * </p>
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
} = useCtoLifecycle(props, loadCtoData, resizeAllCharts, resizeChartsByType);

/**
 * <p>
 * Handles recent date change and updates related charts
 * </p>
 * <p>
 * Updates all recent delivery metrics charts based on selected time period
 * </p>
 */
const handleRecentDateChange = () => {
  if (recentDeliveryData.value && selectedRecentDate.value) {
    updateRecentDateCharts(selectedRecentDate.value, recentDeliveryData.value);
  }
};

/**
 * <p>
 * Loads dashboard data and updates charts
 * </p>
 * <p>
 * Fetches data from API and updates all chart visualizations
 * </p>
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

/**
 * <p>
 * Initializes component on mount
 * </p>
 * <p>
 * Sets up lifecycle management and initializes charts
 * </p>
 */
onMounted(async () => {
  // Setup lifecycle management
  setupLifecycle();
  
  // Initialize charts with DOM references
  const chartRefs = {
    progressRef: progressRef.value,
    recentCompleteRef: recentCompleteRef.value,
    recentOverdueRef: recentOverdueRef.value,
    recentCompletedWorkloadRef: recentCompletedWorkloadRef.value,
    recentSavingRateRef: recentSavingRateRef.value,
    blockTaskRef: blockTaskRef.value,
    blockTaskWorkloadRef: blockTaskWorkloadRef.value,
    overdueTaskRef: overdueTaskRef.value,
    unplanTaskNumRef: unplanTaskNumRef.value,
    unplannedWorkloadRef: unplannedWorkloadRef.value,
    taskTypeRef: taskTypeRef.value,
    taskStatusRef: taskStatusRef.value,
    leadTimeRef: leadTimeRef.value,
    criticalFailureRef: criticalFailureRef.value,
    majorFailureRef: majorFailureRef.value,
    minorFailureRef: minorFailureRef.value,
    trivialFailureRef: trivialFailureRef.value,
    apiOpenTestRef: apiOpenTestRef.value,
    apiSuccessTestRef: apiSuccessTestRef.value,
    scenarioOpenTestRef: scenarioOpenTestRef.value,
    scenarioSuccessTestRef: scenarioSuccessTestRef.value,
    testStatusRef: testStatusRef.value,
    reviewStatusRef: reviewStatusRef.value
  };
  
  initializeCharts(chartRefs);
});

/**
 * <p>
 * Cleans up component on unmount
 * </p>
 * <p>
 * Removes event listeners and resets state
 * </p>
 */
onBeforeUnmount(() => {
  cleanup();
  resetData();
});

/**
 * <p>
 * Exposes methods for parent component access
 * </p>
 */
defineExpose({
  refresh,
  handleWindowResize
});
</script>

<template>
  <div class="py-2 text-3 space-y-2">
    <!-- Total Progress and Backlog Section -->
    <div class="flex space-x-2 h-65">
      <!-- Total Progress -->
      <div class="rounded h-full flex-1/2 flex space-x-2">
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">总进度{{ t('kanban.cto.progress.totalProgress') }}</div>
          <div ref="progressRef" class="h-1/2 w-2/3"></div>
          <div class="flex justify-around mt-3">
            <div class="text-center">
              <div class="font-semibold text-5">{{ progressData.totalNum || 0 }}</div>
              <div>
                {{ props.countType === 'task' ? t('kanban.cto.progress.totalTaskCount' ) : t('kanban.cto.progress.totalUseCaseCount') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ progressData.totalWorkload || 0 }}</div>
              <div>
                {{ t('kanban.cto.progress.totalWorkload') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ memberNum || 0 }}</div>
              <div>
                {{ t('kanban.cto.progress.teamMember') }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- Backlog Section -->
        <div class="flex-1/2 border rounded p-2 space-y-2">
          <div class="font-semibold">{{ props.countType === 'task' ? t('kanban.cto.backlog.backlogTask') : t('kanban.cto.backlog.backlogUseCase') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ backloggedData.backloggedNum || 0 }}</span></div>
              <div>{{ props.countType === 'task' ? t('kanban.cto.backlog.backlogTaskCount') : t('kanban.cto.backlog.backlogUseCaseCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ backloggedData.backloggedCompletionTime || 0 }}</span>小时{{ t('kanban.cto.deliveryCycle.hours') }}</div>
              <div>{{ t('kanban.cto.backlog.estimatedTime') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ backloggedData.backloggedWorkload || 0 }}</span></div>
              <div>{{ t('kanban.cto.backlog.backlogWorkload') }}</div>
            </div>
          </div>
          <div class="flex h-1/2 mt-2">
            <div ref="blockTaskRef" class="flex-1 h-full"></div>
            <div ref="blockTaskWorkloadRef" class="flex-1 h-full"></div>
          </div>
        </div>
      </div>
      
      <!-- Recent Delivery Section -->
      <div class="border rounded h-full flex-1/2  p-2 space-y-2">
        <div class="flex justify-between">
          <div class="font-semibold ">{{ t('kanban.cto.recentDelivery.recentDelivery') }}</div>
          <RadioGroup
            v-model:value="selectedRecentDate"
            optionType="button"
            buttonStyle="solid"
            size="small"
            :options="recentDateOptions"
            @change="handleRecentDateChange" />
        </div>
        <div class="flex space-x-2 justify-around">
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryData?.[selectedRecentDate]?.completedNum || 0 }}</span>/{{ recentDeliveryData?.[selectedRecentDate]?.totalNum || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryTaskCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryData?.[selectedRecentDate]?.OverdueNum || 0 }}</span>/{{ recentDeliveryData?.[selectedRecentDate]?.totalNum || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryTaskOverdueCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryData?.[selectedRecentDate]?.completedWorkload || 0 }}</span>/{{ recentDeliveryData?.[selectedRecentDate]?.totalWorkload || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryWorkload') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ recentDeliveryData?.[selectedRecentDate]?.savingWorkload || 0 }}</span>/{{ recentDeliveryData?.[selectedRecentDate]?.totalWorkload || 0 }}</div>
            <div>{{ t('kanban.cto.recentDelivery.deliveryWorkloadSaving') }}</div>
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

    <!-- Overdue Assessment and Unplanned Work Section -->
    <div class="flex space-x-2 h-65">
      <div class="rounded h-full flex-1/2 flex space-x-2">
        <!-- Overdue Assessment -->
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.overdue.overdueAssessment') }}</div>
          <div class="flex h-1/2 items-center">
            <div ref="overdueTaskRef" class="h-full w-2/3"></div>
            <div class="text-center flex-1">
              <div :class="`risk-level-${overdueAssessmentData?.riskLevel?.value}`" class="font-semibold text-5">{{ overdueAssessmentData?.riskLevel?.message }}</div>
              <div>{{ t('kanban.cto.overdue.overdueRisk') }}</div>
            </div>
          </div>

          <div class="flex justify-around mt-3">
            <div class="text-center">
              <div class="font-semibold text-5">{{ overdueAssessmentData.overdueNum || 0 }}</div>
              <div>
                {{ t('kanban.cto.overdue.overdueCount') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ overdueAssessmentData.overdueWorkload || 0 }}</div>
              <div>
                {{ t('kanban.cto.overdue.overdueWorkload') }}
              </div>
            </div>

            <div class="text-center">
              <div class="font-semibold text-5">{{ overdueAssessmentData.overdueWorkloadProcessingTime || 0 }}</div>
              <div>
                {{ t('kanban.cto.overdue.overdueWorkloadProcessingTime') }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- Unplanned Work -->
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.unplanned.unplannedWork') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ unplannedWorkData.unplannedNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.unplanned.unplannedTaskCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ unplannedWorkData.unplannedWorkloadProcessingTime || 0 }}</span>小时</div>
              <div>{{ t('kanban.cto.unplanned.estimatedTime') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ unplannedWorkData.unplannedWorkload || 0 }}</span></div>
              <div>{{ t('kanban.cto.unplanned.unplannedWorkload') }}</div>
            </div>
          </div>
          <div class="flex h-1/2 mt-2">
            <div ref="unplanTaskNumRef" class="flex-1 h-full"></div>
            <div ref="unplannedWorkloadRef" class="flex-1 h-full"></div>
          </div>
        </div>
      </div>
      
      <!-- Failure Assessment (Task) or API/Scenario Test (Use Case) -->
      <div v-show="props.countType === 'task'" class="border rounded h-full flex-1/2  p-2 space-y-2">
        <div class="flex justify-between">
          <div class="font-semibold ">{{ t('kanban.cto.failureAssessment.failureAssessment ') }}</div>
        </div>
        <div class="flex space-x-2 justify-around">
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureNum || 0 }}</span>/{{ failureAssessmentData?.totalNum || 0 }}</div>
            <div>{{ t('kanban.cto.failureAssessment.failureCount') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureWorkload || 0 }}</span>/{{ failureAssessmentData?.totalWorkload || 0 }}</div>
            <div>{{ t('kanban.cto.failureAssessment.failureWorkload') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureCompletedNum || 0 }}</span>/{{ failureAssessmentData?.failureNum || 0 }}</div>
            <div>{{ t('kanban.cto.failureAssessment.failureCompletedNum') }}</div>
          </div>
          <div class="flex-1 pl-5">
            <div><span class="font-semibold text-6">{{ failureAssessmentData?.failureOverdueNum || 0 }}</span>/{{ failureAssessmentData?.failureNum || 0 }}</div>
            <div>{{ t('kanban.cto.failureAssessment.failureOverdueNum') }}</div>
          </div>
        </div>
        <div class="flex space-x-2 h-1/2 mt-2">
          <div ref="criticalFailureRef" class="h-full flex-1"></div>
          <div ref="majorFailureRef" class="h-full flex-1"></div>
          <div ref="minorFailureRef" class="h-full flex-1"></div>
          <div ref="trivialFailureRef" class="h-full flex-1"></div>
        </div>
      </div>
      
      <!-- API and Scenario Test (Use Case) -->
      <div v-show="props.countType === 'useCase'" class="h-full flex-1/2 space-x-2 flex">
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.apiTest.apiTest') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ apiTestData.enabledTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.enabledTestCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ apiTestData.passedTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.passedTestCount') }}</div>
            </div>
          </div>
          <div class="flex space-x-2 justify-around mt-2 h-1/2">
            <div ref="apiOpenTestRef" class="flex-1 h-full"></div>
            <div ref="apiSuccessTestRef" class="flex-1 h-full"></div>
          </div>
        </div>
        <div class="flex-1/2 border rounded p-2">
          <div class="font-semibold">{{ t('kanban.cto.scenarioTest.scenarioTest') }}</div>
          <div class="flex space-x-2 justify-around mt-2">
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ scenarioTestData.enabledTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.enabledTestCount') }}</div>
            </div>
            <div class="flex-1 pl-5">
              <div><span class="font-semibold text-6">{{ scenarioTestData.passedTestNum || 0 }}</span></div>
              <div>{{ t('kanban.cto.apiTest.passedTestCount') }}</div>
            </div>
          </div>
          <div class="flex space-x-2 justify-around mt-2 h-1/2">
            <div ref="scenarioOpenTestRef" class="flex-1 h-full"></div>
            <div ref="scenarioSuccessTestRef" class="flex-1 h-full"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Task Types/Test Status and Delivery Cycle Section -->
    <div class="flex space-x-2 h-50">
      <div class="rounded h-full flex-1/2 flex space-x-2">
        <div class="flex-1/2 border rounded p-2 flex flex-col space-y-2">
          <div class="font-semibold">{{ props.countType === 'task' ? t('kanban.cto.taskType') : t('kanban.cto.testStatusLable') }}</div>
          <div
            v-show="props.countType === 'task'"
            ref="taskTypeRef"
            class="flex-1"></div>
          <div
            v-show="props.countType === 'useCase'"
            ref="testStatusRef"
            class="flex-1"></div>
        </div>
        <div class="flex-1/2  border rounded p-2 flex flex-col space-y-2">
          <div class="font-semibold">{{ props.countType === 'task' ? t('kanban.cto.taskStatusName') : t('kanban.cto.reviewStatusLable') }}</div>
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
      <div class="flex-1/2 border rounded p-2 flex flex-col space-y-2">
        <div class="font-semibold">{{ t('kanban.cto.deliveryCycle.deliveryCycle') }}</div>
        <div class="flex-1 flex space-x-3 items-center">
          <div class="w-60 pl-4">
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5">{{ t('kanban.cto.deliveryCycle.participantCount') }}</span>
              <span class="flex-3/5 font-semibold text-5">{{ leadTimeData.userNum || 0 }}</span>
            </div>
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5">{{ t('kanban.cto.deliveryCycle.totalDuration') }}</span>
              <div class="flex-3/5 min-w-0">
                <span class="font-semibold text-5">{{ leadTimeData.totalProcessingTime || 0 }} </span> {{ t('kanban.cto.deliveryCycle.hours') }}
              </div>
            </div>
            <div class="flex space-x-2 items-center">
              <span class="flex-2/5">{{ t('kanban.cto.deliveryCycle.averageDuration') }}</span>
              <div class="flex-3/5 min-w-0">
                <span class=" font-semibold text-5">{{ leadTimeData.userAvgProcessingTime || 0 }} </span> {{ t('kanban.cto.deliveryCycle.hoursPerPerson') }}
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
  </div>
</template>

<style scoped>
.risk-level-LOW {
  color: 'gold'
}

.risk-level-HIGH {
  color: 'red'
}

.risk-level-NONE {
  color: '#52C41A'
}
</style>
