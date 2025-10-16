<script setup lang="ts">
import {
  computed, defineAsyncComponent, inject, nextTick, onBeforeUnmount, onMounted, provide, reactive, ref, watch
} from 'vue';
import { Popover, TabPane, Tabs, RadioGroup, RadioButton, Button, Badge } from 'ant-design-vue';
import { ActivityTimeline, Drawer, Hints, Icon, NoData, notification, Spin, AsyncComponent } from '@xcan-angus/vue-ui';
import elementResizeDetector from 'element-resize-detector';
import { AssertionCondition, AssertionType, utils, axiosClient, duration } from '@xcan-angus/infra';
import { dataURLtoBlob } from '@/utils/blob';
import qs from 'qs';
import { deconstruct } from '@/utils/swagger';
import SwaggerUI from '@xcan-angus/swagger-ui';
import XML from 'xml';
import useClipboard from 'vue-clipboard3';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import assertUtils from '@/utils/assertutils';
import apiUtils from '@/utils/apis';

import { apis, services } from '@/api/tester';
import { getStatusText } from '@/views/apis/services/components/Request';
import { getDefaultParams } from '@/views/apis/services/protocol/http/RequestParameter';
import { API_STATUS_COLOR_CONFIG, API_STATUS_BADGE_COLOR_CONFIG, API_EXTENSION_KEY, getModelDataByRef } from '@/utils/apis';

import { formatBytes } from '@/utils/common';
import {
  ApiAssert, ApiCookie, ApiRequest, ApiResponse, ApiTimeline, AssertForm, Authorization,
  debugTip, docInfo, getStatusColor, menus, Method, navs, ParamsItem, RequestBody, RequestCookie, RequestHeader,
  RequestParams, ResponseState, Setting, State
} from './interface';
import {
  convertBlob, travelEmptyObjToString, travelXcValueToString, validateBodyForm, validateQueryParameter
} from './utils';
import { HttpServer } from '@/views/apis/services/protocol/http/path/PropsType';
import { getServerData } from '@/views/apis/services/protocol/http/path/utils';
import { rawTypeOptions, RequestBodyParam } from '@/views/apis/services/protocol/http/requestBody/interface';
import { AssertResult, ConditionResult, Parameter } from './PropsType';
import { encode } from '@/utils/secure';
import { getShowAuthData } from '@/components/ApiAuthencation/interface';

const Indicator = defineAsyncComponent(() => import('@/components/Indicator/index.vue'));
const HttpTestInfo = defineAsyncComponent(() => import('@/components/HttpTestInfo/index.vue'));
const FunctionsButton = defineAsyncComponent(() => import('@xcan-angus/vue-ui').then(resp => resp.FunctionsButton));
const APICaseParametric = defineAsyncComponent(() => import('@/components/apis/parameterization/index.vue'));
const ExecDetail = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/exec/index.vue'));

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
  pid: undefined, // pid 用来标志当前 pane 唯一值
  serviceId: undefined,
  projectId: ''
});

const UnarchivedEditVue = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/slider/UnarchivedEdit.vue'));
const InfoEditVue = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/slider/InfoEdit.vue'));

// const VariableVue = defineAsyncComponent(() => import('@/views/apis/services/components/Variable/index.vue'));
const ShareListVue = defineAsyncComponent(() => import('@/components/share/list.vue'));
const AgentVue = defineAsyncComponent(() => import('@/views/apis/services/components/RequestProxy.vue'));
const CodeSnippetVue = defineAsyncComponent(() => import('@/views/apis/services/components/CodeSnippet.vue'));

const ApiSetting = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/Setting.vue'));
const ServerPath = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/path/index.vue'));
const Toolbar = defineAsyncComponent(() => import('@/components/layout/toolbar/index.vue'));
const ResponseError = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/ResponseError.vue'));
const ApiDoc = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/Doc.vue'));
const TestCase = defineAsyncComponent(() => import('@/views/apis/services/components/case/tableView.vue'));
const ApiMockVue = defineAsyncComponent(() => import('@/views/apis/services/mock/MockApi.vue')); //
const ApiShare = defineAsyncComponent(() => import('@/views/apis/share/Edit.vue'));

const shareVisible = ref(false); // 分享弹窗
const handleShare = () => {
  shareVisible.value = true;
};

// 更新左侧未归档列表
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => { });

const erd = elementResizeDetector({ strategy: 'scroll' });
const { toClipboard } = useClipboard();

