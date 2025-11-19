/**
 * <p>
 * Component props interface
 * </p>
 */
export interface DataAssetsProps {
  /** Project identifier */
  projectId: string;
  /** Creator object type (GROUP, USER, or DEPT) */
  creatorObjectType: 'GROUP' | 'USER' | 'DEPT';
  /** Creator object identifier */
  creatorObjectId: string;
  /** Start date for created items filter */
  createdDateStart: string;
  /** End date for created items filter */
  createdDateEnd: string;
  /** Whether the component is visible */
  onShow: boolean;
}

/**
 * <p>
 * Project type show map interface
 * </p>
 */
export interface ProTypeShowMap {
  /** Whether to show task-related data */
  showTask: boolean;
  /** Whether to show sprint-related data */
  showSprint: boolean;
}

/**
 * <p>
 * Ranking data item interface
 * </p>
 */
export interface RankingItem {
  /** User identifier */
  userId: string;
  /** User's full name */
  fullName: string;
  /** User's avatar URL */
  avatar: string;
  /** Count value */
  count: string;
}

/**
 * <p>
 * Growth trend data point interface
 * </p>
 */
export interface GrowthTrendPoint {
  /** Time series identifier */
  timeSeries: string;
  /** Data value */
  value: number;
}

/**
 * <p>
 * Growth trend data structure
 * </p>
 */
export interface GrowthTrendData {
  [key: string]: GrowthTrendPoint[];
}

/**
 * <p>
 * Case data structure
 * </p>
 */
export interface CaseData {
  /** Total case count */
  allCase?: number;
  /** Cases by test result */
  caseByTestResult?: {
    PENDING?: number;
    PASSED?: number;
    NOT_PASSED?: number;
    BLOCKED?: number;
    CANCELED?: number;
  };
  /** Cases by review status */
  caseByReviewStatus?: {
    PEDING?: number;
    PASSED?: number;
    FAILED?: number;
  };
  /** Total plan count */
  allPlan?: number;
  /** Plans by status */
  planByStatus?: {
    PENDING?: number;
    IN_PROGRESS?: number;
    COMPLETED?: number;
    BLOCKED?: number;
  };
}

/**
 * <p>
 * API data structure
 * </p>
 */
export interface ApiData {
  /** Total API count */
  allApis?: number;
  /** APIs by HTTP method */
  apisByMethod?: {
    PUT?: number;
    POST?: number;
    DELETE?: number;
    GET?: number;
    HEAD?: number;
    PATCH?: number;
    OPTIONS?: number;
    TRACE?: number;
  };
  /** APIs by status */
  apisByStatus?: {
    UNKNOWN?: number;
    IN_DESIGN?: number;
    IN_DEV?: number;
    DEV_COMPLETED?: number;
    RELEASED?: number;
  };
}

/**
 * <p>
 * Task data structure
 * </p>
 */
export interface TaskData {
  /** Total task count */
  allTask?: number;
  /** Tasks by type */
  taskByType?: {
    STORY?: number;
    TASK?: number;
    BUG?: number;
    DESIGN?: number;
    REQUIREMENT?: number;
  };
  /** Tasks by status */
  taskByStatus?: {
    PENDING?: number;
    IN_PROGRESS?: number;
    CONFIRMING?: number;
    COMPLETED?: number;
    CANCELED?: number;
  };
  /** Total sprint count */
  allSprint?: number;
  /** Sprints by status */
  sprintByStatus?: {
    PENDING?: number;
    IN_PROGRESS?: number;
    COMPLETED?: number;
    BLOCKED?: number;
  };
}

/**
 * <p>
 * Scenario data structure
 * </p>
 */
export interface ScenarioData {
  /** Total scenario count */
  allSce?: number;
  /** Scenarios by script type */
  sceByScriptType?: {
    TEST_PERFORMANCE?: number;
    TEST_STABILITY?: number;
    TEST_FUNCTIONALITY?: number;
    TEST_CUSTOMIZATION?: number;
  };
}

/**
 * <p>
 * Script data structure
 * </p>
 */
export interface ScriptData {
  /** Total script count */
  allScript?: number;
  /** Scripts by plugin name */
  scriptByPluginName?: Record<string, number>;
}

/**
 * <p>
 * Mock data structure
 * </p>
 */
export interface MockData {
  /** Total API count */
  allApi: number;
  /** Total service count */
  allService: number;
  /** Total response count */
  allResponse: number;
  /** Total pushback count */
  allPushback: number;
}

/**
 * <p>
 * Data assets structure
 * </p>
 */
export interface DataAssetsData {
  /** Total dataset count */
  allDataset: number;
  /** Total datasource count */
  allDatasource: number;
  /** Total file count */
  allFile: number;
  /** Total variable count */
  allVariable: number;
}

/**
 * <p>
 * Chart configuration interface
 * </p>
 */
export interface ChartConfig {
  /** Chart grid configuration */
  grid: {
    left: string | number;
    right?: string | number;
    bottom: string | number;
    top: string | number;
    containLabel?: boolean;
  };
  /** Chart legend configuration */
  legend?: {
    show?: boolean;
    bottom?: string | number;
    type?: string;
    top?: string;
    right?: string | number;
    orient?: string;
    itemHeight?: number;
    itemWidth?: number;
    itemGap?: number;
  };
  /** Chart tooltip configuration */
  tooltip: {
    show?: boolean;
    trigger?: string;
    axisPointer?: {
      type?: string;
    };
  };
  /** Chart x-axis configuration */
  xAxis: {
    type: string;
    data?: any[];
    axisLabel?: {
      interval?: number;
      rotate?: number;
      overflow?: string;
      formatter?: string;
    };
    splitLine?: {
      show?: boolean;
    };
  };
  /** Chart y-axis configuration */
  yAxis: {
    type: string;
    data?: any[];
    axisLine?: {
      show?: boolean;
    };
    axisTick?: {
      show?: boolean;
    };
    splitLine?: {
      show?: boolean;
    };
    min?: (value: any) => number | undefined;
  };
  /** Chart series configuration */
  series: Array<{
    name?: string;
    type: string;
    data: any[];
    itemStyle?: {
      color?: string;
      borderRadius?: number[];
      borderColor?: string;
      borderWidth?: number;
    };
    barMaxWidth?: string | number;
    barMinWidth?: string | number;
    barGap?: string | number;
    smooth?: boolean;
    connectNulls?: boolean;
    areaStyle?: any;
    showBackground?: boolean;
    label?: {
      show?: boolean;
      position?: string;
      formatter?: string;
    };
    labelLine?: {
      show?: boolean;
      length?: number;
    };
    radius?: string[];
    center?: string[];
    avoidLabelOverlap?: boolean;
    emphasis?: {
      label?: {
        show?: boolean;
      };
    };
  }>;
  /** Chart title configuration */
  title?: {
    text?: string | number;
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
    };
  };
}

/**
 * <p>
 * Chart series color configuration
 * </p>
 */
export interface ChartSeriesColorConfig {
  [key: number]: string;
}

/**
 * <p>
 * HTTP method color configuration
 * </p>
 */
export interface MethodColorConfig {
  GET: string;
  HEAD: string;
  POST: string;
  PUT: string;
  PATCH: string;
  DELETE: string;
  OPTIONS: string;
  TRACE: string;
}

/**
 * <p>
 * Target data category mapping
 * </p>
 */
export interface TargetDataCategory {
  [key: string]: string;
}

/**
 * <p>
 * Rank icon mapping
 * </p>
 */
export interface RankIconConfig {
  [key: number]: string;
}
