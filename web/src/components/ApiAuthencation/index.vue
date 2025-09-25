<script setup lang="ts">
import { watch, ref, computed, reactive, inject, onMounted, nextTick } from 'vue';
import { Input, Select, IconRequired, Icon, SelectEnum, notification } from '@xcan-angus/vue-ui';
import { Button, RadioGroup } from 'ant-design-vue';
import { http, utils, TESTER, AuthClientIn } from '@xcan-angus/infra';
import axios from 'axios';
import { useI18n } from 'vue-i18n';

import { AuthItem, authTypeOptions as _authTypeOptions, flowAuthType, authLabels, flowAuthKeys, encryptionTypeOpt, inOpt, getAuthItem, getApiKeyData, getShowApiKeyData } from './interface';

import ApiUtils from '@/utils/ApiUtils/index';
const { t } = useI18n();
const { API_EXTENSION_KEY, encode, getModelDataByRef } = ApiUtils;

export interface Props {
  defaultValue:AuthItem,
  auth?: boolean;
  viewType: boolean;
}
const { valueKey, securityApiKeyPerfix, oAuth2Key, oAuth2Token, newTokenKey } = API_EXTENSION_KEY;

const apiBaseInfo = inject('apiBaseInfo', ref());
const ws = inject('WS', ref());
let tokenUuid = '';
const authTypeOptions = computed(() => {
  return _authTypeOptions.filter(i => apiBaseInfo.value?.serviceId || i.value !== 'extends');
});

const props = withDefaults(defineProps<Props>(), {
  defaultValue: undefined,
  viewType: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value:AuthItem): void;
  (e: 'update:auth', value:boolean):void;
  (e: 'rendered', value: true);
}>();

const type = ref<string | null>(null);
const authType = ref('authorizationCode');
const scheme = ref(); // http 类型下 值
const apiKeyContentList = ref<{name: string, in: string, [valueKey]: string}[]>([getAuthItem()]); // apikey 类型下 值
const extendsSecurityOpt = ref<{id: string; type: string; model: string; key: string;}[]>([]);
const oauthKey = ref(1);
let scopesObj = {};

const tokenJson = ref({});
const token = ref();

const flowauthLabel = computed(() => {
  return authLabels.filter(i => {
    return flowAuthKeys[authType.value].includes(i.valueKey);
  });
});

// 父级的安全认证
const loadProjectSecurity = async () => {
  const [error, resp] = await http.get(`${TESTER}/services/${apiBaseInfo.value?.serviceId}/comp/type`, {
    types: ['securitySchemes'],
    keys: [],
    ignoreModel: false
  });

  if (error) {
    return;
  }
  extendsSecurityOpt.value = resp.data || [];
};

const httpAuthData = reactive({
  name: '',
  [valueKey]: ''
});

const extendsId = ref();

// 切换认证类型
const handleChangeType = () => {
  httpAuthData.name = '';
  httpAuthData[valueKey] = '';
  scheme.value = '';
  extendsId.value = '';
  authType.value = 'authorizationCode';
  if (['basic', 'bearer'].includes(type.value)) {
    emit('change', { type: 'http' });
  } else {
    emit('change', { type: type.value });
  }
  if (type.value) {
    emit('update:auth', true);
  } else {
    emit('update:auth', false);
  }
};

// http 方案
const handleBlur = (e, key:'name'|'x-xc-value'):void => {
  const value = e.target.value;
  switch (type.value) {
    case 'basic':
      httpAuthData[key] = value;
      // scheme.value = 'Basic ' + encode(httpAuthData?.name, httpAuthData?.value);
      emit('change', { type: 'http', ...(httpAuthData || {}), scheme: type.value });
      break;
    case 'bearer':
      httpAuthData[valueKey] = value.startsWith('Bearer ') ? value : 'Bearer ' + value;
      scheme.value = httpAuthData[valueKey];
      emit('change', { type: 'http', [valueKey]: scheme.value, scheme: type.value });
      break;
  }
  // emit('change', { type: 'http', [valueKey]: scheme.value, scheme: type.value });
};

const initHttpBasicData = (data: {name: string; 'x-xc-value': string}) => {
  // const decodeMap = decode(scheme.value.replace(/Basic\s+/, '')) as {name: string, value: string};
  // httpAuthData.name = decodeMap.name;
  // httpAuthData.value = decodeMap.value;
  const { name = '' } = data || {};
  httpAuthData.name = name || '';
  httpAuthData[valueKey] = data?.[valueKey] || '';
};

