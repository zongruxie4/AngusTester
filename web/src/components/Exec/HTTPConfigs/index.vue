<script setup lang="ts">
import { ref, onMounted, computed, defineAsyncComponent, watch, watchEffect } from 'vue';
import { Tabs, TabPane, Badge } from 'ant-design-vue';
import { Composite, Input, SelectEnum } from '@xcan-angus/vue-ui';
import { utils, HttpMethod as HttpMethodEnum } from '@xcan-angus/infra';
import { getUriByParams } from '@/utils/apis/index';
import { useI18n } from 'vue-i18n';

import { RequestConfigs } from './PropsType';
const { t } = useI18n();

export type ParameterConfig = {
  name: string;
  enabled: boolean;
  disabled: boolean;
  id: string;
  value: string;
  type: 'string';
  in: ParameterIn;
}

export interface Props {
  value: RequestConfigs;
  errorNum?: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  errorNum: 0
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:errorNum', value: number): void;
}>();

const ExecAuthencation = defineAsyncComponent(() => import('@/components/ExecAuthencation/index.vue'));
const RequestBody = defineAsyncComponent(() => import('./RequestBody/index.vue'));
const ParameterInput = defineAsyncComponent(() => import('./ParameterInput.vue'));
const ParameterPure = defineAsyncComponent(() => import('./ParameterPure.vue'));

const NO_BINARY_TYPES = [
  'application/x-www-form-urlencoded',
  'multipart/form-data',
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];
const STREAM_TYPE = 'application/octet-stream';
const ENCODED_TYPE = 'application/x-www-form-urlencoded';

const queryPathRef = ref();
const headerRef = ref();
const cookieRef = ref();
const bodyRef = ref();

const contentType = ref<string>();
const requestBody = ref<RequestConfigs['body']>();

const authentication = ref<RequestConfigs['authentication']>();
const defaultAuthentication = ref<RequestConfigs['authentication']>();
const authInHeader = ref<{name:string;value:string;}[]>([]);

const cookieParams = ref<ParameterConfig[]>([]);
const defaultCookieParams = ref<ParameterConfig[]>([]);
const headerParams = ref<ParameterConfig[]>([]);
const defaultHeaderParams = ref<ParameterConfig[]>([]);
const queryPathParams = ref<ParameterConfig[]>([]);
const defaultQueryPathParams = ref<ParameterConfig[]>([]);

const server = ref<RequestConfigs['server']>();
const endpoint = ref<string>();
const url = ref<string>();
const httpMethod = ref<HttpMethodEnum>(HttpMethodEnum.GET);

const nameRepeat = ref(false);
const urlError = ref(false);

const queryPathErrorNum = ref(0);
const headerErrorNum = ref(0);
const cookieErrorNum = ref(0);
const bodyErrorNum = ref(0);

const urlChange = () => {
  urlError.value = false;
};

const urlBlur = (event: { target: { value: string } }) => {
  const value = event.target.value;
  if (value) {
    const [pathStr, queryStr] = value.split('?');
    if (queryStr) {
      const querys = queryStr.split('&');
      querys.forEach(query => {
        const [name, value] = query.split('=');
        queryPathParams.value.push({
          name,
          value,
          in: 'query',
          type: 'string',
          enabled: true,
          id: utils.uuid(),
          disabled: false
        });
      });
      url.value = pathStr;
    } else {
      url.value = value;
    }
  } else {
    url.value = '';
  }
};

