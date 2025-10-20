import { Ref, ComputedRef } from 'vue';
import { HttpMethod, ParameterIn, ActionOnEOF, SharingMode } from '@xcan-angus/infra';
import { ApiPermission, ApiStatus } from '@/enums/enums';
import { ServerInfo } from '@/views/apis/server/types';
import { RequestBodyParam } from '@/views/apis/services/protocol/http/requestBody/types';
import { ParamsItem, ApisFormEdit } from '@/views/apis/services/protocol/types';
import { AuthenticationItem } from '@/views/apis/services/protocol/http/Authorization';
import { RequestSetting, AssertResult } from '@/views/apis/services/protocol/http/types';
import { ResponseState } from './useResponseHandler';

/**
 * API状态管理composable返回类型
 */
export interface UseApiStateReturn {
  // 状态
  saveParams: Ref<ApisFormEdit>;
  isUnarchivedApi: Ref<boolean>;
  apisStatus: Ref<ApiStatus | undefined>;
  loadingApiDetail: Ref<boolean>;
  apiAuths: Ref<ApiPermission[]>;
  currentServer: Ref<ServerInfo>;
  defaultCurrentServer: Ref<ServerInfo | undefined>;
  availableServers: Ref<ServerInfo[]>;
  apiMethod: Ref<HttpMethod>;
  apiUri: Ref<string | undefined>;
  state: {
    authentication: AuthenticationItem;
    parameter: Record<string, unknown>;
    assertTypeOptions: any[];
    assertConditionOptions: any[];
    paramTypeOptions: any[];
    bodyParamTypeOptions: any[];
    contentTypeOptions: any[];
    formDataTypeOptions: any[];
    methodOptions: any[];
    authTypeOptions: any[];
    parameters: ParamsItem[];
    requestBody: RequestBodyParam;
    headerList: ParamsItem[];
    cookieList: ParamsItem[];
    assertions: any[];
    secured: boolean;
    publishFlag: boolean;
  };
  setting: Ref<RequestSetting>;
  defaultAuthentication: Ref<AuthenticationItem>;
  authInHeader: Ref<Record<string, any>>;
  contentType: Ref<string | null>;
  resolvedRefModels: Ref<Record<string, string>>;
  datasetActionOnEOF: Ref<ActionOnEOF>;
  datasetSharingMode: Ref<SharingMode>;

  // 计算属性
  parametersNum: ComputedRef<number>;
  headerCount: ComputedRef<number>;
  cookieCount: ComputedRef<number>;
  hasBodyContent: ComputedRef<boolean>;

  // 方法
  loadApiDetail: () => Promise<void>;
  loadApiAuth: () => Promise<void>;
  loadServiceServers: (serviceId: string) => Promise<void>;
  handleStatusChange: (value: string) => Promise<void>;
  updateApiInfo: (info: any) => Promise<void>;
  updateUnarchivedApiInfo: (info: any) => Promise<void>;
}

/**
 * 请求处理composable返回类型
 */
export interface UseRequestHandlerReturn {
  loading: Ref<boolean>;
  sendRequest: (
    state: any,
    setting: RequestSetting,
    currentServer: ServerInfo,
    apiUri: string,
    apiMethod: HttpMethod,
    contentType: string | null,
    saveParams: any,
    requestBodyRef: any,
    authorizationRef: any,
    assertFormRef: any,
    isUnarchivedApi: boolean,
    ws: WebSocket | undefined,
    allFuncNames: string[],
    onHttpResponse: (resp: any, request: any) => Promise<void>,
    onWebSocketResponse: (params: any) => void
  ) => Promise<void>;
  handleAbort: () => void;
  getParameter: (
    isDebug?: boolean,
    state?: any,
    setting?: RequestSetting,
    currentServer?: ServerInfo,
    apiUri?: string,
    apiMethod?: HttpMethod,
    contentType?: string | null,
    saveParams?: any,
    requestBodyRef?: any,
    authorizationRef?: any,
    assertFormRef?: any,
    isUnarchivedApi?: boolean
  ) => Promise<any>;
  getRealUri: (pathParams: Record<string, any>, queryParams: Record<string, any>, apiUri: string) => string;
}

/**
 * 响应处理composable返回类型
 */
