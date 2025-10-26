import { ref } from 'vue';
import type { ResponseType, Method } from 'axios';
import { HttpMethod, ParameterIn, axiosClient, utils } from '@xcan-angus/infra';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import qs from 'qs';
import { encode } from '@/utils/secure';
import { CONTENT_TYPE_KEYS, HTTP_HEADERS } from '@/utils/constant';
import apiUtils, { API_EXTENSION_KEY } from '@/utils/apis';
import { ApisProtocol } from '@/enums/enums';
import { SchemaType, SchemaFormat } from '@/types/openapi-types';
import  { services } from '@/api/tester';
import assertUtils from '@/utils/assertutils';


import { getServerData, ServerInfo } from '@/views/apis/server/types';
import { RequestBodyParam, rawTypeOptions } from '@/views/apis/services/protocol/http/requestBody/types';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { AuthenticationItem } from '@/views/apis/services/protocol/http/Authorization';
import { RequestSetting } from '@/views/apis/services/protocol/http/types';
import { validateBodyForm, validateQueryParameter } from '@/views/apis/services/protocol/http/utils';

/**
 * Request handling composable
 * <p>
 * Handles sending HTTP requests, parameter preparation, and validation.
 * </p>
 */
export function useRequestHandler () {
  const { t } = useI18n();
  const uuid = ref('');
  const { valueKey, enabledKey, fileNameKey, idKey, serverSourceKey, requestSettingKey } = API_EXTENSION_KEY;

  // HTTP client
  // eslint-disable-next-line new-cap
  const httpClient = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });

  // Request state
  const loading = ref(false);
  const controller = ref<AbortController>();

  /**
   * Build real URI with path and query params
   * <p>
   * Replace path placeholders and append query string. Handles object/array values.
   * </p>
   * @param pathParams - Params to replace in URI path
   * @param queryParams - Params to append as query string
   * @param apiUri - Base API URI
   * @returns Built URI string
   */
  const getRealUri = (pathParams: Record<string, any>, queryParams: Record<string, any>, apiUri: string) => {
    let processedUri = apiUri || '';
    const paths = Object.keys(pathParams || {}).map(key => ({ name: key, [valueKey]: pathParams?.[key] }));

    // Handle path params (stringify objects/arrays)
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
      // Normalize path by ensuring a single leading slash
      processedUri = '/' + endpoint.replace(/^\/+/, '');
    }

    // Append query params
    const queryUri = qs.stringify(queryParams, { allowDots: true, encode: true });
    if (processedUri.includes('?')) {
      processedUri = processedUri + '&' + queryUri;
    } else if (queryUri) {
      processedUri = processedUri + '?' + queryUri;
    }
    return processedUri;
  };

  /**
   * Get parameters (after validation)
   * <p>
   * Prepare request parameters including headers, body, and authentication
   * </p>
   */
  const getParameter = async (
    isDebug = false,
    state: {
      parameters: ParamsItem[];
      requestBody: RequestBodyParam;
      headerList: ParamsItem[];
      cookieList: ParamsItem[];
      authentication: AuthenticationItem;
      secured: boolean;
    },
    setting: RequestSetting,
    currentServer: ServerInfo,
    apiUri: string,
    apiMethod: HttpMethod,
    contentType: string | null,
    saveParams: any,
    requestBodyRef: any,
    _authorizationRef: any,
    assertFormRef: any,
    _isUnarchivedApi: boolean
  ) => {
    const { parameters, requestBody, headerList, cookieList, authentication, secured } = state;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { content } = requestBody;

    if (!isDebug) {
      const exceedFileSize = requestBodyRef?.value?.ifExceedSize?.() ?? false;
      if (exceedFileSize) {
        Object.keys(content).forEach(key => {
          if (content[key]?.schema?.format === SchemaFormat.binary) {
            delete requestBody.content[key].schema[valueKey];
            delete requestBody.content[key].schema[fileNameKey];
          }
          if (key === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
            if (content[key]?.schema?.properties) {
              Object.keys(content[key]?.schema?.properties).forEach(name => {
                if (content[key].schema.properties[name].format === SchemaFormat.binary) {
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

    if (contentType) {
      headers.unshift({
        name: HTTP_HEADERS.CONTENT_TYPE,
        in: ParameterIn.header,
        [valueKey]: contentType,
        [enabledKey]: true
      });
    }

    let protocol = '';
    if (currentServer?.url) {
      try {
        const _server = new URL(currentServer.url);
        protocol = _server.protocol.replace(':', '');
      } catch {
        protocol = ApisProtocol.http;
        if (!currentServer.url.startsWith(ApisProtocol.http) && !currentServer[idKey]) {
          currentServer.url = 'http://' + currentServer.url;
        }
      }
    } else {
      protocol = ApisProtocol.http;
    }

    if (currentServer[idKey]) {
      const availableServers = []; // 这里需要从外部传入
      const defaultServer = availableServers.find(item => item[idKey] === currentServer[idKey]);
      if (defaultServer) {
        if ((defaultServer as ServerInfo).url !== currentServer.url) {
          delete currentServer[idKey];
        }
      }
    }

    let assertions: any[] = [];
    if (typeof assertFormRef.value?.getData === 'function') {
      assertions = assertFormRef.value.getData().data;
    }

    const currentServerICopy = JSON.parse(JSON.stringify(currentServer));
    delete currentServerICopy[serverSourceKey];

    return {
      ...saveParams,
      assertions,
      parameters: [...parameters, ...headers, ...cookieList.filter(i => i.name)],
      requestBody: requestBody,
      endpoint: apiUri,
      currentServer: currentServerICopy,
      method: apiMethod,
      authentication: secured ? authentication : { type: null },
      secured,
      protocol,
      [requestSettingKey]: {
        ...setting,
        connectTimeout: setting.connectTimeout + 'ms',
        readTimeout: setting.readTimeout + 'ms'
      }
    };
  };

  /**
   * 发送HTTP请求
   * <p>
   * 主要的请求处理器，验证参数，准备请求数据，并通过直接HTTP客户端或WebSocket代理发送请求。
   * 还处理参数验证和断言执行。
   * </p>
   */
  const sendRequest = async (
    state: any,
    setting: RequestSetting,
    currentServer: ServerInfo,
    apiUri: string,
    apiMethod: HttpMethod,
    contentType: string | null,
    saveParams: any,
    requestParamsRef: any,
    requestHeaderRef: any,
    requestBodyRef: any,
    authorizationRef: any,
    assertFormRef: any,
    isUnarchivedApi: boolean,
    ws: WebSocket | undefined,
    allFuncNames: string[],
    assertionVariableExtra: any,
    onHttpResponse: (resp: any, request: any) => Promise<void>,
    openToolBar: () => void
  ) => {
    // 处理请求取消（如果已在进行中）
    if (loading.value) {
      handleAbort();
    }
    loading.value = true;
    assertionVariableExtra.value = {};

    // Validate URL configuration
    if (!currentServer.url && !apiUri) {
      loading.value = false;
      notification.error({
        message: t('service.apis.errors.invalidUrl'),
        description: t('service.apis.errors.invalidUrlDescription')
      });
      return;
    }

    // Prepare request body data
    let requestBodyContents = await (requestBodyRef?.value?.getBodyData?.(state.requestBody, contentType) ?? '');
    let formData: any[] = [];
    const strParam: string[] = [];

    if (contentType === CONTENT_TYPE_KEYS.FORM_URLENCODED || contentType === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
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
    }).filter(Boolean); // Header认证数据

    const authQueryData = Object.keys(auth?.[1] || {}).map(key => ({ name: key, [valueKey]: auth?.[1]?.[key] })); // Query认证数据

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
      }, allFuncNames,
      !isUnarchivedApi ? saveParams.id : undefined,
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
    const validateAssertData = !assertFormRef.value || assertFormRef.value?.validate();

    // Validate parameter data and format when enabled
    if (setting.enableParamValidation) {
      const validatedQuery = validateQueryParameter(funcValues[0]);
      if (!validatedQuery) {
        notification.error(t('service.apis.errors.parameterValidationFailed'));
      }
      const validatedHeader = validateQueryParameter(funcValues[2].filter(i => i.in === ParameterIn.header));
      if (!validatedHeader) {
        notification.error(t('service.apis.errors.headerValidationFailed'));
      }
      const validatedCookie = validateQueryParameter(funcValues[2].filter(i => i.in === ParameterIn.cookie));
      if (!validatedCookie) {
        notification.error(t('service.apis.errors.cookieValidationFailed'));
      }
      const validatedFom = validateBodyForm(funcValues[1].filter(item => item.format !== SchemaFormat.binary && item.name));
      if (!validatedFom) {
        notification.error(t('service.apis.errors.bodyValidationFailed'));
      }
      if (!validateAssertData) {
        notification.error(t('service.apis.errors.assertionValidationFailed'));
      }
      if (!validatedQuery || !validatedFom || !validatedCookie || !validatedHeader || !validateAssertData) {
        loading.value = false;
        return;
      }
    } else {
      if (!validateAssertData) {
        loading.value = false;
        notification.error(t('service.apis.errors.assertionValidationFailed'));
        return;
      }
    }

    // Assemble request body for display
    const showRequestBody: Record<string, any> = {};
    let requestBody;

    if ((contentType === CONTENT_TYPE_KEYS.FORM_URLENCODED ||
      contentType === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) && funcValues[1]?.length) {
      const bodyDataArr = funcValues[1];
      if (contentType === CONTENT_TYPE_KEYS.FORM_URLENCODED) {
        requestBody = '';
        const content = {};
        bodyDataArr.forEach(item => {
          content[item.name] = item[valueKey];
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
      if (contentType === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
        showRequestBody.formData = {};
        requestBody = new FormData();
        bodyDataArr.forEach(item => {
          if (!item.name) {
            return;
          }
          if (item.format !== SchemaFormat.binary) {
            requestBody.append(item.name, item[valueKey]);
            showRequestBody.formData[item.name] = typeof item[valueKey] === 'object' ? JSON.stringify(item[valueKey]) : item[valueKey];
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

    // Prepare JSON for path params
    const pathJson = {};
    const pathArr = funcValues[0].filter(i => i.in === ParameterIn.path);
    pathArr.forEach(i => {
      pathJson[i.name] = i[valueKey];
    });

    // Build query data (query params + auth query)
    const queryJson = {};
    const queryArr = funcValues[0].filter(i => i.in === ParameterIn.query).concat(funcValues[4]);
    queryArr.forEach(i => {
      queryJson[i.name] = i[valueKey];
    });

    const apiPathQuery = getRealUri(pathJson, queryJson, apiUri);
    const serverUrl = getServerData(currentServer);

    // Parse query params for display
    const requestQueryJson = qs.parse(apiPathQuery.split('?')[1] || '');
    uuid.value = utils.uuid('api-request');
    

    // Build final request URL
    let apiHref = '';
    if (!serverUrl.endsWith('/') && apiPathQuery && apiPathQuery.split('?')[0] && !apiPathQuery.startsWith('/')) {
      apiHref = serverUrl + '/' + apiPathQuery;
    } else {
      apiHref = serverUrl + apiPathQuery;
    }
    if (!apiHref.startsWith(ApisProtocol.http)) {
      apiHref = 'http://' + apiHref;
    }

    // Build request headers
    let headers: Record<string, string> = {};
    const headList = funcValues[2].filter(i => i.in === ParameterIn.header);
    const cookieList = funcValues[2].filter(i => i.in === ParameterIn.cookie);

    headList.forEach(item => {
      if (typeof item[valueKey] === 'object') {
        // Use encodeURIComponent for non-ASCII to ensure local request sending
        if (Object.prototype.toString.call(item[valueKey]) === '[object Array]') {
          headers[item.name] = item[valueKey]
            ? item[valueKey].map(value => value && apiUtils.containsAllAscii(value) ? value : value ? encodeURIComponent(value) : '').join(',')
            : '';
        } else {
          headers[item.name] = item[valueKey]
            ? Object.entries(item[valueKey])
              .map(([key, value]) => `${key}=${value && apiUtils.containsAllAscii(String(value)) ? String(value) : value ? encodeURIComponent(String(value)) : ''}`).join(',')
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

    if (contentType) {
      headers[HTTP_HEADERS.CONTENT_TYPE] = contentType;
    }

    // 请求配置
    const { connectTimeout, readTimeout, retryNum, maxRedirects } = setting;
    const isExceedRequestSize = requestBodyRef?.value?.ifExceedRequestSize?.() ?? false;

    // 通过直接HTTP客户端或WebSocket代理发送请求
    if (!ws || isExceedRequestSize) {
      controller.value = new AbortController();
      const signal = controller.value.signal;
      const axiosConfig = {
        responseType: 'blob' as ResponseType,
        url: apiHref,
        method: apiMethod.toLowerCase() as Method,
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
      await onHttpResponse(responses, {
        query: requestQueryJson,
        header: headers,
        path: pathJson,
        requestBody: showRequestBody,
        queryString: apiPathQuery.split('?')[1] || '',
        assertions,
        variableValues
      });
      loading.value = false;
      openToolBar();
    } else if (ws.readyState !== 1) {
      // Handle WebSocket connection error
      loading.value = false;
      notification.error(t('service.apis.errors.proxyNotConnected'));
    } else if (ws) {
      // Send request via WebSocket proxy
      const params = await getParameter(
        true, state, setting, currentServer, apiUri, apiMethod, contentType,
        saveParams, requestBodyRef, authorizationRef, assertFormRef, isUnarchivedApi
      );

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
        params.assertions = params.assertions.filter((item) => item.enabledFlag);
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
      if (!isUnarchivedApi) {
        const conditions = params.assertions.filter(item => item.condition).map(item => item.condition);
        const { extra } = assertUtils.proxy.execute(conditions, variableValues);
        assertionVariableExtra.value = extra;
      }
      const jsonStr = JSON.stringify({
        ...params,
        requestBody,
        server: currentServer,
        requestId: uuid.value,
        messageType: 'HttpRequestProxy',
        endpoint: params.endpoint,
        resolvedRefModels,
        variables: [...variableValues]
      });
      ws.send(jsonStr);
      openToolBar();
    }
  };

  /**
   * Abort current request
   * <p>
   * Cancel ongoing request by aborting HTTP controller or clearing WebSocket request id
   * </p>
   */
  const handleAbort = () => {
    loading.value = false;
    if (controller.value) {
      controller.value.abort();
    }
  };

  return {
    requestUuid: uuid,
    loading,
    sendRequest,
    handleAbort,
    getParameter,
    getRealUri
  };
}
