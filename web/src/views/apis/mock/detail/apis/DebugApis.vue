<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, notification, Toggle } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { convertBlob } from '@/views/apis/services/apiHttp/utils';
import {
  axiosClient,
  ContentEncoding,
  EnumMessage,
  enumUtils,
  HttpMethod,
  HttpStatus,
  ParameterIn,
  utils
} from '@xcan-angus/infra';

import apiUtils from '@/utils/apis/index';
import { dataURLtoBlob, getFileSuffixByContentType } from '@/utils/blob';
import UrlForm from '@/views/apis/mock/detail/apis/components/UrlForm.vue';

import { CONTENT_TYPE, HTTP_HEADERS } from '@/utils/constant';

const Agent = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/Agent.vue'));
const InputGroup = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/content/InputGroup.vue'));
const RequestBody = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/content/RequestBody.vue'));
const ResponseStatus = defineAsyncComponent(() => import('@/components/ResponseStatus/index.vue'));

interface Props {
  serviceOptions: {label: string, value: string}[];
  mockAPIInfo?: {
    id?: string;
    method?: string;
    endpoint?: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  serviceOptions: () => ([]),
  mockAPIInfo: () => ({})
});

const { t } = useI18n();

const WS = inject('WS', ref());
const uuid = inject('uuid', ref());
const wsResponse = inject('wsResponse', ref());

const urlRef = ref();
const inputGroupRef = ref();
const requestBodyRef = ref();

const spread = ref(false);
const showDebug = ref(true);
const debugging = ref(false);
const responseContent = ref();
const openHeader = ref(true);
const openBody = ref(true);

const method = ref();
const server = ref();
const endpoint = ref();
const contentType = ref<string|undefined>(undefined);

const myRequest = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });
const { valueKey } = API_EXTENSION_KEY;
let apiUuid: string;
let controller: AbortController;
let statusOpt: EnumMessage<HttpStatus>[] = [];

// WebSocket message types
const WS_MESSAGE_TYPES = {
  HTTP_REQUEST_PROXY: 'HttpRequestProxy'
} as const;

// Other constants
const OTHER_CONSTANTS = {
  API_ITEM_PREFIX: 'api-item',
  CONTENT_DISPOSITION: 'content-Disposition',
  OCTET_STREAM: 'application/octet-stream',
  WILDCARD_ACCEPT: '*/*',
  DEFAULT_FILENAME: 'response'
} as const;

/**
 * Toggle debug panel spread state
 */
const handleToggleSpread = () => {
  spread.value = !spread.value;
};

/**
 * Change debug mode display
 * @param value - Whether to show debug mode
 */
const handleShowDebugChange = (value: boolean) => {
  showDebug.value = value;
};

/**
 * Load HTTP status enum options
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
const handleStopDebug = () => {
  debugging.value = false;
  if (WS.value && WS.value.readyState === 1) {
    apiUuid = '';
  } else {
    controller.abort();
  }
};

/**
 * Send HTTP request for debugging
 */
const handleSendRequest = async () => {
  if (!urlRef.value?.isValid()) {
    return;
  }
  debugging.value = true;

  const parameters: any[] = [];
  if (typeof inputGroupRef.value?.getData === 'function') {
    parameters.push(...inputGroupRef.value.getData());
  }

  const queryData = parameters.filter(i => i.in === ParameterIn.query)
    .map(i => ({ ...i, in: ParameterIn.query, [valueKey]: i.value }));
  const headerData = parameters.filter(i => i.in === ParameterIn.header)
    .map(i => ({ ...i, in: ParameterIn.header, [valueKey]: i.value }));
  const cookieData = parameters.filter(i => i.in === ParameterIn.cookie)
    .map(i => ({ ...i, in: ParameterIn.cookie, [valueKey]: i.value }));

  let apiHref = server.value + endpoint.value;
  if (queryData.length) {
    const hrefObj = new URL(apiHref);
    queryData.forEach(item => {
      hrefObj.searchParams.append(item.name, item[valueKey]);
    });
    apiHref = hrefObj.toString();
  }

  const { bodyContent, bodyOpenApi } = processRequestBody(headerData);

  if (WS.value && WS.value.readyState === 1) {
    await handleWebSocketRequest(queryData, headerData, cookieData, bodyOpenApi);
  } else if (WS.value && WS.value.readyState !== 1) {
    notification.error(t('mock.detail.apis.notifications.proxyNotConnected'));
    debugging.value = false;
  } else {
    await handleHttpRequest(apiHref, headerData, cookieData, bodyContent);
  }
};

