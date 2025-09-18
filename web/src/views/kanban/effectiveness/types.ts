/**
 * <p>
 * Component props interface
 * </p>
 */
export interface EffectivenessProps {
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
 * Overview configuration item interface
 * </p>
 */
export interface OverviewConfigItem {
  /** Display name */
  name: string;
  /** Data field index */
  dataIndex: string;
  /** Icon identifier */
  icon: string;
  /** Unit for display (optional) */
  unit?: string;
  /**
   * Semantic hint for value coloring
   * rateHighGood: percentage where higher is better (e.g., >=90 green, >=60 orange, else red)
   * countHighGood: count where higher presence is okay/neutral (blue when >0)
   * countLowGood: count where lower is better (e.g., overdue, canceled)
   * workloadSaving: saving workload (green when >0)
   * neutral: no special coloring
   */
  semantic?: 'rateHighGood' | 'countHighGood' | 'countLowGood' | 'workloadSaving' | 'neutral';
}

/**
 * <p>
 * Overview configuration structure
 * </p>
 */
export type OverviewConfig = OverviewConfigItem[][];

/**
 * <p>
 * Burn down chart option interface
 * </p>
 */
export interface BurnDownOption {
  /** Option value */
  value: 'NUM' | 'WORKLOAD';
  /** Display label */
  label: string;
}

/**
 * <p>
 * Time series data point interface
 * </p>
 */
export interface TimeSeriesPoint {
  /** Time series identifier */
  timeSeries: string;
  /** Data value */
  value: number;
}

/**
 * <p>
 * Burn down chart data structure
 * </p>
 */
export interface BurnDownData {
  /** Expected values over time */
  expected: TimeSeriesPoint[];
  /** Remaining values over time */
  remaining: TimeSeriesPoint[];
}

/**
 * <p>
 * Burn down chart data by type
 * </p>
 */
export interface BurnDownDataByType {
  /** Task count or use case count data */
  NUM: BurnDownData;
  /** Workload data */
  WORKLOAD: BurnDownData;
}

/**
 * <p>
 * Ranking data item interface
 * </p>
 */
export interface RankingItem {
  /** Assignee identifier */
  assigneeId: string;
  /** Score value */
  score: number;
}

/**
 * <p>
 * Ranking data structure
 * </p>
 */
export interface RankingData {
  /** Valid task/case number ranking */
  validTaskNumRank?: RankingItem[];
  /** Valid case number ranking */
  validCaseNumRank?: RankingItem[];
  /** Completed number ranking */
  completedNumRank?: RankingItem[];
  /** Passed test number ranking */
  passedTestNumRank?: RankingItem[];
  /** Completed workload ranking */
  completedWorkloadRank?: RankingItem[];
  /** Actual workload ranking */
  actualWorkloadRank?: RankingItem[];
  /** Overdue number ranking */
  overdueNumRank?: RankingItem[];
  /** One time passed number ranking */
  oneTimePassedNumRank?: RankingItem[];
  /** One time passed test number ranking */
  oneTimePassedTestNumRank?: RankingItem[];
  /** Valid bug number ranking */
  validBugNumRank?: RankingItem[];
  /** One time passed review number ranking */
  oneTimePassedReviewNumRank?: RankingItem[];
  /** Completed rate ranking */
  completedRateRank?: RankingItem[];
  /** Passed test rate ranking */
  passedTestRateRank?: RankingItem[];
  /** Saving workload ranking */
  savingWorkloadRank?: RankingItem[];
  /** Overdue rate ranking */
  overdueRateRank?: RankingItem[];
  /** One time passed rate ranking */
  oneTimePassedRateRank?: RankingItem[];
  /** One time passed test rate ranking */
  oneTimePassedTestRateRank?: RankingItem[];
  /** Valid bug rate ranking */
  validBugRateRank?: RankingItem[];
  /** One time passed review rate ranking */
  oneTimePassedReviewRateRank?: RankingItem[];
}

/**
 * <p>
 * Assignee information interface
 * </p>
 */
export interface AssigneeInfo {
  /** Full name */
  fullName: string;
  /** Avatar URL */
  avatar?: string;
}

/**
 * <p>
 * Overview data structure
 * </p>
 */
export interface OverviewData {
  /** Total task number */
  totalTaskNum?: number;
  /** Total case number */
  totalCaseNum?: number;
  /** Progress percentage */
  progress?: number;
  /** Pending number */
  pendingNum?: number;
  /** In progress number */
  inProgressNum?: number;
  /** Confirming number */
  confirmingNum?: number;
  /** Canceled number */
  canceledNum?: number;
  /** Completed number */
  completedNum?: number;
  /** Overdue number */
  overdueNum?: number;
  /** One time passed rate */
  oneTimePassedRate?: number;
  /** Evaluation workload */
  evalWorkload?: number;
  /** Actual workload */
  actualWorkload?: number;
  /** Saving workload */
  savingWorkload?: number;
  /** Pending test number */
  pendingTestNum?: number;
  /** Passed test number */
  passedTestNum?: number;
  /** Not passed test number */
  notPassedTestNum?: number;
  /** Blocked test number */
  blockedTestNum?: number;
  /** Test case hit rate */
  testCaseHitRate?: number;
  /** One time passed test rate */
  oneTimePassedTestRate?: number;
  /** One time passed review rate */
  oneTimePassReviewRate?: number;
}

/**
 * <p>
 * Total type count data structure
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
  /** API test count */
  API_TEST: number;
  /** Scenario test count */
  SCENARIO_TEST: number;
}

