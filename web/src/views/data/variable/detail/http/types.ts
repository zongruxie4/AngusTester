import { AuthClientIn, ContentEncoding, HttpMethod, ParameterIn } from '@xcan-angus/infra';

// TODO 重写整个模块，并将Http相关类型提成公共 - 柳小龙

export type ContentType = null |
  'application/x-www-form-urlencoded' |
  'multipart/form-data' |
  'application/json' |
  'text/html' |
  'application/xml' |
  'application/javascript' |
  'text/plain' |
  'application/octet-stream' |
  '*/*';

export interface Parameter {
  name: string;
  enabled: boolean;
  disabled: boolean;
  id: string;
  value: string;
  type: 'string';
  in: ParameterIn;
}

/**
 * OpenAPI server
 */
export type OASServer = {
  id?: string;
  url: string;
  description?: string;
  variables?: {
    [key: string]: {
      default: string;
      description: string;
      enum: string[];
    }
  };
  'x-xc-serverSource'?: string;
  '"x-xc-id"'?: string;
}

export type Server = {
  url: string;
  variables?: {
    [key: string]: {
      defaultValue: string;
      allowableValues: string[];
      description?: string;
    }
  };
  description?: string;
}

export type Authentication = {
  type: string;
  enabled: boolean;
  'x-xc-value': string;
  'x-scheme': string;
  $ref?: string;
}

export type RequestBodyFormItem = {
  id: string;
  name: string;
  type: 'string' | 'number' | 'integer' | 'boolean' | 'array' | 'file' | 'file(array)';
  description?: string;
  enabled?: boolean;
  format?: 'binary' | 'string';
  contentType?: string;
  contentEncoding?: ContentEncoding;
  fileName?: string;
  value?: string;
}

export type RequestBody = {
  $ref?: string;
  description?: string;
  forms?: RequestBodyFormItem[];
  rawContent?: string;
  type?: 'string' | 'number' | 'integer' | 'boolean';
  format?: 'string' | 'binary';
  contentEncoding?: ContentEncoding;
  fileName?: string;
  content?: {
        [key: string]: {
            schema: { [key: string]: any };
            exampleSetFlag: boolean;
            'x-xc-value': string;
        }
    };
  required?: boolean;
}

export type auth = {
  type: string;
  name: string;
  enabled: boolean;
  description: string;
  value: string;
  apiKeys: {
    name: string;
    in: string;
    value: string;
  }[];
  oauth2: {
    clientCredentials: {
      tokenUrl: string;
      refreshUrl: string;
      scopes: string[];
      clientId: string;
      clientSecret: string;
      clientIn: AuthClientIn;
      username: string;
      password: string;
    };
    password: {
      tokenUrl: string;
      refreshUrl: string;
      scopes: string[];
      clientId: string;
      clientSecret: string;
      clientIn: AuthClientIn;
      username: string;
      password: string;
    };
    authFlow: 'clientCredentials' | 'password';
    newToken: boolean;
    token: string;
  };
};

export type RequestConfig = {
  method: HttpMethod;
  url: string;
  server: Server;
  endpoint: string;
  parameters: Parameter[];
  body?: RequestBody;
  authentication: Authentication;
}

export const deepParseJson = (jsonStr: string) => {
  try {
    const result = JSON.parse(jsonStr);
    if (typeof result === 'string') {
      return deepParseJson(result);
    }
    return result;
  } catch {
    return jsonStr;
  }
};
