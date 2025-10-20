<script setup lang="ts">
import {
  computed,
  defineAsyncComponent,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  provide,
  reactive,
  ref,
  watch
} from 'vue';
import { Badge, Button, Popover, RadioButton, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline, AsyncComponent, Drawer, Hints, Icon, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import elementResizeDetector from 'element-resize-detector';
import {
  ActionOnEOF,
  AssertionCondition,
  AssertionType,
  axiosClient,
  duration,
  HttpMethod,
  ParameterIn,
  SchemaType,
  SharingMode,
  utils
} from '@xcan-angus/infra';
import { dataURLtoBlob } from '@/utils/blob';
import qs from 'qs';
import { deconstruct } from '@/utils/swagger';
import SwaggerUI from '@xcan-angus/swagger-ui';
import XML from 'xml';
import useClipboard from 'vue-clipboard3';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';

import assertUtils from '@/utils/assertutils';
import { encode } from '@/utils/secure';
import { formatBytes } from '@/utils/common';
import { apis, services } from '@/api/tester';
import { ApiPermission, ApiServerSource, ApiStatus, ApisShareScope } from '@/enums/enums';
import { CONTENT_TYPE_KEYS, HTTP_HEADERS } from '@/utils/constant';

// eslint-disable-next-line import/no-duplicates
import apiUtils, {
  API_EXTENSION_KEY,
  API_STATUS_BADGE_COLOR_CONFIG,
  API_STATUS_COLOR_CONFIG,
  getModelDataByRef,
  travelXcValueToString,
  travelEmptyObjToString,
  convertBlob
} from '@/utils/apis';

import { getServerData, ServerInfo } from '@/views/apis/server/types';
import { rawTypeOptions, RequestBodyParam } from '@/views/apis/services/protocol/http/requestBody/types';
import { getShowAuthData } from '@/components/ApiAuthencation/interface';

import { getStatusText } from '@/views/apis/services/components/types';

import {
  ApiAssert,
  ApiCookie,
  ApiRequest,
  ApiResponse,
  ApiTimeline,
  AssertForm,
  Authorization,
  debugTip,
  docInfo,
  getStatusColor,
  menus,
  navs,
  RequestBody,
  RequestCookie,
  RequestHeader,
  RequestParams,
  ResponseState,
  MainState
} from './index';
import {
  getDefaultParams,
  validateBodyForm,
  validateQueryParameter
} from './utils';
import { AssertResult, ConditionResult, Parameter, RequestSetting } from './types';

import SelectEnum from '@/components/enum/SelectEnum.vue';
import { ApisFormEdit, ParamsItem } from '@/views/apis/services/protocol/types';

const Indicator = defineAsyncComponent(() => import('@/components/Indicator/index.vue'));
const HttpTestInfo = defineAsyncComponent(() => import('@/components/HttpTestInfo/index.vue'));
const FunctionsButton = defineAsyncComponent(() => import('@xcan-angus/vue-ui').then(resp => resp.FunctionsButton));
const APICaseParametric = defineAsyncComponent(() => import('@/components/apis/parameterization/index.vue'));
const ExecDetail = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/exec/index.vue'));

const UnarchivedEdit = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/slider/UnarchivedEdit.vue'));
const InfoEdit = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/slider/InfoEdit.vue'));

const ShareList = defineAsyncComponent(() => import('@/components/share/list.vue'));
const RequestProxy = defineAsyncComponent(() => import('@/views/config/proxy/EditableRequestProxy.vue'));
const CodeSnippet = defineAsyncComponent(() => import('@/views/apis/services/components/CodeSnippet.vue'));

const ApiSetting = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/Setting.vue'));
const ServerPath = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/path/index.vue'));
const Toolbar = defineAsyncComponent(() => import('@/components/layout/toolbar/index.vue'));
const ResponseError = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/ResponseError.vue'));
const ApiDoc = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/Doc.vue'));
const TestCase = defineAsyncComponent(() => import('@/views/apis/services/components/case/tableView.vue'));
const ApiMockVue = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/MockApi.vue')); //
const ApiShare = defineAsyncComponent(() => import('@/views/apis/share/Edit.vue'));

interface Props {
  pid: string,
  valueObj: Record<string, any>,
  ws: WebSocket | undefined;
  response: string,
  uuid: string;
  id?: string | undefined;
  serviceId?: string;
  projectId: string;
  appInfo: {[key: string]: string};
  userInfo: {[key: string]: string};
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  pid: undefined, // pid used to identify the unique value of the current pane
  serviceId: undefined,
  projectId: ''
});

const shareVisible = ref(false); // Share dialog visibility
const handleShare = () => {
  shareVisible.value = true;
};

// Update left sidebar unarchived list
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => { });

const erd = elementResizeDetector({ strategy: 'scroll' });
const { toClipboard } = useClipboard();

// eslint-disable-next-line new-cap
const httpClient = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });
const { serverSourceKey, requestSettingKey, valueKey, enabledKey, fileNameKey, idKey } = API_EXTENSION_KEY;
const apiAuths = ref<ApiPermission[]>([]);

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const replaceTabPane = inject<(key: string, data: any) => void>('replaceTabPane', () => { });
const allFunction = inject('allFunction', ref([]));

const activeTabKey = ref('debug');

const allFuncNames = computed(() => {
  return allFunction.value.map(i => i.name);
});

const myNavs = computed(() => {
  if (isUnarchivedApi.value || !props.id) {
    const archivedNav = ['performance', 'variable', 'share', 'test', 'case', 'activity', 'apiMock'];
    return navs.filter(nav => !archivedNav.includes(nav.value)).map(i => ({ ...i, key: i.value }));
  }
  const unarchivedNav = ['saveUnarchived'];
  return navs.filter(nav => !unarchivedNav.includes(nav.value)).map(i => {
    if (i.value === 'save') {
      return {
        ...i,
        key: i.value,
        disabled: !apiAuths.value?.includes(i.auth as ApiPermission),
        name: t('actions.save')
      };
    }
    return {
      ...i,
      key: i.value,
      disabled: !apiAuths.value?.includes(i.auth as ApiPermission)
    };
  });
});

let ws: WebSocket | undefined;

let uuid = '';

const responseError = reactive<{
  show: boolean,
  value: string | undefined
}>({
  show: false,
  value: undefined
});
const setWebSocket = (value) => {
  ws = value;
};

const currentTab = ref<string>('');
const height = ref<number>(34);

const apiMethod = ref<HttpMethod>(HttpMethod.GET);
const currentServer = ref<ServerInfo>({ url: '' });
const defaultCurrentServer = ref();
const apiUri = ref<string>(); // Complete URI
const requestParamsRef = ref(); // Parameters ref
const drawerRef = ref(); // Right drawer APIs ref
const activeDrawerKey = ref();
const toolbarRef = ref(); // Bottom response component ref
const maxHeight = ref(0); // Maximum height
const loading = ref(false); // Send request loading state
const mainWrapper = ref();
const availableServers = ref<ServerInfo[]>([]); // All available servers
const isUnarchivedApi = ref<boolean>(true);
const requestHeaderRef = ref();
const requestCookieRef = ref();
const authorizationRef = ref();
let initParams = {};

const assertResult = ref<AssertResult[]>(); // Assertion results

// Data for saving edits
const saveParams = ref<ApisFormEdit>({
  id: undefined,
  operationId: undefined,
  summary: undefined,
  ownerId: undefined,
  serviceId: undefined,
  serviceName: undefined,
  ownerName: undefined,
  unarchivedId: undefined,
  responses: undefined
});

// eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars
const updateApiGroup = inject('updateApiGroup', (_id) => undefined);

const responseState = reactive<ResponseState>({
  config: {},
  headers: {},
  data: null,
  status: 0,
  size: 0,
  assertions: [],
  cookie: [],
  requestHeaders: [],
  performance: {} as PerformanceEntry,
  contentEncoding: undefined
});

const setting = ref<RequestSetting>({
  enableParamValidation: false,
  connectTimeout: 6000,
  readTimeout: 60000,
  retryNum: 0,
  maxRedirects: 1
});

const state = reactive<MainState>({
  authentication: { type: null },
  parameter: {},
  assertTypeOptions: [],
  assertConditionOptions: [],
  paramTypeOptions: [],
  bodyParamTypeOptions: [],
  contentTypeOptions: [],
  formDataTypeOptions: [],
  methodOptions: [],
  authTypeOptions: [],
  parameters: [],
  requestBody: {},
  headerList: [],
  cookieList: [],
  assertions: [],
  secured: false,
  publishFlag: false
});

const defaultAuthentication = ref({});

const initApiInfo = ref(state);
const authInHeader = ref({});
const requestBodyRef = ref();
const assertFormRef = ref();

const contentType = ref<string | null>(null);
const resolvedRefModels = ref<Record<string, string>>({});
const apisStatus = ref();

const changeParamList = (data: ParamsItem[]): void => {
  state.parameters = data.filter(i => !!i.name || !!i[valueKey]);
};

const addQueryParam = (data) => {
  setTimeout(() => {
    requestParamsRef.value.addParam(data);
  });
};

const parametersNum = computed(() => {
  return state.parameters.length;
});

const assertNum = ref(0);

const changeRequestBody = (data: RequestBodyParam): void => {
  state.requestBody = data;
};

const changeHeaderList = (data: ParamsItem[]): void => {
  state.headerList = data;
};

const changeCookieList = (data: ParamsItem[]): void => {
  state.cookieList = data;
};

const changeAuthData = (data): void => {
  state.authentication = data;
};

const changeSetting = (data: RequestSetting): void => {
  setting.value = data;
};

const handleStatusChange = async (value: string) => {
  const [error] = await apis.patchApiStatus({ status: value, id: props.id });
  if (error) {
    return;
  }
  apisStatus.value = value;
};

