<script setup lang="ts">
import { watch, ref, computed, reactive, inject, onMounted, nextTick } from 'vue';
import { Input, Select, IconRequired, Icon, SelectEnum, notification } from '@xcan-angus/vue-ui';
import { Button, RadioGroup } from 'ant-design-vue';
import { http, utils, TESTER, PoolType } from '@xcan-angus/infra';
import axios from 'axios';
import ApiUtils from 'src/utils/apis';
import { useI18n } from 'vue-i18n';

import { AuthItem, authTypeOptions as _authTypeOptions, flowAuthType, authLabels, flowAuthKeys, encryptionTypeOpt, inOpt, getAuthItem, getApiKeyData } from './interface';
const { t } = useI18n();
// import { API_EXTENSION_KEY, encode, getModelDataByRef } from '../_ApiUtils/index';

export interface Props {
  defaultValue:AuthItem,
  auth?: boolean;
  viewType: boolean;
}

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

const { API_EXTENSION_KEY, encode, decode } = ApiUtils;
const { valueKey, oAuth2Key } = API_EXTENSION_KEY;

const apiBaseInfo = inject('apiBaseInfo', ref());
const ws = inject('WS', ref());
let tokenUuid = '';
const authTypeOptions = computed(() => {
  return _authTypeOptions.filter(i => apiBaseInfo.value?.serviceId || i.value !== 'extends');
});

const type = ref<string | null>(null);
const authType = ref('clientCredentials');
const scheme = ref(); // http 类型下 值
const apiKeyContentList = ref<{name: string, in: string, value: string}[]>([getAuthItem()]); // apikey 类型下 值
const extendsSecurityOpt = ref<{id: string; type: string; model: string; key: string;}[]>([]);
const oauthKey = ref(1);

const tokenJson = ref({});
const token = ref();

/**
 * Reset authentication data to default values
 */
const resetData = () => {
  type.value = null;
  authType.value = 'clientCredentials';
  scheme.value = '';
  apiKeyContentList.value = [getAuthItem()];
  oauthKey.value = 1;
};

const flowauthLabel = computed(() => {
  return authLabels.filter(i => {
    return flowAuthKeys[authType.value].includes(i.valueKey);
  });
});

// 父级的安全认证
/**
 * Load project security configurations
 */
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
  authType.value = 'clientCredentials';
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
const handleBlur = (e, key:'name'|valueKey):void => {
  const value = e.target.value;
  switch (type.value) {
    case 'basic':
      httpAuthData[key] = value;
      scheme.value = 'Basic ' + encode(httpAuthData?.name, httpAuthData?.[valueKey]);
      emit('change', { type: 'http', value: scheme.value });
      break;
    case 'bearer':
      httpAuthData[valueKey] = value.startsWith('Bearer ') ? value : 'Bearer ' + value;
      scheme.value = httpAuthData[valueKey];
      emit('change', { type: 'http', value: scheme.value });
      break;
  }
  // emit('change', { type: 'http', [valueKey]: scheme.value, scheme: type.value });
};

const initHttpBasicData = () => {
  const decodeMap = decode((scheme.value || '').replace(/Basic\s+/, '')) as {name: string, value: string};
  httpAuthData.name = decodeMap.name;
  httpAuthData[valueKey] = decodeMap.value;
  // const { name = '', pssword = '' } = value || {};
  // httpAuthData.name = name || '';
  // httpAuthData.value = pssword || '';
};

// 继承方案
const handleSelectSecurity = (value) => {
  // const data = JSON.parse(option.model);
  emit('change', { $ref: value });
};

// apikey 方案
const initApiKeyContentList = (newValue) => {
  // const { name, in } = newValue;
  // const first = { name: newValue.name, in: newValue.in || 'header', [valueKey]: newValue[valueKey] };
  // const others = newValue[securityApiKeyPerfix] || [];
  // apiKeyContentList.value = [first, ...others];

  apiKeyContentList.value = newValue.apiKeys?.length ? newValue.apiKeys : [getAuthItem()];
};

const handleAddApiKey = () => {
  apiKeyContentList.value.push(getAuthItem());
};
const handleDelApiKey = (index) => {
  apiKeyContentList.value.splice(index, 1);
};

const changeApiKey = () => {
  // const { name } = apiKeyContentList.value.filter(i => i.name)[0] || {};
  // const lists = apiKeyContentList.value.slice(1);
  const data = {
    type: 'apiKey',
    apiKeys: apiKeyContentList.value
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
  clientId: undefined,
  clientSecret: undefined,
  callbackUrl: undefined,
  username: undefined,
  password: undefined,
  clientIn: 'REQUEST_BODY'
});

