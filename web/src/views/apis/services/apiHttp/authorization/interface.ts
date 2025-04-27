import { API_EXTENSION_KEY } from '@/views/apis/utils';

// const { valueKey, securityApiKeyPerfix } = API_EXTENSION_KEY;
const { valueKey, securityApiKeyPerfix, oAuth2Key, oAuth2Token, newTokenKey } = API_EXTENSION_KEY;
export interface AuthItem {
  name?: string;
  type: 'http'|'apiKey'|'oauth2'|'extends'|null;
  flows?: Record<string, any>;
  in?: string;
  scheme?: string;
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
    label: '不认证',
    value: null
  },
  {
    label: 'basic Auth',
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
    label: '公共',
    value: 'extends'
  }
];

export const authInOpt = [
  {
    label: '通过Basic认证头发送',
    value: 'BASIC_AUTH_HEADER'
  },
  {
    label: '通过请求体发送',
    value: 'REQUEST_BODY'
  }
];

export const flowAuthType = [
  {
    value: 'authorizationCode',
    label: '授权码模式（Authorization Code）'
  },
  {
    value: 'password',
    label: '密码模式（Password Credentials）'
  },
  {
    value: 'implicit',
    label: '隐式模式（Implicit）'
  },
  {
    value: 'clientCredentials',
    label: '客户端模式（Client Credentials））'
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
    label: '授权URL',
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-callbackUrl',
    label: '回调URL',
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'tokenUrl',
    label: '访问令牌URL',
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'refreshUrl',
    label: '刷新令牌URL',
    maxLength: 400
  },
  {
    valueKey: 'x-xc-oauth2-clientId',
    label: '客户端ID',
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-clientSecret',
    label: '客户端密钥',
    maxLength: 1024,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-username',
    label: '用户名',
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-password',
    label: '密码',
    maxLength: 1024,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-challengeMethod',
    label: '加密方式'
  },
  {
    valueKey: 'x-xc-oauth2-codeVerifier',
    label: '验证码'
  },
  {
    valueKey: 'scopes',
    label: 'Scope',
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

export const getApiKeyData = (dataSource) => {
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

export const getShowApiKeyData = (dataSource) => {
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

export const getShowAuthData = async (dataSource) => {
  switch (dataSource.type) {
    case 'http':
      return [{
        Authorization: dataSource[valueKey]
      }];
    case 'apiKey':
      return getShowApiKeyData(dataSource);
    case 'extends':
      return [{
        Authorization: dataSource.scheme
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
        return [{
          Authorization: dataSource['x-xc-oauth2-token']
        }];
      }
      break;
  }
};
