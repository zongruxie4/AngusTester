import { ref } from 'vue';
import type { ResponseType, Method } from 'axios';
import { HttpMethod, ParameterIn, axiosClient } from '@xcan-angus/infra';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import qs from 'qs';
import { encode } from '@/utils/secure';
import { CONTENT_TYPE_KEYS, HTTP_HEADERS } from '@/utils/constant';
import apiUtils, { API_EXTENSION_KEY } from '@/utils/apis';
import { ApisProtocol } from '@/enums/enums';
import { SchemaType, SchemaFormat } from '@/types/openapi-types';

import { getServerData, ServerInfo } from '@/views/apis/server/types';
import { RequestBodyParam } from '@/views/apis/services/protocol/http/requestBody/types';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { AuthenticationItem } from '@/views/apis/services/protocol/http/Authorization';
import { RequestSetting } from '@/views/apis/services/protocol/http/types';
import { validateBodyForm, validateQueryParameter } from '@/views/apis/services/protocol/http/utils';

/**
 * 请求处理composable
 * <p>
 * 处理HTTP请求的发送、参数准备、验证等逻辑
 * </p>
 */
export function useRequestHandler () {
  const { t } = useI18n();
  const { valueKey, enabledKey, fileNameKey, idKey, serverSourceKey, requestSettingKey } = API_EXTENSION_KEY;

  // HTTP客户端
  // eslint-disable-next-line new-cap
  const httpClient = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });

  // 请求状态
  const loading = ref(false);
  const controller = ref<AbortController>();

  /**
   * 构建真实URI，包含路径和查询参数
   * <p>
   * 通过替换endpoint中的路径参数并追加查询参数来构建最终URI。
   * 处理对象和数组参数值。
   * </p>
   * @param pathParams - 要替换URI中路径参数的参数
   * @param queryParams - 要追加到URI的查询参数
   * @param apiUri - API基础URI
   * @returns 构建的URI字符串
   */
  const getRealUri = (pathParams: Record<string, any>, queryParams: Record<string, any>, apiUri: string) => {
    let processedUri = apiUri || '';
    const paths = Object.keys(pathParams || {}).map(key => ({ name: key, [valueKey]: pathParams?.[key] }));

    // 处理路径参数（将对象/数组转换为字符串）
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
      // 用参数值替换路径占位符
      const endpoint = paths?.reduce((prevValue, currentValue) => {
        pattern = new RegExp('{' + currentValue.name + '}', 'gi');
        if (pattern.test(prevValue)) {
          prevValue = prevValue.replace(pattern, currentValue[valueKey] || '');
        }
        return prevValue;
      }, processedUri) || '';
      // 通过删除前导斜杠并添加单个前导斜杠来规范化路径
      processedUri = '/' + endpoint.replace(/^\/+/, '');
    }

    // 追加查询参数
    const queryUri = qs.stringify(queryParams, { allowDots: true, encode: true });
    if (processedUri.includes('?')) {
      processedUri = processedUri + '&' + queryUri;
    } else if (queryUri) {
      processedUri = processedUri + '?' + queryUri;
    }
    return processedUri;
  };

  /**
   * 获取参数，验证通过后执行此方法
   * <p>
   * 准备请求参数，包括headers、body、认证等
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
      const exceedFileSize = requestBodyRef.value.ifExceedSize();
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
    requestBodyRef: any,
    authorizationRef: any,
    assertFormRef: any,
    isUnarchivedApi: boolean,
    ws: WebSocket | undefined,
    allFuncNames: string[],
    onHttpResponse: (resp: any, request: any) => Promise<void>,
    onWebSocketResponse: (params: any) => void
  ) => {
    // 处理请求取消（如果已在进行中）
    if (loading.value) {
      handleAbort();
    }
    loading.value = true;

    // 验证URL配置
    if (!currentServer.url && !apiUri) {
      loading.value = false;
      notification.error({
        message: t('service.apis.errors.invalidUrl'),
        description: t('service.apis.errors.invalidUrlDescription')
      });
      return;
    }

    // 准备请求体数据
    let requestBodyContents = await requestBodyRef.value.getBodyData(state.requestBody, contentType);
    let formData: any[] = [];
    const strParam: string[] = [];

    if (contentType === CONTENT_TYPE_KEYS.FORM_URLENCODED || contentType === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
      formData = requestBodyContents;
    } else if (typeof requestBodyContents === 'string') {
      strParam.push(requestBodyContents);
    }

    // 准备认证数据
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
    // 准备参数数据
    const querPathData = state.parameters.filter(i => i.name && i[enabledKey]);

    // 准备断言数据
    let assertions: any[] = [];
    let asserVariableStr: string[] = [];
    if (typeof assertFormRef.value?.getData === 'function') {
      const assertFormData = assertFormRef.value.getData();
      assertions = assertFormData.data;
      asserVariableStr = assertFormData.variables;
    } else {
      assertions = state.assertions;
    }

    // 替换参数中的模拟函数和变量
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

    // 验证断言数据
    const validateAssertData = !assertFormRef.value || assertFormRef.value?.validate();

    // 验证参数数据并在启用时格式化
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

    // 组装请求体数据
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

    // 准备JSON格式的路径参数
    const pathJson = {};
    const pathArr = funcValues[0].filter(i => i.in === ParameterIn.path);
    pathArr.forEach(i => {
      pathJson[i.name] = i[valueKey];
    });

    // 组装查询数据（查询参数 + 认证查询）
    const queryJson = {};
    const queryArr = funcValues[0].filter(i => i.in === ParameterIn.query).concat(funcValues[4]);
    queryArr.forEach(i => {
      queryJson[i.name] = i[valueKey];
    });

    const apiPathQuery = getRealUri(pathJson, queryJson, apiUri);
    const serverUrl = getServerData(currentServer);

    // 解析请求显示的查询参数
    const requestQueryJson = qs.parse(apiPathQuery.split('?')[1] || '');

    // 组装最终请求URL
    let apiHref = '';
    if (!serverUrl.endsWith('/') && apiPathQuery && apiPathQuery.split('?')[0] && !apiPathQuery.startsWith('/')) {
      apiHref = serverUrl + '/' + apiPathQuery;
    } else {
      apiHref = serverUrl + apiPathQuery;
    }
    if (!apiHref.startsWith(ApisProtocol.http)) {
      apiHref = 'http://' + apiHref;
    }

    // 组装请求头
    let headers: Record<string, string> = {};
    const headList = funcValues[2].filter(i => i.in === ParameterIn.header);
    const cookieList = funcValues[2].filter(i => i.in === ParameterIn.cookie);

    headList.forEach(item => {
      if (typeof item[valueKey] === 'object') {
        // 对中文内容使用encodeURIComponent以确保可以发送本地请求
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
    const isExceedRequestSize = requestBodyRef.value.ifExceedRequestSize();

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
    } else if (ws.readyState !== 1) {
      // 处理WebSocket连接错误
      loading.value = false;
      notification.error(t('service.apis.errors.proxyNotConnected'));
    } else if (ws) {
      // 通过WebSocket代理发送请求
      const params = await getParameter(
        true, state, setting, currentServer, apiUri, apiMethod, contentType,
        saveParams, requestBodyRef, authorizationRef, assertFormRef, isUnarchivedApi
      );
      onWebSocketResponse(params);
    }
  };

  /**
   * 中止当前请求
   * <p>
   * 通过中止HTTP控制器或清除WebSocket请求ID来取消正在进行的请求
   * </p>
   */
  const handleAbort = () => {
    loading.value = false;
    if (controller.value) {
      controller.value.abort();
    }
  };

  return {
    loading,
    sendRequest,
    handleAbort,
    getParameter,
    getRealUri
  };
}