const pakageOauthData = () => {
  // if (oauthKey.value === 2) {
  const flowData = {
    clientIn: oauthData.clientIn,
    token: token.value
  };
  flowAuthKeys[authType.value].forEach(i => {
    flowData[i] = oauthData[i];
    // if (i === 'scopes') {
    //   const scopes = {};
    //   oauthData[i].forEach(key => {
    //     scopes[key] = scopesObj[key] || '';
    //   });
    //   flowData[i] = scopes;
    // } else {
    //   flowData[i] = oauthData[i];
    // }
  });
  const authKey = authType.value === 'authorizationCodePKCE' ? 'authorizationCode' : authType.value;
  const data = {
    type: 'oauth2',
    oauth2: {
      [authKey]: flowData,
      newToken: oauthKey.value === 2
    },
    value: scheme.value
    // flows: {
    //   ...props.defaultValue.flows,
    //   [authKey]: flowData
    // },
    // [oAuth2Key]: authType.value,
    // [oAuth2Token]: scheme.value,
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
  oauthData.clientIn = data?.clientIn || 'REQUEST_BODY';
  flowAuthKeys[authType.value].forEach(i => {
    // if (i === 'scopes') {
    //   scopesObj = data[i] || {};
    //   oauthData[i] = Object.keys(scopesObj);
    // } else {
    //   oauthData[i] = data[i];
    // }
    oauthData[i] = data[i];
  });
  onOauthChange();
};

// 获取令牌 密码模式
const fetchOauth2Token = async () => {
  const params: Record<string, string> = {
    grant_type: authType.value
  };
  flowAuthKeys[authType.value].forEach(key => {
    if (key === 'tokenUrl' || key === 'clientIn' || key === 'clientId' || key === 'clientSecret') {
      return;
    }
    if (key === 'scopes') {
      params.scope = oauthData[key].join(' ');
    }
    if (key === 'username') {
      params.username = oauthData[key];
    }
    if (key === 'password') {
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
      if (oauthData.clientIn === 'REQUEST_BODY') {
        requestBody = {
          content: {
            'application/x-www-form-urlencoded': {
              schema: {
                type: 'object',
                properties: {
                  client_id: {
                    type: 'string',
                    [valueKey]: oauthData.clientId
                  },
                  client_secret: {
                    type: 'string',
                    [valueKey]: oauthData.clientId
                  }
                },
                [valueKey]: {
                  client_id: oauthData.clientId,
                  client_secret: oauthData.clientSecret
                }
              }
            }
          }
        };
      }
      if (oauthData.clientIn === 'QUERY_PARAMETER') {
        url.searchParams.append('client_id', oauthData.clientId);
        url.searchParams.append('client_secret', oauthData.clientSecret);
      }
      const header:any[] = [];
      if (oauthData.clientIn === 'BASIC_AUTH_HEADER') {
        header.push({ name: 'Authorization', in: 'header', schema: { type: 'string' }, [valueKey]: 'Basic ' + encode(oauthData.clientId, oauthData.clientSecret) });
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

watch(() => props.defaultValue, (newValue) => {
  resetData();
  if (newValue?.$ref) {
    type.value = 'extends';
    extendsId.value = newValue.$ref;
    return;
  }

  if (newValue?.type === 'http') {
    if (newValue.value?.startsWith('Bearer')) {
      type.value = 'bearer';
      scheme.value = newValue.value;
      httpAuthData[valueKey] = scheme.value;
    } else {
      scheme.value = newValue[valueKey];
      initHttpBasicData();
      type.value = 'basic';
    }
    return;
  }
  if (newValue?.type === 'oauth2') {
    type.value = 'oauth2';
    if (newValue.oauth2?.newToken) {
      oauthKey.value = 2;
    }
    if (newValue.oauth2?.clientCredentials) {
      authType.value = 'clientCredentials';
    }

    if (newValue.oauth2?.password) {
      authType.value = 'passwords';
    }
    if (newValue.oauth2?.[authType.value]) {
      const _oauthData = newValue.oauth2?.[authType.value] || {};
      flowAuthKeys[authType.value].forEach(i => {
        oauthData[i] = _oauthData?.[i];
      });
    }
    if (!authType.value) {
      authType.value = 'clientCredentials';
    }
    scheme.value = newValue.value;
    // authType.value = newValue[oAuth2Key] || 'clientCredentials';
    // if (newValue.flows) {
    //   oauthData.clientIn = newValue.flows[authType.value]?.clientIn;
    //   flowAuthKeys[authType.value].forEach(i => {
    //     if (i === 'scopes') {
    //       scopesObj = newValue.flows[authType.value]?.[i] || {};
    //       oauthData[i] = Object.keys(scopesObj);
    //     } else {
    //       oauthData[i] = newValue.flows[authType.value]?.[i];
    //     }
    //   });
    //   scheme.value = newValue[oAuth2Token];
    // }
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

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

const getAuthData = async (dataSource) => {
  switch (type.value) {
    case 'bearer':
      return [{
        Authorization: scheme.value
      }];
    case 'basic':
      return [{
        Authorization: 'Basic ' + encode(httpAuthData.name, httpAuthData[valueKey])
      }];
    // case 'http-bearer':
    //   return [{
    //     Authorization: scheme.value
    //   }];
    case 'apiKey':
      return getApiKeyData(apiKeyContentList.value);
    // case 'extends':
    //   return await getExtendModel(dataSource.$ref);
    case 'oauth2':
      if (dataSource.newToken) {
        if (dataSource[oAuth2Key] === 'password') {
          if (dataSource.flows.password?.token) {
            return [{ access_token: dataSource.flows.password?.token }];
          } else if (validateOauthData(false)) {
            await fetchOauth2Token();
            const data = pakageOauthData();
            return getAuthData({ ...data, newToken: false });
          }
        }
        if (dataSource[oAuth2Key] === 'clientCredentials') {
          if (dataSource.flows.clientCredentials?.token) {
            return [{ access_token: dataSource.flows.clientCredentials?.token }];
          } else if (validateOauthData(false)) {
            await fetchOauth2Token();
            const data = pakageOauthData();
            return getAuthData({ ...data, newToken: false });
          }
        }
        return [{}];
      } else {
        if (dataSource.value) {
          return [{
            access_token: dataSource.value
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

const getModelResolve = () => {
  return models;
};

defineExpose({ getAuthData, onResponse, getModelResolve });
</script>
<template>
  <div class="rounded">
    <div class="text-3">
      <div class="flex items-center">
        <span class="mr-3">{{ t('xcan_execAuthencation.authenticationType') }}</span>
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
          <span class="mr-2">{{ t('xcan_execAuthencation.selectServiceProjectSecurityAuth') }}</span>
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
            {{ t('common.username') }}
          </span>
          <Input
            :placeholder="t('xcan_execAuthencation.enterUsername')"
            :value="httpAuthData?.name"
            :readonly="props.viewType"
            size="small"
            class="w-100"
            :allowClear="true"
            @blur="handleBlur($event, 'name')" />
        </div>
        <div class="flex items-center flex-grow flex-shrink">
          <span class="text-3 leading-3 text-theme-sub-content  w-15">{{ t('common.password') }}</span>
          <Input
            :placeholder="t('xcan_execAuthencation.enterPassword')"
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
          <span class="w-15">{{ t('xcan_execAuthencation.token') }}</span>
          <Input
            :placeholder="t('xcan_execAuthencation.enterToken')"
            :value="httpAuthData?.name"
            :allowClear="true"
            :readonly="props.viewType"
            class="w-100"
            size="small"
            @blur="handleBlur($event, 'name')" />
        </div>
      </template>
      <template v-if="type==='oauth2'">
        <div class="flex items-center mb-3">
          <span class="w-25">{{ t('xcan_execAuthencation.configurationMethod') }}</span>
          <RadioGroup
            v-model:value="oauthKey"
            :options="[{value: 1, label: t('xcan_execAuthencation.existingToken')}, {value: 2, label: t('xcan_execAuthencation.generateToken')}]"
            :disabled="props.viewType"
            @change="onOauthChange">
          </RadioGroup>
        </div>
        <template v-if="oauthKey === 1">
          <div class="text-3 space-y-3">
            <div class="flex items-center">
              <span class="w-25">{{ t('xcan_execAuthencation.accessToken') }}</span>
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
                v-if="item.valueKey === 'challengeMethod'"
                v-model:value="oauthData[item.valueKey]"
                class="w-100"
                :readonly="props.viewType"
                :placeholder="item.maxLength ? t('xcan_execAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                :options="encryptionTypeOpt"
                @change="onOauthChange" />
              <Select
                v-else-if="item.valueKey==='scopes'"
                v-model:value="oauthData[item.valueKey]"
                :readonly="props.viewType"
                :placeholder="item.maxLength ? t('xcan_execAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                class="w-100"
                mode="tags" />
              <Input
                v-else
                v-model:value="oauthData[item.valueKey]"
                class="w-100"
                :readonly="props.viewType"
                :allowClear="true"
                :placeholder="item.maxLength ? t('xcan_execAuthencation.maxCharacters', { maxLength: item.maxLength }) : ''"
                :maxLength="item.maxLength"
                @blur="onOauthChange" />
              <!-- <span
                v-if="item.maxLength"
                class="ml-2">
                最多可输入{{ item.maxLength }}个字符
              </span> -->
            </div>
            <div class="flex items-center">
              <span class="w-25">{{ t('xcan_execAuthencation.clientAuthentication') }}</span>
              <SelectEnum
                v-model:value="oauthData.clientIn"
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
            {{ t('xcan_execAuthencation.getToken') }}
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
            <span class="w-15">{{ t('xcan_execAuthencation.name') }}</span>
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
            <span class="w-15">{{ t('common.value') }}</span>
            <Input
              v-model:value="item.value"
              :allowClear="true"
              :readonly="props.viewType"
              class="w-100"
              @blur="changeApiKey" />
          </div>
          <div class="flex items-center">
            <span class="w-15">{{ t('common.position') }}</span>
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
