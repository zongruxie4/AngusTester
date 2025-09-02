import { ref, inject, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification, ApiUtils as apiUtils } from '@xcan-angus/vue-ui';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { convertBlob } from '@/views/apis/services/apiHttp/utils';
import { EnumMessage, HttpStatus, utils, enumUtils, axiosClient } from '@xcan-angus/infra';
import { dataURLtoBlob, getFileSuffixByContentType } from '@/utils/blob';

/**
 * Composable for API debugging functionality
 * Handles request sending, response processing, and WebSocket communication
 */
export function useApiDebug() {
  const { t } = useI18n();
  const { valueKey } = API_EXTENSION_KEY;

  // WebSocket injection
  const WS = inject('WS', ref());
  const uuid = inject('uuid', ref());
  const wsResponse = inject('wsResponse', ref());

  // HTTP client
  const myRequest = new axiosClient({ 
    timeout: 0, 
    intervalMs: 500, 
    maxRedirects: 0, 
    maxRetries: 5 
  });

  // State management
  const debugging = ref(false);
  const responseContent = ref();
  const openHeader = ref(true);
  const openBody = ref(true);
  let controller: AbortController;
  let apiUuid: string;

  // Status enum
  let statusOpt: EnumMessage<HttpStatus>[] = [];

  /**
   * Load HTTP status enum
   */
  const loadStatusEnum = () => {
    if (statusOpt.length) {
      return;
    }
    statusOpt = enumUtils.enumToMessages(HttpStatus);
  };

  /**
   * Stop debugging process
   */
  const stopDebug = () => {
    debugging.value = false;
    if (WS.value && WS.value.readyState === 1) {
      apiUuid = '';
    } else {
      controller?.abort();
    }
  };

  /**
   * Process form data for request
   * @param parameters - Raw parameters from form
   * @returns Processed parameter data
   */
  const processFormData = (parameters: any[]) => {
    const queryData = parameters.filter(i => i.in === 'query')
      .map(i => ({ ...i, in: 'query', [valueKey]: i.value }));
    const headerData = parameters.filter(i => i.in === 'header')
      .map(i => ({ ...i, in: 'header', [valueKey]: i.value }));
    const cookieData = parameters.filter(i => i.in === 'cookie')
      .map(i => ({ ...i, in: 'cookie', [valueKey]: i.value }));

    return { queryData, headerData, cookieData };
  };

  /**
   * Build request URL with query parameters
   * @param server - Server URL
   * @param endpoint - API endpoint
   * @param queryData - Query parameters
   * @returns Complete URL
   */
  const buildRequestUrl = (server: string, endpoint: string, queryData: any[]) => {
    let apiHref = server + endpoint;
    if (queryData.length) {
      const hrefObj = new URL(apiHref);
      queryData.forEach(item => {
        hrefObj.searchParams.append(item.name, item[valueKey]);
      });
      apiHref = hrefObj.toString();
    }
    return apiHref;
  };

  /**
   * Process request body data
   * @param bodyObj - Body object from form
   * @param headerData - Header data array
   * @returns Processed body content and OpenAPI format
   */
  const processRequestBody = (bodyObj: any, headerData: any[]) => {
    let bodyContent;
    let bodyOpenApi = {};

    if (bodyObj) {
      const { contentType, forms, rawContent } = bodyObj;
      if (contentType) {
        headerData.push({ in: 'header', name: 'Content-Type', [valueKey]: contentType });
        
        if (contentType === 'application/x-www-form-urlencode') {
          const formUrlEncodeParam = (forms || []).map(i => ({ ...i, [valueKey]: i.value }));
          const formJson = {};
          const formUrl = formUrlEncodeParam.map(item => {
            formJson[item.name] = item[valueKey];
            return `${item.name}=${item[valueKey]}`;
          }).join('&');
          bodyContent = formUrl;
          bodyOpenApi = {
            content: {
              'application/x-www-form-urlencode': {
                [valueKey]: formJson
              }
            }
          };
        } else if (contentType === 'multipart/form-data') {
          const formUrlEncodeParam = (forms || []).map(i => ({ ...i, [valueKey]: i.value }));
          const formData = new FormData();
          const formJson = {};
          formUrlEncodeParam.forEach(item => {
            if (item.name) {
              formJson[item.name] = item[valueKey];
              formData.append(item.name, item[valueKey]);
            }
          });
          bodyContent = formData;
          bodyOpenApi = {
            content: {
              'multipart/form-data': {
                [valueKey]: formJson
              }
            }
          };
        } else {
          bodyContent = rawContent;
          bodyOpenApi = {
            content: {
              [contentType]: {
                [valueKey]: rawContent
              }
            }
          };
        }
      }
    }

    return { bodyContent, bodyOpenApi };
  };

  /**
   * Send request via WebSocket
   * @param requestData - Request data object
   */
  const sendWebSocketRequest = (requestData: any) => {
    apiUuid = utils.uuid('api-item');
    const api = JSON.stringify({
      ...requestData,
      requestId: apiUuid,
      messageType: 'HttpRequestProxy'
    });
    WS.value.send(api);
  };

  /**
   * Send request via HTTP
   * @param config - Axios configuration
   * @returns Promise with response
   */
  const sendHttpRequest = async (config: any) => {
    controller = new AbortController();
    const signal = controller.signal;
    const axiosConfig = {
      ...config,
      signal,
      timeout: 660000,
      maxRedirects: 1,
      maxRetries: 1
    };
    return await myRequest.request(axiosConfig);
  };

  /**
   * Process HTTP response
   * @param resp - Response object
   */
  const onHttpResponse = async (resp: any) => {
    const status = resp.request.status;
    debugging.value = false;
    
    if (status === 0) {
      const responseHeader = [];
      const responseBody = resp.message;
      responseContent.value = { status, responseHeader, responseBody };
    } else if (status < 200 || status >= 400) {
      const responseHeader = Object.keys(resp.response.headers || {}).map(key => {
        return { key, value: resp.response.headers[key] };
      });
      const responseBody = await convertBlob(resp.response.data);
      responseContent.value = { status, responseHeader, responseBody };
    } else {
      const responseBody = await convertBlob(resp.data);
      const responseHeader = Object.keys(resp.headers || {}).map(key => {
        return { key, value: resp.headers[key] };
      });
      responseContent.value = { status, responseHeader, responseBody };
      if (Object.prototype.toString.call(responseBody) === '[object Blob]') {
        downloadResponseBody();
      }
    }
  };

  /**
   * Process WebSocket response
   */
  const onWsResponse = () => {
    debugging.value = false;
    try {
      const respJson = JSON.parse(wsResponse.value);
      const status = +respJson.response.status;
      
      if (status === 0) {
        const responseHeader = [];
        const responseBody = respJson.response?.rawContent;
        responseContent.value = { status, responseHeader, responseBody };
      } else {
        const header: Array<{key: string, value: string}> = [];
        (respJson.response?.headerArray || []).forEach((value: any, idx: number, arr: any[]) => {
          if (idx % 2 === 0) {
            header.push({ key: value, value: arr[idx + 1] });
          }
        });
        const responseHeader = header;
        let responseBody = respJson.response?.rawContent;
        
        if (respJson.response?.contentEncoding === 'base64') {
          responseBody = dataURLtoBlob(responseBody) || responseBody;
        }
        
        responseContent.value = { status, responseHeader, responseBody };
        if (Object.prototype.toString.call(responseBody) === '[object Blob]') {
          downloadResponseBody();
        }
      }
    } catch (error) {
      // Handle parsing error silently
    }
  };

  /**
   * Get download filename from response headers
   * @returns Filename for download
   */
  const getDownloadFilename = () => {
    const disposition = responseContent.value.responseHeader.find((item: any) => item.key === 'content-Disposition');
    let filename = '';
    
    if (disposition) {
      filename = disposition?.split(';')[1]?.split('=')[1];
    }
    
    if (!filename) {
      const paths = responseContent.value.endpoint?.split('/');
      filename = paths?.length ? paths[paths.length - 1] : 'response';
    }
    
    if (!filename.includes('.')) {
      const respContentType = (responseContent.value?.responseHeader || []).find((item: any) => item.key === 'Content-Type')?.value;
      if (respContentType) {
        const suffix = getFileSuffixByContentType(respContentType);
        suffix && (filename = filename + `.${suffix}`);
      }
    }
    
    return filename;
  };

  /**
   * Download response body as file
   */
  const downloadResponseBody = () => {
    let blob = responseContent.value.responseBody;
    if (!(blob instanceof Blob)) {
      if (typeof blob === 'object') {
        blob = JSON.stringify(blob, null, 2);
      }
      blob = new Blob([blob], {
        type: 'application/octet-stream'
      });
    }

    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = getDownloadFilename();
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  /**
   * Watch for WebSocket response
   */
  const watchWebSocketResponse = () => {
    watch(() => uuid.value, (newValue) => {
      if (newValue === apiUuid) {
        debugging.value = false;
        onWsResponse();
      }
    });
  };

  return {
    // State
    debugging,
    responseContent,
    openHeader,
    openBody,
    statusOpt,

    // Methods
    loadStatusEnum,
    stopDebug,
    processFormData,
    buildRequestUrl,
    processRequestBody,
    sendWebSocketRequest,
    sendHttpRequest,
    onHttpResponse,
    onWsResponse,
    downloadResponseBody,
    watchWebSocketResponse
  };
}