const authenticationToHeader = (data: {
  type: 'http' | 'apiKey' | 'oauth2';
  value: string;
  apiKeys: {
    name: string;
    in: 'header' | 'query';
    value: string;
  }[];
  oauth2: {
    clientCredentials: {
      clientIn: string;
      tokenUrl: string;
      refreshUrl: string;
      clientId: string;
      clientSecret: string;
      scopes: string[];
    };
    password: {
      clientIn: string;
      tokenUrl: string;
      refreshUrl: string;
      clientId: string;
      clientSecret: string;
      username: string;
      password: string;
      scopes: string[];
    };
    newToken: boolean;
  }
}) => {
  if (!data) {
    return [];
  }

  const type = data.type;
  const value = data.value;
  const apiKeys = data.apiKeys || [];
  const result:{name:string;value:string}[] = [];
  if (type === 'http') {
    result.push({
      name: 'Authorization',
      value: value
    });
  } else if (type === 'apiKey') {
    if (apiKeys.length) {
      for (let i = 0, len = apiKeys.length; i < len; i++) {
        const item = apiKeys[i];
        if (item.in === 'header') {
          result.push({
            name: item.name,
            value: item.value
          });
        }
      }
    }
  } else if (type === 'oauth2') {
    if (data.oauth2?.newToken !== true) {
      result.push({
        name: 'access_token',
        value
      });
    }
  }

  return result;
};

const chanegAuthentication = async (data: any) => {
  authentication.value = data;
  authInHeader.value = authenticationToHeader(data);
};

const requestBodyChange = (data: RequestConfigs['body']) => {
  if (!requestBody.value) {
    requestBody.value = JSON.parse(JSON.stringify(data));
    return;
  }

  for (const key in requestBody.value) {
    delete requestBody.value[key];
  }

  for (const key in data) {
    requestBody.value[key] = data[key];
  }
};

const contentTypeChange = (value: string) => {
  contentType.value = value;
};

const pathChange = (pathList) => {
  if (pathList.length) {
    if (!url.value) {
      url.value = '/';
    }
  }

  url.value = getUriByParams(url.value, pathList);
};

const cookieChange = (data: ParameterConfig[]) => {
  cookieParams.value = data;
};

const headerChange = (data: ParameterConfig[]) => {
  headerParams.value = data;
};

const queryChange = (data: ParameterConfig[]) => {
  queryPathParams.value = data;
};

const reset = () => {
  contentType.value = undefined;
  requestBody.value = undefined;
  defaultAuthentication.value = undefined;
  authentication.value = undefined;
  authInHeader.value = [];
  cookieParams.value = [];
  defaultCookieParams.value = [];
  headerParams.value = [];
  defaultHeaderParams.value = [];
  queryPathParams.value = [];
  defaultQueryPathParams.value = [];
  server.value = undefined;
  endpoint.value = undefined;
  url.value = undefined;
  httpMethod.value = 'GET';
  queryPathErrorNum.value = 0;
  headerErrorNum.value = 0;
  cookieErrorNum.value = 0;
  bodyErrorNum.value = 0;
};

const initializedData = async () => {
  reset();
  if (!props.value) {
    return;
  }

  const {
    method: _method,
    url: _url,
    server: _server,
    endpoint: _endpoint,
    authentication: _authentication,
    parameters,
    body
  } = JSON.parse(JSON.stringify(props.value));

  httpMethod.value = _method;
  endpoint.value = _endpoint;
  server.value = _server;
  url.value = _url;

  let _contentType: string;
  const _parameters = parameters || [];
  for (let i = 0, len = _parameters.length; i < len; i++) {
    const data: ParameterConfig = {
      ..._parameters[i],
      id: utils.uuid(),
      type: 'string',
      disabled: false
    };
    const _in = data.in;
    if (_in === 'query' || _in === 'path') {
      defaultQueryPathParams.value.push({ ...data });
      queryPathParams.value.push({ ...data });
    } else if (_in === 'cookie') {
      defaultCookieParams.value.push({ ...data });
      cookieParams.value.push({ ...data });
    } else {
      if (data.name === 'Content-Type') {
        _contentType = data.value;
      } else {
        defaultHeaderParams.value.push({ ...data });
        headerParams.value.push({ ...data });
      }
    }
  }

  const paths = _url
    ? _url.match(/{[^{}]+}/gi)?.map(item => {
      return item.replace(/{(\S*)}/gi, '$1');
    })
    : [];
  if (paths?.length) {
    paths.forEach(name => {
      const item = queryPathParams.value.find(ele => ele.name === name && ele.in === 'path');
      if (!item) {
        queryPathParams.value.splice(-1, 0, {
          name,
          type: 'string',
          in: 'path',
          enabled: true,
          value: '',
          disabled: false,
          id: utils.uuid()
        });
      } else {
        item.enabled = true;
      }
    });
  }

  authentication.value = _authentication;
  if (authentication.value) {
    defaultAuthentication.value = JSON.parse(JSON.stringify(authentication.value));
    authInHeader.value = authenticationToHeader(authentication.value);
  }

  if (!_contentType) {
    contentType.value = null;
  } else {
    contentType.value = _contentType;
    if (NO_BINARY_TYPES.includes(_contentType)) {
      contentType.value = _contentType;
    } else {
      contentType.value = STREAM_TYPE;
    }
  }

  if (requestBody.value) {
    const { forms, rawContent, fileName } = requestBody.value;
    // 修复Content-Type为空时，自动设置Content-Type
    if (!contentType.value) {
      if (forms?.length) {
        contentType.value = ENCODED_TYPE;
      } else {
        if (rawContent?.length) {
          if (fileName) {
            contentType.value = STREAM_TYPE;
          } else {
            contentType.value = '*/*';
          }
        }
      }
    }
  }

  requestBody.value = body || {};
};

