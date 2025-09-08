import { Ref } from 'vue';
import { ListData } from '../composables/useExecCount';

/**
 * Performance detail component props interface
 */
export interface PerformanceDetailProps {
  detail?: Record<string, any>;
  exception?: {
    codeName: string;
    messageName: string;
    code: string;
    message: string;
  };
}

/**
 * Performance detail component emits interface
 */
export interface PerformanceDetailEmits {
  (e: 'loaded', data: Record<string, any>): void;
  (e: 'update:loading', value: boolean): void;
}

/**
 * Performance metrics data structure for API dimension
 */
export interface ApiDimensionData {
  duration: number[];
  errors: number[];
  iterations: number[];
  n: number[];
  operations: number[];
  transactions: number[];
  readBytes: number[];
  writeBytes: number[];
  ops: number[];
  minOps: number[];
  maxOps: number[];
  meanOps: number[];
  tps: number[];
  minTps: number[];
  maxTps: number[];
  meanTps: number[];
  brps: number[];
  minBrps: number[];
  maxBrps: number[];
  meanBrps: number[];
  bwps: number[];
  minBwps: number[];
  maxBwps: number[];
  meanBwps: number[];
  tranMean: number[];
  tranMin: number[];
  tranMax: number[];
  tranP50: number[];
  tranP75: number[];
  tranP90: number[];
  tranP95: number[];
  tranP99: number[];
  tranP999: number[];
  errorRate: number[];
  threadPoolSize: number[];
  threadPoolActiveSize: number[];
  threadMaxPoolSize: number[];
}

/**
 * Performance metrics data structure for index dimension
 */
export interface IndexDimensionData {
  duration: { [key: string]: number[] };
  errors: { [key: string]: number[] };
  iterations: { [key: string]: number[] };
  n: { [key: string]: number[] };
  operations: { [key: string]: number[] };
  transactions: { [key: string]: number[] };
  readBytes: { [key: string]: number[] };
  writeBytes: { [key: string]: number[] };
  ops: { [key: string]: number[] };
  minOps: { [key: string]: number[] };
  maxOps: { [key: string]: number[] };
  meanOps: { [key: string]: number[] };
  tps: { [key: string]: number[] };
  minTps: { [key: string]: number[] };
  maxTps: { [key: string]: number[] };
  meanTps: { [key: string]: number[] };
  brps: { [key: string]: number[] };
  minBrps: { [key: string]: number[] };
  maxBrps: { [key: string]: number[] };
  meanBrps: { [key: string]: number[] };
  bwps: { [key: string]: number[] };
  minBwps: { [key: string]: number[] };
  maxBwps: { [key: string]: number[] };
  meanBwps: { [key: string]: number[] };
  tranMean: { [key: string]: number[] };
  tranMin: { [key: string]: number[] };
  tranMax: { [key: string]: number[] };
  tranP50: { [key: string]: number[] };
  tranP75: { [key: string]: number[] };
  tranP90: { [key: string]: number[] };
  tranP95: { [key: string]: number[] };
  tranP99: { [key: string]: number[] };
  tranP999: { [key: string]: number[] };
  errorRate: { [key: string]: number[] };
  threadPoolSize: { [key: string]: number[] };
  threadPoolActiveSize: { [key: string]: number[] };
  threadMaxPoolSize: { [key: string]: number[] };
}

/**
 * Data transfer rate unit type
 */
export type DataRateUnit = 'KB' | 'MB';

/**
 * Performance list parameters interface
 */
export interface PerformanceListParams {
  pageNo: number;
  pageSize: number;
  filters: Array<{
    key: 'timestamp';
    op: 'GREATER_THAN_EQUAL';
    value: string;
  }>;
  nodeId?: string;
}

/**
 * Error count list item interface
 */
export interface ErrorCountListItem {
  name: string;
  errorNum: number | string;
  errorRate: string;
  list: Array<{
    name: string;
    errorNum: number | string;
  }>;
}

/**
 * Sample error content interface
 */
export interface SampleErrorContent {
  timestamp: string;
  [key: string]: any;
}

/**
 * HTTP status code data interface
 */
export interface StatusCodeData {
  [key: string]: {
    '2xx': number;
    '3xx': number;
    '4xx': number;
    '5xx': number;
    Exception: number;
  };
}

/**
 * Computed aggregate values interface
 */
export interface ComputedAggregateValues {
  maxOps: number;
  minOps: number;
  meanOps: number;
  maxTps: number;
  minTps: number;
  meanTps: number;
  maxBrps: number;
  minBrps: number;
  meanBrps: number;
  maxBwps: number;
  minBwps: number;
  meanBwps: number;
}

/**
 * Exception information interface
 */
export interface ExceptionInfo {
  code: string;
  message: string;
  codeName: string;
  messageName: string;
}

/**
 * Performance component state interface
 */
export interface PerformanceState {
  isLoaded: boolean;
  hasFetchedOnce: boolean;
  isFirstLoadList: boolean;
  isFirstUpdatePerfList: boolean;
  isFirstUpdatePerfErrList: boolean;
  countTabKey: string;
  timer: NodeJS.Timeout | null;
}

/**
 * Performance data management interface
 */
export interface PerformanceDataManager {
  apiDimensionObj: Ref<Record<string, ApiDimensionData>>;
  indexDimensionObj: Ref<IndexDimensionData>;
  apiNames: Ref<string[]>;
  timestampData: Ref<string[]>;
  newList: Ref<ListData[]>;
  allList: Ref<ListData[]>;
  perfListData: Ref<ListData[]>;
  perfListParams: Ref<PerformanceListParams>;
  perfListTotal: Ref<number>;
  perfListLastTimestamp: Ref<string>;
  errCountList: Ref<ErrorCountListItem[]>;
  sampleList: Ref<SampleErrorContent[]>;
  errParams: Ref<PerformanceListParams>;
  errTotal: Ref<number>;
  errTimestamp: Ref<string>;
  statusCodeData: Ref<StatusCodeData>;
  infoMaxQps: Ref<string | number>;
  infoMaxTps: Ref<string | number>;
  brpsUnit: Ref<DataRateUnit>;
  minBrpsUnit: Ref<DataRateUnit>;
  maxBrpsUnit: Ref<DataRateUnit>;
  meanBrpsUnit: Ref<DataRateUnit>;
  bwpsUnit: Ref<DataRateUnit>;
  minBwpsUnit: Ref<DataRateUnit>;
  maxBwpsUnit: Ref<DataRateUnit>;
  meanBwpsUnit: Ref<DataRateUnit>;
  exception: Ref<ExceptionInfo | undefined>;
}
