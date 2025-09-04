import { EnumMessage, ScriptSource, ScriptType } from '@xcan-angus/infra';
import { PaginationProps } from 'ant-design-vue';
import { ScriptPermission } from '@/enums/enums';

/**
 * Resource information for script statistics
 * Contains counts for different types of scripts and sources
 */
export type ResourceCount = {
  /** Total number of scripts */
  totalScriptNum: string;
  /** Number of performance test scripts */
  perfScriptNum: string;
  /** Number of functional test scripts */
  functionalScriptNum: string;
  /** Number of stability test scripts */
  stabilityScriptNum: string;
  /** Number of customization test scripts */
  customizationScriptNum: string;
  /** Number of mock data scripts */
  mockDataScriptNum: string;
  /** Number of mock API scripts */
  mockApisScriptNum: string;
  /** Number of created sources */
  createdSourceNum: string;
  /** Number of imported sources */
  importedSourceNum: string;
  /** Number of API sources */
  apisSourceNum: string;
  /** Number of case sources */
  caseSourceNum: string;
  /** Number of scenario sources */
  scenarioSourceNum: string;
}

export type FormState = {
  name: string | undefined;
  type: EnumMessage<ScriptType>,
  typeName: string | undefined;
  description: string | undefined;
}

export type ScriptInfo = {
  id: string;
  name: string;
  description: string;
  auth: boolean;
  content: string;
  plugin: string;
  projectId: string;
  serviceId: string;
  serviceName: string;
  type: EnumMessage<ScriptType>;
  source: EnumMessage<ScriptSource>;
  sourceId: string;
  sourceName: string;
  tags: string[];
  createdBy: string;
  createdByName: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;

  /** Optional link URL for the script name */
  nameLinkUrl?: string;
  /** Optional link URL for source name */
  sourceNameLinkUrl?: string;
  /** Page number for pagination */
  pageNo?: number;
  /** Page size for pagination */
  pageSize?: number;
}

/**
 * Execution information type
 * Represents the structure of execution data
 */
export type ExecInfo = {
  actualStartDate: string;
  assocApiCaseIds: string[];
  canUpdateTestResult: boolean;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  currentDuration: string;
  currentDurationProgress: string;
  currentIterations: string;
  currentIterationsProgress: string;
  duration: {
    unit: string;
    value: string;
  };
  durationProgress: boolean;
  endDate: string;
  execBy: string;
  execByName: string;
  hasOperationPermission: boolean;
  id: string;
  ignoreAssertions: boolean;
  iterations: string;
  iterationsProgress: boolean;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  lastSchedulingDate: string;
  lastSchedulingResult: {
    console: string[];
    deviceId: string;
    execId: string;
    exitCode: string;
    message: string;
    results: string[];
    success: boolean;
  }[];
  meterMessage: string;
  meterStatus: string;
  name: string;
  no: string;
  plugin: string;
  priority: string;
  reportInterval: {
    unit: string;
    value: string;
  };
  sampleSummaryInfo: {
    brps: string;
    bwps: string;
    duration: string;
    endTime: string;
    errorRate: string;
    errors: string;
    extCounter1: string;
    extCounter2: string;
    extCounter3: string;
    extGauge1: string;
    extGauge2: string;
    extGauge3: string;
    finish: boolean;
    iterations: string;
    n: string;
    name: string;
    operations: string;
    ops: string;
    readBytes: string;
    startTime: string;
    threadMaxPoolSize: string;
    threadPoolActiveSize: string;
    threadPoolSize: string;
    threadRunning: boolean;
    threadTerminated: boolean;
    tps: string;
    tranMax: string;
    tranMean: string;
    tranMin: string;
    tranP50: string;
    tranP75: string;
    tranP90: string;
    tranP95: string;
    tranP99: string;
    tranP999: string;
    transactions: string;
    uploadResultBytes: string;
    uploadResultProgress: string;
    uploadResultTotalBytes: string;
    writeBytes: string;
  };
  schedulingNum: string;
  scriptId: string;
  scriptName: string;
  scriptSource: string;
  scriptSourceId: string;
  scriptSourceName: string;
  scriptType: string;
  singleTargetPipeline: boolean;
  startAtDate: string;
  startMode: string;
  status: {
    value: string;
    message: string;
  };
  syncTestResult: boolean;
  syncTestResultFailure: string;
  thread: string;
  trial: boolean;
  updateTestResult: boolean;
}

/**
 * Menu item for quick search options
 */
export type MenuItem = {
  key: string;
  name: string;
}

export type ScriptSearchProps = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

export type ScriptTableProps = {
  projectId: string;
  appId: string;
  userId: string;
  dataSource: ScriptInfo[];
  permissionsMap: { [key: string]: ScriptPermission[] };
  pagination: PaginationProps;
  allowImportSamplesFlag: boolean;
  loaded: boolean;
  loading: boolean;
  resetSelectedIdsNotify: string;
}

/**
 * Chart data item for pie chart visualization
 */
export type ChartDataItem = {
  /** Data item name */
  name: string;
  /** Data item value */
  value: number;
}

/**
 * ECharts configuration options for pie chart
 */
export type PieChartOption = {
  /** Chart tooltip configuration */
  tooltip: {
    trigger: 'item';
    axisPointer: { type: 'shadow' };
    textStyle: {
      fontSize: number;
    };
  };
  /** Chart legend configuration */
  legend: {
    top: string;
    right: number;
    orient: 'vertical';
    itemHeight: number;
    itemWidth: number;
    itemGap: number;
    formatter: (name: string) => string;
  };
  /** Chart color palette */
  color: string[];
  /** Chart series configuration */
  series: Array<{
    name: string;
    type: 'pie';
    center: [string, string];
    radius: [string, string];
    avoidLabelOverlap: boolean;
    itemStyle: {
      borderRadius: number;
      borderColor: string;
      borderWidth: number;
    };
    label: {
      show: boolean;
      formatter: string;
    };
    labelLine: {
      show: boolean;
    };
    emphasis: {
      label: {
        show: boolean;
        fontSize: number;
        fontWeight: string;
      };
    };
    data: ChartDataItem[];
  }>;
}
