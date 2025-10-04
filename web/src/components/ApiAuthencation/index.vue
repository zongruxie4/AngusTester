<script setup lang="ts">
// Vue core imports
import { watch, ref, computed, reactive, inject, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Input, Select, IconRequired, Icon, SelectEnum, notification } from '@xcan-angus/vue-ui';
import { Button, RadioGroup } from 'ant-design-vue';

// Infrastructure imports
import { http, utils, TESTER, AuthClientIn } from '@xcan-angus/infra';
import axios from 'axios';

// Local imports
import {
  AuthItem,
  authenticationTypeOptions as _authTypeOptions,
  oauth2FlowTypeOptions as flowAuthType,
  oauth2FieldLabels as authLabels,
  oauth2FlowFieldKeys as flowAuthKeys,
  oauth2EncryptionMethodOptions as encryptionTypeOpt,
  apiKeyPlacementOptions as inOpt,
  createDefaultAuthenticationItem as getAuthItem,
  processApiKeyData as getApiKeyData,
  processApiKeyDataForDisplay as getShowApiKeyData
} from './interface';

// Utility imports
import ApiUtils from '@/utils/apis/index';

const { t } = useI18n();
const { API_EXTENSION_KEY, encode, getModelDataByRef } = ApiUtils;

// Component props interface
export interface Props {
  defaultValue: AuthItem;
  auth?: boolean;
  viewType: boolean;
}

// API extension key constants
const { valueKey, securityApiKeyPerfix, oAuth2Key, oAuth2Token, newTokenKey } = API_EXTENSION_KEY;

// Injected dependencies
const apiBaseInfo = inject('apiBaseInfo', ref());
const webSocketConnection = inject('WS', ref());

// Token management
let tokenUuid = '';

// Computed authentication type options based on service context
const authenticationTypeOptions = computed(() => {
  return _authTypeOptions.filter(option => apiBaseInfo.value?.serviceId || option.value !== 'extends');
});

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  defaultValue: undefined,
  viewType: false
});

// Component events
const emit = defineEmits<{
  (e: 'change', value: AuthItem): void;
  (e: 'update:auth', value: boolean): void;
  (e: 'rendered', value: true);
}>();

// Authentication state management
const authenticationType = ref<string | null>(null);
const oauth2FlowType = ref('authorizationCode');
const authenticationScheme = ref(); // HTTP authentication authenticationScheme value
const apiKeyConfigurationList = ref<{name: string, in: string, [valueKey]: string}[]>([getAuthItem()]); // API key configuration values
const inheritedSecurityOptions = ref<{id: string; type: string; model: string; key: string;}[]>([]);
const oauth2ConfigurationMode = ref(1);
let oauth2ScopesObject = {};

// Token management state
const tokenResponseData = ref({});
const accessToken = ref();

// Computed OAuth2 flow field labels based on selected flow type
const oauth2FlowFieldLabels = computed(() => {
  return authLabels.filter(field => {
    return flowAuthKeys[oauth2FlowType.value].includes(field.valueKey);
  });
});

/**
 * Load inherited security schemes from parent project
 */
const loadProjectSecurity = async (): Promise<void> => {
  const [error, response] = await http.get(`${TESTER}/services/${apiBaseInfo.value?.serviceId}/comp/type`, {
    types: ['securitySchemes'],
    keys: [],
    ignoreModel: false
  });

  if (error) {
    return;
  }
  inheritedSecurityOptions.value = response.data || [];
};

// HTTP authentication data
const httpAuthenticationData = reactive({
  name: '',
  [valueKey]: ''
});

// Inherited security authenticationScheme selection
const selectedInheritedSecurityId = ref();

/**
 * Handle authentication type change event
 */
const handleAuthenticationTypeChange = (): void => {
  // Reset HTTP authentication data
  httpAuthenticationData.name = '';
  httpAuthenticationData[valueKey] = '';
  authenticationScheme.value = '';
  selectedInheritedSecurityId.value = '';
  oauth2FlowType.value = 'authorizationCode';

  // Emit appropriate change event based on authentication type
  if (['basic', 'bearer'].includes(authenticationType.value)) {
    emit('change', { type: 'http' });
  } else {
    emit('change', { type: authenticationType.value });
  }

  // Update authentication status
  if (authenticationType.value) {
    emit('update:auth', true);
  } else {
    emit('update:auth', false);
  }
};