const myRequest = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });
const { serverSourceKey, requestSettingKey, valueKey, enabledKey, fileNameKey, idKey } = API_EXTENSION_KEY;
const auths = ref<string[]>(['SHARE', 'DEBUG', 'GRANT', 'EXECUTE', 'QUERY', 'DELETE', 'MODIFY']);

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
        disabled: !auths.value?.includes(i.auth as string),
        name: t('actions.save')
      };
    }
    return {
      ...i,
      key: i.value,
      disabled: !auths.value?.includes(i.auth as string)
    };
  });
});

let ws: WebSocket | undefined;

let uuid = '';

const respError = reactive<{
  show: boolean,
  value: string | undefined
}>({
  show: false,
  value: undefined
});
const loadWS = (value) => {
  ws = value;
};

const currentTab = ref<string>('');
const height = ref<number>(34);

const apiMethod = ref<Method>('GET');
const currentServer = ref<HttpServer>({ url: '' });
const defaultCurrentServer = ref();
const apiUri = ref<string>(); // uri整体
const requestParamsRef = ref(); // 参数 ref
const drawerRef = ref(); // 右侧抽屉 apis ref;
const activeDrawerKey = ref();
const toolbarRef = ref(); // 底部响应comp ref;
const maxHeight = ref(0); // 最大高度
const loading = ref(false); // 发送请求
const mainWrapper = ref();
const availableServers = ref<{ description: string; url: string; variabled: boolean;[serverSourceKey]: string }[]>([]); // 所有可用 server
const isUnarchivedApi = ref<boolean>(true);
const requestHeaderRef = ref();
const requestCookieRef = ref();
const authorizationRef = ref();
let initParams = {};

const assertResult = ref<AssertResult[]>();// 断言结果