onMounted(() => {
  watch(() => props.value, () => {
    initializedData();
  }, { immediate: true });

  watchEffect(() => {
    let errorNum = queryPathErrorNum.value +
      headerErrorNum.value +
      cookieErrorNum.value +
      bodyErrorNum.value;

    if (urlError.value) {
      errorNum += 1;
    }

    emit('update:errorNum', errorNum);
  });
});

const hasRequestBody = computed(() => {
  const body = requestBody.value;
  if (body && (body.forms?.length || body.rawContent?.length)) {
    return true;
  }

  if (contentType.value) {
    return true;
  }

  return false;
});

const pureHeaderParams = computed(() => {
  const data: ParameterConfig[] = [];
  if (authInHeader.value?.length) {
    const _data: ParameterConfig[] = authInHeader.value.map(({ name, value }) => {
      return {
        name,
        value,
        enabled: true,
        disabled: true,
        id: utils.uuid(),
        in: 'header',
        type: 'string'
      };
    });

    data.push(..._data);
  }

  if (contentType.value) {
    data.push({
      name: 'Content-Type',
      value: contentType.value,
      enabled: true,
      disabled: true,
      id: utils.uuid(),
      in: 'header',
      type: 'string'
    });
  }

  return data;
});

const headerNum = computed(() => {
  return headerParams.value.length + pureHeaderParams.value.length;
});

const queryPathNum = computed(() => {
  return queryPathParams.value.length;
});

const cookieNum = computed(() => {
  return cookieParams.value.length;
});

const isValid = (): boolean => {
  urlError.value = false;
  nameRepeat.value = false;
  let errorNum = 0;
  if (!url.value) {
    errorNum++;
    urlError.value = true;
  }

  if (typeof queryPathRef.value?.isValid === 'function') {
    if (!queryPathRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof headerRef.value?.isValid === 'function') {
    if (!headerRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof cookieRef.value?.isValid === 'function') {
    if (!cookieRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof bodyRef.value?.isValid === 'function') {
    if (!bodyRef.value.isValid()) {
      errorNum++;
    }
  }

  return !errorNum;
};

const getData = (): RequestConfigs => {
  const parameters: RequestConfigs['parameters'] = [];
  if (queryPathParams.value.length) {
    const list = queryPathParams.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { enabled, type, in: _in, value, name } = list[i];
      parameters.push({
        enabled,
        type,
        in: _in,
        value,
        name
      });
    }
  }

  if (headerParams.value.length) {
    const list = headerParams.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { enabled, type, in: _in, value, name } = list[i];
      parameters.push({
        enabled,
        type,
        in: _in,
        value,
        name
      });
    }
  }

  if (contentType.value) {
    parameters.push({
      name: 'Content-Type',
      value: contentType.value,
      enabled: true,
      in: 'header',
      type: 'string'
    });
  }

  if (cookieParams.value.length) {
    const list = cookieParams.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { enabled, type, in: _in, value, name } = list[i];
      parameters.push({
        enabled,
        type,
        in: _in,
        value,
        name
      });
    }
  }

  return {
    method: httpMethod.value,
    url: url.value,
    server: server.value,
    endpoint: endpoint.value,
    body: requestBody.value,
    authentication: authentication.value,
    parameters
  };
};

