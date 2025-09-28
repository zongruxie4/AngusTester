// Infrastructure imports
import { i18n } from '@xcan-angus/infra';

// Local utility imports
import { API_EXTENSION_KEY, encode } from '@/utils/apis/index';

// Get internationalization function with fallback
const getTranslationFunction = i18n.getI18n()?.global?.t || ((value: string) => value);
const t = getTranslationFunction;

// API extension key constants
export const { valueKey, securityApiKeyPerfix, oAuth2Key, oAuth2Token, newTokenKey } = API_EXTENSION_KEY;
// Authentication type definitions
export type AuthenticationType = 'http' | 'apiKey' | 'oauth2' | 'extends' | null;

// Main authentication item interface
export interface AuthenticationItem {
  name?: string;
  type: AuthenticationType;
  flows?: Record<string, any>;
  in?: string;
  scheme?: string;
  $ref?: string;
  [valueKey]?: string;
}

// Legacy export alias for backward compatibility
export type AuthItem = AuthenticationItem;

// API key placement options configuration
export const apiKeyPlacementOptions = [
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

// Legacy export alias for backward compatibility
export const inOpt = apiKeyPlacementOptions;

/**
 * Create a default authentication item for API key configuration
 * @returns Default authentication item object
 */
export const createDefaultAuthenticationItem = (): AuthenticationItem => {
  return {
    name: '',
    type: 'apiKey',
    in: 'header',
    [valueKey]: ''
  };
};

// Legacy export alias for backward compatibility
export const getAuthItem = createDefaultAuthenticationItem;

// Authentication type options configuration
export const authenticationTypeOptions = [
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

// Legacy export alias for backward compatibility
export const authTypeOptions = authenticationTypeOptions;

// OAuth2 client authentication method options
export const oauth2ClientAuthenticationOptions = [
  {
    label: t('xcan_apiAuthencation.sendViaBasicAuthHeader'),
    value: 'BASIC_AUTH_HEADER'
  },
  {
    label: t('xcan_apiAuthencation.sendViaRequestBody'),
    value: 'REQUEST_BODY'
  }
];

// Legacy export alias for backward compatibility
export const authInOpt = oauth2ClientAuthenticationOptions;

// OAuth2 flow type options configuration
export const oauth2FlowTypeOptions = [
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
  //   label: 'Authorization Code PKCE Mode'
  // }
];

// Legacy export alias for backward compatibility
export const flowAuthType = oauth2FlowTypeOptions;

// OAuth2 field labels configuration
export const oauth2FieldLabels = [
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

// Legacy export alias for backward compatibility
export const authLabels = oauth2FieldLabels;

// OAuth2 encryption method options configuration
export const oauth2EncryptionMethodOptions = [
  {
    value: 'SHA-256',
    label: 'SHA-256'
  },
  {
    value: 'Plain',
    label: 'Plain'
  }
];

// Legacy export alias for backward compatibility
export const encryptionTypeOpt = oauth2EncryptionMethodOptions;

// OAuth2 flow field configurations
export const authorizationCodeFlowFields = [
  'authorizationUrl',
  'x-xc-oauth2-callbackUrl',
  'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'scopes'
];

export const passwordFlowFields = [
  'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'x-xc-oauth2-username',
  'x-xc-oauth2-password',
  'scopes'
];

export const implicitFlowFields = [
  'authorizationUrl',
  'x-xc-oauth2-callbackUrl',
  // 'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'scopes'
];

export const clientCredentialsFlowFields = [
  'tokenUrl',
  'refreshUrl',
  'x-xc-oauth2-clientId',
  'x-xc-oauth2-clientSecret',
  'scopes'
];

export const authorizationCodePKCEFlowFields = [
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

// OAuth2 flow field keys mapping
export const oauth2FlowFieldKeys = {
  authorizationCode: authorizationCodeFlowFields,
  password: passwordFlowFields,
  implicit: implicitFlowFields,
  clientCredentials: clientCredentialsFlowFields,
  authorizationCodePKCE: authorizationCodePKCEFlowFields
};

// Legacy export aliases for backward compatibility
export const authorizationCode = authorizationCodeFlowFields;
export const password = passwordFlowFields;
export const implicit = implicitFlowFields;
export const clientCredentials = clientCredentialsFlowFields;
export const authorizationCodePKCE = authorizationCodePKCEFlowFields;
export const flowAuthKeys = oauth2FlowFieldKeys;

/**
 * Process API key data from data source and separate by placement type
 * @param dataSource - Array of API key items
 * @returns Array containing header and query authentication objects
 */
export const processApiKeyData = (dataSource: any[]): [Record<string, string>, Record<string, string>] => {
  const queryAuth: Record<string, string> = {};
  const queryList = dataSource.filter(item => item.in === 'query');
  queryList.forEach(item => {
    queryAuth[item.name] = item[valueKey];
  });
  
  const headerAuth: Record<string, string> = {};
  const headerList = dataSource.filter(item => item.in === 'header');
  headerList.forEach(item => {
    headerAuth[item.name] = item[valueKey];
  });
  
  return [headerAuth, queryAuth];
};

/**
 * Process API key data for display purposes from data source
 * @param dataSource - API key data source object
 * @returns Array containing header and query authentication objects
 */
export const processApiKeyDataForDisplay = (dataSource: any): [Record<string, string>, Record<string, string>] => {
  const first = { name: dataSource.name, in: dataSource.in, [valueKey]: dataSource[valueKey] };
  const others = dataSource[securityApiKeyPerfix] || [];
  
  const queryAuth: Record<string, string> = {};
  const queryList = [first, ...others].filter(item => item.in === 'query');
  queryList.forEach(item => {
    queryAuth[item.name] = item[valueKey];
  });
  
  const headerAuth: Record<string, string> = {};
  const headerList = [first, ...others].filter(item => item.in === 'header');
  headerList.forEach(item => {
    headerAuth[item.name] = item[valueKey];
  });
  
  return [headerAuth, queryAuth];
};

/**
 * Process authentication data for display purposes based on authentication type
 * @param dataSource - Authentication data source object
 * @returns Array containing processed authentication data
 */
export const processAuthenticationDataForDisplay = async (dataSource: any): Promise<any[]> => {
  switch (dataSource.type) {
    case 'http':
      if (dataSource?.scheme === 'basic') {
        const { name = '' } = dataSource || {};
        const value = (dataSource || {})?.[valueKey] || '';
        const variableRegReplace = /\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g;

        if (!name.match(variableRegReplace) && !value.match(variableRegReplace)) {
          return [{ Authorization: 'Basic ' + encode(name, value) }];
        }
      }
      return [{
        Authorization: dataSource[valueKey]
      }];
    case 'apiKey':
      return processApiKeyDataForDisplay(dataSource);
    case 'extends':
      return [{}];
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
      } else {
        return [{}];
      }
    default:
      return [{}];
  }
};

// Legacy export aliases for backward compatibility
export const getApiKeyData = processApiKeyData;
export const getShowApiKeyData = processApiKeyDataForDisplay;
export const getShowAuthData = processAuthenticationDataForDisplay;

export default {
  getApiKeyData: processApiKeyData,
  getShowApiKeyData: processApiKeyDataForDisplay,
  getShowAuthData: processAuthenticationDataForDisplay
};
