<script setup lang="ts">
import { computed, inject, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, IconRequired, Input, notification, Select } from '@xcan-angus/vue-ui';
import { Button, RadioGroup } from 'ant-design-vue';
import { encode } from '@/utils/secure';
import { http, utils, AuthClientIn } from '@xcan-angus/infra';
import axios from 'axios';
import { services } from '@/api/tester';
import { HTTP_HEADERS, CONTENT_TYPE_KEYS } from '@/utils/constant';
import { QueryAndHeaderInOption } from '@/utils/apis';
import { API_EXTENSION_KEYS, OAuth2GrantType } from '@/types/openapi-types';

import {
  AuthenticationItem, oauth2FieldLabels, authenticationTypeOptions as _authenticationTypeOptions,
  encryptionTypeOptions, oauth2FlowFieldMappings, oauth2FlowTypeOptions, extractApiKeyAuthenticationData,
  createDefaultAuthItem, extractDisplayApiKeyData
} from './Authorization';

import SelectEnum from '@/components/enum/SelectEnum.vue';

/**
 * Component props interface
 * <p>
 * Defines the structure for component props including authentication configuration and WebSocket connection
 * </p>
 */
interface Props {
  defaultValue: AuthenticationItem;
  auth?: boolean;
  ws?: WebSocket;
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: undefined
});

const { t } = useI18n();

/**
 * Component emits interface
 * <p>
 * Defines the events that this component can emit
 * </p>
 */
const emits = defineEmits<{
  (e: 'change', value: AuthenticationItem): void;
  (e: 'update:auth', value: boolean): void;
}>();

const { valueKey, securityApiKeyPrefix, oAuth2Key, oAuth2Token, newTokenKey, basicAuthKey } = API_EXTENSION_KEYS;

/**
 * Injected API base information
 */
const apiBaseInfo = inject('apiBaseInfo', ref());

/**
 * Computed authentication type options
 * <p>
 * Filters available authentication types based on service configuration
 * </p>
 */
const authenticationTypeOptions = computed(() => {
  return _authenticationTypeOptions.filter(option =>
    apiBaseInfo.value?.serviceId || option.value !== 'extends'
  );
});

let tokenRequestId = '';
let oauth2ScopesObject = {};

const currentAuthenticationType = ref<string | null>(null);
const oauth2FlowType = ref('authorizationCode');
const httpAuthenticationScheme = ref();
const apiKeyContentList = ref<{name: string, in: string, [valueKey]: string}[]>([createDefaultAuthItem()]);
const extendedSecurityOptions = ref<{id: string; type: string; model: string; key: string;}[]>([]);
const oauth2ConfigurationKey = ref(1);
const tokenResponseJson = ref({});
const accessToken = ref();
const validate = ref(false);
const models = {};

/**
 * Legacy variable aliases for compatibility
 */
const type = currentAuthenticationType;
const oauthKey = oauth2ConfigurationKey;
const authType = oauth2FlowType;
const scheme = httpAuthenticationScheme;
const tokenJson = tokenResponseJson;
const token = accessToken;
const tokenUuid = tokenRequestId;

/**
 * Computed OAuth2 flow field labels
 * <p>
 * Filters OAuth2 field labels based on the current flow type
 * </p>
 */
const oauth2FlowFieldLabels = computed(() => {
  return oauth2FieldLabels.filter(field => {
    return oauth2FlowFieldMappings[oauth2FlowType.value].includes(field.valueKey);
  });
});

/**
 * Loads service security configuration
 * <p>
 * Fetches extended security options for the current service
 * </p>
 */
const loadServiceSecurityConfiguration = async () => {
  const [error, response] = await services.getCompData(
    apiBaseInfo.value.serviceId,
    ['securitySchemes'],
    [],
    false
  );
  if (error) {
    return;
  }
  extendedSecurityOptions.value = response.data || [];
};

/**
 * HTTP authentication data
 */
const httpAuthenticationData = reactive({
  name: '',
  value: ''
});

/**
 * Selected extended security ID
 */
const selectedExtendedSecurityId = ref();

/**
 * Handles authentication type change
 * <p>
 * Resets authentication data when the type changes and emits appropriate events
 * </p>
 */