/**
 * Handle HTTP authentication input blur event
 * @param event - Input blur event
 * @param fieldKey - Field key being updated
 */
const handleHttpAuthenticationBlur = (event: Event, fieldKey: 'name' | 'x-xc-value'): void => {
  const value = (event.target as HTMLInputElement).value;
  switch (authenticationType.value) {
    case 'basic':
      httpAuthenticationData[fieldKey] = value;
      emit('change', { type: 'http', ...(httpAuthenticationData || {}), scheme: authenticationType.value });
      break;
    case 'bearer':
      httpAuthenticationData[valueKey] = value.startsWith('Bearer ') ? value : 'Bearer ' + value;
      authenticationScheme.value = httpAuthenticationData[valueKey];
      emit('change', { type: 'http', [valueKey]: authenticationScheme.value, scheme: authenticationType.value });
      break;
  }
};

/**
 * Initialize HTTP basic authentication data
 * @param data - Authentication data object
 */
const initHttpBasicData = (data: {name: string; 'x-xc-value': string}): void => {
  const { name = '' } = data || {};
  httpAuthenticationData.name = name || '';
  httpAuthenticationData[valueKey] = data?.[valueKey] || '';
};

/**
 * Handle inherited security authenticationScheme selection
 * @param securityId - Selected security authenticationScheme ID
 */
const handleInheritedSecuritySelection = (securityId: string): void => {
  emit('change', { $ref: securityId });
};

/**
 * Initialize API key configuration list
 * @param newValue - New API key configuration value
 */
const initApiKeyContentList = (newValue: any): void => {
  const first = { name: newValue.name, in: newValue.in || 'header', [valueKey]: newValue[valueKey] };
  const others = newValue[securityApiKeyPerfix] || [];
  apiKeyConfigurationList.value = [first, ...others];
};

/**
 * Add new API key configuration item
 */
const handleAddApiKeyConfiguration = (): void => {
  apiKeyConfigurationList.value.push(getAuthItem());
};

/**
 * Delete API key configuration item
 * @param index - Index of item to delete
 */
const handleDeleteApiKeyConfiguration = (index: number): void => {
  apiKeyConfigurationList.value.splice(index, 1);
};

/**
 * Update API key configuration and emit change event
 */
const updateApiKeyConfiguration = (): void => {
  const { name } = apiKeyConfigurationList.value.filter(item => item.name)[0] || {};
  const additionalKeys = apiKeyConfigurationList.value.slice(1);
  const configurationData = {
    type: 'apiKey',
    name,
    in: apiKeyConfigurationList.value[0].in,
    [valueKey]: apiKeyConfigurationList.value[0][valueKey],
    [securityApiKeyPerfix]: additionalKeys.length ? additionalKeys : undefined
  };
  emit('change', configurationData);
};