/**
 * <p>
 * Effectiveness data response structure
 * </p>
 */
export interface EffectivenessData {
  /** Overview data */
  totalOverview: OverviewData;
  /** Burn down chart data */
  burnDownCharts: BurnDownDataByType;
  /** Total type count */
  totalTypeCount: TotalTypeCount;
  /** Assignee ranking data */
  assigneeRanking?: RankingData;
  /** Tester ranking data */
  testerRanking?: RankingData;
  /** Assignee information */
  assignees?: Record<string, AssigneeInfo>;
  /** Tester information */
  testers?: Record<string, AssigneeInfo>;
}

/**
 * <p>
 * Chart configuration interface
 * </p>
 */
export interface ChartConfig {
  /** Chart title configuration */
  title?: {
    text?: string;
    subtext?: string;
    left?: string;
    top?: string;
    padding?: number;
    textAlign?: string;
    textStyle?: {
      fontSize: number;
      width?: number;
      fontWeight?: string;
    };
    subtextStyle?: {
      fontSize: number;
      color?: string;
    };
  };
  /** Chart tooltip configuration */
  tooltip: {
    trigger: string;
    show?: boolean;
    axisPointer?: {
      type: string;
    };
    valueFormatter?: (value: number) => string;
  };
  /** Chart legend configuration */
  legend?: {
    show?: boolean;
    bottom?: string | number;
    left?: string;
    top?: string;
    orient?: string;
    itemHeight?: number;
    itemWidth?: number;
    itemGap?: number;
    type?: string;
  };
  /** Chart grid configuration */
  grid: {
    left: string | number;
    right?: string | number;
    bottom: string | number;
    top: string | number;
  };
  /** Chart x-axis configuration */
  xAxis: {
    type: string;
    data?: any[];
    axisLabel?: {
      formatter?: string;
      interval?: number;
      overflow?: string;
    };
  };
  /** Chart y-axis configuration */
  yAxis: {
    type: string;
    data?: any[];
    axisLine?: {
      show: boolean;
    };
    axisTick?: {
      show: boolean;
    };
  };
  /** Chart series configuration */
  series: Array<{
    name: string;
    type: string;
    data: any[];
    itemStyle?: {
      color: string;
      borderRadius?: number[];
    };
    barMaxWidth?: string | number;
    barMinWidth?: string | number;
    barGap?: string | number;
    smooth?: boolean;
  }>;
  /** Chart toolbox configuration */
  toolbox?: {
    show: boolean;
  };
  /** Chart graphic configuration */
  graphic?: {
    type: string;
    left: string;
    top: string;
    z: number;
    silent: boolean;
    invisible: boolean;
    style: {
      width?: number;
      image?: string;
    };
  };
}