const handleAuthenticationTypeChange = () => {
  httpAuthenticationData.name = '';
  httpAuthenticationData.value = '';
  httpAuthenticationScheme.value = '';
  selectedExtendedSecurityId.value = '';
  oauth2FlowType.value = 'authorizationCode';
  emits('change', { type: currentAuthenticationType.value } as AuthenticationItem);
  if (currentAuthenticationType.value) {
    emits('update:auth', true);
  } else {
    emits('update:auth', false);
  }
};

/**
 * Handles HTTP authentication field blur event
 * <p>
 * Processes HTTP authentication data based on the authentication type
 * </p>
 * @param event - Change event from input field
 * @param fieldKey - Field key ('name' or 'value')
 */
const handleHttpAuthenticationFieldBlur = (event: ChangeEvent, fieldKey: 'name' | 'value'): void => {
  const fieldValue = event.target.value || '';
  switch (currentAuthenticationType.value) {
    case 'basic':
      httpAuthenticationData[fieldKey] = fieldValue;
      emits('change', {
        type: 'http',
        [basicAuthKey]: httpAuthenticationData
      });
      break;
    case 'bearer':
      httpAuthenticationData.value = fieldValue.startsWith('Bearer ') ? fieldValue : 'Bearer ' + fieldValue;
      httpAuthenticationScheme.value = httpAuthenticationData.value;
      emits('change', {
        type: 'http',
        [valueKey]: httpAuthenticationScheme.value,
        scheme: currentAuthenticationType.value
      });
      break;
  }
};

/**
 * Initializes HTTP basic authentication data
 * <p>
 * Sets up username and password for basic authentication
 * </p>
 * @param credentials - Object containing username and password
 */
const initializeHttpBasicAuthenticationData = (credentials: {username: string; password: string}) => {
  const { username = '', password = '' } = credentials || {};
  httpAuthenticationData.name = username || '';
  httpAuthenticationData.value = password || '';
};

/**
 * Handles extended security selection
 * <p>
 * Emits change event when extended security option is selected
 * </p>
 * @param securityId - Selected security ID
 */
const handleExtendedSecuritySelection = (securityId: string) => {
  emits('change', { $ref: securityId } as any);
};

/**
 * Initializes API key content list
 * <p>
 * Sets up the API key content list with primary and additional keys
 * </p>
 * @param apiKeyData - API key data object
 */
const initializeApiKeyContentList = (apiKeyData: any) => {
  const primaryKey = {
    name: apiKeyData.name,
    in: apiKeyData.in || 'header',
    [valueKey]: apiKeyData[valueKey]
  };
  const additionalKeys = apiKeyData[securityApiKeyPrefix] || [];
  apiKeyContentList.value = [primaryKey, ...additionalKeys];
};

/**
 * Adds a new API key to the list
 * <p>
 * Appends a new default API key item to the content list
 * </p>
 */
const addApiKeyItem = () => {
  apiKeyContentList.value.push(createDefaultAuthItem());
};

/**
 * Removes an API key from the list
 * <p>
 * Removes the API key at the specified index
 * </p>
 * @param index - Index of the API key to remove
 */
const removeApiKeyItem = (index: number) => {
  apiKeyContentList.value.splice(index, 1);
};

/**
 * Handles API key configuration change
 * <p>
 * Processes API key data and emits change event with updated configuration
 * </p>
 */
const handleApiKeyConfigurationChange = () => {
  const primaryKey = apiKeyContentList.value.filter(item => item.name)[0] || {};
  const additionalKeys = apiKeyContentList.value.slice(1);
  const apiKeyConfiguration = {
    type: 'apiKey',
    name: primaryKey.name,
    in: apiKeyContentList.value[0].in,
    [valueKey]: apiKeyContentList.value[0][valueKey],
    [securityApiKeyPrefix]: additionalKeys.length ? additionalKeys : undefined
  };
  emits('change', apiKeyConfiguration as any);
};

/**
 * OAuth2 authentication data
 * <p>
 * Reactive object containing OAuth2 configuration parameters
 * </p>
 */