const getModelFromRef = async (ref) => {
  if (!saveParams.value.serviceId) {
    return {};
  }
  const [error, resp] = await getModelDataByRef(saveParams.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(resp.data || {});
};

/**
 * Load API operation permissions
 * <p>
 * Fetches the current user's permissions for the API and updates the
 * apiAuths reactive reference with the permission values.
 * </p>
 */
const loadApiAuth = async () => {
  const [error, res] = await apis.getCurrentAuth(props.id as string);
  if (error) {
    return;
  }
  apiAuths.value = (res.data?.permissions || []).map(i => i.value);
};

/**
 * Load service server information
 * <p>
 * Fetches available server URLs for the specified service and updates
 * the availableServers reactive reference with server data including extensions.
 * </p>
 * @param serviceId - The service ID to load servers for
 */
const loadServiceServers = async (serviceId: string) => {
  const [error, resp] = await services.getServicesServerUrlInfo(serviceId);
  if (error) {
    return;
  }
  availableServers.value = (resp.data || []).map(i => ({ ...i, ...(i.extensions || {}) }));
};

const loadingApiDetail = ref(false);

/**
 * Load API detail information
 * <p>
 * Fetches detailed API information including parameters, request body, authentication,
 * and other configuration. Handles both archived and unarchived APIs.
 * </p>
 */
const loadApiDetail = async (): Promise<void> => {
  loadingApiDetail.value = true;
  let result;
  if (isUnarchivedApi.value) {
    result = await apis.getUnarchivedApiDetail(props.id as string);
  } else {
    result = await apis.getApiDetail(props.id as string);
  }
  loadingApiDetail.value = false;
  const [error, res] = result;
  if (error) {
    return;
  }

  const {
    assertions,
    authentication,
    operationId,
    id,
    method,
    summary,
    ownerId,
    ownerName,
    parameters,
    serviceId,
    serviceName,
    requestBody,
    endpoint,
    status,
    responses,
    auth,
    serviceAuth,
    datasetActionOnEOF: _datasetActionOnEOF,
    datasetSharingMode: _datasetSharingMode
  } = res.data;

  // Load service servers if service ID is available
  if (serviceId) {
    await loadServiceServers(serviceId);
  }
  apisStatus.value = status?.value;

  // Set dataset configuration
  datasetActionOnEOF.value = _datasetActionOnEOF?.value || _datasetActionOnEOF || ActionOnEOF.RECYCLE;
  datasetSharingMode.value = _datasetSharingMode?.value || _datasetSharingMode || SharingMode.ALL_THREAD;

  // Load API permissions for archived APIs
  if (!isUnarchivedApi.value && auth && serviceAuth) {
    await loadApiAuth();
  }
  initApiInfo.value = res.data;

  if (res.data[requestSettingKey]) {
    const _setting = res.data[requestSettingKey];
    setting.value = {
      ..._setting,
      connectTimeout: (_setting.connectTimeout || '').replace('ms', ''),
      readTimeout: (_setting.readTimeout || '').replace('ms', '')
    };
  }
  saveParams.value.id = id;
  saveParams.value.operationId = operationId;
  saveParams.value.summary = summary;
  saveParams.value.ownerId = ownerId;
  saveParams.value.ownerName = ownerName;
  saveParams.value.serviceId = serviceId;
  saveParams.value.serviceName = serviceName;
  saveParams.value.responses = responses;

  apiMethod.value = method.value || method;
  if (isUnarchivedApi.value) {
    currentServer.value = res.data?.currentServer || { url: '' };
  } else {
    const availableServersFromPro = (res.data?.availableServers || []).map(i => ({ ...i, ...(i.extensions || {}) }));
    if (availableServersFromPro.length) {
      currentServer.value = availableServersFromPro.find(i => i[serverSourceKey] === ApiServerSource.CURRENT_REQUEST) || availableServersFromPro[0];
    } else {
      currentServer.value = { url: '' };
    }
    availableServers.value = (availableServersFromPro || []).filter(i => i[serverSourceKey] !== ApiServerSource.CURRENT_REQUEST);
  }
  defaultCurrentServer.value = JSON.parse(JSON.stringify(currentServer.value));

  // Extract query parameters from endpoint URL
  let queryStrParam = [];
  if (endpoint && endpoint.split('?')[1]) {
    const queryString = endpoint.split('?')[1];
    const queryStrArr = queryString.split('&');
    queryStrParam = queryStrArr.map(keyValue => {
      const [key, value] = keyValue.split('=');
      return getDefaultParams({ [valueKey]: value || '', name: key });
    });
  }
  state.assertions = assertions?.map(item => ({
    ...item,
    assertionCondition: item.assertionCondition?.value,
    extraction: item.extraction
      ? ({
          ...item.extraction,
          method: item.extraction.method.value
        })
      : null
  })) || [];
  assertNum.value = state.assertions?.length;

  state.authentication = authentication ? { ...authentication, ...(authentication.extensions || {}) } : { type: null };
  defaultAuthentication.value = JSON.parse(JSON.stringify(state.authentication));
  state.publishFlag = (status?.value === ApiStatus.RELEASED);
  state.secured = !!state.authentication.type || !!state.authentication.$ref;
  for (const key in parameters) {
    if (parameters[key].$ref) {
      const model = await getModelFromRef(parameters[key].$ref);
      if (model.name) {
        if (!model.schema.type) {
          if (model.schema.properties) {
            model.schema.type = SchemaType.object;
            if (typeof model[valueKey] === 'string') {
              try {
                model[valueKey] = JSON.parse(model[valueKey]);
              } catch {
                console.log(model[valueKey] + 'is not a json string');
              }
            }
          } else if (model.schema.items) {
            model.schema.type = SchemaType.array;
            if (typeof model[valueKey] === 'string') {
              try {
                model[valueKey] = JSON.parse(model[valueKey]);
              } catch {
                console.log(model[valueKey] + 'is not a json string');
              }
            }
          } else {
            model.schema.type = SchemaType.string;
          }
        }
        parameters[key] = { ...parameters[key], ...model };
      } else {
        parameters[key] = { name: undefined, schema: { type: SchemaType.string }, in: ParameterIn.query, [valueKey]: '' };
      }
    }
    if ([SchemaType.object, SchemaType.array, SchemaType.boolean, SchemaType.integer, SchemaType.number]
      .includes(parameters[key].schema?.type) && parameters[key]?.[valueKey] && typeof parameters[key]?.[valueKey] === 'string') {
      try {
        parameters[key][valueKey] = JSON.parse(parameters[key][valueKey]);
      } catch {
        console.log(parameters[key][valueKey] + 'is not an object');
      }
    }
    if (!parameters[key][valueKey] && parameters[key].schema?.type &&
      (parameters[key].schema?.[valueKey] || parameters[key].schema?.properties || parameters[key].schema?.items)) {
      parameters[key][valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(parameters[key].schema, { useValue: true });
    }
  }
  state.parameters = [...queryStrParam, ...(parameters?.filter(item => [ParameterIn.query, ParameterIn.path]
    .includes(item.in) && !!item.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }))];
  state.headerList = (parameters?.filter(i => i.in === ParameterIn.header && !!i.name) || [])
    .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

  apiUri.value = endpoint ? endpoint.split('?')[0] : undefined;
  const contentTypeIndex = state.headerList.findIndex(i => i.name === HTTP_HEADERS.CONTENT_TYPE);
  if (contentTypeIndex > -1) {
    contentType.value = state.headerList[contentTypeIndex]?.[valueKey];
    if (contentType.value) {
      state.headerList.splice(contentTypeIndex, 1);
    }
  }
  state.cookieList = (parameters?.filter(i => i.in === ParameterIn.cookie && !!i.name) || [])
    .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  state.requestBody = requestBody || {};
  if (res.data.resolvedRefModels) {
    resolvedRefModels.value = res.data.resolvedRefModels;
  }
};

// Parameterization logic - start
const datasetActionOnEOF = ref<ActionOnEOF>(ActionOnEOF.RECYCLE);
const datasetSharingMode = ref<SharingMode>(SharingMode.ALL_THREAD);

const targetInfoChange = (data: { id: string; datasetActionOnEOF: ActionOnEOF; datasetSharingMode: SharingMode; }) => {
  datasetActionOnEOF.value = data.datasetActionOnEOF;
  datasetSharingMode.value = data.datasetSharingMode;
};
// Parameterization logic - end

// Check if request body has data
const hasBodyContent = computed(() => {
  return !!contentType.value || !!state.requestBody.content;
});

// Get valid request headers
const getHeaderList = computed(() => {
  return state.headerList.filter(i => !!i.name);
});

const headerCount = computed(() => {
  let base = getHeaderList.value.length;
  if (authInHeader.value) {
    const length = Object.keys(authInHeader.value).length;
    base += length;
  }
  if (contentType.value) {
    base += 1;
  }
  return base;
});

const cookieCount = computed(() => {
  return state.cookieList.filter(i => !!i.name).length;
});

/**
 * Validate parameters
 */
const validateParam = () => {
  return !assertFormRef.value || assertFormRef.value?.validate();
};

/**
 * Get parameters, Execute this method after validation passes.
 */
const getParameter = async (isDebug = false) => {
  const { parameters, requestBody, headerList, cookieList, authentication, secured } = state;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { content } = requestBody;
  if (!isDebug) {
    const exceedFileSize = requestBodyRef.value.ifExceedSize();
    if (exceedFileSize) {
      Object.keys(content).forEach(key => {
        if (content[key]?.schema?.format === 'binary') {
          delete requestBody.content[key].schema[valueKey];
          delete requestBody.content[key].schema[fileNameKey];
        }
        if (key === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
          if (content[key]?.schema?.properties) {
            Object.keys(content[key]?.schema?.properties).forEach(name => {
              if (content[key].schema.properties[name].format === 'binary') {
                const type = content[key].schema.properties[name].type;
                requestBody.content[key].schema.properties[name][valueKey] = (type === SchemaType.array ? [] : undefined);
              }
            });
          }
        }
      });
    }
  }
  const headers = headerList.filter(i => !!i.name || i.description).map(i => {
    delete i.key;
    const header = { ...i };
    if (header.$ref) {
      delete header[valueKey];
    }
    return {
      ...header,
      in: ParameterIn.header
    };
  });
  if (contentType.value) {
    headers.unshift({ name: HTTP_HEADERS.CONTENT_TYPE, in: ParameterIn.header, [valueKey]: contentType.value, [enabledKey]: true });
  }
  let protocol = '';
  if (currentServer.value?.url) {
    try {
      const _server = new URL(currentServer.value.url);
      protocol = _server.protocol.replace(':', '');
    } catch {
      protocol = 'http';
      if (!currentServer.value.url.startsWith('http') && !currentServer.value[idKey]) {
        currentServer.value.url = 'http://' + currentServer.value.url;
      }
    }
  } else {
    protocol = 'http';
  }
  if (currentServer.value[idKey]) {
    const defaultServer = availableServers.value.find(item => item[idKey] === currentServer.value[idKey]);
    if (defaultServer) {
      if (defaultServer.url !== currentServer.value.url) {
        delete currentServer[idKey];
      }
    }
  }
  let assertions: any[] = [];
  if (typeof assertFormRef.value?.getData === 'function') {
    assertions = assertFormRef.value.getData().data;
  }
  const currentServerICopy = JSON.parse(JSON.stringify(currentServer.value));
  delete currentServerICopy[serverSourceKey];
  return {
    ...saveParams.value,
    assertions,
    parameters: [...parameters, ...headers, ...cookieList.filter(i => i.name)],
    requestBody: requestBody,
    endpoint: apiUri.value,
    currentServer: currentServerICopy,
    method: apiMethod.value,
    authentication: secured ? authentication : { type: null },
    secured,
    protocol,
    [requestSettingKey]: {
      ...setting.value,
      connectTimeout: setting.value.connectTimeout + 'ms',
      readTimeout: setting.value.readTimeout + 'ms'
    }
  };
};

/**
 * Build real URI with path and query parameters
 * <p>
 * Constructs the final URI by replacing path parameters in the endpoint
 * and appending query parameters. Handles object and array parameter values.
 * </p>
 * @param pathParams - Path parameters to replace in the URI
 * @param queryParams - Query parameters to append to the URI
 * @returns The constructed URI string
 */
const getRealUri = (pathParams: Record<string, any>, queryParams: Record<string, any>) => {
  let processedUri = apiUri.value || '';
  const paths = Object.keys(pathParams || {}).map(key => ({ name: key, [valueKey]: pathParams?.[key] }));

  // Process path parameters (convert objects/arrays to strings)
  paths.forEach(item => {
    if (typeof item[valueKey] === 'object') {
      if (Object.prototype.toString.call(item[valueKey]) === '[object Array]') {
        item[valueKey] = item[valueKey] ? item[valueKey].map(JSON.stringify).join(',') : '';
      } else {
        item[valueKey] = item[valueKey] ? Object.entries(item[valueKey]).map(([key, value]) => `${key}=${JSON.stringify(value)}`).join(',') : '';
      }
    }
  });
  if (paths?.length) {
    let pattern: RegExp;
    // Replace path placeholders with parameter values
    const endpoint = paths?.reduce((prevValue, currentValue) => {
      pattern = new RegExp('{' + currentValue.name + '}', 'gi');
      if (pattern.test(prevValue)) {
        prevValue = prevValue.replace(pattern, currentValue[valueKey] || '');
      }
      return prevValue;
    }, processedUri) || '';
    // Normalize path by removing leading slashes and adding single leading slash
    processedUri = '/' + endpoint.replace(/^\/+/, '');
  }

  // Append query parameters
  const queryUri = qs.stringify(queryParams, { allowDots: true, encode: true });
  if (processedUri.includes('?')) {
    processedUri = processedUri + '&' + queryUri;
  } else if (queryUri) {
    processedUri = processedUri + '?' + queryUri;
  }
  return processedUri;
};

const activeKey = ref('parameters');
let controller: AbortController;
/**
 * Send HTTP request
 * <p>
 * Main request handler that validates parameters, prepares request data,
 * and sends the request either through direct HTTP client or WebSocket proxy.
 * Also handles parameter validation and assertion execution.
 * </p>
 */
const sendRequest = async () => {
  // Handle request cancellation if already in progress
  if (loading.value) {
    handleAbort();
  }
  loading.value = true;
  assertionVariableExtra.value = {};

  // Validate URL configuration
  if (!currentServer.value.url && !apiUri.value) {
    loading.value = false;
    notification.error({ message: t('service.apis.errors.invalidUrl'), description: t('service.apis.errors.invalidUrlDescription') });
    return;
  }
  // Prepare request body data
  let requestBodyContents = await requestBodyRef.value.getBodyData(state.requestBody, contentType.value);
  let formData: any[] = [];
  const strParam: string[] = [];
  if (contentType.value === CONTENT_TYPE_KEYS.FORM_URLENCODED || contentType.value === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
    formData = requestBodyContents;
  } else if (typeof requestBodyContents === 'string') {
    strParam.push(requestBodyContents);
  }

  // Prepare authentication data
  const auth = authorizationRef.value ? await authorizationRef.value.getAuthData(state.authentication) : [{}];
  const authParamData = Object.keys(auth?.[0] || {}).map(key => {
    if (typeof auth?.[0]?.[key] !== 'object') {
      return { name: key, [valueKey]: auth?.[0]?.[key] };
    } else {
      const { username, password } = auth?.[0]?.[key] || {};
      strParam.push(username || '');
      strParam.push(password || '');
    }
    return undefined;
  }).filter(Boolean); // Header authentication data
  const authQueryData = Object.keys(auth?.[1] || {}).map(key => ({ name: key, [valueKey]: auth?.[1]?.[key] })); // Query authentication data

  const headerCookieData = [...state.headerList, ...state.cookieList].filter(i => i.name && i[valueKey] && i[enabledKey]);
  // Prepare parameter data
  const querPathData = state.parameters.filter(i => i.name && i[enabledKey]);

  // Prepare assertion data
  let assertions: any[] = [];
  let asserVariableStr: string[] = [];
  if (typeof assertFormRef.value?.getData === 'function') {
    const assertFormData = assertFormRef.value.getData();
    assertions = assertFormData.data;
    asserVariableStr = assertFormData.variables;
  } else {
    assertions = state.assertions;
  }

  // Replace mock functions and variables in parameters
  const result = await apiUtils.replaceFuncValue(
    {
      parameter: [querPathData, formData, headerCookieData, authParamData, authQueryData],
      str: strParam,
      variableStr: asserVariableStr
    }, allFuncNames.value,
    !isUnarchivedApi.value ? saveParams.value.id : undefined,
    'API', { ignoreErr: false }
  );
  if (!result || result === true) {
    loading.value = false;
    return;
  }
  const [funcValues, strValues, variableValues = []] = result;
  if (strValues.length) {
    if (strValues.length === 1) {
      requestBodyContents = strValues[0];
    }
    if (strValues.length === 2) {
      funcValues[3] = [
        ...funcValues[3],
        { name: 'Authorization', [valueKey]: 'basic ' + encode(strValues[0], strValues[1]) }
      ];
    }
    if (strValues.length === 3) {
      requestBodyContents = strValues[0];
      funcValues[3] = [
        ...funcValues[3],
        { name: 'Authorization', [valueKey]: 'basic ' + encode(strValues[1], strValues[2]) }
      ];
    }
  }
  // Validate assertion data
  const validateAssertData = validateParam();

  // Validate parameter data and format if enabled
  if (setting.value.enableParamValidation) {
    const validatedQuery = validateQueryParameter(funcValues[0]);
    if (!validatedQuery) {
      requestParamsRef.value.validate();
      notification.error(t('service.apis.errors.parameterValidationFailed'));
    }
    const validatedHeader = validateQueryParameter(funcValues[2].filter(i => i.in === ParameterIn.header));
    if (!validatedHeader) {
      requestHeaderRef.value.validate();
      notification.error(t('service.apis.errors.headerValidationFailed'));
    }
    const validatedCookie = validateQueryParameter(funcValues[2].filter(i => i.in === ParameterIn.cookie));
    if (!validatedCookie) {
      requestCookieRef.value.validate();
      notification.error(t('service.apis.errors.cookieValidationFailed'));
    }
    const validatedFom = validateBodyForm(funcValues[1].filter(item => item.format !== 'binary' && item.name));
    if (!validatedFom) {
      activeKey.value = 'request-body';
      requestBodyRef.value.validate();
      notification.error(t('service.apis.errors.bodyValidationFailed'));
    }
    if (!validateAssertData) {
      notification.error(t('service.apis.errors.assertionValidationFailed'));
    }
    if (!validatedQuery || !validatedFom || !validatedCookie || !validatedHeader || !validateAssertData) {
      loading.value = false;
      return;
    }
    requestParamsRef.value.validate(false);
    requestHeaderRef.value.validate(false);
    requestCookieRef.value.validate(false);
    requestBodyRef.value.validate(false);
  } else {
    requestParamsRef.value.validate(false);
    requestHeaderRef.value.validate(false);
    requestCookieRef.value.validate(false);
    requestBodyRef.value.validate(false);
    if (!validateAssertData) {
      loading.value = false;
      notification.error(t('service.apis.errors.assertionValidationFailed'));
      return;
    }
  }

  // Assemble request body data
  const showRequestBody: Record<string, any> = {};
  let requestBody;
  if ((contentType.value === CONTENT_TYPE_KEYS.FORM_URLENCODED ||
    contentType.value === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) && funcValues[1]?.length) {
    const bodyDataArr = funcValues[1];
    if (contentType.value === CONTENT_TYPE_KEYS.FORM_URLENCODED) {
      requestBody = '';
      const content = {};
      bodyDataArr.forEach(item => {
        content[item.name] = travelEmptyObjToString(item[valueKey]);
      });
      requestBody = qs.stringify(content, { allowDots: true });
      const keyValue = requestBody.split('&');
      showRequestBody.urlencoded = {};
      keyValue.forEach(str => {
        let [key, value] = str.split('=');
        key = decodeURIComponent(key);
        showRequestBody.urlencoded[key] = decodeURIComponent(value || '');
      });
    }
    if (contentType.value === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
      showRequestBody.formData = {};
      requestBody = new FormData();
      bodyDataArr.forEach(item => {
        if (!item.name) {
          return;
        }
        if (item.format !== 'binary') {
          if (item.format?.includes('xml')) {
            requestBody.append(item.name, XML(item[valueKey]));
            showRequestBody.formData[item.name] = XML(item[valueKey]);
          } else {
            requestBody.append(item.name, item[valueKey]);
            showRequestBody.formData[item.name] = typeof item[valueKey] === 'object' ? JSON.stringify(item[valueKey]) : item[valueKey];
          }
        } else {
          if (item.type === SchemaType.array) {
            showRequestBody.formData[item.name] = '<binary array>';
            item[valueKey].forEach(file => {
              requestBody.append(item.name, file?.file || '', file[fileNameKey] || '');
            });
          } else {
            if (item[valueKey]?.file) {
              requestBody.append(item.name, item[valueKey]?.file || '', item[valueKey]?.[fileNameKey] || '');
            }
            showRequestBody.formData[item.name] = '<binary>';
          }
        }
      });
    }
  } else {
    requestBody = requestBodyContents;
    if (requestBody instanceof File) {
      showRequestBody.body = '<binary>';
    } else {
      showRequestBody.body = requestBodyContents || '';
    }
  }

  // Prepare path parameters in JSON format
  const pathJson = {};
  const pathArr = funcValues[0].filter(i => i.in === ParameterIn.path);
  pathArr.forEach(i => {
    pathJson[i.name] = i[valueKey];
  });

  // Assemble query data (query parameters + authentication query)
  const queryJson = {};
  const queryArr = funcValues[0].filter(i => i.in === ParameterIn.query).concat(funcValues[4]);
  queryArr.forEach(i => {
    queryJson[i.name] = travelEmptyObjToString(i[valueKey]);
  });
  const apiPathQuery = getRealUri(pathJson, queryJson);
  const serverUrl = getServerData(currentServer.value);

  // Parse query parameters for request display
  const requestQueryJson = qs.parse(apiPathQuery.split('?')[1] || '');

  // Assemble final request URL
  let apiHref = '';
  if (!serverUrl.endsWith('/') && apiPathQuery && apiPathQuery.split('?')[0] && !apiPathQuery.startsWith('/')) {
    apiHref = serverUrl + '/' + apiPathQuery;
  } else {
    apiHref = serverUrl + apiPathQuery;
  }
  if (!apiHref.startsWith('http')) {
    apiHref = 'http://' + apiHref;
  }
  uuid = utils.uuid('api-item');

  // Assemble request headers
  let headers: Record<string, string> = {};
  const headList = funcValues[2].filter(i => i.in === ParameterIn.header);
  const cookieList = funcValues[2].filter(i => i.in === ParameterIn.cookie);
  headList.forEach(item => {
    if (typeof item[valueKey] === 'object') {
      // Use encodeURIComponent for Chinese content to ensure local requests can be sent
      if (Object.prototype.toString.call(item[valueKey]) === '[object Array]') {
        headers[item.name] = item[valueKey]
          ? item[valueKey].map(value => value && apiUtils.containsAllAscii(value) ? value : value ? encodeURIComponent(value) : '').join(',')
          : '';
      } else {
        headers[item.name] = item[valueKey]
          ? Object.entries(item[valueKey]).map(([key, value]) => `${key}=${value && apiUtils.containsAllAscii(String(value)) ? String(value) : value ? encodeURIComponent(String(value)) : ''}`).join(',')
          : '';
      }
    } else {
      headers[item.name] = item[valueKey] && apiUtils.containsAllAscii(item[valueKey])
        ? item[valueKey]
        : item[valueKey] ? encodeURIComponent(item[valueKey]) : '';
    }
  });
  const cookieStrs = cookieList.map(item => {
    if (typeof item[valueKey] === 'object') {
      if (Object.prototype.toString.call(item[valueKey]) === '[object Array]') {
        const value = item[valueKey].map(value => value ? encodeURIComponent(value) : '').join(',');
        return `${item.name}=${value || ''}`;
      } else {
        const value: string[] = [];
        Object.entries(item[valueKey]).forEach(([key, val]) => {
          value.push(key);
          value.push(encodeURIComponent(String(val)));
        });
        return `${item.name}=${value.join(',')}`;
      }
    } else {
      return `${item.name}=${item[valueKey] || ''}`;
    }
  });
  if (cookieStrs.length) {
    headers.Cookie = cookieStrs.join('; ');
  }

  const authorizationData = {};
  funcValues[3].forEach(i => {
    authorizationData[i.name] = i[valueKey];
  });
  if (auth?.length) {
    headers = {
      ...headers,
      ...authorizationData
    };
  }
  if (contentType.value) {
    headers[HTTP_HEADERS.CONTENT_TYPE] = contentType.value;
  }

  // Request configuration
  const { connectTimeout, readTimeout, retryNum, maxRedirects } = setting.value;
  const isExceedRequestSize = requestBodyRef.value.ifExceedRequestSize();

  // Send request via direct HTTP client or WebSocket proxy
  if (!ws || isExceedRequestSize) {
    controller = new AbortController();
    const signal = controller.signal;
    const axiosConfig = {
      responseType: 'blob',
      url: apiHref,
      method: apiMethod.value.toLowerCase(),
      data: requestBody,
      headers: {
        accept: '*/*',
        ...headers
      },
      signal,
      timeout: (+connectTimeout) + (+readTimeout),
      maxRedirects: maxRedirects,
      maxRetries: retryNum
    };
    const responses = await httpClient.request(axiosConfig);
    setErrTitle();
    await handleHttpResponse(responses, {
      query: requestQueryJson,
      header: headers,
      path: pathJson,
      requestBody: showRequestBody,
      queryString: apiPathQuery.split('?')[1] || '',
      assertions,
      variableValues
    });
    openToolBar();
  }
  // Handle WebSocket connection errors
  else if (ws.readyState !== 1) {
    loading.value = false;
    notification.error(t('service.apis.errors.proxyNotConnected'));
  }
  // Send request via WebSocket proxy
  else if (ws) {
    const { currentServer, requestBody, ...params } = await getParameter(true);
    if (!params?.authentication.type && !params?.authentication.$ref) {
      delete (params as any).authentication;
    } else if (params?.authentication.$ref && saveParams.value.serviceId) {
      const [_error, resp] = await services.getComponentRef(saveParams.value.serviceId, params?.authentication.$ref);
      params.authentication = JSON.parse(resp.data.model);
    }
    delete params.responses;
    if (requestBody?.content) {
      Object.keys(requestBody.content).forEach(key => {
        if (rawTypeOptions.includes(key)) {
          if (requestBody.content[key][valueKey]) {
            delete requestBody.content[key].schema;
          }
        }
      });
    }

    // Filter out disabled assertions
    if (params.assertions?.length) {
      params.assertions = params.assertions.filter((item) => item.enabled);
    }

    const resolvedRefModels = {
      ...requestParamsRef.value.getModelResolve(),
      ...requestHeaderRef.value.getModelResolve(),
      ...requestBodyRef.value.getModelResolve(),
      ...authorizationRef.value.getModelResolve()
    };
    Object.keys(resolvedRefModels).forEach(key => {
      if (typeof resolvedRefModels[key] !== 'string') {
        resolvedRefModels[key] = JSON.stringify(resolvedRefModels[key]);
      }
    });
    if (!isUnarchivedApi.value) {
      const conditions = params.assertions.filter(item => item.condition).map(item => item.condition);
      const { extra } = assertUtils.proxy.execute(conditions, variableValues);
      assertionVariableExtra.value = extra;
    }
    const jsonStr = JSON.stringify({
      ...params,
      requestBody,
      server: currentServer,
      requestId: uuid,
      messageType: 'HttpRequestProxy',
      endpoint: params.endpoint,
      resolvedRefModels,
      variables: [...variableValues]
    });
    ws.send(jsonStr);
    setErrTitle();
    openToolBar();
  }
};

/**
 * Abort current request
 * <p>
 * Cancels the ongoing request either by aborting the HTTP controller
 * or clearing the WebSocket request ID.
 * </p>
 */
const handleAbort = () => {
  loading.value = false;
  // Handle WebSocket request cancellation
  if (ws) {
    uuid = '';
    // ws.close();
  } else {
    // Handle HTTP request cancellation
    controller.abort();
  }
};

/**
 * Handle HTTP response for local requests without proxy
 * <p>
 * Processes the response data, updates the response state, and handles both successful
 * and error responses. Also executes assertions if configured.
 * </p>
 * @param resp - The HTTP response object from axios
 * @param request - The request context containing query, headers, path, and body data
 */
const handleHttpResponse = async (resp, request) => {
  loading.value = false;
  responseError.show = false;
  responseError.value = undefined;
  responseState.performance = resp.performance;
  assertResult.value = undefined;

  // Handle network errors (status 0)
  if (resp.request.status === 0) {
    responseError.show = true;
    responseError.value = resp.message;
    responseState.config = {
      url: decodeURIComponent(resp.config.url),
      method: apiMethod.value,
      status: 0,
      queryString: request.queryString
    };
    responseState.headers = resp.config.headers;
  }
  // Handle HTTP error responses (4xx, 5xx)
  else if (resp.request.status < 200 || resp.request.status >= 300) {
    responseState.data = await convertBlob(resp.response.data);
    responseState.config = {
      url: decodeURIComponent(resp.config.url),
      method: apiMethod.value,
      queryString: request.queryString,
      forms: request.header[HTTP_HEADERS.CONTENT_TYPE] === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA
        ? request.requestBody.formData
        : request.header[HTTP_HEADERS.CONTENT_TYPE] === CONTENT_TYPE_KEYS.FORM_URLENCODED ? request.requestBody.urlencoded : {},
      body: request.requestBody.body || ''
    };
    responseState.headers = resp.response.headers;
    responseState.status = resp.response.status;
    responseState.size = resp.response.data.size;
    responseState.requestHeaders = resp?.config?.headers || {};
    const cookie = resp.response.headers?.[HTTP_HEADERS.SET_COOKIE] ||
      resp.response.headers?.[HTTP_HEADERS.SET_COOKIE_LOWER];
    responseState.cookie = cookie ? (Array.isArray(cookie) ? cookie : [cookie]) : [];
  }
  // Handle successful responses (2xx)
  else {
    responseState.data = await convertBlob(resp.data);
    if (responseState.data instanceof Blob) {
      responseState.contentEncoding = 'base64';
    } else {
      responseState.contentEncoding = undefined;
    }
    responseState.config = {
      url: decodeURIComponent(resp.config.url),
      method: apiMethod.value,
      queryString: request.queryString,
      forms: request.header[HTTP_HEADERS.CONTENT_TYPE] === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA
        ? request.requestBody.formData
        : request.header[HTTP_HEADERS.CONTENT_TYPE] === CONTENT_TYPE_KEYS.FORM_URLENCODED
          ? request.requestBody.urlencoded
          : {},
      body: request.requestBody.body || ''
    };
    responseState.headers = resp.headers;
    responseState.status = resp.status;
    responseState.size = resp.data.size;
    const requestHead = resp?.config?.headers || {};
    responseState.requestHeaders = requestHead;
    const cookie = resp.headers?.[HTTP_HEADERS.SET_COOKIE] ||
      resp.headers?.[HTTP_HEADERS.SET_COOKIE_LOWER];
    responseState.cookie = cookie ? (Array.isArray(cookie) ? cookie : [cookie]) : [];
  }

  // Execute assertions if no error occurred
  if (!responseError.show) {
    // Prepare local request info for assertion execution
    localRequestInfo.responseHeader = responseState.headers;
    localRequestInfo.responseBody = { size: responseState.size || 0, data: responseState.data };
    localRequestInfo.status = responseState.status;
    localRequestInfo.duration = responseState?.performance?.duration || 0;
    localRequestInfo.query = request.query;
    localRequestInfo.path = request.path;
    localRequestInfo.header = request.header;
    localRequestInfo.form = request.header[HTTP_HEADERS.CONTENT_TYPE] === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA
      ? request.requestBody.formData
      : request.header[HTTP_HEADERS.CONTENT_TYPE] === CONTENT_TYPE_KEYS.FORM_URLENCODED
        ? request.requestBody.urlencoded
        : undefined;
    localRequestInfo.rawBody = request.requestBody.body || undefined;

    // Execute assertions with the prepared request context
    assertResult.value = await assertUtils.assert.execute(localRequestInfo, request.assertions, request.variableValues);
  }
};

const assertionVariableExtra = ref<any>({}); // Variables for replacing execution condition expressions

/**
 * Handle WebSocket proxy response
 * <p>
 * Processes responses received through WebSocket proxy, parses JSON data,
 * and updates the response state accordingly.
 * </p>
 */
const onResponse = async () => {
  loading.value = false;
  assertResult.value = undefined;
  let response: any = {};

  // Parse JSON response from WebSocket
  try {
    response = JSON.parse(props.response);
  } catch {
    responseError.show = true;
    responseError.value = props.response;
    responseState.config = {};
    responseState.config = {};
    responseState.data = null;
    responseState.status = 0;
    responseState.size = 0;
    responseState.cookie = [];
    responseState.requestHeaders = [];
    responseState.performance = {} as PerformanceEntry;
    return;
  }
  if (typeof response === 'object') {
    // Handle proxy errors (status 0)
    if (+response.response.status === 0) {
      responseError.show = true;
      assertionVariableExtra.value = {};
      responseError.value = response.response?.rawContent || '';
      responseState.headers = {};
      responseState.config = {};
      responseState.data = null;
      responseState.status = 0;
      responseState.size = 0;
      responseState.cookie = [];
      responseState.requestHeaders = response.response.headers || [];
      responseState.performance = {} as PerformanceEntry;
    }
    // Handle successful proxy responses
    else {
      responseState.config = {
        ...response.request0,
        url: response.request0.url + (response.request0.queryString ? ('?' + response.request0.queryString) : '')
      };

      // Parse header array into object format
      // Headers are stored as [key1, value1, key2, value2, ...] array
      const header = {};
      (response.response?.headerArray || []).forEach((value, idx, arr) => {
        if (idx % 2 === 0) {
          if (header[value]) {
            if (typeof header[value] === 'string') {
              header[value] = [header[value], arr[idx + 1]];
            } else {
              header[value].push(arr[idx + 1]);
            }
          } else {
            header[value] = arr[idx + 1];
          }
        }
      });
      responseState.requestHeaders = response.request0?.headers || [];
      responseState.headers = header;
      responseState.data = response.response.rawContent;
      responseState.contentEncoding = response.response?.contentEncoding;
      // Convert base64 data to blob if needed
      if (response.response.contentEncoding === 'base64') {
        const mime = header[HTTP_HEADERS.CONTENT_TYPE] || header['content-type'] || header['content-Type'] || header['CONTENT-TYPE'];
        responseState.data = dataURLtoBlob(responseState.data, mime);
      }
      responseState.status = response.response.status;
      responseState.size = response.response.size;
      responseState.performance = {
        ...response.response.timeline,
        duration: +response.response.timeline.total || 0
      };
      const cookieStr = header?.[HTTP_HEADERS.SET_COOKIE] || header?.[HTTP_HEADERS.SET_COOKIE_LOWER];
      responseState.cookie = cookieStr ? [cookieStr] : [];
      responseError.show = false;

      assertResult.value = await setAssertResult(response);
    }
  }
};

const localRequestInfo = reactive<Parameter>({
  responseHeader: {},
  responseBody: {
    data: undefined,
    size: 0
  },
  query: {},
  path: {},
  header: {},
  form: undefined,
  duration: 0,
  status: undefined
});

const toolbarMenus = computed(() => {
  if (props.ws) {
    return menus;
  }
  return menus.filter(i => i.value !== 'time');
});

const errorTitle = ref(t('service.apis.errors.requestErrorWithProxy'));

const setErrTitle = () => {
  if (ws) {
    errorTitle.value = t('service.apis.errors.requestErrorWithProxy');
    return;
  }
  errorTitle.value = t('service.apis.errors.requestErrorWithCors');
};

const getHeaderParams = (data: string[], name: string): string => {
  if (!data?.length || !name) {
    return '';
  }

  const result: string[] = [];
  const lowerName = name.toLowerCase();
  for (let i = 0, len = data.length; i < len; i = i + 2) {
    if (lowerName === data[i].toLowerCase()) {
      result.push(data[i + 1]);
    }
  }

  return result.join(',');
};

/**
 * Get assertion value by type
 * <p>
 * Extracts the appropriate value from response data based on assertion type
 * and condition. Handles different assertion types like body, header, status, etc.
 * </p>
 * @param assertionCondition - The assertion condition type
 * @param type - The assertion type (body, header, status, etc.)
 * @param data - Response data containing various metrics
 * @param parameterName - Parameter name for header assertions
 * @returns Object containing extracted data, message, and error message
 */
const getValueByType = (assertionCondition: AssertionCondition, type: AssertionType, data: {
  bodySize: number;
  size: number;
  duration: number;
  status: number;
  responseHeader: string[];
  rawContent: string;
  extractValue: string; // Left value for regex, xpath, jsonpath matching conditions
}, parameterName: string): {
  data: string | null;
  message: string;
  errorMessage: string;
} => {
  if (type === AssertionType.BODY) {
    if ([AssertionCondition.REG_MATCH, AssertionCondition.XPATH_MATCH, AssertionCondition.JSON_PATH_MATCH].includes(assertionCondition)) {
      return {
        data: data.extractValue,
        message: '',
        errorMessage: ''
      };
    }

    return {
      data: data.rawContent,
      message: '',
      errorMessage: ''
    };
  }

  if (type === AssertionType.BODY_SIZE) {
    return {
      data: data.bodySize + '',
      message: '',
      errorMessage: ''
    };
  }

  if (type === AssertionType.DURATION) {
    return {
      data: data.duration + '',
      message: '',
      errorMessage: ''
    };
  }

  if (type === AssertionType.HEADER) {
    if ([AssertionCondition.REG_MATCH, AssertionCondition.XPATH_MATCH, AssertionCondition.JSON_PATH_MATCH].includes(assertionCondition)) {
      return {
        data: data.extractValue,
        message: '',
        errorMessage: ''
      };
    }

    return {
      data: getHeaderParams(data.responseHeader, parameterName),
      message: '',
      errorMessage: ''
    };
  }

  if (type === AssertionType.SIZE) {
    return {
      data: data.size + '',
      message: '',
      errorMessage: ''
    };
  }

  return {
    data: data.status + '',
    message: '',
    errorMessage: ''
  };
};

/**
 * Set assertion results from proxy response
 * <p>
 * Processes assertion results from WebSocket proxy response, evaluates conditions,
 * and prepares the final assertion result data for display.
 * </p>
 * @param responseData - Response data from WebSocket proxy containing assertions
 * @returns Array of processed assertion results
 */
const setAssertResult = async (responseData) => {
  const assertions: AssertResult[] = responseData.assertions;
  if (!assertions?.length) {
    return;
  }

  const result: AssertResult[] = [];
  // Process each assertion result
  for (let i = 0, len = assertions.length; i < len; i++) {
    const {
      name,
      assertionCondition,
      condition,
      expected,
      extraction,
      type,
      parameterName,
      extractValue,
      result: _assertResult
    } = assertions[i];

    let _condition: ConditionResult = {
      failure: false, // Execution result
      name: '', // Extracted variable name
      conditionMessage: '', // Reason for assertion expression error
      failureMessage: '', // Reason for extraction failure
      value: '', // Extracted variable value
      ignored: false, // Whether to ignore this assertion
      message: t('service.case.debugModal.conditionMessageEmpty')
    };

    let ignored = true;
    let failure = false;
    if (_assertResult) {
      ignored = _assertResult.ignored || false;
      failure = _assertResult.failure;

      if (!condition) {
        _condition = {
          failure: false, // Execution result
          name: '', // Extracted variable name
          conditionMessage: '', // Reason for assertion expression error
          failureMessage: '', // Reason for extraction failure
          value: '', // Extracted variable value
          ignored: false, // Whether to ignore this assertion
          message: t('service.case.debugModal.conditionMessageEmpty')
        };
      } else {
        const matchsMap = assertionVariableExtra.value?.matchs || {};
        const varMap = assertionVariableExtra.value?.vars || {};
        const matchs = matchsMap[condition];
        if (!matchs) {
          _condition = {
            failure: false, // Execution result
            name: '', // Extracted variable name
            conditionMessage: t('service.case.debugModal.conditionMsgFormat'), // Reason for assertion expression error
            failureMessage: t('service.case.debugModal.conditionMsgFormatFail'), // Reason for extraction failure
            value: '', // Extracted variable value
            ignored: true, // Whether to ignore this assertion
            message: t('service.case.debugModal.conditionMsgFormatErr')
          };
        } else {
          const [leftOperand] = matchs;
          const varValue = varMap[leftOperand];
          let value = '';
          let failureMessage = '';
          if (varValue) {
            value = varValue.value;
            failureMessage = varValue.failureMessage;
          } else {
            failureMessage = t('service.case.debugModal.conditionMsgDefineFail');
            value = leftOperand;
          }

          if (ignored) {
            _condition = {
              failure: true, // Execution result
              name: leftOperand, // Extracted variable name
              conditionMessage: '', // Reason for assertion expression error
              failureMessage, // Reason for extraction failure
              value, // Extracted variable value
              ignored: true, // Whether to ignore this assertion
              message: t('service.case.debugModal.conditionMsgIgnore')
            };
          } else {
            _condition = {
              failure: false, // Execution result
              name: leftOperand, // Extracted variable name
              conditionMessage: '', // Reason for assertion expression error
              failureMessage, // Reason for extraction failure
              value, // Extracted variable value
              ignored: false, // Whether to ignore this assertion
              message: t('service.case.debugModal.conditionMsgExec')
            };
          }
        }
      }
    }

    // Expected value processing
    const expectedData: { data: string | null; message: string; errorMessage: string; } = { data: expected, message: '', errorMessage: '' };
    if (extraction) {
      expectedData.data = extraction.finalValue;
      if (utils.isEmpty(expectedData.data)) {
        if (expectedData.data === undefined) {
          expectedData.data = null;
        }
      } else if (typeof expectedData.data === 'object') {
        expectedData.data = JSON.stringify(expectedData.data);
      } else if (typeof expectedData.data !== 'string') {
        expectedData.data = expectedData.data + '';
      }
    }

    const data: {
      bodySize: number;
      size: number;
      duration: number;
      status: number;
      responseHeader: string[];
      rawContent: string;
      extractValue: string;
    } = {
      extractValue: extractValue || '',
      bodySize: 0,
      size: 0,
      duration: 0,
      status: 0,
      responseHeader: [],
      rawContent: ''
    };

    if (responseData) {
      const _response = responseData.response;
      data.bodySize = _response.bodySize;
      data.size = _response.size;
      data.status = _response.status;
      data.duration = _response.timeline?.total;
      data.responseHeader = _response.headerArray;
      data.rawContent = _response.rawContent;
    }

    const realValueData = getValueByType(assertionCondition, type.value, data, parameterName);
    const _realData = realValueData.data;
    if (utils.isEmpty(_realData)) {
      if (_realData === undefined) {
        realValueData.data = null;
      }
    } else if (typeof _realData === 'object') {
      realValueData.data = JSON.stringify(_realData);
    } else if (typeof _realData !== 'string') {
      realValueData.data = _realData + '';
    }

    const _result: AssertResult = {
      name,
      parameterName,
      condition,
      assertionCondition,
      type: (type as { message: string; value: AssertionType }).value,
      extraction: !!extraction,
      _condition,
      result: {
        expectedData,
        failure,
        realValueData,
        message: _assertResult?.message
      }
    };
    result.push(_result);
  }
  return result;
};

/**
 * Open response toolbar
 * <p>
 * Opens the response toolbar and sets the default tab to 'response'
 * if not already spread or no current tab is selected.
 * </p>
 */
const openToolBar = () => {
  if (!toolbarRef.value.isSpread || !currentTab.value) {
    toolbarRef.value.handleSelected({ value: 'response' });
  }
  if (height.value < 300) {
    height.value = 300;
  }
};

/**
 * Handle toolbar menu change
 * <p>
 * Updates the current tab when a different toolbar menu is selected.
 * </p>
 * @param menuKey - The selected menu key
 */
const toolbarChange = (menuKey: string) => {
  currentTab.value = menuKey;
};

/**
 * Save API data
 * <p>
 * Handles saving API data based on whether it's an archived or unarchived API.
 * </p>
 */
const save = (): void => {
  if (isUnarchivedApi.value) {
    saveUnarchived();
  } else {
    autoSave();
  }
};

/**
 * Archive API
 * <p>
 * Opens the save drawer for archiving the current API.
 * </p>
 */
const archivedApi = () => {
  drawerRef.value.open('save');
};

/**
 * Auto-save archived API
 * <p>
 * Automatically saves changes to an archived API if the user has modify permissions
 * and the data has actually changed.
 * </p>
 */
const autoSave = async () => {
  if (state.publishFlag) {
    notification.warning(t('service.apis.notifications.apiPublishedWarning'));
  }
  if (!validateParam()) {
    return;
  }
  let params = await getParameter();
  params = JSON.parse(JSON.stringify(params));
  // Compare old and new values to avoid unnecessary saves
  if (utils.deepCompare(initParams, JSON.parse(JSON.stringify(params)))) {
    return;
  }
  if (apiAuths.value.includes(ApiPermission.MODIFY) && !state.publishFlag) {
    if (requestHeaderRef.value) {
      await requestHeaderRef.value.updateComp();
    }
    if (requestParamsRef.value) {
      await requestParamsRef.value.updateComp();
    }
    if (requestBodyRef.value) {
      await requestBodyRef.value.updateComp();
    }
    params.parameters = travelXcValueToString(params.parameters);
    params.requestBody = travelXcValueToString(params.requestBody);
    const [error] = await apis.updateApi([params]);
    initParams = JSON.parse(JSON.stringify(params));
    if (error) {
      return;
    }
    notification.success(t('actions.tips.saveSuccess'));
  }
};

/**
 * Save/update unarchived API content
 * <p>
 * Opens the save drawer for unarchived APIs to allow saving or updating
 * the API content.
 * </p>
 */
const saveUnarchived = async (): Promise<void> => {
  drawerRef.value.open('saveUnarchived');
};

const isResponseEmpty = computed(() => {
  return !responseState.config || !Object.keys(responseState.config).length;
});

/**
 * Copy API URL to clipboard
 * <p>
 * Constructs the complete API URL with path and query parameters,
 * then copies it to the clipboard for easy sharing.
 * </p>
 */
const copyUrl = async () => {
  const pathJson = {};
  const pathArr = state.parameters.filter(i => i.in === ParameterIn.path);
  pathArr.forEach(i => {
    pathJson[i.name || ''] = i[valueKey];
  });

  // Assemble query data (query parameters + authentication query)
  const queryJson = {};
  const quryyArr = state.parameters.filter(i => i.in === ParameterIn.query);
  quryyArr.forEach(i => {
    queryJson[i.name || ''] = travelEmptyObjToString(i[valueKey]);
  });
  const apiPathQuery = getRealUri(pathJson, queryJson);
  const serverUrl = getServerData(currentServer.value);

  // Assemble final request URL
  let apiHref = '';
  if (!serverUrl.endsWith('/') && apiPathQuery && apiPathQuery.split('?')[0] && !apiPathQuery.startsWith('/')) {
    apiHref = serverUrl + '/' + apiPathQuery;
  } else {
    apiHref = serverUrl + apiPathQuery;
  }
  await toClipboard(apiHref);
  notification.success(t('service.apis.notifications.copyUrlSuccess'));
};

const moving = ref(false); // Track if currently dragging

/**
 * Handle window resize events
 * <p>
 * Debounced resize handler that updates the maximum height of the main wrapper
 * when the window is resized.
 * </p>
 */
const resizeHandler = debounce(duration.resize, () => {
  nextTick(() => {
    maxHeight.value = mainWrapper.value.clientHeight;
  });
});

/**
 * Close the drawer
 * <p>
 * Closes the currently open drawer.
 * </p>
 */
const closeDrawer = () => {
  drawerRef.value.close();
};

// Watch authentication changes and update header display
watch(() => state.authentication, async newValue => {
  const data = await getShowAuthData(newValue);
  authInHeader.value = data?.[0] || {};
}, {
  deep: true
});

watch(() => props.id, () => {
  // APIs without ID or marked as unarchived are considered unarchived
  if (props.valueObj.unarchived || !props.id) {
    isUnarchivedApi.value = true;
    if (props.id) {
      loadApiDetail();
    }
  } else {
    isUnarchivedApi.value = false;
    // Load API details for editing
    loadApiDetail();
  }
}, {
  immediate: true
});

watch(() => props.uuid, newValue => {
  // Handle WebSocket response when UUID matches
  if (newValue === uuid) {
    onResponse();
  } else {
    // Handle authorization response for different request
    try {
      const data = JSON.parse(props.response);
      authorizationRef.value.onResponse(data);
    } catch { }
  }
});

watch(() => props.ws, (newValue) => {
  setWebSocket(newValue);
}, {
  immediate: true
});

onMounted(() => {
  resizeHandler();
  erd.listenTo(mainWrapper.value, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(mainWrapper.value, resizeHandler);
});

// Update API information
provide('setApiInfo', async (info) => {
  saveParams.value.id = info.id;
  saveParams.value.ownerId = info.ownerId;
  if (saveParams.value.serviceId) {
    updateApiGroup(saveParams.value.serviceId);
  }
  if (saveParams.value.serviceId !== info.serviceId) {
    saveParams.value.serviceId = info.serviceId;
    updateApiGroup(saveParams.value.serviceId);
  }
  if (isUnarchivedApi.value) {
    refreshUnarchived();
  }
  saveParams.value.summary = info.name;
  isUnarchivedApi.value = false;
  if (props.pid === info.id + 'API') {
    updateTabPane({ _id: props.pid, pid: props.pid, id: info.id, name: info.name, unarchived: false });
  } else {
    replaceTabPane(props.pid, { _id: info.id + 'API', pid: info.id + 'API', id: info.id, name: info.name, unarchived: false, value: 'API' });
  }
  initParams = await getParameter();
});

provide('setUnarchivedApiInfo', async (info) => {
  saveParams.value.id = info.id;
  saveParams.value.summary = info.name;
  isUnarchivedApi.value = true;
  if (props.pid === info.id + 'API') {
    updateTabPane({ _id: props.pid, pid: info.id + 'API', id: info.id, name: info.name, unarchived: true });
  } else {
    replaceTabPane(props.pid, { _id: info.id + 'API', pid: info.id + 'API', id: info.id, name: info.name, unarchived: true, value: 'API' });
  }
  initParams = await getParameter();
});

defineExpose({ autoSave, pid: props.pid });
provide('getParameter', getParameter);
provide('auths', apiAuths);
provide('id', computed(() => saveParams.value.id)); // Provide current API ID to child components
provide('apiBaseInfo', computed(() => saveParams.value)); // API basic information
provide('isUnarchivedApi', isUnarchivedApi); // Provide to child components for determining if API is archived
provide('resolvedRefModels', computed(() => resolvedRefModels.value));
provide('archivedId', computed(() => { return isUnarchivedApi.value ? undefined : saveParams.value.id; }));
provide('selectHandle', closeDrawer);
</script>
<template>
  <div
    class="z-9 h-full"
    :class="{ 'select-none': moving }">
    <div class="flex flex-nowrap h-full w-full">
      <div class="flex flex-col h-full flex-1 min-w-0 relative">
        <div v-if="!isUnarchivedApi" class="mx-5 mt-3 flex justify-between">
          <RadioGroup
            v-model:value="activeTabKey"
            buttonStyle="solid"
            size="small">
            <RadioButton value="debug">{{ t('actions.debug') }}</RadioButton>
            <RadioButton value="case">{{ t('common.useCase') }}</RadioButton>
            <RadioButton value="test">{{ t('service.apis.tabs.test') }}</RadioButton>
            <RadioButton value="mock">{{ t('service.apis.tabs.mock') }}</RadioButton>
            <RadioButton value="doc">{{ t('common.doc') }}</RadioButton>
          </RadioGroup>

          <div class="inline-flex items-center space-x-3">
            <Button
              type="link"
              size="small"
              class="hover:text-status-orange  text-status-orange !bg-board-orange"
              @click="handleShare">
              <Icon icon="icon-fenxiang" class="mr-2" />
              {{ t('actions.share') }}
            </Button>
            <SelectEnum
              v-model:value="apisStatus"
              :disabled="!apiAuths.includes(ApiPermission.MODIFY)"
              :lazy="false"
              class="inline-block"
              enumKey="ApiStatus"
              @change="handleStatusChange">
              <template #option="{label, value}">
                <div :class="[API_STATUS_COLOR_CONFIG[value]]"><Badge :color="API_STATUS_BADGE_COLOR_CONFIG[value]" /> {{ label }}</div>
              </template>
            </SelectEnum>
          </div>
        </div>

        <div
          v-show="activeTabKey === 'debug'"
          ref="mainWrapper"
          class="flex-1 flex flex-col justify-between min-h-0">
          <div class="flex-1 w-full overflow-y-auto min-h-0">
            <ServerPath
              :id="props.id"
              v-model:loading="loading"
              v-model:endpoint="apiUri"
              v-model:currentServer="currentServer"
              v-model:method="apiMethod"
              class="mx-5 mt-3"
              :isUnarchivedApi="isUnarchivedApi"
              :availableServers="availableServers"
              :defaultCurrentServer="defaultCurrentServer"
              @sendRequest="sendRequest"
              @save="save"
              @archived="archivedApi"
              @abort="handleAbort"
              @addQuery="addQueryParam"
              @copyUrl="copyUrl" />
            <Tabs
              v-model:active-key="activeKey"
              style="height: calc(100% - 68px);"
              class="inner-container mt-2.5"
              size="small"
              centered>
              <TabPane key="parameters" :tab="`${t('service.apis.requestTabs.parameters')}(${parametersNum})`">
                <div class="flex">
                  <RequestParams
                    ref="requestParamsRef"
                    v-model:apiUri="apiUri"
                    class="flex-1 px-5"
                    :value="state.parameters"
                    @change="changeParamList" />
                </div>
              </TabPane>
              <TabPane
                key="header"
                :tab="`${t('service.apis.requestTabs.header')}(${headerCount || 0})`"
                :forceRender="true">
                <div class="flex">
                  <RequestHeader
                    ref="requestHeaderRef"
                    v-model:contentType="contentType"
                    class="flex-1 px-5"
                    :value="state.headerList"
                    :authData="authInHeader"
                    @change="changeHeaderList" />
                </div>
              </TabPane>
              <TabPane
                key="cookie"
                :tab="`${t('protocol.cookie')}(${cookieCount || 0})`"
                :forceRender="true">
                <div class="flex">
                  <RequestCookie
                    ref="requestCookieRef"
                    class="flex-1 px-5"
                    :value="state.cookieList"
                    @change="changeCookieList" />
                </div>
              </TabPane>
              <TabPane key="authentication" :forceRender="true">
                <template #tab>
                  <div :class="{ 'has-content': state.secured }"></div>Authorization
                </template>
                <Authorization
                  ref="authorizationRef"
                  v-model:auth="state.secured"
                  class="px-5"
                  :ws="props.ws"
                  :defaultValue="defaultAuthentication"
                  @change="changeAuthData" />
              </TabPane>
              <TabPane key="request-body" :forceRender="true">
                <template #tab>
                  <div :class="{ 'has-content': hasBodyContent }"></div>
                  <span>{{ t('protocol.requestBody') }}</span>
                </template>
                <RequestBody
                  ref="requestBodyRef"
                  v-model:contentType="contentType"
                  class="h-full px-5"
                  :defaultValue="state.requestBody"
                  @change="changeRequestBody" />
              </TabPane>
              <TabPane
                v-if="!!props.id"
                key="parametric"
                :tab="t('common.parameterization')">
                <APICaseParametric
                  :datasetActionOnEOF="datasetActionOnEOF"
                  :datasetSharingMode="datasetSharingMode"
                  :targetId="props.id"
                  targetType="API"
                  class="px-5 pb-5 overflow-auto h-full"
                  @targetInfoChange="targetInfoChange" />
              </TabPane>
              <TabPane
                key="assert"
                :tab="`${t('common.assertionResult')}(${assertNum})`"
                :forceRender="true">
                <AssertForm
                  :id="props.id"
                  ref="assertFormRef"
                  v-model:num="assertNum"
                  class="px-5"
                  :value="state.assertions" />
              </TabPane>
              <TabPane key="setting" :tab="t('common.setting')">
                <ApiSetting
                  class="px-5"
                  :value="setting"
                  @change="changeSetting" />
              </TabPane>
              <template #rightExtra>
                <FunctionsButton class="text-3.5 mr-2" />
                <Popover placement="leftTop" :trigger="['click']">
                  <Icon icon="icon-jieshaoshuoming" class="cursor-pointer mr-2 text-4 text-theme-text-hover" />
                  <template #content>
                    <div class="w-100 text-3">
                      <Hints :text="t('service.apis.hints.notes')" class="font-semibold !text-text-title" />
                      <ul class="mt-2 pl-4 list-disc">
                        <li
                          v-for="item in debugTip"
                          :key="item"
                          class="mt-2 text-text-sub-content text-3">
                          {{ item }}
                        </li>
                      </ul>
                    </div>
                  </template>
                </Popover>
                <Popover placement="leftTop" :trigger="['click']">
                  <Icon icon="icon-tiaoshijiaoben" class="cursor-pointer mr-2 text-3.5 text-theme-text-hover" />
                  <template #content>
                    <div class="w-150 text-3">
                      <Hints :text="t('service.apis.hints.parameterSerialization')" class="font-semibold !text-text-title" />
                      <div class="mt-2">
                        {{ t('service.apis.hints.serializationDescription_pre') }}
                        <a
                          class="text-theme-special"
                          href="https://datatracker.ietf.org/doc/html/rfc6570"
                          target="_blank">RFC6570</a>
                        {{ t('service.apis.hints.serializationDescription_later') }}
                      </div>
                      <ul class="pl-4 list-disc">
                        <li
                          v-for="item in docInfo"
                          :key="item.title"
                          class="mt-2">
                          <div class="font-semibold">{{ item.title }}</div>
                          <div
                            v-for="text in item.rules"
                            :key="text"
                            class="text-text-sub-content text-3">
                            {{ text }}
                          </div>
                        </li>
                      </ul>
                    </div>
                  </template>
                </Popover>
              </template>
            </Tabs>
          </div>
          <Toolbar
            ref="toolbarRef"
            v-model:height="height"
            v-model:tab="currentTab"
            v-model:moving="moving"
            :menus="toolbarMenus"
            :maxHeight="maxHeight"
            @change="toolbarChange">
            <template #content="{ activeMenu }">
              <div class="toolbar-main  relative" :class="{ 'select-text': !moving }">
                <template v-if="['request', 'response', 'time', 'cookie', 'assert'].includes(activeMenu || '')">
                  <template v-if="responseError.show">
                    <ResponseError
                      class="h-full overflow-hidden"
                      :errorTitle="errorTitle"
                      :info="responseError.value" />
                  </template>
                  <template v-else-if="!isResponseEmpty">
                    <ApiRequest
                      v-show="activeMenu === 'request'"
                      class="px-5"
                      :dataSource="responseState" />
                    <ApiResponse
                      v-show="activeMenu === 'response'"
                      class="px-5"
                      :dataSource="responseState" />
                    <ApiTimeline
                      v-show="activeMenu === 'time'"
                      class="px-5"
                      :dataSource="responseState?.performance" />
                    <ApiCookie
                      v-show="activeMenu === ParameterIn.cookie"
                      class="px-5"
                      :dataSource="responseState?.cookie"
                      :host="getServerData(currentServer)" />
                    <ApiAssert
                      v-show="activeMenu === 'assert'"
                      class="px-5"
                      :value="assertResult" />
                  </template>
                  <template v-else>
                    <NoData
                      v-show="activeMenu !== 'define'"
                      class="overflow-hidden"
                      style="height: calc(100% - 29px);" />
                  </template>
                </template>
                <template v-if="activeMenu === 'generateCode'">
                  <CodeSnippet class="px-5 h-full overflow-auto" />
                </template>
                <Spin
                  :spinning="loading"
                  :class="{ '-z-1': !loading }"
                  mask
                  class="!absolute top-0 bottom-0 left-0 right-0">
                </Spin>
              </div>
            </template>
            <template #actions>
              <template v-if="responseState?.status">
                <div
                  class="flex items-center flex-nowrap whitespace-nowrap mr-7.5 text-3 leading-3 text-text-sub-content">
                  <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5">
                    <span class="mr-1">{{ t('protocol.statusCode') }}:</span>
                    <span :class="getStatusColor(responseState?.status)">{{ getStatusText(responseState?.status)
                    }}</span>
                  </div>
                  <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5">
                    <span class="mr-1">{{ t('common.duration') }}:</span>
                    <span class>{{ responseState?.performance?.duration &&
                      (responseState?.performance?.duration.toFixed(0)
                        + 'ms') }}</span>
                  </div>
                  <div class="flex items-center flex-nowrap whitespace-nowrap">
                    <span class="mr-1">{{ t('common.size') }}:</span>
                    <span class>{{ isNaN(Number(responseState?.size)) ? responseState?.size :
                      formatBytes(Number(responseState?.size)) }}</span>
                  </div>
                </div>
              </template>
            </template>
          </Toolbar>
          <AsyncComponent :visible="shareVisible">
            <ApiShare
              v-model:visible="shareVisible"
              :projectId="props.projectId"
              :apisIds="[props.id]"
              :servicesId="saveParams.serviceId"
              :shareScope="ApisShareScope.SINGLE_APIS" />
          </AsyncComponent>
        </div>

        <template v-if="activeTabKey === 'case'">
          <TestCase
            :id="props.id"
            :serviceId="saveParams.serviceId"
            layout="inline"
            class="mx-5"
            type="API" />
        </template>

        <template v-if="activeTabKey === 'test'">
          <ExecDetail
            :apisId="props.id"
            :projectId="props.projectId"
            :userInfo="props.userInfo"
            :appInfo="props.appInfo"
            class="mx-5 mt-3 flex-1 overflow-auto mb-3" />
        </template>

        <template v-if="activeTabKey === 'mock'">
          <ApiMockVue
            :id="props.id"
            class="px-5 mt-3 "
            :disabled="!apiAuths.includes(ApiPermission.MODIFY)" />
        </template>

        <template v-if="activeTabKey === 'doc'">
          <ApiDoc
            :id="props.id"
            class="mx-5 mt-3 flex-1 overflow-auto mb-3" />
        </template>

        <Spin
          :spinning="loadingApiDetail"
          :class="{ '-z-1': !loadingApiDetail }"
          :delay="0"
          class="!absolute top-0 bottom-0 left-0 right-0">
        </Spin>
      </div>
      <Drawer
        v-if="activeTabKey === 'debug'"
        ref="drawerRef"
        v-model:activeKey="activeDrawerKey"
        :menuItems="myNavs">
        <template #saveUnarchived>
          <UnarchivedEdit
            v-if="activeDrawerKey === 'saveUnarchived'"
            :id="props.id"
            class="pr-4"
            type="API" />
        </template>
        <template #save>
          <InfoEdit
            v-if="activeDrawerKey === 'save'"
            :id="props.id"
            class="pr-5"
            :disabled="!apiAuths.includes(ApiPermission.MODIFY)"
            type="API" />
        </template>
        <template #performance>
          <Indicator
            v-if="activeDrawerKey === 'performance'"
            :id="props.id"
            :disabled="!apiAuths.includes(ApiPermission.MODIFY)"
            class="pr-5 mt-2"
            type="API" />
        </template>
        <template #activity>
          <ActivityTimeline
            v-if="activeDrawerKey === 'activity'"
            :id="props.id"
            infoKey="description"
            class="pr-5 pt-2"
            type="API" />
        </template>
        <template #test>
          <HttpTestInfo
            :id="props.id"
            class="pr-5 mt-2"
            type="API" />
        </template>
        <template #apiMock>
        </template>
        <template #share>
          <ShareList
            v-if="activeDrawerKey === 'share'"
            :id="props.id"
            class="mt-2 pr-5"
            :disabled="!apiAuths.includes(ApiPermission.SHARE)"
            type="API" />
        </template>
        <template #agent>
          <RequestProxy v-if="activeDrawerKey === 'agent'" class="pr-5 mt-2" />
        </template>
        <template #cases>
        </template>
      </Drawer>
    </div>
  </div>
</template>
<style scoped>
.toolbar-main {
  height: calc(100% - 34px);
}

.inner-container :deep(.ant-tabs-nav-list) {
  width: 100%;
}

.inner-container :deep(.ant-tabs-tabpane) {
  overflow: auto;
}

.inner-container :deep(.ant-tabs-nav-operations) {
  display: none;
}

.inner-container.ant-tabs.ant-tabs-centered :deep(.ant-tabs-nav) {
  @apply mb-3 bg-white;

  margin-right: 20px;
  margin-left: 20px;
}

.ant-select-focused:not(.ant-select-disabled).ant-select:not(.ant-select-customize-input) :deep(.ant-select-selector) {
  @apply shadow-none;
}

.ant-select+.ant-select::after,
:deep(.input-container)::after {
  content: "";

  @apply absolute left-0 top-0 block w-0.25 h-8 border-border-divider;
}

.ant-btn.ant-btn-primary+.ant-btn {
  @apply ml-3 border-none bg-gray-light-b text-text-content;
}

.ant-select:not(.ant-select-customize-input) .ant-select-selector {
  @apply border-none rounded-none;
}

.inner-container.ant-tabs :deep(.ant-tabs-nav-list) .ant-tabs-tab {
  @apply justify-center text-3 leading-3 py-3 text-center min-w-0;
}

.inner-container :deep(.ant-tabs-nav-list) .ant-tabs-tab .ant-tabs-tab-btn {
  @apply text-text-sub-content;
}

.inner-container.ant-tabs-centered.ant-tabs :deep(.ant-tabs-nav-list) .ant-tabs-tab::before {
  @apply hidden;
}

.icon-inner {
  @apply text-3.5 leading-3.5 text-text-sub-content cursor-pointer;
}

.inner-container.ant-tabs-centered.ant-tabs :deep(.ant-tabs-nav-list) .ant-tabs-tab .has-content {
  @apply w-1.5 h-1.5 inline-block bg-status-success rounded-full mr-1;
}
</style>