export interface UseResponseHandlerReturn {
  responseState: ResponseState;
  responseError: {
    show: boolean;
    value: string | undefined;
  };
  assertResult: Ref<AssertResult[] | undefined>;
  assertionVariableExtra: Ref<any>;
  localRequestInfo: {
    responseHeader: Record<string, any>;
    responseBody: {
      data: any;
      size: number;
    };
    query: Record<string, any>;
    path: Record<string, any>;
    header: Record<string, any>;
    form: any;
    duration: number;
    status: number | undefined;
  };
  isResponseEmpty: ComputedRef<boolean>;
  handleHttpResponse: (resp: any, request: any) => Promise<void>;
  onResponse: (response: string) => Promise<void>;
  resetResponseState: () => void;
  getValueByType: (
    assertionCondition: any,
    type: any,
    data: {
      bodySize: number;
      size: number;
      duration: number;
      status: number;
      responseHeader: string[];
      rawContent: string;
      extractValue: string;
    },
    parameterName: string
  ) => {
    data: string | null;
    message: string;
    errorMessage: string;
  };
  getHeaderParams: (data: string[], name: string) => string;
  setAssertResult: (responseData: any) => Promise<AssertResult[]>;
}

/**
 * 断言处理composable返回类型
 */
export interface UseAssertionHandlerReturn {
  assertNum: Ref<number>;
  assertResult: Ref<AssertResult[] | undefined>;
  assertionVariableExtra: Ref<any>;
  validateParam: (assertFormRef: any) => boolean;
  getAssertionData: (assertFormRef: any) => { data: any[]; variables: any[] };
  executeAssertions: (requestInfo: any, assertions: any[], variableValues: any[]) => Promise<AssertResult[]>;
  executeProxyAssertions: (conditions: any[], variableValues: any[]) => { extra: any };
  getValueByType: (
    assertionCondition: any,
    type: any,
    data: {
      bodySize: number;
      size: number;
      duration: number;
      status: number;
      responseHeader: string[];
      rawContent: string;
      extractValue: string;
    },
    parameterName: string
  ) => {
    data: string | null;
    message: string;
    errorMessage: string;
  };
  getHeaderParams: (data: string[], name: string) => string;
  processAssertionResults: (
    assertions: any[],
    responseData: any,
    assertionVariableExtra: any
  ) => Promise<AssertResult[]>;
  updateAssertNum: (count: number) => void;
  resetAssertionState: () => void;
}

/**
 * UI状态管理composable返回类型
 */
export interface UseUIStateReturn {
  activeTabKey: Ref<string>;
  activeKey: Ref<string>;
  currentTab: Ref<string>;
  activeDrawerKey: Ref<any>;
  shareVisible: Ref<boolean>;
  height: Ref<number>;
  maxHeight: Ref<number>;
  moving: Ref<boolean>;
  errorTitle: Ref<string>;
  myNavs: ComputedRef<any[]>;
  toolbarMenus: ComputedRef<any[]>;
  handleShare: () => void;
  openToolBar: (toolbarRef: any) => void;
  toolbarChange: (menuKey: string) => void;
  setErrTitle: (hasWebSocket: boolean) => void;
  closeDrawer: (drawerRef: any) => void;
  createResizeHandler: (mainWrapper: any) => () => void;
  resetUIState: () => void;
}

/**
 * 参数管理composable返回类型
 */
export interface UseParameterManagerReturn {
  parameters: Ref<ParamsItem[]>;
  headerList: Ref<ParamsItem[]>;
  cookieList: Ref<ParamsItem[]>;
  requestBody: Ref<RequestBodyParam>;
  contentType: Ref<string | null>;
  parametersNum: ComputedRef<number>;
  headerCount: ComputedRef<number>;
  cookieCount: ComputedRef<number>;
  changeParamList: (data: ParamsItem[]) => void;
  changeHeaderList: (data: ParamsItem[]) => void;
  changeCookieList: (data: ParamsItem[]) => void;
  changeRequestBody: (data: RequestBodyParam) => void;
  addQueryParam: (data: any, requestParamsRef: any) => void;
  extractQueryParamsFromUrl: (endpoint: string) => ParamsItem[];
  processParameters: (parameters: any[], endpoint: string) => ParamsItem[];
  processHeaders: (parameters: any[]) => ParamsItem[];
  processCookies: (parameters: any[]) => ParamsItem[];
  extractContentType: (headers: ParamsItem[]) => void;
  getValidHeaders: () => ParamsItem[];
  resetParameterState: () => void;
}

/**
 * 组件Props类型
 */
export interface ComponentProps {
  pid: string;
  valueObj: Record<string, any>;
  ws: WebSocket | undefined;
  response: string;
  uuid: string;
  id?: string | undefined;
  serviceId?: string;
  projectId: string;
  appInfo: { [key: string]: string };
  userInfo: { [key: string]: string };
}

/**
 * 请求上下文类型
 */
export interface RequestContext {
  query: Record<string, any>;
  header: Record<string, any>;
  path: Record<string, any>;
  requestBody: {
    formData?: Record<string, any>;
    urlencoded?: Record<string, any>;
    body?: any;
  };
  queryString: string;
  assertions: any[];
  variableValues: any[];
}
