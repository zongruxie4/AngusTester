<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Switch } from 'ant-design-vue';
import { Composite, Icon, IconRequired, Input, notification, Validate } from '@xcan-angus/vue-ui';
import { HttpMethod, regexpUtils, utils, axiosClient } from '@xcan-angus/infra';

import { ContentType, DelayData, ParametersType, PushbackBody, ResponsePushbackConfig } from './types';
import { CONTENT_TYPE_KEYS, HTTP_HEADERS } from '@/utils/constant';
import { API_EXTENSION_KEY, convertBlob } from '@/utils/apis';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';
import DelayParameter from './DelayParameter.vue';
import RequestBody from './RequestBody.vue';
import InputGroup from './InputGroup.vue';

/**
 * <p>Props interface for PushBack component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value: ResponsePushbackConfig | undefined;
  notify: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const { t } = useI18n();

/**
 * <p>API UUID for request tracking</p>
 * <p>Used to identify specific API requests</p>
 */
let apiUuid: string;

/**
 * <p>Axios client instance for HTTP requests</p>
 * <p>Configured with specific timeout and retry settings</p>
 */
// eslint-disable-next-line new-cap
const myRequest = new axiosClient({ timeout: 0, intervalMs: 500, maxRedirects: 0, maxRetries: 5 });

/**
 * <p>WebSocket connection for proxy requests</p>
 * <p>Injected from parent component</p>
 */
const WS = inject('WS', ref());

/**
 * <p>UUID for request identification</p>
 * <p>Injected from parent component</p>
 */
const uuid = inject('uuid', ref());

/**
 * <p>WebSocket response data</p>
 * <p>Injected from parent component</p>
 */
const wsResponse = inject('wsResponse', ref());

/**
 * <p>AbortController for canceling requests</p>
 * <p>TODO: Consider if we need to abort previous requests when switching APIs</p>
 */
let controller: AbortController;

/**
 * <p>API extension key configuration</p>
 * <p>Used for mapping API data properties</p>
 */
const { valueKey } = API_EXTENSION_KEY;

const delayRef = ref();
const inputGroupRef = ref();
const requestBodyRef = ref();

/**
 * <p>Auto push toggle state</p>
 * <p>Determines if requests should be sent automatically</p>
 */
const autoPush = ref(false);

/**
 * <p>HTTP method for the request</p>
 * <p>Default value is GET</p>
 */
const method = ref<HttpMethod>(HttpMethod.GET);

/**
 * <p>Request URL</p>
 * <p>Target URL for the HTTP request</p>
 */
const url = ref<string>();

/**
 * <p>URL input validation error state</p>
 * <p>Indicates if URL input is invalid</p>
 */
const urlError = ref(false);

/**
 * <p>URL validation error message</p>
 * <p>Displays specific validation error messages</p>
 */
const urlErrorMessage = ref<string>();

/**
 * <p>Request body content type</p>
 * <p>Determines the format of the request body</p>
 */
const requestBodyContentType = ref<ContentType>();

/**
 * <p>Handles HTTP method change event</p>
 * <p>Updates the selected HTTP method</p>
 *
 * @param value - The selected HTTP method
 */
const handleHttpMethodChange = (value: HttpMethod) => {
  method.value = value;
};

/**
 * <p>Handles content type change event</p>
 * <p>Manages Content-Type header based on request body type</p>
 *
 * @param value - Content type change data
 */
const handleContentTypeChange = (value: { value: string; name: string, prevValue: string }) => {
  if (value.value === 'none') {
    inputGroupRef.value?.delHeader(value);
    return;
  }

  inputGroupRef.value?.addHeader({ ...value, disabled: true });
};

/**
 * <p>Handles URL input change event</p>
 * <p>Updates URL value and clears validation errors</p>
 *
 * @param event - Input change event
 */
const handleUrlChange = (event: { target: { value: string } }) => {
  url.value = event.target.value;
  urlError.value = false;
  urlErrorMessage.value = undefined;
};

/**
 * <p>Validates URL format</p>
 * <p>Checks if URL is provided and has valid format</p>
 *
 * @param value - URL string to validate
 * @returns true if URL is valid, false otherwise
 */
const validateUrl = (value: string | undefined): boolean => {
  if (!value) {
    urlError.value = true;
    return false;
  }

  if (!regexpUtils.isUrl(value)) {
    urlError.value = true;
    urlErrorMessage.value = t('mock.detail.apis.components.pushBack.urlError');
    return false;
  }

  return true;
};

/**
 * <p>Sends HTTP request with current configuration</p>
 * <p>Handles both WebSocket proxy and direct HTTP requests</p>
 */
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

  const contentType = headerData.find(item => item.name === HTTP_HEADERS.CONTENT_TYPE)?.value;
  if (contentType) {
    if (contentType === CONTENT_TYPE_KEYS.FORM_URLENCODED) {
      const formUrlEncodeParam = forms || [];
      const formJson = {};
      const formUrl = formUrlEncodeParam.map(item => {
        formJson[item.name] = item.value;
        return `${item.name}=${item.value}`;
      }).join('&');
      bodyContent = formUrl;
      bodyOpenApi = {
        content: {
          'application/x-www-form-urlencoded': {
            [valueKey]: formJson
          }
        }
      };
    } else if (contentType === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
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
    notification.error(t('mock.detail.apis.components.pushBack.proxyNotConnected'));
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
      header[HTTP_HEADERS.CONTENT_TYPE] = contentType;
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
    await handleHttpResponse(resp);
  }
};

