import { computed, ref } from 'vue';
import { kanban } from '@/api/tester';
import {
  ApisTestCount,
  BackloggedCount,
  CtoProps,
  FailureAssessmentCount,
  LeadTimeCount,
  OverdueAssessmentCount,
  RecentDeliveryCount,
  ScenarioTestCount,
  TotalProgressOverview,
  TotalReviewStatusCount,
  TotalStatusCount,
  TotalTestResultCount,
  TotalTypeCount,
  UnplannedWorkCount
} from '../types';

/**
 * CTO data management composable
 *
 * @param props - Component props containing filter parameters
 * @returns Object containing reactive data and methods
 */
export function useCtoData (props: CtoProps) {
  /** Member count for the project */
  const memberNum = ref(0);

  /** Total progress overview data */
  const progressData = ref<TotalProgressOverview>({} as TotalProgressOverview);

  /** Backlogged count data */
  const backloggedData = ref<BackloggedCount>({} as BackloggedCount);

  /** Recent delivery count data */
  const recentDeliveryData = ref<RecentDeliveryCount>({} as RecentDeliveryCount);

  /** Overdue assessment count data */
  const overdueAssessmentData = ref<OverdueAssessmentCount>({} as OverdueAssessmentCount);

  /** Unplanned work count data */
  const unplannedWorkData = ref<UnplannedWorkCount>({} as UnplannedWorkCount);

  /** Failure assessment count data */
  const failureAssessmentData = ref<FailureAssessmentCount>({} as FailureAssessmentCount);

  /** APIs test count data */
  const apiTestData = ref<ApisTestCount>({} as ApisTestCount);

  /** Scenario test count data */
  const scenarioTestData = ref<ScenarioTestCount>({} as ScenarioTestCount);

  /** Total type count data */
  const totalTypeData = ref<TotalTypeCount>({} as TotalTypeCount);

  /** Total status count data */
  const totalStatusData = ref<TotalStatusCount>({} as TotalStatusCount);

  /** Lead time count data */
  const leadTimeData = ref<LeadTimeCount>({} as LeadTimeCount);

  /** Total test result count data */
  const totalTestResultData = ref<TotalTestResultCount>({} as TotalTestResultCount);

  /** Total review status count data */
  const totalReviewStatusData = ref<TotalReviewStatusCount>({} as TotalReviewStatusCount);

  /**
   * Computed parameters for API calls
   * <p>
   * Adjusts planId based on countType - uses sprintId for tasks and planId for use cases
   * </p>
   */
  const apiParams = computed(() => {
    const { countType, planId, sprintId, onShow, ...other } = props;
    if (countType === 'task') {
      return {
        ...other,
        planId: sprintId
      };
    }
    return {
      ...other,
      planId
    };
  });

  /**
   * Loads CTO dashboard data from API
   * <p>
   * Fetches data based on countType and updates all reactive data properties
   * </p>
   */
  const loadCtoData = async () => {
    const [error, { data = {} }] = await (
      props.countType === 'task'
        ? kanban.getTaskCto({ ...apiParams.value })
        : kanban.getTestingCto({ ...apiParams.value })
    );

    if (error) {
      return;
    }

    // Update member count
    memberNum.value = data.memberNum || 0;

    // Update total progress overview
    const totalProgressOverview = data.totalProgressOverview || {};
    if (totalProgressOverview) {
      progressData.value = totalProgressOverview;
    }

    // Update backlogged count
    const backloggedCount = data.backloggedCount || {};
    if (backloggedCount) {
      backloggedData.value = backloggedCount;
    }

    // Update recent delivery count
    const recentDeliveryCount = data.recentDeliveryCount || {};
    if (recentDeliveryCount) {
      recentDeliveryData.value = recentDeliveryCount;
    }

    // Update overdue assessment count
    const overdueAssessmentCount = data.overdueAssessmentCount || {};
    if (overdueAssessmentCount) {
      overdueAssessmentData.value = overdueAssessmentCount;
    }

    // Update unplanned work count
    const unplannedWorkCount = data.unplannedWorkCount || {};
    if (unplannedWorkCount) {
      unplannedWorkData.value = unplannedWorkCount;
    }

    // Update failure assessment count (only for tasks)
    if (props.countType === 'task') {
      const failureAssessmentCount = data.failureAssessmentCount || {};
      if (failureAssessmentCount) {
        failureAssessmentData.value = failureAssessmentCount;
      }
    }

    // Update APIs test count (only for use cases)
    if (props.countType === 'useCase') {
      const apisTestCount = data.apisTestCount || {};
      if (apisTestCount) {
        apiTestData.value = apisTestCount;
      }
    }

    // Update scenario test count (only for use cases)
    if (props.countType === 'useCase') {
      const scenarioTestCount = data.scenarioTestCount || {};
      if (scenarioTestCount) {
        scenarioTestData.value = scenarioTestCount;
      }
    }

    // Update total type count
    const totalTypeCount = data.totalTypeCount || {};
    if (totalTypeCount) {
      totalTypeData.value = totalTypeCount;
    }

    // Update total status count
    const totalStatusCount = data.totalStatusCount || {};
    if (totalStatusCount) {
      totalStatusData.value = totalStatusCount;
    }

    // Update lead time count
    const leadTimeCount = data.leadTimeCount || {};
    if (leadTimeCount) {
      leadTimeData.value = leadTimeCount;
    }

    // Update total test result count (only for use cases)
    if (props.countType === 'useCase') {
      const totalTestResultCount = data.totalTestResultCount || {};
      if (totalTestResultCount) {
        totalTestResultData.value = totalTestResultCount;
      }
    }

    // Update total review status count (only for use cases)
    if (props.countType === 'useCase') {
      const totalReviewStatusCount = data.totalReviewStatusCount || {};
      if (totalReviewStatusCount) {
        totalReviewStatusData.value = totalReviewStatusCount;
      }
    }
  };

  /**
   * Resets all data to initial state
   */
  const resetData = () => {
    memberNum.value = 0;
    progressData.value = {} as TotalProgressOverview;
    backloggedData.value = {} as BackloggedCount;
    recentDeliveryData.value = {} as RecentDeliveryCount;
    overdueAssessmentData.value = {} as OverdueAssessmentCount;
    unplannedWorkData.value = {} as UnplannedWorkCount;
    failureAssessmentData.value = {} as FailureAssessmentCount;
    apiTestData.value = {} as ApisTestCount;
    scenarioTestData.value = {} as ScenarioTestCount;
    totalTypeData.value = {} as TotalTypeCount;
    totalStatusData.value = {} as TotalStatusCount;
    leadTimeData.value = {} as LeadTimeCount;
    totalTestResultData.value = {} as TotalTestResultCount;
    totalReviewStatusData.value = {} as TotalReviewStatusCount;
  };

  return {
    // Reactive data
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

    // Computed properties
    apiParams,

    // Methods
    loadCtoData,
    resetData
  };
}
