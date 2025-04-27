export type ExtractionLocation = 'QUERY_PARAMETER' | 'PATH_PARAMETER' | 'REQUEST_HEADER' | 'FORM_PARAMETER' | 'REQUEST_RAW_BODY' | 'RESPONSE_HEADER' | 'RESPONSE_BODY';

export type ExtractMethod = 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';

export type DefaultExtraction = {
  defaultValue: string;
  expression: string;
  failureMessage: string;
  finalValue: string;
  method: ExtractMethod;
  name: string;
  value: string;
}

export type OAuth2Flow = {
  clientId: string;
  clientIn: 'BASIC_AUTH_HEADER' | 'REQUEST_BODY';
  clientSecret: string;
  extensions: { [key: string]: any };
  password: string;
  refreshUrl: string;
  scopes: string[];
  tokenUrl: string;
  username: string;
}

export type OAuth2Flows = {
  authFlow: string;
  clientCredentials: OAuth2Flow;
  extensions: { [key: string]: any };
  extraction: DefaultExtraction;
  newToken: boolean;
  password: OAuth2Flow;
  token: string;
}

export type ApiKeyRes = {
  extensions: { [key: string]: any };
  in: 'COOKIE' | 'HEADER' | 'QUERY';
  name: string;
  value: string;
}

export type SecurityResSecurityRes = {
  apiKeys: ApiKeyRes[];
  description: string;
  enabled: boolean;
  extensions: { [key: string]: any };
  name: string;
  oauth2: OAuth2Flows;
  type: 'APIKEY' | 'http' | 'NONE' | 'OAUTH2';
  value: string;
}

export type Request = {
  authentication: SecurityResSecurityRes;
  body: string;
  contentType: string;
  dynamicBody: boolean;
  dynamicParameter: boolean;
  dynamicValueRequest: boolean;
  endpoint: string
  extensions: string;
  method: string;
  parameters: string;
  requestId: string;
  server: string;
  setting: string;
  url: string;
}

export type Extraction = {
  method: ExtractMethod|undefined;
  expression: string|undefined;
  location: string|undefined;
  matchItem: string|undefined;
  apisId?: string;
  defaultValue?: string;
  failureMessage?: string;
  finalValue?: string;
  request?: Request;
  name?: string;
  parameterName?: string;
  value?: string;
}
