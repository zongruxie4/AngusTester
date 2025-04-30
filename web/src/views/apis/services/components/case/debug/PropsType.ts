import { decode as dt, encode as et } from 'js-base64';
import { ApiUtils as apiUtils } from '@xcan-angus/vue-ui';

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
export const getDefaultParams = (config = {}) => {
  return {
    name: '',
    in: 'query',
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    schema: {
      type: 'string'
    },
    ...config
  };
};

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

// 删除schame 中 得引用字段
export const travelDelSchemaRef = (schame) => {
  if (typeof schame === 'object') {
    if (schame.$ref) {
      delete schame.$ref;
    }
    if (Object.prototype.toString.call(schame) === '[object Object]') {
      Object.keys(schame).forEach(key => {
        if (key !== valueKey) {
          schame[key] = travelDelSchemaRef(schame[key]);
        }
      });
    }
    if (Object.prototype.toString.call(schame) === '[object Array]') {
      schame.forEach(item => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        item = travelDelSchemaRef(item);
      });
    }
  }
  return schame;
};

// 校验 url 正确性
export const isValidUrl = (url):boolean => {
  try {
    // eslint-disable-next-line no-new
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

// 解密
export const decode = (value: string, native = false): { name: string; value: string; } | string => {
  if (native) {
    return dt(value);
  }

  const slices = dt(value).split(':');
  return {
    name: slices[0],
    value: slices[1]
  };
};

// 加密
export const encode = (name = '', value = ''): string => {
  if (!value) {
    return et(name);
  }

  if (!name && !value) {
    return '';
  }

  return et(name + ':' + value);
};

// 将x-xc-value 的 空object  为  String 类型
export const travelEmptyObjToString = (data) => {
  if (typeof data === 'object') {
    if (JSON.stringify(data) === '{}') {
      data = '';
    }
    if (Object.prototype.toString.call(data) === '[object Object]') {
      Object.keys(data).forEach(key => {
        data[key] = travelEmptyObjToString(data[key]);
      });
    } else if (Object.prototype.toString.call(data) === '[object Array]') {
      data.forEach((i, idx) => {
        data[idx] = travelEmptyObjToString(i);
      });
    }
  }
  return data;
};

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
