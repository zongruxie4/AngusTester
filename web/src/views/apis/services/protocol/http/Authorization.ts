import { i18n } from '@xcan-angus/infra';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { API_EXTENSION_KEYS } from '@/types/openapi-types';

/**
 * Internationalization instance for translations
 */
const i18nInstance = i18n.getI18n();
const translateFunction = i18nInstance?.global?.t || ((value: string): string => value);

const { valueKey, securityApiKeyPrefix } = API_EXTENSION_KEY;

/**
 * Authentication item interface
 * <p>
 * Defines the structure for authentication configuration items including type, flows, and scheme
 * </p>
 */
export interface AuthenticationItem {
  name?: string;
  type: 'http' | 'apiKey' | 'oauth2' | 'extends' | null;
  flows?: Record<string, any>;
  in?: string;
  scheme?: string;
}

/**
 * Creates a default authentication item
 * <p>
 * Returns a default authentication item with empty values
 * </p>
 * @returns Default authentication item object
 */
export const createDefaultAuthItem = () => {
  return {
    name: '',
    in: 'header',
    [valueKey]: ''
  };
};

/**
 * Available authentication type options
 * <p>
 * Configuration options for different authentication types supported by the API
 * </p>
 */
export const authenticationTypeOptions = [
  {
    label: translateFunction('service.apiAuthorization.types.noAuth'),
    value: null
  },
  {
    label: translateFunction('service.apiAuthorization.types.basicAuth'),
    value: 'basic'
  },
  {
    label: translateFunction('service.apiAuthorization.types.bearerToken'),
    value: 'bearer'
  },
  {
    label: translateFunction('service.apiAuthorization.types.apiKey'),
    value: 'apiKey'
  },
  {
    label: translateFunction('service.apiAuthorization.types.oauth2'),
    value: 'oauth2'
  },
  {
    label: translateFunction('service.apiAuthorization.types.extends'),
    value: 'extends'
  }
];

/**
 * OAuth2 flow type options
 * <p>
 * Available OAuth2 authentication flow types for API authorization
 * </p>
 */
export const oauth2FlowTypeOptions = [
  {
    value: 'authorizationCode',
    label: translateFunction('service.apiAuthorization.flowTypes.authorizationCode')
  },
  {
    value: 'password',
    label: translateFunction('common.password')
  },
  {
    value: 'implicit',
    label: translateFunction('service.apiAuthorization.flowTypes.implicit')
  },
  {
    value: 'clientCredentials',
    label: translateFunction('service.apiAuthorization.flowTypes.clientCredentials')
  }
];

/**
 * OAuth2 field configuration labels
 * <p>
 * Configuration labels and validation rules for OAuth2 authentication fields
 * </p>
 */