// 继承方案
const handleSelectSecurity = (value) => {
  // const data = JSON.parse(option.model);
  emit('change', { $ref: value });
};

// apikey 方案
const initApiKeyContentList = (newValue) => {
  // const { name, in } = newValue;
  const first = { name: newValue.name, in: newValue.in || 'header', [valueKey]: newValue[valueKey] };
  const others = newValue[securityApiKeyPerfix] || [];
  apiKeyContentList.value = [first, ...others];
};

const handleAddApiKey = () => {
  apiKeyContentList.value.push(getAuthItem());
};
const handleDelApiKey = (index) => {
  apiKeyContentList.value.splice(index, 1);
};

const changeApiKey = () => {
  const { name } = apiKeyContentList.value.filter(i => i.name)[0] || {};
  const lists = apiKeyContentList.value.slice(1);
  const data = {
    type: 'apiKey',
    name,
    in: apiKeyContentList.value[0].in,
    [valueKey]: apiKeyContentList.value[0][valueKey],
    [securityApiKeyPerfix]: lists.length ? lists : undefined
    // [xcAuthMethod]: type.value
  };
  emit('change', data);
};

// oauth2 方案
const oauthData = reactive({
  authorizationUrl: undefined,
  tokenUrl: undefined,
  refreshUrl: undefined,
  scopes: [],
  'x-xc-oauth2-clientId': undefined,
  'x-xc-oauth2-clientSecret': undefined,
  'x-xc-oauth2-callbackUrl': undefined,
  'x-xc-oauth2-username': undefined,
  'x-xc-oauth2-password': undefined,
  'x-xc-oauth2-clientAuthType': 'REQUEST_BODY'
});

const pakageOauthData = () => {
  // if (oauthKey.value === 2) {
  const flowData = {
    'x-xc-oauth2-clientAuthType': oauthData['x-xc-oauth2-clientAuthType'],
    [oAuth2Token]: token.value
  };
  flowAuthKeys[authType.value].forEach(i => {
    if (i === 'scopes') {
      const scopes = {};
      oauthData[i].forEach(key => {
        scopes[key] = scopesObj[key] || '';
      });
      flowData[i] = scopes;
    } else {
      flowData[i] = oauthData[i];
    }
  });
  const authKey = authType.value === 'authorizationCodePKCE' ? 'authorizationCode' : authType.value;
  const data = {
    type: 'oauth2',
    flows: {
      [authKey]: flowData
    },
    [oAuth2Key]: authType.value,
    [oAuth2Token]: scheme.value,
    [newTokenKey]: oauthKey.value === 2
  };
  return data;
};

const onOauthChange = () => {
  const data = pakageOauthData();
  emit('change', data);
};

const onOauthFlowTypeChange = (authFlow) => {
  const data = props.defaultValue.flows?.[authFlow] || {};
  tokenJson.value = {};
  token.value = undefined;
  validate.value = false;
  oauthData['x-xc-oauth2-clientAuthType'] = data?.['x-xc-oauth2-clientAuthType'] || 'REQUEST_BODY';
  flowAuthKeys[authType.value].forEach(i => {
    if (i === 'scopes') {
      scopesObj = data[i] || {};
      oauthData[i] = Object.keys(scopesObj);
    } else {
      oauthData[i] = data[i];
    }
  });
  onOauthChange();
};

