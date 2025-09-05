import {
  ResponseContentConfig,
  ResponseContentInfo,
  ResponsePushbackConfig,
  ResponsePushbackInfo
} from '@/views/apis/mock/detail/apis/components/contentForm/PropsType';
import { ResponseMatchConfig, ResponseMatchInfo } from '@/views/apis/mock/detail/apis/components/matchForm/PropsType';

/**
 * HTTP methods supported by the Mock API system
 */
export type HttpMethod = MockServicePermission.DELETE | 'GET' | 'HEAD' | 'OPTIONS' | 'PATCH' | 'POST' | 'PUT' | 'TRACE';

/**
 * Response information structure from server
 */
export type ResponseInfo = {
  id: string;
  name: string;
  content: ResponseContentInfo;
  match: ResponseMatchInfo;
  pushback?: ResponsePushbackInfo;
};

/**
 * Response configuration structure for form operations
 */
export type ResponseConfig = {
  name: string | undefined;
  content: ResponseContentConfig | undefined;
  match: ResponseMatchConfig | undefined;
  pushback?: ResponsePushbackConfig | undefined;
};

/**
 * Mock API information structure from server
 */
export type MockAPIInfo = {
  id: string;
  summary: string;
  method: { value: HttpMethod; message: string };
  description: string;
  endpoint: string;
  mockServiceId: string;
  isTempFlag: boolean;
};

/**
 * Mock API configuration structure for form operations
 */
export type MockAPIConfig = {
  id: string;
  summary: string;
  method: HttpMethod;
  description: string;
  endpoint: string;
  mockServiceId: string;
  isTempFlag: boolean; // Flag to identify temporary data for new APIs
  // Fields for copy/link API operations
  selectApiId?: string;
  selectType?: 'link' | 'copy';
};

/**
 * Service URL option structure for dropdown
 */
export type ServiceUrlOption = {
  label: string;
  value: string;
};

/**
 * Form reference structure for validation
 */
export type FormRefs = {
  matchRefs: any[];
  contentRefs: any[];
  pushbackRefs: any[];
};