export const oauth2FieldLabels = [
  {
    valueKey: 'authorizationUrl',
    label: translateFunction('service.apiAuthorization.oauth2Fields.authorizationUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: API_EXTENSION_KEYS.oAuth2CallbackUrlKey,
    label: translateFunction('service.apiAuthorization.oauth2Fields.callbackUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'tokenUrl',
    label: translateFunction('service.apiAuthorization.oauth2Fields.tokenUrl'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: 'refreshUrl',
    label: translateFunction('service.apiAuthorization.oauth2Fields.refreshUrl'),
    maxLength: 400
  },
  {
    valueKey: API_EXTENSION_KEYS.oAuth2ClientIdKey,
    label: translateFunction('service.apiAuthorization.oauth2Fields.clientId'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: API_EXTENSION_KEYS.oAuth2ClientSecretKey,
    label: translateFunction('service.apiAuthorization.oauth2Fields.clientSecret'),
    maxLength: 1024,
    required: true
  },
  {
    valueKey: API_EXTENSION_KEYS.oAuth2UsernameKey,
    label: translateFunction('common.username'),
    maxLength: 400,
    required: true
  },
  {
    valueKey: API_EXTENSION_KEYS.oAuth2PasswordKey,
    label: translateFunction('common.password'),
    maxLength: 1024,
    required: true
  },
  {
    valueKey: 'x-xc-oauth2-challengeMethod',
    label: translateFunction('service.apiAuthorization.oauth2Fields.encryptionMethod')
  },
  {
    valueKey: 'x-xc-oauth2-codeVerifier',
    label: translateFunction('service.apiAuthorization.oauth2Fields.verificationCode')
  },
  {
    valueKey: 'scopes',
    label: translateFunction('service.apiAuthorization.oauth2Fields.scope'),
    maxLength: 200
  }
];

/**
 * Encryption type options for OAuth2
 * <p>
 * Available encryption methods for OAuth2 authentication
 * </p>
 */
export const encryptionTypeOptions = [
  {
    value: 'SHA-256',
    label: 'SHA-256'
  },
  {
    value: 'Plain',
    label: 'Plain'
  }
];

/**
 * Required fields for authorization code flow
 */
export const authorizationCodeRequiredFields = [
  'authorizationUrl',
  API_EXTENSION_KEYS.oAuth2CallbackUrlKey,
  'tokenUrl',
  'refreshUrl',
  API_EXTENSION_KEYS.oAuth2ClientIdKey,
  API_EXTENSION_KEYS.oAuth2ClientSecretKey,
  'scopes'
];

/**
 * Required fields for password flow
 */
export const passwordFlowRequiredFields = [
  'tokenUrl',
  'refreshUrl',
  API_EXTENSION_KEYS.oAuth2ClientIdKey,
  API_EXTENSION_KEYS.oAuth2ClientSecretKey,
  API_EXTENSION_KEYS.oAuth2UsernameKey,
  API_EXTENSION_KEYS.oAuth2PasswordKey,
  'scopes'
];

/**
 * Required fields for implicit flow
 */
export const implicitFlowRequiredFields = [
  'authorizationUrl',
  API_EXTENSION_KEYS.oAuth2CallbackUrlKey,
  'refreshUrl',
  API_EXTENSION_KEYS.oAuth2ClientIdKey,
  API_EXTENSION_KEYS.oAuth2ClientSecretKey,
  'scopes'
];

/**
 * Required fields for client credentials flow
 */
export const clientCredentialsFlowRequiredFields = [
  'tokenUrl',
  'refreshUrl',
  API_EXTENSION_KEYS.oAuth2ClientIdKey,
  API_EXTENSION_KEYS.oAuth2ClientSecretKey,
  'scopes'
];

/**
 * Required fields for authorization code PKCE flow
 */
export const authorizationCodePKCERequiredFields = [
  'authorizationUrl',
  API_EXTENSION_KEYS.oAuth2CallbackUrlKey,
  'tokenUrl',
  'refreshUrl',
  API_EXTENSION_KEYS.oAuth2ClientIdKey,
  API_EXTENSION_KEYS.oAuth2ClientSecretKey,
  'x-xc-oauth2-challengeMethod',
  'x-xc-oauth2-codeVerifier',
  'scopes'
];

/**
 * OAuth2 flow field mappings
 * <p>
 * Maps OAuth2 flow types to their required field configurations
 * </p>
 */
export const oauth2FlowFieldMappings = {
  authorizationCode: authorizationCodeRequiredFields,
  password: passwordFlowRequiredFields,
  implicit: implicitFlowRequiredFields,
  clientCredentials: clientCredentialsFlowRequiredFields,
  authorizationCodePKCE: authorizationCodePKCERequiredFields
};

/**
 * Extracts API key authentication data from data source
 * <p>
 * Processes API key authentication data and separates it into header and query parameters
 * </p>
 * @param dataSource - Source data containing API key information
 * @returns Array containing header and query authentication data
 */
export const extractApiKeyAuthenticationData = (dataSource: any[]) => {
  const queryAuthenticationData = {};
  const queryParameterList = dataSource.filter(item => item.in === 'query');
  queryParameterList.forEach(item => {
    queryAuthenticationData[item.name] = item[valueKey];
  });

  const headerAuthenticationData = {};
  const headerParameterList = dataSource.filter(item => item.in === 'header');
  headerParameterList.forEach(item => {
    headerAuthenticationData[item.name] = item[valueKey];
  });

  return [headerAuthenticationData, queryAuthenticationData];
};

/**
 * Extracts display API key data from data source
 * <p>
 * Processes API key data for display purposes, including primary and additional keys
 * </p>
 * @param dataSource - Source data containing API key information
 * @returns Array containing header and query authentication data for display
 */
export const extractDisplayApiKeyData = (dataSource: any) => {
  const primaryKey = {
    name: dataSource.name,
    in: dataSource.in,
    [valueKey]: dataSource[valueKey]
  };
  const additionalKeys = dataSource[securityApiKeyPrefix] || [];

  const queryAuthenticationData = {};
  const queryParameterList = [primaryKey, ...additionalKeys].filter(item => item.in === 'query');
  queryParameterList.forEach(item => {
    queryAuthenticationData[item.name] = item[valueKey];
  });

  const headerAuthenticationData = {};
  const headerParameterList = [primaryKey, ...additionalKeys].filter(item => item.in === 'header');
  headerParameterList.forEach(item => {
    headerAuthenticationData[item.name] = item[valueKey];
  });

  return [headerAuthenticationData, queryAuthenticationData];
};
