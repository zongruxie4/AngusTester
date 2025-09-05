<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { Button, Switch } from 'ant-design-vue';
import { Composite, Icon, IconRequired, Input, notification, Validate } from '@xcan-angus/vue-ui';
import { HttpMethod, regexpUtils, utils, axiosClient } from '@xcan-angus/infra';

import SelectEnum from '@/components/SelectEnum/index.vue';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { convertBlob } from '@/views/apis/services/apiHttp/utils';

import { ContentType, DelayData, ParametersType, PushbackBody, ResponsePushbackConfig } from './types';
import DelayParameter from './DelayParameter.vue';
import RequestBody from './RequestBody.vue';
import InputGroup from './InputGroup.vue';

interface Props {
  value:ResponsePushbackConfig|undefined;
  notify:number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

let apiUuid;
const myRequest = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });
const WS = inject('WS', ref());
const uuid = inject('uuid', ref());
const wsResponse = inject('wsResponse', ref());

// @TODO 切换接口之后是否需要终止上个接口的请求？？？
let controller;
const { valueKey } = API_EXTENSION_KEY;

const delayRef = ref();
const inputGroupRef = ref();
const requestBodyRef = ref();

const autoPush = ref(false);
const method = ref<HttpMethod>('GET');
const url = ref<string>();
const urlError = ref(false);
const urlErrorMessage = ref<string>();
const requestBodyContentType = ref<ContentType>();

const httpMethodChange = (value: HttpMethod) => {
  method.value = value;
};

const contentTypeChange = (value:{value:string;name:string, prevValue:string}) => {
  if (value.value === 'none') {
    inputGroupRef.value?.delHeader(value);
    return;
  }

  inputGroupRef.value?.addHeader({ ...value, disabled: true });
};

const urlChange = (event: { target: { value: string } }) => {
  url.value = event.target.value;
  urlError.value = false;
  urlErrorMessage.value = undefined;
};

const validateUrl = (value:string|undefined):boolean => {
  if (!value) {
    urlError.value = true;
    return false;
  }

  if (!regexpUtils.isUrl(value)) {
    urlError.value = true;
    urlErrorMessage.value = '回推地址格式错误';
    return false;
  }

  return true;
};

