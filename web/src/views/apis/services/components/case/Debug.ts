import { decode as dt, encode as et } from 'js-base64';

import apiUtils from '@/utils/apis';

export const { API_EXTENSION_KEY } = apiUtils;
const { valueKey, enabledKey } = API_EXTENSION_KEY;

export const ToolBarMenus = [
  {
    name: '基本',
    value: 'request'
  },
  {
    name: '响应',
    value: 'response'
  },
  {
    name: '耗时分析',
    value: 'time'
  },
  {
    name: 'Cookie',
    value: 'cookie'
  },
  {
    name: '断言结果',
    value: 'assert'
  }
];

const deconstruct = (data: Record<string, any>) => {
  const handler = (schema: Record<string, any>) => {
    if (Object.prototype.hasOwnProperty.call(schema, '$ref')) {
      const model = resolvedRefModels[schema.$ref];
      return handler(model);
    } else {
      for (const key in schema) {
        if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
          schema[key] = handler(schema[key]);
        }
      }
    }

    return schema;
  };

  const model = JSON.parse(data.model || 'null');
  let resolvedRefModels = data.resolvedRefModels;
  if (!resolvedRefModels) {
    return model;
  }

  resolvedRefModels = Object.entries(resolvedRefModels).reduce((prev, [key, value]) => {
    const _value = (value || 'null') as string;
    prev[key] = JSON.parse(_value);
    return prev;
  }, {});

  return handler(model);
};

export const getModelFromRef = async (serviceId, ref) => {
  const [error, resp] = await apiUtils.getModelDataByRef(serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(resp.data || {});
};

export interface ParameterItem {
  name: string;
  in?: 'header'|'path'|'query'|'cookie';
  [valueKey]: any;
  [enabledKey]: boolean;
}

export type ApiInfo = {
    apisId?: string;
    summary?: string;
    method: string;
    endpoint: string;
    serviceId?: string;
    serviceName?: string;
    parameters: ParameterItem[];
    requestBody?: {
      content?: Record<string, any>;
    },
    currentServer: {
      url: string
    },
    id?: string;
    authentication?: Record<string, any>;
    assertions?: any[];
    resolvedRefModels?: Record<string, any>;
}

export type Parameter = {
  form: { [key: string]: any };
  rawBody: { [key: string]: any };
  responseBody: {
      data: any;
      size: number;
  };
  query: { [key: string]: any };
  path: { [key: string]: any };
  header: { [key: string]: any };
  duration: number;
  responseHeader: Record<string, string>;
  status: number;
}

export const rawTypeOptions = [
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];
