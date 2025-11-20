/**
 * <p>
 * Component props interface
 * </p>
 */
export interface CtoProps {
  /** Project identifier */
  projectId: string;
  /** Sprint identifier */
  sprintId: string;
  /** Plan identifier */
  planId: string;
  /** Creator object type (GROUP, USER, or DEPT) */
  creatorObjectType: 'GROUP' | 'USER' | 'DEPT';
  /** Creator object identifier */
  creatorObjectId: string;
  /** Start date for created items filter */
  createdDateStart: string;
  /** End date for created items filter */
  createdDateEnd: string;
  /** Type of items to count (task or useCase) */
  countType: 'task' | 'useCase';
  /** Whether the component is visible */
  onShow: boolean;
}

/**
 * <p>
 * Recent date options for filtering
 * </p>
 */
export interface RecentDateOption {
  /** Option value */
  value: 'today' | 'last7Days' | 'lastMonth';
  /** Display label */
  label: string;
}

/**
 * <p>
 * Total progress overview data
 * </p>
 */
export interface TotalProgressOverview {
  /** Total number of items */
  totalNum: number;
  /** Total completed number */
  totalCompletedNum: number;
  /** Total completed rate percentage */
  totalCompletedRate: number;
  /** Total workload */
  totalWorkload: number;
}

/**
 * <p>
 * Backlogged count data
 * </p>
 */
export interface BackloggedCount {
  /** Backlogged completion time in hours */
  backloggedCompletionTime: number;
  /** Number of backlogged items */
  backloggedNum: number;
  /** Backlogged rate percentage */
  backloggedRate: number;
  /** Backlogged workload */
  backloggedWorkload: number;
  /** Backlogged workload rate percentage */
  backloggedWorkloadRate: number;
  /** Daily processed number */
  dailyProcessedNum: number;
  /** Daily processed workload */
  dailyProcessedWorkload: number;
  /** Processed in day flag */
  processedInDay: boolean;
  /** Total number */
  totalNum: number;
  /** Total workload */
  totalWorkload: number;
}

/**
 * <p>
 * Recent delivery count data
 * </p>
 */
export interface RecentDeliveryCount {
  /** Completed number */
  completedNum: number;
  /** Total number */
  totalNum: number;
  /** Completed rate percentage */
  completedRate: number;
  /** Overdue number */
  OverdueNum: number;
  /** Completed workload */
  completedWorkload: number;
  /** Total workload */
  totalWorkload: number;
  /** Completed workload rate percentage */
  completedWorkloadRate: number;
  /** Saving workload */
  savingWorkload: number;
  /** Saving workload rate percentage */
  savingWorkloadRate: number;
}

/**
 * <p>
 * Overdue assessment count data
 * </p>
 */
export interface OverdueAssessmentCount {
  /** Daily processed workload */
  dailyProcessedWorkload: number;
  /** Overdue number */
  overdueNum: number;
  /** Overdue rate percentage */
  overdueRate: number;
  /** Overdue time */
  overdueTime: number;
  /** Overdue workload */
  overdueWorkload: number;
  /** Overdue workload processing time */
  overdueWorkloadProcessingTime: number;
  /** Overdue workload rate percentage */
  overdueWorkloadRate: number;
  /** Risk level information */
  riskLevel: {
    /** Risk level value */
    value: string;
    /** Risk level message */
    message: string;
  };
  /** Total number */
  totalNum: number;
  /** Total workload */
  totalWorkload: number;
}

/**
 * <p>
 * Unplanned work count data
 * </p>
 */
export interface UnplannedWorkCount {
  /** Total number */
  totalNum: number;
  /** Total workload */
  totalWorkload: number;
  /** Unplanned completed number */
  unplannedCompletedNum: number;
  /** Unplanned completed rate percentage */
  unplannedCompletedRate: number;
  /** Unplanned number */
  unplannedNum: number;
  /** Unplanned rate percentage */
  unplannedRate: number;
  /** Unplanned workload */
  unplannedWorkload: number;
  /** Unplanned workload completed */
  unplannedWorkloadCompleted: number;
  /** Unplanned workload completed rate percentage */
  unplannedWorkloadCompletedRate: number;
  /** Unplanned workload rate percentage */
  unplannedWorkloadRate: number;
}

/**
 * <p>
 * Failure assessment count data
 * </p>
 */
export interface FailureAssessmentCount {
  /** Failure level count breakdown */
  failureLevelCount: {
    /** Critical failures */
    CRITICAL: number;
    /** Major failures */
    MAJOR: number;
    /** Minor failures */
    MINOR: number;
    /** Trivial failures */
    TRIVIAL: number;
  };
  /** Total failure number */
  failureNum: number;
  /** Total number */
  totalNum: number;
  /** Total workload */
  totalWorkload: number;
}

/**
 * <p>
 * APIs test count data
 * </p>
 */
export interface ApisTestCount {
  /** Enabled test API number */
  enabledTestApiNum: number;
  /** Enabled test API rate percentage */
  enabledTestApiRate: number;
  /** Enabled test number */
  enabledTestNum: number;
  /** Passed test API number */
  passedTestApiNum: number;
  /** Passed test number */
  passedTestNum: number;
  /** Test API progress */
  testApiProgress: number;
  /** Test progress */
  testProgress: number;
  /** Total API number */
  totalApiNum: number;
}

/**
 * <p>
 * Scenario test count data
 * </p>
 */
