import { ref, reactive, computed } from 'vue';
import { AssertionType, AssertionCondition, utils } from '@xcan-angus/infra';
import { dataURLtoBlob } from '@/utils/blob';
import { convertBlob } from '@/utils/apis';
import { HTTP_HEADERS, CONTENT_TYPE_KEYS } from '@/utils/constant';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { AssertResult, ConditionResult, Extraction, Parameter } from '@/views/apis/services/protocol/http/types';
import assertUtils from '@/utils/assertutils';

/**
 * 响应状态接口
 */
export interface ResponseState {
  config?: Record<string, any>;
  headers?: Record<string, any>;
  data?: any;
  status?: number;
  size?: number;
  assertions?: any[];
  performance?: PerformanceEntry;
  cookie?: string[];
  requestHeaders?: ParamsItem[];
  errorMessage?: string;
  contentEncoding?: string;
}

/**
 * 响应处理composable
 * <p>
 * 处理HTTP响应的解析、显示和断言执行
 * </p>
 */
export function useResponseHandler () {
  // 响应状态
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

  // 错误状态
  const responseError = reactive<{
    show: boolean;
    value: string | undefined;
  }>({
    show: false,
    value: undefined
  });

  // 断言结果
  const assertResult = ref<AssertResult[]>();
  const assertionVariableExtra = ref<any>({});

  // 本地请求信息（用于断言执行）
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

  // 计算属性
  const isResponseEmpty = computed(() => {
    return !responseState.config || !Object.keys(responseState.config).length;
  });

  /**
   * 处理HTTP响应（本地请求，无代理）
   * <p>
   * 处理响应数据，更新响应状态，并处理成功和错误响应。如果配置了断言，也会执行断言。
   * </p>
   * @param resp - 来自axios的HTTP响应对象
   * @param request - 包含查询、headers、路径和body数据的请求上下文
   */
  const handleHttpResponse = async (resp: any, request: any) => {
    responseError.show = false;
    responseError.value = undefined;
    responseState.performance = resp.performance;
    assertResult.value = undefined;

    // 处理网络错误（状态0）
    if (resp.request.status === 0) {
      responseError.show = true;
      responseError.value = resp.message;
      responseState.config = {
        url: decodeURIComponent(resp.config.url),
        method: request.method,
        status: 0,
        queryString: request.queryString
      };
      responseState.headers = resp.config.headers;
    }
    // 处理HTTP错误响应（4xx, 5xx）
    else if (resp.request.status < 200 || resp.request.status >= 300) {
      responseState.data = await convertBlob(resp.response.data);
      responseState.config = {
        url: decodeURIComponent(resp.config.url),
        method: request.method,
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
    // 处理成功响应（2xx）
    else {
      responseState.data = await convertBlob(resp.data);
      if (responseState.data instanceof Blob) {
        responseState.contentEncoding = 'base64';
      } else {
        responseState.contentEncoding = undefined;
      }
      responseState.config = {
        url: decodeURIComponent(resp.config.url),
        method: request.method,
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

    // 如果没有发生错误，执行断言
    if (!responseError.show) {
      // 为断言执行准备本地请求信息
      localRequestInfo.responseHeader = responseState.headers;
      localRequestInfo.responseBody = { size: responseState.size || 0, data: responseState.data };
      localRequestInfo.status = responseState.status || 0;
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

      // 使用准备的请求上下文执行断言
      assertResult.value = await assertUtils.assert.execute(localRequestInfo, request.assertions, request.variableValues);
    }
  };

  /**
   * 处理WebSocket代理响应
   * <p>
   * 处理通过WebSocket代理接收的响应，解析JSON数据，并相应地更新响应状态。
   * </p>
   */
  const onResponse = async (response: string) => {
    assertResult.value = undefined;
    let responseData: any = {};

    // 解析来自WebSocket的JSON响应
    try {
      responseData = JSON.parse(response);
    } catch {
      responseError.show = true;
      responseError.value = response;
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

    if (typeof responseData === 'object') {
      // 处理代理错误（状态0）
      if (+responseData.response.status === 0) {
        responseError.show = true;
        assertionVariableExtra.value = {};
        responseError.value = responseData.response?.rawContent || '';
        responseState.headers = {};
        responseState.config = {};
        responseState.data = null;
        responseState.status = 0;
        responseState.size = 0;
        responseState.cookie = [];
        responseState.requestHeaders = responseData.response.headers || [];
        responseState.performance = {} as PerformanceEntry;
      }
      // 处理成功的代理响应
      else {
        responseState.config = {
          ...responseData.request0,
          url: responseData.request0.url + (responseData.request0.queryString ? ('?' + responseData.request0.queryString) : '')
        };

        // 将header数组解析为对象格式
        // Headers存储为[key1, value1, key2, value2, ...]数组
        const header = {};
        (responseData.response?.headerArray || []).forEach((value, idx, arr) => {
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

        responseState.requestHeaders = responseData.request0?.headers || [];
        responseState.headers = header;
        responseState.data = responseData.response.rawContent;
        responseState.contentEncoding = responseData.response?.contentEncoding;

        // 如果需要，将base64数据转换为blob
        if (responseData.response.contentEncoding === 'base64') {
          const mime = header[HTTP_HEADERS.CONTENT_TYPE] || header['content-type'] || header['content-Type'] || header['CONTENT-TYPE'];
          responseState.data = dataURLtoBlob(responseState.data, mime);
        }

        responseState.status = responseData.response.status;
        responseState.size = responseData.response.size;
        responseState.performance = {
          ...responseData.response.timeline,
          duration: +responseData.response.timeline.total || 0
        };

        const cookieStr = header?.[HTTP_HEADERS.SET_COOKIE] || header?.[HTTP_HEADERS.SET_COOKIE_LOWER];
        responseState.cookie = cookieStr ? [cookieStr] : [];
        responseError.show = false;

        assertResult.value = await setAssertResult(responseData);
      }
    }
  };

  /**
   * 根据类型获取断言值
   * <p>
   * 根据断言类型和条件从响应数据中提取适当的值。处理不同的断言类型，如body、header、status等。
   * </p>
   * @param assertionCondition - 断言条件类型
   * @param type - 断言类型（body、header、status等）
   * @param data - 包含各种指标的响应数据
   * @param parameterName - header断言的参数名
   * @returns 包含提取数据、消息和错误消息的对象
   */
  const getValueByType = (assertionCondition: AssertionCondition, type: AssertionType, data: {
    bodySize: number;
    size: number;
    duration: number;
    status: number;
    responseHeader: string[];
    rawContent: string;
    extractValue: string; // 用于regex、xpath、jsonpath匹配条件的左值
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
   * 获取header参数
   * <p>
   * 从header数组中提取指定名称的参数值
   * </p>
   */
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
   * 从代理响应设置断言结果
   * <p>
   * 处理来自WebSocket代理响应的断言结果，评估条件，并准备用于显示的最终断言结果数据。
   * </p>
   * @param responseData - 来自WebSocket代理的包含断言的响应数据
   * @returns 处理后的断言结果数组
   */
  const setAssertResult = async (responseData: any) => {
    const assertions: AssertResult[] = responseData.assertions;
    if (!assertions?.length) {
      return;
    }

    const result: AssertResult[] = [];
    // 处理每个断言结果
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
        value: '', // 提取的变量值
        ignored: false, // 是否忽略此断言
        message: '条件消息为空'
      };

      let ignored = true;
      let failure = false;
      if (_assertResult) {
        ignored = _assertResult.ignored || false;
        failure = _assertResult.failure;

        if (!condition) {
          _condition = {
            failure: false, // 执行结果
            name: '', // 提取的变量名
            conditionMessage: '', // 断言表达式错误的原因
            failureMessage: '', // 提取失败的原因
            value: '', // 提取的变量值
            ignored: false, // 是否忽略此断言
            message: '条件消息为空'
          };
        } else {
          const matchsMap = assertionVariableExtra.value?.matchs || {};
          const varMap = assertionVariableExtra.value?.vars || {};
          const matchs = matchsMap[condition];
          if (!matchs) {
            _condition = {
              failure: false, // 执行结果
              name: '', // 提取的变量名
              conditionMessage: '条件消息格式错误', // 断言表达式错误的原因
              failureMessage: '条件消息格式失败', // 提取失败的原因
              value: '', // 提取的变量值
              ignored: true, // 是否忽略此断言
              message: '条件消息格式错误'
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
              failureMessage = '条件消息定义失败';
              value = leftOperand;
            }

            if (ignored) {
              _condition = {
                failure: true, // 执行结果
                name: leftOperand, // 提取的变量名
                conditionMessage: '', // 断言表达式错误的原因
                failureMessage, // 提取失败的原因
                value, // 提取的变量值
                ignored: true, // 是否忽略此断言
                message: '条件消息忽略'
              };
            } else {
              _condition = {
                failure: false, // 执行结果
                name: leftOperand, // 提取的变量名
                conditionMessage: '', // 断言表达式错误的原因
                failureMessage, // 提取失败的原因
                value, // 提取的变量值
                ignored: false, // 是否忽略此断言
                message: '条件消息执行'
              };
            }
          }
        }
      }

      // 期望值处理
      const expectedData: { data: string | null; message: string; errorMessage: string; } = {
        data: (expected ?? null),
        message: '',
        errorMessage: ''
      };
      if (extraction) {
        expectedData.data = (extraction as Extraction).finalValue ?? null;
        if (expectedData.data !== null && typeof expectedData.data === 'object') {
          expectedData.data = JSON.stringify(expectedData.data);
        } else if (expectedData.data !== null && typeof expectedData.data !== 'string') {
          expectedData.data = String(expectedData.data);
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

      // Normalize type to enum value (supports enum or { message, value } shape)
      const typeValue = (typeof type === 'object' && type && 'value' in type)
        ? (type as { message: string; value: AssertionType }).value
        : (type as AssertionType);

      const realValueData = getValueByType(assertionCondition, typeValue, data, parameterName);
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

      const extractionData = extraction
        ? (extraction as any)
        : { method: undefined, expression: undefined, location: undefined, matchItem: undefined };

      const _result: AssertResult = {
        name,
        parameterName,
        condition,
        assertionCondition,
        type: typeValue,
        extraction: extractionData,
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
   * 重置响应状态
   * <p>
   * 清空所有响应相关的状态
   * </p>
   */
  const resetResponseState = () => {
    Object.assign(responseState, {
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

    responseError.show = false;
    responseError.value = undefined;
    assertResult.value = undefined;
  };

  return {
    // 状态
    responseState,
    responseError,
    assertResult,
    assertionVariableExtra,
    localRequestInfo,

    // 计算属性
    isResponseEmpty,

    // 方法
    handleHttpResponse,
    onResponse,
    resetResponseState,
    getValueByType,
    getHeaderParams,
    setAssertResult
  };
}
