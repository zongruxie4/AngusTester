import { ref, reactive, computed } from 'vue';
import { AssertionType, AssertionCondition, utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { dataURLtoBlob } from '@/utils/blob';
import { convertBlob } from '@/utils/apis';
import { HTTP_HEADERS, CONTENT_TYPE_KEYS } from '@/utils/constant';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { AssertResult, ConditionResult, Extraction, Parameter } from '@/views/apis/services/protocol/http/types';
import assertUtils from '@/utils/assertutils';

/**
 * Response state interface
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
 * Response handling composable
 * <p>
 * Handles parsing, displaying and assertion execution for HTTP responses.
 * </p>
 */
export function useResponseHandler () {
  // Initialize i18n for internationalization
  const { t } = useI18n();

  // Response state
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

  // Error state
  const responseError = reactive<{
    show: boolean;
    value: string | undefined;
  }>({
    show: false,
    value: undefined
  });

  // Assertion results
  const assertResult = ref<AssertResult[]>();
  const assertionVariableExtra = ref<any>({});

  // Local request info (for assertion execution)
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

  // Computed
  const isResponseEmpty = computed(() => {
    return !responseState.config || !Object.keys(responseState.config).length;
  });

  /**
   * Handle HTTP response (local request, no proxy)
   * <p>
   * Handle response data, update response state, and handle success/error responses.
   * Execute assertions if configured.
   * </p>
   * @param resp - HTTP response object from axios
   * @param request - Request context including query, headers, path and body
   */
  const handleHttpResponse = async (resp: any, request: any) => {
    responseError.show = false;
    responseError.value = undefined;
    responseState.performance = resp.performance;
    assertResult.value = undefined;

    // Handle network error (status 0)
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
    // Handle HTTP error response (4xx, 5xx)
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
    // Handle success response (2xx)
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

    // Execute assertions if no error occurred
    if (!responseError.show) {
      // Prepare local request info for assertions
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

      // Execute assertions with prepared request context
      assertResult.value = await assertUtils.assert.execute(localRequestInfo, request.assertions, request.variableValues);
    }
  };

  /**
   * Handle WebSocket proxy response
   * <p>
   * Process responses received via WebSocket proxy, parse JSON and update state.
   * </p>
   */
  const onResponse = async (response: string) => {
    assertResult.value = undefined;
    let responseData: any = {};

    // Parse JSON response from WebSocket
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
      // Handle proxy error (status 0)
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
      // Handle successful proxy response
      else {
        responseState.config = {
          ...responseData.request0,
          url: responseData.request0.url + (responseData.request0.queryString ? ('?' + responseData.request0.queryString) : '')
        };

        // Parse header array into object
        // Headers are stored as [key1, value1, key2, value2, ...]
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

        // Convert base64 data to blob if needed
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
   * Get assertion value by type
   * <p>
   * Extract value from response based on type and condition. Handles body, header, status, etc.
   * </p>
   * @param assertionCondition - Assertion condition type
   * @param type - Assertion type (body, header, status, ...)
   * @param data - Response metrics
   * @param parameterName - Parameter name for header assertions
   * @returns Object containing extracted data, message and errorMessage
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
   * Get header parameter
   * <p>
   * Extract the parameter value for the specified name from the header array
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
   * Set assertion results from proxy response
   * <p>
   * Process assertion results from WebSocket proxy response, evaluate conditions, and prepare final data for display.
   * </p>
   * @param responseData - Response data from WebSocket proxy including assertions
   * @returns Processed assertion results
   */
  const setAssertResult = async (responseData: any) => {
    const assertions: AssertResult[] = responseData.assertions;
    if (!assertions?.length) {
      return;
    }

    const result: AssertResult[] = [];
    // Handle each assertion result
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
        failure: false, // execution result
        name: '', // extracted variable name
        conditionMessage: '', // reason for assertion expression error
        failureMessage: '', // reason for extraction failure
        value: '', // extracted variable value
        ignored: false, // whether to ignore this assertion
        message: t('service.apis.assertion.conditionMessages.empty')
      };

      let ignored = true;
      let failure = false;
      if (_assertResult) {
        ignored = _assertResult.ignored || false;
        failure = _assertResult.failure;

        if (!condition) {
          _condition = {
            failure: false, // execution result
            name: '', // extracted variable name
            conditionMessage: '', // reason for assertion expression error
            failureMessage: '', // reason for extraction failure
            value: '', // extracted variable value
            ignored: false, // whether to ignore this assertion
            message: t('service.apis.assertion.conditionMessages.empty')
          };
        } else {
          const matchsMap = assertionVariableExtra.value?.matchs || {};
          const varMap = assertionVariableExtra.value?.vars || {};
          const matchs = matchsMap[condition];
          if (!matchs) {
            _condition = {
              failure: false, // execution result
              name: '', // extracted variable name
              conditionMessage: t('service.apis.assertion.conditionMessages.formatError'), // reason for assertion expression error
              failureMessage: t('service.apis.assertion.conditionMessages.formatFailed'), // reason for extraction failure
              value: '', // extracted variable value
              ignored: true, // whether to ignore this assertion
              message: t('service.apis.assertion.conditionMessages.formatError')
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
              failureMessage = t('service.apis.assertion.conditionMessages.defineFailed');
              value = leftOperand;
            }

            if (ignored) {
              _condition = {
                failure: true, // execution result
                name: leftOperand, // extracted variable name
                conditionMessage: '', // reason for assertion expression error
                failureMessage, // reason for extraction failure
                value, // extracted variable value
                ignored: true, // whether to ignore this assertion
                message: t('service.apis.assertion.conditionMessages.ignored')
              };
            } else {
              _condition = {
                failure: false, // execution result
                name: leftOperand, // extracted variable name
                conditionMessage: '', // reason for assertion expression error
                failureMessage, // reason for extraction failure
                value, // extracted variable value
                ignored: false, // whether to ignore this assertion
                message: t('service.apis.assertion.conditionMessages.executed')
              };
            }
          }
        }
      }

      // Expected value processing
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
   * Reset response state
   * <p>
   * Clear all response-related state
   * </p>
   */
  const resetResponseState = (): void => {
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
    // State
    responseState,
    responseError,
    assertResult,
    assertionVariableExtra,
    localRequestInfo,

    // Computed
    isResponseEmpty,

    // Methods
    handleHttpResponse,
    onResponse,
    resetResponseState,
    getValueByType,
    getHeaderParams,
    setAssertResult
  };
}