// 保存编辑的数据
const saveParams = ref({
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

// 更新 api 信息
provide('setApiInfo', (info) => {
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
  initParams = getParameter();
});

provide('setUnarchivedApiInfo', (info) => {
  saveParams.value.id = info.id;
  saveParams.value.summary = info.name;
  isUnarchivedApi.value = true;
  if (props.pid === info.id + 'API') {
    updateTabPane({ _id: props.pid, pid: info.id + 'API', id: info.id, name: info.name, unarchived: true });
  } else {
    replaceTabPane(props.pid, { _id: info.id + 'API', pid: info.id + 'API', id: info.id, name: info.name, unarchived: true, value: 'API' });
  }
  initParams = getParameter();
});

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

const setting = ref<Setting>({
  enableParamValidation: false,
  connectTimeout: 6000,
  readTimeout: 60000,
  retryNum: 0,
  maxRedirects: 1
});
const state = reactive<State>({
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

const changeSetting = (data: Setting): void => {
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
  const [error, resp] = await getModelDataByRef(saveParams.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(resp.data || {});
};

// 获取当前 api 的操作权限
const loadApiAuth = async () => {
  const [error, res] = await apis.getCurrentAuth(props.id as string);
  if (error) {
    return;
  }
  auths.value = (res.data?.permissions || []).map(i => i.value);
};

const loadProjectServers = async (serviceId) => {
  const [error, resp] = await services.getServicesServerUrlInfo(serviceId);
  if (error) {
    return;
  }
  availableServers.value = (resp.data || []).map(i => ({ ...i, ...(i.extensions || {}) }));
};

const loadingInfo = ref(false);
// 获取当前 api 信息
const loadApiInfo = async (): Promise<void> => {
  let result;
  loadingInfo.value = true;
  if (isUnarchivedApi.value) {
    result = await apis.getUnarchivedApiDetail(props.id as string);
  } else {
    result = await apis.getApiDetail(props.id as string);
  }
  loadingInfo.value = false;
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

  if (serviceId) {
    loadProjectServers(serviceId);
  }
  apisStatus.value = status?.value;

  datasetActionOnEOF.value = _datasetActionOnEOF?.value || _datasetActionOnEOF || 'RECYCLE';
  datasetSharingMode.value = _datasetSharingMode?.value || _datasetSharingMode || 'ALL_THREAD';

  if (!isUnarchivedApi.value && auth && serviceAuth) {
    loadApiAuth();
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
      currentServer.value = availableServersFromPro.find(i => i[serverSourceKey] === 'CURRENT_REQUEST') || availableServersFromPro[0];
    } else {
      currentServer.value = { url: '' };
    }
    availableServers.value = (availableServersFromPro || []).filter(i => i[serverSourceKey] !== 'CURRENT_REQUEST');
  }
  defaultCurrentServer.value = JSON.parse(JSON.stringify(currentServer.value));

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
  state.publishFlag = (status?.value === 'RELEASED');
  state.secured = !!state.authentication.type || !!state.authentication.$ref;
  for (const key in parameters) {
    if (parameters[key].$ref) {
      const model = await getModelFromRef(parameters[key].$ref);
      if (model.name) {
        if (!model.schema.type) {
          if (model.schema.properties) {
            model.schema.type = 'object';
            if (typeof model[valueKey] === 'string') {
              try {
                model[valueKey] = JSON.parse(model[valueKey]);
              } catch {
                console.log(model[valueKey] + 'is not a json string');
              }
            }
          } else if (model.schema.items) {
            model.schema.type = 'array';
            if (typeof model[valueKey] === 'string') {
              try {
                model[valueKey] = JSON.parse(model[valueKey]);
              } catch {
                console.log(model[valueKey] + 'is not a json string');
              }
            }
          } else {
            model.schema.type = 'string';
          }
        }
        parameters[key] = { ...parameters[key], ...model };
      } else {
        parameters[key] = { name: undefined, schema: { type: 'string' }, in: 'query', [valueKey]: '' };
      }
    }
    if (['object', 'array', 'boolean', 'integer', 'number'].includes(parameters[key].schema?.type) && parameters[key]?.[valueKey] && typeof parameters[key]?.[valueKey] === 'string') {
      try {
        parameters[key][valueKey] = JSON.parse(parameters[key][valueKey]);
      } catch {
        console.log(parameters[key][valueKey] + 'is not an object');
      }
    }
    if (!parameters[key][valueKey] && parameters[key].schema?.type && (parameters[key].schema?.[valueKey] || parameters[key].schema?.properties || parameters[key].schema?.items)) {
      parameters[key][valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(parameters[key].schema, { useValue: true });
    }
  }
  state.parameters = [...queryStrParam, ...(parameters?.filter(item => ['query', 'path'].includes(item.in) && !!item.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }))];
  state.headerList = (parameters?.filter(i => i.in === 'header' && !!i.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

  apiUri.value = endpoint ? endpoint.split('?')[0] : undefined;
  const contentTypeIndex = state.headerList.findIndex(i => i.name === 'Content-Type');
  if (contentTypeIndex > -1) {
    contentType.value = state.headerList[contentTypeIndex]?.[valueKey];
    if (contentType.value) {
      state.headerList.splice(contentTypeIndex, 1);
    }
  }
  state.cookieList = (parameters?.filter(i => i.in === 'cookie' && !!i.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  state.requestBody = requestBody || {};
  if (res.data.resolvedRefModels) {
    resolvedRefModels.value = res.data.resolvedRefModels;
  }
};

// 参数化逻辑 - start
const datasetActionOnEOF = ref<'RECYCLE' | 'STOP_THREAD'>('RECYCLE');
const datasetSharingMode = ref<'ALL_THREAD' | 'CURRENT_THREAD'>('ALL_THREAD');

const targetInfoChange = (data: { id: string; datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD'; datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD'; }) => {
  datasetActionOnEOF.value = data.datasetActionOnEOF;
  datasetSharingMode.value = data.datasetSharingMode;
};
// 参数化逻辑 - end

// 请求体是否有数据
const hasBodyContent = computed(() => {
  return !!contentType.value || !!state.requestBody.content;
});

// 显示认证信息在请求头
watch(() => state.authentication, async newValue => {
  const data = await getShowAuthData(newValue);
  authInHeader.value = data?.[0] || {};
}, {
  deep: true
});

// 获取有效 请求头
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
 *
 * 分两步获取，
 * 1. 先校验数据 validate
 */
const validateParam = () => {
  return !assertFormRef.value || assertFormRef.value?.validate();
};

/**
 *
 * 分两步获取，
 * 2. 校验通过执行该方法
 */
const getParameter = async (isdebug = false) => {
  const { parameters, requestBody, headerList, cookieList, authentication, secured } = state;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { content } = requestBody;
  if (!isdebug) {
    const exceedFileSize = requestBodyRef.value.ifExceedSize();
    if (exceedFileSize) {
      Object.keys(content).forEach(key => {
        if (content[key]?.schema?.format === 'binary') {
          delete requestBody.content[key].schema[valueKey];
          delete requestBody.content[key].schema[fileNameKey];
        }
        if (key === 'multipart/form-data') {
          if (content[key]?.schema?.properties) {
            Object.keys(content[key]?.schema?.properties).forEach(name => {
              if (content[key].schema.properties[name].format === 'binary') {
                const type = content[key].schema.properties[name].type;
                requestBody.content[key].schema.properties[name][valueKey] = (type === 'array' ? [] : undefined);
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
      in: 'header'
    };
  });
  if (contentType.value) {
    headers.unshift({ name: 'Content-Type', in: 'header', [valueKey]: contentType.value, [enabledKey]: true });
  }
  let protocol = '';
  if (currentServer.value?.url) {
    try {
      const _server = new URL(currentServer.value.url);
      protocol = _server.protocol.replace(':', '');
    } catch {
      protocol = 'http';
      if (!currentServer.value.url.startsWith('http') && !currentServer.value['x-xc-id']) {
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
  const res = {
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
  return res;
};

const getRealUri = (pathParams: Record<string, any>, queryParams: Record<string, any>) => {
  let tempUri = apiUri.value || '';
  const paths = Object.keys(pathParams || {}).map(key => ({ name: key, [valueKey]: pathParams?.[key] }));
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
    const endpoint = paths?.reduce((prevValue, currentValue) => {
      pattern = new RegExp('{' + currentValue.name + '}', 'gi');
      if (pattern.test(prevValue)) {
        prevValue = prevValue.replace(pattern, currentValue[valueKey] || '');
      }
      return prevValue;
    }, tempUri) || '';
    // path 中的占位符使用参数值替换
    tempUri = '/' + endpoint.replace(/^\/+/, '');
  }
  const queryUri = qs.stringify(queryParams, { allowDots: true, encode: true });
  if (tempUri.includes('?')) {
    tempUri = tempUri + '&' + queryUri;
  } else if (queryUri) {
    tempUri = tempUri + '?' + queryUri;
  }
  return tempUri;
};

const activeKey = ref('parameters');
let controller: AbortController;
// 发送请求 Send the request
const sendRequest = async () => {
  if (loading.value) {
    handleAbort();
  }
  loading.value = true;
  assertVariableExtra.value = {};
  // 校验 url
  if (!currentServer.value.url && !apiUri.value) {
    loading.value = false;
    notification.error({ message: t('service.apis.errors.invalidUrl'), description: t('service.apis.errors.invalidUrlDescription') });
    return;
  }
  // 准备 body 数据
  let requestBodyContents = await requestBodyRef.value.getBodyData(state.requestBody, contentType.value);
  let formData: any[] = [];
  const strParam: string[] = [];
  if (contentType.value === 'application/x-www-form-urlencoded' || contentType.value === 'multipart/form-data') {
    formData = requestBodyContents;
  } else if (typeof requestBodyContents === 'string') {
    strParam.push(requestBodyContents);
  }
  //  准备认证数据
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
  }).filter(Boolean); // header 认证
  const authQueryData = Object.keys(auth?.[1] || {}).map(key => ({ name: key, [valueKey]: auth?.[1]?.[key] })); // query 认证

  const headerCookieData = [...state.headerList, ...state.cookieList].filter(i => i.name && i[valueKey] && i[enabledKey]);
  // 准备 parameter 数据
  const querPathData = state.parameters.filter(i => i.name && i[enabledKey]);

  // 断言数据
  let assertions: any[] = [];
  let asserVariableStr: string[] = [];
  if (typeof assertFormRef.value?.getData === 'function') {
    const assertFormData = assertFormRef.value.getData();
    assertions = assertFormData.data;
    asserVariableStr = assertFormData.variables;
  } else {
    assertions = state.assertions;
  }

  // 替换 mock 函数和变量
  const result = await apiUtils.replaceFuncValue({ parameter: [querPathData, formData, headerCookieData, authParamData, authQueryData], str: strParam, variableStr: asserVariableStr }, allFuncNames.value, !isUnarchivedApi.value ? saveParams.value.id : undefined, 'API', { ignoreErr: false });
  if (!result) {
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
  // 校验 断言数据
  const validateAssertData = validateParam();

  // 校验 parameter 数据 和 format 数据
  if (setting.value.enableParamValidation) {
    const validatedQuery = validateQueryParameter(funcValues[0]);
    if (!validatedQuery) {
      requestParamsRef.value.validate();
      notification.error(t('service.apis.errors.parameterValidationFailed'));
    }
    const validatedHeader = validateQueryParameter(funcValues[2].filter(i => i.in === 'header'));
    if (!validatedHeader) {
      requestHeaderRef.value.validate();
      notification.error(t('service.apis.errors.headerValidationFailed'));
    }
    const validatedCookie = validateQueryParameter(funcValues[2].filter(i => i.in === 'cookie'));
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

  // 组装 body 数据
  let requestBody;
  const showRequestBody: Record<string, any> = {};
  if ((contentType.value === 'application/x-www-form-urlencoded' || contentType.value === 'multipart/form-data') && funcValues[1]?.length) {
    const bodyDataArr = funcValues[1];
    if (contentType.value === 'application/x-www-form-urlencoded') {
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
    if (contentType.value === 'multipart/form-data') {
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
          if (item.type === 'array') {
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

  // parameterPath 的 JSON 格式
  const pathJson = {};
  const pathArr = funcValues[0].filter(i => i.in === 'path');
  pathArr.forEach(i => {
    pathJson[i.name] = i[valueKey];
  });

  // 组装 query 数据 query + 认证 query
  const queryJson = {};
  const quryyArr = funcValues[0].filter(i => i.in === 'query').concat(funcValues[4]);
  quryyArr.forEach(i => {
    queryJson[i.name] = travelEmptyObjToString(i[valueKey]);
  });
  const apiPathQuery = getRealUri(pathJson, queryJson);
  const serverUrl = getServerData(currentServer.value);

  // parameterQuery 的 JSON 格式
  const requestQueryJson = qs.parse(apiPathQuery.split('?')[1] || '');

  // 组装请求 url
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

  // 组装 请求header
  let headers: Record<string, string> = {};
  const headList = funcValues[2].filter(i => i.in === 'header');
  const cookieList = funcValues[2].filter(i => i.in === 'cookie');
  headList.forEach(item => {
    if (typeof item[valueKey] === 'object') {
      // encodeURIComponent 用于编译中文内容， 否则本地请求无法发送
      if (Object.prototype.toString.call(item[valueKey]) === '[object Array]') {
        headers[item.name] = item[valueKey] ? item[valueKey].map(value => value && apiUtils.containsAllAscii(value) ? value : value ? encodeURIComponent(value) : '').join(',') : '';
      } else {
        headers[item.name] = item[valueKey] ? Object.entries(item[valueKey]).map(([key, value]) => `${key}=${value && apiUtils.containsAllAscii(value) ? value : value ? encodeURIComponent(value) : ''}`).join(',') : '';
      }
    } else {
      headers[item.name] = item[valueKey] && apiUtils.containsAllAscii(item[valueKey]) ? item[valueKey] : item[valueKey] ? encodeURIComponent(item[valueKey]) : '';
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
          value.push(encodeURIComponent(val));
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
    headers['Content-Type'] = contentType.value;
  }

  // 请求设置
  const { connectTimeout, readTimeout, retryNum, maxRedirects } = setting.value;
  const isExceedRequestSize = requestBodyRef.value.ifExceedRequestSize();
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
    const responese = await myRequest.request(axiosConfig);
    setErrTitle();
    await onHttpResponse(responese, { query: requestQueryJson, header: headers, path: pathJson, requestBody: showRequestBody, queryString: apiPathQuery.split('?')[1] || '', assertions, variableValues });
    openToolBar();
  } else if (ws.readyState !== 1) {
    loading.value = false;
    notification.error(t('service.apis.errors.proxyNotConnected'));
  } else if (ws) {
    const { currentServer, requestBody, ...params } = await getParameter(true);
    if (!params?.authentication.type && !params?.authentication.$ref) {
      delete params.authentication;
    } else if (params?.authentication.$ref) {
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

    // 过滤掉没有启用的断言
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
      assertVariableExtra.value = extra;
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

// 终止请求
const handleAbort = () => {
  loading.value = false;
  // 验证websocket中断功能
  if (ws) {
    uuid = '';
    // ws.close();
  } else {
    controller.abort();
  }
};

// 处理本地无代理请求
const onHttpResponse = async (resp, request) => {
  loading.value = false;
  respError.show = false;
  respError.value = undefined;
  responseState.performance = resp.performance;
  assertResult.value = undefined;
  if (resp.request.status === 0) {
    respError.show = true;
    respError.value = resp.message;
    responseState.config = {
      url: decodeURIComponent(resp.config.url),
      method: apiMethod.value,
      status: 0,
      queryString: request.queryString
    };
    responseState.headers = resp.config.headers;
  } else if (resp.request.status < 200 || resp.request.status >= 300) {
    responseState.data = await convertBlob(resp.response.data);
    responseState.config = {
      url: decodeURIComponent(resp.config.url),
      method: apiMethod.value,
      queryString: request.queryString,
      forms: request.header['Content-Type'] === 'multipart/form-data'
        ? request.requestBody.formData
        : request.header['Content-Type'] === 'application/x-www-form-urlencoded'
          ? request.requestBody.urlencoded
          : {},
      body: request.requestBody.body || ''
    };
    responseState.headers = resp.response.headers;
    responseState.status = resp.response.status;
    responseState.size = resp.response.data.size;
    const requestHead = resp?.config?.headers || {};
    responseState.requestHeaders = requestHead;
    const cookie = resp.config.headers?.['Set-Cookie'];
    responseState.cookie = cookie ? [cookie] : [];
  } else {
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
      forms: request.header['Content-Type'] === 'multipart/form-data'
        ? request.requestBody.formData
        : request.header['Content-Type'] === 'application/x-www-form-urlencoded'
          ? request.requestBody.urlencoded
          : {},
      body: request.requestBody.body || ''
    };
    responseState.headers = resp.headers;
    responseState.status = resp.status;
    responseState.size = resp.data.size;
    const requestHead = resp?.config?.headers || {};
    responseState.requestHeaders = requestHead;
    const cookie = resp.config.headers?.['Set-Cookie'];
    responseState.cookie = cookie ? [cookie] : [];
  }

  if (!respError.show) {
    localRequstInfo.responseHeader = responseState.headers;
    localRequstInfo.responseBody = { size: responseState.size || 0, data: responseState.data };
    localRequstInfo.status = responseState.status;
    localRequstInfo.duration = responseState?.performance?.duration || 0;
    localRequstInfo.query = request.query;
    localRequstInfo.path = request.path;
    localRequstInfo.header = request.header;
    localRequstInfo.form = request.header['Content-Type'] === 'multipart/form-data'
      ? request.requestBody.formData
      : request.header['Content-Type'] === 'application/x-www-form-urlencoded'
        ? request.requestBody.urlencoded
        : undefined;
    localRequstInfo.rawBody = request.requestBody.body || undefined;
    assertResult.value = await assertUtils.assert.execute(localRequstInfo, request.assertions, request.variableValues);
  }
};

const assertVariableExtra = ref<any>({});// 替换执行条件执行条件表达式的变量

// 拿到代理请求结果
const onResponse = async () => {
  loading.value = false;
  assertResult.value = undefined;
  let response: any = {};
  try {
    response = JSON.parse(props.response);
  } catch {
    respError.show = true;
    respError.value = props.response;
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
    if (+response.response.status === 0) {
      respError.show = true;
      assertVariableExtra.value = {};
      respError.value = response.response?.rawContent || '';
      responseState.headers = {};
      responseState.config = {};
      responseState.data = null;
      responseState.status = 0;
      responseState.size = 0;
      responseState.cookie = [];
      responseState.requestHeaders = response.response.headers || [];
      responseState.performance = {} as PerformanceEntry;
    } else {
      responseState.config = {
        ...response.request0,
        url: response.request0.url + (response.request0.queryString ? ('?' + response.request0.queryString) : '')
      };

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
      if (response.response.contentEncoding === 'base64') { // 后台转 base64 了
        const mime = header['content-Type'];
        responseState.data = dataURLtoBlob(responseState.data, mime);
      }
      responseState.status = response.response.status;
      responseState.size = response.response.size;
      responseState.performance = {
        ...response.response.timeline,
        duration: +response.response.timeline.total || 0
      };
      const cookieStr = header?.['Set-Cookie'] || header?.['set-cookie'];
      responseState.cookie = cookieStr ? [cookieStr] : [];
      respError.show = false;

      assertResult.value = await setAssertResult(response);
    }
  }
};

const localRequstInfo = reactive<Parameter>({
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

const getValueByType = (assertionCondition: AssertionCondition, type: AssertionType, data: {
  bodySize: number;
  size: number;
  duration: number;
  status: number;
  responseHeader: string[];
  rawContent: string;
  extractValue: string;// 断言条件为正则匹配、xpath匹配、jsonpath匹配的左值
}, parameterName: string): {
  data: string | null;
  message: string;
  errorMessage: string;
} => {
  if (type === 'BODY') {
    if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
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

  if (type === 'BODY_SIZE') {
    return {
      data: data.bodySize + '',
      message: '',
      errorMessage: ''
    };
  }

  if (type === 'DURATION') {
    return {
      data: data.duration + '',
      message: '',
      errorMessage: ''
    };
  }

  if (type === 'HEADER') {
    if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
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

  if (type === 'SIZE') {
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

const setAssertResult = async (responseData) => {
  const assertions: AssertResult[] = responseData.assertions;
  if (!assertions?.length) {
    return;
  }

  const result: AssertResult[] = [];
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
      failure: false, // 执行结果
      name: '', // 提取的变量名
      conditionMessage: '', // 断言表达式错误的原因
      failureMessage: '', // 提取失败的原因
      value: '', // 提取变量的值
      ignored: false, // 是否忽略该条断言
      message: t('service.case.debugModal.conditionMessageEmpty')
    };

    let ignored = true;
    let failure = false;
    if (_assertResult) {
      ignored = _assertResult.ignored;
      failure = _assertResult.failure;

      if (!condition) {
        _condition = {
          failure: false, // 执行结果
          name: '', // 提取的变量名
          conditionMessage: '', // 断言表达式错误的原因
          failureMessage: '', // 提取失败的原因
          value: '', // 提取变量的值
          ignored: false, // 是否忽略该条断言
          message: t('service.case.debugModal.conditionMessageEmpty')
        };
      } else {
        const matchsMap = assertVariableExtra.value?.matchs || {};
        const varMap = assertVariableExtra.value?.vars || {};
        const matchs = matchsMap[condition];
        if (!matchs) {
          _condition = {
            failure: false, // 执行结果
            name: '', // 提取的变量名
            conditionMessage: t('service.case.debugModal.conditionMsgFormat'), // 断言表达式错误的原因
            failureMessage: t('service.case.debugModal.conditionMsgFormatFail'), // 提取失败的原因
            value: '', // 提取变量的值
            ignored: true, // 是否忽略该条断言
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
              failure: true, // 执行结果
              name: leftOperand, // 提取的变量名
              conditionMessage: '', // 断言表达式错误的原因
              failureMessage, // 提取失败的原因
              value, // 提取变量的值
              ignored: true, // 是否忽略该条断言
              message: t('service.case.debugModal.conditionMsgIgnore')
            };
          } else {
            _condition = {
              failure: false, // 执行结果
              name: leftOperand, // 提取的变量名
              conditionMessage: '', // 断言表达式错误的原因
              failureMessage, // 提取失败的原因
              value, // 提取变量的值
              ignored: false, // 是否忽略该条断言
              message: t('service.case.debugModal.conditionMsgExec')
            };
          }
        }
      }
    }

    // 期望值
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
      extractValue,
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

const openToolBar = () => {
  if (!toolbarRef.value.isSpread || !currentTab.value) {
    toolbarRef.value.handleSelected({ value: 'response' });
  }
  if (height.value < 300) {
    height.value = 300;
  }
};

// 点击保存
const save = (): void => {
  if (isUnarchivedApi.value) {
    saveUnarchived();
  } else {
    autoSave();
  }
};

// 归档
const archivedApi = () => {
  drawerRef.value.open('save');
};

// 保存已归档 api
const autoSave = async () => {
  if (state.publishFlag) {
    notification.warning(t('service.apis.notifications.apiPublishedWarning'));
  }
  if (!validateParam()) {
    return;
  }
  let params = await getParameter();
  params = JSON.parse(JSON.stringify(params));
  if (utils.deepCompare(initParams, JSON.parse(JSON.stringify(params)))) { // 比较新旧值
    return;
  }
  if (auths.value.includes('MODIFY') && !state.publishFlag) {
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

// 保存/更新 未归档接口内容
const saveUnarchived = async (): Promise<void> => {
  drawerRef.value.open('saveUnarchived');
};

const isEmpty = computed(() => {
  return !responseState.config || !Object.keys(responseState.config).length;
});

const copyUrl = async () => {
  const pathJson = {};
  const pathArr = state.parameters.filter(i => i.in === 'path');
  pathArr.forEach(i => {
    pathJson[i.name || ''] = i[valueKey];
  });

  // 组装 query 数据 query + 认证 query
  const queryJson = {};
  const quryyArr = state.parameters.filter(i => i.in === 'query');
  quryyArr.forEach(i => {
    queryJson[i.name || ''] = travelEmptyObjToString(i[valueKey]);
  });
  const apiPathQuery = getRealUri(pathJson, queryJson);
  const serverUrl = getServerData(currentServer.value);

  // 组装请求 url
  let apiHref = '';
  if (!serverUrl.endsWith('/') && apiPathQuery && apiPathQuery.split('?')[0] && !apiPathQuery.startsWith('/')) {
    apiHref = serverUrl + '/' + apiPathQuery;
  } else {
    apiHref = serverUrl + apiPathQuery;
  }
  await toClipboard(apiHref);
  notification.success(t('service.apis.notifications.copyUrlSuccess'));
};

watch(() => props.id, () => {
  // 没有id的 或者是 未归档的 api, 都属于未归档
  if (props.valueObj.unarchived || !props.id) {
    isUnarchivedApi.value = true;
    if (props.id) {
      loadApiInfo();
    }
  } else {
    isUnarchivedApi.value = false;
    // 编辑API
    loadApiInfo();
  }
}, {
  immediate: true
});

const moving = ref(false); // 记录当前是否在拖拽中

// 监听 window 变化事件, 重置最大高度
const resizeHandler = debounce(duration.resize, () => {
  nextTick(() => {
    maxHeight.value = mainWrapper.value.clientHeight;
  });
});

onBeforeUnmount(() => {
  erd.removeListener(mainWrapper.value, resizeHandler);
});

watch(() => props.uuid, newValue => {
  if (newValue === uuid) {
    onResponse();
  } else {
    try {
      const data = JSON.parse(props.response);
      authorizationRef.value.onResponse(data);
    } catch { }
  }
});

watch(() => props.ws, (newValue) => {
  loadWS(newValue);
}, {
  immediate: true
});

onMounted(() => {
  resizeHandler();
  erd.listenTo(mainWrapper.value, resizeHandler);
});

const closeDrawer = () => {
  drawerRef.value.close();
};

defineExpose({ autoSave, pid: props.pid });
provide('getParameter', getParameter);
provide('auths', auths);
provide('id', computed(() => saveParams.value.id)); // 提供给子组件 当前 api 的 id
provide('apiBaseInfo', computed(() => saveParams.value)); // api 基本信息
provide('isUnarchivedApi', isUnarchivedApi); // 提供给子组件用于判断接口是否归档
provide('resolvedRefModels', computed(() => resolvedRefModels.value));
provide('archivedId', computed(() => {
  return isUnarchivedApi.value ? undefined : saveParams.value.id;
}));
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
              :disabled="!auths.includes('MODIFY')"
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
                  <!-- <ApiDoc
                    v-show="showDoc"
                    v-model:data="initApiInfo"
                    tab="parameters"
                    class="flex-1" /> -->
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
                  <!-- <ApiDoc
                    v-show="showDoc"
                    :data="initApiInfo"
                    tab="header"
                    class="flex-1" /> -->
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
                <template v-if="['request', 'response', 'time', 'cookie', 'assert'].includes(activeMenu)">
                  <template v-if="respError.show">
                    <ResponseError
                      class="h-full overflow-hidden"
                      :errorTitle="errorTitle"
                      :info="respError.value" />
                  </template>
                  <template v-else-if="!isEmpty">
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
                      v-show="activeMenu === 'cookie'"
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
                <!-- <template v-if="activeMenu === 'define'">
                  <ApiDefine
                    :id="props.id"
                    :serviceId="saveParams.serviceId"
                    :value="saveParams.responses"
                    class="pt-4" />
                </template> -->

                <template v-if="activeMenu === 'generateCode'">
                  <CodeSnippetVue class="px-5 h-full overflow-auto" />
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
              shareScope="SINGLE_APIS" />
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
            :disabled="!auths.includes('MODIFY')" />
        </template>

        <template v-if="activeTabKey === 'doc'">
          <ApiDoc
            :id="props.id"
            class="mx-5 mt-3 flex-1 overflow-auto mb-3" />
        </template>

        <Spin
          :spinning="loadingInfo"
          :class="{ '-z-1': !loadingInfo }"
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
          <UnarchivedEditVue
            v-if="activeDrawerKey === 'saveUnarchived'"
            :id="props.id"
            class="pr-4"
            type="API" />
        </template>
        <template #save>
          <InfoEditVue
            v-if="activeDrawerKey === 'save'"
            :id="props.id"
            class="pr-5"
            :disabled="!auths.includes('MODIFY')"
            type="API" />
        </template>
        <template #performance>
          <Indicator
            v-if="activeDrawerKey === 'performance'"
            :id="props.id"
            :disabled="!auths.includes('MODIFY')"
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
        <!-- <template #variable>
          <VariableVue
            v-if="activeDrawerKey === 'variable'"
            :id="props.id"
            :disabled="!auths.includes('MODIFY')"
            class="pr-5 pt-2"
            type="API" />
        </template> -->
        <template #apiMock>
          <!-- <ApiMockVue
            v-if="activeDrawerKey === 'apiMock'"
            :id="props.id"
            class="pt-2 pr-5"
            :disabled="!auths.includes('MODIFY')" /> -->
        </template>
        <template #share>
          <ShareListVue
            v-if="activeDrawerKey === 'share'"
            :id="props.id"
            class="mt-2 pr-5"
            :disabled="!auths.includes('SHARE')"
            type="API" />
        </template>
        <template #agent>
          <AgentVue v-if="activeDrawerKey === 'agent'" class="pr-5 mt-2" />
        </template>
        <template #cases>
          <!-- <TestCase
            v-if="activeDrawerKey === 'case'"
            :id="props.id"
            type="API"
            class="pr-5" /> -->
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
