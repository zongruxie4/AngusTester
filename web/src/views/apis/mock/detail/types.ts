

// Keep view-model consistent with ActivityInfo expected fields.
// Ensure avatar is always a non-empty string to satisfy library prop typing.
export interface ActivityItem {
  id: string;
  optDate: string;
  targetId: string;
  targetType: string;
  title: string;
  fullName: string;
  description: string;
  detail: string;
  details: string[];
  avatar: string;
}

/**
 * Chart data structure for cache metrics visualization
 */
export interface CacheChartData {
  xData: string[];
  numData: number[];
  totalData: number[];
  usedData: number[];
  maxTotalData: number;
  maxNumData: number;
}

/**
 * Chart data structure for RAM metrics visualization
 */
export interface RamChartData {
  xData: string[];
  submitData: number[];
  totalData: number[];
  usedData: number[];
}

/**
 * Chart data structure for CPU metrics visualization
 */
export interface CpuChartData {
  xData: string[];
  totalData: number[];
  systemCpuUsed: number[];
  processCpuUsed: number[];
}

/**
 * Mock service statistics overview
 */
export interface MockServiceStats {
  apisNum: string;
  requestNum: string;
  pushbackNum: string;
  simulateErrorNum: string;
  successNum: string;
  exceptionNum: string;
}

/**
 * Mock API item in the table list
 */
export interface MockApiItem {
  id: string;
  summary: string;
  method: {
    value: string;
    message: string;
  };
  endpoint: string;
  requestNum: string;
  simulateErrorNum: string;
  pushbackNum: string;
  successNum: string;
  exceptionNum: string;
}

/**
 * Chart time type for different display formats
 */
export type ChartTimeType = 'select' | 'normal';

/**
 * Raw metrics data from API response
 */
export interface MetricsDataItem {
  timestamp: string;
  cvsJvm: string;
  cvsProcessor: string;
}

export type FilterOp =
  'EQUAL'
  | 'NOT_EQUAL'
  | 'GREATER_THAN'
  | 'GREATER_THAN_EQUAL'
  | 'LESS_THAN'
  | 'LESS_THAN_EQUAL'
  | 'CONTAIN'
  | 'NOT_CONTAIN'
  | 'MATCH_END'
  | 'MATCH'
  | 'IN'
  | 'NOT_IN';
export type FilterItem = {
  key: string;
  value: string | boolean | string[];
  op: FilterOp;
};
export type SearchParams = {
  pageNo: number;
  pageSize: number;
  filters?: FilterItem[];
  orderBy?: string;
  orderSort?: 'ASC' | 'DESC';
  [key: string]: any;
};

/**
 * Request record item in the API log list
 */
export interface RequestRecordItem {
  id: string;
  summary: string;
  requestId: string;
  requestDate: string;
  endpoint: string;
  method: string;
  responseStatus: string;
}

/**
 * Detailed request/response information
 */
export interface RequestRecordDetail {
  id: string;
  summary: string;
  requestId: string;
  requestDate: string;
  endpoint: string;
  method: string;
  responseStatus: string;
  requestHeaders?: HeaderItem[];
  requestBody?: string;
  responseHeaders?: HeaderItem[];
  responseBody?: string;
}

/**
 * Header item structure
 */
export interface HeaderItem {
  name: string;
  value: string;
}

/**
 * Grid column configuration for header display
 */
export interface GridColumn {
  label: string;
  dataIndex: string;
}

/**
 * Format options for response body display
 */
export type FormatType = 'pretty' | 'raw' | 'preview';
/**
 * HTTP method color mapping keys
 */
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'OPTIONS' | 'HEAD' | 'TRACE';

/**
 * Basic service information form state
 */
export interface ServiceInfoForm {
  serviceDomainUrl: string;
  name: string;
}

/**
 * API security configuration
 */
export interface ApiSecurityItem {
  keyName: string;
  in: string;
  value: string;
}

export interface SecurityForm {
  apisSecurity: ApiSecurityItem[];
}

/**
 * CORS configuration
 */
export interface CorsConfig {
  allowCorsCredentials: boolean;
  allowCorsOrigin: string;
  allowCorsRequestHeaders: string;
  allowCorsRequestMethods: string;
  allowExposeHeaders: string;
  enabled: boolean;
}

/**
 * Service settings configuration
 */
export interface ServiceSettings {
  useSsl: boolean;
  workThreadNum: string;
  enableNettyLog: boolean;
  sendRequestLog: boolean;
  logFileLevel: string;
  maxContentLength: string;
  workPushbackThreadNum: string;
  maxPushbackConnectTimeout: string;
  maxPushbackRequestTimeout: string;
}

/**
 * Complete mock service information
 */
export interface MockServiceInfo {
  id: string;
  name: string;
  serviceDomainUrl: string;
  servicePort: string;
  nodeName: string;
  nodeIp: string;
  nodePublicIp?: string;
  agentPort?: string;
  apisSecurity?: ApiSecurityItem[];
  apisCors: CorsConfig;
  setting: ServiceSettings;
  auth?: boolean;
  currentAuths?: Array<{ value: string }>;
}

/**
 * Select options for form controls
 */
export interface SelectOption {
  label: string;
  value: string | boolean;
}
