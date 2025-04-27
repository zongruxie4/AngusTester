<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, reactive, ref, watch } from 'vue';
import { Input, NoData, notification, Select, SelectInput } from '@xcan-angus/vue-ui';
import { Badge, TabPane, Tabs } from 'ant-design-vue';
import qs from 'qs';
import XML from 'xml';
import { axiosClient, TESTER, http, utils } from '@xcan-angus/tools';
import SwaggerUI from 'swagger-ui';
import apiUtils from 'angus-design/utils';
import assertUtils from 'angus-design/assertUtils';
import authUtils from 'angus-design/authUtils';
import { type AssertResult, type ConditionResult } from 'angus-design/types/types';
import 'angus-design/style.css';
import {
  ApiInfo,
  getModelFromRef,
  isValidUrl,
  rawTypeOptions,
  ToolBarMenus,
  travelDelSchemaRef,
  travelEmptyObjToString
} from './PropsType';
import { getRequestBodyData } from './utils';
// import PIcon from './PIcon/index.vue';
import { API_EXTENSION_KEY } from './PropsType.ts';
import { services } from '@/api/altester';

export interface Props {
  disabled: boolean;
  case: ApiInfo;
  viewType: boolean;
  debuging?: boolean;
  validateInfos?: { apisId: any };
  apisId: string;
  serviceId: string;
  caseInfoData: {id:string;datasetActionOnEOF:string;datasetSharingMode:string;};
}

const props = withDefaults(defineProps<Props>(), {
  apisId: '',
  disabled: false,
  viewType: false,
  validateInfos: () => ({ apisId: {} }),
  caseInfoData: undefined,
  serviceId: ''
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:debuging', value: boolean): void;
  (e: 'update:rendered', value: boolean): void;
  (e: 'apiChange', value?: string): void
}>();

const ApiParameter = defineAsyncComponent(() => import('angus-design').then(design => design.ApiParameter));
const ApiHeader = defineAsyncComponent(() => import('angus-design').then(design => design.ApiHeader));
const ApiCookie = defineAsyncComponent(() => import('angus-design').then(design => design.ApiCookie));
const ApiBody = defineAsyncComponent(() => import('angus-design').then(design => design.ApiBody));
const ApiAssert = defineAsyncComponent(() => import('angus-design').then(design => design.ApiAssert));
const Toolbar = defineAsyncComponent(() => import('angus-design').then(design => design.Toolbar));
const ResponseBase = defineAsyncComponent(() => import('angus-design').then(design => design.ResponseBase));
const ResponseBody = defineAsyncComponent(() => import('angus-design').then(design => design.ResponseBody));
const ResponseCookie = defineAsyncComponent(() => import('angus-design').then(design => design.ResponseCookie));
const ApiAuthencation = defineAsyncComponent(() => import('angus-design').then(design => design.ApiAuthencation));
const ResponseError = defineAsyncComponent(() => import('angus-design').then(design => design.ResponseError));
const ResponseTimeAnalysis = defineAsyncComponent(() => import('angus-design').then(design => design.ResponseTimeAnalysis));
const ResponseAssert = defineAsyncComponent(() => import('angus-design').then(design => design.ResponseAssert));
const ResponseStatus = defineAsyncComponent(() => import('angus-design').then(design => design.ResponseStatus));
// const parameterization = defineAsyncComponent(() => import('@/components/parameterization/index.vue'));

const allFunction = inject('allFunction', ref([]));

const allFuncNames = computed(() => {
  return allFunction.value.map(i => i.name);
});
const { valueKey, enabledKey, serverSourceKey, fileNameKey } = API_EXTENSION_KEY;
const activeTab = ref('parameters');
const apiEleRef = ref();
const WS = inject('WS', ref());
const requestId = inject('requestId', ref());
const responseData = inject('responseData', ref());
const myRequest = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });
const uuid = ref();
// const apiBaseInfo = inject('apiBaseInfo', ref({serviceId: ''}));

const authInHeader = ref({});

// 用例信息
const caseInfo = reactive<{
  apisId?: string;
  method: string;
  summary: string;
  endpoint: string;
  protocol: 'http'|'https';
  currentServer: { url: string };
}>({
  apisId: '',
  method: '',
  endpoint: '',
  summary: '',
  protocol: 'http',
  currentServer: { url: '' }
});

const avariableServers = ref([]);

const queryPathRef = ref();
const headerRef = ref();
const cookieRef = ref();
const bodyRef = ref();
const assertRef = ref();
const authenticationRef = ref();

