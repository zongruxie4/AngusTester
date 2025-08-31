import { HttpMethod } from '@xcan-angus/infra';

export type HttpServer = {
    id: string;
    url: string;
    variables?: any,
    description?: string;
    'x-xc-serverSource'?: string;
    '"x-xc-id"'?: string;
}

export type RequestConfigs = {
    method: HttpMethod;
    url: string;
    server: {
        url: string;
        variables?: {
            [key: string]: {
                defaultValue: string;
                allowableValues: string[];
                description?: string;
            }
        };
        description?: string;
    };
    endpoint: string;
    parameters: {
        name: string;
        in: ParameterIn;
        value: string;
        enabled: boolean;
        type: 'string';
    }[];
    body: {
        format: string;
        contentEncoding: ContentEncoding;
        forms: {
            name: string;
            description: string;
            enabled: string;
            type: string;
            format: string;
            contentType: string;
            contentEncoding: ContentEncoding;
            fileName: string;
            value: string;
        }[];
        rawContent: string;
        fileName: string;
    };
    authentication: {
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
}

export type RequestBodyFormItem = {
  id: string;
  name: string;
  type: 'string' | 'number' | 'integer' | 'boolean' | 'array' | 'file' | 'file(array)';
  description?: string;
  enabled?: boolean;
  format?: 'binary' | 'string';
  contentType?: string;
  contentEncoding?: 'gzip_base64';
  fileName?: string;
  value?: string;
}

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

export type RequestBody = {
  forms: RequestBodyFormItem[];
  rawContent: string;
  type: 'string' | 'number' | 'integer' | 'boolean';
  format: 'string' | 'binary';
  contentEncoding: 'gzip_base64';
  fileName: string;
};

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