// 获取令牌 密码模式
const fetchOauth2Token = async () => {
  const params: Record<string, string> = {
    grant_type: authType.value
  };
  flowAuthKeys[authType.value].forEach(key => {
    if (key === 'tokenUrl' || key === 'x-xc-oauth2-clientAuthType' || key === 'x-xc-oauth2-clientId' || key === 'x-xc-oauth2-clientSecret') {
      return;
    }
    if (key === 'scopes') {
      params.scope = oauthData[key].join(' ');
    }
    if (key === 'x-xc-oauth2-username') {
      params.username = oauthData[key];
    }
    if (key === 'x-xc-oauth2-password') {
      params.password = oauthData[key];
    }
    if (key === 'refreshUrl') {
      params.refresh_url = oauthData[key];
    }
  });
  if (authType.value !== 'password' && authType.value !== 'clientCredentials') {
    try {
      // eslint-disable-next-line no-new
      new URL(oauthData.tokenUrl);
      const uri = http.getURLSearchParams(params, true);
      window.open(oauthData.tokenUrl + '?' + uri);
    } catch {
      notification.error('Authorization failed Couldn’t complete authentication.');
    }
    return;
  }

  if (authType.value === 'clientCredentials' || authType.value === 'password') {
    const validateRes = validateOauthData();
    if (!validateRes) {
      return;
    }
    try {
      let url:any = new URL(oauthData.tokenUrl);
      for (const key in params) {
        if (params[key]) {
          if (key === 'grant_type' && params[key] === 'clientCredentials') {
            url.searchParams.append(key, 'client_credentials');
          } else {
            url.searchParams.append(key, params[key]);
          }
        }
      }
      let requestBody;
      if (oauthData['x-xc-oauth2-clientAuthType'] === 'REQUEST_BODY') {
        requestBody = {
          content: {
            'application/x-www-form-urlencoded': {
              schema: {
                type: 'object',
                properties: {
                  client_id: {
                    type: 'string',
                    [valueKey]: oauthData['x-xc-oauth2-clientId']
                  },
                  client_secret: {
                    type: 'string',
                    [valueKey]: oauthData['x-xc-oauth2-clientId']
                  }
                },
                [valueKey]: {
                  client_id: oauthData['x-xc-oauth2-clientId'],
                  client_secret: oauthData['x-xc-oauth2-clientSecret']
                }
              }
            }
          }
        };
      }
      if (oauthData['x-xc-oauth2-clientAuthType'] === 'QUERY_PARAMETER') {
        url.searchParams.append('client_id', oauthData['x-xc-oauth2-clientId']);
        url.searchParams.append('client_secret', oauthData['x-xc-oauth2-clientSecret']);
      }
      const header:any[] = [];
      if (oauthData['x-xc-oauth2-clientAuthType'] === 'BASIC_AUTH_HEADER') {
        header.push({ name: 'Authorization', in: 'header', schema: { type: 'string' }, [valueKey]: 'Basic ' + encode(oauthData['x-xc-oauth2-clientId'], oauthData['x-xc-oauth2-clientSecret']) });
      }
      if (requestBody) {
        header.push({ name: 'Content-Type', in: 'header', schema: { type: 'string' }, [valueKey]: 'application/x-www-form-urlencoded' });
      }
      url = url.toString();
      tokenUuid = utils.uuid('anthencation-token');
      if (ws.value) {
        if (ws.value.readyState !== 1) {
          notification.error(t('xcan_apiAuthencation.proxyNotConnected'));
          return;
        }
        const params = {
          method: 'post',
          server: { url: url },
          endpoint: '',
          requestId: tokenUuid,
          messageType: 'HttpRequestProxy',
          requestBody,
          parameters: header
        };
        if (!requestBody) {
          delete params.requestBody;
        }
        ws.value.send(JSON.stringify(params));
        return;
      }
      const headers: Record<string, string> = {};
      if (requestBody) {
        headers['Content-Type'] = 'application/x-www-form-urlencoded';
      }
      tokenJson.value = {};
      token.value = undefined;
      if (header.length) {
        headers.Authorization = header[0][valueKey];
      }
      axios.post(url, requestBody?.['application/x-www-form-urlencoded'], {
        headers
      }).then(resp => {
        tokenJson.value = resp.data || {};
        const result = resp.data;
        for (const key in result) {
          if (key === 'access_token') {
            token.value = result.access_token;
          }
          if (Object.prototype.toString.call(result[key]) === '[object Object]') {
            for (const subKey in result[key]) {
              if (subKey === 'access_token') {
                token.value = result[key].access_token;
              }
            }
          }
        }
        onOauthChange();
      }).catch(error => {
        onOauthChange();
        notification.warning(error?.message);
      });
    } catch {
      notification.error('Authorization failed Couldn’t complete authentication.');
    }
  }
};