const contentType = ref();
const queryPathParameter = ref([]);
const headerParameter = ref([]);
const cookieParameter = ref([]);
const requestBodyParam = ref({});
let resolveModel = {};
const authenticationParam = ref({ type: null, $ref: undefined });
const assertionsParam = ref([]);

const renderedNum = ref(0);
const rendered = (key:string) => {
  if (['apiParameter', 'apiHeader', 'apiCookie', 'apiAuthencation', 'apiAssert', 'toolbar'].includes(key)) {
    renderedNum.value++;
  }

  // 6个子组件渲染完成，执行调试用例操作不会报错，因为会用到这6个子组件的方法，确保6个子组件渲染完成
  if (renderedNum.value >= 6) {
    emits('update:rendered', true);
  }
};

const changeAuthentication = (value) => {
  authenticationParam.value = value;
};

const changeQueryPathParams = (data) => {
  queryPathParameter.value = data.filter(i => !!i.name || !!i[valueKey]);
};

const changeHeaderParams = (data) => {
  headerParameter.value = data;
};

const changeCookieParams = (data) => {
  cookieParameter.value = data;
};

const changeCurrentServer = (value, option) => {
  if (option) {
    caseInfo.currentServer = option;
  } else {
    caseInfo.currentServer = { url: value };
  }
  if (caseInfo.currentServer.url) {
    try {
      const url = new URL(caseInfo.currentServer.url);
      caseInfo.protocol = (url.protocol || '').replace(':', '');
    } catch {
      caseInfo.protocol = 'http';
    }
  }
};

const handleBodyChange = (value) => {
  requestBodyParam.value = value;
};

const toolbarHeight = ref(30);
const toolbarMaxHeight = ref(500);
const currentTab = ref();
const toolBarMoving = ref(false);
const respError = reactive({
  show: false,
  value: undefined
});

