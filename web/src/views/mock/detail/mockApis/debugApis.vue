<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { AsyncComponent, Icon, notification, Toggle, ApiUtils as apiUtils, ResponseStatus } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { convertBlob } from '@/views/apis/services/apiHttp/utils';
import { utils, enumUtils, axiosClient } from '@xcan-angus/infra';

import { dataURLtoBlob, getFileSuffixByContentType } from '@/utils/blob';
import UrlForm from '@/views/mock/detail/mockApis/components/urlForm/index.vue';

const Agent = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/agent/index.vue'));
const InputGroup = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/contentForm/InputGroup.vue'));
const RequestBody = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/contentForm/requestBody.vue'));

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

const urlRef = ref();
const myRequest = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });
let apiUuid;

const WS = inject('WS', ref());
const uuid = inject('uuid', ref());
const wsResponse = inject('wsResponse', ref());
// const currentProxy = ref(); // CLIENT_PROXY|NO_PROXY|SERVER_PROXY
// const currentProxyUrl = ref();

// const BodyFormType = [null, 'application/x-www-form-urlencode', 'multipart/form-data'].map(i => ({ label: i || 'none', value: i }));
// const BodyRawType = [
//   'application/json',
//   'text/html',
//   'application/xml',
//   'application/javascript',
//   'text/plain',
//   '*/*'
// ].map(i => ({ label: i, value: i }));

const { valueKey } = API_EXTENSION_KEY;
// 展开收起
const spread = ref(false);
const toggleSpread = () => {
  if (spread.value) {
    spread.value = false;
  } else {
    spread.value = true;
  }
};

const showDebug = ref(true);
const changeShowDebug = (value: boolean) => {
  showDebug.value = value;
};

let statusOpt:{value: string, message: string}[] = [];
const loadStatusEnum = async () => {
  if (statusOpt.length) {
    return;
  }
  const [error, data] = await enumUtils.enumToMessages('HttpStatus');
  if (error) {
    return;
  }
  statusOpt = data || [];
};
const method = ref();
const server = ref();
const endpoint = ref();
const contentType = ref<string|undefined>(undefined);
// raw 选中效果
// const checkeRaw = computed(() => {
//   return !!BodyRawType.find(i => i.value === contentType.value);
// });

// const queryRef = ref();
// const headerRef = ref();
// const cookieRef = ref();
// const formUrlEncodeRef = ref();
// const formDataRef = ref();

// const requestBody = ref();
const inputGroupRef = ref();
const requestBodyRef = ref();

// // 添加query
// const addQuery = () => {
//   queryRef.value.add();
// };
// // 添加header
// const addHeader = () => {
//   headerRef.value.add();
// };
// // 添加cookie
// const addCookie = () => {
//   cookieRef.value.add();
// };

// const addForm = () => {
//   if (contentType.value === 'application/x-www-form-urlencode') {
//     formUrlEncodeRef.value.add();
//   } else {
//     formDataRef.value.add();
//   }
// };

// const clickRawRadio = (RadioEvent) => {
//   const checked = RadioEvent.target.checked;
//   if (checked) {
//     contentType.value = BodyRawType[0].value;
//   }
// };

let controller;

const debuging = ref(false);
const responseContent = ref();
const openHeader = ref(true);
const openBody = ref(true);
const stopDebug = () => {
  debuging.value = false;
  if (WS.value && WS.value.readyState === 1) {
    apiUuid = '';
  } else {
    controller.abort();
  }
};