// 获取继承的 oauth 的 token
const fetchOauthToken = async (oauthData) => {
  if (!oauthData[newTokenKey] && oauthData[oAuth2Token]) {
    return [{ access_token: oauthData[oAuth2Token] }];
  }
  const authFlowType = oauthData['x-xc-oauth2-authFlow'];
  const params: Record<string, string> = {
    grant_type: authFlowType
  };
  flowAuthKeys[authFlowType].forEach(key => {
    if (key === 'tokenUrl' || key === 'x-xc-oauth2-clientAuthType' || key === 'x-xc-oauth2-clientId' || key === 'x-xc-oauth2-clientSecret') {
      return;
    }
    if (key === 'scopes') {
      params.scope = oauthData[key].join(' ');
    }
    if (key === 'x-xc-oauth2-username') {
      params.username = oauthData[key];
    }
    if (key === 'x-xc-oauth2-password') {
      params.password = oauthData[key];
    }
    if (key === 'refreshUrl') {
      params.refresh_url = oauthData[key];
    }
  });
  if (authType.value === 'clientCredentials' || authType.value === 'password') {
    try {
      let url:any = new URL(oauthData.tokenUrl);
      for (const key in params) {
        if (params[key]) {
          if (key === 'grant_type' && params[key] === 'clientCredentials') {
            url.searchParams.append(key, 'client_credentials');
          } else {
            url.searchParams.append(key, params[key]);
          }
        }
      }
      let requestBody;
      if (oauthData['x-xc-oauth2-clientAuthType'] === 'REQUEST_BODY') {
        requestBody = {
          content: {
            'application/x-www-form-urlencoded': {
              schema: {
                type: 'object',
                properties: {
                  client_id: {
                    type: 'string',
                    [valueKey]: oauthData['x-xc-oauth2-clientId']
                  },
                  client_secret: {
                    type: 'string',
                    [valueKey]: oauthData['x-xc-oauth2-clientId']
                  }
                },
                [valueKey]: {
                  client_id: oauthData['x-xc-oauth2-clientId'],
                  client_secret: oauthData['x-xc-oauth2-clientSecret']
                }
              }
            }
          }
        };
      }
      if (oauthData['x-xc-oauth2-clientAuthType'] === 'QUERY_PARAMETER') {
        url.searchParams.append('client_id', oauthData['x-xc-oauth2-clientId']);
        url.searchParams.append('client_secret', oauthData['x-xc-oauth2-clientSecret']);
      }
      const header:any[] = [];
      if (oauthData['x-xc-oauth2-clientAuthType'] === 'BASIC_AUTH_HEADER') {
        header.push({ name: 'Authorization', in: 'header', schema: { type: 'string' }, [valueKey]: 'Basic ' + encode(oauthData['x-xc-oauth2-clientId'], oauthData['x-xc-oauth2-clientSecret']) });
      }
      if (requestBody) {
        header.push({ name: 'Content-Type', in: 'header', schema: { type: 'string' }, [valueKey]: 'application/x-www-form-urlencoded' });
      }
      url = url.toString();
      // if (ws.value) {
      //   const params = {
      //     method: 'post',
      //     server: { url: url },
      //     endpoint: '',
      //     requestId: 'anthencation-token',
      //     messageType: 'HttpRequestProxy',
      //     requestBody,
      //     parameters: header
      //   };
      //   if (!requestBody) {
      //     delete params.requestBody;
      //   }
      //   ws.value.send(JSON.stringify(params));
      //   return;
      // }
      const headers: Record<string, string> = {};
      if (requestBody) {
        headers['Content-Type'] = 'application/x-www-form-urlencoded';
      }
      tokenJson.value = {};
      token.value = undefined;
      if (header.length) {
        headers.Authorization = header[0][valueKey];
      }
      const resp = await axios.post(url, requestBody?.['application/x-www-form-urlencoded'], {
        headers
      });
      tokenJson.value = resp.data || {};
      const result = resp.data;
      let tokenValue;
      for (const key in result) {
        if (key === 'access_token') {
          tokenValue = result.access_token;
        }
        if (Object.prototype.toString.call(result[key]) === '[object Object]') {
          for (const subKey in result[key]) {
            if (subKey === 'access_token') {
              tokenValue = result[key].access_token;
            }
          }
        }
      }
      if (tokenValue) {
        return [{ access_token: tokenValue }];
      }
      return [{}];
    } catch {
      return [{}];
    }
  } else {
    return [{}];
  }
};