const oauth2AuthenticationData = reactive({
  authorizationUrl: undefined,
  tokenUrl: undefined,
  refreshUrl: undefined,
  scopes: [],
  [API_EXTENSION_KEYS.oAuth2ClientIdKey]: undefined,
  [API_EXTENSION_KEYS.oAuth2ClientSecretKey]: undefined,
  [API_EXTENSION_KEYS.oAuth2CallbackUrlKey]: undefined,
  [API_EXTENSION_KEYS.oAuth2UsernameKey]: undefined,
  [API_EXTENSION_KEYS.oAuth2PasswordKey]: undefined,
  [API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey]: AuthClientIn.REQUEST_BODY
});

/**
 * Packages OAuth2 authentication data
 * <p>
 * Prepares OAuth2 configuration data for API requests
 * </p>
 * @returns Packaged OAuth2 authentication data
 */
const packageOAuth2AuthenticationData = () => {
  const oauth2FlowData = {
    [API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey]: oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey],
    [API_EXTENSION_KEYS.oAuth2Token]: accessToken.value
  };
  oauth2FlowFieldMappings[oauth2FlowType.value].forEach(fieldKey => {
    if (fieldKey === 'scopes') {
      const scopesObject = {};
      oauth2AuthenticationData[fieldKey].forEach(scopeKey => {
        scopesObject[scopeKey] = oauth2ScopesObject[scopeKey] || '';
      });
      oauth2FlowData[fieldKey] = scopesObject;
    } else {
      oauth2FlowData[fieldKey] = oauth2AuthenticationData[fieldKey];
    }
  });
  const authenticationKey = oauth2FlowType.value === 'authorizationCodePKCE' ? 'authorizationCode' : oauth2FlowType.value;
  return {
    type: 'oauth2',
    flows: {
      ...props.defaultValue.flows,
      [authenticationKey]: oauth2FlowData
    },
    [oAuth2Key]: oauth2FlowType.value,
    [oAuth2Token]: httpAuthenticationScheme.value,
    [newTokenKey]: oauth2ConfigurationKey.value === 2
  };
};

/**
 * Handles OAuth2 configuration change
 * <p>
 * Emits change event when OAuth2 configuration is updated
 * </p>
 */
const handleOAuth2ConfigurationChange = () => {
  const oauth2Data = packageOAuth2AuthenticationData();
  emits('change', oauth2Data as any);
};

/**
 * Handles OAuth2 flow type change
 * <p>
 * Updates OAuth2 configuration when flow type changes
 * </p>
 * @param authFlow - Authentication flow type
 */
const handleOAuth2FlowTypeChange = (authFlow: string) => {
  const flowData = props.defaultValue.flows?.[authFlow] || {};
  tokenResponseJson.value = {};
  accessToken.value = undefined;
  validate.value = false;
  oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] = flowData?.[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] || AuthClientIn.REQUEST_BODY;
  oauth2FlowFieldMappings[oauth2FlowType.value].forEach(fieldKey => {
    if (fieldKey === 'scopes') {
      oauth2ScopesObject = flowData[fieldKey] || {};
      oauth2AuthenticationData[fieldKey] = Object.keys(oauth2ScopesObject);
    } else {
      oauth2AuthenticationData[fieldKey] = flowData[fieldKey];
    }
  });
  handleOAuth2ConfigurationChange();
};

/**
 * Fetches OAuth2 access token
 * <p>
 * Retrieves OAuth2 access token using the configured flow type
 * </p>
 */