const sendRequest = async () => {
  if (!validateUrl(url.value)) {
    return;
  }

  const { body, method, parameters, ...other } = getData();
  const queryData = (parameters || []).filter(item => item.in === 'query').map(i => ({ ...i, in: 'query', [valueKey]: i.value }));
  const headerData = (parameters || []).filter(item => item.in === 'header').map(i => ({ ...i, in: 'header', [valueKey]: i.value }));
  const cookieData = (parameters || []).filter(item => item.in === 'cookie').map(i => ({ ...i, in: 'cookie', [valueKey]: i.value }));

  let bodyContent;
  let bodyOpenApi = {};
  const { forms, rawContent } = body || {};

  const contentType = headerData.find(item => item.name === 'Content-Type')?.value;
  if (contentType) {
    if (contentType === 'application/x-www-form-urlencode') {
      const formUrlEncodeParam = forms || [];
      const formJson = {};
      const formUrl = formUrlEncodeParam.map(item => {
        formJson[item.name] = item.value;
        return `${item.name}=${item.value}`;
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
      const formUrlEncodeParam = forms || [];
      const formData = new FormData();
      const formJson = {};
      formUrlEncodeParam.forEach(item => {
        if (item.name) {
          formJson[item.name] = item.value;
          formData.append(item.name, item.value);
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

  if (WS.value && WS.value.readyState === 1) {
    const parameters = [
      ...queryData,
      ...headerData,
      ...cookieData
    ];

    apiUuid = utils.uuid('api-item');
    const api = JSON.stringify({
      parameters,
      mehtod: method,
      server: { url: other.url },
      requestBody: bodyOpenApi,
      requestId: apiUuid,
      messageType: 'HttpRequestProxy',
      endpoint: ''
    });
    WS.value.send(api);
  } else if (WS.value && WS.value.readyState !== 1) {
    notification.error('代理未链接， 请检查代理配置');
  } else {
    const header: Record<string, string> = {};
    if (headerData.length) {
      headerData.forEach(item => {
        if (item.name) {
          header[item.name] = encodeURIComponent(item[valueKey]);
        }
      });
    }
    if (cookieData.length) {
      header.Cookie = encodeURIComponent(cookieData.filter(i => i.name).map(item => {
        return `${header[item.name]}=${item[valueKey]}`;
      }).join('; '));
    }
    const url = new URL(other.url);
    if (queryData.length) {
      queryData.forEach(query => {
        url.searchParams.append(query.name, query.value);
      });
    }
    if (contentType) {
      header['Content-Type'] = contentType;
    }
    controller = new AbortController();
    const signal = controller.signal;
    const axiosConfig = {
      responseType: 'blob',
      url: url.toString(),
      method: method,
      data: bodyContent,
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

// 本地发送响应
const onHttpResponse = async (resp) => {
  const status = resp.request?.status;
  if (status === undefined) {
    notification.warning('请求错误');
    return;
  }
  if (status === 0) {
    const response = await convertBlob(resp);
    notification.error(response.message);
  } else if (status < 200 || status >= 400) {
    notification.warning(resp.message);
  } else {
    notification.success('发送成功');
  }
};

// 代理响应
const onWsResponse = () => {
  try {
    const respJson = JSON.parse(wsResponse.value);
    const status = +respJson.response.status;
    if (status === 0) {
      notification.error(respJson.response.rawContent);
    } else if (status < 200 || status >= 400) {
      notification.warning(respJson.response.rawContent);
    } else {
      notification.success('发送成功');
    }
  } catch {}
};

const reset = () => {
  autoPush.value = false;
  method.value = 'GET';
  url.value = undefined;
  urlError.value = false;
  urlErrorMessage.value = undefined;
  requestBodyContentType.value = undefined;
};

onMounted(() => {
  watch(() => props.notify, () => {
    reset();
  });

  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    const {
      autoPush: _autoPush,
      url: _url,
      method: _method,
      parameters
    } = newValue;
    autoPush.value = _autoPush;
    method.value = _method;
    url.value = _url;
    const contentType = parameters?.find(item => item.name === 'Content-Type')?.value;
    if (contentType && CONTENTTYPES.includes(contentType as ContentType)) {
      requestBodyContentType.value = contentType as ContentType;
    }
  }, { immediate: true });

  watch(() => uuid.value, (newValue) => {
    if (newValue === apiUuid) {
      onWsResponse();
    }
  }, { immediate: true });
});

const getData = () => {
  let delay:DelayData|undefined;
  if (typeof delayRef.value?.getData === 'function') {
    delay = delayRef.value.getData();
  }

  const parameters: ParametersType[] = [];
  if (typeof inputGroupRef.value?.getData === 'function') {
    parameters.push(...inputGroupRef.value.getData());
  }

  let body:PushbackBody|undefined;
  if (typeof requestBodyRef.value?.getData === 'function') {
    body = requestBodyRef.value.getData();
  }

  return {
    delay,
    body,
    autoPush: autoPush.value,
    method: method.value,
    url: url.value!,
    parameters: parameters.length ? parameters : undefined
  };
};

defineExpose({
  getData,
  isValid: ():boolean => {
    let errorNum = 0;
    if (!validateUrl(url.value)) {
      errorNum++;
    }

    if (typeof delayRef.value?.isValid === 'function') {
      if (!delayRef.value.isValid()) {
        errorNum++;
      }
    }

    if (typeof inputGroupRef.value?.isValid === 'function') {
      if (!inputGroupRef.value.isValid()) {
        errorNum++;
      }
    }

    if (typeof requestBodyRef.value?.isValid === 'function') {
      if (!requestBodyRef.value.isValid()) {
        errorNum++;
      }
    }

    return !errorNum;
  }
});

const CONTENTTYPES:ContentType[] = ['application/x-www-form-urlencode', 'multipart/form-data', 'application/json', 'text/html', 'application/xml', 'application/javascript', 'text/plain', '*/*'];
const optionStyle = {
  GET: 'color:rgba(30, 136, 229, 1);',
  HEAD: 'color:rgba(255, 82, 82, 1);',
  POST: 'color:rgba(51, 183, 130, 1);',
  PUT: 'color:rgba(255, 167, 38, 1);',
  PATCH: 'color:rgba(171, 71, 188, 1);',
  DELETE: 'color:rgba(255, 82, 82, 1);',
  OPTIONS: 'color:rgba(255, 82, 82, 1);',
  TRACE: 'color:rgba(255, 82, 82, 1);'
};
</script>
<template>
  <div class="leading-5">
    <div class="flex items-center justify-between">
      <div class="flex items-center">
        <div class="mr-1.5">请求触发后自动推送</div>
        <Switch v-model:checked="autoPush" size="small" />
      </div>
      <Button
        type="link"
        size="small"
        @click="sendRequest">
        <div class="flex items-center">
          <Icon icon="icon-fasong" class="mr-1" />
          <span>手动发送</span>
        </div>
      </Button>
    </div>
    <div class="mt-2">
      <div class="mb-0.5">
        <IconRequired /><span>回推地址</span>
      </div>
      <Composite>
        <SelectEnum
          :value="method"
          :optionStyle="optionStyle"
          class="w-25 flex-shrink-0"
          enumKey="HttpMethod"
          placeholder="请求方法"
          @change="httpMethodChange" />
        <Validate
          fixed
          class="flex-1"
          :text="urlErrorMessage">
          <Input
            :value="url"
            :error="urlError"
            :maxlength="800"
            trim
            placeholder="最大支持800个字符"
            @change="urlChange" />
        </Validate>
      </Composite>
    </div>
    <div class="mt-4">
      <div class="mb-0.5">回推延迟</div>
      <DelayParameter ref="delayRef" :value="props.value?.delay" />
    </div>
    <InputGroup
      ref="inputGroupRef"
      class="mt-5"
      :value="props.value?.parameters"
      :notify="props.notify" />
    <RequestBody
      ref="requestBodyRef"
      class="mt-4"
      :value="props.value?.body"
      :contentType="requestBodyContentType"
      :notify="props.notify"
      @contentTypeChange="contentTypeChange" />
  </div>
</template>