const validate = ref(false);
const validateOauthData = (showValidate = true) => {
  if (showValidate) {
    validate.value = true;
  }
  const keys = flowauthLabel.value.map(i => i.valueKey);
  if (keys.every(key => key === 'refreshUrl' || !!oauthData[key])) {
    return true;
  } else {
    return false;
  }
};

const models = {};
// 获取继承的认证模型
const getExtendModel = async (ref) => {
  if (!ref) {
    return [{}];
  }
  const [error, resp] = await getModelDataByRef(apiBaseInfo.value?.serviceId, ref);
  if (error) {
    return [{}];
  }
  models[ref] = resp?.data?.model || {};
  let data = JSON.parse(resp.data.model);
  data = { ...data, ...(data?.extentions || {}) };
  if (data.type === 'http') {
    if (data.scheme === 'basic') {
      return [{ Authorization: { username: data.name, password: data[valueKey] } }];
    }
    return [{ Authorization: data[valueKey] }];
  }
  if (data.type === 'apiKey') {
    return getShowApiKeyData(data);
  }
  if (data.type === 'oauth2') {
    return await fetchOauthToken(data);
  }
};

watch(() => props.defaultValue, (newValue) => {
  if (newValue?.$ref) {
    type.value = 'extends';
    extendsId.value = newValue.$ref;
    return;
  }
  if (newValue?.scheme) {
    if (newValue.scheme === 'basic') {
      // scheme.value = newValue[valueKey];
      initHttpBasicData(newValue);
      type.value = 'basic';
    } else if (newValue.scheme === 'bearer') {
      type.value = 'bearer';
      scheme.value = newValue[valueKey];
      httpAuthData[valueKey] = scheme.value;
    }
    return;
  }
  if (newValue?.type === 'oauth2') {
    type.value = 'oauth2';
    if (newValue[newTokenKey]) {
      oauthKey.value = 2;
    }
    authType.value = newValue[oAuth2Key] || 'authorizationCode';
    if (newValue.flows) {
      oauthData['x-xc-oauth2-clientAuthType'] = newValue.flows[authType.value]?.['x-xc-oauth2-clientAuthType'];
      flowAuthKeys[authType.value].forEach(i => {
        if (i === 'scopes') {
          scopesObj = newValue.flows[authType.value]?.[i] || {};
          oauthData[i] = Object.keys(scopesObj);
        } else {
          oauthData[i] = newValue.flows[authType.value]?.[i];
        }
      });
      scheme.value = newValue[oAuth2Token];
    }
    return;
  }
  if (newValue?.type === 'apiKey') {
    type.value = 'apiKey';
    initApiKeyContentList(newValue);
  }
}, {
  immediate: true,
  deep: true
});

watch(() => apiBaseInfo.value?.serviceId, newValue => {
  if (newValue) {
    loadProjectSecurity();
  }
}, {
  immediate: true
});
const getAuthData = async (dataSource) => {
  switch (type.value) {
    case 'bearer':
      return [{
        Authorization: scheme.value
      }];
    case 'basic':
      return [{
        Authorization: { username: httpAuthData.name, password: httpAuthData[valueKey] }
      }];
    // case 'http-bearer':
    //   return [{
    //     Authorization: scheme.value
    //   }];
    case 'apiKey':
      return getApiKeyData(apiKeyContentList.value);
    case 'extends':
      return await getExtendModel(dataSource.$ref);
    case 'oauth2':
      if (dataSource[newTokenKey]) {
        if (dataSource[oAuth2Key] === 'password') {
          if (dataSource.flows.password?.[oAuth2Token]) {
            return [{ access_token: dataSource.flows.password?.[oAuth2Token] }];
          } else if (validateOauthData(false)) {
            await fetchOauth2Token();
            const data = pakageOauthData();
            return getAuthData({ ...data, [newTokenKey]: false });
          }
        }
        if (dataSource[oAuth2Key] === 'clientCredentials') {
          if (dataSource.flows.clientCredentials?.[oAuth2Token]) {
            return [{ access_token: dataSource.flows.clientCredentials?.[oAuth2Token] }];
          } else if (validateOauthData(false)) {
            await fetchOauth2Token();
            const data = pakageOauthData();
            return getAuthData({ ...data, [newTokenKey]: false });
          }
        }
        return [{}];
      } else {
        if (dataSource[oAuth2Token]) {
          return [{
            access_token: dataSource[oAuth2Token]
          }];
        } else {
          return [{}];
        }
      }
  }
};