/**
 * <p>Handles local HTTP response</p>
 * <p>Processes direct HTTP request responses and shows appropriate notifications</p>
 *
 * @param resp - HTTP response object
 */
const handleHttpResponse = async (resp: any) => {
  const status = resp.request?.status;
  if (status === undefined) {
    notification.warning(t('mock.detail.apis.components.pushBack.requestError'));
    return;
  }
  if (status === 0) {
    const response = await convertBlob(resp);
    notification.error(response.message);
  } else if (status < 200 || status >= 400) {
    notification.warning(resp.message);
  } else {
    notification.success(t('actions.tips.sendSuccess'));
  }
};

/**
 * <p>Handles WebSocket proxy response</p>
 * <p>Processes WebSocket responses and shows appropriate notifications</p>
 */
const handleWsResponse = () => {
  try {
    const respJson = JSON.parse(wsResponse.value);
    const status = +respJson.response.status;
    if (status === 0) {
      notification.error(respJson.response.rawContent);
    } else if (status < 200 || status >= 400) {
      notification.warning(respJson.response.rawContent);
    } else {
      notification.success(t('actions.tips.sendSuccess'));
    }
  } catch (error) {
    // Silently handle JSON parse errors
  }
};

/**
 * <p>Resets component to initial state</p>
 * <p>Clears all form data and resets to default values</p>
 */
const resetComponent = () => {
  autoPush.value = false;
  method.value = HttpMethod.GET;
  url.value = undefined;
  urlError.value = false;
  urlErrorMessage.value = undefined;
  requestBodyContentType.value = undefined;
};

/**
 * <p>Component mounted lifecycle hook</p>
 * <p>Sets up watchers and initializes component state</p>
 */
onMounted(() => {
  watch(() => props.notify, () => {
    resetComponent();
  });

  watch(() => props.value, (newValue) => {
    resetComponent();
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
    const contentType = parameters?.find(item => item.name === HTTP_HEADERS.CONTENT_TYPE)?.value;
    if (contentType && CONTENTTYPES.includes(contentType as ContentType)) {
      requestBodyContentType.value = contentType as ContentType;
    }
  }, { immediate: true });

  watch(() => uuid.value, (newValue) => {
    if (newValue === apiUuid) {
      handleWsResponse();
    }
  }, { immediate: true });
});

/**
 * <p>Gets current component data</p>
 * <p>Collects data from all child components and returns complete configuration</p>
 *
 * @returns Complete pushback configuration object
 */
const getData = () => {
  let delay: DelayData | undefined;
  if (typeof delayRef.value?.getData === 'function') {
    delay = delayRef.value.getData();
  }

  const parameters: ParametersType[] = [];
  if (typeof inputGroupRef.value?.getData === 'function') {
    parameters.push(...inputGroupRef.value.getData());
  }

  let body: PushbackBody | undefined;
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

/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides getData and isValid methods for external access</p>
 */
defineExpose({
  /**
   * <p>Gets the current pushback configuration data</p>
   * <p>Collects data from all child components and returns complete config</p>
   *
   * @returns ResponsePushbackConfig object with current configuration
   */
  getData,

  /**
   * <p>Validates the current form state</p>
   * <p>Checks if all required fields and child components are valid</p>
   *
   * @returns true if form is valid, false otherwise
   */
  isValid: (): boolean => {
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

/**
 * <p>Supported content types for request body</p>
 * <p>Defines valid content types for request body configuration</p>
 */
const CONTENTTYPES: ContentType[] = [
  'application/x-www-form-urlencoded',
  'multipart/form-data',
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

/**
 * <p>HTTP method color styles</p>
 * <p>Defines color styling for different HTTP methods in the UI</p>
 */
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
        <div class="mr-1.5">{{ t('mock.detail.apis.components.pushBack.autoPush') }}</div>
        <Switch v-model:checked="autoPush" size="small" />
      </div>
      <Button
        type="link"
        size="small"
        @click="sendRequest">
        <div class="flex items-center">
          <Icon icon="icon-fasong" class="mr-1" />
          <span>{{ t('mock.detail.apis.components.pushBack.manualSend') }}</span>
        </div>
      </Button>
    </div>
    <div class="mt-2">
      <div class="mb-0.5">
        <IconRequired /><span>{{ t('mock.detail.apis.components.pushBack.pushUrl') }}</span>
      </div>
      <Composite>
        <SelectEnum
          :value="method"
          :optionStyle="optionStyle"
          class="w-25 flex-shrink-0"
          enumKey="HttpMethod"
          :placeholder="t('common.method')"
          @change="handleHttpMethodChange" />
        <Validate
          fixed
          class="flex-1"
          :text="urlErrorMessage">
          <Input
            :value="url"
            :error="urlError"
            :maxlength="800"
            trim
            :placeholder="t('mock.detail.apis.components.pushBack.urlPlaceholder')"
            @change="handleUrlChange" />
        </Validate>
      </Composite>
    </div>
    <div class="mt-4">
      <div class="mb-0.5">{{ t('mock.detail.apis.components.pushBack.delay') }}</div>
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
      @contentTypeChange="handleContentTypeChange" />
  </div>
</template>
