import { i18n } from '@xcan-angus/infra';
import { API_EXTENSION_KEY } from '@/utils/apis';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);

// const { valueKey, securityApiKeyPrefix } = API_EXTENSION_KEY;
const { valueKey, securityApiKeyPrefix } = API_EXTENSION_KEY;

// TODO 替换类型
export interface AuthItem {
  name?: string;
  type: 'http'|'apiKey'|'oauth2'|'extends'|null;
  flows?: Record<string, any>;
  in?: string;
  scheme?: string;
}

export const getAuthItem = () => {
  return {
    name: '',
    in: 'header',
    [valueKey]: ''
  };
};

export const authTypeOptions = [
  {
    label: t('service.apiAuthorization.types.noAuth'),
    value: null
  },
  {
    label: t('service.apiAuthorization.types.basicAuth'),
    value: 'basic'
  },
  {
    label: t('service.apiAuthorization.types.bearerToken'),
    value: 'bearer'
  },
  {
    label: t('service.apiAuthorization.types.apiKey'),
    value: 'apiKey'
  },
  {
    label: t('service.apiAuthorization.types.oauth2'),
    value: 'oauth2'
  },
  {
    label: t('service.apiAuthorization.types.extends'),
    value: 'extends'
  }
];

export const flowAuthType = [
  {
    value: 'authorizationCode',
    label: t('service.apiAuthorization.flowTypes.authorizationCode')
  },
  {
    value: 'password',
    label: t('common.password')
  },
  {
    value: 'implicit',
    label: t('service.apiAuthorization.flowTypes.implicit')
  },
  {
    value: 'clientCredentials',
    label: t('service.apiAuthorization.flowTypes.clientCredentials')
  }
];

export const authLabels = [
  {
    valueKey: 'authorizationUrl',
    label: t('service.apiAuthorization.oauth2Fields.authorizationUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-callbackUrl',
    label: t('service.apiAuthorization.oauth2Fields.callbackUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'tokenUrl',
    label: t('service.apiAuthorization.oauth2Fields.tokenUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'refreshUrl',
    label: t('service.apiAuthorization.oauth2Fields.refreshUrl'),
    maxLength: 400
  },
  {
    valueKey: 'x-xc-oauth2-clientId',
    label: t('service.apiAuthorization.oauth2Fields.clientId'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-clientSecret',
    label: t('service.apiAuthorization.oauth2Fields.clientSecret'),
    maxLength: 1024,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-username',
    label: t('common.username'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-password',
    label: t('common.password'),
    maxLength: 1024,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-challengeMethod',
    label: t('service.apiAuthorization.oauth2Fields.encryptionMethod')
  },
  {
    valueKey: 'x-xc-oauth2-codeVerifier',
    label: t('service.apiAuthorization.oauth2Fields.verificationCode')
  },
  {
    valueKey: 'scopes',
    label: t('service.apiAuthorization.oauth2Fields.scope'),
    maxLength: 200
  }
];

export const encryptionTypeOpt = [
  {
    value: 'SHA-256',
    label: 'SHA-256'
  },
  {
    value: 'Plain',
    label: 'Plain'
  }
];

export const authorizationCode = [
  'authorizationUrl',
  'x-xc-oauth2-callbackUrl',
  'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'scopes'
];

export const password = [
  'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'x-xc-oauth2-username',
  'x-xc-oauth2-password',
  'scopes'
];

export const implicit = [
  'authorizationUrl',
  'x-xc-oauth2-callbackUrl',
  // 'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'scopes'
];

export const clientCredentials = [
  'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'scopes'
];

export const authorizationCodePKCE = [
  'authorizationUrl',
  'x-xc-oauth2-callbackUrl',
  'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'x-xc-oauth2-challengeMethod',
  'x-xc-oauth2-codeVerifier',
  'scopes'
];

export const flowAuthKeys = {
  authorizationCode,
  password,
  implicit,
  clientCredentials,
  authorizationCodePKCE
};

export const getApiKeyData = (dataSource) => {
  const queryAuth = {};
  const queryList = dataSource.filter(item => item.in === 'query');
  queryList.forEach(item => {
    queryAuth[item.name] = item[valueKey];
  });
  const headerAuth = {};
  const headerList = dataSource.filter(item => item.in === 'header');
  headerList.forEach(item => {
    headerAuth[item.name] = item[valueKey];
  });
  return [headerAuth, queryAuth];
};

export const getShowApiKeyData = (dataSource) => {
  // const { extensions } = dataSource;
  const first = { name: dataSource.name, in: dataSource.in, [valueKey]: dataSource[valueKey] };
  const others = dataSource[securityApiKeyPrefix] || [];
  const queryAuth = {};
  const queryList = [first, ...others].filter(item => item.in === 'query');
  queryList.forEach(item => {
    queryAuth[item.name] = item[valueKey];
  });
  const headerAuth = {};
  const headerList = [first, ...others].filter(item => item.in === 'header');
  headerList.forEach(item => {
    headerAuth[item.name] = item[valueKey];
  });
  return [headerAuth, queryAuth];
};