const fetchOAuth2AccessToken = async () => {
  const requestParams: Record<string, string> = {
    grant_type: oauth2FlowType.value
  };
  oauth2FlowFieldMappings[oauth2FlowType.value].forEach(fieldKey => {
    if (fieldKey === 'tokenUrl' || fieldKey === API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey || fieldKey === API_EXTENSION_KEYS.oAuth2ClientIdKey || fieldKey === API_EXTENSION_KEYS.oAuth2ClientSecretKey) {
      return;
    }
    if (fieldKey === 'scopes') {
      requestParams.scope = oauth2AuthenticationData[fieldKey].join(' ');
    }
    if (fieldKey === API_EXTENSION_KEYS.oAuth2UsernameKey) {
      requestParams.username = oauth2AuthenticationData[fieldKey];
    }
    if (fieldKey === API_EXTENSION_KEYS.oAuth2PasswordKey) {
      requestParams.password = oauth2AuthenticationData[fieldKey];
    }
    if (fieldKey === 'refreshUrl') {
      requestParams.refresh_url = oauth2AuthenticationData[fieldKey];
    }
  });
  if (oauth2FlowType.value !== 'password' && oauth2FlowType.value !== 'clientCredentials') {
    try {
      // eslint-disable-next-line no-new
      new URL(oauth2AuthenticationData.tokenUrl || '');
      const queryString = http.getURLSearchParams(requestParams, true);
      window.open(oauth2AuthenticationData.tokenUrl + '?' + queryString);
    } catch {
      notification.error('Authorization failed Couldn\'t complete authentication.');
    }
    return;
  }

  if (oauth2FlowType.value === 'clientCredentials' || oauth2FlowType.value === 'password') {
    const validationResult = validateOAuth2Data();
    if (!validationResult) {
      return;
    }
    try {
      const tokenUrl: any = new URL(oauth2AuthenticationData.tokenUrl || '');
      for (const paramKey in requestParams) {
        if (requestParams[paramKey]) {
          if (paramKey === 'grant_type' && requestParams[paramKey] === 'clientCredentials') {
            tokenUrl.searchParams.append(paramKey, OAuth2GrantType.CLIENT_CREDENTIALS);
          } else {
            tokenUrl.searchParams.append(paramKey, requestParams[paramKey]);
          }
        }
      }
      let requestBodyData;
      if (oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] === AuthClientIn.REQUEST_BODY) {
        requestBodyData = {
          content: {
            'application/x-www-form-urlencoded': {
              schema: {
                type: 'object',
                properties: {
                  client_id: {
                    type: 'string',
                    [valueKey]: oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientIdKey]
                  },
                  client_secret: {
                    type: 'string',
                    [valueKey]: oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]
                  }
                },
                [valueKey]: {
                  client_id: oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientIdKey],
                  client_secret: oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]
                }
              }
            }
          }
        };
      }
      if (oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] === AuthClientIn.QUERY_PARAMETER) {
        tokenUrl.searchParams.append('client_id', oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientIdKey]);
        tokenUrl.searchParams.append('client_secret', oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]);
      }
      const requestHeaders: any[] = [];
      if (oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] === AuthClientIn.BASIC_AUTH_HEADER) {
        requestHeaders.push({
          name: HTTP_HEADERS.AUTHORIZATION,
          in: 'header',
          schema: { type: 'string' },
          [valueKey]: 'basic ' + encode(
            oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientIdKey],
            oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]
          )
        });
      }
      if (requestBodyData) {
        requestHeaders.push({
          name: HTTP_HEADERS.CONTENT_TYPE,
          in: 'header',
          schema: { type: 'string' },
          [valueKey]: CONTENT_TYPE_KEYS.FORM_URLENCODED
        });
      }
      const tokenUrlString = tokenUrl.toString();
      tokenRequestId = utils.uuid('authentication-token');
      if (props.ws) {
        if (props.ws.readyState !== 1) {
          notification.error(t('service.apiAuthorization.messages.proxyNotConnected'));
          return;
        }
        const websocketParams = {
          method: 'post',
          server: { url: tokenUrlString },
          endpoint: '',
          requestId: tokenRequestId,
          messageType: 'HttpRequestProxy',
          requestBody: requestBodyData,
          parameters: requestHeaders
        };
        if (!requestBodyData) {
          delete websocketParams.requestBody;
        }
        props.ws.send(JSON.stringify(websocketParams));
        return;
      }
      const axiosHeaders: Record<string, string> = {};
      if (requestBodyData) {
        axiosHeaders[HTTP_HEADERS.CONTENT_TYPE] = CONTENT_TYPE_KEYS.FORM_URLENCODED;
      }
      tokenResponseJson.value = {};
      accessToken.value = undefined;
      if (requestHeaders.length) {
        axiosHeaders.Authorization = requestHeaders[0][valueKey];
      }
      axios.post(tokenUrlString, requestBodyData?.[CONTENT_TYPE_KEYS.FORM_URLENCODED], {
        headers: axiosHeaders
      }).then(response => {
        tokenResponseJson.value = response.data || {};
        const responseData = response.data;
        for (const key in responseData) {
          if (key === 'access_token') {
            accessToken.value = responseData.access_token;
          }
          if (Object.prototype.toString.call(responseData[key]) === '[object Object]') {
            for (const subKey in responseData[key]) {
              if (subKey === 'access_token') {
                accessToken.value = responseData[key].access_token;
              }
            }
          }
        }
        handleOAuth2ConfigurationChange();
      }).catch(error => {
        handleOAuth2ConfigurationChange();
        notification.warning(error.message);
      });
    } catch {
      notification.error('Authorization failed Couldn\'t complete authentication.');
    }
  }
};