// 发送请求
const sendRequest = async () => {
  if (!urlRef.value?.isValid()) {
    return;
  }
  debuging.value = true;
  const parameters:any[] = [];
  if (typeof inputGroupRef.value?.getData === 'function') {
    parameters.push(...inputGroupRef.value.getData());
  }
  const queryData = parameters.filter(i => i.in === 'query').map(i => ({ ...i, in: 'query', [valueKey]: i.value }));
  const headerData = parameters.filter(i => i.in === 'header').map(i => ({ ...i, in: 'header', [valueKey]: i.value }));
  const cookieData = parameters.filter(i => i.in === 'cookie').map(i => ({ ...i, in: 'cookie', [valueKey]: i.value }));
  // const query = {};

  let apiHref = server.value + endpoint.value;
  if (queryData.length) {
    const hrefObj = new URL(apiHref);
    queryData.forEach(item => {
      hrefObj.searchParams.append(item.name, item[valueKey]);
    });
    apiHref = hrefObj.toString();
  }

  let bodyObj;
  if (typeof requestBodyRef.value?.getData === 'function') {
    bodyObj = requestBodyRef.value.getData();
  }
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
          if (item.mame) {
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

  if (WS.value && WS.value.readyState === 1) {
    const parameters = [
      ...queryData,
      ...headerData,
      ...cookieData
    ];

    apiUuid = utils.uuid('api-item');
    const api = JSON.stringify({
      parameters,
      method: method.value,
      server: { url: server.value },
      requestBody: bodyOpenApi,
      requestId: apiUuid,
      messageType: 'HttpRequestProxy',
      endpoint: endpoint.value
    });
    WS.value.send(api);
  } else if (WS.value && WS.value.readyState !== 1) {
    notification.error('代理未链接， 请检查代理配置');
    debuging.value = false;
  } else {
    const header: Record<string, string> = {};
    if (headerData.length) {
      headerData.forEach(item => {
        if (item.name) {
          header[item.name] = item[valueKey] && apiUtils.containsAllAscii(item[valueKey]) ? item[valueKey] : item[valueKey] ? encodeURIComponent(item[valueKey]) : '';
        }
      });
    }
    if (cookieData.length) {
      const cookieStr = cookieData.filter(i => i.name).map(item => {
        return `${header[item.name]}=${item[valueKey]}`;
      }).join('; ');
      header.Cookie = cookieStr && apiUtils.containsAllAscii(cookieStr) ? cookieStr : cookieStr ? encodeURIComponent(cookieStr) : '';
    }
    if (contentType.value) {
      header['Content-Type'] = contentType.value;
    }
    controller = new AbortController();
    const signal = controller.signal;
    const axiosConfig = {
      responseType: 'blob',
      url: apiHref,
      method: method.value,
      data: bodyContent || true,
      headers: {
        accept: '*/*',
        ...header
      },
      signal,
      timeout: 660000,
      maxRedirects: 1,
      maxRetries: 1
    };
    const resp = await myRequest.request(axiosConfig);
    await onHttpResponse(resp);
  }
};
// 本地响应
const onHttpResponse = async (resp) => {
  const status = resp.request.status;
  debuging.value = false;
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
  // 响应状态
  // 响应头
  // 响应体
};
// 代理响应
const onWsResponse = () => {
  debuging.value = false;
  try {
    const respJson = JSON.parse(wsResponse.value);
    const status = +respJson.response.status;
    if (status === 0) {
      const responseHeader = [];
      const responseBody = respJson.response?.rawContent;
      responseContent.value = { status, responseHeader, responseBody };
    } else {
      const header = [];
      (respJson.response?.headerArray || []).forEach((value, idx, arr) => {
        if (idx % 2 === 0) {
          header.push({ key: value, value: arr[idx + 1] });
          // if (header[value]) {
          //   if (typeof header[value] === 'string') {
          //     header[value] = [header[value], arr[idx + 1]];
          //   } else {
          //     header[value].push(arr[idx + 1]);
          //   }
          // } else {
          //   header[value] = arr[idx + 1];
          // }
        }
      });
      const responseHeader = header;
      let responseBody = respJson.response?.rawContent;
      if (respJson.response?.contentEncoding) {
        if (respJson.response?.contentEncoding === 'base64') {
          responseBody = dataURLtoBlob(responseBody) || responseBody;
        }
      }
      responseContent.value = { status, responseHeader, responseBody };
      if (Object.prototype.toString.call(responseBody) === '[object Blob]') {
        downloadResponseBody();
      }
    }
  } catch {}
};

const getDownloadFilename = () => {
  const disposition = responseContent.value.responseHeader.find(item => item.key === 'content-Disposition');
  let filename = '';
  if (disposition) {
    filename = disposition?.split(';')[1]?.split('=')[1];
  }
  if (!filename) {
    const paths = endpoint.value?.split('/');
    filename = paths.length ? paths[paths.length - 1] : 'response';
  }
  if (!filename.includes('.')) {
    const respContentType = (responseContent.value?.responseHeader || []).find(item => item.key === 'Content-Type')?.value;
    if (respContentType) {
      const suffix = getFileSuffixByContentType(respContentType);
      suffix && (filename = filename + `.${suffix}`);
    }
  }
  responseContent.value.responseBody = filename;
  return filename;
};

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

watch(() => props.mockAPIInfo.id, () => {
  method.value = props.mockAPIInfo.method || 'GET';
  endpoint.value = props.mockAPIInfo.endpoint || '';
  contentType.value = undefined;
  responseContent.value = undefined;
}, {
  immediate: true
});

watch(() => uuid.value, newValue => {
  if (newValue === apiUuid) {
    debuging.value = false;
    onWsResponse();
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
      @click="toggleSpread">
      <div
        v-show="!spread || showDebug"
        class="bg-status-success text-white flex flex-col items-center rounded-l-xl py-2 h-max cursor-pointer"
        @click="changeShowDebug(true)">
        <Icon icon="icon-zhihangceshi" class="text-3.5 leading-3.5" />
        <span style="writing-mode: vertical-lr;" class="mt-1">测试请求</span>
      </div>
      <div
        v-show="!spread || !showDebug"
        class="bg-orange-bg text-white flex flex-col items-center rounded-l-xl py-2 h-max cursor-pointer"
        @click="changeShowDebug(false)">
        <Icon icon="icon-jiekoudaili" class="text-3.5 leading-3.5" />
        <span style="writing-mode: vertical-lr;" class="mt-1">代理</span>
      </div>
    </div>
    <div
      class="bg-white border-status-success rounded transition-all duration-500 box-border overflow-x-hidden overflow-y-auto space-y-5"
      :class="[spread && showDebug ? 'w-200 border p-3' : 'w-0 border-0']"
      style="height: 70vh;">
      <span class="font-semibold">测试请求</span>
      <UrlForm
        ref="urlRef"
        v-model:method="method"
        v-model:endpoint="endpoint"
        v-model:server="server"
        :options="props.serviceOptions" />
      <InputGroup ref="inputGroupRef" />
      <RequestBody ref="requestBodyRef" />
      <div>
        <template v-if="debuging">
          <Button
            size="small"
            @click="stopDebug">
            <LoadingOutlined />
            终止
          </Button>
        </template>
        <template v-else>
          <Button
            size="small"
            type="primary"
            @click="sendRequest">
            发送
          </Button>
        </template>
      </div>
      <div>
        <div class="space-x-2 mb-1.5">
          <span class="font-semibold">响应</span>
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