export interface ScenarioTestCount {
  /** Enabled test scenario number */
  enabledTestScenarioNum: number;
  /** Enabled test scenario rate percentage */
  enabledTestScenarioRate: number;
  /** Enabled test number */
  enabledTestNum: number;
  /** Passed test scenario number */
  passedTestScenarioNum: number;
  /** Passed test number */
  passedTestNum: number;
  /** Test scenario progress */
  testScenarioProgress: number;
  /** Test progress */
  testProgress: number;
  /** Total scenario number */
  totalScenarioNum: number;
}

/**
 * <p>
 * Total type count data
 * </p>
 */
export interface TotalTypeCount {
  /** Story count */
  STORY: number;
  /** Requirement count */
  REQUIREMENT: number;
  /** Task count */
  TASK: number;
  /** Bug count */
  BUG: number;
  /** Design count */
  DESIGN: number;
}

/**
 * <p>
 * Total status count data
 * </p>
 */
export interface TotalStatusCount {
  /** Pending count */
  PENDING: number;
  /** In progress count */
  IN_PROGRESS: number;
  /** Confirming count */
  CONFIRMING: number;
  /** Completed count */
  COMPLETED: number;
  /** Canceled count */
  CANCELED: number;
}

/**
 * <p>
 * Lead time count data
 * </p>
 */
export interface LeadTimeCount {
  /** Average processing time */
  avgProcessingTime: number;
  /** Maximum processing time */
  maxProcessingTime: number;
  /** Minimum processing time */
  minProcessingTime: number;
  /** P50 processing time */
  p50ProcessingTime: number;
  /** P75 processing time */
  p75ProcessingTime: number;
  /** P90 processing time */
  p90ProcessingTime: number;
  /** P95 processing time */
  p95ProcessingTime: number;
  /** P99 processing time */
  p99ProcessingTime: number;
  /** User number */
  userNum: number;
  /** Total processing time */
  totalProcessingTime: number;
  /** User average processing time */
  userAvgProcessingTime: number;
}

/**
 * <p>
 * Total test result count data
 * </p>
 */
export interface TotalTestResultCount {
  /** Pending count */
  PENDING: number;
  /** Passed count */
  PASSED: number;
  /** Not passed count */
  NOT_PASSED: number;
  /** Blocked count */
  BLOCKED: number;
  /** Canceled count */
  CANCELED: number;
}

/**
 * <p>
 * Total review status count data
 * </p>
 */
export interface TotalReviewStatusCount {
  /** Pending count */
  PENDING: number;
  /** Passed count */
  PASSED: number;
  /** Failed count */
  FAILED: number;
}

/**
 * <p>
 * CTO data response structure
 * </p>
 */
export interface CtoData {
  /** Member number */
  memberNum: number;
  /** Total progress overview */
  totalProgressOverview: TotalProgressOverview;
  /** Backlogged count */
  backloggedCount: BackloggedCount;
  /** Recent delivery count */
  recentDeliveryCount: RecentDeliveryCount;
  /** Overdue assessment count */
  overdueAssessmentCount: OverdueAssessmentCount;
  /** Unplanned work count */
  unplannedWorkCount: UnplannedWorkCount;
  /** Failure assessment count */
  failureAssessmentCount: FailureAssessmentCount;
  /** APIs test count */
  apisTestCount: ApisTestCount;
  /** Scenario test count */
  scenarioTestCount: ScenarioTestCount;
  /** Total type count */
  totalTypeCount: TotalTypeCount;
  /** Total status count */
  totalStatusCount: TotalStatusCount;
  /** Lead time count */
  leadTimeCount: LeadTimeCount;
  /** Total test result count */
  totalTestResultCount: TotalTestResultCount;
  /** Total review status count */
  totalReviewStatusCount: TotalReviewStatusCount;
}

/**
 * <p>
 * Chart configuration interface
 * </p>
 */
export interface ChartConfig {
  /** Chart title configuration */
  title: {
    text: string;
    left?: string;
    top?: string;
    padding?: number;
    textAlign?: string;
    textStyle?: {
      fontSize: number;
      fontWeight: string;
    };
    subtext?: string;
    subtextStyle?: {
      fontSize: number;
      color: string;
    };
    itemGap?: number;
  };
  /** Chart tooltip configuration */
  tooltip: {
    trigger: string;
    show?: boolean;
  };
  /** Chart legend configuration */
  legend: {
    top?: string;
    left?: string;
    right?: string;
    orient: string;
    font?: string;
    itemHeight: number;
    itemWidth: number;
    itemGap: number;
    textStyle?: {
      fontSize: number;
    };
  };
  /** Chart series configuration */
  series: Array<{
    name: string;
    type: string;
    radius: string[];
    center: string[];
    avoidLabelOverlap: boolean;
    label: {
      show: boolean;
      formatter: string;
      fontSize?: number;
      position?: string;
    };
    itemStyle: {
      borderRadius: number;
      borderColor: string;
      borderWidth: number;
      color?: string;
    };
    emphasis: {
      label: {
        show: boolean;
      };
    };
    labelLine: {
      show: boolean;
      length: number;
      length2?: number;
    };
    data: Array<{
      name: string;
      value: number;
      itemStyle: {
        color: string;
      };
    }>;
  }>;
  /** Chart grid configuration */
  grid?: {
    left: string;
    right: string;
    bottom: string;
    top: string;
  };
  /** Chart x-axis configuration */
  xAxis?: {
    type: string;
    data: string[];
    axisLabel: {
      interval: number;
      overflow: string;
    };
  };
  /** Chart y-axis configuration */
  yAxis?: {
    type: string;
  };
  /** Chart graphic configuration */
  graphic?: {
    type: string;
    left: string;
    bottom: number;
    z: number;
    font: string;
    silent: boolean;
    invisible: boolean;
    style: {
      text: string;
    };
  };
}