// 获取继承的 oauth 的 token
const fetchOauthToken = async (oauthData) => {
  if (!oauthData[newTokenKey] && oauthData[oAuth2Token]) {
    return [{ access_token: oauthData[oAuth2Token] }];
  }
  const authFlowType = oauthData[API_EXTENSION_KEYS.oAuth2Key];
  const params: Record<string, string> = {
    grant_type: authFlowType
  };
  flowAuthKeys[authFlowType].forEach(key => {
    if (key === 'tokenUrl' ||
      key === API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey ||
      key === API_EXTENSION_KEYS.oAuth2ClientIdKey ||
      key === API_EXTENSION_KEYS.oAuth2ClientSecretKey) {
      return;
    }
    if (key === 'scopes') {
      params.scope = oauthData[key].join(' ');
    }
    if (key === API_EXTENSION_KEYS.oAuth2UsernameKey) {
      params.username = oauthData[key];
    }
    if (key === API_EXTENSION_KEYS.oAuth2PasswordKey) {
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
            url.searchParams.append(key, OAuth2GrantType.CLIENT_CREDENTIALS);
          } else {
            url.searchParams.append(key, params[key]);
          }
        }
      }
      let requestBody;
      if (oauthData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] === AuthClientIn.REQUEST_BODY) {
        requestBody = {
          content: {
            'application/x-www-form-urlencoded': {
              schema: {
                type: 'object',
                properties: {
                  client_id: {
                    type: 'string',
                    [valueKey]: oauthData[API_EXTENSION_KEYS.oAuth2ClientIdKey]
                  },
                  client_secret: {
                    type: 'string',
                    [valueKey]: oauthData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]
                  }
                },
                [valueKey]: {
                  client_id: oauthData[API_EXTENSION_KEYS.oAuth2ClientIdKey],
                  client_secret: oauthData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]
                }
              }
            }
          }
        };
      }
      if (oauthData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] === AuthClientIn.QUERY_PARAMETER) {
        url.searchParams.append('client_id', oauthData[API_EXTENSION_KEYS.oAuth2ClientIdKey]);
        url.searchParams.append('client_secret', oauthData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]);
      }
      const header:any[] = [];
      if (oauthData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] === AuthClientIn.BASIC_AUTH_HEADER) {
        header.push({
          name: HTTP_HEADERS.AUTHORIZATION,
          in: 'header',
          schema: { type: 'string' },
          [valueKey]: 'basic ' + encode(
            oauthData[API_EXTENSION_KEYS.oAuth2ClientIdKey],
            oauthData[API_EXTENSION_KEYS.oAuth2ClientSecretKey]
          )
        });
      }
      if (requestBody) {
        header.push({
          name: HTTP_HEADERS.CONTENT_TYPE,
          in: 'header',
          schema: { type: 'string' },
          [valueKey]: CONTENT_TYPE_KEYS.FORM_URLENCODED
        });
      }
      url = url.toString();
      const headers: Record<string, string> = {};
      if (requestBody) {
        headers[HTTP_HEADERS.CONTENT_TYPE] = CONTENT_TYPE_KEYS.FORM_URLENCODED;
      }
      tokenJson.value = {};
      token.value = undefined;
      if (header.length) {
        headers.Authorization = header[0][valueKey];
      }
      const resp = await axios.post(url, requestBody?.[CONTENT_TYPE_KEYS.FORM_URLENCODED], {
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

/**
 * Validates OAuth2 data
 * <p>
 * Checks if all required OAuth2 fields are properly filled
 * </p>
 * @param showValidate - Whether to show validation errors
 * @returns True if validation passes, false otherwise
 */
const validateOAuth2Data = (showValidate = true) => {
  if (showValidate) {
    validate.value = true;
  }
  const requiredKeys = oauth2FlowFieldLabels.value.map(field => field.valueKey);
  return requiredKeys.every(key => key === 'refreshUrl' || !!oauth2AuthenticationData[key]);
};

/**
 * Legacy method aliases for compatibility
 */
const flowAuthKeys = oauth2FlowFieldMappings;
const initHttpBasicData = initializeHttpBasicAuthenticationData;
const initApiKeyContentList = initializeApiKeyContentList;
const onOauthChange = handleOAuth2ConfigurationChange;
const loadServiceSecurity = loadServiceSecurityConfiguration;
const getApiKeyData = extractApiKeyAuthenticationData;
const getShowApiKeyData = extractDisplayApiKeyData;

const oauthData = oauth2AuthenticationData;
const httpAuthData = httpAuthenticationData;
const extendsId = selectedExtendedSecurityId;

const getExtendModel = async (ref) => {
  if (!ref) {
    return [{}];
  }
  const [error, resp] = await services.getComponentRef(apiBaseInfo.value?.serviceId, ref);
  if (error) {
    return [{}];
  }
  models[ref] = resp.data.model;
  const data = JSON.parse(resp.data.model);
  if (data.type === 'http') {
    if (data.scheme === 'basic') {
      const { password, username } = data[basicAuthKey];
      return [{ Authorization: encode(username, password) }];
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
        tokenJson.value = JSON.parse(response.response.rawContent);
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

const getAuthData = async (dataSource) => {
  switch (type.value) {
    case 'bearer':
      return [{
        Authorization: scheme.value
      }];
    case 'basic':
      return [{
        Authorization: { username: httpAuthData.name, password: httpAuthData.value }
      }];
    case 'apiKey':
      return getApiKeyData(apiKeyContentList.value);
    case 'extends':
      return await getExtendModel(dataSource.$ref);
    case 'oauth2':
      if (dataSource[newTokenKey]) {
        if (dataSource[oAuth2Key] === 'password') {
          if (dataSource.flows.password?.[API_EXTENSION_KEYS.oAuth2Token]) {
            return [{ access_token: dataSource.flows.password?.[API_EXTENSION_KEYS.oAuth2Token] }];
          } else if (validateOAuth2Data(false)) {
            await fetchOAuth2AccessToken();
            const oauth2Data = packageOAuth2AuthenticationData();
            return getAuthData({ ...oauth2Data, [newTokenKey]: false });
          }
        }
        if (dataSource[oAuth2Key] === 'clientCredentials') {
          if (dataSource.flows.clientCredentials?.[API_EXTENSION_KEYS.oAuth2Token]) {
            return [{ access_token: dataSource.flows.clientCredentials?.[API_EXTENSION_KEYS.oAuth2Token] }];
          } else if (validateOAuth2Data(false)) {
            await fetchOAuth2AccessToken();
            const oauth2Data = packageOAuth2AuthenticationData();
            return getAuthData({ ...oauth2Data, [newTokenKey]: false });
          }
        }
        return [{}];
      } else {
        if (dataSource[API_EXTENSION_KEYS.oAuth2Token]) {
          return [{
            access_token: dataSource[API_EXTENSION_KEYS.oAuth2Token]
          }];
        } else {
          return [{}];
        }
      }
  }
};

watch(() => props.defaultValue, (newValue) => {
  if ((newValue as any)?.$ref) {
    type.value = 'extends';
    extendsId.value = (newValue as any).$ref;
    return;
  }
  if (newValue?.scheme) {
    if (newValue.scheme === 'basic') {
      // scheme.value = newValue[valueKey];
      initHttpBasicData(newValue[basicAuthKey]);
      type.value = 'basic';
    } else if (newValue.scheme === 'bearer') {
      type.value = 'bearer';
      scheme.value = newValue[valueKey];
      httpAuthData.name = scheme.value;
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
      oauthData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey] = newValue.flows[authType.value]?.[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey];
      flowAuthKeys[authType.value].forEach(i => {
        if (i === 'scopes') {
          oauth2ScopesObject = newValue.flows?.[authType.value]?.[i] || {};
          oauthData[i] = Object.keys(oauth2ScopesObject);
        } else {
          oauthData[i] = newValue.flows?.[authType.value]?.[i];
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
    loadServiceSecurity();
  }
}, {
  immediate: true
});

defineExpose({ getAuthData, onResponse, getModelResolve });
</script>
<template>
  <div class="rounded">
    <div class="p-2 text-3">
      <div class="flex items-center">
        <span class="mr-3">{{ t('service.apiAuthorization.title') }}</span>
        <RadioGroup
          v-model:value="currentAuthenticationType"
          :options="authenticationTypeOptions"
          @change="handleAuthenticationTypeChange">
        </RadioGroup>
      </div>
    </div>
    <div class="min-h-50 p-2 text-3">
      <template v-if="currentAuthenticationType === 'extends'">
        <div class="flex items-center">
          <span class="mr-2">{{ t('service.apiAuthorization.extends.selectServiceSecurity') }}</span>
          <Select
            v-model:value="selectedExtendedSecurityId"
            class="w-100"
            :options="extendedSecurityOptions"
            :fieldNames="{value: 'ref', label: 'key'}"
            @change="(value) => handleExtendedSecuritySelection(value as string)">
          </Select>
        </div>
      </template>
      <template v-if="currentAuthenticationType==='basic'">
        <div class="flex items-center flex-grow flex-shrink mb-3">
          <span class="text-3 leading-3 w-15 relative">
            <IconRequired class="absolute -left-2" />
            {{ t('common.username') }}
          </span>
          <Input
            :placeholder="t('service.apiAuthorization.basic.usernamePlaceholder')"
            :value="httpAuthenticationData?.name"
            size="small"
            class="w-100"
            :allowClear="true"
            @blur="handleHttpAuthenticationFieldBlur($event, 'name')" />
        </div>
        <div class="flex items-center flex-grow flex-shrink">
          <span class="text-3 leading-3 w-15">{{ t('common.password') }}</span>
          <Input
            :placeholder="t('service.apiAuthorization.basic.passwordPlaceholder')"
            :value="httpAuthenticationData?.value"
            :allowClear="true"
            type="password"
            class="w-100"
            size="small"
            @blur="handleHttpAuthenticationFieldBlur($event, 'value')" />
        </div>
      </template>
      <template v-if="currentAuthenticationType==='bearer'">
        <div class="flex items-center">
          <span class="w-15">{{ t('service.apiAuthorization.bearer.token') }}</span>
          <Input
            :placeholder="t('service.apiAuthorization.bearer.tokenPlaceholder')"
            :value="httpAuthenticationData?.name"
            :allowClear="true"
            class="w-100"
            size="small"
            @blur="handleHttpAuthenticationFieldBlur($event, 'name')" />
        </div>
      </template>
      <template v-if="currentAuthenticationType==='oauth2'">
        <div class="flex items-center mb-3">
          <span class="w-25">{{ t('service.apiAuthorization.oauth2.configMethod') }}</span>
          <RadioGroup
            v-model:value="oauth2ConfigurationKey"
            :options="[
              {value: 1, label: t('service.apiAuthorization.oauth2.existingToken')},
              {value: 2, label: t('service.apiAuthorization.oauth2.generateToken')}
            ]">
          </RadioGroup>
        </div>
        <template v-if="oauth2ConfigurationKey === 1">
          <div class="text-3 space-y-3">
            <div class="flex items-center">
              <span class="w-25">{{ t('service.apiAuthorization.oauth2.accessToken') }}</span>
              <Input
                v-model:value="httpAuthenticationScheme"
                :allowClear="true"
                class="w-100"
                @blur="handleOAuth2ConfigurationChange" />
            </div>
          </div>
        </template>
        <template v-if="oauth2ConfigurationKey === 2">
          <div class="text-3 space-y-3">
            <div class="flex items-center">
              <span class="w-25">{{ t('common.type') }}</span>
              <Select
                v-model:value="oauth2FlowType"
                class="w-100"
                :options="oauth2FlowTypeOptions"
                @change="(value) => handleOAuth2FlowTypeChange(value as string)" />
            </div>
            <div
              v-for="item in oauth2FlowFieldLabels"
              :key="item.valueKey"
              :class="{'error': validate && !oauth2AuthenticationData[item.valueKey] && !['scopes', 'refreshUrl'].includes(item.valueKey)}"
              class="flex items-center">
              <span class="w-25">
                <IconRequired v-show="item.required" />{{ t(item.label) }}
              </span>
              <Select
                v-if="item.valueKey === 'x-xc-oauth2-challengeMethod'"
                v-model:value="oauth2AuthenticationData[item.valueKey]"
                class="w-100"
                :placeholder="item.maxLength ? t('service.apiAuthorization.messages.maxLengthTip', { maxLength: item.maxLength }) : ''"
                :options="encryptionTypeOptions"
                @change="handleOAuth2ConfigurationChange" />
              <Select
                v-else-if="item.valueKey==='scopes'"
                v-model:value="oauth2AuthenticationData[item.valueKey]"
                :placeholder="item.maxLength ? t('service.apiAuthorization.messages.maxLengthTip', { maxLength: item.maxLength }) : ''"
                class="w-100"
                mode="tags" />
              <Input
                v-else
                v-model:value="oauth2AuthenticationData[item.valueKey]"
                class="w-100"
                :allowClear="true"
                :placeholder="item.maxLength ? t('service.apiAuthorization.messages.maxLengthTip', { maxLength: item.maxLength }) : ''"
                :maxLength="item.maxLength"
                @blur="handleOAuth2ConfigurationChange" />
            </div>
            <div class="flex items-center">
              <span class="w-25">{{ t('service.apiAuthorization.oauth2.clientAuth') }}</span>
              <SelectEnum
                v-model:value="oauth2AuthenticationData[API_EXTENSION_KEYS.oAuth2ClientAuthTypeKey]"
                class="w-100"
                enumKey="AuthClientIn"
                :lazy="false"
                @change="handleOAuth2ConfigurationChange" />
            </div>
          </div>
          <div
            v-for="key in Object.keys(tokenResponseJson)"
            :key="key"
            class="mt-2">
            <span>{{ key }}</span>: <span>{{ tokenResponseJson[key] }}</span>
          </div>
          <Button
            type="primary"
            size="small"
            class="ml-25 mt-3"
            @click="fetchOAuth2AccessToken">
            {{ t('service.apiAuthorization.oauth2.getToken') }}
          </Button>
        </template>
      </template>
      <template v-if="currentAuthenticationType==='apiKey'">
        <div
          v-for="(item, index) in apiKeyContentList"
          :key="index"
          class="space-y-2 mb-4">
          <div class="flex items-center">
            <span class="w-15">{{ t('common.name') }}</span>
            <Input
              v-model:value="item.name"
              class="w-100"
              :maxLength="100"
              :allowClear="true"
              dataType="mixin-en"
              includes="-_"
              @blur="handleApiKeyConfigurationChange" />
            <Icon
              v-show="apiKeyContentList.length > 1"
              icon="icon-qingchu"
              class="text-4 ml-1 cursor-pointer"
              @click="removeApiKeyItem(index)" />
            <Icon
              icon="icon-jia"
              class="text-4 ml-1 cursor-pointer"
              @click="addApiKeyItem" />
          </div>
          <div class="flex items-center">
            <span class="w-15">{{ t('common.value') }}</span>
            <Input
              v-model:value="item[valueKey]"
              :allowClear="true"
              class="w-100"
              @blur="handleApiKeyConfigurationChange" />
          </div>
          <div class="flex items-center">
            <span class="w-15">{{ t('common.position') }}</span>
            <Select
              v-model:value="item.in"
              class="w-100"
              :options="QueryAndHeaderInOption"
              @change="handleApiKeyConfigurationChange" />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
<style scoped>

</style>
