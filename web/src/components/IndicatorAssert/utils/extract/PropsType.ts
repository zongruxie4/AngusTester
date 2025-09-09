import { ExtractionMethod, AuthClientIn, SecurityIn } from '@xcan-angus/infra';

// TODO 存在重复代码

export type DefaultExtraction = {
  defaultValue: string;
  expression: string;
  failureMessage: string;
  finalValue: string;
  method: ExtractionMethod;
  name: string;
  value: string;
}

export type OAuth2Flow = {
  clientId: string;
  clientIn: AuthClientIn;
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
  in: SecurityIn;
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
  type: 'APIKEY' | 'HTTP' | 'NONE' | 'OAUTH2';
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
  method: ExtractionMethod|undefined;
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
