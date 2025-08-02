import { AuthClientIn } from '@xcan-angus/infra';

export type AuthType = 'basic' | 'bearer' | 'apiKey' | 'oauth2';

export type ApiKeyIN = 'header' | 'query';

export type FlowKey = 'authorizationCode' | 'password' | 'implicit' | 'clientCredentials';

export type ApiKeyExtensionFields = {
  name: string;
  value: string;
  in: ApiKeyIN;
  nameErr: boolean;
  valueErr: boolean;
}

export type ModelObj = {
  type: AuthType;
  username: string;
  password: string;
  token: string;
  name: string;
  value: string;
  in: ApiKeyIN;
  apiKeyList: ApiKeyExtensionFields[];
  'x-xc-oauth2-newToken': boolean;
  'x-xc-oauth2-authFlow': FlowKey;
  'x-xc-oauth2-token':string;
  'x-xc-oauth2-clientAuthType': AuthClientIn;
  'x-xc-oauth2-callbackUrl': string;
  tokenUrl: string;
  refreshUrl: string;
  authorizationUrl: string;
  'x-xc-oauth2-clientId': string;
  'x-xc-oauth2-clientSecret': string;
  scopes: string[];
  usernameErr?: boolean;
  passwordErr?: boolean;
  tokenErr?: boolean;
  oauth2TokenErr?: boolean;
  refreshUrlErr: {
    isEmpty: boolean,
    isError:boolean
  };
  callbackUrlErr: {
    isEmpty: boolean,
    isError:boolean
  };
  tokenUrlErr: {
    isEmpty: boolean,
    isError:boolean
  };
  authorizationUrlErr: {
    isEmpty: boolean,
    isError:boolean
  };
  scopesErr?: boolean;
}

export type AuthConfigObj = {
  id: string;
  projectId: string;
  type: {
    value: string;
    message: string;
  },
  key: string;
  ref: string;
  model: ModelObj;
  hasModel:boolean;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  isEdit: boolean;
  isAdd: boolean;
  isExpand: boolean;
  delLoading: boolean;
  saveloading: boolean;
  keyErr: boolean;
  description?:string;
}