const assertResult = ref<AssertResult[]>();// 断言结果
const responseState = reactive({
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

const analysisApiParam = async (data) => {
  const { endpoint, parameters, requestBody, authentication, assertions, resolvedRefModels, serviceId } = data || {};

  authenticationParam.value = authentication || { type: null };
  assertionsParam.value = (assertions || []).map(item => ({
    ...item,
    assertionCondition: item.assertionCondition?.value,
    extraction: item.extraction
      ? ({
          ...item.extraction,
          method: item.extraction.method.value
        })
      : null
  }));
  let queryStrParam = [];
  if (endpoint.split('?')[1]) {
    const queryString = endpoint.split('?')[1];
    const queryStrArr = queryString.split('&');
    queryStrParam = queryStrArr.map(keyValue => {
      const [key, value] = keyValue.split('=');
      return apiUtils.getDefaultParams({ [valueKey]: value || '', name: key, in: 'query' });
    });
    caseInfo.endpoint = endpoint.split('?')[0];
  }
  for (const key in parameters) {
    if (parameters[key].$ref) {
      const model = await getModelFromRef(serviceId, parameters[key].$ref);
      if (model.name) {
        if (!model.schema?.type) {
          if (model.schema?.properties) {
            model.schema.type = 'object';
            if (typeof model[valueKey] === 'string') {
              try {
                model[valueKey] = JSON.parse(model[valueKey]);
              } catch {
                console.log(model[valueKey] + 'is not a json string');
              }
            }
          } else if (model.schema?.items) {
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
        delete parameters[key].$ref;
        parameters[key] = { ...parameters[key], ...model };
      } else {
        parameters[key] = { schema: { type: 'string' }, name: undefined, in: 'query', [valueKey]: '' };
      }
    }
    if (['object', 'array', 'boolean', 'integer', 'number'].includes(parameters[key].schema?.type) && parameters[key]?.[valueKey] && typeof parameters[key]?.[valueKey] === 'string') {
      try {
        if (['integer', 'number'].includes(parameters[key].schema?.type)) {
          if (parameters[key][valueKey] <= 9007199254740992 && parameters[key][valueKey] >= -9007199254740992) {
            parameters[key][valueKey] = JSON.parse(parameters[key][valueKey]);
          }
        } else {
          parameters[key][valueKey] = JSON.parse(parameters[key][valueKey]);
        }
      } catch {
        console.log(parameters[key][valueKey] + 'is not an object');
      }
    }
    if (!parameters[key][valueKey] && parameters[key].schema?.type && (parameters[key].schema?.[valueKey] || parameters[key].schema?.properties || parameters[key].schema?.items)) {
      parameters[key][valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(parameters[key].schema, { useValue: true });
    }
  }
  queryPathParameter.value = [...queryStrParam, ...(parameters?.filter(item => ['query', 'path'].includes(item.in) && !!item.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }))];
  headerParameter.value = (parameters?.filter(i => i.in === 'header' && !!i.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  const contentTypeIndex = headerParameter.value.findIndex(i => i.name === 'Content-Type');
  if (contentTypeIndex > -1) {
    contentType.value = headerParameter.value[contentTypeIndex]?.[valueKey];
    if (contentType.value) {
      headerParameter.value.splice(contentTypeIndex, 1);
    }
  }
  cookieParameter.value = (parameters?.filter(i => i.in === 'cookie' && !!i.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  requestBodyParam.value = JSON.parse(JSON.stringify(requestBody || {}));
  // if (!resolvedRefModels) {
  //   resolveModel = awa
  // }
  resolveModel = resolvedRefModels || {};
};

const serviceId = ref();
// 接口详情
const loadApiInfo = async (apisId) => {
  bodyRef.value && bodyRef.value.resetBodyData();
  const [error, resp] = await http.get(`${TESTER}/apis/${apisId}`, {
    resolveRefFlag: true
  });
  if (error) {
    return;
  }

  if (resp.data.availableServers?.length) {
    const targetServer = resp.data.availableServers.find(item => item[serverSourceKey] === 'CURRENT_REQUEST') || resp.data.availableServers[0];
    caseInfo.currentServer = targetServer;
    avariableServers.value = resp.data.availableServers;
  }
  caseInfo.method = resp.data.method.value;
  caseInfo.endpoint = resp.data.endpoint;
  caseInfo.apisId = apisId;

  const data = resp.data;
  serviceId.value = data.serviceId;
  caseInfo.protocol = data.protocol.value || data.protocols;
  analysisApiParam(data);
};

const getData = async () => {
  function returnData () {
    const queryParameters = travelDelSchemaRef(queryPathRef.value.getData());
    const headerParameter = travelDelSchemaRef(headerRef.value.getData());
    const cookieParameter = travelDelSchemaRef(cookieRef.value.getData());
    const assertions = assertRef.value ? assertRef.value.getData().data : [];
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { summary, ...caseInfoParams } = caseInfo;
    const bodyParams = bodyRef.value ? requestBodyParam.value : apiUtils.analysisBody(requestBodyParam.value, resolveModel);
    return {
      ...caseInfoParams,
      parameters: [...queryParameters, ...headerParameter, ...cookieParameter],
      requestBody: travelDelSchemaRef(bodyParams),
      authentication: (authenticationParam.value?.type || authenticationParam.value?.$ref) ? authenticationParam.value : undefined,
      assertions
    };
  }

  return new Promise((resolve) => {
    function delayTime () {
      setTimeout(() => {
        if (!queryPathRef.value || !headerRef.value || !cookieRef.value || !assertRef.value) {
          delayTime();
        } else {
          return resolve(returnData());
        }
      }, 1000);
    }
    delayTime();
  });
};

const errorTitle = ref();
const setErrTitle = () => {
  if (WS.value?.readyState === 1) {
    errorTitle.value = '发送请求出现错误（请检查请求参数或代理连接状态）';
    return;
  }
  errorTitle.value = '发送请求出现错误（请检查请求参数或是否触发浏览器“CORS-跨源资源共享”限制，若触发跨域限制请使用代理或配置接口服务允许跨域访问）';
};

const getRealUri = (pathParams: Record<string, any>, queryParams: Record<string, any>) => {
  let tempUri = caseInfo.endpoint || '';
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
  // const querys: ParamsItem[] = queryParams;
  // if (querys?.length) {
  //   const queryUri = querys.reduce((preValue, currentValue) => {
  //     preValue = preValue + `${preValue ? '&' : ''}${currentValue.name}=${currentValue[valueKey] || ''}`;
  //     return preValue;
  //   }, '');
  //   if (tempUri.includes('?')) {
  //     tempUri = tempUri + '&' + queryUri;
  //   } else {
  //     tempUri = tempUri + '?' + queryUri;
  //   }
  // }
  const queryUri = qs.stringify(queryParams, { allowDots: true, encode: true });
  if (tempUri.includes('?')) {
    tempUri = tempUri + '&' + queryUri;
  } else if (queryUri) {
    tempUri = tempUri + '?' + queryUri;
  }
  return tempUri;
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

const getValueByType = (_assertionCondition: string, _type: string, data: {
  bodySize: number;
  size: number;
  duration: number;
  status: number;
  responseHeader: string[];
  rawContent: string;
  extractValue: string;// 断言条件为正则匹配、xpath匹配、jsonpath匹配的左值
}, headerName: string): {
  data: string | null;
  message: string;
  errorMessage: string;
} => {
  const assertionCondition = _assertionCondition?.value || _assertionCondition;
  const type = _type?.value || _type;

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
      data: getHeaderParams(data.responseHeader, headerName),
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
  const assertions: any[] = responseData.assertions;
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
      headerName,
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
      message: '表达式为空，执行该条断言'
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
          message: '表达式为空，执行该条断言'
        };
      } else {
        const matchsMap = assertVariableExtra.value?.matchs || {};
        const varMap = assertVariableExtra.value?.vars || {};
        const matchs = matchsMap[condition];
        if (!matchs) {
          _condition = {
            failure: false, // 执行结果
            name: '', // 提取的变量名
            conditionMessage: '表达式格式错误，仅支持运算符["=", "!=", ">=", "<=", ">", "<"]', // 断言表达式错误的原因
            failureMessage: '表达式格式错误，无法提取变量名', // 提取失败的原因
            value: '', // 提取变量的值
            ignored: true, // 是否忽略该条断言
            message: '表达式格式错误，忽略该条断言'
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
            failureMessage = '没有定义该变量，该变量的值按变量名处理';
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
              message: '运算结果不成立，忽略该条断言'
            };
          } else {
            _condition = {
              failure: false, // 执行结果
              name: leftOperand, // 提取的变量名
              conditionMessage: '', // 断言表达式错误的原因
              failureMessage, // 提取失败的原因
              value, // 提取变量的值
              ignored: false, // 是否忽略该条断言
              message: '运算结果成立，执行该条断言'
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

    const realValueData = getValueByType(assertionCondition, type, data, headerName);
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
      type,
      headerName,
      condition,
      assertionCondition,
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

// header 计数
const headerCount = computed(() => {
  let base = headerParameter.value.length;
  if (authInHeader.value) {
    const length = Object.keys(authInHeader.value).length;
    base += length;
  }
  if (contentType.value) {
    base += 1;
  }
  return base;
});
// 请求体是否有数据
const hasBodyContent = computed(() => {
  return !!contentType.value || !!requestBodyParam.value?.content;
});
// 认证是否有选择类型
const hasAuthentication = computed(() => {
  return !!authenticationParam.value.type || !!authenticationParam.value.$ref;
});

const assertVariableExtra = ref<any>({});// 替换执行条件执行条件表达式的变量

const debuging = ref(false);
let controller;
const debug = async () => {
  if (debuging.value) {
    return;
  }
  debuging.value = true;
  emits('update:debuging', debuging.value);
  const apiData = await getData();

  // 校验地址
  if (!isValidUrl(caseInfo.currentServer?.url)) {
    debuging.value = false;
    emits('update:debuging', debuging.value);
    notification.warning('接口地址无效，请重新选择');
    return;
  }

  // 准备 body 数据
  let requestBodyContents = await (bodyRef.value ? bodyRef.value.getBodyData(apiData.requestBody, contentType.value) : getRequestBodyData(apiData.requestBody, resolveModel, contentType.value));
  let formData: any[] = [];
  const strBody: string[] = [];
  if (contentType.value === 'application/x-www-form-urlencoded' || contentType.value === 'multipart/form-data') {
    formData = requestBodyContents;
  } else if (typeof requestBodyContents === 'string') {
    strBody.push(requestBodyContents);
  }

  //  准备认证数据
  const auth = authenticationRef.value && apiData.authentication ? await authenticationRef.value.getAuthData(apiData.authentication) : [{}];
  const authParamData = Object.keys(auth?.[0] || {}).map(key => {
    if (typeof auth?.[0]?.[key] !== 'object') {
      return { name: key, [valueKey]: auth?.[0]?.[key] };
    } else {
      const { username, password } = auth?.[0]?.[key] || {};
      strBody.push(username || '');
      strBody.push(password || '');
    }
    return undefined;
  }).filter(Boolean); // header 认证
  const authQueryData = Object.keys(auth?.[1] || {}).map(key => ({ name: key, [valueKey]: auth?.[1]?.[key] })); // query 认证

  // 准备 header、 cookie 数据
  const headerCookieData = apiData.parameters.filter(i => ['header', 'cookie'].includes(i.in) && i.name && i[valueKey] && i[enabledKey]);
  // 准备 query 、path 数据
  const querPathData = apiData.parameters.filter(i => ['query', 'path'].includes(i.in) && i.name && i[enabledKey]);
  // 断言数据
  let assertions: any[] = [];
  let asserVariableStr: string[] = [];
  if (typeof assertRef.value?.getData === 'function') {
    const assertFormData = assertRef.value.getData();
    assertions = assertFormData.data;
    asserVariableStr = assertFormData.variables;
  } else {
    assertions = apiData.assertions;
  }

  // 替换 mock 函数和变量
  const result = await apiUtils.replaceFuncValue({ parameter: [querPathData, formData, headerCookieData, authParamData, authQueryData], str: strBody, variableStr: asserVariableStr }, allFuncNames.value, props.apisId, 'API', { ignoreErr: false });
  if (!result) {
    debuging.value = false;
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
        { name: 'Authorization', [valueKey]: 'basic ' + apiUtils.encode(strValues[0], strValues[1]) }
      ];
    }
    if (strValues.length === 3) {
      requestBodyContents = strValues[0];
      funcValues[3] = [
        ...funcValues[3],
        { name: 'Authorization', [valueKey]: 'basic ' + apiUtils.encode(strValues[1], strValues[2]) }
      ];
    }
  }
  // 校验 断言数据
  const validateAssertData = assertRef.value ? assertRef.value.validate() : true;
  if (!validateAssertData) {
    debuging.value = false;
    emits('update:debuging', debuging.value);
    notification.error('断言数据校验不通过');
    return;
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
              requestBody.append(item.name, file?.originFileObj || '', file[fileNameKey] || '');
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
  // const pathJson = getJsonFromSplitData(funcValues[0]);
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
  const serverUrl = apiUtils.getServerData(caseInfo.currentServer);

  // parameterQuery 的 JSON 格式
  const requestQueryJson = qs.parse(apiPathQuery.split('?')[1] || '');

  // 组装请求 url
  let apiHref = '';
  if (!serverUrl.endsWith('/') && apiPathQuery && apiPathQuery.split('?')[0] && !apiPathQuery.startsWith('/')) {
    apiHref = serverUrl + '/' + apiPathQuery;
  } else {
    apiHref = serverUrl + apiPathQuery;
  }
  uuid.value = utils.uuid('api-item');

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
        const value = item[valueKey].map(value => value && apiUtils.containsAllAscii(value) ? value : value ? encodeURIComponent(value) : '').join(',');
        return `${item.name}=${value || ''}`;
      } else {
        const value: string[] = [];
        Object.entries(item[valueKey]).forEach(([key, val]) => {
          value.push(key);
          value.push(val && apiUtils.containsAllAscii(val) ? val : val ? encodeURIComponent(val) : '');
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

  if (WS.value && WS.value.readyState === 1) { // 代理连接正常
    const { requestBody, apisId, endpoint, ...params } = apiData;
    if (!params?.authentication?.type && !params?.authentication?.$ref) {
      delete params.authentication;
    } else if (params?.authentication?.$ref) {
      const [_error, resp] = await services.getRefInfo(serviceId.value || props.serviceId, params?.authentication.$ref);
      params.authentication = JSON.parse(resp.data.model);
    }
    if (requestBody?.content) {
      Object.keys(requestBody.content).forEach(key => {
        if (rawTypeOptions.includes(key)) {
          if (requestBody.content[key][valueKey] && requestBody.content[key]?.schema?.format !== 'binary') {
            delete requestBody.content[key].schema;
          }
        }
      });
    }
    // 过滤掉没有启用的断言
    const conditions = params.assertions.filter(item => item.condition).map(item => item.condition);
    const { extra } = assertUtils.proxy.execute(conditions, variableValues);
    assertVariableExtra.value = extra;

    const jsonStr = JSON.stringify({
      ...params,
      id: apisId,
      requestBody,
      server: caseInfo.currentServer,
      requestId: uuid.value,
      messageType: 'HttpRequestProxy',
      endpoint: endpoint,
      variables: [...variableValues]
    });
    WS.value.send(jsonStr);
    setErrTitle();
    openToolBar();
  } else {
    controller = new AbortController();
    const signal = controller.signal;
    const axiosConfig = {
      responseType: 'blob',
      url: apiHref,
      method: caseInfo.method,
      data: requestBody,
      headers: {
        accept: '*/*',
        ...headers
      },
      signal,
      timeout: 66000,
      maxRedirects: 1,
      maxRetries: 0
    };
    const responese = await myRequest.request(axiosConfig);
    setErrTitle();
    await onHttpResponse(responese, { query: requestQueryJson, header: headers, path: pathJson, requestBody: showRequestBody, queryString: apiPathQuery.split('?')[1] || '', assertions, variableValues });
    openToolBar();
  }
};

// 本地请求响应
const onHttpResponse = async (resp, request) => {
  debuging.value = false;
  emits('update:debuging', debuging.value);
  respError.show = false;
  respError.value = undefined;
  responseState.performance = resp.performance;
  assertResult.value = undefined;
  if (resp.request.status === 0) {
    respError.show = true;
    respError.value = resp.message;
    responseState.config = {
      url: decodeURIComponent(resp.config.url),
      method: caseInfo.method,
      status: 0,
      queryString: request.queryString
    };
    responseState.headers = resp.config.headers;
  } else if (resp.request.status < 200 || resp.request.status >= 300) {
    responseState.data = await convertBlob(resp.response.data);
    responseState.config = {
      url: decodeURIComponent(resp.config.url),
      method: caseInfo.method,
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
      method: caseInfo.method,
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

// 代理请求响应
const onResponse = async () => {
  debuging.value = false;
  emits('update:debuging', debuging.value);
  assertResult.value = undefined;
  let response: Record<string, any> = {};
  try {
    response = JSON.parse(responseData.value);
  } catch {
    respError.show = true;
    respError.value = responseData.value;
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
      // if (response.response.rawContent) {
      // 拿到返回值的类型, 如果是文件或者下载文件的用toUint8Array转义, 否则用decode转义
      // const contentType = response.response.contentType;
      // if (RESP_FILE_TYPE.find(type => contentType.includes(type))) {
      //   response.responseBody = toUint8Array(response.responseBody);
      // } else {
      //   response.responseBody = decode(response.responseBody, true);
      // }
      // }
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
      if (response.response.contentEncoding === 'base64') { // 后台转 base64 了
        // responseState.data = atob(responseState.data);
        // const mime = response.response.contentType.split(';')[0];
        const mime = header['content-Type'];
        responseState.data = apiUtils.dataURLtoBlob(responseState.data, mime);
      }
      responseState.status = response.response.status;
      responseState.size = response.response.size;
      responseState.performance = {
        ...response.response.timeline,
        duration: +response.response.timeline.total || 0
      };
      responseState.cookie = header?.['Set-Cookie'] || header?.['set-cookie'] || [];
      respError.show = false;

      assertResult.value = await setAssertResult(response);
    }
  }
};

// @TODO 将响应结果blob转为对应的数据类型
const convertBlob = (blob: Blob) => {
  return new Promise((resolve) => {
    const mimeType = blob.type;
    const reader = new FileReader();

    reader.onload = () => {
      resolve(reader.result);
    };

    reader.onerror = () => {
      resolve(blob);
    };

    if (/^(text)\/{1}.+/gi.test(mimeType) || /^(application\/json).*/gi.test(mimeType)) {
      reader.readAsText(blob, 'utf-8');
    } else {
      resolve(blob);
    }
  }).then((res) => {
    try {
      return JSON.parse(res as string);
    } catch (error) {
      return res;
    }
  });
};

const localRequstInfo = reactive({
  responseHeader: {},
  responseBody: {
    data: undefined,
    size: 0
  },
  rawBody: undefined,
  query: {},
  path: {},
  header: {},
  form: undefined,
  duration: 0,
  status: undefined
});

const toolbarMenus = computed(() => {
  if (WS.value && WS.value.readyState === 1) {
    return ToolBarMenus;
  }
  return ToolBarMenus.filter(i => i.value !== 'time');
});

const toolbarRef = ref();
const openToolBar = () => {
  debugInfoExpand.value = true;
  if (!toolbarRef.value.isSpread || !currentTab.value) {
    toolbarRef.value.handleSelected({ value: 'response' });
  }
  if (toolbarHeight.value < 300) {
    toolbarHeight.value = 300;
  }
};

// 终止请求
const stopDebug = () => {
  debuging.value = false;
  emits('update:debuging', debuging.value);
  if (WS.value?.readyState === 1) {
    uuid.value = undefined;
  } else {
    controller.abort();
  }
};

// const getApiAvaribaleServer = async (apisId) => {
//   const [error, res] = await http.get(`${TESTER}/apis/${apisId}`);
//   if (error) {
//     avariableServers.value = [];
//     return;
//   }
//   avariableServers.value = res.data.availableServers || [];
// };

watch(() => props.case, async (newValue) => {
  if (!newValue) {
    return;
  }

  bodyRef.value && bodyRef.value.resetBodyData();
  contentType.value = null;
  activeTab.value = 'parameters';
  if (newValue.id) {
    // await loadApiResolveModel();
    Object.keys(caseInfo).forEach(key => {
      caseInfo[key] = props.case[key] || undefined;
    });
    analysisApiParam(newValue);
  } else {
    Object.keys(caseInfo).forEach(key => {
      caseInfo[key] = undefined;
    });
    caseInfo.currentServer = { url: '' };
    queryPathParameter.value = [];
    headerParameter.value = [];
    cookieParameter.value = [];
    requestBodyParam.value = {};
    authenticationParam.value = { type: null };
    assertionsParam.value = [];
    if (props.apisId) {
      await loadApiInfo(props.apisId);
    }
  }
}, {
  deep: true,
  immediate: true
});

watch(() => requestId.value, newValue => {
  if (newValue === uuid.value) {
    onResponse();
  } else {
    try {
      const data = JSON.parse(responseData.value);
      authenticationRef.value.onResponse(data);
    } catch { }
  }
});

// 显示认证信息在请求头
watch(() => authenticationParam.value, async newValue => {
  const data = await authUtils.getShowAuthData(newValue);
  authInHeader.value = data?.[0] || {};
}, {
  deep: true,
  immediate: true
});

const resetMaxheight = () => {
  if (apiEleRef.value) {
    toolbarMaxHeight.value = apiEleRef.value.clientHeight + 30;
    if (toolbarMaxHeight.value < 300) {
      toolbarMaxHeight.value = 300;
    }
  }
};

const resetResponse = () => {
  uuid.value = undefined;
  responseState.status = 0;
  currentTab.value = undefined;
  toolbarHeight.value = 30;
};

// 参数化逻辑 - start
const caseId = ref<string>();
// const datasetActionOnEOF = ref<'RECYCLE' | 'STOP_THREAD'>('RECYCLE');
// const datasetSharingMode = ref<'ALL_THREAD' | 'CURRENT_THREAD'>('ALL_THREAD');

// const targetInfoChange = (data: { id: string; datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD'; datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD'; }) => {
//   datasetActionOnEOF.value = data.datasetActionOnEOF;
//   datasetSharingMode.value = data.datasetSharingMode;
// };
// 参数化逻辑 - end

onMounted(() => {
  setTimeout(() => {
    resetMaxheight();
  }, 50);

  watch(() => props.caseInfoData, (newValue) => {
    if (!newValue) {
      return;
    }

    caseId.value = newValue.id;
    // datasetActionOnEOF.value = newValue?.datasetActionOnEOF?.value || newValue?.datasetActionOnEOF || 'RECYCLE';
    // datasetSharingMode.value = newValue?.datasetSharingMode?.value || newValue?.datasetSharingMode || 'ALL_THREAD';
  }, { immediate: true });
});

const debugInfoExpand = ref(true);

defineExpose({
  getData,
  debug,
  stopDebug,
  resetResponse
});
</script>
<template>
  <span class="text-3 ">
    测试接口
  </span>
  <div class="flex space-x-2 pr-20">
    <Select
      :value="caseInfo.method"
      disabled
      class="!w-24" />
    <SelectInput
      :title="caseInfo.currentServer.url"
      :value="caseInfo.currentServer.url"
      class="!w-56"
      :options="avariableServers"
      :fieldNames="{ value: 'url', label: 'url' }"
      @change="changeCurrentServer">
    </SelectInput>
    <Input
      :value="caseInfo.endpoint"
      disabled
      class="flex-1" />
  </div>
  <span class="text-3 mt-4">
    测试用例
  </span>
  <div ref="apiEleRef" class="flex-1 min-h-0 overflow-auto pr-2">
    <Tabs v-model:activeKey="activeTab" size="small">
      <TabPane
        key="parameters"
        :tab="`请求参数(${queryPathParameter?.length || 0})`"
        :forceRender="true">
        <ApiParameter
          ref="queryPathRef"
          v-model:apiUri="caseInfo.endpoint"
          :hideImportBtn="true"
          :viewType="props.viewType"
          :value="queryPathParameter"
          @change="changeQueryPathParams"
          @rendered="rendered('apiParameter')" />
        <NoData v-show="props.viewType && !queryPathParameter.length" size="small" />
      </TabPane>
      <TabPane
        key="header"
        :tab="`请求头(${headerCount || 0})`"
        :forceRender="true">
        <ApiHeader
          ref="headerRef"
          :contentType="contentType"
          :hideImportBtn="true"
          :authData="authInHeader"
          :viewType="props.viewType"
          :value="headerParameter"
          @change="changeHeaderParams"
          @rendered="rendered('apiHeader')" />
        <NoData v-show="props.viewType && !headerParameter.length" size="small" />
      </TabPane>
      <TabPane
        key="cookie"
        :tab="`Cookie(${cookieParameter?.length || 0})`"
        :forceRender="true">
        <ApiCookie
          ref="cookieRef"
          :hideImportBtn="true"
          :viewType="props.viewType"
          :value="cookieParameter"
          @change="changeCookieParams"
          @rendered="rendered('apiCookie')" />
        <NoData v-show="props.viewType && !cookieParameter.length" size="small" />
      </TabPane>
      <TabPane key="body" :forceRender="false">
        <template #tab>
          <Badge v-show="hasBodyContent" status="success" />
          <span>请求体</span>
        </template>
        <ApiBody
          ref="bodyRef"
          v-model:contentType="contentType"
          class="-mt-2"
          :hideImportBtn="true"
          :viewType="props.viewType"
          :defaultValue="requestBodyParam"
          @change="handleBodyChange"
          @rendered="rendered('apiBody')" />
      </TabPane>
      <TabPane key="authentication" :forceRender="true">
        <template #tab>
          <Badge v-show="hasAuthentication" status="success" />Authorization
        </template>
        <ApiAuthencation
          ref="authenticationRef"
          class="-mt-2"
          :viewType="props.viewType"
          :defaultValue="authenticationParam"
          @change="changeAuthentication"
          @rendered="rendered('apiAuthencation')" />
      </TabPane>
      <!-- <TabPane
        v-if="!!caseId"
        key="parametric"
        tab="参数化">
        <parameterization
          :datasetActionOnEOF="datasetActionOnEOF"
          :datasetSharingMode="datasetSharingMode"
          :targetId="caseId"
          targetType="API_CASE"
          @targetInfoChange="targetInfoChange" />
      </TabPane> -->
    </Tabs>
    <div v-show="assertionsParam.length || !props.viewType" class="mt-20">
      <div class="border-b mb-3 text-3">断言</div>
      <ApiAssert
        :id="caseInfo.apisId"
        ref="assertRef"
        :value="assertionsParam"
        :viewType="props.viewType"
        @rendered="rendered('apiAssert')" />
    </div>
  </div>
  <Toolbar
    ref="toolbarRef"
    v-model:height="toolbarHeight"
    v-model:tab="currentTab"
    v-model:moving="toolBarMoving"
    :showZoomBtn="false"
    :maxHeight="toolbarMaxHeight"
    :menus="toolbarMenus"
    @rendered="rendered('toolbar')">
    <template #content="{ activeMenu }">
      <div class="relative h-full flex-1 min-h-0">
        <template v-if="!uuid">
          <NoData size="small" class="my-10" />
        </template>
        <template v-else-if="respError.show">
          <ResponseError
            :errorTitle="errorTitle"
            :info="respError.value"
            @rendered="rendered('responseError')" />
        </template>
        <template v-else>
          <ResponseBase
            v-show="activeMenu === 'request'"
            :dataSource="responseState"
            @rendered="rendered('responseBase')" />
          <ResponseBody
            v-show="activeMenu === 'response'"
            :dataSource="responseState"
            @rendered="rendered('responseBody')" />
          <ResponseTimeAnalysis
            v-show="activeMenu === 'time'"
            :dataSource="responseState.performance"
            @rendered="rendered('responseTimeAnalysis')" />
          <ResponseCookie
            v-show="activeMenu === 'cookie'"
            :dataSource="responseState.cookie"
            :host="caseInfo.currentServer?.url"
            @rendered="rendered('responseCookie')" />
          <ResponseAssert
            v-show="activeMenu === 'assert'"
            :value="assertResult"
            @rendered="rendered('responseAssert')" />
          <NoData
            v-show="!activeMenu"
            class="mt-4"
            size="small" />
        </template>
      </div>
    </template>
    <template #actions>
      <template v-if="responseState?.status">
        <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5 text-3 leading-3 text-text-sub-content">
          <ResponseStatus :status="responseState?.status" @rendered="rendered('responseStatus')" />
          <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5">
            <span class="mr-1">耗时:</span>
            <span class>{{ responseState?.performance?.duration && (responseState?.performance?.duration.toFixed(0)
              + 'ms') }}</span>
          </div>
          <div class="flex items-center flex-nowrap whitespace-nowrap">
            <span class="mr-1">大小:</span>
            <span class>{{ isNaN(Number(responseState?.size)) ? responseState?.size :
              apiUtils.formatBytes(Number(responseState?.size)) }}</span>
          </div>
        </div>
      </template>
    </template>
  </Toolbar>
</template>