// OAuth2 configuration data
const oauth2ConfigurationData = reactive({
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

/**
 * Package OAuth2 data for configuration
 * @returns Packaged OAuth2 configuration data
 */
const packageOauth2ConfigurationData = () => {
  const flowData = {
    'x-xc-oauth2-clientAuthType': oauth2ConfigurationData['x-xc-oauth2-clientAuthType'],
    [oAuth2Token]: accessToken.value
  };
  flowAuthKeys[oauth2FlowType.value].forEach(field => {
    if (field === 'scopes') {
      const scopes = {};
      oauth2ConfigurationData[field].forEach(key => {
        scopes[key] = oauth2ScopesObject[key] || '';
      });
      flowData[field] = scopes;
    } else {
      flowData[field] = oauth2ConfigurationData[field];
    }
  });
  const authKey = oauth2FlowType.value === 'authorizationCodePKCE' ? 'authorizationCode' : oauth2FlowType.value;
  const configurationData = {
    type: 'oauth2',
    flows: {
      [authKey]: flowData
    },
    [oAuth2Key]: oauth2FlowType.value,
    [oAuth2Token]: authenticationScheme.value,
    [newTokenKey]: oauth2ConfigurationMode.value === 2
  };
  return configurationData;
};

/**
 * Handle OAuth2 configuration change
 */
const handleOauth2ConfigurationChange = (): void => {
  const configurationData = packageOauth2ConfigurationData();
  emit('change', configurationData);
};

/**
 * Handle OAuth2 flow type change
 * @param authFlow - Authentication flow type
 */
const handleOauth2FlowTypeChange = (authFlow: string): void => {
  const flowData = props.defaultValue.flows?.[authFlow] || {};
  tokenResponseData.value = {};
  accessToken.value = undefined;
  validate.value = false;
  oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] = flowData?.['x-xc-oauth2-clientAuthType'] || 'REQUEST_BODY';
  flowAuthKeys[oauth2FlowType.value].forEach(field => {
    if (field === 'scopes') {
      oauth2ScopesObject = flowData[field] || {};
      oauth2ConfigurationData[field] = Object.keys(oauth2ScopesObject);
    } else {
      oauth2ConfigurationData[field] = flowData[field];
    }
  });
  handleOauth2ConfigurationChange();
};

// 获取令牌 密码模式
const fetchOauth2Token = async () => {
  const params: Record<string, string> = {
    grant_type: oauth2FlowType.value
  };
  flowAuthKeys[oauth2FlowType.value].forEach(key => {
    if (key === 'tokenUrl' || key === 'x-xc-oauth2-clientAuthType' || key === 'x-xc-oauth2-clientId' || key === 'x-xc-oauth2-clientSecret') {
      return;
    }
    if (key === 'scopes') {
      params.scope = oauth2ConfigurationData[key].join(' ');
    }
    if (key === 'x-xc-oauth2-username') {
      params.username = oauth2ConfigurationData[key];
    }
    if (key === 'x-xc-oauth2-password') {
      params.password = oauth2ConfigurationData[key];
    }
    if (key === 'refreshUrl') {
      params.refresh_url = oauth2ConfigurationData[key];
    }
  });
  if (oauth2FlowType.value !== 'password' && oauth2FlowType.value !== 'clientCredentials') {
    try {
      // eslint-disable-next-line no-new
      new URL(oauth2ConfigurationData.tokenUrl);
      const uri = http.getURLSearchParams(params, true);
      window.open(oauth2ConfigurationData.tokenUrl + '?' + uri);
    } catch {
      notification.error('Authorization failed Couldn’t complete authentication.');
    }
    return;
  }

  if (oauth2FlowType.value === 'clientCredentials' || oauth2FlowType.value === 'password') {
    const validateRes = validateOauthData();
    if (!validateRes) {
      return;
    }
    try {
      let url:any = new URL(oauth2ConfigurationData.tokenUrl);
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
      if (oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] === 'REQUEST_BODY') {
        requestBody = {
          content: {
            'application/x-www-form-urlencoded': {
              schema: {
                type: 'object',
                properties: {
                  client_id: {
                    type: 'string',
                    [valueKey]: oauth2ConfigurationData['x-xc-oauth2-clientId']
                  },
                  client_secret: {
                    type: 'string',
                    [valueKey]: oauth2ConfigurationData['x-xc-oauth2-clientId']
                  }
                },
                [valueKey]: {
                  client_id: oauth2ConfigurationData['x-xc-oauth2-clientId'],
                  client_secret: oauth2ConfigurationData['x-xc-oauth2-clientSecret']
                }
              }
            }
          }
        };
      }
      if (oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] === 'QUERY_PARAMETER') {
        url.searchParams.append('client_id', oauth2ConfigurationData['x-xc-oauth2-clientId']);
        url.searchParams.append('client_secret', oauth2ConfigurationData['x-xc-oauth2-clientSecret']);
      }
      const header:any[] = [];
      if (oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] === 'BASIC_AUTH_HEADER') {
        header.push({ name: 'Authorization', in: 'header', schema: { type: 'string' }, [valueKey]: 'Basic ' + encode(oauth2ConfigurationData['x-xc-oauth2-clientId'], oauth2ConfigurationData['x-xc-oauth2-clientSecret']) });
      }
      if (requestBody) {
        header.push({ name: 'Content-Type', in: 'header', schema: { type: 'string' }, [valueKey]: 'application/x-www-form-urlencoded' });
      }
      url = url.toString();
      tokenUuid = utils.uuid('anthencation-token');
      if (webSocketConnection.value) {
        if (webSocketConnection.value.readyState !== 1) {
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
        webSocketConnection.value.send(JSON.stringify(params));
        return;
      }
      const headers: Record<string, string> = {};
      if (requestBody) {
        headers['Content-Type'] = 'application/x-www-form-urlencoded';
      }
      tokenResponseData.value = {};
      accessToken.value = undefined;
      if (header.length) {
        headers.Authorization = header[0][valueKey];
      }
      axios.post(url, requestBody?.['application/x-www-form-urlencoded'], {
        headers
      }).then(resp => {
        tokenResponseData.value = resp.data || {};
        const result = resp.data;
        for (const key in result) {
          if (key === 'access_token') {
            accessToken.value = result.access_token;
          }
          if (Object.prototype.toString.call(result[key]) === '[object Object]') {
            for (const subKey in result[key]) {
              if (subKey === 'access_token') {
                accessToken.value = result[key].access_token;
              }
            }
          }
        }
        handleOauth2ConfigurationChange();
      }).catch(error => {
        handleOauth2ConfigurationChange();
        notification.warning(error?.message);
      });
    } catch {
      notification.error('Authorization failed Couldn’t complete authentication.');
    }
  }
};