/**
 * Process request body data
 * @param headerData - Header data array
 * @returns Processed body content and OpenAPI format
 */
const processRequestBody = (headerData: any[]) => {
  let bodyObj;
  if (typeof requestBodyRef.value?.getData === 'function') {
    bodyObj = requestBodyRef.value.getData();
  }

  let bodyContent;
  let bodyOpenApi = {};

  if (bodyObj) {
    const { contentType: bodyContentType, forms, rawContent } = bodyObj;
    if (bodyContentType) {
      headerData.push({ in: ParameterIn.header, name: HTTP_HEADERS.CONTENT_TYPE, [valueKey]: bodyContentType });

      if (bodyContentType === CONTENT_TYPE.FORM_URLENCODED) {
        const formUrlEncodeParam = (forms || []).map(i => ({ ...i, [valueKey]: i.value }));
        const formJson = {};
        const formUrl = formUrlEncodeParam.map(item => {
          formJson[item.name] = item[valueKey];
          return `${item.name}=${item[valueKey]}`;
        }).join('&');
        bodyContent = formUrl;
        bodyOpenApi = {
          content: {
            [CONTENT_TYPE.FORM_URLENCODED]: {
              [valueKey]: formJson
            }
          }
        };
      } else if (bodyContentType === CONTENT_TYPE.MULTIPART_FORM_DATA) {
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
            [CONTENT_TYPE.MULTIPART_FORM_DATA]: {
              [valueKey]: formJson
            }
          }
        };
      } else {
        bodyContent = rawContent;
        bodyOpenApi = {
          content: {
            [bodyContentType]: {
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
 * Handle WebSocket request
 * @param queryData - Query parameters
 * @param headerData - Header parameters
 * @param cookieData - Cookie parameters
 * @param bodyOpenApi - Request body in OpenAPI format
 */
const handleWebSocketRequest = async (queryData: any[], headerData: any[], cookieData: any[], bodyOpenApi: any) => {
  const parameters = [
    ...queryData,
    ...headerData,
    ...cookieData
  ];

  apiUuid = utils.uuid(OTHER_CONSTANTS.API_ITEM_PREFIX);
  const api = JSON.stringify({
    parameters,
    method: method.value,
    server: { url: server.value },
    requestBody: bodyOpenApi,
    requestId: apiUuid,
    messageType: WS_MESSAGE_TYPES.HTTP_REQUEST_PROXY,
    endpoint: endpoint.value
  });
  WS.value.send(api);
};

/**
 * Handle HTTP request
 * @param apiHref - API URL
 * @param headerData - Header parameters
 * @param cookieData - Cookie parameters
 * @param bodyContent - Request body content
 */
const handleHttpRequest = async (apiHref: string, headerData: any[], cookieData: any[], bodyContent: any) => {
  const header: Record<string, string> = {};

  if (headerData.length) {
    headerData.forEach(item => {
      if (item.name) {
        header[item.name] = item[valueKey] && apiUtils.containsAllAscii(item[valueKey])
          ? item[valueKey]
          : item[valueKey] ? encodeURIComponent(item[valueKey]) : '';
      }
    });
  }

  if (cookieData.length) {
    const cookieStr = cookieData.filter(i => i.name).map(item => {
      return `${item.name}=${item[valueKey]}`;
    }).join('; ');
    header[HTTP_HEADERS.COOKIE] = cookieStr && apiUtils.containsAllAscii(cookieStr)
      ? cookieStr
      : cookieStr ? encodeURIComponent(cookieStr) : '';
  }

  if (contentType.value) {
    header[HTTP_HEADERS.CONTENT_TYPE] = contentType.value;
  }

  controller = new AbortController();
  const signal = controller.signal;
  const axiosConfig = {
    responseType: 'blob',
    url: apiHref,
    method: method.value,
    data: bodyContent || true,
    headers: {
      [HTTP_HEADERS.ACCEPT]: OTHER_CONSTANTS.WILDCARD_ACCEPT,
      ...header
    },
    signal,
    timeout: 660000,
    maxRedirects: 1,
    maxRetries: 1
  };

  const resp = await myRequest.request(axiosConfig);
  await handleHttpResponse(resp);
};
/**
 * Handle HTTP response
 * @param resp - HTTP response object
 */
const handleHttpResponse = async (resp: any) => {
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
      handleDownloadResponseBody();
    }
  }
};

/**
 * Handle WebSocket response
 */
const handleWebSocketResponse = () => {
  debugging.value = false;
  try {
    const respJson = JSON.parse(wsResponse.value);
    const status = +respJson.response.status;

    if (status === 0) {
      const responseHeader = [];
      const responseBody = respJson.response?.rawContent;
      responseContent.value = { status, responseHeader, responseBody };
    } else {
      const header = [];
      (respJson.response?.headerArray || []).forEach((value: string, idx: number, arr: string[]) => {
        if (idx % 2 === 0) {
          header.push({ key: value, value: arr[idx + 1] });
        }
      });
      const responseHeader = header;
      let responseBody = respJson.response?.rawContent;

      if (respJson.response?.contentEncoding) {
        if (respJson.response?.contentEncoding === ContentEncoding.base64) {
          responseBody = dataURLtoBlob(responseBody) || responseBody;
        }
      }

      responseContent.value = { status, responseHeader, responseBody };

      if (Object.prototype.toString.call(responseBody) === '[object Blob]') {
        handleDownloadResponseBody();
      }
    }
  } catch (error) {
    // Handle parsing error silently
  }
};

/**
 * Get download filename from response headers
 * @returns Generated filename
 */
const getDownloadFilename = (): string => {
  const disposition = responseContent.value.responseHeader.find(item => item.key === OTHER_CONSTANTS.CONTENT_DISPOSITION);
  let filename = '';

  if (disposition) {
    filename = disposition?.split(';')[1]?.split('=')[1];
  }

  if (!filename) {
    const paths = endpoint.value?.split('/');
    filename = paths.length ? paths[paths.length - 1] : OTHER_CONSTANTS.DEFAULT_FILENAME;
  }

  if (!filename.includes('.')) {
    const respContentType = (responseContent.value?.responseHeader || []).find(item => item.key === HTTP_HEADERS.CONTENT_TYPE)?.value;
    if (respContentType) {
      const suffix = getFileSuffixByContentType(respContentType);
      suffix && (filename = filename + `.${suffix}`);
    }
  }

  responseContent.value.responseBody = filename;
  return filename;
};

/**
 * Download response body as file
 */
const handleDownloadResponseBody = () => {
  let blob = responseContent.value.responseBody;

  if (!(blob instanceof Blob)) {
    if (typeof blob === 'object') {
      blob = JSON.stringify(blob, null, 2);
    }

    blob = new Blob([blob], {
      type: OTHER_CONSTANTS.OCTET_STREAM
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

watch(() => props.mockAPIInfo.id, () => {
  method.value = props.mockAPIInfo.method || HttpMethod.GET;
  endpoint.value = props.mockAPIInfo.endpoint || '';
  contentType.value = undefined;
  responseContent.value = undefined;
}, {
  immediate: true
});

watch(() => uuid.value, newValue => {
  if (newValue === apiUuid) {
    debugging.value = false;
    handleWebSocketResponse();
  }
});

onMounted(() => {
  loadStatusEnum();
});
</script>
<template>
  <div class="absolute top-10 right-0 flex text-3 z-9">
    <div
      class="w-9 h-max transform-gpu translate-y-0.75 space-y-0.5"
      @click="handleToggleSpread">
      <div
        v-show="!spread || showDebug"
        class="bg-status-success text-white flex flex-col items-center rounded-l-xl py-2 h-max cursor-pointer"
        @click="handleShowDebugChange(true)">
        <Icon icon="icon-zhihangceshi" class="text-3.5 leading-3.5" />
        <span style="writing-mode: vertical-lr;" class="mt-1">{{ t('mock.detail.apis.testRequest') }}</span>
      </div>
      <div
        v-show="!spread || !showDebug"
        class="bg-orange-bg text-white flex flex-col items-center rounded-l-xl py-2 h-max cursor-pointer"
        @click="handleShowDebugChange(false)">
        <Icon icon="icon-jiekoudaili" class="text-3.5 leading-3.5" />
        <span style="writing-mode: vertical-lr;" class="mt-1">{{ t('mock.detail.apis.proxy') }}</span>
      </div>
    </div>
    <div
      class="bg-white border-status-success rounded transition-all duration-500 box-border overflow-x-hidden overflow-y-auto space-y-5"
      :class="[spread && showDebug ? 'w-200 border p-3' : 'w-0 border-0']"
      style="height: 70vh;">
      <span class="font-semibold">{{ t('mock.detail.apis.testRequest') }}</span>
      <UrlForm
        ref="urlRef"
        v-model:method="method"
        v-model:endpoint="endpoint"
        v-model:server="server"
        :options="props.serviceOptions" />
      <InputGroup ref="inputGroupRef" />
      <RequestBody ref="requestBodyRef" />
      <div>
        <template v-if="debugging">
          <Button
            size="small"
            @click="handleStopDebug">
            <LoadingOutlined />
            {{ t('mock.detail.apis.terminate') }}
          </Button>
        </template>
        <template v-else>
          <Button
            size="small"
            type="primary"
            @click="handleSendRequest">
            {{ t('mock.detail.apis.send') }}
          </Button>
        </template>
      </div>
      <div>
        <div class="space-x-2 mb-1.5">
          <span class="font-semibold">{{ t('mock.detail.apis.responseTitle') }}</span>
          <ResponseStatus v-if="responseContent?.status > 0" :status="responseContent?.status" />
        </div>
        <div class="min-h-50 bg-gray-light rounded whitespace-break-spaces space-y-2 p-2">
          <Toggle
            v-if="responseContent"
            v-model:open="openHeader"
            class="text-3"
            title="Response Header">
            <div>
              <div
                v-for="(header, idx) in responseContent.responseHeader"
                :key="idx"
                class="flex span-x-2">
                <label class="text-text-sub-content">{{ header.key }}: </label>
                <span class="text-text-content"> {{ header.value }}</span>
              </div>
            </div>
          </Toggle>
          <Toggle
            v-if="responseContent"
            v-model:open="openBody"
            title="Response Body">
            <div>
              {{ responseContent?.responseBody }}
            </div>
          </Toggle>
        </div>
      </div>
    </div>
    <div
      class="bg-white border-orange-bg rounded transition-all duration-500 box-border overflow-x-hidden overflow-y-auto space-y-2 flex flex-col"
      :class="[spread && !showDebug ? 'w-200 border p-3' : 'w-0 border-0']"
      style="height: 160px;">
      <AsyncComponent :visible="spread && !showDebug">
        <Agent />
      </AsyncComponent>
    </div>
  </div>
</template>
<style scoped>
:deep(.toggle-container) .toggle-title {
  @apply text-3;
}

</style>