const onResponse = (response) => {
  if (response.requestId === tokenUuid) {
    try {
      if (response.response.status >= 200 && response.response.status < 300) {
        const result = JSON.parse(response.response.rawContent);
        tokenJson.value = result;
        for (const key in result) {
          if (key === 'access_token') {
            token.value = result.access_token;
          }
          if (Object.prototype.toString.call(result[key]) === '[object Object]') {
            for (const subKey in result[key]) {
              if (subKey === 'access_token') {
                token.value = result[key].access_token;
              }
            }
          }
        }
      } else {
        const result = JSON.parse(response.response.rawContent);
        tokenJson.value = result;
      }
    } catch {
      notification.warning(response.response.rawContent);
    } finally {
      onOauthChange();
    }
  }
};

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

const getModelResolve = () => {
  return models;
};

defineExpose({ getAuthData, onResponse, getModelResolve });
</script>
<template>
  <div class="rounded">
    <div class="text-3">
      <div class="flex items-center">
        <span class="mr-3">{{ t('xcan_apiAuthencation.authenticationType') }}</span>
        <RadioGroup
          v-model:value="type"
          :disabled="props.viewType"
          :options="authTypeOptions"
          @change="handleChangeType">
        </RadioGroup>
      </div>
    </div>
    <div class="min-h-50 py-2 text-3">
      <template v-if="type === 'extends'">
        <div class="flex items-center">
          <span class="mr-2">{{ t('xcan_apiAuthencation.selectServiceProjectSecurityAuth') }}</span>
          <Select
            v-model:value="extendsId"
            class="w-100"
            :readonly="props.viewType"
            :options="extendsSecurityOpt"
            :fieldNames="{value: 'ref', label: 'key'}"
            @change="handleSelectSecurity">
          </Select>
        </div>
      </template>
      <template v-if="type==='basic'">
        <div class="flex items-center flex-grow flex-shrink mb-3">
          <span class="text-3 leading-3 text-theme-sub-content w-15 relative">
            <IconRequired class="absolute -left-2" />
            {{ t('xcan_apiAuthencation.username') }}
          </span>
          <Input
            :placeholder="t('xcan_apiAuthencation.enterUsername')"
            :value="httpAuthData?.name"
            :readonly="props.viewType"
            size="small"
            class="w-100"
            :allowClear="true"
            @blur="handleBlur($event, 'name')" />
        </div>
        <div class="flex items-center flex-grow flex-shrink">
          <span class="text-3 leading-3 text-theme-sub-content  w-15">{{ t('xcan_apiAuthencation.password') }}</span>
          <Input
            :placeholder="t('xcan_apiAuthencation.enterPassword')"
            :value="httpAuthData?.[valueKey]"
            :allowClear="true"
            :readonly="props.viewType"
            type="password"
            class="w-100"
            size="small"
            @blur="handleBlur($event, valueKey)" />
        </div>
      </template>
      <template v-if="type==='bearer'">
        <div class="flex items-center">
          <span class="w-15">{{ t('xcan_apiAuthencation.accessToken') }}</span>
          <Input
            :placeholder="t('xcan_apiAuthencation.enterToken')"
            :value="httpAuthData?.[valueKey]"
            :allowClear="true"
            :readonly="props.viewType"
            class="w-100"
            size="small"
            @blur="handleBlur($event, valueKey)" />
        </div>
      </template>
      <template v-if="type==='oauth2'">
        <div class="flex items-center mb-3">
          <span class="w-25">{{ t('xcan_apiAuthencation.configurationMethod') }}</span>
          <RadioGroup
            v-model:value="oauthKey"
            :options="[{value: 1, label: t('xcan_apiAuthencation.existingToken')}, {value: 2, label: t('xcan_apiAuthencation.generateToken')}]"
            :disabled="props.viewType"
            @change="onOauthChange">
          </RadioGroup>
        </div>
        <template v-if="oauthKey === 1">
          <div class="text-3 space-y-3">
            <div class="flex items-center">
              <span class="w-25">{{ t('xcan_apiAuthencation.accessToken') }}</span>
              <Input
                v-model:value="scheme"
                :allowClear="true"
                :readonly="props.viewType"
                class="w-100"
                @blur="onOauthChange" />
            </div>
          </div>
        </template>
        <template v-if="oauthKey === 2">
          <div class="text-3 space-y-3">
            <div class="flex items-center">
              <span class="w-25">{{ t('common.type') }}</span>
              <Select
                v-model:value="authType"
                class="w-100"
                :readonly="props.viewType"
                :options="flowAuthType"
                @change="onOauthFlowTypeChange" />
            </div>
            <div
              v-for="item in flowauthLabel"
              :key="item.valueKey"
              :class="{'error': validate && !oauthData[item.valueKey] && !['scopes', 'refreshUrl'].includes(item.valueKey)}"
              class="flex items-center">
              <span class="w-25">
                <IconRequired v-show="item.required" />{{ item.label }}
              </span>
              <Select
                v-if="item.valueKey === 'x-xc-oauth2-challengeMethod'"
                v-model:value="oauthData[item.valueKey]"
                class="w-100"
                :readonly="props.viewType"
                :placeholder="item.maxLength ? t('xcan_apiAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                :options="encryptionTypeOpt"
                @change="onOauthChange" />
              <Select
                v-else-if="item.valueKey==='scopes'"
                v-model:value="oauthData[item.valueKey]"
                :readonly="props.viewType"
                :placeholder="item.maxLength ? t('xcan_apiAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                class="w-100"
                mode="tags" />
              <Input
                v-else
                v-model:value="oauthData[item.valueKey]"
                class="w-100"
                :readonly="props.viewType"
                :allowClear="true"
                :placeholder="item.maxLength ? t('xcan_apiAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                :maxLength="item.maxLength"
                @blur="onOauthChange" />
              <!-- <span
                v-if="item.maxLength"
                class="ml-2">
                最多可输入{{ item.maxLength }}个字符
              </span> -->
            </div>
            <div class="flex items-center">
              <span class="w-25">{{ t('xcan_apiAuthencation.clientAuthentication') }}</span>
              <SelectEnum
                v-model:value="oauthData['x-xc-oauth2-clientAuthType']"
                :readonly="props.viewType"
                class="w-100"
                :enumKey="AuthClientIn"
                :lazy="false"
                @change="onOauthChange" />
            </div>
          </div>
          <div
            v-for="key in Object.keys(tokenJson)"
            :key="key"
            class="mt-2">
            <span>{{ key }}</span>: <span>{{ tokenJson[key] }}</span>
          </div>
          <Button
            type="primary"
            size="small"
            class="ml-25 mt-3"
            :disabled="props.viewType"
            @click="fetchOauth2Token">
            {{ t('xcan_apiAuthencation.getToken') }}
          </Button>
        </template>
        <!-- <Tabs>
          <TabPane key="1" tab="配置已有令牌">
          </TabPane>
          <TabPane key="2" tab="配置生成令牌">
          </TabPane>
        </Tabs> -->
      </template>
      <template v-if="type==='apiKey'">
        <div
          v-for="(item, index) in apiKeyContentList"
          :key="index"
          class="space-y-2 mb-4">
          <div class="flex items-center">
            <span class="w-15">{{ t('xcan_apiAuthencation.name') }}</span>
            <Input
              v-model:value="item.name"
              class="w-100"
              :maxLength="100"
              :allowClear="true"
              :readonly="props.viewType"
              dataType="mixin-en"
              includes="-_"
              @blur="changeApiKey" />
            <Icon
              v-show="apiKeyContentList.length > 1"
              icon="icon-qingchu"
              class="text-4 ml-1 cursor-pointer"
              @click="handleDelApiKey(index)" />
            <Icon
              icon="icon-jia"
              class="text-4 ml-1 cursor-pointer"
              @click="handleAddApiKey" />
          </div>
          <div class="flex items-center">
            <span class="w-15">{{ t('xcan_apiAuthencation.value') }}</span>
            <Input
              v-model:value="item[valueKey]"
              :allowClear="true"
              :readonly="props.viewType"
              class="w-100"
              @blur="changeApiKey" />
          </div>
          <div class="flex items-center">
            <span class="w-15">{{ t('xcan_apiAuthencation.position') }}</span>
            <Select
              v-model:value="item.in"
              class="w-100"
              :options="inOpt"
              :readonly="props.viewType"
              @change="changeApiKey" />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
<style scoped>
/* .error {

} */
</style>