defineExpose({
  getData,
  isValid
});
</script>

<template>
  <Composite>
    <SelectEnum
      v-model:value="httpMethod"
      class="w-25"
      :enumKey="HttpMethodEnum"
      :placeholder="t('protocol.http.requestMethod')" />
    <Input
      :value="url"
      :error="urlError"
      :maxlength="800"
      :title="url"
      trim
      class="flex-1"
      :placeholder="t('xcan_exec.urlMax800Chars')"
      @change="urlChange"
      @blur="urlBlur" />
  </Composite>
  <Tabs size="small">
    <TabPane key="query">
      <template #tab>
        <Badge
          class="count-Badge-container"
          size="small"
          :count="queryPathErrorNum">
          <div class="flex items-center space-x-0.5">
            <div>{{ t('xcan_exec.requestParameters') }}</div>
            <div class="flex items-center space-x-0.5">
              <em>(</em>
              <span>{{ queryPathNum }}</span>
              <em>)</em>
            </div>
          </div>
        </Badge>
      </template>
      <ParameterInput
        ref="queryPathRef"
        v-model:errorNum="queryPathErrorNum"
        defaultIn="query"
        :defaultValue="defaultQueryPathParams"
        :showInType="true"
        @changePath="pathChange"
        @change="queryChange" />
    </TabPane>
    <TabPane key="header">
      <template #tab>
        <Badge
          class="count-Badge-container"
          size="small"
          :count="headerErrorNum">
          <div class="flex items-center space-x-0.5">
            <div>{{ t('xcan_exec.requestHeaders') }}</div>
            <div class="flex items-center space-x-0.5">
              <em>(</em>
              <span>{{ headerNum }}</span>
              <em>)</em>
            </div>
          </div>
        </Badge>
      </template>
      <ParameterPure :value="pureHeaderParams" />
      <ParameterInput
        ref="headerRef"
        v-model:errorNum="headerErrorNum"
        :defaultValue="defaultHeaderParams"
        defaultIn="header"
        @change="headerChange" />
    </TabPane>
    <TabPane key="Cookie">
      <template #tab>
        <Badge
          class="count-Badge-container"
          size="small"
          :count="cookieErrorNum">
          <div class="flex items-center space-x-0.5">
            <div>Cookie</div>
            <div class="flex items-center space-x-0.5">
              <em>(</em>
              <span>{{ cookieNum }}</span>
              <em>)</em>
            </div>
          </div>
        </Badge>
      </template>
      <ParameterInput
        ref="cookieRef"
        v-model:errorNum="cookieErrorNum"
        :defaultValue="defaultCookieParams"
        defaultIn="cookie"
        @change="cookieChange" />
    </TabPane>
    <TabPane key="Authorization">
      <template #tab>
        <Badge v-if="!!authentication?.type" color="green" />
        <span>Authorization</span>
      </template>
      <ExecAuthencation :defaultValue="defaultAuthentication" @change="chanegAuthentication" />
    </TabPane>
    <TabPane key="body">
      <template #tab>
        <Badge v-if="hasRequestBody" color="green" />
        <Badge size="small" :count="bodyErrorNum">{{ t('xcan_exec.requestBody') }}</Badge>
      </template>
      <RequestBody
        ref="bodyRef"
        v-model:errorNum="bodyErrorNum"
        :contentType="contentType"
        :value="requestBody"
        @change="requestBodyChange"
        @contentTypeChange="contentTypeChange" />
    </TabPane>
  </Tabs>
</template>
<style scoped>
.ant-tabs> :deep(.ant-tabs-nav) {
  padding-top: 4px;
}

em {
  font-style: normal;
}

.ant-badge {
  color: inherit;
}

.count-Badge-container :deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
}
</style>
