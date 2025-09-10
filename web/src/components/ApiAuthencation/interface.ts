import { API_EXTENSION_KEY, encode } from '@/utils/ApiUtils/index';
import { i18n } from '@xcan-angus/infra';
const t = i18n.getI18n()?.global?.t || ((value: string) => value);

// const { valueKey, securityApiKeyPerfix } = API_EXTENSION_KEY;
const { valueKey, securityApiKeyPerfix, oAuth2Key, oAuth2Token, newTokenKey, basicAuthKey } = API_EXTENSION_KEY;
export interface AuthItem {
  name?: string;
  type: 'http'|'apiKey'|'oauth2'|'extends'|null;
  flows?: Record<string, any>;
  in?: string;
  scheme?: string;
  $ref?: string;
  [valueKey]?: string
}

export const inOpt = [
  {
    value: 'query',
    label: 'query'
  },
  {
    value: 'header',
    label: 'header'
  }
  // {
  //   value: 'cookie',
  //   label: 'cookie'
  // }
];

export const getAuthItem = () => {
  return {
    name: '',
    in: 'header',
    [valueKey]: ''
  };
};

export const authTypeOptions = [
  {
    label: t('xcan_apiAuthencation.noAuth'),
    value: null
  },
  {
    label: 'Basic Auth',
    value: 'basic'
  },
  {
    label: 'Bearer Token',
    value: 'bearer'
  },
  {
    label: 'Api Key',
    value: 'apiKey'
  },
  {
    label: 'OAuth 2.0',
    value: 'oauth2'
  },
  // {
  //   label: 'openIdConnect',
  //   value: 'openIdConnect'
  // },
  {
    label: t('xcan_apiAuthencation.public'),
    value: 'extends'
  }
];

export const authInOpt = [
  {
    label: t('xcan_apiAuthencation.sendViaBasicAuthHeader'),
    value: 'BASIC_AUTH_HEADER'
  },
  {
    label: t('xcan_apiAuthencation.sendViaRequestBody'),
    value: 'REQUEST_BODY'
  }
];

export const flowAuthType = [
  {
    value: 'authorizationCode',
    label: t('xcan_apiAuthencation.authorizationCodeMode')
  },
  {
    value: 'password',
    label: t('xcan_apiAuthencation.passwordMode')
  },
  {
    value: 'implicit',
    label: t('xcan_apiAuthencation.implicitMode')
  },
  {
    value: 'clientCredentials',
    label: t('xcan_apiAuthencation.clientCredentialsMode')
  }
  // {
  //   value: 'authorizationCodePKCE',
  //   label: '授权码模式-带PKCE（Authorization Code PKCE）'
  // }
];

// authorizationUrl 授权URL
// tokenUrl 访问令牌URL
// refreshUrl 刷新令牌URL
// scopes 访问范围

// x-xc-oauth2-clientId
// x-xc-oauth2-clientSecret
// x-xc-oauth2-callbackUrl
// x-xc-oauth2-username
// x-xc-oauth2-password

export const authLabels = [
  {
    valueKey: 'authorizationUrl',
    label: t('xcan_apiAuthencation.authorizationUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-callbackUrl',
    label: t('xcan_apiAuthencation.callbackUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'tokenUrl',
    label: t('xcan_apiAuthencation.accessTokenUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'refreshUrl',
    label: t('xcan_apiAuthencation.refreshTokenUrl'),
    maxLength: 400
  },
  {
    valueKey: 'x-xc-oauth2-clientId',
    label: t('xcan_apiAuthencation.clientId'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-clientSecret',
    label: t('xcan_apiAuthencation.clientSecret'),
    maxLength: 1024,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-username',
    label: t('xcan_apiAuthencation.username'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-password',
    label: t('xcan_apiAuthencation.password'),
    maxLength: 1024,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-challengeMethod',
    label: t('xcan_apiAuthencation.encryptionMethod')
  },
  {
    valueKey: 'x-xc-oauth2-codeVerifier',
    label: t('xcan_apiAuthencation.verificationCode')
  },
  {
    valueKey: 'scopes',
    label: t('xcan_apiAuthencation.scope'),
    maxLength: 200
  }
  // {
  //   valueKey: 'state',
  //   label: '状态'
  // }
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

const getApiKeyData = (dataSource) => {
  // const { extensions } = dataSource;
  // const first = { name: dataSource.name, in: dataSource.in, [valueKey]: dataSource[valueKey] };
  // const others = dataSource[securityApiKeyPerfix] || [];
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

const getShowApiKeyData = (dataSource) => {
  // const { extensions } = dataSource;
  const first = { name: dataSource.name, in: dataSource.in, [valueKey]: dataSource[valueKey] };
  const others = dataSource[securityApiKeyPerfix] || [];
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

const getShowAuthData = async (dataSource) => {
  switch (dataSource.type) {
    case 'http':
      if (dataSource?.scheme === 'basic') {
        // if (dataSource[basicAuthKey]) {
        //   const { name } = dataSource[basicAuthKey] || {};
        //   // eslint-disable-next-line prefer-regex-literals
        //   const value = (dataSource[basicAuthKey] || {})?.[valueKey];
        //   const variableRegReplace = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g);
        //   if (!name.match(variableRegReplace) && !value.match(variableRegReplace)) {
        //     return 'Basic ' + encode(name, value);
        //   }
        // }
        const { name = '' } = dataSource || {};
        // eslint-disable-next-line prefer-regex-literals
        const value = (dataSource || {})?.[valueKey] || '';
        const variableRegReplace = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g);

        if (!name.match(variableRegReplace) && !value.match(variableRegReplace)) {
          return [{ Authorization: 'Basic ' + encode(name, value) }];
        }
      }
      return [{
        Authorization: dataSource[valueKey]
      }];
    case 'apiKey':
      return getShowApiKeyData(dataSource);
    case 'extends':
      return [{
        // Authorization: dataSource.scheme
      }];
    case 'oauth2':
      if (dataSource.flows) {
        if (dataSource[newTokenKey]) {
          if (dataSource.flows[dataSource[oAuth2Key]]) {
            const oauth2Data = dataSource.flows[dataSource[oAuth2Key]];
            if (oauth2Data[oAuth2Token]) {
              return [{ access_token: dataSource[oAuth2Token] }];
            }
          }
        } else if (dataSource[oAuth2Token]) {
          return [{ access_token: dataSource[oAuth2Token] }];
        }
        return [{}];
        // oauthKey.value = 2;
        // const flowAuthKey = Object.keys(dataSource.flows)[0];
        // oauthData['x-xc-oauth2-clientAuthType'] = dataSource.flows?.[authType.value]?.extensions?.['x-xc-oauth2-clientAuthType'];
        // flowAuthKeys[authType.value].forEach(i => {
        //   if (i.includes('x-xc-')) {
        //     oauthData[i] = dataSource.flows?.[flowAuthKey].extensions?.[i];
        //   } else {
        //     oauthData[i] = dataSource.flows?.[flowAuthKey][i];
        //   }
        // });
        // authType.value = dataSource.extensions?.['x-xc-flowAuthKey'] || 'authorizationCode';
      } else {
        return [{}];
      }
      break;
  }
};

export {
  getApiKeyData,
  getShowApiKeyData,
  getShowAuthData
};

export default {
  getApiKeyData,
  getShowApiKeyData,
  getShowAuthData
};
