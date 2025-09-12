import { MockService } from '@/views/apis/mock/types';

/**
 * Chart time type for different display formats
 */
export type ChartTimeType = 'select' | 'normal';

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
export interface MockServiceCount {
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
 * Raw metrics data from API response
 */
export interface MetricsDataItem {
  timestamp: string;
  cvsJvm: string;
  cvsProcessor: string;
}

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
 * Header item structure
 */
export interface HttpHeaderItem {
  name: string;
  value: string;
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
  requestHeaders?: HttpHeaderItem[];
  requestBody?: string;
  responseHeaders?: HttpHeaderItem[];
  responseBody?: string;
}

/**
 * Grid column configuration for header display
 */
export interface GridColumn {
  label: string;
  dataIndex: string;
}

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

export interface ApiSecurityForm {
  apisSecurity: ApiSecurityItem[];
}

/**
 * CORS configuration
 */
export interface CorsSetting {
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
export interface ServerSettings {
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
export interface MockServiceDetail extends MockService {
  agentPort?: string;
  apisSecurity?: ApiSecurityItem[];
  apisCors: CorsSetting;
  setting: ServerSettings;
}