// 获取继承的 oauth 的 token
const fetchOauthToken = async (oauth2ConfigurationData) => {
  if (!oauth2ConfigurationData[newTokenKey] && oauth2ConfigurationData[oAuth2Token]) {
    return [{ access_token: oauth2ConfigurationData[oAuth2Token] }];
  }
  const authFlowType = oauth2ConfigurationData['x-xc-oauth2-authFlow'];
  const params: Record<string, string> = {
    grant_type: authFlowType
  };
  flowAuthKeys[authFlowType].forEach(key => {
    if (key === 'tokenUrl' || key === 'x-xc-oauth2-clientAuthType' || key === 'x-xc-oauth2-clientId' || key === 'x-xc-oauth2-clientSecret') {
      return;
    }
    if (key === 'scopes') {
      params.scope = oauth2ConfigurationData[key].join(' ');
    }
    if (key === 'x-xc-oauth2-username') {
      params.username = oauth2ConfigurationData[key];
    }
    if (key === 'x-xc-oauth2-password') {
      params.password = oauth2ConfigurationData[key];
    }
    if (key === 'refreshUrl') {
      params.refresh_url = oauth2ConfigurationData[key];
    }
  });
  if (oauth2FlowType.value === 'clientCredentials' || oauth2FlowType.value === 'password') {
    try {
      let url:any = new URL(oauth2ConfigurationData.tokenUrl);
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
      if (oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] === 'REQUEST_BODY') {
        requestBody = {
          content: {
            'application/x-www-form-urlencoded': {
              schema: {
                type: 'object',
                properties: {
                  client_id: {
                    type: 'string',
                    [valueKey]: oauth2ConfigurationData['x-xc-oauth2-clientId']
                  },
                  client_secret: {
                    type: 'string',
                    [valueKey]: oauth2ConfigurationData['x-xc-oauth2-clientId']
                  }
                },
                [valueKey]: {
                  client_id: oauth2ConfigurationData['x-xc-oauth2-clientId'],
                  client_secret: oauth2ConfigurationData['x-xc-oauth2-clientSecret']
                }
              }
            }
          }
        };
      }
      if (oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] === 'QUERY_PARAMETER') {
        url.searchParams.append('client_id', oauth2ConfigurationData['x-xc-oauth2-clientId']);
        url.searchParams.append('client_secret', oauth2ConfigurationData['x-xc-oauth2-clientSecret']);
      }
      const header:any[] = [];
      if (oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] === 'BASIC_AUTH_HEADER') {
        header.push({ name: 'Authorization', in: 'header', schema: { type: 'string' }, [valueKey]: 'Basic ' + encode(oauth2ConfigurationData['x-xc-oauth2-clientId'], oauth2ConfigurationData['x-xc-oauth2-clientSecret']) });
      }
      if (requestBody) {
        header.push({ name: 'Content-Type', in: 'header', schema: { type: 'string' }, [valueKey]: 'application/x-www-form-urlencoded' });
      }
      url = url.toString();
      // if (webSocketConnection.value) {
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
      //   webSocketConnection.value.send(JSON.stringify(params));
      //   return;
      // }
      const headers: Record<string, string> = {};
      if (requestBody) {
        headers['Content-Type'] = 'application/x-www-form-urlencoded';
      }
      tokenResponseData.value = {};
      accessToken.value = undefined;
      if (header.length) {
        headers.Authorization = header[0][valueKey];
      }
      const resp = await axios.post(url, requestBody?.['application/x-www-form-urlencoded'], {
        headers
      });
      tokenResponseData.value = resp.data || {};
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
  if (keys.every(key => key === 'refreshUrl' || !!oauth2ConfigurationData[key])) {
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
    if (data.authenticationScheme === 'basic') {
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
    authenticationType.value = 'extends';
    selectedInheritedSecurityId.value = newValue.$ref;
    return;
  }
  if (newValue?.authenticationScheme) {
    if (newValue.authenticationScheme === 'basic') {
      // authenticationScheme.value = newValue[valueKey];
      initHttpBasicData(newValue);
      authenticationType.value = 'basic';
    } else if (newValue.authenticationScheme === 'bearer') {
      authenticationType.value = 'bearer';
      authenticationScheme.value = newValue[valueKey];
      httpAuthenticationData[valueKey] = authenticationScheme.value;
    }
    return;
  }
  if (newValue?.type === 'oauth2') {
    authenticationType.value = 'oauth2';
    if (newValue[newTokenKey]) {
      oauth2ConfigurationMode.value = 2;
    }
    oauth2FlowType.value = newValue[oAuth2Key] || 'authorizationCode';
    if (newValue.flows) {
      oauth2ConfigurationData['x-xc-oauth2-clientAuthType'] = newValue.flows[oauth2FlowType.value]?.['x-xc-oauth2-clientAuthType'];
      flowAuthKeys[oauth2FlowType.value].forEach(i => {
        if (i === 'scopes') {
          oauth2ScopesObject = newValue.flows[oauth2FlowType.value]?.[i] || {};
          oauth2ConfigurationData[i] = Object.keys(oauth2ScopesObject);
        } else {
          oauth2ConfigurationData[i] = newValue.flows[oauth2FlowType.value]?.[i];
        }
      });
      authenticationScheme.value = newValue[oAuth2Token];
    }
    return;
  }
  if (newValue?.type === 'apiKey') {
    authenticationType.value = 'apiKey';
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
  switch (authenticationType.value) {
    case 'bearer':
      return [{
        Authorization: authenticationScheme.value
      }];
    case 'basic':
      return [{
        Authorization: { username: httpAuthenticationData.name, password: httpAuthenticationData[valueKey] }
      }];
    // case 'http-bearer':
    //   return [{
    //     Authorization: authenticationScheme.value
    //   }];
    case 'apiKey':
      return getApiKeyData(apiKeyConfigurationList.value);
    case 'extends':
      return await getExtendModel(dataSource.$ref);
    case 'oauth2':
      if (dataSource[newTokenKey]) {
        if (dataSource[oAuth2Key] === 'password') {
          if (dataSource.flows.password?.[oAuth2Token]) {
            return [{ access_token: dataSource.flows.password?.[oAuth2Token] }];
          } else if (validateOauthData(false)) {
            await fetchOauth2Token();
            const data = packageOauth2ConfigurationData();
            return getAuthData({ ...data, [newTokenKey]: false });
          }
        }
        if (dataSource[oAuth2Key] === 'clientCredentials') {
          if (dataSource.flows.clientCredentials?.[oAuth2Token]) {
            return [{ access_token: dataSource.flows.clientCredentials?.[oAuth2Token] }];
          } else if (validateOauthData(false)) {
            await fetchOauth2Token();
            const data = packageOauth2ConfigurationData();
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
        tokenResponseData.value = result;
        for (const key in result) {
          if (key === 'access_token') {
            accessToken.value = result.access_token;
          }
          if (Object.prototype.toString.call(result[key]) === '[object Object]') {
            for (const subKey in result[key]) {
              if (subKey === 'access_token') {
                accessToken.value = result[key].access_token;
              }
            }
          }
        }
      } else {
        const result = JSON.parse(response.response.rawContent);
        tokenResponseData.value = result;
      }
    } catch {
      notification.warning(response.response.rawContent);
    } finally {
      handleOauth2ConfigurationChange();
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
          v-model:value="authenticationType"
          :disabled="props.viewType"
          :options="authenticationTypeOptions"
          @change="handleAuthenticationTypeChange">
        </RadioGroup>
      </div>
    </div>
    <div class="min-h-50 py-2 text-3">
      <template v-if="authenticationType === 'extends'">
        <div class="flex items-center">
          <span class="mr-2">{{ t('xcan_apiAuthencation.selectServiceProjectSecurityAuth') }}</span>
          <Select
            v-model:value="selectedInheritedSecurityId"
            class="w-100"
            :readonly="props.viewType"
            :options="inheritedSecurityOptions"
            :fieldNames="{value: 'ref', label: 'key'}"
            @change="handleInheritedSecuritySelection">
          </Select>
        </div>
      </template>
      <template v-if="authenticationType==='basic'">
        <div class="flex items-center flex-grow flex-shrink mb-3">
          <span class="text-3 leading-3 text-theme-sub-content w-15 relative">
            <IconRequired class="absolute -left-2" />
            {{ t('common.username') }}
          </span>
          <Input
            :placeholder="t('xcan_apiAuthencation.enterUsername')"
            :value="httpAuthenticationData?.name"
            :readonly="props.viewType"
            size="small"
            class="w-100"
            :allowClear="true"
            @blur="handleHttpAuthenticationBlur($event, 'name')" />
        </div>
        <div class="flex items-center flex-grow flex-shrink">
          <span class="text-3 leading-3 text-theme-sub-content  w-15">{{ t('common.password') }}</span>
          <Input
            :placeholder="t('xcan_apiAuthencation.enterPassword')"
            :value="httpAuthenticationData?.[valueKey]"
            :allowClear="true"
            :readonly="props.viewType"
            type="password"
            class="w-100"
            size="small"
            @blur="handleHttpAuthenticationBlur($event, valueKey)" />
        </div>
      </template>
      <template v-if="authenticationType==='bearer'">
        <div class="flex items-center">
          <span class="w-15">{{ t('xcan_apiAuthencation.accessToken') }}</span>
          <Input
            :placeholder="t('xcan_apiAuthencation.enterToken')"
            :value="httpAuthenticationData?.[valueKey]"
            :allowClear="true"
            :readonly="props.viewType"
            class="w-100"
            size="small"
            @blur="handleHttpAuthenticationBlur($event, valueKey)" />
        </div>
      </template>
      <template v-if="authenticationType==='oauth2'">
        <div class="flex items-center mb-3">
          <span class="w-25">{{ t('xcan_apiAuthencation.configurationMethod') }}</span>
          <RadioGroup
            v-model:value="oauth2ConfigurationMode"
            :options="[{value: 1, label: t('xcan_apiAuthencation.existingToken')}, {value: 2, label: t('xcan_apiAuthencation.generateToken')}]"
            :disabled="props.viewType"
            @change="handleOauth2ConfigurationChange">
          </RadioGroup>
        </div>
        <template v-if="oauth2ConfigurationMode === 1">
          <div class="text-3 space-y-3">
            <div class="flex items-center">
              <span class="w-25">{{ t('xcan_apiAuthencation.accessToken') }}</span>
              <Input
                v-model:value="authenticationScheme"
                :allowClear="true"
                :readonly="props.viewType"
                class="w-100"
                @blur="handleOauth2ConfigurationChange" />
            </div>
          </div>
        </template>
        <template v-if="oauth2ConfigurationMode === 2">
          <div class="text-3 space-y-3">
            <div class="flex items-center">
              <span class="w-25">{{ t('common.type') }}</span>
              <Select
                v-model:value="oauth2FlowType"
                class="w-100"
                :readonly="props.viewType"
                :options="flowAuthType"
                @change="handleOauth2FlowTypeChange" />
            </div>
            <div
              v-for="item in oauth2FlowFieldLabels"
              :key="item.valueKey"
              :class="{'error': validate && !oauth2ConfigurationData[item.valueKey] && !['scopes', 'refreshUrl'].includes(item.valueKey)}"
              class="flex items-center">
              <span class="w-25">
                <IconRequired v-show="item.required" />{{ item.label }}
              </span>
              <Select
                v-if="item.valueKey === 'x-xc-oauth2-challengeMethod'"
                v-model:value="oauth2ConfigurationData[item.valueKey]"
                class="w-100"
                :readonly="props.viewType"
                :placeholder="item.maxLength ? t('xcan_apiAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                :options="encryptionTypeOpt"
                @change="handleOauth2ConfigurationChange" />
              <Select
                v-else-if="item.valueKey==='scopes'"
                v-model:value="oauth2ConfigurationData[item.valueKey]"
                :readonly="props.viewType"
                :placeholder="item.maxLength ? t('xcan_apiAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                class="w-100"
                mode="tags" />
              <Input
                v-else
                v-model:value="oauth2ConfigurationData[item.valueKey]"
                class="w-100"
                :readonly="props.viewType"
                :allowClear="true"
                :placeholder="item.maxLength ? t('xcan_apiAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                :maxLength="item.maxLength"
                @blur="handleOauth2ConfigurationChange" />
              <!-- <span
                v-if="item.maxLength"
                class="ml-2">
                最多可输入{{ item.maxLength }}个字符
              </span> -->
            </div>
            <div class="flex items-center">
              <span class="w-25">{{ t('xcan_apiAuthencation.clientAuthentication') }}</span>
              <SelectEnum
                v-model:value="oauth2ConfigurationData['x-xc-oauth2-clientAuthType']"
                :readonly="props.viewType"
                class="w-100"
                :enumKey="AuthClientIn"
                :lazy="false"
                @change="handleOauth2ConfigurationChange" />
            </div>
          </div>
          <div
            v-for="key in Object.keys(tokenResponseData)"
            :key="key"
            class="mt-2">
            <span>{{ key }}</span>: <span>{{ tokenResponseData[key] }}</span>
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
      <template v-if="authenticationType==='apiKey'">
        <div
          v-for="(item, index) in apiKeyConfigurationList"
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
              @blur="updateApiKeyConfiguration" />
            <Icon
              v-show="apiKeyConfigurationList.length > 1"
              icon="icon-qingchu"
              class="text-4 ml-1 cursor-pointer"
              @click="handleDeleteApiKeyConfiguration(index)" />
            <Icon
              icon="icon-jia"
              class="text-4 ml-1 cursor-pointer"
              @click="handleAddApiKeyConfiguration" />
          </div>
          <div class="flex items-center">
            <span class="w-15">{{ t('common.value') }}</span>
            <Input
              v-model:value="item[valueKey]"
              :allowClear="true"
              :readonly="props.viewType"
              class="w-100"
              @blur="updateApiKeyConfiguration" />
          </div>
          <div class="flex items-center">
            <span class="w-15">{{ t('common.position') }}</span>
            <Select
              v-model:value="item.in"
              class="w-100"
              :options="inOpt"
              :readonly="props.viewType"
              @change="updateApiKeyConfiguration" />
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
